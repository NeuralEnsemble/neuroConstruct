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

package ucl.physiol.neuroconstruct.neuroml;

import java.io.*;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Vector;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.BiophysicalProperties;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.Connection;
import org.neuroml.model.Include;
import org.neuroml.model.InitMembPotential;
import org.neuroml.model.Input;
import org.neuroml.model.InputList;
import org.neuroml.model.Instance;
import org.neuroml.model.IntracellularProperties;
import org.neuroml.model.Location;
import org.neuroml.model.Member;
import org.neuroml.model.MembraneProperties;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Point3DWithDiam;
import org.neuroml.model.Population;
import org.neuroml.model.Projection;
import org.neuroml.model.PulseGenerator;
import org.neuroml.model.Resistivity;
import org.neuroml.model.SegmentGroup;
import org.neuroml.model.SegmentParent;
import org.neuroml.model.SpecificCapacitance;
import org.neuroml.model.SpikeThresh;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml1.model.bio.InitialMembPotential;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.ChannelMechanism;
import ucl.physiol.neuroconstruct.cell.Section;
import ucl.physiol.neuroconstruct.cell.Segment;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.stimulation.IClamp;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.UnitConverter;


/**
 * NeuroML 2 file Reader. Importer of NeuroML 2 files to neuroConstruct
 *
 * @author Padraig Gleeson
 *  
 * 
 */

public class NeuroML2Reader implements NetworkMLnCInfo
{
    private static ClassLogger logger = new ClassLogger("NeuroML2Reader");

    private long foundRandomSeed = Long.MIN_VALUE;

    private String foundSimConfig = null;

    private GeneratedCellPositions cellPos = null;

    private GeneratedNetworkConnections netConns = null;
    
    private GeneratedElecInputs elecInputs = null;    
    
    private Project project = null;
    
    public boolean testMode = false;

    public NeuroML2Reader(Project project)
    {
        this.cellPos = project.generatedCellPositions;
        this.netConns = project.generatedNetworkConnections;
        this.elecInputs = project.generatedElecInputs;
        this.project = project;
        this.testMode = false;
		

    }
    
    public void setTestMode(boolean test)
    {
        this.testMode = test;
    }


    public String getSimConfig()
    {
        return this.foundSimConfig;
    }

    public long getRandomSeed()
    {
        return this.foundRandomSeed;
    }
    
    public void parse(File nml2File) throws NeuroMLException
    {
        parse(nml2File, "");
    }
    
