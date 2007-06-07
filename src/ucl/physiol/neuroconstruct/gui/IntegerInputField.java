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
import java.text.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Extension of JFormattedTextField for simplified input of integer values in a text field
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */


public class IntegerInputField extends JFormattedTextField
{
    ClassLogger logger = new ClassLogger("IntegerInputField");

    private static NumberFormat format = NumberFormat.getInstance();


    static
    {
        format.setMaximumFractionDigits(0);
        format.setGroupingUsed(false);
    }


    public IntegerInputField()
    {
        super(format);



    }

    public void setIntValue(int value)
    {
        logger.logComment("Setting value to: "+ value);
        setText(value+"");
    }


    public int getIntValue()
    {
        try
        {
            return Integer.parseInt(this.getText());
        }
        catch(NumberFormatException ne)
        {
            return 0;
        }
    }

}
