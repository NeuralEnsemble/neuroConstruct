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

package ucl.physiol.neuroconstruct.gui.plotter;

import java.util.*;

import java.awt.*;

import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Central point for controlling references to plot frames, to facilitate plotting multiple DataSets
 * in each PlotFrame.
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public class PlotManager
{
    ClassLogger logger = new ClassLogger("PlotManager");

    private static Hashtable<String, PlotterFrame> existingPlotFrames 
                                    = new Hashtable<String, PlotterFrame>();

    /**
     * Reference to current project, needed for interaction with saved projects
     */
    private static Project project = null;


    /**
     * Creates a new PlotterFrame if one with that title isn't present, and if it is returns the
     * existing one (and so allows 2 or more data sets in same frame...)
     */
    public static PlotterFrame getPlotterFrame(String reference)
    {
        return getPlotterFrame(reference, false, true);
    }


    public static PlotterFrame getPlotterFrame(String reference, 
                                                boolean standalone, 
                                                boolean setVisible)
    {

        if (existingPlotFrames.containsKey(reference))
        {
            PlotterFrame frame = (PlotterFrame)existingPlotFrames.get(reference);
            return frame;
        }
        PlotterFrame frame = new PlotterFrame(reference, project, standalone);
        existingPlotFrames.put(reference, frame);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);


        frame.setVisible(setVisible);

        return frame;
    }


    public static Vector<String> getPlotterFrameReferences()
    {
        Vector<String> allRefs = new Vector<String>();
        Enumeration<String> enumeration = existingPlotFrames.keys();
        while (enumeration.hasMoreElements()) {
            allRefs.add(enumeration.nextElement());

        }
        return allRefs;
    }

    public static void plotFrameClosing(String ref)
    {
        existingPlotFrames.remove(ref);
    }

    public static void setCurrentProject(Project currProject)
    {
        project = currProject;
    }



}
