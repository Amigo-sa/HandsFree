package by.citech.contact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import by.citech.element.ElementsMemCtrl;
import by.citech.element.IElement;
import by.citech.exchange.IMsgToUi;
import by.citech.logic.IBase;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class Contactor
        implements IElement<Contact>, IBase {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.CONTACTOR;

    private ContactsDbCtrl dbCtrl;
    private IContactsListener listener;
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
                if (instance == null) {
                    instance = new Contactor();
                }
            }
        }
        return instance;
    }

    //--------------------- getters and setters

    public List<Contact> getContacts() {
        if (debug) Log.i(TAG, "getContacts");
        return memCtrl.getList();
    }

    //--------------------- main

    public void build(@NonNull Context context,
                      @NonNull IContactsListener listener,
                      @NonNull IMsgToUi iMsgToUi) {
        if (debug) Log.i(TAG, "build");
        if (context == null || listener == null || iMsgToUi == null) {
            Log.e(TAG, "start illegal parameters");
            return;
        }
        if (isInitiated) {
            Log.e(TAG, "start already started");
            return;
        } else {
            isInitiated = true;
        }
        this.listener = listener;
        dbCtrl = new ContactsDbCtrl(context);
    }

    public void getAllContacts() {
        if (debug) Log.i(TAG, "getAllContacts");
        if (!isInitiated) return;
        dbCtrl.test(); //TODO: remove, test
        if (!dbCtrl.downloadAllContacts(contacts)) {
            Log.e(TAG, "getAllContacts downloadAllContacts fail");
            return;
        } else if (contacts.isEmpty()) {
            if (debug) Log.i(TAG, "getAllContacts contacts is empty");
            return;
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
    }

    private boolean check(Contact toUpdate, Contact toCopy) {
        if (debug) Log.i(TAG, "check if copy");
        if (toUpdate != null) {
            if (toCopy == null) {
                toUpdate.setState(ContactState.FailUpdate);
            } else if (!Contact.checkForValid(toCopy)) {
                toUpdate.setState(ContactState.FailInvalid);
            } else if (toUpdate.equals(toCopy)) {
                return true;
            } else if (!memCtrl.checkForUniq(toCopy)) {
                toUpdate.setState(ContactState.FailNotUnique);
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
        iMsgToUi.sendToUiRunnable(false, () -> listener.onContactsChange(toReport));
    }

    //--------------------- interfaces

    @Override
    public void addElement(Contact toAdd) {
        if (debug) Log.i(TAG, "addElement");
        if (check(toAdd) && isInitiated) {
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
        }
    }

    @Override
    public void deleteElement(Contact toDelete) {
        if (debug) Log.i(TAG, "deleteElement");
        if ((toDelete != null) && isInitiated) {
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

        //TODO: to remove, test area start
        Contact backup = null;
        if (toUpdate != null) {
            try {
                backup = toUpdate.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        if (backup == null) {
            Log.e(TAG, "updateElement backup is null");
        }
        //TODO: to remove, test area end

        if (check(toUpdate, toCopy) && isInitiated) {
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
        }
    }

    @Override
    public void baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        if (!isInitiated) {
            Log.e(TAG, "baseStop already stopped");
        } else {
            if (dbCtrl != null) {
                dbCtrl = null;
            }
            if (contacts != null) {
                contacts.clear();
                contacts = null;
            }
            listener = null;
            memCtrl = null;
            isInitiated = false;
            iMsgToUi = null;
        }
    }

}
