package org.sirimangalo.meditationplus;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by noah on 10/16/14.
 */
public class ActivityPrefs extends PreferenceActivity {

    private String TAG = "ActivityPrefs";

    private MediaPlayer player;
    private Preference play;

    private final int SELECT_RINGTONE = 0;
    private final int SELECT_FILE = 1;

    private String lastToneType;

    private HashMap<String,String> entryMap;

    private TextToSpeech tts;

    private ActivityPrefs context;
    private SharedPreferences prefs;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        tts = new TextToSpeech(this,null);

        ActionBar actionBar = getActionBar();

        if(Build.VERSION.SDK_INT >= 14 && actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        addPreferencesFromResource(R.xml.preferences);

        String [] entries = getResources().getStringArray(R.array.sound_names);
        final String [] entryValues = getResources().getStringArray(R.array.sound_uris);

        entryMap = new HashMap<String, String>();

        for(int i = 0; i < entries.length; i++) {
            entryMap.put(entryValues[i],entries[i]);
        }

        final ListPreference tone = (ListPreference)findPreference("notification_uri");
        play = (Preference)findPreference("play_sound");

        //Default value
        if(tone.getValue() == null) tone.setValue((String)entryValues[1]);
        tone.setDefaultValue((String)entryValues[1]);
        tone.setSummary(entryMap.get(tone.getValue()));

        tone.setEntries(entries);
        tone.setEntryValues(entryValues);

        player = new MediaPlayer();

        tone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(player.isPlaying()) {
                    play.setTitle(context.getString(R.string.play_sound));
                    play.setSummary(context.getString(R.string.play_sound_desc));
                    player.stop();
                }
                if(newValue.toString().equals("system")) {
                    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                    context.startActivityForResult(intent, SELECT_RINGTONE);
                }
                else if(newValue.toString().equals("file")) {

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("audio/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);

                    try {
                        context.startActivityForResult(Intent.createChooser(intent, "Select Sound File"), SELECT_FILE);
                    }
                    catch (ActivityNotFoundException ex) {
                        Toast.makeText(context, "Please install a File Manager.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else if(newValue.toString().equals("tts")) {
                    final EditText input = new EditText(context);
                    input.setText(prefs.getString("tts_string",getString(R.string.alarm_desc)));
                    new AlertDialog.Builder(context)
                            .setTitle(getString(R.string.input_text))
                            .setMessage(getString(R.string.input_text_desc))
                            .setView(input)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if(input.getText().toString().equals(""))
                                        return;

                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("tts_string", input.getText().toString());
                                    editor.apply();
                                }
                            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Do nothing.
                        }
                    }).show();
                }
                else
                    lastToneType = (String) newValue;

                preference.setSummary(entryMap.get(lastToneType));

                return true;
            }

        });

        play.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(final Preference preference) {
                if (player.isPlaying()) {
                    player.stop();
                    play.setTitle(context.getString(R.string.play_sound));
                    play.setSummary(context.getString(R.string.play_sound_desc));
                    return false;
                }

                try {
                    String notificationUri = prefs.getString("notification_uri", "android.resource://org.sirimangalo.meditationplus/" + R.raw.bell);
                    if (notificationUri.equals("system"))
                        notificationUri = prefs.getString("SystemUri", "");
                    else if (notificationUri.equals("file"))
                        notificationUri = prefs.getString("FileUri", "");
                    else if (notificationUri.equals("tts")) {
                        notificationUri = "";
                        final String ttsString = prefs.getString("tts_string",context.getString(R.string.timer_done));
                        tts.speak(ttsString, TextToSpeech.QUEUE_ADD, null);
                    }
                    if (notificationUri.equals(""))
                        return false;
                    Log.v(TAG, "Playing Uri: " + notificationUri);
                    player.reset();
                    player.setDataSource(context, Uri.parse(notificationUri));
                    player.prepare();
                    player.setLooping(false);
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            preference.setTitle(context.getString(R.string.play_sound));
                            preference.setSummary(context.getString(R.string.play_sound_desc));
                        }
                    });
                    player.start();
                    preference.setTitle(context.getString(R.string.stop_sound));
                    preference.setSummary(context.getString(R.string.stop_playing_sound));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return false;
            }

        });

        final CheckBoxPreference alarm = (CheckBoxPreference) findPreference("set_alarm");
        final CheckBoxPreference vibrate = (CheckBoxPreference) findPreference("alarm_vibrate");
        final CheckBoxPreference led = (CheckBoxPreference) findPreference("alarm_led");
        if(!alarm.isChecked()) {
            tone.setEnabled(false);
            play.setEnabled(false);
            vibrate.setEnabled(false);
            led.setEnabled(false);
        }
            
        alarm.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if((Boolean) o) {
                    tone.setEnabled(true);
                    play.setEnabled(true);
                    vibrate.setEnabled(true);
                    led.setEnabled(true);
                }
                else {
                    tone.setEnabled(false);
                    play.setEnabled(false);
                    vibrate.setEnabled(false);
                    led.setEnabled(false);
                }

                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

            getMenuInflater().inflate(R.menu.prefs, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent i;
        switch(id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_htm:
                Utils.openHTM(this);
                return true;
            case R.id.action_help:
                i = new Intent(this,ActivityHelp.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        if (player.isPlaying()) {
            player.stop();
            play.setTitle(context.getString(R.string.play_sound));
            play.setSummary(context.getString(R.string.play_sound_desc));
        }
        super.onPause();
    }

    @Override
    public void onDestroy(){


        //Close the Text to Speech Library
        if(tts != null) {

            tts.stop();
            tts.shutdown();
            Log.d(TAG, "TTSService Destroyed");
        }

        super.onDestroy();
    }
    @Override
    public void onResume() {

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        super.onResume();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent)
    {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            SharedPreferences.Editor mSettingsEdit = prefs.edit();
            switch(requestCode) {
                case SELECT_RINGTONE :
                    uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (uri != null) {
                        Log.i("Timer","Got ringtone "+uri.toString());
                        mSettingsEdit.putString("SystemUri", uri.toString());
                        lastToneType = "system";
                    }
                    else {
                        mSettingsEdit.putString("SystemUri", "");
                        mSettingsEdit.putString("notification_uri",lastToneType);
                    }
                    break;
                case SELECT_FILE:
                    // Get the Uri of the selected file
                    uri = intent.getData();
                    if (uri != null) {
                        Log.i(TAG, "File Path: " + uri);
                        mSettingsEdit.putString("FileUri", uri.toString());
                        lastToneType = "file";
                    }
                    else {
                        mSettingsEdit.putString("FileUri", "");
                        mSettingsEdit.putString("notification_uri",lastToneType);
                    }
                    break;
            }
            mSettingsEdit.apply();
        }
    }


}
