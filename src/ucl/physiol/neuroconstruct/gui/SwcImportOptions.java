package ucl.physiol.neuroconstruct.gui;

import javax.swing.*;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;

@SuppressWarnings("serial")

public class SwcImportOptions extends JDialog
{
    ClassLogger logger = new ClassLogger("SwcImportOptions");


    boolean cancelled = false;

    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelButtons = new JPanel();
    JButton jButtonCancel = new JButton();
    JButton jButtonOK = new JButton();
    JButton jButtonMore = new JButton();
    JPanel jPanelMain = new JPanel();
    JPanel jPanelFeatures = new JPanel();
    JPanel jPanelInfo = new JPanel();
    JCheckBox jCheckBoxFeatures = new JCheckBox();
    JTextArea jTextAreaFeatures = new JTextArea();
    JPanel jPanelDaughter = new JPanel();
    JCheckBox jCheckBoxDaughter = new JCheckBox();
    JTextArea jTextAreaDaughter = new JTextArea();
    BorderLayout borderLayout2 = new BorderLayout();
    
    JTextArea jTextAreaInfo = new JTextArea();

    public SwcImportOptions(Frame frame, String title, boolean modal)
    {
        super(frame, title, modal);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        try
        {
            jbInit();
            pack();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    private void jbInit() throws Exception
    {

        this.getContentPane().setLayout(borderLayout1); 
        jButtonOK.setActionCommand("OK"); 
        jButtonOK.addActionListener(new  ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOK_actionPerformed(e);
            }
        }); 
        
        jButtonCancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCancel_actionPerformed(e);
            }
        });
        
        jButtonMore.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonMore_actionPerformed(e);
            }
        });
        
        jTextAreaInfo.setLineWrap(true);
        jTextAreaInfo.setWrapStyleWord(true);
        //jTextAreaInfo.setMaximumSize(new Dimension(350, 50));
        //jTextAreaInfo.setMinimumSize(new Dimension(350, 50));
        jTextAreaInfo.setEditable(false);
        jTextAreaInfo.setBackground(jCheckBoxFeatures.getBackground());
        jTextAreaInfo.setColumns(40);
        
        
        jTextAreaInfo.setText("Some options when importing the SWC format morphology.\n\n"
                +"Note: As Cvapp is mainly concerned with dendritic reconstructions, there may be a limited amount of information present about the soma in such files."
                +"The second entry on each line of the *.swc file indicates what type of compartment it is. 1 usually indicates soma, 2 axons, 3 dendrites, etc.\n\n"
                +"Close examination of the original file is advised to correctly import the morphology."
                +"");
        
        
        jButtonOK.setText("OK");
        
        jButtonMore.setText("More on importing SWC");
        
        jCheckBoxFeatures.setText("Include anatomical features");

        jTextAreaFeatures.setBackground(jCheckBoxFeatures.getBackground());
        jPanelFeatures.setMaximumSize(new Dimension(350, 40));
        jPanelFeatures.setMinimumSize(new Dimension(350, 40));
        jPanelFeatures.setPreferredSize(new Dimension(350, 40));

        jTextAreaFeatures.setText("If extra anatomical detail is present in the file (e.g. from Neurolucida extra features) include if this box is ticked. "
                +"Note: lines with compartment type 10 or greater (second value in 7 entry SWC line) will be considered anatomical features. These will be included as sections, " +
                "and should be removed if the cells are to be used for simulations.");

        jTextAreaFeatures.setColumns(26);
        jTextAreaFeatures.setLineWrap(true);
        jTextAreaFeatures.setRows(4);
        jTextAreaFeatures.setWrapStyleWord(true);


        jTextAreaDaughter.setBackground(jTextAreaFeatures.getBackground());
        
        //jTextAreaDaughter.setMaximumSize(new Dimension(350, 40));
        //jTextAreaDaughter.setMinimumSize(new Dimension(350, 40));
       //jTextAreaDaughter.setPreferredSize(new Dimension(350, 40));

        jTextAreaDaughter.setText("If selected, daughter sections OF THE SOMA will start with the radius of the soma at the connection point."
                                  +" Otherwise the daughter start radii at the connection points will be the same as the first full daughter point."
                                  +" The second option avoids large cone like initial segments on initial dendrites.");

        jTextAreaDaughter.setColumns(26);
        jTextAreaDaughter.setLineWrap(true);
        jTextAreaDaughter.setRows(6);
        jTextAreaDaughter.setWrapStyleWord(true);

        jTextAreaDaughter.setEditable(false);
        this.jTextAreaFeatures.setEditable(false);




        jCheckBoxDaughter.setText("Daughters of somas inherit radii");
        jPanelMain.setLayout(borderLayout2);
        jPanelButtons.add(jButtonOK);
        jPanelButtons.add(jButtonMore);
        jPanelButtons.add(jButtonCancel);
        this.getContentPane().add(jPanelButtons, java.awt.BorderLayout.SOUTH);
        this.getContentPane().add(jPanelMain, java.awt.BorderLayout.CENTER); 
        
        jPanelDaughter.add(jCheckBoxDaughter);
        jPanelDaughter.add(jTextAreaDaughter);
        jPanelFeatures.add(jCheckBoxFeatures);
        jPanelFeatures.add(jTextAreaFeatures);
        
        jPanelInfo.add(jTextAreaInfo);

        jPanelMain.add(jPanelInfo, java.awt.BorderLayout.NORTH);
        jPanelMain.add(jPanelDaughter, java.awt.BorderLayout.SOUTH);
        jPanelMain.add(jPanelFeatures, java.awt.BorderLayout.CENTER);
        
        Dimension d = new Dimension(600,400);
        jPanelMain.setMaximumSize(d);
        jPanelMain.setMinimumSize(d);
        jPanelMain.setPreferredSize(d);
        
        jButtonCancel.setText("Cancel");

        this.jCheckBoxDaughter.setSelected(false);
        this.jCheckBoxFeatures.setSelected(false);
    }

    public static void main(String[] args)
    {
        SwcImportOptions dlg = new SwcImportOptions(new Frame(),"SWC/Cvapp import options", true);


        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = dlg.getSize();
        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;
        }
        dlg.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        dlg.setVisible(true);
        dlg.pack();

    }

    public boolean includeAnatFeatures()
    {
        return this.jCheckBoxFeatures.isSelected();
    }

    public boolean daughtersInherit()
    {
        return this.jCheckBoxDaughter.isSelected();
    }



    public void jButtonOK_actionPerformed(ActionEvent e)
    {
        logger.logComment("OK pressed...");

        this.dispose();
    }

    public void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel pressed...");

        cancelled = true;
        this.dispose();

    }

    public void jButtonMore_actionPerformed(ActionEvent e)
    {
        logger.logComment("More pressed...");

        cancelled = true;
        this.dispose();

        File f = new File(ProjectStructure.getHelpImportFile());
        
        try 
        {
            HelpFrame.showFrame(f.toURL(), "Importing morphologies", false);
        }
        catch (MalformedURLException m)
        {
            logger.logError("Error showing help file", m);
        }

    }

}
