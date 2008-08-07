/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell;

import java.util.ArrayList;
import org.junit.*;
import ucl.physiol.neuroconstruct.utils.equation.*;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class VariableParameterTest 
{
    VariableParameter vp1 = null;
    
    public VariableParameterTest() 
    {
    }


    @Before
    public void setUp() throws EquationException
    {
        System.out.println("---------------   setUp() VariableParameterTest");
        String expression1 = "A + B*(p+C)";


        Variable p = new Variable("p");
        Argument a = new Argument("A", 2);
        Argument b = new Argument("B", 4);
        Argument c = new Argument("C", -1);

        ArrayList<Argument> expressionArgs1 =  new ArrayList<Argument>();

        expressionArgs1.add(a);
        expressionArgs1.add(b);
        expressionArgs1.add(c);

        vp1 = new VariableParameter("cap", expression1, p, expressionArgs1);
            
        System.out.println("Var param: "+ vp1); 
    }


    @Test
    public void testEvaluateAt() throws Exception 
    {
        System.out.println("---  testEvaluateAt...");
        
        double val1 = vp1.evaluateAt(0);
        
        assertEquals(val1, -2, 0);
        
        double val2 = vp1.evaluateAt(1);
        
        assertEquals(val2, 2, 0);
    }


}