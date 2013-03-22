/**
 *  neuroConstruct
 *  Software for developing large scale 3D networks of biologically realistic neurons
 * 
 *  Copyright (c) 2009 Padraig Gleeson
 *  UCL Department of Neuroscience, Physiology and Pharmacology
 *
 *  Development of this software was made possible with funding from the
 *  Medical Research Council and the Wellcome Trust
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package ucl.physiol.neuroconstruct.utils.xml;

import java.util.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Simple XML API. Note this is a very limited XML API, just enough to handle
 * the current NeuroML/MorphML specs, and only tested with these. Not for use
 * with general XML files
 *
 * @author Padraig Gleeson
 *  
 */


public class SimpleXMLElement extends SimpleXMLEntity
{
    public SimpleXMLElement()
    {
        try
        {
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    ClassLogger logger = new ClassLogger("SimpleXMLElement");

    ArrayList<SimpleXMLAttribute> attributes = new ArrayList<SimpleXMLAttribute> ();
    ArrayList<SimpleXMLEntity> contents = new ArrayList<SimpleXMLEntity> ();
    ArrayList<SimpleXMLNamespace> namespaces = new ArrayList<SimpleXMLNamespace> ();

    String name = null;

    String nsPrefix = null;

    SimpleXMLElement parent = null;

    //String content = null;

    String currIndent = "    ";

    public SimpleXMLElement(String name)
    {
        this.logger.setThisClassSilent(true);

        mainFormattingColour = "#000088"; // dark blue

        if (name.indexOf(":") >= 0)
        {
            this.name = name.substring(name.indexOf(":") + 1);
            this.nsPrefix = name.substring(0, name.indexOf(":"));
        }
        else this.name = name;
    }

    public SimpleXMLElement(String name, String simpleContents)
    {
        this(name);
        addContent(simpleContents);
    }


    public void addAttribute(SimpleXMLAttribute attr)
    {
        clearCache();
        attributes.add(attr);
    }

    public void addAttribute(String name, String value)
    {
        clearCache();
        attributes.add(new SimpleXMLAttribute(name, value));
    }

    public void addNamespace(SimpleXMLNamespace ns)
    {
        clearCache();
        namespaces.add(ns);
    }


    public void addChildElement(SimpleXMLElement childElement)
    {
        clearCache();
        childElement.setParent(this);
        contents.add(childElement);
    }

    public void addComment(SimpleXMLComment comment)
    {
        clearCache();
        //comment.setParent(this);
        contents.add(comment);
    }

    public void addComment(String comment)
    {
        clearCache();
        //comment.setParent(this);
        contents.add(new SimpleXMLComment(comment));
    }



    public SimpleXMLElement getParent()
    {
        return parent;
    };

    public ArrayList<SimpleXMLAttribute> getAttributes()
    {
        return this.attributes;
    };

    public String getAttributeValue(String attrName)
    {
        String value = null;

        for (int i = 0; i < attributes.size(); i++)
        {
            if (attributes.get(i).getName().equals(attrName))
            {
                return attributes.get(i).getValue();
            }
        }

        return value;
    }
    
    public void setAttributeValue(String attrName, String attrVal)
    {

        for (int i = 0; i < attributes.size(); i++)
        {
            if (attributes.get(i).getName().equals(attrName))
            {
                attributes.get(i).setValue(attrVal);
            }
        }

    }

    public boolean hasAttributeValue(String attrName)
    {
        boolean has = false;

        for (int i = 0; i < attributes.size(); i++)
        {
            if (attributes.get(i).getName().equals(attrName))
            {
                has=true;
            }
        }

        return has;
    }

    public ArrayList<SimpleXMLEntity> getContents()
    {
        return this.contents;
    };

    public void removeAllContents()
    {
        clearCache();
        contents = new ArrayList<SimpleXMLEntity> ();
    }



    public ArrayList<SimpleXMLNamespace> getNamespaces()
    {
        return this.namespaces;
    };


    /**
     * Needed for converting < to &lt; etc...
     * By no means exhaustive.
     *
     */
    public static String convertToXMLFriendly(String content)
    {
        String formattedContent = null;
        if (content!=null) formattedContent = new String(content);
        else formattedContent = "";

        /** @todo Check if there is a built in version of this function...! */
        formattedContent = GeneralUtils.replaceAllTokens(formattedContent, "&", "&amp;");
        formattedContent = GeneralUtils.replaceAllTokens(formattedContent, "<", "&lt;");
        formattedContent = GeneralUtils.replaceAllTokens(formattedContent, ">", "&gt;");

        return formattedContent;
    }

    /**
     * Needed for converting &lt; to &amp;lt; etc...
     * By no means exhaustive.
     *
     */
    public static String convertXMLToHTMLFriendly(String content)
    {
        String formattedContent = null;
        if (content!=null) formattedContent = new String(content);
        else formattedContent = "";

        /** @todo Check if there is a built in version of this function...! */
        formattedContent = GeneralUtils.replaceAllTokens(formattedContent, "&", "&amp;");

        return formattedContent;
    }



    public void setName(String name)
    {
        this.name = name;
    };

    public String getName()
    {
        return name;
    };


    public String getNsPrefix()
    {
        return nsPrefix;
    };


    public void setNsPrefix(String nsPrefix)
    {
        clearCache();
        this.nsPrefix = nsPrefix;
    };




    public void setParent(SimpleXMLElement parent)
    {
        this.parent = parent;
    };

    public void addContent(String text)
    {
        clearCache();
        SimpleXMLContent content = new SimpleXMLContent(text);
        this.contents.add(content);
    };


    /**
     * Gets a value in the tree from an expression in a *very limited* form of XPath
     */
    protected String getValueByXPath(String simpleXPathExp)
    {
        //System.out.println("Getting it");
        logger.logComment(".. Getting the value of ("+simpleXPathExp+") in "+name);

        SimpleXMLEntity[] entities = getXMLEntities(simpleXPathExp);

        if (entities == null)
        {
            logger.logComment("Null entities");
            return null;
        }
        if (entities.length == 0)
        {
            logger.logComment("No entities");
            return null;
        }


        logger.logComment("Found "+entities.length+" entities: "+ entities);


        if (entities[0] instanceof SimpleXMLContent)
        {
            logger.logComment("First entity is SimpleXMLContent: " + entities[0]);

            return ( (SimpleXMLContent) entities[0]).getText();
        }

        if (entities[0] instanceof SimpleXMLAttribute)
        {
            logger.logComment("First entity is SimpleXMLAttribute: " + ((SimpleXMLAttribute)entities[0]).getXMLString("", false));

            String val = ( (SimpleXMLAttribute) entities[0]).getValue();

            logger.logComment("Returning val: " + val);
            return val;
        }

        if (entities[0] instanceof SimpleXMLElement)
        {
            logger.logComment("First entity is SimpleXMLElement: " + entities[0]);
            SimpleXMLElement el = (SimpleXMLElement) entities[0];
            StringBuffer value = new StringBuffer();

            for (int i = 0; i < el.getContents().size(); i++)
            {
                if (el.getContents().get(i) instanceof SimpleXMLContent)
                    value.append( ( (SimpleXMLContent) el.getContents().get(i)).getText());
            }
            return value.toString();
        }

        return null;
    }

    /**
     * Sets a value in the tree from an expression in a limited form of XPath
     */
    protected boolean setValueByXPath(String simpleXPathExp, String value)
    {
        clearCache();
        logger.logComment("Setting value of ("+simpleXPathExp+") in "+name +" to: "+value);

        SimpleXMLEntity[] entities = getXMLEntities(simpleXPathExp);

        logger.logComment("Found entity: "+ entities);

        if (entities.length>1) return false;

        if (entities[0] instanceof SimpleXMLContent)
        {
            ((SimpleXMLContent)entities[0]).setText(value);
            return true;
        }

        if (entities[0] instanceof SimpleXMLAttribute)
        {
            ((SimpleXMLAttribute)entities[0]).setValue(value);
            return true;
        }
        return false;
    }

    // To enable caching of XPath query results
    private Hashtable<String,SimpleXMLEntity[]> lastXPathResults = new Hashtable<String,SimpleXMLEntity[]>();
    private long lastXPathReqTime = -1;

    private void clearCache()
    {
        lastXPathResults = new Hashtable<String,SimpleXMLEntity[]>();
        lastXPathReqTime = -1;
    }

    /**
     * Gets a SimpleXMLEntity in the tree from an expression in a limited form of XPath
     */
    public SimpleXMLEntity[] getXMLEntities(String simpleXPathExp)
    {
        boolean verbose = false;
        boolean performCaching = true;

        String attrInfo = "";
        if (verbose)
        {
            for(SimpleXMLAttribute a: attributes)
            {
                attrInfo = " "+ attrInfo+ a.getXMLString("", false);
            }
        }

        long currTime = System.currentTimeMillis();

        if (performCaching &&
            lastXPathResults.size()>0 &&
            (currTime-lastXPathReqTime<200) &&
            lastXPathResults.keySet().contains(simpleXPathExp))
        {
            logger.logComment("-- -   Returning cached val for "+simpleXPathExp +" in entity: <"+ this.toString()+" "+attrInfo+">", verbose);
            return lastXPathResults.get(simpleXPathExp);
        }
        lastXPathReqTime = System.currentTimeMillis();

        SimpleXMLEntity[] emptyArray = new SimpleXMLEntity[]{};
        logger.logComment("-- -   Getting "+simpleXPathExp +" in entity: <"+ this.toString()+" "+attrInfo+">", verbose);

        if (simpleXPathExp.startsWith("/") && !simpleXPathExp.startsWith("//"))
        {
            logger.logError("Looking for something on the root element. Don't allow that here!!");

            lastXPathResults.put(simpleXPathExp, emptyArray);
            return emptyArray;
        }

        boolean anyLocation = false;

        if (simpleXPathExp.startsWith("//"))
        {
            anyLocation = true;
            simpleXPathExp = simpleXPathExp.substring(2);
        }
        logger.logComment("simpleXPathExp: "+ simpleXPathExp);

        String firstElement = null;
        String subElement = null;
        int indexFirstSeparator = simpleXPathExp.indexOf("/");

        if (indexFirstSeparator>0)
        {
            firstElement = simpleXPathExp.substring(0,indexFirstSeparator);
            subElement = simpleXPathExp.substring(indexFirstSeparator+1);
        }
        else
        {
            logger.logComment("No separator found...");
            if (simpleXPathExp.indexOf("@")==0)
            {
                String attributeName = simpleXPathExp.substring(1);
                logger.logComment("Found request for attribute: "+ attributeName);
                for (int i = 0; i < attributes.size(); i++)
                {
                    if(attributes.get(i).getName().equals(attributeName))
                    {
                        logger.logComment("Found matching: "+ attributes.get(i));
                        SimpleXMLEntity[] ents = new SimpleXMLEntity[]{attributes.get(i)};
                        logger.logComment("ents: "+ents);
                        lastXPathResults.put(simpleXPathExp, ents);
                        return ents;
                    }
                }
                if (!anyLocation)
                {
                    lastXPathResults.put(simpleXPathExp, emptyArray);
                    return emptyArray;
                }
            }
            firstElement = simpleXPathExp;
            subElement = "";
        }

        Hashtable<String, Integer> countEachElementName = new Hashtable<String, Integer>();
        ArrayList<SimpleXMLEntity> allFoundEntities = new ArrayList<SimpleXMLEntity>();

        for (int i = 0; i < contents.size(); i++)
        {
            logger.logComment("Checking "+contents.get(i) + " against firstElement: ("+firstElement
                +"), subElement: ("+subElement+"), anyLocation: "+anyLocation);


            if (firstElement.equals(".") && subElement.equals("")
                && contents.get(i) instanceof SimpleXMLContent &&
                ((SimpleXMLContent)contents.get(i)).getText().trim().length()>0)
            {
                SimpleXMLEntity[] ents = new SimpleXMLEntity[]{contents.get(i)};

                lastXPathResults.put(simpleXPathExp, ents);
                return ents;
            }
            if (contents.get(i) instanceof SimpleXMLElement)
            {

                SimpleXMLElement xe = (SimpleXMLElement)contents.get(i);
                logger.logComment("Simple element: "+xe);

                Integer numSoFarThisName = countEachElementName.get(xe.getName());
                if (numSoFarThisName == null) numSoFarThisName = new Integer(0);
                countEachElementName.put(xe.getName(), numSoFarThisName.intValue()+1);

                String namePart = firstElement;
                int index = -1;
                if (firstElement.indexOf("[")>0)
                {
                    namePart = firstElement.substring(0, firstElement.indexOf("["));
                    String indexPart = firstElement.substring(firstElement.indexOf("[")+1, firstElement.indexOf("]"));
                    index = Integer.parseInt(indexPart);
                }

                if (xe.getName().equals(namePart))
                {
                    logger.logComment("Found element named: "+ firstElement);
                    if (index<0 ||     /* i.e. first or only element element*/
                        countEachElementName.get(xe.getName()).intValue() == index) /* match number*/
                    {
                        if (subElement.trim().length() == 0)
                        {
                            logger.logComment("---  Zero sub element, it's the one we're after...");
                            
                            allFoundEntities.add(xe);
                        }
                        else
                        {
                            /** @todo Repeat this recursively... */
                            SimpleXMLEntity[] partialRes = xe.getXMLEntities(subElement);

                            if (partialRes!=null)
                            {
                                logger.logComment("Repeating over "+partialRes.length+" sub els");
                                for (int p = 0; p < partialRes.length; p++)
                                {
                                    allFoundEntities.add(partialRes[p]);
                                }
                            }
                        }
                    }
                }


                if (anyLocation)
                {
                    logger.logComment("Checking in the sub element for the path");
                    SimpleXMLEntity[] subElementResults = xe.getXMLEntities("//" + simpleXPathExp);
                    if (subElementResults!=null && subElementResults.length>0)
                    {
                        lastXPathResults.put(simpleXPathExp, subElementResults);
                        return subElementResults;
                    }
                }
            }
        }

        if (allFoundEntities.size()==0)
        {
            lastXPathResults.put(simpleXPathExp, emptyArray);
            return emptyArray;
        }

        SimpleXMLEntity[] results = new SimpleXMLEntity[allFoundEntities.size()];
        for (int i = 0; i < allFoundEntities.size(); i++)
        {
            results[i] = allFoundEntities.get(i);
        }
        
        lastXPathResults.put(simpleXPathExp, results);
        return results;

    }

    /**
     * Gets locations of 'interesting' items in the form of simplified XPath expressions
     * @param ignoreEmptyContent If true, ignore SimpleXMLContent entries whith only whitespaces,
     * some of which may be left in for clarity when regenerating the parsed XML file
     */
    public ArrayList<String> getXPathLocations(boolean ignoreEmptyContent)
    {
        ArrayList<String> interestingXPathLocations = new ArrayList<String>();

        for (int i = 0; i < attributes.size(); i++)
        {
            interestingXPathLocations.add("@"+ attributes.get(i).getName());
        }
        Hashtable<String, Integer> totalEachElementName = new Hashtable<String, Integer>();

        for (int k = 0; k < contents.size(); k++)
        {
            if (contents.get(k) instanceof SimpleXMLElement)
            {
                String elementName = ((SimpleXMLElement)contents.get(k)).getName();
                Integer numSoFar = totalEachElementName.get(elementName);
                if (numSoFar == null) numSoFar = new Integer(0);

                totalEachElementName.put(elementName, new Integer(numSoFar.intValue()+1));
            }
        }

        Hashtable<String, Integer> countEachElementName = new Hashtable<String, Integer>();


        //if (contents.size()==1 && contents.get(0) instanceof SimpleXMLContent)
        //{
       //
       // }
       // else
      //  {
            for (int k = 0; k < contents.size(); k++)
            {
                //System.out.println("... Checking out: ("+ contents.get(k) + ") in "+this.getName());
                if (contents.get(k) instanceof SimpleXMLContent)
                {
                    if (! (ignoreEmptyContent && ( (SimpleXMLContent) contents.get(k)).getText().trim().length() == 0))
                    {
                        //System.out.println("Interesting...");
                        //interestingXPathLocations.add("."/*+((SimpleXMLContent)contents.get(k)).getText()*/);

                        interestingXPathLocations.add(".");
                    }
                    else
                    {
                        //System.out.println("Not interesting...");
                    }
                }
                if (contents.get(k) instanceof SimpleXMLElement)
                {
                    SimpleXMLElement childElement = (SimpleXMLElement) contents.get(k);
                    //System.out.println("... Checking out element: "+ childElement);

                    ArrayList<String> interestingChildXPathLocs = childElement.getXPathLocations(ignoreEmptyContent);

                    if (interestingChildXPathLocs.size()==1 && interestingChildXPathLocs.get(0).equals("."))
                    {
                        interestingXPathLocations.add(childElement.getName());
                    }
                    else
                    {

                        Integer numWithThisName = totalEachElementName.get(childElement.getName());

                        String prefix = null;
                        if (numWithThisName.intValue() == 1)
                        {
                            prefix = childElement.getName() + "/";
                        }
                        else
                        {
                            Integer numSoFarThisName = countEachElementName.get(childElement.getName());
                            if (numSoFarThisName == null) numSoFarThisName = new Integer(0);

                            countEachElementName.put(childElement.getName(),
                                                     new Integer(numSoFarThisName.intValue() + 1));

                            prefix = childElement.getName() + "[" +
                                countEachElementName.get(childElement.getName()) + "]/";

                        }

                        for (int j = 0; j < interestingChildXPathLocs.size(); j++)
                        {
                            interestingXPathLocations.add(prefix + interestingChildXPathLocs.get(j));
                        }
                    }
                }

            }
       // }
        //System.out.println("countEachElementName: "+ countEachElementName);

        return interestingXPathLocations;
    }



    public String toString()
    {
        return "Element: "+this.getName();
    }


    public String getXMLString(String indent, boolean formatted)
    {

        String endOfLine  = "\n";
        if (formatted) endOfLine  = "<br>\n";

        StringBuffer fullXMLString = new StringBuffer();

        logger.logComment("Getting XML string for: "+ this);

        String realIndent = indent;
        if (parent == null) realIndent = "";

        String qualifiedName = name;
        if (this.nsPrefix!=null) qualifiedName = nsPrefix + ":" + name;

        if (!formatted) fullXMLString.append(realIndent+"<"+qualifiedName);
        else
        {
            realIndent = GeneralUtils.replaceAllTokens(realIndent, " ", "&nbsp;");
            fullXMLString.append(""+realIndent + "&lt;<span style=\"color:"+mainFormattingColour+";font-weight: bold\">" + qualifiedName+"</span>");
        }

        for (int i = 0; i < namespaces.size(); i++)
        {
            fullXMLString.append(" "+ namespaces.get(i).getXMLString("", formatted));
        }

        for (int i = 0; i < attributes.size(); i++)
        {
            fullXMLString.append(" "+ attributes.get(i).getXMLString("", formatted));
        }

        //String endOfLine  = "\n";
        //if (formatted) endOfLine  = "<br>\n";

        if (contents.size()==0)
        {
            if (!formatted) fullXMLString.append("/>");
            else  fullXMLString.append("/&gt;");
        }
        else
        {

            if (!formatted) fullXMLString.append(">");
            else  fullXMLString.append("&gt;");

            for (int i = 0; i < contents.size(); i++)
            {
                if (contents.get(i) instanceof SimpleXMLElement)
                {
                    if (i==0)
                    {
                        fullXMLString.append(""+endOfLine);
                    }

                    if (i>0 && (contents.get(i-1) instanceof SimpleXMLContent))
                    {
                        fullXMLString.append(contents.get(i).getXMLString("", formatted));
                    }
                    else
                    {
                        fullXMLString.append(contents.get(i).getXMLString(realIndent
                            + currIndent, formatted));

                        if (((SimpleXMLElement)contents.get(i)).getContents().size()>0)
                        {
                            fullXMLString.append(""+endOfLine);
                        }
                        else
                        {
                            fullXMLString.append(""/*+endOfLine*/);
                        }
                    }

                }
                else if (contents.get(i) instanceof SimpleXMLComment)
                {
                    //fullXMLString.append("comm:");
                    //////if (i==0) fullXMLString.append(endOfLine);
                    //////fullXMLString.append(contents.get(i).getXMLString(realIndent + currIndent, formatted)+endOfLine);
                    fullXMLString.append(contents.get(i).getXMLString(realIndent + currIndent, formatted));
                }

                else
                {
                    fullXMLString.append(contents.get(i).getXMLString("", formatted));
                }
            }

            if ((contents.get(contents.size()-1) instanceof SimpleXMLElement))
                fullXMLString.append(realIndent);

            if (!formatted)
            {
                fullXMLString.append("</" + qualifiedName + ">");
            }
            else
            {
                fullXMLString.append("&lt;<span style=\"color:"+mainFormattingColour+";font-weight: bold\">/" + qualifiedName + "</span>&gt;");
            }
        }

        logger.logComment("XML string for: "+ this+": ("+fullXMLString.toString()+")");
        return fullXMLString.toString();
    }


    public static void main(String[] args)
    {
        try
        {

            /*  All  oved to simpxmldoc


            SimpleXMLElement sxe = new SimpleXMLElement("tester");

            sxe.addComment(new SimpleXMLComment("This is an element from the mml namespace..."));

            //sxe.addAttribute(new SimpleXMLAttribute("attr1","1234"));

            //sxe.addNamespace(new SimpleXMLNamespace("", "http://morphml.org/neuroml/schema"));



            SimpleXMLElement sxe2 = new SimpleXMLElement("kid");

            //sxe2.setNsPrefix("mml");

            //SimpleXMLAttribute a2 = new SimpleXMLAttribute("attr1","1234");
            //a2.setNsPrefix("mml");

            //sxe2.addAttribute(a2);


            //sxe.addNamespace(new SimpleXMLNamespace("mml", "http://morphml.org/morphml/schema"));


            sxe2.addContent("aaaaaaaaaa");

            sxe2.addChildElement(new SimpleXMLElement("inner"));

            sxe2.addContent("bbbbbbb\n");

            //SimpleXMLElement sxe3 = new SimpleXMLElement("text");

            //sxe3.addContent("   \n   ");

            //sxe.addChildElement(sxe2);
            //sxe.addChildElement(sxe3);



            SimpleXMLElement sxe4 = new SimpleXMLElement("four");
            SimpleXMLElement sxe5 = new SimpleXMLElement("five");


            SimpleXMLElement mm = new SimpleXMLElement("four");
            SimpleXMLElement ff = new SimpleXMLElement("five");
            ff.addContent("ff");


            sxe5.addContent("555");

            SimpleXMLElement sxe6 = new SimpleXMLElement("five");
            sxe6.addContent("666");



            sxe.addChildElement(sxe4);
            sxe.addChildElement(mm);
            mm.addChildElement(ff);
            sxe4.addChildElement(sxe5);
            sxe4.addChildElement(sxe6);



            System.out.println("To string plain:");
            System.out.println(sxe.getXMLString("", false));

            //System.out.println("To string:");
            //System.out.println(sxe.getXMLString("", true));

            SimpleXMLEntity[] gates = sxe.getXMLEntities("four/five[2]");

            for (int gateNum = 0; gateNum < gates.length; gateNum++)
            {
                System.out.println("Looking at: " + gates[gateNum].getXMLString("",false));
            }



            File tempFile = new File("../temp/spacer.xml");

            FileWriter fw = new FileWriter(tempFile);

            fw.write(sxe.getXMLString("", false));


            fw.close();

            SimpleXMLDocument doc = SimpleXMLReader.getSimpleXMLDoc(tempFile);

            System.out.println("Docread plain: ");
            System.out.println(doc.getXMLString("", false));



            System.out.println("Docread: ");
            System.out.println(doc.getXMLString("", true));
*/



        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    private void jbInit() throws Exception
    {
    }
}
