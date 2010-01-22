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

package ucl.physiol.neuroconstruct.genesis;

import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
import java.util.*;

/**
 * The general parameters needed for GENESIS simulations
 *
 * @author Padraig Gleeson
 *  
 */

public class GenesisSettings
{
    private ClassLogger logger = new ClassLogger("GenesisSettings");

    //String textBeforeCellCreation = null;
    //String textAfterCellCreation = null;

    private Hashtable<Integer, String> nativeBlocks = new Hashtable<Integer, String>();

    boolean symmetricCompartments = false;

    boolean generateComments = true;

    boolean showVoltPlot = true;

    public enum GraphicsMode{ ALL_SHOW, NO_PLOTS, NO_CONSOLE }

    private GraphicsMode graphicsMode = GraphicsMode.ALL_SHOW;
    //private boolean graphicsMode = true;

    boolean showShapePlot = false;

    int unitSystemToUse = UnitConverter.GENESIS_SI_UNITS;

    NumericalMethod numMethod = new NumericalMethod();

    private boolean copySimFiles = true;

    private boolean mooseCompatMode = false;

    private float reloadSimAfterSecs = -1;

    private float absRefractSpikegen = -1;


    public GenesisSettings()
    {
    }


    /**
     * Should only be used by XMLEncoder, hence use of deprecated
     * @deprecated
     */
    public Hashtable<Integer, String> getNativeBlocks()
    {
        return nativeBlocks;
    }

    /**
     * Should only be used by XMLEncoder, hence use of deprecated
     * @deprecated
     */
    public void setNativeBlocks(Hashtable<Integer, String> nb)
    {
        nativeBlocks = nb;
    }

    public String getNativeBlock(ScriptLocation ncl)
    {
        logger.logComment("Looking for text for location: "+ ncl);
        ArrayList<ScriptLocation> locsAvailable = ScriptLocation.getAllKnownLocations();

        for (int i = 0; i < locsAvailable.size(); i++)
        {
            ScriptLocation nextLoc = locsAvailable.get(i);
            //System.out.println("Checking: "+nextLoc);
            if (ncl.getPositionReference() == nextLoc.getPositionReference())
            {
                String text = nativeBlocks.get(new Integer(ncl.getPositionReference()));
                logger.logComment("Found: "+ text);
                return text;
            }
        }
        return null;
    }


    public void setNativeBlock(ScriptLocation ncl, String text)
    {
        ArrayList<ScriptLocation> locsAvailable = ScriptLocation.getAllKnownLocations();

        for (int i = 0; i < locsAvailable.size(); i++)
        {
            ScriptLocation nextLoc = locsAvailable.get(i);
            //System.out.println("Checking: "+nextLoc);
            if (ncl.getPositionReference() == nextLoc.getPositionReference())
            {
                logger.logComment("Setting block type: "+ ncl.getPositionReference()+" to: "+ text);
                nativeBlocks.put(new Integer(ncl.getPositionReference()), text);
            }
        }
    }

    public float getReloadSimAfterSecs() {
        return reloadSimAfterSecs;
    }

    public void setReloadSimAfterSecs(float reloadSimAfterSecs) {
        this.reloadSimAfterSecs = reloadSimAfterSecs;
    }

    public float getAbsRefractSpikegen() 
    {
        return absRefractSpikegen;
    }

    public void setAbsRefractSpikegen(float absRefractSpikegen)
    {
        this.absRefractSpikegen = absRefractSpikegen;
    }

    

  
    /**
     * Used to ensure compatibility with old method of code blocks...
     * @deprecated
     */
    public void setTextAfterCellCreation(String textAfterCellCreation)
    {
        //this.textAfterCellCreation = textAfterCellCreation;
        nativeBlocks.put(ScriptLocation.BEFORE_FINAL_RESET.getPositionReference(), textAfterCellCreation);

    }

    /**
     * Used to ensure compatibility with old method of code blocks...
     * @deprecated
     */
    public void setTextBeforeCellCreation(String textBeforeCellCreation)
    {
        //this.textBeforeCellCreation = textBeforeCellCreation;
        nativeBlocks.put(ScriptLocation.BEFORE_CELL_CREATION.getPositionReference(), textBeforeCellCreation);

    }


    public boolean isSymmetricCompartments()
    {
        return symmetricCompartments;
    }
    public void setSymmetricCompartments(boolean symmetricCompartments)
    {
        this.symmetricCompartments = symmetricCompartments;
    }
    public int getUnitSystemToUse()
    {
        return unitSystemToUse;
    }
    public boolean isSIUnits()
    {
        return unitSystemToUse == UnitConverter.GENESIS_SI_UNITS;
    }
    public boolean isPhysiologicalUnits()
    {
        return unitSystemToUse == UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS;
    }

    public boolean isMooseCompatMode()
    {
        return mooseCompatMode;
    }

    public void setMooseCompatMode(boolean mooseCompatMode)
    {
        this.mooseCompatMode = mooseCompatMode;
    }

    
    public boolean isShowShapePlot()
    {
        return showShapePlot;
    }
    public boolean isShowVoltPlot()
    {
        return showVoltPlot;
    }
    public void setShowVoltPlot(boolean showVoltPlot)
    {
        this.showVoltPlot = showVoltPlot;
    }
    public void setShowShapePlot(boolean showShapePlot)
    {
        this.showShapePlot = showShapePlot;
    }
    public boolean isGenerateComments()
    {
        return generateComments;
    }
    public void setGenerateComments(boolean generateComments)
    {
        this.generateComments = generateComments;
    }
    public NumericalMethod getNumMethod()
    {
        return numMethod;
    }
    public void setNumMethod(NumericalMethod numMethod)
    {
        this.numMethod = numMethod;
    }
    public void setUnitSystemToUse(int unitSystemToUse)
    {
        this.unitSystemToUse = unitSystemToUse;
    }


    /**
     * @deprecated
     */
    public void setGraphicsMode(boolean graphicsMode)
    {
        if (graphicsMode) this.graphicsMode = GraphicsMode.ALL_SHOW;
        else this.graphicsMode = GraphicsMode.NO_PLOTS;
    }

    public void setNoConsole()
    {
        this.graphicsMode = GraphicsMode.NO_CONSOLE;
    }


    public void setGraphicsMode(GraphicsMode graphicsMode)
    {
        this.graphicsMode = graphicsMode;
    }

    public GraphicsMode getGraphicsMode()
    {
        return graphicsMode;
    }

    public boolean isCopySimFiles()
    {
        return copySimFiles;
    }
    public void setCopySimFiles(boolean copySimFiles)
    {
        this.copySimFiles = copySimFiles;
    }


}
