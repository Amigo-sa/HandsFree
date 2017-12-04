package by.citech.gui;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import by.citech.contact.ActiveContactState;
import by.citech.contact.Contact;
import by.citech.contact.ContactsRecyclerAdapter;
import by.citech.contact.EditorState;
import by.citech.dialog.DialogProcessor;
import by.citech.dialog.DialogState;
import by.citech.dialog.DialogType;
import by.citech.element.IElementAdd;
import by.citech.element.IElementDel;
import by.citech.element.IElementUpd;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class ContactEditorHelper {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.EDITOR_HELPER;

    private Contact contactToEdit, contactToAdd;
    private int contactToEditPosition, contactToDeletePosition;
    private boolean isEditPending, isAddPending, isDeletePending, isEdited, isAdded, isDeleted, isSwipedIn;
    private EditorState editorState;

    private ViewHelper viewHelper;
    private ContactsRecyclerAdapter.SwipeCrutch swipeCrutch;
    private ActiveContactHelper activeContactHelper;
    private DialogProcessor dialogProcessor;
    private IElementDel<Contact> iContactDel;
    private IElementAdd<Contact> iContactAdd;
    private IElementUpd<Contact> iContactUpd;
    private ContactsRecyclerAdapter contactsAdapter;

    public ContactEditorHelper(ViewHelper viewHelper, ContactsRecyclerAdapter.SwipeCrutch swipeCrutch,
                               ActiveContactHelper activeContactHelper, DialogProcessor dialogProcessor,
                               IElementDel<Contact> iContactDel, IElementAdd<Contact> iContactAdd,
                               IElementUpd<Contact> iContactUpd, ContactsRecyclerAdapter contactsAdapter) {
        this.viewHelper = viewHelper;
        this.swipeCrutch = swipeCrutch;
        this.activeContactHelper = activeContactHelper;
        this.dialogProcessor = dialogProcessor;
        this.iContactDel = iContactDel;
        this.iContactAdd = iContactAdd;
        this.iContactUpd = iContactUpd;
        this.contactsAdapter = contactsAdapter;
        contactToEditPosition = -1;
        contactToDeletePosition = -1;
        editorState = EditorState.Inactive;
    }

    public void setSwipedIn() {
        isSwipedIn = true;
    }

    public EditorState getState() {
        return editorState;
    }

    public boolean isUpdPending(Contact contact) {
        return ((contactToEdit == contact) && isEditPending);
    }

    public boolean isAddPending(Contact contact) {
        return ((contactToAdd == contact) && isAddPending);
    }

    public boolean isDelPending(Contact contact) {
        return ((contactToEdit == contact) && isDeletePending);
    }

    public void startEditorEditContact(Contact contact, int position) {
        if (debug) Log.i(TAG, "startEditorEditContact");
        goToState(EditorState.Edit, contact, position);
    }

    public void startEditorAddContact() {
        if (debug) Log.i(TAG, "startEditorAddContact");
        goToState(EditorState.Add);
    }

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
                isAdded = false;
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
        map.put(DialogState.Proceed, () -> iContactDel.deleteElement(contactToEdit));
        map.put(DialogState.Cancel, this::releaseState);
        dialogProcessor.runDialog(DialogType.Delete, map);
    }

    public void saveContact() {
        if (debug) Log.i(TAG, "saveContact");
        switch (editorState) {
            case Add:
                freezeState();
                isAddPending = true;
                contactToAdd = activeContactHelper.getContact();
                iContactAdd.addElement(contactToAdd);
                break;
            case Edit:
                freezeState();
                isEditPending = true;
                iContactUpd.updateElement(contactToEdit, activeContactHelper.getContact());
                break;
            case Inactive:
                Log.e(TAG, "saveContact editorState Inactive");
                break;
            default:
                Log.e(TAG, "saveContact editorState default");
                break;
        }
    }

    public void onContactDelSuccess() {
        if (debug) Log.i(TAG, "onContactDelSuccess");
        isDeletePending = false;
        isDeleted = true;
        contactsAdapter.notifyItemRemoved(contactToDeletePosition);
        contactToEdit = null;
        contactToDeletePosition = -1;
        goToState(EditorState.Add);
    }

    public void onContactAddSucc(int position) {
        if (debug) Log.i(TAG, "onContactAddSucc");
        isAddPending = false;
        isAdded = true;
        contactToEditPosition = position;
        goToState(EditorState.Edit, contactToAdd, position);
        contactToAdd = null;
    }

    public void onContactEditSucc(int position) {
        if (debug) Log.i(TAG, "onContactEditSucc");
        isEditPending = false;
        isEdited = true;
        contactToEditPosition = position;
        goToState(EditorState.Edit, contactToEdit, position);
    }

    public void onContactAddFail() {
        if (debug) Log.i(TAG, "onContactAddFail");
        isAddPending = false;
        contactToAdd = null;
        releaseState();
    }

    public void onContactEditFail() {
        if (debug) Log.i(TAG, "onContactEditFail");
        isEditPending = false;
        releaseState();
    }

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

}
