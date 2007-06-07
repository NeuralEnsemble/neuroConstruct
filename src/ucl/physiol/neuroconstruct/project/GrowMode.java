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

/**
 * Helper class for specifying grow modes, i.e. specify whether the axon showld grow
 * to meet the dendrite, vice versa or neither
 *
 * @author Padraig Gleeson
 * @version 1.0.3
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
