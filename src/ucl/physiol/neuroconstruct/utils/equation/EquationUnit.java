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

import java.io.Serializable;
import ucl.physiol.neuroconstruct.utils.GeneralUtils;


/**
 * Helper class for parsing equations
 *
 * @author Padraig Gleeson
 *  
 */

public abstract class EquationUnit implements Serializable
{
    /*
     * A unique id to assist serialisation of the cell class. Don't change this or saved morphologies
     * won't reload!
     */
    private static final long serialVersionUID = -5346073251568817855L;
    
    private String name = null;

    public EquationUnit()
    {
    }

    protected void setName(String name)
    {
        this.name = name;
    }
    
    public EquationUnit(String name)
    {
        this.name = name;
    }


    public String getName()
    {
        return name;
    }

    public abstract String getNiceString();

    @Override
    public abstract String toString();
    
    
    @Override
    public abstract boolean equals(Object obj);
    
    @Override
    public abstract int hashCode();
    


    public abstract double evaluateAt(Argument[] args) throws EquationException;

        public static void main(String[] args)
    {


        try
        {

            Variable r = new Variable("r");
            Variable[] vars = new Variable[]{r};

            String expr1 = "exp(((-1) * (r * r)) / ((2 * 250) * 250))";
            String expr2 = "exp(r * r / -125000)";

            //System.out.println("Parsed: "+ func1m.getNiceString());

            GeneralUtils.timeCheck("Before test", true);

            for(float i=0;i<500;i=i+0.1f)
            {
                EquationUnit func1m = Expression.parseExpression(expr2, vars);
                Argument[] a0 = new Argument[]{new Argument(r.getName(), i)};
                double f = func1m.evaluateAt(a0);
                //System.out.println("i: "+i+", = "+f);
            }

            GeneralUtils.timeCheck("After test", true);
        }

        catch (EquationException ex)
        {
            ex.printStackTrace();
        }
    }
}








