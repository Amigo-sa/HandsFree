package by.citech.param;

public enum OpMode {

    Normal {
        @Override public String getSettingName() {return "Normal";}
        @Override public String getSettingNumber() {return "1";}
    },
    Bt2Bt {
        @Override public String getSettingName() {return "Bt2Bt";}
        @Override public String getSettingNumber() {return "2";}
    },
    Net2Net {
        @Override public String getSettingName() {return "Net2Net";}
        @Override public String getSettingNumber() {return "3";}
    },
    Record {
        @Override public String getSettingName() {return "Record";}
        @Override public String getSettingNumber() {return "4";}
    },
    Bt2AudOut {
        @Override public String getSettingName() {return "Bt2AudOut";}
        @Override public String getSettingNumber() {return "5";}
    },
    AudIn2Bt {
        @Override public String getSettingName() {return "AudIn2Bt";}
        @Override public String getSettingNumber() {return "6";}
    },
    AudIn2AudOut {
        @Override public String getSettingName() {return "AudIn2AudOut";}
        @Override public String getSettingNumber() {return "7";}
    };

    public abstract String getSettingName();
    public abstract String getSettingNumber();
}
