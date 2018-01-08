package by.citech.handsfree.contact;

import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

import by.citech.handsfree.common.ICopy;
import by.citech.handsfree.common.IIdentifier;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.util.InetAddress;
import by.citech.handsfree.util.Name;


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
    private EContactState state;

    //--------------------- common

    private Contact() {
    }

    public Contact(String name, String ip) {
        id = -1;
        state = EContactState.Null;
        this.name = name;
        this.ip = ip;
        if (debug) Log.d(TAG, toString() + " contactTrunkCount is: " + (++contactTrunkCount));
    }

    public Contact(long id, String name, String ip) {
        this(name, ip);
        this.id = id;
        if (debug) Log.d(TAG, toString() + " contactFullCount is: " + (++contactFullCount));
    }

    public Contact(long id, Contact contact) {
        this(contact.getName(), contact.getIp());
        this.id = id;
        if (debug) Log.d(TAG, toString() + " contactFullCount is: " + (++contactFullCount));
    }

    public static boolean checkForEqual(Contact toCheck1, Contact toCheck2) {
        if (debug) Log.i(TAG, "checkForEqual");
        return toCheck1.ip.equals(toCheck2.ip);
    }

    public static boolean checkForValid(Contact contact) {
        if (debug) Log.i(TAG, "checkForValid");
        boolean isIpValid = InetAddress.checkForValidityIpAddr(contact.getIp());
        boolean isNameValid = Name.checkForValidityContactName(contact.getName());
        if (debug) Log.i(TAG, String.format("checkForValid ip is %s, name is %s",
                isIpValid ? VALID : INVALID,
                isNameValid ? VALID : INVALID));
        return isIpValid && isNameValid;
    }

    //--------------------- getters and setters

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setState(EContactState state) {
        this.state = state;
    }

    //--------------------- interfaces

    @Override
    public long getId() {
        return id;
    }

    @Override
    public EContactState getState() {
        return state;
    }

    @Override
    public int compareTo(@NonNull Contact o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public void doCopy(Contact toCopy) {
        this.name = toCopy.name;
        this.ip = toCopy.ip;
    }

    //--------------------- base

    @Override
    protected Contact clone() throws CloneNotSupportedException {
        Contact clone = (Contact) super.clone();
        clone.name = this.name;
        clone.ip = this.ip;
        clone.id = this.id;
        clone.state = EContactState.valueOf(this.state.name());
        if (debug) Log.w(TAG, "original is " + this.toString() + "\nclone is " + clone.toString());
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contact contact = (Contact) o;

        if (id != contact.id) return false;
        if (!name.equals(contact.name)) return false;
        if (!ip.equals(contact.ip)) return false;
        return state == contact.state;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + name.hashCode();
        result = 31 * result + ip.hashCode();
        result = 31 * result + state.hashCode();
        return result;
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
