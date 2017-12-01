package by.citech.param;

import android.util.Log;

/**
 * Created by tretyak on 01.12.2017.
 */

public class Measurement {

    public static void timeStamp(final String TAG, boolean eventOne, boolean eventTwo){
       long prevTime, deltaTime;
        if (!Settings.debug) {
            if (eventOne)
                prevTime = System.currentTimeMillis();
            if (eventTwo) {
                deltaTime = System.currentTimeMillis() - prevTime;
                Log.i(TAG, "timeStamp = " + deltaTime);
            }
        }
    }



}
