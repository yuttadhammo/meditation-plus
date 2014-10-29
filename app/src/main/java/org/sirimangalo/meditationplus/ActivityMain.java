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
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


public class ActivityMain extends ActionBarActivity implements ActionBar.TabListener,View.OnClickListener,View.OnLongClickListener {

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

    private static String TAG = "ActivityMain";
    private static SharedPreferences prefs;
    private int LOGIN_CODE = 555;

    private static String username;
    private String password;
    private String loginToken;

    private ListView chatList;
    private static ListView medList;
    private ListView commitList;
    private TextView onlineList;
    private static LinearLayout smiliesShell;

    private boolean isShowing = false;

    private int listVersion = -1;
    private int chatVersion = -1;
    private int logVersion = -1;
    private int commitVersion = -1;

    private static ActivityMain context;
    private static NumberPicker sittingPicker;
    private static NumberPicker walkingPicker;
    private int refreshCount = 0;

    public static Button medButton;

    private JSONArray jsonChats;
    private static JSONArray jsonList;
    private JSONArray jsonLogged;
    private JSONArray jsonCommit;

    private String lastSubmit = "";
    private static GridView smilies;
    private int fullRefreshPeriod = 60;
    private int refreshPeriod = 10;
    private CountDownTimer ct;
    private boolean restartTimer;
    private int currentPosition;
    private int lastChatTime = -1;
    private boolean newChats = false;
    private boolean firstPage = true;

    public boolean isAdmin = false;
    private PostTaskRunner postTask;

    private int lastWalking;
    private int lastSitting;
    private boolean startMeditating = false;

    private ScheduleClient scheduleClient;
    private PendingIntent walkPendingIntent;
    private PendingIntent sitPendingIntent;
    private AlarmManager mAlarmMgr;
    public NotificationManager mNM;

    private ProgressDialog loadingDialog;

    public static String special = "none";
    private static ArrayList<JSONObject> medArray;
    private InputMethodManager inputManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        context = this;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        postTask = new PostTaskRunner(postHandler, this);

        // Create a new service client and bind our activity to this service
        scheduleClient = new ScheduleClient(this);
        scheduleClient.doBindService();

        mAlarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReceiverAlarm.class);
        walkPendingIntent = PendingIntent.getBroadcast((Context)context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        sitPendingIntent = PendingIntent.getBroadcast((Context)context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNM = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        setContentView(R.layout.activity_main);

        onlineList = (TextView) findViewById(R.id.online);

        // loading dialog
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setTitle(R.string.processing);

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

                // close keyboard

                View view = getCurrentFocus();
                if (view != null) {
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                // reset chat title

                if(position == 1 && newChats) {
                    if(actionBar.getTabAt(1) != null)
                        actionBar.getTabAt(1).setText(getString(R.string.title_section2).toUpperCase(Locale.getDefault()));
                    newChats = false;
                }

                currentPosition = position;
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
        username = prefs.getString("username","");
        loginToken = prefs.getString("login_token","");
        if(loginToken.equals(""))
            showLogin();

    }

    @Override
    protected void onResume() {
        super.onResume();
        isShowing = true;
        listVersion = -1;
        chatVersion = -1;
        restartTimer = true;

        refreshPeriod = Integer.parseInt(prefs.getString("refresh_period","10"))*1000;
        refreshPeriod = Math.max(refreshPeriod, 10);

        fullRefreshPeriod = Integer.parseInt(prefs.getString("full_refresh_period","60"))*1000;
        fullRefreshPeriod = Math.max(fullRefreshPeriod,60);

        if(ct != null)
            ct.cancel();

        ct = new CountDownTimer(fullRefreshPeriod,refreshPeriod) {
            @Override
            public void onTick(long l) {
                ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
                if(l < fullRefreshPeriod - refreshPeriod && isShowing) {
                    doSubmit(null,nvp,false);
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
                    doSubmit(null, nvp, false);
                }
            }
        };
        loginToken = prefs.getString("login_token","");
        if(!loginToken.equals("")) {
            username = prefs.getString("username","");
            password = "";
            doSubmit(null, new ArrayList<NameValuePair>(), true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isShowing = false;
    }

    @Override
    protected void onStop() {
        // When our activity is stopped ensure we also stop the connection to the service
        // this stops us leaking our activity into the system *bad*

        if(scheduleClient != null)
            scheduleClient.doUnbindService();
        super.onStop();
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
            case R.id.action_htm:
                Utils.openHTM(this);
                return true;
            case R.id.action_refresh:
                listVersion = -1;
                chatVersion = -1;
                restartTimer = true;
                ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
                nvp.add(new BasicNameValuePair("full_update", "true"));
                doSubmit(null, nvp, true);
                return true;
            case R.id.action_profile:
                showProfile(username);
                return true;
            case R.id.action_logout:
                doLogout();
                return true;
            case R.id.action_settings:
                i = new Intent(this,ActivityPrefs.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
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

                smiliesShell.setVisibility(View.GONE);
                EditText message = (EditText) findViewById(R.id.chat_text);
                String messageT = message.getText().toString();
                if(messageT.length() == 0) {
                    Toast.makeText(this,R.string.no_message,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(messageT.length() > 140) {
                    Toast.makeText(this,R.string.message_too_log,Toast.LENGTH_SHORT).show();
                    return;
                }
                nvp.add(new BasicNameValuePair("message", messageT));
                doSubmit("chatform", nvp, true);
                break;
            case R.id.med_send:
                int w = walkingPicker.getValue();
                int s = sittingPicker.getValue();

                if(w == 0 && s == 0) {
                    Toast.makeText(this,R.string.no_time,Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("walking",w);
                editor.putInt("sitting",s);
                editor.apply();

                lastWalking = w*5;
                lastSitting = s*5;

                startMeditating = true;

                nvp.add(new BasicNameValuePair("walking", lastWalking+""));
                nvp.add(new BasicNameValuePair("sitting", lastSitting + ""));
                doSubmit("timeform", nvp, true);
                break;
            case R.id.med_cancel:

                mAlarmMgr.cancel(walkPendingIntent);
                mAlarmMgr.cancel(sitPendingIntent);
                mNM.cancelAll();

                doSubmit("cancelform", nvp, true);
                break;
            case R.id.smily_button:
                if(smiliesShell.getVisibility() == View.GONE)
                    smiliesShell.setVisibility(View.VISIBLE);
                else
                    smiliesShell.setVisibility(View.GONE);
                break;
            case R.id.chat_text:
                smiliesShell.setVisibility(View.GONE);
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
                break;
            case R.id.new_commit:
                Intent i = new Intent(this,ActivityCommit.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;
            default:
                smiliesShell.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();
        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
        switch(id) {
            case R.id.med_send:
                nvp.add(new BasicNameValuePair("type", special.equals("love")?"none":"love"));

                doSubmit("change_type",nvp,true);
                return true;
        }
        return false;
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


    private void showLogin() {
        Intent i = new Intent(this,ActivityLogin.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(i, LOGIN_CODE);
    }

    private void doLogin() {

        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
        nvp.add(new BasicNameValuePair("password", password));
        nvp.add(new BasicNameValuePair("login_token", loginToken));
        nvp.add(new BasicNameValuePair("submit", "Login"));

        lastSubmit = "login";

        doPostTask(nvp);

        Log.d(TAG, "Executing: login");
    }

    private void doLogout() {

        postTask = new PostTaskRunner(postHandler, this); // new session

        username = "";
        password = "";

        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("login_token");
        editor.apply();

        showLogin();
    }

    private void doRegister() {

        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
        nvp.add(new BasicNameValuePair("password", password));
        nvp.add(new BasicNameValuePair("submit", "Register"));

        lastSubmit = "register";

        doPostTask(nvp);

        Log.d(TAG, "Executing: login");
    }

    public void doSubmit(String formId, ArrayList<NameValuePair> nvp, boolean loading) {

        nvp.add(new BasicNameValuePair("login_token", loginToken));
        nvp.add(new BasicNameValuePair("list_version", listVersion+""));
        nvp.add(new BasicNameValuePair("chat_version", chatVersion+""));
        nvp.add(new BasicNameValuePair("submit", "Refresh"));

        if(formId == null)
            lastSubmit = "";
        else {
            nvp.add(new BasicNameValuePair("form_id", formId));
            lastSubmit = formId;
        }

        if(listVersion == -1 && chatVersion == -1)
            nvp.add(new BasicNameValuePair("full_update", "true"));

        if(loading)
            showLoading(true);

        doPostTask(nvp);
    }

    public void doPostTask(ArrayList<NameValuePair> nvp) {

        nvp.add(new BasicNameValuePair("username", username));
        postTask.doPostTask(nvp);
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

                    smiliesShell = (LinearLayout) rootView.findViewById(R.id.smilies_shell);

                    smilies = (GridView) rootView.findViewById(R.id.smilies);
                    smilies.setAdapter(new AdapterImage(context));
                    smilies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            String[] s = context.getResources().getStringArray(R.array.smily_tags);
                            chatInput.setText(chatInput.getText()+s[position]);
                            smiliesShell.setVisibility(View.GONE);
                        }
                    });
                    break;
                case 3:
                    rootView = inflater.inflate(R.layout.fragment_commit, container, false);
                    Button newCommit = (Button) rootView.findViewById(R.id.new_commit);
                    newCommit.setOnClickListener(context);

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

                    medButton = (Button) rootView.findViewById(R.id.med_send);
                    medButton.setOnClickListener(context);

                    medButton.setOnLongClickListener(context);

                    Button cancelButton = (Button) rootView.findViewById(R.id.med_cancel);
                    cancelButton.setOnClickListener(context);
                    break;
            }

            return rootView;
        }
    }

    Handler postHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            showLoading(false);

            String result = (String) msg.obj;
            
            if(msg.what != 1) {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();

                if(lastSubmit.equals("login") || lastSubmit.equals("register") && isShowing)
                    showLogin();

                startMeditating = false;
                return;
            }

            if(result.equals("success"))
                return;

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
                        if(isShowing)
                            showLogin();
                        startMeditating = false;
                        return;
                    }
                    else if(success == 1) {
                        if(lastSubmit.equals("chatform"))
                            ((EditText) findViewById(R.id.chat_text)).setText("");
                    }
                }
                if(json.has("admin")) {
                    if(json.get("admin") instanceof String)
                        isAdmin = json.getString("admin").equals("true");
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
                if(json.has("logged")) {
                    populateOnline(json.getJSONArray("logged"));

                }
                if(json.has("login_token")) {
                    if(lastSubmit.equals("register"))
                        Toast.makeText(context,getString(R.string.registered),Toast.LENGTH_SHORT).show();

                    if(!loginToken.equals(json.getString("login_token"))) {
                        loginToken = json.getString("login_token");
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("login_token", loginToken);
                        editor.apply();
                    }

                    if(isShowing && (lastSubmit.equals("register") || lastSubmit.equals("login"))) {
                        doSubmit(null, new ArrayList<NameValuePair>(), false);
                        return;
                    }
                }

            } catch (Exception e) {
                //Log.e(TAG,"ERROR");
                e.printStackTrace();
                startMeditating = false;
            }

            if(isShowing && restartTimer) {
                restartTimer = false;
                ct.start();
            }
        }
    };

    private void populateLog(JSONArray jsonLogged) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int hour = utc.get(Calendar.HOUR_OF_DAY);

        int max_hour = 0;
        int max_height = 100;

        LinearLayout hll = (LinearLayout) findViewById(R.id.time_log);

        if(hll == null)
            return;

        hll.removeAllViews();

        try {
            max_hour = jsonLogged.getInt(0);
            for(int i = 1; i < jsonLogged.length(); i++){
                max_hour = Math.max(max_hour,jsonLogged.getInt(i));
            }
            for(int i = 0; i < jsonLogged.length(); i++){
                int height = (int) Math.ceil(max_height*jsonLogged.getInt(i)/max_hour);
                LinearLayout ll = (LinearLayout) context.getLayoutInflater().inflate(R.layout.list_item_log, null);

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

        int newChatNo = 0;

        int latestChatTime = 0;
        ArrayList<JSONObject> chatArray = new ArrayList<JSONObject>();
        for(int i = 0; i < chats.length(); i++) {
            try {
                JSONObject chat = chats.getJSONObject(i);
                latestChatTime = Integer.parseInt(chat.getString("time"));
                if(latestChatTime > lastChatTime)
                    newChatNo++;

                chatArray.add(chat);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(latestChatTime < lastChatTime)
            newChatNo = -1;

        if(admin)
            chatList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String cid = (String) view.findViewById(R.id.message).getTag();
                    ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
                    doSubmit("delchat_" + cid, nvp, true);
                    return true;
                }
            });

        // save index and top position
        int index = chatList.getFirstVisiblePosition();
        View v = chatList.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();

        AdapterChat adapter = new AdapterChat(this, R.layout.list_item_chat, chatArray);
        chatList.setAdapter(adapter);

        // restore index and position

        if(newChatNo != 0)
            chatList.setSelection(adapter.getCount() - 1);
        else
            chatList.setSelectionFromTop(index, top);


        if(newChatNo > 0) {
            if(currentPosition != 1) {
                newChats = true;
                ActionBar actionBar = getSupportActionBar();
                if (lastChatTime != -1 && actionBar.getTabAt(1) != null)
                    actionBar.getTabAt(1).setText(Html.fromHtml(getString(R.string.title_section2).toUpperCase(Locale.getDefault()) + " (" + newChatNo + ")"));
            }
        }
        lastChatTime = latestChatTime;
    }

    private void populateMeds(JSONArray meds) {
        if((ListView) findViewById(R.id.med_list) == null)
            return;
        medList = (ListView) findViewById(R.id.med_list);

        medArray = new ArrayList<JSONObject>();
        for(int i = 0; i < meds.length(); i++) {
            try {
                JSONObject med = meds.getJSONObject(i);

                if(med.getString("username").equals(username)) {
                    if (startMeditating) {
                        startMeditating = false;

                        if (lastWalking > 0)
                            scheduleClient.setAlarmForNotification(lastWalking, lastWalking, getString(R.string.walking));

                        if (lastSitting > 0)
                            scheduleClient.setAlarmForNotification(lastSitting, lastWalking + lastSitting, getString(R.string.sitting));
                    }

                    if(med.getString("type").equals("love")) {
                        special = "love";
                        Spannable span = new SpannableString("LOVE");
                        span.setSpan(new ForegroundColorSpan(Color.RED), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        span.setSpan(new ForegroundColorSpan(Color.YELLOW), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        span.setSpan(new ForegroundColorSpan(Color.GREEN), 2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        span.setSpan(new ForegroundColorSpan(Color.BLUE), 3, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        medButton.setText(span);
                    }
                    else {
                        special = "none";
                        medButton.setText(R.string.start);
                    }

                }

                medArray.add(med);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        TextView emptyText = (TextView)findViewById(android.R.id.empty);
        medList.setEmptyView(emptyText);

        AdapterMed adapter = new AdapterMed(this, R.layout.list_item_med, medArray);
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

        AdapterCommit adapter = new AdapterCommit(this, R.layout.list_item_med, commitArray, username);
        commitList.setAdapter(adapter);
    }

    private void populateOnline(JSONArray onlines) {

        if(onlines.length() == 0) {
            onlineList.setVisibility(View.GONE);
            return;
        }

        onlineList.setVisibility(View.VISIBLE);

        ArrayList<String> onlineArray = new ArrayList<String>();

        // collect into array

        for(int i = 0; i < onlines.length(); i++) {
            try {
                onlineArray.add(onlines.getString(i).replaceFirst("\\^.*", ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String text = getString(R.string.online)+" ";

        // add spans

        int pos = text.length(); // start after "Online: "

        text += TextUtils.join(", ", onlineArray);
        Spannable span = new SpannableString(text);

        span.setSpan(new StyleSpan(Typeface.BOLD), 0, pos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // bold the "Online: "

        for(int i = 0; i < onlineArray.size(); i++) {
            try {

                final String oneOn = onlineArray.get(i);

                int end = pos+oneOn.length();

                boolean isMed = false;

                for(int j = 0; j < jsonList.length(); j++) {
                    JSONObject user = jsonList.getJSONObject(j);
                    String username = user.getString("username");
                    if(username.equals(oneOn))
                        isMed = true;
                }

                ClickableSpan clickable = new ClickableSpan() {

                    @Override
                    public void onClick(View widget) {
                        showProfile(oneOn);
                    }

                };
                span.setSpan(clickable, pos, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                span.setSpan(new UnderlineSpan() {
                    public void updateDrawState(TextPaint tp) {
                        tp.setUnderlineText(false);
                    }
                }, pos, end, 0);

                span.setSpan(new ForegroundColorSpan(isMed ? 0xFF009900 : 0xFFFF9900), pos, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                pos += oneOn.length() + 2;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        onlineList.setText(span);
        onlineList.setMovementMethod(LinkMovementMethod.getInstance());

    }

    public void showProfile(String profile) {
        Intent i = new Intent(context,ActivityProfile.class);
        i.putExtra("profile_name",profile);
        i.putExtra("can_edit",username.equals(profile) || isAdmin);
        context.startActivity(i);
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
