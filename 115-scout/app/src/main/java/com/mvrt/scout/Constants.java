package com.mvrt.scout;

/**
 * Created by Lee Mracek on 10/16/2014.
 * Holds constants
 */
public class Constants {
    public enum Logging {
        MAIN_LOGCAT("com.mvrt.scout");

        public String getPath() {
            return path;
        }

        private String path;

        private Logging(String path) {
            this.path=path;
        }
    }
}
