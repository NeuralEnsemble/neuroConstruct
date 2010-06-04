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
        super(1);
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
        super(1);
        this.distributionType = FUNCTION;
        this.somaToSoma = soma;
        this.inhomoExpr = Expression.parseExpression(expr, VolumeBasedConnGenerator.allowedVars);    
    }
    
    
    /**
     * Generates a nominal value for the number. Useful only for giving a rough indication of what the value will be.
     * Will be the function evaluated at 100um, or the evaluation according to NumberGenerator.getNominalNumber()
     */
    @Override
    public float getNominalNumber()
    {
        if (this.distributionType != FUNCTION)
            return super.getNominalNumber();
        
        float nominalDistance = 100;
        return this.getNextNumber(nominalDistance);
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
        if (!this.isTypeFunction())
        {
            return super.toString();
        }
        if (this.somaToSoma)
        {
            return ("Expression: " + this.inhomoExpr.toString() + " function of soma to soma distance");
        }
        else
        {
            return ("Expression: " + this.inhomoExpr.toString() + " function of the distance between connection points");
        }
        
    }
     
    /**
     * Gives a short representation of the string for textfields, etc.
     */
    @Override
    public String toShortString()
    {
        if (!this.isTypeFunction())
        {
            return super.toShortString();
        }
        if (!somaToSoma)
            return (this.inhomoExpr.getNiceString());
        
        return (this.inhomoExpr.getNiceString()+" (soma to soma)");
        
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
