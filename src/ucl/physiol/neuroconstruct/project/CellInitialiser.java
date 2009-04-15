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

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.utils.*;
/**
 * Thread to handle initialisation of cells, in the case that initial potential is not fixed
 *
 * @author Padraig Gleeson
 *  
 */


public class CellInitialiser extends Thread
{
    ClassLogger logger = new ClassLogger("CellInitialiser");

    public final static String myGeneratorType = "CellInitialiser";

    Project project = null;
    boolean continueGeneration = true;

    GenerationReport myReportInterface = null;

    private SimConfig simConfig = null;


    public CellInitialiser(Project project, GenerationReport reportInterface)
    {
        super(myGeneratorType);

        logger.logComment("New CellInitialiser created");
        this.project = project;

        myReportInterface = reportInterface;

    }

    public void setSimConfig(SimConfig simConfig)
    {
        this.simConfig = simConfig;
    }



    public void stopGeneration()
    {
        logger.logComment("CellInitialiser being told to stop...");
        continueGeneration = false;
    }


    @Override
    public void run()
    {
        logger.logComment("Running CellInitialiser thread...");


        ArrayList<String> cellGroups = simConfig.getCellGroups();

        for (String cellGroup: cellGroups)
        {
            int num = project.generatedCellPositions.getNumberInCellGroup(cellGroup);

            if (num>0)
            {
                Cell cell = project.cellManager.getCell(project.cellGroupsInfo.getCellType(cellGroup));
                if (!cell.getInitialPotential().isTypeFixedNum())
                {

                    for (PositionRecord pr: project.generatedCellPositions.getPositionRecords(cellGroup))
                    {
                        float nextInitPot = cell.getInitialPotential().getNextNumber();
                        pr.setInitV(nextInitPot);
                    }
                }
            }

        }




        sendGenerationReport(false);

    }

    private void sendGenerationReport(boolean interrupted)
    {
        StringBuffer generationReport = new StringBuffer();


        generationReport.append("<b>Finished assigning initail potentials to cells</b>");


        if (myReportInterface!=null)
        {
            myReportInterface.giveGenerationReport(generationReport.toString(),
                                                   myGeneratorType,
                                                   simConfig);
        }
    }


}
