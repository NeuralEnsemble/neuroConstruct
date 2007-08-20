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

package ucl.physiol.neuroconstruct.project;

/**
 * Class containing basic information about the project
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

public class BasicProjectInfo
{
    private String projectName = null;
    private String projectDescription = null;
    private String projectFileVersion = null;

    public BasicProjectInfo()
    {
    }

    public String getProjectDescription()
    {
        return projectDescription;
    }

    public String getProjectFileVersion()
    {
        return projectFileVersion;
    }

    public String getProjectFileVersionNumber()
    {
        int indexV = projectFileVersion.lastIndexOf("v");
        if (indexV<0) return projectFileVersion;
        return projectFileVersion.substring(indexV+1);
    }



    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public void setProjectFileVersion(String projectFileVersion)
    {
        this.projectFileVersion = projectFileVersion;
    }

    public void setProjectDescription(String projectDescription)
    {
        this.projectDescription = projectDescription;
    }

}
