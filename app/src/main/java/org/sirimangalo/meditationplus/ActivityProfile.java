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
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by noah on 10/16/14.
 */
public class ActivityProfile extends ActionBarActivity {

    private static String TAG = "ActivityProfile";
    private static SharedPreferences prefs;
    private int LOGIN_CODE = 555;

    private String username;
    private String password;
    private String loginToken;

    private String profileName;

    private String lastSubmit;

    private JSONObject jsonFields;
    private TextView titleView;
    private TextView nameView;
    private TextView aboutView;
    private TextView emailView;
    private TextView websiteView;
    private View countryView;
    private CheckBox showEmailView;

    private String oldName;
    private String[] ccodes;
    private String[] cnames;
    private Context context;
    private String uid;
    private boolean canEdit;
    private ImageView flagView;
    private PostTaskRunner postRunner;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        postRunner = new PostTaskRunner(postHandler,this);

        loadingDialog = new ProgressDialog(this);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // pref variables

        username = prefs.getString("username","");
        loginToken = prefs.getString("login_token","");

        cnames = getResources().getStringArray(R.array.country_names);
        ccodes = getResources().getStringArray(R.array.country_codes);

        // intent specific variables

        if(getIntent().hasExtra("edit"))
            setContentView(R.layout.activity_profile_edit);
        else
            setContentView(R.layout.activity_profile);

        canEdit = getIntent().getBooleanExtra("can_edit",false);

        if(getIntent().hasExtra("profile_name"))
            profileName = getIntent().getStringExtra("profile_name");
        else profileName = username;

        oldName = profileName;

        // get profile

        doSubmit("",new ArrayList<NameValuePair>(), profileName);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(getIntent().hasExtra("edit"))
            getMenuInflater().inflate(R.menu.profile_edit, menu);
        else if(canEdit)
            getMenuInflater().inflate(R.menu.profile_can_edit, menu);
        else
            getMenuInflater().inflate(R.menu.profile, menu);
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
            case R.id.action_logout:
                doLogout();
                return true;
            case R.id.action_htm:
                Utils.openHTM(this);
                return true;
            case R.id.action_settings:
                i = new Intent(this,ActivityPrefs.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.action_save:
                doSaveEdit();
                return true;
            case R.id.action_edit:
                if(!canEdit)
                    return true;

                i = new Intent(this,ActivityProfile.class);
                i.putExtra("edit",true);
                i.putExtra("profile_name",profileName);
                i.putExtra("can_edit",canEdit);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case android.R.id.home:
                finish();
            case R.id.action_help:
                i = new Intent(this,ActivityHelp.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;

        }


        return super.onOptionsItemSelected(item);
    }

