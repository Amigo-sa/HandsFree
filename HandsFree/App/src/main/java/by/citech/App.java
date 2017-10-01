package by.citech;

import android.app.Application;
import android.content.Intent;

import by.citech.data.AppData;
import by.citech.logic.State;
import by.citech.utils.Logger;

public class App extends Application {
    private static App sInstance;

    private State mState;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        AppData.init(this);

        setState(State.Idle);
    }

    public static App getInstance() {
        return sInstance;
    }

    public void restart() {
        Intent i = new Intent(getBaseContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void setState(State state) {
        Logger.debug("State changed into " + state.getName());
        mState = state;
    }

    public State getState() {
        return mState;
    }
}