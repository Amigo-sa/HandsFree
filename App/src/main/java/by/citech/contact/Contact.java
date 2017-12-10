package by.citech.contact;

import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

import by.citech.param.Settings;
import by.citech.param.Tags;
import by.citech.util.InetAddress;
import by.citech.util.Name;


public class Contact
        implements Comparable<Contact>, IIdentifier, ICopy<Contact>, IContactState, Cloneable {

    private static final String TAG = Tags.CONTACT;
    private static final boolean debug = Settings.debug;

    private static final String VALID = "valid";
    private static final String INVALID = "invalid";
    private static int contactTrunkCount = 0;
    private static int contactFullCount = 0;

    //TODO: проверить соответствие типам SQLite
    private long id;
    private String name;
    private String ip;
    private ContactState state;

    //--------------------- common

    private Contact() {
    }

    public Contact(String name, String ip) {
        id = -1;
        state = ContactState.Null;
        this.name = name;
        this.ip = ip;
        if (debug) Log.i(TAG, toString() + " contactTrunkCount is: " + (++contactTrunkCount));
    }

    public Contact(long id, String name, String ip) {
        this(name, ip);
        this.id = id;
        if (debug) Log.i(TAG, toString() + " contactFullCount is: " + (++contactFullCount));
    }

    public Contact(long id, Contact contact) {
        this(contact.getName(), contact.getIp());
        this.id = id;
        if (debug) Log.i(TAG, toString() + " contactFullCount is: " + (++contactFullCount));
    }

    public static boolean checkForValid(Contact contact) {
        if (debug) Log.i(TAG, "checkForValid");
        boolean isIpValid = InetAddress.checkForValidityIpAddress(contact.getIp());
        boolean isNameValid = Name.checkForValidityContactName(contact.getName());
        if (debug) Log.i(TAG, String.format("checkForValid ip is %s, name is %s",
                isIpValid ? VALID : INVALID,
                isNameValid ? VALID : INVALID));
        return isIpValid && isNameValid;
    }

    //--------------------- getters and setters

    public String getName() {return name;}
    public String getIp() {return ip;}
    public void setId(long id) {this.id = id;}
    public void setState(ContactState state) {this.state = state;}

    //--------------------- interfaces

    @Override
    public long getId() {
        return id;
    }

    @Override
    public ContactState getState() {
        return state;
    }

    @Override
    public int compareTo(@NonNull Contact o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public void doCopy(Contact contact) {
        this.name = contact.name;
        this.ip = contact.ip;
    }

    //--------------------- base

    @Override
    protected Contact clone() throws CloneNotSupportedException {
        Contact clone = (Contact) super.clone();
        clone.name = this.name;
        clone.ip = this.ip;
        clone.id = this.id;
        clone.state = ContactState.valueOf(this.state.name());
        if (debug) Log.w(TAG, "original is " + this.toString() + "\nclone is " + clone.toString());
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return ip.equals(contact.ip);
    }

    @Override
    public int hashCode() {
        return ip.hashCode();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Contact{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", ip='").append(ip).append('\'');
        sb.append(", state=").append(state);
        sb.append('}');
        return sb.toString();
    }

    public static abstract class Contract implements BaseColumns {
        public static final String TABLE_NAME = "contacts";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_IP = "ip";
    }

}
