package org.sirimangalo.meditationplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by noah on 21/10/14.
 */
public class ActivityCommit extends ActionBarActivity {
    private ActivityCommit context;
    private SharedPreferences prefs;
    private String lastSubmit;
    private String username;
    private String loginToken;

    private String cid = "-1";

    private Spinner periodList;
    private Spinner dowList;
    private EditText titleView;
    private EditText descView;
    private EditText domView;
    private EditText doyView;
    private RadioGroup typeRadios;
    private EditText amountView;
    private CheckBox specCheck;
    private LinearLayout dowShell;
    private LinearLayout domShell;
    private LinearLayout doyShell;
    private LinearLayout amountShell;
    private LinearLayout repAmountShell;
    private LinearLayout timeShell;
    private LinearLayout specTimeShell;
    private EditText walkView;
    private EditText sitView;
    private EditText hourView;
    private EditText minView;
    private String TAG = "ActivityCommit";
    private PostTaskRunner postRunner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        postRunner = new PostTaskRunner(postHandler,this);

        // pref variables

        username = prefs.getString("username","");
        loginToken = prefs.getString("login_token","");

        setContentView(R.layout.activity_commit_edit);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // views

        titleView = (EditText) findViewById(R.id.title_field);
        descView = (EditText) findViewById(R.id.desc_field);
        domView = (EditText) findViewById(R.id.dom_field);
        doyView = (EditText) findViewById(R.id.doy_field);
        amountView = (EditText) findViewById(R.id.amount_field);
        walkView = (EditText) findViewById(R.id.walking_field);
        sitView = (EditText) findViewById(R.id.sitting_field);
        hourView = (EditText) findViewById(R.id.hour_field);
        minView = (EditText) findViewById(R.id.min_field);

        specCheck = (CheckBox) findViewById(R.id.spec_time_check);

        typeRadios = (RadioGroup) findViewById(R.id.type_radios);

        // spinners

        periodList = (Spinner) findViewById(R.id.period_list);
        dowList = (Spinner) findViewById(R.id.dow_list);

        ArrayAdapter<CharSequence> periodAdapter = ArrayAdapter.createFromResource(this, R.array.periods, android.R.layout.simple_spinner_item);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodList.setAdapter(periodAdapter);

        ArrayAdapter<CharSequence> dowAdapter = ArrayAdapter.createFromResource(this, R.array.dow, android.R.layout.simple_spinner_item);
        dowAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dowList.setAdapter(dowAdapter);

        // shells

        dowShell = (LinearLayout) findViewById(R.id.dow_shell);
        domShell = (LinearLayout) findViewById(R.id.dom_shell);
        doyShell = (LinearLayout) findViewById(R.id.doy_shell);

        amountShell = (LinearLayout) findViewById(R.id.amount_shell);
        repAmountShell = (LinearLayout) findViewById(R.id.repeat_amount_shell);

        specTimeShell = (LinearLayout) findViewById(R.id.spec_time_shell);
        timeShell = (LinearLayout) findViewById(R.id.time_shell);

        // clicks

