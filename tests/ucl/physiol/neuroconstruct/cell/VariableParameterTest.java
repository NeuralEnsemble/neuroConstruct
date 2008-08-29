/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell;

import java.util.ArrayList;

import org.junit.*;
import org.junit.runner.Result;
import test.MainTest;
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



    @Test
    public void testCloneAndEquals() throws EquationException
    {
        System.out.println("---  testCloneAndEquals...");
        
        VariableParameter vp2 = (VariableParameter)vp1.clone();
        
        System.out.println("Testing equality of: "+ vp1);
        System.out.println("with:                "+ vp2);
        
        assertEquals(vp1, vp2);
        
        vp2.setName("xgjchgj");
        
        assertNotSame(vp1, vp2);
        
    }
    
    
    /**
     * Test of equals method, of class VariableParameter.
     */
    @Test
    public void testEquals() throws EquationException
    {
        System.out.println("---  testEquals...");
        String expression2 = "  A + (B*(p+C))  ";
        
        Variable p = new Variable("p");
        Argument a = new Argument("A", 2);
        Argument b = new Argument("B", 4);
        Argument c = new Argument("C", -1);

        ArrayList<Argument> expressionArgs1 =  new ArrayList<Argument>();

        expressionArgs1.add(a);
        expressionArgs1.add(b);
        expressionArgs1.add(c);

        VariableParameter vp2 = new VariableParameter("cap", expression2, p, expressionArgs1);
        
    
        assertEquals(vp1, vp2);
    }

    /**
     * Test of hashCode method, of class VariableParameter.
    
    @Test
    public void testHashCode()
    {
        System.out.println("hashCode");
        VariableParameter instance = new VariableParameter();
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    } */
    
    
    public static void main(String[] args) throws EquationException
    {
        VariableParameterTest ct = new VariableParameterTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }



}