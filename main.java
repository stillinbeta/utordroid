import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import java.net.URL;
import java.util.HashMap;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.net.MalformedURLException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

class Main extends DefaultHandler{
    private boolean inForm;
    private boolean formParsed;
    private HashMap<String,String> fields;

    public Main() {
        this.inForm = false;
        this.formParsed = false;
        this.fields = new HashMap<String,String>();
    }

    public static void main(String[] args){
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
            System.out.println(handler.fields);
        }
        catch (Exception e) {
            System.out.println("Something went wrong"+e.toString());
        }

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
