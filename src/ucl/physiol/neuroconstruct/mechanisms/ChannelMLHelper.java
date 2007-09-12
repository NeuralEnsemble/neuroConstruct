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

package ucl.physiol.neuroconstruct.mechanisms;

import java.io.*;
import java.util.*;

import ucl.physiol.neuroconstruct.utils.*;

/**
 * Helper class for ChannelML Cell Mechanisms
 *
 * @author Padraig Gleeson
 *  
 */

public  class ChannelMLHelper
{
    static ClassLogger logger = new ClassLogger("ChannelMLHelper");

    private static ArrayList<File> availableCMLCellMechanisms = new ArrayList<File>();

    static
    {
        //String dir = ""

        /** @todo automatically look these up... */
        availableCMLCellMechanisms.add(new File("templates/xmlTemplates/ChannelMLPrototypes/Leak.xml"));
    }

    public static ArrayList<File> getChannelMLCellMechanisms()
    {
        return availableCMLCellMechanisms;
    }

}
