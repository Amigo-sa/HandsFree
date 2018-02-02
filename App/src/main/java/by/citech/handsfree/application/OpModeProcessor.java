package by.citech.handsfree.application;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import by.citech.handsfree.activity.CallActivity;

public class OpModeProcessor {

    public static void onOpModeChange() {
        Context context = ThisApp.getAppContext();
        Intent intent = new Intent(context, CallActivity.class);
        int pendingIntentId = 123456;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, pendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManaget = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManaget.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);
        System.exit(0);
    }

}
