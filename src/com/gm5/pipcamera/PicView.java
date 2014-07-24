package com.gm5.pipcamera;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;



public class PicView extends View {
		private int SCREENW;
		private int SCREENH;
	    private Bitmap myBitmap;

	    private Canvas myCanvas;
	    private Paint myPaint;
	    private Path myPath;
	    private float myX,myY;
	    private static final float TOUCH_TOLERANCE = 4;

	       public PicView(Context context,Bitmap bmp1,Bitmap bmp2,int h,int w) {
	           super(context);
	           setFocusable(true);
	           SCREENW = w;
	           SCREENH = h;
	           setUpBmp2(bmp1,bmp2,h,w);
	       }

	       public PicView(Context context,int h,int w) {
	           super(context);
	           setFocusable(true);
	           SCREENW = w;
	           SCREENH = h;

	          // setBackgroundResource(R.drawable.background);
	       //    Bitmap bmp1 = BitmapFactory.decodeResource(getResources(), R.drawable.background);
	    //       setUpBmp(bmp1);
	      //     Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.san);
	     //      FrameLayout fm = new FrameLayout(context);
	      //     setUpBmp2(bmp1,bmp);
	        //   setUpBmp(bmp1);
	       }
	       
	       void setUpBmp(Bitmap bmp) {
	           // TODO Auto-generatedmethod stub
	           myPaint = new Paint();
	           myPaint.setAlpha(110);

	    //       myPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
	           myPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
	           myPaint.setAntiAlias(true);
	           myPaint.setDither(true);
	           myPaint.setStyle(Paint.Style.STROKE);
	           //myPaint.setStrokeCap(Paint.Cap.ROUND);
	           myPaint.setStrokeJoin(Paint.Join.ROUND);
	           myPaint.setStrokeWidth(20);
	           // 设置路径
	           myPath = new Path();
	           myBitmap = Bitmap.createBitmap(SCREENW, SCREENH,Config.ARGB_8888);
	           myCanvas = new Canvas();
	           myCanvas.setBitmap(myBitmap);
	           myCanvas.drawBitmap(bmp, 0, 0,null);
	       }
	       
	       void setUpBmp2(Bitmap bmp,Bitmap mtp1,int SCREENW,int SCREENH) {
	           // TODO Auto-generatedmethod stub
	           myPaint = new Paint();
	   			myPaint.setAlpha(110);
	           myPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
	           myPaint.setAntiAlias(true);
	           myPaint.setDither(true);
	           myPaint.setStyle(Paint.Style.STROKE);
	           myPaint.setStrokeJoin(Paint.Join.ROUND);
	           myPaint.setStrokeWidth(20);
	           myPath = new Path();
	           myBitmap = Bitmap.createBitmap(SCREENW, SCREENH,Config.ARGB_8888);
	           myCanvas = new Canvas();
	           myCanvas.setBitmap(myBitmap);
	           myCanvas.drawBitmap(bmp, 0, 0,null);
	           myCanvas.drawBitmap(mtp1, 0, 0,null);
	           

	       }
	       
	       
	       public void savepic(){
	           OutputStream os=null;
			try {
				os = new FileOutputStream("/sdcard/Pictures/pipCamera/tmp.jpg");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	           myBitmap.compress(CompressFormat.PNG, 100, os);
	    	   
	       }
	      
	       protected void onDraw(Canvas canvas){
	           canvas.drawBitmap(myBitmap, 0, 0,null);
	           myCanvas.drawPath(myPath, myPaint);
	           super.onDraw(canvas);
	       }
	      
	       private void touch_start(float x, float y){
	           myPath.reset();
	           myPath.moveTo(x, y);
	           myX=x;
	           myY=y;
	       }
	       private void touch_move(float x,float y){
	           float dx = Math.abs(x-myX);
	           float dy = Math.abs(y-myY);
	           if(dx>=TOUCH_TOLERANCE || dy>=TOUCH_TOLERANCE ){
	              myPath.quadTo(myX, myY, (x+myX)/2, (y+myY)/2);
	              myX=x;
	              myY=y;
	           }
	       }
	       private void touch_up(){
	           myPath.lineTo(myX, myY);
	           myCanvas.drawPath(myPath,myPaint);
	           myPath.reset();
	       }
	       public boolean onTouchEvent(MotionEvent event){
	           float x = event.getX();
	           float y = event.getY();
	           switch(event.getAction()){
	           case MotionEvent.ACTION_DOWN:
	              touch_start(x,y);
	              invalidate();
	              break;
	           case MotionEvent.ACTION_MOVE:
	              touch_move(x,y);
	              invalidate();
	              break;
	           case MotionEvent.ACTION_UP:
	              touch_up();
	              invalidate();
	              break;
	           }
	           return true;
	       }
}