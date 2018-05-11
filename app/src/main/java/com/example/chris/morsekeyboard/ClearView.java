package com.example.chris.morsekeyboard;
/* Mostly taken from Penti Keyboard
* https://software-lab.de/penti.html
* */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

// class to store information about a touch
class Touchable {
    MotionEvent Ev;
    public long downTime;
    public long upTime;
    public float pushedY, pushedX; // where we pushed
    public float tX,tY,tR; // target loc
    public int hit = -1;
    public Context c;
    Paint Ring = new Paint();
    Paint CircleFill = new Paint();

    public Touchable(){
        // a circle that is transparent save it's dotted outline
        Ring.setColor(Color.BLACK);
        Ring.setPathEffect(new DashPathEffect(new float[]{7,7}, 0));
        Ring.setStyle(Paint.Style.STROKE);
        Ring.setStrokeWidth(4);
        reset();
    }

    // check if push should initialize
    public boolean down(MotionEvent ev) {
        if(isHit(ev)) {
            init(ev);
            hit=1;
            Toast.makeText(c,"Hit Down: "  + Integer.toString(hit) ,Toast.LENGTH_SHORT).show();
            return(true);
        }
        return(false);
    }

    //set initial values
    public void init(MotionEvent ev) {
        downTime=(Ev=ev).getEventTime();
        pushedY=ev.getY();
        pushedX=ev.getX();
        //TODO: start timer to change color after long enough press
    }

    // set target position (done every draw?!)
    public void draw(Canvas cv, float x, float y, float r){
        tX=x;tY=y;tR=r;

        // without shader, circle is not visible
        Ring.setShader(new RadialGradient(x, y, tR, Color.BLACK, Color.WHITE, Shader.TileMode.CLAMP));
        cv.drawCircle(tX,tY,tR,Ring);

        if(hit!=0) fill(cv);
    }
    public void up(MotionEvent ev){
        if(hit!=0){
            Toast.makeText(c,"Hit Up: " + Float.toString(System.currentTimeMillis() - downTime),Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(c, "Up No Hit", Toast.LENGTH_SHORT).show();
        }
        reset();
    }
    public void reset(){
        hit=0;
        pushedX=0; pushedY=0; downTime=0;
    }

    // within radius
    public boolean isHit(MotionEvent ev){return isHit(ev.getX(),ev.getY());}
    public boolean isHit(float x,float y){
        return Math.sqrt( Math.pow(tX-x,2.0) + Math.pow(tY-y,2.0) ) <= tR;
    }

    // fill circle, default to white
    public void fill(Canvas canvas){
        int centerColor = Color.WHITE;
        long current = System.currentTimeMillis();
        if (current - downTime > 10e3) {
            centerColor = Color.RED;
        }
        CircleFill.setShader(new LinearGradient(tX/4, 0, tY*3/4, 0, Color.BLACK, centerColor, Shader.TileMode.CLAMP));
        canvas.drawCircle(tX, tY, tR, CircleFill);
    }
}

public class ClearView extends View {
    MotionEvent Ev;
    MorseIME Ime;
    Paint Text1 = new Paint();
    Context c;
    Touchable t = new Touchable();
    float R = 100;
    public ClearView(Context context, AttributeSet attrs) {
        super(context, attrs);
        c = context;
        Text1.setColor(Color.BLACK);
        Text1.setStyle(Paint.Style.STROKE);
        Text1.setTextAlign(Paint.Align.CENTER);
        if(c != null) t.c = c;


    }
    @Override public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        //Toast.makeText(this.c,Integer.toString(ev.getActionMasked()),Toast.LENGTH_SHORT).show();
        switch (ev.getActionMasked()) {
            // we pushed down (just one. ACTION_POINTER_DOWN is many)
            case MotionEvent.ACTION_DOWN:
                if (t.down(ev)) return(true);
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                t.up(ev);
            break;

        }
        return(false);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = canvas.getWidth();
        float h = canvas.getHeight();
        float x,y;
        x=w/2; y=h/2;
        t.draw(canvas, x, y,R);


    }
    public void reset() {
      return;
    }

}
