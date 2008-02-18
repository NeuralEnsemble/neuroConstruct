/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
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
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
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

    private DefaultListModel listModelGroupsIn = new DefaultListModel();
    private DefaultListModel listModelGroupsOut = new DefaultListModel();

    String defaultMechSelection = "-- Channel Mechanisms: --";
    String pointProcessSelection = "-- Point processes: --";
    String passivePropsSelection = "-- Passive properties: --";
    String specCapSelection = "Specific Capacitance";
    String specAxResSelection = "Specific Axial Resistance";
    String extraMechSelection = "-- Other mechanisms: --";
    String apPropVelSelection = "Action Potential propagation speed";

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
        super(owner, "Edit Group to "+mechType+ " associations", true);

        this.project = project;

        this.mechType = mechType;

        myCell = cell;

        ArrayList<ChannelMechanism> mechs = cell.getAllChannelMechanisms(false);

        // create copy...
        
        Vector<String> mechListTemp = project.cellMechanismInfo.getAllCellMechanismNames();
        Vector<String> mechList = new Vector<String>();
        mechList.addAll(mechListTemp);

        for (int i = 0; i < mechs.size(); i++)
        {
            ChannelMechanism nextMech = mechs.get(i);

            if (!mechList.contains(nextMech.getName()))
            {
                logger.logComment("Latest chan mechs: "+ cell.getAllChannelMechanisms(false));

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

                    logger.logComment("Chan mechs: "+ cell.getAllChannelMechanisms(false));
                }
                else logger.logComment("Leaving it alone...");

                logger.logComment("Details: "+ CellTopologyHelper.printDetails(cell, null));
            }
        }

        myParent = owner;
        try
        {
            jbInit();
            extraInit();
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

    private void extraInit()
    {
        jComboBoxMechNames.addItem(defaultMechSelection);


        Vector mechList = project.cellMechanismInfo.getChanMechsAndIonConcs();

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
        

        jComboBoxMechNames.addItem(this.passivePropsSelection);
        jComboBoxMechNames.addItem(this.specCapSelection);
        jComboBoxMechNames.addItem(this.specAxResSelection);
        jComboBoxMechNames.addItem(this.extraMechSelection);
        jComboBoxMechNames.addItem(this.apPropVelSelection);


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

            jListGroupsOut.setSelectedIndices(new int[]
                                                {});

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

            jListGroupsIn.setSelectedIndices(new int[]
                                               {});

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
    }

    void jButtonOK_actionPerformed(ActionEvent e)
    {
        logger.logComment("OK button pressed");

        this.dispose();
    }
    
    
    void jButtonEditExtraValue_actionPerformed(ActionEvent e)
    {
        logger.logComment("Edit extra value button pressed");
        
        String selectedMechanism = (String) jComboBoxMechNames.getSelectedItem();

        if (selectedMechanism.equals(defaultMechSelection) ||
            selectedMechanism.equals(this.extraMechSelection)) return;
        
        
        Object[] selected = this.jListGroupsIn.getSelectedValues();
        
        if (selected.length>1)
            GuiUtils.showErrorMessage(logger, "Please select a single mechanism/group association" , null, this);
        
        if (selectedMechanism.equals(this.apPropVelSelection) ||
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
            selectedMechanism.equals(this.extraMechSelection)) return;

        logger.logComment("Edit value button pressed");

        Object[] selected = this.jListGroupsIn.getSelectedValues();

        for (int j = 0; j < selected.length; j++)
        {
            String next = (String)selected[j];

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
                selectedMechanism.equals(this.extraMechSelection))
            {
                return;
            }



            logger.logComment("\n ----  Setting the selected Mech to: " + selectedMechanism);

            Vector allGroups = myCell.getAllGroupNames();

            for (int j = 0; j < allGroups.size(); j++)
            {
                String nextGroup = (String)allGroups.elementAt(j);

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
                        listModelGroupsIn.addElement(nextGroup
                                                     + " (speed: "
                                                     + appv.getSpeed() + ")");
                    }

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
                        listModelGroupsIn.addElement(nextGroup
                                                     + " (specCap: "
                                                     + specCap + ")");
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
                        listModelGroupsIn.addElement(nextGroup
                                                     + " (specAxRes: "
                                                     + specAxRes + ")");
                    }
                }



                else
                {
                    ArrayList<ChannelMechanism> allChanMechs = myCell.getChanMechsForGroup(nextGroup);

                    logger.logComment("Looking at group: " +
                                      nextGroup
                                      + " which has chans: "
                                      + allChanMechs);

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

        }
    }


    private float getDensForGroup(String cellMech, float suggestedValue, String groupRef)
    {
        float dens = -1;

        while (dens<0)
        {
            String request = "Please enter the value for the max conductance density for the channel:\n"
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
                + UnitConverter.rateUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()

                + ")";

            String inputValue
                = JOptionPane.showInputDialog(this,
                                              request,
                                              suggestedValue + "");

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


    void jButtonAdd_actionPerformed(ActionEvent e)
    {
        String selectedMech = (String) jComboBoxMechNames.getSelectedItem();

        if (selectedMech.equals(defaultMechSelection) ||
            selectedMech.equals(this.extraMechSelection)) return;

        if (selectedMech.equals(this.apPropVelSelection))
        {
            //float vel = this.getApPropVelForGroup(123);
            float vel = this.getApPropSpeedForGroup(GeneralProperties.getDefaultApPropagationVelocity());
            ApPropSpeed appv = new ApPropSpeed(vel);

            int[] selected = jListGroupsOut.getSelectedIndices();

            for (int i = 0; i < selected.length; i++)
            {
                String group = (String) listModelGroupsOut.elementAt(selected[i]);
                logger.logComment("Item: " + selected[i] + " (" + group + ") is selected...");

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
                try
                {
                    //String inputValue = null;
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
        String selectedMech = (String) jComboBoxMechNames.getSelectedItem();

        if (selectedMech.equals(defaultMechSelection) ||
            selectedMech.equals(this.extraMechSelection)) return;



        int[] selected = jListGroupsIn.getSelectedIndices();

        for (int i = 0; i < selected.length; i++)
        {
            String groupAndDens = (String)listModelGroupsIn.elementAt(selected[i]);


            String groupName = groupAndDens.substring(0, groupAndDens.indexOf(" ")).trim();

            logger.logComment("Item: " + selected[i] + " ("+groupAndDens
                              +") is selected, so group: " + groupName);

            if (selectedMech.equals(this.apPropVelSelection))
            {
                myCell.disassociateGroupFromApPropSpeeds(groupName);
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

        // simple update...
        this.jComboBoxMechNames.setSelectedItem(defaultMechSelection);
        this.jComboBoxMechNames.setSelectedItem(selectedMech);

    }




    public static void main(String[] args) throws ProjectFileParsingException
    {
        String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();
        try
        {
            UIManager.setLookAndFeel(favouredLookAndFeel);
        }
        catch (Exception ex)
        {

        }
   /*     SimpleCell cell = new SimpleCell("");

        String chanMechName1 = "ggg";
        String chanMechName2 = "ghfg";

        ChannelMechanism chMech1 = new ChannelMechanism( chanMechName1, 22);
        ChannelMechanism chMech2 = new ChannelMechanism( chanMechName1, 227);
        ChannelMechanism chMech3 = new ChannelMechanism( chanMechName2, 224);
        ChannelMechanism chMech4 = new ChannelMechanism( chanMechName2, 2246);

        cell.getFirstSomaSegment().getSection().addToGroup("ppp");
        cell.getFirstSomaSegment().getSection().addToGroup("ppp2");


        cell.associateGroupWithChanMech("all", chMech1);
        cell.associateGroupWithChanMech("soma_group", chMech2);
        cell.associateGroupWithChanMech("soma_group", chMech4);
        cell.associateGroupWithChanMech("dendrite_group", chMech3);

        System.out.println(CellTopologyHelper.printDetails(cell));



        Vector list = new Vector();
        list.add(chanMechName1);
        list.add(chanMechName2);
*/
        Project testProj = Project.loadProject(new File("examples/Ex4-NEURONGENESIS/Ex4-NEURONGENESIS.neuro.xml"),
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

        Cell cell = (Cell)testProj.cellManager.getCell("TestCell_ChannelML");

        //Cell cell = (Cell)testProj.cellManager.getCell("GranuleCellExp");
/*
        ApPropSpeed appv = new ApPropSpeed(123);
        ApPropSpeed appv2 = new ApPropSpeed(222);


        cell.associateGroupWithApPropSpeed("axon_group", appv);
        cell.associateGroupWithApPropSpeed("dendrite_group", appv2);
*/


        System.out.println(CellTopologyHelper.printDetails(cell, testProj));

        //Segment endSeg = cell.getSegmentWithId(3);


        EditGroupCellDensMechAssociations dlg = new EditGroupCellDensMechAssociations(cell, null, "Cell Density Mechanism", testProj);

        dlg.setModal(true);
        GuiUtils.centreWindow(dlg);
        dlg.setVisible(true);

    }

}
