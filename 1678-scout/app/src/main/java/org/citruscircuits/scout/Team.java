package org.citruscircuits.scout;

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
