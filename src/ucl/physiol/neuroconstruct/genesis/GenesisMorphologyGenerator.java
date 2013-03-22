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

package ucl.physiol.neuroconstruct.genesis;

import java.io.*;
import java.util.*;

import java.util.ArrayList;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.compartment.*;
import ucl.physiol.neuroconstruct.utils.equation.EquationException;
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
    static ClassLogger logger = new ClassLogger("GenesisMorphologyGenerator");

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

        StringBuilder spaceLessName = new StringBuilder();

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

    public File getFile()
    {
        return this.morphFile;
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
            
            fw.write(checkExtraParams());

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

    private String checkExtraParams()
    {
        logger.logComment("calling checkExtraParams");
        StringBuilder response = new StringBuilder();
        if (CellTopologyHelper.hasExtraCellMechParams(cell))
        {
            GenesisFileManager.addMajorComment(response, "          ****  NOTE  ****\n" +
                    "Some of the channel mechanisms in this cell have some of their internal\n" +
                    "parameters changed after initialisation. This is not possible to specify\n" +
                    "in a *.p file, and so this file should not be used on its own for this cell,\n" +
                    "but in conjunction with the parameter changes in the main file after the readcell command.");
        }
  
        return response.toString();
    }      

    
    private String getCommonHeader()
    {

        StringBuilder response = new StringBuilder();
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
        StringBuilder response = new StringBuilder();

        //Vector segments = cell.getAllSegments();
        Vector<Segment> segments = cell.getExplicitlyModelledSegments();
        
        Hashtable<String, ArrayList<ChannelMechanism>> chanMechsVsGrps = new Hashtable<String, ArrayList<ChannelMechanism>>();
        ArrayList<String> passiveChanMechs = new ArrayList<String>();
        ArrayList<String> notPassiveChanMechs = new ArrayList<String>();

        Hashtable<Vector<String>, String[]> groupListVsChanStrings = new Hashtable<Vector<String>, String[]>();


        logger.logComment("Investigating " + segments.size() + " segments...");
        String prePassiveLine = null;
        float lastSpecAxRes = -1;
        float lastSpecCap = -1;

        float globalRA = cell.getSpecAxResForGroup(Section.ALL);
        if (!Float.isNaN(globalRA))
        {
            prePassiveLine = "*set_global	         RA "
                + (float)UnitConverter.getSpecificAxialResistance(globalRA,
                                                           UnitConverter.NEUROCONSTRUCT_UNITS,
                                                           project.genesisSettings.getUnitSystemToUse());
            lastSpecAxRes = globalRA;
            if (project.genesisSettings.isGenerateComments())
            {
                prePassiveLine = prePassiveLine + "  // using: "
                    + UnitConverter.specificAxialResistanceUnits[project.genesisSettings.getUnitSystemToUse()];

            }
            response.append(prePassiveLine+"\n\n");
        }

        float globalCM = cell.getSpecCapForGroup(Section.ALL);

        if (!Float.isNaN(globalCM))
        {
            prePassiveLine = "*set_global	         CM "
                + (float)UnitConverter.getSpecificCapacitance(globalCM,
                                                       UnitConverter.NEUROCONSTRUCT_UNITS,
                                                       project.genesisSettings.getUnitSystemToUse());
            lastSpecCap = globalCM;
            if (project.genesisSettings.isGenerateComments())
                prePassiveLine = prePassiveLine + "   // using: "
                    + UnitConverter.specificCapacitanceUnits[project.genesisSettings.getUnitSystemToUse()];

            response.append(prePassiveLine+"\n\n");
        }

        prePassiveLine = "*set_global	         RM	"
                        + (float)UnitConverter.getSpecificMembraneResistance(project.simulationParameters.getGlobalRm(),
                                                                      UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                      project.genesisSettings.getUnitSystemToUse());

        if (project.genesisSettings.isGenerateComments())
                        prePassiveLine = prePassiveLine + "  // using: "
                        + UnitConverter.specificMembraneResistanceUnits[project.genesisSettings.getUnitSystemToUse()];

        response.append(prePassiveLine+"\n\n");


        prePassiveLine = "*set_global	         EREST_ACT	"
                        + (float)UnitConverter.getVoltage(cell.getInitialPotential().getNominalNumber(),
                                                   UnitConverter.NEUROCONSTRUCT_UNITS,
                                                   project.genesisSettings.getUnitSystemToUse());

        if (project.genesisSettings.isGenerateComments())
        {
            prePassiveLine = prePassiveLine + "  // using: "
                + UnitConverter.voltageUnits[project.genesisSettings.getUnitSystemToUse()];

            if (cell.getInitialPotential().getDistributionType() != NumberGenerator.FIXED_NUM)
            {
                prePassiveLine = prePassiveLine + "  // Note this actually has value: " + cell.getInitialPotential().toShortString()
                    + " and so will be reset for each instance of the cell";
            }
        }

        response.append(prePassiveLine+"\n\n");

        prePassiveLine = "*set_compt_param     ELEAK	"
                        + (float)UnitConverter.getVoltage(project.simulationParameters.getGlobalVLeak(),
                                                   UnitConverter.NEUROCONSTRUCT_UNITS,
                                                   project.genesisSettings.getUnitSystemToUse());

        if (project.genesisSettings.isGenerateComments())
                        prePassiveLine = prePassiveLine + "  // using: "
                        + UnitConverter.voltageUnits[project.genesisSettings.getUnitSystemToUse()];

        response.append(prePassiveLine+"\n\n");


        if (!mooseCompatMode())
        {
            if (project.genesisSettings.isSymmetricCompartments())
                response.append("*compt /library/symcompartment\n\n");
            else
                response.append("*compt /library/compartment\n\n");
        }

        if (project.genesisSettings.isGenerateComments())
        {
            response.append(
                "// NOTE: readcell usually assumes SI units (meters, seconds, ohms) above and lengths (below)\n");
            response.append(
                "// in micrometers. If we use physiological units above, the lengths below will appear quite high\n\n\n");
        }

        String lastPassParamInfo = "";

        logger.logComment("=======    Going to calculate chan mechs....");

        boolean cellHasVarMechs = (cell.getVarMechsVsParaGroups().size()>0);

        boolean warnedLengthLine = false;

        for (int ii = 0; ii < segments.size(); ii++)
        {
            Segment segment = segments.elementAt(ii);

            logger.logComment("**************************************************\n\nLooking at segment number " + ii + ": " + segment);

            String segName = segment.getSegmentName();
            segName = SimEnvHelper.getSimulatorFriendlyName(segName);

            String parentName = "none";

            float specAxRes = cell.getSpecAxResForSection(segment.getSection());
            float specCap = cell.getSpecCapForSection(segment.getSection());

            if (specAxRes!= lastSpecAxRes)
            {
                response.append("*set_compt_param     RA " + (float)UnitConverter.getSpecificAxialResistance(specAxRes,
                    UnitConverter.NEUROCONSTRUCT_UNITS,
                    project.genesisSettings.getUnitSystemToUse()) + "\n");

                lastSpecAxRes = specAxRes;
            }
            if (specCap!= lastSpecCap)
            {
                if (specCap == 0)
                {
                    response.append("// Spec cap is zero, causes problems, so using v small CM \n");
                    specCap = 1e-18f;
                }
                response.append("*set_compt_param     CM " + (float)UnitConverter.getSpecificCapacitance(specCap,
                    UnitConverter.NEUROCONSTRUCT_UNITS,
                    project.genesisSettings.getUnitSystemToUse()) + "\n");

                lastSpecCap = specCap;
            }




            if (segment.getParentSegment() != null)
            {
                parentName = segment.getParentSegment().getSegmentName();
                parentName = SimEnvHelper.getSimulatorFriendlyName(parentName);
            }

            Vector<String> groups = segment.getGroups();

            groups = (Vector<String>)GeneralUtils.reorderAlphabetically(groups, true);

            String passParamInfo = new String();
            String channelCondInfo = new String();
            String comments = new String();

            if (groupListVsChanStrings.containsKey(groups)&&!cellHasVarMechs&&false)
            {
                String[] chanInfo = groupListVsChanStrings.get(groups);
                passParamInfo = chanInfo[0];
                channelCondInfo = chanInfo[1];
                comments = chanInfo[2];
            }
            else
            {
                Vector<ChannelMechanism> longChanMechs = new Vector<ChannelMechanism>();

                String firstPassiveCellMech = null; // first passive cond will be use to set Rm

                for (int j = 0; j < groups.size(); j++)
                {
                    ArrayList<ChannelMechanism> thisGroupChanMechs = null;

                    if(!chanMechsVsGrps.containsKey(groups.elementAt(j)))
                    {
                        ArrayList<ChannelMechanism> cms = cell.getChanMechsForGroup(groups.elementAt(j));

                        thisGroupChanMechs = (ArrayList<ChannelMechanism>)GeneralUtils.reorderAlphabetically(cms, true);

                        chanMechsVsGrps.put(groups.elementAt(j), thisGroupChanMechs);
                    }
                    else
                    {
                        thisGroupChanMechs = chanMechsVsGrps.get(groups.elementAt(j));
                    }



                    logger.logComment("Looking for chan mechs in group: " + groups.elementAt(j));

                    for (int k = 0; k < thisGroupChanMechs.size(); k++)
                    {
                        ChannelMechanism nextChanMech = thisGroupChanMechs.get(k);

                        logger.logComment("Looking at: "+ nextChanMech);
                        CellMechanism cellMech = project.cellMechanismInfo.getCellMechanism(nextChanMech.getName());

                        if (cellMech.isPointProcess())
                        {
                            GuiUtils.showErrorMessage(logger, "The cell mechanism: "+cellMech+" is a Point Process. These are not yet supported in GENESIS!", null, null);
                            return "";
                        }

                        if (!longChanMechs.contains(nextChanMech))
                        {

                            if (project.genesisSettings.isGenerateComments())
                            {
                                comments = comments +"// Cell mechanism: " +nextChanMech.toString() + "\n";
                            }


                            boolean isCMLPassive = false;

                            if(notPassiveChanMechs.contains(cellMech.getInstanceName()))
                            {
                                isCMLPassive = false;
                            }
                            else if(passiveChanMechs.contains(cellMech.getInstanceName()))
                            {
                                isCMLPassive = true;
                            }
                            else if (cellMech instanceof ChannelMLCellMechanism)
                            {
                                try
                                {
                                    isCMLPassive = ( (ChannelMLCellMechanism) cellMech).isPassiveNonSpecificCond();
                                    if(isCMLPassive && !passiveChanMechs.contains(cellMech.getInstanceName()))
                                    {
                                        passiveChanMechs.add(cellMech.getInstanceName());
                                    }
                                    if(!isCMLPassive && !notPassiveChanMechs.contains(cellMech.getInstanceName()))
                                    {
                                        notPassiveChanMechs.add(cellMech.getInstanceName());
                                    }
                                }
                                catch (CMLMechNotInitException e)
                                {
                                    ChannelMLCellMechanism cp = (ChannelMLCellMechanism) cellMech;
                                    try
                                    {
                                        cp.initialise(project, false);
                                        isCMLPassive = cp.isPassiveNonSpecificCond();

                                        if(isCMLPassive && !passiveChanMechs.contains(cellMech.getInstanceName()))
                                        {
                                            passiveChanMechs.add(cellMech.getInstanceName());
                                        }
                                        if(!isCMLPassive && !notPassiveChanMechs.contains(cellMech.getInstanceName()))
                                        {
                                            notPassiveChanMechs.add(cellMech.getInstanceName());
                                        }
                                    }
                                    catch (CMLMechNotInitException cmle)
                                    {
                                        // nothing more to try...
                                    }
                                    catch (XMLMechanismException ex1)
                                    {
                                        GuiUtils.showErrorMessage(logger,
                                                                  "Error creating implementation of Cell Mechanism: " +
                                                                  cellMech.getInstanceName(),
                                                                  ex1,
                                                                  null);

                                    }

                                }
                            }


                            if ((firstPassiveCellMech==null) &&
                                (cellMech instanceof PassiveMembraneMechanism || isCMLPassive)
                                && nextChanMech.getDensity() >=0 )
                            {
                                logger.logComment("First passive?");
                                try
                                {
                                    float revPotential = 0;
                                    if (cellMech instanceof PassiveMembraneMechanism)
                                    {
                                        revPotential = ((PassiveMembraneMechanism)cellMech).getParameter(PassiveMembraneMechanism.REV_POTENTIAL);
                                    }
                                    else if (cellMech instanceof ChannelMLCellMechanism)
                                    {
                                        String xpath = ChannelMLConstants.getPreV1_7_3IonsXPath();
                                        logger.logComment("Checking xpath: " + xpath);
                                        ChannelMLCellMechanism cmlCm = (ChannelMLCellMechanism)cellMech;
                                        String units = cmlCm.getUnitsUsedInFile();

                                        SimpleXMLEntity[] ions = cmlCm.getXMLDoc().getXMLEntities(xpath);

                                        if (ions != null && ions.length>0)
                                        {
                                            for (int i = 0; i < ions.length; i++)
                                            {
                                                logger.logComment("Got entity: " + ions[i].getXMLString("", false));

                                                if(((SimpleXMLElement)ions[i]).getAttributeValue(ChannelMLConstants.LEGACY_ION_NAME_ATTR)
                                                       .equals(ChannelMLConstants.NON_SPECIFIC_ION_NAME))
                                                {
                                                    String erev = ((SimpleXMLElement)ions[i]).getAttributeValue(ChannelMLConstants.ION_REVERSAL_POTENTIAL_ATTR);

                                                    logger.logComment("Setting erev: "+ erev);

                                                    revPotential = Float.parseFloat(erev);
                                                    if (units.equals(ChannelMLConstants.SI_UNITS))
                                                        revPotential = revPotential*1000;
                                                }
                                            }
                                        }
                                        else   // post v1.7.3
                                        {
                                            xpath = ChannelMLConstants.getCurrVoltRelXPath()+"/@"+ChannelMLConstants.ION_REVERSAL_POTENTIAL_ATTR;
                                            String erev = ((ChannelMLCellMechanism)cellMech).getXMLDoc().getValueByXPath(xpath);

                                            logger.logComment("Setting erev from post v1.7.3 location: "+ erev);

                                            revPotential = Float.parseFloat(erev);
                                                    if (units.equals(ChannelMLConstants.SI_UNITS))
                                                        revPotential = revPotential*1000;
                                        }
                                    }

                                    String preLine = "*set_compt_param     ELEAK "
                                        + (float)UnitConverter.getVoltage(revPotential,
                                                                 UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                 project.genesisSettings.getUnitSystemToUse());

                                    if (project.genesisSettings.isGenerateComments())
                                        preLine = preLine + "  // using: "
                                            + UnitConverter.voltageUnits[project.genesisSettings.getUnitSystemToUse()];


                                    preLine = preLine+ "\n*set_compt_param     RM "
                                        +
                                        (float)(1 / UnitConverter.getConductanceDensity(nextChanMech.getDensity(),
                                        UnitConverter.NEUROCONSTRUCT_UNITS,
                                        project.genesisSettings.getUnitSystemToUse()));

                                    if (project.genesisSettings.isGenerateComments())
                                        preLine = preLine + "  // using: "
                                            + UnitConverter.specificMembraneResistanceUnits[project.genesisSettings.getUnitSystemToUse()];

                                    //if (!lastPassiveParams.equals(preLine))
                                    //{
                                        passParamInfo = preLine + "\n\n\n";

                                        //lastPassiveParams = preLine;
                                    //}

                                    firstPassiveCellMech = cellMech.getInstanceName();

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
                                logger.logComment("Ignoring, firstPassiveCellMech: "+firstPassiveCellMech+", cellMech class: "+cellMech.getClass().getName());
                            }
                        }
                        else
                        {
                            logger.logComment("Already added that chan mech...");
                        }

                        longChanMechs.add(nextChanMech);
                    }

                }

                if (firstPassiveCellMech==null)
                {
                    passParamInfo = passParamInfo +"\n// No passive conductance found!!\n*set_compt_param     RM "
                                        + (float)UnitConverter.getResistance(1e12,
                                        UnitConverter.NEUROCONSTRUCT_UNITS,
                                        project.genesisSettings.getUnitSystemToUse())+"\n\n\n";
                }

                StringBuilder channelCondString = new StringBuilder();

                // Consolidating chan mechs for this segment, ie. joining...
                //  nextChanMech: naf2 (density: -1.0 mS um^-2, fastNa_shift = -2.5)
                //  nextChanMech: naf2 (density: 6.0E-7 mS um^-2)

                Vector<ChannelMechanism> consolChanMechs = new Vector<ChannelMechanism>();

            //logger.logComment("Consolidating...", true);

                for (int ll = 0; ll < longChanMechs.size(); ll++)
                {
                    ChannelMechanism nextChanMech = longChanMechs.elementAt(ll);
                    boolean dealtWith = false;

                    for (int pre = 0; pre < consolChanMechs.size(); pre++)
                    {
                        ChannelMechanism preChanMech = consolChanMechs.elementAt(pre);

                        if (preChanMech.getName().equals(nextChanMech.getName()))
                        {
                            if (preChanMech.getDensity()<0 && nextChanMech.getDensity()>=0)
                                preChanMech.setDensity(nextChanMech.getDensity());

                            for (MechParameter mp: nextChanMech.getExtraParameters())
                            {
                                preChanMech.getExtraParameters().add((MechParameter)mp.clone());
                            }

                            dealtWith = true;
                        }
                    }
                    if (!dealtWith)
                        consolChanMechs.add((ChannelMechanism)nextChanMech.clone());
                }
                logger.logComment("Done Consolidating: ..."+ consolChanMechs);

                for (int ll = 0; ll < consolChanMechs.size(); ll++)
                {
                    ChannelMechanism nextChanMech = consolChanMechs.elementAt(ll);
                    logger.logComment("--  nextChanMech: " + nextChanMech);
                    CellMechanism cellMech = project.cellMechanismInfo.getCellMechanism(nextChanMech.getName());

                    logger.logComment("--  checking mech : " + cellMech.getInstanceName());

                    if(!cellMech.getInstanceName().equals(firstPassiveCellMech))
                    {
                        try
                        {
                            float genDens =  (float)UnitConverter.getConductanceDensity(
                                    nextChanMech.getDensity(),
                                    UnitConverter.NEUROCONSTRUCT_UNITS,
                                    project.genesisSettings.getUnitSystemToUse());

                            logger.logComment("Adding chan mech: "+cellMech.getInstanceName()
                                              +", dens: "+nextChanMech.getDensity());

                            if (cellMech.getMechanismType().equals(CellMechanism.ION_CONCENTRATION) &&
                                    cellMech instanceof ChannelMLCellMechanism)
                            {
                                ChannelMLCellMechanism cmlMech = (ChannelMLCellMechanism)cellMech;

                                SimpleCompartment comp = new SimpleCompartment(segment);

                                double area = comp.getCurvedSurfaceArea();

                                if (segment.isSpherical())  area = 4 * Math.PI * segment.getRadius()*segment.getRadius();


                                area =  area * ((float)(UnitConverter.getArea(1, UnitConverter.NEURON_UNITS,
                                        project.genesisSettings.getUnitSystemToUse())));

                                if (cmlMech.getValue(ChannelMLConstants.getIonConcFixedPoolXPath())!=null)
                                {

                                    double phi = Float.parseFloat( cmlMech.getValue(ChannelMLConstants.getIonConcFixedPoolPhiXPath().trim()));

                                    if (nextChanMech.getExtraParameter(ChannelMLConstants.ION_CONC_FIXED_POOL_PHI_ELEMENT)!=null)
                                    {
                                        phi = nextChanMech.getExtraParameter(ChannelMLConstants.ION_CONC_FIXED_POOL_PHI_ELEMENT).getValue();
                                    }

                                    phi = phi / UnitConverter.getCurrentDensity(1,
                                                                                UnitConverter.NEURON_UNITS,
                                                                                project.genesisSettings.getUnitSystemToUse());

                                    float B = (float)( phi / area);

                                    /** @todo Fix this for correct units of ConcFixedPool!!! ... */
                                    float factor = 1;
                                    if (project.genesisSettings.isPhysiologicalUnits()) factor = 1e-6f;
                                    if (project.genesisSettings.isSIUnits()) factor = 1e3f;

                                    B = -1 * B *factor; //-1 so readcell will use absolute value (see http://www.genesis-sim.org/GENESIS/Hyperdoc/Manual-25.html#ss25.131)

                                    logger.logComment("phi: "+ phi+", comp: "+comp.toString()+", area: "+ area+", B: "+B+", seg: "+segment.getRadius());

                                    channelCondString.append(nextChanMech.getUniqueName()+ " "+ B+ " ");
                                }
                                else if (genDens == 0 && 
                                    (cmlMech.getValue(ChannelMLConstants.getIonConcShellThicknessXPath())!=null ||
                                     cmlMech.getValue(ChannelMLConstants.getIonConcShellThicknessXPath_pre1_7_3())!=null))
                                {

                                    double thicknessOrig = Float.NaN;
                                    
                                    if (cmlMech.getValue(ChannelMLConstants.getIonConcShellThicknessXPath())!=null)
                                        thicknessOrig = Float.parseFloat( cmlMech.getValue(ChannelMLConstants.getIonConcShellThicknessXPath().trim()));
                                    else if (cmlMech.getValue(ChannelMLConstants.getIonConcShellThicknessXPath_pre1_7_3())!=null)
                                        thicknessOrig = Float.parseFloat( cmlMech.getValue(ChannelMLConstants.getIonConcShellThicknessXPath_pre1_7_3().trim()));

                                    double thicknessGen = UnitConverter.getLength(thicknessOrig, UnitConverter.getUnitSystemIndex(cmlMech.getUnitsUsedInFile()),
                                                                                project.genesisSettings.getUnitSystemToUse());

                                    double thicknessnC = UnitConverter.getLength(thicknessOrig, UnitConverter.getUnitSystemIndex(cmlMech.getUnitsUsedInFile()),
                                                                                UnitConverter.NEUROCONSTRUCT_UNITS);

                                    SimpleCompartment inside = new SimpleCompartment(comp.getStartRadius()-thicknessnC, comp.getEndRadius()-thicknessnC, comp.getHeight());

                                    double volComp = comp.getVolume();
                                    double insideVol = inside.getVolume();

                                    if (segment.isSpherical())
                                    {
                                        double r = segment.getRadius();
                                        //System.out.println("r: "+r);
                                        double ri = segment.getRadius()-thicknessnC;

                                        volComp = (4d/3) * Math.PI * r*r*r;
                                        insideVol = (4d/3) * Math.PI * ri*ri*ri;
                                    }
                                    
                                    double totVol = volComp - insideVol;
                                    totVol = UnitConverter.getVolume(totVol, UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                                project.genesisSettings.getUnitSystemToUse());

                                    int valence = 2; /*TODO: check in ChannelML!!!*/
                                    float faraday = 96485.3415f;

                                    float B = 1 / ((float)totVol * (valence * faraday));

                                    if (project.genesisSettings.isPhysiologicalUnits())
                                    {
                                        B = B * 1e-9f; /*TODO: work out from units of faraday!!!*/
                                    }

                                    /*
                                    System.out.println("thicknessGen: "+ thicknessGen);
                                    System.out.println("thicknessnC: "+ thicknessnC);
                                    System.out.println("totVol: "+ totVol);
                                    System.out.println("B: "+ B);*/

                                    channelCondString.append(nextChanMech.getUniqueName()+ " -"+ B + " ");

                                }
                                else
                                {
                                    // In case line is too long...
                                    String d = genDens+ "";
                                    if (genDens==(int)genDens)
                                        d = (int)genDens+ "";
                                    if (d.startsWith("0."))
                                        d = d.substring(1);

                                    channelCondString.append(nextChanMech.getUniqueName()+ " "+ d+ " ");
                                }
                            }
                            else
                            {
                                if (genDens>=0)
                                {
                                    // In case line is too long...
                                    String d = genDens+ "";
                                    if (genDens==(int)genDens)
                                        d = (int)genDens+ "";
                                    if (d.startsWith("0."))
                                        d = d.substring(1);

                                    channelCondString.append(nextChanMech.getUniqueName()+ " "+ d+ " ");
                                }
                                //if (genDens==0)
                                //{
                                //    logger.logComment("Ignoring density of "+ genDens+ " on "+cellMech.getInstanceName()+ " as it is zero...");
                               //    // channelCondString.append(nextChanMech.getUniqueName()+ " "+ genDens+ "  ");
                               // }
                                else
                                {
                                    logger.logComment("Ignoring density of "+ genDens+ " on "+cellMech.getInstanceName()+ " as it is negative...");
                                }
                            }
                        }
                        catch (Exception ex1)
                        {
                            GuiUtils.showErrorMessage(logger, "Problem including cell mech: "
                                                      + nextChanMech
                                                      + " in morphology file for cell: " + cell, ex1, null);

                            //return "";

                        }

                    }
                    else
                    {
                        logger.logComment("Not adding chan mech: "+cellMech.getInstanceName()+" to the list. channelCondString: "+channelCondString);
                    }

                    logger.logComment("channelCondString: "+channelCondString);
                }

                logger.logComment("Done Consolidating 2...");

                ArrayList<VariableMechanism> varMechs = cell.getVarChanMechsForSegment(segment);
                for(VariableMechanism vm: varMechs)
                {
                    ParameterisedGroup pg = cell.getVarMechsVsParaGroups().get(vm);
                    try
                    {
                        double pgVal = pg.evaluateAt(cell, segment, 0.5f);

                        float dens = (float)vm.evaluateAt(pgVal);

                        float genDens =  (float)UnitConverter.getConductanceDensity(
                                dens,
                                UnitConverter.NEUROCONSTRUCT_UNITS,
                                project.genesisSettings.getUnitSystemToUse());

                        String name = vm.getName();

                        for(ChannelMechanism con: consolChanMechs)
                        {
                            if (con.getName().equals(name))
                            {
                                logger.logComment("Instead of "+name+", using unique name of: "+ con);
                                name = con.getUniqueName();
                            }
                        }
                        comments = comments +"// Variable cell mechanism "+vm.getName()+" evaluated as: " +dens + " on "+name+" \n";

                        channelCondString.append(name+ " "+ genDens+" ");


                    }
                    catch (ParameterException ex)
                    {
                        String error = "Unable to evaluate values for: "+ pg+" on cell: "+cell;
                        GuiUtils.showErrorMessage(logger, error, ex, null);
                        return error;
                    }
                    catch (EquationException ex)
                    {
                        String error = "Unable to evaluate values for: "+ vm+" on cell: "+cell;
                        GuiUtils.showErrorMessage(logger, error, ex, null);
                        return error;
                    }
                }

                channelCondInfo = channelCondString.toString();

                

                String[] chanInfo = new String[3];
                chanInfo[0] = passParamInfo;
                chanInfo[1] = channelCondInfo;
                chanInfo[2] = comments;

                groupListVsChanStrings.put(groups, chanInfo);
                //System.out.println("groupListVsChanString size: "+ groupListVsChanString.keySet().size());

            }

            response.append(comments);
            if (!lastPassParamInfo.equals(passParamInfo))
            {
                response.append(passParamInfo);
                lastPassParamInfo = passParamInfo;
            }
                

            // Note: the use of a GenesisCompartmentalisation will make much of this redundant.
            // All generated sections will be single segment and cylindrical.

            float r1 = segment.getSegmentStartRadius();
            float r2 = segment.getRadius();
            float h = segment.getSegmentLength();

            float equivalentRadius = (float)CompartmentHelper.getEquivalentRadius(r1, r2, h);

            String line = null;

            if (segment.getSegmentShape() == Segment.CYLINDRICAL_SHAPE)
            {
                boolean specifyStartPoint = (segment.getParentSegment() == null);

                if (segment.getParentSegment()!=null)
                {
                    if (!segment.getParentSegment().getEndPointPosition().equals(segment.getStartPointPosition()))
                    {
                        specifyStartPoint = true;
                    }
                }


                if (segment.isFirstSectionSegment() &&
                    specifyStartPoint)
                {
                    // i.e. root segment
                    response.append("*double_endpoint\n");
                    line = segName + " "
                                    + parentName + " "
                                    + convertToMorphDataLength(segment.getStartPointPosition().x) + " "
                                    + convertToMorphDataLength(segment.getStartPointPosition().y) + " "
                                    + convertToMorphDataLength(segment.getStartPointPosition().z) + " "
                                    + convertToMorphDataLength(segment.getEndPointPositionX()) + " "
                                    + convertToMorphDataLength(segment.getEndPointPositionY()) + " "
                                    + convertToMorphDataLength(segment.getEndPointPositionZ()) + " "
                                    + convertToMorphDataLength(equivalentRadius * 2d) + " "
                                    + channelCondInfo;

                    response.append(line + "\n");
                    
                    response.append("*double_endpoint_off\n");

                }
                else
                {
                    // ordinary cylindrical segment..

                    line = segName + " "
                                    + parentName + " "
                                    + convertToMorphDataLength(segment.getEndPointPositionX()) + " "
                                    + convertToMorphDataLength(segment.getEndPointPositionY()) + " "
                                    + convertToMorphDataLength(segment.getEndPointPositionZ()) + " "
                                    + convertToMorphDataLength(equivalentRadius * 2d) + " "
                                    + channelCondInfo;

                    response.append(line + "\n");

                }
            }
            else
            {

                response.append("*spherical \n\n");

                line = segName + " "
                                + parentName + " "
                                + convertToMorphDataLength(segment.getEndPointPositionX()) // = startPoint
                                + " " + convertToMorphDataLength(segment.getEndPointPositionY()) // = startPoint
                                + " " + convertToMorphDataLength(segment.getEndPointPositionZ()) // = startPoint
                                + " " + convertToMorphDataLength( (segment.getRadius() * 2d))
                                + " " + channelCondInfo;

                response.append(line + "\n");

                if (segments.size() > 1)
                    response.append("\n*cylindrical \n");

            }


            if (!mooseCompatMode() && !warnedLengthLine && line.length()>=300)
            {
                GuiUtils.showWarningMessage(logger, "Warning: the following line (and perhaps other lines) in morphology file for "
                    +cell.getInstanceName()+":\n"+line+"\n is of length "+line.length()
                    +", but there appears to be a limit of 300 chars for lines in read cell files.\n" +
                    "Check for any errors in GENESIS output!!", null);
                warnedLengthLine = true;
            }

            response.append("\n");

        }

/* For testing...
        Enumeration<String> grps = chanMechsVsGrps.keys();
        while(grps.hasMoreElements())
        {
            String g = grps.nextElement();

            ArrayList<ChannelMechanism> cm = chanMechsVsGrps.get(g);

            for(ChannelMechanism c: cm)
            {
                System.out.println("Chan mech in "+g+": "+c);
            }
        }

        //Hashtable<Vector<String>, String[]> groupListVsChanStrings = new Hashtable<Vector<String>, String[]>();

        Enumeration<Vector<String>> grpls = groupListVsChanStrings.keys();

        while(grpls.hasMoreElements())
        {
            Vector<String> g = grpls.nextElement();

            String[] cs = groupListVsChanStrings.get(g);

            System.out.println("\n\n------------------------------------------------");
            for(int i=0;i< cs.length;i++)
            {
                if(i!=2)
                {
                    System.out.println(i+": chan string for "+g+": \n"+cs[i]);
                    System.out.println("\n------------");
                }
            }

        }
            System.out.println("\n\n------------------------------------------------");

        for(String c: passiveChanMechs)
        {
            System.out.println("passiveChanMechs: "+c);
        }
        for(String c: notPassiveChanMechs)
        {
            System.out.println("notPassiveChanMechs: "+c);
        }
 */

        return response.toString();
    }


    public boolean mooseCompatMode()
    {
        return project.genesisSettings.isMooseCompatMode();
    }

    /**
     * Converts to the proper length for the morphological data
     * If it's SI units (which GENESIS normally assumes):
     * 2.5E-6 meters will actually be written 2.5
     * Therefore the conversion factor is 1000000
     * @param length The length in "normal" units
     * @return The length which will be used in the main morphology lines
     */
    private String convertToMorphDataLength(double length)
    {
        logger.logComment("Converting double: "+ length);
        double conversionFactor = 1000000;
        float len = (float)(conversionFactor * UnitConverter.getLength(length,
                                       UnitConverter.NEUROCONSTRUCT_UNITS,
                                       project.genesisSettings.getUnitSystemToUse()));

        if (len==(int)(len)) return (int)len+"";
        return len+"";
    }



    public static void main(String[] args)
    {
        try
        {

            GeneralUtils.timeCheck("Before proj load", true);

            //File projFile = new File("models/BioMorph/BioMorph.neuro.xml");
            File projFile = new File("nCmodels/Thalamocortical/Thalamocortical.ncx");
            //File projFile = new File("testProjects/TestDetailedMorphs/TestDetailedMorphs.neuro.xml");

            Project testProj = Project.loadProject(projFile, new ProjectEventListener()
            {
                public void tableDataModelUpdated(String tableModelName)
                {};
                public void tabUpdated(String tabName)
                {};
                public void cellMechanismUpdated()
                {};

            });

            //logger.setThisClassVerbose(true);

            System.out.println("Loaded project: "+testProj.getProjectFullFileName());

            //SimpleCell cell = new SimpleCell("DummyCell");
            //ComplexCell cell = new ComplexCell("DummyCell");

            //Cell cell = testProj.cellManager.getCell("LongCellDelayLine");
            //Cell cell_1 = testProj.cellManager.getCell("L23PyrRS_cut");
            Cell cell_1 = testProj.cellManager.getCell("L23PyrFRB");
            //Cell cell_1 = testProj.cellManager.getCell("SampleCell");

            GenesisCompartmentalisation gc = new GenesisCompartmentalisation();

            
            Cell cell_2 = gc.generateComp(cell_1);


            Cell cellToUse = cell_2;

            //File f = new File("/home/padraig/temp/tempNC/NEURON/PatTest/basics/");
            File f = new File("../temp");

            System.out.println("Cell has # comps/segments: "+ cellToUse.getAllSegments().size());

            GenesisMorphologyGenerator cellTemplateGenerator1 = new GenesisMorphologyGenerator(cellToUse,testProj, f);


            GeneralUtils.timeCheck("Before gen morph", true);

            System.out.println("Generated: " + cellTemplateGenerator1.generateFile());

            GeneralUtils.timeCheck("After gen morph", true);

            //System.out.println(CellTopologyHelper.printDetails(cell, null));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
