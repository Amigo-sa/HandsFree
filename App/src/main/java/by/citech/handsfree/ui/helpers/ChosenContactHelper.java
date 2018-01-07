package by.citech.handsfree.ui.helpers;

import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class ChosenContactHelper {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.CHOSEN_HELPER;

    private boolean isChosen;
    private Contact chosenContact;
    private int chosenContactPosition;

    private ViewManager viewManager;

    public ChosenContactHelper(ViewManager viewManager) {
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
