package by.citech.handsfree.contact;

import android.widget.TextView;

import static by.citech.handsfree.ui.helpers.ViewHelper.setText;

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
