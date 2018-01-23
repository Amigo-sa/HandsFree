package by.citech.handsfree.ui.helpers;

import by.citech.handsfree.activity.CallActivityViewManager;
import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;

public class ChosenContactHelper {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.ChosenContactHelper;

    private boolean isChosen;
    private Contact chosenContact;
    private int chosenContactPosition;

    private CallActivityViewManager viewManager;

    public ChosenContactHelper(CallActivityViewManager viewManager) {
        this.viewManager = viewManager;
        chosenContactPosition = -1;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public Contact getContact() {
        return chosenContact;
    }

    public void choose(Contact contact, int position) {
        isChosen = true;
        chosenContact = contact;
        chosenContactPosition = position;
        viewManager.showChosen();
        viewManager.setChosenContactInfo(chosenContact);
    }

    public void clear() {
        isChosen = false;
        chosenContact = null;
        chosenContactPosition = -1;
        viewManager.hideChosen();
        viewManager.clearChosenContactInfo();
    }

}
