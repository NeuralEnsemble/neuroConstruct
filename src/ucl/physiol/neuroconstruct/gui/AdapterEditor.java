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
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.project.packing.*;


/**
 * Implementation of AbstractCellEditor for CellPacking adapter column
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class AdapterEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
{
    ClassLogger logger = new ClassLogger("AdapterEditor");

    JButton button;

    CellPackingAdapter myAdapter = null;

    Frame myParent = null;

    public AdapterEditor(Frame parent)
    {
        logger.logComment("New AdapterEditor created...");

        myParent = parent;

        button = new JButton();
        button.setActionCommand("Edit");
        button.addActionListener(this);
        button.setBorderPainted(false);
    }


    public void actionPerformed(ActionEvent e)
    {
        if ("Edit".equals(e.getActionCommand()))
        {
            CellPackingPatternDialog dlg = new CellPackingPatternDialog(myParent, myAdapter);
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

            myAdapter = dlg.getFinalCellPackingAdapter();
            fireEditingStopped();
        }
        else
        {
            logger.logComment("actionPerformed else... ");
            logger.logComment("Adapter: "+myAdapter);
            //currentColour = colourChooser.getColor();
        }
    }

    public Object getCellEditorValue()
    {
        logger.logComment("getCellEditorValue called");
        return myAdapter;
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column)
    {
        logger.logComment("getTableCellEditorComponent called, object type: "+ value.getClass().getName());

        myAdapter = (CellPackingAdapter)value;
        logger.logComment("Adapter: "+myAdapter);
        return button;
    }
}
