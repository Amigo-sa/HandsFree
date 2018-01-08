package by.citech.handsfree.ui.helpers;

import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.ui.helpers.state.ActiveContactState;
import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.contact.EContactState;
import by.citech.handsfree.contact.ContactsRecyclerAdapter;
import by.citech.handsfree.ui.helpers.state.EditorState;
import by.citech.handsfree.contact.IContactsListener;
import by.citech.handsfree.dialog.DialogState;
import by.citech.handsfree.dialog.DialogType;
import by.citech.handsfree.element.IElement;
import by.citech.handsfree.ui.IMsgToUi;
import by.citech.handsfree.management.IBase;
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

    private Contact contactToEdit, contactToAdd;
    private int contactToEditPosition, contactToDeletePosition;
    private boolean isEditPending, isAddPending, isDeletePending, isEdited, isDeleted, isSwipedIn;
    private EditorState editorState;
    private ViewManager viewManager;
    private ContactsRecyclerAdapter.SwipeCrutch swipeCrutch;
    private ActiveContactHelper activeContactHelper;
    private IElement<Contact> iContact;
    private ContactsRecyclerAdapter contactsAdapter;
    private IMsgToUi iMsgToUi;

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
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return editorState != null;
    }

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
        prepareObject();
        return true;
    }

    @Override
    public boolean baseStop() {
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
        viewManager = null;
        swipeCrutch = null;
        activeContactHelper = null;
        iContact = null;
        contactsAdapter = null;
        iMsgToUi = null;
        IBase.super.baseStop();
        return true;
    }

    //--------------------- getters and setters

    public ContactEditorHelper setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
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


    public void setEditorSwipedIn() {
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

    public void startEditorEdit(Contact contact, int position) {
        if (debug) Log.i(TAG, "startEditorEdit");
        goToState(EditorState.Edit, contact, position);
    }

    public void startEditorAdd() {
        if (debug) Log.i(TAG, "startEditorAdd");
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
                viewManager.setEditorAdd();
                break;
            case Edit:
                contactToEdit = contact;
                contactToEditPosition = position;
                viewManager.setEditorEdit(contactToEdit);
                break;
            case Inactive:
                contactToEdit = null;
                contactToAdd = null;
                contactToEditPosition = -1;
                viewManager.hideEditor();
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
        viewManager.showEditor();
        activeContactHelper.goToState(ActiveContactState.FromEditor);
    }

    //--------------------- commands

    public void getAllContacts() {
        if (debug) Log.i(TAG, "getAllContacts");
        addRunnable(() -> iContact.initiateElements());
    }

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
                Log.e(TAG, "cancelInEditor editorState Inactive");
                break;
            default:
                Log.e(TAG, "cancelInEditor editorState default");
                break;
        }
    }

    public void deleteContact() {
        if (debug) Log.i(TAG, "deleteContact");
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
                Log.e(TAG, "saveInEditor editorState Inactive");
                break;
            default:
                Log.e(TAG, "saveInEditor editorState default");
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
        viewManager.setEditorFieldChanged();
    }

    private void freezeState() {
        if (debug) Log.i(TAG, "freezeState");
        viewManager.setEditorButtonsFreeze();
    }

    private void releaseState() {
        if (debug) Log.i(TAG, "releaseState");
        viewManager.setEditorButtonsRelease();
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
        EContactState state = contact.getState();
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