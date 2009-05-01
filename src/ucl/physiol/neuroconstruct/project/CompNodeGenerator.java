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

import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;
import ucl.physiol.neuroconstruct.hpc.mpi.MpiConfiguration;
//import ucl.physiol.neuroconstruct.hpc.mpi.MpiSettings;


/**
 * Thread to handle generation of node ids of the generated cells, possibly based on cell position and 
 * network connections
 *
 * @author Padraig Gleeson
 *  
 */


public class CompNodeGenerator extends Thread
{
    ClassLogger logger = new ClassLogger("CompNodeGenerator");

    public final static String myGeneratorType = "CompNodeGenerator";

    Project project = null;
    long startGenerationTime;
    boolean continueGeneration = true;

    GenerationReport myReportInterface = null;


    private SimConfig simConfig = null;



    public CompNodeGenerator(Project project, GenerationReport reportInterface)
    {
        super(myGeneratorType);

        logger.logComment("New CompNodeGenerator created");
        this.project = project;

        myReportInterface = reportInterface;

    }

    public void setSimConfig(SimConfig simConfig)
    {
        this.simConfig = simConfig;
    }


    public void stopGeneration()
    {
        logger.logComment("CellPositionGenerator being told to stop...");
        continueGeneration = false;
    }

    @Override
    public void run()
    {
        logger.logComment("Running CompNodeGenerator thread...");
        startGenerationTime = System.currentTimeMillis();

        LinkedList<String> cellGroupNames = simConfig.getPrioritizedCellGroups(project);
        
        MpiConfiguration mpiConfig = simConfig.getMpiConf();

        int totalProcs = mpiConfig.getTotalNumProcessors();
        
       
        
        //Random r = ProjectManager.getRandomGenerator();
        
        Hashtable<String, ArrayList<Integer>> hostsVsNumOnProcs = new Hashtable<String, ArrayList<Integer>>();
        
        int cellCount = 0;


        for (int l = 0; l < cellGroupNames.size(); l++)
        {
            
            if (continueGeneration)
            {
                String nextCellGroup = cellGroupNames.get(l);

                logger.logComment(">>>>>   Generating compute nodes for cell group: " + nextCellGroup+", all cell groups: "+ cellGroupNames);

                this.myReportInterface.giveUpdate("Generating compute nodes for Cell Group: " + nextCellGroup+"...");

                ArrayList<PositionRecord> posRecs = project.generatedCellPositions.getPositionRecords(nextCellGroup);
                
                for(PositionRecord pos: posRecs)
                {
                    cellCount++;
                    
                    //int nodeID = r.nextInt(totalProcs);
                    int nodeID = ((cellCount-1)%totalProcs);
                    
                    logger.logComment("cellCount: "+cellCount+", nextCellGroup: "
                            +nextCellGroup+", nodeID: "+nodeID);
                    
                    
                    pos.setNodeId(nodeID);
                    String host = mpiConfig.getHostForGlobalId(nodeID);
                    int procNum = mpiConfig.getProcForGlobalId(nodeID);
                    
                    if (hostsVsNumOnProcs.get(host)==null)
                    {
                        ArrayList<Integer> numOnProcs = new ArrayList<Integer>();
                        for(int i=0;i<mpiConfig.getNumProcessorsOnHost(host);i++)
                        {
                            numOnProcs.add(0);
                        }
                        hostsVsNumOnProcs.put(host, numOnProcs);
                    }
                    ArrayList<Integer> numOnProcs = hostsVsNumOnProcs.get(host);
                    numOnProcs.set(procNum, numOnProcs.get(procNum)+1);
                    
                }
                

            }
            if (myReportInterface != null) myReportInterface.majorStepComplete();
        }

        long positionsGeneratedTime = System.currentTimeMillis();
        float seconds = (float) (positionsGeneratedTime - startGenerationTime) / 1000f;

        logger.logComment("Generating the report to send...");

        /*
        StringBuffer generationReport = new StringBuffer();
        
        String info = mpiConfig.toString();
        info = GeneralUtils.replaceAllTokens(info, "\n", "<br>");
        

        generationReport.append("<center><b>Compute nodes:</b></center>");


        if (!continueGeneration)
            generationReport.append("<center><b>NOTE: Generation interrupted</b></center><br>");


        generationReport.append("Time taken to generate compute nodes: " + secondsPosns + " seconds.<br>");
        

        generationReport.append("Compute nodes generated for:<br><b>"+info+"</b>");
        
        Enumeration<String> hosts = hostsVsNumOnProcs.keys();
        
        while(hosts.hasMoreElements())
        {
            String host = hosts.nextElement();
            
            generationReport.append("Host: <b>"+host+"</b>: ");

            ArrayList<Integer> numOnProcs = hostsVsNumOnProcs.get(host);
            
            for(int i=0;i<numOnProcs.size();i++)
            {
                generationReport.append("proc: "+i+" has <b>"+numOnProcs.get(i)+"</b> cells");
                if(i<numOnProcs.size()-1) 
                    generationReport.append(", ");
                else
                    generationReport.append("<br><br>");
                    
            }
        }
        generationReport.append("<br>");*/
        
        StringBuffer generationReport = new StringBuffer(generateCompNodesReport(hostsVsNumOnProcs, mpiConfig, seconds));
        
        if (!continueGeneration)
            generationReport.append("<center><b>NOTE: Generation interrupted</b></center><br>");

        if (myReportInterface!=null)
        {
            myReportInterface.giveGenerationReport(generationReport.toString(),
                                                   myGeneratorType,
                                                   simConfig);
        }
    }
    
