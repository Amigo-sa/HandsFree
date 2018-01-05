package by.citech.handsfree.generator;

import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

public abstract class DataGeneratorFactory implements IDataGenerator {

    private static final String STAG = Tags.Generator;

    public static IDataGenerator getDataGenerator(int buffSize, boolean isShorts, DataType dataType) throws Exception{
        if (buffSize < 1 || dataType == null) {
            throw new Exception(STAG + " " + StatusMessages.ERR_PARAMETERS);
        }
        switch (dataType) {
            case Sine:
                return new SineGenerator(buffSize, isShorts, 32767.0D, false);
            case Circle:
                return new CircleGenerator(buffSize, isShorts, 32767.0D, false);
        }
        return null;
    }

}
