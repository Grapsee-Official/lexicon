#!/usr/bin/env bash
set -euo pipefail

mkdir -p "${RUNNER_TEMP}/ios-screenshots"
APP_PATH="$(find "${RUNNER_TEMP}/DerivedData" -name 'Lexicon.app' -type d | head -n 1)"
echo "Resolved Lexicon.app: ${APP_PATH}"
test -n "${APP_PATH}"

BUNDLE_ID="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleIdentifier' "${APP_PATH}/Info.plist")"
echo "Resolved CFBundleIdentifier: ${BUNDLE_ID}"

# Ad-hoc sign with entitlements preserved
codesign --force --deep --sign - --timestamp=none "${APP_PATH}"

# Prefer iOS 18.x / 17.x Simulator runtime first (boots in ~8s instead of iOS 26 beta's 5m Data Migration!)
SIM_UDID="$(xcrun simctl list devices available -j | python3 -c '
import json, sys
data = json.load(sys.stdin)["devices"]
candidates = []
for rt, devs in data.items():
    if "iOS" not in rt:
        continue
    # Prefer stable iOS 18 / 17 over slow beta 26 if present
    rt_bonus = 20 if ("18" in rt or "17" in rt) else 10
    for d in devs:
        if d.get("isAvailable") and "iPhone" in d.get("name", ""):
            dev_score = 3 if ("Pro" in d["name"] and "Max" not in d["name"]) else (2 if "iPhone 16" in d["name"] or "iPhone 15" in d["name"] else 1)
            candidates.append((rt_bonus, dev_score, rt, d["udid"], d["name"]))
candidates.sort(reverse=True)
if candidates:
    print(candidates[0][3])
')"
echo "Selected Simulator UDID: ${SIM_UDID}"
test -n "${SIM_UDID}"

xcrun simctl boot "${SIM_UDID}" || true
xcrun simctl bootstatus "${SIM_UDID}" -b

# Give SpringBoard & LaunchServices 5s to settle after boot
sleep 5

xcrun simctl status_bar "${SIM_UDID}" override \
  --time "9:41" \
  --dataNetwork wifi \
  --wifiMode active \
  --wifiBars 3 \
  --cellularMode active \
  --cellularBars 4 \
  --batteryState charged \
  --batteryLevel 100 || true

xcrun simctl install "${SIM_UDID}" "${APP_PATH}"

capture_mode() {
  local mode="$1"
  local extra_flag="$2"
  local extra_val="$3"
  local out_file="$4"

  xcrun simctl ui "${SIM_UDID}" appearance "${mode}"

  # Start temporary log stream for Lexicon + SpringBoard crash reason
  LOG_FILE="${RUNNER_TEMP}/sim-log-${mode}-${extra_val}.txt"
  xcrun simctl spawn "${SIM_UDID}" log stream --level info --style compact \
    --predicate 'process == "Lexicon" OR (process == "SpringBoard" AND eventMessage CONTAINS "Lexicon") OR (process == "ReportCrash")' \
    > "${LOG_FILE}" 2>&1 &
  STREAM_PID=$!

  local attempt=1
  local launched_ok=0
  while [ "${attempt}" -le 3 ]; do
    echo "Attempt ${attempt}: Launching ${BUNDLE_ID} (-lexoraTheme ${mode} ${extra_flag} ${extra_val})..."
    LAUNCH_OUT="$(xcrun simctl launch --terminate-running-process "${SIM_UDID}" "${BUNDLE_ID}" -lexoraTheme "${mode}" "${extra_flag}" "${extra_val}" 2>&1)" || true
    echo "Launch output: ${LAUNCH_OUT}"
    APP_PID="$(echo "${LAUNCH_OUT}" | awk -F': ' '{print $2}' | tr -d '[:space:]')"
    sleep 5

    if [ -n "${APP_PID}" ] && kill -0 "${APP_PID}" 2>/dev/null; then
      echo "SUCCESS: ${BUNDLE_ID} is alive at PID ${APP_PID}!"
      launched_ok=1
      break
    fi
    echo "Process ${APP_PID:-unknown} not running yet; retrying in 3s..."
    sleep 3
    attempt=$((attempt + 1))
  done

  kill "${STREAM_PID}" 2>/dev/null || true

  if [ "${launched_ok}" -ne 1 ]; then
    echo "=== Simulator Log Stream Diagnostics ==="
    cat "${LOG_FILE}" | tail -n 120 || true
    exit 1
  fi

  xcrun simctl io "${SIM_UDID}" screenshot "${out_file}"
  xcrun simctl terminate "${SIM_UDID}" "${BUNDLE_ID}" || true
  sleep 1
}

capture_mode light -lexoraTab home "${RUNNER_TEMP}/ios-screenshots/ios-home-light.png"
capture_mode dark -lexoraTab home "${RUNNER_TEMP}/ios-screenshots/ios-home-dark.png"
capture_mode dark -lexoraWord Wanderlust "${RUNNER_TEMP}/ios-screenshots/ios-word-detail-dark.png"

ditto -c -k --sequesterRsrc --keepParent "${APP_PATH}" "${RUNNER_TEMP}/Lexora-iOS-Simulator.app.zip"
ls -lh "${RUNNER_TEMP}/ios-screenshots/"
