#!/usr/bin/env bash
set -euo pipefail

mkdir -p "${RUNNER_TEMP}/ios-screenshots"
APP_PATH="$(find "${RUNNER_TEMP}/DerivedData" -name 'Lexicon.app' -type d | head -n 1)"
echo "Resolved Lexicon.app: ${APP_PATH}"
test -n "${APP_PATH}"

echo "=== Mach-O Load Commands Check ==="
otool -l "${APP_PATH}/Lexicon" | grep -A 5 "LC_BUILD_VERSION" || true

BUNDLE_ID="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleIdentifier' "${APP_PATH}/Info.plist")"
echo "Resolved CFBundleIdentifier: ${BUNDLE_ID}"

codesign --force --deep --sign - --timestamp=none "${APP_PATH}"

SIM_UDID="$(xcrun simctl list devices available -j | python3 -c '
import json, sys
data = json.load(sys.stdin)["devices"]
candidates = []
for rt, devs in data.items():
    if "iOS" not in rt:
        continue
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
sleep 4

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
  local self_out="${RUNNER_TEMP}/self-$(basename "${out_file}")"

  rm -f "${self_out}" "${out_file}"
  xcrun simctl ui "${SIM_UDID}" appearance "${mode}"

  echo "Launching ${BUNDLE_ID} (-lexoraTheme ${mode} ${extra_flag} ${extra_val})..."
  LAUNCH_OUT="$(xcrun simctl launch --terminate-running-process "${SIM_UDID}" "${BUNDLE_ID}" \
    -lexoraTheme "${mode}" "${extra_flag}" "${extra_val}" -lexoraSnapshotOut "${self_out}" 2>&1)" || {
    echo "simctl launch failed: ${LAUNCH_OUT}"
    exit 1
  }
  echo "Launch output: ${LAUNCH_OUT}"

  # Wait up to 12 seconds for the app's native UIWindow snapshot file to be written
  local waited=0
  while [ "${waited}" -lt 12 ] && [ ! -s "${self_out}" ]; do
    sleep 1
    waited=$((waited + 1))
  done

  if [ ! -s "${self_out}" ]; then
    echo "ERROR: Native SwiftUI window did not write ${self_out}! Checking Simulator logs & CrashReports..."
    ls -lt ~/Library/Logs/DiagnosticReports/ 2>/dev/null | head -n 10 || true
    LATEST_IPS="$(ls -t ~/Library/Logs/DiagnosticReports/*Lexicon* 2>/dev/null | head -n 1 || true)"
    if [ -n "${LATEST_IPS}" ]; then
      cat "${LATEST_IPS}" | head -n 140 || true
    fi
    xcrun simctl spawn "${SIM_UDID}" log show --last 45s --style compact \
      --predicate 'process == "Lexicon" OR eventMessage CONTAINS "Lexicon" OR eventMessage CONTAINS "com.lexicon.app"' \
      2>/dev/null | tail -n 80 || true
    exit 1
  fi

  # Capture full simulator screen (includes iOS 9:41 status bar + home indicator) now that we KNOW the app window rendered!
  xcrun simctl io "${SIM_UDID}" screenshot "${out_file}"
  echo "Captured ${out_file} ($(wc -c < "${out_file}") bytes) + verified window render (${self_out})"
  xcrun simctl terminate "${SIM_UDID}" "${BUNDLE_ID}" || true
  sleep 1
}

capture_mode light -lexoraTab home "${RUNNER_TEMP}/ios-screenshots/ios-home-light.png"
capture_mode dark -lexoraTab home "${RUNNER_TEMP}/ios-screenshots/ios-home-dark.png"
capture_mode dark -lexoraWord Wanderlust "${RUNNER_TEMP}/ios-screenshots/ios-word-detail-dark.png"

ditto -c -k --sequesterRsrc --keepParent "${APP_PATH}" "${RUNNER_TEMP}/Lexora-iOS-Simulator.app.zip"
ls -lh "${RUNNER_TEMP}/ios-screenshots/"
