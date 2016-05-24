package com.example.imageprocess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Ovulation {
	private Bitmap bmp = null;
	private int left_x =0;
	private int left_y =0;
	private int right_x=0;
	private int right_y=0;
	public Bitmap detected_bmp=null;
    public TreeMap<Integer, Integer> red_map = new TreeMap<Integer, Integer>();
	//public float ovulation_val[] = {0,0.001f,0.0257f,0.4f,0.562f};
	public float ovulation_val[] = {0,0.001f,0.94f,2.78f,6.0f};

	public float concentration_log[] = {0, 0.92942f, 1.9294f, 2.9294f, 3.9294f};
	public float ratio = 0;
	
	public Ovulation(Bitmap bmp){
		this.bmp = bmp;
		detected_bmp = detect_rect(bmp);
		scan_red();
}
	
	
	private void scan_red(){
		//left anchor point, left_x
		//right anchor point, right_x
		//need to scan left_y to 0

		for(int i = left_y; i>=0; i--){
			
			
			for(int j = left_x; j<=right_x; j++){
				int argb = 0;
				int red =0;
				int green =0;
				int blue =0;
				argb = bmp.getPixel(j, i);
	  	    	red = red + Color.red(argb);
    	    	green =  green +Color.green(argb);
    	    	blue = blue +Color.blue(argb);
    	    	rgbValue rgb = new rgbValue(red, green, blue);
    	    	hsbValue hsb = rgb.rgb2hsb(rgb);
    	    	
    	    	if(hsb.brightness<0.9 && red <220 && green <180){
    	    		
    	    		if(!red_map.containsKey(i))
    	    			red_map.put(i, 1);
    	    		else
    	    			red_map.put(i, red_map.get(i)+1);
    	    		

    	    	}
				
			}
		}
		int num_red = 0;
		int min = Integer.MAX_VALUE;
		int num_red_control = 0;
		int min_control = Integer.MAX_VALUE;
		int control_ctr=0;
		int ctr= 0;
		Iterator num_itr = red_map.entrySet().iterator();
		while(num_itr.hasNext()){
			Map.Entry i = (Entry) num_itr.next();
			int key = (Integer) i.getKey();
			int val = (Integer) i.getValue();
			//if(key>=5 && key<=20){
			//Logan modified. 
			if(key>=65 && key<=80){
				control_ctr++;
				num_red_control+=val;
				min_control = Math.min(min_control, val);

			}
			//if(key>=55 && key<=70){
			if(key>=115 && key<=130){
				ctr++;
				min = Math.min(min, val);
				num_red += val;

							
			}
		}
		control_ctr = control_ctr==0?1:control_ctr;
		ctr = ctr==0?1:ctr;
		min = ctr==15?min:0;
		min_control= control_ctr==15?min_control:0;
		Log.d("test", "The control is " + num_red_control +" The test is " + num_red );
		num_red = num_red - min*ctr;
		num_red_control = num_red_control - min_control*control_ctr;
		Log.d("test", "The min control is " + min_control +" The min test is " + min);
		ratio = (float)num_red/(float)(num_red_control);
		Log.d("test", "The control is " + num_red_control +" The test is " + num_red + " the ratio is " + ratio);
		
	
		

		
		//float result = Method.linear_regression_float(concentration_log, ovulation_val, ratio);
		

	}
	
	//find and locate the black rectangle. 
		private Bitmap detect_rect(Bitmap originalPhoto){
			


	            Mat imgMat=new Mat();

	            Utils.bitmapToMat(originalPhoto,imgMat);

	        Mat imgSource=imgMat.clone();

	        Imgproc.cvtColor( imgMat, imgMat, Imgproc.COLOR_BGR2GRAY);
	        Bitmap grayscale=Bitmap.createBitmap(imgMat.cols(),imgMat.rows(),Bitmap.Config.ARGB_8888);
	        Utils.matToBitmap(imgMat,grayscale);



	        Imgproc.Canny(imgMat,imgMat,0,255);
	        Bitmap canny=Bitmap.createBitmap(imgMat.cols(),imgMat.rows(),Bitmap.Config.ARGB_8888);
	        Utils.matToBitmap(imgMat,canny);

	        Imgproc.GaussianBlur(imgMat, imgMat, new  org.opencv.core.Size(1, 1), 2, 2);
	        Bitmap blur=Bitmap.createBitmap(imgMat.cols(),imgMat.rows(),Bitmap.Config.ARGB_8888);
	        Utils.matToBitmap(imgMat,blur);

	        //find the contours
	        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	        Imgproc.findContours(imgMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);

	        MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point

	        for (int idx = 0; idx < contours.size(); idx++) {
	            temp_contour = contours.get(idx);
	                //check if this contour is a square

	                MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );

	                int contourSize = (int)temp_contour.total();
	                MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
	                Imgproc.approxPolyDP(new_mat, approxCurve_temp, contourSize*0.05, true);

	                if (approxCurve_temp.total() == 4) {
	                    MatOfPoint points = new MatOfPoint( approxCurve_temp.toArray() );
	                    Rect rect = Imgproc.boundingRect(points);
	                    if(rect.width > 30 && rect.width/rect.height>1){
	                    //Log.i("test", "The width is: " + rect.width + "The height is: " + rect.height + "\n");
	                    left_x = rect.x;
	                    left_y = rect.y;
	                    right_x = rect.x+rect.width;
	                    right_y = rect.y;
	                    
	                    Core.rectangle(imgSource, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar(255, 0, 0, 255), 3);
	                    break;
	                    }
	                }
	                
	        }
	        Bitmap analyzed=Bitmap.createBitmap(imgSource.cols(),imgSource.rows(),Bitmap.Config.ARGB_8888);
	        Utils.matToBitmap(imgSource,analyzed);
	        return analyzed;
		}
}
