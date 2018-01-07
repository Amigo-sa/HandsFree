package by.citech.handsfree.generator;

import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

public abstract class DataGeneratorFactory
        implements IDataGenerator {

    private static final String STAG = Tags.Generator;
    static final double MAX_SHORT = 32767.0D;
    static final int QUARTER_1 = 1;
    static final int QUARTER_2 = 2;
    static final int QUARTER_3 = 3;
    static final int QUARTER_4 = 4;
    static final int QPP = 4;

    public static IDataGenerator getDataGenerator(int buffSize, boolean isShorts, DataType dataType) throws Exception {
        if (buffSize < 1 || dataType == null) {
            throw new Exception(STAG + " " + StatusMessages.ERR_PARAMETERS);
        }
        switch (dataType) {
            case Sine:
                return new SineGenerator(buffSize, isShorts, 300, true);
            case Circle:
                return new CircleGenerator(buffSize, isShorts, MAX_SHORT, false);
            default:
                throw new Exception(STAG + " " + StatusMessages.ERR_PARAMETERS + " " + dataType);
        }
    }

    //--------------------- checks

    static void checkParameters(int quarterNum, double mult, int div) throws Exception {
        if (quarterNum < QUARTER_1 || quarterNum > QUARTER_4 || mult < 1 || mult > MAX_SHORT || div < 1 || div > MAX_SHORT / QPP) {
            throw new Exception(STAG + " " + StatusMessages.ERR_PARAMETERS);
        }
    }

    static void checkParameters(double mult, int div) throws Exception {
        checkParameters(QUARTER_1, mult, QPP * div);
    }

}
