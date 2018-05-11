# MorseKeyboard
I thought it would be cool to have an input method on Android where you enter Morse code instead of using a normal keyboard. At the very least, it would be a way to learn Morse code. However, searches for Morse keyboards all turned up keyboards with two buttons (gasp!) one for dit and one for dah. This is silly and not the point of a time-dependent encoding.

As the saying goes, if you want something done right, install Android Studio, learn Java, and spend hours searching through documentation to produce what turns out to be a keyboard that's really hard to use.

## Inspiration
 1. [penti keyboard](https://software-lab.de/penti.html)
 2. [MorseKeyboard](https://software-lab.de/penti.html)
 3. [dotdash-keyboard-android](https://software-lab.de/penti.html)

## Installation
### With Android Studio
The easiest way is to enable developer mode on your phone and running the code. Go to *Settings > About phone* and tap the Build number seven times. Going back to the Settings screen, there sholud now be *Developer options* somewhere near the bottom.

Connect your phone via USB, then click the Run button in Android Studio. This will build and install the keyboard.

You can select input methods in *Settings > Language and input*.

### Without Android Studio
There are .apk files in `MorseKeyboard/app/build/output/apk` which I took out of my `.gitignore` for anyone who doesn't have Android Studio. To install an apk file, you need to allow unknown sources *Settings > Security*`by checking the Unknown Sources" box.

At this point you should be able to download the file from your phone or copy it to your phone from a computer. Opening the file should allow you to install it.

You can then select the input method in *Settings > Language and input*

#### quick updates
```bash
adb uninstall com.example.chris.morsekeyboard &&
 adb install ./app/build/outputs/apk/app-debug.apk &&
 adb shell ime enable com.example.chris.morsekeyboard/.MorseIME
```
## Usage
Tap Morse code into the foo button. This keyboard uses the international variant of morse code including punctuation.

A dit or "." is currently set to .17 seconds. This will be configurable in a later update. A dah, dash, or, "-" is 3 dit periods. Dits and dahs are separated by the period of one dit. Letters are separated by 3 dit periods and words are separated by 5 dit periods.

The spacebar, delete, and shift work about how you would expect. If you have a partially entered letter, they will cancel it as well.
## Miscellaneous
The characters ".", ",", ";", ":", "!", and "?" will be placed before a preceding space. Currently it does not handle multiple spaces.

## Planned Features
- Prosigns to implement behavior such as "repeat last word", "end transmission" (close keyboard and send), and newline
- [Abbreviations](https://en.wikipedia.org/wiki/Morse_code_abbreviations), possibly with configuration options
- I don't know why holding the delete key doesn't make it repeat. I set isRepeatable to true.

## Possible Features
I will probably never get to these but they'd be cool.
- Automatic timing detection
- Non-English characters
- Caps lock

## Known Bugs
- The shift indicator light doesn't update when it switches back to lower case
- There is probably a lot of weird edge case bugs for when shift is pressed before a word timeout
