package org.sirimangalo.meditationplus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by noah on 15/12/14.
 */
public class ActivitySchedule extends Activity {
    private ListView scheduleList;

    private SharedPreferences prefs;
    private Activity context;

    private PostTaskRunner postRunner;
    private TextView timeView;

    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isAdmin = getIntent().getBooleanExtra("admin", false);

        postRunner = new PostTaskRunner(postHandler, this);

        context = this;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_schedule);

        timeView = (TextView) findViewById(R.id.time);

        Button ok = (Button) findViewById(R.id.ok);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        scheduleList = (ListView) findViewById(R.id.schedule_list);
        //scheduleList.setEmptyView(findViewById(R.id.empty));
        getSchedule();
/*
        if (isAdmin) {

            scheduleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                }
            });
        }
        */
    }


    private void getSchedule() {

        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();

        nvp.add(new BasicNameValuePair("form_id", "schedule_form"));
        nvp.add(new BasicNameValuePair("submit", "Schedule"));

        postRunner.doPostTask(nvp);


    }

    Handler postHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            String result = (String) msg.obj;

            if(msg.what != 1) {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();

                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(result);

                JSONArray json = jsonObject.getJSONArray("schedule");

                ArrayList scheduleArray = new ArrayList<JSONArray>();
                for(int i = 0; i < json.length(); i++)
                    scheduleArray.add(json.getJSONObject(i));

                scheduleList.setAdapter(new AdapterSchedule(context, scheduleArray, isAdmin));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    BroadcastReceiver _broadcastReceiver;
    private final SimpleDateFormat _sdfWatchTime = new SimpleDateFormat("HH:mm");

    @Override
    public void onStart()
    {
        super.onStart();

        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        _sdfWatchTime.setTimeZone(TimeZone.getTimeZone("GMT"));

        timeView.setText(String.format(getString(R.string.time_now_x),_sdfWatchTime.format(new Date())));

        _broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent)
            {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    timeView.setText(String.format(getString(R.string.time_now_x),_sdfWatchTime.format(new Date())));
                }
            }
        };

        registerReceiver(_broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (_broadcastReceiver != null)
            unregisterReceiver(_broadcastReceiver);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = new Intent(context, ActivitySchedule.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
