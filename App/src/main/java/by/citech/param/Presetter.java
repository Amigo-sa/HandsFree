package by.citech.param;

public class Presetter {
    public static void setMode(OpMode opMode) {
        switch (opMode) {
            case Bt2Bt:
                Settings.opMode = OpMode.Bt2Bt;
                break;
            case AudIn2Bt:
                Settings.opMode = OpMode.AudIn2Bt;
                break;
            case Bt2AudOut:
                Settings.opMode = OpMode.Bt2AudOut;
                break;
            case AudIn2AudOut:
                Settings.opMode = OpMode.AudIn2AudOut;
                break;
            case Normal:
            default:
                Settings.opMode = OpMode.Normal;
                break;
        }
    }
}
