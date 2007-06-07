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
 * Implementation of AbstractCellEditor for generic list of int->String combos
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */


public class DropDownCellValuesEditor extends AbstractCellEditor implements TableCellEditor
{
    ClassLogger logger = new ClassLogger("DropDownCellValuesEditor");

    JComboBox comboBox;

    /** @todo do better? */
    Vector allIntegers = new Vector();
    Vector allStrings = new Vector();

    public DropDownCellValuesEditor()
    {
        logger.logComment("New DropDownCellValuesEditor created...");

        comboBox = new JComboBox();
        comboBox.setActionCommand("Choose");
    }


    public void addValue(int intValue, String stringAlternative)
    {
        comboBox.addItem(stringAlternative);
        allIntegers.add(new Integer(intValue));
        allStrings.add(stringAlternative);
    }


    public Object getCellEditorValue()
    {
        logger.logComment("getCellEditorValue called");
        String selString = (String)comboBox.getSelectedItem();
        int index = allStrings.indexOf(selString);
        return (Integer)allIntegers.elementAt(index);
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column)
    {
        logger.logComment("getTableCellEditorComponent called, object type: "+ value.getClass().getName());
        Integer chosenInt = ((Integer)value);
        int index = allIntegers.indexOf(chosenInt);
        String correspString = (String)allStrings.elementAt(index);
        comboBox.setSelectedItem(correspString);

        return comboBox;
    }


}
