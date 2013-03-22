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
 * Frame for editing simple strings in an editor pane
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class SimpleTextInput extends JDialog
{

    private static ClassLogger logger = new ClassLogger("SimpleTextInput");


    JPanel jPanelMain = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JPanel jPanelMainString = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JButton jButtonClose = new JButton();
    JEditorPane jEditorPaneMain = new JEditorPane();
    BorderLayout borderLayout2 = new BorderLayout();
    Border border1;
    JScrollPane scroller = new JScrollPane();

    boolean standalone = true;
    URL fileUrl = null;

    String initialText = null;
    boolean cancelled = false;



    private SimpleTextInput(boolean standalone, JFrame parent)
    {
        super(parent, true);

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

    private SimpleTextInput(boolean standalone, JDialog parent)
    {
        super(parent, true);

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



/*
    public SimpleTextInput(File file,
                           int fontSize,
                           boolean standalone,
                           boolean html)
    {
        this(file, fontSize, standalone, html, (JFrame)null, false);
    }

    public SimpleViewer(File file,
                        int fontSize,
                        boolean standalone,
                        boolean html,
                        JFrame parent,
                        boolean modal)
    {

        this(standalone, parent, modal);

        setUpWithFile(file, fontSize, html);
    }
    public SimpleTextInput(File file,
                        int fontSize,
                        boolean standalone,
                        boolean html,
                        JDialog parent,
                        boolean modal)
    {
        this(standalone, parent, modal);

        setUpWithFile(file, fontSize, html);
    }


    public void setUpWithFile(File file, int fontSize, boolean html)
    {
        try
        {

            if (html) setContentType("text/html");


            URL fileUrl = file.toURL();

            logger.logComment("Setting URL to: "+ fileUrl.toString());

            jEditorPaneMain.setPage(fileUrl);
            Font myFont = new Font("Monospaced", Font.PLAIN, fontSize);
            jEditorPaneMain.setFont(myFont );

            //jEditorPaneMain.getEditorKit().

            this.setTitle("File: "+ file);
        }
        catch(IOException ioe)
        {
            logger.logError("Error opening file: "+ file, ioe);
            this.jEditorPaneMain.setText("Error opening file: "+ file);
        }
        catch(Exception e)
        {
            logger.logError("Exception starting frame to show: "+ file, e);
        }
    }
*/


    public SimpleTextInput(String message,
                           String title,
                           int fontSize,
                           boolean standalone,
                           boolean html)
    {
        this(message, title, fontSize, standalone, html, (JFrame)null);
    }


    public SimpleTextInput(File file,
                        int fontSize,
                        boolean standalone,
                        boolean html,
                        JFrame parent)
    {

        this(standalone, parent);

        setUpWithFile(file, fontSize, html);
    }

    public SimpleTextInput(File file,
                        int fontSize,
                        boolean standalone,
                        boolean html,
                        JDialog parent)
    {
        this(standalone, parent);

        setUpWithFile(file, fontSize, html);
    }


    public void setUpWithFile(File file, int fontSize, boolean html)
    {
        try
        {
            if (html) setContentType("text/html");

            fileUrl = file.toURL();

            logger.logComment("Setting URL to: "+ fileUrl.toString());

            jEditorPaneMain.setPage(fileUrl);
            Font myFont = new Font("Monospaced", Font.PLAIN, fontSize);
            jEditorPaneMain.setFont(myFont );

            //jEditorPaneMain.getEditorKit().

            this.setTitle("Editing file: "+ file);

            jButtonClose.setText("Close without saving");

            JButton jButtonSave = new JButton("Save file");

            JButton jButtonHelp = new JButton("?");


            jPanelButtons.remove(jButtonClose);
            jPanelButtons.add(jButtonSave, null);
            jPanelButtons.add(jButtonClose, null);
            jPanelButtons.add(jButtonHelp, null);

            jButtonSave.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    jButtonSave_actionPerformed(e);
                }
            });

            jButtonHelp.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    jButtonHelp_actionPerformed(e);
                }
            });

            initialText = this.getShownText();



        }
        catch(IOException ioe)
        {
            logger.logError("Error opening file: "+ file, ioe);
            this.jEditorPaneMain.setText("Error opening file: "+ file);
        }
        catch(Exception e)
        {
            logger.logError("Exception starting frame to show: "+ file, e);
            this.jEditorPaneMain.setText("Error opening file: "+ file);
        }
    }





    public SimpleTextInput(String message,
                           String title,
                           int fontSize,
                           boolean standalone,
                           boolean html,
                           JDialog parent)
    {
        this(standalone, parent);

        setUpWithString(message, title, fontSize, html);
    }

    public SimpleTextInput(String message,
                           String title,
                           int fontSize,
                           boolean standalone,
                           boolean html,
                           JFrame parent)
    {
        this(standalone, parent);

        setUpWithString(message, title, fontSize, html);
    }


    public void setUpWithString(String message, String title, int fontSize, boolean html)
    {
        initialText = message;
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


            JButton jButtonOk = new JButton("Ok");


            jPanelButtons.remove(jButtonClose);
            jPanelButtons.add(jButtonOk, null);
            jPanelButtons.add(jButtonClose, null);

            jButtonOk.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    jButtonSave_actionPerformed(e);
                }
            });


            jEditorPaneMain.setText(message);

            Font myFont = new Font("Monospaced", Font.PLAIN, fontSize);
            jEditorPaneMain.setFont(myFont );

            this.setTitle(title);

            initialText = this.getShownText();

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
        jPanelMainString.setBorder(BorderFactory.createEtchedBorder());
        jPanelButtons.setBorder(BorderFactory.createEtchedBorder());
        jButtonClose.setText("Cancel");
        jButtonClose.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonClose_actionPerformed(e);
            }
        });
        this.setTitle("");
        jEditorPaneMain.setBorder(border1);
        jEditorPaneMain.setEditable(true);
        jEditorPaneMain.setText("Text...\nText...\nText...\nText...\nText...\nText...\nText...\n");
        borderLayout2.setHgap(10);
        borderLayout2.setVgap(10);

        scroller.setMaximumSize(new Dimension(500, 500));
        scroller.setMinimumSize(new Dimension(500, 500));
        scroller.setPreferredSize(new Dimension(500, 500));

        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
        jPanelMain.add(jPanelMainString, BorderLayout.CENTER);
        jPanelMain.add(jPanelButtons,  BorderLayout.SOUTH);
        jPanelButtons.add(jButtonClose, null);
        jPanelMainString.setLayout(borderLayout2);

        JViewport vp = scroller.getViewport();
        vp.add(jEditorPaneMain);

        jPanelMainString.add(scroller, BorderLayout.CENTER);

        //this.jEditorPaneMain


    }

    public static SimpleTextInput showFile(String filename, int fontSize, boolean standalone, boolean html, JFrame parent)
    {
        return showFile(filename, fontSize, standalone, html, 0.9f, 0.9f, parent);

    }


    public static SimpleTextInput showFile(String filename,
                                           int fontSize,
                                           boolean standalone,
                                           boolean html,
                                           float widthFraction,
                                           float heightFraction,
                                           JFrame parent)

    {
        SimpleTextInput simpleTextInput = new SimpleTextInput(new File(filename), fontSize, standalone, html, parent);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        simpleTextInput.setFrameSize( (int) (screenSize.getWidth() * widthFraction), (int) (screenSize.getHeight() * heightFraction));
        Dimension frameSize = simpleTextInput.getSize();

        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;

        simpleTextInput.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        simpleTextInput.setVisible(true);


        return simpleTextInput;
    }


    protected void setContentType(String contentType)
    {
        jEditorPaneMain.setContentType(contentType);
    }


    protected String getShownText()
    {
        return this.jEditorPaneMain.getText();
    }

    public boolean isChanged()
    {
        if (cancelled) return false;
        return !initialText.equals(getShownText());
    }

    /**
     * Shows SimpleViewer frame with String message, taking up the specified fraction of width/height of screen
     */
    public static SimpleTextInput showString(String message,
                                          String title,
                                          int fontSize,
                                          boolean standalone,
                                          boolean html,
                                          float fraction)
    {
        return showString(message, title, fontSize, standalone, html, fraction, (JFrame)null);

    }


    /**
     * Shows SimpleViewer frame with String message, taking up the specified fraction of width/height of screen
     */
    public static SimpleTextInput showString(String message,
                                  String title,
                                  int fontSize,
                                  boolean standalone,
                                  boolean html,
                                  float fraction,
                                  JDialog parent)
    {
        logger.logComment("Showing string of length: "+ message.length());

        SimpleTextInput simpleTextInput = new SimpleTextInput(message, title, fontSize, standalone, html, parent);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        simpleTextInput.setFrameSize((int)(screenSize.getWidth()*fraction), (int)(screenSize.getHeight()*fraction));

        Dimension frameSize = simpleTextInput.getSize();

        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;

        simpleTextInput.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        simpleTextInput.setVisible(true);

        return simpleTextInput;
    }


    /**
     * Shows SimpleViewer frame with String message, taking up the fraction of width/height of screen
     */
    public static SimpleTextInput showString(String message,
                                             String title,
                                             int fontSize,
                                             boolean standalone,
                                             boolean html,
                                             float fraction,
                                             JFrame parent)
    {
        if (message==null) message = "";
        logger.logComment("Showing string of length: "+ message.length());


        SimpleTextInput simpleTextInput = new SimpleTextInput(message, title, fontSize, standalone, html, parent);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        simpleTextInput.setFrameSize((int)(screenSize.getWidth()*fraction), (int)(screenSize.getHeight()*fraction));

        Dimension frameSize = simpleTextInput.getSize();

        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;

        simpleTextInput.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        simpleTextInput.setVisible(true);

        return simpleTextInput;
    }



    /**
     * Shows SimpleTextInput frame with String message, taking up 90% of width/height of screen
     */
    public static SimpleTextInput showString(String message,
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




/*
    public static String showEditableString(String message,
                                    String title,
                                    int fontSize,
                                    boolean standalone)
    {
        SimpleViewer simpleViewer = new SimpleViewer(message, title, fontSize, standalone, false);


        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        simpleViewer.setFrameSize((int)(screenSize.getWidth()*0.4d), (int)(screenSize.getHeight()*0.4d));

        Dimension frameSize = simpleViewer.getSize();

        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;

        simpleViewer.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        simpleViewer.setEditable();
        //simpleViewer.
        simpleViewer.show();

        return simpleViewer.getString();

    }
    */
    protected void setEditable()
    {
        jEditorPaneMain.setEditable(true);
    }

    protected String getString()
    {
        if (cancelled) return initialText;
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
        this.cancelled = true;

        if (standalone) System.exit(0);

    }

    void jButtonSave_actionPerformed(ActionEvent e)
    {
        if (this.fileUrl!=null)
        {
            try
            {
                FileWriter fw = new FileWriter(fileUrl.getFile());
                fw.write(this.getShownText());
                fw.close();
            }
            catch (IOException ex)
            {
                logger.logError("Exception creating file: "+fileUrl.getFile());
            }
        }
        this.dispose();

        if (standalone) System.exit(0);

    }

    void jButtonOk_actionPerformed(ActionEvent e)
    {

        this.dispose();

        if (standalone) System.exit(0);

    }



    void jButtonHelp_actionPerformed(ActionEvent e)
    {
        GuiUtils.showInfoMessage(logger,
                                 "Information",
                                 "This is just a simple interface for quick editing of property files, etc. Functions like Undo\n"
                                 +"are not supported, so for major edits an external editor is advised. Note that if the files \n"
                                 +"are edited externally the project will have to be reopened for the changes to take effect", this);


    }




    public void setLineWrap(boolean wrap)
    {
        //jEditorPaneMain.setl
    }





    public static void main(String[] args)
    {
        String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();
        try
        {
            UIManager.setLookAndFeel(favouredLookAndFeel);
        }
        catch (Exception ex)
        {

        }
        /*
        SimpleTextInput sv = showString("<h1>ffff</h1>This is a <b>big</b> testThis is a <b>big</b> test<br><p>This is</p> a <b>big</b> testThis is a <b>big</b> testThis is a <b>big</b> test",
                                        "Tester",
                                        12,
                                        true,
                                        false,
            .40f);*/

        SimpleTextInput sv = showString("cznxfhgfjh", "gxzfghfgh",
                                        12,
                                        true,false, .20f);



        System.out.println("String: "+ sv.getString());
    }

}
