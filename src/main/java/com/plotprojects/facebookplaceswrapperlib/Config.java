package com.plotprojects.facebookplaceswrapperlib;

public class Config {
    static final String FB_API_VERSION = "v3.2";

    static final double MIN_ACCURACY_METERS = 200;
    static final double MAX_PLACE_DISTANCE_METERS = 250;
    static final long MIN_LAST_RUN_TIME_DIFF = 5 * 1000L; // 5 seconds

    public static final String LOG_TAG = "Plot/FB_Places";

    private Config() {
    }
}
