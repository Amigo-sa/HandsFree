package by.citech.param;

public enum OpMode {

    Bt2Bt {@Override public String getSettingName() {return "Bt2Bt";}},
    Net2Net {@Override public String getSettingName() {return "Net2Net";}},
    Record {@Override public String getSettingName() {return "Record";}},
    Bt2AudOut {@Override public String getSettingName() {return "Bt2AudOut";}},
    AudIn2Bt {@Override public String getSettingName() {return "AudIn2Bt";}},
    AudIn2AudOut {@Override public String getSettingName() {return "AudIn2AudOut";}},
    Normal {@Override public String getSettingName() {return "Normal";}};

    public abstract String getSettingName();
}
