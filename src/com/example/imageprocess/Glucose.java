package com.example.imageprocess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

public class Glucose {
	public int red=0;
	public int green=0;
	public int blue=0;
	public int x;
	public int y;
	private int size = 0;
	public HashMap<Double, rgbValue> map;
	public float[] concentration = {0, 10, 20, 30,40, 50,60,70, 80, 90, 100};
	public int[] green_channel = {235, 215, 175, 156, 135, 115, 110, 97,91,70,54};
	public TreeMap<Integer, Integer> treeMap = new TreeMap();
	public Glucose(){
		
	}
	
	public Glucose(int x, int y, String photoPath, int size){
		this.x = x;
		this.y = y;
		this.size = size;
		getAvgRGB(x, y, photoPath);
		map = new HashMap<Double, rgbValue>();

	}
	
	private void getAvgRGB(int x, int y, String photoPath){
		Bitmap image = BitmapFactory.decodeFile(photoPath);		
		int counter = 0;
		int argb = 0;
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i = x-size; i<x+size; i++){
			for(int j = y-size;j<y+size; j++){
				argb = image.getPixel(i,j);
	  	    	red = red + Color.red(argb);
    	    	green =  green +Color.green(argb);
    	    	blue = blue +Color.blue(argb);
				counter++;
				list.add(Color.green(argb));
			}
		}
		red = red/counter;
		green = green/counter;
		blue = blue/counter;
		
		Collections.sort(list);
		Iterator itr = list.iterator();
		while(itr.hasNext()){
			int i = (Integer) itr.next();
			if(treeMap.containsKey(i))
				treeMap.put(i, treeMap.get(i)+1);
			else
				treeMap.put(i,1);
			//Log.i("test", "green val: " + itr.next());
		}
		
		Iterator map_itr = treeMap.entrySet().iterator();
		int ctr=0;
		while(map_itr.hasNext()){
			Map.Entry i = (Entry) map_itr.next();
			if((Integer)i.getValue()>15){
				green+=(Integer)i.getKey()*(Integer)i.getValue();
				ctr+=(Integer)i.getValue();
			}
				
			Log.i("test", "green statistic: " + i.getKey() +" "+ i.getValue());
		}
		green/=ctr;
		/**green = 0;
		for(int i = 0; i<list.size(); i++){
			if(i>list.size()*3/10 && i<list.size()*9/10)
				green+=list.get(i);
		}
		green/=list.size()*3/5;
		**/
	}
	
	
	
	
}
