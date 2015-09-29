package org.sirimangalo.meditationplus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by noah on 15/12/14.
 */
public class ActivityQuote extends Activity {

    private SharedPreferences prefs;
    private static Activity context;

    private PostTaskRunner postRunner;
    private static WebView quoteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        postRunner = new PostTaskRunner(postHandler, this);

        context = this;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final RelativeLayout rootView = (RelativeLayout)getLayoutInflater().inflate(R.layout.activity_quote, null);

        setContentView(rootView);

        final LinearLayout buttonFrame = (LinearLayout) findViewById(R.id.buttonFrame);
        final LinearLayout buttonFrame2 = (LinearLayout) findViewById(R.id.buttonFrame2);

        final RelativeLayout quoteFrameView = (RelativeLayout) findViewById(R.id.quoteFrame);

        quoteView = (WebView) findViewById(R.id.quote);

        quoteView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if (quoteView.getMeasuredHeight() == 0)
                    return;

                Log.d("test",quoteView.getMeasuredHeight() + " "+ quoteFrameView.getMeasuredHeight() + " " + buttonFrame2.getMeasuredHeight());

                if (quoteView.getMeasuredHeight() >= quoteFrameView.getMeasuredHeight() - buttonFrame2.getMeasuredHeight()) {
                    quoteFrameView.setBackgroundColor(0xFFFFFFFF);
                    buttonFrame2.setVisibility(View.VISIBLE);
                } else
                    buttonFrame.setVisibility(View.VISIBLE);
            }
        });


        Button ok = (Button) findViewById(R.id.ok);
        Button ok2 = (Button) findViewById(R.id.ok2);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ok2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getQuote();
    }


    private void getQuote() {

        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();

        nvp.add(new BasicNameValuePair("form_id", "quote_form"));
        nvp.add(new BasicNameValuePair("submit", "Quote"));

        postRunner.doPostTask(nvp);


    }

    static Handler postHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            String result = (String) msg.obj;

            if(msg.what != 1) {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();

                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(result);

                String quoteStr = jsonObject.getString("quote");
                String citeStr = jsonObject.getString("cite");

                outputQuote(quoteStr,citeStr);

            } catch (Exception e) {
                e.printStackTrace();
                context.finish();
            }

        }
    };

    private static void outputQuote(String quoteStr, String citeStr) {

        quoteView.loadData("<html>" +
                " <head></head>" +
                " <body>" +
                "<div style=\"font-size:20px;font-weight:bold;margin-bottom:8px\">" +
                "Quote of the Day" +
                "</div>" +
                "<div style=\"text-align:justify;font-size:15px;\">" +
                quoteStr +
                "</div>" +
                "<div style=\"font-weight:bold;font-style:italic;font-size:12px;text-align:right;\">-- " +
                citeStr +
                "</div>" +
                " </body>" +
                "</html>", "text/html", "utf-8");

        quoteView.requestLayout();

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }

}
