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

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.packing.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Dialog for creating new or editing existing Cell Groups
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class CellGroupDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("CellGroupDialog");

    boolean editingExisting = false;

    String cellGroupName = null;
    //String cellType = null;
    //int numInCellGroup = 1;

    ToolTipHelper toolTipText = ToolTipHelper.getInstance();

    CellPackingAdapter chosenAdapter = new RandomCellPackingAdapter(); // default...


    Project project = null;

    boolean cancelled = false;

    JPanel panel1 = new JPanel();
    JLabel jLabelCellGroupName = new JLabel();
    JTextField jTextFieldCellGroupName = new JTextField();
    JLabel jLabelCellType = new JLabel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JPanel jPanelLabel = new JPanel();
    JPanel jPanel2 = new JPanel();
    JButton jButtonOK = new JButton();
    JButton jButtonCancel = new JButton();
    BorderLayout borderLayout1 = new BorderLayout();
    JLabel jLabelMain = new JLabel();
    JComboBox jComboBoxCellType = new JComboBox();
    JLabel jLabelRegionName = new JLabel();
    JComboBox jComboBoxRegionName = new JComboBox();
    JLabel jLabelColour = new JLabel();
    JButton jButtonColour = new JButton();

    JLabel jLabelPackingPattern = new JLabel();
    JLabel jLabelPriority = new JLabel();
    JTextField jTextFieldPriority = new JTextField();


    JButton jButtonPackPattern = new JButton();

    JTextField adapterString = new JTextField();

    /**
     * Constructor for new Cell Group
     */
    public CellGroupDialog(Frame frame, String title, String newCellGroupProposedName, int proposedPriority, Project project)
    {
        super(frame, title, false);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        this.project = project;

        this.cellGroupName = newCellGroupProposedName;


        try
        {
            jbInit();
            extraInit();
            pack();
            this.jTextFieldPriority.setText(""+proposedPriority);
        }
        catch(Exception ex)
        {
            logger.logComment("Exception starting GUI: "+ ex);
        }
    }

    /**
     * Constructor for editing existing cell group
     */
    public CellGroupDialog(Frame frame,
                              String title,
                              String existingCellGroupName,
                              String cellType,
                              String regionName,
                              Color color,
                              CellPackingAdapter adapter,
                              int priority,
                              Project project)
    {
        super(frame, title, false);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        this.project = project;

        this.cellGroupName = existingCellGroupName;
        editingExisting = true;


        try
        {
            jbInit();

            chosenAdapter = adapter;

            extraInit();

            this.jTextFieldCellGroupName.setText(cellGroupName);
            jTextFieldCellGroupName.setEditable(false);
            this.jButtonColour.setBackground(color);
            this.jTextFieldPriority.setText(priority+"");

            jComboBoxRegionName.setSelectedItem(regionName);

            for (int i = 0; i < jComboBoxCellType.getItemCount(); i++)
            {
                    Cell cell = (Cell)jComboBoxCellType.getItemAt(i);
                    if (cell.getInstanceName().equals(cellType))
                        jComboBoxCellType.setSelectedIndex(i);
            }
            //jCheckBoxEnabled.setSelected(enabled);

            pack();


        }
        catch(Exception ex)
        {
            logger.logComment("Exception starting GUI: "+ ex);
        }
    }


    private CellGroupDialog()
    {
        //this(null, "", false);
    }
    private void jbInit() throws Exception
    {
        panel1.setLayout(gridBagLayout1);

        jLabelCellGroupName.setText("Cell Group Name:");
        jTextFieldCellGroupName.setText(this.cellGroupName);
        jTextFieldCellGroupName.setColumns(6);

        adapterString.setEditable(false);


        jLabelCellType.setDisplayedMnemonic('0');
        jLabelCellType.setText("Cell Type:");

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
        this.getContentPane().setLayout(borderLayout1);
        jLabelMain.setText("Please enter the details of the new Cell group");
        jLabelRegionName.setText("Region Name:");
        jLabelColour.setText("Cell Group Colour:");
        jButtonColour.setText("Choose...");
        this.jButtonOK.setText("Ok");
        this.jLabelPriority.setText("Priority:");
        this.jTextFieldPriority.setText("5");

        Random rand = new Random();
        Color cellGroupColour = new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat());
        jButtonColour.setBackground(cellGroupColour);

        jButtonColour.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonColour_actionPerformed(e);
            }
        });
        //jLabelNumInCellGroup.setText("Density:");
        jLabelPackingPattern.setText("Packing Pattern:");
        jButtonPackPattern.setText("Choose...");
        jButtonPackPattern.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonPackPattern_actionPerformed(e);
            }
        });
        adapterString.setToolTipText("");
       adapterString.setText("...");
        ///jCheckBoxEnabled.setSelected(true);
        ///jCheckBoxEnabled.setText("Cell group is enabled");
        getContentPane().add(panel1, BorderLayout.CENTER);


        panel1.add(jLabelCellGroupName,
                   new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
                                          , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                          new Insets(6, 14, 6, 0),
                                          75, 14));
        panel1.add(jTextFieldCellGroupName,
                   new GridBagConstraints(2, 1, 2, 1, 1.0, 0.0
                                          , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                          new Insets(6, 0, 6, 14), 86, 0));
        panel1.add(jLabelCellType,
                   new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
                                          , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                          new Insets(6, 14, 6, 0), 0, 0));


        this.getContentPane().add(jPanelLabel, BorderLayout.NORTH);
        jPanelLabel.add(jLabelMain, null);
        this.getContentPane().add(jPanel2,  BorderLayout.SOUTH);
        jPanel2.add(jButtonOK, null);
        jPanel2.add(jButtonCancel, null);


        panel1.add(jComboBoxCellType,
                   new GridBagConstraints(2, 2, 2, 1, 0.0, 0.0
                                          , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                          new Insets(6, 0, 6, 14), 0, 0));

        panel1.add(jLabelRegionName,
                   new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
                                          , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                          new Insets(6, 14, 6, 0), 0, 0));

        panel1.add(jComboBoxRegionName,
                   new GridBagConstraints(2, 3, 2, 1, 0.0, 0.0
                                          , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                          new Insets(6, 0, 6, 14), 0, 0));

        panel1.add(jLabelColour,
                   new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
                                          , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                          new Insets(6, 14, 6, 0), 0, 0));

        panel1.add(jButtonColour,
                   new GridBagConstraints(2, 4, 2, 1, 0.0, 0.0
                                          , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                          new Insets(6, 0, 6, 14), 0, 0));

        panel1.add(jLabelPackingPattern,
                   new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0
                                          , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                          new Insets(6, 14, 6, 0), 0, 0));

        panel1.add(jButtonPackPattern,
                   new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0
                                          , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                          new Insets(6, 0, 6, 14), 0, 0));

        panel1.add(adapterString,
                   new GridBagConstraints(1, 6, 3, 1, 0.0, 0.0
                                          , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                          new Insets(6, 0, 6, 14), 0, 0));

        panel1.add(this.jLabelPriority,
                   new GridBagConstraints(0, 7, 2, 1, 0.0, 0.0
                                          , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                          new Insets(6, 14, 6, 14), 0, 0));

        panel1.add(this.jTextFieldPriority,
                   new GridBagConstraints(2, 7, 2, 1, 0.0, 0.0
                                          , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                          new Insets(6,14, 6, 14), 0, 0));


    }

    /**
     * Extra initiation stuff, not automatically added by IDE
     */
    private void extraInit()
    {
        String[] regions = project.regionsInfo.getAllRegionNames();
        for (int i = 0; i < regions.length; i++)
        {
            this.jComboBoxRegionName.addItem(regions[i]);
        }


        Vector<Cell> cells = project.cellManager.getAllCells();

        for (Cell cell: cells)
        {


            this.jComboBoxCellType.addItem(cell);
        }
        adapterString.setText(chosenAdapter.toString());


        ///jCheckBoxEnabled.setToolTipText(toolTipText.getToolTip("Enabled Cell Group"));

        jButtonPackPattern.setToolTipText(toolTipText.getToolTip("Packing Pattern"));
    }


    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            doCancel();
        }
        super.processWindowEvent(e);
    }
    //Close the dialog

    public String getRegionName()
    {
        return (String)jComboBoxRegionName.getSelectedItem();
    }

    public String getCellType()
    {
        return ((Cell)this.jComboBoxCellType.getSelectedItem()).getInstanceName();
    }

    public String getCellGroupName()
    {
        return this.cellGroupName;
    }
    public int getPriority()
    {
        return Integer.parseInt(this.jTextFieldPriority.getText());
    }


    public Color getCellGroupColour()
    {
        return this.jButtonColour.getBackground();
    }

    public CellPackingAdapter getCellPackingAdapter()
    {
        return this.chosenAdapter;
    }


