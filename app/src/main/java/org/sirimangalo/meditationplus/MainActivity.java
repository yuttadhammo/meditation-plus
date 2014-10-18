/*
    This file is part of MeditationPlus.

    MeditationPlus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MeditationPlus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MeditationPlus.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.sirimangalo.meditationplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


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
    private static SharedPreferences prefs;
    private int LOGIN_CODE = 555;

    private String username;
    private String password;
    private String loginToken;
    private ConnectivityManager cnnxManager;

    private ListView chatList;
    private ListView medList;
    private ListView commitList;
    private TextView onlineList;

    private boolean isShowing = false;

    private int listVersion = -1;
    private int chatVersion = -1;
    private int logVersion = -1;
    private int commitVersion = -1;

    HttpClient httpclient;
    private static MainActivity context;
    private static NumberPicker sittingPicker;
    private static NumberPicker walkingPicker;
    private int refreshCount = 0;

    private JSONArray jsonChats;
    private JSONArray jsonList;
    private JSONArray jsonLogged;
    private JSONArray jsonCommit;

    private String lastSubmit = "";
    private static GridView smilies;
    private int fullRefreshPeriod = 60;
    private int refreshPeriod = 10;
    private CountDownTimer ct;
    private boolean restartTimer;

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
        mViewPager.setOffscreenPageLimit(2);

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
        if(loginToken.equals(""))
            showLogin();
        else {
            username = prefs.getString("username","");
            password = "";
            doRefresh(new ArrayList<NameValuePair>());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        isShowing = true;
        listVersion = -1;
        chatVersion = -1;
        restartTimer = true;

        refreshPeriod = Integer.parseInt(prefs.getString("refresh_period","10"))*1000;
        fullRefreshPeriod = Integer.parseInt(prefs.getString("full_refresh_period","60"))*1000;

        if(ct != null)
            ct.cancel();

        ct = new CountDownTimer(fullRefreshPeriod,refreshPeriod) {
            @Override
            public void onTick(long l) {
                ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
                if(isShowing) {
                    doRefresh(nvp);
                }
            }

            @Override
            public void onFinish() {
                if(isShowing) {
                    listVersion = -1;
                    chatVersion = -1;
                    restartTimer = true;
                    ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
                    nvp.add(new BasicNameValuePair("full_update", "true"));
                    doRefresh(nvp);
                }
            }
        };
        doRefresh(new ArrayList<NameValuePair>());
    }

    @Override
    protected void onPause() {
        super.onPause();
        isShowing = false;
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
        Intent i;

        switch(id) {
            case R.id.action_profile:
                i = new Intent(this,ProfileActivity.class);
                i.putExtra("username",username);
                startActivity(i);
                return true;
            case R.id.action_logout:
                doLogout();
                return true;
            case R.id.action_settings:
                i = new Intent(this,PrefsActivity.class);
                startActivity(i);
                return true;
        }


        return super.onOptionsItemSelected(item);
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

    private int singleClick = 0;

    @Override
    public void onClick(View view) {
        int id = view.getId();
        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
        switch(id) {
            case R.id.chat_send:
                EditText message = (EditText) findViewById(R.id.chat_text);
                String messageT = message.getText().toString();
                if(messageT.length() > 140) {
                    Toast.makeText(this,R.string.message_too_log,Toast.LENGTH_SHORT).show();
                    return;
                }
                nvp.add(new BasicNameValuePair("message", messageT));
                doSubmit("chatform", nvp);
                break;
            case R.id.med_send:
                int w = walkingPicker.getValue();
                int s = sittingPicker.getValue();

                if(w == 0 && s == 0)
                    return;

                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("walking",w);
                editor.putInt("sitting",s);
                editor.apply();

                nvp.add(new BasicNameValuePair("walking", Integer.toString(w * 5)));
                nvp.add(new BasicNameValuePair("sitting", Integer.toString(s * 5)));
                doSubmit("timeform", nvp);
                break;
            case R.id.med_cancel:
                doSubmit("cancelform", nvp);
                break;
            case R.id.smily_button:
                if(smilies.getVisibility() == View.GONE)
                    smilies.setVisibility(View.VISIBLE);
                else
                    smilies.setVisibility(View.GONE);
                break;
            case R.id.chat_text:
                singleClick++;
                Handler handler = new Handler();
                Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        singleClick = 0;
                    }
                };

                if (singleClick == 1) {
                    //Single click
                    handler.postDelayed(r, 250);
                } else if (singleClick == 2) {
                    //Double click
                    singleClick = 0;
                    ((EditText) findViewById(R.id.chat_text)).setText("");
                }
            default:
                smilies.setVisibility(View.GONE);
        }
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

    public void doRefresh(ArrayList<NameValuePair> nvp) {

        nvp.add(new BasicNameValuePair("list_version", listVersion+""));
        nvp.add(new BasicNameValuePair("chat_version", chatVersion+""));
        nvp.add(new BasicNameValuePair("username", username));
        nvp.add(new BasicNameValuePair("login_token", loginToken));
        nvp.add(new BasicNameValuePair("submit", "Refresh"));

        if(listVersion == -1 && chatVersion == -1)
            nvp.add(new BasicNameValuePair("full_update", "true"));

        PostTask pt = new PostTask();
        pt.execute(nvp);
    }

    public void doSubmit(String formId, ArrayList<NameValuePair> nvp) {

        lastSubmit = formId;

        nvp.add(new BasicNameValuePair("form_id", formId));

        doRefresh(nvp);
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
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_chat, container, false);
                    Button chatButton = (Button) rootView.findViewById(R.id.chat_send);
                    chatButton.setOnClickListener(context);

                    Button smilyButton = (Button) rootView.findViewById(R.id.smily_button);
                    smilyButton.setOnClickListener(context);

                    final TextView chatInput = (TextView) rootView.findViewById(R.id.chat_text);

                    chatInput.setOnClickListener(context);

                    smilies = (GridView) rootView.findViewById(R.id.smilies);
                    smilies.setAdapter(new ImageAdapter(context));
                    smilies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            String[] s = context.getResources().getStringArray(R.array.smily_tags);
                            chatInput.setText(chatInput.getText()+s[position]);
                            smilies.setVisibility(View.GONE);
                        }
                    });
                    break;
                case 3:
                    rootView = inflater.inflate(R.layout.fragment_commit, container, false);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.fragment_main, container, false);
                    walkingPicker = (NumberPicker) rootView.findViewById(R.id.walking_input);
                    sittingPicker = (NumberPicker) rootView.findViewById(R.id.sitting_input);

                    walkingPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                    sittingPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

                    String[] numbers = getResources().getStringArray(R.array.numbers);

                    walkingPicker.setMaxValue(numbers.length-1);
                    sittingPicker.setMaxValue(numbers.length - 1);
                    walkingPicker.setDisplayedValues(numbers);
                    sittingPicker.setDisplayedValues(numbers);

                    walkingPicker.setValue(prefs.getInt("walking",0));
                    sittingPicker.setValue(prefs.getInt("sitting",0));

                    Button medButton = (Button) rootView.findViewById(R.id.med_send);
                    medButton.setOnClickListener(context);

                    Button cancelButton = (Button) rootView.findViewById(R.id.med_cancel);
                    cancelButton.setOnClickListener(context);
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
            }
            return responseString;
        }
        @Override
        protected  void onPreExecute()
        {
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("success"))
                return;
            if(result.equals("") && (lastSubmit.equals("login") || lastSubmit.equals("register"))) {
                Log.e(TAG, "error logging in or registering");
                showLogin();
                return;
            }


            try {
                //Log.d(TAG,"result:"+result);
                JSONObject json = new JSONObject(result);

                if(json.has("error") && json.getString("error").length() > 0)
                    Toast.makeText(context,json.getString("error"),Toast.LENGTH_SHORT).show();

                if(json.has("list_version"))
                    listVersion = json.getInt("list_version");
                if(json.has("chat_version"))
                    chatVersion = json.getInt("chat_version");


                if(json.has("success")) {
                    int success = Integer.parseInt(json.getString("success"));
                    if(success == -1) { // not logged in
                        Log.e(TAG, "not logged in");
                        showLogin();
                        return;
                    }
                    else if(success == 1) {
                        if(lastSubmit.equals("chatform"))
                            ((EditText) findViewById(R.id.chat_text)).setText("");
                    }
                }
                if(json.has("chat")) {
                    if(json.get("chat") instanceof JSONArray)
                        jsonChats = json.getJSONArray("chat");
                    if(jsonChats == null)
                        chatVersion = -1;
                    else
                        populateChat(jsonChats, json.has("admin") && json.getString("admin").equals("true"));
                }
                if(json.has("list")) {
                    if(json.get("list") instanceof JSONArray)
                        jsonList = json.getJSONArray("list");
                    if(jsonList == null)
                        listVersion = -1;
                    else
                        populateMeds(jsonList);
                }
                if(json.has("hours")) {
                    if(json.get("hours") instanceof JSONArray)
                        jsonLogged = json.getJSONArray("hours");
                    if(jsonLogged == null)
                        logVersion = -1;
                    else
                        populateLog(jsonLogged);
                }
                if(json.has("commit")) {
                    if(json.get("commit") instanceof JSONObject) {
                        jsonCommit = json.getJSONObject("commit").getJSONArray("commitments");

                        if (jsonCommit == null)
                            commitVersion = -1;
                        else
                            populateCommit(jsonCommit);
                    }
                }
                if(json.has("login_token")) {
                    if(lastSubmit.equals("register"))
                        Toast.makeText(context,getString(R.string.registered),Toast.LENGTH_SHORT);

                    loginToken = json.getString("login_token");
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("login_token", loginToken);
                    editor.apply();
                    doRefresh(new ArrayList<NameValuePair>());
                }
                if(json.has("logged")) {
                    populateOnline(json.getJSONArray("logged"));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(restartTimer) {
                restartTimer = false;
                ct.start();
            }
        }


    }

    private void populateLog(JSONArray jsonLogged) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int hour = utc.get(Calendar.HOUR_OF_DAY);

        int max_hour = 0;
        int max_height = 100;

        LinearLayout hll = (LinearLayout) findViewById(R.id.time_log);
        hll.removeAllViews();

        try {
            max_hour = jsonLogged.getInt(0);
            for(int i = 1; i < jsonLogged.length(); i++){
                max_hour = Math.max(max_hour,jsonLogged.getInt(i));
            }
            for(int i = 0; i < jsonLogged.length(); i++){
                int height = (int) Math.ceil(max_height*jsonLogged.getInt(i)/max_hour);
                LinearLayout ll = (LinearLayout) context.getLayoutInflater().inflate(R.layout.log_cell, null);

                ImageView iv = (ImageView) ll.findViewById(R.id.min_cell);
                iv.getLayoutParams().height = height;
                iv.getLayoutParams().width = hll.getWidth()/24;
                TextView tv = (TextView) ll.findViewById(R.id.hour_no);
                tv.setText(i+"");
                if(hour == i)
                    tv.setBackgroundColor(0xFFFFFF33);
                ImageView sv = (ImageView) ll.findViewById(R.id.space_cell);
                sv.getLayoutParams().height = 100-height;
                hll.addView(ll);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void populateChat(JSONArray chats, boolean admin) {
        if((ListView) findViewById(R.id.chat_list) == null)
            return;
        chatList = (ListView) findViewById(R.id.chat_list);

        ArrayList<JSONObject> chatArray = new ArrayList<JSONObject>();
        for(int i = 0; i < chats.length(); i++) {
            try {
                JSONObject chat = chats.getJSONObject(i);
                chatArray.add(chat);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ChatAdapter adapter = new ChatAdapter(this, R.layout.chat_list_item, chatArray);
        chatList.setAdapter(adapter);
        chatList.setSelection(adapter.getCount() - 1);
        if(admin)
            chatList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    TextView cid = (TextView) view.findViewById(R.id.cid);
                    Log.d(TAG,"long click: "+cid.getText());
                    ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
                    Log.d(TAG,cid.getText()+"");
                    doSubmit("delchat_" + cid.getText(), nvp);
                    return true;
                }
            });

    }

    private void populateMeds(JSONArray meds) {
        if((ListView) findViewById(R.id.med_list) == null)
            return;
        medList = (ListView) findViewById(R.id.med_list);

        ArrayList<JSONObject> medArray = new ArrayList<JSONObject>();
        for(int i = 0; i < meds.length(); i++) {
            try {
                JSONObject med = meds.getJSONObject(i);
                medArray.add(med);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        TextView emptyText = (TextView)findViewById(android.R.id.empty);
        medList.setEmptyView(emptyText);

        MedAdapter adapter = new MedAdapter(this, R.layout.med_list_item, medArray);
        medList.setAdapter(adapter);
    }

    private void populateCommit(JSONArray commitJ) {
        if((ListView) findViewById(R.id.commit_list) == null)
            return;
        commitList = (ListView) findViewById(R.id.commit_list);

        ArrayList<JSONObject> commitArray = new ArrayList<JSONObject>();
        for(int i = 0; i < commitJ.length(); i++) {
            try {
                JSONObject med = commitJ.getJSONObject(i);
                commitArray.add(med);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        CommitAdapter adapter = new CommitAdapter(this, R.layout.med_list_item, commitArray, username);
        commitList.setAdapter(adapter);
    }

    private void populateOnline(JSONArray onlines) {

        if(onlines.length() == 0) {
            onlineList.setVisibility(View.GONE);
            return;
        }

        onlineList.setVisibility(View.VISIBLE);


        String onlineText = "<b>"+getString(R.string.online)+"</b>";

        ArrayList<String> online = new ArrayList<String>();
        for(int i = 0; i < onlines.length(); i++) {
            try {
                String oneOn = onlines.getString(i).replaceFirst("\\^.*", "");

                boolean isMed = false;

                for(int j = 0; j < jsonList.length(); j++) {
                    JSONObject user = jsonList.getJSONObject(j);
                    String username = user.getString("username");
                    if(username.equals(oneOn))
                        isMed = true;
                }
                online.add("<font color=\"" + (isMed ? "#009900" : "#FF9900") + "\">" + oneOn + "</font>");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        onlineText += " "+TextUtils.join(", ",online);

        onlineList.setText(Html.fromHtml(onlineText));

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
