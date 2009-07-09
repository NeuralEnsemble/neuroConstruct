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

package ucl.physiol.neuroconstruct.utils;

import ucl.physiol.neuroconstruct.gui.SimpleViewer;
import java.util.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Class for developing VERY simple HTML files formatted in a standard style,
 * which can also be outputted as plain text.
 *
 * @author Padraig Gleeson
 *  
 */

public class SimpleHtmlDoc
{
    private static ClassLogger logger = new ClassLogger("SimpleHtmlDoc");
    
    ArrayList<SimpleHtmlElement> contents = new ArrayList<SimpleHtmlElement>();

    int mainFontSize = 12;


    public void addTaggedElement(String text, String tab)
    {
        contents.add(new TabbedElement(text, tab));
    }

    public void addTaggedElement(String text, String innerTab, String outerTab)
    {
        ArrayList<String> tabs = new ArrayList<String>();
        tabs.add(innerTab);
        tabs.add(outerTab);

        contents.add(new TabbedElement(text, tabs));
    }

    public void addTaggedElement(String text, ArrayList<String> tabs)
    {
        contents.add(new TabbedElement(text, tabs));
    }

    public void addTaggedElement(SimpleHtmlElement el, String tab)
    {
        contents.add(new TabbedElement(el, tab));
    }


    public SimpleHtmlElement getLinkedText(String text, String link)
    {
        return new LinkedElement(text, link);
    }

    public void addLinkedText(String text, String link)
    {
        contents.add(getLinkedText(text, link));
    }


    public void addRawHtml(String html)
    {
        contents.add(new RawHtml(html));
    }



    public void addBreak()
    {
        contents.add(new BreakElement());
    }


    public SimpleHtmlDoc()
    {
    }

    public void setMainFontSize(int fs)
    {
        mainFontSize = fs;
    }

    public String toHtmlString()
    {
        StringBuffer message = new StringBuffer("<html>\n<head>\n<style type=\"text/css\">"

            + "         h1 {color: gray; font-family: Dialog, Verdana, Helvetica, Arial, sans-serif}"
            + "         h2 {color: gray; font-family: Dialog, Verdana, Helvetica, Arial, sans-serif}"
            + "         h3 {color: gray; font-family: Dialog, Verdana, Helvetica, Arial, sans-serif}"
            + "         p {font-family: Dialog, Verdana, Helvetica, Arial, sans-serif; font-size: "+mainFontSize+"pt}"
            + "         td {font-family: Dialog, Verdana, Helvetica, Arial, sans-serif; font-size: "+mainFontSize+"pt}"
            + "         li {font-family: Dialog, Verdana, Helvetica, Arial, sans-serif}"
            + "         ol {font-family: Dialog, Verdana, Helvetica, Arial, sans-serif}"
            + "         ul {font-family: Dialog, Verdana, Helvetica, Arial, sans-serif}"
            + "         table {border-collapse:collapse}"

            /*+ " p {text-align: left; font-size: 12pt; font-family: monospaced}"*/
            + "\n</style>\n</head>\n"
            +"<body>");

        for (int i = 0; i < contents.size(); i++)
        {
            message.append(contents.get(i).toHtmlString()+"\n");
        }
        message.append("</body>\n</html>");

        return message.toString();

    }

    @Override
    public String toString()
    {
        StringBuffer message = new StringBuffer();

        for (int i = 0; i < contents.size(); i++)
        {
            message.append(contents.get(i).toString()+"\n");
        }

        return message.toString();
    }


    private class TabbedElement extends SimpleHtmlElement
    {
        public ArrayList<SimpleHtmlElement> contents = new ArrayList<SimpleHtmlElement>();
        public ArrayList<String> tabs = new ArrayList<String>();

        public TabbedElement(String contents, String tab)
        {
            this.contents.add(new RawHtml(contents));
            tabs.add(tab);
        }
        public TabbedElement(String contents, ArrayList<String> tabs)
        {
            this.contents.add(new RawHtml(contents));
            this.tabs = tabs;
        }
        public TabbedElement(SimpleHtmlElement contents, String tab)
        {
            this.contents.add(contents);
            tabs.add(tab);
        }
        public TabbedElement(SimpleHtmlElement contents, ArrayList<String> tabs)
        {
            this.contents.add(contents);
            this.tabs = tabs;
        }
        public TabbedElement(ArrayList<SimpleHtmlElement> contents, String tab)
        {
            this.contents = contents;
            tabs.add(tab);
        }
        public TabbedElement(ArrayList<SimpleHtmlElement> contents, ArrayList<String> tabs)
        {
            this.contents = contents;
            this.tabs = tabs;
        }

        public String toHtmlString()
        {
            String tabbedUp = "";

            for(SimpleHtmlElement she: contents)
                tabbedUp = tabbedUp + she.toHtmlString();

            for (int i = 0; i < tabs.size(); i++)
            {
                tabbedUp = GeneralUtils.getTabbedString(tabbedUp, tabs.get(i), true);
            }

            return tabbedUp;
        }

        public String toString()
        {
            String fullString = "";

            for(SimpleHtmlElement she: contents)
                fullString = fullString + she.toHtmlString();

            return fullString;
        }
    }

    public class LinkedElement extends SimpleHtmlElement
    {
        public String text = null;
        public String link = null;

        public LinkedElement(String text, String link)
        {
            this.text = text;
            this.link = link;
        }

        public String toHtmlString()
        {

            return "<a href=\""+link+"\">"+text+"</a>";
        }

        public String toString()
        {
            return text+"("+link+")";
        }
    }

    private class BreakElement extends SimpleHtmlElement
    {
        public String toHtmlString()
        {
            return "<br>";
        };
        public String toString()
        {
            return "\n";
        };
    }

    private class RawHtml extends SimpleHtmlElement
    {
        public String contents = null;

        public RawHtml(String contents)
        {
            this.contents = contents;
        }
        public String toHtmlString()
        {
            return contents;
        };
        public String toString()
        {
        	
            return "???";
        };
    }

    public abstract class SimpleHtmlElement
    {
        public abstract String toHtmlString();
        @Override
        public abstract String toString();
    }



    public void saveAsFile(File file)
    {
        try
        {
        	if (!file.getParentFile().exists())
        	{
        		file.getParentFile().mkdir();
        	}
            FileWriter fw = new FileWriter(file);
            fw.write(this.toHtmlString());
            fw.close();

            logger.logComment("Created doc at: "+ file.getCanonicalPath());
        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger, "Exception writing to file: "+ file, ex, null);
            return;
        }

    }




    public static void main(String[] args)
    {
        SimpleHtmlDoc simplehtmldoc = new SimpleHtmlDoc();

        simplehtmldoc.addTaggedElement("Heading", "h1");
        simplehtmldoc.addTaggedElement("This is me, red", "font color=\"red\"");
        simplehtmldoc.addTaggedElement("This is me, green", "font color=\"green\"");


        SimpleViewer.showString(simplehtmldoc.toString(), "Validity status", 12, true, true);



    }
}
