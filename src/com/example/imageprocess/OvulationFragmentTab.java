package com.example.imageprocess;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
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


public class OvulationFragmentTab extends Fragment {
	private String source = null;
	private int ovu_x = 0;
	private int ovu_y = 0;
	private int size = 0;
	private DataMap map;
	private String name = "";
	private Bitmap bmp = null;
	private Ovulation ovulation;
	private float result=0;
	

	public OvulationFragmentTab(Bitmap bmp, DataMap map, Ovulation ovulation, float result, String name){
			this.bmp = bmp;
			this.map = map;
			this.ovulation = ovulation;
			this.result = result;
			this.name = name;
	}
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.ovulation_layout, container, false);
        
	    ImageView imageview = (ImageView)rootView.findViewById(R.id.ovu_small_scale);
	    //imageview.setImageBitmap(getResizedBitmap(bmp, 240, 240));
	    imageview.setImageBitmap(ovulation.detected_bmp);
	    plot_trend(rootView);
	    plot_analysis(rootView);

	    
	    
	    //imageview.setImageBitmap(detect_rect(detected_bmp));
 
	    
	    //Need to detect the red pixel here.
	    
	    
	    
	    

	    
        return rootView;
    }
	
	private void plot_trend(View rootView){
		final SimpleDateFormat df = new SimpleDateFormat("MM-dd");
		ArrayList list = map.get(name);
		if(list == null){
			   Toast.makeText(getActivity(), "Something wrong....",
		                Toast.LENGTH_SHORT).show();
			   return;
		}
		TextView t = (TextView)rootView.findViewById(R.id.ovulationTrend);
		t.setText("Ovulation trend for " + name);
		GraphView graph = (GraphView) rootView.findViewById(R.id.trend_graph);
		LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
		series.setDrawDataPoints(true);
		series.setDataPointsRadius(5f);
		DateAsXAxisLabelFormatter timeLabelFormat = new DateAsXAxisLabelFormatter(rootView.getContext(), df);
		graph.getGridLabelRenderer().setLabelFormatter(timeLabelFormat);

		Iterator itr = list.iterator();
		DateValue date = null;
		while(itr.hasNext()){
			date = (DateValue) itr.next();
			
			series.appendData(new DataPoint(date.getDate(), date.getConcentration()), true, 10);
			Log.i("test","con from map: " + date.getConcentration()+ " "+date.getDate());
		}
        t = (TextView)rootView.findViewById(R.id.ovu_report_name);
        t.setText("Patient Name: " + name + "\n");
        t = (TextView)rootView.findViewById(R.id.ovu_report_date);
        t.setText("Test Date: " + df.format(date.getDate()) + "\n");
        t = (TextView)rootView.findViewById(R.id.ovu_report_concentration);
        t.setText("Ovulation Concentration: " + String.format("%.3f", date.getConcentration()) + "U/ml\n");
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
            	Toast.makeText(getActivity(), "Concentration measured on "+df.format(dataPoint.getX()) +" is " +  String.format("%.3f", dataPoint.getY()) +"U/ml", Toast.LENGTH_SHORT).show();
                        }
        });
		graph.addSeries(series);


	}
	
	
	private void plot_analysis(View rootView){
		//left anchor point, left_x
		//right anchor point, right_x
		//need to scan left_y to 0	    
		GraphView graph = (GraphView) rootView.findViewById(R.id.graph);
	    graph.getViewport().setXAxisBoundsManual(true);
	    graph.getViewport().setYAxisBoundsManual(true);
		graph.getViewport().setMinX(0);
		graph.getViewport().setMaxX(500);
		graph.getViewport().setMinY(0);
		graph.getViewport().setMaxY(50);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        Iterator map_itr = ovulation.red_map.entrySet().iterator();
		while(map_itr.hasNext()){
			Map.Entry i = (Entry) map_itr.next();
			Log.i("test", Integer.toString((Integer) i.getKey()) +" "+ Integer.toString((Integer) i.getValue()));
			series.appendData(new DataPoint((Integer)i.getKey(),(Integer)i.getValue()), true, 3000);
		}
		



		graph.addSeries(series);
		
	}
	
	
	
}
