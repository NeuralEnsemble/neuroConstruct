/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ucl.physiol.neuroconstruct.cell.converters;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.BiophysicalProperties;
import org.neuroml.model.BiophysicalProperties2CaPools;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.Include;
import org.neuroml.model.InitMembPotential;
import org.neuroml.model.IntracellularProperties;
import org.neuroml.model.Member;
import org.neuroml.model.MembraneProperties;
import org.neuroml.model.MembraneProperties2CaPools;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Point3DWithDiam;
import org.neuroml.model.Resistivity;
import org.neuroml.model.SegmentGroup;
import org.neuroml.model.SegmentParent;
import org.neuroml.model.SpecificCapacitance;
import org.neuroml.model.util.NeuroMLConverter;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.ChannelMechanism;
import ucl.physiol.neuroconstruct.cell.Section;
import ucl.physiol.neuroconstruct.cell.Segment;
import ucl.physiol.neuroconstruct.neuroml.NeuroMLException;
import ucl.physiol.neuroconstruct.project.Project;
import ucl.physiol.neuroconstruct.utils.GuiUtils;
import ucl.physiol.neuroconstruct.utils.NumberGenerator;
import ucl.physiol.neuroconstruct.utils.units.UnitConverter;
import ucl.physiol.neuroconstruct.utils.ClassLogger;

/**
 * @author Padraig Gleeson
 * 
 * @author Rokas Stanislovas
 */
public class NeuroML2CellReader {
    
    public static ClassLogger logger = new ClassLogger("NeuroML2CellReader");
    
    org.neuroml.model.Cell nml2Cell = null;
    
    org.neuroml.model.Cell2CaPools nml2Cell2CaPools = null;
    
    org.neuroml.model.Morphology NML2morphology = null;
    
    private Cell cell = null;
    
    private String cellID=null;
    
    private HashMap<String,Integer> SectionsVSegs = new HashMap<String,Integer>();
    
    String infoOnnCCellSupport = "Currently, neuroConstruct import of NeuroML2 files is geared towards files it has generated itself.\n"
                    + "Restrictions on files it can import include:\n"
                    + " - &lt;segmentGroup&gt; should have only &lt;member&gt; elements (interpreted as unbranched Sections)\n"
                    + "   OR only &lt;include&gt; elements (interpreted as Section groups)\n"
                    + " - Recognised neuroLexIds: \n"
                    + "      GO:0043025 == soma_group\n"
                    + "      GO:0030425 == dendrite_group\n"
                    + "      GO:0030424 == axon_group\n";
    
    public NeuroML2CellReader(org.neuroml.model.Cell2CaPools neuroml2_cell, String cellTypeId) 
    {
        this.nml2Cell2CaPools=neuroml2_cell;
        
        this.cellID=cellTypeId;
        
    }
    
    public NeuroML2CellReader(org.neuroml.model.Cell neuroml2_cell, String cellTypeId) 
    {
        this.nml2Cell=neuroml2_cell;
        
        this.cellID=cellTypeId;
    }
    
