package com.example.imageprocess;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.*;
import com.opencsv.CSVWriter;

public class AllFragmentTab extends Fragment{
	private String source = null;
	private int poi_x = 0;
	private int poi_y = 0;
	private int size = 0;
	
	
	
	public AllFragmentTab(String source, int poi_x, int poi_y, int poi_size){
		this.source =source;
		this.poi_x = poi_x;
		this.poi_y = poi_y;
		this.size = poi_size;
	}
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.all_layout, container, false);
        //TO DO, use x y to calculate other pad position. 
        //then get the average rgb and hsb information from them. 
        //Then write it to local
        //hsbValue hsb = rgb.rgb2hsb(rgb);
        ArrayList<rgbValue> rgbList = new ArrayList<rgbValue>();
        ArrayList<hsbValue> hsbList = new ArrayList<hsbValue>();
        //List sequence is: glucose, bilirubin, ketone, specific gravity, blood, ph, protein, uro-bilinogen, nitrite, leukocytes.
        Bitmap bmp = BitmapFactory.decodeFile(this.source);
        
        Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        Canvas canvas = new Canvas(mutableBitmap);
        int offset = 85;
        int offset_x = 75;
        canvas.drawCircle(this.poi_x, this.poi_y, 2, paint);
        canvas.drawCircle(this.poi_x, this.poi_y-offset, 2, paint);
        canvas.drawCircle(this.poi_x, this.poi_y-offset*2, 2, paint);
        canvas.drawCircle(this.poi_x, this.poi_y-offset*3, 2, paint);
        canvas.drawCircle(this.poi_x, this.poi_y-offset*4, 2, paint);
        canvas.drawCircle(this.poi_x+offset_x, this.poi_y, 2, paint);
        canvas.drawCircle(this.poi_x+offset_x, this.poi_y-offset, 2, paint);
        canvas.drawCircle(this.poi_x+offset_x, this.poi_y-offset*2, 2, paint);
        canvas.drawCircle(this.poi_x+offset_x, this.poi_y-offset*3, 2, paint);
        canvas.drawCircle(this.poi_x+offset_x, this.poi_y-offset*4, 2, paint);
        
        rgbList.add(getAvgRGB(poi_x,poi_y,this.source));
        rgbList.add(getAvgRGB(poi_x,poi_y-offset,this.source));
        rgbList.add(getAvgRGB(poi_x,poi_y-offset*2,this.source));
        rgbList.add(getAvgRGB(poi_x,poi_y-offset*3,this.source));
        rgbList.add(getAvgRGB(poi_x,poi_y-offset*4,this.source));
        rgbList.add(getAvgRGB(poi_x+offset_x,poi_y,this.source));
        rgbList.add(getAvgRGB(poi_x+offset_x,poi_y-offset,this.source));
        rgbList.add(getAvgRGB(poi_x+offset_x,poi_y-offset*2,this.source));
        rgbList.add(getAvgRGB(poi_x+offset_x,poi_y-offset*3,this.source));
        rgbList.add(getAvgRGB(poi_x+offset_x,poi_y-offset*4,this.source));
        
        for(rgbValue rgb : rgbList){
        	hsbList.add(rgbValue.rgb2hsb(rgb));
        }
        
        write2CSV(rgbList, hsbList);
        
	    ImageView imageview = (ImageView)rootView.findViewById(R.id.imageWithCircles);
	    imageview.setImageBitmap(mutableBitmap);
        
        //after populate the both lists, should write to local memory. 
        
            
        return rootView;
    }
	
	
	
	
	
	private rgbValue getAvgRGB(int x, int y, String photoPath){
		Bitmap image = BitmapFactory.decodeFile(photoPath);		
		int counter = 0;
		int argb = 0;
		int red=0;
		int green = 0;
		int blue = 0;
		for(int i = x-size; i<x+size; i++){
			for(int j = y-size;j<y+size; j++){
				argb = image.getPixel(i,j);
	  	    	red = red + Color.red(argb);
    	    	green =  green +Color.green(argb);
    	    	blue = blue +Color.blue(argb);
				counter++;
			}
		}
		red = red/counter;
		green = green/counter;
		blue = blue/counter;
		rgbValue result = new rgbValue(red,green,blue);
		return result;
	}
	
	
	private void write2CSV(ArrayList<rgbValue> rgbList, ArrayList<hsbValue> hsbList){
		String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
	    
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date curDate = new Date();
		String fileTime = sdf.format(curDate);
	    //System.out.println( sdf.format(cal.getTime()) );
		String fileName = "AllConcentrationTestLog";
		String filePath = baseDir + File.separator + fileName;
		File f = new File(filePath );
		CSVWriter writer = null; 
		FileWriter mFileWriter = null;
		// File exist
		if(f.exists() && !f.isDirectory()){
			try {
				mFileWriter = new FileWriter(filePath , true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writer = new CSVWriter(mFileWriter);
		}
		else {
			try {
				writer = new CSVWriter(new FileWriter(filePath));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String[] time = {fileTime,"The sequence of the data is red, blue, green, hue, saturation and brightness from left to right"};
		writer.writeNext(time);
		String[] Name = {"Glucose", "Bilirubin", "Ketone", "Specific gravity", "Blood", "pH", "Protein", "Uro-Bilinogen", 
				"Nitrite", "Leukocytes"};
		
		
		for(int i=0;i<Name.length; i++){
			rgbValue rgb = rgbList.get(i);
			hsbValue hsb = hsbList.get(i);			
			String[] data = {Name[i],Integer.toString(rgb.red),Integer.toString(rgb.green),Integer.toString(rgb.blue), 
					String.format("%.2f", hsb.hue),String.format("%.2f", hsb.saturation),String.format("%.2f", hsb.brightness)};
			writer.writeNext(data);
		}
		Toast.makeText(getActivity(), "All data written complete!", 
				   Toast.LENGTH_LONG).show();
		
		
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

}
	
	

}






