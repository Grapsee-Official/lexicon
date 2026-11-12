#!/usr/bin/env bash
set -euo pipefail

mkdir -p "${RUNNER_TEMP}/ios-screenshots"
APP_PATH="$(find "${RUNNER_TEMP}/DerivedData" -name 'Lexicon.app' -type d | head -n 1)"
echo "Resolved Lexicon.app: ${APP_PATH}"
test -n "${APP_PATH}"

BUNDLE_ID="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleIdentifier' "${APP_PATH}/Info.plist")"
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
sleep 3

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

capture_ios() {
  local mode="$1"
  local screen="$2"
  local out_name="$3"
  local out_file="${RUNNER_TEMP}/ios-screenshots/${out_name}"
  local self_out="${RUNNER_TEMP}/self-${out_name}"

  rm -f "${self_out}" "${out_file}"
  xcrun simctl ui "${SIM_UDID}" appearance "${mode}"

  LAUNCH_OUT="$(xcrun simctl launch --terminate-running-process "${SIM_UDID}" "${BUNDLE_ID}" \
    -lexoraTheme "${mode}" -lexiconScreen "${screen}" -lexoraSnapshotOut "${self_out}" 2>&1)" || {
    echo "simctl launch failed on screen=${screen}: ${LAUNCH_OUT}"
    exit 1
  }

  local waited=0
  while [ "${waited}" -lt 20 ] && [ ! -s "${self_out}" ]; do
    sleep 0.5
    waited=$((waited + 1))
  done

  if [ ! -s "${self_out}" ]; then
    echo "ERROR: Native SwiftUI window did not write ${self_out} on screen=${screen}!"
    LATEST_IPS="$(ls -t ~/Library/Logs/DiagnosticReports/*Lexicon* 2>/dev/null | head -n 1 || true)"
    if [ -n "${LATEST_IPS}" ]; then
      head -n 120 "${LATEST_IPS}" || true
    fi
    exit 1
  fi

  xcrun simctl io "${SIM_UDID}" screenshot "${out_file}"
  echo "Captured ${out_name}"
  xcrun simctl terminate "${SIM_UDID}" "${BUNDLE_ID}" || true
}

# Capture all 10 Countryside Light Mode screens + Nocturnal Dark Mode verification!
capture_ios light 1  "ios-01-dictionary-home-light.png"
capture_ios light 2  "ios-02-word-of-the-day-light.png"
capture_ios light 3  "ios-03-word-detail-light.png"
capture_ios light 4  "ios-04-collections-light.png"
capture_ios light 5  "ios-05-my-notes-light.png"
capture_ios light 6  "ios-06-study-lists-light.png"
capture_ios light 7  "ios-07-quick-templates-light.png"
capture_ios light 8  "ios-08-search-discovery-light.png"
capture_ios light 9  "ios-09-search-results-light.png"
capture_ios light 10 "ios-10-expanded-exploration-light.png"
capture_ios dark  1  "ios-01-dictionary-home-dark.png"
capture_ios dark  3  "ios-03-word-detail-dark.png"

ditto -c -k --sequesterRsrc --keepParent "${APP_PATH}" "${RUNNER_TEMP}/Lexicon-iOS-Simulator.app.zip"
ls -lh "${RUNNER_TEMP}/ios-screenshots/"
