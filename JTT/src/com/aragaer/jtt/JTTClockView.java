package com.aragaer.jtt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.TextView;

public class JTTClockView extends TextView {
	private final static int step = 360 / 12;
	private final static float gap = 1.5f;
	
	private final Paint mStrokePaint = new Paint();
	private final Paint mSolidPaint = new Paint();
	private final Paint mSolidPaint2 = new Paint();
	private final Bitmap clocks_h[] = new Bitmap[12], clocks_v[] = new Bitmap[12]; 
	private JTTHour hour;
	public JTTClockView(Context context) {
		super(context);
	}

	public JTTClockView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public JTTClockView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (hour == null)
			return;
		
		int w = super.getWidth();
		int h = super.getHeight();
		
		Bitmap[] clocks;
		int size;
		
		if (h > w) {
			clocks = clocks_v;
			size = w;
		} else {
			clocks = clocks_h;
			size = h;
		}
		
		if (clocks[hour.num] == null)
			clocks[hour.num] = drawBitmap(hour.num, size);
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		
		Matrix transform = new Matrix();
		transform.setTranslate(w/2 - size/2, 3*h/5 - size/2);
		transform.preRotate(step*(0.5f - hour.num - hour.fraction), size/2, size/2);
		canvas.drawBitmap(clocks[hour.num], transform, paint);
		
		paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.argb(255, 255, 255, 255));
        paint.setTextSize(h/20);
        
        canvas.drawText("Hour of "+hour.hour, w/2, h/10, paint);
	}
	
    private final void setupPaint() {
    	LightingColorFilter f = new LightingColorFilter(0xFFFFFFFF, 0xFFCCCCCC);
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setTextAlign(Paint.Align.CENTER);
        mStrokePaint.setColor(Color.argb(192, 0, 0, 0));

        mSolidPaint.setAntiAlias(true);
        mSolidPaint.setStyle(Paint.Style.FILL);
        mSolidPaint.setTextAlign(Paint.Align.CENTER);
        mSolidPaint.setColor(R.color.fill);
        mSolidPaint.setColorFilter(f);

        mSolidPaint2.setAntiAlias(true);
        mSolidPaint2.setStyle(Paint.Style.FILL);
        mSolidPaint2.setTextAlign(Paint.Align.CENTER);
        mSolidPaint2.setColor(Color.argb(128, 128, 128, 128));
        mSolidPaint2.setStrokeWidth(1.3f);
    }
    
    private Bitmap drawBitmap(int num, int size) {
		setupPaint();

		int cx = size/2;
    	int cy = size/2;
		Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		
		int innerR = size/5;
		int thick = size/5;
		int outerR = innerR+thick;
		int selR = outerR + thick/4;
		
		mStrokePaint.setTextSize(thick/3);
		mSolidPaint2.setTextSize(thick/3);
		
		RectF outer = new RectF(cx-outerR,cy-outerR,cx+outerR,cy+outerR);
		RectF inner = new RectF(cx-innerR,cy-innerR,cx+innerR,cy+innerR);
		RectF sel = new RectF(cx-selR,cy-selR,cx+selR,cy+selR);
		RectF sun = new RectF(cx-innerR*0.2f, cy-innerR*0.2f, cx+innerR*0.2f, cy+innerR*0.2f);
		
		canvas.rotate(-step/2,cx,cy);
		canvas.translate(-innerR*0.75f, 0);
		canvas.drawArc(sun, 0, 360, false, mSolidPaint);
		canvas.drawArc(sun, 0, 360, false, mStrokePaint);
		canvas.rotate(90, cx+innerR*0.75f, cy);
		canvas.drawArc(sun, 0, 180, false, mSolidPaint);
		canvas.drawArc(sun, 180, 180, false, mSolidPaint2);
		canvas.drawArc(sun, 0, 360, false, mStrokePaint);
		canvas.drawLine(cx-innerR*0.2f, cy, cx+innerR*0.2f, cy, mStrokePaint);
		canvas.rotate(90, cx+innerR*0.75f, cy);
		canvas.drawArc(sun, 0, 360, false, mSolidPaint2);
		canvas.drawArc(sun, 0, 360, false, mStrokePaint);
		canvas.rotate(90, cx+innerR*0.75f, cy);
		canvas.drawArc(sun, 180, 180, false, mSolidPaint);
		canvas.drawArc(sun, 0, 180, false, mSolidPaint2);
		canvas.drawArc(sun, 0, 360, false, mStrokePaint);
		canvas.drawLine(cx-innerR*0.2f, cy, cx+innerR*0.2f, cy, mStrokePaint);
		canvas.translate(innerR*0.75f, 0);
		canvas.rotate(90 + step/2,cx,cy);
		
		int arc_start = -90-Math.round(step/2-gap);
		int arc_end = -90+Math.round(step/2-gap);
		int arc_len = arc_end - arc_start;

		double start = Math.toRadians(arc_start);
		double end = Math.toRadians(arc_end);
		
		for (int hr = 0; hr < 12; hr++) {
			Path path = new Path();
			path.addArc(inner, arc_start, arc_len);
			if (hr == num) {
				path.lineTo((float)(cx + selR*Math.cos(end)), (float)(cy+selR*Math.sin(end)));
				path.addArc(sel, arc_end, -arc_len);
			} else {
				path.lineTo((float)(cx + outerR*Math.cos(end)), (float)(cy+outerR*Math.sin(end)));
				path.addArc(outer, arc_end, -arc_len);
			}
			path.lineTo((float)(cx + innerR*Math.cos(start)), (float)(cy+innerR*Math.sin(start)));
			canvas.drawPath(path, mSolidPaint);
			canvas.drawPath(path, mStrokePaint);
			
			float glyph_y;
			if (hr == num)
				glyph_y = cy - innerR - 5*thick/9;
			else
				glyph_y = cy - innerR - 4*thick/9;
			canvas.drawText(JTT.Glyphs[hr], cx, glyph_y, mSolidPaint2);
			canvas.drawText(JTT.Glyphs[hr], cx, glyph_y, mStrokePaint);
			canvas.rotate(step, cx, cy);
		}
		
		return result;
	}
    
    public void setJTTHour(JTTHour new_hour) {
    	hour = new_hour;
    	invalidate();
    }
}