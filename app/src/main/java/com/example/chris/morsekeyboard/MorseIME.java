package com.example.chris.morsekeyboard;

/**
 * Morse code IME
 */

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard;
import android.util.SparseArray;
import android.view.View;
import android.view.inputmethod.InputConnection;
import java.util.Timer;
import java.util.TimerTask;

public class MorseIME extends InputMethodService
    implements KeyboardView.OnKeyboardActionListener{

    private long pressTime;
    private long releaseTime;
    private long ditTime = 200000000; // time of a dit in nanoseconds, space between dit/dahs, 1/3 dah, 1/3 space between letters, 1/5 space between words
    // wikipedia says 50 ms for good people.TODO make this a setting
    private boolean newEntry; // used for starting new input to ignore preceeding space
    private int currentLetter; // horrendous implementation; each digit 0 for unused, 1 for dit, 2 for dah; ex: 12 is A; long because if you do too many dit/dahs it'll poop; TODO find a prettier solution

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
    }};
    // TODO prosigns
    // period, comma, etc. should put themselves before the space, then put the space back
    // quote formatting?

    // TODO automatic capitalization

    @Override
    public View onCreateInputView() {
        KeyboardView kv;
        Keyboard keyboard;
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.onebutton);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        kv.setPreviewEnabled(false);
        newEntry = true;
        return kv;
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
    }

    @Override
    public void onPress(int primaryCode) {
        //TODO include shift and delete conditionals and edit the release code accordingly
        InputConnection ic = getCurrentInputConnection();
        pressTime = System.nanoTime();
        if(newEntry) {
            newEntry = false; // time is recorded. Nothing else needed
        }
        else {
            wordTimer.cancel();
            long timeDiff = pressTime-releaseTime;
            //android.util.Log.d("release_time",Long.toString(timeDiff));
            // new word pauses handled by wordTimer started in onRelease()
            if(pressTime-releaseTime > 2* ditTime) { // match 3 dits +/- 1 dit; new letter
                //android.util.Log.d("currentLetter", Long.toString(currentLetter));
                //android.util.Log.d("morsekeyboard", "new letter");
                if(morse.get(currentLetter)==null) {
                    currentLetter = 0;
                    android.util.Log.d("onPress","letter failed, new letter");
                    // TODO call error haptics, should ignore input for a pause so user notices failed letter
                } else {
                    ic.commitText(morse.get(currentLetter), 1);
                    currentLetter = 0; // new letter
                    newEntry = true; // resets time next press rather than guessing letter &c.
                }
                currentLetter = 0;
            } else {
            }
        }

    }

    @Override
    public void onRelease(int primaryCode) {
        InputConnection ic = getCurrentInputConnection();
        releaseTime=System.nanoTime();
        long timeDiff = releaseTime-pressTime;
        //android.util.Log.d("press_time",Long.toString(timeDiff));
        wordTimer = new Timer();
        android.util.Log.d("onRelease","release happened. Timer should start.");
        wordTimer.schedule(new TimerTask() {
            @Override
            public void run() { // on timeout, commit text and start new letter
                InputConnection ic = getCurrentInputConnection();
                android.util.Log.d("timer", "new word timeout");
                if(morse.get(currentLetter)==null) {
                    currentLetter = 0;
                    android.util.Log.d("timer","letter failed, new word cancelled");
                    // TODO call error haptics
                } else {
                    ic.commitText(morse.get(currentLetter, "") + " ", 1);
                    currentLetter = 0; // new letter
                    newEntry = true; // resets time next press rather than guessing letter &c.
                }
            }
        }, (long)((ditTime/1000000)*5)); // set timer for new word after long pause (5 dits)

        if(timeDiff>10*ditTime) { // long press to restart letter
            currentLetter = 0;
            android.util.Log.d("morsekeyboard","reset letter");
        } else if(timeDiff>2*ditTime) { // dah
            currentLetter = 10 * currentLetter + 2;
        } else { // dit
            currentLetter = 10 * currentLetter + 1;
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
        InputConnection ic = getCurrentInputConnection();
        ic.deleteSurroundingText(1,0);
        currentLetter = 0;
        wordTimer.cancel();
    }

    @Override
    public void swipeRight() {
        //send message or something?
    }

    @Override
    public void swipeUp() {
    }
}
