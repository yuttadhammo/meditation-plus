package org.sirimangalo.meditationplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.CheckedInputStream;

/**
 * Created by noah on 10/16/14.
 */
public class ProfileActivity extends ActionBarActivity {

    private static String TAG = "ProfileActivity";
    private static SharedPreferences prefs;
    private int LOGIN_CODE = 555;

    private String username;
    private String password;
    private String loginToken;
    private ConnectivityManager cnnxManager;

    private String profileName;

    private String lastSubmit;

    HttpClient httpclient;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        httpclient = new DefaultHttpClient();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        cnnxManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

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
                i = new Intent(this,PrefsActivity.class);
                startActivity(i);
                return true;
            case R.id.action_save:
                doSaveEdit();
                return true;
            case R.id.action_edit:
                if(!canEdit)
                    return true;

                i = new Intent(this,ProfileActivity.class);
                i.putExtra("edit",true);
                i.putExtra("profile_name",profileName);
                i.putExtra("can_edit",canEdit);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case android.R.id.home:
                finish();

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
        doSubmit("profile", nvp, username);

    }

    private void showLogin() {
        Intent i = new Intent(this,LoginActivity.class);
        startActivityForResult(i, LOGIN_CODE);
    }

    private void doLogin() {

        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
        nvp.add(new BasicNameValuePair("username", username));
        nvp.add(new BasicNameValuePair("password", password));
        nvp.add(new BasicNameValuePair("login_token", loginToken));
        nvp.add(new BasicNameValuePair("submit", "Login"));

        lastSubmit = "login";

        PostTask pt = new PostTask();
        pt.execute(nvp);
        Log.d(TAG, "Executing: login");
    }

    private void doLogout() {

        httpclient = new DefaultHttpClient(); // new session

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

        PostTask pt = new PostTask();
        pt.execute(nvp);
        Log.d(TAG, "Executing: login");
    }

    public void doSubmit(String formId, ArrayList<NameValuePair> nvp, String profile) {

        lastSubmit = formId;

        nvp.add(new BasicNameValuePair("profile", profile));
        nvp.add(new BasicNameValuePair("username", username));
        nvp.add(new BasicNameValuePair("login_token", loginToken));
        nvp.add(new BasicNameValuePair("form_id", formId));
        nvp.add(new BasicNameValuePair("submit", "Profile"));

        PostTask pt = new PostTask();
        pt.execute(nvp);
    }

    private class PostTask extends AsyncTask<ArrayList<NameValuePair>, String, String> {

        int response = R.string.success;
        String error = "";
        String stringNote = "";

        @Override
        protected String doInBackground(ArrayList<NameValuePair>... arg0) {
            String responseString = "";
            for(ArrayList<NameValuePair> nameValuePair : arg0) {
                try {
                    // if we have no data connection, no point in proceeding.
                    NetworkInfo ni = cnnxManager.getActiveNetworkInfo();
                    if (ni == null || !ni.isAvailable() || !ni.isConnected()) {
                        response = R.string.no_internet;
                        return "";
                    } else {

                        // Create a new HttpClient and Post Header
                        HttpPost httppost = new HttpPost("http://meditation.sirimangalo.org/post.php");

                        try {
                            httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));

                            // Execute HTTP Post Request
                            HttpResponse aresponse = httpclient.execute(httppost);
                            StatusLine statusLine = aresponse.getStatusLine();

                            HttpEntity data = aresponse.getEntity();
                            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                aresponse.getEntity().writeTo(out);
                                out.close();
                                responseString = out.toString();
                            } else{
                                //Closes the connection.
                                aresponse.getEntity().getContent().close();
                                throw new IOException(statusLine.getReasonPhrase());
                            }

                        } catch (ClientProtocolException e) {
                            error = e.getMessage();
                        } catch (IOException e) {
                            error = e.getMessage();
                        }
                    }

                    // / grab and log data
                } catch (Exception e) {
                    e.printStackTrace();
                    response = R.string.error;

                    error = e.getMessage();
                    return "";
                }
            }
            return responseString;
        }
        @Override
        protected  void onPreExecute()
        {
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("success")) {
                if(lastSubmit.equals("profile")) {

                    Intent i = new Intent(context, ProfileActivity.class);
                    i.putExtra("profile_name",profileName);
                    i.putExtra("can_edit",canEdit);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                return;
            }
            if(result.equals("") && (lastSubmit.equals("login") || lastSubmit.equals("register"))) {
                Log.e(TAG, "error logging in or registering");
                showLogin();
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
                            Intent i = new Intent(context, ProfileActivity.class);
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
    }

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

            titleView.setText(String.format(getString(R.string.s_profile),jsonFields.getString("username")));
            nameView.setText(jsonFields.getString("username"));
            aboutView.setText(jsonFields.getString("description"));
            emailView.setText(jsonFields.getString("email"));
            websiteView.setText(jsonFields.getString("website"));

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
