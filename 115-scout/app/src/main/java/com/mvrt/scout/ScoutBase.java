package com.mvrt.scout;

import android.app.Application;
import android.content.Context;

/**
 * Created by Lee Mracek on 10/20/14.
 * Serves as Application wrapper
 */
public class ScoutBase extends Application {
    private static Context context;

    public static Context getAppContext() {
        return context;
    }

    public void onCreate() {
        context = getApplicationContext();
    }
}
