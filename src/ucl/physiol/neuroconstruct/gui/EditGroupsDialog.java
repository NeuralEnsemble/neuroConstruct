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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.cell.*;
import java.util.*;
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
    GridBagLayout gridBagLayout3 = new GridBagLayout();
    JButton jButtonAddGroup = new JButton();
    JTextField jTextFieldNewGroup = new JTextField();

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

        jButtonAddGroup.setText("Add group:");
        jButtonAddGroup.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonAddGroup_actionPerformed(e);
            }
        });
        jTextFieldNewGroup.setText("");
        jTextFieldNewGroup.setColumns(6);
        jPanelSwitch.setBorder(null);
        jPanelLists.setMaximumSize(new Dimension(410, 300));
        jPanelLists.setMinimumSize(new Dimension(410, 300));
        jPanelLists.setPreferredSize(new Dimension(410, 300));
        jPanelMain.setMaximumSize(new Dimension(490, 380));
        jPanelMain.setMinimumSize(new Dimension(490, 380));
        jPanelMain.setPreferredSize(new Dimension(490, 380));
        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
        jPanelMain.add(jPanelSelectGroup,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 300, 0));
        jPanelMain.add(jPanelLists, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
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
        jPanelMain.add(jPanelButtons, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
                                                             , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                             new Insets(0, 0, 1, 0), 300, 0));
        jPanelButtons.add(jButtonOK, null);
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
        //viewportSectionsIn.setView(jListSectionsIn);

        jListSectionsIn.addListSelectionListener(this);
        jListSectionsOut.addListSelectionListener(this);

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

    public void valueChanged(ListSelectionEvent e)
    {
        logger.logComment("Value changed: " + e);

        if (e.getSource().equals(jListSectionsIn))
        {
            logger.logComment("SectionsIn change: " + e.getFirstIndex());

            this.jButtonRemove.setEnabled(true);
            this.jButtonAdd.setEnabled(false);

            jListSectionsOut.setSelectedIndices(new int[]
                                                {});

            if (e.getValueIsAdjusting())
            {
                logger.logComment("Selected: " + e.getFirstIndex());
            }
        }
        else if (e.getSource().equals(jListSectionsOut))
        {
            logger.logComment("SectionsOut change: " + e.getFirstIndex());
            this.jButtonRemove.setEnabled(false);
            this.jButtonAdd.setEnabled(true);

            jListSectionsIn.setSelectedIndices(new int[]
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

            SectionHelper selSection = (SectionHelper)listModelSectionsOut.elementAt(selected[i]);

            logger.logComment("Item: " + selected[i] + " ("+selSection+") is selected...");

            selSection.getSection().addToGroup(selectedGroup);
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

            SectionHelper selSection = (SectionHelper)listModelSectionsIn.elementAt(selected[i]);

            logger.logComment("Item: " + selected[i] + " ("+selSection+") is selected...");

            selSection.getSection().removeFromGroup(selectedGroup);
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

}
