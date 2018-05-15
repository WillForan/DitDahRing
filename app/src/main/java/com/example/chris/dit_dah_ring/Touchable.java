package com.example.chris.dit_dah_ring;

/*
  -- Meat of project -- code for single "ring" drawn over transparent view
  implements:
    * drawing
    * push and swipe
    * sending keyboard events
    * timer (TODO)
    * anination (TODO)
  NB. Ime object is defined elsewhere (in ClearView, from MorseIME, set like t.Ime = this)
  NB. Context defined elsewhere, same as above
  NB. postInvalidate is called in ClearView to force redraw (e.g. to fill the ring after a push)

    *
 */

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import static android.os.SystemClock.uptimeMillis;

// class to store information about a touch
public class Touchable {
    private  long ditdur=100; //milliseconds; how long is a single interval (dit=.)
    MorseIME Ime; // is set by MorseIME->ClearView
    private  long downTime; // when did we push down on the target
    private  float tX,tY,tR; // target loc
    private  int hit = -1; // are we currently on an inside-the-ring touch
    public Context c; //used for toast
    private boolean showHelp = true; // rectangle with ditdot<->char dictionary
    private boolean useTimer = true; // send letter after > 4 ditdur

    // modifiers
    private boolean isShift = false;
    // TODO: control

    // stuff to draw
    private Paint Ring = new Paint();          // push target
    private Paint CircleFill = new Paint();    // hit
    private Paint CircleFill_dah = new Paint(); // hit of dah duration
    private Paint CircleFill_ltr = new Paint(); // hit of ltr duration

    private Paint ditdahLabel = new Paint();
    private Paint boxbg = new Paint();
    private StaticLayout MChelp; // morse code character dictionary

    // current morse code and timer
    private String currentDitDah ="";

    // timer for sending letters (otherwise swipe right)
    private Handler letterHandler;
    private Runnable letterTimeout = new Runnable() {
        @Override public void run() {
            // timed out reached and we are not holding down the button
            // send a letter
            if(hit!=1) {
                reset_letter(true);
                // TODO: redraw to remove display of currentDitDah
            }
        }
    };

    // trace around a circle for dit time
    // Path circlePath = new Path();
    // https://stackoverflow.com/questions/5367950/android-drawing-an-animated-line
    // http://www.curious-creature.com/2013/12/21/android-recipe-4-path-tracing/
    // https://medium.com/@ali.muzaffar/android-change-colour-of-drawable-asset-programmatically-with-animation-e42ca595fabb

    ValueAnimator animator_dah = ValueAnimator.ofInt(100,255);
    ValueAnimator fill_to_dah = ValueAnimator.ofInt(10,255);
    ValueAnimator animator_ltr = ValueAnimator.ofInt(0,255);



