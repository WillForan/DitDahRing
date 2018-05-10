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

public class ClearView extends View {
    MorseIME Ime;
    Paint Text1 = new Paint();
    Paint Circle1 = new Paint();
    Context c;
    float R = 100;
    public ClearView(Context context, AttributeSet attrs) {
        super(context, attrs);
        c = context;
        Text1.setColor(Color.BLACK);
        Text1.setStyle(Paint.Style.STROKE);
        Text1.setTextAlign(Paint.Align.CENTER);
        // a circle that is transparent save it's dotted outline
        Circle1.setColor(Color.BLACK);
        Circle1.setPathEffect(new DashPathEffect(new float[]{7,7}, 0));
        Circle1.setStyle(Paint.Style.STROKE);
        Circle1.setStrokeWidth(4);

    }
    @Override public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        Toast.makeText(this.c,Integer.toString(ev.getActionMasked()),Toast.LENGTH_SHORT).show();
        return(false);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = canvas.getWidth();
        float h = canvas.getHeight();
        float x,y;
        x=w/2; y=h/2;

        // without shader, circle is not visible
        Circle1.setShader(new RadialGradient(x, y, R, Color.BLACK, Color.WHITE, Shader.TileMode.CLAMP));
        canvas.drawCircle(x,y,R,Circle1);

    }
    public void reset() {
      return;
    }

}
