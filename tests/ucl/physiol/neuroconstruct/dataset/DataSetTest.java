/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.dataset;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import ucl.physiol.neuroconstruct.test.MainTest;
import static org.junit.Assert.*;

/**
 *
 * @author padraig
 */
public class DataSetTest
{

    DataSet ds1 = null;
    final int ds1Size = 1000;
    final int commented = 333;
    final String comment = "This is a comment";

    
    DataSet ds2 = null;
    final int ds2Size = 2000;

    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() CellTest");

        ds1 = new DataSet("DataSet1", "DataSet1", "s", "mV", "Time", "Membrane Potential");

        for(int i=0;i<ds1Size;i++)
        {
            ds1.addPoint(i, Math.sin(i));
        }
        ds1.setCommentOnPoint(commented, comment);

        ds2 = new DataSet("DataSet2", "DataSet2", "s", "mV", "Time", "Membrane Potential");

        for(int i=0;i<ds2Size;i++)
        {
            ds2.addPoint(i/10, Math.cos(i));
        }
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test of getPoint method, of class DataSet.
     */
    @Test
    public void testGetPoint() {
        System.out.println("getPoint");
        int index = 0;
        DataSet instance = ds1;
        double[] expResult = new double[]{0, 0};

        double[] result = instance.getPoint(index);
        assertTrue(expResult[0]==result[0]);
        assertTrue(expResult[1]==result[1]);
    }


    /**
     * Test of getComment method, of class DataSet.
     */
    @Test
    public void testGetComment() {
        System.out.println("getComment");
        int pointNum = commented;
        DataSet instance = ds1;
        String expResult = comment;
        String result = instance.getComment(pointNum);
        assertEquals(expResult, result);
    }

    /**
     * Test of getNumberPoints method, of class DataSet.
     */
    @Test
    public void testGetNumberPoints() {
        System.out.println("getNumberPoints");
        DataSet instance = ds1;
        int expResult = ds1Size;
        int result = instance.getNumberPoints();
        assertEquals(expResult, result);
    }

    /**
     * Test of addPoint method, of class DataSet.
     */
    @Test
    public void testAddPoint() {
        System.out.println("addPoint");
        double x = 5555;
        double y = 3333;
        DataSet instance = ds1;

        int num = instance.addPoint(x, y);

        assertEquals(num, ds1Size);

        double[] result = instance.getPoint(num);
        assertTrue(x==result[0]);
        assertTrue(y==result[1]);
    }

    /**
     * Test of areXvalsStrictlyIncreasing method, of class DataSet.
     */
    @Test
    public void testAreXvalsStrictlyIncreasing() throws DataSetException
    {
        System.out.println("areXvalsStrictlyIncreasing");
        DataSet instance = ds1;
        boolean expResult = true;
        boolean result = instance.areXvalsStrictlyIncreasing();
        assertEquals(expResult, result);

        instance.setXValue((int)(ds1Size/2), 444);
        
        expResult = false;
        result = instance.areXvalsStrictlyIncreasing();
        assertEquals(expResult, result);

    }

    /**
     * Test of deletePoint method, of class DataSet.
     */
    @Test
    public void testDeletePoint() throws DataSetException
    {
        System.out.println("deletePoint");
        int pointNum = 0;
        DataSet instance = ds1;

        System.out.println("Before: "+ ds1.toString());
        instance.deletePoint(pointNum);

        System.out.println("After delete 1: "+ ds1.toString());

        assertTrue(ds1.areXvalsStrictlyIncreasing());

        instance.deletePoint(ds1.getNumberPoints()-1);

        assertTrue(ds1.areXvalsStrictlyIncreasing());

        instance.deletePoint((int)(ds1.getNumberPoints()/2));

        assertFalse(ds1.areXvalsStrictlyIncreasing());
    }

    /**
     * Test of getXSpacing method, of class DataSet.
     */
    @Test
    public void testGetXSpacing() throws DataSetException
    {
        System.out.println("getXSpacing");
        DataSet instance = ds1;
        double expResult = 1;
        double result;
        try {
            result = instance.getXSpacing();
            assertEquals(expResult, result, 0.0);

        }
        catch (DataSetException ex)
        {
            fail("Not evenly spaced.");
        }

        instance.deletePoint((int)(ds1.getNumberPoints()/2));

        try {
            result = instance.getXSpacing();
            fail("Should not be evenly spaced.");

        }
        catch (DataSetException ex)
        {
        }
    }

    /**
     * Test of getYvalue method, of class DataSet.
     */
    @Test
    public void testGetYvalue() throws Exception {
        System.out.println("getYvalue");
        double x = 0.0;
        DataSet instance = ds2;
        double expResult = 1;
        double result = instance.getYvalue(x);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getXValues method, of class DataSet.
     */
    @Test
    public void testGetXValues() {
        System.out.println("getXValues");
        DataSet instance = ds1;
        double[] result = instance.getXValues();
        assertEquals(ds1Size, result.length);
    }

    @Test
    public void testGetSetXValue() throws Exception
    {
        System.out.println("testGetSetXValue");
        Random rand = new Random();
        int pointNum = rand.nextInt(ds1Size);
        double value = 333;
        DataSet instance = ds1;
        instance.setXValue(pointNum, value);
        
        assertEquals(instance.getPoint(pointNum)[0], value, 0);

    }

    @Test
    public void testGetSetYValue() throws Exception
    {
        System.out.println("testGetSetYValue");
        Random rand = new Random();
        int pointNum = rand.nextInt(ds1Size);
        double value = 333;
        DataSet instance = ds1;
        instance.setYValue(pointNum, value);

        assertEquals(instance.getPoint(pointNum)[1], value, 0);
    }


    /**
     * Test of toString method, of class DataSet.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        DataSet instance = ds1;
        String exp = ""+instance.getNumberPoints();
        String result = instance.toString();

        System.out.println("String: "+ result);

        assertTrue(result.indexOf(exp)>=0);
    }


    /**
     * Test of getMaxX method, of class DataSet.
     */
    @Test
    public void testGetMaxX() {
        System.out.println("getMaxX");
        DataSet instance = ds1;
        double expResult = ds1Size-1;
        double[] result = instance.getMaxX();
        assertEquals(expResult, result[0], 0);
    }

    /**
     * Test of getMinX method, of class DataSet.
     */
    @Test
    public void testGetMinX() {
        System.out.println("getMinX");
        DataSet instance = ds1;
        double expResult = 0;
        double[] result = instance.getMinX();
        assertEquals(expResult, result[0], 0);
    }

    /**
     * Test of getMaxY method, of class DataSet.
     */
    @Test
    public void testGetMaxY() {
        System.out.println("getMaxY");
        DataSet instance = ds1;
        double expResult = 1;
        double[] result = instance.getMaxY();
        assertEquals(expResult, result[1],0.0001);
    }

    /**
     * Test of getMinY method, of class DataSet.
     */
    @Test
    public void testGetMinY() {
        System.out.println("getMinY");
        DataSet instance = ds1;
        double expResult = -1;
        double[] result = instance.getMinY();
        assertEquals(expResult, result[1],0.0001);

    }





    public static void main(String[] args)
    {
        DataSetTest dst = new DataSetTest();
        Result r = org.junit.runner.JUnitCore.runClasses(dst.getClass());
        MainTest.checkResults(r);

    }

}