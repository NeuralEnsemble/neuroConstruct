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

package ucl.physiol.neuroconstruct.project.cellchoice;

import java.util.*;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.ClassLogger;


/**
 * Base class for all Cell Choosers. This can be extended to allow different subsets of
 * Cell Groups to be selected. The primary purpose of this is to allow elec stims be put
 * on subsets of cell groups, e.g. all cells, a percentage of cells, cells in a 3D
 * region, etc. Alternative future uses may be envisioned
 *
 * @author Padraig Gleeson
 *  
 */


public abstract class CellChooser
{
    static ClassLogger logger = new ClassLogger("CellChooser");

    ArrayList<PositionRecord> cellPositions = null;

    String description = null;

    InternalStringFloatParameter[] parameterList = null;
    
    ArrayList<Integer> cachedCellList = null;
    

    private CellChooser()
    {

    }

    public CellChooser(String description)
    {
        this.description = description;
    }
    
    @Override
    public Object clone(){return null;};
    
    protected ArrayList<PositionRecord> getCopyCellPositions()
    {
        ArrayList<PositionRecord> cpClone = new ArrayList<PositionRecord>();
        
        for(PositionRecord pr: this.cellPositions)
        {
            cpClone.add((PositionRecord)pr.clone());
        }
        
        return cpClone;
        
    }
    

    public String getDescription()
    {
        return description;
    };

    public void initialise(ArrayList<PositionRecord> cellPositions)
    {
        this.cellPositions = cellPositions;
        this.reinitialise();
    }

    public ArrayList<Integer> getOrderedCellList() throws CellChooserException
    {
        ArrayList<Integer> allCells = new ArrayList<Integer>();

        try
        {
            while (true)
            {
                int next = getNextCellIndex();
                logger.logComment("Added cell: " + next);
                allCells.add(next);
            }
        }
        catch (AllCellsChosenException ex)
        {
            logger.logComment("All done...");
        }
        boolean swapped = true;

        while (swapped)
        {
            swapped = false;
            for (int i = 0; i < allCells.size()-1; i++)
            {

                if (allCells.get(i)>allCells.get(i+1))
                {
                    swapped = true;
                    int big = allCells.get(i);
                    int small = allCells.get(i+1);
                    allCells.set(i,small);
                    allCells.set(i+1,big);
                }
            }
        }
        cachedCellList = allCells;
        return  allCells;
    }
    
    public ArrayList<Integer> getCachedCellList() throws CellChooserException
    {
        if (cellPositions == null) throw new CellChooserException("CellChooser not yet initialised");
        return cachedCellList;
    }

    /**
     * Gets the next chosen cell index based on the settings in the sub class
     */
    public int getNextCellIndex() throws AllCellsChosenException, CellChooserException
    {
        if (cellPositions == null) throw new CellChooserException("CellChooser not yet initialised");

        return generateNextCellIndex();
    };
    
    public boolean isInitialised()
    {
        return (cellPositions != null);
    }

    protected abstract int generateNextCellIndex() throws AllCellsChosenException, CellChooserException;

    protected abstract void reinitialise();

    public float getParameterValue(String parameterName)
    {
        for (int i = 0; i < parameterList.length; i++)
        {
            if (parameterList[i].getParameterName().equals(parameterName))
            {
                return parameterList[i].getValue();
            }
        }
        return Float.NaN;
    }

    public String getParameterStringValue(String parameterName)
    {
        for (int i = 0; i < parameterList.length; i++)
        {
            if (parameterList[i].getParameterName().equals(parameterName))
            {
                return parameterList[i].getStringValue();
            }
        }
        return null;
    }

