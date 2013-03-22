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

import java.util.*;

import ucl.physiol.neuroconstruct.gui.ClickProjectHelper;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Storage for what variables in the cells to save/plot
 *
 * @author Padraig Gleeson
 *  
 */

public class GeneratedPlotSaves
{
    ClassLogger logger = new ClassLogger("GeneratedPlotSaves");

    //Hashtable<String, PlotSaveDetails> myPlotSaves = null;
    ArrayList<PlotSaveDetails> plotSaves = null;

    public GeneratedPlotSaves()
    {
        //myPlotSaves = new Hashtable<String, PlotSaveDetails>();
        plotSaves = new ArrayList<PlotSaveDetails>();

    }

    public void reset()
    {
        this.plotSaves.clear();
        logger.logComment("Reset called. Info: "+ this.toString());
    }

    public ArrayList<String> getAllPlotSaveRefs()
    {
        ArrayList<String> refs = new ArrayList<String>();

        for (PlotSaveDetails plotSave: plotSaves)
        {
            refs.add(plotSave.simPlot.getPlotReference());
        }

        return refs;
    }

    public ArrayList<PlotSaveDetails> getAllPlotSaves()
    {
        return plotSaves;
    }

    public ArrayList<PlotSaveDetails> getPlottedPlotSaves()
    {
        ArrayList<PlotSaveDetails> plots = new ArrayList<PlotSaveDetails>();

        for (PlotSaveDetails plotSave: plotSaves)
        {
            if (plotSave.simPlot.toBePlotted())
                plots.add(plotSave);
        }

        return plots;
    }

    public ArrayList<PlotSaveDetails> getSavedPlotSaves()
    {
        ArrayList<PlotSaveDetails> plots = new ArrayList<PlotSaveDetails>();

        for (PlotSaveDetails plotSave: plotSaves)
        {
            if (plotSave.simPlot.toBeSaved())
                plots.add(plotSave);
        }

        return plots;
    }






    public PlotSaveDetails getPlotSaveDetails(String ref)
    {
        for (PlotSaveDetails plotSave: plotSaves)
        {
            if (plotSave.simPlot.getPlotReference().equals(ref))
            {
                return plotSave;
            }
        }
        return null;

    }


    public void addPlotSaveDetails(String ref,
                                   SimPlot simPlot,
                                   ArrayList<Integer> cellNumsToPlot,
                                   ArrayList<Integer> segIdsToPlot,
                                   boolean allCellsInGroup,
                                   boolean allSegments)
    {
        PlotSaveDetails psd = new PlotSaveDetails();

        psd.simPlot = simPlot;
        psd.allCellsInGroup = allCellsInGroup;
        psd.allSegments = allSegments;
        psd.segIdsToPlot = segIdsToPlot;
        psd.cellNumsToPlot = cellNumsToPlot;

        plotSaves.add(psd);
    }


    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(getAllPlotSaveRefs()+"");

        return sb.toString();
    }

    public String details()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("There are "+plotSaves.size()+" PlotSaves present:");
        for (PlotSaveDetails plotSave: plotSaves)
        {
            sb.append("\n    "+plotSave.getDescription(false, false));
        }

        return sb.toString();
    }




    /**
     * In absence of a better name for something which can be plotted or saved...
     */
    public class PlotSaveDetails
    {
        /** @todo Add get/sets... */
        public SimPlot simPlot = null;
        public boolean allCellsInGroup = false;
        public boolean allSegments = false;


        public ArrayList<Integer> cellNumsToPlot = new ArrayList<Integer>();
        public ArrayList<Integer> segIdsToPlot = new ArrayList<Integer>();


        public PlotSaveDetails()
        {

        }


        public String getDescription(boolean saveDetailsOnly, boolean html)
        {
            StringBuilder sb = new StringBuilder();

            if (simPlot.getPlotAndOrSave().equals(SimPlot.PLOT_ONLY))
                sb.append("Plotting ");
            else if (simPlot.getPlotAndOrSave().equals(SimPlot.PLOT_AND_SAVE) && !saveDetailsOnly)
                sb.append("Plotting and saving ");
            else if (simPlot.getPlotAndOrSave().equals(SimPlot.SAVE_ONLY) || saveDetailsOnly)
                sb.append("Saving ");

            sb.append(simPlot.getValuePlotted()+ " on ");

            if (segIdsToPlot.size() == 1)
            {
                if (allSegments)
                {
                    sb.append("the only seg ");
                }
                else
                {
                    sb.append("only one seg, id: " + segIdsToPlot.get(0) + ", ");
                }
            }
            else
            {
                if (allSegments)
                {
                    sb.append("all " + segIdsToPlot.size() + " segments ");

                }
                else
                {
                    sb.append("only " + segIdsToPlot.size() + " segments ");
                }
            }

            if (cellNumsToPlot.size() == 1)
            {
                if (allCellsInGroup)
                {
                    sb.append("in the only cell in ");
                }
                else
                {
                    sb.append("in only cell: " + cellNumsToPlot.get(0) + " in ");
                }
            }
            else
            {
                if (allCellsInGroup)
                {
                    sb.append("in all " + cellNumsToPlot.size() + " cells in ");
                }
                else
                {
                    sb.append("in only " + cellNumsToPlot.size() + " cells in ");

                }
            }
            if (html)
                sb.append(ClickProjectHelper.getCellGroupLink(simPlot.getCellGroup()));
            else
                sb.append(simPlot.getCellGroup());


            return sb.toString();
        }

    }

    public static void main(String[] args)
    {
        try
        {

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }


}
