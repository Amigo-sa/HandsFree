package by.citech.bluetoothlegatt;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by tretyak on 04.12.2017.
 */

public enum BluetoothLeState {

    IDLE{ // disconnect state
        @Override
        public HashSet<BluetoothLeState> availableStates() {
            return new HashSet<> (Arrays.asList(SCAN, CONNECTING));
        }
        @Override
        public String getName() {
            return "Idle";
        }
    },

    SCAN{
        @Override
        public HashSet<BluetoothLeState> availableStates() {
            return new HashSet<> (Arrays.asList(IDLE, CONNECTING));
        }

        @Override
        public String getName() { return "Scanning";}
    },

    CONNECTING{
        @Override
        public HashSet<BluetoothLeState> availableStates() {
            return new HashSet<> (Arrays.asList(IDLE, CONNECTED));
        }

        @Override
        public String getName() {
            return "Connecting";
        }
    },

    CONNECTED{
        @Override
        public HashSet<BluetoothLeState> availableStates() {
            return new HashSet<> (Arrays.asList(IDLE, TRANSMIT_DATA));
        }

        @Override
        public String getName() {
            return "Connected";
        }
    },

    TRANSMIT_DATA{
        @Override
        public HashSet<BluetoothLeState> availableStates() {
            return new HashSet<> (Arrays.asList(IDLE, CONNECTED));
        }

        @Override
        public String getName() {
            return "Transmit data";
        }
    };

    public abstract HashSet<BluetoothLeState> availableStates();
    public abstract String getName();

}
