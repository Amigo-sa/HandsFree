package by.citech.handsfree.util;

import android.util.Log;

import java.util.Arrays;

import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.Settings;

import static by.citech.handsfree.util.MathHelper.arrayDoubleToShort;
import static by.citech.handsfree.util.MathHelper.invertDoubleArr;
import static by.citech.handsfree.util.MathHelper.revertDoubleArr;

public class DataGenerator {

    private static final String STAG = Tags.DataGenerator;
    private static final boolean debug = Settings.debug;
    private static final double M_PI = Math.PI;
    private static final double MAX_COS = 1.0D;
    private static final double MIN_COS = 0.0D;
    private static final int QUARTER_1 = 1;
    private static final int QUARTER_2 = 2;
    private static final int QUARTER_3 = 3;
    private static final int QUARTER_4 = 4;
    private static final int QPP = 4;

    public static class Sine {

        public static class PseudoSine {

            private static final int DATA_SIZE = 80;
            private static final double MULT = 32767.0D;
            private static final short[][] period;

            static {
                period = new short[QPP][DATA_SIZE];
                for (int i = 0; i < DATA_SIZE; i++) {
                    period[0][i] = (short) (Math.sin((2.0D * M_PI / (double) DATA_SIZE) * (double) i) * MULT);
                    period[1][i] = (short) (Math.sin((double) i * M_PI / 180.0D * 2.82D / 4.0D + M_PI / 180.0D * 90.0D) * MULT);
                    period[2][i] = (short) (Math.sin((double) i * M_PI / 180.0D * 2.82D / 4.0D + M_PI / 180.0D * 180.0D) * MULT);
                    period[3][i] = (short) (Math.sin((double) i * M_PI / 180.0D * 2.82D / 4.0D + M_PI / 180.0D * 270.0D) * MULT);
                }
                for (int i = 0; i < QPP; i++) {
                    if (debug) Log.i(STAG, String.format(
                            "static value of sine chunk number %d: %s",
                            i, Arrays.toString(period[i]))
                    );
                }
            }

            public static short[] getPeriod(int quarterNum) throws Exception {
                if (quarterNum < 1 || quarterNum > 4) {
                    throw new Exception(StatusMessages.ERR_PARAMETERS);
                }
                return period[quarterNum+1];
            }

        }

        public static class TrueSine {

            public static short[] getQuarter(int quarterNum, double mult, int div) throws Exception {
                if (quarterNum < 1 || quarterNum > 4 || mult < 1 || mult > 32768 || div < 1 || div > 32768) {
                    throw new Exception(StatusMessages.ERR_PARAMETERS);
                }

                if (debug) Log.i(STAG, String.format(
                        "getQuarter: quarterNum = %d, mult = %s, div = %d",
                        quarterNum, mult, div)
                );

                double delta = (MAX_COS - MIN_COS) / (double) div;
                double[] cos = new double[div];
                double[] sin = new double[div];

                for (int i = 0; i < div; i++) {
                    cos[i] =  (double) i * delta;
                    sin[i] = mult*Math.sqrt((1.0D-cos[i])*((1.0D+cos[i])));
                }

                double[] quarterD = new double[div];

                switch (quarterNum) {
                    case QUARTER_1:
                        quarterD = sin;
                        break;
                    case QUARTER_2:
                        quarterD = sin;
                        quarterD = revertDoubleArr(quarterD);
                        break;
                    case QUARTER_3:
                        quarterD = sin;
                        invertDoubleArr(quarterD);
                        break;
                    case QUARTER_4:
                        quarterD = sin;
                        quarterD = revertDoubleArr(quarterD);
                        invertDoubleArr(quarterD);
                        break;
                    default:
                        break;
                }

                short[] quarterS = new short[div];
                arrayDoubleToShort(quarterD, quarterS);
                return quarterS;
            }


            public static short[] getPeriod(double mult, int div) throws Exception {
                if (mult < 1 || mult > 32768 || div < 1 || div > 32768/QPP) {
                    throw new Exception(StatusMessages.ERR_PARAMETERS);
                }

                if (debug) Log.i(STAG, String.format(
                        "getPeriod: mult = %s, div = %d",
                        mult, div)
                );

                int quarterDiv = div/QPP;
                if (debug) Log.i(STAG, "getPeriod: quarterDiv = " + quarterDiv);

                div = quarterDiv*QPP;
                if (debug) Log.i(STAG, "getPeriod: div = " + div);

                short[] period = new short[div];
                int quarterNum;
                for (int i = 0; i < QPP; i++) {
                    quarterNum = QPP - i;
                    if (debug) Log.i(STAG, "getPeriod: processing qurter number " + quarterNum);
                    System.arraycopy(getQuarter(quarterNum, mult, quarterDiv), 0, period, quarterDiv*i, quarterDiv);
                }
                return period;
            }
        }

    }

}
