package by.citech.contact;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import by.citech.element.ElementsMemCtrl;
import by.citech.element.IElementAdd;
import by.citech.element.IElementDel;
import by.citech.element.IElementUpd;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class Contactor
        implements IElementDel<Contact>, IElementAdd<Contact>, IElementUpd<Contact> {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.CONTACTOR;

    private ContactsDbCtrl dbCtrl;
    private IContactsListener iContactsListener;
    private ElementsMemCtrl<Contact> memCtrl;
    private List<Contact> contacts;

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
        return memCtrl.getElements();
    }

    //--------------------- main

    public void start(Context context, IContactsListener iContactsListener) {
        if (debug) Log.i(TAG, "start");
        if (context == null || iContactsListener == null) {
            if (debug) Log.e(TAG, "start at least one of key parameters are null");
            return;
        }
        this.iContactsListener = iContactsListener;
        new Thread(() -> {
            dbCtrl = new ContactsDbCtrl(context);
            dbCtrl.test();
            getAllContacts();
        }).start();
    }

    private void getAllContacts() {
        if (debug) Log.i(TAG, "getAllContacts");
        if (!dbCtrl.downloadAllContacts(contacts)) {
            Log.e(TAG, "getAllContacts downloadAllContacts fail");
            return;
        }
        if (contacts.isEmpty()) {
            if (debug) Log.i(TAG, "getAllContacts contacts is empty");
            return;
        }
        memCtrl.sort();
        for (Contact contact : contacts)
            contact.setState(ContactState.SuccessAdd);
        iContactsListener.onContactsChange(contacts.toArray(new Contact[contacts.size()]));
    }

    private boolean check(Contact toUpdate, Contact toCopy) {
        if (debug) Log.i(TAG, "check if copy");
        if (toUpdate == null) {
        } else if (toCopy == null) {
            toUpdate.setState(ContactState.FailCopyNull);
        } else if (!Contact.checkForValid(toCopy)) {
            toUpdate.setState(ContactState.FailInvalid);
        } else if (toUpdate.equals(toCopy)) {
            return true;
        } else if (!memCtrl.checkForUniq(toCopy)) {
            toUpdate.setState(ContactState.FailNotUnique);
        }
        iContactsListener.onContactsChange(toUpdate);
        return false;
     }

    private boolean check(Contact contact) {
        if (debug) Log.i(TAG, "check");
        if (contact == null) {
        } else if (!Contact.checkForValid(contact)) {
            contact.setState(ContactState.FailInvalid);
        } else if (!memCtrl.checkForUniq(contact)) {
            contact.setState(ContactState.FailNotUnique);
        } else {
            return true;
        }
        iContactsListener.onContactsChange(contact);
        return false;
    }

    //--------------------- interfaces

    @Override
    public void addElement(Contact contactToAdd) {
        if (debug) Log.i(TAG, "addElement");
        if (check(contactToAdd)) {
            long contactId = dbCtrl.add(contactToAdd);
            if (contactId == -1) {
                contactToAdd.setState(ContactState.FailToAdd);
                Log.e(TAG, "addElement to db fail");
            } else {
                contactToAdd.setId(contactId);
                if (!memCtrl.add(contactToAdd)) {
                    contactToAdd.setState(ContactState.FailToAdd);
                    Log.e(TAG, "addElement to memory fail");
                } else {
                    contactToAdd.setState(ContactState.SuccessAdd);
                }
            }
            iContactsListener.onContactsChange(contactToAdd);
        }
    }

    @Override
    public void deleteElement(Contact contact) {
        if (debug) Log.i(TAG, "deleteElement");
        boolean isMemOpSuccess;
        boolean isDbOpSuccess;
        if (contact != null) {
            if (!dbCtrl.delete(contact)) {
                contact.setState(ContactState.FailDelete);
                Log.e(TAG, "deleteElement from db fail");
            } else if (!memCtrl.delete(contact)) {
                contact.setState(ContactState.FailDelete);
                Log.e(TAG, "deleteElement from memory fail");
            } else {
                contact.setState(ContactState.SuccessDelete);
            }
        }
        iContactsListener.onContactsChange(contact);
    }

    @Override
    public void updateElement(Contact toUpdate, Contact toCopy) {
        if (debug) Log.i(TAG, "updateElement");
        Contact backup = null;
        boolean isMemOpSuccess;
        boolean isDbOpSuccess;
        try {
            backup = toUpdate.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        if (backup == null) {
            Log.e(TAG, "updateElement backup is null");
        }
        if (check(toUpdate, toCopy)) {
            if (!dbCtrl.update(toUpdate, toCopy)) {
                toUpdate.setState(ContactState.FailUpdate);
            } else if (!memCtrl.update(toUpdate, toCopy)) {
                Log.e(TAG, "updateElement memory fail");
                if (backup != null) {
                    if (!dbCtrl.update(toUpdate, backup)) {
                        Log.e(TAG, "updateElement backup fail");
                    }
                    Log.e(TAG, "updateElement backup success");
                }
            } else {
                toUpdate.setState(ContactState.SuccessUpdate);
            }
            iContactsListener.onContactsChange(toUpdate);
        }
    }

}
