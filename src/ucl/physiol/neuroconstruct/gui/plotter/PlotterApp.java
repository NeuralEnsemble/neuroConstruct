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

import javax.swing.UIManager;
import ucl.physiol.neuroconstruct.gui.*;

/**
 * Application for popping up a graph of a vector of points
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */


public class PlotterApp
{
    boolean packFrame = true;

    //Construct the application
    public PlotterApp()
    {
        PlotterFrame frame = PlotManager.getPlotterFrame("Testo", true, true);
        //PlotManager.getPlotterFrame("Testo non standalone", false);

        System.out.println("All plots: "+ PlotManager.getPlotterFrameReferences());



        frame.addSampleData();

        frame.setViewMode(PlotCanvas.STACKED_VIEW);
    }

    //Main method
    public static void main(String[] args)
    {
        String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();
        try
        {
           // UIManager.setLookAndFeel(favouredLookAndFeel);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("Using look and feel: "+ UIManager.getLookAndFeel().getDescription());

        new PlotterApp();
    }
}
