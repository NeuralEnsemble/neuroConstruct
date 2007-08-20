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

package ucl.physiol.neuroconstruct.utils;

import java.util.*;

import java.awt.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.*;

/**
 * Storing of various properties related to how the cells should be displayed in 3D
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */


public class Display3DProperties
{

    public static final String DISPLAY_SOMA_NEURITE_SOLID = "All solid";
    public static final String DISPLAY_SOMA_NEURITE_SOLID_UNSHINY = "All solid (unshiny)";
    public static final String DISPLAY_SOMA_SOLID_NEURITE_NONE= "Soma solid, no neurites";
    public static final String DISPLAY_SOMA_SOLID_NEURITE_LINE= "Soma solid, neurite lines";
    public static final String DISPLAY_SOMA_LINE_NEURITE_LINE = "All lines";



    /**
     * Main colour of background
     */
    private Color backgroundColour3D = null;

    /**
     * Default colour for a cell when just one is shown (as in OneCell3D)
     */
    private Color cellColour3D = null;

    private boolean show3DAxes = false;
    private boolean showRegions = true;
    private boolean showInputs = true;
    private boolean showAxonalArbours = true;
    private boolean showSynapseConns = true;
    private boolean showSynapseEndpoints = true;

    private String displayOption = DISPLAY_SOMA_NEURITE_SOLID;

    /**
     * Indication of resolution for creating cylinders/sphere's etc. in 3D. Quicker, but not as
     * nice looking when smaller
     */
    private int resolution3DElements = 30;


    private float transparency = 0.85f;


    public Display3DProperties()
    {
    }


    public Display3DProperties(Color backgroundColour3D,
                               Color cellColour3D,
                               boolean show3DAxes,
                               boolean showRegions,
                               boolean showInputs,
                               boolean showAxonalArbours,
                               boolean showSynapseConns,
                               boolean showSynapseEndpoints,
                               String dendriteDisplayOption,
                               float transparency)
    {
        this.backgroundColour3D = backgroundColour3D;
        this.cellColour3D = cellColour3D;
        this.show3DAxes = show3DAxes;
        this.showRegions = showRegions;
        this.showInputs = showInputs;
        this.showAxonalArbours = showAxonalArbours;
        this.showSynapseConns = showSynapseConns;
        this.showSynapseEndpoints = showSynapseEndpoints;
        this.displayOption = dendriteDisplayOption;
        this.transparency = transparency;
    }
    
    public String toString()
    {
    	return "Display3DProperties: backgroundColour3D: "+backgroundColour3D+", displayOption: "+displayOption;
    }



    /**
     * Gets the defaults from GeneralProperties. This is put in a separate
     * function (a.o.t the constructor) to allow the XMLEncoder to record the
     * correct values at the time of saving
     */
    public void initialiseDefaultValues()
    {
        backgroundColour3D = GeneralProperties.getDefault3DBackgroundColor();
        cellColour3D = GeneralProperties.getDefaultCellColor3D();

        show3DAxes = GeneralProperties.getDefault3DAxesOption();
        showRegions = GeneralProperties.getDefaultShowRegions();
        showInputs = GeneralProperties.getDefaultShowInputs();
        this.showAxonalArbours = GeneralProperties.getDefaultShowAxonalArbours();
        showSynapseConns = GeneralProperties.getDefaultShowSynapseConns();
        showSynapseEndpoints = GeneralProperties.getDefaultShowSynapseEndpoints();
        displayOption = GeneralProperties.getDefaultDisplayOption();
        resolution3DElements = GeneralProperties.getDefaultResolution3DElements();
    }

    /**
     * Get allowable display formats
     * @param inclComps include compartmentalisation options
     */
    public static Vector getDisplayOptions(boolean inclComps)
    {
        Vector<Object> options = new Vector<Object>();
        options.add(DISPLAY_SOMA_NEURITE_SOLID);
        //options.add(DISPLAY_SOMA_NEURITE_SOLID_UNSHINY);
        options.add(DISPLAY_SOMA_SOLID_NEURITE_LINE);
        options.add(DISPLAY_SOMA_SOLID_NEURITE_NONE);
        options.add(DISPLAY_SOMA_LINE_NEURITE_LINE);
        //options.add(DISPLAY_NEURON);
        //options.add(DISPLAY_GENESIS_SIMPLE);
        //options.add(DISPLAY_GENESIS_MULTI);

        if (inclComps)
        {
            ArrayList<MorphCompartmentalisation> morphProjs = CompartmentalisationManager.getAllMorphProjections();

            for (int i = 0; i < morphProjs.size(); i++)
            {
                options.add(morphProjs.get(i));
            }
        }

        return options;
    }


    public float getTransparency()
    {
        return this.transparency;
    }

    public void setTransparency(float transparency)
    {
        this.transparency = transparency;
    }


    public Color getBackgroundColour3D()
    {
        return backgroundColour3D;
    }


    public void setBackgroundColour3D(Color colour)
    {
        backgroundColour3D = colour;
    }


    public boolean getShow3DAxes()
    {
        return show3DAxes;
    }


    public void setShow3DAxes(boolean showAxes)
    {
        show3DAxes = showAxes;
    }


    public boolean getShowInputs()
    {
        return showInputs;
    }


    public void setShowInputs(boolean show)
    {
        this.showInputs = show;
    }


    public boolean getShowAxonalArbours()
    {
        return showAxonalArbours;
    }


    public void setShowAxonalArbours(boolean show)
    {
        this.showAxonalArbours = show;
    }




    /**
     * How to display the dendrites, etc. sticks or with diameters
     *
     */
    public String getDisplayOption()
    {
    	if (displayOption== null) displayOption = DISPLAY_SOMA_NEURITE_SOLID;
        return displayOption;
    }

    /**
     * Returns wether this display is not of the original cell, but a compartmentalisation
     */
    public boolean isCompartmentalisationDisplay()
    {
        return displayOption.indexOf("Compartmentalisation")>0;
    }

    /**
     * How to display the dendrites, etc. sticks or with diameters
     */
    public void setDisplayOption(String dispOpt)
    {
        displayOption = dispOpt;
    }



    public boolean getShowRegions()
    {
        return showRegions;
    }


    public void setShowRegions(boolean show)
    {
        showRegions = show;
    }


    public boolean getShowSynapseConns()
    {
        return showSynapseConns;
    }


    public void setShowSynapseConns(boolean show)
    {
        showSynapseConns = show;
    }


    public boolean getShowSynapseEndpoints()
    {
        return showSynapseEndpoints;
    }


    public void setShowSynapseEndpoints(boolean show)
    {
        showSynapseEndpoints = show;
    }
    public int getResolution3DElements()
    {
        return resolution3DElements;
    }
    public void setResolution3DElements(int resolution3DElements)
    {
        this.resolution3DElements = resolution3DElements;
    }
    public Color getCellColour3D()
    {
        return cellColour3D;
    }
    public void setCellColour3D(Color cellColour3D)
    {
        this.cellColour3D = cellColour3D;
    }



}
