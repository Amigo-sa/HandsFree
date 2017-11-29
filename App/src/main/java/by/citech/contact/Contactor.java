package by.citech.contact;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import by.citech.element.ElementsMemCtrl;
import by.citech.element.IElementAdd;
import by.citech.element.IElementDel;
import by.citech.element.IElementUpd;

public class Contactor
        implements IElementDel<Contact>, IElementAdd<Contact>, IElementUpd<Contact> {

    private static final boolean debug = true;
    private static final String TAG = "WSD_Contactor";
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
        dbCtrl = new ContactsDbCtrl(context);
        dbCtrl.test();
        getAllContacts();
    }

    private void getAllContacts() {
        if (debug) Log.i(TAG, "getAllContacts");  //TODO: заменить на норм. условие
        dbCtrl.downloadAllContacts(contacts);
        memCtrl.sort();
        for (Contact contact : contacts)
            contact.setState(ContactState.SuccessAdd);
        iContactsListener.doCallbackOnContactsChange(contacts.toArray(new Contact[contacts.size()]));
    }

    private boolean check(Contact contacToUpdate, Contact contactToCopy) {
        if (debug) Log.i(TAG, "check if copy");
        if (!Contact.checkForValidity(contactToCopy)) {
            contactToCopy.setState(ContactState.FailInvalidContact);
            iContactsListener.doCallbackOnContactsChange(contactToCopy);
            return false;
        }
        if (contacToUpdate.equals(contactToCopy)) {
            return true;
        } else if (!memCtrl.checkForUniqueness(contactToCopy)) {
            contactToCopy.setState(ContactState.FailNotUniqueContact);
            iContactsListener.doCallbackOnContactsChange(contactToCopy);
            return false;
        }
        return true;
     }

    private boolean check(Contact contact) {
        if (debug) Log.i(TAG, "check");
        if (!Contact.checkForValidity(contact)) {
            contact.setState(ContactState.FailInvalidContact);
            iContactsListener.doCallbackOnContactsChange(contact);
            return false;
        }
        if (!memCtrl.checkForUniqueness(contact)) {
            contact.setState(ContactState.FailNotUniqueContact);
            iContactsListener.doCallbackOnContactsChange(contact);
            return false;
        }
        return true;
    }

    //--------------------- interfaces

    @Override
    public void addElement(Contact contactToAdd) {
        if (debug) Log.i(TAG, "addElement");
        if (check(contactToAdd)) {
            long contactId = dbCtrl.add(contactToAdd);
            if (contactId != -1) {
                contactToAdd.setId(contactId);
                memCtrl.add(contactToAdd);
                contactToAdd.setState(ContactState.SuccessAdd);
            } else {
                contactToAdd.setState(ContactState.FailToAdd);
            }
            iContactsListener.doCallbackOnContactsChange(contactToAdd);
        }
    }

    @Override
    public void deleteElement(Contact contact) {
        if (debug) Log.i(TAG, "deleteElement");
        dbCtrl.delete(contact);
        memCtrl.delete(contact);
        contact.setState(ContactState.SuccessDelete);
        iContactsListener.doCallbackOnContactsChange(contact);
    }

    @Override
    public void updateElement(Contact contacToUpdate, Contact contactToCopy) {
        if (debug) Log.i(TAG, "updateElement");
        if (check(contacToUpdate, contactToCopy)) {
            dbCtrl.update(contacToUpdate, contactToCopy);
            memCtrl.update(contacToUpdate, contactToCopy);
            contacToUpdate.setState(ContactState.SuccessUpdate);
            iContactsListener.doCallbackOnContactsChange(contacToUpdate);
        }
    }

}
