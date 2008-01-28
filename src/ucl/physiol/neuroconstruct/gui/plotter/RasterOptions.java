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
 * Class to store info on options for showing a raster plot
 *
 * @author Padraig Gleeson
 *  
 */

public class RasterOptions
{
    int thickness = 2;
    float percentage = 90;

    float threshold = -20;

    public RasterOptions()
    {
    }

    public float getPercentage()
    {
        return percentage;
    }
    public int getThickness()
    {
        return thickness;
    }
    public void setPercentage(float percentage)
    {
        this.percentage = percentage;
    }
    public void setThickness(int thickness)
    {
        this.thickness = thickness;
    }
    public float getThreshold()
    {
        return threshold;
    }
    public void setThreshold(float threshold)
    {
        this.threshold = threshold;
    }
}