    Touchable(){
        // timer
        letterHandler = new Handler();

        // a dotted ring (visible on black background)
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

        // help box char dictionary
        // display tree diagram instead? -- not as useful for quick look up
        TextPaint helpPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        helpPaint.setColor(Color.YELLOW);
        helpPaint.setTextSize(40);
        helpPaint.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.BOLD));

        boxbg.setColor(Color.BLACK);
        boxbg.setStyle(Paint.Style.FILL);
        MChelp = new StaticLayout(MorseCode.strTable(10), helpPaint, 800,
                Layout.Alignment.ALIGN_NORMAL,1.0f,0.0f,false);

        // animations
        CircleFill_dah.setStyle(Paint.Style.FILL);
        fill_to_dah.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                CircleFill.setARGB( 255,0,0, (int) animation.getAnimatedValue());
            }
        });
        animator_ltr.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                CircleFill_ltr.setAlpha((int) animation.getAnimatedValue());
            }
        });
    }

    // on touch down: returns true when draw update needed
    // but also if tracking ev for later
    public boolean down(MotionEvent ev) {
        // new push always resets send letter timeout
        letterHandler.removeCallbacks(letterTimeout);
        if(isHit(ev)) {
            downTime=ev.getEventTime(); //used by up to get dit or dash
            hit=1;
            long delayMillis = 4*ditdur;
            if(useTimer) letterHandler.postDelayed(letterTimeout, delayMillis);
            return(true);
        } else {
            return(true);
        }

    }

    // set target position (done every draw?!)
    public void draw(Canvas cv, float x, float y, float r){
        tX=x;tY=y;tR=r;

        // label for dits and dots keyed in so far
        cv.drawText(currentDitDah, x, y-tR, ditdahLabel);

        // two rings, one for dark background
        // another slightly smaller for light background
        // without shader, circle is not visible
        Ring.setShader(new RadialGradient(x, y, tR, Color.BLACK, Color.WHITE, Shader.TileMode.CLAMP));
        cv.drawCircle(tX,tY,tR,Ring);
        Ring.setShader(new RadialGradient(x, y, tR-4, Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP));
        cv.drawCircle(tX,tY,tR-4,Ring);

        if(hit!=0) fill(cv);
        if(showHelp) {
            cv.drawRect(new Rect(0,0,MChelp.getWidth(),MChelp.getHeight()),boxbg);
            MChelp.draw(cv);
        }

    }
    /* on touch release:
       * always clear hit settings (at end)
       if we had hit
        * swipe or long press resets letter
        * otherwise add dit or dah to letter
     */
    public void up(MotionEvent ev){
        long pressTime = uptimeMillis();
        long pressDur = pressTime - ev.getDownTime();
        // initial push was on target
        if(hit!=0){
            // touch stayed in the ring
            if(isHit(ev)){
                // wait 4 dit durs before submitting character
                //long nextTimer = 4*ditdur + pressTime;
                if(pressDur <= ditdur){
                    // . dit
                    currentDitDah += ".";
                    //Toast.makeText(c,currentDitDah,Toast.LENGTH_SHORT).show();

                }else if(pressDur <= ditdur*3) {
                    // _ dah
                    currentDitDah += "_";
                    //Toast.makeText(c,currentDitDah,Toast.LENGTH_SHORT).show();

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
                    int strlen = currentDitDah.length();
                    if(!useTimer && strlen > 0 ){
                       currentDitDah = currentDitDah.substring(0,strlen-1);
                       hit=0;
                       return; // don't reset_letter (just the hit)
                    } else {
                       Ime.getCurrentInputConnection().deleteSurroundingText(1, 0);
                    }

                } else if(!above_xEmy && above_xEy) {
                    //swipe right -- send space or (esp if not using timer) send morse code to input method
                    reset_letter(true);
                    //Toast.makeText(c,"Swipe Right",Toast.LENGTH_SHORT).show();


                } else if(above_xEmy && above_xEy) {
                    // swipe up -- toggle shift
                    isShift = !isShift;
                    Toast.makeText(c,"shift: " + Boolean.toString(isShift),Toast.LENGTH_SHORT).show();

                } else if(!above_xEmy && ! above_xEy) {
                    //swipe down
                    // TODO: hide keyboard, set control?
                    //Toast.makeText(c,"Swipe Down",Toast.LENGTH_SHORT).show();
                }

                reset_letter(false);
            }
        } else {
            // if releasing after a non-button push
            // short: show help, long: toggle using timer
            if(pressDur < 3*ditdur) {
                showHelp = !showHelp;
            }else{
                useTimer = !useTimer;
                Toast.makeText(c,"timer: " + Boolean.toString(useTimer),Toast.LENGTH_SHORT).show();
            }
        }
        reset_hit();
    }
    public void reset_hit(){
        hit=0;
        downTime=0;
        animator_dah.removeAllUpdateListeners();
    }
    public void reset_letter(boolean send){
        if(send) {
            InputConnection ic = Ime.getCurrentInputConnection();
            String letter = MorseCode.ditdah_letter(currentDitDah, isShift);
            if(letter != null)  ic.commitText(letter, 1);
        }
        currentDitDah = "";
        letterHandler.removeCallbacks(letterTimeout);
    }

    // within radius
    public boolean isHit(MotionEvent ev){return isHit(ev.getX(),ev.getY());}
    public boolean isHit(float x,float y){
        return Math.sqrt( Math.pow(tX-x,2.0) + Math.pow(tY-y,2.0) ) <= tR;
    }

    // fill circle -- called on draw (if hit)
    // what is drawn is only changed on push/release (when onDraw calls postInvalidate)
    public void fill(Canvas canvas){
        int ditColor = Color.WHITE;
        int dahColor = Color.BLUE;
        int ltrColor = Color.RED;

        // it's a hit -- add white color
        //CircleFill.setShader(new LinearGradient(tX/4, 0, tY*3/4, 0, Color.BLACK, ditColor, Shader.TileMode.CLAMP));
        CircleFill.setColor(ditColor);
        canvas.drawCircle(tX, tY, tR, CircleFill);

       /*
         // that might turn into a dah -- animate that transition
        CircleFill_dah.setColor(dahColor);
        CircleFill_dah.setAlpha(200);
        animator_dah.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                CircleFill_dah.setAlpha((int) animation.getAnimatedValue());

            }
        });
        canvas.drawCircle(tX+50, tY+50, tR, CircleFill_dah);
        animator_dah.setDuration(100);
        animator_dah.setRepeatMode(ValueAnimator.REVERSE);
        animator_dah.setRepeatCount(-1);
        animator_dah.start();
        //animator_dah.removeAllUpdateListeners();
        */
        // and then queue the long letter

        /* https://developer.android.com/guide/topics/graphics/prop-animation
        Depending on what property or object you are animating,
         you might need to call the invalidate() method on a View to
         force the screen to redraw itself with the updated animated values.
         You do this in the onAnimationUpdate() callback. For example, animating
         the color property of a Drawable object only causes updates to the screen
         when that object redraws itself. All of the property setters on View,
         such as setAlpha() and setTranslationX() invalidate the View properly,
         so you do not need to invalidate the View when calling these methods with new values.
         For more information on listeners, see the section about Animation listeners.
         */
    }
}
