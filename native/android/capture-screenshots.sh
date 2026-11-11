#!/usr/bin/env bash
set -euo pipefail

mkdir -p "${RUNNER_TEMP}/android-screenshots"
APK_PATH="$(ls native/android/app/build/outputs/apk/debug/*.apk | head -n 1)"
echo "Installing APK: ${APK_PATH}"
test -f "${APK_PATH}"
adb install -r "${APK_PATH}"

adb shell settings put global sysui_demo_allowed 1 || true
adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 0941 || true
adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false || true
adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 -e mobile show -e datatype none -e level 4 || true
adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false || true

# 1) Capture Light Mode Home
adb shell cmd uimode night no || true
adb shell am start -W -S -n com.lexicon.debug/com.lexicon.MainActivity --es lexora_theme light --es lexora_tab home
sleep 6
adb shell pidof com.lexicon.debug || {
  echo "ERROR: com.lexicon.debug crashed! Dumping AndroidRuntime logcat:"
  adb logcat -d -s AndroidRuntime:E Lexicon:D DEBUG:* | tail -n 120 || true
  exit 1
}
adb exec-out screencap -p > "${RUNNER_TEMP}/android-screenshots/android-home-light.png"

# 2) Capture Dreamy Dark Mode Home
adb shell cmd uimode night yes || true
adb shell am start -W -S -n com.lexicon.debug/com.lexicon.MainActivity --es lexora_theme dark --es lexora_tab home
sleep 6
adb shell pidof com.lexicon.debug
adb exec-out screencap -p > "${RUNNER_TEMP}/android-screenshots/android-home-dark.png"

# 3) Capture Word Detail Sheet via live C++ engine lookup intent ("Wanderlust") in Dark Mode
adb shell am start -W -S -n com.lexicon.debug/com.lexicon.MainActivity --es lexora_theme dark --es lexora_word Wanderlust
sleep 5
adb shell pidof com.lexicon.debug
adb exec-out screencap -p > "${RUNNER_TEMP}/android-screenshots/android-word-detail-dark.png"

ls -lh "${RUNNER_TEMP}/android-screenshots/"
