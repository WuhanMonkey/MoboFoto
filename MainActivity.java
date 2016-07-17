package com.example.imageprocess;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import com.google.gson.Gson;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	Uri selectedImage;
    //private FragmentManager fragmentManager;  
    //private RadioGroup radioGroup;
    public static final String PREFS_NAME = "AOP_PREFS";
	private static Editable getFocus = null;
	private static Editable  getISO = null;
	private static Editable  getExposure = null;
	private static Bitmap org_bitmap;
	private static Bitmap bicolor;
	//private static Bitmap smoothed;
	private static String pictureName;
	private double[] lefttop = new double[2];
	private double[] righttop= new double[2];
	private double[] leftbot= new double[2];
	private double[] rightbot= new double[2];
	private static final String PREF_FILE_NAME = "DataFile";
	private static final String DATA_NAME ="Data";
	private static final String PREF_OVU_NAME = "OvuFile";
	private static final String DATA_OVU ="Ovu";
	private String analysisType ="";
	static {
	    if (!OpenCVLoader.initDebug()) {
	    	Log.d("opencv_error", "opencv failed to load!");
	        // Handle initialization error
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
		bar.setIcon(
				   new ColorDrawable(getResources().getColor(android.R.color.transparent))); 
		
		//Swipe functionality
		View rootView = findViewById(android.R.id.content);
		rootView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
		    public void onSwipeTop() {
		    }
		    public void onSwipeRight() {
		    }
		    public void onSwipeLeft() {
				Intent analysisIntent=new Intent(MainActivity.this, analysisMain.class);
				analysisIntent.putExtra("photoPath", pictureName);				
				startActivity(analysisIntent);
		    }
		    public void onSwipeBottom() {
		    }

		});
		
		
		
		//Image analysis
		final Button analysis = (Button) this.findViewById(R.id.analysis);
		View analysischeck=getLayoutInflater().inflate(R.layout.analysischeck, null);
		final EditText patientName=(EditText) analysischeck.findViewById(R.id.name);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this).setView(analysischeck).
                setTitle("Analysis options").
                setMessage("Please provide Patient's name.").
      			setNegativeButton("Cancel", new OnClickListener() {            				
    				@Override
    				public void onClick(DialogInterface dialog, int which) {

    				}
    			}).
    			setPositiveButton("Proceed", new OnClickListener() {            				
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					if(org_bitmap!=null){
    					Intent analysisIntent=new Intent(MainActivity.this, analysisMain.class);
    					analysisIntent.putExtra("photoPath", pictureName);
    					double poi_y = (leftbot[1]-lefttop[1])*32/37+lefttop[1];
    					double poi_x = (rightbot[0]-leftbot[0])*3.5/12.5+leftbot[0];
    					analysisIntent.putExtra("poi_x", poi_x);
    					analysisIntent.putExtra("poi_y", poi_y);
    					analysisIntent.putExtra("lefttop_x", lefttop[0]);
    					analysisIntent.putExtra("lefttop_y", lefttop[1]);
    					analysisIntent.putExtra("righttop_x", righttop[0]);
    					analysisIntent.putExtra("righttop_y", righttop[1]);
    					analysisIntent.putExtra("leftbot_x", leftbot[0]);
    					analysisIntent.putExtra("leftbot_y", leftbot[1]);
    					analysisIntent.putExtra("rightbot_x", rightbot[0]);
    					analysisIntent.putExtra("rightbot_y", rightbot[1]);
    					analysisIntent.putExtra("name", patientName.getText().toString());
    					analysisIntent.putExtra("type", analysisType);
    					startActivity(analysisIntent);
    					}
    					else{
    			            Toast.makeText(MainActivity.this, "Please first select or take a picture!",
    				                Toast.LENGTH_SHORT).show();
    					}
    						
    					
    				}
    			});
		final AlertDialog alertDialog = alertDialogBuilder.create();
		
		AlertDialog.Builder alertDialogBuilderForOpt = new AlertDialog.Builder(MainActivity.this).
                setTitle("Analysis Type").
                setMessage("Please choose your analysis type").
      			setNegativeButton("Glucose", new OnClickListener() {            				
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					if(org_bitmap!=null){
        					analysisType = "Glucose";
        					alertDialog.show();
    					}
    					else{
    			            Toast.makeText(MainActivity.this, "Please first select or take a picture!",
    				                Toast.LENGTH_SHORT).show();
    					}
    				}
    			}).
    			setPositiveButton("Ovulation", new OnClickListener() {            				
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					if(org_bitmap!=null){
    						analysisType = "Ovulation";
    						alertDialog.show();
    					}
    					else{
    			            Toast.makeText(MainActivity.this, "Please first select or take a picture!",
    				                Toast.LENGTH_SHORT).show();
    					}
    						
    					
    				}
    			}).setNeutralButton("All",new OnClickListener() {            				
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					if(org_bitmap!=null){
    						analysisType = "All";
    						alertDialog.show();
    					}
    					else{
    			            Toast.makeText(MainActivity.this, "Please first select or take a picture!",
    				                Toast.LENGTH_SHORT).show();
    					}
    						
    					
    				}
    			});
		
		
			
		
		final AlertDialog alertDialogForOpt = alertDialogBuilderForOpt.create();
		
		
		
		
		analysis.setOnClickListener(new View.OnClickListener() {
	
			@Override
			public void onClick(View v) {
				alertDialogForOpt.show();

			}
		});
		
		
		//Image processing
		final Button edge = (Button) this.findViewById(R.id.edge);
		edge.setTag(1);
		edge.setOnClickListener(new View.OnClickListener() {
        	
            @Override
            public void onClick(View v) {
            
            	final ImageView image = (ImageView) findViewById(R.id.iv);
            if(image.getDrawable() == null){
	            Toast.makeText(MainActivity.this, "Please first select or take a picture!",
		                Toast.LENGTH_SHORT).show();
	            edge.setTag(0);
            }
            final int status =(Integer) v.getTag();
            //No bitmap assigned.
            if(status == 0)
            	return;
            	
            if(status == 3){
            	
  
            	
            	}
            //Image processing
            else if(status == 1){
            	final ProgressDialog progress = new ProgressDialog(MainActivity.this, ProgressDialog.STYLE_SPINNER);
            	progress.setCancelable(false);
            	progress.setTitle("Image processing...");
            	progress.show();
            	v.setTag(2);
           
            	new AsyncTask<Void, Void, Void>()
                {
                    @Override
                    protected Void doInBackground(Void... params)
                    {
                    			          
                    	Bitmap bmp = ((BitmapDrawable)image.getDrawable()).getBitmap();
                    	Log.d("Test","bmp width and length are: " + bmp.getWidth() + " "+bmp.getHeight());
                		if(bicolor==null){
                    	Mat mImg = new Mat();
                        Utils.bitmapToMat(bmp,mImg);
                        Imgproc.cvtColor(mImg, mImg, Imgproc.COLOR_RGBA2RGB); 
                        Imgproc.pyrMeanShiftFiltering(mImg,mImg,10,20);
                        bicolor = Bitmap.createBitmap(mImg.cols(), mImg.rows(), Bitmap.Config.ARGB_8888); 
                		
                        Utils.matToBitmap(mImg, bicolor); 
                      //convert to mat:
                    	final Bitmap bitmap = bicolor;
                        //Bitmalap bitmap = smoothed;
                    	
                    	int iCannyLowerThreshold = 60, iCannyUpperThreshold = 100;      
                        Mat m = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
                        Utils.bitmapToMat(bitmap, m);
                        Mat thr = new Mat(m.rows(),m.cols(),CvType.CV_8UC1); 
                        Mat dst = new Mat(m.rows(), m.cols(), CvType.CV_8UC1, Scalar.all(0));
                        Imgproc.cvtColor(m, thr, Imgproc.COLOR_BGR2GRAY);
                        Imgproc.threshold(thr, thr, 50, 255, Imgproc.THRESH_BINARY);        
                        Imgproc.Canny(thr, thr, iCannyLowerThreshold, iCannyUpperThreshold);
                        //Imgproc.findContours(m, contours, new Mat(), 0, 1);
                        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                        Imgproc.findContours( thr, contours, new Mat(),Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0) );
                        Scalar color = new Scalar( 255,255,255);
                        //find the max contour
                        double maxArea = -1;
                        int maxAreaIdx = -1;
                        for (int idx = 0; idx < contours.size(); idx++) {
                            Mat contour = contours.get(idx);
                            double contourarea = Imgproc.contourArea(contour);
                            if (contourarea > maxArea) {
                                maxArea = contourarea;
                                maxAreaIdx = idx;
                            }
                        }
                        
                        
                        Imgproc.drawContours(dst, contours, maxAreaIdx, color, 3);    

                        //corner detection
                        //TO-DO find the exact value for 4 points.
                        MatOfKeyPoint points = new MatOfKeyPoint(); 

                        
                        Mat mat = new Mat();
                        Imgproc.cvtColor(dst, mat, Imgproc.COLOR_GRAY2RGBA);
                        FeatureDetector fast = FeatureDetector.create(FeatureDetector.FAST);
                        fast.detect(mat, points);


                        Scalar redcolor = new Scalar(255,0,0);
                    
                        Mat mRgba= mat.clone();
                        Imgproc.cvtColor(mat, mRgba, Imgproc.COLOR_RGBA2RGB);
                        //Core.line(mRgba, new Point(100, 100), new Point(300,300), new Scalar(0, 0, 255));
                        KeyPoint[] point = points.toArray();
                        //Log.d("test", "the points length is "+ point.length);
                        //Calculate the max min X and Y.
                        double[] thres_x = new double[point.length];
                        double[] thres_y = new double[point.length];
                        for(int i =0; i<point.length; i++){
                        	thres_x[i]= point[i].pt.x;
                        	thres_y[i]= point[i].pt.y;
                        	Log.d("test","keypoint size is " + point[i].size);
                        }
                        Arrays.sort(thres_x);
                        Arrays.sort(thres_y);
                    	//Log.d("test", "the max threshold points x,y is "+ thres_x[point.length-1] +" "+thres_y[point.length-1] +"\n");
                    	//Log.d("test", "the min threshold points x,y is "+ thres_x[0] +" "+thres_y[0] +"\n");
                    	lefttop[0] = thres_x[0];
                    	lefttop[1] = thres_y[0];
                    	righttop[0] = thres_x[point.length-1];
                    	righttop[1] = thres_y[0];
                    	leftbot[0] = thres_x[0];
                    	leftbot[1] = thres_y[point.length-1];
                    	rightbot[0] = thres_x[point.length-1];
                    	rightbot[1] = thres_y[point.length-1];
                    	Point leftTop = new Point();
                    	Point rightTop = new Point();
                    	Point leftBot = new Point();
                    	Point rightBot = new Point();
                    	leftTop.set(lefttop);
                    	rightTop.set(righttop);
                    	leftBot.set(leftbot);
                    	rightBot.set(rightbot);
                    	KeyPoint KleftTop = new KeyPoint();
                    	KeyPoint Krighttop = new KeyPoint();
                    	KeyPoint Kleftbot = new KeyPoint();
                    	KeyPoint Krightbot = new KeyPoint();
                    	KleftTop.pt = leftTop;
                    	Krighttop.pt =rightTop;
                    	Kleftbot.pt = leftBot;
                    	Krightbot.pt = rightBot;
                    	KleftTop.size = 7f;
                    	Krighttop.size =7f;
                    	Kleftbot.size = 7f;
                    	Krightbot.size = 7f;
                    	KeyPoint[] corner_point = {KleftTop, Krighttop, Kleftbot, Krightbot};
                    	Log.d("test", "The new length is " + corner_point.length);
                    	MatOfKeyPoint corner_points = new MatOfKeyPoint(corner_point);
                    	//corner_points.fromArray(corner_point);
                        Features2d.drawKeypoints(mRgba, corner_points, mRgba, redcolor, 4);
                        Imgproc.cvtColor(mRgba, mat, Imgproc.COLOR_RGB2RGBA);
                       
                        

                        Utils.matToBitmap(mat,bitmap);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            	image.setImageBitmap(bitmap);

                           }
                       });
                        
                        bicolor = bitmap;
                        
                		}
                		else
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                	image.setImageBitmap(bicolor);

                               }
                           });
                			
                    	return null;
                    }
                    @Override
                    protected void onPostExecute(Void result)
                    {	
                    	if(progress.isShowing())
                        		progress.dismiss();   
                        edge.setText("Original");          
                        
                        View detectView=getLayoutInflater().inflate(R.layout.detectalert, null);
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setView(detectView).
                        setMessage("Please proceed to analysis only when all the corners been detected.\n (At least one red circle on each corner)").
                        setTitle("Alert").
            			setNegativeButton("Okay!", new OnClickListener(){
            			@Override
            			public void onClick(DialogInterface dialog, int which){
            				dialog.cancel();
            			}
            			}
            			).
            			setPositiveButton("Why?", new OnClickListener() {
            				
            				@Override
            				public void onClick(DialogInterface dialog, int which) {
            					
            				}
            			}).show();
                        
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

                        lp.copyFrom(alertDialog.getWindow().getAttributes());
                        lp.width = 700;
                        lp.height = 800;
                        lp.x=0;
                        lp.y=150;
                        alertDialog.getWindow().setAttributes(lp);
                        
                    }
                }.execute();
                
                

            }
            
           
            
            else if(status == 2){
            		edge.setText("Detect");
            		v.setTag(1);
            		image.setImageBitmap(org_bitmap);
            	}
            }
        });

	}


	
	public Bitmap smooth(Bitmap temp){
		Bitmap temp2 = temp.copy(Bitmap.Config.ARGB_8888, true );
		
		int width = temp.getWidth();
		int height = temp.getHeight();
		for(int i = 1; i<height-1; i++){
			for(int j = 1; j<width-1; j++){
				int rgb = temp.getPixel(j, i);
				int rgb1 = temp.getPixel(j-1, i-1);
				int rgb2 = temp.getPixel(j, i-1);
				int rgb3 = temp.getPixel(j+1, i-1);
				int rgb4 = temp.getPixel(j-1, i);
				int rgb5 = temp.getPixel(j+1, i);
				int rgb6 = temp.getPixel(j-1, i+1);
				int rgb7 = temp.getPixel(j, i+1);
				int rgb8 = temp.getPixel(j+1, i+1);
				if(rgb != Color.BLACK &&  rgb1!=Color.BLACK &&  rgb2!=Color.BLACK &&  rgb3!=Color.BLACK &&  rgb4!=Color.BLACK &&  rgb5!=Color.BLACK &&  rgb6!=Color.BLACK &&  rgb7!=Color.BLACK &&  rgb8!=Color.BLACK) 
					temp2.setPixel(j,i,rgb);
				else{
					temp2.setPixel(j, i, getIntFromColor(0,0,0));
				}
			}
		}
				return temp2;
	}
	
	
	public int getIntFromColor(int Red, int Green, int Blue){
	    Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
	    Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
	    Blue = Blue & 0x000000FF; //Mask out anything not blue.

	    return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		bicolor = null;
		TextView bg = (TextView) this.findViewById(R.id.infoText);
		final Button edge = (Button) this.findViewById(R.id.edge);
		edge.setText("Detect");
		edge.setTag(1);
		bg.setVisibility(View.VISIBLE);
		
		Log.d("Test", "Back button take me here!");
		if(resultCode == RESULT_OK)
			Log.d("Test", "Back result is okay!");
		else{
			Log.d("Test", "Back result is not okay!");
			  Intent intent = getIntent();
			   finish();
			   startActivity(intent);
			   Log.d("Test", "Activity restart.");
		
		
		}
		//photo selected
		 if (requestCode == 1 && resultCode == RESULT_OK && null != data) {  
		        
		        bg.setVisibility(View.GONE);
			 	selectedImage = data.getData();  
		        String[] filePathColumn = { MediaStore.Images.Media.DATA };  
		  
		        Cursor cursor = getContentResolver().query(selectedImage,  
		                filePathColumn, null, null, null);  
		        cursor.moveToFirst();  
		  
		        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);  
		        String picturePath = cursor.getString(columnIndex);  
		        cursor.close();  
		          
		        ImageView imageView = (ImageView) findViewById(R.id.iv);  
		        Log.d("test", picturePath);
		        pictureName = picturePath;
		        imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath)); 
		        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
		        org_bitmap = bitmap;
		        Log.i("test", Integer.toString(bitmap.getHeight()) + Integer.toString(bitmap.getWidth()));
		      
		    } 
		 //photo taken
		 if(requestCode == 2 && resultCode == RESULT_OK && null != data)
		 {
			 bg.setVisibility(View.GONE);
			 
			
		        if (data.hasExtra("myData1")) {
		            Toast.makeText(this, data.getExtras().getString("myData1"),
		                Toast.LENGTH_SHORT).show();
		            String path = data.getStringExtra("image");
		            File appDir = new File(Environment.getExternalStorageDirectory(), "Pictures");
		            String fileName = path + ".jpg";
		            
		            File image = new File(appDir, fileName);
		            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		            Log.d("test", image.getAbsolutePath());
		            pictureName = image.getAbsolutePath();
		            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
		            org_bitmap = bitmap;
		            ImageView imageview = (ImageView)this.findViewById(R.id.iv);
		            imageview.setImageBitmap(bitmap);
		        }
		          
		 }

		
		
	}
	
	
	
	
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		
		MenuInflater inflater=new MenuInflater(this);
		inflater.inflate(R.menu.mainactivitymenu, menu);
		setIconEnable(menu,true);
		return super.onCreateOptionsMenu(menu);
