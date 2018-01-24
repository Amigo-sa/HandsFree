package by.citech.handsfree.connection;

import static by.citech.vincentwillemvangogh.connection.LightConfig.ELight.basicLight;

public class LightConfig {

    private boolean[][] cfg;
    {cfg = new boolean[EArea.values().length][ELight.values().length];}
    public boolean[][] getCfg() {return cfg;}

    public enum EArea {

        Building1 {
            @Override public String getName() {return "  Здание 1";}
            @Override public int getDataPosition(ELight light) { switch (light) {default: return -1;
                case basicLight: return 0;}}
        },
        Building2 {
            @Override public String getName() {return "  Здание 2";}
            @Override public int getDataPosition(ELight light) { switch (light) {default: return -1;
                case basicLight: return 2;}}},
        Building3 {
            @Override public String getName() {return "  Здание 3";}
            @Override public int getDataPosition(ELight light) { switch (light) {default: return -1;
                case basicLight: return 4;}}},
        Building4 {
            @Override public String getName() {return "  Здание 4";}
            @Override public int getDataPosition(ELight light) { switch (light) {default: return -1;
                case basicLight: return 6;}}},
        Building5 {
            @Override public String getName() {return "  Здание 5";}
            @Override public int getDataPosition(ELight light) { switch (light) {default: return -1;
                case basicLight: return 8;}}},
        Street {
            @Override public String getName() {return "     Улица";}
            @Override public int getDataPosition(ELight light) { switch (light) {default: return -1;
                case basicLight: return 9;}}},
        Seafront {
            @Override public String getName() {return "Набережная";}
            @Override public int getDataPosition(ELight light) { switch (light) {default: return -1;
                case basicLight: return 10;}}},
        Bridge {
            @Override public String getName() {return "      Мост";}
            @Override public int getDataPosition(ELight light) { switch (light) {default: return -1;
                case basicLight: return 11;}}};

        public abstract String getName();
        public abstract int getDataPosition(ELight light);
        public ELight getBasicLightSwitch() {return basicLight;}

    }

    public enum ELight {

        basicLight;

        public static byte getValue(boolean isOn) {
            if (isOn) return on;
            else      return off;
        }

        public static byte[] getValues() {return values;}
        public static boolean isOn(byte value) {return value == on;}

        public static final byte on = (byte) 0x01;
        public static final byte off = (byte) 0x00;
        public static final byte[] values = new byte[]{off, on};

    }

}
