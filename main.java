import java.io.OutputStream;
import java.net.HttpURLConnection;
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

class Main extends DefaultHandler{
    private boolean inForm;
    private boolean formParsed;
    private HashMap<String,String> fields;

    private static String username = "USERNAME HERE";
    
    public Main() {
        this.inForm = false;
        this.formParsed = false;
        this.fields = new HashMap<String,String>();
    }

    public static void main(String[] args){

        //Follow the redirect to the login URL
        URL url;
        try {
            url = new URL("http://login.wireless.utoronto.ca");
        }
        catch (MalformedURLException e) {
            System.out.println("Malformed URL");
            return;
        }
        String newURL;  
        try {
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.connect();
            newURL = conn.getHeaderField("Location");
        }
        catch (Exception e) {
            System.out.println("Couldn't Retrieve Redirect URL");
            return; 
        }
        
        // We have the URL, scrape the form data
        try {
            url = new URL(newURL);
        }
        catch (MalformedURLException e) {
            System.out.println("Malformed URL");
            return;
        }

        Main handler = new Main();
        XMLReader xr = new Parser();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);    

        try {
            xr.parse(new InputSource(url.openStream()));
        }
        catch (Exception e) {
            System.out.println("Something went wrong"+e.toString());
        }
        // Process the form data, add our fields.        
        handler.addField("username",username);
        handler.addField("password",new Scanner(System.in).next());
        String payload = "";
        try {
            for (Map.Entry<String,String> e : handler.getFields().entrySet()) {
               payload +=  URLEncoder.encode(e.getKey(),"UTF-8")
                    + "=" + URLEncoder.encode(e.getValue(), "UTF-8") + "&";
            }
        }
        catch (Exception e) {
            System.out.println("Failed to encode Response!");
        }

        // Send the response
        try {
            url = new URL("https://connect.utoronto.ca/authen/index.php");
        }
        catch (MalformedURLException e) {
            System.out.println("Malformed URL");
            return;
        }
        
        try {
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            os.write ( payload.getBytes() );

            System.out.println(conn.getResponseCode());
        }
        catch (Exception e) {
            System.out.println("Something went wrong..."+e.toString());
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
