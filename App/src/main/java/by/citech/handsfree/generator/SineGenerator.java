package by.citech.handsfree.generator;

import android.util.Log;

import java.util.Arrays;

import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.Settings;

import static by.citech.handsfree.util.MathHelper.arrayDoubleToShort;
import static java.lang.Math.PI;

public class SineGenerator
        extends DataGeneratorFactory {

    private static final String TAG = Tags.SineGenerator;
    private static final String STAG = TAG + " ST";
    private static final boolean debug = Settings.debug;

    private static final double RAD_000 = 0.0D*PI;
    private static final double RAD_360 = 2.0D*PI;

    private static final double MULT = MAX_SHORT;
    private static final int DIV = 80;

    private final boolean isShorts;

    private final int length;
    private short[][] generatedDataShorts;
    private int chunkNumber;

    static {
        short[][] generatedDataShorts = new short[QPP][DIV];
        for (int i = 0; i < DIV; i++) {
            generatedDataShorts[0][i] = (short) (Math.sin((2.0D * PI / (double) DIV) * (double) i) * MULT);
            generatedDataShorts[1][i] = (short) (Math.sin((double) i * PI / 180.0D * 2.82D / 4.0D + PI / 180.0D * 90.0D) * MULT);
            generatedDataShorts[2][i] = (short) (Math.sin((double) i * PI / 180.0D * 2.82D / 4.0D + PI / 180.0D * 180.0D) * MULT);
            generatedDataShorts[3][i] = (short) (Math.sin((double) i * PI / 180.0D * 2.82D / 4.0D + PI / 180.0D * 270.0D) * MULT);
        }
        for (int i = 0; i < QPP; i++) {
            if (debug) Log.i(STAG, String.format(
                    "static value of sine chunk number %d: %s",
                    i, Arrays.toString(generatedDataShorts[i]))
            );
        }
    }

    SineGenerator(int buffSize, boolean isShorts, double mult, boolean isPeriod) throws Exception {
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

        short[] quarter = new short[div];
        short[] period = getPeriod(mult, QPP * div);

        System.arraycopy(period, (quarterNum - 1) * div, quarter, 0, div);

        return quarter;
    }

    private static short[] getPeriod(double mult, int div) throws Exception {
        checkParameters(mult, div);

        if (debug) Log.i(STAG, String.format(
                "getPeriod: mult = %s, div = %d",
                mult, div)
        );

        short[] period = new short[div];
        arrayDoubleToShort(getSinePeriod(mult, div), period);
        return period;
    }

    private static double[] getSinePeriod(double mult, int div) {
        double delta = (RAD_360 - RAD_000) / (double) div;
        double[] sine = new double[div];

        for (int i = 0; i < div; i++) {
            sine[i] = mult * Math.sin(delta * i);
        }

        return sine;
    }

}
