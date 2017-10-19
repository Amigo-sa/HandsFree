/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package by.citech.data;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG  = "00002902-0000-1000-8000-00805f9b34fb";


    public static String CIT_HANDS_FREE = "00002a00-0000-1000-8000-00805f9b34fb";
    public static String WRITE_BYTES = "f000b002-0451-4000-b000-000000000000";
    public static String READ_BYTES  = "f000b003-0451-4000-b000-000000000000";


    static {
        // Sample Services.
        attributes.put("00000801-0000-1000-8000-00805f9b34fb", "Temperature");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "GAP");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "GATT");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information");
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("f000b000-0451-4000-b000-000000000000", "HandsFree Data Exchange");
        // Sample Characteristics.
        attributes.put(CIT_HANDS_FREE, "Device Name");
        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        attributes.put("00002a04-0000-1000-8000-00805f9b34fb", "Peripheral Preffered Connection Parameters");
        attributes.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");
        attributes.put("00002a06-0000-1000-8000-00805f9b34fb", "Alert Level");
        attributes.put("00002a07-0000-1000-8000-00805f9b34fb", "Tx Power Level");
        attributes.put("00002a08-0000-1000-8000-00805f9b34fb", "Date Time");
        attributes.put("00002a09-0000-1000-8000-00805f9b34fb", "Day of Week");
        attributes.put("00002a0a-0000-1000-8000-00805f9b34fb", "Day Date Time");
        attributes.put("00002a0b-0000-1000-8000-00805f9b34fb", "Exact Time 100");

        /** all characteristic
         * https://www.bluetooth.com/specifications/gatt/characteristics
         * */

        attributes.put("00002a23-0000-1000-8000-00805f9b34fb", "System ID");
        attributes.put("00002a24-0000-1000-8000-00805f9b34fb", "Model Number String");
        attributes.put("00002a25-0000-1000-8000-00805f9b34fb", "Serial Number String");
        attributes.put("00002a26-0000-1000-8000-00805f9b34fb", "Firmware Revision String");
        attributes.put("00002a27-0000-1000-8000-00805f9b34fb", "Hardware Revision String");
        attributes.put("00002a28-0000-1000-8000-00805f9b34fb", "Software Revision String");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00002a2a-0000-1000-8000-00805f9b34fb", "IEEE 11073-20601 Regulatory");
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a50-0000-1000-8000-00805f9b34fb", "PNPID");

        attributes.put("f000b001-0451-4000-b000-000000000000", "Read/write 1 byte (now is not working)");
        attributes.put(WRITE_BYTES, "Write 16 byte");
        attributes.put(READ_BYTES, "Read/notify 16 byte from Hands free");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
