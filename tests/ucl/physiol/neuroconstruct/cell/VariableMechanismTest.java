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
public class VariableMechanismTest 
{
    VariableParameter vp1 = null;
    VariableParameter vp2 = null;
    
    Variable p = null;
    
    VariableMechanism cm = null;

    public VariableMechanismTest() throws EquationException 
    {
        System.out.println("---------------   setUp() VariableMechanismTest");
        String expression1 = "A + B*(p+C)";
        String expression2 = "p*p";


        p = new Variable("p");
        Argument a = new Argument("A", 2);
        Argument b = new Argument("B", 4);
        Argument c = new Argument("C", 1);

        ArrayList<Argument> expressionArgs1 =  new ArrayList<Argument>();
        ArrayList<Argument> expressionArgs2 =  new ArrayList<Argument>();

        expressionArgs1.add(a);
        expressionArgs1.add(b);
        expressionArgs1.add(c);

        vp1 = new VariableParameter("cap", expression1, p, expressionArgs1);
            
        System.out.println("Var param 1: "+ vp1); 
        
        vp2 = new VariableParameter("Rm", expression2, p, expressionArgs2);

        System.out.println("Var param 2: "+ vp2); 
        

        cm = new VariableMechanism("cm");
        
        cm.getParams().add(vp1);

        System.out.println("ChannelMechanism: " + cm);
    }


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    
    @Test
    public void testEvaluateAt() throws Exception 
    {
        System.out.println("---  testEvaluateAt...");

        double val1 = cm.evaluateAt("cap", 0);
        double val2 = cm.evaluateAt("cap", 10);
        
        assertEquals(val1, 6, 0);
        assertEquals(val2, 46, 0);
        
    }


}