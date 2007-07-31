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
import javax.swing.table.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;


/**
 * Implementation of AbstractCellEditor for drop down lost of generic objects.
 * Object should have good toString implemented
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public class DropDownObjectCellEditor extends AbstractCellEditor implements TableCellEditor
{
    ClassLogger logger = new ClassLogger("DropDownObjectCellEditor");

    JComboBox comboBox;

    Vector allObjects = new Vector();

    public DropDownObjectCellEditor()
    {
        logger.logComment("New DropDownObjectCellEditor created...");

        comboBox = new JComboBox();
        comboBox.setActionCommand("Choose");
    }


    public void addValue(Object obj)
    {
        logger.logComment("addValue called, object type: "+ obj.getClass().getName()+", value: "+ obj);

        comboBox.addItem(obj);
        allObjects.add(obj);
    }


    public Object getCellEditorValue()
    {
        logger.logComment("getCellEditorValue called");
        int index = comboBox.getSelectedIndex();
        return allObjects.elementAt(index);
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column)
    {
        logger.logComment("getTableCellEditorComponent called, object type: "+ value.getClass().getName()+", value: "+ value);

        comboBox.setSelectedItem(value);

        return comboBox;
    }


}
