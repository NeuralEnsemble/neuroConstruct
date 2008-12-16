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

package ucl.physiol.neuroconstruct.utils;



import java.util.logging.Level;
import java.util.logging.Logger;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.equation.Argument;
import ucl.physiol.neuroconstruct.utils.equation.EquationException;
import ucl.physiol.neuroconstruct.utils.equation.EquationUnit;
import ucl.physiol.neuroconstruct.utils.equation.Expression;

/**
 * Generates numbers according to a fixed pattern (see NumberGenerator)
 * or a function of the radius or the soma to soma distance, gived by the user.
 * Any number of these can be generated from a single Weigh Generator
 *
 * @author Padraig Gleeson & Matteo Farinella
 *  
 */

public class WeightGenerator extends NumberGenerator
{    
    static final long serialVersionUID = -566478567883676739L;
    
    private transient ClassLogger logger = new ClassLogger("NumberGenerator");
    
    public static final int FUNCTION = 3;
    
    public EquationUnit inhomoExpr; 
    //public String inhomoExprString;
    
    public boolean somaToSoma = false; // if this flag is true the expression is a function of the soma to soma distance
      
    /**
     * Generates a simple WeighGenerator as a simple function of r
     */
    public WeightGenerator()
    {
        String inhomoExprString = "r";
        try {

            this.inhomoExpr = Expression.parseExpression(inhomoExprString, VolumeBasedConnGenerator.allowedVars);
        } catch (EquationException ex) 
        {
            logger.logError("Problem with expression: "+ inhomoExprString);
            
        }

    }
    
    /**
     * Generates a simple WeighGenerator with a fixed value of fixedNum
     */
    public WeightGenerator(String expr, boolean soma) throws EquationException
    {
        this.inhomoExpr = Expression.parseExpression(expr, VolumeBasedConnGenerator.allowedVars);    
    }
    
    
    
    public boolean isTypeFunction()
    {
        return this.distributionType == FUNCTION;
    }
    
    public void initialiseAsFunction(String expr, boolean soma) throws EquationException
    {
        this.distributionType = FUNCTION;
        this.inhomoExpr = Expression.parseExpression(expr, VolumeBasedConnGenerator.allowedVars);
        this.somaToSoma = soma;
    }
    
    
    public static WeightGenerator initialiseFromNumGenerator(NumberGenerator numGen)
    {
       
        WeightGenerator wg;
        
        wg = new WeightGenerator();
     
        wg.setDistributionType(numGen.distributionType);
        wg.setNumberType(numGen.numberType);
        wg.setMax(numGen.max);
        wg.setMin(numGen.min);
        wg.setMean(numGen.mean);
        wg.setFixedNum(numGen.fixedNum);
        wg.setStdDev(numGen.stdDev);
        
        return wg;
        
    
    }
    
    
    
    @Override
    public String toString()
    {
        if (this.somaToSoma)
        {
        return ("Expression: " + this.inhomoExpr.toString() + " function of soma to soma distance");
        }
        else
        {
        return ("Expression: " + this.inhomoExpr.toString() + " function of the radius");
        }
        
    }
     
    /**
     * Gives a short representation of the string for textfields, etc.
     */
     @Override
    public String toShortString()
    {
            return (this.inhomoExpr.getNiceString());
    }
    
     
    public float getNextNumber(float dist)
    {

        Argument r = new Argument("r", dist);
        Argument[] args = new Argument[]{r};
        
        double y = 0.0;
        
        try {
            
            y = this.inhomoExpr.evaluateAt(args);
            
            
        } catch (EquationException ex) {
            Logger.getLogger(WeightGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return (float)y;
    }
    
    public static void main(String[] args)
    {
        try
        {
            WeightGenerator ng1 = new WeightGenerator();
        
        ng1.initialiseAsFunction("(r*r)", true);

        WeightGenerator ng3 = new WeightGenerator(ng1.toShortString(), ng1.isSomaToSoma());

        System.out.println("Old NG: "+ ng1.toShortString());
        System.out.println("New NG: "+ ng3.toShortString());

        for (int i = 0; i < 5; i++)
        {
            System.out.println("i:"+i+" Next Int: "+ ng1.getNextNumber(i));
            //System.out.println("Next Float: "+ ng.getNextFloat());
        }
        
        }
        catch (EquationException e) { System.out.println("ERROR: " + e.getMessage());}
    }

    public void setInhomoExpr(EquationUnit inhomoExpr) {
        this.inhomoExpr = inhomoExpr;
    }

    public EquationUnit getInhomoExpr() {
        return inhomoExpr;
    }
    
    public void setInhomoExpr(String expr) throws EquationException
    {
        this.inhomoExpr = Expression.parseExpression(expr, VolumeBasedConnGenerator.allowedVars);
        //this.inhomoExprString = expr;
    }

    public boolean isSomaToSoma() {
        return somaToSoma;
    }
    
    public void setSomaToSoma(boolean soma)
    {
        this.somaToSoma = soma;
    }
    
    

}
