package by.citech.contact;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

public class ContactsDbCtrl extends SQLiteOpenHelper {

    private static final boolean debug = true;
    private static final String TAG = "WSD_ContactsDbCtrl";
    private static final int DB_CONTACTS_VERSION = 1;
    private static final String DB_CONTACTS_NAME = "DB_CONTACTS";

    private SQLiteDatabase db;

    private static final String CREATE_DB_CONTACTS = String.format(
                    "CREATE TABLE IF NOT EXISTS %1$s (" +
                    "%2$s INTEGER PRIMARY KEY NOT NULL" +
                    "," +
                    "%3$s TEXT NOT NULL" +
                    "," +
                    "%4$s TEXT NOT NULL UNIQUE" +
                    ");",
            ContactsContract.Contacts.TABLE_NAME,
            ContactsContract.Contacts.COLUMN_NAME_ID,
            ContactsContract.Contacts.COLUMN_NAME_NAME,
            ContactsContract.Contacts.COLUMN_NAME_IP
            );

    //--------------------- base

    ContactsDbCtrl(Context context) {
        super(context, DB_CONTACTS_NAME, null, DB_CONTACTS_VERSION);
        if (debug) Log.i(TAG, "constructor");
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (debug) Log.i(TAG, "onCreate");
        if (debug) Log.i(TAG, "onCreate SQL-request is: " + CREATE_DB_CONTACTS);
        db.execSQL(CREATE_DB_CONTACTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (debug) Log.i(TAG, "onUpgrade");
        //TODO: описание действий при переходе с одной версии БД на другую
    }

    //--------------------- main

    private static ContentValues buildContent(Contact contact) {
        if (debug) Log.d(TAG, "buildContent");
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsContract.Contacts.COLUMN_NAME_NAME, contact.getName());
        contentValues.put(ContactsContract.Contacts.COLUMN_NAME_IP, contact.getIp());
        return contentValues;
    }

    void downloadAllContacts(List<Contact> contacts) {
        if (debug) Log.i(TAG, "downloadAllContacts");
        Cursor cursor = db.query(ContactsContract.Contacts.TABLE_NAME, null, null, null ,null, null, null);
        if (cursor.moveToFirst()) {
            contacts.clear();
            if (debug) Log.i(TAG, "downloadAllContacts db already have contacts");
            int i = 0;
            do {
                contacts.add(new Contact(
                        cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.COLUMN_NAME_ID)),
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.COLUMN_NAME_NAME)),
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.COLUMN_NAME_IP))));
                if (debug) Log.i(TAG, "downloadAllContacts added contact to list: " + ++i);
            } while (cursor.moveToNext());
            if (debug) Log.i(TAG, "downloadAllContacts added all contacts to list");
        } else {
            if (debug) Log.i(TAG, "downloadAllContacts db have no contacts");
        }
        cursor.close();
    }

    long add(Contact contact) {
        if (debug) Log.d(TAG, "add");
        if (debug) {
            long id = db.insert(ContactsContract.Contacts.TABLE_NAME, null, buildContent(contact));
            Log.d(TAG, "add id: " + id);
            return id;
        } else {
            return db.insert(ContactsContract.Contacts.TABLE_NAME, null, buildContent(contact));
        }
    }

    void delete(Contact contact) {
        if (debug) Log.i(TAG, "delete");
        int delCount = db.delete(ContactsContract.Contacts.TABLE_NAME, "id = " + contact.getId(), null);
        if (debug && (delCount != 1)) Log.e(TAG, "delete deleted: " + delCount);
    }

    void update(Contact contactToUpd, Contact contactToCopy) {
        if (debug) Log.i(TAG, "update");
        int updCount = db.update(ContactsContract.Contacts.TABLE_NAME, buildContent(contactToCopy), "id = " + contactToUpd.getId(), null);
        if (debug && (updCount != 1)) Log.e(TAG, "update updated: " + updCount);
    }

    //--------------------- test

    void test() {
        Log.w(TAG, "TEST DB");
        testPrintDb();
        testGetSizeDb();
        if (testIsEmptyDb()) {
            testFillDb();
        } else {
            testClearDb();
            testFillDb();
        }
        Log.w(TAG, "TEST DB DONE");
    }

    long testGetSizeDb() {
        long size = DatabaseUtils.queryNumEntries(db, ContactsContract.Contacts.TABLE_NAME);
        Log.i(TAG, "testGetSizeDb db size is " + size);
        return size;
    }

    void testPrintDb() {
        Log.i(TAG, "testPrintDb");
        Cursor cursor = db.query(ContactsContract.Contacts.TABLE_NAME, null, null, null ,null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Log.i(TAG, String.format("ID is %d, Name is %s, IP is %s",
                        cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.COLUMN_NAME_ID)),
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.COLUMN_NAME_NAME)),
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.COLUMN_NAME_IP))));
            } while (cursor.moveToNext());
        } else {
            Log.i(TAG, "testPrintDb db have no contacts");
        }
        cursor.close();
    }

    boolean testIsEmptyDb() {
        boolean isClear = (DatabaseUtils.queryNumEntries(db, ContactsContract.Contacts.TABLE_NAME) == 0);
        Log.i(TAG, "testIsEmptyDb db is " + (isClear ? "" : "not ") + "empty");
        return isClear;
    }

    void testClearDb() {
        Log.i(TAG, "testClearDb");
        db.delete(ContactsContract.Contacts.TABLE_NAME, null, null);
    }

    void testFillDb() {
        Log.i(TAG, "testFillDb");
        add(new Contact("Петька", "192.168.0.1"));
        add(new Contact("Саня", "192.12.0.1"));
        add(new Contact("Федя", "192.238.0.1"));
        add(new Contact("Коля", "192.38.0.1"));
        add(new Contact("Google", "8.8.8.8"));
        add(new Contact("КГБ", "127.0.0.1"));
        add(new Contact("Петька", "192.168.0.113"));
        add(new Contact("Василий", "192.168.10.16"));
        add(new Contact("Борис Моисеев", "176.168.0.1"));
        add(new Contact("Илона", "176.95.0.11"));
        add(new Contact("НАГИБ@ТОР_666", "176.95.0.34"));
        add(new Contact("Потрох", "176.95.1.34"));
        add(new Contact("Урукхай", "80.80.8.80"));
        add(new Contact("Щука", "80.0.0.80"));
        add(new Contact("Бодряк", "255.255.255.255"));
        add(new Contact("Silvia Saint", "255.255.2.3"));
        add(new Contact("Иозя", "123.255.255.255"));
    }
}