    public static Hashtable<String, ArrayList<Integer>> getHostsVsNumOnProcs(Project project, SimConfig simConfig)
    {
        Hashtable<String, ArrayList<Integer>> hostsVsNumOnProcs = new Hashtable<String, ArrayList<Integer>>();
        
        Iterator<String> cellGroupNames = project.generatedCellPositions.getNamesGeneratedCellGroups();
        
        if (!simConfig.getMpiConf().isParallelOrRemote())
        {
            ArrayList<Integer> allLocal = new ArrayList<Integer>();
            allLocal.add(project.generatedCellPositions.getNumberInAllCellGroups());
            hostsVsNumOnProcs.put("localhost", allLocal);
            return hostsVsNumOnProcs;
        }

        while (cellGroupNames.hasNext())
        {
            String nextCellGroup = cellGroupNames.next();

            ArrayList<PositionRecord> posRecs = project.generatedCellPositions.getPositionRecords(nextCellGroup);

            for(PositionRecord pos: posRecs)
            {
                String host = simConfig.getMpiConf().getHostForGlobalId(pos.getNodeId());
                int procNum = simConfig.getMpiConf().getProcForGlobalId(pos.getNodeId());

                if (hostsVsNumOnProcs.get(host)==null)
                {
                    ArrayList<Integer> numOnProcs = new ArrayList<Integer>();
                    for(int i=0;i<simConfig.getMpiConf().getNumProcessorsOnHost(host);i++)
                    {
                        numOnProcs.add(0);
                    }
                    hostsVsNumOnProcs.put(host, numOnProcs);
                }
                ArrayList<Integer> numOnProcs = hostsVsNumOnProcs.get(host);
                numOnProcs.set(procNum, numOnProcs.get(procNum)+1);

            }
                

        }
        return hostsVsNumOnProcs;
    }
    
    
    public static String generateCompNodesReport(Hashtable<String, ArrayList<Integer>> hostsVsNumOnProcs, MpiConfiguration mpiConfig, float seconds)
    {
        StringBuffer generationReport = new StringBuffer();
        
        String info = mpiConfig.toString();
        info = GeneralUtils.replaceAllTokens(info, "\n", "<br>");
        

        generationReport.append("<center><b>Compute nodes:</b></center>");

        if (seconds>=0)
        {
            generationReport.append("Time taken to generate compute nodes: " + seconds + " seconds.<br>");
            generationReport.append("Compute nodes generated for:<br><b>"+info+"</b>");
        }
        else
        {
            generationReport.append("Compute nodes reloaded using:<br><b>"+info+"</b>");
        }
        
        Enumeration<String> hosts = hostsVsNumOnProcs.keys();
        
        while(hosts.hasMoreElements())
        {
            String host = hosts.nextElement();
            
            generationReport.append("Host: <b>"+host+"</b>: ");

            ArrayList<Integer> numOnProcs = hostsVsNumOnProcs.get(host);
            
            for(int i=0;i<numOnProcs.size();i++)
            {
                generationReport.append("proc: "+i+" has <b>"+numOnProcs.get(i)+"</b> cells");
                if(i<numOnProcs.size()-1) 
                    generationReport.append(", ");
                else
                    generationReport.append("<br><br>");
                    
            }
        }
        generationReport.append("<br>");
        
        return generationReport.toString();
    }

}
