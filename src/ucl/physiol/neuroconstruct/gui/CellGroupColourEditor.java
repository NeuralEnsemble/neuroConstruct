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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;


/**
 * Implementation of AbstractCellEditor for cell colour column
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class CellGroupColourEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
{
    Color currentColour;
    JButton button;
    JColorChooser colourChooser;
    JDialog dialog;

    public CellGroupColourEditor()
    {
        button = new JButton();
        button.setActionCommand("Edit");
        button.addActionListener(this);
        button.setBorderPainted(false);

        colourChooser = new JColorChooser();
        dialog = JColorChooser.createDialog(button,
                                            "Pick a colour for this Cell Group",
                                            true,
                                            colourChooser,
                                            this,
                                            null);
    }


    public void actionPerformed(ActionEvent e)
    {
        if ("Edit".equals(e.getActionCommand()))
        {
            button.setBackground(currentColour);
            colourChooser.setColor(currentColour);
            dialog.setVisible(true);

            fireEditingStopped();

        }
        else
        {
            currentColour = colourChooser.getColor();
        }
    }

    public Object getCellEditorValue()
    {
        return currentColour;
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column)
    {
        currentColour = (Color)value;
        return button;
    }
}
