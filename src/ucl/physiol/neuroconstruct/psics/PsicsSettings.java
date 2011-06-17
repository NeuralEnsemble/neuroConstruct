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

package ucl.physiol.neuroconstruct.psics;


import java.util.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * The general parameters needed for PSICS simulations
 *
 * @author Padraig Gleeson
 *  
 */

public class PsicsSettings
{
    private ClassLogger logger = new ClassLogger("PsicsSettings");


    private boolean showHtmlSummary = true;
    private boolean showPlotSummary = true;
    private boolean showConsole = true;
    private float spatialDiscretisation = 25;
    private float singleChannelCond = 3e-8f; //mS

    public PsicsSettings()
    {
    }

    public boolean isShowHtmlSummary()
    {
        return showHtmlSummary;
    }

    public void setShowHtmlSummary(boolean showHtmlSummary)
    {
        this.showHtmlSummary = showHtmlSummary;
    }

    public boolean isShowPlotSummary()
    {
        return showPlotSummary;
    }

    public void setShowPlotSummary(boolean showPlotSummary)
    {
        this.showPlotSummary = showPlotSummary;
    }

    public boolean isShowConsole() {
        return showConsole;
    }

    public void setShowConsole(boolean showConsole) {
        this.showConsole = showConsole;
    }
    

    public float getSingleChannelCond()
    {
        return singleChannelCond;
    }

    public void setSingleChannelCond(float singleChannelCond)
    {
        this.singleChannelCond = singleChannelCond;
    }

    public float getSpatialDiscretisation()
    {
        return spatialDiscretisation;
    }

    public void setSpatialDiscretisation(float spatialDiscretisation)
    {
        this.spatialDiscretisation = spatialDiscretisation;
    }






}
