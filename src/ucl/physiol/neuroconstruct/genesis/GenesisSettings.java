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

package ucl.physiol.neuroconstruct.genesis;

import ucl.physiol.neuroconstruct.utils.units.*;
import java.util.Hashtable;
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

    private boolean graphicsMode = true;

    boolean showShapePlot = false;

    int unitSystemToUse = UnitConverter.GENESIS_SI_UNITS;

    NumericalMethod numMethod = new NumericalMethod();

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
    public boolean isGraphicsMode()
    {
        return graphicsMode;
    }
    public void setGraphicsMode(boolean graphicsMode)
    {
        this.graphicsMode = graphicsMode;
    }

}
