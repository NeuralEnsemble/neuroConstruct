/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.utils.xml;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;

import ucl.physiol.neuroconstruct.utils.*;

/**
 * Simple XML Reader. Note this is part of a very limited XML API, just enough to handle
 * the current NeuroML/MorphML specs, and only tested with these. Not for use
 * with general XML files
 *
 * @author Padraig Gleeson
 *  
 */

public class SimpleXMLReader extends XMLFilterImpl implements LexicalHandler
{
    private static ClassLogger logger = new ClassLogger("SimpleXMLReader");

    SimpleXMLDocument docRead = new SimpleXMLDocument();
    SimpleXMLElement currentElement = null;

    //private boolean preserve

    ArrayList<SimpleXMLNamespace> namespaceMappings = new ArrayList<SimpleXMLNamespace>();

    public SimpleXMLDocument getDocRead()
    {
        return docRead;
    }

    public void characters(char[] ch, int start, int length)
    {
        String gotString = new String(ch, start, length);

        logger.logComment("Found characters: ("+ gotString+") for element: "+ currentElement.getName());

        ArrayList<SimpleXMLEntity> existingContents = currentElement.getContents();

        // replace the & with &amp; etc;
        gotString = SimpleXMLElement.convertToXMLFriendly(gotString);


        if (existingContents.size() > 0 &&
            existingContents.get(existingContents.size() - 1) instanceof SimpleXMLContent)
        {
            SimpleXMLContent prev = (SimpleXMLContent) existingContents.get(existingContents.size() - 1);
            prev.appendText(gotString);

            logger.logComment("Adding chars to existing SimpleXMLContent: "+ prev.getText());

        }
        else
        {
            currentElement.addContent(gotString);
        }
    } 

    public void startDocument()
    {
        logger.setThisClassSilent(true);
        docRead = new SimpleXMLDocument();
    }

    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes attributes)
    {
        logger.logComment("-----   Start element: namespaceURI: " + namespaceURI
                           + ", localName: " + localName
                           + ", qName: " + qName);

        SimpleXMLElement newElement = new SimpleXMLElement(qName);

        for (int i = 0; i < this.namespaceMappings.size(); i++)
        {
            newElement.addNamespace(namespaceMappings.get(i));
        }
        this.namespaceMappings.clear();


        int attrsLength = attributes.getLength();
        for (int i = 0; i < attrsLength; i++)
        {
            String name = attributes.getLocalName(i);
            String val = SimpleXMLElement.convertToXMLFriendly(attributes.getValue(i));


            logger.logComment("Attr name: " + name+ ", val: " + val+ ", qname: "
                               + attributes.getQName(i)+ ", uri: " + attributes.getURI(i));

            SimpleXMLAttribute sa = new SimpleXMLAttribute(attributes.getQName(i), val);
            newElement.addAttribute(sa);

           // logger.logComment("Aded attribute: "+ sa.getXMLString("", false));

        }

        if (currentElement == null)
        {
            docRead.addRootElement(newElement);
        }
        else
        {
            currentElement.addChildElement(newElement);

        }
        currentElement = newElement;

    }

    public void endElement(String namespaceURI, String localName, String qName)
    {
        currentElement = currentElement.getParent();

        //logger.logComment("-----   End element: " + localName);
    }


    public void startPrefixMapping (String prefix, String uri)
    {
        //logger.logComment("startPrefixMapping called... prefix: "+ prefix + ", uri: "+ uri);

        this.namespaceMappings.add(new SimpleXMLNamespace(prefix, uri));

    }

    public void endPrefixMapping (String prefix)
    {
        logger.logComment("endPrefixMapping called... prefix: "+ prefix);
    }

    public void skippedEntity (String name)
    {
        logger.logComment("skippedEntity called... name: "+ name);

    }
    public void processingInstruction (String target, String data)
    {
        logger.logComment("processingInstruction called... target: "+target+", data: "+data);
    }

    public void startDTD (String name, String publicId, String systemId)
    {
        logger.logComment("startDTD called, doing nothing...");
    }


    public void endDTD ()
    {
        logger.logComment("endDTD called, doing nothing...");
    }


    public void startEntity(String name)
    {
        logger.logComment("startEntity called, doing nothing...");
    }

    public void endEntity (String name)
    {
        logger.logComment("endEntity called, doing nothing...");
    }


    public void startCDATA()
    {
        logger.logComment("startCDATA called, doing nothing...");
    }

    public void endCDATA ()
    {
        logger.logComment("endCDATA called, doing nothing...");
    }


    public void comment (char ch[], int start, int length)
    {
        logger.logComment("comment called...");
        String comment = new String(ch, start, length);

        if (comment.trim().length() > 0)
        {
            //currentElement.addComment(new SimpleXMLComment(comment.trim()));
            if (currentElement == null)
            {
                docRead.addComment(new SimpleXMLComment(comment));
            }
            else
            {
                currentElement.addComment(new SimpleXMLComment(comment));

            }

        }

    }





    public static SimpleXMLDocument getSimpleXMLDoc(File xmlFile) throws IOException, SAXException, ParserConfigurationException
    {
        logger.logComment("Getting doc for: "+ xmlFile);
        FileInputStream instream = null;
        InputSource is = null;

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        XMLReader xmlReader = spf.newSAXParser().getXMLReader();

        SimpleXMLReader docBuilder = new SimpleXMLReader();
        xmlReader.setContentHandler(docBuilder);

        xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", docBuilder);

        instream = new FileInputStream(xmlFile);

        is = new InputSource(instream);

        xmlReader.parse(is);

        SimpleXMLDocument doc = docBuilder.getDocRead();

        return doc;

    }



    public static void main(String args[])
    {
        System.out.println("Trying out SimpleXMLReader...");
        try
        {

            //File f = new File("../NeuroMLValidator/web/NeuroMLFiles/Examples/ChannelML/CaPool.xml");
            File f = new File("models\\GCLayer\\cellMechanisms\\Generic_CaHVA\\CaHVA_Chan.xml");

            //File f = new File("../NeuroMLValidator/web/NeuroMLFiles/Schemata/v1.1/Level2/ChannelML_v1.1_GENESIStab.xsl");
            //File f = new File("../neuroConstruct/models/MDeSXML/cellProcesses/Generic_H/GenericChannel.xml");
            //File f = new File("C:\\NeuroMLValidator\\web\\NeuroMLFiles\\Examples\\MorphML\\ChansIncluded.xml");
            //File f = new File("");

            SimpleXMLDocument doc = SimpleXMLReader.getSimpleXMLDoc(f);

            System.out.println("Docread: ");
            //String stringForm = doc.getXMLString("", false);
            String stringForm = doc.getXMLString("", false);
            System.out.println(stringForm);
             stringForm = doc.getXMLString("", true);
            System.out.println(stringForm);


/*
            File outF = new File("../temp/GenericChannel2.html");

            FileWriter fw = new FileWriter(outF);

            fw.write(stringForm);

            fw.close();

            ArrayList<String> locs = doc.getXPathLocations(true);

            for (int k = 0; k < locs.size(); k++)
            {
                System.out.println("----   Subelement : " + locs.get(k) + " = " + doc.getValueByXPath(locs.get(k)));
            }

            String xpath1 = "/channelml/notes";

            SimpleXMLEntity[] entities = doc.getXMLEntities(xpath1);

            for (int j = 0; j < entities.length; j++)
            {
                System.out.println("Entity " + j + " at " + xpath1 + ": " + entities[j]);
            }

            System.out.println("Value of "+xpath1+": "+doc.getValueByXPath(xpath1));
*/

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }

}
