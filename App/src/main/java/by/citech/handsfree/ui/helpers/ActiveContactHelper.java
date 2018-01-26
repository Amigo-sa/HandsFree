package by.citech.handsfree.ui.helpers;

import by.citech.handsfree.activity.CallActivityViewManager;
import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

public class ActiveContactHelper {

    private static final boolean debug = Settings.debug;

    private EActiveContactState currState;
    private ChosenContactHelper chosenContactHelper;
    private CallActivityViewManager viewManager;

    public ActiveContactHelper(ChosenContactHelper chosenContactHelper, CallActivityViewManager viewManager) {
        this.chosenContactHelper = chosenContactHelper;
        this.viewManager = viewManager;
        currState = EActiveContactState.IpFromSearch;
    }

    //--------------------- getters and setters

    public Contact getContact() {
        if (debug) Timber.i("getContact %s", currState);
        switch (currState) {
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
        if (debug) Timber.i("getName %s", currState);
        switch (currState) {
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
        if (debug) Timber.i("getIp %s", currState);
        switch (currState) {
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
        return currState;
    }

    public void goToState(EActiveContactState toState) {
        if (debug) Timber.i("goToState %s from %s", toState, currState);
        currState = toState;
        switch (currState) {
            case Default:
                if (chosenContactHelper.isChosen())
                    currState = EActiveContactState.FromChosen;
                else
                    currState = EActiveContactState.IpFromSearch;
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
