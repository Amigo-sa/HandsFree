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

package  by.citech.bluetoothlegatt;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG  = "00002902-0000-1000-8000-00805f9b34fb";
    public static String CENTRAL_CHARACTERISTIC_CONFIG = "f0002902-0451-4000-b000-000000000000";


    public static String CIT_HANDS_FREE = "00002a00-0000-1000-8000-00805f9b34fb";
    public static String WRITE_BYTES = "f000b002-0451-4000-b000-000000000000";
    public static String READ_BYTES  = "f000b003-0451-4000-b000-000000000000";


    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Heart Rate Device Information Service");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("f000b000-0451-4000-b000-000000000000", "Data exchange with Hands free");

        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put(CIT_HANDS_FREE, "BLE Hands Free");
        attributes.put("f000b001-0451-4000-b000-000000000000", "Read/write 1 byte (now is not working)");
        attributes.put(WRITE_BYTES, "Write 16 byte");
        attributes.put(READ_BYTES, "Read/notify 16 byte from Hands free");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
