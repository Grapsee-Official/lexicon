#!/usr/bin/env bash
# Native QA evidence only. Internal debug APK; never signs or publishes a release.
set -euo pipefail

mkdir -p qa-captures/android
PYTHON="${RUNNER_TEMP}/lexicon-qa-venv/bin/python"
test -x "$PYTHON" || { echo "::error::Screenshot validator is not installed"; exit 1; }

timeout 120 adb install -r native/android/app/build/outputs/apk/debug/app-debug.apk
adb shell settings put system font_scale 1.0
adb shell wm size 1080x2400
adb shell wm density 420
adb shell settings put system accelerometer_rotation 0

for theme in light dark; do
  for screen in home dictionary_home word_of_the_day word_detail collections my_notes study_lists quick_templates search_discovery search_results expanded_exploration more translations library library_article; do
    output="qa-captures/android/$theme-$screen.png"
    valid=0
    for attempt in 1 2 3; do
      # On an emulator ANR, dismiss any stale OS panel then restart the
      # application for a true frame, rather than uploading an invalid image.
      if [ "$attempt" -gt 1 ]; then
        adb shell input keyevent KEYCODE_BACK || true
        adb shell am force-stop com.google.android.apps.nexuslauncher || true
      fi
      adb shell am force-stop com.lexicon.debug || true
      timeout 45 adb shell am start -W -n com.lexicon.debug/com.lexicon.MainActivity --es lexora_theme "$theme" --es lexicon_screen "$screen"
      sleep 5
      if ! timeout 15 adb shell pidof com.lexicon.debug >/dev/null; then
        echo "::warning::Lexora process is absent on $theme/$screen attempt $attempt"
        continue
      fi
      if ! timeout 30 adb exec-out screencap -p > "$output"; then
        echo "::warning::ADB screenshot failed: $output"
        rm -f "$output"
        continue
      fi
      if "$PYTHON" scripts/check_capture_quality.py "$output"; then
        valid=1
        break
      fi
      echo "::warning::Android $theme/$screen capture failed visual-presence validation on attempt $attempt"
    done
    if [ "$valid" -ne 1 ]; then
      echo "::error::Android $theme/$screen could not be captured without OS dialogs"
      adb shell dumpsys window | grep -E 'mCurrentFocus|mFocusedApp' | head -6 || true
      adb logcat -d -t 250 | grep -E 'ANR in|FATAL EXCEPTION|AndroidRuntime|com.lexicon' | tail -55 || true
      exit 1
    fi
    echo "Verified native Android screenshot: $output"
  done
done
count=$(find qa-captures/android -type f -name '*.png' | wc -l)
test "$count" -eq 30
echo "Captured $count validated native Android screens without publication."
