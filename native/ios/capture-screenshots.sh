#!/usr/bin/env bash
set -euo pipefail

mkdir -p "${RUNNER_TEMP}/ios-screenshots"
APP_PATH="$(find "${RUNNER_TEMP}/DerivedData" -name 'Lexicon.app' -type d | head -n 1)"
echo "Resolved Lexicon.app: ${APP_PATH}"
test -n "${APP_PATH}"

echo "=== Info.plist inside built Lexicon.app ==="
plutil -p "${APP_PATH}/Info.plist" || true
BUNDLE_ID="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleIdentifier' "${APP_PATH}/Info.plist")"
echo "Resolved CFBundleIdentifier: ${BUNDLE_ID}"

# Ad-hoc sign so Simulator AMFI never rejects unsigned Mach-O on Xcode 26+
codesign --force --deep --sign - "${APP_PATH}"

SIM_UDID="$(xcrun simctl list devices available -j | python3 -c '
import json, sys
data = json.load(sys.stdin)["devices"]
candidates = []
for rt, devs in data.items():
    if "iOS" not in rt: continue
    for d in devs:
        if d.get("isAvailable") and "iPhone" in d.get("name", ""):
            score = (2 if "Pro" in d["name"] and "Max" not in d["name"] else (1 if "iPhone 16" in d["name"] or "iPhone 15" in d["name"] else 0))
            candidates.append((rt, score, d["udid"], d["name"]))
candidates.sort(reverse=True)
print(candidates[0][2] if candidates else "")
')"
echo "Selected Simulator UDID: ${SIM_UDID}"
test -n "${SIM_UDID}"

xcrun simctl boot "${SIM_UDID}" || true
xcrun simctl bootstatus "${SIM_UDID}" -b

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
  local tab="$2"
  local out_file="$3"

  xcrun simctl ui "${SIM_UDID}" appearance "${mode}"
  echo "Launching ${BUNDLE_ID} (${mode}, ${tab})..."
  LAUNCH_OUT="$(xcrun simctl launch "${SIM_UDID}" "${BUNDLE_ID}" -lexoraTheme "${mode}" -lexoraTab "${tab}" 2>&1)" || {
    echo "simctl launch failed: ${LAUNCH_OUT}"
    exit 1
  }
  echo "${LAUNCH_OUT}"
  APP_PID="$(echo "${LAUNCH_OUT}" | awk -F': ' '{print $2}' | tr -d '[:space:]')"
  sleep 6

  if ! kill -0 "${APP_PID}" 2>/dev/null; then
    echo "ERROR: ${BUNDLE_ID} (PID ${APP_PID}) crashed or exited! Recent CrashReporter logs:"
    ls -lt ~/Library/Logs/DiagnosticReports/ 2>/dev/null | head -n 10 || true
    LATEST_IPS="$(ls -t ~/Library/Logs/DiagnosticReports/*Lexicon* 2>/dev/null | head -n 1 || true)"
    if [ -n "${LATEST_IPS}" ]; then
      head -n 120 "${LATEST_IPS}" || true
    fi
    exit 1
  fi

  echo "Verified ${BUNDLE_ID} is running cleanly at PID ${APP_PID}!"
  xcrun simctl io "${SIM_UDID}" screenshot "${out_file}"
  xcrun simctl terminate "${SIM_UDID}" "${BUNDLE_ID}" || true
  sleep 1
}

capture_mode light home "${RUNNER_TEMP}/ios-screenshots/ios-home-light.png"
capture_mode dark home "${RUNNER_TEMP}/ios-screenshots/ios-home-dark.png"
capture_mode dark dictionary "${RUNNER_TEMP}/ios-screenshots/ios-dictionary-dark.png"

ditto -c -k --sequesterRsrc --keepParent "${APP_PATH}" "${RUNNER_TEMP}/Lexora-iOS-Simulator.app.zip"
ls -lh "${RUNNER_TEMP}/ios-screenshots/"
