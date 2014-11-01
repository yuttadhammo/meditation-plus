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

        PendingIntent mPendingIntent;
        Intent intent = new Intent(context, ReceiverAlarm.class);
        if(walking > 0) {
            intent.putExtra("period_time", walking);
            intent.putExtra("time", walking);
            intent.putExtra("type", context.getString(R.string.walking));
            int mTime = walking * 60 * 1000;

            mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= 19) {
                mAlarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mTime, mPendingIntent);
            }
            else {
                mAlarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mTime, mPendingIntent);
            }
        }

        if(sitting > 0) {
            intent.putExtra("period_time", sitting);
            intent.putExtra("time", walking+sitting);
            intent.putExtra("type", context.getString(R.string.sitting));
            int mTime = (walking+sitting) * 60 * 1000;

            mPendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= 19) {
                mAlarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mTime, mPendingIntent);
            }
            else {
                mAlarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mTime, mPendingIntent);
            }
        }

    }
}