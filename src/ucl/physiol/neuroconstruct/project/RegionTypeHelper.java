/**
 *  neuroConstruct
 *  Software for developing large scale 3D networks of biologically realistic neurons
 * 
 *  Copyright (c) 2009 Padraig Gleeson
 *  UCL Department of Neuroscience, Physiology and Pharmacology
 *
 *  Development of this software was made possible with funding from the
 *  Medical Research Council and the Wellcome Trust
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package ucl.physiol.neuroconstruct.project;

import java.util.*;

import ucl.physiol.neuroconstruct.utils.*;

/**
 * Helper class for getting list of Region types
 *
 * @author Padraig Gleeson
 *  
 */

public class RegionTypeHelper
{
    private static ClassLogger logger = new ClassLogger("RegionTypeHelper");

    private static Vector<Region> regionTypes = new Vector<Region>();

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
