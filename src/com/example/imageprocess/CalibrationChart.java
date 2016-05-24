package com.example.imageprocess;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

public class CalibrationChart {
	//the 5X3 chart will be stored in a two dimension array.
	//The left top cube (the 128 grey) is assigned to be (0,0)
	private int[][] red_chart = new int[5][3];
	private int[][] green_chart = new int[5][3];
	private int[][] blue_chart = new int[5][3];
	private Bitmap image;
	
	public CalibrationChart(String photoPath){
		this.image = BitmapFactory.decodeFile(photoPath);
		load();
	}
	
	private void load(){
		int width = image.getWidth();
		int height = image.getHeight();
		Log.i("test", "grey x y is " + width*15/58 + " "+ height*32/81);
		avgRGB(0,0,(int)width*15/58, (int)height*22/81);
		avgRGB(1,0,(int)width*23/58, (int)height*22/81);
		avgRGB(2,0, (int)width*32/58, (int)height*22/81);
		avgRGB(0,4, (int)width*15/58, (int)height*60/81);
		avgRGB(1,4, (int)width*23/58, (int)height*60/81);
		avgRGB(2,4, (int)width*32/58, (int)height*60/81);
		
		
		
	}
	
	private void avgRGB(int loc_x, int loc_y, int x, int y){
		int counter = 0;
		int size = 10;
		int argb = 0;
		for(int i = x-size; i<x+size; i++){
			for(int j = y-size;j<y+size; j++){
				argb = image.getPixel(i,j);
	  	    	red_chart[loc_y][loc_x] = red_chart[loc_y][loc_x] + Color.red(argb);
	  	    	green_chart[loc_y][loc_x] =  green_chart[loc_y][loc_x] +Color.green(argb);
	  	    	blue_chart[loc_y][loc_x] = blue_chart[loc_y][loc_x] +Color.blue(argb);
				counter++;
			}
		}
		red_chart[loc_y][loc_x] = red_chart[loc_y][loc_x]/counter;
		green_chart[loc_y][loc_x] = green_chart[loc_y][loc_x]/counter;
		blue_chart[loc_y][loc_x] = blue_chart[loc_y][loc_x]/counter;
	}
	
	public int getRed(int loc_x,int loc_y){
		return red_chart[loc_y][loc_x];
	}
	
	public int getGreen(int loc_x,int loc_y){
		return green_chart[loc_y][loc_x];
	}
	
	public int getBlue(int loc_x,int loc_y){
		return blue_chart[loc_y][loc_x];
	}
	
	
}
