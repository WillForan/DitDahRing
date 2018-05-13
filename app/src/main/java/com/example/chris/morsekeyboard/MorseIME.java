package com.example.chris.morsekeyboard;

/**
 * Morse code IME
 */

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.view.View;


public class MorseIME extends InputMethodService
    implements KeyboardView.OnKeyboardActionListener{


    // draw ring over screen instead of drawing a keyboard (intercepts all pushes everywhere)
    // from Penti keyboard, which links to
    //  https://stackoverflow.com/questions/11461002/transparent-inputmethod-for-android/20319466#20319466
    @Override public View onCreateInputView() { return null; }
    @Override public boolean onEvaluateFullscreenMode() { return false;  }
    @Override public void onFinishInput() { setCandidatesViewShown(false); super.onFinishInput(); }

    @Override public View onCreateCandidatesView() {  // http://stackoverflow.com/a/20319466/1160216
        ClearView CV = (ClearView)getLayoutInflater().inflate(R.layout.input, null);
        CV.Ime = this;
        CV.reset();
        return CV;
    }
    @Override public void onStartInputView(android.view.inputmethod.EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        setCandidatesViewShown(true);
    }


    // required, but handled by local Touchable class instead
    @Override public void onKey(int primaryCode, int[] keyCodes) { }
    @Override public void onPress(int primaryCode) {}
    @Override public void onRelease(int primaryCode) {}
    @Override public void onText(CharSequence text) {}
    @Override public void swipeDown() {}
    @Override public void swipeLeft() {}
    @Override public void swipeRight(){}
    @Override public void swipeUp() {}
}