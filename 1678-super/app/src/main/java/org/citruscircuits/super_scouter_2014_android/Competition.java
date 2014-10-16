package org.citruscircuits.super_scouter_2014_android;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;

@ParseClassName("Competition")
public class Competition extends ParseObject
{
	public List<Match> findAndFetchQualificationSchedule()
	{	
		List<Match> schedule = (List<Match>) this.get("matches");
		
		if(schedule == null) {
			return new ArrayList<Match>();
		}
		
		try
		{
			Log.e("stupid logcat", "Fetching schedule");
			
//			ParseObject.fetchAllIfNeeded(schedule);

			Log.e("stupid logcat", "Fetching teams");
			int i = 0;
			for(Match match : schedule)
			{
				Log.e("stupid logcat", "Match id: " + match.getObjectId());

				match = match.fetchIfNeeded();
				
				ParseObject.fetchAllIfNeeded(match.getRedAlliance());
				ParseObject.fetchAllIfNeeded(match.getBlueAlliance());
				
				i++;
				
				Log.e("stupid logcat", (float)i / schedule.size() * 100 + "% done fetching schedule");
			}
		}
		catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return schedule;
	}
}
