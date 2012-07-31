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
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.ParameterisedGroup.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.equation.Argument;
import ucl.physiol.neuroconstruct.utils.equation.EquationException;
import ucl.physiol.neuroconstruct.utils.equation.Variable;
import ucl.physiol.neuroconstruct.utils.units.*;

/**
 * Dialog for editing which channel mechanisms are associated with which groups
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class EditGroupCellDensMechAssociations extends JDialog implements ListSelectionListener
{
    ClassLogger logger = new ClassLogger("EditGroupCellDensMechAssociations");
    public boolean cancelled = false;

    protected boolean standalone = false;

    private DefaultListModel listModelGroupsIn = new DefaultListModel();
    private DefaultListModel listModelGroupsOut = new DefaultListModel();

    String defaultMechSelection = "-- Channel Mechanisms: --";
    String spacing = " ";
    String pointProcessSelection = "-- Point processes: --";
    String passivePropsSelection = "-- Passive properties: --";
    String specCapSelection = "Specific Capacitance";
    String specAxResSelection = "Specific Axial Resistance";
    
    //String varMechs = "-- Variable mechanism parameters --";

    String sbmlSelection = "-- SBML Mechanisms: --";

    String extraMechSelection = "-- Other mechanisms: --";
    String apPropVelSelection = "Action Potential propagation speed";
    String ionPropsSelection = "Ion Properties";

    Frame myParent = null;

    Project project = null;

    String mechType = null;

    //UpdateOneCell updateInterface = null;

    Cell myCell = null;

    JPanel jPanelMain = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JPanel jPanelSelectMech = new JPanel();
    JPanel jPanelLists = new JPanel();
    JLabel jLabelSelect = new JLabel();
    JComboBox jComboBoxMechNames = new JComboBox();
    JList jListGroupsOut = new JList(listModelGroupsOut);
    JList jListGroupsIn = new JList(listModelGroupsIn);

    JScrollPane scrollPaneSectionsOut = new JScrollPane(jListGroupsOut);
    JScrollPane scrollPaneSectionsIn = new JScrollPane(jListGroupsIn);

    //JViewport viewportSectionsOut = scrollPaneSectionsOut.getViewport();
    //JViewport viewportSectionsIn = scrollPaneSectionsIn.getViewport();

    JPanel jPanelGroupsIn = new JPanel();
    JPanel jPanelSwitch = new JPanel();
    JPanel jPanelGroupsOut = new JPanel();
    JButton jButtonAdd = new JButton();
    JButton jButtonRemove = new JButton();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel jLabelGroupsOut = new JLabel();
    BorderLayout borderLayout2 = new BorderLayout();
    Border border1;
    Border border2;
    JLabel jLabelGroupsIn = new JLabel();
    BorderLayout borderLayout3 = new BorderLayout();
    Border border3;
    Border border4;
    GridBagLayout gridBagLayout2 = new GridBagLayout();

    JButton jButtonEditValue = new JButton();
    JButton jButtonEditExtraValue = new JButton();

    JButton jButtonOK = new JButton();
    GridBagLayout gridBagLayout3 = new GridBagLayout();

    private EditGroupCellDensMechAssociations()
    {

    }

    public EditGroupCellDensMechAssociations(Cell cell,
                                         Frame owner,
                                         String mechType,
                                         Project project) throws HeadlessException
    {
        super(owner, "Edit Group to "+mechType+ " associations for "+cell.getInstanceName(), true);
        
        this.logger.setThisClassVerbose(false);

        this.project = project;

        this.mechType = mechType;

        myCell = cell;

        ArrayList<ChannelMechanism> mechs = cell.getAllUniformChanMechs(false);

        // create copy...
        
        ArrayList<String> mechListTemp = project.cellMechanismInfo.getAllCellMechanismNames();
        ArrayList<String> mechList = new ArrayList<String>();
        mechList.addAll(mechListTemp);

        for (int i = 0; i < mechs.size(); i++)
        {
            ChannelMechanism nextMech = mechs.get(i);

            if (!mechList.contains(nextMech.getName()))
            {
                logger.logComment("Latest chan mechs: "+ cell.getAllUniformChanMechs(false));

                int result = JOptionPane.showConfirmDialog(this, "The channel mechanism " +
                                                           nextMech +
                                                           " is present on this cell, but there is no corresponding "
                                                           +
                                                           "\nCell Mech in the project. Delete the channel mechanism from the cell?",
                                                           "Warning",
                                                           JOptionPane.YES_NO_OPTION);

                if (result==JOptionPane.YES_OPTION)
                {

                    Vector groups = cell.getGroupsWithChanMech(nextMech);

                    logger.logComment("Deleting "+nextMech + " from: "+ groups);

                    for (int k = 0; k < groups.size(); k++)
                    {
                        String nextGroup = (String)groups.elementAt(k);
                        cell.disassociateGroupFromChanMech(nextGroup, nextMech);

                    }

                    logger.logComment("Chan mechs: "+ cell.getAllUniformChanMechs(false));
                }
                else logger.logComment("Leaving it alone...");

                logger.logComment("Details: "+ CellTopologyHelper.printDetails(cell, null));
            }
        }

        myParent = owner;
        try
        {
            jbInit();
            initialiseOptions();
            pack();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    public void setSelected(String chanMech)
    {
        this.jComboBoxMechNames.setSelectedItem(chanMech);
    }

    private void jbInit() throws Exception
    {
        border1 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white,
            Color.white, new Color(124, 124, 124), new Color(178, 178, 178)),
                                                     BorderFactory.createEmptyBorder(3, 3, 3, 3));
        border2 = BorderFactory.createEmptyBorder(5,5,5,5);
        border3 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white,
            Color.white, new Color(124, 124, 124), new Color(178, 178, 178)),
                                                     BorderFactory.createEmptyBorder(3, 3, 3, 3));
        border4 = BorderFactory.createEmptyBorder(5,5,5,5);
        jPanelMain.setLayout(gridBagLayout3);
        jLabelSelect.setText("Please select:");
        jPanelLists.setLayout(gridBagLayout2);
        jPanelGroupsOut.setBorder(border2);

        //Dimension groupsDim = new Dimension(160, 119);
        Dimension groupsDim = new Dimension(200, 200);

        jPanelGroupsOut.setMaximumSize(groupsDim);
        jPanelGroupsOut.setMinimumSize(groupsDim);
        jPanelGroupsOut.setPreferredSize(groupsDim);
        jPanelGroupsOut.setLayout(borderLayout2);
        
        
        jPanelGroupsIn.setBorder(border4);
        jPanelGroupsIn.setMaximumSize(groupsDim);
        jPanelGroupsIn.setMinimumSize(groupsDim);
        jPanelGroupsIn.setPreferredSize(groupsDim);
        jPanelGroupsIn.setLayout(borderLayout3);
        
        
        jButtonAdd.setText(">");
        jButtonAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonAdd_actionPerformed(e);
            }
        });
        jButtonRemove.setText("<");
        jButtonRemove.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonRemove_actionPerformed(e);
            }
        });
        jPanelSwitch.setLayout(gridBagLayout1);
        jLabelGroupsOut.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelGroupsOut.setText("Groups without " + mechType);
        jListGroupsOut.setBorder(border1);
        jLabelGroupsIn.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelGroupsIn.setText("Groups with " + mechType);
        jListGroupsIn.setBorder(border3);
        //viewportSectionsOut.setScrollMode(1);
        
        scrollPaneSectionsOut.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPaneSectionsIn.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        jComboBoxMechNames.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxMechNames_itemStateChanged(e);
            }
        });

        jButtonOK.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOK_actionPerformed(e);
            }
        });

        jButtonEditValue.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonEditValue_actionPerformed(e);
            }
        });

        jButtonEditExtraValue.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonEditExtraValue_actionPerformed(e);
            }
        });





        jPanelSwitch.setBorder(null);
        jPanelSwitch.setMinimumSize(new Dimension(30, 30));
        jPanelSwitch.setPreferredSize(new Dimension(30, 30));

        Dimension mainDim = new Dimension(600, 410);
        jPanelMain.setMaximumSize(mainDim);
        jPanelMain.setMinimumSize(mainDim);
        jPanelMain.setPreferredSize(mainDim);

        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
        jPanelMain.add(jPanelSelectMech,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 300, 0));
        jPanelMain.add(jPanelLists, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                                                           , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                           new Insets(0, 0, 0, 0), 0, 0));
        jPanelLists.add(jPanelGroupsOut, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                                                  , GridBagConstraints.CENTER,
                                                                  GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0),
                                                                  70, 0));
        jPanelLists.setBorder(BorderFactory.createEmptyBorder(0, 8,0,8));

        jPanelLists.add(jPanelSwitch,   new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 35, 254));
        jPanelSwitch.add(jButtonAdd, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                            , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                            new Insets(0, 0, 12, 0), 0, 0));
        
        jPanelLists.add(jPanelGroupsIn, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
                                                                 , GridBagConstraints.CENTER,
                                                                 GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 1),
                                                                 70, 0));
        
        jPanelMain.add(jPanelButtons, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
                                                             , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                             new Insets(0, 0, 1, 0), 100, 0));
        jPanelButtons.add(jButtonEditValue, null);
        jPanelButtons.add(jButtonEditExtraValue, null);
        
        jPanelButtons.add(jButtonOK, null);
        jButtonOK.setText("OK");
        jButtonEditValue.setText("Edit value");
        jButtonEditExtraValue.setText("Edit extra parameters");

        jButtonEditValue.setEnabled(false);
        jPanelSelectMech.add(jLabelSelect, null);
        jPanelSelectMech.add(jComboBoxMechNames, null);

        jPanelSwitch.add(jButtonRemove, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                                               , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                               new Insets(0, 0, 0, 1), 0, 0));

        jPanelGroupsOut.add(jLabelGroupsOut, BorderLayout.NORTH);
        jPanelGroupsOut.add(scrollPaneSectionsOut, BorderLayout.CENTER);
        //viewportSectionsOut.setView(jListSectionsOut);

        jPanelGroupsIn.add(jLabelGroupsIn, BorderLayout.NORTH);
        jPanelGroupsIn.add(scrollPaneSectionsIn, BorderLayout.CENTER);
        //viewportSectionsIn.setView(jListSectionsIn);

        jListGroupsIn.addListSelectionListener(this);
        jListGroupsOut.addListSelectionListener(this);

    }

    private void initialiseOptions()
    {
        jComboBoxMechNames.removeAllItems();
        
        jComboBoxMechNames.addItem(defaultMechSelection);

        Vector<String> mechList = project.cellMechanismInfo.getChanMechsAndIonConcs();

        for (int i = 0; i < mechList.size(); i++)
        {
            jComboBoxMechNames.addItem(mechList.elementAt(i));
        }
        Vector<String> pps = project.cellMechanismInfo.getPointProcessess();
        
        if (pps.size()>0)
        {
            jComboBoxMechNames.addItem(pointProcessSelection);
            for(String pp: pps)
            {
                jComboBoxMechNames.addItem(pp);
            }
        }

        jComboBoxMechNames.addItem(spacing);
        jComboBoxMechNames.addItem(this.passivePropsSelection);
        jComboBoxMechNames.addItem(this.specCapSelection);
        jComboBoxMechNames.addItem(this.specAxResSelection);
        jComboBoxMechNames.addItem(spacing);
        
        /*
        if (myCell.getParameterisedGroups().size()>0)
        {
            jComboBoxMechNames.addItem(varMechs);
            Hashtable<ChannelMechanism, Vector<String>> cmVsGrp = myCell.getChanMechsVsGroups();
            Enumeration<ChannelMechanism> cms = cmVsGrp.keys();
            
            
            while(cms.hasMoreElements())                
            {
                ChannelMechanism chanMech = cms.nextElement();
                 //jComboBoxMechNames.addItem(mechList.elementAt(i));
                //
                Vector<String> groups = cmVsGrp.get(chanMech);
                
                CellMechanism cellMech = project.cellMechanismInfo.getCellMechanism(chanMech.getName());
                
                for(String group:groups)
                {
                    if (cellMech.isChannelMechanism())
                    {
                        jComboBoxMechNames.addItem("gmax of: "+chanMech.getName()+" on group: "+group);
                    }
                }
            }
        
            jComboBoxMechNames.addItem(spacing);
        }*/
        

        jComboBoxMechNames.addItem(this.sbmlSelection);

        Vector<String> sbmlMechList = project.cellMechanismInfo.getSBMLMechs();

        for (int i = 0; i < sbmlMechList.size(); i++)
        {
            jComboBoxMechNames.addItem(sbmlMechList.elementAt(i));
        }

        jComboBoxMechNames.addItem(this.extraMechSelection);
        jComboBoxMechNames.addItem(this.apPropVelSelection);
        jComboBoxMechNames.addItem(this.ionPropsSelection);


        logger.logComment("Finished initialising...");
    }

    public void valueChanged(ListSelectionEvent e)
    {
        logger.logComment("Value changed: " + e);

        if (e.getSource().equals(jListGroupsIn))
        {
            if (this.jListGroupsIn.getSelectedValues().length>0)
            {
                jButtonEditValue.setEnabled(true);
                jButtonEditExtraValue.setEnabled(true);
            }
            logger.logComment("GroupsIn change: " + e.getFirstIndex());

            this.jButtonRemove.setEnabled(true);
            this.jButtonAdd.setEnabled(false);

            jListGroupsOut.setSelectedIndices(new int[] {});

            if (e.getValueIsAdjusting())
            {
                logger.logComment("Selected: " + e.getFirstIndex());
            }
        }
        else if (e.getSource().equals(jListGroupsOut))
        {

            jButtonEditValue.setEnabled(false);
            jButtonEditExtraValue.setEnabled(false);

            logger.logComment("SectionsOut change: " + e.getFirstIndex());
            this.jButtonRemove.setEnabled(false);
            this.jButtonAdd.setEnabled(true);

            jListGroupsIn.setSelectedIndices(new int[]{});

            if (e.getValueIsAdjusting())
            {
                logger.logComment("Selected: " + e.getFirstIndex());
            }
        }

    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel button pressed");
        cancelled = true;
        this.dispose();
        if (standalone)
            System.exit(0);
    }

    void jButtonOK_actionPerformed(ActionEvent e)
    {
        logger.logComment("OK button pressed");

        this.dispose();

        if (standalone)
        {
            logger.logComment(CellTopologyHelper.printDetails(myCell, project));
            System.exit(0);
        }

    }
    
    
    void jButtonEditExtraValue_actionPerformed(ActionEvent e)
    {
        logger.logComment("Edit extra value button pressed");
        
        String selectedMechanism = (String) jComboBoxMechNames.getSelectedItem();

         if (selectedMechanism.equals(defaultMechSelection) ||
            selectedMechanism.equals(this.extraMechSelection)||
            selectedMechanism.equals(this.sbmlSelection)||
            selectedMechanism.equals(spacing)) return;
        
        
        Object[] selected = this.jListGroupsIn.getSelectedValues();
        
        if (selected.length>1)
            GuiUtils.showErrorMessage(logger, "Please select a single mechanism/group association" , null, this);
        
        if (selectedMechanism.equals(this.apPropVelSelection) ||
            selectedMechanism.equals(this.ionPropsSelection) ||
            selectedMechanism.equals(this.specAxResSelection) ||
            selectedMechanism.equals(this.specCapSelection))
        {
            GuiUtils.showErrorMessage(logger, "No extra params can be added for that mechanism" , null, this);
            return;
        }
        
        ChannelMechanism chanMech = null;
        String sel = (String)selected[0];
        
        String groupName = sel.substring(0, sel.indexOf(" ")).trim();
        
        ArrayList<ChannelMechanism> allChans = myCell.getChanMechsForGroup(groupName);
        
        for (int k = 0; k < allChans.size(); k++)
        {
            if (allChans.get(k).getName().equals(selectedMechanism))
                chanMech = allChans.get(k);
        }

        
        ArrayList<MechParameter> oldMps = chanMech.getExtraParameters();
        
        ArrayList<MechParameter> newMps = new ArrayList<MechParameter>();
        
        for (MechParameter mp: oldMps)
        {
            String message0 = "There is a parameter: "+mp.getName()+" present, value: "+mp.getValue();
            
            
            Object[] options = new Object[]
                    {"Keep parameter with this value", "Change value" , "Delete parameter"};
                
            
            int opt = JOptionPane.showOptionDialog(this, message0, "Edit parameters", 
                                                   JOptionPane.OK_CANCEL_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE,
                                                   null,
                                                   options,
                                                   options[0]);
            
            if (opt == 0)
            {
                newMps.add(new MechParameter(mp));
            }
            else if (opt == 2)
            {
                logger.logComment("Removing MechParameter: "+ mp);
            }
            else
            {
        
                String message2 = "Please enter the new default value of parameter: "+ mp.getName();
                
                String valString = JOptionPane.showInputDialog(this, message2, mp.getValue()+"");

                float value = 0;

                try
                {
                    value = Float.parseFloat(valString);
                }
                catch (NumberFormatException ex)
                {
                    GuiUtils.showErrorMessage(logger, "Could not parse value: "+valString+" for parameter: "+mp.getName() , ex, this);
                    return;
                }
                newMps.add(new MechParameter(mp.getName(), value));
                
            }
        }
        
        
        String message1 = "Please enter the name of a new parameter, or press Cancel to keep current set";
        

        String paramName = JOptionPane.showInputDialog(this, message1, "");
        
        if (paramName!=null && paramName.length()>0)
        {
            float value = 0;

            String message2 = "Please enter the default value of parameter: "+ paramName;

            String valString = JOptionPane.showInputDialog(this, message2, value+"");

            try
            {
                value = Float.parseFloat(valString);
            }
            catch (NumberFormatException ex)
            {
                GuiUtils.showErrorMessage(logger, "Could not parse value: "+valString+" for parameter: "+paramName , ex, this);
                return;
            }


            newMps.add(new MechParameter(paramName, value));
        }
        
        chanMech.setExtraParameters(newMps);
        

        myCell.associateGroupWithChanMech(groupName, chanMech);
        
        
        this.jComboBoxMechNames.setSelectedIndex(0);
        this.jComboBoxMechNames.setSelectedItem(selectedMechanism); // refreshes lists
        
    }

    void jButtonEditValue_actionPerformed(ActionEvent e)
    {
        String selectedMechanism = (String) jComboBoxMechNames.getSelectedItem();

        if (selectedMechanism.equals(defaultMechSelection) ||
            selectedMechanism.equals(this.extraMechSelection)||
            selectedMechanism.equals(this.sbmlSelection)||
            selectedMechanism.equals(spacing)) return;

        logger.logComment("Edit value button pressed");

        //Object[] selected = this.jListGroupsIn.getSelectedValues();

        int[] selected = jListGroupsIn.getSelectedIndices();

        ArrayList<ParameterisedGroup> paramGroups = new ArrayList<ParameterisedGroup>();

        for (int i = 0; i < selected.length; i++)
        {
            String group = (String) listModelGroupsIn.elementAt(selected[i]);
            logger.logComment("Selected: "+ group);
            if (!myCell.isGroup(group))
            {
                String name = group.substring(0,group.indexOf(" "));
                for(ParameterisedGroup pg: myCell.getParameterisedGroups())
                {
                    if (name.equals(pg.getName()))
                        paramGroups.add(pg);
                }
            }
        }
        logger.logComment("paramGroups: "+ paramGroups);

        if(paramGroups.size()>1 || (paramGroups.size()>0 && selected.length>1))
        {
            GuiUtils.showWarningMessage(logger,
                                      "Warning, can only edit parameter groups for mechanisms one at a time",null);
        }
        else if(paramGroups.size() == 1)
        {
            /*
            String request = "Please enter a value for the max conductance density for the channel:\n"
                + cellMech + " placed on sections in " + groupRef + " (Units: "
                + UnitConverter.conductanceDensityUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()
                + ")";

            String inputValue
                = JOptionPane.showInputDialog(this,
                                              request,
                                              suggestedValue + "");

            if (inputValue==null || inputValue.length()==0) return -1;*/
        }
        else
        {


            for (int j = 0; j < selected.length; j++)
            {
                String next = (String)listModelGroupsIn.elementAt(selected[j]);

                logger.logComment("Changing value of: "+ next);

                String groupName = next.substring(0, next.indexOf(" ")).trim();

                logger.logComment("Item: " + selected[j] + " ("+next
                                  +") is selected, so group: " + groupName);

                if (selectedMechanism.equals(this.apPropVelSelection))
                {
                    ApPropSpeed appv = myCell.getApPropSpeedForGroup(groupName);

                    float vel = this.getApPropSpeedForGroup(appv.getSpeed());

                    appv.setSpeed(vel);
                    myCell.associateGroupWithApPropSpeed(groupName, appv);
                }
                else if (selectedMechanism.equals(this.ionPropsSelection))
                {
                    ArrayList<IonProperties> ips = myCell.getIonPropertiesForGroup(groupName);
                    for (IonProperties ip: ips)
                    {
                        IonProperties ip2 = this.getIonPropertiesForGroup(ip);
                        myCell.associateGroupWithIonProperties(groupName, ip2);
                    }

                }
                else if (selectedMechanism.equals(this.specAxResSelection))
                {
                    float specAxRes = myCell.getSpecAxResForGroup(groupName);

                    specAxRes = this.getSpecAxResForGroup(specAxRes, groupName);

                    myCell.associateGroupWithSpecAxRes(groupName, specAxRes);
                }
                else if (selectedMechanism.equals(this.specCapSelection))
                {
                    float specCap = myCell.getSpecCapForGroup(groupName);

                    specCap = this.getSpecCapForGroup(specCap, groupName);

                    myCell.associateGroupWithSpecCap(groupName, specCap);
                }
                else
                {
                    ChannelMechanism chanMech = null;
                    ArrayList<ChannelMechanism> allChans = myCell.getChanMechsForGroup(groupName);
                    for (int k = 0; k < allChans.size(); k++)
                    {
                        if (allChans.get(k).getName().equals(selectedMechanism))
                            chanMech = allChans.get(k);
                    }

                    float newVal = this.getDensForGroup(selectedMechanism, chanMech.getDensity(), "(" + groupName + ")");

                    if (newVal >=0)
                    {
                        chanMech.setDensity(newVal);
                        myCell.associateGroupWithChanMech(groupName, chanMech);
                    }
                }
            }
        }

        this.jComboBoxMechNames.setSelectedIndex(0);
        this.jComboBoxMechNames.setSelectedItem(selectedMechanism); // refreshes lists
    }


    void jComboBoxMechNames_itemStateChanged(ItemEvent e)
    {
        logger.logComment("" + e);

        this.jButtonEditValue.setEnabled(false);
        this.jButtonEditExtraValue.setEnabled(false);

        if (e.getStateChange() == ItemEvent.SELECTED)
        {
            listModelGroupsIn.clear();
            listModelGroupsOut.clear();

            String selectedMechanism = (String) jComboBoxMechNames.getSelectedItem();

            if (selectedMechanism.equals(defaultMechSelection) ||
                selectedMechanism.equals(this.extraMechSelection) ||
                selectedMechanism.equals(this.sbmlSelection) ||
                selectedMechanism.equals(this.spacing))
            {
                return;
            }
            logger.logComment("\n ----  Setting the selected Mechanism to: " + selectedMechanism);

            Vector<String> allGroups = myCell.getAllGroupNames();

            for (int j = 0; j < allGroups.size(); j++)
            {
                String nextGroup = allGroups.elementAt(j);

                logger.logComment("Considering group: "+ nextGroup);

                if (selectedMechanism.equals(this.apPropVelSelection))
                {
                    ApPropSpeed appv = myCell.getApPropSpeedForGroup(nextGroup);

                    if (appv == null)
                    {
                        if (!listModelGroupsOut.contains(nextGroup))
                             listModelGroupsOut.addElement(nextGroup);
                    }
                    else
                    {
                        listModelGroupsIn.addElement(nextGroup + " (speed: " + appv.getSpeed() + ")");
                    }

                }
                else if (selectedMechanism.equals(this.ionPropsSelection))
                {
                    ArrayList<IonProperties> ips = myCell.getIonPropertiesForGroup(nextGroup);

                    if (ips.isEmpty())
                    {
                        //...
                    }
                    else
                    {
                        for (IonProperties ip: ips)
                            listModelGroupsIn.addElement(nextGroup + " (" + ip.toString() + ")");
                    }

                    if (!listModelGroupsOut.contains(nextGroup))
                        listModelGroupsOut.addElement(nextGroup);

                }
                else if (selectedMechanism.equals(this.specCapSelection))
                {
                    float specCap = myCell.getSpecCapForGroup(nextGroup);

                    if (Float.isNaN(specCap))
                    {
                        if (!listModelGroupsOut.contains(nextGroup))
                            listModelGroupsOut.addElement(nextGroup);
                    }
                    else
                    {
                        listModelGroupsIn.addElement(nextGroup + " (specCap: " + specCap + ")");
                    }
                }
                else if (selectedMechanism.equals(this.specAxResSelection))
                {
                    float specAxRes = myCell.getSpecAxResForGroup(nextGroup);

                    if (Float.isNaN(specAxRes))
                    {
                        if (!listModelGroupsOut.contains(nextGroup))
                            listModelGroupsOut.addElement(nextGroup);
                    }
                    else
                    {
                        listModelGroupsIn.addElement(nextGroup + " (specAxRes: " + specAxRes + ")");
                    }
                }
                else
                {
                    ArrayList<ChannelMechanism> allChanMechs = myCell.getChanMechsForGroup(nextGroup);

                    logger.logComment("Looking at group: " + nextGroup
                                      + " which has chans: " + allChanMechs);

                    ChannelMechanism foundChanMech = null;

                    for (int k = 0; k < allChanMechs.size(); k++)
                    {
                        ChannelMechanism nextChanMech = allChanMechs.get(k);
                        logger.logComment("Next chan mech = " + nextChanMech);

                        if (nextChanMech.getName().equals(selectedMechanism))
                            foundChanMech = nextChanMech;

                    }

                    if (allChanMechs.size() > 0 && foundChanMech != null)
                    {
                        logger.logComment("Adding group: " + nextGroup + ", chan: " + foundChanMech +
                                          " to the in list...");
                        listModelGroupsIn.addElement(nextGroup
                                                     + " (density: "
                                                     + foundChanMech.getDensity()+foundChanMech.getExtraParamsDesc() + ")");
                    }
                    else
                    {
                        logger.logComment("Adding group: " + nextGroup + " to the out list...");
                        if (!listModelGroupsOut.contains(nextGroup))
                            listModelGroupsOut.addElement(nextGroup);
                    }
                }

            }
            
            
            for(ParameterisedGroup pg: myCell.getParameterisedGroups())
            {
                logger.logComment("Considering : "+ pg);
                if (selectedMechanism.equals(this.apPropVelSelection))
                {
                    logger.logComment("Not offering to associate param group with AP prop velocity");
                }
                else if (selectedMechanism.equals(this.ionPropsSelection))
                {
                    logger.logComment("Not YET offering to associate param group with IonProperties");
                }
                else if (selectedMechanism.equals(this.specCapSelection))
                {
                    
                    logger.logComment("Not YET offering to associate param group with spec cap");
                    
                    //if (!listModelGroupsOut.contains(pg.toShortString()))
                    //    listModelGroupsOut.addElement(pg.toShortString());
                   
                }
                else if (selectedMechanism.equals(this.specAxResSelection))
                {
                    logger.logComment("Not YET offering to associate param group with ax res");
                    
                    //if (!listModelGroupsOut.contains(pg.toShortString()))
                    //    listModelGroupsOut.addElement(pg.toShortString());
                }
                else
                {
                    Hashtable<VariableMechanism, ParameterisedGroup> varMechsVsParaGroups = myCell.getVarMechsVsParaGroups();
                    
                    boolean pgUsed = false;
                    VariableMechanism vmUsed = null;
                    
                    for(VariableMechanism vm : varMechsVsParaGroups.keySet())
                    {
                        if(vm.getName().equals(selectedMechanism))
                        {
                            logger.logComment("Selected: "+selectedMechanism+" is mech for: "+ vm);
                            ParameterisedGroup assPg = varMechsVsParaGroups.get(vm);
                            if (pg.equals(assPg))
                            {
                                pgUsed = true;
                                vmUsed = vm;
                                //listModelGroupsIn.addElement(pg.toShortString());
                            }
                            
                        }
                    }
                    if(pgUsed && vmUsed!=null)
                    {
                        listModelGroupsIn.addElement(pg.toShortString()+" ("+ vmUsed.getParam()+")");
                    }
                    else
                    {
                        if (!listModelGroupsOut.contains(pg.toShortString()))
                            listModelGroupsOut.addElement(pg.toShortString());
                    }
                }
            }
        }
    }


    private float getDensForGroup(String cellMech, float suggestedValue, String groupRef)
    {
        float dens = Float.MIN_VALUE;

        while (dens==Float.MIN_VALUE)
        {
            String request = "Please enter a value for the max conductance density for the channel:\n"
                + cellMech + " placed on sections in " + groupRef + " (Units: "
                + UnitConverter.conductanceDensityUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()
                + ")";

            String inputValue
                = JOptionPane.showInputDialog(this,
                                              request,
                                              suggestedValue + "");
            
            if (inputValue==null || inputValue.length()==0) return -1;
            
            try
            {
                dens = Float.parseFloat(inputValue);
            }
            catch (NumberFormatException ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a valid float value", ex, this);
                dens = Float.MIN_VALUE;
            }
        }
        return dens;
    }

    private float getSpecCapForGroup(float suggestedValue, String groupRef)
    {
        float sc = -1;

        while (sc<0)
        {
            String request = "Please enter the value for the specific capacitance of sections in " + groupRef + " (Units: "
                + UnitConverter.specificCapacitanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()
                + ")";

            String inputValue
                = JOptionPane.showInputDialog(this,
                                              request,
                                              suggestedValue + "");
            try
            {
                sc = Float.parseFloat(inputValue);
            }
            catch (NumberFormatException ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a valid positive float value", ex, this);
            }
        }
        return sc;
    }

    private float getSpecAxResForGroup(float suggestedValue, String groupRef)
    {
        float sar = -1;

        while (sar<0)
        {
            String request = "Please enter the value for the specific axial resistance of sections in " + groupRef + " (Units: "
                + UnitConverter.specificAxialResistanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()
                + ")";

            String inputValue
                = JOptionPane.showInputDialog(this,
                                              request,
                                              suggestedValue + "");
            try
            {
                sar = Float.parseFloat(inputValue);
            }
            catch (NumberFormatException ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a valid positive float value", ex, this);
            }
        }
        return sar;
    }




    private float getApPropSpeedForGroup(float suggestedValue)
    {
        float vel = -1;

        while (vel < 0)
        {
            String request = "Please enter the value for the propagation speed of the Action Potential\n"
                + "on sections in selected groups (Units: "
                + UnitConverter.lengthUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol() + " "
                + UnitConverter.rateUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+ ")";

            String inputValue
                = JOptionPane.showInputDialog(this,request,suggestedValue + "");

            try
            {
                vel = Float.parseFloat(inputValue);
            }
            catch (NumberFormatException ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a valid float value", ex, this);
            }
        }
        return vel;
    }


    private IonProperties getIonPropertiesForGroup(IonProperties suggested)
    {
        float val = Float.NaN;
        String name = null;

        while (Float.isNaN(val))
        {
            String request = "Please enter the name of the ion";

            name = JOptionPane.showInputDialog(this,request,suggested.getName() + "");


            request = "Please enter the value for the reversal potential of ion "+name+" \n"
                + "on sections in selected groups (Units: "
                + UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+ ")\n"
                + "    - or -\n"
                + "press Cancel to specify values for the initial internal & external concentration of this ion\n";

            String inputValue
                = JOptionPane.showInputDialog(this,request,suggested.getReversalPotential() + "");

            if (inputValue==null)
            {
                request = "Please enter the value for the initial internal concentration of ion "+name+" \n"
                + "on sections in selected groups (Units: "
                + UnitConverter.concentrationUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+")";

                String intConc
                    = JOptionPane.showInputDialog(this,request,suggested.getInternalConcentration() + "");

                request = "Please enter the value for the initial external concentration of ion "+name+" \n"
                + "on sections in selected groups (Units: "
                + UnitConverter.concentrationUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+")";

                String extConc
                    = JOptionPane.showInputDialog(this,request,suggested.getExternalConcentration() + "");

                try
                {
                    return new IonProperties(name, Float.parseFloat(intConc), Float.parseFloat(extConc));
                }
                catch (NumberFormatException ex)
                {
                    GuiUtils.showErrorMessage(logger, "Please enter a valid float value", ex, this);
                }
            }

            try
            {
                val = Float.parseFloat(inputValue);
            }
            catch (NumberFormatException ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a valid float value", ex, this);
            }
        }
        return new IonProperties(name, val);
    }


    void jButtonAdd_actionPerformed(ActionEvent e)
    {
        String selectedMech = (String) jComboBoxMechNames.getSelectedItem();

        if (selectedMech.equals(defaultMechSelection) ||
            selectedMech.equals(this.extraMechSelection)||
            selectedMech.equals(this.sbmlSelection)||
            selectedMech.equals(spacing)) return;
        
        

        if (selectedMech.equals(this.apPropVelSelection))
        {
            float vel = this.getApPropSpeedForGroup(GeneralProperties.getDefaultApPropagationVelocity());
            ApPropSpeed appv = new ApPropSpeed(vel);
            int[] selected = jListGroupsOut.getSelectedIndices();

            for (int i = 0; i < selected.length; i++)
            {
                String group = (String) listModelGroupsOut.elementAt(selected[i]);
                logger.logComment("Item: " + selected[i] + " (" + group + ") is selected...");
                
                if (!myCell.isGroup(group))
                {
                    GuiUtils.showErrorMessage(logger, "Note: cannot apply an action potential propagation speed to: "+ group, null, this);
                    return;
                }

                if (group.equals(Section.SOMA_GROUP))
                {
                    GuiUtils.showErrorMessage(logger, "Note: cannot apply an action potential propagation speed to the soma group!\n"
                                              +"This section must be explicitly modelled", null, this);
                }
                else if (group.equals(Section.ALL))
                {
                    GuiUtils.showErrorMessage(logger, "Note: cannot apply an action potential propagation speed to group: all\n"
                                              +"as this will contain the soma_group section, which must be explicitly modelled", null, this);
                }
                else
                {
                    myCell.associateGroupWithApPropSpeed(group, appv);
                }
            }
        }
        else if (selectedMech.equals(this.ionPropsSelection))
        {
            IonProperties val = this.getIonPropertiesForGroup(new IonProperties("na", 55));

            int[] selected = jListGroupsOut.getSelectedIndices();

            for (int i = 0; i < selected.length; i++)
            {
                String group = (String) listModelGroupsOut.elementAt(selected[i]);
                logger.logComment("Item: " + selected[i] + " (" + group + ") is selected...");

                if (!myCell.isGroup(group))
                {
                    GuiUtils.showErrorMessage(logger, "Note: cannot apply an IonProperties to: "+ group, null, this);
                    return;
                }
                myCell.associateGroupWithIonProperties(group, val);
                
            }
        }
        else if(selectedMech.equals(this.specAxResSelection))
        {
            int[] selected = jListGroupsOut.getSelectedIndices();

            float defaultVal = project.simulationParameters.getGlobalRa();

            for (int i = 0; i < selected.length; i++)
            {
                String group = (String) listModelGroupsOut.elementAt(selected[i]);

                float specAxRes = this.getSpecAxResForGroup(defaultVal, group);

                defaultVal = specAxRes;

                myCell.associateGroupWithSpecAxRes(group, specAxRes);
            }
        }
        else if(selectedMech.equals(this.specCapSelection))
        {
            float defaultVal = project.simulationParameters.getGlobalCm();
            int[] selected = jListGroupsOut.getSelectedIndices();

            for (int i = 0; i < selected.length; i++)
            {
                String group = (String) listModelGroupsOut.elementAt(selected[i]);

                float specCap = this.getSpecCapForGroup(defaultVal, group);

                defaultVal = specCap;

                myCell.associateGroupWithSpecCap(group, specCap);
            }
        }
        else
        {
            CellMechanism cellMech = project.cellMechanismInfo.getCellMechanism(selectedMech);

            float condDens = -1;
            
            if (cellMech.isPointProcess())
            {
                GuiUtils.showInfoMessage(logger, "Point processes added", 
                    "Note that the mechanism: "+selectedMech+" is a point process and will be added once to the 0.5 point of each of the selected sections.\n"
                    +"The max conductance density value will be ignored", this);
                condDens = 0;
            }
            
            else
            {
                int[] selected = jListGroupsOut.getSelectedIndices();
                ArrayList<ParameterisedGroup> paramGroups = new ArrayList<ParameterisedGroup>();
                
                for (int i = 0; i < selected.length; i++)
                {
                    String group = (String) listModelGroupsOut.elementAt(selected[i]);
                    if (!myCell.isGroup(group))
                    {
                        String name = group.substring(0,group.indexOf(" "));
                        for(ParameterisedGroup pg: myCell.getParameterisedGroups())
                        {
                            if (name.equals(pg.getName()))
                                paramGroups.add(pg);
                        }
                    }                
                }
                
                if(paramGroups.size()>1 || (paramGroups.size()>0 && selected.length>1))
                {
                    GuiUtils.showWarningMessage(logger,
                                              "Warning, can only associate parameter groups to mechanisms one at a time",null);
                }
                else if(paramGroups.size() == 1) 
                {
                    String group = (String) listModelGroupsOut.elementAt(selected[0]);
                    logger.logComment("Going to create variable mechanism of "+selectedMech+" for parameterised group: "+ group);
                    
                    String var  ="gmax";

                    Variable p = new Variable(paramGroups.get(0).getVariable());
                    
                    String testExpr = "1e-7*exp(-"+p.getName()+"/100)";
                    
                    
                    String expr
                        = JOptionPane.showInputDialog(this,
                                              "Please enter the expression for how the value of "+var+" changes over "+group+" as a function of "+p,
                                              testExpr);
                    
                    if (expr==null || expr.trim().length()==0)
                        return;
                    
                    
                    ArrayList<Argument> expressionArgs1 =  new ArrayList<Argument>();
                    VariableParameter vp1 = null;
                    try
                    {
                        vp1 = new VariableParameter(var, expr, p, expressionArgs1);
                    }
                    catch (EquationException ex)
                    {
                        GuiUtils.showErrorMessage(logger,
                                                  "Could not parse expression: "+expr+" in terms of "+p,
                                                  ex, null);
                    }
                    
                    VariableMechanism vm = new VariableMechanism(selectedMech, vp1);
                    myCell.associateParamGroupWithVarMech(paramGroups.get(0), vm);
                    
                    
                }
                else
                {
                
                    try
                    {
                        String suggestedValue = null;
                        if (cellMech instanceof AbstractedCellMechanism)
                        {
                            try
                            {
                                suggestedValue = ( (AbstractedCellMechanism) cellMech).getParameter(DistMembraneMechanism.COND_DENSITY)+ "";
                            }
                            catch (CellMechanismException ex)
                            {
                                logger.logComment("Problem getting default value, using 0");
                                suggestedValue = "0";
                            }
                        }
                        else if (cellMech instanceof ChannelMLCellMechanism)
                        {
                            ChannelMLCellMechanism cmlCellMech = (ChannelMLCellMechanism) cellMech;
                            cmlCellMech.initialise(project, false);

                            suggestedValue = cmlCellMech.getValue("//@" + ChannelMLConstants.DEFAULT_COND_DENSITY_ATTR);

                            if (suggestedValue == null || suggestedValue.length() == 0) suggestedValue = "1";

                            String unitsUsed = cmlCellMech.getValue(ChannelMLConstants.getUnitsXPath());

                            logger.logComment("Units used = " + unitsUsed);

                            if (unitsUsed != null)
                            {
                                double suggValDouble = Double.parseDouble(suggestedValue);

                                if (unitsUsed.equals(ChannelMLConstants.SI_UNITS))
                                {
                                    suggValDouble = UnitConverter.getConductanceDensity(suggValDouble,
                                        UnitConverter.GENESIS_SI_UNITS,
                                        UnitConverter.NEUROCONSTRUCT_UNITS);
                                }
                                else if (unitsUsed.equals(ChannelMLConstants.PHYSIOLOGICAL_UNITS))
                                {
                                    suggValDouble = UnitConverter.getConductanceDensity(suggValDouble,
                                        UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS,
                                        UnitConverter.NEUROCONSTRUCT_UNITS);
                                }
                                suggestedValue = suggValDouble + "";

                            }
                        }
                        else
                        {
                            suggestedValue="0";
                        }

                        condDens = getDensForGroup(selectedMech, Float.parseFloat(suggestedValue), "the selected groups");
                    }
                    catch (Exception ex)
                    {
                        GuiUtils.showErrorMessage(logger,
                                                  "Error getting default value for conductance density on mech: " +
                                                  selectedMech,
                                                  ex, null);
                        return;
                    }
                }
            }

            if (condDens>=0)
            {
                int[] selected = jListGroupsOut.getSelectedIndices();

                for (int i = 0; i < selected.length; i++)
                {
                    String group = (String) listModelGroupsOut.elementAt(selected[i]);
                    logger.logComment("Item: " + selected[i] + " (" + group + ") is selected...");

                    ChannelMechanism selChanMech = new ChannelMechanism(selectedMech, condDens);

                    myCell.associateGroupWithChanMech(group, selChanMech);

                }
            }
        }
        // simple update...
        this.jComboBoxMechNames.setSelectedItem(defaultMechSelection);
        this.jComboBoxMechNames.setSelectedItem(selectedMech);
    }


    void jButtonRemove_actionPerformed(ActionEvent e)
    {
        logger.logComment("Remove button pressed: "+ e);

        String selectedMech = (String) jComboBoxMechNames.getSelectedItem();

        if (selectedMech.equals(defaultMechSelection) ||
            selectedMech.equals(this.extraMechSelection)||
            selectedMech.equals(this.sbmlSelection)) return;

        int[] selected = jListGroupsIn.getSelectedIndices();
        
        ArrayList<ParameterisedGroup> paramGroups = new ArrayList<ParameterisedGroup>();
                
        for (int i = 0; i < selected.length; i++)
        {
            String group = (String) listModelGroupsIn.elementAt(selected[i]);
            logger.logComment("Selected: "+ group);
            if (!myCell.isGroup(group))
            {
                String name = group.substring(0,group.indexOf(" "));
                for(ParameterisedGroup pg: myCell.getParameterisedGroups())
                {
                    if (name.equals(pg.getName()))
                        paramGroups.add(pg);
                }
            }                
        }
        logger.logComment("paramGroups: "+ paramGroups);

        if(paramGroups.size()>1 || (paramGroups.size()>0 && selected.length>1))
        {
            GuiUtils.showWarningMessage(logger,
                                      "Warning, can only associate parameter groups to mechanisms one at a time",null);
        }
        else if(paramGroups.size() == 1) 
        {
            //String selMechParaGroupAssoc = (String)listModelGroupsIn.elementAt(0);
            
            logger.logComment("-------- Removing assoc: "+ paramGroups.get(0));
                    
            Iterator<VariableMechanism> vMechs = myCell.getVarMechsVsParaGroups().keySet().iterator();
            VariableMechanism vmToRemove = null;
            ParameterisedGroup pgToRemove = null;
            
            while(vMechs.hasNext())
            {
                VariableMechanism nextVMech = vMechs.next();
                logger.logComment("-- nextVMech: "+ nextVMech);
                if(nextVMech.getName().equals(selectedMech))
                {
                    ParameterisedGroup pg = myCell.getVarMechsVsParaGroups().get(nextVMech);
                    logger.logComment("pg: "+ pg+", paramGroups.get(0): "+paramGroups.get(0));
                    //logger.logComment("nextVMech.getParam().getExpression(): "+nextVMech.getParam().getExpression());

                    if(paramGroups.get(0).equals(pg)/* && selectedMech.indexOf(nextVMech.getParam().getExpression().toString())>=0*/)
                    {
                        vmToRemove = nextVMech;
                        pgToRemove = pg;
                        //myCell.dissociateParamGroupFromVarMech(pg, nextVMech);
                        //logger.logComment("Removed assoc for: "+ nextVMech, true);
                    }
                }

            }
            logger.logComment("vmToRemove: "+ vmToRemove);
            logger.logComment("pgToRemove: "+ pgToRemove);

            if(vmToRemove!=null && pgToRemove!=null)
            {
                myCell.dissociateParamGroupFromVarMech(pgToRemove, vmToRemove);
                logger.logComment("^^ Removed assoc for: "+ vmToRemove);
            }

            logger.logComment("-------- Done removing assoc: "+ paramGroups.get(0));
        }
        else
        {
            for (int i = 0; i < selected.length; i++)
            {
                String groupAndDens = (String)listModelGroupsIn.elementAt(selected[i]);
                String groupName = groupAndDens.substring(0, groupAndDens.indexOf(" ")).trim();
                String extra = "";
                if (groupAndDens.indexOf(")")>0)
                    extra = groupAndDens.substring(groupAndDens.indexOf("(")+1, groupAndDens.indexOf(")")).trim();

                logger.logComment("Item: " + selected[i] + " ("+groupAndDens
                                  +") is selected, so group: " + groupName+", extra: ["+extra+"]");

                if (selectedMech.equals(this.apPropVelSelection))
                {
                    myCell.disassociateGroupFromApPropSpeeds(groupName);
                }
                else if (selectedMech.equals(this.ionPropsSelection))
                {

                    ArrayList<IonProperties> ips = myCell.getIonPropertiesForGroup(groupName);
                    logger.logComment("ips: " + ips);
                    for (IonProperties ip: ips)
                    {
                        if (ip.toString().equals(extra))
                            myCell.disassociateGroupFromIonProperties(groupName, ip);
                    }
                    ips = myCell.getIonPropertiesForGroup(groupName);
                    logger.logComment("ips: " + ips);
                }
                else if (selectedMech.equals(this.specCapSelection))
                {
                    myCell.disassociateGroupFromSpecCap(groupName);
                }
                else if (selectedMech.equals(this.specAxResSelection))
                {
                    myCell.disassociateGroupFromSpecAxRes(groupName);
                }
                else
                {
                    myCell.disassociateGroupFromChanMech(groupName, selectedMech);
                }
            }
        }

        // simple update...
        this.jComboBoxMechNames.setSelectedItem(defaultMechSelection);
        this.jComboBoxMechNames.setSelectedItem(selectedMech);

    }




    public static void main(String[] args) throws ProjectFileParsingException, EquationException
    {
        String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();
        try
        {
            UIManager.setLookAndFeel(favouredLookAndFeel);
        }
        catch (Exception ex)
        {

        }
        File f = new File("../copyNcModels/Inhomogen/Inhomogen.neuro.xml");

        f = new File("../nC_projects/GG/GG.ncx");
        
        Project testProj = Project.loadProject(f,
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

        Cell cell = testProj.cellManager.getCell("Longer");
        if (cell == null)
            cell = testProj.cellManager.getCell("Granule_98");;
        if (cell == null)
            cell = testProj.cellManager.getAllCells().get(0);
        /*
        String expression1 = "100 + 200*(p+10)";
        
        VariableParameter vp1 = null;
        VariableMechanism vm = null;
    
        Variable p = null;

        p = new Variable("p");

        ArrayList<Argument> expressionArgs1 =  new ArrayList<Argument>();

        vp1 = new VariableParameter("gmax", expression1, p, expressionArgs1);
            
        System.out.println("Var param 1: "+ vp1); 

        vm = new VariableMechanism("KConductance", vp1);
        
        cell.associateParamGroupWithVarMech(cell.getParameterisedGroups().get(0), vm);
        
        System.out.println(CellTopologyHelper.printDetails(cell, testProj));*/
        

/*
        ParameterisedGroup pg3 = new ParameterisedGroup("PathLengthOverDendrites", 
                                                       Section.DENDRITIC_GROUP, 
                                                       Metric.PATH_LENGTH_FROM_ROOT, 
                                                       ProximalPref.NO_TRANSLATION, 
                                                       DistalPref.NO_NORMALISATION);
        
        cell.addParameterisedGroup(pg3);


        //Segment endSeg = cell.getSegmentWithId(3);

*/
        EditGroupCellDensMechAssociations dlg = new EditGroupCellDensMechAssociations(cell, null, "Cell Density Mechanism", testProj);
        dlg.standalone = true;
        dlg.setModal(true);
        GuiUtils.centreWindow(dlg);
        dlg.setVisible(true);

    }

}
