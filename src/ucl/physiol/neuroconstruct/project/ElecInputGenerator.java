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


import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.SegmentLocation;
import ucl.physiol.neuroconstruct.project.cellchoice.*;
import ucl.physiol.neuroconstruct.project.segmentchoice.AllSegmentsChosenException;
import ucl.physiol.neuroconstruct.project.segmentchoice.SegmentChooserException;
import ucl.physiol.neuroconstruct.project.stimulation.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Thread to handle generation of the electrical inputs based on the project settings
 *
 * @author Padraig Gleeson
 *  
 */


public class ElecInputGenerator extends Thread
{
    ClassLogger logger = new ClassLogger("ElecInputGenerator");

    public final static String myGeneratorType = "ElecInputGenerator";

    Project project = null;
    long startGenerationTime;
    boolean continueGeneration = true;

    GenerationReport myReportInterface = null;

    private SimConfig simConfig = null;


    public ElecInputGenerator(Project project, GenerationReport reportInterface)
    {
        super(myGeneratorType);

        logger.logComment("New ElecInputGenerator created");
        this.project = project;

        myReportInterface = reportInterface;

    }

    public void setSimConfig(SimConfig simConfig)
    {
        this.simConfig = simConfig;
    }



    public void stopGeneration()
    {
        logger.logComment("ElecInputGenerator being told to stop...");
        continueGeneration = false;
    }


    public ArrayList<String> getRelevantElecInputs()
    {
        Vector allElecInputs = project.elecInputInfo.getAllStimRefs();

        ArrayList<String> allElecInputsInSimConfig = simConfig.getInputs();

        ArrayList<String> elecInputsInSimConfig = new ArrayList<String>();

        for (int i = 0; i < allElecInputsInSimConfig.size(); i++)
        {
            if (allElecInputs.contains(allElecInputsInSimConfig.get(i)))
                elecInputsInSimConfig.add(allElecInputsInSimConfig.get(i));
        }

        return elecInputsInSimConfig;

    }


