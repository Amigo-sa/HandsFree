package by.citech.handsfree.generator;

import android.util.Log;

import java.util.Arrays;

import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.Settings;

import static by.citech.handsfree.util.MathHelper.arrayDoubleToShort;
import static by.citech.handsfree.util.MathHelper.invertDoubleArr;
import static by.citech.handsfree.util.MathHelper.revertDoubleArr;
import static java.lang.Math.PI;

public class SineGenerator
        extends DataGeneratorFactory {

    private static final String STAG = Tags.SineGenerator;
    private static final boolean debug = Settings.debug;

    private static final int DATA_SIZE = 80;
    private static final int QPP = 4;

    private final boolean isShorts;

    private final int length;
    private short[][] generatedDataShorts;
    private int chunkNumber;

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
        return new byte[0];
    }

    //--------------------- main

    public static short[] getQuarter(int quarterNum, double mult, int div) throws Exception {
        if (quarterNum < 1 || quarterNum > 4 || mult < 1 || mult > 32768 || div < 1 || div > 32768) {
            throw new Exception(StatusMessages.ERR_PARAMETERS);
        }
//      generatedDataShorts = new short[QPP][DATA_SIZE];
//      for (int i = 0; i < DATA_SIZE; i++) {
//          generatedDataShorts[0][i] = (short) (Math.sin((2.0D * PI / (double) DATA_SIZE) * (double) i) * multiplier);
//          generatedDataShorts[1][i] = (short) (Math.sin((double) i * PI / 180.0D * 2.82D / 4.0D + PI / 180.0D * 90.0D) * multiplier);
//          generatedDataShorts[2][i] = (short) (Math.sin((double) i * PI / 180.0D * 2.82D / 4.0D + PI / 180.0D * 180.0D) * multiplier);
//          generatedDataShorts[3][i] = (short) (Math.sin((double) i * PI / 180.0D * 2.82D / 4.0D + PI / 180.0D * 270.0D) * multiplier);
//      }
//      for (int i = 0; i < QPP; i++) {
//          if (debug) Log.i(STAG, String.format(
//                  "static value of sine chunk number %d: %s",
//                  i, Arrays.toString(generatedDataShorts[i]))
//          );
//      }
        return null;
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
