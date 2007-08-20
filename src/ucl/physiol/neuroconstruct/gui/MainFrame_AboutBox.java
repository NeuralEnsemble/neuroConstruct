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
 * @version 1.0.6
 */


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

    //String copyright = "Copyright (c) 2006 UCL";
    String comments
        = "Java application for creating 3D networks of biologically realistic\n"
         +"neurons for the NEURON and GENESIS simulators. "
         +"\nThis work has been primarily funded by the Medical Research Council"
         +"\nand has also been supported by the Wellcome Trust";

    Border border1;
    JLabel jLabelWeb = new JLabel();
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
        label3.setText("Copyright (c) 2006 Dept of Physiology, UCL");
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
        jLabelWeb.setText("http://www.neuroConstruct.org");
        jLabelWeb.setForeground(Color.blue);

        jLabelWeb.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent mouseEvent)
            {
                String browserPath = GeneralProperties.getBrowserPath(true);
                Runtime rt = Runtime.getRuntime();

                String command = browserPath + " " + jLabelWeb.getText();

                logger.logComment("Going to execute command: " + command);

                try
                {
                    Process currentProcess = rt.exec(command);
                }
                catch (IOException ex)
                {
                    logger.logError("Error running " + command);
                }

                logger.logComment("Have successfully executed command: " + command);

            }
            public void mouseEntered(MouseEvent mouseEvent)
            {
                jLabelWeb.setForeground(Color.BLACK);
            }
            public void mouseExited(MouseEvent mouseEvent)
            {
                jLabelWeb.setForeground(Color.BLUE);

            }


        });

        insetsPanel2.add(imageLabel, null);
        panel2.add(insetsPanel2,  BorderLayout.NORTH);
        this.getContentPane().add(panel1, null);
        insetsPanel3.add(label1,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 6, 0), 0, 0));

        insetsPanel3.add(CommentArea,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 20, 0), 0, 0));

        insetsPanel3.add(label3,    new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 6, 0), 0, 0));
        insetsPanel3.add(label4,    new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 6, 0), 0, 0));
        insetsPanel3.add(jLabelWeb,   new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        panel2.add(insetsPanel3, BorderLayout.CENTER);
        insetsPanel1.add(button1, null);
        panel1.add(insetsPanel1, BorderLayout.SOUTH);
        panel1.add(panel2, BorderLayout.NORTH);
        setResizable(true);
    }
    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            cancel();
        }
        super.processWindowEvent(e);
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
