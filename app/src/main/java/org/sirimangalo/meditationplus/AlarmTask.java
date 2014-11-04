package org.sirimangalo.meditationplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Set an alarm for the date passed into the constructor
 * When the alarm is raised it will start the NotifyService
 *
 * This uses the android build in alarm manager *NOTE* if the phone is turned off this alarm will be cancelled
 *
 * This will run on it's own thread.
 *
 * @author paul.blundell
 */
public class AlarmTask implements Runnable{
    // The date selected for the alarm
    private String TAG = "AlarmTask";
    private int sitting;
    private int walking;

    // The android system alarm manager
    private final AlarmManager mAlarmMgr;
    // Your context to retrieve the alarm manager from
    private final Context context;

    public AlarmTask(Context context, int _walking, int _sitting) {
        this.context = context;
        this.mAlarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.walking = _walking;
        this.sitting = _sitting;
    }

    @Override
    public void run() {

        if(walking > 0) {
            PendingIntent wPendingIntent;
            Intent wIntent = new Intent(context, ReceiverAlarm.class);
            wIntent.putExtra("period_time", walking);
            wIntent.putExtra("time", walking);
            wIntent.putExtra("type", context.getString(R.string.walking));
            int mTime = walking * 60 * 1000;

            wPendingIntent = PendingIntent.getBroadcast(context, 0, wIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= 19) {
                mAlarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mTime, wPendingIntent);
            }
            else {
                mAlarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mTime, wPendingIntent);
            }
        }

        if(sitting > 0) {
            Intent sIntent = new Intent(context, ReceiverAlarm.class);
            sIntent.putExtra("period_time", sitting);
            sIntent.putExtra("time", walking + sitting);
            sIntent.putExtra("type", context.getString(R.string.sitting));
            int mTime = (walking+sitting) * 60 * 1000;

            PendingIntent sPendingIntent = PendingIntent.getBroadcast(context, 1, sIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= 19) {
                mAlarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mTime, sPendingIntent);
            }
            else {
                mAlarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mTime, sPendingIntent);
            }
        }

    }
}