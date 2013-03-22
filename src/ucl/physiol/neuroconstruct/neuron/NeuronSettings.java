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

    //private Hashtable<Integer, String> nativeBlocks = new Hashtable<Integer, String>();
    private Hashtable<Float, String> nativeHocBlocks = new Hashtable<Float, String>();

    private boolean showShapePlot = false;

    public enum GraphicsMode{ ALL_SHOW, NO_PLOTS, NO_CONSOLE };

    private GraphicsMode graphicsMode = GraphicsMode.ALL_SHOW;

    private boolean generateComments = true;

    private boolean useVarTimeStep = false;

    private float varTimeAbsTolerance = 0.001f; // Default in NEURON is 0.001
    
    private boolean genAllModFiles = false;

    private boolean forceModFileRegeneration = false;

    private boolean modSilentMode = true;
    
    private boolean copySimFiles = false;

    private boolean forceCorrectInit = true;

    public enum DataSaveFormat{ TEXT_NC, HDF5_NC };

    private DataSaveFormat dataSaveFormat = DataSaveFormat.TEXT_NC;

    //private subsApPropVel

    public NeuronSettings()
    {
    }



    /**
     * Should only be used by XMLEncoder, hence use of deprecated
     * @deprecated
     */
    public Hashtable<Float, String> getNativeBlocks()
    {
        logger.logComment("Calling getNativeBlocks...");
        return nativeHocBlocks;
    }

    /**
     * Should only be used by XMLEncoder, hence use of deprecated
     * @deprecated
     */
    public void setNativeBlocks(Hashtable nb)
    {
        logger.logComment("Calling setNativeBlocks...");

        Enumeration types = nb.keys();
        while (types.hasMoreElements())
        {
            Object t = types.nextElement();
            if (t instanceof Float)
            {
                nativeHocBlocks.put((Float)t, (String)nb.get(t));
            }
            if (t instanceof Integer) // old method of storing types
            {
                Float f = new Float((Integer)t);
                nativeHocBlocks.put(f, (String)nb.get(t));
            }
        }
    }

    /*
     * To deal with old method of storing native blocks with type Integer
     */
    private void checkNativeBlocks()
    {
        Enumeration e = nativeHocBlocks.keys();

        Hashtable<Float, String> newNativeHocBlocks = new Hashtable<Float, String>();

        while (e.hasMoreElements())
        {
            Object type = e.nextElement();
            logger.logComment("nativeHocBlocks has key: "+ type);
            if (type instanceof Integer)
            {
                String block = new String(nativeHocBlocks.get(type));
                nativeHocBlocks.remove(type);
                Float newType = new Float((Integer)type);
                newNativeHocBlocks.put(newType, block);
            }
            else if (type instanceof Float)
            {
                String block = new String(nativeHocBlocks.get(type));
                newNativeHocBlocks.put((Float)type, block);
            }
        }
        
        nativeHocBlocks = newNativeHocBlocks;

    }


    public String getNativeBlock(NativeCodeLocation ncl)
    {
        checkNativeBlocks();
        logger.logComment("Looking for text for location: ("+ ncl+") out of: "+ nativeHocBlocks.keySet());

        ArrayList<NativeCodeLocation> locsAvailable = NativeCodeLocation.getAllKnownLocations();

        for (int i = 0; i < locsAvailable.size(); i++)
        {
            NativeCodeLocation nextLoc = locsAvailable.get(i);
            logger.logComment("Checking: "+nextLoc.getPositionReference()+ " = "+ncl.getPositionReference());

            if (ncl.getPositionReference() == nextLoc.getPositionReference())
            {
                String text = nativeHocBlocks.get(new Float(ncl.getPositionReference()));
                if (text == null)
                    text = nativeHocBlocks.get(new Integer((int)ncl.getPositionReference()));
                
                logger.logComment("Found: "+ GeneralUtils.getMaxLenLine(text, 100));
                return text;
            }
        }
        logger.logComment("No matching block found!");
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
                nativeHocBlocks.put(new Float(ncl.getPositionReference()), text);
            }
        }
    }

    public DataSaveFormat getDataSaveFormat()
    {
        return dataSaveFormat;
    }

    public void setDataSaveFormat(DataSaveFormat dataSaveFormat)
    {
        this.dataSaveFormat = dataSaveFormat;
    }





    /**
     * Used to ensure compatibility with old method of code blocks...
     * @deprecated
     */
    public void setTextAfterCellCreation(String textAfterCellCreation)
    {
        //this.textAfterCellCreation = textAfterCellCreation;

        nativeHocBlocks.put(NativeCodeLocation.BEFORE_INITIAL.getPositionReference(), textAfterCellCreation);
    }

    /**
     * Used to ensure compatibility with old method of code blocks...
     * @deprecated
     */
    public void setTextBeforeCellCreation(String textBeforeCellCreation)
    {
        //this.textBeforeCellCreation = textBeforeCellCreation;

        nativeHocBlocks.put(NativeCodeLocation.BEFORE_CELL_CREATION.getPositionReference(), textBeforeCellCreation);

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

    /*
     * Currently these funcs are only used through Python interface
     */
    public float getVarTimeAbsTolerance()
    {
        return varTimeAbsTolerance;
    }

    public void setVarTimeAbsTolerance(float varTimeAbsTolerance)
    {
        this.varTimeAbsTolerance = varTimeAbsTolerance;
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
