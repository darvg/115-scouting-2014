package com.mvrt.scout;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lee on 10/21/14.
 */
public class Match {
    private int matchNumber;
    private List<Team> redAlliance = Arrays.asList(new Team(), new Team(), new Team());
    private List<Team> blueAlliance = Arrays.asList(new Team(), new Team(), new Team());

    public int getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(int matchNumber) {
        this.matchNumber = matchNumber;
    }

    public void setRedAllianceJSON(JSONArray redAlliance) {
        for (int i = 0; i < redAlliance.length(); i++) {
            try {
                this.redAlliance.set(i, new Team(redAlliance.getInt(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setBlueAllianceJSON(JSONArray blueAlliance) {
        for (int i = 0; i < blueAlliance.length(); i++) {
            try {
                this.blueAlliance.set(i, new Team(blueAlliance.getInt(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Team> getRedAlliance() {
        return redAlliance;
    }

    public List<Team> getBlueAlliance() {
        return blueAlliance;
    }
}
