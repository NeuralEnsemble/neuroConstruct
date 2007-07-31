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

import javax.swing.*;

import ucl.physiol.neuroconstruct.utils.*;

/**
 * GUI element to allow easy linking between neuroConstruct tabs
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

@SuppressWarnings("serial")

public class ClickPanel extends JPanel
{
    ClassLogger logger = new ClassLogger("ClickPanel");

    int totalStringLength= 0;
    int maxWidth = 100;

    int defaultHeight = 28;
    int defaultWidth = 500;

    public ClickPanel()
    {
        this.setMinimumSize(new Dimension(defaultWidth, defaultHeight));
        this.setPreferredSize(new Dimension(defaultWidth, defaultHeight));
    }

    public void removeAll()
    {
        this.setMinimumSize(new Dimension(defaultWidth, defaultHeight));
        this.setPreferredSize(new Dimension(defaultWidth, defaultHeight));
        totalStringLength = 0;
        super.removeAll();
    };

    public Component add(Component component)
    {
        totalStringLength = totalStringLength + component.getName().length() + 3;


        logger.logComment("Total size: "+totalStringLength+" on adding: "+ component.getName());

        int numRows = (int)Math.floor((double)totalStringLength/(double)maxWidth)+1;
        if (numRows>1)
        {
            int height = 8 + (20*numRows);
            logger.logComment("numrows: "+numRows);
            this.setMinimumSize(new Dimension(500, height));
            this.setPreferredSize(new Dimension(500, height));
        }

        return         super.add(component);
    };
}
