package by.citech.param;

public enum OpMode {

    Bt2Bt {@Override public String getName() {return "Bt2Bt";}},
    Net2Net {@Override public String getName() {return "Net2Net";}},
    Record {@Override public String getName() {return "Record";}},
    Bt2AudOut {@Override public String getName() {return "Bt2AudOut";}},
    AudIn2Bt {@Override public String getName() {return "AudIn2Bt";}},
    AudIn2AudOut {@Override public String getName() {return "AudIn2AudOut";}},
    Normal {@Override public String getName() {return "Normal";}};

    public abstract String getName();
}
