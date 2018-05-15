all:
	./gradlew compileDebugSources 
	adb uninstall com.example.chris.dit_dah_ring 
	adb install ./app/build/outputs/apk/debug/app-debug.apk
	adb shell ime enable com.example.chris.dit_dah_ring/.MorseIME
