package com.example.chris.morsekeyboard;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import static android.os.SystemClock.uptimeMillis;

// class to store information about a touch
class Touchable {
    MorseIME Ime; // is set by MorseIME->ClearView
    MotionEvent Ev;
    public long downTime;
    public float tX,tY,tR; // target loc
    public int hit = -1;
    public Context c;
    public long ditdur=100;
    Paint Ring = new Paint();
    Paint CircleFill = new Paint();
    Paint ditdahLabel = new Paint();
    public boolean isShift = false;


    String currentLetter="";
    Handler letterHandler;
    // timer for letters/words
    private Runnable letterTimeout = new Runnable() {
        @Override
        public void run() {
            int a = 1;
            // timed out -- not currently holding, send character
            if( hit != 1) {
                reset_letter(true);

            } else{
                // too soon or we are holding down
                reset_letter(true); // do not sent!
            }

        }
    };

    // trace around a circle for dit time
    // Path circlePath = new Path();
    // https://stackoverflow.com/questions/5367950/android-drawing-an-animated-line
    // http://www.curious-creature.com/2013/12/21/android-recipe-4-path-tracing/
    // https://medium.com/@ali.muzaffar/android-change-colour-of-drawable-asset-programmatically-with-animation-e42ca595fabb

    public Touchable(){
        // timer
        letterHandler = new Handler();

        // a dotted ring to reprsent push target
        Ring.setColor(Color.BLACK);
        Ring.setPathEffect(new DashPathEffect(new float[]{7,7}, 0));
        Ring.setStyle(Paint.Style.STROKE);
        Ring.setStrokeWidth(4);

        // label for what we've keyed in
        ditdahLabel.setColor(Color.YELLOW);
        ditdahLabel.setStyle(Paint.Style.STROKE);
        ditdahLabel.setTextAlign(Paint.Align.CENTER);
        ditdahLabel.setTextSize(100);
        ditdahLabel.setStrokeWidth(100 / 8);
        reset_hit();
    }

    // on touch down -- determin if within button
    public boolean down(MotionEvent ev) {
        // new push always resets send letter timeout
        letterHandler.removeCallbacks(letterTimeout);
        if(isHit(ev)) {
            downTime=(Ev=ev).getEventTime(); //used by up to get dit or dash
            hit=1;
            //Toast.makeText(c,"Hit Down: "  + Integer.toString(hit) ,Toast.LENGTH_SHORT).show();
            letterHandler.postDelayed(letterTimeout, downTime + 4*ditdur );
            return(true);
        }

        return(false);
    }

    // set target position (done every draw?!)
    public void draw(Canvas cv, float x, float y, float r){
        tX=x;tY=y;tR=r;

        // label for dits and dots keyed in so far
        cv.drawText(currentLetter, x, y-tR, ditdahLabel);

        // without shader, circle is not visible
        Ring.setShader(new RadialGradient(x, y, tR, Color.BLACK, Color.WHITE, Shader.TileMode.CLAMP));
        cv.drawCircle(tX,tY,tR,Ring);

        if(hit!=0) fill(cv);

    }
    /* on touch release:
       * always clear hit settings (at end)
       if we had hit
        * swipe or long press resets letter
        * otherwise add dit or dah to letter
     */
    public void up(MotionEvent ev){
        // initial push was on target
        if(hit!=0){
            long pressTime = uptimeMillis();
            long pressDur = pressTime - downTime;

            // touch stayed in the ring
            if(isHit(ev)){
                // wait 4 dit durs before submitting character
                //long nextTimer = 4*ditdur + pressTime;
                if(pressDur <= ditdur){
                    // . dit
                    currentLetter += ".";
                    //Toast.makeText(c,currentLetter,Toast.LENGTH_SHORT).show();

                }else if(pressDur <= ditdur*3) {
                    // _ dah
                    currentLetter += "_";
                    //Toast.makeText(c,currentLetter,Toast.LENGTH_SHORT).show();

                } else {
                    // long press
                    Toast.makeText(c,"Long press (rm cb) " + Float.toString(pressDur),Toast.LENGTH_SHORT).show();
                    reset_letter(false);
                }
                // possible swipe action
            } else {

                float dX = tX-ev.getX();
                float dY = tY-ev.getY();
                // swipe direction based on identity lines y=x and y=-x
                boolean above_xEmy = dX > - dY;  // pos rel to y=-x
                boolean above_xEy  = dX <   dY;  // pos rel to y=x

                if(above_xEmy && ! above_xEy) {
                    // swipe left => delete
                    //Toast.makeText(c,"Swipe Left",Toast.LENGTH_SHORT).show();
                    Ime.getCurrentInputConnection().deleteSurroundingText(1, 0);

                } else if(!above_xEmy && above_xEy) {
                    //swipe right -- send morse code to input method
                    reset_letter(true);
                    //Toast.makeText(c,"Swipe Right",Toast.LENGTH_SHORT).show();


                } else if(above_xEmy && above_xEy) {
                    // swipe up -- toggle shift
                    isShift = !isShift;
                    Toast.makeText(c,"shift: " + Boolean.toString(isShift),Toast.LENGTH_SHORT).show();

                } else if(!above_xEmy && ! above_xEy) {
                    //swipe down
                    //Toast.makeText(c,"Swipe Down",Toast.LENGTH_SHORT).show();
                }

                reset_letter(false);
            }
        }
        reset_hit();
    }
    public void reset_hit(){
        hit=0;
        downTime=0;
    }
    public void reset_letter(boolean send){
        if(send) {
            InputConnection ic = Ime.getCurrentInputConnection();
            String letter = MorseCode.lookup.get(currentLetter);
            // enter or lookup character
            if(letter != null) {
                if(isShift && letter.equals(" ")) letter = "\t";
                else if(isShift ) letter = letter.toUpperCase();

                ic.commitText(letter, 1);
                //Ime.sendKeyChar((char)letter[0]);
            }
        }
        currentLetter = "";
        letterHandler.removeCallbacks(letterTimeout);
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
