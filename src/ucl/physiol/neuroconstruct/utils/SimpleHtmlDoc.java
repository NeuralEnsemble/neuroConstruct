/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2007 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.utils;

import ucl.physiol.neuroconstruct.gui.SimpleViewer;
import java.util.*;
import java.io.*;

/**
 * Class for developing VERY simple HTML files formatted in a standard style,
 * which can also be outputted as plain text.
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class SimpleHtmlDoc
{
    ArrayList<SimpleHtmlElement> contents = new ArrayList<SimpleHtmlElement>();

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




    public void addBreak()
    {
        contents.add(new BreakElement());
    }


    public SimpleHtmlDoc()
    {
    }

    public String toHtmlString()
    {
        StringBuffer message = new StringBuffer("<html>\n<head>\n<style type=\"text/css\">"
            + " p {text-align: left; font-size: 12pt; font-family: monospaced}"
            + "\n</style>\n</head>\n"
            +"<body>");

        for (int i = 0; i < contents.size(); i++)
        {
            message.append(contents.get(i).toHtmlString()+"\n");
        }
        message.append("</body>\n</html>");

        return message.toString();

    }

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
        public String contents = null;
        public ArrayList<String> tabs = new ArrayList<String>();

        public TabbedElement(String contents, String tab)
        {
            this.contents = contents;
            tabs.add(tab);
        }

        public TabbedElement(String contents, ArrayList<String> tabs)
        {
            this.contents = contents;
            this.tabs = tabs;
        }



        public String toHtmlString()
        {
            String tabbedUp = contents;
            for (int i = 0; i < tabs.size(); i++)
            {
                tabbedUp = GeneralUtils.getTabbedString(tabbedUp, tabs.get(i), true);
            }
            return tabbedUp;
        }

        public String toString()
        {
            return contents;
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

    private abstract class SimpleHtmlElement
    {
        public abstract String toHtmlString();
        public abstract String toString();
    }



    public void saveAsFile(File file)
    {
/*
        try
        {
            FileWriter fw = new FileWriter(targetFile);
            fw.write(result);
            fw.close();
        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger, "Exception writing to file: "+ targetFile, ex, null);
            return false;
        }*/

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
