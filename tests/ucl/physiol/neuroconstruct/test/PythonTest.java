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

package ucl.physiol.neuroconstruct.test;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import java.io.File;
import org.junit.Test;
import org.junit.runner.*;
import ucl.physiol.neuroconstruct.project.ProjectStructure;
import static org.junit.Assert.*;

/**
 *
 * Test core behaviour of neuroConstruct example models
 *
 * @author Padraig Gleeson
 * 
 */
public class PythonTest 
{
/**/
    @Test public void testEx4()
    {
        String projFileName = "nCexamples/Ex4_HHcell/Ex4_HHcell.ncx";
        checkPythonScripts(projFileName);
    }
    @Test public void testLems()
    {
        String projFileName = "lems/nCproject/LemsTest/LemsTest.ncx";
        checkPythonScripts(projFileName, "RunTests");
        checkPythonScripts(projFileName, "RunTests2");
    }

    @Test public void testGranuleCell()
    {
        String projFileName = "nCmodels/GranuleCell/GranuleCell.ncx";
        checkPythonScripts(projFileName);
    }

    @Test public void testGranCellLayer()
    {
        String projFileName = "nCmodels/GranCellLayer/GranCellLayer.ncx";
        checkPythonScripts(projFileName, "RunGolgiTests");
        checkPythonScripts(projFileName, "RunGranTests");
    }

    @Test public void testCA1()
    {
        String projFileName = "nCmodels/CA1PyramidalCell/CA1PyramidalCell.ncx";
        checkPythonScripts(projFileName);
    }

    @Test public void testMainen()
    {
        String projFileName = "nCmodels/MainenEtAl_PyramidalCell/MainenEtAl_PyramidalCell.ncx";
        checkPythonScripts(projFileName);
    }

    @Test public void testPurkinje()
    {
        String projFileName = "nCmodels/PurkinjeCell/PurkinjeCell.ncx";
        checkPythonScripts(projFileName);
    }

    @Test public void testSolinasEtAl_GolgiCell()
    {
        String projFileName = "nCmodels/SolinasEtAl_GolgiCell/SolinasEtAl_GolgiCell.ncx";
        checkPythonScripts(projFileName);
    }

    @Test public void testVervaekeEtAl_GolgiCellNetwork()
    {
        String projFileName = "nCmodels/VervaekeEtAl-GolgiCellNetwork/VervaekeEtAl-GolgiCellNetwork.ncx";
        checkPythonScripts(projFileName);
    }

    @Test public void testThalamocortical()
    {
        String projFileName = "nCmodels/Thalamocortical/Thalamocortical.ncx";
        checkPythonScripts(projFileName);
    }/**/



    private void checkPythonScripts(String projFileName)
    {
        checkPythonScripts(projFileName, "RunTests");
    }


    private void checkPythonScripts(String projFileName, String testScriptName)
    {
        File projFile = new File(projFileName);
        System.out.println("Going to check project: "+ projFile.getAbsolutePath());

        assertTrue("Problem finding file: "+projFile.getAbsolutePath(), projFile.exists());

        File pythonScriptDir = new File(projFile.getParentFile(), "pythonScripts");
        File pythonTestScript = new File(pythonScriptDir, testScriptName+".py");


        assertTrue("Problem finding file: "+pythonTestScript, pythonTestScript.exists());

        PythonInterpreter interp = new PythonInterpreter();

        interp.exec("import sys, os");

        interp.exec("sys.path.append(\""+pythonScriptDir.getAbsolutePath()+"\")");
        interp.exec("os.environ[\"NC_HOME\"] = \""+ ProjectStructure.getnCHome().getAbsolutePath()+"\"");


        interp.exec("os.chdir(\""+ pythonScriptDir.getAbsolutePath()+"\")");
        interp.exec("print \"Jython working in: \" + os.getcwd()+ \" with sys.path: \"+ str(sys.path)");


        interp.exec("import "+testScriptName);
        interp.exec("reload("+testScriptName+")");
        interp.exec("result = "+testScriptName+".testAll()");
        interp.exec("print \"Have run test script: "+pythonTestScript+"\"");

        PyObject result = interp.get("result");

        System.out.println("Result in Java: " + result);

        assertTrue("More than one failure when running script: "+pythonTestScript+":"+result, result.toString().indexOf(" 0 tests failed")>=0);


        interp.exec("sys.path.remove(\""+pythonScriptDir.getAbsolutePath()+"\")");


    }



    public static void main(String[] args)
            
    {
        System.out.println("Running the main nC model python script tests...");


        Result r = null;

        
        r = org.junit.runner.JUnitCore.runClasses(ucl.physiol.neuroconstruct.test.PythonTest.class);
        
        MainTest.checkResults(r);

    }
    
}