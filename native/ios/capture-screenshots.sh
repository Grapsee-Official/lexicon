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
xcrun simctl ui "${SIM_UDID}" appearance light

# Launch Lexicon ONCE to render & capture all 10 Light + 2 Dark screens directly from the live iOS Simulator UIWindow (~10s total)!
xcrun simctl launch --terminate-running-process "${SIM_UDID}" "${BUNDLE_ID}" \
  -lexiconSnapshotAllDir "${RUNNER_TEMP}/ios-screenshots"

waited=0
while [ "${waited}" -lt 45 ] && [ ! -f "${RUNNER_TEMP}/ios-screenshots/_DONE" ]; do
  sleep 1
  waited=$((waited + 1))
done

test -f "${RUNNER_TEMP}/ios-screenshots/_DONE"
rm -f "${RUNNER_TEMP}/ios-screenshots/_DONE"

xcrun simctl terminate "${SIM_UDID}" "${BUNDLE_ID}" || true
ditto -c -k --sequesterRsrc --keepParent "${APP_PATH}" "${RUNNER_TEMP}/Lexicon-iOS-Simulator.app.zip"
ls -lh "${RUNNER_TEMP}/ios-screenshots/"
