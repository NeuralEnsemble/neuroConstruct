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

package ucl.physiol.neuroconstruct.utils;


import java.lang.management.*;
import ucl.physiol.neuroconstruct.project.ProjectStructure;


/**
 * A number of settings required from the user
 *
 * @author Padraig Gleeson
 *  
 */

public class UserSettings
{

    private String nCProjectsDir = null;

    private String neuronHome = null;
    private String psicsJar = null;
    
    private String executableCommandLine = null;

    private String browserExecutable = null;
    private String editorExecutable = null;

    private String locationLogFiles = "logs";

    private boolean logFilePrintToScreenPolicy = false;

    private boolean logFileSaveToFilePolicy = false;
    
    private boolean generateMatlab = false;
    private boolean generateIgor = false;

    private int numProcessorstoUse = -1;
    
    private String prefNeuroMLVersionString = null;


    public UserSettings()
    {
        // @todo solve initialisation error when line below used
        //preferredSaveFormat = ProjectStructure.JAVA_XML_FORMAT;
        //preferredSaveFormat = "Java XML Format";
    }

    
    public String getNCProjectsDir()
    {
    	if (nCProjectsDir == null)
    	{
    		nCProjectsDir = ProjectStructure.getDefaultnCProjectsDir().getAbsolutePath();
    	}
        return nCProjectsDir;
    }

    public void setNCProjectsDir(String dir)
    {
        nCProjectsDir = dir;
    }

    public int getNumProcessorstoUse()
    {
        if (numProcessorstoUse<=0)
        {
            numProcessorstoUse = 1;
        }

        return numProcessorstoUse;
    }

    public void setNumProcessorstoUse(int numProcessorstoUse)
    {
        if (numProcessorstoUse<1)
            numProcessorstoUse = 1;
        this.numProcessorstoUse = numProcessorstoUse;
    }




    public void setGenerateMatlab(boolean gen)
    {
        this.generateMatlab = gen;
    }

    public void setGenerateIgor(boolean gen)
    {
        this.generateIgor = gen;
    }

    public boolean getGenerateMatlab()
    {
        return generateMatlab;
    }

    public boolean getGenerateIgor()
    {
        return generateIgor;
    }


    public String getExecutableCommandLine()
    {
        return executableCommandLine;
    }
    public String getLocationLogFiles()
    {
        return locationLogFiles;
    }
    public boolean getLogFilePrintToScreenPolicy()
    {
        return logFilePrintToScreenPolicy;
    }
    public boolean getLogFileSaveToFilePolicy()
    {
        return logFileSaveToFilePolicy;
    }
    public void setExecutableCommandLine(String executableCommandLine)
    {
        this.executableCommandLine = executableCommandLine;
    }
    public void setLocationLogFiles(String locationLogFiles)
    {
        this.locationLogFiles = locationLogFiles;
    }
    public void setLogFilePrintToScreenPolicy(boolean logFilePrintToScreenPolicy)
    {
        this.logFilePrintToScreenPolicy = logFilePrintToScreenPolicy;
    }
    public void setLogFileSaveToFilePolicy(boolean logFileSaveToFilePolicy)
    {
        this.logFileSaveToFilePolicy = logFileSaveToFilePolicy;
    }
    public String getNeuronHome()
    {
        return neuronHome;
    }
    public void setNeuronHome(String neuronHome)
    {
        this.neuronHome = neuronHome;
    }
    public String getPsicsJar()
    {
        return psicsJar;
    }
    public void setPsicsJar(String jar)
    {
        this.psicsJar = jar;
    }
    public String getBrowserPath()
    {
        return this.browserExecutable;
    }
    public void setBrowserPath(String browserExecutable)
    {
        this.browserExecutable = browserExecutable;
    }
    
    public String getEditorPath()
    {
        return this.editorExecutable;
    }
    public void setEditorPath(String editorExecutable)
    {
        this.editorExecutable = editorExecutable;
    }
  
    public String getPrefNeuroMLVersionString()
    {
        return prefNeuroMLVersionString;
    }

    public void setPrefNeuroMLVersionString(String preferredNeuroMLVersion)
    {
        this.prefNeuroMLVersionString = preferredNeuroMLVersion;
    }

}
