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

package ucl.physiol.neuroconstruct.gui.plotter;

/**
 * Class to store info on options for analysing the spiking patterns of plots
 *
 * @author Padraig Gleeson
 *  
 */

public class SpikeAnalysisOptions
{
    float threshold = -20;

    float startTime = 0;
    float stopTime = 100;


    public SpikeAnalysisOptions()
    {
    }

    public float getThreshold()
    {
        return threshold;
    }
    public void setThreshold(float threshold)
    {
        this.threshold = threshold;
    }
    public float getStopTime()
    {
        return stopTime;
    }
    public float getStartTime()
    {
        return startTime;
    }
    public void setStartTime(float startTime)
    {
        this.startTime = startTime;
    }
    public void setStopTime(float stopTime)
    {
        this.stopTime = stopTime;
    }
}
