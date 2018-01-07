package by.citech.handsfree.ui.helpers;

import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.ui.helpers.state.EditorState;

public interface IContactEditorHelper {

    default void getAllContacts() {
        ContactEditorHelper.getInstance().getAllContacts();
    }

    default EditorState getEditorState() {
        return ContactEditorHelper.getInstance().getState();
    }

    default void goToEditorState(EditorState toState) {
        ContactEditorHelper.getInstance().goToState(toState);
    }

    default void cancelInEditor() {
        ContactEditorHelper.getInstance().cancelContact();
    }

    default void saveInEditor() {
        ContactEditorHelper.getInstance().saveContact();
    }

    default void deleteFromEditor() {
        ContactEditorHelper.getInstance().deleteContact();
    }

    default void setEditorSwipedIn() {
        ContactEditorHelper.getInstance().setEditorSwipedIn();
    }

    default void startEditorAddContact() {
        ContactEditorHelper.getInstance().startEditorAdd();
    }

    default void startEditorEditContact(Contact contactToEdit, int position) {
        ContactEditorHelper.getInstance().startEditorEdit(contactToEdit, position);
    }

}