        periodList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                showHideShells();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        typeRadios.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                showHideShells();
            }
        });

        specCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                showHideShells();
            }
        });

        // if editing an existing commit

        if(getIntent().hasExtra("commitment")) {
            setTitle(R.string.edit_commit);
            populateFields(getIntent().getStringExtra("commitment"));
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.commit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent i;

        switch(id) {
            case R.id.action_htm:
                Utils.openHTM(this);
                return true;
            case R.id.action_settings:
                i = new Intent(this,ActivityPrefs.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.action_save:
                doSubmit();
                return true;
            case android.R.id.home:
                finish();

        }


        return super.onOptionsItemSelected(item);
    }


    private void showHideShells() {

        dowShell.setVisibility(View.GONE);
        domShell.setVisibility(View.GONE);
        doyShell.setVisibility(View.GONE);

        switch(periodList.getSelectedItemPosition() ) {
            case 0:
                break;
            case 1:
                dowShell.setVisibility(View.VISIBLE);
                break;
            case 2:
                domShell.setVisibility(View.VISIBLE);
                break;
            case 3:
                doyShell.setVisibility(View.VISIBLE);
                break;
        }

        amountShell.setVisibility(View.GONE);
        repAmountShell.setVisibility(View.GONE);
        specTimeShell.setVisibility(View.GONE);
        timeShell.setVisibility(View.GONE);

        if(typeRadios.getCheckedRadioButtonId() == R.id.type_total) {
            amountShell.setVisibility(View.VISIBLE);
        }
        else {
            repAmountShell.setVisibility(View.VISIBLE);
            specTimeShell.setVisibility(View.VISIBLE);
            if(specCheck.isChecked())
                timeShell.setVisibility(View.VISIBLE);
        }



    }

    private void populateFields(String json) {
        try {
            JSONObject p = new JSONObject(json);
            cid = p.getString("cid");

            titleView.setText(p.getString("title"));
            descView.setText(p.getString("description"));

            String period = p.getString("period");
            String day = p.getString("day");
            String length = p.getString("length");
            String time = p.getString("time");

            boolean repeat = false;


            if(!day.equals("-1") || (period.equals("daily") && !time.equals("any")))
                repeat = true;

            if(repeat) {

                ((RadioButton)findViewById(R.id.type_repeat)).setChecked(true);

                if (period.equals("weekly")) {
                    periodList.setSelection(1);
                    dowList.setSelection(Integer.parseInt(day));
                }
                else if (period.equals("monthly")) {
                    periodList.setSelection(2);
                    domView.setText(day);
                }
                else if (period.equals("yearly")) {
                    periodList.setSelection(3);
                    doyView.setText(day);
                }
            }

            if(length.indexOf(":") > 0) {
                String[] lengthA = length.split(":");
                walkView.setText(lengthA[0]);
                sitView.setText(lengthA[1]);
            }
            else
                amountView.setText(length);


            if(!time.equals("any")) { // spec time
                specCheck.setChecked(true);
                timeShell.setVisibility(View.VISIBLE);
                String[] timeA = time.split(":");
                hourView.setText(timeA[0]);
                minView.setText(timeA[1]);

            }


        }
        catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    public void doSubmit() {

        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();

        String formId = "newcommit";

        if(!cid.equals("-1"))
            nvp.add(new BasicNameValuePair("edit-com", cid));

        lastSubmit = formId;

        String title = titleView.getText().toString();
        String desc = descView.getText().toString();
        String[] periodl = getResources().getStringArray(R.array.periods);
        String period = periodl[dowList.getSelectedItemPosition()];

        int dow = dowList.getSelectedItemPosition() - 1;
        String dom = domView.getText().toString();
        String doy = doyView.getText().toString();

        String type = typeRadios.getCheckedRadioButtonId() == R.id.type_total ? "total" : "repeat";

        String length;
        if(type.equals("total"))
            nvp.add(new BasicNameValuePair("length", amountView.getText().toString()));
        else {
            nvp.add(new BasicNameValuePair("walking", walkView.getText().toString()));
            nvp.add(new BasicNameValuePair("sitting", sitView.getText().toString()));
        }

        boolean specTime = specCheck.isChecked();

        String hour = hourView.getText().toString();
        String min = minView.getText().toString();

        nvp.add(new BasicNameValuePair("title", title));
        nvp.add(new BasicNameValuePair("desc", desc));
        nvp.add(new BasicNameValuePair("period", period));
        nvp.add(new BasicNameValuePair("type", type));

        if(period.equals("weekly")) {
            nvp.add(new BasicNameValuePair("dow", dow+""));
        }
        else if(period.equals("monthly")) {
            nvp.add(new BasicNameValuePair("dom", dom));
        }
        else if(period.equals("yearly")) {
            nvp.add(new BasicNameValuePair("doy", doy));
        }

        String time;
        if(specTime) {
            nvp.add(new BasicNameValuePair("spec-time", "true"));
            nvp.add(new BasicNameValuePair("hour", hour));
            nvp.add(new BasicNameValuePair("min", min));
        }

        nvp.add(new BasicNameValuePair("username", username));
        nvp.add(new BasicNameValuePair("login_token", loginToken));
        nvp.add(new BasicNameValuePair("form_id", formId));
        nvp.add(new BasicNameValuePair("submit", "Commit"));

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

            finish();
        }
    };

}
