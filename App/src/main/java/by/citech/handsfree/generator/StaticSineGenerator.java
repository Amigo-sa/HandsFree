package by.citech.handsfree.generator;

import java.util.Arrays;

import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

import static by.citech.handsfree.generator.DataGeneratorFactory.MAX_SHORT;
import static by.citech.handsfree.generator.DataGeneratorFactory.QPP;
import static java.lang.Math.PI;

public class StaticSineGenerator {

    private static final double MULT = MAX_SHORT;
    private static short[][] staticSineShorts;
    private static final int DIV = 80;
    private static final double QUARTER = 90.0D;
    private static final double HALF = 180.0D;
    private static final double FREQUENCY = QUARTER/DIV;
    private static final double SHIFT = 0.0D;
    private static final double RAD2GRAD = PI / 180.0D;
    private static final double PHASE_0 = SHIFT + 0.0D;
    private static final double PHASE_1 = SHIFT + 90.0D;
    private static final double PHASE_2 = SHIFT + 180.0D;
    private static final double PHASE_3 = SHIFT + 270.0D;
    private static final double[] PHASE;

    static {
        PHASE = new double[]{PHASE_0, PHASE_1, PHASE_2, PHASE_3};
        staticSineShorts = new short[QPP][DIV];
        for (int j = 0; j  < QPP; j++) {
            for (int i = 0; i < DIV; i++) {
                staticSineShorts[j][i] = (short) (Math.sin(i * RAD2GRAD * FREQUENCY + RAD2GRAD * PHASE[j]) * MULT);
            }
        }
    }

    public static void printSine() {
        for (int i = 0; i < QPP; i++) {
            Timber.i("printSine value of sine chunk number %d: %s",
                    i, Arrays.toString(staticSineShorts[i]));
        }
    }

}
