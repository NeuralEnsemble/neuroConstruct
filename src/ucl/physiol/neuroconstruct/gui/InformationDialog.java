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

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.border.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Frame for viewing text files
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")
public class InformationDialog extends JDialog
{

    private static ClassLogger logger = new ClassLogger("InformationDialog");


    JPanel jPanelMain = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JPanel jPanelFile = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JButton jButtonClose = new JButton();
    
    JEditorPane jEditorPaneMain = new JEditorPane();
    BorderLayout borderLayout2 = new BorderLayout();
    Border border1;
    JScrollPane scroller = new JScrollPane();

    boolean standalone = true;


    String source = null;

    private InformationDialog(boolean standalone, JFrame parent, boolean modal)
    {
        super(parent, modal);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try
        {
            this.standalone = standalone;
            jbInit();

        }
        catch(Exception e)
        {
            logger.logError("Exception starting frame", e);
        }
    }

    private InformationDialog(boolean standalone, JDialog parent, boolean modal)
    {
        super(parent, modal);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try
        {
            this.standalone = standalone;
            jbInit();

        }
        catch(Exception e)
        {
            logger.logError("Exception starting frame", e);
        }
    }



    public InformationDialog(String message,
                        String title,
                        int fontSize,
                        boolean standalone,
                        boolean html,
                        JFrame parent,
                        boolean modal)
    {
        this(standalone, parent, modal);

        setUpWithString(message, title, fontSize, html);
    }


    public InformationDialog(String message,
                        String title,
                        int fontSize,
                        boolean standalone,
                        boolean html,
                        JDialog parent,
                        boolean modal)
    {
        this(standalone, parent, modal);

        setUpWithString(message, title, fontSize, html);
    }


