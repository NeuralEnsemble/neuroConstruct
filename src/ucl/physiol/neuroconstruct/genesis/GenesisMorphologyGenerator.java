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

package ucl.physiol.neuroconstruct.genesis;

import java.io.*;
import java.util.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.compartment.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.utils.xml.*;


/**
 * A single GENESIS morphology file from a Cell object
 *
 * @author Padraig Gleeson
 *  
 */

public class GenesisMorphologyGenerator
{
    ClassLogger logger = new ClassLogger("GenesisMorphologyGenerator");

    Cell cell = null;

    File morphFile = null;


    Project project = null;

    private GenesisMorphologyGenerator()
    {
    }


    /**
     * Generates the Genesis morphology for a single cell.
     * @param cell The Cell to generate for
     * @param project The project to generate for
     * @param dirForGenesisFile the Directory to generate into
     */
    public GenesisMorphologyGenerator(Cell cell,
                                      Project project,
                                      File dirForGenesisFile)
    {
        logger.logComment("GenesisMorphologyGenerator created for: " + cell.toString());
        this.cell = cell;

        StringBuffer spaceLessName = new StringBuffer();

        this.project = project;

        for (int i = 0; i < cell.getInstanceName().length(); i++)
        {
            char c = cell.getInstanceName().charAt(i);
            if (c != ' ') spaceLessName.append(c);
        }

        morphFile = new File(dirForGenesisFile, spaceLessName + ".p");


    }


    public String getFilename()
    {
        return this.morphFile.getAbsolutePath();
    }


    public String generateFile() throws GenesisException
    {
        logger.logComment("Starting generation of template file: " + morphFile);

        FileWriter fw = null;
        try
        {
            fw = new FileWriter(morphFile);

            fw.write("\n");
            fw.write(GenesisFileManager.getGenesisFileHeader());

            fw.write(this.getCommonHeader());
            fw.write(this.getMainMorphology());

            fw.flush();
            fw.close();
        }
        catch (Exception ex)
        {
            logger.logError("Error writing to file: " + morphFile, ex);
            try
            {
                fw.flush();
                fw.close();
            }
            catch (IOException ex1)
            {
            }
            throw new GenesisException("Error writing to file: " + morphFile, ex);

        }

        return morphFile.getAbsolutePath();
    }




    private String getCommonHeader()
    {
        logger.logComment("calling getCommonHeader");
        StringBuffer response = new StringBuffer();

        response.append("*absolute\n");
        response.append("*cartesian\n");

        if (project.genesisSettings.isSymmetricCompartments())
            response.append("*symmetric\n\n");
        else
            response.append("*asymmetric\n\n");

        return response.toString();
    }