    public void parse(File nml2File, String idPrefix) throws NeuroMLException
    {
        try 
        {
            NeuroMLConverter neuromlConverter=new NeuroMLConverter();

            NeuroMLDocument neuroml = neuromlConverter.urlToNeuroML(nml2File.toURI().toURL());

            logger.logComment("Reading in NeuroML 2: "+ neuroml.getId(), true);
            
            
            /// Cells
            String infoOnnCCellSupport = "Currently, neuroConstruct import of NeuroML2 files is geared towards files it has generated itself.\n"
                    + "Restrictions on files it can import include:\n"
                    + " - &lt;segmentGroup&gt; should have only &lt;member&gt; elements (interpreted as unbranched Sections)\n"
                    + "   OR only &lt;include&gt; elements (interpreted as Section groups)\n"
                    + " - Recognised neuroLexIds: \n"
                    + "      GO:0043025 == soma_group\n"
                    + "      GO:0030425 == dendrite_group\n"
                    + "      GO:0030424 == axon_group\n";
            
            for (org.neuroml.model.Cell nml2Cell: neuroml.getCell()) {
                
                String newCellId = idPrefix+nml2Cell.getId();
                
                if (project.cellManager.getAllCellTypeNames().contains(newCellId)) 
                {
                    throw new NeuroMLException("The project "+project.getProjectName() +" already contains a cell with ID "+ newCellId);
                }
                Cell nCcell = new Cell();
                nCcell.setInstanceName(newCellId);
                if (nml2Cell.getNotes()!=null)
                    nCcell.setCellDescription(nml2Cell.getNotes());
                
                HashMap<Integer, Segment> segIdVsSegments = new HashMap<Integer, Segment>();
                HashMap<String, Section> secNameVsSections = new HashMap<String, Section>();
                
                for (org.neuroml.model.Segment nml2Segment: nml2Cell.getMorphology().getSegment())
                {
                    logger.logComment("Adding Segment: "+ nml2Segment.getId(), true);
                    Point3DWithDiam dist = nml2Segment.getDistal();
                    SegmentParent parent = nml2Segment.getParent();
                    Point3DWithDiam prox = nml2Segment.getProximal();
                    
                    
                    Segment nCsegment = new Segment();
                    nCsegment.setSegmentId(nml2Segment.getId());
                    nCsegment.setSegmentName(nml2Segment.getName());
                    
                    nCsegment.setEndPointPositionX((float)dist.getX());
                    nCsegment.setEndPointPositionY((float)dist.getY());
                    nCsegment.setEndPointPositionZ((float)dist.getZ());
                    nCsegment.setRadius((float)dist.getDiameter()/2);
                    
                    segIdVsSegments.put(nml2Segment.getId(), nCsegment);
                    
                }
                Vector<Segment> allSegments = new Vector<Segment>();
                allSegments.addAll(segIdVsSegments.values());
                nCcell.setAllSegments(allSegments);
                
                for (SegmentGroup segGroup: nml2Cell.getMorphology().getSegmentGroup())
                {
                    String grpName = segGroup.getId();
                    
                    if (segGroup.getMember().size()>0 && segGroup.getInclude().isEmpty()) 
                    {
                        Section sec = new Section(grpName);
                        secNameVsSections.put(grpName, sec);
                        for (Member memb: segGroup.getMember()) {
                            Segment seg = segIdVsSegments.get(memb.getSegment());
                            seg.setSection(sec);
                        }
                    }
                    else if (segGroup.getInclude().size()>0 && segGroup.getMember().isEmpty()) 
                    {
                        for (Include inc: segGroup.getInclude()) {
                            Section sec = secNameVsSections.get(inc.getSegmentGroup());
                            sec.addToGroup(grpName);
                        }
                    }
                    else 
                    {
                        GuiUtils.showErrorMessage(logger, infoOnnCCellSupport, null, null);
                    }
                }
                
                // To set section start points
                for (org.neuroml.model.Segment nml2Segment: nml2Cell.getMorphology().getSegment())
                {
                    logger.logComment("Checking Segment: "+ nml2Segment.getId(), true);
                    SegmentParent parent = nml2Segment.getParent();
                    Point3DWithDiam prox = nml2Segment.getProximal();
                            
                    Segment seg = segIdVsSegments.get(nml2Segment.getId());
                    
                    if (prox!=null) 
                    {
                        Section section = seg.getSection();
                        section.setStartPointPositionX((float)prox.getX());
                        section.setStartPointPositionY((float)prox.getY());
                        section.setStartPointPositionZ((float)prox.getZ());
                        section.setStartRadius((float)prox.getDiameter()/2);
                    }
                    
                    if (parent!=null) 
                    {
                        Segment parentSeg = segIdVsSegments.get(parent.getSegment());
                        seg.setParentSegment(parentSeg);
                    }
                    
                }
                if (nml2Cell.getBiophysicalProperties()!=null) {
                    
                    BiophysicalProperties bp = nml2Cell.getBiophysicalProperties();
                    MembraneProperties mp = bp.getMembraneProperties();
                    
                    for (SpecificCapacitance specCap: mp.getSpecificCapacitance()) {
                        
                        String group = (specCap.getSegmentGroup()==null || specCap.getSegmentGroup().length()==0) ? Section.ALL : specCap.getSegmentGroup();
                        float valInSI = Utils.getMagnitudeInSI(specCap.getValue());
                        float valInnC = (float)UnitConverter.getSpecificCapacitance(valInSI, UnitConverter.GENESIS_SI_UNITS, UnitConverter.NEUROCONSTRUCT_UNITS);
                        nCcell.associateGroupWithSpecCap(group, valInnC);
                    }
                    
                    for (InitMembPotential imp: mp.getInitMembPotential()) {
                        
                        String group = (imp.getSegmentGroup()==null || imp.getSegmentGroup().length()==0) ? Section.ALL : imp.getSegmentGroup();
                        if (!group.equals(Section.ALL)) 
                        {
                            throw new NeuroMLException("neuroConstruct can only import cells with the same initial membrane potential across all segments!");
                        }
                        float valInSI = Utils.getMagnitudeInSI(imp.getValue());
                        float valInnC = (float)UnitConverter.getVoltage(valInSI, UnitConverter.GENESIS_SI_UNITS, UnitConverter.NEUROCONSTRUCT_UNITS);
                        nCcell.setInitialPotential(new NumberGenerator(valInnC));
                    }
                    
                    for(ChannelDensity cd: mp.getChannelDensity()) {
                        
                        String group = (cd.getSegmentGroup()==null || cd.getSegmentGroup().length()==0) ? Section.ALL : cd.getSegmentGroup();
                        float valInSI = Utils.getMagnitudeInSI(cd.getCondDensity());
                        float valInnC = (float)UnitConverter.getConductanceDensity(valInSI, UnitConverter.GENESIS_SI_UNITS, UnitConverter.NEUROCONSTRUCT_UNITS);
                        ChannelMechanism cm = new ChannelMechanism(cd.getIonChannel(), valInnC);
                        nCcell.associateGroupWithChanMech(group, cm);
                        
                    }
                    if (!mp.getChannelDensityGHK().isEmpty()) {
                        throw new NeuroMLException("Import of NeuroML2 with channelDensityGHK not yet implemented! Ask and it shall be done...");
                    }
                    if (!mp.getChannelDensityNernst().isEmpty()) {
                        throw new NeuroMLException("Import of NeuroML2 with channelDensityNernst not yet implemented! Ask and it shall be done...");
                    }
                    if (!mp.getChannelDensityNonUniform().isEmpty()) {
                        throw new NeuroMLException("Import of NeuroML2 with channelDensityNonUniform not yet implemented! Ask and it shall be done...");
                    }
                    /*
                    for (SpikeThresh st: mp.getSpikeThresh()) {
                        
                        String group = (st.getSegmentGroup()==null || st.getSegmentGroup().length()==0) ? Section.ALL : st.getSegmentGroup();
                        if (!group.equals(Section.ALL)) 
                        {
                            throw new NeuroMLException("neuroConstruct can only import cells with the same spike threshold across all segments!");
                        }
                        float valInSI = Utils.getMagnitudeInSI(st.getValue());
                        float valInnC = (float)UnitConverter.getVoltage(valInSI, UnitConverter.GENESIS_SI_UNITS, UnitConverter.NEUROCONSTRUCT_UNITS);
                        //nCcell.set(new NumberGenerator(valInnC));
                    }*/
                    
                    IntracellularProperties ip = bp.getIntracellularProperties();
                    
                    for (Resistivity res: ip.getResistivity()) 
                    {
                        String group = (res.getSegmentGroup()==null || res.getSegmentGroup().length()==0) ? Section.ALL : res.getSegmentGroup();
                        float valInSI = Utils.getMagnitudeInSI(res.getValue());
                        float valInnC = (float)UnitConverter.getSpecificAxialResistance(valInSI, UnitConverter.GENESIS_SI_UNITS, UnitConverter.NEUROCONSTRUCT_UNITS);
                        nCcell.associateGroupWithSpecAxRes(group, valInnC);
                    }
                }
                
                logger.logComment("Read in NeuroML 2 cell: "+ CellTopologyHelper.printDetails(nCcell, project), true);
                
            }
            
            HashMap<String, PulseGenerator> pulseGenerators = new HashMap<String, PulseGenerator>();
            for (PulseGenerator pg: neuroml.getPulseGenerator()) 
            {
                pulseGenerators.put(pg.getId(), pg);
            }
            
            /// Networks

            if (neuroml.getNetwork().size()>1)
            {
                GuiUtils.showErrorMessage(logger, "Currently it is only possible to load a NeuroML file containing a single <network> element.\n"
                        + "There are "+neuroml.getNetwork().size()+" networks in the file: "+nml2File.getAbsolutePath(), null, null);
                return;
            }
            else if (neuroml.getNetwork().size()==1) 
            {

                Network network = neuroml.getNetwork().get(0); // Only first network...

                if (network.getType()!=null && network.getType().toString().equals(NetworkMLConstants.NEUROML2_NETWORK_WITH_TEMP_TYPE)) {

                    float tempSI = Utils.getMagnitudeInSI(network.getTemperature());
                    float tempnC = Utils.getMagnitudeInSI(project.simulationParameters.getTemperature()+"degC");

                    if (Math.abs(tempSI-tempnC)>1e-6)
                    {
                        GuiUtils.showWarningMessage(logger, "Note that the imported network file specifies a temperature of "+network.getTemperature()
                                +", but the neuroConstruct project has a temperature setting of "+project.simulationParameters.getTemperature()+" deg C", null);

                    }
                }

                for (Population population: network.getPopulation())
                {
                    if (!project.cellGroupsInfo.getAllCellGroupNames().contains(population.getId())) 
                    {
                        throw new NeuroMLException("neuroConstruct can only import populations from networks when a Cell Group with that name already exists!");
                    }
                    for (Instance instance: population.getInstance()) 
                    {
                        Location loc = instance.getLocation();

                        logger.logComment("Adding instance "+instance.getId()+" at: "+ loc+" in "+population.getId());
                        this.cellPos.addPosition(population.getId(), new PositionRecord(instance.getId().intValue(), loc.getX(), loc.getY(), loc.getZ()));
                    }
                }
                for (Projection projection: network.getProjection())
                {
                    String netConn = projection.getId();
                    String source = projection.getPresynapticPopulation();
                    String target = projection.getPostsynapticPopulation();
                    
                    if (! (project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames().contains(netConn) ||
                           project.volBasedConnsInfo.getAllAAConnNames().contains(netConn)))
                    {
                        throw new NeuroMLException("neuroConstruct can only import network connections from networks when a Network Connection with that name already exists!");
                    }
                    if (project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames().contains(netConn)) {
                        if (!project.morphNetworkConnectionsInfo.getSourceCellGroup(netConn).equals(source) ||
                            !project.morphNetworkConnectionsInfo.getTargetCellGroup(netConn).equals(target)) {
                            throw new NeuroMLException("Mismatch in the source/target of net conn "+netConn+" between neuroConstruct/NeuroML!");
                        }
                    }
                    if (project.volBasedConnsInfo.getAllAAConnNames().contains(netConn)) {
                        if (!project.volBasedConnsInfo.getSourceCellGroup(netConn).equals(source) ||
                            !project.volBasedConnsInfo.getTargetCellGroup(netConn).equals(target)) {
                            throw new NeuroMLException("Mismatch in the source/target of net conn "+netConn+" between neuroConstruct/NeuroML!");
                        }
                    }

                    for (Connection conn: projection.getConnection())
                    {
                        int preSeg = conn.getPreSegmentId();
                        int postSeg = conn.getPostSegmentId();

                        float preFract = (new Double(conn.getPreFractionAlong())).floatValue();
                        float postFract = (new Double(conn.getPostFractionAlong())).floatValue();

                        this.netConns.addSynapticConnection(netConn, 
                                                            GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION,
                                                            parseForCellNumber(conn.getPreCellId()), 
                                                            preSeg,
                                                            preFract,
                                                            parseForCellNumber(conn.getPostCellId()),
                                                            postSeg,
                                                            postFract,
                                                            0,
                                                            null);
                    }
                }
                
                
                for (InputList inputList: network.getInputList()) 
                {
                    String inputId = inputList.getId();
                    
                    if (!project.elecInputInfo.getAllStimRefs().contains(inputId)) 
                    {
                        throw new NeuroMLException("neuroConstruct can only import inputLists from NeuroML when an electrical input with that name already exists in the project!");
                    }
                    
                    for (Input input: inputList.getInput()) 
                    {
                        String inputType = null;
                        
                        if (pulseGenerators.containsKey(inputId))
                            inputType = IClamp.TYPE;
                        else 
                        {
                            throw new NeuroMLException("Can not determine the type of the electrical input to "+inputId+" (no <pulseGenerator> with that id)!");
                        }
                        
                        int segmentId = input.getSegmentId()!=null ? input.getSegmentId() : 0;

                        float fractAlong = input.getFractionAlong()!=null ? input.getFractionAlong().floatValue() : 0.5f;
                        
                        elecInputs.addSingleInput(inputId, 
                                                  inputType, 
                                                  inputList.getPopulation(), 
                                                  parseForCellNumber(input.getTarget()), 
                                                  segmentId, 
                                                  fractAlong);
                    }
                }
            }
        }
        catch (MalformedURLException e) 
        {
            throw new NeuroMLException("Problem parsing NeuroML file: "+nml2File, e);
        } 
        catch (org.neuroml.model.util.NeuroMLException e) 
        {
            throw new NeuroMLException("Problem parsing NeuroML file: "+nml2File, e);
        }
        
        
    }
    
