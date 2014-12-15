package org.sirimangalo.meditationplus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;

/**
 * Created by noah on 15/12/14.
 */
public class ActivitySchedule extends Activity {
    public class ActivityLogin extends Activity {
        private SharedPreferences prefs;
        private Context context;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            context = this;

            prefs = PreferenceManager.getDefaultSharedPreferences(this);

            setContentView(R.layout.activity_schedule);


        }

    }
}
