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

package ucl.physiol.neuroconstruct.gui;
import java.awt.*;
import ucl.physiol.neuroconstruct.cell.ParameterisedGroup;

/**
 * GUI element to allow easy linking between neuroConstruct tabs
 *
 * @author Padraig Gleeson
 *  
 */

public class ClickProjectHelper 
{
    
    public static final String PROTOCOL = "nCproj";
    public static final String CELL_GROUP = "cellGroup";
    public static final String REGION = "region";
    public static final String SECTION_GROUP = "sectionGroup";
    public static final String PARAMETERISED_GROUP = "parameterisedGroup";
    public static final String CELL_TYPE = "cellType";
    public static final String NET_CONNECTION = "netConn";
    public static final String CELL_MECHANISM = "cellMechanism";
    public static final String ELEC_INPUT = "elecInput";
    public static final String PLOT_SAVE = "plotSave";
    
    private static String styleInfo = " style=\"color: #2F4F4F\"";
    
    public static String getCellGroupLink(String name)
    {
        return "<a href=\""+PROTOCOL+"://"+CELL_GROUP+"="+name+"\" "+styleInfo+">"+name+"</a>";
    }
    public static String getRegionLink(String name)
    {
        return "<a href=\""+PROTOCOL+"://"+REGION+"="+name+"\" "+styleInfo+">"+name+"</a>";
    }
    public static String getCellTypeLink(String name)
    {
        return "<a href=\""+PROTOCOL+"://"+CELL_TYPE+"="+name+"\" "+styleInfo+">"+name+"</a>";
    }
    
    
    public static String getCellSectionGroupLink(String cellType, String secGroup)
    {
        return "<a href=\""+PROTOCOL+"://"+CELL_TYPE+"="+cellType+"&"+SECTION_GROUP+"="+secGroup+"\" "+styleInfo+">"+secGroup+"</a>";
    }
    
    public static String getParamGroupLink(String cellType, ParameterisedGroup pg)
    {
        return "<a href=\""+PROTOCOL+"://"+CELL_TYPE+"="+cellType+"&"+PARAMETERISED_GROUP+"="+pg.getName()+"\" "+styleInfo+">"+pg.getName()+"</a>";
    }

    public static String getNetConnLink(String name)
    {
        return "<a href=\""+PROTOCOL+"://"+NET_CONNECTION+"="+name+"\" "+styleInfo+">"+name+"</a>";
    }
    public static String getCellMechLink(String name)
    {
        return "<a href=\""+PROTOCOL+"://"+CELL_MECHANISM+"="+name+"\" "+styleInfo+">"+name+"</a>";
    }
    public static String getElecInputLink(String name)
    {
        return "<a href=\""+PROTOCOL+"://"+ELEC_INPUT+"="+name+"\" "+styleInfo+">"+name+"</a>";
    }
    public static String getPlotSaveLink(String name)
    {
        return "<a href=\""+PROTOCOL+"://"+PLOT_SAVE+"="+name+"\" "+styleInfo+">"+name+"</a>";
    }

    private ClickProjectHelper()
    {
    }

}
