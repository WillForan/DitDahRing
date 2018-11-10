package com.example.chris.dit_dah_ring;

/*
 Transparent view with one button overlayed ontop of screen to act as keyboard
 inflated by MorseIME

 Mostly taken from Penti Keyboard
 https://software-lab.de/penti.html
*/

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class ClearView extends View {
    MorseIME Ime;
    Context c;
    Touchable t = new Touchable();
    float R = 200;

    public ClearView(Context context, AttributeSet attrs) {
        super(context, attrs);
        c = context;
        if(c != null) t.c = c;
        t.Ime = Ime;
        t.view = this;
    }
    @Override public boolean onTouchEvent(MotionEvent ev) {
        //Toast.makeText(this.c,Integer.toString(ev.getActionMasked()),Toast.LENGTH_SHORT).show();
        switch (ev.getActionMasked()) {
            // we pushed down (just one. ACTION_POINTER_DOWN is many)
            case MotionEvent.ACTION_DOWN:
                if (t.down(ev)) {
                    // redraw to get circle change on button push
                    postInvalidate();
                    return(true);
                }
            break;
            // up and cancel do the same thing
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                t.up(ev);
                postInvalidate();
            break;
        }
        return(false);
    }
    @Override protected void onDraw(Canvas canvas) {
        t.draw(canvas, R);

        super.onDraw(canvas);
    }
    public void reset() {
      t.Ime = Ime;
    }
}
