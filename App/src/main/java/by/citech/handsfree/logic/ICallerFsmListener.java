package by.citech.handsfree.logic;

public interface ICallerFsmListener {
    void onCallerStateChange(CallerState from, CallerState to, ECallReport why);
}
