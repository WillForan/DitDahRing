#!/usr/bin/env bash
set -euo pipefail

adb uninstall com.example.chris.dit_dah_ring  || echo "not already installed"
adb install ./app/build/outputs/apk/debug/app-debug.apk 
adb shell ime disable com.example.chris.dit_dah_ring/.MorseIME || :
adb shell ime enable com.example.chris.dit_dah_ring/.MorseIME || :
adb shell ime enable com.example.chris.dit_dah_ring/.MorseIME || :