    public void parse() throws NeuroMLException, org.neuroml.model.util.NeuroMLException
    {
        cell= new Cell();
        
        cell.setInstanceName(cellID);
        
        if (nml2Cell != null)
        {
           if (nml2Cell.getNotes()!=null)
              cell.setCellDescription(nml2Cell.getNotes());
           
           NML2morphology= nml2Cell.getMorphology();
        }
        
        if (nml2Cell2CaPools != null)
        {
            if (nml2Cell2CaPools.getNotes()!=null)
               cell.setCellDescription(nml2Cell2CaPools.getNotes());
           
            NML2morphology= nml2Cell2CaPools.getMorphology();
        }
        
        HashMap<Integer, Segment> segIdVsSegments = new HashMap<Integer, Segment>();
        HashMap<String, Section> secNameVsSections = new HashMap<String, Section>();
        Vector<Segment> allSegments = new Vector<Segment>();     
        for (org.neuroml.model.Segment nml2Segment: NML2morphology.getSegment())
        {
            logger.logComment("Adding Segment: "+ nml2Segment.getId());
            Point3DWithDiam dist = nml2Segment.getDistal();
                    
                    
            Segment nCsegment = new Segment();
            nCsegment.setSegmentId(nml2Segment.getId());
            nCsegment.setSegmentName(nml2Segment.getName());
                    
            nCsegment.setEndPointPositionX((float)dist.getX());
            nCsegment.setEndPointPositionY((float)dist.getY());
            nCsegment.setEndPointPositionZ((float)dist.getZ());
            nCsegment.setRadius((float)dist.getDiameter()/2);
                    
            segIdVsSegments.put(nml2Segment.getId(), nCsegment);
            
            //System.out.println("Printing added segment id: "+nml2Segment.getId());
            
            allSegments.add(nCsegment);
            
                    
        }
        cell.setAllSegments(allSegments);
        
        for (SegmentGroup segGroup: NML2morphology.getSegmentGroup())
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
       for (org.neuroml.model.Segment nml2Segment: NML2morphology.getSegment())
       {
            logger.logComment("Checking Segment: "+ nml2Segment.getId());
            SegmentParent parent = nml2Segment.getParent();
            Point3DWithDiam prox = nml2Segment.getProximal();
            Point3DWithDiam dist = nml2Segment.getDistal();
                            
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
                seg.setFractionAlongParent(parent.getFractionAlong());
                
            }            
        }
        if (nml2Cell !=null)
        {
         if (nml2Cell.getBiophysicalProperties()!=null) 
         {
                    
            BiophysicalProperties bp = nml2Cell.getBiophysicalProperties();
            MembraneProperties mp = bp.getMembraneProperties();
                    
            for (SpecificCapacitance specCap: mp.getSpecificCapacitance()) {
                        
                String group = (specCap.getSegmentGroup()==null || specCap.getSegmentGroup().length()==0) ? Section.ALL : specCap.getSegmentGroup();
                float valInSI = Utils.getMagnitudeInSI(specCap.getValue());
                float valInnC = (float)UnitConverter.getSpecificCapacitance(valInSI, UnitConverter.GENESIS_SI_UNITS, UnitConverter.NEUROCONSTRUCT_UNITS);
                cell.associateGroupWithSpecCap(group, valInnC);
            }
                    
            for (InitMembPotential imp: mp.getInitMembPotential()) {
                        
                String group = (imp.getSegmentGroup()==null || imp.getSegmentGroup().length()==0) ? Section.ALL : imp.getSegmentGroup();
                if (!group.equals(Section.ALL)) 
                {
                    throw new NeuroMLException("neuroConstruct can only import cells with the same initial membrane potential across all segments!");
                }
                float valInSI = Utils.getMagnitudeInSI(imp.getValue());
                float valInnC = (float)UnitConverter.getVoltage(valInSI, UnitConverter.GENESIS_SI_UNITS, UnitConverter.NEUROCONSTRUCT_UNITS);
                cell.setInitialPotential(new NumberGenerator(valInnC));
            }
                    
            for(ChannelDensity cd: mp.getChannelDensity())
            {
                        
                String group = (cd.getSegmentGroup()==null || cd.getSegmentGroup().length()==0) ? Section.ALL : cd.getSegmentGroup();
                float valInSI = Utils.getMagnitudeInSI(cd.getCondDensity());
                float valInnC = (float)UnitConverter.getConductanceDensity(valInSI, UnitConverter.GENESIS_SI_UNITS, UnitConverter.NEUROCONSTRUCT_UNITS);
                ChannelMechanism cm = new ChannelMechanism(cd.getIonChannel(), valInnC);
                cell.associateGroupWithChanMech(group, cm);
                        
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
                cell.associateGroupWithSpecAxRes(group, valInnC);
            }
         }
        }
        if (nml2Cell2CaPools !=null)
        {
         if (nml2Cell2CaPools.getBiophysicalProperties2CaPools()!=null) 
         {
                    
            BiophysicalProperties2CaPools bp = nml2Cell2CaPools.getBiophysicalProperties2CaPools();
            MembraneProperties2CaPools mp = bp.getMembraneProperties2CaPools();
                    
            for (SpecificCapacitance specCap: mp.getSpecificCapacitance()) {
                        
                String group = (specCap.getSegmentGroup()==null || specCap.getSegmentGroup().length()==0) ? Section.ALL : specCap.getSegmentGroup();
                float valInSI = Utils.getMagnitudeInSI(specCap.getValue());
                float valInnC = (float)UnitConverter.getSpecificCapacitance(valInSI, UnitConverter.GENESIS_SI_UNITS, UnitConverter.NEUROCONSTRUCT_UNITS);
                cell.associateGroupWithSpecCap(group, valInnC);
            }
                    
            for (InitMembPotential imp: mp.getInitMembPotential()) {
                        
                String group = (imp.getSegmentGroup()==null || imp.getSegmentGroup().length()==0) ? Section.ALL : imp.getSegmentGroup();
                if (!group.equals(Section.ALL)) 
                {
                    throw new NeuroMLException("neuroConstruct can only import cells with the same initial membrane potential across all segments!");
                }
                float valInSI = Utils.getMagnitudeInSI(imp.getValue());
                float valInnC = (float)UnitConverter.getVoltage(valInSI, UnitConverter.GENESIS_SI_UNITS, UnitConverter.NEUROCONSTRUCT_UNITS);
                cell.setInitialPotential(new NumberGenerator(valInnC));
            }
                    
            for(ChannelDensity cd: mp.getChannelDensity())
            {
                
                String group = (cd.getSegmentGroup()==null || cd.getSegmentGroup().length()==0) ? Section.ALL : cd.getSegmentGroup();
                float valInSI = Utils.getMagnitudeInSI(cd.getCondDensity());
                float valInnC = (float)UnitConverter.getConductanceDensity(valInSI, UnitConverter.GENESIS_SI_UNITS, UnitConverter.NEUROCONSTRUCT_UNITS);
                ChannelMechanism cm = new ChannelMechanism(cd.getIonChannel(), valInnC);
                cell.associateGroupWithChanMech(group, cm);
                        
            }
            
            //if (!mp.getChannelDensityGHK().isEmpty()) {
              //  throw new NeuroMLException("Import of NeuroML2 with channelDensityGHK not yet implemented! Ask and it shall be done...");
            //}
            if (!mp.getChannelDensityNernst().isEmpty()) {
                
              for (org.neuroml.model.ChannelDensityNernst cdn: mp.getChannelDensityNernst())
              {
                String group = (cdn.getSegmentGroup()==null || cdn.getSegmentGroup().length()==0) ? Section.ALL : cdn.getSegmentGroup();
                float valInSI = Utils.getMagnitudeInSI(cdn.getCondDensity());
                float valInnC = (float)UnitConverter.getConductanceDensity(valInSI, UnitConverter.GENESIS_SI_UNITS, UnitConverter.NEUROCONSTRUCT_UNITS);
                ChannelMechanism cm = new ChannelMechanism(cdn.getIonChannel(), valInnC);
                cell.associateGroupWithChanMech(group, cm);
                //// ion species ?
              }
            }
            //if (!mp.getChannelDensityNonUniform().isEmpty()) {
              //  throw new NeuroMLException("Import of NeuroML2 with channelDensityNonUniform not yet implemented! Ask and it shall be done...");
            //}
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
                    
            IntracellularProperties ip = bp.getIntracellularProperties2CaPools();
                    
            for (Resistivity res: ip.getResistivity()) 
            {
                String group = (res.getSegmentGroup()==null || res.getSegmentGroup().length()==0) ? Section.ALL : res.getSegmentGroup();
                float valInSI = Utils.getMagnitudeInSI(res.getValue());
                float valInnC = (float)UnitConverter.getSpecificAxialResistance(valInSI, UnitConverter.GENESIS_SI_UNITS, UnitConverter.NEUROCONSTRUCT_UNITS);
                cell.associateGroupWithSpecAxRes(group, valInnC);
            }
         }
        }         
    }
    
    
    
    public Cell getBuiltCell()
    {
        return cell;
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
            
            f = new File("osb/cerebellum/cerebellar_granule_cell/GranuleCell/neuroConstruct/generatedNeuroML2/Granule_98.cell.nml");
            f = new File("osb/cerebral_cortex/networks/ACnet2/neuroConstruct/generatedNeuroML2/bask.cell.nml");
                
            logger.logComment("Loading nml cell from "+ f.getAbsolutePath()+" for proj: "+ testProj);
            
            NeuroMLConverter neuromlConverter=new NeuroMLConverter();

            NeuroMLDocument neuroml = neuromlConverter.urlToNeuroML(f.toURI().toURL());

            logger.logComment("Reading in NeuroML2: "+ neuroml.getId(), true);
            
            for (org.neuroml.model.Cell NML2Cell: neuroml.getCell())
            {
                NeuroML2CellReader cellReader= new NeuroML2CellReader(NML2Cell,NML2Cell.getId());
                         
                cellReader.parse();
                
                
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }
    
    
    
    
}