///    public boolean isCellGroupEnabled()
///    {
///        return jCheckBoxEnabled.isSelected();
///    }


    void doCancel()
    {
        logger.logComment("Actions for cancel...");

        cancelled = true;

    }

    void jButtonOK_actionPerformed(ActionEvent e)
    {
        logger.logComment("OK pressed...");

        this.cellGroupName = this.jTextFieldCellGroupName.getText();
        ArrayList<String> namesAlready = project.cellGroupsInfo.getAllCellGroupNames();

        if (!editingExisting && namesAlready.contains(cellGroupName))
        {
            GuiUtils.showErrorMessage(logger, "This name is already taken. Please select another name for the Cell Group", null, this);
            return;
        }


        if (cellGroupName.indexOf(" ")>0 || !cellGroupName.trim().equals(cellGroupName))
        {
            GuiUtils.showErrorMessage(logger, "Please choose a Cell Group name without spaces", null, this);
            return;
        }

        try
        {
            Float.parseFloat(cellGroupName);
            GuiUtils.showErrorMessage(logger, "Please don't use a number for the name", null, this);
            return;
        }
        catch (NumberFormatException ex)
        {};


        try
        {
            Integer.parseInt(this.jTextFieldPriority.getText());
        }
        catch (NumberFormatException ex1)
        {
            GuiUtils.showErrorMessage(logger, "Please specify an integer value for the priority", ex1, this);
            return;
        }




        this.dispose();
    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel pressed...");
        doCancel();
        this.dispose();

    }

    void jButtonColour_actionPerformed(ActionEvent e)
    {
        Color c = JColorChooser.showDialog(this, "Please choose a colour for the new Cell Group", jButtonColour.getBackground());
        jButtonColour.setBackground(c);
    }

    void jButtonPackPattern_actionPerformed(ActionEvent e)
    {
        logger.logComment("Creating packing dialog...");

        CellPackingPatternDialog dlg = new CellPackingPatternDialog(this, chosenAdapter);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);

       //dlg.pack();
        dlg.setVisible(true);


        if (dlg.cancelled)
        {
            logger.logComment("The action was cancelled...");
            return;
        }

        chosenAdapter = dlg.getFinalCellPackingAdapter();

        adapterString.setText(chosenAdapter.toString());


    }
}
