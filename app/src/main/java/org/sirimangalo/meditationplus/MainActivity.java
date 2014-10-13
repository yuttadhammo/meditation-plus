package org.sirimangalo.meditationplus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {


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

    HttpClient httpclient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        httpclient = new DefaultHttpClient();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        cnnxManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        setContentView(R.layout.activity_main);

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

        if(loginToken.equals(""))
            showLogin();
        else {
            doLogin();
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

        PostTask pt = new PostTask();
        pt.execute(nvp);
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
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_chat, container, false);
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
                            // TODO Auto-generated catch block
                            error = e.getMessage();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
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
                JSONObject json = new JSONObject(result);
                if(json.has("chats")) {
                    populateChat(json.getJSONArray("chats"));
                }
                if(json.has("meds")) {
                    populateMeds(json.getJSONArray("meds"));
                }
                if(json.has("login_token")) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("login_token",json.getString("login_token"));
                    editor.apply();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }

    private void populateChat(JSONArray chats) {
    }

    private void populateMeds(JSONArray meds) {
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
