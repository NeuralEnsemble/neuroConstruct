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

import java.util.*;


/**
 * Helper class for parsing equations
 *
 * @author Padraig Gleeson
 *  
 */

public class RelationalOperator
{
    public static final String EQUAL = "==";
    public static final String NOT_EQUAL = "!=";
    public static final String GREATER_THAN_OR_EQUAL = ">=";
    public static final String GREATER_THAN = ">";
    public static final String LESS_THAN_OR_EQUAL = "<=";
    public static final String LESS_THAN = "<";
    
    public String operator = null;
    
    public static ArrayList<RelationalOperator> allROs = new ArrayList<RelationalOperator>();
    
    static 
    {
        allROs.add(new RelationalOperator(EQUAL));
        allROs.add(new RelationalOperator(NOT_EQUAL));
        allROs.add(new RelationalOperator(GREATER_THAN_OR_EQUAL));
        allROs.add(new RelationalOperator(GREATER_THAN));
        allROs.add(new RelationalOperator(LESS_THAN_OR_EQUAL));
        allROs.add(new RelationalOperator(LESS_THAN));
    }
    
    private RelationalOperator(String operator)
    {
        this.operator = operator;
    }
    
    public boolean evaluate(double x, double y)
    {
        if (operator.equals(EQUAL)) return x==y;
        else if (operator.equals(NOT_EQUAL)) return x!=y;
        else if (operator.equals(GREATER_THAN)) return x>y;
        else if (operator.equals(GREATER_THAN_OR_EQUAL)) return x>=y;
        else if (operator.equals(LESS_THAN)) return x<y;
        else if (operator.equals(LESS_THAN_OR_EQUAL)) return x<=y;
        
        return false;
    }
    
    public String toString()
    {
        return operator;
    }

    public static void main(String[] args)
    {
        System.out.println("All ROs: "+ RelationalOperator.allROs);
        
        for(RelationalOperator ro: RelationalOperator.allROs)
        {
            double x = 2;
            double y=3;

            System.out.println("x: "+x+", y: "+y+", ro: "+ro+ ", evaluated: "+ ro.evaluate(x, y));
        }
    }
    
    
}