    private String getMainMorphology()
    {
        logger.logComment("calling getMainMorphology");
        StringBuffer response = new StringBuffer();

        //Vector segments = cell.getAllSegments();
        Vector<Segment> segments = cell.getExplicitlyModelledSegments();

        logger.logComment("Investigating " + segments.size() + " segments...");
        String line = null;
        float lastSpecAxRes = -1;
        float lastSpecCap = -1;

        float globalRA = cell.getSpecAxResForGroup(Section.ALL);
        if (!Float.isNaN(globalRA))
        {
            line = "*set_global RA "
                + UnitConverter.getSpecificAxialResistance(globalRA,
                                                           UnitConverter.NEUROCONSTRUCT_UNITS,
                                                           project.genesisSettings.getUnitSystemToUse());
            lastSpecAxRes = globalRA;
            if (project.genesisSettings.isGenerateComments())
            {
                line = line + "  // using: "
                    + UnitConverter.specificAxialResistanceUnits[project.genesisSettings.getUnitSystemToUse()];

            }
            response.append(line+"\n\n");
        }

        float globalCM = cell.getSpecCapForGroup(Section.ALL);
                if (!Float.isNaN(globalCM))
        {
            line = "*set_global CM "
                + UnitConverter.getSpecificCapacitance(globalCM,
                                                       UnitConverter.NEUROCONSTRUCT_UNITS,
                                                       project.genesisSettings.getUnitSystemToUse());
            lastSpecCap = globalCM;
            if (project.genesisSettings.isGenerateComments())
                line = line + "   // using: "
                    + UnitConverter.specificCapacitanceUnits[project.genesisSettings.getUnitSystemToUse()];

            response.append(line+"\n\n");
        }

        line = "*set_global	RM	"
                        + UnitConverter.getSpecificMembraneResistance(project.simulationParameters.getGlobalRm(),
                                                                      UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                      project.genesisSettings.getUnitSystemToUse());

        if (project.genesisSettings.isGenerateComments())
                        line = line + "  // using: "
                        + UnitConverter.specificMembraneResistanceUnits[project.genesisSettings.getUnitSystemToUse()];

        response.append(line+"\n\n");


        line = "*set_global     EREST_ACT	"
                        + UnitConverter.getVoltage(cell.getInitialPotential().getNominalNumber(),
                                                   UnitConverter.NEUROCONSTRUCT_UNITS,
                                                   project.genesisSettings.getUnitSystemToUse());

        if (project.genesisSettings.isGenerateComments())
        {
            line = line + "  // using: "
                + UnitConverter.voltageUnits[project.genesisSettings.getUnitSystemToUse()];

            if (cell.getInitialPotential().getDistributionType() != NumberGenerator.FIXED_NUM)
            {
                line = line + "  // Note this actually has value: " + cell.getInitialPotential().toShortString()
                    + " and so will be reset for each instance of the cell";
            }
        }

        response.append(line+"\n\n");

        line = "*set_compt_param     ELEAK	"
                        + UnitConverter.getVoltage(project.simulationParameters.getGlobalVLeak(),
                                                   UnitConverter.NEUROCONSTRUCT_UNITS,
                                                   project.genesisSettings.getUnitSystemToUse());

        if (project.genesisSettings.isGenerateComments())
                        line = line + "  // using: "
                        + UnitConverter.voltageUnits[project.genesisSettings.getUnitSystemToUse()];

        response.append(line+"\n\n");


        if (project.genesisSettings.isSymmetricCompartments()) response.append("*compt /library/symcompartment\n\n");
        else response.append("*compt /library/compartment\n\n");

        if (project.genesisSettings.isGenerateComments())
        {
            response.append(
                "// NOTE: readcell usually assumes SI units (meters, seconds, ohms) above and lengths (below)\n");
            response.append(
                "// in micrometers. If we use physiological units above, the lengths below will appear quite high\n\n\n");
        }

        String lastPassiveParams = "";

        logger.logComment("=======    Going to calculate chan mechs....");


        for (int ii = 0; ii < segments.size(); ii++)
        {
            Segment segment = (Segment) segments.elementAt(ii);

            logger.logComment("Looking at segment number " + ii + ": " + segment);

            String segName = segment.getSegmentName();
            segName = SimEnvHelper.getSimulatorFriendlyName(segName);

            String parentName = "none";

            float specAxRes = cell.getSpecAxResForSection(segment.getSection());
            float specCap = cell.getSpecCapForSection(segment.getSection());

            if (specAxRes!= lastSpecAxRes)
            {
                response.append("*set_compt_param RA " + UnitConverter.getSpecificAxialResistance(specAxRes,
                    UnitConverter.NEUROCONSTRUCT_UNITS,
                    project.genesisSettings.getUnitSystemToUse()) + "\n");

                lastSpecAxRes = specAxRes;
            }
            if (specCap!= lastSpecCap)
            {
                response.append("*set_compt_param CM " + UnitConverter.getSpecificCapacitance(specCap,
                    UnitConverter.NEUROCONSTRUCT_UNITS,
                    project.genesisSettings.getUnitSystemToUse()) + "\n");

                lastSpecCap = specCap;
            }




            if (segment.getParentSegment() != null)
            {
                parentName = segment.getParentSegment().getSegmentName();
                parentName = SimEnvHelper.getSimulatorFriendlyName(parentName);
            }

            Vector groups = segment.getGroups();
            Vector<ChannelMechanism> chanMechs = new Vector<ChannelMechanism>();

            String firstPassiveCellProc = null; // first passive cond will be use to set Rm

            for (int j = 0; j < groups.size(); j++)
            {
                ArrayList<ChannelMechanism> thisGroupChanMechs = cell.getChanMechsForGroup( (String) groups.elementAt(j));

                logger.logComment("Looking for chan mechs in group: " + groups.elementAt(j));

                for (int k = 0; k < thisGroupChanMechs.size(); k++)
                {
                    ChannelMechanism nextChanMech = thisGroupChanMechs.get(k);

                    logger.logComment("Looking at: "+ nextChanMech);

                    if (!chanMechs.contains(nextChanMech))
                    {
                        CellMechanism cellMech = project.cellMechanismInfo.getCellMechanism(nextChanMech.getName());

                        if (project.genesisSettings.isGenerateComments())
                            response.append("// Cell mechanism: " + cellMech + ", density: "+nextChanMech.getDensity()+" "
                                            + UnitConverter.conductanceDensityUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSafeSymbol() + "\n");


                        boolean isCMLPassive = false;
                        if (cellMech instanceof ChannelMLCellMechanism)
                        {
                            try
                            {
                                isCMLPassive = ( (ChannelMLCellMechanism) cellMech).isPassiveNonSpecificCond();
                            }
                            catch (CMLMechNotInitException e)
                            {
                                ChannelMLCellMechanism cp = (ChannelMLCellMechanism) cellMech;
                                try
                                {
                                    isCMLPassive = cp.isPassiveNonSpecificCond();
                                    cp.initialise(project, false);
                                }
                                catch (CMLMechNotInitException cmle)
                                {
                                    // nothing more to try...
                                }
                                catch (ChannelMLException ex1)
                                {
                                    GuiUtils.showErrorMessage(logger,
                                                              "Error creating implementation of Cell Mechanism: " +
                                                              cellMech.getInstanceName(),
                                                              ex1,
                                                              null);

                                }

                            }
                        }


                        if ((firstPassiveCellProc==null) &&
                            (cellMech instanceof PassiveMembraneMechanism || isCMLPassive))
                        {
                            logger.logComment("First passive?");
                            try
                            {
                                float revPotential = 0;
                                if (cellMech instanceof PassiveMembraneMechanism)
                                {
                                    revPotential = ((PassiveMembraneMechanism)cellMech).getParameter(PassiveMembraneMechanism.
                                    REV_POTENTIAL);
                                }
                                else if (cellMech instanceof ChannelMLCellMechanism)
                                {
                                    String xpath = ChannelMLConstants.getIonsXPath();
                                    logger.logComment("Checking xpath: " + xpath);

                                    SimpleXMLEntity[] ions = ((ChannelMLCellMechanism)cellMech).getXMLDoc().getXMLEntities(xpath);

                                    if (ions != null)
                                    {
                                        for (int i = 0; i < ions.length; i++)
                                        {
                                            logger.logComment("Got entity: " + ions[i].getXMLString("", false));

                                            if(((SimpleXMLElement)ions[i]).getAttributeValue(ChannelMLConstants.ION_NAME_ATTR)
                                                   .equals(ChannelMLConstants.NON_SPECIFIC_ION_NAME))
                                            {
                                                String erev = ((SimpleXMLElement)ions[i])
                                                    .getAttributeValue(ChannelMLConstants.ION_REVERSAL_POTENTIAL_ATTR);

                                                logger.logComment("Setting erev: "+ erev);

                                                revPotential = Float.parseFloat(erev);
                                            }
                                        }
                                    }
                                }

                                line = "*set_compt_param     ELEAK "
                                    +
                                    UnitConverter.getVoltage(revPotential,
                                                             UnitConverter.NEUROCONSTRUCT_UNITS,
                                                             project.genesisSettings.getUnitSystemToUse());

                                if (project.genesisSettings.isGenerateComments())
                                    line = line + "  // using: "
                                        + UnitConverter.voltageUnits[project.genesisSettings.getUnitSystemToUse()];


                                line = line+ "\n*set_compt_param     RM "
                                    +
                                    (1 / UnitConverter.getConductanceDensity(nextChanMech.getDensity(),
                                    UnitConverter.NEUROCONSTRUCT_UNITS,
                                    project.genesisSettings.getUnitSystemToUse()));

                                if (project.genesisSettings.isGenerateComments())
                                    line = line + "  // using: "
                                        +
                                        UnitConverter.specificMembraneResistanceUnits[project.genesisSettings.getUnitSystemToUse()];

                                if (!lastPassiveParams.equals(line)) response.append(line + "\n");

                                lastPassiveParams = line;

                                firstPassiveCellProc = cellMech.getInstanceName();

                            }
                            catch (CellMechanismException ex)
                            {
                                GuiUtils.showErrorMessage(logger,
                                                          "Problem getting the cell mechanism info from: " + cellMech,
                                                          ex, null);
                            }
                        }
                        else
                        {
                            logger.logComment("Ignoring, firstPassiveCellProc: "+firstPassiveCellProc+", cellMech class: "+cellMech.getClass().getName());
                        }
                    }
                    else
                    {
                        logger.logComment("Already added that chan mech...");
                    }

                    chanMechs.add(nextChanMech);
                }

            }

            StringBuffer channelCondString = new StringBuffer();

            //boolean ignored firs

            for (int ll = 0; ll < chanMechs.size(); ll++)
            {

                ChannelMechanism nextChanMech = (ChannelMechanism) chanMechs.elementAt(ll);
                logger.logComment("--  nextChanMech: " + nextChanMech);
                CellMechanism cellMech = project.cellMechanismInfo.getCellMechanism(nextChanMech.getName());

                logger.logComment("--  checking mech : " + cellMech.getInstanceName());

                if(!cellMech.getInstanceName().equals(firstPassiveCellProc))
                {

                    try
                    {
                        logger.logComment("Adding chan mech: "+cellMech.getInstanceName()
                                          +", dens: "+nextChanMech.getDensity());

                        channelCondString.append(cellMech.getInstanceName()
                                                 + " "
                                                 + UnitConverter.getConductanceDensity(
                                                     nextChanMech.getDensity(),
                                                     UnitConverter.NEUROCONSTRUCT_UNITS,
                                                     project.genesisSettings.getUnitSystemToUse())
                                                 + " ");
                    }
                    catch (Exception ex1)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem including cell mech: "
                                                  + nextChanMech
                                                  + " in morphology file for cell: " + cell, null, null);

                        //return "";

                    }

                }
                else
                {
                    logger.logComment("Not adding chan mech: "+cellMech.getInstanceName()+" to the list. channelCondString: "+channelCondString);
                }

                logger.logComment("channelCondString: "+channelCondString);
            }

