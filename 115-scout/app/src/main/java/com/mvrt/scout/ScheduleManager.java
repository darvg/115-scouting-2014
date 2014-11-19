package com.mvrt.scout;

import android.net.ConnectivityManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by akhil on 11/15/14.
 */
public class ScheduleManager {

    private ArrayList<Match> matchSchedule;
    ConnectivityManager connManager;
    int currentMatch = 1;

    public ScheduleManager(){
        currentMatch = 1;
    }

    public ScheduleManager(int currentMatch){
        this.currentMatch = currentMatch;
    }

    public void loadScheduleFromFile(File schedule){
        char[] rawRead = new char[(int) schedule.length()];

        try {
            FileReader reader = new FileReader(schedule);
            reader.read(rawRead);
            reader.close();
        } catch (FileNotFoundException e) { }
        catch (IOException e) {}

        loadScheduleFromJSON(String.valueOf(rawRead));
    }

    public void loadScheduleFromJSON(String JSON){

        matchSchedule = new ArrayList<Match>();
        try {
            JSONObject jsonFile = new JSONObject(JSON);
            JSONArray matchArray = jsonFile.getJSONArray("qualificationSchedule");
            for (int i = 0; i < matchArray.length(); i++) {
                JSONObject matchObject = matchArray.getJSONObject(i);
                Match match = new Match();
                match.setMatchNumber(matchObject.getInt("matchNumber"));
                Log.d(Constants.Logging.MAIN_LOGCAT.getPath(), "" + matchObject.getJSONArray("redAlliance"));
                match.setRedAllianceJSON(matchObject.getJSONArray("redAlliance"));
                match.setBlueAllianceJSON(matchObject.getJSONArray("blueAlliance"));
                matchSchedule.add(match);
            }
        } catch (JSONException e) {

        }
    }

    public Match getMatch(int matchNumber){
        if(matchSchedule.size() <= matchNumber)matchNumber = matchSchedule.size() - 1;
        if(matchNumber == -1)return null;
        return matchSchedule.get(matchNumber);
    }

    public void setMatch(Match m){
        matchSchedule.set(m.getMatchNumber() - 1, m);
    }

    public Match getCurrentMatch(){
        return matchSchedule.get(currentMatch - 1); //shifts from 0-indexed to 1-indexed
    }

    public int getCurrentMatchNo(){ return currentMatch; }

    public void setCurrentMatch(int match){
        currentMatch = match;
    }

}
