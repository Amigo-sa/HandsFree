package by.citech.handsfree.util;

import by.citech.handsfree.settings.Settings;

public class Measurement {

    public static void timeStamp(final String TAG, boolean eventOne, boolean eventTwo){
       long prevTime, deltaTime;
        if (!Settings.debug) {
            if (eventOne)
                prevTime = System.currentTimeMillis();
            if (eventTwo) {
                //TODO: это статический метод, инициилизируй переменные
//                deltaTime = System.currentTimeMillis() - prevTime;
//                Log.i(TAG, "timeStamp = " + deltaTime);
            }
        }
    }



}
