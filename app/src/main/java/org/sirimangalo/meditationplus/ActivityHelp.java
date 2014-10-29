package org.sirimangalo.meditationplus;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebView;

/**
 * Created by noah on 29/10/14.
 */
public class ActivityHelp extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        WebView wv;
        wv = (WebView) findViewById(R.id.web_view);
        wv.loadUrl("file:///android_asset/help.html");
    }
}
