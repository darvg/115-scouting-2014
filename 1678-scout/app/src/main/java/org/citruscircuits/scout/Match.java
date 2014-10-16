package org.citruscircuits.scout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.parse.ParseClassName;
import com.parse.ParseObject;


@ParseClassName("Match")
public class Match extends ParseObject
{
	// Accessors for alliances
	public List<Integer> redTeamNumbers;
	public List<Integer> blueTeamNumbers;
	
	public List<Team> getRedAlliance()
	{
		return (List<Team>) this.get("redAlliance");
	}

	public void setRedAllianceTeamNumbers(JSONArray redTeams) { // DOES NOT SAVE TO DATABASE
		redTeamNumbers = new ArrayList<Integer>();
		try {
			redTeamNumbers.add(redTeams.getInt(0));
			redTeamNumbers.add(redTeams.getInt(1));
			redTeamNumbers.add(redTeams.getInt(2));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void setBlueAllianceTeamNumbers(JSONArray blueTeams) { // DOES NOT SAVE TO DATABASE
		blueTeamNumbers = new ArrayList<Integer>();
		try {
			blueTeamNumbers.add(blueTeams.getInt(0));
			blueTeamNumbers.add(blueTeams.getInt(1));
			blueTeamNumbers.add(blueTeams.getInt(2));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public List<Team> getBlueAlliance()
	{
		return (List<Team>) this.get("blueAlliance");
	}

	// Accessors for "matchNumber"
	public String getMatchNumber()
	{
		return this.getString("matchNumber");
	}

	public void setMatchNumber(String matchNumber)
	{
		this.put("matchNumber", matchNumber);
	}

	// Accessors for "redAllianceSuperScoutData"
	public HashMap<String, HashMap<String, Object>> getRedAllianceSuperScoutData()
	{
		return (HashMap<String, HashMap<String, Object>>) this.get("redAllianceSuperScoutData");
	}

	public void setRedAllianceSuperScoutData(HashMap<String, HashMap<String, Object>> superScoutData)
	{
		this.put("redAllianceSuperScoutData", superScoutData);
	}

	// Accessors for "blueAllianceSuperScoutData"
	public HashMap<String, HashMap<String, Object>> getBlueAllianceSuperScoutData()
	{
		return (HashMap<String, HashMap<String, Object>>) this.get("blueAllianceSuperScoutData");
	}

	public void setBlueAllianceSuperScoutData(HashMap<String, HashMap<String, Object>> superScoutData)
	{
		this.put("blueAllianceSuperScoutData", superScoutData);
	}
	
	public ParseObject getData()
	{
		return this;
	}
}
