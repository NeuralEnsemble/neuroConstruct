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

package ucl.physiol.neuroconstruct.gui.plotter;

import java.util.*;

import java.awt.*;
import java.util.Map.Entry;

import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Central point for controlling references to plot frames, to facilitate plotting multiple DataSets
 * in each PlotFrame.
 *
 * @author Padraig Gleeson
 *  
 */


public class PlotManager
{
    ClassLogger logger = new ClassLogger("PlotManager");

    private static HashMap<String, PlotterFrame> existingPlotFrames
                                    = new HashMap<String, PlotterFrame>();

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
                                               boolean setVisible)
    {
        return getPlotterFrame(reference, false, setVisible);
    }


    public static PlotterFrame getPlotterFrame(String reference, 
                                                boolean standalone, 
                                                boolean setVisible)
    {
        GuiUtils.setShowInfoGuis(true); // In case started at command line & Plot frame explicitly called => show dialogs for warnings etc.

        if (existingPlotFrames.containsKey(reference))
        {
            PlotterFrame frame = existingPlotFrames.get(reference);
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

        frame.setVisible(setVisible);

        return frame;
    }


    public static ArrayList<String> getPlotterFrameReferences()
    {
        ArrayList<String> allRefs = new ArrayList<String>();

        allRefs.addAll(existingPlotFrames.keySet());
        /*
        Enumeration<String> enumeration = existingPlotFrames.keys();
        while (enumeration.hasMoreElements()) {
            allRefs.add(enumeration.nextElement());

        }*/
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

    public static void arrangeFrames()
    {
        int num = existingPlotFrames.size();
        Iterator<PlotterFrame>  frames = existingPlotFrames.values().iterator();
        int i = 0;
        int[] layout = new int[]{2, 2}; // {across, down}

        if (num<=4) layout = new int[]{2,2};
        else if(num <= 6) layout = new int[]{2, 3};
        else if(num <= 9) layout = new int[]{3, 3};
        else if(num <= 12) layout = new int[]{3, 4};
        else if(num <= 16) layout = new int[]{4, 4};

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        while(frames.hasNext())
        {
            Frame f = frames.next();
            int index = i%(layout[0]*layout[1]);
            int x = i%(layout[0]);
            int y = (int)i/layout[0];

            //System.out.println("Frime "+i+": "+index+", x="+x+", y="+y);
            f.setSize(screenSize.width/layout[0], screenSize.height/layout[1]);
            f.setLocation(x*screenSize.width/layout[0], y*screenSize.height/layout[1]);
            i++;
        }
    }


    //Main method
    public static void main(String[] args) throws InterruptedException
    {
        int num = 7;
        for(int i=0;i<num;i++)
        {
            PlotterFrame framex = PlotManager.getPlotterFrame("Test_"+i, true, true);
            framex.addSampleData();
        }

        Thread.sleep(3000);

        System.out.println("Plotting all...");

        PlotManager.arrangeFrames();

        System.out.println("Arranged all...");

    }



}