    private int parseForCellNumber(String cellIdString) 
    {
        //System.out.println("cellIdString: "+cellIdString);
        int lastSlash = cellIdString.lastIndexOf("/");
        int secondLastSlash = cellIdString.substring(0, lastSlash).lastIndexOf("/");
        return Integer.parseInt(cellIdString.substring(secondLastSlash+1, lastSlash));
    }


    public static void main(String args[])
    {

        try
        {
            //Project testProj = Project.loadProject(new File("testProjects/TestNetworkML/TestNetworkML.neuro.xml"),null);
            Project testProj = Project.loadProject(new File("osb/invertebrate/celegans/CElegansNeuroML/CElegans/CElegans.ncx"),null);
            testProj = Project.loadProject(new File("osb/cerebellum/cerebellar_granule_cell/GranuleCell/neuroConstruct/GranuleCell.ncx"),null);
            testProj = Project.loadProject(new File("osb/cerebral_cortex/networks/ACnet2/neuroConstruct/ACnet2.ncx"),null);

            File f = new File("testProjects/TestNetworkML/savedNetworks/test_nml2.xml");
            f = new File("testProjects/TestNetworkML/savedNetworks/nnn.nml");
            
            boolean network = true;
            if (network) 
            {
                f = new File("osb/invertebrate/celegans/CElegansNeuroML/CElegans/pythonScripts/CElegansConnectome.nml");
                f = new File("osb/cerebral_cortex/networks/ACnet2/neuroConstruct/generatedNeuroML2/ACnet2.net.nml");

                logger.logComment("Loading nml cell from "+ f.getAbsolutePath()+" for proj: "+ testProj);

                ProjectManager pm = new ProjectManager(null, null);

                pm.setCurrentProject(testProj);


                pm.doLoadNeuroML2Network(f, false);


                while (pm.isGenerating())
                {
                    Thread.sleep(2);
                    System.out.println("Waiting...");
                }

                System.out.println(testProj.generatedCellPositions.details());
                System.out.println(testProj.generatedNetworkConnections.details());
            }
            else 
            {
                f = new File("osb/cerebellum/cerebellar_granule_cell/GranuleCell/neuroConstruct/generatedNeuroML2/Granule_98.cell.nml");
                f = new File("osb/cerebral_cortex/networks/ACnet2/neuroConstruct/generatedNeuroML2/bask.cell.nml");
                
                logger.logComment("Loading nml cell from "+ f.getAbsolutePath()+" for proj: "+ testProj);


                NeuroML2Reader nml2Reader = new NeuroML2Reader(testProj);

                nml2Reader.parse(f, "New_");
            }
            

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }

}
