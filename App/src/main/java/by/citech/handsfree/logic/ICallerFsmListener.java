package by.citech.handsfree.logic;

public interface ICallerFsmListener {
    void onCallerStateChange(ECallerState from, ECallerState to, ECallReport why);
}
