package org.citruscircuits.super_scouter_2014_android;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Team")
public class Team extends ParseObject
{
	public int getNumber()
	{
		return this.getInt("number");
	}
	
	public String getName()
	{
		return this.getString("name");
	}
}
