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
import android.content.Context;
import android.content.Intent;
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
	
	private static int wscreen;
	private static int hscreen;
	
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
		
		Button takepic = (Button)findViewById(R.id.takepic);
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
		if (lastpicpath != "")
			updateOpenPicImgBtn(lastpicpath);
		
	}//end-oncreate

	
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
				mCamera.takePicture(null, null, mPicture);
			}
		});
	}
	private PictureCallback mPicture = new PictureCallback(){

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d("son", "trying to save pic");
			// TODO Auto-generated method stub
		     File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE); 
		        if (pictureFile == null){ 
		            return; 
		        }
		        try { 
		        	
		            FileOutputStream fos = new FileOutputStream(pictureFile); 
		            Log.d("son", "save picfile");
		            fos.write(data); 
		            fos.close(); 
		            mPreview.startPreview();
			        lastpicpath = pictureFile.getAbsolutePath();
			        updateOpenPicImgBtn(lastpicpath);
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

}