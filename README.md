# Dit Dah Ring
A fullscreen mostly transparent Morse Code keyboard for android frankensteined from other keyboards.

![termux](imgs/darkbg.png?raw=True)
![gmail](imgs/whitebg_ditdah.png?raw=True)
![dictionary](imgs/push_and_dict.png?raw=True)

## Inspiration
 1. [MorseKeyboard](https://github.com/gitonwithit/MorseKeyboard.git) - initial base, stole timing code
 1. [penti keyboard](https://software-lab.de/penti.html) - stole keyboard as overlay, ring buttons
 3. [dotdash-keyboard-android](https://github.com/agwells/dotdash-keyboard-android) - stole popup character dictionary

## Building and Hacking
see [Hacking.md](./Hacking.md)

## Installation
If you don't want to compile yourself
1. allow unknown sources *Settings > Security* by checking the Unknown Sources" box.
2. download and run (on phone) this [apk](./app/build/outputs/apk/debug/app-debug.apk?raw=True)

You can then select the input method in *Settings > Language and input*

## Usage
Tap Morse code into the ring.
 * A dit or "." is currently set to 100 milliseconds. 
 * A dah, dash, or, "-" is 3 dit periods. 
 * Letters are separated by 3 dit periods. 
 * long press to clear

Swipe from the ring to outside it.
 * Left to backspace
 * Right for space or tab if shift is on. If timer is disabled, right swipe will make a letter of the current Morse Code stack.
 * Up to enable shift. 
 * *TODO* down to hide keyboard (or control if shift is on?)

Tap anywhere else quickly
 * quick press to see MC -> letter dictionary
 * long press to toggle letter timer


## Known Issues
- When keyboard is active, nothing else is clickable and autocompletions don't seem to work (e.g. addresses in gmail)
- entering a new line (`._._` -> `\n`) does not send a message.
- MC stack visualization is not cleared by timer sending letter.
- No configuration
- Toast messages do not clear. if overlap, only first is seen.
- Toast message for disabled timer sent on release. could be confusing
- Help dict is dimensions are hardcoded.
