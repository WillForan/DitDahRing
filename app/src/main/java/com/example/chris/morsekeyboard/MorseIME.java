package com.example.chris.morsekeyboard;

/**
 * Morse code IME
 */

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard;
import android.os.Handler;
import android.os.Vibrator;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import java.util.Timer;
import java.util.TimerTask;


public class MorseIME extends InputMethodService
    implements KeyboardView.OnKeyboardActionListener{
    private Keyboard keyboard;
    private long ditTime = 120000000; // time of a dit in nanoseconds, space between dit/dahs, 1/3 dah, 1/3 space between letters, 1/5 space between words
    // wikipedia says 50 ms for good people. TODO make this a setting
    private long ditMillis = ditTime/1000000;
    private int currentLetter;  // horrendous implementation; each digit 0 for unused, 1 for dit, 2 for dah
                                // ex: 12 is A
                                // long to not mess up on excessive dits/dahs
    private int nextPressAction = 0; // 0 for new dit/dah within letter, 1 for new letter within word, 2 for new word

    private SparseArray<String> morse = new SparseArray<String>() {{
        put(12,"a");
        put(2111,"b");
        put(2121,"c");
        put(211,"d");
        put(1,"e");
        put(1121,"f");
        put(221,"g");
        put(1111,"h");
        put(11,"i");
        put(1222,"j");
        put(212,"k");
        put(1211,"l");
        put(22,"m");
        put(21,"n");
        put(222,"o");
        put(1221,"p");
        put(2212,"q");
        put(121,"r");
        put(111,"s");
        put(2,"t");
        put(112,"u");
        put(1112,"v");
        put(122,"w");
        put(2112,"x");
        put(2122,"y");
        put(2211,"z");
        put(12222,"1");
        put(11222,"2");
        put(11122,"3");
        put(11112,"4");
        put(11111,"5");
        put(21111,"6");
        put(22111,"7");
        put(22211,"8");
        put(22221,"9");
        put(22222,"0");
        put(121212,".");
        put(221122,",");
        put(112211,"?");
        put(122221,"'");
        put(212122,"!");
        put(21121,"/");
        put(21221,"(");
        put(212212,")");
        put(12111,"&");
        put(222111,":");
        put(212121,";");
        put(21112,"=");
        put(12121,"+");
        put(211112,"-");
        put(112212,"_");
        put(121121,"\"");
        put(1112112,"$");
        put(122121,"@");
    }};

    private static final String punctuation = ".,:;!?"; // punctuation that usually has no space before and space after
    private static final String capitalizeAfter = ".!?"; // punctuation after which you use a capital letter

    private Handler wordHandler = new Handler();

    private void wordStart()    {
        letterStart();
    }

    private void letterStart()  {
        //initialize letter variables
        currentLetter = 0;
        getCurrentInputConnection().commitText(" ", 1); // put a space here because ditDahStart blindly updates the character before the cursor
        ditDahStart();
    }

    private void ditDahStart()  {
        //dit/dah detection timing
        nextPressAction = 0; // next action will be the same letter still, unless changed later
        currentLetter = currentLetter * 10 + 1; //append an assumed dit to the current letter
        String currentLetterString = morse.get(currentLetter,"");
        if(keyboard.isShifted() == true) currentLetterString = currentLetterString.toUpperCase();
        InputConnection ic = getCurrentInputConnection();
        ic.deleteSurroundingText(1, 0);
        ic.commitText(currentLetterString,1);
        wordHandler.postDelayed(ditPressTimeout, 2 * ditMillis); //cancelled by onRelease to finish dit
    }

    private Runnable ditPressTimeout = new Runnable() {
        @Override
        public void run() {
            //optional vibration feedback when you've passed max dit time
            //Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            //v.vibrate(10);

            //correct to dah
            currentLetter = currentLetter+1;
            String currentLetterString = morse.get(currentLetter,"");
            if(keyboard.isShifted() == true) currentLetterString = currentLetterString.toUpperCase();
            InputConnection ic = getCurrentInputConnection();
            ic.deleteSurroundingText(1, 0); // delete the letter in output we're working on ...
            ic.commitText(currentLetterString,1); // ... and replace it with the corrected one
            wordHandler.postDelayed(longPressTimeout, 8 * ditMillis); //cancelled by a release, total of 10 dit hold ditPressTimeout + longPressTimeout
        }
    };

    private Runnable longPressTimeout = new Runnable()    {
        //long hold to cancel current letter (and therefore not a dah)
        //delete letter, send error vibration feedback, wait for release
        @Override
        public void run()   {
            getCurrentInputConnection().deleteSurroundingText(1, 0);
            // todo send error feedback
            nextPressAction = 1; // next press will start a new letter without leaving the word
        }
    };

    private void spaceStart()   {
        wordHandler.postDelayed(letterSpaceTimeout, 2 * ditMillis); //cancelled by a press
    }

    private Runnable letterSpaceTimeout = new Runnable()    {
        // the space is too long to stay in the current letter
        @Override
        public void run()   {
            nextPressAction = 1;
            wordHandler.postDelayed(wordSpaceTimeout, 2 * ditMillis); //cancelled by a press, total of 4 dits letterSpaceTimeout + wordSpaceTimeout
            InputConnection ic = getCurrentInputConnection();
            String currentLetterString = morse.get(currentLetter,"");
            if(keyboard.isShifted())    {
                currentLetterString = currentLetterString.toUpperCase();
                keyboard.setShifted(false); // Finished capitalizing, now next letter is lower case
            }
            if(punctuation.contains(currentLetterString))  {
                if(ic.getTextBeforeCursor(2, 0).charAt(0) == ' ')  {
                    ic.deleteSurroundingText(2, 0);
                    ic.commitText(currentLetterString, 1); // punctuation goes before space
                } else  {
                    ic.commitText(currentLetterString, 1);
                }
            }
            if(capitalizeAfter.contains(currentLetterString))  {
                keyboard.setShifted(true);
            }
        }
    };

    private Runnable wordSpaceTimeout = new Runnable() {
        // the space is too long to stay in the current word
        @Override
        public void run() {
            nextPressAction = 2;
            getCurrentInputConnection().commitText(" ", 1);
        }
    };

    @Override
    public View onCreateInputView() {
        KeyboardView kv;
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.onebutton);
        keyboard.setShifted(true);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        kv.setPreviewEnabled(false);
        return kv;
    }

    /* Really cool prosign feedback when you open the keyboard. Also super obnoxious.
    @Override
    public void onWindowShown() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0,150,50,50,50,150}; // "K" invitation to transmit prosign
        v.vibrate(pattern,-1);
    }
    */

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        // delete lives here because it repeats and messes stuff up
        if(primaryCode == Keyboard.KEYCODE_DELETE) {
            getCurrentInputConnection().deleteSurroundingText(1, 0);
            //getCurrentInputConnection().commitText("foo",1);
            nextPressAction = 1;
        }
    }

    @Override
    public void onPress(int primaryCode) {
        wordHandler.removeCallbacks(letterSpaceTimeout);
        wordHandler.removeCallbacks(wordSpaceTimeout);
        if(primaryCode == KeyEvent.KEYCODE_SPACE) {
            getCurrentInputConnection().commitText(" ", 1);
            nextPressAction = 2; // next morse key press is going to be a new word
        } else if(primaryCode == Keyboard.KEYCODE_SHIFT) { // TODO: should this cancel a letter or finish the current one?
            keyboard.setShifted(!keyboard.isShifted());
            // TODO I don't think I actually do capital letters yet.
        } else if(primaryCode != Keyboard.KEYCODE_DELETE) { // delete is done in onKey because it repeats
                                                         // must be the morse key after this point

            switch (nextPressAction)    {
                case 0: ditDahStart();
                    break;
                case 1: letterStart();
                    break;
                case 2: wordStart();
                    break;
            }
        }
    }

    @Override
    public void onRelease(int primaryCode) {
        wordHandler.removeCallbacks(ditPressTimeout);
        wordHandler.removeCallbacks(longPressTimeout);
        if(primaryCode == 4) {
            spaceStart();
        }
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
        //send message or something?
    }

    @Override
    public void swipeUp() {
    }
}