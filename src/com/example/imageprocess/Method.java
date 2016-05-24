package com.example.imageprocess;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import android.util.Log;

public class Method {
	public Method(){
		
	}
	
	
	
	public static float linear_regression_nonfit(float[] concentration, int[] k, int target_k){
    	float result = 0;
    	SimpleRegression regression = new SimpleRegression();
    	for(int i = 0; i<concentration.length; i++){
    		regression.addData(concentration[i], k[i]);
    	}

    	// displays intercept of regression line
    	Log.i("Testing", "Intercept is: " + regression.getIntercept());
    	// displays slope of regression line
    	Log.i("Testing", "slope is: " + regression.getSlope());
    	// displays slope standard error
    	Log.i("Testing", "standard error is: " + regression.getSlopeStdErr());
    	
    	result = (float) (target_k - regression.getIntercept())/((float)regression.getSlope());
    	
    	return result;
    	
    	
    }
	
	public static float linear_regression(float[] concentration, int[] k, int target_k){
			if(target_k>k[0] || target_k< k[k.length-1])
				return linear_regression_nonfit(concentration, k, target_k);
		
			int i = 0;
			for(i = 0; i<k.length-1; i++){
				if(k[i]>=target_k && k[i+1]<=target_k)
						break;
			}
			SimpleRegression regression = new SimpleRegression();
			regression.addData(concentration[i], k[i]);
			regression.addData(concentration[i+1], k[i+1]);
			if(i>0)
				regression.addData(concentration[i-1],k[i-1]);
			if(i<k.length-1)
				regression.addData(concentration[i+1],k[i+1]);
			
			return (float) (target_k - regression.getIntercept())/((float)regression.getSlope());
			

    	
    	
    }
	
	public static float linear_regression_float(float[] concentration, float [] k, float target_k){
		SimpleRegression regression = new SimpleRegression();
		int len = k.length;
		if(target_k>=k[len-1]){
			regression.addData(concentration[len-1], k[len-1]);
			regression.addData(concentration[len-2], k[len-2]);
		}
		else if(target_k<=k[0]){
			regression.addData(concentration[0],k[0]);
			regression.addData(concentration[1],k[1]);
		}
		else{
			int i;
			for(i = 0; i<k.length-1; i++){
				if(k[i]<=target_k && k[i+1]>=target_k)
						break;
			}
			
			regression.addData(concentration[i], k[i]);
			regression.addData(concentration[i+1], k[i+1]);	
		}
		
		
		
		return (float) (target_k - regression.getIntercept())/((float)regression.getSlope());
		

	
	
}
	
	
}
