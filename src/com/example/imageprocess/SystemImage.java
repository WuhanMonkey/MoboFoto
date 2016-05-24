package com.example.imageprocess;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
















import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SystemImage extends Activity {
	
	SurfaceView sView;
	SurfaceHolder surfaceHolder;
	int screenWidth;
	double screenHeight;
	// ¶¨ÒåÏµÍ³ËùÓÃµÄÕÕÏà»ú
	Camera camera;
	// ÊÇ·ñÔÚÔ¤ÀÀÖÐ
	boolean isPreview = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// ÉèÖÃÈ«ÆÁ
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.systemimage);
		// »ñÈ¡´°¿Ú¹ÜÀíÆ÷
		WindowManager wm = getWindowManager();
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		// »ñÈ¡ÆÁÄ»µÄ¿íºÍ¸ß
		display.getMetrics(metrics);
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels*0.8;
		// »ñÈ¡½çÃæÖÐSurfaceView×é¼þ
		sView = (SurfaceView) findViewById(R.id.sView);
		// ÉèÖÃ¸ÃSurface²»ÐèÒª×Ô¼ºÎ¬»¤»º³åÇø
		sView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// »ñµÃSurfaceViewµÄSurfaceHolder
		surfaceHolder = sView.getHolder();
		// ÎªsurfaceHolderÌí¼ÓÒ»¸ö»Øµ÷¼àÌýÆ÷
		surfaceHolder.addCallback(new Callback()
		{
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
				int width, int height)
			{
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder)
			{
				// ´ò¿ªÉãÏñÍ·
				initCamera();
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder)
			{
				// Èç¹ûcamera²»Îªnull ,ÊÍ·ÅÉãÏñÍ·
				if (camera != null)
				{
					if (isPreview) camera.stopPreview();
					camera.release();
					camera = null;
				}
			}
		});
	}

	private void initCamera()
	{
		// Èç¹ûÏà»úÎª·ÇÔ¤ÀÀÄ£Ê½£¬Ôò´ò¿ªÏà»ú
		if (!isPreview) {
			camera=Camera.open(); // ´ò¿ªÏà»ú
			camera.setDisplayOrientation(90);
		}
		try {
			Intent intent = getIntent();
			camera.setPreviewDisplay(surfaceHolder); // ÉèÖÃÓÃÓÚÏÔÊ¾Ô¤ÀÀµÄSurfaceView
			Camera.Parameters parameters = camera.getParameters();	//»ñÈ¡Ïà»ú²ÎÊý
			parameters.setPictureSize(3264,2448);	//ÉèÖÃÔ¤ÀÀ»­ÃæµÄ³ß´ç
			parameters.setPictureFormat(PixelFormat.JPEG);	//Ö¸¶¨Í¼Æ¬ÎªJPEGÍ¼Æ¬
			parameters.set("jpeg-quality", 100);	//ÉèÖÃÍ¼Æ¬µÄÖÊÁ¿
			//parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
			parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
			parameters.set("focus-distances", intent.getStringExtra("focus"));
			Log.d("focus", intent.getStringExtra("focus"));
			parameters.set("ISO-values", intent.getStringExtra("iso"));
			TextView camDisplay = (TextView) this.findViewById(R.id.camDisplay);
			camDisplay.setText("Camera Parameters\n" + "Focus Distance: " + intent.getStringExtra("focus")+"\n"+ "ISO Values: " + intent.getStringExtra("iso") + "\n" + "Exposure Compensation: " + intent.getStringExtra("exposure"));
			
		   // parameters.setZoom(4);
			
			parameters.setExposureCompensation(Integer.valueOf(intent.getStringExtra("exposure")));
			parameters.setPictureSize(3264,2448); 	//ÉèÖÃÅÄÉãÍ¼Æ¬µÄ³ß´ç
			
			parameters.set("rotation", 90);
			camera.setParameters(parameters);	//ÖØÐÂÉèÖÃÏà»ú²ÎÊý
			System.out.println(parameters.flatten());
			camera.startPreview();	//¿ªÊ¼Ô¤ÀÀ
			camera.autoFocus(null); // ÉèÖÃ×Ô¶¯¶Ô½¹
		} catch (IOException e) {
			e.printStackTrace();
		}

	
			isPreview = true;
		}
	

	public void capture(View source)
	{
		if (camera != null)
		{
			// ¿ØÖÆÉãÏñÍ·×Ô¶¯¶Ô½¹ºó²ÅÅÄÕÕ
			camera.autoFocus(autoFocusCallback);  //¢Ü
		}
	}

	AutoFocusCallback autoFocusCallback = new AutoFocusCallback()
	{
		// µ±×Ô¶¯¶Ô½¹Ê±¼¤·¢¸Ã·½·¨
		@Override
		public void onAutoFocus(boolean success, Camera camera)
		{
			if (success)
			{
				// takePicture()·½·¨ÐèÒª´«Èë3¸ö¼àÌýÆ÷²ÎÊý
				// µÚ1¸ö¼àÌýÆ÷£ºµ±ÓÃ»§°´ÏÂ¿ìÃÅÊ±¼¤·¢¸Ã¼àÌýÆ÷
				// µÚ2¸ö¼àÌýÆ÷£ºµ±Ïà»ú»ñÈ¡Ô­Ê¼ÕÕÆ¬Ê±¼¤·¢¸Ã¼àÌýÆ÷
				// µÚ3¸ö¼àÌýÆ÷£ºµ±Ïà»ú»ñÈ¡JPGÕÕÆ¬Ê±¼¤·¢¸Ã¼àÌýÆ÷
				camera.takePicture(new ShutterCallback()
				{
					public void onShutter()
					{
						// °´ÏÂ¿ìÃÅË²¼ä»áÖ´ÐÐ´Ë´¦´úÂë
					}
				}, new PictureCallback()
				{
					public void onPictureTaken(byte[] data, Camera c)
					{
						// ´Ë´¦´úÂë¿ÉÒÔ¾ö¶¨ÊÇ·ñÐèÒª±£´æÔ­Ê¼ÕÕÆ¬ÐÅÏ¢
					}
				}, myJpegCallback);  //¢Ý
			}
		}
	};
	
	public static void saveImageToGallery(Context context, Bitmap bmp,String s,int width,double height) {
	    // Ê×ÏÈ±£´æÍ¼Æ¬
	    File appDir = new File(Environment.getExternalStorageDirectory(), "Pictures");
	    if (!appDir.exists()) {
	        appDir.mkdir();
	    }
	    String fileName = s + ".jpg";
	    File file = new File(appDir, fileName);
	    try {
	    	file.createNewFile();
	        FileOutputStream fos = new FileOutputStream(file);
	        Bitmap mBitmap = Bitmap.createScaledBitmap(bmp,width, (int)height, true);
			mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
	        fos.flush();
	        fos.close();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
		}
	    
	    // Æä´Î°ÑÎÄ¼þ²åÈëµ½ÏµÍ³Í¼¿â
	    try {
	        MediaStore.Images.Media.insertImage(context.getContentResolver(),
					file.getAbsolutePath(), fileName, null);
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    }
	    // ×îºóÍ¨ÖªÍ¼¿â¸üÐÂ
	    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,	Uri.fromFile(new File(file.getPath()))));
	}
	PictureCallback myJpegCallback=new PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			BitmapFactory.Options options = new BitmapFactory.Options();  
			options.inSampleSize=2;//图片高宽度都为原来的二分之一，即图片大小为原来的大小的四分之一 
			final Bitmap bm=BitmapFactory.decodeByteArray(data, 0, data.length,options);
			
			Matrix m = new Matrix();
		    m.setRotate(90,(float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
		    final Bitmap bm0 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
			
			
			
			View saveView=getLayoutInflater().inflate(R.layout.save, null);
			final EditText photoName=(EditText) saveView.findViewById(R.id.phone_name);
			ImageView show=(ImageView) saveView.findViewById(R.id.show);
			show.setImageBitmap(bm0);
			
			new AlertDialog.Builder(SystemImage.this).setView(saveView).
			setNegativeButton("cancel", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
	            Toast.makeText(SystemImage.this, "Image not saved",
		                Toast.LENGTH_SHORT).show();
			}
			}
			).
			setPositiveButton("save", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {

					saveImageToGallery(SystemImage.this, bm0, photoName.getText().toString(), screenWidth, screenHeight);
					
					Intent data = new Intent();
					data.putExtra("myData1", "Image saved successfully");

				    data.putExtra("image", photoName.getText().toString());
					// Activity finished ok, return the data
					setResult(RESULT_OK, data);
					finish();
					
				}
			}).show();
			
			
		    
			
			
			camera.stopPreview();
			camera.startPreview();
			isPreview=true;
		}
	};
}
