package by.citech.handsfree.generator;

import android.util.Log;

import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.Settings;

import static by.citech.handsfree.util.MathHelper.arrayDoubleToShort;
import static by.citech.handsfree.util.MathHelper.invertDoubleArr;
import static by.citech.handsfree.util.MathHelper.revertDoubleArr;

public class CircleGenerator
        extends DataGeneratorFactory {

    private static final String TAG = Tags.CircleGenerator;
    private static final String STAG = TAG + " ST";
    private static final boolean debug = Settings.debug;

    private static final double MIN_COS = 0.0D;
    private static final double MAX_COS = 1.0D;

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
    public short[] getNextDataShorts() {
        if (!isShorts) {
            if (debug) Log.e(TAG, "getNextDataShorts while !isShorts");
            return null;
        } else if (chunkNumber == (length+1)) {
            chunkNumber = 0;
        } else {
            chunkNumber++;
        }
        return generatedDataShorts[chunkNumber];
    }

    @Override
    public byte[] getNextDataBytes() {
        if (isShorts) {
            if (debug) Log.e(TAG, "getNextDataBytes while isShorts");
            return null;
        } else {
            if (debug) Log.e(STAG, "getNextDataBytes not supported yet");
            return new byte[0];
        }
    }

    //--------------------- main

    private static short[] getQuarter(int quarterNum, double mult, int div) throws Exception {
        checkParameters(quarterNum, mult, div);

        if (debug) Log.i(STAG, String.format(
                "getQuarter: quarterNum = %d, mult = %s, div = %d",
                quarterNum, mult, div)
        );

        double[] quarterD = new double[div];

        switch (quarterNum) {
            case QUARTER_1:
                quarterD = getCircleQuarter(mult, div);
                break;
            case QUARTER_2:
                quarterD = getCircleQuarter(mult, div);
                quarterD = revertDoubleArr(quarterD);
                break;
            case QUARTER_3:
                quarterD = getCircleQuarter(mult, div);
                invertDoubleArr(quarterD);
                break;
            case QUARTER_4:
                quarterD = getCircleQuarter(mult, div);
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

    private static short[] getPeriod(double mult, int div) throws Exception {
        checkParameters(mult, div);

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

    private static double[] getCircleQuarter(double mult, int div) {
        double delta = (MAX_COS - MIN_COS) / (double) div;
        double[] cosine = new double[div];
        double[] sine = new double[div];

        for (int i = 0; i < div; i++) {
            cosine[i] = (double) i * delta;
            sine[i] = mult * Math.sqrt((1.0D - cosine[i]) * ((1.0D + cosine[i])));
        }

        return sine;
    }

}