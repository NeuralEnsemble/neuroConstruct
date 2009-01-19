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

import java.awt.*;
import javax.swing.*;
import java.util.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.awt.event.*;
import java.io.File;
import ucl.physiol.neuroconstruct.utils.equation.EquationException;


/**
 * Dialog for the properties of the synaptic connection between cell groups
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class SynapticPropertiesDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("SynapticPropertiesDialog");
    //Project project = null;

    private SynapticProperties mySynProps = null;

    Project project = null;

    boolean cancelled = false;

    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelMain = new JPanel();
    JLabel jLabelSynapseType = new JLabel();


    JComboBox jComboBoxSynapseType = new JComboBox();
    JLabel jLabelDelay = new JLabel();
    JTextField jTextFieldDelay = new JTextField();
    JPanel jPanelButtons = new JPanel();
    JButton jButtonOK = new JButton();
    JButton jButtonCancel = new JButton();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JButton jButtonDelay = new JButton();
    JLabel jLabelWeights = new JLabel();
    JTextField jTextFieldWeights = new JTextField();
    JButton jButtonWeights = new JButton();
    JLabel jLabelThreshold = new JLabel();
    JTextField jTextFieldThreshold = new JTextField();


    public SynapticPropertiesDialog(Dialog dlg, SynapticProperties synProps, Project project)
    {
        super(dlg, "Synaptic Properties", false);

        logger.logComment("Starting with: "+ synProps);

        this.mySynProps = synProps;
        this.project = project;

        try
        {
            jbInit();
            extraInit();
            pack();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }

    public SynapticPropertiesDialog(Frame frame, SynapticProperties synProps, Project project)
    {
        super(frame, "Synaptic Properties", false);

        this.mySynProps = synProps;
        this.project = project;

        try
        {
            jbInit();
            extraInit();
            pack();
        }
        catch(Exception ex)
        {
            logger.logComment("Exception starting GUI: "+ ex);
        }

    }

    public SynapticProperties getFinalSynProps()
    {
        return mySynProps;
    }


    private void jbInit() throws Exception
    {
        panel1.setLayout(borderLayout1);
        jLabelSynapseType.setText("Synapse type:");
        jLabelDelay.setText("Internal delay:");
        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOK_actionPerformed(e);
            }
        });
        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCancel_actionPerformed(e);
            }
        });
        jPanelMain.setLayout(gridBagLayout1);
        jButtonDelay.setText("...");
        jButtonDelay.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonDelay_actionPerformed(e);
            }
        });
        jTextFieldDelay.setEditable(false);
        jTextFieldDelay.setText("");
        panel1.setMaximumSize(new Dimension(400, 200));
        panel1.setMinimumSize(new Dimension(400, 200));
        panel1.setPreferredSize(new Dimension(400, 200));
        jLabelWeights.setText("Synaptic weights:");
        jButtonWeights.setText("...");
        jButtonWeights.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonWeights_actionPerformed(e);
            }
        });
        jTextFieldWeights.setEditable(false);
        jTextFieldWeights.setText("");
        jLabelThreshold.setText("Voltage threshold:");
        jTextFieldThreshold.setText("");
        getContentPane().add(panel1);
        panel1.add(jPanelMain, BorderLayout.CENTER);
        jPanelMain.add(jLabelSynapseType,      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 20, 6, 12), 0, 0));

        jPanelMain.add(jLabelDelay,      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 20, 6, 0), 0, 0));
        jPanelMain.add(jTextFieldDelay,        new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 6, 6), 0, 0));
        jPanelMain.add(jButtonDelay,    new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 6, 0, 20), 0, 0));
        jPanelMain.add(jLabelWeights,     new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 12, 6, 12), 0, 0));
        jPanelMain.add(jTextFieldWeights,     new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 6, 6), 0, 0));
        jPanelMain.add(jButtonWeights,    new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 6, 6, 20), 0, 0));
        panel1.add(jPanelButtons,  BorderLayout.SOUTH);
        jPanelButtons.add(jButtonOK, null);
        jPanelButtons.add(jButtonCancel, null);
        jPanelMain.add(jLabelThreshold,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 12, 6, 12), 0, 0));
        jPanelMain.add(jTextFieldThreshold,    new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 6, 20), 0, 0)); jPanelMain.add(
            jComboBoxSynapseType, new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0
                                                         , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                                         new Insets(6, 0, 6, 20), 0, 0));
    }

    private void extraInit()
    {
        addSynapses();

        jTextFieldDelay.setText(mySynProps.getDelayGenerator().toShortString());
        jTextFieldWeights.setText(mySynProps.getWeightsGenerator().toShortString());
        jTextFieldThreshold.setText(mySynProps.getThreshold()+ "");

        jComboBoxSynapseType.setSelectedItem(mySynProps.getSynapseType());
    }



    private boolean addSynapses()
    {
        //ModFile[] inbuiltSynapses = ModFileHelper.getSynapseModFilesInDir(new File(GeneralProperties.getModTemplateDirectory()));

        Vector synapticTypes =  project.cellMechanismInfo.getAllChemElecSynMechNames();

        for (int i = 0; i < synapticTypes.size(); i++)
        {
            jComboBoxSynapseType.addItem(synapticTypes.elementAt(i));

            if (this.mySynProps.getSynapseType().equals(synapticTypes.elementAt(i)))
            {
                jComboBoxSynapseType.setSelectedItem(synapticTypes.elementAt(i));
            }
        }
/*
        ModFile[] mods = null;


        File neuronCodeDir = new File(project.getProjectFile()
                                + System.getProperty("file.separator")
                                + GeneralProperties.getDirForNeuronCode());

        mods = ModFileHelper.getSynapseModFilesInDir(neuronCodeDir);

        for (int i = 0; i < mods.length; i++)
        {
            jComboBoxSynapseType.addItem(mods[i]);
            if (this.mySynProps.synapseType.equals(mods[i].myNeuronElement.getProcessName()))
            {
                jComboBoxSynapseType.setSelectedItem(mods[i]);
            }

        }
*/

        return true;
    }

    void jButtonDelay_actionPerformed(ActionEvent e)
    {
        NumberGeneratorDialog dlg = new NumberGeneratorDialog((Frame)null, "Delay", "Delay of synapse in ms",mySynProps.getDelayGenerator());

        //Center the window
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
        dlg.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        dlg.setVisible(true);

        jTextFieldDelay.setText(mySynProps.getDelayGenerator().toShortString());

    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel button pressed");
        cancelled = true;
        this.dispose();
    }

    void jButtonOK_actionPerformed(ActionEvent e)
    {
        logger.logComment("OK button pressed");
        cancelled = false;

        double threshold;

        try
        {
            threshold = Double.parseDouble(jTextFieldThreshold.getText());
        }
        catch (NumberFormatException ex)
        {
            GuiUtils.showErrorMessage(logger, "Please enter a number (usually -50 -> 0) for the millivolt value of the firing threshold", ex, this);
            return;
        }
        mySynProps.setThreshold(threshold);

        String selectedSynapse = (String)jComboBoxSynapseType.getSelectedItem();

        mySynProps.setSynapseType(selectedSynapse);

        this.dispose();
    }

    void jButtonWeights_actionPerformed(ActionEvent e)
    {
        NumberGeneratorDialog dlg 
                = new NumberGeneratorDialog((Frame)null, "Weights", "Weight of synaptic connections", mySynProps.getWeightsGenerator(), true);

        //Center the window
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
        dlg.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        dlg.setVisible(true);


        jTextFieldWeights.setText(mySynProps.getWeightsGenerator().toShortString());
        
                //mySynProps.setWeightsGenerator(dlg.getFinalNumGen());
    }



    public static void main(String[] args) throws ProjectFileParsingException, EquationException
    {
        
            Project testProj = Project.loadProject(new File("examples/Ex5-Networks/Ex5-Networks.neuro.xml"),
                                                   new ProjectEventListener()
            {
                public void tableDataModelUpdated(String tableModelName)
                {};

                public void tabUpdated(String tabName)
                {};
                public void cellMechanismUpdated()
                {
                };

            });
        
        SynapticProperties sp = new SynapticProperties("Syn1");
        sp.setDelayGenerator(new NumberGenerator(33));
        WeightGenerator wg = new WeightGenerator("r*r", true);
        
        sp.setWeightsGenerator(wg);

        SynapticPropertiesDialog dlg = new SynapticPropertiesDialog(new Frame(), sp, testProj);



    //    Dimension dlgSize = dlg.getPreferredSize();
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);
        
        System.out.println("wg: "+ sp.getWeightsGenerator());

    }




}
