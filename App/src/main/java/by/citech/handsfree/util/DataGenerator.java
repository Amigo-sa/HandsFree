package by.citech.handsfree.util;

import android.util.Log;

import java.util.Arrays;

import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.Settings;

public class DataGenerator {

    public static class Sine {

        private static final String STAG = Tags.DataGenerator;
        private static final int DATA_SIZE = 80;
        private static final int SIN_CHUNK_NUM = 4;
        private static final double M_PI = 3.14;
        private static final short[][] sineData;

        static {
            sineData = new short[SIN_CHUNK_NUM][DATA_SIZE];
            for (int i = 0; i < DATA_SIZE; i++) {
                sineData[0][i] = (short) (Math.sin((2.0D * M_PI / (double) DATA_SIZE) * (double) i) * 32767.0D);
                sineData[1][i] = (short) (Math.sin((double) i * M_PI / 180.0D * 2.82D / 4.0D + M_PI / 180.0D * 90.0D) * 32767.0D);
                sineData[2][i] = (short) (Math.sin((double) i * M_PI / 180.0D * 2.82D / 4.0D + M_PI / 180.0D * 180.0D) * 32767.0D);
                sineData[3][i] = (short) (Math.sin((double) i * M_PI / 180.0D * 2.82D / 4.0D + M_PI / 180.0D * 270.0D) * 32767.0D);
            }
            for (int i = 0; i < SIN_CHUNK_NUM; i++) {
                if (Settings.debug) Log.i(STAG, String.format(
                        "static value of sinus chunk number %d: %s",
                        i, Arrays.toString(sineData[i]))
                );
            }
        }

        public static short[] getSineChunk(int sineChunk) {
            return sineData[sineChunk];
        }

    }

}
