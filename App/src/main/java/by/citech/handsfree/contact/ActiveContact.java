package by.citech.handsfree.contact;

import by.citech.handsfree.activity.CallActivityViewManager;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

public class ActiveContact {

    private static final boolean debug = Settings.debug;

    private EActiveContactState currState;
    private ChosenContact chosenContact;
    private CallActivityViewManager viewManager;

    public ActiveContact(ChosenContact chosenContact, CallActivityViewManager viewManager) {
        this.chosenContact = chosenContact;
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
                return chosenContact.getContact();
            default:
                return null;
        }
    }

    public String getName() {
        if (debug) Timber.i("getName %s", currState);
        switch (currState) {
            case FromChosen:
                return getContact().getName();
            case FromEditor:
                return viewManager.getEditorContactNameText();
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
        if (currState == EActiveContactState.Default) {
            if (chosenContact.isChosen()) {
                currState = EActiveContactState.FromChosen;
            } else {
                currState = EActiveContactState.IpFromSearch;
            }
        }
    }

}
