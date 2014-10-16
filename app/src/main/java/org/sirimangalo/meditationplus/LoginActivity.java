package org.sirimangalo.meditationplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by noah on 11/10/14.
 */
public class LoginActivity extends Activity {
    private SharedPreferences prefs;
    private String error = "";
    private String username = "";
    private String password = "";
    private Context context;
    private EditText user;
    private EditText pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_login);

        user = (EditText) findViewById(R.id.username);
        pass = (EditText) findViewById(R.id.password);

        user.setText(prefs.getString("username",""));
        pass.setText(prefs.getString("password",""));

        Button login = (Button) findViewById(R.id.login);
        Button cancel = (Button) findViewById(R.id.cancel);
        Button register = (Button) findViewById(R.id.register);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValues()) {

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username",username);
                    editor.putString("password",password);
                    editor.apply();

                    Intent i = new Intent();
                    i.putExtra("username",user.getText().toString());
                    i.putExtra("password",pass.getText().toString());
                    i.putExtra("method","login");
                    setResult(Activity.RESULT_OK,i);
                    finish();
                }
                else {
                    Toast.makeText(context,error,Toast.LENGTH_SHORT);
                }
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValues()) {

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username",username);
                    editor.putString("password",password);
                    editor.apply();

                    Intent i = new Intent();
                    i.putExtra("username",user.getText().toString());
                    i.putExtra("password",pass.getText().toString());
                    i.putExtra("method","register");
                    setResult(Activity.RESULT_OK,i);
                    finish();
                }
                else {
                    Toast.makeText(context,error,Toast.LENGTH_SHORT);
                }
            }
        });

    }

    private boolean checkValues() {

        username = user.getText().toString();
        password = pass.getText().toString();

        if(username.matches("[^A-Za-z_ -]") || username.length() < 4 || username.length() > 20) {
            error = "invalid username";
            return false;
        }
        if(password.length() < 4 || password.length() > 20) {
            error = "invalid password";
            return false;
        }

        return true;
    }
}