    /**
     * Puts the specified parameter into the proper InternalStringFloatParameter
     * Note: can be over ridden if there are specific checks to be made on the parameter
     */
    public void setParameter(String parameterName,
                             float parameterValue) throws CellChooserException
    {
        for (int j = 0; j < parameterList.length; j++)
        {

            if (parameterList[j].getParameterName().equals(parameterName))
            {
                if (parameterList[j].getStringValue() != null)
                {
                    throw new CellChooserException("Parameter with the name: " + parameterName +
                                                   " has a String value in " +
                                                   this.getClass().getName());
                }

                Object[] accVals = parameterList[j].getAcceptableParameterValues();

                if (accVals != null)
                {
                    StringBuffer sb = new StringBuffer("(");
                    for (int k = 0; k < accVals.length; k++)
                    {
                        float nextVal = ( (Float) accVals[k]).floatValue();
                        if (nextVal == parameterValue)
                        {
                            parameterList[j].setValue(parameterValue);
                            return;
                        }
                        else
                        {
                            sb.append(nextVal);
                            if (k < accVals.length - 2) sb.append(", ");
                            else if (k < accVals.length - 1) sb.append(" or ");
                        }
                    }
                    sb.append(")");
                    throw new CellChooserException("Parameter: " + parameterName + " can only have values: " +
                                                   sb.toString());
                }

                parameterList[j].setValue(parameterValue);
                return;
            }
        }
        throw new CellChooserException("No parameter with the name: " + parameterName + " found in " +
                                       this.getClass().getName());
    }

    /**
     * Puts the specified parameter into the proper InternalStringFloatParameter
     * Note: can be over ridden if there are specific checks to be made on the parameter
     */
    public void setParameter(String parameterName,
                             String parameterStringValue) throws CellChooserException
    {

        logger.logComment("Setting string CellChooser parameter with the name: " + parameterName + " found in " +
                          this.getClass().getName());

        for (int i = 0; i < parameterList.length; i++)
        {
            if (parameterList[i].getParameterName().equals(parameterName))
            {
                if (parameterList[i].getDefaultStringValue() == null)
                {
                    throw new CellChooserException("Parameter with the name: " + parameterName +
                                                   " has a float value in " + this.getClass().getName());
                }

                Object[] accVals = parameterList[i].getAcceptableParameterValues();

                if (accVals != null)
                {
                    StringBuffer sb = new StringBuffer("(");
                    for (int k = 0; k < accVals.length; k++)
                    {
                        String nextVal = (String) accVals[i];
                        if (parameterStringValue.equals(nextVal))
                        {
                            parameterList[i].setStringValue(parameterStringValue);
                            parameterList[i].setValue(Float.NaN);
                            parameterList[i].setDefaultValue(Float.NaN);

                            return;
                        }
                        else
                        {
                            sb.append(nextVal);
                            if (k < accVals.length - 2) sb.append(", ");
                            else if (k < accVals.length - 1) sb.append(" or ");
                        }
                    }
                    sb.append(")");
                    throw new CellChooserException("Parameter: " + parameterName + " can only have values: " +
                                                   sb.toString());
                }

                parameterList[i].setStringValue(parameterStringValue);
                parameterList[i].setValue(Float.NaN);
                parameterList[i].setDefaultValue(Float.NaN);
                return;
            }
        }
        throw new CellChooserException("No parameter with the name: " + parameterName + " found in " +
                                       this.getClass().getName());

    }

    //public Object[] getAcceptableParameterValues()
    // {
//
    //  }

    @Override
    public String toString()
    {
        /*
        StringBuffer sb = new StringBuffer();
        String nameOfClass = this.getClass().getName();

        sb.append(nameOfClass.substring(nameOfClass.lastIndexOf(".") + 1) + "[");

        for (int i = 0; i < parameterList.length; i++)
        {
            if (parameterList[i].getStringValue() == null)
            {
                sb.append(parameterList[i].getParameterName() + " = " + parameterList[i].getValue() );
            }
            else
            {
                sb.append(parameterList[i].getParameterName() + " = " +
                          parameterList[i].getStringValue() );
            }
            if (i < parameterList.length - 1) sb.append(", ");
        }

        sb.append("]");*/

        return (toNiceString());
    }


    public abstract String toNiceString();

    public abstract String toShortString();




    public InternalStringFloatParameter[] getParameterList()
    {
        //System.out.println("getParameterList() called..");
        return parameterList;
    };

    public void setParameterList(InternalStringFloatParameter[] parameterList)
    {
        this.parameterList = parameterList;
    }


}

