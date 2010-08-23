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

import ucl.physiol.neuroconstruct.utils.*;

/**
 * Helper class for storing the conditions on the number of target cells which
 * can be connected to by source cells, and vice versa
 *
 * @author Padraig Gleeson
 *  
 */


public class ConnectivityConditions
{
    public final static int SOURCE_TO_TARGET = 0;
    public final static int TARGET_TO_SOURCE = 1;

    /**
     * Whether the generation begins by picking a source cell, and the
     * connections to the target cells picked a/c to these conditions, or vice versa
     *
     */
    private int generationDirection = SOURCE_TO_TARGET;

    /**
     * The number of connections per cell in the source (or target, if
     * generationDirection = TARGET_TO_SOURCE) cell group
     */
    private NumberGenerator numConnsInitiatingCellGroup = new NumberGenerator(1);

    /**
     * If true cells in the source (or target, if generationDirection =
     * TARGET_TO_SOURCE) cell group, will be connected to any cell in the
     * opposite group at most once
     *
     */
      private boolean onlyConnectToUniqueCells = false;


    /**
     * Maximum number of presynaptic cells (either the same cell or different cells)
     * which can be connected to each postsynaptic cell, or vice versa if
     * generationDirection = TARGET_TO_SOURCE
     *
     */
    private int maxNumInitPerFinishCell = Integer.MAX_VALUE;
    
    /*
     * When source & target cell groups are the same, this indicates whether synapses from
     * a cell to itself are allowed
     */ 
    private boolean allowAutapses = true;
    
    private boolean noRecurrent = false;

    private PrePostAllowedLocs prePostAllowedLoc = new PrePostAllowedLocs();


    public ConnectivityConditions()
    {
    };



    /**
     * Return a simple string representation...
     * @return A string summarising the state
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (generationDirection == SOURCE_TO_TARGET)
        {
            sb.append("Gen Src->Tgt, ");
        }
        else
        {
            sb.append("Gen Tgt->Src, ");
        }
        sb.append(numConnsInitiatingCellGroup.toShortString());

        if (generationDirection == SOURCE_TO_TARGET)
        {
            sb.append(" per Src, max: "+getMaxNumInitPerFinishCellString()+ " per Tgt");
        }
        else
        {
            sb.append(" per Tgt, max: "+getMaxNumInitPerFinishCellString()+ " per Src");
        }
        if (onlyConnectToUniqueCells)
        {
            sb.append(", unique conns");
        }
        if (allowAutapses)
        {
            sb.append(", autapses allowed");
        }
        if (noRecurrent)
        {
            sb.append(", direct recurrent connections not allowed");
        }
        sb.append(", "+prePostAllowedLoc.toString());


        return sb.toString();
    }

    public String getMaxNumInitPerFinishCellString()
    {
        if (this.maxNumInitPerFinishCell==Integer.MAX_VALUE) return "MAX";
        return maxNumInitPerFinishCell+"";
    }

    public int getMaxNumInitPerFinishCell()
    {
        return maxNumInitPerFinishCell;
    }



    public void setMaxNumInitPerFinishCell(String value) throws NumberFormatException
    {
        value = value.trim();
        if (value.equalsIgnoreCase("MAX")) maxNumInitPerFinishCell = Integer.MAX_VALUE;
        else maxNumInitPerFinishCell = Integer.parseInt(value);
    }

    public void setMaxNumInitPerFinishCell(int value)
    {
        maxNumInitPerFinishCell = value;
    }


    public static void main(String[] args)
    {
        ConnectivityConditions cc = new ConnectivityConditions();
        System.out.println("cc old: "+ cc.toString());
        cc.generationDirection = TARGET_TO_SOURCE;
        //cc.onlyConnectToUniqueCells = true;
        cc.numConnsInitiatingCellGroup = new NumberGenerator(777);

        System.out.println("cc old: "+ cc.toString());
        ConnectivityConditions cc2 = new ConnectivityConditions();
        NumberGenerator num = new NumberGenerator(1);
        num.setDistributionType(NumberGenerator.GAUSSIAN_NUM);
        num.setMean(0.4f);
        cc2.setNumConnsInitiatingCellGroup(num);

        System.out.println("cc new: "+ cc2.toString());

    }
    public int getGenerationDirection()
    {
        return generationDirection;
    }
    public NumberGenerator getNumConnsInitiatingCellGroup()
    {
        return numConnsInitiatingCellGroup;
    }
    public void setGenerationDirection(int generationDirection)
    {
        this.generationDirection = generationDirection;
    }
    public void setNumConnsInitiatingCellGroup(NumberGenerator numConnsInitiatingCellGroup)
    {
        this.numConnsInitiatingCellGroup = numConnsInitiatingCellGroup;
    }



    public boolean isOnlyConnectToUniqueCells()
    {
        return onlyConnectToUniqueCells;
    }


    public void setOnlyConnectToUniqueCells(boolean onlyConnectToUniqueCells)
    {
        this.onlyConnectToUniqueCells = onlyConnectToUniqueCells;

    }


    public boolean isAllowAutapses()
    {
        return allowAutapses;
    }
    
    
    public boolean isNoRecurrent()
    {
        return noRecurrent;
    }    


    public void setAllowAutapses(boolean aa)
    {
        this.allowAutapses = aa;

    }
    
    public void setNoRecurrent(boolean aa)
    {
        this.noRecurrent = aa;
    }

    public PrePostAllowedLocs getPrePostAllowedLoc()
    {
        return prePostAllowedLoc;
    }

    public void setPrePostAllowedLoc(PrePostAllowedLocs prePostAllowedLoc)
    {
        this.prePostAllowedLoc = prePostAllowedLoc;
    }
    
    


}
