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

package ucl.physiol.neuroconstruct.simulation;

import ucl.physiol.neuroconstruct.project.SimPlot;
import ucl.physiol.neuroconstruct.project.PostSynapticObject;

/**
 * Class which stores info on one recorded set of data from a simulation, e.g. one voltage trace. Could do with a better name...
 *
 * @author Padraig Gleeson
 *  
 */

public class DataStore
{
 

    private double[] dataPoints;

    private String cellGroupName = null;
    private int cellNumber = -1;
    private int segId = -1;
    private String variable = SimPlot.VOLTAGE;

    private String xUnit = "";
    private String yUnit = "";

    private double maxVal = -1* Double.MAX_VALUE;
    private double minVal = Double.MAX_VALUE;

    /**
     * Duplication of data here...
     */
    private PostSynapticObject pso = null;

    private boolean containsSpikeTimes = false;

    private double spikingVal = 1;
    private double nonSpikingVal = 0;

    private double startTime = 0;
    private double endTime = 100;
    private double timeStep = 0.01;


    public DataStore(double[] dataPoints,
                     String cellGroupName,
                     int cellNumber,
                     int segId,
                     String variable,
                     String xUnit,
                     String yUnit,
                     PostSynapticObject pso)
    {
        this.dataPoints = dataPoints;
        this.cellGroupName = cellGroupName;
        this.cellNumber = cellNumber;
        this.segId = segId;
        this.variable = variable;
        this.xUnit = xUnit;
        this.yUnit = yUnit;
        this.pso = pso;

        containsSpikeTimes = false;

        refreshMaxMin();
    }

    public DataStore(double[] spikeTimes,
                     double nonSpikingVal,
                     double spikingVal,
                     double startTime,
                     double endTime,
                     double timeStep,
                     String cellGroupName,
                     int cellNumber,
                     int segId,
                     String variable,
                     String xUnit,
                     String yUnit,
                     PostSynapticObject pso)
    {
        this.dataPoints = spikeTimes;

        this.spikingVal = spikingVal;
        this.nonSpikingVal = nonSpikingVal;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeStep = timeStep;

        this.cellGroupName = cellGroupName;
        this.cellNumber = cellNumber;
        this.segId = segId;
        this.variable = variable;
        this.xUnit = xUnit;
        this.yUnit = yUnit;
        this.pso = pso;

        containsSpikeTimes = true;

        refreshMaxMin();
    }

    public boolean isSpikeTimes()
    {
        return containsSpikeTimes;
    }

    public void setDataPoints(double[] dataPoints)
    {
        this.dataPoints = dataPoints;
        containsSpikeTimes = false;
        refreshMaxMin();
    }

    public void setSpikeTimes(double[] spikeTimes,
                              double nonSpikingVal,
                              double spikingVal,
                              double startTime,
                              double endTime,
                              double timeStep)
    {
        this.dataPoints = spikeTimes;
        this.spikingVal = spikingVal;
        this.nonSpikingVal = nonSpikingVal;
        this.nonSpikingVal = nonSpikingVal;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeStep = timeStep;
        containsSpikeTimes = true;
        refreshMaxMin();
    }

    private void refreshMaxMin()
    {
        if (containsSpikeTimes)
        {
            maxVal = Math.max(spikingVal, nonSpikingVal);
            minVal = Math.min(spikingVal, nonSpikingVal);
        }
        else
        {
            maxVal = -1* Double.MAX_VALUE;
            minVal = Double.MAX_VALUE;
            for (int i = 0; i < dataPoints.length; i++)
            {
                if (dataPoints[i]>maxVal) maxVal = dataPoints[i];
                if (dataPoints[i]<minVal) minVal = dataPoints[i];
            }
        }
    }

    public String getCellRef()
    {
        return SimulationData.getCellRef(cellGroupName, cellNumber);
    }

    public String getCellSegRef()
    {
        return SimulationData.getCellSegRef(cellGroupName, cellNumber, segId);
    }

    public String getCellGroupName()
    {
        return this.cellGroupName;
    }
    public String getVariable()
    {
        return this.variable;
    }

    public PostSynapticObject getPostSynapticObject()
    {
        return this.pso;
    }

    public boolean isSynapticMechData()
    {
        return pso != null;
    }



    public int getCellNumber()
    {
        return this.cellNumber;
    }

    public double[] getDataPoints()
    {
        if (!containsSpikeTimes)
        {
            return dataPoints;
        }
        else
        {
            double[] continuous 
                = SimulationData.convertSpikeTimesToContinuous(this.dataPoints, 
                                                               this.startTime, 
                                                               this.endTime, 
                                                               this.timeStep, 
                                                               this.nonSpikingVal,
                                                               this.spikingVal);
            return continuous;
        }
    }


    public int getAssumedSegmentId()
    {
        if (segId<0) return 0;
        return segId;
    }


    public double getMaxVal()
    {
        return this.maxVal;
    }

    public double getMinVal()
    {
        return this.minVal;
    }




    public boolean isSegmentSpecified()
    {
        return segId>0;
    }



    public String getXUnit()
    {
        return this.xUnit;
    }

    public String getYUnit()
    {
        return this.yUnit;
    }

    @Override
    public String toString()
    {
        String synInfo = "";
        if (pso!=null) synInfo =  " (synapse: "+pso.getSynRef()+")";
        String info = "DataStore"+synInfo+" for "+variable+" on segment: "+segId+" on "+ getCellRef();

        info = info + ": (";

        if (dataPoints.length==1) info = info + (float)dataPoints[0]+")";

        else if(dataPoints.length == 2) info = info + (float) dataPoints[0] + ", " + (float) dataPoints[1] + ")";

        else if(dataPoints.length == 3) info = info + (float) dataPoints[0] + ", " + (float) dataPoints[1] + ", " + (float) dataPoints[2] + ")";

        else info = info + (float) dataPoints[0] + ", " + (float) dataPoints[1] + ", ... [" + dataPoints.length + " entries])";

        return info;
    }



}
