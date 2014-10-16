package org.citruscircuits.super_scouter_2014_android;

import com.parse.Parse;
import com.parse.ParseObject;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application
{
	private static Context context;

	public void onCreate()
	{
		super.onCreate();
		MyApplication.context = getApplicationContext();

		// Initialize Parse
		ParseObject.registerSubclass(Team.class);
		ParseObject.registerSubclass(Match.class);
		ParseObject.registerSubclass(Competition.class);
		Parse.initialize(getApplicationContext(), "replaced for seurity, parse.com", "replaced for security read the docs.");
	}

	public static Context getAppContext()
	{
		return MyApplication.context;
	}
}
