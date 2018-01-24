package by.citech.handsfree.activity.fsm;

public interface IActivityFsmListener {
    void onActivityFsmStateChange(EActivityState from, EActivityState to, EActivityReport why);
}
