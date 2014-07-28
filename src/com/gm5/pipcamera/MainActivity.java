package com.gm5.pipcamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static int wscreen=480;
	private static int hscreen=640;
	
	PicView myview;
	PicView myview2;
	
	Button takepic;
	Button savebutton;
	
	private Camera mCamera;
	private CameraPreview mPreview;
	
	private FrameLayout preview;
	
	private String lastpicpath="";   //the path of last pic

	public static final int MEDIA_TYPE_IMAGE = 1; 
	public static final int MEDIA_TYPE_VIDEO = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Window window = getWindow(); // get window
		requestWindowFeature(Window.FEATURE_NO_TITLE); // remove title 
		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); // full-screen
		
		setContentView(R.layout.activity_main);
		
		
		if(isCamera(this) == false){
			Toast.makeText(this, "there is not a camera dev!", Toast.LENGTH_LONG).show();
			return;
		}
		
		mCamera = getCameraInstance();
		setCameraParams(mCamera);
		mPreview = new CameraPreview(this,mCamera);
		preview = (FrameLayout)findViewById(R.id.preview);
		
		preview.addView(mPreview);
		
		myview = new PicView(this,hscreen,wscreen);
		myview2 = new PicView(this,hscreen,wscreen);
		Bitmap bmp1 = BitmapFactory.decodeResource(getResources(),
				R.drawable.touming);
		myview.setUpBmp(bmp1);
		preview.addView(myview);
		preview.bringChildToFront(myview);
		
		
		takepic = (Button)findViewById(R.id.takepic);
		takepic.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				takeFocusedPic();

			}
		});
		
		ImageButton showbef = (ImageButton)findViewById(R.id.showbef);
		showbef.setOnClickListener(new OpenPictureListener());
		lastpicpath = getLastCaptureFile();
		if (lastpicpath != "")	{updateOpenPicImgBtn(lastpicpath);Log.d("son", "lastpath= "+ lastpicpath);}
		
		savebutton = (Button)findViewById(R.id.savepic);
		savebutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("son", "check save button seccuss");
				savePic();
			}
		});
		savebutton.setClickable(false);
		savebutton.setVisibility(View.INVISIBLE);
		
		Button delbutton = (Button)findViewById(R.id.del);
		delbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Bitmap touming = BitmapFactory.decodeResource(getResources(),
						R.drawable.touming);
				preview.removeView(myview);
				myview.setUpBmp(touming);
				preview.addView(myview);
				savebutton.setClickable(false);
				savebutton.setVisibility(View.INVISIBLE);
				takepic.setClickable(true);
				takepic.setVisibility(View.VISIBLE);
			}
		});
		
		registerBoradcastReceiver();
		
	}//end-oncreate

	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals("changebutton")){
				savebutton.setClickable(false);
				savebutton.setVisibility(View.INVISIBLE);
				takepic.setClickable(true);
				takepic.setVisibility(View.VISIBLE);
			}
		}
		
	};


    public void registerBoradcastReceiver(){  
        IntentFilter myIntentFilter = new IntentFilter();  
        myIntentFilter.addAction("changebutton");  
        //注册广播        
        registerReceiver(mBroadcastReceiver, myIntentFilter);  
    }  
	

	
	
	void savePic() {
		File picFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
		if (picFile == null) {
			return;
		}
		myview.savepic(picFile.getPath());
		Toast.makeText(this, "savepic.name :" + picFile.getName(), Toast.LENGTH_LONG).show();
		lastpicpath = picFile.getAbsolutePath();
		updateOpenPicImgBtn(lastpicpath);
		Bitmap bmp = BitmapFactory.decodeResource(getResources(),
				R.drawable.touming);
		preview.removeView(myview);

		mPreview.startPreview();
		

		takepic.setClickable(true);
		takepic.setVisibility(View.VISIBLE);
		savebutton.setClickable(false);
		savebutton.setVisibility(View.INVISIBLE);
		
		myview.setUpBmp(bmp);
		preview.addView(myview);
		preview.bringChildToFront(myview);
		//takepic.setClickable(true);
	}
	
    // 读取保存目录中最新的图像文件
    String getLastCaptureFile()
    {
    	File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory( 
	              Environment.DIRECTORY_PICTURES), "PipCamera");  
    	if(mediaStorageDir.exists() == false)
    	{
    		return "";
    	}
    	File [] fs = mediaStorageDir.listFiles();  
    	if(fs.length <= 0)
    		return "";
    	Arrays.sort(fs, new MainActivity.CompratorByLastModified());
    	return fs[fs.length-1].getPath();
    }
    
    // 排序器，按修改时间从新到旧排序
    static class CompratorByLastModified implements Comparator<File>  
    {  
	     public int compare(File f1, File f2) 
	     {  
	    	 long diff = f1.lastModified()-f2.lastModified();  
	         if(diff>0)  
	            return 1;  
	         else if(diff==0)  
	            return 0;  
	         else  
	            return -1;  
	    }
	     
	    public boolean equals(Object obj)
	    {  
	      return true;  
	    }  
    } 
    
	/** 为保存图片或视频创建文件Uri */ 
	private  Uri getOutputMediaFileUri(int type){ 
	      return Uri.fromFile(getOutputMediaFile(type)); 
	} 
	
	private void takeFocusedPic(){
		mCamera.autoFocus(new Camera.AutoFocusCallback() {
			
			@Override
			public void onAutoFocus(boolean arg0, Camera arg1) {
				// TODO Auto-generated method stub
				Log.d("son", "takepic");
			//	takepic.setClickable(false);
				mCamera.takePicture(null, null, mPicture);
			}
		});
	}
	
	private PictureCallback mPicture = new PictureCallback(){

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d("son", "trying to save pic");
			// TODO Auto-generated method stub
			File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory( 
		              Environment.DIRECTORY_PICTURES), "PipCamera");
		    if (! mediaStorageDir.exists()){ 
		        if (! mediaStorageDir.mkdirs()){ 
		  
		        } 
		    }    
			File pictureFile = new File(mediaStorageDir.getPath()+File.separator+".Cameratmp.png");
			if(pictureFile != null){
				pictureFile.delete();
			}
			
		        try { 
		        	
		            FileOutputStream fos = new FileOutputStream(pictureFile); 
		            Log.d("son", "save picfile");
		            fos.write(data); 
		            fos.close(); 
		            
			        lastpicpath = pictureFile.getAbsolutePath();
		            
		            Bitmap bmp1 = BitmapFactory.decodeFile(lastpicpath);
		            myview.savepic("/sdcard/Pictures/PipCamera/.viewtmp.png");
		            Bitmap bmp2 = BitmapFactory.decodeFile("/sdcard/Pictures/PipCamera/.viewtmp.png");
		            myview2.setUpBmp2(bmp1, bmp2, wscreen, hscreen);
		            myview2.savepic("/sdcard/Pictures/PipCamera/.finalcanvas.png");// save the comp pic
		            Bitmap bmp3 = BitmapFactory.decodeFile("/sdcard/Pictures/PipCamera/.finalcanvas.png");
		            myview.setUpBmp(bmp3);
		            preview.removeView(myview);
		            
		            mPreview.startPreview();
			        preview.addView(myview);
			        preview.bringChildToFront(myview);
					savebutton.setClickable(true);
					savebutton.setVisibility(View.VISIBLE);
					takepic.setClickable(false);
					takepic.setVisibility(View.INVISIBLE);

			     //   updateOpenPicImgBtn(lastpicpath);
		        } catch (FileNotFoundException e) { 
		        } catch (IOException e) { 
		        } 
		}	
	};
	
	public class OpenPictureListener implements View.OnClickListener
	{
		@Override
		public void onClick(View v) {
			File file = new File(lastpicpath); 
		    Intent intent = new Intent(Intent.ACTION_VIEW); 
		    intent.setDataAndType(Uri.fromFile(file), "image/*");
		    startActivity(intent);
		}	
	};
	
	public void updateOpenPicImgBtn(String path)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;                     //表明只获取图像大小
		Bitmap bm = BitmapFactory.decodeFile(path, options);    //由于inJustDecodeBounds为true，此时bm为null
		options.inSampleSize = options.outWidth/64;
		options.inJustDecodeBounds = false;
		bm = BitmapFactory.decodeFile(path, options);
		ImageButton showbef = (ImageButton)findViewById(R.id.showbef);
		showbef.setImageBitmap(bm);
	}
	
	private void checkDirexist(){
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory( 
	              Environment.DIRECTORY_PICTURES), "PipCamera");
	    if (! mediaStorageDir.exists()){ 
	        if (! mediaStorageDir.mkdirs()){ 
	  
	        } 
	    }    
	}
	
	private File getOutputMediaFile(int type){
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory( 
	              Environment.DIRECTORY_PICTURES), "PipCamera");
	    if (! mediaStorageDir.exists()){ 
	        if (! mediaStorageDir.mkdirs()){ 
	            return null; 
	        } 
	    }    
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmms").format(new Date());
		File mediaFile;
		if(type == MEDIA_TYPE_IMAGE){
			mediaFile = new File(mediaStorageDir.getPath()+File.separator+
					"IMG_"+timeStamp+".jpg");
		}else if(type == MEDIA_TYPE_VIDEO){
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + 
			        "VID_"+ timeStamp + ".mp4"); 
		}else{
			return null;
		}
		return mediaFile;
	}
	
	private void setCameraParams(Camera camera){
		camera.setDisplayOrientation(90);
		Camera.Parameters params = camera.getParameters();
		params.setRotation(90);
		camera.setParameters(params);
	}

	private Camera getCameraInstance(){
		Camera camera = null;
		try{	
			camera = Camera.open();
		}catch(Exception e){	}
		return camera;
	}

	private boolean isCamera(Context context){
		if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
			return true;
		}else{
			return false;
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        if(mCamera == null)
        {
            mCamera = getCameraInstance();
            setCameraParams(mCamera);
            mPreview.setCamera(mCamera);
            mCamera.startPreview(); 	
        }
        
    }
	
    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }
    
	 private void releaseCamera(){ 
	        if (mCamera != null){ 
	            mCamera.release();        // 为其它应用释放摄像头
	            mCamera = null; 
	        } 
	    } 

}
