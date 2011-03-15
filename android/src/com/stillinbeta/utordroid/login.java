package com.stillinbeta.utordroid;

import java.io.OutputStream;
import java.io.IOException;
import javax.net.ssl.HttpsURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.content.Context;
import android.view.View;
import android.util.Log;

class Login extends DefaultHandler implements OnClickListener{
    private boolean inForm;
    private boolean formParsed;
    private HashMap<String,String> fields;
    private Context context;

    private final static String LOGIN_URL = "http://login.wireless.utoronto.ca";
    private final static String POST_URL = "https://connect.utoronto.ca/authen/index.php";
    private final static String TAG = "UTorDroid";

    private static String username = "frosteli";
    
    public Login(Context context) {
        this.inForm = false;
        this.formParsed = false;
        this.fields = new HashMap<String,String>();
        this.context = context;
    }

    public void onClick(View v){
        int duration = Toast.LENGTH_LONG;
        
        //Follow the redirect to the login URL
        
        HttpsURLConnection conn;
        String newURL;  
        try {
            conn = new HttpsURLConnection(LOGIN_URL);
            conn.connect();
            newURL = conn.getHeaderField("Location");
            Log.d(TAG,"" + newURL);
        }
        /* IOException or MalformedURLException */
        catch (MalformedURLException e) {
            Log.e(TAG,"Malformed URL!");
            return;
        }
        catch (IOException e) {
   /*         Toast toast = Toast.makeText(context,"retrieval:" + e.toString(),duration);
            toast.show(); */
            Log.e(TAG,"Error in redirect retrieval");
            return;
        }

        // We have the URL, scrape the form data
        try {
            url = new URL(newURL);
        }
        catch (MalformedURLException e) {
           /* Toast toast = Toast.makeText(context,conn.getHeaderFields().toString(),duration);
            toast.show();
            return; */
            Log.e(TAG,conn.getHeaderFields().toString());
        }

        XMLReader xr = new Parser();
        xr.setContentHandler(this);
        xr.setErrorHandler(this);    

        try {
            xr.parse(new InputSource(url.openStream()));
        }
        catch (Exception e) {
            Log.e(TAG,"Error parsing!",e);
        }
        // Process the form data, add our fields.        
        this.addField("username",username);
        this.addField("password",new Scanner(System.in).next());
        String payload = "";
        try {
            for (Map.Entry<String,String> e : this.getFields().entrySet()) {
               payload +=  URLEncoder.encode(e.getKey(),"UTF-8")
                    + "=" + URLEncoder.encode(e.getValue(), "UTF-8") + "&";
            }
        }
        catch (Exception e) {
            Toast toast = Toast.makeText(context,e.toString(),duration);
            toast.show();
            return;
        }
        // Send the response
        try {
            url = new URL();
            conn = new HttpsURLConnection(POST_URL);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            os.write ( payload.getBytes() );
        }
        catch (Exception e) {
            Toast toast = Toast.makeText(context,e.toString(),duration);
            toast.show();
            return;
        }
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
