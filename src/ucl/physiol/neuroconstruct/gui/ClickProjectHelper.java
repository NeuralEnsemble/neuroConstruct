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

package ucl.physiol.neuroconstruct.gui;
import java.awt.*;

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
