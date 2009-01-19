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
