import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.dom.DOMResult;
import java.net.URL;
import java.net.MalformedURLException;

class Main {
    public static void main(String[] args){
        URL url;
        try {
            url = new URL("http://www.stillinbeta.com");
        }
        catch (MalformedURLException e) {
            System.out.println("Malformed URL");
            return;
        }
        XMLReader reader;
        Transformer transformer;
        DOMResult result;
        try {
            reader = new Parser();
            reader.setFeature(Parser.namespacesFeature, false);
            reader.setFeature(Parser.namespacePrefixesFeature, false);
            transformer = TransformerFactory.newInstance().newTransformer();
            result = new DOMResult();
            transformer.transform(new SAXSource(reader, 
                        new InputSource(url.openStream())), result);
        }
        catch (Exception e) {
            System.out.println("Something went wrong");
            return;  
        }
    }
}
