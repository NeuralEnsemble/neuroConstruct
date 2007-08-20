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

/**
 * Extension of JFormattedTextField for simplified input of double values in TextFields
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

@SuppressWarnings("serial")

public class DoubleInputField extends JFormattedTextField
{
    private static NumberFormat format = NumberFormat.getInstance();


    static
    {
        format.setMaximumFractionDigits(20);
        format.setGroupingUsed(false);
    }


    public DoubleInputField()
    {
        super(format);



    }

    public void setDoubleValue(double value)
    {
        super.setText(value+"");
    }

    public double getDoubleValue()
    {
        return Double.parseDouble(this.getText());
    }

}
