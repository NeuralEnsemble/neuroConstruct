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

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.project.GeneratedPlotSaves.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;

/**
 * Info on something to plot **and/or save** during a simulation
 *
 * @author Padraig Gleeson
 *  
 */

public class SimPlot
{
    // known Values...
    public static String VOLTAGE = "VOLTAGE";
    public static String SPIKE = "SPIKE";
    public static String CONCENTRATION = "CONC";
    public static String COND_DENS = "COND_DENS";
    public static String CURR_DENS = "CURR_DENS";
    public static String REV_POT = "REV_POT";
    public static String SYNAPSES = "SYN";

    public static String SYN_COND = "COND";

    public static String SYN_CURR = "SYN_CURR";

    // whether to just plot or plot and save...
    public static String PLOT_ONLY = "Plot only";
    public static String SAVE_ONLY = "Save only";
    public static String PLOT_AND_SAVE = "Plot and save";

    //Extensions for files
    public static String CONTINUOUS_DATA_EXT = "dat";
    public static String SPIKE_EXT = "spike";
    public static String H5_EXT = "h5";


    public static float DEFAULT_THRESHOLD = -20;

    /**
     * Used to separate the element from the field, e.g. NaConductance:Gk
     */
    public static String PLOTTED_VALUE_SEPARATOR = ":";


    String plotReference = null;
    String graphWindow = null;

    String cellGroup = null;
    String cellNumber = null;
    String segmentId = null;
    String valuePlotted = VOLTAGE;

    String plotAndOrSave = PLOT_ONLY; // should have been enum...

    float minValue = -100;
    float maxValue = 100;

    public SimPlot(String plotReference,
                   String graphWindow,
                   String cellGroup,
                   String cellNumber,
                   String segmentId,
                   String valuePlotted,
                   float minValue,
                   float maxValue,
                   String plotAndOrSave)
    {
        this.plotReference = plotReference;
        this.graphWindow = graphWindow;
        this.cellGroup = cellGroup;
        this.cellNumber = cellNumber;
        this.segmentId = segmentId;
        this.valuePlotted = valuePlotted;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.plotAndOrSave = plotAndOrSave;
    }

    public SimPlot()
    {
    }
    
    
    @Override
    public Object clone()
    {
        SimPlot sp = new SimPlot(plotReference,
                            graphWindow,
                            cellGroup,
                            cellNumber,
                            segmentId,
                            valuePlotted,
                            this.minValue,
                            this.maxValue, plotAndOrSave);
        
        return sp;
    }

    /**
     * Gets the units if the quantity plotted can be determine from valuePlotted
     */
    public static String getUnitSymbol(String variable)
    {
    /**
     * Gets the units if the quantity plotted can be determine from valuePlotted
     */
        Units[] units = getUnits(variable);
        if (units==null) 
            return "";
        return units[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol();
        
    }
    
    public static Units[] getUnits(String variable)
    {
        if (variable.indexOf(SimPlot.VOLTAGE) >= 0)
        {
            return UnitConverter.voltageUnits;
        }
        if (variable.indexOf(SimPlot.SPIKE) >= 0)
        {
            return UnitConverter.voltageUnits;
        }
        else if (variable.indexOf(SimPlot.COND_DENS) >= 0)
        {
            return UnitConverter.conductanceDensityUnits;
        }
        else if (variable.indexOf(SimPlot.CONCENTRATION) >= 0)
        {
            return UnitConverter.concentrationUnits;
        }
        else if (variable.indexOf(SimPlot.CURR_DENS) >= 0)
        {
            return UnitConverter.currentDensityUnits;
        }
        else if (variable.indexOf(SimPlot.REV_POT) >= 0)
        {
            return UnitConverter.voltageUnits;
        }
        else if (variable.indexOf(SimPlot.SYN_COND) >= 0)
        {
            return UnitConverter.conductanceUnits;
        }
        else if (variable.indexOf(SimPlot.SYN_CURR) >= 0)
        {
            return UnitConverter.currentUnits;
        }

        return null;
    }

    /**
     * Gets an appropriate legend for graphs if the quantity plotted can be determine from valuePlotted
     */
    public static String getLegend(String variable)
    {
        if (variable.indexOf(SimPlot.VOLTAGE) >= 0)
        {
            return "Membrane Potential";
        }
        if (variable.indexOf(SimPlot.SPIKE) >= 0)
        {
            return "Spike";
        }
        else if (variable.indexOf(SimPlot.COND_DENS) >= 0)
        {
            return "Conductance density";
        }
        else if (variable.indexOf(SimPlot.CONCENTRATION) >= 0)
        {
            return "Concentration";
        }
        else if (variable.indexOf(SimPlot.CURR_DENS) >= 0)
        {
            return "Currrent density";
        }
        else if (variable.indexOf(SimPlot.REV_POT) >= 0)
        {
            return "Reversal potential";
        }
        else if (variable.indexOf(SimPlot.SYN_COND) >= 0)
        {
            return "Synaptic conductance";
        }
        else if (variable.indexOf(SimPlot.SYN_CURR) >= 0)
        {
            return "Synaptic current";
        }

        return "";
    }

    public boolean isVoltage()
    {
        return valuePlotted.equals(VOLTAGE);
    }

    public boolean isSynapticMechanism()
    {
        return (valuePlotted.indexOf(SimPlot.SYNAPSES) >= 0);
    }

    public static String getNetConnName(String synapticMechVariable)
    {
        if (synapticMechVariable.indexOf(SimPlot.SYNAPSES) < 0)
        {
            return null;
        }
        int firstToken = synapticMechVariable.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR);
        int secondToken = synapticMechVariable.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR, firstToken+1);
        String netConnName = synapticMechVariable.substring(firstToken+1, secondToken);

