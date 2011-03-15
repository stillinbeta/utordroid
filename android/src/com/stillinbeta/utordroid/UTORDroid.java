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
import java.io.IOException;
import java.net.MalformedURLException;
import org.xml.sax.SAXException;
import com.stillinbeta.utordroid.Login;

public class UTORDroid extends Activity
{
/*
    private OnClickListener connectListener = new OnClickListener() {
        public void onClick(View v) {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;

            
            EditText usernameField = (EditText)findViewById(R.id.username);
            EditText passwordField = (EditText)findViewById(R.id.password);

            String username = usernameField.getText().toString();
            String password = passwordField.getText().toString();

            if (username.length() == 0 || password.length() == 0) {
                CharSequence error = "You forgot something!";
                Toast toast = Toast.makeText(context,error,duration);
                toast.show();
                return;
            }

            try {
                    Login.login(usernameField.getText().toString(),
                    passwordField.getText().toString());
            }
            catch (MalformedURLException e) {
                CharSequence error = "Could not connect!"+e.toString();
                Toast toast = Toast.makeText(context,error,duration);
                toast.show();
                return;
            }
            catch (IOException e) {
                CharSequence error = "Error Connecting!"+e.toString();
                Toast toast = Toast.makeText(context,error,duration);
                toast.show();
                return;
            }
            catch (SAXException e) {
                CharSequence error = "Error parsing respone!"+e.toString();
                Toast toast = Toast.makeText(context,error,duration);
                toast.show();
                return;
            }
            CharSequence success = "Connected to UTORWin!";
            Toast toast = Toast.makeText(context,success,duration);
            toast.show();
        }
    };
  */  
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
        connect.setOnClickListener(new Login(getApplicationContext()));

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
