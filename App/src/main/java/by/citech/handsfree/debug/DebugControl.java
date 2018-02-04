package by.citech.handsfree.debug;

public class DebugControl {

//    private class DebugFsmListener implements DebugFsm.IDebugFsmListener {
//        @Override
//        public void onFsmStateChange(IFsmState from, IFsmState to, IFsmReport report) {
//            if (Settings.debug) Timber.i(TAG, "onFsmStateChange");
//            EDebugReport reportCasted = (EDebugReport) report;
//            switch (reportCasted) {
//                case RP_TurningOn:
//                case RP_StartDebug:
//                    switch (Settings.Common.opMode) {
//                        case DataGen2Bt:
//                        case AudIn2Bt:
//                            enableTransmitData();
//                            break;
//                        case Bt2Bt:
//                            if (getBLEState() == BluetoothLeState.SERVICES_DISCOVERED) {
//                                enableTransmitData();
//                            }
//                            break;
//                        case Record:
//                            if (getCallFsmState() == ECallState.ST_DebugRecord) {
//                                enableTransmitData();
//                            }
//                            break;
//                        case Bt2AudOut:
//                            onlyReceiveData();
//                            break;
//                        default:
//                            break;
//                    }
//                    break;
//                case RP_StopDebug:
//                    switch (Settings.Common.opMode) {
//                        default:
//                            disableTransmitData();
//                            break;
//                    }
//                    break;
//            }
//        }
//    }

}
