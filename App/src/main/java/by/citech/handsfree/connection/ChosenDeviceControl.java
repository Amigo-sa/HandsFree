package by.citech.handsfree.connection;

import by.citech.handsfree.activity.fsm.ActivityFsm;
import by.citech.handsfree.activity.fsm.EActivityReport;
import by.citech.handsfree.activity.fsm.EActivityState;
import by.citech.handsfree.connection.fsm.IConnectionFsmReporter;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;

import static by.citech.handsfree.application.ThisApp.setBtConnectedAddr;

public class ChosenDeviceControl
    implements ActivityFsm.IActivityFsmListener, IConnectionFsmReporter {

    private static final String STAG = Tags.ChosenDeviceControl;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    //--------------------- singleton

    private static volatile ChosenDeviceControl instance = null;

    private ChosenDeviceControl() {
    }

    public static ChosenDeviceControl getInstance() {
        if (instance == null) {
            synchronized (ChosenDeviceControl.class) {
                if (instance == null) {
                    instance = new ChosenDeviceControl();
                }
            }
        }
        return instance;
    }

    //--------------------- IActivityFsmListener

    @Override
    public void onActivityFsmStateChange(EActivityState from, EActivityState to, EActivityReport why) {
        switch (to) {
//            case LightA:
//                if (isValidAddr(Settings.Bluetooth.btChosenAddr)) {
//                    if (isValidAddr(getBtConnectedAddr())
//                            && !Settings.Bluetooth.btChosenAddr.matches(getBtConnectedAddr())) {
//                        reportToBtFsm(getBtFsmState(), ReportSearchStop, TAG);
//                        reportToBtFsm(getBtFsmState(), ReportConnectStop, TAG);
//                    } else reportToBtFsm(getBtFsmState(), ReportChosenDeviceValid, TAG);
//                } else reportToBtFsm(getBtFsmState(), ReportChosenDeviceInvalid, TAG);
//                break;
            case SettingsA:
                setBtConnectedAddr(Settings.Bluetooth.btChosenAddr);
                break;
//            case ScanA:
//                setBtConnectedAddr(Settings.Bluetooth.btChosenAddr);
//                break;
            default:
                break;
        }
    }

}
