package org.sirimangalo.meditationplus;

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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by noah on 21/10/14.
 */
public class CommitActivity extends ActionBarActivity {
    private CommitActivity context;
    private DefaultHttpClient httpclient;
    private SharedPreferences prefs;
    private ConnectivityManager cnnxManager;
    private String lastSubmit;
    private String username;
    private String loginToken;
    private Spinner periodList;
    private Spinner dowList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        httpclient = new DefaultHttpClient();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        cnnxManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // pref variables

        username = prefs.getString("username","");
        loginToken = prefs.getString("login_token","");

        setContentView(R.layout.activity_commit_edit);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // spinners

        periodList = (Spinner) findViewById(R.id.period_list);
        dowList = (Spinner) findViewById(R.id.dow_list);

        ArrayAdapter<CharSequence> periodAdapter = ArrayAdapter.createFromResource(this, R.array.periods, android.R.layout.simple_spinner_item);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodList.setAdapter(periodAdapter);

        ArrayAdapter<CharSequence> dowAdapter = ArrayAdapter.createFromResource(this, R.array.dow, android.R.layout.simple_spinner_item);
        dowAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dowList.setAdapter(dowAdapter);

        if(getIntent().hasExtra("edit")) {
            populateFields();
        }

    }

    private void populateFields() {
    }

    public void doSubmit(String formId, ArrayList<NameValuePair> nvp, String profile) {

        lastSubmit = formId;

        String title = ((TextView) findViewById(R.id.title_field)).getText().toString();
        String desc = ((TextView) findViewById(R.id.desc_field)).getText().toString();
        String[] periodl = getResources().getStringArray(R.array.periods);
        String period = periodl[dowList.getSelectedItemPosition()];

        //String[] dowl = getResources().getStringArray(R.array.dow);
        //String dow = dowl[dowList.getSelectedItemPosition()];
        int dow = dowList.getSelectedItemPosition() - 1;
        String dom = ((TextView) findViewById(R.id.dom_field)).getText().toString();
        String doy = ((TextView) findViewById(R.id.doy_field)).getText().toString();

        String type = ((RadioGroup) findViewById(R.id.type_radios)).getCheckedRadioButtonId() == R.id.type_total ? "total" : "repeat";

        String length;
        if(type.equals("total"))
            length = ((TextView) findViewById(R.id.amount_field)).getText().toString();
        else
            length = ((TextView) findViewById(R.id.walking_field)).getText().toString()+":"+((TextView) findViewById(R.id.sitting_field)).getText().toString();

        boolean specTime = ((CheckBox) findViewById(R.id.spec_time_check)).isChecked();

        String hour = ((TextView) findViewById(R.id.hour_field)).getText().toString();
        String min = ((TextView) findViewById(R.id.min_field)).getText().toString();



        nvp.add(new BasicNameValuePair("title", title));
        nvp.add(new BasicNameValuePair("desc", desc));
        nvp.add(new BasicNameValuePair("period", period));

        if(period.equals("weekly")) {
            nvp.add(new BasicNameValuePair("dow", dow+""));
        }
        else if(period.equals("monthly")) {
            nvp.add(new BasicNameValuePair("dom", dom));
        }
        else if(period.equals("yearly")) {
            nvp.add(new BasicNameValuePair("doy", doy));
        }

        nvp.add(new BasicNameValuePair("length", length));

        String time;
        if(specTime)
            time = hour+":"+min;
        else
            time = "any";

        nvp.add(new BasicNameValuePair("time", time));

        nvp.add(new BasicNameValuePair("username", username));
        nvp.add(new BasicNameValuePair("login_token", loginToken));
        nvp.add(new BasicNameValuePair("form_id", formId));
        nvp.add(new BasicNameValuePair("submit", "Commit"));

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
            finish();
        }
    }

}
