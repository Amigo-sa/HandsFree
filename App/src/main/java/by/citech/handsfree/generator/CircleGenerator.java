package by.citech.handsfree.generator;

import android.util.Log;

import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.Settings;

import static by.citech.handsfree.util.MathHelper.arrayDoubleToShort;
import static by.citech.handsfree.util.MathHelper.invertDoubleArr;
import static by.citech.handsfree.util.MathHelper.revertDoubleArr;

public class CircleGenerator
        extends DataGeneratorFactory {

    private static final String STAG = Tags.CircleGenerator;
    private static final boolean debug = Settings.debug;

    private static final double MAX_COS = 1.0D;
    private static final double MIN_COS = 0.0D;
    private static final int QUARTER_1 = 1;
    private static final int QUARTER_2 = 2;
    private static final int QUARTER_3 = 3;
    private static final int QUARTER_4 = 4;
    private static final int QPP = 4;

    private final boolean isShorts;

    private final int length;
    private short[][] generatedDataShorts;
    private int chunkNumber;

    CircleGenerator(int buffSize, boolean isShorts, double mult, boolean isPeriod) throws Exception {
        this.isShorts = isShorts;
        if (isPeriod) {
            generatedDataShorts = new short[0][buffSize];
            length = 0;
            generatedDataShorts[0] = getPeriod(mult, buffSize);
        } else {
            generatedDataShorts = new short[QPP][buffSize];
            length = QPP;
            for (int i = 0; i < QPP; i++) {
                generatedDataShorts[i] = getQuarter(QPP-1, mult, buffSize);
            }
        }
    }

    //--------------------- IDataGenerator

    @Override
    public short[] getDataShorts() {
        if (!isShorts) return null;
        if (chunkNumber == (length+1)) {
            chunkNumber = 0;
        } else {
            chunkNumber++;
        }
        return generatedDataShorts[chunkNumber];
    }

    @Override
    public byte[] getDataBytes() {
        if (isShorts) return null;
        return new byte[0];
    }

    //--------------------- main

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
            cos[i] = (double) i * delta;
            sin[i] = mult * Math.sqrt((1.0D - cos[i]) * ((1.0D + cos[i])));
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
        if (mult < 1 || mult > 32768 || div < 1 || div > 32768 / QPP) {
            throw new Exception(StatusMessages.ERR_PARAMETERS);
        }

        if (debug) Log.i(STAG, String.format(
                "getPeriod: mult = %s, div = %d",
                mult, div)
        );

        int quarterDiv = div / QPP;
        if (debug) Log.i(STAG, "getPeriod: quarterDiv = " + quarterDiv);

        div = quarterDiv * QPP;
        if (debug) Log.i(STAG, "getPeriod: div = " + div);

        short[] period = new short[div];
        int quarterNum;
        for (int i = 0; i < QPP; i++) {
            quarterNum = QPP - i;
            if (debug) Log.i(STAG, "getPeriod: processing qurter number " + quarterNum);
            System.arraycopy(getQuarter(quarterNum, mult, quarterDiv), 0, period, quarterDiv * i, quarterDiv);
        }
        return period;
    }

}
