package by.citech.handsfree.settings;

public class SettingsSubscriber {
    private ISettingsCtrl iSettingsCtrl;
    public SettingsSubscriber(ISettingsCtrl iSettingsCtrl) {
        this.iSettingsCtrl = iSettingsCtrl;
    }
    public ISettingsCtrl getiSettingsCtrl() {
        return iSettingsCtrl;
    }
}
