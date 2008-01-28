/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.mechanisms;

import java.util.*;

/**
 * Class holding info on single plot relevant to a Cell Mechanism
 *
 * @author Padraig Gleeson
 *  
 */

public class CellMechanismPlot
{
    public CellMechanismPlot()
    {
        try
        {
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    protected String plotName = null;
    protected float maxValue = 1;
    protected  float minValue = -1;
    protected String independentVariable = "x";
    protected String expression = "cos(x)";

    protected Hashtable parameters = new Hashtable();



    public String toString()
    {
        return plotName + ": f("
            +independentVariable
            + ") = "+expression+"";
    }

    private void jbInit() throws Exception
    {
    }

    public void setPlotName(String plotName)
    {
        this.plotName = plotName;
    }

    public String getPlotName()
    {
        return plotName;
    }

    public String getExpression()
    {
        return expression;
    }


    public String getIndependentVariable()
    {
        return independentVariable;
    }


    public float getMaxValue()
    {
        return maxValue;
    }


    public float getMinValue()
    {
        return minValue;
    }
    public Hashtable getParameters()
    {
        return parameters;
    }


}
