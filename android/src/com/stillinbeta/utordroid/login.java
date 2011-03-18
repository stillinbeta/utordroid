package com.stillinbeta.utordroid;

import java.io.OutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.lang.Exception;
import android.util.Log;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;
import org.apache.http.message.BasicNameValuePair;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import java.lang.StringBuffer;

import android.net.http.AndroidHttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

class Login extends DefaultHandler {
    private HashMap<String,String> fields;
    private String username;
    private String password;

    private final static String LOGIN_URL = "http://login.wireless.utoronto.ca";
    private final static String POST_URL = "https://connect.utoronto.ca/authen/index.php";
    private final static String TAG = "UTorDroid";
    private final static String ENCODING = "UTF-8";
    private final static String USER_AGENT = "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.15) Gecko/20110304 Firefox/3.6.15";

    public class LoginException extends Exception {
        public LoginException(String s) {
            super(s);
        }
    }

    public Login(String username, String password) {
        this.fields = new HashMap<String,String>();
        this.username = username;
        this.password = password;
    }

    public boolean login() throws LoginException {
        
        // Follow the Redirect to get the Actual URL
        String redirect;
        AndroidHttpClient client;
        try {
            client = AndroidHttpClient.newInstance(USER_AGENT);
            HttpUriRequest request = new HttpGet(LOGIN_URL);
            request.setHeader("Cache-control","no-cache");
            HttpResponse response = client.execute(request);
            Log.i(TAG,"Redirect page retrieved");
            redirect = response.getFirstHeader("Location").getValue();
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
           request.setHeader("Cache-control","no-cache");
           entity = client.execute(request).getEntity(); 
        }
        catch (Exception e) {
            Log.e(TAG,"Error retriveing login page",e);
            throw new LoginException("Connection Error");
        }
        Log.d(TAG,"Retrieved login page");
    
        // Parse Login Page 
        XMLReader xr = new Parser();
        SaxParseForm spf = new SaxParseForm();
        xr.setContentHandler(spf);
        xr.setErrorHandler(spf);   

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
        if (fields.get("state") != null && fields.get("state").equals("modify")) {
            Log.e(TAG,"Already logged in");
            throw new LoginException("Already Logged In");
        }

        // Process the form data, add our fields.        
        fields.put("username",username);
        fields.put("password",password);
        
        // Convert the Hashmap to a List of NameValuePairs 
        ArrayList<BasicNameValuePair> nameValPairs = new ArrayList<BasicNameValuePair>();
        try {
            for (Entry<String, String> entry: fields.entrySet()) {
                BasicNameValuePair kvp = new BasicNameValuePair(
                entry.getKey(),entry.getValue());
                nameValPairs.add(kvp);
            }
        }
        catch (Exception e) {
            Log.e(TAG,"Problem encoding",e);
            throw new LoginException("Error Sending Login Information"); 
        } 
        Log.d(TAG,"Prepared response");
        Log.i(TAG,nameValPairs.toString());

       // Send the response
        
        try {
            HttpPost request = new HttpPost(POST_URL);
            request.setEntity(new UrlEncodedFormEntity(nameValPairs));
            entity = client.execute(request).getEntity();

        }
        catch (Exception e) {
            Log.e(TAG,"Problem transmitting login data",e);
            throw new LoginException("Error Sending Login Information");
        }
        SaxParseResponse spr = new SaxParseResponse();
        xr.setContentHandler(spr);
        xr.setErrorHandler(spr);   

        try {
            xr.parse(new InputSource(entity.getContent()));
        }
        catch (Exception e) {
            Log.e(TAG,"Could not parse response page",e);
            throw new LoginException("Could not understand response!");
        }
        Log.d(TAG,"Response parsed.");

        return spr.getIsLoggedIn();
    }
    

    private class SaxParseForm extends DefaultHandler {
        private boolean inForm;
        private boolean formParsed;
        
        public SaxParseForm() {
            this.inForm = false;
            this.formParsed = false;
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

    private class SaxParseResponse extends DefaultHandler {
        private boolean inHeader;
        private boolean isLoggedIn;
        private StringBuffer text;
        
        public SaxParseResponse() {
            this.inHeader = false;
            this.isLoggedIn = false;
            this.text = new StringBuffer();
        }
        public void startElement (String uri, String name,
                          String qName, Attributes atts) {
            if(name.equals("h2")) {
               text.setLength(0); //Clear out the buffer
                this.inHeader = true;
            }
        }
        public void characters (char ch[], int start, int length) {
            if (this.inHeader) {
                text.append(ch, start, length);
            }
        }

        public void endElement (String uri, String name, String qName) {
            if(this.inHeader) { //Check if we're done with the header
                this.inHeader = false;
                if (text.toString().equals("UTORcwn Authentication")) {
                    this.isLoggedIn = true;
                    Log.d(TAG,"We are logged in!");
                }
                else {
                    Log.d(TAG,text.toString());
                }
            }
        }

        public boolean getIsLoggedIn() {
            return this.isLoggedIn;
        }
    }

    // Very useful for debugging
    public static void readInputStream(InputStream stream) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(stream));
            String x = r.readLine();
            while(x != null) {
                Log.d(TAG,x);
                x = r.readLine();
            }
        }
        catch (Exception e) {
           Log.e(TAG,"Error reading stream",e);
        }
    }
}