            // Note: the use of a GenesisCompartmentalisation will make much of this redundant.
            // All generated sections will be single segment and cylindrical.

            float r1 = segment.getSegmentStartRadius();
            float r2 = segment.getRadius();
            float h = segment.getSegmentLength();

            float equivalentRadius = (float)CompartmentHelper.getEquivalentRadius(r1, r2, h);


            if (segment.getSegmentShape() == Segment.CYLINDRICAL_SHAPE)
            {
                boolean specifyStartPoint = (segment.getParentSegment() == null);

                if (segment.getParentSegment()!=null)
                {
                    if (!segment.getParentSegment().getEndPointPosition().equals(segment.getStartPointPosition()))
                    {
                        //System.out.println(segment.getParentSegment().getEndPointPosition() + " not equal to: "+
                         //   segment.getStartPointPosition());

                        specifyStartPoint = true;
                    }
                }

                if (segment.isFirstSectionSegment() &&
                    specifyStartPoint)
                {
                    // i.e. root segment
                    response.append("*double_endpoint\n");
                    response.append(segName
                                    + " "
                                    + parentName
                                    + " "
                                    + convertToMorphDataLength(segment.getStartPointPosition().x)
                                    + " "
                                    + convertToMorphDataLength(segment.getStartPointPosition().y)
                                    + " "
                                    + convertToMorphDataLength(segment.getStartPointPosition().z)
                                    + " "
                                    + convertToMorphDataLength(segment.getEndPointPositionX())
                                    + " "
                                    + convertToMorphDataLength(segment.getEndPointPositionY())
                                    + " "
                                    + convertToMorphDataLength(segment.getEndPointPositionZ())
                                    + " "
                                    + convertToMorphDataLength(equivalentRadius * 2d)

                                    + " " + channelCondString + "\n");
                    response.append("*double_endpoint_off\n");

                }
                else
                {
                    // ordinary cylindrical segment..

                    response.append(segName
                                    + " "
                                    + parentName
                                    + " "
                                    + convertToMorphDataLength(segment.getEndPointPositionX())
                                    + " "
                                    + convertToMorphDataLength(segment.getEndPointPositionY())
                                    + " "
                                    + convertToMorphDataLength(segment.getEndPointPositionZ())
                                    + " "
                                    + convertToMorphDataLength(equivalentRadius * 2d)

                                    + " " + channelCondString + "\n");
                }
            }
            else
            {

                response.append("*spherical \n\n");

                response.append(segName
                                + " "
                                + parentName
                                + " "
                                + convertToMorphDataLength(segment.getEndPointPositionX()) // = startPoint
                                + " "
                                + convertToMorphDataLength(segment.getEndPointPositionY()) // = startPoint
                                + " "
                                + convertToMorphDataLength(segment.getEndPointPositionZ()) // = startPoint
                                + " "
                                + convertToMorphDataLength( (segment.getRadius() * 2d))
                                + " "
                                + channelCondString + "\n");

                if (segments.size() > 1)
                    response.append("\n*cylindrical \n");

            }

