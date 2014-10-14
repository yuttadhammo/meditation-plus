package org.sirimangalo.meditationplus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener,View.OnClickListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private static String TAG = "MainActivity";
    private SharedPreferences prefs;
    private int LOGIN_CODE = 555;

    private String username;
    private String password;
    private String loginToken;
    private ConnectivityManager cnnxManager;

    private ListView chatList;
    private ListView medList;
    private TextView onlineList;

    private boolean isShowing = false;

    HttpClient httpclient;
    private static MainActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        httpclient = new DefaultHttpClient();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        cnnxManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        setContentView(R.layout.activity_main);

        onlineList = (TextView) findViewById(R.id.online);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        loginToken = prefs.getString("login_token","");

    }
    @Override
    protected void onResume() {
        super.onResume();
        isShowing = true;
        if(loginToken.equals(""))
            showLogin();
        else {
            username = prefs.getString("username","");
            password = "";
            doRefresh();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isShowing = false;
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

        PostTask pt = new PostTask();
        pt.execute(nvp);
        Log.d(TAG, "Executing: login");
    }

    private void doRefresh() {
        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
        nvp.add(new BasicNameValuePair("username", username));
        nvp.add(new BasicNameValuePair("submit", "Refresh"));

        PostTask pt = new PostTask();
        pt.execute(nvp);
        Log.d(TAG, "Executing: refresh");
    }

    private void doSubmit(String formId, ArrayList<NameValuePair> nvpTemp) {
        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
        nvp.add(new BasicNameValuePair("username", username));
        nvp.add(new BasicNameValuePair("login_token", username));
        nvp.add(new BasicNameValuePair("form_id", formId));

        for(NameValuePair nv : nvpTemp) {
            nvp.add(nv);
        }

        nvp.add(new BasicNameValuePair("submit", "Refresh"));

        PostTask pt = new PostTask();
        pt.execute(nvp);
        Log.d(TAG, "Executing: submit");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
        switch(id) {
            case R.id.chat_send:
                EditText message = (EditText) findViewById(R.id.chat_text);
                nvp.add(new BasicNameValuePair("message", message.getText().toString()));
                doSubmit("chatform", nvp);
                break;
            case R.id.med_send:
                EditText walking = (EditText) findViewById(R.id.walking_input);
                EditText sitting = (EditText) findViewById(R.id.sitting_input);
                nvp.add(new BasicNameValuePair("walking", walking.getText().toString()));
                nvp.add(new BasicNameValuePair("sitting", sitting.getText().toString()));
                doSubmit("timeform", nvp);
                break;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            return fragment;
        }

        public PlaceholderFragment() {
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View rootView;

            int section = getArguments().getInt(ARG_SECTION_NUMBER);

            switch(section) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_main, container, false);
                    Button medButton = (Button) rootView.findViewById(R.id.med_send);
                    medButton.setOnClickListener(context);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_chat, container, false);
                    Button chatButton = (Button) rootView.findViewById(R.id.chat_send);
                    chatButton.setOnClickListener(context);
                    break;
                case 3:
                    rootView = inflater.inflate(R.layout.fragment_profile, container, false);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.fragment_main, container, false);
                    break;
            }

            return rootView;
        }
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
/*
                String which = "";

                for(NameValuePair nvp : nameValuePair){
                    if(nvp.getName().equals("formid")) {
                        which = nvp.getValue();
                        break;
                    }
                }

                if(which.equals("chatform")) {

                }
                else if(which.equals("medform")) {
                }
                else if(which.equals("loginform")) {
                }
                else if(which.equals("registerform")) {
                }
*/
            }
            return responseString;
        }
        @Override
        protected  void onPreExecute()
        {
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("") || result.equals("success"))
                return;

            try {
                //Log.d(TAG,"result:"+result);
                JSONObject json = new JSONObject(result);
                if(json.has("success")) {
                    int success = Integer.parseInt(json.getString("success"));
                    if(success == -1) // not logged in
                        showLogin();
                }
                if(json.has("logged")) {
                    populateOnline(json.getJSONArray("logged"));

                }
                if(json.has("chat")) {
                    populateChat(json.getJSONArray("chat"));
                }
                if(json.has("list")) {
                    populateMeds(json.getJSONArray("list"));
                }
                if(json.has("login_token")) {
                    loginToken = json.getString("login_token");
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("login_token", loginToken);
                    editor.apply();
                    doRefresh();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            CountDownTimer ct = new CountDownTimer(10000,10000) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    if(isShowing)
                        doRefresh();
                }
            };
            ct.start();
        }


    }

    private void populateChat(JSONArray chats) {
        ArrayList<JSONObject> chatArray = new ArrayList<JSONObject>();
        for(int i = 0; i < chats.length(); i++) {
            try {
                JSONObject chat = chats.getJSONObject(i);
                chatArray.add(chat);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        chatList = (ListView) findViewById(R.id.chat_list);
        if(chatList == null)
            return;

        ChatAdapter adapter = new ChatAdapter(this, R.layout.chat_list_item, chatArray);
        chatList.setAdapter(adapter);
        chatList.setSelection(adapter.getCount() - 1);
    }
    private void populateOnline(JSONArray onlines) {
        ArrayList<String> online = new ArrayList<String>();
        for(int i = 0; i < onlines.length(); i++) {
            try {
                String oneOn = onlines.getString(i);
                online.add(oneOn.replaceFirst("\\^.*",""));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        onlineList.setText(TextUtils.join(", ",online));

    }

    private void populateMeds(JSONArray meds) {
        ArrayList<JSONObject> medArray = new ArrayList<JSONObject>();
        for(int i = 0; i < meds.length(); i++) {
            try {
                JSONObject med = meds.getJSONObject(i);
                medArray.add(med);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        medList = (ListView) findViewById(R.id.med_list);
        if(medList == null)
            return;

        MedAdapter adapter = new MedAdapter(this, R.layout.med_list_item, medArray);
        medList.setAdapter(adapter);
    }

    public class ChatAdapter extends ArrayAdapter<JSONObject> {


        private final List<JSONObject> values;

        public ChatAdapter(Context context, int resource, List<JSONObject> items) {
            super(context, resource, items);
            this.values = items;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.chat_list_item, parent, false);

            JSONObject p = values.get(position);
            try {
                int then = Integer.parseInt(p.getString("time"));
                int now = Math.round(new Date().getTime()/1000);

                int ela = now - then;
                int day = 60*60*24;
                ela = ela > day ? day : ela;
                int intColor = 255 - Math.round(ela*255/day);
                intColor = intColor > 100 ? intColor : 100;
                String hexTransparency = Integer.toHexString(intColor);
                hexTransparency = hexTransparency.length() > 1 ? hexTransparency : "0"+hexTransparency;
                String hexColor = "#"+hexTransparency+"000000";
                int transparency = Color.parseColor(hexColor);

                TextView time = (TextView) rowView.findViewById(R.id.time);
                if (time != null) {
                    String ts = null;
                    ts = time2Ago(then);
                    time.setText(ts);
                    time.setTextColor(transparency);
                }
                TextView mess = (TextView) rowView.findViewById(R.id.message);
                if (mess != null) {
                    String text = "<b>"+(p.getString("me").equals("true")?"<font color=\"blue\">":"")+p.getString("user")+(p.getString("me").equals("true")?"</font>":"")+"</b>: "+p.getString("message");
                    Spanned html = Html.fromHtml(text);

                    mess.setText(html);
                    mess.setTextColor(transparency);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return rowView;

        }
    }

    public class MedAdapter extends ArrayAdapter<JSONObject> {


        private final List<JSONObject> values;

        public MedAdapter(Context context, int resource, List<JSONObject> items) {
            super(context, resource, items);
            this.values = items;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.med_list_item, parent, false);

            JSONObject p = values.get(position);

            TextView walk = (TextView) rowView.findViewById(R.id.one_walking);
            TextView sit = (TextView) rowView.findViewById(R.id.one_sitting);
            TextView name = (TextView) rowView.findViewById(R.id.one_med);

            try {
                walk.setText(p.getString("walking"));
                sit.setText(p.getString("sitting"));
                name.setText(p.getString("user"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return rowView;
        }
    }

    private String time2Ago(int then) {
        int now = (int) Math.round(new Date().getTime()/1000);

        int ela = now - then;

        String time = "";
        if (ela < 5)
            time = "now";
        else if(ela < 60)
            time = ela + "s ago";
        else if(ela < 60*60)
            time = (int) Math.floor(ela/60) + "m ago";
        else if(ela < 60*60*24)
            time = (int) Math.floor(ela/60/60) + "h ago";
        else if(ela < 60*60*24*7)
            time = (int) Math.floor(ela/60/60/24) + "d ago";
        else {
            Date date = new Date(then*1000);
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
            time = df.format(new Date(0));
        }
        return time;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == LOGIN_CODE && resultCode == Activity.RESULT_OK) {
            username = data.getStringExtra("username");
            password = data.getStringExtra("password");
            doLogin();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
