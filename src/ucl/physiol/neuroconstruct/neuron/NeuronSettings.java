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

package ucl.physiol.neuroconstruct.neuron;


import java.util.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * The general parameters needed for NEURON simulations
 *
 * @author Padraig Gleeson
 *  
 */

public class NeuronSettings
{
    private ClassLogger logger = new ClassLogger("NeuronSettings");

    //String textBeforeCellCreation = null;
    //String textAfterCellCreation = null;

    private Hashtable<Integer, String> nativeBlocks = new Hashtable<Integer, String>();

    private boolean showShapePlot = false;

    public enum GraphicsMode{ ALL_SHOW, NO_PLOTS, NO_CONSOLE }

    private GraphicsMode graphicsMode = GraphicsMode.ALL_SHOW;

    private boolean generateComments = true;

    private boolean useVarTimeStep = false;
    
    private boolean genAllModFiles = false;

    private boolean forceModFileRegeneration = false;

    private boolean modSilentMode = true;
    
    private boolean copySimFiles = false;

    
    private boolean forceCorrectInit = true;

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

    /*
     * Should the Shape Plot (3D cell view) be shown when NEURON is run?
     */
    public boolean isShowShapePlot()
    {
        return showShapePlot;
    }

    /*
     * Should the Shape Plot (3D cell view) be shown when NEURON is run?
     */
    public void setShowShapePlot(boolean showShapePlot)
    {
        this.showShapePlot = showShapePlot;
    }


    /**
     * @deprecated
     */
    public void setGraphicsMode(boolean graphicsMode)
    {
        if (graphicsMode) this.graphicsMode = GraphicsMode.ALL_SHOW;
        else this.graphicsMode = GraphicsMode.NO_PLOTS;
    }


    public void setGraphicsMode(GraphicsMode graphicsMode)
    {
        this.graphicsMode = graphicsMode;
    }

    public GraphicsMode getGraphicsMode()
    {
        return graphicsMode;
    }

    /*
     * Sets the graphics mode to GraphicsMode.NO_CONSOLE, i.e. no console pops up when simulation is run, but
     * nrniv runs in background
     */
    public void setNoConsole()
    {
        this.graphicsMode = GraphicsMode.NO_CONSOLE;
    }




    /*
     * If true puts comments & print statements in generated code
     */
    public boolean isGenerateComments()
    {
        return generateComments;
    }


    /*
     * If true puts comments & print statements in generated code
     */
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


    public boolean isModSilentMode()
    {
        return modSilentMode;
    }

    public void setModSilentMode(boolean modSilentMode)
    {
        this.modSilentMode = modSilentMode;
    }


    public boolean isGenAllModFiles()
    {
        return genAllModFiles;
    }
    public void setGenAllModFiles(boolean genAllModFiles)
    {
        this.genAllModFiles = genAllModFiles;
    }


    public boolean isForceModFileRegeneration()
    {
        return forceModFileRegeneration;
    }
    public void setForceModFileRegeneration(boolean forceModFileRegeneration)
    {
        this.forceModFileRegeneration = forceModFileRegeneration;
    }



    public boolean isCopySimFiles()
    {
        return copySimFiles;
    }
    public void setCopySimFiles(boolean copySimFiles)
    {
        this.copySimFiles = copySimFiles;
    }



    public boolean isForceCorrectInit()
    {
        return forceCorrectInit;
    }
    public void setForceCorrectInit(boolean forceCorrectInit)
    {
        this.forceCorrectInit = forceCorrectInit;
    }




}
