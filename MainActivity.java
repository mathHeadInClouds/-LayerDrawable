package com.example.layers;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.Display;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

public class MainActivity extends Activity {

	ImageView myImageView;

	int screenWidthPixels;
	Bitmap numbersBitmapBig;     // 2400 x 1200 
	Bitmap numbersBitmapSmall;   // 1200 x  600 
	Bitmap ovalBitmap;           //  800 x  300
	int viewHeight;              // 400
	
	static Bitmap makeNumbersBitmap(Resources resources, int width, int height){
		int padding = 10;
		Paint paint = new Paint();
		Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		int innerWidth = width - 2*padding;
		int innerHeight = height  - 2*padding;
		Canvas canvas = new Canvas(bm);
		paint.setColor(Color.BLACK);
		paint.setTextSize(0.1f*width);
		paint.setTextAlign(Align.CENTER);
		int numRows = 3;
		int numCols = 5;
		int i = 0;
		for (int row = 0; row < numRows; row++){
			for (int col = 0; col < numCols; col++){
				i++;
				float x = padding + ((col + 0.5f) / numCols) * innerWidth;
				float y = padding + ((row + 0.75f) / numRows) * innerHeight;
				canvas.drawText("" + i, x, y, paint);
			}
		}
		paint.setStrokeWidth(5);
		for (int row = 0; row <= numRows; row++){
			float y = padding + ((row + 0f) / numRows) * innerHeight;
			float startX = padding;
			float startY = y;
			float stopX = padding + innerWidth;
			float stopY = y;
			canvas.drawLine(startX, startY, stopX, stopY, paint);
		}
		for (int col = 0; col <= numCols; col++){
			float x = padding + ((col + 0f) / numCols) * innerWidth;
			float startX = x;
			float startY = padding;
			float stopX = x;
			float stopY = padding + innerHeight;
			canvas.drawLine(startX, startY, stopX, stopY, paint);
		}
		return bm;
	}
	static Bitmap makeOvalBitmap(Resources resources, int width, int height){
		Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bm);
		float padding = 6;
		float left   = padding;
		float right  = width - padding;
		float top    = padding;
		float bottom = height - padding;
		RectF containerRect = new RectF(left, top, right, bottom);
		Path path = new Path();
		path.addArc(containerRect, 0, 360);
		Paint paint = new Paint();
		paint.setColor(Color.CYAN);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(20);
		canvas.drawPath(path, paint);
		return bm;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Display display = getWindowManager().getDefaultDisplay();
		screenWidthPixels = display.getWidth();  // 1200
		setContentView(R.layout.main);
		myImageView = (ImageView)findViewById(R.id.myImageView);
		viewHeight = screenWidthPixels/3;
		Resources r = getResources();
		numbersBitmapBig   = makeNumbersBitmap(r, 2*screenWidthPixels, 3*viewHeight); 
		numbersBitmapSmall = makeNumbersBitmap(r, screenWidthPixels, viewHeight*3/2); 
		ovalBitmap = makeOvalBitmap(r, screenWidthPixels*2/3, viewHeight*3/4);
		test4();
	}
	void test1(){ // works
		working(getResources(), screenWidthPixels, viewHeight, numbersBitmapBig, ovalBitmap);
	}
	void test2(){ // makes the view bigger (higher) than it should be
		notWorkingAtAll_outputSpecNotRespected(getResources(), screenWidthPixels, viewHeight, numbersBitmapBig, ovalBitmap);
	}
	void test3(){ // shrinks the drawable, oval too big
		notWorkingEither(getResources(), screenWidthPixels, viewHeight, numbersBitmapBig, ovalBitmap);
	}
	void test4(){ // oval too big
		notWorkingEither(getResources(), screenWidthPixels, viewHeight, numbersBitmapSmall, ovalBitmap);
	}
	double divide(int k, int n) {
		return ((double)k)/n;
	}
	int verticalPaddingForHorizontalFit(int viewWidth, int viewHeight, int drawableWidth, int drawableHeight){
		// you want to draw a drawable into a view, with an exact match in the width. Then either [1] or [2]
		// [1] the view is taller than the drawable (i.e., height/width bigger for the view)
		//     --> method result is positive,
		//     and gives the amount of padding top and bottom
		// [2] the drawable is taller
		//     --> method result is negative,
		//         and gives (minus) the amount that needs to be clipped from top and bottom
		// such that the drawable is vertically centered
		double viewAspect     = divide(viewHeight,     viewWidth    );
		double drawableAspect = divide(drawableHeight, drawableWidth);
		return (int)Math.round(0.5 * viewWidth * (viewAspect - drawableAspect));
	}
	int[] paddingWhenCenteredAt(int viewWidth, int viewHeight, int drawableWidth, int drawableHeight, double drawableScale, int centerX, int centerY){
		// scale the drawable with drawableScale, and put it into the view
		// such that the center of the drawable has coordinates (centerX, centerY)
		// return the padding needed as array of left, top, right, bottom, in that order
		// negative values indicating clipping instead of padding
		double w = drawableScale * drawableWidth;
		double h = drawableScale * drawableHeight;
		double left = centerX - 0.5*w;
		double right = viewWidth - (centerX + 0.5*w);
		double top = centerY - 0.5*h;
		double bottom = viewHeight - (centerY + 0.5*h);
		return new int[]{(int)Math.round(left), (int)Math.round(top), (int)Math.round(right), (int)Math.round(bottom)};
	}
	LayerDrawable makeLayerDrawable(Resources r, int outputWidth, int outputHeight, Bitmap bm1, Bitmap bm2){
		Drawable[] layers = new Drawable[2];
		BitmapDrawable bmd1 = new BitmapDrawable(r, bm1);
		int width1  = bmd1.getIntrinsicWidth();
		int height1 = bmd1.getIntrinsicHeight();
		layers[0]   = bmd1;
		BitmapDrawable bmd2 = new BitmapDrawable(r, bm2);
		int width2  = bmd2.getIntrinsicWidth();
		int height2 = bmd2.getIntrinsicHeight();
		layers[1]   = bmd2;
		LayerDrawable result = new LayerDrawable(layers);
		int vPad = verticalPaddingForHorizontalFit(outputWidth, outputHeight, width1, height1);
		result.setLayerInset(0, 0, vPad, 0, vPad);
		int[] ltrb = paddingWhenCenteredAt(outputWidth, outputHeight, width2, height2, 0.5, outputWidth/2, outputHeight/2);
		result.setLayerInset(1, ltrb[0], ltrb[1], ltrb[2], ltrb[3]);
		result.setBounds(0, 0, outputWidth, outputHeight);
		return result;
	}
	void notWorkingAtAll_outputSpecNotRespected(Resources r,int outputWidth, int outputHeight, Bitmap bm1, Bitmap bm2){
		LayerDrawable layd = makeLayerDrawable(r, outputWidth, outputHeight, bm1, bm2);
		myImageView.setImageDrawable(layd);
	}
	void notWorkingEither(Resources r,int outputWidth, int outputHeight, Bitmap bm1, Bitmap bm2){
		LayerDrawable layd = makeLayerDrawable(r, outputWidth, outputHeight, bm1, bm2);
		LayoutParams lp = new LayoutParams(outputWidth, outputHeight);
		myImageView.setLayoutParams(lp);
		myImageView.setImageDrawable(layd);
	}
	void working(Resources r,int outputWidth, int outputHeight, Bitmap bm1, Bitmap bm2){
		LayerDrawable layd = makeLayerDrawable(r, outputWidth, outputHeight, bm1, bm2);
		Bitmap b = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(b);
		layd.draw(canvas);
		BitmapDrawable result = new BitmapDrawable(getResources(), b);
		result.setBounds(0, 0, outputWidth, outputHeight);
		myImageView.setImageDrawable(result);
	}
	
}
