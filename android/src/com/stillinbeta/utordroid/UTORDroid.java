package com.stillinbeta.utordroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.content.SharedPreferences;

public class UTORDroid extends Activity
{

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        String username = settings.getString("username","");

        EditText usernameField = (EditText)findViewById(R.id.username);
        usernameField.setText(username);
        
    }

    public void onStop() {
        super.onStop();

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        EditText usernameField = (EditText)findViewById(R.id.username);
        editor.putString("username",usernameField.getText().toString());
        
        editor.commit();
    }
}
