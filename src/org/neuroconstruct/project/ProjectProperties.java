/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.project;


/**
 * A class to contain non 3D related project specific settings
 *
 * @author Padraig Gleeson
 *  
 */

public class ProjectProperties 
{
    
    private String preferredSaveFormat = ProjectStructure.JAVA_XML_FORMAT;
    
    private String saveOption = ProjectStructure.PROJ_SVN_SAVE;
        
    
    public ProjectProperties()
    {
        
    }
    
    public String getPreferredSaveFormat()
    {
        return preferredSaveFormat;
    }
    public void setPreferredSaveFormat(String prefSaveFormat)
    {
        this.preferredSaveFormat = prefSaveFormat;
    }
    
    public String getSaveOption()
    {
        return saveOption;
    }
    public void setSaveOption(String saveOption)
    {
        this.saveOption = saveOption;
    }



}
