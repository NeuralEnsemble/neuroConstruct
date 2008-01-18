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
 * A class to contain non 3D related project specific settings
 *
 * @author Padraig Gleeson
 *  
 */

public class ProjectProperties {
    
    private String preferredSaveFormat = ProjectStructure.JAVA_XML_FORMAT;
        
    
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



}
