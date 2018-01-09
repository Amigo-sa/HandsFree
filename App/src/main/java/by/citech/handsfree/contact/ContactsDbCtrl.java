package by.citech.handsfree.contact;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;

public class ContactsDbCtrl
        extends SQLiteOpenHelper {

    private static final String STAG = Tags.CONTACTS_DB_CTRL + " ST";
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private static final int DB_CONTACTS_VERSION = 1;
    private static final String DB_CONTACTS_NAME = "DB_CONTACTS";

    private static final String CREATE_DB_CONTACTS = String.format(
                    "CREATE TABLE IF NOT EXISTS %1$s (" +
                    "%2$s INTEGER PRIMARY KEY NOT NULL," +
                    "%3$s TEXT NOT NULL," +
                    "%4$s TEXT NOT NULL UNIQUE" +
                    ");",
            Contact.Contract.TABLE_NAME,
            Contact.Contract.COLUMN_NAME_ID,
            Contact.Contract.COLUMN_NAME_NAME,
            Contact.Contract.COLUMN_NAME_IP
            );

    //--------------------- base

    ContactsDbCtrl(Context context) {
        super(context, DB_CONTACTS_NAME, null, DB_CONTACTS_VERSION);
        if (debug) Log.i(TAG, "constructor");
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
        if (debug) Log.d(STAG, "buildContent");
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contact.Contract.COLUMN_NAME_NAME, contact.getName());
        contentValues.put(Contact.Contract.COLUMN_NAME_IP, contact.getIp());
        return contentValues;
    }

    private static boolean proc(SQLiteDatabase db, Runnable runnable) {
        if (db == null || runnable == null) {
            Log.e(STAG, "proc illegal parameters");
            return false;
        }
        db.beginTransaction();
        try {
            runnable.run();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return true;
    }

    boolean downloadAllContacts(List<Contact> contacts) {
        if (debug) Log.i(TAG, "downloadAllContacts");
        if (contacts == null) {
            Log.e(TAG, "downloadAllContacts contacts is null");
            return false;
        }
        SQLiteDatabase db = getReadableDatabase();
        if (db == null) {
            Log.e(TAG, "downloadAllContacts db is null");
            return false;
        }
        Cursor cursor = db.query(Contact.Contract.TABLE_NAME, null, null, null ,null, null, null);
        if (cursor == null) {
            Log.e(TAG, "downloadAllContacts cursor is null");
            return false;
        }
        if (cursor.moveToFirst()) {
            contacts.clear();
            if (debug) Log.i(TAG, "downloadAllContacts db already have contacts");
            int i = 0;
            do {
                contacts.add(new Contact(
                        cursor.getLong(cursor.getColumnIndex(Contact.Contract.COLUMN_NAME_ID)),
                        cursor.getString(cursor.getColumnIndex(Contact.Contract.COLUMN_NAME_NAME)),
                        cursor.getString(cursor.getColumnIndex(Contact.Contract.COLUMN_NAME_IP))));
                ++i;
            } while (cursor.moveToNext());
            if (debug) Log.i(TAG, "downloadAllContacts added all contacts to list: " + i);
        } else {
            if (debug) Log.i(TAG, "downloadAllContacts db have no contacts");
        }
        cursor.close();
        this.close();
        return true;
    }

    long add(Contact contact) {
        if (debug) Log.d(TAG, "add");
        if (contact == null) {
            Log.e(TAG, "delete contact is null");
            return -1;
        }
        final long[] id = new long[1];
        SQLiteDatabase db = getWritableDatabase();
        if (proc(db, () -> id[0] = db.insert(Contact.Contract.TABLE_NAME, null, buildContent(contact)))) {
            if (debug) Log.d(TAG, "add id: " + id[0]);
            return id[0];
        } else {
            return -1;
        }
    }

    boolean delete(Contact contact) {
        if (debug) Log.i(TAG, "delete");
        if (contact == null) {
            Log.e(TAG, "delete contact is null");
            return false;
        }
        final int[] delCount = new int[1];
        SQLiteDatabase db = getWritableDatabase();
        proc(db, () -> delCount[0] = db.delete(Contact.Contract.TABLE_NAME, "id = " + contact.getId(), null));
        if (delCount[0] == 1) {
            if (debug) Log.i(TAG, "delete deleted 1");
            return true;
        } else if (delCount[0] < 1) {
            Log.e(TAG, "delete LESS then 1: " + delCount[0]);
            return false;
        } else {
            Log.e(TAG, "delete MORE then 1: " + delCount[0]);
            return true;
        }
    }

    boolean update(Contact contactToUpd, Contact contactToCopy) {
        if (debug) Log.i(TAG, "update");
        if (contactToUpd == null || contactToCopy == null) {
            Log.e(TAG, "update illegal parameters");
            return false;
        }
        final int[] updCount = new int[1];
        SQLiteDatabase db = getWritableDatabase();
        proc(db, () -> updCount[0] = db.update(Contact.Contract.TABLE_NAME, buildContent(contactToCopy), "id = " + contactToUpd.getId(), null));
        if (updCount[0] == 1) {
            if (debug) Log.i(TAG, "update updated 1");
            return true;
        } else if (updCount[0] < 1) {
            Log.e(TAG, "update LESS then 1: " + updCount[0]);
            return false;
        } else {
            Log.e(TAG, "update MORE then 1: " + updCount[0]);
            return true;
        }
    }

    //--------------------- test

    void test() {
        Log.w(TAG, "TEST DB START");
        printDb();
        getSizeDb();
        if (isEmptyDb()) {
            testFillDb();
        }
        Log.w(TAG, "TEST DB END");
    }

    private void getSizeDb() {
        if (debug) Log.i(TAG, "getSizeDb");
        long size = DatabaseUtils.queryNumEntries(getReadableDatabase(), Contact.Contract.TABLE_NAME);
        this.close();
        Log.i(TAG, "getSizeDb db size is " + size);
    }

    private void printDb() {
        Log.i(TAG, "printDb");
        Cursor cursor = getReadableDatabase().query(Contact.Contract.TABLE_NAME, null, null, null ,null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Log.i(TAG, String.format("ID is %d, Name is %s, IP is %s",
                        cursor.getLong(cursor.getColumnIndex(Contact.Contract.COLUMN_NAME_ID)),
                        cursor.getString(cursor.getColumnIndex(Contact.Contract.COLUMN_NAME_NAME)),
                        cursor.getString(cursor.getColumnIndex(Contact.Contract.COLUMN_NAME_IP))));
            } while (cursor.moveToNext());
        } else {
            Log.i(TAG, "printDb db have no contacts");
        }
        cursor.close();
        this.close();
    }

    private boolean isEmptyDb() {
        if (debug) Log.i(TAG, "isEmptyDb");
        boolean isClear = (DatabaseUtils.queryNumEntries(getReadableDatabase(), Contact.Contract.TABLE_NAME) == 0);
        this.close();
        Log.i(TAG, "isEmptyDb db is " + (isClear ? "" : "not ") + "empty");
        return isClear;
    }

    private void clearDb() {
        if (debug) Log.i(TAG, "clearDb");
        getWritableDatabase().delete(Contact.Contract.TABLE_NAME, null, null);
        this.close();
    }

    private void testFillDb() {
        Log.w(TAG, "TEST FILL DB START");
        add(new Contact("Петька", "192.168.0.1"));
        add(new Contact("Саня", "192.12.0.1"));
        add(new Contact("Федя", "192.238.0.1"));
        add(new Contact("Коля", "192.38.0.1"));
        add(new Contact("Николаша", "19.38.0.1"));
        add(new Contact("Ушат Отходов", "76.3.0.1"));
        add(new Contact("Google", "8.8.8.8"));
        add(new Contact("КГБ", "127.0.0.1"));
        add(new Contact("Петька", "192.168.0.113"));
        add(new Contact("Бука Сука Димка", "66.18.0.263"));
        add(new Contact("Василий", "192.168.10.16"));
        add(new Contact("Борис Моисеев", "176.168.0.1"));
        add(new Contact("Илона", "176.95.0.11"));
        add(new Contact("НАГИБ@ТОР_666", "176.95.0.34"));
        add(new Contact("Потрох", "176.95.1.34"));
        add(new Contact("Урукхай", "80.80.8.80"));
        add(new Contact("Щука", "80.0.0.80"));
        add(new Contact("Бодряк", "255.255.255.255"));
        add(new Contact("Zoltan", "35.2.255.255"));
        add(new Contact("Silvia Saint", "255.255.2.3"));
        add(new Contact("Иозя", "123.255.255.255"));
        Log.w(TAG, "TEST FILL DB END");
    }

}
