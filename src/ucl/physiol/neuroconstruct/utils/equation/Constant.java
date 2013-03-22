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


package ucl.physiol.neuroconstruct.utils.equation;

import ucl.physiol.neuroconstruct.gui.plotter.PlotterFrame;
import ucl.physiol.neuroconstruct.gui.plotter.PlotManager;
import ucl.physiol.neuroconstruct.dataset.DataSet;


/**
 * Helper class for parsing equations
 *
 * @author Padraig Gleeson
 *  
 */

public class Constant extends EquationUnit
{
    static final long serialVersionUID = -8761786276L;
    
    private double value = 0;

    public Constant(double value)
    {
        super("Constant = "+value);
        this.value = value;
    }

    public Constant()
    {
    }
    
    

    public double evaluateAt(Argument[] args) throws EquationException
    {
        return value;
    };


    public String getNiceString()  // who doesn't like a nice string..?
    {
        if ( (int) value == value)
        {
            if (value<0) return "(" + (int) value + ")";
            return (int) value + "";
        }
        if (value<0) return "("+ value + ")";

        return value + "";
    }


    public String toString()
    {
        return getNiceString();
    }
    
    
    
    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof Constant)
        {
            Constant c = (Constant)obj;
            if (c.value == value) return true;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + (int)Double.doubleToLongBits(value);
        return hash;
    }

    public double getValue()
    {
        return value;
    }

    public void setValue(double value)
    {
        this.value = value;
    }


    public static void main(String[] args)
    {

    try
    {
        double minx = 0;
        double maxx = 10;
        int numPoints = (int)(maxx - minx)*200 +1;

        Variable x = new Variable("x");
        Variable[] vars = new Variable[]{x};

        float taur = 1;
        float taud = 10f;
        double tp = ((taur*taud)/(taud - taur)) * Math.log(taud/taur);
        double factor = 1/ (Math.exp(-tp/taud) - Math.exp(-tp/taur));

        String exp1k = factor+ " * (exp(-1* x / "+taud+") - exp(-1* x / "+taur+"))";

        System.out.println("Factor: "+factor);

        Constant cc = new Constant(1);

        System.out.println("Const: " + (EquationUnit)cc);

        //String exp2k = "exp(-1* x / "+taud+")";
        //String exp1k = "exp(-1* x / "+taud+")";

        String exp2k = "-1* exp(-1* x / "+taur+")";

        System.out.println("exp1k: "+ exp1k);

        EquationUnit func1k = Expression.parseExpression(exp1k, vars);

        System.out.println("func1k: "+ func1k.getNiceString());

        String plotName1k = "y = " + func1k.getNiceString() +" or y = "+ exp1k;

        DataSet ds1k = new DataSet(plotName1k, plotName1k, "", "", "", "");


        System.out.println("exp2k: " + exp2k);

        EquationUnit func2k = Expression.parseExpression(exp2k, vars);

        System.out.println("func2k: " + func2k.getNiceString());

        String plotName2k = "y = " + func2k.getNiceString() + " or y = " + exp2k;

        DataSet ds2k = new DataSet(plotName2k, plotName2k, "", "", "", "");

        for (int i = 0; i < numPoints; i++)
        {
            double nextXval = minx + (((maxx-minx)/(numPoints-1))* i);

            Argument[] a0 = new Argument[]{new Argument(x.getName(), nextXval)};

            ds1k.addPoint(nextXval, func1k.evaluateAt(a0));
            ds2k.addPoint(nextXval, func2k.evaluateAt(a0));
        }

        PlotterFrame frame = PlotManager.getPlotterFrame("Some plots", true, true);

        frame.addDataSet(ds1k);
        //frame.addDataSet(ds2k);

        frame.setVisible(true);

    }
    catch (EquationException ex)
    {
        ex.printStackTrace();
    }
}

}
