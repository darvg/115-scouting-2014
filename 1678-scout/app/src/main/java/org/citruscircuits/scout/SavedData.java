package org.citruscircuits.scout;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class SavedData {
	private static HashMap<String, Integer> autoData = new HashMap<String, Integer>();
	private static HashMap<String, Integer> teleData = new HashMap<String, Integer>();
	
	public static void createHashMaps()
	{
		autoData.put("Make High Hot", 0);
		autoData.put("Make High Cold", 0);
		autoData.put("Miss High", 0);
		autoData.put("Make Low Hot", 0);
		autoData.put("Make Low Cold", 0);
		autoData.put("Miss Low", 0);
		autoData.put("G. Receive", 0);
		autoData.put("Eject", 0);
		autoData.put("Mobility", 0);
		// Tele
		teleData.put("Make High", 0);
		teleData.put("Miss High", 0);
		teleData.put("Make Low", 0);
		teleData.put("Miss Low", 0);
		teleData.put("HP Receive", 0);
		teleData.put("HP Receive Fail", 0);
		teleData.put("G. Receive", 0); //FML.
		teleData.put("Eject", 0);
		teleData.put("Truss", 0);
		teleData.put("Catch", 0);
		teleData.put("Goalie Block", 0);

	}
	
	public static Integer getAutoData(String key)
	{
		return autoData.get(key);
	}
	
	public static Integer getTeleData(String key)
	{
		return teleData.get(key);
	}
	
	public static void setAutoData(String key, Integer value)
	{
		autoData.put(key, value);
	}
	
	public static void setTeleData(String key, Integer value)
	{
		Log.e("log", "saving key=" + key + "  val=" + value);
		teleData.put(key, value);
	}
	
	public static String getJson(String matchNumber, Integer teamNumber, String scoutName, Boolean isRed)
	{
		JSONObject finalJson = new JSONObject();
		
		JSONObject autoArray = new JSONObject();
		for(Map.Entry<String, Integer> e : autoData.entrySet())
		{
		    Log.e("asdf", e.getKey()+": "+e.getValue());
		    try {
				autoArray.put(e.getKey(), e.getValue());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		
		Log.e("rawr", teleData.entrySet().toString());
		
		JSONObject teleArray = new JSONObject();
		for(Map.Entry<String, Integer> e : teleData.entrySet())
		{
		    Log.e("asdf", e.getKey()+": "+e.getValue());
		    try {
				teleArray.put(e.getKey(), e.getValue());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		
		try {
			finalJson.put("auto", autoArray);
			finalJson.put("tele", teleArray);

			finalJson.put("matchNumber", matchNumber);
			finalJson.put("teamNumber", teamNumber);
			finalJson.put("scoutName", scoutName);
			finalJson.put("isRed", isRed);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		Log.e("asdf", finalJson.toString());
		return finalJson.toString();
	}
}