    public void setUpWithString(String message, String title, int fontSize, boolean html)
    {
        try
        {

            if (html)
            {
                setContentType("text/html");
                if (!message.trim().toLowerCase().startsWith("<html>"))
                {
                    message = "<html><head><style type=\"text/css\">"
                        + " p {text-align: left; font-size: 12pt; font-family: monospaced}"
                        + "</style></head>"
                        +"<body>"
                        + message
                        + "</body>"
                        + "</html>";
                }
            }
            else
            {
                //this.jButtonToggleHtmlSource.setEnabled(false);
            }
            source = message;


            jEditorPaneMain.setText(source);

            Font myFont = new Font("Monospaced", Font.PLAIN, fontSize);
            jEditorPaneMain.setFont(myFont );

            this.setTitle(title);

            jEditorPaneMain.setCaretPosition(0);
        }
        catch(Exception e)
        {
            logger.logError("Exception starting frame: "+ title, e);
        }
    }


    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            this.jButtonClose_actionPerformed(null);
        }
    }



    private void jbInit() throws Exception
    {
        border1 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,Color.white,Color.white,new Color(124, 124, 124),new Color(178, 178, 178)),BorderFactory.createEmptyBorder(5,5,5,5));
        jPanelMain.setDebugGraphicsOptions(0);
        jPanelMain.setMaximumSize(new Dimension(600, 600));
        jPanelMain.setMinimumSize(new Dimension(600, 600));
        jPanelMain.setPreferredSize(new Dimension(600, 600));

        this.setSize(new Dimension(630, 630));

        jPanelMain.setLayout(borderLayout1);
        jPanelFile.setBorder(BorderFactory.createEtchedBorder());
        jPanelButtons.setBorder(BorderFactory.createEtchedBorder());
        jButtonClose.setText("Close");
        ///this.jButtonToggleHtmlSource.setText("Source");
        jButtonClose.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonClose_actionPerformed(e);
            }
        });
        

        this.setTitle("");
        jEditorPaneMain.setBorder(border1);
        jEditorPaneMain.setEditable(false);
        jEditorPaneMain.setText("Text...\nText...\nText...\nText...\nText...\nText...\nText...\n");
        borderLayout2.setHgap(10);
        borderLayout2.setVgap(10);

        scroller.setMaximumSize(new Dimension(500, 500));
        scroller.setMinimumSize(new Dimension(500, 500));
        scroller.setPreferredSize(new Dimension(500, 500));

        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
        jPanelMain.add(jPanelFile, BorderLayout.CENTER);
        jPanelMain.add(jPanelButtons,  BorderLayout.SOUTH);
        //jPanelButtons.add(this.jButtonToggleHtmlSource, null);
        jPanelButtons.add(jButtonClose, null);
        jPanelFile.setLayout(borderLayout2);

        JViewport vp = scroller.getViewport();
        vp.add(jEditorPaneMain);

        jPanelFile.add(scroller, BorderLayout.CENTER);


    }

    protected void addButton(String extraButtonTitle, ActionListener extraButtonListener)
    {

        JButton jButtonExtra = new JButton(extraButtonTitle);
        jButtonExtra.addActionListener(extraButtonListener);

        jPanelButtons.add(jButtonExtra, 0);


    }




    protected void setContentType(String contentType)
    {
        jEditorPaneMain.setContentType(contentType);
    }


    protected String getShownText()
    {
        return this.jEditorPaneMain.getText();
    }

    /**
     * Shows InformationDialog frame with String message, taking up percentage% of width/height of screen
     */
    public static InformationDialog showString(String message,
                                          String title,
                                          int fontSize,
                                          boolean standalone,
                                          boolean html,
                                          float screenFraction)
    {
        return showString(message, title, fontSize, standalone, html, screenFraction, screenFraction, (JFrame)null, false, null, null);

    }
    /**
     * Shows SimpleViewer frame with String message, taking up percentage% of width/height of screen
     */
    public static InformationDialog showString(String message,
                                          String title,
                                          int fontSize,
                                          boolean standalone,
                                          boolean html,
                                          float widthFraction,
                                          float heightFraction)
    {
        return showString(message, title, fontSize, standalone, html, widthFraction, heightFraction, (JFrame)null, false, null, null);

    }



    /**
     * Shows SimpleViewer frame with String message, taking up percentage% of width/height of screen
     */
    public static InformationDialog showString(String message,
                                  String title,
                                  int fontSize,
                                  boolean standalone,
                                  boolean html,
                                  float widthFraction,
                                  float heightFraction,
                                  JDialog parent,
                                  boolean modal,
                                  String extraButtonTitle,
                                  ActionListener extraButtonListener)
    {
        logger.logComment("Showing string of length: "+ message.length());

        InformationDialog simpleViewer = new InformationDialog(message, title, fontSize, standalone, html, parent, modal);

        if (extraButtonTitle!=null && extraButtonListener!=null)
        {
            simpleViewer.addButton(extraButtonTitle, extraButtonListener);
        }


        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        simpleViewer.setFrameSize((int)(screenSize.getWidth()*widthFraction), (int)(screenSize.getHeight()*heightFraction));

        Dimension frameSize = simpleViewer.getSize();

        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;

        simpleViewer.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        simpleViewer.setVisible(true);

        return simpleViewer;
    }


    /**
     * Shows SimpleViewer frame with String message, taking up percentage% of width/height of screen
     */
    public static InformationDialog showString(String message,
                                  String title,
                                  int fontSize,
                                  boolean standalone,
                                  boolean html,
                                  float widthFraction,
                                  float heightFraction,
                                  JFrame parent,
                                  boolean modal,
                                  String extraButtonTitle,
                                  ActionListener extraButtonListener
)
    {
        logger.logComment("Showing string of length: "+ message.length());

        InformationDialog simpleViewer = new InformationDialog(message, title, fontSize, standalone, html, parent, modal);

        if (extraButtonTitle!=null && extraButtonListener!=null)
        {
            simpleViewer.addButton(extraButtonTitle, extraButtonListener);
        }


        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        simpleViewer.setFrameSize((int)(screenSize.getWidth()*widthFraction), (int)(screenSize.getHeight()*heightFraction));

        Dimension frameSize = simpleViewer.getSize();

        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;

        simpleViewer.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        simpleViewer.setVisible(true);

        simpleViewer.setToStartOfMessage();

        return simpleViewer;
    }

    public void setToStartOfMessage()
    {
        jEditorPaneMain.setCaretPosition(0);
    }



    /**
     * Shows SimpleViewer frame with String message, taking up 90% of width/height of screen
     */
    public static InformationDialog showString(String message,
                                  String title,
                                  int fontSize,
                                  boolean standalone,
                                  boolean html)
    {
        return showString(message,
                                  title,
                                  fontSize,
                                  standalone,
                                  html,
                                  0.9f);

    }


    protected void setEditable()
    {
        jEditorPaneMain.setEditable(true);
    }

    protected String getString()
    {
        return jEditorPaneMain.getText();
    }




    public void setFrameSize(int width, int height)
    {
        jPanelMain.setMaximumSize(new Dimension(width, height));
        jPanelMain.setMinimumSize(new Dimension(width, height));
        jPanelMain.setPreferredSize(new Dimension(width, height));
        this.pack();
    }

    void jButtonClose_actionPerformed(ActionEvent e)
    {
        this.dispose();

        if (standalone) System.exit(0);

    }

   


    public void setLineWrap(boolean wrap)
    {
        //jEditorPaneMain.setl
    }





    public static void main(String[] args)
    {
        System.out.println("Showning simp viewer");
        
        String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();
        try
        {
            UIManager.setLookAndFeel(favouredLookAndFeel);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        showString("Hello","There", 12, true, true);

 
    }

}
