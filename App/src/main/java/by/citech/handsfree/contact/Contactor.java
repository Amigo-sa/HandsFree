package by.citech.handsfree.contact;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.element.ElementsMemCtrl;
import by.citech.handsfree.element.IElement;
import by.citech.handsfree.ui.IMsgToUi;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

public class Contactor
        implements IElement<Contact>, IPrepareObject {

    private static final boolean debug = Settings.debug;

    //--------------------- non-settings

    private ContactsDbCtrl dbCtrl;
    private IContactsChangeListener listener;
    private ElementsMemCtrl<Contact> memCtrl;
    private List<Contact> contacts;
    private IMsgToUi iMsgToUi;
    private boolean isInitiated;

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
        if (!prepareObject()) {
            Timber.e("getContacts object not prepared, return");
            return null;
        } else {
            return memCtrl.getList();
        }
    }

    public Contactor setListener(IContactsChangeListener listener) {
        this.listener = listener;
        return this;
    }

    public Contactor setiMsgToUi(IMsgToUi iMsgToUi) {
        this.iMsgToUi = iMsgToUi;
        return this;
    }

    public Contactor setContext(Context context) {
        dbCtrl = new ContactsDbCtrl(context);
        return this;
    }

    //--------------------- contacts

    @Override
    public boolean initiateElements() {
        if (debug) Timber.i("getAllContacts");
        dbCtrl.test(); //TODO: remove, test
        if (!dbCtrl.downloadAllContacts(contacts)) {
            Timber.e("getAllContacts downloadAllContacts fail");
            return false;
        } else if (contacts.isEmpty()) {
            if (debug) Timber.i("getAllContacts contacts is empty");
            return false;
        }
        memCtrl.sort();
        for (Contact contact : contacts) {
            if (contact != null) {
                contact.setState(EContactState.SuccessAdd);
            } else {
                Timber.e("getAllContacts one of contacts is null, deleting");
                contacts.remove(null);
            }
        }
        reportContact(contacts.toArray(new Contact[contacts.size()]));
        return true;
    }

    private boolean check(Contact toUpdate, Contact toCopy) {
        if (debug) Timber.i("check if copy");
        if (toUpdate != null) {
            if (toCopy == null) {
                toUpdate.setState(EContactState.FailUpdate);
            } else if (!Contact.checkForValid(toCopy)) {
                toUpdate.setState(EContactState.FailInvalid);
            } else if (Contact.checkForEqual(toUpdate, toCopy)) {
                return true;
            } else if (!memCtrl.checkForUniq(toCopy)) {
                toUpdate.setState(EContactState.FailNotUnique);
            } else {
                return true;
            }
        }
        reportContact(toUpdate);
        return false;
     }

    private boolean check(Contact contact) {
        if (debug) Timber.i("check");
        if (contact != null) {
            if (!Contact.checkForValid(contact)) {
                contact.setState(EContactState.FailInvalid);
            } else if (!memCtrl.checkForUniq(contact)) {
                contact.setState(EContactState.FailNotUnique);
            } else {
                return true;
            }
        }
        reportContact(contact);
        return false;
    }

    private void reportContact(Contact... toReport) {
        if (debug) Timber.i("reportContact");
        if (listener != null && iMsgToUi != null) {
            iMsgToUi.sendToUiRunnable(false, () -> listener.onContactsChange(toReport));
        } else {
            Timber.e("reportContact%s", StatusMessages.ERR_PARAMETERS);
        }
    }

    //--------------------- interfaces

    @Override
    public void addElement(Contact toAdd) {
        if (debug) Timber.i("addElement");
        if (check(toAdd)) {
            if (debug) Timber.tag(TAG).w("addElement toAdd is %s", toAdd.toString());
            long contactId = dbCtrl.add(toAdd);
            if (contactId == -1) {
                toAdd.setState(EContactState.FailToAdd);
                Timber.e("addElement to db fail");
            } else {
                toAdd.setId(contactId);
                if (!memCtrl.add(toAdd)) {
                    toAdd.setState(EContactState.FailToAdd);
                    Timber.e("addElement to memory fail");
                } else {
                    toAdd.setState(EContactState.SuccessAdd);
                }
            }
            reportContact(toAdd);
            if (debug) Timber.tag(TAG).w("addElement added is %s", toAdd.toString());
        }
    }

    @Override
    public void deleteElement(Contact toDelete) {
        if (debug) Timber.i("deleteElement");
        if (toDelete != null) {
            if (!dbCtrl.delete(toDelete)) {
                toDelete.setState(EContactState.FailDelete);
                Timber.e("deleteElement db fail");
            } else if (!memCtrl.delete(toDelete)) {
                toDelete.setState(EContactState.FailDelete);
                Timber.e("deleteElement memory fail");
            } else {
                toDelete.setState(EContactState.SuccessDelete);
            }
        }
        reportContact(toDelete);
    }

    @Override
    public void updateElement(Contact toUpdate, Contact toCopy) {
        if (debug) Timber.i("updateElement");
        if (check(toUpdate, toCopy)) {
            if (debug) Timber.tag(TAG).w("updateElement toCopy is %s", toCopy.toString());
            if (debug) Timber.tag(TAG).w("updateElement toUpdate is %s", toUpdate.toString());
            if (!dbCtrl.update(toUpdate, toCopy)) {
                toUpdate.setState(EContactState.FailUpdate);
                Timber.e("updateElement db fail");
            } else if (!memCtrl.update(toUpdate, toCopy)) {
                toUpdate.setState(EContactState.FailUpdate);
                Timber.e("updateElement memory fail");
            } else {
                toUpdate.setState(EContactState.SuccessUpdate);
            }
            reportContact(toUpdate);
            if (debug) Timber.tag(TAG).w("updateElement updated is %s", toUpdate.toString());
        }
    }

}