//		return true;
			
	}
	 private void setIconEnable(Menu menu, boolean enable)
	    {
	    	try 
	    	{
				Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");
				Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
				m.setAccessible(true);
				
				
				m.invoke(menu, enable);
	    		
			} catch (Exception e) 
			{
				e.printStackTrace();
			}
	    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.cameraopen:
			Intent cameraopenIntent=new Intent(MainActivity.this,SystemImage.class);
			SharedPreferences settings_put =getPreferences(MODE_PRIVATE);
			String focus_put = settings_put.getString("focus", null);
			String iso_put = settings_put.getString("iso", null);
			String exposure_put = settings_put.getString("exposure", null);
			if(focus_put == null)
				focus_put = "3.85";
			if(iso_put == null)
				iso_put = "50";
			if(exposure_put == null)
				exposure_put = "0";
			cameraopenIntent.putExtra("focus", focus_put);
			cameraopenIntent.putExtra("iso", iso_put);
			cameraopenIntent.putExtra("exposure", exposure_put);
			
			startActivityForResult(cameraopenIntent,2);
			
			break;
		case R.id.select:
			Intent imageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); 
			startActivityForResult(imageIntent, 1); 
			break;
			
		case R.id.del_data:
			DataMap map = null;
			SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
			Gson gson = new Gson();
			if(preferences.contains(DATA_NAME)){
				   Toast.makeText(MainActivity.this, "Reset the glucose data....",
			                Toast.LENGTH_LONG).show();
				   String mp = preferences.getString(DATA_NAME, null);
				   map = gson.fromJson(mp, DataMap.class);
				   map.clear();	
				   String serializedMap = gson.toJson(map);
				   SharedPreferences.Editor editor = preferences.edit();
				   //editor.putString(DATA_NAME, serializedMap); // value to store
				   editor.remove(DATA_NAME);
				   editor.commit();
			}	
			map = null;
			preferences = getSharedPreferences(PREF_OVU_NAME, MODE_PRIVATE);
			gson = new Gson();
			if(preferences.contains(DATA_OVU)){
				   Toast.makeText(MainActivity.this, "Reset the ovulation data....",
			                Toast.LENGTH_LONG).show();
				   String mp = preferences.getString(DATA_OVU, null);
				   map = gson.fromJson(mp, DataMap.class);
				   map.clear();	
				   String serializedMap = gson.toJson(map);
				   SharedPreferences.Editor editor = preferences.edit();
				   //editor.putString(DATA_OVU, serializedMap); // value to store
				   editor.remove(DATA_OVU);
				   editor.commit();
			}
			
			
			break;

		case R.id.settings:
			 SharedPreferences settings =getPreferences(MODE_PRIVATE);

			 View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.settings, null);
			 final EditText focusDefault = (EditText) view.findViewById(R.id.FoucsDistance);
			 final EditText ISODefault = (EditText) view.findViewById(R.id.ISO);
			 final EditText ExposureDefault = (EditText) view.findViewById(R.id.exposure);
			 String focus = settings.getString("focus", null);
			 String iso = settings.getString("iso", null);
			 String exposure = settings.getString("exposure", null);
			 if(focus == null)	
				 focusDefault.setText("3.85");
			 else
				 focusDefault.setText(focus);
			 if(iso == null)	 
				 ISODefault.setText("50");
			 else
				 ISODefault.setText(iso);
			 if(exposure == null)
			 	 ExposureDefault.setText("2");
			 else
				 ExposureDefault.setText(exposure);
			 
				 
				 


			 
			 AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
			 alertBuilder.setView(view);
			 alertBuilder.setCancelable(true)
			 .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					getFocus = focusDefault.getText();
					getISO = ISODefault.getText();
					getExposure = ExposureDefault.getText();
				    SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
				    editor.putString("focus", getFocus.toString());
				    editor.putString("iso", getISO.toString());
				    editor.putString("exposure", getExposure.toString());
				    editor.apply();
				}
			});
			 
			 Dialog dialog = alertBuilder.create();
			 
			 WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			    lp.copyFrom(dialog.getWindow().getAttributes());
			    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
			    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
			    dialog.show();
			    dialog.getWindow().setAttributes(lp);
			 
			    
			    

			
			 break;
		}
		return super.onOptionsItemSelected(item);
	}
}
