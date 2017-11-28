package by.citech.contact;

import android.provider.BaseColumns;

public final class ContactsContract {

    public ContactsContract() {
    }

    public static abstract class Contacts implements BaseColumns {
        public static final String TABLE_NAME = "contacts";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_IP = "ip";
    }
}
