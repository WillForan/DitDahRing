package com.example.chris.morsekeyboard;

/**
 * Morse code IME
 */

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard;
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
    private long ditTime = 170000000; // time of a dit in nanoseconds, space between dit/dahs, 1/3 dah, 1/3 space between letters, 1/5 space between words
    // wikipedia says 50 ms for good people.TODO make this a setting
    private boolean newEntry; // used for starting new input to ignore preceding space
    private int currentLetter;  // horrendous implementation; each digit 0 for unused, 1 for dit, 2 for dah
                                // ex: 12 is A
                                // long to not mess up on excessive dits/dahs
    private Timer wordTimer;
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
    // TODO automatic capitalization

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
        if(primaryCode==Keyboard.KEYCODE_SHIFT) {
            keyboard.setShifted(!keyboard.isShifted());
        }
    }

    @Override
    public void onPress(int primaryCode) {
        InputConnection ic = getCurrentInputConnection();
        if(primaryCode == KeyEvent.KEYCODE_DEL) {
            ic.deleteSurroundingText(1, 0);
            // TODO cancel timer but don't crash when none scheduled
            // wordTimer.cancel();
            currentLetter = 0;
            newEntry = true;
        } else if(primaryCode == Keyboard.KEYCODE_SHIFT) {
            // TODO cancel timer but don't crash when none scheduled
        } else {
            pressTime = System.nanoTime();
            if (newEntry) {
                newEntry = false; // time is recorded. Nothing else needed
            } else {
                wordTimer.cancel();
                android.util.Log.d("onPress","wordTimer cancelled");
                long timeDiff = pressTime - releaseTime;
                // new word 5 dit pauses handled by wordTimer started in onRelease() to move to next word
                if (pressTime - releaseTime > 2 * ditTime) { // match 3 dit periods gap with tolerance of 1 dit for new letter
                    if (morse.get(currentLetter) == null) {
                        android.util.Log.d("onPress", "letter failed, new letter");
                        // TODO call error haptics, should ignore input for short time so user notices failure
                    } else {
                        String letterString = morse.get(currentLetter);
                        // TODO why don't I work? if(keyboard.isShifted()) {
                        if(keyboard.isShifted()) {
                            letterString = letterString.toUpperCase();
                            keyboard.setShifted(false);
                        }
                        ic.commitText(letterString, 1);
                        newEntry = true; // resets time next press rather than guessing letter &c.
                    }
                    currentLetter = 0; // clear letter
                } //else { // match anything shorter, just one dit period
                    // dit time gap. Only for continuing current letter
                    // time already recorded. Don't need to do anything
                //}
            }
        }
    }

    @Override
    public void onRelease(int primaryCode) {
        InputConnection ic = getCurrentInputConnection();
        if(primaryCode == 4) {
            releaseTime = System.nanoTime();
            long timeDiff = releaseTime - pressTime;
            wordTimer = new Timer();
            android.util.Log.d("onRelease","wordTimer scheduled");
            wordTimer.schedule(new TimerTask() {
                @Override
                public void run() { // on timeout, commit text and start new letter
                    InputConnection ic = getCurrentInputConnection();
                    android.util.Log.d("timer", "new word timeout");
                    if (morse.get(currentLetter) == null) {
                        currentLetter = 0;
                        android.util.Log.d("timer", "letter failed, new word cancelled");
                        // TODO call error haptics
                    } else {
                        String letterString = morse.get(currentLetter);
                        if(keyboard.isShifted()) {
                            letterString = letterString.toUpperCase();
                            keyboard.setShifted(false);
                        }
                        ic.commitText(letterString + " ", 1);
                        currentLetter = 0; // new letter
                        newEntry = true; // resets time next press rather than guessing letter &c.
                    }
                }
            }, (long) ((ditTime / 1000000) * 6));   // set timer for new word after long pause (5 dits)
                                                    // include extra tolerance of 1 dit for total 6-dit pause

            if (timeDiff > 10 * ditTime) { // long press to restart letter
                currentLetter = 0;
                android.util.Log.d("morsekeyboard", "reset letter");
            } else if (timeDiff > 2 * ditTime) { // dah
                currentLetter = 10 * currentLetter + 2;
            } else { // dit
                currentLetter = 10 * currentLetter + 1;
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