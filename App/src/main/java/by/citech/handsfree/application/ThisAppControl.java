package by.citech.handsfree.application;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import by.citech.handsfree.activity.CallActivity;
import by.citech.handsfree.activity.ExitActivity;

public class ThisAppControl {

    public static void exitApplication() {
        Context context = ThisApp.getAppContext();
        Intent intent = new Intent(context, ExitActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NO_ANIMATION
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
    }

    public static void restartApplication() {
        Context context = ThisApp.getAppContext();
        Intent intent = new Intent(context, CallActivity.class);
        int pendingIntentId = 123456;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, pendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManaget = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManaget.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);
        exitApplication();
    }

}
