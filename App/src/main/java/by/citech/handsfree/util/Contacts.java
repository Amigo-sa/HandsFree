package by.citech.handsfree.util;

import android.widget.EditText;
import android.widget.TextView;

import by.citech.handsfree.contact.Contact;

public class Contacts {

    public static void setContactInfo(TextView name, TextView ip) {
        setContactInfo(null, name, ip);
    }

    public static void setContactInfo(EditText name, EditText ip) {
        setContactInfo(null, name, ip);
    }

    public static void setContactInfo(Contact contact, TextView name, TextView ip) {
        if (name != null) {
            if (contact != null)
                name.setText(contact.getName());
            else
                name.setText("");
        }
        if (ip != null) {
            if (contact != null)
                ip.setText(contact.getIp());
            else
                ip.setText("");
        }
    }

    public static void setContactInfo(Contact contact, EditText name, EditText ip) {
        if (name != null) {
            if (contact != null)
                name.setText(contact.getName());
            else
                name.setText("");
        }
        if (ip != null) {
            if (contact != null)
                ip.setText(contact.getIp());
            else
                ip.setText("");
        }
    }

}
