#!/usr/bin/env bash
# Evidence only: never signs or publishes an APK.
set -euo pipefail
mkdir -p qa-captures/android
adb install -r native/android/app/build/outputs/apk/debug/app-debug.apk
adb shell settings put system font_scale 1.0
adb shell wm size 1080x2400
adb shell wm density 420
adb shell settings put system accelerometer_rotation 0

for theme in light dark; do
  for screen in home dictionary_home word_of_the_day word_detail collections my_notes study_lists quick_templates search_discovery search_results expanded_exploration; do
    adb shell am force-stop com.lexicon.debug || true
    adb shell am start -W -n com.lexicon.debug/com.lexicon.MainActivity --es lexora_theme "$theme" --es lexicon_screen "$screen"
    sleep 4
    if ! adb shell pidof com.lexicon.debug >/dev/null; then
      echo "Lexicon crashed during $theme/$screen"
      adb logcat -d -t 350 | grep -E 'FATAL EXCEPTION|AndroidRuntime|com.lexicon' | tail -55 || true
      exit 1
    fi
    output="qa-captures/android/$theme-$screen.png"
    adb exec-out screencap -p > "$output"
    test "$(wc -c < "$output")" -gt 20000
    echo "Native Android screenshot: $output"
  done
done
count=$(find qa-captures/android -type f -name '*.png' | wc -l)
test "$count" -eq 22
echo "Captured $count native Android screens without publication."
