import java.net.URL;
import java.net.MalformedURLException;
import org.ccil.cowan.tagsoup.Parser;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.xml.sax.XMLReader;

class Main {
    public static void main(String[] args){
        SAXBuilder builder = new SAXBuilder("org.ccil.cowan.tagsoup.Parser");
        URL url;
        try {
            url = new URL("http://www.stillinbeta.com");
        }
        catch (MalformedURLException e) {
            System.out.println("Malformed URL");
            return;
        }
        Document doc;
        try {
            doc = builder.build(url);
        }
        catch (Exception e) {
            System.out.println("JDOM Exception: "+e.toString());
        }

        XMLReader reader;
        try {
            reader = new Parser();
            reader.setFeature(Parser.namespacesFeature, false);
            reader.setFeature(Parser.namespacePrefixesFeature, false);
        }
        catch (Exception e){
            System.out.println("Something went wrong: "+e.toString());
        }
/*
        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        DOMResult result = new DOMResult();
        transformer.transform(new SAXSource(reader, 
            new InputSource(url.openStream())), result); */
        System.out.println("At least it compiled");  
        
    }
}
