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

/**
 * Helper class for specifying grow modes, i.e. specify whether the axon showld grow
 * to meet the dendrite, vice versa or neither
 *
 * @author Padraig Gleeson
 *  
 */


public class GrowMode
{
    public int type;


    public static final int GROW_MODE_JUMP = 0;

    //@Deprecated
    //public static final int GROW_MODE_DEND_GROW = 1;
    //@Deprecated
    //public static final int GROW_MODE_AXON_GROW = 2;

    static final String GROW_MODE_JUMP_STRING = "Don't grow";
    static final String GROW_MODE_DEND_GROW_STRING = "Grow dendrites";
    static final String GROW_MODE_AXON_GROW_STRING = "Grow axons";



    public static GrowMode getGrowModeJump()
    {
        GrowMode gm = new GrowMode();
        gm.type = GROW_MODE_JUMP;
        return gm;
    }

/*
    public static GrowMode getGrowModeDendsGrow()
    {
        GrowMode gm = new GrowMode();
        gm.type = GROW_MODE_DEND_GROW;
        return gm;
    }


    public static GrowMode getGrowModeAxonsGrow()
    {
        GrowMode gm = new GrowMode();
        gm.type = GROW_MODE_AXON_GROW;
        return gm;
    }
*/

    /**
     * Return a simple string representation...
     * @return A string summarising the state
     */
    public String toString()
    {
        switch (type)
        {
         //   case (GROW_MODE_DEND_GROW):
         //       return GROW_MODE_DEND_GROW_STRING;
         //   case (GROW_MODE_AXON_GROW):
         //       return GROW_MODE_AXON_GROW_STRING;
            case (GROW_MODE_JUMP):
                return GROW_MODE_JUMP_STRING;
        }
        return "Unknown GrowMode";
    }
    public int getType()
    {
        return type;
    }
    public void setType(int type)
    {
        this.type = type;
    }



}
