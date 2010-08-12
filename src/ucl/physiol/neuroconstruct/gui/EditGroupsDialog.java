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
import java.awt.event.*;
import javax.swing.*;

import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.cell.*;
import java.util.*;
import java.util.ArrayList;
import javax.swing.border.*;
import javax.swing.event.*;
import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.cell.examples.*;

/**
 * Dialog for editing section groups in cells
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class EditGroupsDialog
    extends JDialog
    implements ListSelectionListener
{
    ClassLogger logger = new ClassLogger("EditGroupsDialog");
    public boolean cancelled = false;

    private DefaultListModel listModelSectionsIn = new DefaultListModel();
    private DefaultListModel listModelSectionsOut = new DefaultListModel();

    String defaultGroupSelection = "-- Please select a group --";

    //boolean initialising = true;

    Frame myParent = null;

    Cell myCell = null;

    UpdateOneCell updateInterface = null;

    JPanel jPanelMain = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JPanel jPanelSelectGroup = new JPanel();
    JPanel jPanelLists = new JPanel();
    JLabel jLabelSelect = new JLabel();
    JComboBox jComboBoxGroupNames = new JComboBox();
    JList jListSectionsOut = new JList(listModelSectionsOut);
    JList jListSectionsIn = new JList(listModelSectionsIn);

    JScrollPane scrollPaneSectionsOut = new JScrollPane(jListSectionsOut);
    JScrollPane scrollPaneSectionsIn = new JScrollPane(jListSectionsIn);

    //JViewport viewportSectionsOut = scrollPaneSectionsOut.getViewport();
    //JViewport viewportSectionsIn = scrollPaneSectionsIn.getViewport();

    JPanel jPanelSectionsIn = new JPanel();
    JPanel jPanelSwitch = new JPanel();
    JPanel jPanelSectionsOut = new JPanel();
    JButton jButtonAdd = new JButton();
    JButton jButtonRemove = new JButton();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel jLabelSectionsOut = new JLabel();
    BorderLayout borderLayout2 = new BorderLayout();
    Border border1;
    Border border2;
    JLabel jLabelSectionsIn = new JLabel();
    BorderLayout borderLayout3 = new BorderLayout();
    Border border3;
    Border border4;
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JButton jButtonOK = new JButton();
    JButton jButtonRename = new JButton();
    GridBagLayout gridBagLayout3 = new GridBagLayout();
    JButton jButtonAddGroup = new JButton();
    JTextField jTextFieldNewGroup = new JTextField();
    
    JPanel jPanelAddWhat = new JPanel();
    JLabel jLabelAddWhat = new JLabel();
    JRadioButton jRadioButtonAddSections = new JRadioButton();
    JRadioButton jRadioButtonAddGroups = new JRadioButton();
    ButtonGroup JButtonGroup = new ButtonGroup();

    private EditGroupsDialog()
    {

    }

    public EditGroupsDialog(Cell cell,
                            Frame owner,
                            String title,
                            UpdateOneCell update) throws HeadlessException
    {
        super(owner, title, true);

        myCell = cell;

        updateInterface = update;

        myParent = owner;
        try
        {
            jbInit();
            extraInit();
            pack();
        }
        catch (Exception ex)
        {
            logger.logComment("Exception starting GUI: "+ ex);
        }
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
        jLabelSelect.setText("Group to edit:");
        jPanelLists.setLayout(gridBagLayout2);
        jPanelSectionsOut.setBorder(border2);
        jPanelSectionsOut.setLayout(borderLayout2);
        jPanelSectionsIn.setBorder(border4);
        jPanelSectionsIn.setLayout(borderLayout3);
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
        jLabelSectionsOut.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelSectionsOut.setText("Sections outside group");
        jListSectionsOut.setBorder(border1);
        jLabelSectionsIn.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelSectionsIn.setText("Sections in this group");
        jListSectionsIn.setBorder(border3);
        //viewportSectionsOut.setScrollMode(1);
        scrollPaneSectionsOut.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPaneSectionsIn.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jComboBoxGroupNames.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxGroupNames_itemStateChanged(e);
            }
        });
        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOK_actionPerformed(e);
            }
        });

        jButtonRename.setText("Rename group");
        jButtonRename.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonRename_actionPerformed(e);
            }
        });
        

        jButtonAddGroup.setText("New group:");
        
        ActionListener al = new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonAddGroup_actionPerformed(e);
            }
        };
        
        jButtonAddGroup.addActionListener(al);
        
        
        
        
        
        jTextFieldNewGroup.setText("");
        jTextFieldNewGroup.setColumns(6);
        jPanelSwitch.setBorder(null);
        jPanelLists.setMaximumSize(new Dimension(410, 300));
        jPanelLists.setMinimumSize(new Dimension(410, 300));
        jPanelLists.setPreferredSize(new Dimension(410, 300));
        jPanelMain.setMaximumSize(new Dimension(490, 380));
        jPanelMain.setMinimumSize(new Dimension(500, 400));
        jPanelMain.setPreferredSize(new Dimension(500, 400));
        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
        jPanelMain.add(jPanelSelectGroup,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 300, 0));
        jPanelMain.add(jPanelLists, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
                                                           , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                           new Insets(0, 0, 0, 0), 0, 0));
        jPanelLists.add(jPanelSectionsOut, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                                                  , GridBagConstraints.CENTER,
                                                                  GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0),
                                                                  70, 0));
        jPanelLists.add(jPanelSwitch, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
                                                             , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                             new Insets(0, 0, 0, 0), 50, 254));
        jPanelSwitch.add(jButtonAdd, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                            , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                            new Insets(0, 0, 12, 0), 0, 0));
        jPanelLists.add(jPanelSectionsIn, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
                                                                 , GridBagConstraints.CENTER,
                                                                 GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 1),
                                                                 70, 0));
        jPanelMain.add(jPanelButtons, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
                                                             , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                             new Insets(0, 0, 1, 0), 300, 0));
        jPanelButtons.add(jButtonOK, null);
        jPanelButtons.add(jButtonRename, null);
        jPanelSelectGroup.add(jLabelSelect, null);
        jPanelSelectGroup.add(jComboBoxGroupNames, null);
        jPanelSelectGroup.add(jButtonAddGroup, null);
        jPanelSwitch.add(jButtonRemove, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                                               , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                               new Insets(0, 0, 0, 1), 0, 0));

        jPanelSectionsOut.add(jLabelSectionsOut, BorderLayout.NORTH);
        jPanelSectionsOut.add(scrollPaneSectionsOut, BorderLayout.CENTER);
        //viewportSectionsOut.setView(jListSectionsOut);

        jPanelSectionsIn.add(jLabelSectionsIn, BorderLayout.NORTH);
        jPanelSectionsIn.add(scrollPaneSectionsIn, BorderLayout.CENTER);
        jPanelSelectGroup.add(jTextFieldNewGroup, null);
//        viewportSectionsIn.setView(jListSectionsIn);

        jListSectionsIn.addListSelectionListener(this);
        jListSectionsOut.addListSelectionListener(this);
        
        jLabelAddWhat.setText("Add");
        jRadioButtonAddSections.setText("sections");
        jRadioButtonAddSections.setSelected(true);
        jRadioButtonAddGroups.setText("groups");
//        jRadioButtonAddGroups.setEnabled(false);
        JButtonGroup.add(jRadioButtonAddSections);
        JButtonGroup.add(jRadioButtonAddGroups);
        jPanelMain.add(jPanelAddWhat, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.WEST, GridBagConstraints.WEST, new Insets(0, 0, 0, 0), 300, 0));
        jPanelAddWhat.add(jLabelAddWhat, null);
        jPanelAddWhat.add(jRadioButtonAddSections, null);
        jPanelAddWhat.add(jRadioButtonAddGroups, null);
        jRadioButtonAddSections.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButtonAddSections_actionPerformed(e);
            }

            
        });
        jRadioButtonAddGroups.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButtonAddGroups_actionPerformed(e);
            }

        });
        
    }
    
    private void jRadioButtonAddSections_actionPerformed(ActionEvent e) {
               jLabelSectionsOut.setText("Sections outside group");
               jLabelSectionsIn.setText("Sections in this group");
               listModelSectionsIn.clear();
               listModelSectionsOut.clear();
               
               String selectedGroup = (String) jComboBoxGroupNames.getSelectedItem();
                logger.logComment("Setting the selected group to: " + selectedGroup);

                if (selectedGroup.equals(defaultGroupSelection))
                {
                    return;
                }

                if (selectedGroup.equals("all"))
                {
                    GuiUtils.showErrorMessage(logger, "That group cannot be altered", null, this);
                    jComboBoxGroupNames.setSelectedItem(defaultGroupSelection);
                    return;
                }
                
                Vector allSegments = myCell.getAllSegments();
                for (int i = 0; i < allSegments.size(); i++) {
                Segment segment = (Segment) allSegments.elementAt(i);
                if (segment.isFirstSectionSegment()) {
                    Vector sectionGroups = segment.getSection().getGroups();
                    if (sectionGroups.contains(selectedGroup)) {
                        listModelSectionsIn.addElement(new SectionHelper(segment.getSection()));
                    } else {
                        listModelSectionsOut.addElement(new SectionHelper(segment.getSection()));
                    }
                }
            }
            updateInterface.refreshGroup(selectedGroup);
            
            // simple update...
            this.jComboBoxGroupNames.setSelectedItem(defaultGroupSelection);
            this.jComboBoxGroupNames.setSelectedItem(selectedGroup);
    }
            
            
    private void jRadioButtonAddGroups_actionPerformed(ActionEvent e) {
                jLabelSectionsOut.setText("other groups in the cell");
                jLabelSectionsIn.setText("subgroups");
                listModelSectionsIn.clear();                
                listModelSectionsOut.clear();
                
               String selectedGroup = (String) jComboBoxGroupNames.getSelectedItem();
                logger.logComment("Setting the selected group to: " + selectedGroup);

                if (selectedGroup.equals(defaultGroupSelection))
                {
                    return;
                }

                if (selectedGroup.equals("all"))
                {
                    GuiUtils.showErrorMessage(logger, "That group cannot be altered", null, this);
                    jComboBoxGroupNames.setSelectedItem(defaultGroupSelection);
                    return;
                }
                
                Vector<String> allGroups = myCell.getAllGroupNames();
                for (int i = 0; i < allGroups.size(); i++) {
                    String group = allGroups.elementAt(i);
                    if (!selectedGroup.equals(group)) {
                        
                        ArrayList<Section> allSecInGroup = myCell.getSectionsInGroup(group);
                        
                        if (!allSecInGroup.containsAll(myCell.getSectionsInGroup(selectedGroup))) { //groups that contains the selected group should not appear
                            
                            if (myCell.getSectionsInGroup(selectedGroup).containsAll(allSecInGroup)) {
                                listModelSectionsIn.addElement(group);
                            } else {                           
                                listModelSectionsOut.addElement(group);
                            }
                        }
                    }
                }
                updateInterface.refreshGroup(selectedGroup);
                
                // simple update...
                this.jComboBoxGroupNames.setSelectedItem(defaultGroupSelection);
                this.jComboBoxGroupNames.setSelectedItem(selectedGroup);
    }

    private void extraInit()
    {
        jComboBoxGroupNames.addItem(defaultGroupSelection);

        Vector allGroups = myCell.getAllGroupNames();

        for (int i = 0; i < allGroups.size(); i++)
        {
            jComboBoxGroupNames.addItem(allGroups.elementAt(i));
        }

        logger.logComment("Finished initialising...");
    }
    
    public void setSelectedGroup(String group)
    {
        if (group!=null)
        {
            jComboBoxGroupNames.setSelectedItem(group);
        }
    }

    public void valueChanged(ListSelectionEvent e)
    {
        logger.logComment("Value changed: " + e);

        String selectedGroup = (String) jComboBoxGroupNames.getSelectedItem();

        if (e.getSource().equals(jListSectionsIn))
        {
            logger.logComment("SectionsIn change: " + e.getFirstIndex());

            this.jButtonRemove.setEnabled(true);
            this.jButtonAdd.setEnabled(false);

            jListSectionsOut.setSelectedIndices(new int[] {});

            if (e.getValueIsAdjusting())
            {
                int[] sel = jListSectionsIn.getSelectedIndices();

                logger.logComment("Selected: " + e.getFirstIndex()+ " to "+ e.getLastIndex()+", tot selected: "+ sel.length, true);


                for (int i: sel)
                {
                    SectionHelper selSection = (SectionHelper)listModelSectionsIn.elementAt(i);

                    logger.logComment("Item: " + i + " ("+selSection+") is selected...", true);
                    this.updateInterface.tempShowSection(selSection.getSection().getSectionName(), selectedGroup);

                }
            }
        }
        else if (e.getSource().equals(jListSectionsOut))
        {
            logger.logComment("SectionsOut change: " + e.getFirstIndex());
            this.jButtonRemove.setEnabled(false);
            this.jButtonAdd.setEnabled(true);

            jListSectionsIn.setSelectedIndices(new int[] {});

            if (e.getValueIsAdjusting())
            {
                int[] sel = jListSectionsOut.getSelectedIndices();

                logger.logComment("Selected: " + e.getFirstIndex()+ " to "+ e.getLastIndex()+", tot selected: "+ sel.length, true);


                for (int i: sel)
                {
                    SectionHelper selSection = (SectionHelper)listModelSectionsOut.elementAt(i);

                    logger.logComment("Item: " + i + " ("+selSection+") is selected...", true);
                    this.updateInterface.tempShowSection(selSection.getSection().getSectionName(), selectedGroup);
                }
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
        UpdateOneCell update = new UpdateOneCell()
        {
          public void refreshGroup(String groupName){};
          public void tempShowSection(String secName, String selectedGroup){};
        };

        SimpleCell cell = new SimpleCell("");
        EditGroupsDialog dlg = new EditGroupsDialog(cell, null, "Edit groups", update);

        dlg.setModal(true);
        dlg.setVisible(true);

    }

    void jComboBoxGroupNames_itemStateChanged(ItemEvent e)
    {
        logger.logComment("" + e);

        if (e.getStateChange() == ItemEvent.SELECTED)
        {
            listModelSectionsIn.clear();
            listModelSectionsOut.clear();


            String selectedGroup = (String) jComboBoxGroupNames.getSelectedItem();

            logger.logComment("Setting the selected group to: " + selectedGroup);

            if (selectedGroup.equals(defaultGroupSelection))
            {
                return;
            }

            if (selectedGroup.equals("all"))
            {
                GuiUtils.showErrorMessage(logger, "That group cannot be altered", null, this);
                jComboBoxGroupNames.setSelectedItem(defaultGroupSelection);
                return;
            }

            if (jRadioButtonAddSections.isSelected()== true) {
                    
            Vector allSegments = myCell.getAllSegments();

            for (int i = 0; i < allSegments.size(); i++)
            {
                Segment segment = (Segment)allSegments.elementAt(i);
                if (segment.isFirstSectionSegment())
                {

                    Vector sectionGroups = segment.getSection().getGroups();

                    if (sectionGroups.contains(selectedGroup))
                    {
                        listModelSectionsIn.addElement(new SectionHelper(segment.getSection()));
                    }
                    else
                    {
                        listModelSectionsOut.addElement(new SectionHelper(segment.getSection()));
                    }
                }
            }
            }
            
            else if (jRadioButtonAddGroups.isSelected()== true)  {
                
            Vector<String> allGroups = myCell.getAllGroupNames();

            for (int i = 0; i <  allGroups.size(); i++)
            {
                String group = allGroups.elementAt(i);                
                if (!selectedGroup.equals(group))
                {
                    ArrayList<Section> allSecInGroup = myCell.getSectionsInGroup(group);
                    
                    if (myCell.getSectionsInGroup(selectedGroup).containsAll(allSecInGroup))
                        listModelSectionsIn.addElement(allGroups.elementAt(i)); 
                    else
                        listModelSectionsOut.addElement(allGroups.elementAt(i));  
                }
            }
            }
            updateInterface.refreshGroup(selectedGroup);
        }
    }


    void jButtonAdd_actionPerformed(ActionEvent e)
    {
        String selectedGroup = (String) jComboBoxGroupNames.getSelectedItem();

        if (selectedGroup.equals(defaultGroupSelection)) return; // Why???

        int[] selected = jListSectionsOut.getSelectedIndices();

        for (int i = 0; i < selected.length; i++)
        {
            if (jRadioButtonAddSections.isSelected()== true)
            {

            SectionHelper selSection = (SectionHelper)listModelSectionsOut.elementAt(selected[i]);

            logger.logComment("Item: " + selected[i] + " ("+selSection+") is selected...");

            selSection.getSection().addToGroup(selectedGroup);

             }
             
             else if (jRadioButtonAddGroups.isSelected()== true)
             {
                 ArrayList<Section> sectionsToAdd = myCell.getSectionsInGroup((String)listModelSectionsOut.elementAt(selected[i]));
                 
                 for (int j = 0; j < sectionsToAdd.size(); j++) {
                     
                    Section selSection = sectionsToAdd.get(j);
                    
                    logger.logComment("Item: " + selSection + "section  of group "+ selected[i] +" is selected...");
                    
                    if (!myCell.getSectionsInGroup(selectedGroup).contains(selSection)) //check for overlapping sections
                        selSection.addToGroup(selectedGroup);
                     
                 }

             }
            
//             listModelSectionsIn.addElement(jListSectionsOut.getName());
             listModelSectionsIn.addElement(listModelSectionsOut.elementAt(selected[i]));
        }

        // simple update...
        this.jComboBoxGroupNames.setSelectedItem(defaultGroupSelection);
        this.jComboBoxGroupNames.setSelectedItem(selectedGroup);
    }

    private class SectionHelper
    {
        private Section mySection = null;

        public SectionHelper(Section section)
        {
            mySection = section;
        }

        @Override
        public String toString()
        {
            return mySection.getSectionName();
        }

        public Section getSection()
        {
            return mySection;
        }
    }

    void jButtonRemove_actionPerformed(ActionEvent e)
    {
        String selectedGroup = (String) jComboBoxGroupNames.getSelectedItem();

        if (selectedGroup.equals(defaultGroupSelection)) return; // Why???

        int[] selected = jListSectionsIn.getSelectedIndices();

        for (int i = 0; i < selected.length; i++)
        {
            
            if (jRadioButtonAddSections.isSelected()== true)
            {

            SectionHelper selSection = (SectionHelper)listModelSectionsIn.elementAt(selected[i]);

            logger.logComment("Item: " + selected[i] + " ("+selSection+") is selected...");

            selSection.getSection().removeFromGroup(selectedGroup);
            
            }
            
             else if (jRadioButtonAddGroups.isSelected()== true)
             {
                     
                 ArrayList<Section> sectionsToRemove = myCell.getSectionsInGroup((String)listModelSectionsIn.elementAt(selected[i]));
                 
                 for (int j = 0; j < sectionsToRemove.size(); j++) {
                     
                     Section selSection = sectionsToRemove.get(j);

                     selSection.removeFromGroup(selectedGroup);              
                                  
                 }
                 
             }
            
        }

        // simple update...
        this.jComboBoxGroupNames.setSelectedItem(defaultGroupSelection);
        this.jComboBoxGroupNames.setSelectedItem(selectedGroup);

    }

    void jButtonAddGroup_actionPerformed(ActionEvent e)
    {
        String newGroupName = jTextFieldNewGroup.getText().trim();


        /** @todo Check for spaces, etc... */
        if (newGroupName.length()==0) return;


        for (int i = 0; i < jComboBoxGroupNames.getItemCount(); i++)
        {
              if (jComboBoxGroupNames.getItemAt(i).equals(newGroupName))
              {
                  GuiUtils.showErrorMessage(logger, "That group name is already taken", null, this);
                  return;
              }
        }


        if (newGroupName.equalsIgnoreCase("all"))
        {
            GuiUtils.showErrorMessage(logger, "The group name: "+newGroupName+" may cause conflicts when generating the hoc.\nPlease choose another.", null, this);
            jTextFieldNewGroup.setText("");
            return;
        }



        jComboBoxGroupNames.addItem(newGroupName);
        jComboBoxGroupNames.setSelectedItem(newGroupName);

    }

    void jButtonRename_actionPerformed(ActionEvent e)
    {
        String oldGroupName = (String) jComboBoxGroupNames.getSelectedItem();

        String groupName = new String();
        groupName = JOptionPane.showInputDialog("Please enter the new name for the group: "+ oldGroupName);
        if (groupName == null) return;
        String newGroupName = groupName;
//        String newGroupName = jTextFieldRename.getText().trim();
        if (newGroupName.length()==0) return;


        for (int i = 0; i < jComboBoxGroupNames.getItemCount(); i++)
        {
              if (jComboBoxGroupNames.getItemAt(i).equals(newGroupName))
              {
                  GuiUtils.showErrorMessage(logger, "That group name is already taken", null, this);
                  return;
              }
        }

        if (newGroupName.equalsIgnoreCase("all"))
        {
            GuiUtils.showErrorMessage(logger, "The group name: "+newGroupName+" may cause conflicts when generating the hoc.\nPlease choose another.", null, this);
            jTextFieldNewGroup.setText("");
            return;
        }
        
        jComboBoxGroupNames.addItem(newGroupName);
        jComboBoxGroupNames.removeItem(oldGroupName);
        jComboBoxGroupNames.setSelectedItem(newGroupName);

        // rename the group in the cell
        
        myCell.renameGroup(oldGroupName, newGroupName);
        //updateInterface.refreshGroup(newGroupName);
        
        jComboBoxGroupNames.setSelectedItem(defaultGroupSelection);
        jComboBoxGroupNames.setSelectedItem(newGroupName);
    }
    
}
