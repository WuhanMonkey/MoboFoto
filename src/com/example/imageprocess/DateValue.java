package com.example.imageprocess;

import java.util.Date;

public class DateValue {
	private Date date;
	private float concentration;
	
	public DateValue(Date date, float concentration){
		this.date = date;
		this.concentration = concentration;
	}
	
	public Date getDate(){
		return date;
	}
	
	public float getConcentration(){
		return concentration;
	}
}
