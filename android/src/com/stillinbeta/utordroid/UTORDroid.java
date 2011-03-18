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
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import java.io.IOException;
import java.net.MalformedURLException;
import org.xml.sax.SAXException;
import com.stillinbeta.utordroid.Login;
import com.stillinbeta.utordroid.Login.LoginException;

public class UTORDroid extends Activity {

    private ProgressDialog dialog;

    private OnClickListener connectListener = new OnClickListener() {
        public void onClick(View v) {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            EditText usernameField = (EditText)findViewById(R.id.username);
            EditText passwordField = (EditText)findViewById(R.id.password);

            String username = usernameField.getText().toString();
            String password = passwordField.getText().toString();

            new LoginTask().execute(username,password);

            dialog = ProgressDialog.show(UTORDroid.this,"",
                getString(R.string.working));
        }
    }; 
    
    private class LoginTask extends AsyncTask<String, Void, Boolean> {

        private LoginException exception;

        protected Boolean doInBackground(String... params) {
            Login login = new Login(params[0], params[1]);
            try {
                return login.login();
            }
            catch (LoginException e) {
                exception = e;
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {
            if (dialog != null) {
                dialog.dismiss();
            }

            // Display error message, or success message and close
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            if (!result) {
                if ( exception != null ) {
                    Toast toast = Toast.makeText(context,exception.getMessage(),duration);
                    toast.show();
                }
                else {
                    Toast toast = Toast.makeText(context,
                        getString(R.string.login_failure), duration);
                    toast.show();
                }
            }
            else {
                 Toast toast = Toast.makeText(context,
                    getString(R.string.login_success),duration); 
                 toast.show();
                 finish(); //Our work here is done
            }
        }
    } 
           
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
            passwordField.setText(""); //Clear the password from the text field
        }

        editor.commit();
    }
}
