package by.citech.logic;

import java.io.Serializable;

public class Resource implements Serializable{
    private boolean callback;
    private int supportedMTU;

    public Resource(boolean callback, int supportedMTU) {
        this.callback = callback;
        this.supportedMTU = supportedMTU;
    }

    public boolean isCallback() {
        return callback;
    }

    public void setCallback(boolean callback) {
        this.callback = callback;
    }

    public int getSupportedMTU() {
        return supportedMTU;
    }

    public void setSupportedMTU(int supportedMTU) {
        this.supportedMTU = supportedMTU;
    }
}
