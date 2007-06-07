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

package ucl.physiol.neuroconstruct.project;

import java.util.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Helper class for getting list of Region types
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class RegionTypeHelper
{
    private static ClassLogger logger = new ClassLogger("RegionTypeHelper");

    private static Vector regionTypes = new Vector();

    static
    {
        /** @todo automatically look these up... */
        regionTypes.add(new RectangularBox());
        regionTypes.add(new SphericalRegion());
        regionTypes.add(new CylindricalRegion());
        regionTypes.add(new ConicalRegion());
    }

    public static String[] getRegionDescriptions()
    {
        /** @todo Make more efficient... */

        String[] allRegionDescs = new String[regionTypes.size()];

        for (int i = 0; i < regionTypes.size(); i++)
        {
            allRegionDescs[i] = ((Region)regionTypes.elementAt(i)).getDescription();
        }

        return allRegionDescs;
    }

    public static Region getRegionInstance(String desc)
    {
        logger.logComment("Getting region for desc: "+ desc);
        /** @todo replace with reflection... */
        for (int i = 0; i < regionTypes.size(); i++)
        {
            Region region = (Region)regionTypes.elementAt(i);
            if (desc.equals(region.getDescription()))
            return (Region)region.clone();
        }
        return null;
    }

}
