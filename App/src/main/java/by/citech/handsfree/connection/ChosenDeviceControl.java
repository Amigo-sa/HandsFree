package by.citech.handsfree.connection;

import by.citech.handsfree.activity.fsm.EActivityReport;
import by.citech.handsfree.activity.fsm.EActivityState;
import by.citech.handsfree.activity.fsm.IActivityFsmListener;
import by.citech.handsfree.connection.fsm.IConnectionFsmReporter;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;

import static by.citech.handsfree.application.ThisApplication.getBtConnectedAddr;
import static by.citech.handsfree.application.ThisApplication.setBtConnectedAddr;
import static by.citech.handsfree.connection.fsm.EConnectionReport.ChosenDeviceFailedTheCheck;
import static by.citech.handsfree.connection.fsm.EConnectionReport.ChosenDevicePassedTheCheck;
import static by.citech.handsfree.connection.fsm.EConnectionReport.ConnectStopped;
import static by.citech.handsfree.connection.fsm.EConnectionReport.SearchStopped;
import static by.citech.handsfree.util.BluetoothHelper.isValidAddr;

public class ChosenDeviceControl
    implements IActivityFsmListener, IConnectionFsmReporter {

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
            case LightA:
                if (isValidAddr(Settings.Bluetooth.btChosenAddr)) {
                    if (isValidAddr(getBtConnectedAddr())
                            && !Settings.Bluetooth.btChosenAddr.matches(getBtConnectedAddr())) {
                        reportToConnectionFsm(getConnectionFsmState(), SearchStopped, TAG);
                        reportToConnectionFsm(getConnectionFsmState(), ConnectStopped, TAG);
                    } else reportToConnectionFsm(getConnectionFsmState(), ChosenDevicePassedTheCheck, TAG);
                } else reportToConnectionFsm(getConnectionFsmState(), ChosenDeviceFailedTheCheck, TAG);
                break;
            case SettingsA:
                setBtConnectedAddr(Settings.Bluetooth.btChosenAddr);
                break;
            case ScanA:
                setBtConnectedAddr(Settings.Bluetooth.btChosenAddr);
                break;
            default:
                break;
        }
    }

}
