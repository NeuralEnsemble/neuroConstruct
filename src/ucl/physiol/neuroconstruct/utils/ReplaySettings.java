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


/**
 * A number of settings required from the user
 *
 * @author Padraig Gleeson
 *  
 */

public class ReplaySettings
{
    private int millisBetweenShownFrames = 0;
    private int numberofFramesToHide = 10;


    public ReplaySettings()
    {
    }

    public String toString()
    {
        return "ReplaySettings ["
            + "millisBetweenShownFrames = "
            + millisBetweenShownFrames
            + ", numberofFramesToHide = "
            + numberofFramesToHide
            + "]";
    }
    public int getMillisBetweenShownFrames()
    {
        return millisBetweenShownFrames;
    }
    public int getNumberofFramesToHide()
    {
        return numberofFramesToHide;
    }
    public void setMillisBetweenShownFrames(int millisBetweenShownFrames)
    {
        this.millisBetweenShownFrames = millisBetweenShownFrames;
    }
    public void setNumberofFramesToHide(int numberofFramesToHide)
    {
        this.numberofFramesToHide = numberofFramesToHide;
    }

}