        return netConnName;
    }

    public static String getSynapseType(String synapticMechVariable)
    {
        if (synapticMechVariable.indexOf(SimPlot.SYNAPSES) < 0)
        {
            return null;
        }
        int firstToken = synapticMechVariable.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR);
        int secondToken = synapticMechVariable.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR, firstToken+1);
        int thirdToken = synapticMechVariable.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR, secondToken+1);
        String synType = synapticMechVariable.substring(secondToken+1, thirdToken);

        return synType;
    }


    public static String getSynapseVariable(String synapticMechVariable)
    {
        if (synapticMechVariable.indexOf(SimPlot.SYNAPSES) < 0)
        {
            return null;
        }
        int firstToken = synapticMechVariable.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR);
        int secondToken = synapticMechVariable.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR, firstToken+1);
        int thirdToken = synapticMechVariable.indexOf(SimPlot.PLOTTED_VALUE_SEPARATOR, secondToken+1);
        String synVar = synapticMechVariable.substring(thirdToken+1);

        return synVar;
    }



    public String getCellGroup()
    {
        return cellGroup;
    }
    public String getCellNumber()
    {
        return cellNumber;
    }

    public float getMaxValue()
    {
        return maxValue;
    }
    public float getMinValue()
    {
        return minValue;
    }

    public String getPlotReference()
    {
        return plotReference;
    }
    public String getValuePlotted()
    {
        return valuePlotted;
    }

    public void setCellGroup(String cellGroup)
    {
        this.cellGroup = cellGroup;
    }
    public void setCellNumber(String cellNumber)
    {
        this.cellNumber = cellNumber;
    }

    public void setMaxValue(float maxValue)
    {
        this.maxValue = maxValue;
    }
    public void setMinValue(float minValue)
    {
        this.minValue = minValue;
    }

    public void setPlotReference(String plotReference)
    {
        // as spaces in a reference will lead to errors..
        this.plotReference = GeneralUtils.replaceAllTokens(plotReference, " ", "_");
    }
    public void setValuePlotted(String valuePlotted)
    {
        this.valuePlotted = valuePlotted;
    }
    public String getGraphWindow()
    {
        return graphWindow;
    }

    public String getPlotAndOrSave()
    {
        return plotAndOrSave;
    }

    public boolean toBePlotted()
    {
        return plotAndOrSave.equals(PLOT_ONLY) || plotAndOrSave.equals(PLOT_AND_SAVE);
    }

    public boolean toBeSaved()
    {
        return plotAndOrSave.equals(SAVE_ONLY) || plotAndOrSave.equals(PLOT_AND_SAVE);
    }



    public void setPlotAndOrSave(String plotAndOrSave)
    {
        this.plotAndOrSave = plotAndOrSave;
    }

    public void setGraphWindow(String graphWindow)
    {
        this.graphWindow = graphWindow;
    }

    public String getSegmentId()
    {
        return segmentId;
    }
    public void setSegmentId(String segmentId)
    {
        this.segmentId = segmentId;
    }

    @Override
    public String toString()
    {
        return "Plot/save of "+ this.getValuePlotted()+" in "+ this.getCellGroup();
    }

    public String getPlotSaveString()
    {
        if (plotAndOrSave.equals(PLOT_ONLY))
            return "Plotting";
        else if(plotAndOrSave.equals(SAVE_ONLY))
            return "Saving";
        else if(plotAndOrSave.equals(PLOT_AND_SAVE))
            return "Plotting and saving";
        return "";
    }


    public String getSafeVarName()
    {
        String varName = GeneralUtils.replaceAllTokens(getValuePlotted(), ":", "_");

        return GeneralUtils.replaceAllTokens(varName, "-", "min");
    }




    public static String getFilename(PlotSaveDetails record, Segment segment, String cellNumber)
    {
        String variable = ""; // ignore this if its voltage...

        if (!record.simPlot.getValuePlotted().equals(SimPlot.VOLTAGE))
        {
            String varName = record.simPlot.getSafeVarName();
            variable = "."+ varName;

        }

        String extension = CONTINUOUS_DATA_EXT;
        if (record.simPlot.getValuePlotted().indexOf(SimPlot.SPIKE)>=0) extension = SPIKE_EXT;

        String fileName
            = SimEnvHelper.getSimulatorFriendlyName(record.simPlot.getCellGroup() + "_"+cellNumber+"." +
                                                    segment.getSegmentId() + variable+ "."+extension);

        if (record.segIdsToPlot.size() == 1 && segment.isRootSegment())
        {
            // will use this to colour the whole cell
            fileName
                = SimEnvHelper.getSimulatorFriendlyName(record.simPlot.getCellGroup() + "_"+cellNumber+""+variable+"."+extension);

        }
        return fileName;

    }



    public static String getFilename(PlotSaveDetails record, PostSynapticObject pso, String cellNumber)
    {
        String variable = ""; // ignore this if its voltage...

        variable = "."+ pso.getSynRef() + "."+SimPlot.getSynapseVariable(record.simPlot.valuePlotted);


        String extension = CONTINUOUS_DATA_EXT;

        String fileName
            = SimEnvHelper.getSimulatorFriendlyName(record.simPlot.getCellGroup() + "_"+cellNumber+"." +
                                                    pso.segmentId + variable+ "."+extension);


        return fileName;

    }



}
