package org.sirimangalo.meditationplus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by noah on 10/03/15.
 */
public class ActivityScheduleEdit extends Activity {

    private ProgressDialog loadingDialog;
    private PostTaskRunner postRunner;

    private Context context;
    private SharedPreferences prefs;
    private String username;
    private String loginToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        // pref variables
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        username = prefs.getString("username","");
        loginToken = prefs.getString("login_token","");


        postRunner = new PostTaskRunner(postHandler,this);

        loadingDialog = new ProgressDialog(this);


        setContentView(R.layout.list_item_schedule_edit);

        final TextView titleV = (TextView) findViewById(R.id.title);
        final TextView timeV = (TextView) findViewById(R.id.time);
        final TextView descV = (TextView) findViewById(R.id.desc);

        titleV.setText(getIntent().getStringExtra("title"));
        timeV.setText(getIntent().getStringExtra("time"));
        descV.setText(getIntent().getStringExtra("desc"));

        ((Button) findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ((Button) findViewById(R.id.save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleV.getText().toString();
                String desc = descV.getText().toString();
                String time = timeV.getText().toString();
                String sid = getIntent().getStringExtra("sid");
                ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
                nvp.add(new BasicNameValuePair("title", title));
                nvp.add(new BasicNameValuePair("desc", desc));
                nvp.add(new BasicNameValuePair("time", time));
                nvp.add(new BasicNameValuePair("sid", sid));

                doSubmit("profile", nvp);

            }
        });

    }
    public void doSubmit(String formId, ArrayList<NameValuePair> nvp) {

        nvp.add(new BasicNameValuePair("username", username));
        nvp.add(new BasicNameValuePair("login_token", loginToken));
        nvp.add(new BasicNameValuePair("form_id", "schedule_edit"));
        nvp.add(new BasicNameValuePair("submit", "Schedule"));

        showLoading(true);
        postRunner.doPostTask(nvp);
    }

    public void showLoading(boolean show) {
        if(loadingDialog != null && loadingDialog.isShowing())
            loadingDialog.dismiss();

        if(!show)
            return;

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setTitle(R.string.processing);
        loadingDialog.setMessage(getString(R.string.loading_message));
        loadingDialog.show();

    }

    Handler postHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            showLoading(false);

            setResult(Activity.RESULT_OK);
            finish();
        }
    };

}
