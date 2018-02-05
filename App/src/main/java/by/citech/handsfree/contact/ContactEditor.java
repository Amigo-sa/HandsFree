package by.citech.handsfree.contact;

import java.util.HashMap;
import java.util.Map;

import by.citech.handsfree.activity.CallActivityViewManager;
import by.citech.handsfree.element.IElementsChangeListener;
import by.citech.handsfree.dialog.EDialogState;
import by.citech.handsfree.dialog.EDialogType;
import by.citech.handsfree.element.IElement;
import by.citech.handsfree.ui.IMsgToUi;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.threading.IThreading;
import timber.log.Timber;

public class ContactEditor
        implements IElementsChangeListener<Contact>, IThreading {

    private static final boolean debug = Settings.debug;

    //--------------------- preparation

    private Contact contactToEdit, contactToAdd;
    private int contactToEditPosition, contactToDeletePosition;
    private boolean isEditPending, isAddPending, isDeletePending, isEdited, isDeleted, isSwipedIn;
    private EContactEditorState editorState;
    private CallActivityViewManager viewManager;
    private ContactsAdapter.SwipeCrutch swipeCrutch;
    private ActiveContact activeContact;
    private IElement<Contact> iContact;
    private ContactsAdapter contactsAdapter;
    private IMsgToUi iMsgToUi;

    {
        contactToEditPosition = -1;
        contactToDeletePosition = -1;
        editorState = EContactEditorState.Inactive;
    }

    //--------------------- getters and setters

    public ContactEditor setViewManager(CallActivityViewManager viewManager) {
        this.viewManager = viewManager;
        return this;
    }

    public ContactEditor setSwipeCrutch(ContactsAdapter.SwipeCrutch swipeCrutch) {
        this.swipeCrutch = swipeCrutch;
        return this;
    }

    public ContactEditor setActiveContactHelper(ActiveContact activeContact) {
        this.activeContact = activeContact;
        return this;
    }

    public ContactEditor setiMsgToUi(IMsgToUi iMsgToUi) {
        this.iMsgToUi = iMsgToUi;
        return this;
    }

    public ContactEditor setiContact(IElement<Contact> iContact) {
        this.iContact = iContact;
        return this;
    }

    public ContactEditor setContactsAdapter(ContactsAdapter contactsAdapter) {
        this.contactsAdapter = contactsAdapter;
        return this;
    }

    public void setEditorSwipedIn() {
        isSwipedIn = true;
    }

    public EContactEditorState getState() {
        return editorState;
    }

    //--------------------- status

    private boolean isUpdPending(Contact contact) {
        return ((contactToEdit == contact) && isEditPending);
    }

    private boolean isAddPending(Contact contact) {
        return ((contactToAdd == contact) && isAddPending);
    }

    private boolean isDelPending(Contact contact) {
        return ((contactToEdit == contact) && isDeletePending);
    }

    //--------------------- enter point

    public void startEditorEdit(Contact contact, int position) {
        Timber.i("startEditorEdit");
        goToState(EContactEditorState.Edit, contact, position);
    }

    public void startEditorAdd() {
        Timber.i("startEditorAdd");
        goToState(EContactEditorState.Add);
    }

    //--------------------- states

    public void goToState(EContactEditorState toState) {
        goToState(toState, null, -1);
    }

    public void goToState(EContactEditorState toState, Contact contact, int position) {
        Timber.i("goToState");
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
                    Timber.i("goToState Inactive isSwipedIn");
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
                activeContact.goToState(EActiveContactState.Default);
                return;
            default:
                Timber.e("goToState editorState default");
                return;
        }
        viewManager.showEditor();
        activeContact.goToState(EActiveContactState.FromEditor);
    }

    //--------------------- commands

    public void getAllContacts() {
        Timber.i("getAllContacts");
        addRunnable(() -> iContact.initiateElements());
    }

    public void cancelContact() {
        Timber.i("cancelContact");
        switch (editorState) {
            case Edit:
                goToState(EContactEditorState.Edit, contactToEdit, contactToEditPosition);
                break;
            case Add:
                goToState(EContactEditorState.Add);
                break;
            default:
                break;
        }
    }

    public void deleteContact() {
        Timber.i("deleteContact");
        isDeletePending = true;
        freezeState();
        contactToDeletePosition = contactToEditPosition;
        Map<EDialogState, Runnable> map = new HashMap<>();
        map.put(EDialogState.Proceed, () -> addRunnable(() -> iContact.deleteElement(contactToEdit)));
        map.put(EDialogState.Cancel, this::releaseState);
        iMsgToUi.sendToUiDialog(true, EDialogType.Delete, map);
    }

    public void saveContact() {
        Timber.i("saveContact");
        switch (editorState) {
            case Add:
                freezeState();
                isAddPending = true;
                contactToAdd = activeContact.getContact();
                addRunnable(() -> iContact.addElement(contactToAdd));
                break;
            case Edit:
                freezeState();
                isEditPending = true;
                addRunnable(() -> iContact.updateElement(contactToEdit, activeContact.getContact()));
                break;
            default:
                break;
        }
    }

    //--------------------- on command results

    private void onContactDelSucc() {
        Timber.i("onContactDelSucc");
        isDeletePending = false;
        isDeleted = true;
        contactsAdapter.notifyItemRemoved(contactToDeletePosition);
        contactToEdit = null;
        contactToDeletePosition = -1;
        goToState(EContactEditorState.Add);
    }

    private void onContactAddSucc(int position) {
        Timber.i("onContactAddSucc");
        isAddPending = false;
        contactToEditPosition = position;
        goToState(EContactEditorState.Edit, contactToAdd, position);
        contactToAdd = null;
    }

    private void onContactEditSucc(int position) {
        Timber.i("onContactEditSucc");
        isEditPending = false;
        isEdited = true;
        contactToEditPosition = position;
        goToState(EContactEditorState.Edit, contactToEdit, position);
    }

    private void onContactDelFail() {
        Timber.i("onContactDelFail");
        isDeletePending = false;
        isDeleted = false;
        releaseState();
    }

    private void onContactAddFail() {
        Timber.i("onContactAddFail");
        isAddPending = false;
        contactToAdd = null;
        releaseState();
    }

    private void onContactEditFail() {
        Timber.i("onContactEditFail");
        isEditPending = false;
        releaseState();
    }

    //--------------------- additional

    public void contactFieldChanged() {
        viewManager.setEditorFieldChanged();
    }

    private void freezeState() {
        Timber.i("freezeState");
        viewManager.setEditorButtonsFreeze();
    }

    private void releaseState() {
        Timber.i("releaseState");
        viewManager.setEditorButtonsRelease();
    }

    //--------------------- IElementsChangeListener

    @Override
    public void onChange(Contact... contacts) {
        Timber.i("onChange");
        if (contacts == null || contacts.length == 0 || contacts[0] == null) {
            Timber.e("onChange returned contact is null");
//          goToState(EContactEditorState.Inactive); //TODO: разобраться, зачем это тут было
            return;
        }
        Contact contact = contacts[0];
        EContactState state = contact.getState();
        iMsgToUi.sendToUiToast(true, state.getMessage());
        if (contacts.length > 1) {
            contactsAdapter.notifyDataSetChanged();
        } else {
            int position = contactsAdapter.getItemPosition(contact);
            Timber.w("onChange: state is %s, pos is %d, contact is %s",
                    state.getMessage(), position, contact.toString());
            switch (state) {
                case FailDelete:
                    if (isDelPending(contact)) onContactDelFail();
                    break;
                case SuccessAdd:
                    contactsAdapter.notifyItemInserted(position);
                    if (isAddPending(contact)) onContactAddSucc(position);
                    break;
                case SuccessUpdate:
                    contactsAdapter.notifyItemChanged(position);
                    if (isUpdPending(contact)) onContactEditSucc(position);
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
                    if (isDelPending(contact)) {
                        onContactDelSucc();
                    } else {
                        contactsAdapter.notifyDataSetChanged();
                    }
                    break;
                case FailToAdd:
                    if (isAddPending(contact)) onContactAddFail();
                default:
                    break;
            }
        }
    }

}
