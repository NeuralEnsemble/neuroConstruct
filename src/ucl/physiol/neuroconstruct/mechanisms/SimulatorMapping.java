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

package ucl.physiol.neuroconstruct.mechanisms;

import ucl.physiol.neuroconstruct.project.Project;
import java.io.File;
import ucl.physiol.neuroconstruct.project.ProjectStructure;
import ucl.physiol.neuroconstruct.utils.ClassLogger;



/**
 * Mapping of Cell Mechanism File (e.g. via XSL file) to simulation environment
 *
 * @author Padraig Gleeson
 *  
 */

public class SimulatorMapping
{

    ClassLogger logger = new ClassLogger("SimulatorMapping");

    private String mappingFile = null;
    String simulationEnvironment = null;

    private boolean requiresCompilation = true;

    public SimulatorMapping()
    {

    }

    public SimulatorMapping(String mappingFile, String simulationEnvironment, boolean requiresCompilation)
    {
        this.mappingFile = mappingFile;
        this.simulationEnvironment = simulationEnvironment;
        this.requiresCompilation = requiresCompilation;
    }

    public String getSimEnv()
    {
        return this.simulationEnvironment;
    }
    public String getMappingFile()
    {
        return this.mappingFile;
    }

    public File getXslFileObject(Project project, String cellMechanismName)
    {
        File targetDir = ProjectStructure.getCellMechanismDir(project.getProjectMainDirectory());


        File idealFileLocation = null;

        if (targetDir.exists())
        {
            File idealDir = new File(targetDir, cellMechanismName);

            idealFileLocation = new File(idealDir, mappingFile);

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
        logger.logError(mappingFile+" not found near: "+ targetDir.getAbsolutePath(), null);
        return null;

    }



    public void setSimEnv(String simEnv)
    {
        this.simulationEnvironment = simEnv;
    }

    public void setMappingFile(String mappingFile)
    {
        this.mappingFile = mappingFile;
    }

    public void setRequiresCompilation(boolean rc)
    {
        this.requiresCompilation = rc;
    }
    public boolean isRequiresCompilation()
    {
        return requiresCompilation;
    }


    @Override
    public String toString()
    {
        return "SimulatorMapping[file: "+ mappingFile
            +", simulationEnvironment: "+simulationEnvironment+
            ", requiresCompilation: "+requiresCompilation+"]";
    }

}
