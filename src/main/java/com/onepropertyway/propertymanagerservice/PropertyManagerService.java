package com.onepropertyway.propertymanagerservice;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.util.Currency;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RestController
@CrossOrigin(origins = "*")
public class PropertyManagerService {
	
	private static final DocumentBuilderFactory dbFac;
    private static final DocumentBuilder docBuilder;
    static
    {
        try
        {
            dbFac = DocumentBuilderFactory.newInstance();
            docBuilder = dbFac.newDocumentBuilder();
        }
        catch(ParserConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    }
    private static final String DEEP_URL = "http://www.zillow.com/webservice/GetDeepSearchResults.htm";
    private static final String ZESTIMATE_URL = "http://www.zillow.com/webservice/GetZestimate.htm";
    
    //private static final String PROPERTY_DETAILS_URL = "http://www.zillow.com/webservice/GetSearchResults.htm";

    private static final String PROPERTY_DETAILS_URL = "http://www.zillow.com/webservice/GetUpdatedPropertyDetails.htm";

    private static final String ZWSID = "X1-ZWz1gd9zcxjv2j_7yjzz";

    private static final NumberFormat nf = NumberFormat.getCurrencyInstance();

    // Returns Zestimate value for address.
    @RequestMapping("/getPropertyDetails")
    public static String getPropertyDetails(@RequestParam String address, @RequestParam String cityStateZip) throws SAXException, IOException, TransformerException
    {
        Document deepDoc = docBuilder.parse(DEEP_URL + 
                                        "?zws-id=" + ZWSID + 
                                        "&address=" + address + 
                                        "&citystatezip=" + cityStateZip);
        printDocument(deepDoc, System.out);
        Element firstResult = (Element)deepDoc.getElementsByTagName("result").item(0);
       
        String zpid = firstResult.getElementsByTagName("zpid").item(0).getTextContent();
        
        Document valueDoc = docBuilder.parse(ZESTIMATE_URL + 
                                             "?zws-id=" + ZWSID + 
                                             "&zpid=" + zpid);
        printDocument(valueDoc, System.out);
        Element zestimate = (Element)valueDoc.getElementsByTagName("zestimate").item(0);
        Element amount = (Element)zestimate.getElementsByTagName("amount").item(0);
        String currency = amount.getAttribute("currency");
        nf.setCurrency(Currency.getInstance(currency));
        
        Document propertyDoc = docBuilder.parse(PROPERTY_DETAILS_URL + 
                "?zws-id=" + ZWSID + 
                "&zpid=" + zpid);
        //printDocument(propertyDoc, System.out);
        System.out.println(propertyDoc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(new DOMSource(propertyDoc), result);
        System.out.println("XML IN String format is: \n" + writer.toString());
        JSONObject xmlJSONObj = null;
        String jsonPrettyPrintString = null;
        try { 
            xmlJSONObj = XML.toJSONObject(writer.toString());
            jsonPrettyPrintString = xmlJSONObj.toString(4);
            System.out.println(jsonPrettyPrintString);
        } catch (JSONException je) {
            System.out.println(je.toString());
        } 
        return jsonPrettyPrintString;
    }
    
    public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
     
        transformer.transform(new DOMSource(doc), 
             new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    } 
    
    public static void printDocument(Element doc, OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
     
        transformer.transform(new DOMSource(doc), 
             new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    } 
    
    public static void main(String[] args) throws Throwable
    {
        String address = "1122 Ayers Plantation Way";
        String cityStateZip = "Mount PleasantSC29466";
        System.out.println(getPropertyDetails(address, cityStateZip));
    }

}
