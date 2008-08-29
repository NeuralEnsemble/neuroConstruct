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

    public static String[] allFunctions
        = new String[]{SINE,COSINE,TANGENT,EXPONENT,LN,LOG};


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



         throw new EquationException("Function "+ name + " not found");
     }


}
