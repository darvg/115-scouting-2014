package com.mvrt.scout;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by akhil on 11/18/14.
 */
public abstract class DataCollectionFragment extends Fragment {

    public final String PREFERENCES_FILE = "com.mvrt.scout.preferences";
    public final String PREFERENCES_SCOUT_KEY = "scoutid";

    ScheduleManager scheduleManager;
    SharedPreferences preferences;

    int scoutID = 0;

    boolean isRed() {
        return scoutID <= 3;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        preferences = getActivity().getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        scheduleManager = ((ScoutBase)getActivity().getApplication()).getScheduleManager();

        scoutID = preferences.getInt(PREFERENCES_SCOUT_KEY, 1);
        if (scoutID < 1 || scoutID > 6)
            scoutID = 1;
    }

    @Override
    public void onPause(){
        super.onPause();
        getDataFromUI();
    }

    public abstract void getDataFromUI();

    public DataCollectionFragment(){ }

}
