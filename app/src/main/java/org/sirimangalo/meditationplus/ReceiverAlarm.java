package org.sirimangalo.meditationplus;

/*
    This file is part of Bodhi Timer.

    Bodhi Timer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Bodhi Timer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Bodhi Timer.  If not, see <http://www.gnu.org/licenses/>.
*/

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class ReceiverAlarm extends BroadcastReceiver {
    private final static String TAG = "TimerReceiver";
    final static String CANCEL_NOTIFICATION = "CANCEL_NOTIFICATION";
    public static MediaPlayer player;

    private TextToSpeech tts;

    @Override
    public void onReceive(Context context, Intent pIntent)
    {
        Log.v(TAG, "ALARM: received alarm");

        NotificationManager mNM = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(player != null) {
            Log.v(TAG,"Releasing media player...");
            try{
                player.release();
                player = null;
            }
            catch(Exception e) {
                e.printStackTrace();
                player = null;
            }
            finally {
                // do nothing
            }
        }

        // Cancel notification and return...
        if (CANCEL_NOTIFICATION.equals(pIntent.getAction())) {
            Log.v(TAG,"Cancelling notification...");

            mNM.cancelAll();
            return;
        }

        // ...or display a new one

        Log.v(TAG,"Showing notification...");

        player = new MediaPlayer();

        int setTime = pIntent.getIntExtra("period_time",0);
        String medType = pIntent.getStringExtra("type");

        String title = context.getString(R.string.alarm_title);
        String desc = String.format(context.getString(R.string.alarm_desc), medType, setTime);

        Log.v(TAG,"Notification: "+desc);

        // Load the settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean led = prefs.getBoolean("alarm_led",true);
        boolean vibrate = prefs.getBoolean("alarm_vibrate",true);
        String notificationUri = prefs.getString("notification_uri", "android.resource://org.sirimangalo.meditationplus/" + R.raw.bell);

        Log.v(TAG,"notification uri: "+notificationUri);

        if(notificationUri.equals("system"))
            notificationUri = prefs.getString("SystemUri", "");
        else if(notificationUri.equals("file"))
            notificationUri = prefs.getString("FileUri", "");
        else if (notificationUri.equals("tts")) {
            notificationUri = "";
            final String ttsString = prefs.getString("tts_string",desc);
            Intent ttsIntent = new Intent(context,TTSService.class);
            ttsIntent.putExtra("spoken_text", ttsString);
            context.startService(ttsIntent);
        }

        if(notificationUri.equals(""))
            return;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context.getApplicationContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(desc);

        Uri uri = Uri.parse(notificationUri);
        mBuilder.setSound(uri);

        // Vibrate
        if(vibrate){
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        }

        // Have a light
        if(led){
            mBuilder.setLights(0xffffffff, 300, 1000);
        }

        mBuilder.setAutoCancel(true);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context,ActivityMain.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ActivityMain.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        mNotificationManager.notify(0, mBuilder.build());

    }
}