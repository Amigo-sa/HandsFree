package by.citech.handsfree.gui.helper;

import android.widget.TextView;

import by.citech.handsfree.contact.Contact;

import static by.citech.handsfree.gui.helper.ViewHelper.setText;

public class ContactHelper {

    public static void setContactInfo(TextView name, TextView ip) {
        setContactInfo(null, name, ip);
    }

    public static void setContactInfo(Contact contact, TextView name, TextView ip) {
        if (contact != null) {
            setText(name, contact.getName());
            setText(ip, contact.getIp());
        } else {
            setText(name, "");
            setText(ip, "");
        }
    }

}
