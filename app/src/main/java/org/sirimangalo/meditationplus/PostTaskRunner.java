package org.sirimangalo.meditationplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by noah on 25/10/14.
 */
public class PostTaskRunner {
    private final Handler handler;
    private final Context context;
    private ConnectivityManager cnnxManager;
    HttpClient httpclient;


    public PostTaskRunner(Handler h, Context c) {
        handler = h;
        context = c;
        cnnxManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        httpclient = new DefaultHttpClient();
    }

    public void doPostTask(ArrayList<NameValuePair> nvp) {
        new PostTask().execute(nvp);
    }

    private class PostTask extends AsyncTask<ArrayList<NameValuePair>, String, String> {

        String error = "";

        @Override
        protected String doInBackground(ArrayList<NameValuePair>... arg0) {
            String responseString = "";
            for(ArrayList<NameValuePair> nameValuePair : arg0) {
                try {
                    // if we have no data connection, no point in proceeding.
                    NetworkInfo ni = cnnxManager.getActiveNetworkInfo();
                    if (ni == null || !ni.isAvailable() || !ni.isConnected()) {
                        error = context.getString(R.string.no_internet);
                        return "";
                    } else {

                        // Create a new HttpClient and Post Header
                        HttpPost httppost = new HttpPost("http://meditation.sirimangalo.org/post.php");

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
                    }

                    // / grab and log data
                } catch (Exception e) {
                    //e.printStackTrace();

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
            Message msg = new Message();

            if(error.equals("")) {
                msg.what = 1;
                msg.obj = result;
            }
            else {
                msg.what = 0;
                msg.obj = error;
            }
            handler.dispatchMessage(msg);
        }


    }

}
