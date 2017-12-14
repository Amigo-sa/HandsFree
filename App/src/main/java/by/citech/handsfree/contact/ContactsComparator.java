package by.citech.handsfree.contact;

import java.util.Comparator;

public class ContactsComparator implements Comparator<Contact> {

    @Override
    public int compare(Contact contact1, Contact contact2) {
        return contact1.compareTo(contact2);
    }
}
