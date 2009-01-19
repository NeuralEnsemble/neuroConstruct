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
