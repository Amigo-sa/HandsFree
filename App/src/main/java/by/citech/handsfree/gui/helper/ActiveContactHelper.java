package by.citech.handsfree.gui.helper;

import android.util.Log;

import by.citech.handsfree.common.IBase;
import by.citech.handsfree.gui.helper.state.ActiveContactState;
import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class ActiveContactHelper
        implements IBase {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.ACTIVE_HELPER;

    private ActiveContactState activeContactState;
    private ChosenContactHelper chosenContactHelper;
    private ViewHelper viewHelper;

    public ActiveContactHelper(ChosenContactHelper chosenContactHelper, ViewHelper viewHelper) {
        this.chosenContactHelper = chosenContactHelper;
        this.viewHelper = viewHelper;
        activeContactState = ActiveContactState.IpFromSearch;
    }

    //--------------------- IBase

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        return true;
    }

    @Override
    public boolean baseStop() {
//        IBase.super.baseStop();
        if (debug) Log.i(TAG, "baseStop");
        activeContactState = null;
        chosenContactHelper = null;
        viewHelper = null;
        return true;
    }

    //--------------------- getters and setters

    public Contact getContact() {
        switch (activeContactState) {
            case IpFromSearch:
                if (debug) Log.i(TAG, "getContact IpFromSearch");
            case FromEditor:
                if (debug) Log.i(TAG, "getContact FromEditor");
                return new Contact(getName(), getIp());
            case FromChosen:
                if (debug) Log.i(TAG, "getContact FromChosen");
                return chosenContactHelper.getContact();
            case Null:
                Log.e(TAG, "ActiveContactHelper getContact state null");
                break;
            default:
                Log.e(TAG, "ActiveContactHelper getContact state default");
                break;
        }
        return null;
    }

    public ActiveContactState getState() {
        return activeContactState;
    }

    public void goToState(ActiveContactState toState) {
        if (debug) Log.i(TAG, "goToState");
        this.activeContactState = toState;
        switch (activeContactState) {
            case FromChosen:
                break;
            case Null:
                break;
            case Default:
                if (chosenContactHelper.isChosen())
                    activeContactState = ActiveContactState.FromChosen;
                else
                    activeContactState = ActiveContactState.IpFromSearch;
                break;
            case IpFromSearch:
                break;
            case FromEditor:
                break;
            default:
                Log.e(TAG, "goToState activeContactState default");
                break;
        }
    }

    public String getName() {
        if (debug) Log.i(TAG, "getName");
        switch (activeContactState) {
            case FromChosen:
                return getContact().getName();
            case FromEditor:
                return viewHelper.getEditorContactNameText();
            case IpFromSearch:
                return "";
            default:
                Log.e(TAG, "getSettingName editorState default");
                return "";
        }
    }

    public String getIp() {
        if (debug) Log.i(TAG, "getIp");
        switch (activeContactState) {
            case FromChosen:
                if (debug) Log.i(TAG, "getIp FromChosen");
                return getContact().getIp();
            case FromEditor:
                if (debug) Log.i(TAG, "getIp FromEditor");
                return viewHelper.getEditorContactIpText();
            case IpFromSearch:
                if (debug) Log.i(TAG, "getIp IpFromSearch");
                return viewHelper.getSearchText();
            default:
                Log.e(TAG, "getIp editorState default");
                return "";
        }
    }

}
