package by.citech.handsfree.gui.helper;

import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import by.citech.handsfree.common.IBaseCtrl;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.gui.helper.state.ActiveContactState;
import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.contact.ContactState;
import by.citech.handsfree.contact.ContactsRecyclerAdapter;
import by.citech.handsfree.gui.helper.state.EditorState;
import by.citech.handsfree.contact.IContactsListener;
import by.citech.handsfree.dialog.DialogState;
import by.citech.handsfree.dialog.DialogType;
import by.citech.handsfree.element.IElement;
import by.citech.handsfree.exchange.IMsgToUi;
import by.citech.handsfree.common.IBase;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.threading.IThreadManager;

public class ContactEditorHelper
        implements IBase, IContactsListener, IPrepareObject, IThreadManager {

    private static final String STAG = Tags.EDITOR_HELPER;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private boolean isPrepared;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        contactToEditPosition = -1;
        contactToDeletePosition = -1;
        editorState = EditorState.Inactive;
        isPrepared = true;
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return isPrepared;
    }

    //--------------------- non-settings

    private Contact contactToEdit, contactToAdd;
    private int contactToEditPosition, contactToDeletePosition;
    private boolean isEditPending, isAddPending, isDeletePending, isEdited, isDeleted, isSwipedIn;
    private EditorState editorState;

    private ViewHelper viewHelper;
    private ContactsRecyclerAdapter.SwipeCrutch swipeCrutch;
    private ActiveContactHelper activeContactHelper;
    private IElement<Contact> iContact;
    private ContactsRecyclerAdapter contactsAdapter;
    private IMsgToUi iMsgToUi;

    //--------------------- singleton

    private static volatile ContactEditorHelper instance = null;

    private ContactEditorHelper() {
    }

    public static ContactEditorHelper getInstance() {
        if (instance == null) {
            synchronized (ContactEditorHelper.class) {
                if (instance == null) {
                    instance = new ContactEditorHelper();
                }
            }
        } else {
            instance.prepareObject();
        }
        return instance;
    }

    //--------------------- base

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        return true;
    }

    @Override
    public boolean baseStop() {
//        IBase.super.baseStop();
        if (debug) Log.i(TAG, "baseStart");
        contactToEdit = null;
        contactToAdd = null;
        contactToEditPosition = -1;
        contactToDeletePosition = -1;
        isEditPending = false;
        isAddPending = false;
        isDeletePending = false;
        isEdited = false;
        isDeleted = false;
        isSwipedIn = false;
        editorState = null;
        viewHelper = null;
        swipeCrutch = null;
        activeContactHelper = null;
        iContact = null;
        contactsAdapter = null;
        iMsgToUi = null;
        return true;
    }

    //--------------------- getters and setters

    public ContactEditorHelper setViewHelper(ViewHelper viewHelper) {
        this.viewHelper = viewHelper;
        return this;
    }

    public ContactEditorHelper setSwipeCrutch(ContactsRecyclerAdapter.SwipeCrutch swipeCrutch) {
        this.swipeCrutch = swipeCrutch;
        return this;
    }

    public ContactEditorHelper setActiveContactHelper(ActiveContactHelper activeContactHelper) {
        this.activeContactHelper = activeContactHelper;
        return this;
    }

    public ContactEditorHelper setiMsgToUi(IMsgToUi iMsgToUi) {
        this.iMsgToUi = iMsgToUi;
        return this;
    }

    public ContactEditorHelper setiContact(IElement<Contact> iContact) {
        this.iContact = iContact;
        return this;
    }

    public ContactEditorHelper setContactsAdapter(ContactsRecyclerAdapter contactsAdapter) {
        this.contactsAdapter = contactsAdapter;
        return this;
    }


    public void setSwipedIn() {
        isSwipedIn = true;
    }

    public EditorState getState() {
        return editorState;
    }

    //--------------------- status

    public boolean isUpdPending(Contact contact) {
        return ((contactToEdit == contact) && isEditPending);
    }

    public boolean isAddPending(Contact contact) {
        return ((contactToAdd == contact) && isAddPending);
    }

    public boolean isDelPending(Contact contact) {
        return ((contactToEdit == contact) && isDeletePending);
    }

    //--------------------- enter point

    public void startEditorEditContact(Contact contact, int position) {
        if (debug) Log.i(TAG, "startEditorEditContact");
        goToState(EditorState.Edit, contact, position);
    }

    public void startEditorAddContact() {
        if (debug) Log.i(TAG, "startEditorAddContact");
        goToState(EditorState.Add);
    }

    //--------------------- states

    public void goToState(EditorState toState) {
        if (toState != EditorState.Edit)
            goToState(toState, null, -1);
        else
            Log.e(TAG, "goToState editorState illegal");
    }

    public void goToState(EditorState toState, Contact contact, int position) {
        if (debug) Log.i(TAG, "ContactEditorHelper goToState");
        editorState = toState;
        switch (editorState) {
            case Add:
                viewHelper.setEditorAdd();
                break;
            case Edit:
                contactToEdit = contact;
                contactToEditPosition = position;
                viewHelper.setEditorEdit(contactToEdit);
                break;
            case Inactive:
                contactToEdit = null;
                contactToAdd = null;
                contactToEditPosition = -1;
                viewHelper.hideEditor();
                if (isSwipedIn) {
                    if (debug) Log.i(TAG, "goToState Inactive isSwipedIn");
                    if (isDeleted || isEdited) {
                        swipeCrutch.resetSwipe();
                    } else {
                        swipeCrutch.resolveSwipe();
                    }
                    isSwipedIn = false;
                }
                isDeletePending = false;
                isAddPending = false;
                isEditPending = false;
                isDeleted = false;
                isEdited = false;
                activeContactHelper.goToState(ActiveContactState.Default);
                return;
            default:
                Log.e(TAG, "goToState editorState default");
                return;
        }
        viewHelper.showEditor();
        activeContactHelper.goToState(ActiveContactState.FromEditor);
    }

    //--------------------- commands

    public void cancelContact() {
        if (debug) Log.i(TAG, "cancelContact");
        switch (editorState) {
            case Edit:
                goToState(EditorState.Edit, contactToEdit, contactToEditPosition);
                break;
            case Add:
                goToState(EditorState.Add);
                break;
            case Inactive:
                Log.e(TAG, "cancelContact editorState Inactive");
                break;
            default:
                Log.e(TAG, "cancelContact editorState default");
                break;
        }
    }

    public void deleteContact() {
        if (debug) Log.i(TAG, "tryToDeleteContact");
        isDeletePending = true;
        freezeState();
        contactToDeletePosition = contactToEditPosition;
        Map<DialogState, Runnable> map = new HashMap<>();
        map.put(DialogState.Proceed, () -> addRunnable(() -> iContact.deleteElement(contactToEdit)));
        map.put(DialogState.Cancel, this::releaseState);
        iMsgToUi.sendToUiDialog(true, DialogType.Delete, map);
    }

    public void saveContact() {
        if (debug) Log.i(TAG, "saveContact");
        switch (editorState) {
            case Add:
                freezeState();
                isAddPending = true;
                contactToAdd = activeContactHelper.getContact();
                addRunnable(() -> iContact.addElement(contactToAdd));
                break;
            case Edit:
                freezeState();
                isEditPending = true;
                addRunnable(() -> iContact.updateElement(contactToEdit, activeContactHelper.getContact()));
                break;
            case Inactive:
                Log.e(TAG, "saveContact editorState Inactive");
                break;
            default:
                Log.e(TAG, "saveContact editorState default");
                break;
        }
    }

    //--------------------- on command results

    private void onContactDelSucc() {
        if (debug) Log.i(TAG, "onContactDelSucc");
        isDeletePending = false;
        isDeleted = true;
        contactsAdapter.notifyItemRemoved(contactToDeletePosition);
        contactToEdit = null;
        contactToDeletePosition = -1;
        goToState(EditorState.Add);
    }

    private void onContactAddSucc(int position) {
        if (debug) Log.i(TAG, "onContactAddSucc");
        isAddPending = false;
        contactToEditPosition = position;
        goToState(EditorState.Edit, contactToAdd, position);
        contactToAdd = null;
    }

    private void onContactEditSucc(int position) {
        if (debug) Log.i(TAG, "onContactEditSucc");
        isEditPending = false;
        isEdited = true;
        contactToEditPosition = position;
        goToState(EditorState.Edit, contactToEdit, position);
    }

    private void onContactDelFail() {
        if (debug) Log.i(TAG, "onContactDelFail");
        isDeletePending = false;
        isDeleted = false;
        releaseState();
    }

    private void onContactAddFail() {
        if (debug) Log.i(TAG, "onContactAddFail");
        isAddPending = false;
        contactToAdd = null;
        releaseState();
    }

    private void onContactEditFail() {
        if (debug) Log.i(TAG, "onContactEditFail");
        isEditPending = false;
        releaseState();
    }

    //--------------------- additional

    public void contactFieldChanged() {
        viewHelper.setEditorFieldChanged();
    }

    private void freezeState() {
        if (debug) Log.i(TAG, "freezeState");
        viewHelper.setEditorButtonsFreeze();
    }

    private void releaseState() {
        if (debug) Log.i(TAG, "releaseState");
        viewHelper.setEditorButtonsRelease();
    }

    //--------------------- IContactsListener

    @Override
    public void onContactsChange(final Contact... contacts) {
        if (debug) Log.i(TAG, "onContactsChange");
        if (contacts == null || contacts[0] == null) {
            Log.e(TAG, "onContactsChange returned contact is null");
            goToState(EditorState.Inactive);
            return;
        }
        Contact contact = contacts[0];
        ContactState state = contact.getState();
        iMsgToUi.sendToUiToast(true, state.getMessage());
        if (contacts.length > 1) {
            contactsAdapter.notifyDataSetChanged();
        } else {
            int position = contactsAdapter.getItemPosition(contact);
            if (debug) Log.w(TAG, String.format(Locale.US,
                    "onContactsChange: state is %s, pos is %d, contact is %s",
                    state.getMessage(), position, contact.toString()));
            switch (state) {
                case FailDelete:
                    if (isDelPending(contact))
                        onContactDelFail();
                    break;
                case SuccessAdd:
                    contactsAdapter.notifyItemInserted(position);
                    if (isAddPending(contact))
                        onContactAddSucc(position);
                    break;
                case SuccessUpdate:
                    contactsAdapter.notifyItemChanged(position);
                    if (isUpdPending(contact))
                        onContactEditSucc(position);
                    break;
                case FailUpdate:
                case FailInvalid:
                case FailNotUnique:
                    if (isUpdPending(contact)) {
                        onContactEditFail();
                    } else if (isAddPending(contact)) {
                        onContactAddFail();
                    }
                    break;
                case SuccessDelete:
                    if (isDelPending(contact))
                        onContactDelSucc();
                    else
                        contactsAdapter.notifyDataSetChanged();
                    break;
                case FailToAdd:
                    if (isAddPending(contact))
                        onContactAddFail();
                case Null:
                    Log.e(TAG, "onContactsChange state Null");
                    break;
                default:
                    Log.e(TAG, "onContactsChange state default");
                    break;
            }
        }
    }

}
