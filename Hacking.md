# Hacking
## Generic Android Development
  1. Enable developer mode on your phone. *Settings > About phone* and tap the Build number seven times. You'll see a Toast message if successful
  2. Connect your phone via USB after enabling "USB debugging".  Be sure to click the smaller notification to change "Use USB" from "charging" to "file transfer"
  4. Open and build (with Android Studio) and install (see below)
  3. Allow the virtual keyboard. See input methods in *Settings > Language and input*.

## Quick Updates
Each update requires re-enabling the keyboard. `abd` can do this.

```bash
./gradlew compileDebugSources # if not using android studio to build
adb uninstall com.example.chris.dit_dah_ring &&
 adb install ./app/build/outputs/apk/app-debug.apk &&
 adb shell ime enable com.example.chris.dit_dah_ring/.MorseIME
```

## Implementation

### On the shoulders of

  * `MorseIME.java` is based on [MorseKeyboard](https://github.com/gitonwithit/MorseKeyboard.git). 
  * `ClearView.java` and `Touchable.java`  were created by referencing the [penti keyboard](https://software-lab.de/penti.html). 

### Outline

  * `MoreseIME`  is the entry point. It initializes `ClearView` and assigns `ClearView.Ime` which passes to a `Touchable` object so it can implement key presses.
  * `ClearView.java` implements a view to override `ondraw` and `ontouchevent`. `postInvalidate` is done here.
  * `Touchable.java` has the bulk of the code and functionality. It 
    * needs a reference to Ime that comes ultimately from `MorseIME.` 
    * what Touchable tries to draw will only be pushed to the screen if `postInvalidate` is called (likely from `ClearView::onDraw`)
    * holds a Handle+Runnable "timer" that will send dits and dahs to be translated into letters after some inactivity
  * `MorseCode.java` implements the translations from dits and dahs to letters.


