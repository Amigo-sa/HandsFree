package by.citech.handsfree.bluetoothlegatt;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by tretyak on 04.12.2017.
 */

public enum BluetoothLeState {

    DISCONECTED{
        @Override
        public HashSet<BluetoothLeState> availableStates() {
            return new HashSet<> (Arrays.asList(CONNECTED));
        }

        @Override
        public String getName() {
            return "Disconected";
        }
    },

    CONNECTING{
        @Override
        public HashSet<BluetoothLeState> availableStates() {
            return new HashSet<> (Arrays.asList(DISCONECTED, CONNECTED));
        }

        @Override
        public String getName() {
            return "Connected";
        }
    },

    CONNECTED{
        @Override
        public HashSet<BluetoothLeState> availableStates() {
            return new HashSet<> (Arrays.asList(DISCONECTED, SERVICES_DISCOVERED));
        }

        @Override
        public String getName() {
            return "Connected";
        }
    },

    SERVICES_DISCOVERED{
        @Override
        public HashSet<BluetoothLeState> availableStates() {
            return new HashSet<> (Arrays.asList(DISCONECTED, CONNECTED, TRANSMIT_DATA));
        }

        @Override
        public String getName() {
            return "Service discovered";
        }
    },

    TRANSMIT_DATA{
        @Override
        public HashSet<BluetoothLeState> availableStates() {
            return new HashSet<> (Arrays.asList(DISCONECTED, SERVICES_DISCOVERED, TRANSMIT_DATA));
        }

        @Override
        public String getName() {
            return "Transmit data";
        }
    };

    public abstract HashSet<BluetoothLeState> availableStates();
    public abstract String getName();
}
