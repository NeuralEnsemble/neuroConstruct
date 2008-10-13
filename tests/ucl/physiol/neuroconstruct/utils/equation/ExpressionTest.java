/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.utils.equation;


import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.Result;
import test.MainTest;
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
                                            "100.0 *(  (v - -0.0089) / (-0.0050) ) / (1 - exp(-1 * ( (v - -0.0089) / (-0.0050) )))",
                                            "v-(-0.065)",};
        
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
        Argument[] v90deg = new Argument[]{new Argument(v.getName(), Math.PI/2)};
        
        assertEquals(eqnUnits.get(2).evaluateAt(v0), 0.00024893534183931975, 0);
        
        assertEquals(eqnUnits.get(3).evaluateAt(v90deg), 1, 0);
        assertEquals(eqnUnits.get(4).evaluateAt(v0), 1, 0);
        assertEquals(eqnUnits.get(5).evaluateAt(v0), 0, 0);
        assertEquals(eqnUnits.get(6).evaluateAt(v1), 0, 0);
        assertEquals(eqnUnits.get(7).evaluateAt(v1), 0, 0);
        
    }
    
    
    public static void main(String[] args)
    {
        ExpressionTest ct = new ExpressionTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }

}