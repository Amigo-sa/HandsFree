package by.citech.handsfree.ui.helpers;

import android.util.Log;

import by.citech.handsfree.management.IBase;
import by.citech.handsfree.contact.Contact;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;

public class ActiveContactHelper
        implements IBase {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.ActiveContactHelper;

    private EActiveContactState activeContactState;
    private ChosenContactHelper chosenContactHelper;
    private ViewManager viewManager;

    public ActiveContactHelper(ChosenContactHelper chosenContactHelper, ViewManager viewManager) {
        this.chosenContactHelper = chosenContactHelper;
        this.viewManager = viewManager;
        activeContactState = EActiveContactState.IpFromSearch;
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
        if (debug) Log.i(TAG, "baseStop");
        activeContactState = null;
        chosenContactHelper = null;
        viewManager = null;
        IBase.super.baseStop();
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

    public EActiveContactState getState() {
        return activeContactState;
    }

    public void goToState(EActiveContactState toState) {
        if (debug) Log.i(TAG, "goToState");
        this.activeContactState = toState;
        switch (activeContactState) {
            case FromChosen:
                break;
            case Null:
                break;
            case Default:
                if (chosenContactHelper.isChosen())
                    activeContactState = EActiveContactState.FromChosen;
                else
                    activeContactState = EActiveContactState.IpFromSearch;
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
                return viewManager.getEditorContactNameText();
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
                return viewManager.getEditorContactIpText();
            case IpFromSearch:
                if (debug) Log.i(TAG, "getIp IpFromSearch");
                return viewManager.getSearchText();
            default:
                Log.e(TAG, "getIp editorState default");
                return "";
        }
    }

}
