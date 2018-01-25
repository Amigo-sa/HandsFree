package by.citech.handsfree.settings;

public enum EOpMode
        implements ISettingEnum<EOpMode> {

    Normal {},
    Bt2Bt {},
    Net2Net {},
    Record {},
    Bt2AudOut {},
    AudIn2Bt {},
    DataGen2Bt {},
    AudIn2AudOut {};

    @Override public String getSettingName() {return this.toString();}
    @Override public String getSettingNumber() {return String.valueOf(this.ordinal() + 1);}
    @Override public String getDefaultName() {return getDefaultValue().getSettingName();};
    @Override public String getTypeName() {return SettingsDefault.TypeName.opMode;}
    @Override public EOpMode getDefaultValue() {return SettingsDefault.Common.opMode;}
    @Override public EOpMode[] getValues() {return values();}



}
