package com.stillinbeta.utordroid;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.SharedPreferences;

public class UTORDroid extends Activity
{

    private OnClickListener connectListener = new OnClickListener() {
        public void onClick(View v) {
            Context context = getApplicationContext();
            CharSequence text = "yeah toast!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context,text,duration);
            toast.show();
        }
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button connect = (Button)findViewById(R.id.connect);        
        EditText usernameField = (EditText)findViewById(R.id.username);
        EditText passwordField = (EditText)findViewById(R.id.password);
        CheckBox savePassword = (CheckBox)findViewById(R.id.remember);

        //Setup Event Handlers
        connect.setOnClickListener(connectListener);

        // Restore saved preferences        
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        String username = settings.getString("username","");
        String password = settings.getString("password","");

        usernameField.setText(username);
        // Only set password if we have a recalled password
        if (password.compareTo("") != 0) {
            passwordField.setText(username);
            savePassword.setChecked(true);
        }
        
    }

    public void onPause() {
        super.onPause();

        // If we aren't saving passwords, clear the password on lost focus
        CheckBox savePassword = (CheckBox)findViewById(R.id.remember);
        EditText passwordField = (EditText)findViewById(R.id.password);

        if (!savePassword.isChecked()) {
            passwordField.setText("");
        }

    }

    public void onStop() {
        super.onStop();

        EditText usernameField = (EditText)findViewById(R.id.username);
        EditText passwordField = (EditText)findViewById(R.id.password);
        CheckBox savePassword = (CheckBox)findViewById(R.id.remember);
        
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        
        editor.putString("username",usernameField.getText().toString());
        // only save password if we've been asked to.
        if (savePassword.isChecked()) {
            editor.putString("password",passwordField.getText().toString());
        }
        else {
            //Override an old string if we no longer want a saved password
            editor.putString("password","");
        }

        editor.commit();
    }
}
