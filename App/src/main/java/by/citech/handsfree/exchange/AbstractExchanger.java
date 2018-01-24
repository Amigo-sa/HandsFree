package by.citech.handsfree.exchange;

import by.citech.vincentwillemvangogh.parameters.Tags;
import by.citech.vincentwillemvangogh.settings.Settings;

public abstract class AbstractExchanger {

    private static final String STAG = Tags.Exchanger + " ST";
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String NSTAG;
    private String TAG;
    static {objCount = 0;}
    {objCount++;NSTAG = STAG + " " + objCount;}

    private boolean isFinished;

    void setTag(String additionalTag) {
        if (additionalTag != null) {
            this.TAG = this.NSTAG + " " + additionalTag;
        }
    }

    boolean isFinished() {
        return isFinished;
    }

    void setFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    abstract void reset();
    abstract protected void process();
    abstract protected boolean isReady();

}
