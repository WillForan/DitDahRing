# MorseKeyboard
I thought it would be cool to have an input method on Android where you enter Morse code instead of using a normal keyboard. At the very least, it would be a way to learn Morse code. However, searches for Morse keyboards all turned up keyboards with two buttons (gasp!) one for dit and one for dah. This is silly and not the point of a time-dependent encoding.

As the saying goes, if you want something done right, install Android Studio, learn Java, and spend hours searching through documentation to produce what turns out to be a keyboard that's really hard to use.

## Installation
I'll get to this when I know what I should say.

If you have Android Studio, enable developer mode on your phone, connect it via USB, and hit the Run button and select "Choose running device" which will install it. Select the "MorseIME" input method in your settings.

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
