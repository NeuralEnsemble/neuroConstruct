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

import ucl.physiol.neuroconstruct.utils.*;

/**
 * Helper class for storing the conditions on the number of target cells which
 * can be connected to by source cells, and vice versa
 *
 * @author Padraig Gleeson
 * @version 1.0.3
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
    private NumberGenerator numConnsInitiatingCellGroup = new NumberGenerator();

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



    public ConnectivityConditions()
    {
    };



    /**
     * Return a simple string representation...
     * @return A string summarising the state
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
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
        cc.generationDirection = cc.TARGET_TO_SOURCE;
        //cc.onlyConnectToUniqueCells = true;
        cc.numConnsInitiatingCellGroup = new NumberGenerator(777);

        System.out.println("cc old: "+ cc.toString());
        ConnectivityConditions cc2 = new ConnectivityConditions();
        NumberGenerator num = new NumberGenerator();
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



}
