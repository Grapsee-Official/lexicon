#!/usr/bin/env bash
set -euo pipefail

mkdir -p "${RUNNER_TEMP}/android-screenshots"
APK_PATH="$(ls native/android/app/build/outputs/apk/debug/*.apk | head -n 1)"
echo "Installing APK: ${APK_PATH}"
test -f "${APK_PATH}"
adb install -r "${APK_PATH}"

# Suppress background system ANR / crash dialogs from Pixel Launcher during boot
adb shell settings put global hide_error_dialogs 1 || true
adb shell settings put secure anr_show_background 0 || true
adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS || true
sleep 3

adb shell settings put global sysui_demo_allowed 1 || true
adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 0941 || true
adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false || true
adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 -e mobile show -e datatype none -e level 4 || true
adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false || true

# Cold-start Lexicon ONCE; singleTop MainActivity receives subsequent intents via onNewIntent in ~0.3s!
adb shell cmd uimode night no || true
adb shell am start -W -S -n com.lexicon.debug/com.lexicon.MainActivity \
  --es lexora_theme light --es lexicon_screen 1 >/dev/null
sleep 4

# Dismiss any residual boot ANR dialog if one appeared
if adb shell dumpsys window | grep -q "Application Not Responding"; then
  adb shell input keyevent 4 || true
  sleep 1
fi

capture_android() {
  local theme="$1"
  local screen="$2"
  local out_name="$3"

  adb shell am start -W -n com.lexicon.debug/com.lexicon.MainActivity \
    --es lexora_theme "${theme}" --es lexicon_screen "${screen}" >/dev/null
  sleep 1.4
  adb shell pidof com.lexicon.debug >/dev/null || {
    echo "ERROR: com.lexicon.debug crashed on screen=${screen}! Dumping AndroidRuntime logcat:"
    adb logcat -d -s AndroidRuntime:E Lexicon:D DEBUG:* | tail -n 120 || true
    exit 1
  }
  adb exec-out screencap -p > "${RUNNER_TEMP}/android-screenshots/${out_name}"
  echo "Captured ${out_name}"
}

capture_android light 1  "android-01-dictionary-home-light.png"
capture_android light 2  "android-02-word-of-the-day-light.png"
capture_android light 3  "android-03-word-detail-light.png"
capture_android light 4  "android-04-collections-light.png"
capture_android light 5  "android-05-my-notes-light.png"
capture_android light 6  "android-06-study-lists-light.png"
capture_android light 7  "android-07-quick-templates-light.png"
capture_android light 8  "android-08-search-discovery-light.png"
capture_android light 9  "android-09-search-results-light.png"
capture_android light 10 "android-10-expanded-exploration-light.png"
capture_android dark  1  "android-01-dictionary-home-dark.png"
capture_android dark  3  "android-03-word-detail-dark.png"

ls -lh "${RUNNER_TEMP}/android-screenshots/"
