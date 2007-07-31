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


package ucl.physiol.neuroconstruct.utils.equation;


/**
 * Helper class for parsing equations
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

public class BasicFunctions
{

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
             FunctionUnit eq = new FunctionUnit(SINE, internalEqn)
             {
                 public double evaluateAt(Argument[] args) throws EquationException
                 {
                     return Math.sin(internalEqn.evaluateAt(args));
                 }
             };
             return eq;
         }
         else if (name.equals(COSINE))
         {
             FunctionUnit eq = new FunctionUnit(COSINE, internalEqn)
             {
                 public double evaluateAt(Argument[] args) throws EquationException
                 {
                     return Math.cos(internalEqn.evaluateAt(args));
                 }
             };
             return eq;
         }
         else if (name.equals(TANGENT))
         {
             FunctionUnit eq = new FunctionUnit(TANGENT, internalEqn)
             {
                 public double evaluateAt(Argument[] args) throws EquationException
                 {
                     return Math.tan(internalEqn.evaluateAt(args));
                 }
             };
             return eq;
         }
         else if (name.equals(EXPONENT))
         {
             FunctionUnit eq = new FunctionUnit(EXPONENT, internalEqn)
             {
                 public double evaluateAt(Argument[] args) throws EquationException
                 {
                     return Math.exp(internalEqn.evaluateAt(args));
                 }
             };
             return eq;
         }
         else if (name.equals(LN))
         {
             FunctionUnit eq = new FunctionUnit(LN, internalEqn)
             {
                 public double evaluateAt(Argument[] args) throws EquationException
                 {
                     return Math.log(internalEqn.evaluateAt(args));
                 }
             };
             return eq;
         }
         else if (name.equals(LOG))
         {
             FunctionUnit eq = new FunctionUnit(LOG, internalEqn)
             {
                 public double evaluateAt(Argument[] args) throws EquationException
                 {
                     return Math.log(internalEqn.evaluateAt(args));
                 }
             };
             return eq;
         }



         throw new EquationException("Function "+ name + " not found");
     }


}
