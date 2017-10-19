package by.citech.bluetoothlegatt;

import java.io.Serializable;

/**
 * Created by tretyak on 05.10.2017.
 */

public class Resource implements Serializable{
    private boolean loopback;
    private boolean write;

    public Resource(boolean write) {
        this.write = write;
    }

    public boolean isLoopback() {
        return loopback;
    }
    public void setLoopback(boolean loopback) {
        this.loopback = loopback;
    }

    public boolean isWrite() {
        return write;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }
}
