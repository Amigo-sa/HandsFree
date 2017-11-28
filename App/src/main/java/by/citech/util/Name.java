package by.citech.util;

public class Name {

    private final static String PATTERN = "^[^ ].*$";

    public static boolean checkForValidityContactName(String name) {
        return (name != null && !name.isEmpty() && name.matches(PATTERN));
    }

}
