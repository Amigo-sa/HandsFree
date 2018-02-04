package by.citech.handsfree.proximity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

import by.citech.handsfree.application.ThisApp;
import by.citech.handsfree.threading.IThreading;
import timber.log.Timber;

import static by.citech.handsfree.proximity.ProximitySensorListener.EDistance.*;

public class ProximitySensorListener
        implements SensorEventListener, IThreading {

    private List<Runnable> toDoOnClose, toDoOnFar;
    private SensorManager sensorManager;
    private Sensor sensor;
    private EDistance state;

    public ProximitySensorListener() {
        sensorManager = (SensorManager) ThisApp.getAppContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            toDoOnClose = new ArrayList<>();
            toDoOnFar = new ArrayList<>();
        }
    }

    public void register() {
        if (isReady()) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
            prepare();
        }
    }

    public void unregister() {
        if (isReady()) {
            sensorManager.unregisterListener(this);
            prepare();
        }
    }

    public void addOnClose(Runnable r) {
        if (isReady() && r != null) toDoOnClose.add(r);
    }

    public void addOnFar(Runnable r) {
        if (isReady() && r != null) toDoOnFar.add(r);
    }

    //-------------------------- SensorEventListener

    @Override
    public void onSensorChanged(SensorEvent event) {
        float distance = event.values[0];
        Timber.w("onSensorChanged %f", distance);
        switch(processDistance(distance)) {
            case Close:
                foreach(toDoOnClose);
                break;
            case Far:
                foreach(toDoOnFar);
                break;
            case Same:
            default:
                break;
        }
    }

    private EDistance processDistance(float distance) {
        if (distance <= 0.1F && state != Close) {
            state = Close;
        } else if (distance > 0.1F && state != Far) {
            state = Far;
        } else {
            return Same;
        }
        return state;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Timber.w("onAccuracyChanged");
    }

    //-------------------------- additional

    private void prepare() {
        toDoOnClose.clear();
        toDoOnFar.clear();
    }

    private void foreach(List<Runnable> toDo) {
        for (Runnable r : toDo) addRunnable(r);
    }

    private boolean isReady() {
        return sensorManager != null && sensor != null;
    }

    enum EDistance {
        Close, Far, Same
    }

}
