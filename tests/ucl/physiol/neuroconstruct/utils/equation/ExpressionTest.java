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


import java.util.ArrayList;
import org.junit.*;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
import static org.junit.Assert.*;

/**
 *
 * @author Padraig
 */
public class ExpressionTest {

    public ExpressionTest() {
    }



    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreateExpression() throws EquationException 
    {
        System.out.println("---  testCreateExpression...");
        
        String[] expressions = new String[]{" 0.058*exp(-(v-10)/15)",
                                            "-exp((-1*(t1*t2)/(t2-t1)*log(t2/t1))/t1) + exp((-1*(t1*t2)/(t2-t1)*log(t2/t1))/t2)",
                                            "0.005 *(exp(-0.05*(v - (-60))))",
                                            "sin(v)",
                                            "cos(v)",
                                            "tan(v)",
                                            "log(v)",
                                            "ln(v)",
                                            "random(v)",
                                            "100.0 *(  (v - -0.0089) / (-0.0050) ) / (1 - exp(-1 * ( (v - -0.0089) / (-0.0050) )))",
                                            "v-(-0.065)",
                                            "sin(sin(v)-1)",
                                            "sqrt(v)",
                                            "H(100+ (-v))",
                                            "v^2 + v^3",
                                            "2^3+ 2 / 2 ^ 2 * 4"};
        
        Variable v = new Variable("v");
        Variable t = new Variable("t");
        Variable t1 = new Variable("t1");
        Variable t2 = new Variable("t2");
        
        Variable[] vars = new Variable[]{v, t, t1, t2};
        
        ArrayList<EquationUnit> eqnUnits = new ArrayList<EquationUnit>();
        
        for(String expression:expressions)
        {
            EquationUnit eqn = Expression.parseExpression(expression, vars);

            System.out.println("Expression line " + expression + " parsed as: " + eqn.getNiceString());
            eqnUnits.add(eqn);
        }
        
        
        Argument[] v0 = new Argument[]{new Argument(v.getName(), 0)};
        Argument[] v1 = new Argument[]{new Argument(v.getName(), 1)};
        Argument[] v3 = new Argument[]{new Argument(v.getName(), 3)};
        Argument[] v99 = new Argument[]{new Argument(v.getName(), 99)};
        Argument[] v101 = new Argument[]{new Argument(v.getName(), 101)};
        double halfPi = Math.PI/2;
        
        Argument[] v90deg = new Argument[]{new Argument(v.getName(), halfPi)};
        
        assertEquals(eqnUnits.get(2).evaluateAt(v0), 0.00024893534183931975, 0);
        
        assertEquals(eqnUnits.get(3).evaluateAt(v90deg), 1, 0);
        assertEquals(eqnUnits.get(4).evaluateAt(v0), 1, 0);
        assertEquals(eqnUnits.get(5).evaluateAt(v0), 0, 0);
        assertEquals(eqnUnits.get(6).evaluateAt(v1), 0, 0);
        assertEquals(eqnUnits.get(7).evaluateAt(v1), 0, 0);
        
        assertTrue(eqnUnits.get(8).evaluateAt(v90deg)<=halfPi);
        assertTrue(eqnUnits.get(8).evaluateAt(v90deg)>=0);
        assertTrue(eqnUnits.get(11).evaluateAt(v90deg)==0);

        assertEquals(eqnUnits.get(12).evaluateAt(v90deg), Math.sqrt(halfPi), 0);
        assertEquals(eqnUnits.get(13).evaluateAt(v99), 1, 0);
        assertEquals(eqnUnits.get(13).evaluateAt(v101), 0, 0);
        assertEquals(eqnUnits.get(14).evaluateAt(v1), 2, 0);
        assertEquals(eqnUnits.get(14).evaluateAt(v3), 36, 0);
        assertEquals(eqnUnits.get(15).evaluateAt(v3), 10, 0);
        
    }
    
    
    public static void main(String[] args)
    {
        ExpressionTest ct = new ExpressionTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }

}