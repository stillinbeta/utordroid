package com.stillinbeta.utordroid;

import java.io.OutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.lang.Exception;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.net.http.AndroidHttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import java.net.URLEncoder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;

class Login extends DefaultHandler {
    private boolean inForm;
    private boolean formParsed;
    private HashMap<String,String> fields;
    private String username;
    private String password;

    private final static String LOGIN_URL = "http://login.wireless.utoronto.ca";
    private final static String POST_URL = "https://connect.utoronto.ca/authen/index.php";
    private final static String TAG = "UTorDroid";

    
    public Login(String username, String password) {
        this.inForm = false;
        this.formParsed = false;
        this.fields = new HashMap<String,String>();
        this.username = username;
        this.password = password;
    }

    public class LoginException extends Exception {
        public LoginException(String s) {
            super(s);
        }
    }

    public void login() throws LoginException {
        
        // Follow the Redirect to get the Actual URL
        String redirect;
        AndroidHttpClient client;
        try {
            client = AndroidHttpClient.newInstance("Android");
            HttpUriRequest request = new HttpGet(LOGIN_URL);
            redirect = client.execute(request).getFirstHeader("Location").getValue();
        }
        catch (Exception e) {
            Log.e(TAG,"Error retrieving redirect",e);
            throw new LoginException("Connection Error");
        }
        Log.d(TAG, "Redirect URL: "+redirect);
   
        // Retrieve the Actual Login Page 
        HttpEntity entity;
        try {
           HttpUriRequest request = new HttpGet(redirect);
           entity = client.execute(request).getEntity(); 
        }
        catch (Exception e) {
            Log.e(TAG,"Error retriveing login page",e);
            throw new LoginException("Connection Error");
        }
        Log.d(TAG,"Retrieved login page");
    
        // Parse Login Page 
        XMLReader xr = new Parser();
        xr.setContentHandler(this);
        xr.setErrorHandler(this);   

        try {
            xr.parse(new InputSource(entity.getContent()));
        }
        catch (Exception e) {
            Log.e(TAG,"Error parsing!",e);
            throw new LoginException("Could not Parse Login Page");
        }
        Log.d(TAG,"Finished parsing file");
        Log.i(TAG,fields.toString());

        // Check if we're already logged in
        if (fields.get("state").equals("modify")) {
            Log.e(TAG,"Already logged in");
            throw new LoginException("Already Logged In");
        }

        // Process the form data, add our fields.        
        this.addField("username",username);
        this.addField("password",password);
        String payload = "";
        try {
            for (Map.Entry<String,String> e : this.getFields().entrySet()) {
               payload +=  URLEncoder.encode(e.getKey(),"UTF-8")
                    + "=" + URLEncoder.encode(e.getValue(), "UTF-8") + "&";
            }
        }
        catch (Exception e) {
            Log.e(TAG,"Problem encoding",e);
            throw new LoginException("Error Sending Login Information"); 
        } 
        Log.d(TAG,"Prepared response");

    /*    // Send the response
        
        try {
            HttpUriRequest request = new HttpPost(POST_URL);
        }
        catch (Exception e) {
        }  */
    } 

    public void addField(String k, String v) {
        this.fields.put(k,v);
    }

    public HashMap<String,String> getFields() {
        return this.fields;
    }

    public void startElement (String uri, String name,
                      String qName, Attributes atts) {
        if (!this.formParsed && name.compareTo("form") == 0) {
            this.inForm = true;
        }
        if ( this.inForm && name.compareTo("input") == 0 &&
            atts.getValue("type").compareTo("hidden") == 0 ) {
            fields.put(atts.getValue("name"),atts.getValue("value"));
        }
    }

    public void endElement (String uri, String name, String qName) {
        if (name.compareTo("form") == 0) {
            this.inForm = false;
            this.formParsed = true;
        }
    }
}