    private void doSaveEdit() {
        String nameE = nameView.getText().toString();
        String aboutE = aboutView.getText().toString();
        String emailE = emailView.getText().toString();
        String showEmailE = showEmailView.isChecked()?"1":"0";
        String websiteE = websiteView.getText().toString();

        // checks

        int error = 0;

        if(nameE.length() > 20) {
            error = R.string.name_too_long;
            nameView.setBackgroundColor(0xFFFFDDDD);
        }
        if(nameE.length() < 4) {
            error = R.string.name_too_short;
            nameView.setBackgroundColor(0xFFFFDDDD);
        }
        if(aboutE.length() > 255) {
            aboutView.setBackgroundColor(0xFFFFDDDD);
            error = R.string.about_too_long;
        }
        if(emailE.length() > 50) {
            emailView.setBackgroundColor(0xFFFFDDDD);
            error = R.string.email_too_long;
        }
        if(websiteE.length() > 100) {
            websiteView.setBackgroundColor(0xFFFFDDDD);
            error = R.string.website_too_long;
        }

        if(error != 0) {
            Toast.makeText(this,error,Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
        nvp.add(new BasicNameValuePair("name", nameE));
        nvp.add(new BasicNameValuePair("uid", uid));
        nvp.add(new BasicNameValuePair("old_name", oldName));
        nvp.add(new BasicNameValuePair("desc", aboutE));
        nvp.add(new BasicNameValuePair("email", emailE));
        nvp.add(new BasicNameValuePair("show_email", showEmailE));
        nvp.add(new BasicNameValuePair("website", websiteE));

        String country = ccodes[((Spinner)countryView).getSelectedItemPosition()];
        nvp.add(new BasicNameValuePair("country", country));
        doSubmit("profile", nvp, oldName);

    }

    private void showLogin() {
        Intent i = new Intent(this,ActivityLogin.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(i, LOGIN_CODE);
    }

    private void doLogin() {

        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
        nvp.add(new BasicNameValuePair("username", username));
        nvp.add(new BasicNameValuePair("password", password));
        nvp.add(new BasicNameValuePair("login_token", loginToken));
        nvp.add(new BasicNameValuePair("submit", "Login"));

        lastSubmit = "login";

        postRunner.doPostTask(nvp);
        Log.d(TAG, "Executing: login");
    }

    private void doLogout() {

        postRunner = new PostTaskRunner(postHandler, this);

        username = "";
        password = "";

        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("login_token");
        editor.apply();

        showLogin();
    }

    private void doRegister() {

        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
        nvp.add(new BasicNameValuePair("username", username));
        nvp.add(new BasicNameValuePair("password", password));
        nvp.add(new BasicNameValuePair("submit", "Register"));

        lastSubmit = "register";

        postRunner.doPostTask(nvp);
    }

    public void doSubmit(String formId, ArrayList<NameValuePair> nvp, String profile) {

        lastSubmit = formId;

        nvp.add(new BasicNameValuePair("profile", profile));
        nvp.add(new BasicNameValuePair("username", username));
        nvp.add(new BasicNameValuePair("login_token", loginToken));
        nvp.add(new BasicNameValuePair("form_id", formId));
        nvp.add(new BasicNameValuePair("submit", "Profile"));

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

            String result = (String) msg.obj;

            if(msg.what != 1) {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();

                return;
            }

            if(result.equals("success")) {
                if(lastSubmit.equals("profile")) {

                    Intent i = new Intent(context, ActivityProfile.class);
                    i.putExtra("profile_name",profileName);
                    i.putExtra("can_edit",canEdit);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                return;
            }

            try {
                JSONObject json = new JSONObject(result);

                if(json.has("success")) {
                    int success = Integer.parseInt(json.getString("success"));
                    if(success == -1) { // not logged in
                        Log.e(TAG, "not logged in");
                        showLogin();
                        return;
                    }
                    else if(success == 1) {
                        if(lastSubmit.equals("profile")) {
                            Intent i = new Intent(context, ActivityProfile.class);
                            i.putExtra("profile_name",profileName);
                            i.putExtra("can_edit",canEdit);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }
                        return;
                    }
                }
                populateFields(json, json.has("admin") && json.getString("admin").equals("true"));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private void populateFields(JSONObject jsonFields, boolean isAdmin) {
        titleView = (TextView) findViewById(R.id.profile_title);
        flagView = (ImageView) findViewById(R.id.profile_flag);
        nameView = (TextView) findViewById(R.id.name_field);
        aboutView = (TextView) findViewById(R.id.about_field);
        emailView = (TextView) findViewById(R.id.email_field);
        websiteView = (TextView) findViewById(R.id.website_field);

        countryView = findViewById(R.id.country_field);
        showEmailView = (CheckBox) findViewById(R.id.show_email_field);

        try {
            uid = jsonFields.getString("uid");
            oldName = jsonFields.getString("username");

            String desc = jsonFields.has("description")? jsonFields.getString("description"):"";
            String email = jsonFields.has("email")? jsonFields.getString("email"):"";
            String website = jsonFields.has("website")? jsonFields.getString("website"):"";

            titleView.setText(String.format(getString(R.string.s_profile),oldName));
            nameView.setText(oldName);
            aboutView.setText(desc);
            emailView.setText(email);
            websiteView.setText(website);

            String cname = "";
            int idx = 0;

            for(int i = 0; i < ccodes.length; i++) {
                if(jsonFields.has("country") && ccodes[i].equals(jsonFields.getString("country"))){
                    cname = cnames[i];
                    idx = i;
                    break;
                }
            }

            if(getIntent().hasExtra("edit")) {
                showEmailView.setChecked(jsonFields.getInt("show_email") == 1);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.country_names, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                ((Spinner) countryView).setAdapter(adapter);
                ((Spinner) countryView).setSelection(idx);
            }
            else {
                if(jsonFields.has("country")) {
                    int fid = getResources().getIdentifier("flag_"+jsonFields.getString("country").toLowerCase(),"drawable", getPackageName());
                    flagView.setImageResource(fid);
                    flagView.setVisibility(View.VISIBLE);
                }
                ((TextView)countryView).setText(cname);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == LOGIN_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                username = data.getStringExtra("username");
                password = data.getStringExtra("password");
                String method = data.getStringExtra("method");
                if (method.equals("login"))
                    doLogin();
                else if (method.equals("register"))
                    doRegister();
            }
            else {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
