package com.example.imageprocess;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
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
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.*;
import com.jjoe64.graphview.series.Series;

public class GlucoseFragmentTab extends Fragment{
	private String source = null;
	private int glu_x = 0;
	private int glu_y = 0;
	private int size = 0;
	private Glucose glucose;
	private DataMap map;
	private String name = "";
	public GlucoseFragmentTab(String source, int glu_x, int glu_y, int size, Glucose glucose, DataMap map, String name){
		this.source = source;
		this.glu_x = glu_x;
		this.glu_y = glu_y;
		this.size = size;
		this.glucose = glucose;
		this.map = map;
		this.name = name;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.glucose_layout, container, false);
      //Load image
        display_part(rootView); 
        
        TreeMap map = glucose.treeMap;
     // generate Dates

        GraphView graph = (GraphView) rootView.findViewById(R.id.graph);
        BarGraphSeries<DataPoint> series = new BarGraphSeries<DataPoint>();
        Iterator map_itr = map.entrySet().iterator();
		while(map_itr.hasNext()){
			Map.Entry i = (Entry) map_itr.next();
			series.appendData(new DataPoint((Integer)i.getKey(),(Integer)i.getValue()), true, 900);
		}
        
        
        
        graph.addSeries(series);
        

        //Populate data
        plotByName(rootView);
        
     // set date label formatter
        //graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
        //graph.getGridLabelRenderer().setNumHorizontalLabels(2); // only 4 because of the space

        // set manual x bounds to have nice steps
        //graph.getViewport().setMinX(d1.getTime());
        //graph.getViewport().setMaxX(d3.getTime());
        //graph.getViewport().setXAxisBoundsManual(true);
        
        return rootView;
    }
	
	private void plotByName(View rootView){
		final SimpleDateFormat df = new SimpleDateFormat("MM-dd");
		ArrayList list = map.get(name);
		if(list == null){
			   Toast.makeText(getActivity(), "Something wrong....",
		                Toast.LENGTH_SHORT).show();
			   return;
		}
		TextView t = (TextView)rootView.findViewById(R.id.glucoseTrend);
		t.setText("Glucose trend for " + name);
		GraphView graph = (GraphView) rootView.findViewById(R.id.trend_graph);
		
		Viewport vp = graph.getViewport();
		LegendRenderer lr = graph.getLegendRenderer();
		lr.isVisible();
		
		vp.isScalable();
		vp.isScrollable();
		LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
		series.setDrawDataPoints(true);
		series.setDataPointsRadius(5f);
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
            	Toast.makeText(getActivity(), "Concentration measured on "+df.format(dataPoint.getX()) +" is " +  String.format("%.3f", dataPoint.getY()) +" mmol", Toast.LENGTH_SHORT).show();
                        }
        });
		Iterator itr = list.iterator();
		DateValue date = null;
		while(itr.hasNext()){
			date = (DateValue) itr.next();
			
			series.appendData(new DataPoint(date.getDate(), date.getConcentration()), true, 10);
		}
        t = (TextView)rootView.findViewById(R.id.report_name);
        t.setText("Patient Name: " + name + "\n");
        t = (TextView)rootView.findViewById(R.id.report_date);
        t.setText("Test Date: " + df.format(date.getDate()) + "\n");
        t = (TextView)rootView.findViewById(R.id.report_concentration);
        t.setText("Glucose Concentration: " + String.format("%.3f", date.getConcentration()) + "mmol\n");
		graph.addSeries(series);
		DateAsXAxisLabelFormatter timeLabelFormat = new DateAsXAxisLabelFormatter(rootView.getContext(), df);
		graph.getGridLabelRenderer().setLabelFormatter(timeLabelFormat);

			
	}
	
	
	private void display_part(View rootView){
		Bitmap bmp = BitmapFactory.decodeFile(source);
		Bitmap resizedbitmap=Bitmap.createBitmap(bmp, glu_x-size,glu_y-size,size*2, size*2);
	    ImageView imageview = (ImageView)rootView.findViewById(R.id.small_scale);
	    imageview.setImageBitmap(getResizedBitmap(resizedbitmap, 240, 240));
	}
	
	
	private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    float scaleWidth = ((float) newWidth) / width;
	    float scaleHeight = ((float) newHeight) / height;
	    // CREATE A MATRIX FOR THE MANIPULATION
	    Matrix matrix = new Matrix();
	    // RESIZE THE BIT MAP
	    matrix.postScale(scaleWidth, scaleHeight);

	    // "RECREATE" THE NEW BITMAP
	    Bitmap resizedBitmap = Bitmap.createBitmap(
	        bm, 0, 0, width, height, matrix, false);
	    bm.recycle();
	    return resizedBitmap;
	}
}
