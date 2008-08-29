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

public class CosFunctionUnit extends FunctionUnit
{
    static final long serialVersionUID = -5473567L;
                     

    public CosFunctionUnit(EquationUnit internalEqn)
    {
        super(BasicFunctions.COSINE, internalEqn);
    }

    public CosFunctionUnit()
    {
    }
    
    

    public double evaluateAt(Argument[] args) throws EquationException
    {
        return Math.cos(internalEqn.evaluateAt(args));
    }    
    


}
