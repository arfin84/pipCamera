package com.gm5.pipcamera;

import java.io.IOException;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

	public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback { 

	    private SurfaceHolder mHolder; 
	    private Camera mCamera; 

	    public CameraPreview(Context context, Camera camera) { 
	        super(context); 
	        mCamera = camera; 
	        mHolder = getHolder(); 
	        mHolder.addCallback(this); 
	        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
	    } 

	    public void startPreview()
	    {
	    	mCamera.startPreview();
	    }
	    
	    public void surfaceCreated(SurfaceHolder holder) { 
	        try { 
	            mCamera.setPreviewDisplay(holder); 
	            mCamera.startPreview(); 
	        } catch (IOException e) { 
	 
	        } 
	    } 

	    public void surfaceDestroyed(SurfaceHolder holder) { 
	      
	    } 

	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) { 
	    	if (mHolder.getSurface() == null){ 
	          return; 
	        } 
	        try { 
	            mCamera.stopPreview(); 
	        } catch (Exception e){ 
	        } 
	        try { 
	            mCamera.setPreviewDisplay(mHolder); 
	            mCamera.setDisplayOrientation(90); 
	            mCamera.startPreview(); 
	        } catch (Exception e){ 

	        } 
	    } 
	    
	    public void setCamera(Camera camera)
	    {
	    	try {
	    		mCamera = camera;
				camera.setPreviewDisplay(mHolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}