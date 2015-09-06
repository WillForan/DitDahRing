package com.example.chris.morsekeyboard;

/**
 * Morse code IME
 */

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard;
import android.os.Handler;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import java.util.Timer;
import java.util.TimerTask;


public class MorseIME extends InputMethodService
    implements KeyboardView.OnKeyboardActionListener{
    private Keyboard keyboard;
    private long pressTime;
    private long releaseTime;
    private long ditTime = 150000000; // time of a dit in nanoseconds, space between dit/dahs, 1/3 dah, 1/3 space between letters, 1/5 space between words
    // wikipedia says 50 ms for good people. TODO make this a setting
    private boolean newEntry; // used for starting new input to ignore preceding space
    private int currentLetter;  // horrendous implementation; each digit 0 for unused, 1 for dit, 2 for dah
                                // ex: 12 is A
                                // long to not mess up on excessive dits/dahs



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
    // TODO prosigns
    private static final String punctuation = ".,:;!?"; // punctuation that shouldn't be after a space.
    private static final String capitalizeAfter = ".!?";

    private Handler wordHandler = new Handler();
    private Runnable wordTimeout = new Runnable() {
        @Override
        public void run() {
            InputConnection ic = getCurrentInputConnection();
            android.util.Log.d("wordHandler", "new word timeout");
            if(releaseTime-pressTime < 2 * ditTime) { // previous was a dit
                currentLetter = currentLetter * 10 + 1;
            } else { // previous was a dah
                currentLetter = currentLetter * 10 + 2;
            }
            letterCommit(currentLetter, true);
            currentLetter = 0;
            newEntry = true;
        }
    };

    public void letterCommit(int letterKey, boolean newWord) {
        InputConnection ic = getCurrentInputConnection();
        if(morse.get(letterKey) == null) {
            //TODO call error haptics
            android.util.Log.d("letterCommit","Letter failed.");
        } else {
            String letterString = morse.get(letterKey);
            if (keyboard.isShifted()) {
                letterString = letterString.toUpperCase();
                keyboard.setShifted(false);
            }
            if (punctuation.contains(letterString)) { // put punctuation before a space
                if (" ".equals(ic.getTextBeforeCursor(1, 0))) {
                    ic.deleteSurroundingText(1, 0);
                    ic.commitText(letterString + " ", 1);
                } else {
                    ic.commitText(letterString + " ", 1);
                }
            } else {
                if (newWord) {
                    ic.commitText(letterString + " ", 1);
                } else {
                    ic.commitText(letterString, 1);
                }
            }
            if (capitalizeAfter.contains(letterString)) {
                keyboard.setShifted(true);
            }
        }
    }

    @Override
    public View onCreateInputView() {
        KeyboardView kv;

        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.onebutton);
        keyboard.setShifted(true);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        kv.setPreviewEnabled(false);
        newEntry = true;
        return kv;
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        android.util.Log.d("onKey", String.valueOf(primaryCode));
        if(primaryCode == KeyEvent.KEYCODE_DEL) {
            android.util.Log.d("onKey", "delete");
            InputConnection ic = getCurrentInputConnection();
            ic.deleteSurroundingText(1, 0);
            wordHandler.removeCallbacks(wordTimeout);
            currentLetter = 0;
            newEntry = true;
        }
        if(primaryCode==KeyEvent.KEYCODE_SHIFT_LEFT) {
            keyboard.setShifted(!keyboard.isShifted());
        }
    }

    @Override
    public void onPress(int primaryCode) {
        if(primaryCode == KeyEvent.KEYCODE_SPACE) {
            getCurrentInputConnection().commitText(" ", 1);
            wordHandler.removeCallbacks(wordTimeout);
            currentLetter = 0;
            newEntry = true;
        } else if(primaryCode == Keyboard.KEYCODE_SHIFT) { // TODO: should this cancel a letter?
            wordHandler.removeCallbacks(wordTimeout);      // TODO: should this even cancel the word handler?
        } else if(primaryCode != KeyEvent.KEYCODE_DEL) { // delete is done in onKey because it repeats
                                                         // Only the letter button after this point
            wordHandler.removeCallbacks(wordTimeout);
            if(newEntry) {
                pressTime = System.nanoTime();
                newEntry = false;
            } else {
                long prevPressTime = pressTime;
                pressTime = System.nanoTime();
                if (pressTime - prevPressTime < 3 * ditTime) { // previous press was a dit
                    currentLetter = currentLetter * 10 + 1;
                } else if(pressTime - prevPressTime < 5 * ditTime) { //  previous dah+short pause or previous dit+letter separation pause
                    if(pressTime - releaseTime < 2 * ditTime) { // previous dah+short pause
                        currentLetter = currentLetter * 10 + 2;
                    } else { // previous dit+letter separation pause
                        currentLetter = currentLetter * 10 + 1;
                        letterCommit(currentLetter,false);
                        currentLetter = 0;
                    }
                } else { //anything left not handled by wordHandler is a dah+letter separation pause
                    currentLetter = currentLetter * 10 + 2;
                    letterCommit(currentLetter,false);
                    currentLetter = 0;
                }
            }
        }
    }

    @Override
    public void onRelease(int primaryCode) {
        if(primaryCode == 4) {
            releaseTime = System.nanoTime();
            if(releaseTime-pressTime < 2 * ditTime) { // previous press was a dit, new word is 6 ditTime from the press
                wordHandler.postDelayed(wordTimeout, 6*(ditTime/1000000) - pressTime/1000000 + releaseTime/1000000);
            } else if(releaseTime-pressTime < 6 * ditTime) { // previous press was a dah, new word is 8 ditTime from the press
                wordHandler.postDelayed(wordTimeout, 8*(ditTime/1000000) - pressTime/1000000 + releaseTime/1000000);
            } else { // long press to cancel letter if you know you messed up
                currentLetter = 0;
                newEntry = true;
            }
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