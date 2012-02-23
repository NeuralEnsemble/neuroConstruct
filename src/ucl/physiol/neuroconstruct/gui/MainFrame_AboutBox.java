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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * About box for project
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class MainFrame_AboutBox extends JDialog implements ActionListener
{

    ClassLogger logger = new ClassLogger("MainFrame_AboutBox");
    JPanel panel1 = new JPanel();
    JPanel panel2 = new JPanel();
    JPanel insetsPanel1 = new JPanel();
    JPanel insetsPanel2 = new JPanel();
    JPanel insetsPanel3 = new JPanel();
    JButton button1 = new JButton();
    JLabel imageLabel = new JLabel();
    JLabel label1 = new JLabel();
    JLabel label2 = new JLabel();
    JLabel label3 = new JLabel();
    JLabel label4 = new JLabel();
    JTextArea CommentArea = new JTextArea();
    ImageIcon image1 = new ImageIcon();
    ImageIcon image2 = new ImageIcon();
    BorderLayout borderLayout1 = new BorderLayout();
    BorderLayout borderLayout2 = new BorderLayout();
    FlowLayout flowLayout1 = new FlowLayout();

    String comments
        = "Java application for creating 3D networks of biologically realistic neurons\n"
         +"for NEURON, GENESIS, MOOSE, PSICS and PyNN compliant simulators. "
         +"\nThis work is primarily funded by the Wellcome Trust and has also"
         +"\nbeen supported by the Medical Research Council";

    Border border1;
    JLabel jLabelWeb1 = new JLabel();
    JLabel jLabelWeb2 = new JLabel();
    JLabel jLabelInfo = new JLabel();

    JLabel jLabelLib1 = new JLabel();
    JLabel jLabelLib2 = new JLabel();
    JLabel jLabelLib3 = new JLabel();
    JLabel jLabelLib4 = new JLabel();

    Border border2;
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    Border border3;

    public MainFrame_AboutBox(Frame parent)
    {
        super(parent);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try
        {
            jbInit();
        }
        catch(Exception e)
        {
            logger.logComment("Exception starting GUI: "+ e);
        }
    }
    //Component initialization
    private void jbInit() throws Exception
    {
        //image1 = new ImageIcon(ucl.physiol.neuroconstruct.gui.MainFrame.class.getResource("about.png"));
        image1 = new ImageIcon(ucl.physiol.neuroconstruct.gui.MainFrame.class.getResource("about.png"));
        image2 = new ImageIcon(ucl.physiol.neuroconstruct.gui.MainFrame.class.getResource("logo.png"));
        border1 = BorderFactory.createEmptyBorder(10,20,10,20);
        border2 = BorderFactory.createEmptyBorder(0,0,12,0);
        border3 = BorderFactory.createEmptyBorder(5,5,5,5);
        imageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        imageLabel.setIcon(image2);
        this.setTitle("About");
        panel1.setLayout(borderLayout1);
        panel2.setLayout(borderLayout2);
        insetsPanel1.setLayout(flowLayout1);
        insetsPanel2.setLayout(flowLayout1);
        insetsPanel2.setBorder(border3);
        label1.setHorizontalAlignment(SwingConstants.LEFT);
        label1.setText("neuroConstruct v"+ GeneralProperties.getVersionNumber());
        label3.setText("Copyright (c) 2012, Dept of Neuroscience, Pharmacology & Physiology, UCL");
        label4.setText("Contact Padraig Gleeson (p.gleeson@ucl.ac.uk) for more information");

        label4.setToolTipText("This work is dedicated to the memory of Nancy Gleeson");

        //CommentArea.setBorder(null);
        CommentArea.setColumns(20);
        CommentArea.setBackground(UIManager.getColor("Button.background"));
        CommentArea.setBorder(null);
        CommentArea.setCaretColor(Color.black);
        CommentArea.setEditable(false);
        CommentArea.setText(comments);
        insetsPanel3.setLayout(gridBagLayout1);
        insetsPanel3.setBorder(border1);
        button1.setText("OK");
        button1.addActionListener(this);
        jLabelWeb1.setText("http://www.neuroConstruct.org");
        addWebLink(jLabelWeb1, jLabelWeb1.getText());

        jLabelWeb2.setText("Credits");
        addWebLink(jLabelWeb2, "http://www.neuroconstruct.org/contact/index.html");

        jLabelInfo.setText("This application includes libraries from:");

        jLabelLib1.setText("The HDF Group");
        addWebLink(jLabelLib1, "http://www.hdfgroup.org");
        jLabelLib2.setText("Jython");
        addWebLink(jLabelLib2, "http://www.jython.org");
        jLabelLib3.setText("JUnit");
        addWebLink(jLabelLib3, "http://www.junit.org");
        jLabelLib4.setText("Java3D");
        addWebLink(jLabelLib4, "https://java3d.dev.java.net");

        

        insetsPanel2.add(imageLabel, null);
        panel2.add(insetsPanel2,  BorderLayout.NORTH);
        this.getContentPane().add(panel1, null);
        insetsPanel3.add(label1,    new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 6, 0), 0, 0));

        insetsPanel3.add(CommentArea,    new GridBagConstraints(0, 1, 4, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 20, 0), 0, 0));

        insetsPanel3.add(label3,    new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 6, 0), 0, 0));

        insetsPanel3.add(label4,    new GridBagConstraints(0, 3, 4, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 6, 0), 0, 0));


        insetsPanel3.add(jLabelWeb1,   new GridBagConstraints(0, 4, 4, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 6, 0), 0, 0));

        insetsPanel3.add(jLabelWeb2,   new GridBagConstraints(0, 5, 4, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 6, 0), 0, 0));

        insetsPanel3.add(jLabelInfo,   new GridBagConstraints(0, 6, 4, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 6, 0), 0, 0));

        insetsPanel3.add(jLabelLib1,   new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,6,12), 0, 0));
        insetsPanel3.add(jLabelLib2,   new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,6,12), 0, 0));
        insetsPanel3.add(jLabelLib3,   new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,6,12), 0, 0));
        insetsPanel3.add(jLabelLib4,   new GridBagConstraints(3, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,6,12), 0, 0));


        panel2.add(insetsPanel3, BorderLayout.CENTER);
        insetsPanel1.add(button1, null);
        panel1.add(insetsPanel1, BorderLayout.SOUTH);
        panel1.add(panel2, BorderLayout.NORTH);
        setResizable(true);
    }
    //Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            cancel();
        }
        super.processWindowEvent(e);
    }

    private void addWebLink(final JLabel jLabel, final String url)
    {
        jLabel.setForeground(Color.BLUE);
        
        MouseAdapter ma = new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent mouseEvent)
            {
                String browserPath = GeneralProperties.getBrowserPath(true);
                if (browserPath==null)
                {
                    GuiUtils.showErrorMessage(logger, "Could not start a browser!", null, null);
                    return;
                }

                Runtime rt = Runtime.getRuntime();

                String command = browserPath + " " + url;

                logger.logComment("Going to execute command: " + command);

                try
                {
                    rt.exec(command);
                }
                catch (IOException ex)
                {
                    logger.logError("Error running " + command);
                }

                logger.logComment("Have successfully executed command: " + command);

            }
            @Override
            public void mouseEntered(MouseEvent mouseEvent)
            {
                jLabel.setForeground(Color.BLACK);
            }
            @Override
            public void mouseExited(MouseEvent mouseEvent)
            {
                jLabel.setForeground(Color.BLUE);

            }


        };

        jLabel.addMouseListener(ma);
    }


    //Close the dialog
    void cancel()
    {
        dispose();
    }
    //Close the dialog on a button event
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == button1)
        {
            cancel();
        }
    }
}