    @Override
    public void run()
    {
        logger.logComment("Running ElecInputGenerator thread...");

        startGenerationTime = System.currentTimeMillis();

        ArrayList<String> elecInputsInSimConfig = getRelevantElecInputs();

        project.generatedElecInputs.reset();

        for (int j = 0; j < elecInputsInSimConfig.size(); j++)
        {
            if (!continueGeneration)
            {
                logger.logComment("Discontinuing generation...");
                sendGenerationReport(true);
                return;
            }

            String elecInputRef =  elecInputsInSimConfig.get(j);

            logger.logComment("Looking at ElecInput: " + elecInputRef);


                this.myReportInterface.giveUpdate("Generating input: " + elecInputRef+"...");

            StimulationSettings nextStim = project.elecInputInfo.getStim(elecInputRef);

            logger.logComment("nextStim chooser: " + nextStim.getCellChooser());

            if (!project.cellGroupsInfo.getAllCellGroupNames().contains(nextStim.getCellGroup()))
            {
                GuiUtils.showErrorMessage(logger, "The Cell Group specified for the Stimulation: " + nextStim.getReference() +
                                           " does not exist!", null, null);
                sendGenerationReport(true);
                return;

            }

            CellChooser cellChooser = nextStim.getCellChooser();
            
            ArrayList<PositionRecord> positions = project.generatedCellPositions.getPositionRecords(nextStim.getCellGroup());

            cellChooser.initialise(positions);
            
            if (cellChooser instanceof RegionAssociatedCells)
            {
                RegionAssociatedCells rac = (RegionAssociatedCells)cellChooser;
                
                String region = rac.getParameterStringValue(RegionAssociatedCells.REGION_NAME);
                
                if (project.regionsInfo.getRegionObject(region)==null)
                {
                    GuiUtils.showErrorMessage(logger, "Error. Region: "+region+" specified for electrical input: "+elecInputRef+
                            " does not exist", null, null);
                    
                }

                rac.setProject(project);  // to give info on regions...
            }

            try
            {
                while(true)
                {
                    logger.logComment("Getting the next cell num...");
                    int nextCellNumber = cellChooser.getNextCellIndex();

                    logger.logComment("Adding stim to cell number: "+ nextCellNumber);

                    
                    Cell cell = project.cellManager.getCell(project.cellGroupsInfo.getCellType(nextStim.getCellGroup()));
                    
                    nextStim.getSegChooser().initialise(cell);

                    try
                    {
                        while (true)
                        {
                            SegmentLocation sl = nextStim.getSegChooser().getNextSegLoc();

                            InputInstanceProps ip = null;

                            if (nextStim.getElectricalInput() instanceof IClamp)
                            {
                                IClamp ic = (IClamp)nextStim.getElectricalInput();

                                if (!ic.getDel().isTypeFixedNum() ||
                                    !ic.getDur().isTypeFixedNum() ||
                                    !ic.getAmp().isTypeFixedNum())
                                {
                                    IClampInstanceProps icip = new IClampInstanceProps();
                                    icip.setDelay(ic.getDel().getNextNumber());
                                    icip.setDuration(ic.getDur().getNextNumber());
                                    icip.setAmplitude(ic.getAmp().getNextNumber());
                                    ip = icip;
                                }


                            }
                            if (nextStim.getElectricalInput() instanceof IClampVariable)
                            {
                                IClampVariable ic = (IClampVariable)nextStim.getElectricalInput();

                                if (!ic.getDel().isTypeFixedNum() ||
                                    !ic.getDur().isTypeFixedNum())
                                {
                                    IClampVariableInstanceProps icip = new IClampVariableInstanceProps();
                                    icip.setDelay(ic.getDel().getNextNumber());
                                    icip.setDuration(ic.getDur().getNextNumber());
                                    ip = icip;
                                }
                            }

                            if (nextStim.getElectricalInput() instanceof RandomSpikeTrain)
                            {
                                RandomSpikeTrain rst = (RandomSpikeTrain)nextStim.getElectricalInput();

                                if (!rst.getRate().isTypeFixedNum())
                                {
                                    RandomSpikeTrainInstanceProps rstip = new RandomSpikeTrainInstanceProps();
                                    rstip.setRate(rst.getRate().getNextNumber());
                                    ip = rstip;
                                }


                            }

                            if (nextStim.getElectricalInput() instanceof RandomSpikeTrainExt)
                            {
                                RandomSpikeTrainExt rste = (RandomSpikeTrainExt)nextStim.getElectricalInput();

                                if (!rste.getRate().isTypeFixedNum() ||
                                    !rste.getDelay().isTypeFixedNum() ||
                                    !rste.getDuration().isTypeFixedNum())
                                {
                                    RandomSpikeTrainExtInstanceProps rsteip = new RandomSpikeTrainExtInstanceProps();
                                    rsteip.setRate(rste.getRate().getNextNumber());
                                    rsteip.setDelay(rste.getDelay().getNextNumber());
                                    rsteip.setDuration(rste.getDuration().getNextNumber());
                                    ip = rsteip;
                                }
                            }

                            if (nextStim.getElectricalInput() instanceof RandomSpikeTrainVariable)
                            {
                                RandomSpikeTrainVariable rste = (RandomSpikeTrainVariable)nextStim.getElectricalInput();

                                if (!rste.getDelay().isTypeFixedNum() ||
                                    !rste.getDuration().isTypeFixedNum())
                                {
                                    RandomSpikeTrainVarInstanceProps rsteip = new RandomSpikeTrainVarInstanceProps();
                                    rsteip.setDelay(rste.getDelay().getNextNumber());
                                    rsteip.setDuration(rste.getDuration().getNextNumber());
                                    ip = rsteip;
                                }
                            }
                            
                            project.generatedElecInputs.addSingleInput(nextStim.getReference(), 
                                                                       nextStim.getElectricalInput().getType(), 
                                                                       nextStim.getCellGroup(), 
                                                                       nextCellNumber, 
                                                                       sl.getSegmentId(), 
                                                                       sl.getFractAlong(), 
                                                                       ip);

                        }
                    }
                    catch (AllSegmentsChosenException ex)
                    {
                        logger.logComment("All segs which could be generated have been..");
                    }
                    catch (SegmentChooserException ex)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem determining segment locations from: "+nextStim.getSegChooser(), ex, null);
                        return;
                        
                    }
                    
                    

                }
            }
            catch (CellChooserException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem adding cells to be stimulated", ex, null);
            }
            catch (AllCellsChosenException ex)
            {
                logger.logComment("Normal end of loop...");
            }



            if (myReportInterface!=null) myReportInterface.majorStepComplete();

        } // for...

        // Finished the main generation part...


        //System.out.println("Stims generated: "+ project.generatedElecInputs);

        sendGenerationReport(false);

    }

    private void sendGenerationReport(boolean interrupted)
    {
        StringBuffer generationReport = new StringBuffer();


        ArrayList<String> elecInputsInSimConfig = getRelevantElecInputs();


        long generationTime = System.currentTimeMillis();
        float seconds = (float) (generationTime - startGenerationTime) / 1000f;

        generationReport.append("<center><b>Electrical Inputs:</b></center>");
        generationReport.append("Time taken to generate Electrical Inputs: " + seconds +
                                " seconds.<br>");

        if (interrupted)
            generationReport.append("<center><b>NOTE: Generation interrupted</b></center><br>");

        generationReport.append(project.generatedElecInputs.getHtmlReport());
        /*
        if (elecInputsInSimConfig.size() == 0)
        {
            generationReport.append("No Electrical Inputs generated<br><br>");

        }
        for (int i = 0; i < elecInputsInSimConfig.size(); i++)
        {
            String elecInputName = elecInputsInSimConfig.get(i);
            StimulationSettings s = project.elecInputInfo.getStim(elecInputName);

            generationReport.append("<b>" + ClickProjectHelper.getElecInputLink(elecInputName) + "</b> ("+s.getElectricalInput().toLinkedString()+" on "+s.getCellChooser().toNiceString()+" of "
                                    + ClickProjectHelper.getCellGroupLink(s.getCellGroup())+", seg: "+s.getSegmentID()+")<br>");

            generationReport.append("Number of individual inputs: <b>"
                                    +
                                    project.generatedElecInputs.getNumberSingleInputs(elecInputName)
                                    + "</b><br><br>");
        }*/

        if (myReportInterface!=null)
        {
            myReportInterface.giveGenerationReport(generationReport.toString(),
                                                   myGeneratorType,
                                                   simConfig);
        }
    }


}
