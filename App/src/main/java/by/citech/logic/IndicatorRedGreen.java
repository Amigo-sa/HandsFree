package by.citech.logic;

import android.util.Log;
import android.widget.Button;

import by.citech.param.Settings;
import by.citech.param.Tags;

public class IndicatorRedGreen {

    //--------------------- Buttons

    private void onClickBtnRed() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "onClickBtnRed");
        if (isOnCall) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "onClickBtnRed isOnCall");
            callEnd(iConnCtrl);
        } else if (isOutcomingCall || isOutcomingConnection) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "onClickBtnRed isOutcomingCall");
            callOutcomingCancel();
        } else if (isIncomingCall) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "onClickBtnRed isIncomingCall");
            callIncomingReject();
        } else {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "onClickBtnRed unknown");
        }
    }

    private void onClickBtnGreen() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "onClickBtnGreen");
        if (!isOnCall) {
            if (!isOutcomingCall && !isIncomingCall) {
                if (Settings.debug) Log.i(Tags.ACT_DPL, "onClickBtnGreen !isOutcomingCall && !isIncomingCall");
                callOutcoming();
            } else if (isIncomingCall) {
                if (Settings.debug) Log.i(Tags.ACT_DPL, "onClickBtnGreen isIncomingCall");
                callIncomingAccept();
            } else {
                if (Settings.debug) Log.i(Tags.ACT_DPL, "onClickBtnGreen unknown");
            }
        }
    }

    private void btnSetDisabled(Button button, String label, int color) {
        button.setEnabled(false);
        btnSetColorLabel(button, label, color);
    }

    private void btnSetEnabled(Button button, String label, int color) {
        button.setEnabled(true);
        btnSetColorLabel(button, label, color);
    }

    private void btnSetColorLabel(Button button, String label, int color) {
        button.setText(label);
        button.setBackgroundColor(color);
    }

    private void callAnimStart() {
        if (Settings.debug && !isCallAnim) Log.i(Tags.ACT_DPL, "callAnimStart");
        btnGreen.startAnimation(animCall);
        isCallAnim = true;
    }

    private void callAnimStop() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "callAnimStop");
        btnGreen.clearAnimation();
        isCallAnim = false;
    }
}
