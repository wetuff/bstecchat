package actiondev.weme.bstec;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class MyAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            context.startForegroundService(new Intent(context, MyFirebaseMessagingService.class));
        }
        else
        {
            context.startService(new Intent(context, MyFirebaseMessagingService.class));
        }

        Intent i = new Intent(context, BackgroundService.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //context.startActivity(i);


        MyFirebaseMessagingService.start(context);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent mIntent = new Intent();
        mIntent.setAction("actiondev.weme.bstec.MyAlarmReceiver");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, mIntent, 0);

        if (alarmManager == null) return;
        if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2 * 60 * 1000, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2 * 60 * 1000, pendingIntent);
        }
    }
}
