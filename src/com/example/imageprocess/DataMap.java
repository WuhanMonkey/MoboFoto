package com.example.imageprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DataMap {
	private HashMap<String, ArrayList<DateValue>> map;
	
	public DataMap(){
		map = new HashMap<String, ArrayList<DateValue>>();
	}
	
	
	public void put(String name, DateValue dateValue){
		if(map.containsKey(name)){
			map.get(name).add(dateValue);
		}
		else{
			ArrayList<DateValue> list = new ArrayList<DateValue>();
			list.add(dateValue);
			map.put(name, list);
		}
	}
	
	public ArrayList get(String name){
		return map.get(name);
	}
	
	public void clear(){
		map.clear();
	}
	
	public boolean containsKey(String name){
		return map.containsKey(name);
	}
}
