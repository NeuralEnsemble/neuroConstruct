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

package ucl.physiol.neuroconstruct.mechanisms;

import ucl.physiol.neuroconstruct.project.Project;
import java.io.File;
import ucl.physiol.neuroconstruct.project.ProjectStructure;
import ucl.physiol.neuroconstruct.utils.ClassLogger;



/**
 * Mapping of ChannelML File via XSL file to simulation environment
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class SimXSLMapping
{

    ClassLogger logger = new ClassLogger("SimXSLMapping");

    private String xslFile = null;
    String simulationEnvironment = null;

    private boolean requiresCompilation = true;

    public SimXSLMapping()
    {

    }

    public SimXSLMapping(String xslFile, String simulationEnvironment, boolean requiresCompilation)
    {
        this.xslFile = xslFile;
        this.simulationEnvironment = simulationEnvironment;
        this.requiresCompilation = requiresCompilation;
    }

    public String getSimEnv()
    {
        return this.simulationEnvironment;
    }
    public String getXslFile()
    {
        return this.xslFile;
    }

    public File getXslFileObject(Project project, String cellMechanismName)
    {
        File targetDir = ProjectStructure.getCellMechanismDir(project.getProjectMainDirectory());


        File idealFileLocation = null;

        if (targetDir.exists())
        {
            File idealDir = new File(targetDir, cellMechanismName);

            idealFileLocation = new File(idealDir, xslFile);

            if (idealFileLocation.exists())
            {
                logger.logComment("File is where it should be: " + idealFileLocation.getAbsolutePath());
                return idealFileLocation;
            }
            else
            {
                //System.out.println(");
            }
        }
        logger.logError(xslFile+" not found near: "+ targetDir.getAbsolutePath(), null);
        return null;

    }



    public void setSimEnv(String simEnv)
    {
        this.simulationEnvironment = simEnv;
    }

    public void setXslFile(String xslFile)
    {
        this.xslFile = xslFile;
    }

    public void setRequiresCompilation(boolean rc)
    {
        this.requiresCompilation = rc;
    }
    public boolean isRequiresCompilation()
    {
        return requiresCompilation;
    }


    public String toString()
    {
        return "SimXSLMapping[xslFile: "+ xslFile
            +", simulationEnvironment: "+simulationEnvironment+
            ", requiresCompilation: "+requiresCompilation+"]";
    }

}
