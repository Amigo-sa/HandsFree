package by.citech.data;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by tretyak on 04.10.2017.
 */

public class StorageData {

        private ArrayList<byte []> databuffer;
        private boolean open;

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public StorageData() {
        databuffer = new ArrayList<byte[]>();
    }

    public synchronized byte[] getData() {
            byte[]  tmpData;
            if (databuffer.isEmpty()) {
                try {
                    wait();
                }
                catch (InterruptedException e) {
                }
            }
            tmpData = databuffer.get(0);
            databuffer.remove(0);
            notify();
            return tmpData;
        }


        public synchronized void putData(byte[] dataByte) {
            if (databuffer.size() > 100) {
                try {
                    wait();
                }
                catch (InterruptedException e) {
                }
            }
            databuffer.add(dataByte);
            notify();
        }

}
