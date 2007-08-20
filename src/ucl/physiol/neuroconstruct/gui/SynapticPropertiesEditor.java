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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;


/**
 * Implementation of AbstractCellEditor for cell colour column
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */


@SuppressWarnings("serial")

public class SynapticPropertiesEditor extends AbstractCellEditor 
                    implements TableCellEditor, ActionListener
{
    public SynapticPropertiesEditor()
    {
        try
        {
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    ClassLogger logger = new ClassLogger("SynapticPropertiesEditor");

    SynapticProperties currentSynapticProperties;
    JButton button;

    Project project = null;
    Frame myParent = null;

    public SynapticPropertiesEditor(Frame parent, Project project)
    {
        this.myParent = parent;

        this.project = project;
        button = new JButton();
        button.setActionCommand("Edit");
        button.addActionListener(this);
        button.setBorderPainted(false);
/*
        colourChooser = new JColorChooser();
        dialog = JColorChooser.createDialog(button,
                                            "Pick a colour for this Cell Group",
                                            true,
                                            colourChooser,
                                            this,
                                            null);
        }
   */     }


    public void actionPerformed(ActionEvent e)
    {
        if ("Edit".equals(e.getActionCommand()))
        {
            logger.logComment("Edit pressed...");
            SynapticPropertiesDialog dlg
                = new SynapticPropertiesDialog(myParent,
                                               currentSynapticProperties,
                                               project);

            Dimension dlgSize = dlg.getPreferredSize();
            Dimension frmSize = myParent.getSize();
            Point loc = myParent.getLocation();
            dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                            (frmSize.height - dlgSize.height) / 2 + loc.y);
            dlg.setModal(true);

            dlg.pack();
            dlg.setVisible(true);


            fireEditingStopped();

        }
        else
        {
            logger.logComment("Other event: "+ e);
        }
    }

    public Object getCellEditorValue()
    {
        return currentSynapticProperties;
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column)
    {
        currentSynapticProperties = (SynapticProperties)value;
        return button;
    }

    private void jbInit() throws Exception
    {
    }
}
