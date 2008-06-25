/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell.utils;

import java.util.Enumeration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.cell.Cell;

/**
 *
 * @author padraig
 */
public class CellTypeHelperTest {

    public CellTypeHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getCell method, of class CellTypeHelper.
     */
    @Test
    public void testGetCell() {
        System.out.println("getCell");
        String cellType = "";
        String cellName = "";
        Cell expResult = null;
        Cell result = CellTypeHelper.getCell(cellType, cellName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addNewOrdinaryCellType method, of class CellTypeHelper.
     */
    @Test
    public void testAddNewOrdinaryCellType() {
        System.out.println("addNewOrdinaryCellType");
        String cellType = "";
        String description = "";
        CellTypeHelper.addNewOrdinaryCellType(cellType, description);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addNewMorphCellType method, of class CellTypeHelper.
     */
    @Test
    public void testAddNewMorphCellType() {
        System.out.println("addNewMorphCellType");
        String fullClassName = "";
        CellTypeHelper.addNewMorphCellType(fullClassName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getExtsForCellType method, of class CellTypeHelper.
     */
    @Test
    public void testGetExtsForCellType() {
        System.out.println("getExtsForCellType");
        String cellType = "";
        String[] expResult = null;
        String[] result = CellTypeHelper.getExtsForCellType(cellType);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDescForCellType method, of class CellTypeHelper.
     */
    @Test
    public void testGetDescForCellType() {
        System.out.println("getDescForCellType");
        String cellType = "";
        String expResult = "";
        String result = CellTypeHelper.getDescForCellType(cellType);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllCellTypeNames method, of class CellTypeHelper.
     */
    @Test
    public void testGetAllCellTypeNames() {
        System.out.println("getAllCellTypeNames");
        Enumeration expResult = null;
        Enumeration result = CellTypeHelper.getAllCellTypeNames();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isAMorphologyCellType method, of class CellTypeHelper.
     */
    @Test
    public void testIsAMorphologyCellType() {
        System.out.println("isAMorphologyCellType");
        String cellTypeName = "";
        boolean expResult = false;
        boolean result = CellTypeHelper.isAMorphologyCellType(cellTypeName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class CellTypeHelper.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        CellTypeHelper.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}