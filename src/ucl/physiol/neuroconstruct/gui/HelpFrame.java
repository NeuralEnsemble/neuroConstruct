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

package ucl.physiol.neuroconstruct.gui;

import java.io.*;
import java.net.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import ucl.physiol.neuroconstruct.utils.*;
import java.util.ArrayList;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Frame for viewing simple HTML files or XML files with XSL creating HTML
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class HelpFrame extends JFrame implements HyperlinkListener
{
    private static ClassLogger logger = new ClassLogger("HelpFrame");

    /**
     * The single help frame which can be instantiated
     */
    private static HelpFrame theHelpFrame = null;

    JPanel contentPane;
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelMain = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JPanel jPanelMainHTMLView = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JScrollPane scrollerMain = null;
    JButton jButtonOK = new JButton();

    JEditorPane jEditorPaneMain = new JEditorPane();
    BorderLayout borderLayout3 = new BorderLayout();

    boolean standalone = true;

    //File myFile = null;
    JPanel jPanelMenu = new JPanel();
    JEditorPane jEditorPaneMenu = new JEditorPane();
    BorderLayout borderLayout4 = new BorderLayout();
    JScrollPane scrollerMenu = new JScrollPane();
    JLabel statusBar = new JLabel();
    JPanel jPanelBrowseButtons = new JPanel();
    JButton jButtonBack = new JButton();
    JButton jButtonForwards = new JButton();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JButton jButtonHome = new JButton();

    History history = new History();


    private HelpFrame(URL url, String title, boolean standalone)
    {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try
        {
            this.standalone = standalone;

            jbInit();

            extraInit();

            this.setTitle(title);

            jEditorPaneMain.addHyperlinkListener(this);
            jEditorPaneMenu.addHyperlinkListener(this);


            setContent(history.addNewPage(url.toString()));
            /*
            String fileType = file.getCanonicalPath().substring(file.getCanonicalPath().lastIndexOf(".")+1);
            if (fileType.equals("html"))
            {
                String url = file.toURL().toString();

                //myFile = file;
            }
           else if (fileType.equals("xml"))
            {


                try
                {
                    File htmlEquivalent = new File(file.getAbsolutePath().substring(0,
                        file.getAbsolutePath().lastIndexOf(".")) + ".html");

                    boolean success = XMLUtils.transform(file,
                                                         htmlEquivalent);

                    if (!success)
                    {
                        setContent("Problem transforming the file: " + file);
                        return;
                    }

                    logger.logComment("The result is in " + htmlEquivalent + " *************");

                    setContent(htmlEquivalent.toURL().toString());
                    myFile = htmlEquivalent;


                    /*
                    File xslFile = null;
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                    DocumentBuilder db = dbf.newDocumentBuilder();

                    Document doc = db.parse(file);

                    //logger.logComment("N: "+doc.getChildNodes().item(0).getNodeType());//.getElementsByTagName("xml-stylesheet").item(0).getNodeName());
                    NodeList nl = doc.getChildNodes();
                    for (int j = 0; j < nl.getLength(); j++)
                    {
                        Node node = nl.item(j);
                        logger.logComment("Type: "+ node.getNodeType());
                        if (node.getNodeName().equals("xml-stylesheet"))
                        {
                            String nodeVal = node.getNodeValue();

                            logger.logComment("Looking at: " + nodeVal);
                            String xslFileName = nodeVal.substring(nodeVal.indexOf("href=\"") + 6, nodeVal.length() - 1);
                            xslFile = new File(xslFileName);
                            if (!xslFile.exists()) xslFile = new File(file.getParent(), xslFileName);

                        }
                    }


                    logger.logComment("The xslFile is " + xslFile + " *************");

                    TransformerFactory tFactory = TransformerFactory.newInstance();

                    StreamSource source = new StreamSource(xslFile);

                    Transformer transformer = tFactory.newTransformer(source);



                    transformer.transform(new StreamSource(file),
                                          new StreamResult(new FileOutputStream(htmlEquivalent)));

                    logger.logComment("The result is in " + htmlEquivalent + " *************");

                    setContent(htmlEquivalent.toURL().toString());
                    myFile = htmlEquivalent;
                }
                catch (Exception e)
                {
                    GuiUtils.showErrorMessage(logger, "Error when loading the XML file: "+file, e, this);
                }



            }
            else
            {
                GuiUtils.showErrorMessage(logger, "The file type: "+fileType+" is not supported by this application", null, this);
                if (standalone) System.exit(0);
                else dispose();
                return;

            }*/
        }
        catch (Exception e)
        {
            logger.logError("Error starting up: ", e);
        }
    }


    public static HelpFrame showFrame(URL url, String title, boolean standalone)
    {
        if (url==null)
        {
            try
            {
                url = ProjectStructure.getGlossaryHtmlFile().toURL();
            }
            catch (MalformedURLException m)
            {
                
            }
        }
        if (theHelpFrame ==null)
        {
            theHelpFrame = new HelpFrame(url, title, standalone);
        }
        else
        {
            theHelpFrame.setTitle(title);
            theHelpFrame.setStandalone(standalone);
        }
        //theHelpFrame.setFrameSize(900, 600);
        
        GuiUtils.centreWindow(theHelpFrame, .85f);
        theHelpFrame.setVisible(true);

        return theHelpFrame;
    }

    public static HelpFrame showGlossaryItem(String item)
    {
        File f = ProjectStructure.getGlossaryHtmlFile();
        try
        {
            String extra = "";
            if (item.trim().length()>0) extra = "#"+ item;
            
            URL internalURL = new URL("file:"+ f.getCanonicalPath()+ extra);
            
            HelpFrame simpleViewer = HelpFrame.showFrame(internalURL, f.getAbsolutePath(), false);

            //simpleViewer.setFrameSize(800, 600);


            GuiUtils.centreWindow(theHelpFrame, .85f);

            simpleViewer.setVisible(true);

            return simpleViewer;

        }
        catch (IOException io)
        {
            GuiUtils.showErrorMessage(logger, "Problem showing help frame", io, null);
            return null;
        }
    }




    public void setStandalone(boolean standalone)
    {
        this.standalone = standalone;
    }

    private void jbInit() throws Exception
    {
        contentPane = (JPanel)this.getContentPane();
        contentPane.setLayout(borderLayout1);
        this.setSize(new Dimension(400, 300));
        jPanelMain.setLayout(borderLayout2);
        jPanelButtons.setBorder(BorderFactory.createEtchedBorder());
        jPanelMainHTMLView.setBorder(BorderFactory.createEtchedBorder());
        jPanelMainHTMLView.setLayout(borderLayout3);
        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOK_actionPerformed(e);
            }
        });
        jPanelMenu.setLayout(borderLayout4);
        jPanelMenu.setBorder(BorderFactory.createEtchedBorder());
        jEditorPaneMenu.setText("");
        scrollerMenu.setMinimumSize(new Dimension(150, 35));
        scrollerMenu.setPreferredSize(new Dimension(150,35));
        jPanelMenu.setMinimumSize(new Dimension(150, 35));
        jPanelMenu.setPreferredSize(new Dimension(150, 35));

        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setMaximumSize(new Dimension(8, 19));
        statusBar.setMinimumSize(new Dimension(8, 19));
        statusBar.setPreferredSize(new Dimension(8, 19));
        statusBar.setText(""); jButtonBack.setText("<-"); jButtonBack.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonBack_actionPerformed(e);
            }
        }); jButtonForwards.setText("->"); jButtonForwards.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonForwards_actionPerformed(e);
            }
        }); jPanelBrowseButtons.setLayout(
            gridBagLayout1); jButtonHome.setText("Home"); jButtonHome.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonHome_actionPerformed(e);
            }
        });
        contentPane.add(jPanelMain, BorderLayout.CENTER);
        jPanelMain.add(jPanelButtons, BorderLayout.SOUTH);
        jPanelButtons.add(jButtonOK, null);
        jPanelMain.add(jPanelMainHTMLView, BorderLayout.CENTER);

        jEditorPaneMain.setContentType("text/html");
        jEditorPaneMain.setEditable(false);

        jEditorPaneMenu.setContentType("text/html");
        jEditorPaneMenu.setEditable(false);


        scrollerMain = new JScrollPane(jEditorPaneMain);


        jPanelMainHTMLView.add(scrollerMain, BorderLayout.CENTER);
        jPanelMain.add(jPanelMenu,  BorderLayout.WEST);
        jPanelMenu.add(scrollerMenu, BorderLayout.CENTER); jPanelBrowseButtons.add(jButtonBack, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                                        , GridBagConstraints.CENTER,
                                                                        GridBagConstraints.NONE, new Insets(6, 6, 6, 6),
                                                                        0, 0)); jPanelBrowseButtons.
            add(jButtonHome, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
                                                 , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                 new Insets(6, 6, 6, 6), 0, 0)); jPanelBrowseButtons.add(
            jButtonForwards, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                                                    , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                    new Insets(6, 6, 6, 6), 0, 0)); jPanelMain.add(jPanelBrowseButtons, java.awt.BorderLayout.NORTH);
        contentPane.add(statusBar,  BorderLayout.SOUTH);

        scrollerMenu = new JScrollPane(jEditorPaneMenu);

        jPanelMenu.add(scrollerMenu, BorderLayout.CENTER);

        //jPanelMenu.add(jEditorPane1, BorderLayout.NORTH);

    }

    private void extraInit()
    {
        File menuPage = ProjectStructure.getHelpMenuFile();
        try
        {

            URL fileUrl = new URL(menuPage.toURL().toString());

            logger.logComment("Setting URL to: " + fileUrl.toString());

            jEditorPaneMenu.setPage(fileUrl);

        }
        catch (IOException ex)
        {
            logger.logError("Error with content: "+ menuPage, ex);
        }

    }

    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
           if (standalone) System.exit(0);
           else dispose();
        }
    }

    public void setContent(String url)
    {
        try
        {
            URL fileUrl = new URL(url); //(new File(url)).toURL();

            logger.logComment(">>>   Setting URL to: " + fileUrl.toString());

            this.setTitle("neuroConstruct Help: " + fileUrl.toString());

            jEditorPaneMain.setPage(fileUrl);

          //  if (fileUrl.toString().indexOf("#")>0)
          //  {
           //     logger.logComment("Resetting the page, due to problem of going directly to page anchor in new page");
           //     jEditorPaneMain.setPage(fileUrl);

          //  }

        }
        catch (IOException ex)
        {
            logger.logError("Error with content: "+ url, ex);
        }

    }

    void jButtonOK_actionPerformed(ActionEvent e)
    {
        if (standalone) System.exit(0);
        else dispose();
    }

    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        //logger.logComment("Hyperlink!!");
        //logger.logComment(e+"");

        if (e.getEventType() != null
            && e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
        {
            //logger.logComment("e.getEventType(): " + e.getEventType());
           // logger.logComment("Loading: " + e.getURL().toExternalForm());
            String url = e.getURL().toExternalForm();

            logger.logComment("URL: " + url);

            if (url.startsWith("http://"))
            {
                logger.logComment("External link: " + url);
                String browserPath = GeneralProperties.getBrowserPath(true);
                if (browserPath==null)
                {
                    GuiUtils.showErrorMessage(logger, "Could not start a browser!", null, this);
                    return;
                }
            

                Runtime rt = Runtime.getRuntime();

                String command = browserPath + " \"" + url + "\"";
                if (GeneralUtils.isLinuxBasedPlatform())
                {
                    command = browserPath + " " + url;
                }

                logger.logComment("Going to execute command: " + command);

                try
                {
                    rt.exec(command);

                    logger.logComment("Have successfully executed command: " + command);

                }
                catch (IOException ex)
                {
                    logger.logError("Error running " + command);
                }

            }
            else
            {

                setContent(history.addNewPage(url));
            }


        }

        else if (e.getEventType() != null
            && e.getEventType().equals(HyperlinkEvent.EventType.ENTERED))
        {
           // logger.logComment("e.getEventType(): " + e.getEventType());
            //logger.logComment("Setting status bar to: " + e.getURL().toExternalForm());
            statusBar.setText("Link: "+ e.getURL().toExternalForm());
        }


        else if (e.getEventType() != null
            && e.getEventType().equals(HyperlinkEvent.EventType.EXITED))
        {
         //   logger.logComment("e.getEventType(): " + e.getEventType());
            statusBar.setText("");
        }


        else
        {
            logger.logComment("Not equal");
        }
    }

    public void setFrameSize(int width, int height)
    {
        jPanelMain.setMaximumSize(new Dimension(width, height));
        jPanelMain.setMinimumSize(new Dimension(width, height));
        jPanelMain.setPreferredSize(new Dimension(width, height));
        this.pack();
    }


    public static void main(String[] args)
    {
        try
        {
            File f = new File("docs/helpdocs/Glossary_gen.html");

            HelpFrame simpleViewer = new HelpFrame(f.toURL(), f.getAbsolutePath(), true);
            System.out.println("Created viewer");

            simpleViewer.setFrameSize(800, 600);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = simpleViewer.getSize();

            if (frameSize.height > screenSize.height)
                frameSize.height = screenSize.height;
            if (frameSize.width > screenSize.width)
                frameSize.width = screenSize.width;

            simpleViewer.setLocation( (screenSize.width - frameSize.width) / 2,
                                     (screenSize.height - frameSize.height) / 2);

            simpleViewer.setVisible(true);
        }
        catch(Exception e)
        {
            e.printStackTrace();


        }
    }

    public void jButtonHome_actionPerformed(ActionEvent e)
    {
        setContent(history.getStartPage());
    }

    public void jButtonBack_actionPerformed(ActionEvent e)
    {
        setContent(history.getPrevPage());

    }

    public void jButtonForwards_actionPerformed(ActionEvent e)
    {
        setContent(history.getNextPage());

    }


    private class History
    {

        ArrayList<String> history = new ArrayList<String>();
        int historyIndex = 0;

        public String getStartPage()
        {
            logger.logComment("Adding start page: "+history.get(0));
            logger.logComment("Old history: "+ history);
            logger.logComment("historyIndex: "+historyIndex+ ", hist size: "+history.size() );

            truncateHistory();

            history.add(history.get(0)); // add to end
            historyIndex = history.size() -1;
            return history.get(historyIndex);
        }

        private void truncateHistory()
        {
            logger.logComment("Truncating history, cutting off entries after historyIndex: "+historyIndex);

            logger.logComment("Old history: "+ history+ ", hist size: "+history.size() );

            if (history.size() > historyIndex+1)
            {
                for (int i = history.size()-1; i > historyIndex; i--)
                {
                    logger.logComment("Removing entry at "+i+": "+ history.get(i));
                    history.remove(i);
                }
            }

            logger.logComment("New history: "+ history);
            logger.logComment("historyIndex: "+historyIndex);


        }

        public String getPrevPage()
        {
            logger.logComment("Getting prev in history: "+ history);
            logger.logComment("historyIndex: "+historyIndex);
            if (historyIndex >0) historyIndex--;
            return history.get(historyIndex);
        }

        public String getNextPage()
        {
            logger.logComment("Getting next in history: "+ history);
            logger.logComment("historyIndex: "+historyIndex);
            if (historyIndex < (history.size() -1)) historyIndex++;
            return history.get(historyIndex);
        }

        public String addNewPage(String url)
        {
            logger.logComment("Adding new page to history: "+ history);
            logger.logComment("historyIndex: "+historyIndex+ ", hist size: "+history.size() );

            truncateHistory();

            history.add(url);
            historyIndex = history.size()-1;

            logger.logComment("New history: "+ history);
            logger.logComment("historyIndex: "+historyIndex);
            return history.get(historyIndex);

        }


    }

}
