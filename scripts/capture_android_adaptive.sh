#!/usr/bin/env bash
# Test the SAME Android application on compact phones and larger displays.
# Uses only emulator display configuration: never creates a separate project.
set -euo pipefail
PYTHON="${RUNNER_TEMP}/lexicon-qa-venv/bin/python"
test -x "$PYTHON"

restore_screen() {
  adb shell wm size 1080x2400 || true
  adb shell wm density 420 || true
  adb shell settings put system font_scale 1.0 || true
}
trap restore_screen EXIT

for layout in compact tablet; do
  case "$layout" in
    compact)
      # 720 physical pixels / 2.25x density = 320dp logical viewport.
      adb shell wm size 720x1600
      adb shell wm density 360
      adb shell settings put system font_scale 1.0
      ;;
    tablet)
      # 1600 physical pixels / 2x density = 800dp logical viewport.
      adb shell wm size 1600x2560
      adb shell wm density 320
      adb shell settings put system font_scale 1.3
      ;;
  esac
  outdir="qa-captures/android-adaptive/$layout"
  mkdir -p "$outdir"
  for theme in light dark; do
    for screen in home dictionary_home word_detail collections my_notes more library_article; do
      output="$outdir/$theme-$screen.png"
      valid=0
      for attempt in 1 2 3; do
        if [ "$attempt" -gt 1 ]; then
          adb shell input keyevent KEYCODE_BACK || true
          adb shell am force-stop com.google.android.apps.nexuslauncher || true
        fi
        adb shell am force-stop com.lexicon.debug || true
        adb shell am start -W -n com.lexicon.debug/com.lexicon.MainActivity \
          --es lexora_theme "$theme" --es lexicon_screen "$screen"
        sleep 4
        adb exec-out screencap -p > "$output"
        if "$PYTHON" scripts/check_capture_quality.py "$output"; then
          valid=1
          break
        fi
        echo "::warning::Adaptive $layout/$theme/$screen attempt $attempt invalid"
      done
      if [ "$valid" -ne 1 ]; then
        echo "::error::Adaptive $layout/$theme/$screen failed: missing UI or OS dialog"
        exit 1
      fi
      echo "Verified responsive $layout/$theme/$screen"
    done
  done
  count=$(find "$outdir" -maxdepth 1 -name '*.png' | wc -l)
  test "$count" -eq 14
done
echo "Validated 28 additional phone/tablet layout captures in the original app."
