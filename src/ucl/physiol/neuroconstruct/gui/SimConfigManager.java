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


import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;


import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.project.*;

import java.io.*;
import ucl.physiol.neuroconstruct.project.packing.*;
import ucl.physiol.neuroconstruct.hpc.mpi.*;
import ucl.physiol.neuroconstruct.simulation.*;

/**
 * GUI for editing simulation configurations
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class SimConfigManager extends JFrame implements ListSelectionListener, ItemListener, DocumentListener
{
    ClassLogger logger = new ClassLogger("SimConfigManager");

    public final static String SIM_CONFIG_HELP = "Simulation Configuration";

    private DefaultListModel listModelNames = new DefaultListModel();

    Hashtable<String, JCheckBox> cellGroupCheckBoxes = new Hashtable<String, JCheckBox>();
    Hashtable<String, JCheckBox> netConnCheckBoxes = new Hashtable<String, JCheckBox>();
    Hashtable<String, JCheckBox> inputCheckBoxes = new Hashtable<String, JCheckBox>();
    Hashtable<String, JCheckBox> plotCheckBoxes = new Hashtable<String, JCheckBox>();

    private Project project = null;

    private boolean updatingGUI = false;

    SimConfigInfo simConfigInfo = null;
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelList = new JPanel();
    JPanel jPanelDescription = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JButton jButtonOK = new JButton("Close");
    JButton jButtonInfo = new JButton("Info...");
    JButton jButtonEditDesc = new JButton("Edit description");
    JButton jButtonDelete = new JButton("Delete selected");
    JScrollPane jScrollPane1 = new JScrollPane();
    JList jListNames = new JList();
    BorderLayout borderLayout2 = new BorderLayout();
    JScrollPane jScrollPane2 = new JScrollPane();
    JTextArea jTextAreaDescription = new JTextArea();
    Border border1 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
    Border border2 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
    BorderLayout borderLayout3 = new BorderLayout();
    JButton jButtonAdd = new JButton("Add new");
    JButton jButtonHelp = new JButton();
    JPanel jPanelCheckBoxes = new JPanel();
    JPanel jPanelSimDur = new JPanel();
    JLabel jLabelSimDur = new JLabel();
    JTextField jTextFieldSimDur = new JTextField();
    JLabel jLabelMs = new JLabel();
    JLabel jLabelMpiConf = new JLabel();
    
    JComboBox jComboBoxMpiConfs = new JComboBox();
    

    public SimConfigManager(SimConfigInfo simConfigInfo, Frame owner, Project project)
    {
        super();
        this.simConfigInfo = simConfigInfo;
        this.project = project;
        try
        {
            jbInit();
            jListNames.setModel(listModelNames);
            refresh();

            this.jListNames.setSelectedIndex(0);

            this.pack();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private void jbInit() throws Exception
    {
        getContentPane().setLayout(borderLayout1);


        jButtonOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOK_actionPerformed(e);
            }
        });

        jButtonInfo.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonInfo_actionPerformed(e);
            }
        });


        jPanelList.setLayout(borderLayout2);
        jPanelDescription.setLayout(borderLayout3);

        this.setTitle("Simulation Configuration Manager");

        jTextAreaDescription.setColumns(10);
        jTextAreaDescription.setRows(7);
        jTextAreaDescription.setWrapStyleWord(true);
        jTextAreaDescription.setLineWrap(true);
        jTextAreaDescription.setEditable(false);

        jPanelDescription.setBorder(border1);
        jPanelList.setBorder(border2);
        jButtonDelete.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonDelete_actionPerformed(e);
            }
        }); jButtonEditDesc.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonEditDesc_actionPerformed(e);
            }
        });
        jButtonAdd.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonAdd_actionPerformed(e);
            }
        }); jButtonHelp.setText("?"); jButtonHelp.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonHelp_actionPerformed(e);
            }
        }); 
        jPanelCheckBoxes.setBorder(BorderFactory.createEtchedBorder()); 
        jLabelSimDur.setText("Simulation Duration:");
        jTextFieldSimDur.setText("..."); 
        jTextFieldSimDur.setColumns(6); 
        jLabelMs.setText("ms");

        jTextFieldSimDur.getDocument().addDocumentListener(this);

        this.getContentPane().add(jPanelDescription, java.awt.BorderLayout.CENTER); 
        jPanelButtons.add(jButtonAdd);
        jPanelButtons.add(jButtonEditDesc);


        //jPanelCheckBoxes.setMinimumSize(new Dimension(400, 100));
        //jPanelCheckBoxes.setPreferredSize(new Dimension(400, 100));

        jPanelButtons.add(jButtonDelete); jPanelButtons.add(jButtonOK); jPanelButtons.add(jButtonHelp);
        this.getContentPane().add(jPanelButtons,java.awt.BorderLayout.SOUTH);
        this.getContentPane().add(jPanelList,java.awt.BorderLayout.NORTH);
        jPanelList.add(jScrollPane1, java.awt.BorderLayout.CENTER); 
        jScrollPane2.getViewport().add(jTextAreaDescription);
        jScrollPane1.getViewport().add(jListNames); 
        jPanelDescription.add(jScrollPane2, java.awt.BorderLayout.SOUTH);
        jPanelDescription.add(jPanelCheckBoxes, java.awt.BorderLayout.CENTER); 
        jPanelDescription.add(jPanelSimDur, java.awt.BorderLayout.NORTH); 
        jPanelSimDur.add(jLabelSimDur); jPanelSimDur.add(jTextFieldSimDur);
            
        jPanelSimDur.add(jLabelMs); 
        
        if (GeneralUtils.includeParallelFunc()) jLabelMs.setText("Parallel configuration: ");
        
        if (GeneralUtils.includeParallelFunc()) jPanelSimDur.add(jLabelMpiConf);
        
        if(GeneralUtils.includeParallelFunc())
            jPanelSimDur.add(jComboBoxMpiConfs);
        
        if(GeneralUtils.includeParallelFunc())
            jPanelSimDur.add(jButtonInfo);
        
        jListNames.addListSelectionListener(this);
    }

    private void resizeCheckBox(JCheckBox newCB, int totalCBs)
    {
        if (totalCBs < 30)
        {
            Float fontsize = new Float(12);
            newCB.setFont(newCB.getFont().deriveFont(fontsize));
        }
        else if (totalCBs < 60)
        {
            Float fontsize = new Float(10);
            newCB.setFont(newCB.getFont().deriveFont(fontsize));
        }
        else
        {
            Float fontsize = new Float(8);
            newCB.setFont(newCB.getFont().deriveFont(fontsize));
        }
    }

    private void refresh()
    {
        logger.logComment("<<< Refreshing view on SimConfigManager");
        listModelNames.removeAllElements();

        logger.logComment("List cleared");
        ArrayList<SimConfig> simConfigs = simConfigInfo.getAllSimConfigs();

        logger.logComment("Populating the list");
        for (int i = 0; i < simConfigs.size(); i++)
        {
            SimConfig simConfig = simConfigs.get(i);
            logger.logComment("Adding "+ simConfig);
            listModelNames.add(i, simConfig.getName());
        }

        this.jPanelCheckBoxes.removeAll();
        jPanelCheckBoxes.setLayout(new GridLayout(4,1));
        
        
        
        
        jComboBoxMpiConfs.removeAll();
        ArrayList<MpiConfiguration> mcs = GeneralProperties.getMpiSettings().getMpiConfigurations();
        
        for(MpiConfiguration mc: mcs)
        {
            jComboBoxMpiConfs.addItem(mc);
            logger.logComment("Adding: "+ mc);
        }

        jComboBoxMpiConfs.addItemListener(this);
        
        
        

        logger.logComment("Selecting cell groups");

        ArrayList<String> cellGroups = project.cellGroupsInfo.getAllCellGroupNames();
        //JPanel jPanelCellGroups = new JPanel();
        JPanel jPanelCellGroupsCB = new JPanel();
        jPanelCellGroupsCB.setBorder(BorderFactory.createTitledBorder("Cell Groups included in this simulation configuration"));
        
        //jPanelCellGroups.setMaximumSize(new Dimension(500,500));
        //jPanelCellGroups.setLayout(new GridLayout(cellGroups.size()+1,1));

        //jPanelCellGroups.add(new Label("Cell Groups included in this simulation configuration: "));
        //jPanelCellGroups.add( jPanelCellGroupsCB);

        logger.logComment("cellGroups.size(): "+cellGroups.size());

        for (int i = 0; i < cellGroups.size(); i++)
        {
            String nextCellGroup = cellGroups.get(i);
            JCheckBox newCB = new JCheckBox(nextCellGroup);

            resizeCheckBox(newCB, cellGroups.size());
            
            jPanelCellGroupsCB.add(newCB);
            cellGroupCheckBoxes.put(nextCellGroup, newCB);

            newCB.addItemListener(this);


            String cellType = project.cellGroupsInfo.getCellType(nextCellGroup);
            CellPackingAdapter adapter = project.cellGroupsInfo.getCellPackingAdapter(nextCellGroup);
            String region = project.cellGroupsInfo.getRegionName(nextCellGroup);

            String info = "<html>Cell Group: "+ nextCellGroup + "<br>"
                + "Cell Type: "+ cellType + "<br>"
                + "Region: " + region + "<br>"
                + "Packing: "+ adapter.toString()+"</html>";

            newCB.setToolTipText(info);

        }
        jPanelCheckBoxes.add(jPanelCellGroupsCB);

        Vector<String> netConns = project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames();
        Vector<String> aaNetConns = project.volBasedConnsInfo.getAllAAConnNames();
        netConns.addAll(aaNetConns);

        JPanel jPanelNetConns = new JPanel();
        //jPanelNetConns.setMaximumSize(new Dimension(500,500));
        //jPanelNetConns.add(new Label("Network Connections included in this simulation configuration: "));
        jPanelNetConns.setBorder(BorderFactory.createTitledBorder("Network Connections included in this simulation configuration"));
        
        if (netConns.size() > 0)
        {

        for (int i = 0; i < netConns.size(); i++)
        {
            String name = netConns.elementAt(i);
            JCheckBox newCB = new JCheckBox(name);

            resizeCheckBox(newCB, netConns.size());
            
            jPanelNetConns.add(newCB);
            netConnCheckBoxes.put(name, newCB);
            String source = null;
            String target = null;
            String synInfo = null;

            if (aaNetConns.contains(name))
            {
                source = project.volBasedConnsInfo.getSourceCellGroup(name);
                target = project.volBasedConnsInfo.getTargetCellGroup(name);

                synInfo = project.volBasedConnsInfo.getSynapseList(name).toString();
            }
            else
            {
                source = project.morphNetworkConnectionsInfo.getSourceCellGroup(name);
                target = project.morphNetworkConnectionsInfo.getTargetCellGroup(name);

                //synInfo = project.simpleNetworkConnectionsInfo.getSynapseProperties(name).toString();
                synInfo = project.morphNetworkConnectionsInfo.getSynapseList(name).toString();
            }
            String info = "<html>Network Connection: "+ name + "<br>"
                + "Source: "+ source + "<br>"
                + "Target: "+ target + "<br>"
                + "Synapse: "+ synInfo + "<br>"
                +"</html>";

            newCB.setToolTipText(info);
            
            
            newCB.addItemListener(this);

        }
        jPanelCheckBoxes.add(jPanelNetConns);
        }
        else
        {
            jPanelCheckBoxes.setLayout(new GridLayout(3,1));
        }
        
        Vector inputs = project.elecInputInfo.getAllStimRefs();
        JPanel jPanelInputs = new JPanel();
        //jPanelInputs.setMaximumSize(new Dimension(500,500));
        //jPanelInputs.add(new Label("Electrical inputs included in this simulation configuration: "));
        jPanelInputs.setBorder(BorderFactory.createTitledBorder("Electrical inputs included in this simulation configuration"));

        for (int i = 0; i < inputs.size(); i++)
        {
            String name = (String)inputs.elementAt(i);
            JCheckBox newCB = new JCheckBox(name);

            resizeCheckBox(newCB, inputs.size());

            
            jPanelInputs.add(newCB);
            inputCheckBoxes.put(name, newCB);

            newCB.addItemListener(this);

            StimulationSettings stim = project.elecInputInfo.getStim(name);
            //project.elecInputInfo.get(name);

            String info = "<html>Electrical Input: " + name + "<br>"
                + "Cell Group: " + stim.getCellGroup() + "<br>"
                + "Info: " + stim.toString() + "<br>" + "</html>";

            newCB.setToolTipText(info);


        }
        jPanelCheckBoxes.add(jPanelInputs);



        Vector plots = project.simPlotInfo.getAllSimPlotRefs();
        JPanel jPanelPlots = new JPanel();

        if (plots.size()>6 || inputs.size()>6 || netConns.size()>6 || cellGroups.size()>6)
        {
            // Enlarges all...
            jPanelPlots.setMinimumSize(new Dimension(500,260));
            jPanelPlots.setPreferredSize(new Dimension(500,180));
        }

        //jPanelPlots.setMaximumSize(new Dimension(500,500));
        //jPanelPlots.add(new Label("Plots included in this simulation configuration: "));
        jPanelPlots.setBorder(BorderFactory.createTitledBorder("Plots included in this simulation configuration"));

        for (int i = 0; i < plots.size(); i++)
        {
            String name = (String)plots.elementAt(i);
            JCheckBox newCB = new JCheckBox(name);

            resizeCheckBox(newCB, plots.size());
            

            jPanelPlots.add(newCB);
            plotCheckBoxes.put(name, newCB);

            newCB.addItemListener(this);

            SimPlot simPlot = project.simPlotInfo.getSimPlot(name);
       //project.elecInputInfo.get(name);

       String info = "<html>Plot: " + name + "<br>"
           + "Cell Group: " + simPlot.getCellGroup() + "<br>"
           + "Cell Number: " + simPlot.getCellNumber() + "<br>"
           + "Segment: " + simPlot.getSegmentId()+ "<br>"
           + "To plot: " + simPlot.getValuePlotted()+"<br>"
            + simPlot.getPlotAndOrSave()+"</html>";

       newCB.setToolTipText(info);


        }
        jPanelCheckBoxes.add(jPanelPlots);

        this.expandToScreen();

        logger.logComment(">>> Done refreshing view on SimConfigManager");

    }


    public void insertUpdate(DocumentEvent e)
    {
        logger.logComment("***  DocumentEvent: " + e);

        if (!updatingGUI) updateSimConfigWithGUIVals();

        project.markProjectAsEdited();

    }

    public void removeUpdate(DocumentEvent e)
    {
        logger.logComment("***  DocumentEvent: " + e);

        if (!updatingGUI) updateSimConfigWithGUIVals();

        project.markProjectAsEdited();

    }

    public void changedUpdate(DocumentEvent e)
    {
        logger.logComment("***  DocumentEvent: " + e);

        if (!updatingGUI) updateSimConfigWithGUIVals();

        project.markProjectAsEdited();

    }


    public void itemStateChanged(ItemEvent e)
    {
        logger.logComment("***  ItemEvent: " + e);

        if (!updatingGUI) updateSimConfigWithGUIVals();

        project.markProjectAsEdited();

    }

    public void updateSimConfigWithGUIVals()
    {
        logger.logComment("..........     Updating the selected sim config...");

        SimConfig simConfig = getSelectedSimConfig();
        if (simConfig==null)
        {
            logger.logError("Selection: "+this.jListNames.getSelectedValue()+" is null");
        }
        else
        {
            if (jTextFieldSimDur.getText().length()>0)
            {
                try
                {
                    float dur = Float.parseFloat(this.jTextFieldSimDur.getText());
                    simConfig.setSimDuration(dur);
                }
                catch (NumberFormatException nfe)
                {
                    GuiUtils.showErrorMessage(logger, "Please enter a valid time for the simulation duration", nfe, this);
                    return;
                }
            }

            logger.logComment("Before: "+simConfig.getMpiConf());
            
            simConfig.setMpiConf((MpiConfiguration)jComboBoxMpiConfs.getSelectedItem());

            logger.logComment("After: "+simConfig.getMpiConf());
            
            simConfig.getCellGroups();
            simConfig.getNetConns();
            simConfig.getInputs();

            logger.logComment("Selected sim config: " + simConfig.toString());
            logger.logComment("Cell groups in sim config before: " + simConfig.getCellGroups());

            simConfig.getCellGroups().clear();

            Enumeration<String> allCellGroups = this.cellGroupCheckBoxes.keys();

            while (allCellGroups.hasMoreElements())
            {
                String nextCellGroup = allCellGroups.nextElement();
                JCheckBox nextCB = cellGroupCheckBoxes.get(nextCellGroup);


                logger.logComment("Checking cell group: " + nextCellGroup + ", selected: " + nextCB.isSelected());
                if (nextCB.isSelected())
                {
                    simConfig.getCellGroups().add(nextCellGroup);
                }
                else
                {
                    simConfig.getCellGroups().remove(nextCellGroup);
                }
            }

            logger.logComment("Cell groups in sim config after: " + simConfig.getCellGroups());

            simConfig.getNetConns().clear();

            Enumeration<String> allNetConns = this.netConnCheckBoxes.keys();

            while (allNetConns.hasMoreElements())
            {
                String next = allNetConns.nextElement();
                JCheckBox nextCB = netConnCheckBoxes.get(next);

                logger.logComment("Checking: " + next + ", selected: " + nextCB.isSelected());
                if (nextCB.isSelected())
                {
                    simConfig.getNetConns().add(next);
                }
                else
                {
                    simConfig.getNetConns().remove(next);
                }
            }

            simConfig.getInputs().clear();

            Enumeration<String> allInputs = this.inputCheckBoxes.keys();

            while (allInputs.hasMoreElements())
            {
                String next = allInputs.nextElement();
                JCheckBox nextCB = inputCheckBoxes.get(next);

                logger.logComment("Checking: " + next + ", selected: " + nextCB.isSelected());
                if (nextCB.isSelected())
                {
                    simConfig.getInputs().add(next);
                }
                else
                {
                    simConfig.getInputs().remove(next);
                }
            }

            simConfig.getPlots().clear();

            Enumeration<String> allPlots = this.plotCheckBoxes.keys();

            while (allPlots.hasMoreElements())
            {
                String next = allPlots.nextElement();
                JCheckBox nextCB = plotCheckBoxes.get(next);

                logger.logComment("Checking: " + next + ", selected: " + nextCB.isSelected());
                if (nextCB.isSelected())
                {
                    simConfig.getPlots().add(next);
                }
                else
                {
                    simConfig.getPlots().remove(next);
                }
            }
        }



    }

    private SimConfig getSelectedSimConfig()
    {
        String selected = (String)this.jListNames.getSelectedValue();

        logger.logComment("Sim config name selected: "+ selected);

        ArrayList<SimConfig> simConfigs = simConfigInfo.getAllSimConfigs();

        for (int j = 0; j < simConfigs.size();j++)
        {
            SimConfig simConfig = simConfigs.get(j);
            if (simConfig.getName().equals(selected))

            return simConfig;
        }
        return null;

    }

    public void valueChanged(ListSelectionEvent e)
    {

        logger.logComment("***  Value changed: " + e);
        SimConfig simConfig = getSelectedSimConfig();

        if (simConfig==null)
        {
            logger.logError("Selection: "+this.jListNames.getSelectedValue()+" is null");
        }
        else
        {
            logger.logComment("valueChanged with Selected : " + simConfig.toString());

            logger.logComment("Cell groups in sim config: " + simConfig.getCellGroups());

            updatingGUI = true;

            this.jTextAreaDescription.setText(simConfig.getDescription());

            if (simConfig.getSimDuration() == 0)
            {
                logger.logComment("No sim dur in sim config...");
                simConfig.setSimDuration(project.simulationParameters.getDuration());
            }
            this.jTextFieldSimDur.setText(simConfig.getSimDuration()+"");
            
            logger.logComment("Before selected: "+jComboBoxMpiConfs.getSelectedItem());

            for (int i=0;i<jComboBoxMpiConfs.getItemCount();i++)
            {
                if (((MpiConfiguration)jComboBoxMpiConfs.getItemAt(i)).getName().equals(simConfig.getMpiConf().getName()))
                {
                    jComboBoxMpiConfs.setSelectedIndex(i);
                }
            }
            
            logger.logComment("After selected: "+jComboBoxMpiConfs.getSelectedItem());
            

            ArrayList<String> incCellGroups = simConfig.getCellGroups();
            Enumeration<String> allCellGroups = this.cellGroupCheckBoxes.keys();

            while (allCellGroups.hasMoreElements())
            {
                String nextCellGroup = allCellGroups.nextElement();
                JCheckBox nextCB = cellGroupCheckBoxes.get(nextCellGroup);
                nextCB.setSelected(incCellGroups.contains(nextCellGroup));
                logger.logComment("nextCellGroup: " + nextCellGroup + ", in incCellGroups: "
                                  + incCellGroups.contains(nextCellGroup));
            }

            ArrayList<String> incNetConns = simConfig.getNetConns();
            Enumeration<String> allNetConns = this.netConnCheckBoxes.keys();

            while (allNetConns.hasMoreElements())
            {
                String next = allNetConns.nextElement();
                JCheckBox nextCB = netConnCheckBoxes.get(next);
                nextCB.setSelected(incNetConns.contains(next));
                logger.logComment("next: " + next + ", in incNetConns: "
                                  + incNetConns.contains(next));
            }

            ArrayList<String> incInputs = simConfig.getInputs();
            Enumeration<String> allInputs = this.inputCheckBoxes.keys();

            while (allInputs.hasMoreElements())
            {
                String next = allInputs.nextElement();
                JCheckBox nextCB = inputCheckBoxes.get(next);
                nextCB.setSelected(incInputs.contains(next));
                logger.logComment("next: " + next + ", in incInputs: "
                                  + incInputs.contains(next));
            }

            ArrayList<String> incPlots = simConfig.getPlots();
            Enumeration<String> allPlots = this.plotCheckBoxes.keys();

            while (allPlots.hasMoreElements())
            {
                String next = allPlots.nextElement();
                JCheckBox nextCB = plotCheckBoxes.get(next);
                nextCB.setSelected(incPlots.contains(next));
                logger.logComment("next: " + next + ", in incPlots: "
                                  + incPlots.contains(next));
            }

            updatingGUI = false;
        }
    }
    

    public void jButtonOK_actionPerformed(ActionEvent e)
    {
        this.dispose();
    }
    
    public void jButtonInfo_actionPerformed(ActionEvent e)
    {
        SimConfig simConfig = getSelectedSimConfig();
        MpiConfiguration mpic = simConfig.getMpiConf();
        GuiUtils.showInfoMessage(logger, "Information on Parallel Configuration: "+mpic, mpic.toLongString(), this);
    }

    public void jButtonDelete_actionPerformed(ActionEvent e)
    {
        String selected = (String)this.jListNames.getSelectedValue();

        if (selected.equals(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME))
        {
            GuiUtils.showErrorMessage(logger, "Cannot delete that Simulation configuration, as it is the default for the project."
                +"\nHowever, the description can be altered to reflect how it is used in the project.", null, this);
            return;
        }

        int selIndex = jListNames.getSelectedIndex();

        ArrayList<SimConfig> simConfigs = simConfigInfo.getAllSimConfigs();


        for (int i = 0; i < simConfigs.size(); i++)
        {
            SimConfig simConfig = simConfigs.get(i);
            if (simConfig.getName().equals(selected))
            {
                simConfigInfo.remove(simConfig);
                this.refresh();

                this.jListNames.setSelectedIndex(selIndex>0 ? selIndex-1 : 0);
            }
        }

        project.markProjectAsEdited();
    }

    public void jButtonEditDesc_actionPerformed(ActionEvent e)
    {
        String selected = (String)this.jListNames.getSelectedValue();
        int selIndex = jListNames.getSelectedIndex();

        ArrayList<SimConfig> simConfigs = simConfigInfo.getAllSimConfigs();

        SimConfig selSimConfig = null;

        for (int i = 0; i < simConfigs.size(); i++)
        {
            SimConfig simConfig = simConfigs.get(i);
            if (simConfig.getName().equals(selected)) selSimConfig = simConfig;
        }

        if (selSimConfig!=null)
        {
            String inst = "Description of the Simulation configuration: "+ selSimConfig.getName();

            SimpleTextInput sti = SimpleTextInput.showString(selSimConfig.getDescription(),
                                                             inst,
                                                             12,
                                                             false,
                                                             false,
                                                             .4f,
                                                             this);

            String newDesc = sti.getString();

            //String newDesc = JOptionPane.showInputDialog(this, inst, selSimConfig.getDescription());
            selSimConfig.setDescription(newDesc);
            refresh();
            this.jListNames.setSelectedIndex(selIndex);
        }

        project.markProjectAsEdited();
    }

    public void jButtonAdd_actionPerformed(ActionEvent e)
    {
        String newName = null;
        Boolean NOT_SELECTED = true;
        Boolean INST_SET = false;
        ArrayList currentNames = simConfigInfo.getAllSimConfigNames();

        String inst = "Please enter the name of the new Simulation configuration";
     // while(newName == null)
     // {
     //     newName = JOptionPane.showInputDialog(this, inst, "NewSimConfig");
     //     if (currentNames.contains(newName))
     //     {
     //         newName = null;
     //         inst = "That simulation configuration name is already used.\n"+inst;
     //     }
     // }
        while (NOT_SELECTED)
        {
            newName = JOptionPane.showInputDialog(this, inst, "NewSimConfig");
            if (newName == null)
            {
                return;
            }
            if (currentNames.contains(newName))
            {
                newName = null;
                if (!INST_SET)
                {
                    inst = "That simulation configuration name is already used.\n"+inst;
                    INST_SET = true;
                }
            }    
            else
            {
                NOT_SELECTED = false;
            }
        }

        inst = "Please enter the description of the Simulation configuration: "+ newName;
        String newDesc = JOptionPane.showInputDialog(this, inst, "");
        SimConfig newSimConfig = new SimConfig(newName, newDesc);

        simConfigInfo.add(newSimConfig);
        //this.jListNames.setSelectedValue(newSimConfig.getName(), true);

        logger.logComment("Selected value: "+ jListNames.getSelectedValue());

        this.refresh();

        project.markProjectAsEdited();
    }

    public void jButtonHelp_actionPerformed(ActionEvent e)
    {

        HelpFrame.showGlossaryItem(SIM_CONFIG_HELP);

    }


    public void setSelectedSimConfig(String simConfigName)
    {
        this.jListNames.setSelectedValue(simConfigName, true);
    }
    
    
    public void expandToScreen()
    {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            //Dimension frameSize = frame.getSize();
            
            float fraction = 0.9f;
            
            this.setSize((int)(screenSize.width * fraction), (int)(screenSize.height * fraction));
            
            this.setLocation( (screenSize.width - this.getWidth()) / 2, (screenSize.height - this.getHeight()) / 2);
    }



    public static void main(String[] args)
    {
        try
        {
            //Project proj = Project.loadProject(new File("examples/Ex5-Networks/Ex5-Networks.neuro.xml"), null);
            //Project proj = Project.loadProject(new File("osb/cerebral_cortex/neocortical_pyramidal_neuron/L5bPyrCellHayEtAl2011/neuroConstruct/L5bPyrCellHayEtAl2011.ncx"), null);
            Project proj = Project.loadProject(new File("../neuroConstructSVN/models/Parallel/Parallel.ncx"), null);
            SimConfigManager frame = new SimConfigManager(proj.simConfigInfo, null, proj);

            String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();

            UIManager.setLookAndFeel(favouredLookAndFeel);

            frame.expandToScreen();
/*
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = frame.getSize();

            if (frameSize.height > screenSize.height)
                frameSize.height = screenSize.height;

            if (frameSize.width > screenSize.width)
                frameSize.width = screenSize.width;

            frame.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);*/

            frame.setVisible(true);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }


}
