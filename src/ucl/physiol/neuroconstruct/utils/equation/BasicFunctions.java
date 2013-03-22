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
import ucl.physiol.neuroconstruct.utils.equation.functions.*;


/**
 * Helper class for parsing equations
 *
 * @author Padraig Gleeson
 *  
 */

public class BasicFunctions implements Serializable
{
    static final long serialVersionUID = 876768226452L;

    public static String SINE = "sin";
    public static String COSINE = "cos";
    public static String TANGENT = "tan";
    public static String EXPONENT = "exp";
    public static String LN = "ln";
    public static String LOG = "log";
    public static String HEAVISIDE = "H";
    public static String RANDOM = "random";
    public static String SQUAREROOT = "sqrt";
    //public static String SQUARE = "sqr";      // Note: gets confused with sqrt when parsing eqns! Better name?

    public static String[] allFunctions
        = new String[]{SINE,COSINE,TANGENT,EXPONENT,LN,LOG, HEAVISIDE, RANDOM, SQUAREROOT};


 /*
    static
    {
        sin = new FunctionUnit("sine", x)
    {
        public double evaluateAt(Argument[] args) throws EquationException
        {
            return Math.sin(internalEqn.evaluateAt(args));
        }
    };
}

*/

     public static FunctionUnit getFunction(String name, EquationUnit internalEqn) throws EquationException
     {
         if (name.equals(SINE))
         {
             FunctionUnit eq = new SineFunctionUnit(internalEqn);
             return eq;
         }
         else if (name.equals(COSINE))
         {
             
             FunctionUnit eq = new CosFunctionUnit(internalEqn);
             return eq;
         }
         else if (name.equals(TANGENT))
         {
             FunctionUnit eq = new TanFunctionUnit(internalEqn);
             return eq;
         }
         else if (name.equals(EXPONENT))
         {
             
             FunctionUnit eq = new ExpFunctionUnit(internalEqn);
             return eq;
         }
         else if (name.equals(LN))
         {
             
             FunctionUnit eq = new LnFunctionUnit(internalEqn);
             return eq;
         }
         else if (name.equals(LOG))
         {
             
             FunctionUnit eq = new LogFunctionUnit(internalEqn);
             return eq;
         }
         else if (name.equals(HEAVISIDE))
         {
             
             FunctionUnit eq = new HeavisideStepFunctionUnit(internalEqn);
             return eq;
         }
         else if (name.equals(RANDOM))
         {

             FunctionUnit eq = new RandomFunctionUnit(internalEqn);
             return eq;
         }
         /*
         else if (name.equals(SQUARE))
         {

             FunctionUnit eq = new SqrFunctionUnit(internalEqn);
             return eq;
         }*/
         else if (name.equals(SQUAREROOT))
         {

             FunctionUnit eq = new SqrtFunctionUnit(internalEqn);
             return eq;
         }



         throw new EquationException("Function "+ name + " not found");
     }


}
