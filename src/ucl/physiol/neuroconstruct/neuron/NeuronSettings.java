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

package ucl.physiol.neuroconstruct.neuron;


import java.util.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * The general parameters needed for NEURON simulations
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

public class NeuronSettings
{
    private ClassLogger logger = new ClassLogger("NeuronSettings");

    //String textBeforeCellCreation = null;
    //String textAfterCellCreation = null;

    private Hashtable<Integer, String> nativeBlocks = new Hashtable<Integer, String>();

    private boolean showShapePlot = false;

    private boolean graphicsMode = true;

    private boolean generateComments = true;

    private boolean useVarTimeStep = false;

    //private subsApPropVel

    public NeuronSettings()
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

    public String getNativeBlock(NativeCodeLocation ncl)
    {
        logger.logComment("Looking for text for location: "+ ncl);
        ArrayList<NativeCodeLocation> locsAvailable = NativeCodeLocation.getAllKnownLocations();

        for (int i = 0; i < locsAvailable.size(); i++)
        {
            NativeCodeLocation nextLoc = locsAvailable.get(i);
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


    public void setNativeBlock(NativeCodeLocation ncl, String text)
    {
        ArrayList<NativeCodeLocation> locsAvailable = NativeCodeLocation.getAllKnownLocations();

        for (int i = 0; i < locsAvailable.size(); i++)
        {
            NativeCodeLocation nextLoc = locsAvailable.get(i);
            //System.out.println("Checking: "+nextLoc);
            if (ncl.getPositionReference() == nextLoc.getPositionReference())
            {
                logger.logComment("Setting block type: "+ ncl.getPositionReference()+" to: "+ text);
                nativeBlocks.put(new Integer(ncl.getPositionReference()), text);
            }
        }
    }



/*
    public String getTextAfterCellCreation()
    {
        return nativeBlocks.get(NativeCodeLocation.BEFORE_INITIAL);
    }
    public String getTextBeforeCellCreation()
    {
        return nativeBlocks.get(NativeCodeLocation.BEFORE_CELL_CREATION);
    }*/

    /**
     * Used to ensure compatibility with old method of code blocks...
     * @deprecated
     */
    public void setTextAfterCellCreation(String textAfterCellCreation)
    {
        //this.textAfterCellCreation = textAfterCellCreation;

        nativeBlocks.put(NativeCodeLocation.BEFORE_INITIAL.getPositionReference(), textAfterCellCreation);
    }

    /**
     * Used to ensure compatibility with old method of code blocks...
     * @deprecated
     */
    public void setTextBeforeCellCreation(String textBeforeCellCreation)
    {
        //this.textBeforeCellCreation = textBeforeCellCreation;

        nativeBlocks.put(NativeCodeLocation.BEFORE_CELL_CREATION.getPositionReference(), textBeforeCellCreation);

    }


    public boolean isShowShapePlot()
    {
        return showShapePlot;
    }
    public void setShowShapePlot(boolean showShapePlot)
    {
        this.showShapePlot = showShapePlot;
    }

    public boolean isGraphicsMode()
    {
        return graphicsMode;
    }
    public void setGraphicsMode(boolean graphicsMode)
    {
        this.graphicsMode = graphicsMode;
    }



    public boolean isGenerateComments()
    {
        return generateComments;
    }
    public void setGenerateComments(boolean generateComments)
    {
        this.generateComments = generateComments;
    }


    public boolean isVarTimeStep()
    {
        return useVarTimeStep;
    }
    public void setVarTimeStep(boolean useVarTimeStep)
    {
        this.useVarTimeStep = useVarTimeStep;
    }




}
