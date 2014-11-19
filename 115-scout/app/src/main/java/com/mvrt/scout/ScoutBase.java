package com.mvrt.scout;

import android.app.Application;
import android.content.Context;

/**
 * Created by Lee Mracek on 10/20/14.
 * Serves as Application wrapper
 */
public class ScoutBase extends Application {
    private static Context context;

    private ScheduleManager schm;

    public static Context getAppContext() {
        return context;
    }

    public void onCreate() {
        context = getApplicationContext();
        schm = new ScheduleManager();
    }

    public ScheduleManager getScheduleManager(){
        return schm;
    }

}
