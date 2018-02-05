package by.citech.handsfree.generator;

import android.util.Log;

import java.util.Arrays;

import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

import static by.citech.handsfree.util.ArrayHelper.arrayDoubleToShort;
import static by.citech.handsfree.util.ArrayHelper.invertDoubleArr;
import static by.citech.handsfree.util.ArrayHelper.getRevertedDoubleArr;

class CircleGenerator
        extends DataGeneratorFactory {

    private static final String TAG = Tags.CircleGenerator;
    private static final String STAG = TAG + " ST";
    private static final boolean debug = Settings.debug;

    private static final double MIN_COS = 0.0D;
    private static final double MAX_COS = 1.0D;

    private final boolean isShorts;

    private final int length;
    private short[][] circleShorts;
    private int chunkNumber;

    CircleGenerator(int buffSize, boolean isShorts, double mult, boolean isPeriod) throws Exception {
        this.isShorts = isShorts;
        if (isPeriod) {
            circleShorts = new short[1][buffSize];
            circleShorts[0] = getPeriod(mult, buffSize);
        } else {
            circleShorts = new short[QPP][buffSize];
            for (int i = 0; i < QPP; i++) {
                circleShorts[i] = getQuarter(i+1, mult, buffSize);
            }
        }
        length = circleShorts.length;
        for (int i = 0; i < length; i++) {
            Timber.i("constructor value of circle chunk number %d: %s",
                    i, Arrays.toString(circleShorts[i]));
        }
    }

    //--------------------- IDataGenerator

    @Override
    public short[] getNextDataShorts() {
        if (!isShorts) {
            Timber.e("getNextDataShorts while !isShorts");
            return null;
        } else if (chunkNumber == (length - 1)) {
            chunkNumber = 0;
        } else {
            chunkNumber++;
        }
        return circleShorts[chunkNumber];
    }

    @Override
    public byte[] getNextDataBytes() {
        if (isShorts) {
            Timber.e("getNextDataBytes while isShorts");
            return null;
        } else {
            Timber.e("getNextDataBytes not supported yet");
            return new byte[0];
        }
    }

    //--------------------- main

    private static short[] getQuarter(int quarterNum, double mult, int div) throws Exception {
        checkParameters(quarterNum, mult, div);

        Timber.i("getQuarter: quarterNum = %d, mult = %s, div = %d",
                quarterNum, mult, div);

        double[] quarterD = new double[div];

        switch (quarterNum) {
            case QUARTER_1:
                quarterD = getCircleQuarter(mult, div);
                break;
            case QUARTER_2:
                quarterD = getCircleQuarter(mult, div);
                quarterD = getRevertedDoubleArr(quarterD);
                break;
            case QUARTER_3:
                quarterD = getCircleQuarter(mult, div);
                invertDoubleArr(quarterD);
                break;
            case QUARTER_4:
                quarterD = getCircleQuarter(mult, div);
                quarterD = getRevertedDoubleArr(quarterD);
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

        Timber.i("getPeriod: mult = %s, div = %d",
                mult, div);

        int quarterDiv = div / QPP;
        Timber.i("getPeriod: quarterDiv = %s", quarterDiv);

        div = quarterDiv * QPP;
        Timber.i("getPeriod: div = %s", div);

        short[] period = new short[div];
        int quarterNum;

        for (int i = 0; i < QPP; i++) {
            quarterNum = QPP - i;
            Timber.i("getPeriod: processing qurter number %s", quarterNum);
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
