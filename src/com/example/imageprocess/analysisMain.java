package com.example.imageprocess;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import com.google.gson.Gson;

import android.app.ActionBar;
import android.app.ActionBar.TabListener;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


//The main class for colorimetric analysis
public class analysisMain extends Activity{
	private static String photoPath;
	 ActionBar.Tab glucoseTab, ovulationTab, allTab;
	 Fragment glucoseFragmentTab;	 
	 Fragment ovulationFragmentTab;	
	 Fragment allFragmentTab;
	 private int poi_size = 15;

	 private String analysisType = "";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.analysis_main);
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
		bar.setIcon(
				   new ColorDrawable(getResources().getColor(android.R.color.transparent))); 
        // Screen handling while hiding ActionBar icon.
		bar.setDisplayShowHomeEnabled(false);
        // Screen handling while hiding Actionbar title.
		bar.setDisplayShowTitleEnabled(false);
        // Creating ActionBar tabs.
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		Intent intent = getIntent();
		analysisType = intent.getStringExtra("type");
		final String PREF_FILE_NAME = "DataFile";
		final String DATA_NAME ="Data";
		final String PREF_OVU_NAME = "OvuFile";
		final String DATA_OVU ="Ovu";
		if(analysisType.equals("Glucose"))
			glucose_activity(intent, bar,  PREF_FILE_NAME,  DATA_NAME );
		else if(analysisType.equals("Ovulation"))		
			ovulation_activity(intent, bar, PREF_OVU_NAME, DATA_OVU);
		else if(analysisType.equals("All"))
			all_activity(intent,bar);
	
		

        
       	


		
		//CalibrationChart calibrationChart = new CalibrationChart(photoPath);
	}
	
	
	private void glucose_activity(Intent intent, ActionBar bar, String PREF_FILE_NAME, String DATA_NAME ){
		
		photoPath = intent.getStringExtra("photoPath");
		int glu_x = (int)intent.getDoubleExtra("poi_x", 0);
		int glu_y = (int)intent.getDoubleExtra("poi_y", 0);
		Glucose glucose = new Glucose(glu_x, glu_y, photoPath,poi_size);
        float result = Method.linear_regression(glucose.concentration, glucose.green_channel, glucose.green);
        String name = intent.getStringExtra("name");
      //Fetch history data if there is any
        DataMap map = fetch(name, result, PREF_FILE_NAME, DATA_NAME);
        glucoseTab = bar.newTab().setText("Glucose");
        glucoseFragmentTab = new GlucoseFragmentTab(photoPath, glu_x, glu_y,poi_size, glucose, map, name);
        glucoseTab.setTabListener(new tabListener(glucoseFragmentTab));
        bar.addTab(glucoseTab);
		/**new AlertDialog.Builder(analysisMain.this)
	    .setTitle("Report")
	    .setMessage("the red is " + glucose.red +" " + "the green is " + glucose.green + " " + "the blue is "+glucose.blue + "\n" + "the tested concentration is: " + result)
	     .show();**/
	}
	
	private void ovulation_activity(Intent intent, ActionBar bar, String PREF_OVU_NAME, String DATA_OVU ){
		int lefttop_x = (int)intent.getDoubleExtra("lefttop_x", 0);
		int lefttop_y = (int)intent.getDoubleExtra("lefttop_y", 0);
		int righttop_x = (int)intent.getDoubleExtra("righttop_x", 0);
		int righttop_y = (int)intent.getDoubleExtra("righttop_y", 0);
		int leftbot_x = (int)intent.getDoubleExtra("leftbot_x",0);
		int leftbot_y = (int)intent.getDoubleExtra("leftbot_y", 0);
		int rightbot_x = (int)intent.getDoubleExtra("rightbot_x", 0);
		int rightbot_y = (int)intent.getDoubleExtra("rightbot_y",0);
		photoPath = intent.getStringExtra("photoPath");
		String name = intent.getStringExtra("name");
		Bitmap bmp = BitmapFactory.decodeFile(photoPath);
		Bitmap resizedbitmap=Bitmap.createBitmap(bmp, lefttop_x, lefttop_y, rightbot_x-lefttop_x, rightbot_y-lefttop_y);
		ovulationTab = bar.newTab().setText("Ovulation");
		Ovulation ovulation = new Ovulation(resizedbitmap);
		float result = Method.linear_regression_float(ovulation.concentration_log, ovulation.ovulation_val, ovulation.ratio);
		result = (float) Math.pow(10.0, result);
        DataMap map = fetch(name, result, PREF_OVU_NAME, DATA_OVU);
        
		ovulationFragmentTab= new OvulationFragmentTab(resizedbitmap, map, ovulation, result, name);
		
		
        //ovulationFragmentTab = new OvulationFragmentTab(photoPath, glu_x, glu_y,poi_size, glucose, map, name);
        ovulationTab.setTabListener(new tabListener(ovulationFragmentTab));
        bar.addTab(ovulationTab);
	}
	
	private void all_activity(Intent intent, ActionBar bar){
		
		int poi_x = (int)intent.getDoubleExtra("poi_x", 0);
		int poi_y = (int)intent.getDoubleExtra("poi_y", 0);
		photoPath = intent.getStringExtra("photoPath");
		allTab = bar.newTab().setText("Calibrate All");
        allFragmentTab = new AllFragmentTab(photoPath, poi_x, poi_y,poi_size);
        allTab.setTabListener(new tabListener(allFragmentTab));
        bar.addTab(allTab);
	}
	
	
	private DataMap fetch(String name, float result, String PREF_FILE_NAME, String DATA_NAME ){
		Log.i("test", DATA_NAME + " " + PREF_FILE_NAME);
			DataMap map = null;
		   SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
		   Gson gson = new Gson();
		   if(!preferences.contains(DATA_NAME)){
			   Toast.makeText(analysisMain.this, "The entire database does not exist! Going to make a new one",
		                Toast.LENGTH_SHORT).show();
			   map = new DataMap();
			   Calendar calendar = Calendar.getInstance();
			   
			   Log.i("test", "The result is: " + Float.toString(result));
			   Date date = calendar.getTime();
			   map.put(name, new DateValue(date, 1));	
			   calendar.add(Calendar.SECOND, 5);
			   date = calendar.getTime();
			   map.put(name, new DateValue(date, result));	
			   //Serialize to string for preference storage
			   
			   String serializedMap = gson.toJson(map);
			   SharedPreferences.Editor editor = preferences.edit();
			   editor.putString(DATA_NAME, serializedMap); // value to store
			   editor.commit();
		   }
		   else{
			   //deserialize it if there is data exist
			   Log.i("test", "The result is: " + Float.toString(result));
			   Toast.makeText(analysisMain.this, "Fetching data....",
		                Toast.LENGTH_SHORT).show();
			   String mp = preferences.getString(DATA_NAME, null);
			   map = gson.fromJson(mp, DataMap.class);
			   Calendar calendar = Calendar.getInstance();
			   Date date = calendar.getTime();
			   if(!map.containsKey(name))
				   map.put(name, new DateValue(date, 1));
			   calendar.add(Calendar.SECOND, 5);
			   date = calendar.getTime();
			   map.put(name, new DateValue(date, result));
			   //Serialize to string for preference storage
			   
			   String serializedMap = gson.toJson(map);
			   SharedPreferences.Editor editor = preferences.edit();
			   editor.putString(DATA_NAME, serializedMap); // value to store
			   editor.commit();
		   }
		   
		   return map;
		   
	}



}
