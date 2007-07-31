/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2007 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.nmodleditor.processes;

import java.io.*;
import java.util.*;
import ucl.physiol.neuroconstruct.nmodleditor.modfile.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

public class TestHocFileGenerator
{
    ClassLogger logger = new ClassLogger("TestHocFileGenerator");

    File myFile = null;

    String densityMechanismName = null;
    String pointProcessName = null;
    float simRuntime;
    float simDT;
    TestStimulationSettings myStimulation =  null;

    Hashtable initialParamSettings = new Hashtable();

    String testCellName = new String("testCell");

    public TestHocFileGenerator(File hocFileName)
    {
        myFile = hocFileName;
    }

    public String getGeneratedFilename()
    {
        if (!myFile.exists()) return null;
        return myFile.getAbsolutePath();
    }

    public void addDensityMechanism(String name)
    {
        densityMechanismName = name;
    }

    public void addPointProcess(String name)
    {
        pointProcessName = name;
    }

    public void setMainSimulationParams(float runtime, float dt)
    {
        this.simRuntime = runtime;
        this.simDT = dt;
    }

    public void addStimulation(TestStimulationSettings stim)
    {
        myStimulation = stim;
    }


    public void addInitialParameterSetting(String paramName, float value)
    {
        initialParamSettings.put(paramName, new Float(value));
    }

    public String getContentAsString()
    {
        StringBuffer wholeCentent = new StringBuffer();

        wholeCentent.append(generateBasicHeader());
        wholeCentent.append(createAndAccess());
        wholeCentent.append(generateTestCell());
        wholeCentent.append(generatePointProcess());
        wholeCentent.append(generateStimulation());
        wholeCentent.append(generateRunControls());
        wholeCentent.append(generateVoltageGraph());

        return wholeCentent.toString();
    }


    public void generateTheHocFile() throws ModFileException
    {
        try
        {
            FileWriter fw = new FileWriter(myFile);

            fw.write(getContentAsString());


            fw.flush();
            fw.close();
        }
        catch (Exception ex)
        {
            throw new ModFileException("Error writing to file: " + myFile.getAbsolutePath(), ex);

        }
        logger.logComment("... Created Main hoc file: " + myFile.getAbsolutePath());
    }

    private String generateBasicHeader()
    {
        StringBuffer response = new StringBuffer();
        response.append("load_file(\"nrngui.hoc\")" + "\n\n");
        return response.toString();
    }

    private String createAndAccess()
    {
        StringBuffer response = new StringBuffer();
        response.append("create "+testCellName+"\n");
        response.append("access "+testCellName+"\n\n");
        return response.toString();
    }

    private String generateTestCell()
    {
        StringBuffer response = new StringBuffer();
        response.append(testCellName+" {\n");
        response.append("nseg = 5\n");

        if (densityMechanismName!=null)
        {
            response.append("insert "+ densityMechanismName+"\n\n");
            Enumeration params = initialParamSettings.keys();

            while (params.hasMoreElements())
            {

                String nextParam = (String)params.nextElement();
                logger.logComment("Looking at param: "+ nextParam);
                Float paramVal = (Float)initialParamSettings.get(nextParam);
                response.append(nextParam+"_"+densityMechanismName+" = "+ paramVal.floatValue()+"\n");
            }

        }

        response.append("}\n\n");

        response.append(testCellName+".L = 20 // typical value...\n");
        response.append(testCellName+".diam = 20 // typical value...\n");
        response.append(testCellName+".Ra = 123 // typical value...\n");
        response.append("\n");

        return response.toString();

    }

    public String generateVoltageGraph()
    {
        StringBuffer response = new StringBuffer();
        response.append("// This code merely pops up the voltage graph\n\n");


        response.append("objectvar save_window_, scene_vector_[3]\n");

//response.append("objectvar ocbox_, ocbox_list_, scene_, scene_list_\n");
//response.append("{ocbox_list_ = new List()  scene_list_ = new List()}\n");
//response.append("{pwman_place(0,0,0)}\n\n");

        response.append("{\n");
        response.append("save_window_ = new Graph(0)\n");
        response.append("save_window_.size(0,"+simRuntime+",-80,40)\n");
        response.append("scene_vector_[2] = save_window_\n");
        response.append("{save_window_.view(0, -80, "+simRuntime+", 120, 60, 686, 963.9, 240.4)}\n");
        response.append("graphList[0].append(save_window_)\n");
        response.append("save_window_.save_name(\"graphList[0].\")\n");
        response.append("save_window_.addexpr(\"v(.5)\", 1, 1, 0.8, 0.9, 2)\n");
        response.append("}\n");
      //  response.append("objectvar scene_vector_[1]\n");
     //   response.append("{doNotify()}\n\n");
        return response.toString();

    }

    private String generatePointProcess()
    {
        if (pointProcessName==null) return "";

        StringBuffer response = new StringBuffer();
        response.append("objref pointProcess\n");

        response.append(testCellName+" pointProcess = new "+pointProcessName+"(0.5)\n\n");


        Enumeration params = initialParamSettings.keys();

        while (params.hasMoreElements())
        {
            String nextParam = (String) params.nextElement();
            Float paramVal = (Float) initialParamSettings.get(nextParam);
            response.append("pointProcess." + nextParam + " = " + paramVal.floatValue() + "\n");
        }
        response.append("\n");


        return response.toString();
    }

    public String generateStimulation()
    {
        if (myStimulation==null) return "";

        StringBuffer response = new StringBuffer();


        response.append("objref clamp\n");
        response.append(testCellName+" clamp = new IClamp(0.5)\n");
        response.append("clamp.del = "+myStimulation.delay+"\n");
        response.append("clamp.dur = "+myStimulation.duration+"\n");
        response.append("clamp.amp = "+myStimulation.amplitude+"\n\n");
        return response.toString();
    }


    public String generateRunControls()
    {
        StringBuffer response = new StringBuffer();

        response.append("\n\n// This code merely pops up a Run Control\n");

        response.append("{\n");
        response.append("xpanel(\"RunControl\", 0)\n");
        response.append("v_init = -65\n");
        response.append("xvalue(\"Init\",\"v_init\", 1,\"stdinit()\", 1, 1 )\n");
        response.append("xbutton(\"Init & Run\",\"run()\")\n");
        response.append("xbutton(\"Stop\",\"stoprun=1\")\n");
        response.append("t = 0\n");
        response.append("xvalue(\"t\",\"t\", 2 )\n");
        response.append("tstop = "+simRuntime+"\n");
        response.append("xvalue(\"Tstop\",\"tstop\", 1,\"tstop_changed()\", 0, 1 )\n");
        response.append("dt = "+simDT+"\n");
        response.append(" xvalue(\"dt\",\"dt\", 1,\"setdt()\", 0, 1 )\n");
        response.append("xpanel(1090,132)\n");
        response.append("}\n\n");
        return response.toString();
    }



    public static void main(String[] args)
    {
        File tempHoc = new File("C:\\nrn54\\PatTest\\nmodl\\modfiles\\temp\\test.hoc");
        TestHocFileGenerator testHocFileGenerator1 = new TestHocFileGenerator(tempHoc);
        testHocFileGenerator1.addDensityMechanism("kd");
        testHocFileGenerator1.addPointProcess("IClamp1");

        try
        {
            testHocFileGenerator1.generateTheHocFile();
        }
        catch (ModFileException ex)
        {
            ex.printStackTrace();
        }
    }




}
