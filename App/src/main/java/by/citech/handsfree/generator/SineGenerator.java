package by.citech.handsfree.generator;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;

import static by.citech.handsfree.util.ListHelper.getListInitiatedWithNulls;
import static by.citech.handsfree.util.MathHelper.arrayDoubleToShort;
import static java.lang.Math.PI;

class SineGenerator
        extends DataGeneratorFactory {

    private static final String TAG = Tags.SineGenerator;
    private static final String STAG = TAG + " ST";
    private static final boolean debug = Settings.debug;

    private static final double RAD_000 = 0.0D*PI;
    private static final double RAD_360 = 2.0D*PI;

    private final boolean isShorts;

    private List<short[]> quartersS;
    private short[] periodS;
    private double[] periodD;

    private short[][] preparedSineS;

    private final int length;
    private int chunkNumber;

    SineGenerator(int buffSize, boolean isShorts, double mult, boolean isPeriod) throws Exception {
        this.isShorts = isShorts;
        if (isPeriod) {
            preparedSineS = new short[1][buffSize];
            preparedSineS[0] = getPeriod(mult, buffSize);
        } else {
            preparedSineS = new short[QPP][buffSize];
            for (int i = 0; i < QPP; i++) {
                preparedSineS[i] = getQuarter(i+1, mult, buffSize);
            }
        }
        length = preparedSineS.length;
        for (int i = 0; i < length; i++) {
            if (debug) Log.i(TAG, String.format(
                    "constructor value of sine chunk number %d: %s",
                    i, Arrays.toString(preparedSineS[i]))
            );
        }
    }

    //--------------------- IDataGenerator

    @Override
    public short[] getNextDataShorts() {
        if (!isShorts) {
            if (debug) Log.e(TAG, "getNextDataShorts while !isShorts");
            return null;
        } else if (chunkNumber == (length - 1)) {
            chunkNumber = 0;
        } else {
            chunkNumber++;
        }
        return preparedSineS[chunkNumber];
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

    private short[] getQuarter(int quarterNum, double mult, int div) throws Exception {
        checkParameters(quarterNum, mult, div);

        if (quartersS == null) {
            quartersS = getListInitiatedWithNulls(QUARTER_1, QPP);
        } else if (quartersS.get(quarterNum) != null) {
            return quartersS.get(quarterNum);
        }

        if (debug) Log.i(TAG, String.format(
                "getQuarter: quarterNum = %d, mult = %s, div = %d",
                quarterNum, mult, div)
        );

        short[] quarter = new short[div];
        short[] period = getPeriod(mult, QPP * div);

        System.arraycopy(period, (quarterNum - 1) * div, quarter, 0, div);

        if (debug) Log.i(TAG, String.format(
                "getQuarter length of quarter is %d, value is %s",
                quarter.length, Arrays.toString(quarter))
        );

        quartersS.add(quarterNum, quarter);
        return quarter;
    }

    private short[] getPeriod(double mult, int div) throws Exception {
        checkParameters(mult, div);

        if (periodS == null) {
            periodS = new short[div];
        } else {
            return periodS;
        }

        if (debug) Log.i(TAG, String.format(
                "getPeriod: mult = %s, div = %d",
                mult, div)
        );

        short[] period = periodS;
        arrayDoubleToShort(getSinePeriod(mult, div), period);

        if (debug) Log.i(TAG, String.format(
                "getPeriod length of periodD is %d, value is %s",
                period.length, Arrays.toString(period))
        );

        return period;
    }

    private double[] getSinePeriod(double mult, int div) {
        if (periodD == null) {
            periodD = new double[div];
        } else {
            return periodD;
        }

        double delta = (RAD_360 - RAD_000) / (double) div;
        double[] sine = periodD;

        for (int i = 0; i < div; i++) {
            sine[i] = mult * Math.sin(delta * i);
        }

        if (debug) Log.i(TAG, String.format(
                "getSinePeriod length of sine is %d, value is %s",
                sine.length, Arrays.toString(sine))
        );

        return sine;
    }

}
