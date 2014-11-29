package org.sirimangalo.meditationplus;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.IntentCompat;
import android.util.Log;

import java.io.IOException;

/**
 * Created by noah on 28/11/14.
 */
public class ServiceMediaPlayer extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    public static final String ACTION_PLAY = "org.sirimangalo.meditationplus.PLAY_FEED";
    public static final String ACTION_STOP = "org.sirimangalo.meditationplus.STOP_FEED";
    public static final String ACTION_CHECK = "org.sirimangalo.meditationplus.CHECK_FEED";
    private static MediaPlayer mMediaPlayer = null;
    public static final int NOTIFICATION_ID = 1234;
    private static WifiManager.WifiLock wifiLock;
    public static final int REQUEST_CODE = 5555;

    private String TAG = "ServiceMediaPlayer";

    private ResultReceiver resultReceiver;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_STOP)) {
                doRelease();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        resultReceiver = intent.getParcelableExtra("receiver");

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STOP);

        registerReceiver(broadcastReceiver, filter);

        if (intent.getAction().equals(ACTION_PLAY)) {


            if(mMediaPlayer != null) {
                if(mMediaPlayer.isPlaying()) {
                    Log.d(TAG, "Stopping audio stream");
                }
                doRelease();
                return START_STICKY;
            }

            Log.d(TAG, "Starting audio stream");

            String url = intent.getStringExtra("live_url");
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {
                mMediaPlayer.setDataSource(url);
            } catch (IOException e) {
                e.printStackTrace();
                return START_STICKY;
            }

            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.prepareAsync(); // prepare async to not block main thread

            resultReceiver.send(200, null);

            wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

            wifiLock.acquire();
        }
        else if (intent.getAction().equals(ACTION_STOP)) {
            doRelease();
        }
        else if (intent.getAction().equals(ACTION_CHECK)) {
            if(mMediaPlayer == null)
                resultReceiver.send(100, null);
            else if(mMediaPlayer.isPlaying())
                resultReceiver.send(300, null);
            else
                resultReceiver.send(200, null); // preparing
        }
        return START_STICKY;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        doRelease();
        return true;
    }

    public void doRelease() {
        try {
            wifiLock.release();
        }
        catch(Exception e) {
            // do nothing
        }

        try {
            mMediaPlayer.release();
        }
        catch(Exception e) {
            // do nothing
        }

        mMediaPlayer = null;

        resultReceiver.send(100, null);

        try{
            unregisterReceiver(broadcastReceiver);
        }
        catch(Exception e) {
            //do nothing
        }

        stopForeground(true);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                doRelease();
            }
        });

        player.start();

        Intent intent = new Intent(getApplicationContext(), ActivityMain.class);
        ComponentName cn = intent.getComponent();
        Intent mainIntent = IntentCompat.makeRestartActivityTask(cn);

        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), REQUEST_CODE,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);

        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.playing_live))
                .setTicker(getString(R.string.playing_live))
                .setOngoing(true)
                .setContentIntent(pi)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.stop), stopPendingIntent);

        startForeground(NOTIFICATION_ID, mBuilder.build());
        resultReceiver.send(300, null);
    }

    @Override
    public void onDestroy() {
        doRelease();
        super.onDestroy();
    }
}