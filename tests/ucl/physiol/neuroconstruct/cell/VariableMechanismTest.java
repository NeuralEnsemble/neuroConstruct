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
import java.beans.*;
import java.io.*;

/**
 *
 * @author padraig
 */
public class VariableMechanismTest 
{
    //VariableParameter vp1 = null;
    //VariableParameter vp2 = null;
    
    //Variable p = null;
    

    public static VariableMechanism getVariableMechanism() throws EquationException 
    {
        String expression1 = "A + B*(p+C) + sin(p * 0)";
        
        VariableParameter vp1 = null;
        VariableMechanism vm = null;
        //VariableParameter vp2 = null;
    
        Variable p = null;

        p = new Variable("p");
        Argument a = new Argument("A", 2);
        Argument b = new Argument("B", 4);
        Argument c = new Argument("C", 1);

        ArrayList<Argument> expressionArgs1 =  new ArrayList<Argument>();

        expressionArgs1.add(a);
        expressionArgs1.add(b);
        expressionArgs1.add(c);

        vp1 = new VariableParameter("cap", expression1, p, expressionArgs1);
            
        System.out.println("Var param 1: "+ vp1); 
        
        

        vm = new VariableMechanism("cm", vp1);
        

        System.out.println("VariableMechanism: " + vm);
        
        return vm;
        
    }


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    
    @Test
    public void testCloneAndEquals() throws EquationException
    {
        System.out.println("---  testCloneAndEquals...");
        
        VariableMechanism vm1 = getVariableMechanism();
        
        VariableMechanism vm2 = (VariableMechanism)vm1.clone();
        
        System.out.println("Testing equality of: "+ vm1);
        
        System.out.println("with:                "+ vm2);
        
        assertEquals(vm1, vm2);
        
        vm2.setName("xgjchgj");
        
        assertNotSame(vm1, vm2);
        
    }

    
    @Test
    public void testEvaluateAt() throws Exception 
    {
        System.out.println("---  testEvaluateAt...");

        VariableMechanism vm = getVariableMechanism();
        
        double val1 = vm.evaluateAt(0);
        double val2 = vm.evaluateAt(10);
        
        assertEquals(val1, 6, 0);
        assertEquals(val2, 46, 0);
        
    }
    
    @Test
    public void testSaveLoad()  throws FileNotFoundException, IOException, EquationException
    {
        System.out.println("---  testSaveLoad...");
        
        VariableMechanism vm = getVariableMechanism();
        
        XMLEncoder xmlEncoder = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        
        File f = new File("../temp/TestVM.ser");

        System.out.println("Saving to: "+f.getCanonicalPath());
        
        fos = new FileOutputStream(f);
        bos = new BufferedOutputStream(fos);
        xmlEncoder = new XMLEncoder(bos);
        
        Object o1 = vm;
        
        System.out.println("Pre:  "+ o1);
        
        xmlEncoder.writeObject(o1);
        xmlEncoder.close();
        
        FileInputStream fis = new FileInputStream(f);
        BufferedInputStream bis = new BufferedInputStream(fis);
        XMLDecoder xd = new XMLDecoder(bis);
        
        Object o2 = xd.readObject();
        
        System.out.println("Post: "+ o2);
        
        
        assertEquals(o2, o1);
        
        ///VariableMechanism vm = getVariableMechanism();
        
        
        ///assertEquals(val2, 46, 0);
        
     
    }

 
    public static void main(String[] args) throws EquationException
    {
        VariableMechanismTest ct = new VariableMechanismTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }



}