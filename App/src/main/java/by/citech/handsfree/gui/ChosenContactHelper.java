package by.citech.handsfree.gui;

import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.param.Settings;
import by.citech.handsfree.param.Tags;

public class ChosenContactHelper {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.CHOSEN_HELPER;

    private boolean isChosen;
    private Contact chosenContact;
    private int chosenContactPosition;

    private ViewHelper viewHelper;

    public ChosenContactHelper(ViewHelper viewHelper) {
        this.viewHelper = viewHelper;
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
        viewHelper.showChosen();
        viewHelper.setChosenContactInfo(chosenContact);
    }

    public void clear() {
        isChosen = false;
        chosenContact = null;
        chosenContactPosition = -1;
        viewHelper.hideChosen();
        viewHelper.clearChosenContactInfo();
    }

}
