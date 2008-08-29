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


package ucl.physiol.neuroconstruct.utils.equation.functions;

import ucl.physiol.neuroconstruct.utils.equation.*;


/**
 * Helper class for parsing equations
 *
 * @author Padraig Gleeson
 *  
 */

public class SineFunctionUnit extends FunctionUnit
{
    static final long serialVersionUID = -97991769713623L;
                     

    public SineFunctionUnit(EquationUnit internalEqn)
    {
        super(BasicFunctions.SINE, internalEqn);
    }

    public SineFunctionUnit()
    {
    }
    
    

    public double evaluateAt(Argument[] args) throws EquationException
    {
        return Math.sin(internalEqn.evaluateAt(args));
    }    
    


}
