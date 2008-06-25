/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell.converters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.Attributes;
import ucl.physiol.neuroconstruct.cell.Cell;

/**
 *
 * @author padraig
 */
public class MorphMLReaderTest {

    public MorphMLReaderTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of characters method, of class MorphMLReader.
     */
    @Test
    public void testCharacters() throws Exception {
        System.out.println("characters");
        char[] ch = null;
        int start = 0;
        int length = 0;
        MorphMLReader instance = new MorphMLReader();
        instance.characters(ch, start, length);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBuiltCell method, of class MorphMLReader.
     */
    @Test
    public void testGetBuiltCell() {
        System.out.println("getBuiltCell");
        MorphMLReader instance = new MorphMLReader();
        Cell expResult = null;
        Cell result = instance.getBuiltCell();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWarnings method, of class MorphMLReader.
     */
    @Test
    public void testGetWarnings() {
        System.out.println("getWarnings");
        MorphMLReader instance = new MorphMLReader();
        String expResult = "";
        String result = instance.getWarnings();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of startDocument method, of class MorphMLReader.
     */
    @Test
    public void testStartDocument() {
        System.out.println("startDocument");
        MorphMLReader instance = new MorphMLReader();
        instance.startDocument();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of endDocument method, of class MorphMLReader.
     */
    @Test
    public void testEndDocument() {
        System.out.println("endDocument");
        MorphMLReader instance = new MorphMLReader();
        instance.endDocument();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCurrentElement method, of class MorphMLReader.
     */
    @Test
    public void testGetCurrentElement() {
        System.out.println("getCurrentElement");
        MorphMLReader instance = new MorphMLReader();
        String expResult = "";
        String result = instance.getCurrentElement();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getParentElement method, of class MorphMLReader.
     */
    @Test
    public void testGetParentElement() {
        System.out.println("getParentElement");
        MorphMLReader instance = new MorphMLReader();
        String expResult = "";
        String result = instance.getParentElement();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAncestorElement method, of class MorphMLReader.
     */
    @Test
    public void testGetAncestorElement() {
        System.out.println("getAncestorElement");
        int generationsBack = 0;
        MorphMLReader instance = new MorphMLReader();
        String expResult = "";
        String result = instance.getAncestorElement(generationsBack);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCurrentElement method, of class MorphMLReader.
     */
    @Test
    public void testSetCurrentElement() {
        System.out.println("setCurrentElement");
        String newElement = "";
        MorphMLReader instance = new MorphMLReader();
        instance.setCurrentElement(newElement);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stepDownElement method, of class MorphMLReader.
     */
    @Test
    public void testStepDownElement() {
        System.out.println("stepDownElement");
        MorphMLReader instance = new MorphMLReader();
        instance.stepDownElement();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of startElement method, of class MorphMLReader.
     */
    @Test
    public void testStartElement() throws Exception {
        System.out.println("startElement");
        String namespaceURI = "";
        String localName = "";
        String qName = "";
        Attributes attributes = null;
        MorphMLReader instance = new MorphMLReader();
        instance.startElement(namespaceURI, localName, qName, attributes);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of endElement method, of class MorphMLReader.
     */
    @Test
    public void testEndElement() {
        System.out.println("endElement");
        String namespaceURI = "";
        String localName = "";
        String qName = "";
        MorphMLReader instance = new MorphMLReader();
        instance.endElement(namespaceURI, localName, qName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class MorphMLReader.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        MorphMLReader.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}