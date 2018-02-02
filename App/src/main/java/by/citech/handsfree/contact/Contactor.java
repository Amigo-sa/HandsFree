package by.citech.handsfree.contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import by.citech.handsfree.application.ThisApp;
import by.citech.handsfree.element.IElementsChangeListener;
import by.citech.handsfree.element.ElementsMemCtrl;
import by.citech.handsfree.element.IElement;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.ui.IMsgToUi;
import timber.log.Timber;

import static by.citech.handsfree.contact.EContactState.*;

public class Contactor implements IElement<Contact> {

    private static final boolean debug = Settings.debug;

    //--------------------- non-settings

    private ContactsDbCtrl dbCtrl;
    private IElementsChangeListener<Contact> listener;
    private ElementsMemCtrl<Contact> memCtrl;
    private List<Contact> contacts;
    private IMsgToUi iMsgToUi;

    //--------------------- singleton

    private static volatile Contactor instance = null;

    private Contactor() {
        contacts = Collections.synchronizedList(new ArrayList<>());
        memCtrl = new ElementsMemCtrl<>(contacts);
    }

    public static Contactor getInstance() {
        if (instance == null) {
            synchronized (Contactor.class) {
                if (instance == null) {instance = new Contactor();}}}
        return instance;
    }

    //--------------------- getters and setters

    public List<Contact> getContacts() {
        if (debug) Timber.i("getContacts");
        return memCtrl.getList();
    }

    public Contactor setListener(IElementsChangeListener<Contact> listener) {
        this.listener = listener;
        return this;
    }

    public Contactor setiMsgToUi(IMsgToUi iMsgToUi) {
        this.iMsgToUi = iMsgToUi;
        return this;
    }

    //--------------------- interfaces

    @Override
    public boolean initiateElements() {
        if (debug) Timber.i("getAllContacts");
        getDbCtrl().test(); //TODO: remove, test
        if (!getDbCtrl().downloadAllContacts(contacts)) {
            if (debug) Timber.e("getAllContacts downloadAllContacts fail");
            return false;
        }
        memCtrl.sort();
        for (Contact contact : contacts) contact.setState(SuccessAdd);
        reportContact(contacts.toArray(new Contact[contacts.size()]));
        return true;
    }

    @Override
    public void addElement(Contact toAdd) {
        if (debug) Timber.i("addElement");
        if (check(toAdd)) {
            if (debug) Timber.w("addElement toAdd is %s", toAdd.toString());
            long contactId = getDbCtrl().add(toAdd);
            if (contactId == -1) {
                toAdd.setState(FailToAdd);
                if (debug) Timber.e("addElement to db fail");
            } else {
                toAdd.setId(contactId);
                if (!memCtrl.add(toAdd)) {
                    toAdd.setState(FailToAdd);
                    if (debug) Timber.e("addElement to memory fail");
                } else toAdd.setState(SuccessAdd);
            }
            reportContact(toAdd);
            if (debug) Timber.w("addElement added is %s", toAdd.toString());
        }
    }

    @Override
    public void deleteElement(Contact toDelete) {
        if (debug) Timber.i("deleteElement");
        if (toDelete != null) {
            if (!getDbCtrl().delete(toDelete)) {
                toDelete.setState(FailDelete);
                if (debug) Timber.e("deleteElement db fail");
            } else if (!memCtrl.delete(toDelete)) {
                toDelete.setState(FailDelete);
                if (debug) Timber.e("deleteElement memory fail");
            } else toDelete.setState(SuccessDelete);
        }
        reportContact(toDelete);
    }

    @Override
    public void updateElement(Contact toUpdate, Contact toCopy) {
        if (debug) Timber.i("updateElement");
        if (check(toUpdate, toCopy)) {
            if (debug) Timber.w("updateElement toCopy is %s", toCopy);
            if (debug) Timber.w("updateElement toUpdate is %s", toUpdate);
            if (!getDbCtrl().update(toUpdate, toCopy)) {
                toUpdate.setState(FailUpdate);
                if (debug) Timber.e("updateElement db fail");
            } else if (!memCtrl.update(toUpdate, toCopy)) {
                toUpdate.setState(FailUpdate);
                if (debug) Timber.e("updateElement memory fail");
            } else toUpdate.setState(SuccessUpdate);
            reportContact(toUpdate);
            if (debug) Timber.w("updateElement updated is %s", toUpdate.toString());
        }
    }

    //--------------------- contacts

    private ContactsDbCtrl getDbCtrl() {
        if (dbCtrl == null) {
            dbCtrl = new ContactsDbCtrl(ThisApp.getAppContext());
        }
        return dbCtrl;
    }

    private boolean check(Contact toUpdate, Contact toCopy) {
        if (debug) Timber.i("check if copy");
        if (toUpdate != null) {
            if (toCopy == null) {
                toUpdate.setState(FailUpdate);
            } else if (!Contact.checkForValid(toCopy)) {
                toUpdate.setState(FailInvalid);
            } else if (Contact.checkForEqual(toUpdate, toCopy)) {
                return true;
            } else if (!memCtrl.checkForUniq(toCopy)) {
                toUpdate.setState(FailNotUnique);
            } else return true;
        }
        reportContact(toUpdate);
        return false;
     }

    private boolean check(Contact contact) {
        if (debug) Timber.i("check");
        if (contact != null) {
            if (!Contact.checkForValid(contact)) {
                contact.setState(FailInvalid);
            } else if (!memCtrl.checkForUniq(contact)) {
                contact.setState(FailNotUnique);
            } else return true;
        }
        reportContact(contact);
        return false;
    }

    private void reportContact(Contact... toReport) {
        if (debug) Timber.i("reportContact");
        if (listener != null && iMsgToUi != null) {
            iMsgToUi.sendToUiRunnable(false, () -> listener.onChange(toReport));
        } else if (debug) Timber.e("reportContact%s", StatusMessages.ERR_PARAMETERS);
    }

}
