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
import by.citech.handsfree.management.IBase;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

public class Contactor
        implements IElement<Contact>, IBase, IPrepareObject {

    private static final String STAG = Tags.CONTACTOR;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        contacts = Collections.synchronizedList(new ArrayList<>());
        memCtrl = new ElementsMemCtrl<>(contacts);
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return contacts != null && memCtrl != null;
    }

    //--------------------- non-settings

    private ContactsDbCtrl dbCtrl;
    private IContactsListener listener;
    private ElementsMemCtrl<Contact> memCtrl;
    private List<Contact> contacts;
    private IMsgToUi iMsgToUi;
    private boolean isReady;

    //--------------------- singleton

    private static volatile Contactor instance = null;

    private Contactor() {
    }

    public static Contactor getInstance() {
        if (instance == null) {
            synchronized (Contactor.class) {
                if (instance == null) {
                    instance = new Contactor();
                }
            }
        } else {
            instance.prepareObject();
        }
        return instance;
    }

    //--------------------- getters and setters

    public List<Contact> getContacts() {
        if (debug) Log.i(TAG, "getContacts");
        if (!prepareObject()) {
            Log.e(TAG, "getContacts object not prepared, return");
            return null;
        } else {
            return memCtrl.getList();
        }
    }

    public Contactor setListener(IContactsListener listener) {
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

    //--------------------- main

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        if (!prepareObject()) {
            Log.e(TAG, "baseStart object not prepared, return");
            return false;
        }
        if (dbCtrl == null || listener == null || iMsgToUi == null) {
            Log.e(TAG, "baseStart illegal parameters");
            return false;
        }
        isReady = true;
        return true;
    }

    @Override
    public boolean baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        dbCtrl = null;
        if (contacts != null) {
            contacts.clear();
            contacts = null;
        }
        listener = null;
        memCtrl = null;
        iMsgToUi = null;
        isReady = false;
        IBase.super.baseStop();
        return true;
    }

    @Override
    public boolean initiateElements() {
        if (debug) Log.i(TAG, "getAllContacts");
        if (!isReady) {
            Log.e(TAG, "getAllContacts not ready");
            return false;
        }
        dbCtrl.test(); //TODO: remove, test
        if (!dbCtrl.downloadAllContacts(contacts)) {
            Log.e(TAG, "getAllContacts downloadAllContacts fail");
            return false;
        } else if (contacts.isEmpty()) {
            if (debug) Log.i(TAG, "getAllContacts contacts is empty");
            return false;
        }
        memCtrl.sort();
        for (Contact contact : contacts) {
            if (contact != null) {
                contact.setState(ContactState.SuccessAdd);
            } else {
                Log.e(TAG, "getAllContacts one of contacts is null, deleting");
                contacts.remove(null);
            }
        }
        reportContact(contacts.toArray(new Contact[contacts.size()]));
        return true;
    }

    private boolean check(Contact toUpdate, Contact toCopy) {
        if (debug) Log.i(TAG, "check if copy");
        if (toUpdate != null) {
            if (toCopy == null) {
                toUpdate.setState(ContactState.FailUpdate);
            } else if (!Contact.checkForValid(toCopy)) {
                toUpdate.setState(ContactState.FailInvalid);
            } else if (Contact.checkForEqual(toUpdate, toCopy)) {
                return true;
            } else if (!memCtrl.checkForUniq(toCopy)) {
                toUpdate.setState(ContactState.FailNotUnique);
            } else {
                return true;
            }
        }
        reportContact(toUpdate);
        return false;
     }

    private boolean check(Contact contact) {
        if (debug) Log.i(TAG, "check");
        if (contact != null) {
            if (!Contact.checkForValid(contact)) {
                contact.setState(ContactState.FailInvalid);
            } else if (!memCtrl.checkForUniq(contact)) {
                contact.setState(ContactState.FailNotUnique);
            } else {
                return true;
            }
        }
        reportContact(contact);
        return false;
    }

    private void reportContact(Contact... toReport) {
        if (debug) Log.i(TAG, "reportContact");
        if (listener != null && iMsgToUi != null) {
            iMsgToUi.sendToUiRunnable(false, () -> listener.onContactsChange(toReport));
        } else {
            Log.e(TAG, "reportContact" + StatusMessages.ERR_PARAMETERS);
        }
    }

    //--------------------- interfaces

    @Override
    public void addElement(Contact toAdd) {
        if (debug) Log.i(TAG, "addElement");
        if (!isReady) {
            Log.e(TAG, "addElement not ready");
            return;
        }
        if (check(toAdd)) {
            if (debug) Log.w(TAG, "addElement toAdd is " + toAdd.toString());
            long contactId = dbCtrl.add(toAdd);
            if (contactId == -1) {
                toAdd.setState(ContactState.FailToAdd);
                Log.e(TAG, "addElement to db fail");
            } else {
                toAdd.setId(contactId);
                if (!memCtrl.add(toAdd)) {
                    toAdd.setState(ContactState.FailToAdd);
                    Log.e(TAG, "addElement to memory fail");
                } else {
                    toAdd.setState(ContactState.SuccessAdd);
                }
            }
            reportContact(toAdd);
            if (debug) Log.w(TAG, "addElement added is " + toAdd.toString());
        }
    }

    @Override
    public void deleteElement(Contact toDelete) {
        if (debug) Log.i(TAG, "deleteElement");
        if (!isReady) {
            Log.e(TAG, "deleteElement not ready");
            return;
        }
        if (toDelete != null) {
            if (!dbCtrl.delete(toDelete)) {
                toDelete.setState(ContactState.FailDelete);
                Log.e(TAG, "deleteElement db fail");
            } else if (!memCtrl.delete(toDelete)) {
                toDelete.setState(ContactState.FailDelete);
                Log.e(TAG, "deleteElement memory fail");
            } else {
                toDelete.setState(ContactState.SuccessDelete);
            }
        }
        reportContact(toDelete);
    }

    @Override
    public void updateElement(Contact toUpdate, Contact toCopy) {
        if (debug) Log.i(TAG, "updateElement");
        if (!isReady) {
            Log.e(TAG, "updateElement not ready");
            return;
        }
        if (check(toUpdate, toCopy)) {
            if (debug) Log.w(TAG, "updateElement toCopy is " + toCopy.toString());
            if (debug) Log.w(TAG, "updateElement toUpdate is " + toUpdate.toString());
            if (!dbCtrl.update(toUpdate, toCopy)) {
                toUpdate.setState(ContactState.FailUpdate);
                Log.e(TAG, "updateElement db fail");
            } else if (!memCtrl.update(toUpdate, toCopy)) {
                toUpdate.setState(ContactState.FailUpdate);
                Log.e(TAG, "updateElement memory fail");
            } else {
                toUpdate.setState(ContactState.SuccessUpdate);
            }
            reportContact(toUpdate);
            if (debug) Log.w(TAG, "updateElement updated is " + toUpdate.toString());
        }
    }

}
