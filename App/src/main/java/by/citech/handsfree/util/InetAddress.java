package by.citech.handsfree.util;

public class InetAddress {
    private static final String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

    public static boolean checkForValidityIpAddr(String ip) {
        return (ip != null
                && !ip.isEmpty()
                && ip.matches(PATTERN));
    }
}
