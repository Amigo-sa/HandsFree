package by.citech.handsfree.ui.helpers;

import by.citech.handsfree.activity.CallActivityViewManager;
import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

public class ActiveContactHelper {

    private static final boolean debug = Settings.debug;

    private EActiveContactState activeContactState;
    private ChosenContactHelper chosenContactHelper;
    private CallActivityViewManager viewManager;

    public ActiveContactHelper(ChosenContactHelper chosenContactHelper, CallActivityViewManager viewManager) {
        this.chosenContactHelper = chosenContactHelper;
        this.viewManager = viewManager;
        activeContactState = EActiveContactState.IpFromSearch;
    }

    //--------------------- getters and setters

    public Contact getContact() {
        if (debug) Timber.i("getContact %s", activeContactState);
        switch (activeContactState) {
            case IpFromSearch:
            case FromEditor:
                return new Contact(getName(), getIp());
            case FromChosen:
                return chosenContactHelper.getContact();
            case Null:
            default:
                break;
        }
        return null;
    }

    public String getName() {
        if (debug) Timber.i("getName %s", activeContactState);
        switch (activeContactState) {
            case FromChosen:
                return getContact().getName();
            case FromEditor:
                return viewManager.getEditorContactNameText();
            case IpFromSearch:
            default:
                return "";
        }
    }

    public String getIp() {
        if (debug) Timber.i("getIp %s", activeContactState);
        switch (activeContactState) {
            case FromChosen:
                return getContact().getIp();
            case FromEditor:
                return viewManager.getEditorContactIpText();
            case IpFromSearch:
                return viewManager.getSearchText();
            default:
                return "";
        }
    }

    //--------------------- states

    public EActiveContactState getState() {
        return activeContactState;
    }

    public void goToState(EActiveContactState toState) {
        if (debug) Timber.i("goToState %s, activeContactState %s", toState, activeContactState);
        this.activeContactState = toState;
        switch (activeContactState) {
            case Default:
                if (chosenContactHelper.isChosen())
                    activeContactState = EActiveContactState.FromChosen;
                else
                    activeContactState = EActiveContactState.IpFromSearch;
                break;
            case FromChosen:
            case Null:
            case IpFromSearch:
            case FromEditor:
            default:
                break;
        }
    }

}