            response.append("\n");

        }
        return response.toString();
    }



    /**
     * Converts to the proper length for the morphological data
     * If it's SI units (which GENESIS normally assumes):
     * 2.5E-6 meters will actually be written 2.5
     * Therefore the conversion factor is 1000000
     * @param length The length in "normal" units
     * @return The length which will be used in the main morphology lines
     */
    private double convertToMorphDataLength(double length)
    {
        logger.logComment("Converting double: "+ length);
        double conversionFactor = 1000000;
        return conversionFactor * UnitConverter.getLength(length,
                                       UnitConverter.NEUROCONSTRUCT_UNITS,
                                       project.genesisSettings.getUnitSystemToUse());
    }



    public static void main(String[] args)
    {
        try
                {
                    Project testProj = Project.loadProject(new File("models/BioMorph/BioMorph.neuro.xml"),
                                                           new ProjectEventListener()
                    {
                        public void tableDataModelUpdated(String tableModelName)
                        {};

                        public void tabUpdated(String tabName)
                        {};
                        public void cellMechanismUpdated()
                        {
                        };

                    });

                    //SimpleCell cell = new SimpleCell("DummyCell");
                    //ComplexCell cell = new ComplexCell("DummyCell");

                    Cell cell = testProj.cellManager.getCell("LongCellDelayLine");

                    //File f = new File("/home/padraig/temp/tempNC/NEURON/PatTest/basics/");
                    File f = new File("../temp");

                    GenesisMorphologyGenerator cellTemplateGenerator1 = new GenesisMorphologyGenerator(cell,testProj,
                        f);

                    System.out.println("Generated: " + cellTemplateGenerator1.generateFile());

                    System.out.println(CellTopologyHelper.printDetails(cell, null));
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
        }
    }

}
