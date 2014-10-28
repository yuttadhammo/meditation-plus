package org.sirimangalo.meditationplus;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

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
    private int time;
    private int periodTime;
    private String type;

    // The android system alarm manager
    private final AlarmManager mAlarmMgr;
    // Your context to retrieve the alarm manager from
    private final Context context;

    public AlarmTask(Context context, int _periodTime, int _time, String _type) {
        this.context = context;
        this.mAlarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.periodTime = _periodTime;
        this.time = _time;
        this.type = _type;
    }

    @Override
    public void run() {
        Intent intent = new Intent(context, ReceiverAlarm.class);
        intent.putExtra("period_time", periodTime);
        intent.putExtra("time", time);
        intent.putExtra("type", type);

        int mTime = time * 60 * 1000;

        int id = type.equals(context.getString(R.string.walking)) ? 0 : 1;

        //Log.d(TAG, String.format(context.getString(R.string.alarm_desc), intent.getStringExtra("type"), intent.getIntExtra("period_time",0)));

        PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= 19) {
            mAlarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mTime, mPendingIntent);
        }
        else {
            mAlarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mTime, mPendingIntent);
        }
    }
}