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

import java.io.*;
import java.util.ArrayList;
import java.util.Vector;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import ucl.physiol.neuroconstruct.mechanisms.CellMechanism;
import ucl.physiol.neuroconstruct.mechanisms.ChannelMLCellMechanism;
import ucl.physiol.neuroconstruct.mechanisms.XMLMechanismException;
import ucl.physiol.neuroconstruct.project.packing.CellPackingAdapter;

/**
 * Class for generating HTML representation of neuroConstruct project
 *
 * @author Padraig Gleeson
 *  
 */

public class Expand
{
    private static ClassLogger logger = new ClassLogger("Expand");
    
    static String CELL_TYPES = "cellTypes";
    static String CELL_MECHANISMS = "cellMechanisms";

    private static int fontSize = 10;
    
    public Expand()
    {
    }


    public static void generateProjectView(Project project, File dir)
    {

    }
    
    public static String getItemPage(String origName)
    {
    	return GeneralUtils.replaceAllTokens(origName, " ", "_")+".html";
    }
    

    public static String getCellTypePage(String cellTypeName)
    {
    	return CELL_TYPES+"/"+getItemPage(cellTypeName);
    }
    public static String getCellMechPage(String cellMechName)
    {
    	return CELL_MECHANISMS+"/"+getItemPage(cellMechName);
    }

    public static File generateMainPage(Project project, File dirToCreateIn)
    {
        String mainPageTitle = "index";

        ArrayList<String> pages = new ArrayList<String>();
        pages.add(mainPageTitle);

        GeneralUtils.removeAllFiles(dirToCreateIn, false, false, true);

        for (String sc: project.simConfigInfo.getAllSimConfigNames())
            pages.add(sc);

        for(String title: pages)
        {
            File fileToSave = new File(dirToCreateIn, getItemPage(title));
            
            SimpleHtmlDoc mainPage = new SimpleHtmlDoc(project.getProjectName()+ ": "+ title, fontSize);

            mainPage.addTaggedElement("neuroConstruct project: "+ project.getProjectName(), "h2");

            mainPage.addTaggedElement("Simulation configurations", "h2");
            
            
            for (String sc: project.simConfigInfo.getAllSimConfigNames())
            {
                String scFile = getItemPage(sc);
                mainPage.addTaggedElement(mainPage.getLinkedText(sc, scFile), "b");
            }

            String desc = project.getProjectDescription();

            Vector<Cell> cells = new Vector<Cell>();

            ArrayList<String> cellMechs = new ArrayList<String>();
            ArrayList<String> cellGroups = new ArrayList<String>();
            ArrayList<String> netConns = new ArrayList<String>();
            ArrayList<String> inputs = new ArrayList<String>();

            if(title.equals(mainPageTitle))
            {
                cells = project.cellManager.getAllCells();

                cellMechs.addAll(project.cellMechanismInfo.getAllCellMechanismNames());

                cellGroups = project.cellGroupsInfo.getAllCellGroupNames();

                netConns.addAll(project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames());
                netConns.addAll(project.volBasedConnsInfo.getAllAAConnNames());

                inputs.addAll(project.elecInputInfo.getAllStimRefs());
            }
            else
            {
                SimConfig sc = project.simConfigInfo.getSimConfig(title);
                desc=sc.getDescription();
                //cells.removeAllElements();
                //cellMechs.removeAllElements();

                cellGroups = sc.getCellGroups();
                netConns = sc.getNetConns();
                inputs = sc.getInputs();

                for(String cg: sc.getCellGroups())
                {
                    String cellType = project.cellGroupsInfo.getCellType(cg);
                    Cell cell = project.cellManager.getCell(cellType);

                    if (!cells.contains(cell))
                        cells.add(cell);

                    ArrayList<String> cms = cell.getAllChanMechNames(true);

                    cms.addAll(cell.getAllAllowedSynapseTypes());
                    for(String cm: cms)
                    {
                        if (!cellMechs.contains(cm))
                            cellMechs.add(cm);
                    }

                }
            }

            int width = 700;
            int width1 = 140;


            mainPage.addRawHtml("<p>&nbsp;</p>");

            mainPage.addRawHtml("<table border=\"1\" width='"+width+"' valign='centre' cellpadding='3'>");

            mainPage.addRawHtml("<tr><td class='header'  colspan='2'><b>A: Model Summary</b></td></tr>");
            mainPage.addRawHtml("<tr><td width='"+width1+"'><b>Description</b></td><td>"+ handleWhitespaces(desc)+"</td></tr>");
            mainPage.addRawHtml("<tr><td><b>Populations</b></td><td>");
            for(String cg: cellGroups)
            {
                String cellType = project.cellGroupsInfo.getCellType(cg);
                String cellPageLoc = getCellTypePage(cellType);
                mainPage.addRawHtml("<a href=\"#"+cg+"\">"+cg+"</a>");

                if (!cg.equals(cellGroups.get(cellGroups.size()-1)))
                    mainPage.addRawHtml(" ");
            }

            mainPage.addRawHtml("</td></tr>");
            mainPage.addRawHtml("<tr><td><b>Topology</b></td><td>Network of neurons positioned & connected in 3D space</td></tr>");

            mainPage.addRawHtml("<tr><td><b>Connectivity</b></td><td>");
            if (netConns.size()==0)
                mainPage.addRawHtml("No network connections in this Simulation Configuration");

            for(String nc: netConns)
            {
                mainPage.addRawHtml("<a href=\"#"+nc+"\">"+nc+"</a>");
                if (!nc.equals(netConns.get(netConns.size()-1)))
                    mainPage.addRawHtml(",");
            }
            mainPage.addRawHtml("</td></tr>");


            mainPage.addRawHtml("<tr><td><b>Neuron models</b></td><td>");
            for(Cell cell: cells)
            {
                //mainPage.addRawHtml(cell.getInstanceName());

                String cellPageLoc = getCellTypePage(cell.getInstanceName());
                mainPage.addRawHtml(" "+"<a href = \"#"+cell.getInstanceName()+"\">"+cell.getInstanceName()+"</a>");

                if (!cell.equals(cells.get(cells.size()-1)))
                    mainPage.addRawHtml(", ");

                SimpleHtmlDoc cellPage = new SimpleHtmlDoc("Cell: "+ cell.getInstanceName(), fontSize);

                cellPage.addRawHtml(CellTopologyHelper.printDetails(cell, project, true, true, false, true));

                File cellFile = new File(fileToSave.getParentFile(), cellPageLoc);
                if (!cellFile.exists())
                {
                    cellPage.saveAsFile(cellFile);
                }

            }
            mainPage.addRawHtml("</td></tr>");


            StringBuffer cmInfo = new StringBuffer();
            StringBuffer synInfo = new StringBuffer();

            for(String cmName: cellMechs)
            {
                StringBuffer mechInfo = cmInfo;

                CellMechanism cm = project.cellMechanismInfo.getCellMechanism(cmName);


                if(cm.isSynapticMechanism() || cm.isGapJunctionMechanism())
                    mechInfo = synInfo;
                
                
                //mechInfo.append(cm+" ");
                

                String cmPageLoc = getCellMechPage(cm.getInstanceName());

                mechInfo.append("<a href = \""+cmPageLoc	+"\">"+cm.getInstanceName()+"</a> ");

                File xslDoc = GeneralProperties.getChannelMLReadableXSL();


                SimpleHtmlDoc cmPage = new SimpleHtmlDoc("Cell Mechanisms: "+ cm.getInstanceName(), fontSize);

                File cmPageFile = new File(fileToSave.getParentFile(), cmPageLoc);

                if (cm instanceof ChannelMLCellMechanism)
                {
                    ChannelMLCellMechanism cmlCm = (ChannelMLCellMechanism)cm;

                    String cmXmlPageLoc = getCellMechPage(cm.getInstanceName()+".channelml");
                    File cmXmlPageFile = new File(fileToSave.getParentFile(), cmXmlPageLoc);

                    
                    if (!cmXmlPageFile.exists() || !cmPageFile.exists())
                    {
                        try
                        {
                            String readable = XMLUtils.transform(cmlCm.getXMLDoc().getXMLString("", false),xslDoc);

                            cmPage.addRawHtml(readable);
                        }
                        catch (XMLMechanismException e)
                        {
                            cmPage.addTaggedElement("Unable to generate HTML representation of: "+ cm.getInstanceName(), "b");
                        }

                        SimpleHtmlDoc cmXmlPage = new SimpleHtmlDoc("Cell Mechanism: "+ cm.getInstanceName()+" in ChannelML", fontSize);


                        try
                        {
                            String cmlString = cmlCm.getXMLDoc().getXMLString("", true);


                            cmXmlPage.addRawHtml(cmlString);
                        }
                        catch (XMLMechanismException e)
                        {
                            cmXmlPage.addTaggedElement("Unable to generate ChannelML representation of: "+ cm.getInstanceName(), "b");
                        }

                        cmXmlPage.saveAsFile(cmXmlPageFile);
                        cmPage.saveAsFile(cmPageFile);

                        //mainPage.addRawHtml("</td><td><a href = "+cmXmlPageLoc	+">ChannelML file</td><td>");
                    }
                }
                else
                {

                }



            }

            mainPage.addRawHtml("<tr><td><b>Channel models</b></td><td>"+cmInfo+"</td></tr>");
            if (synInfo.length()==0)
                synInfo.append("No synapses present in this Simulation Configuration");
            mainPage.addRawHtml("<tr><td><b>Synapse models</b></td><td>"+synInfo+"</td></tr>");


            mainPage.addRawHtml("<tr><td><b>Input</b></td><td>");
            for(String in: inputs)
            {
                String cg = project.elecInputInfo.getStim(in).getCellGroup();

                mainPage.addRawHtml(" "+in+" (to <a href=\"#"+cg+"\">"+ cg + "</a>)");
            }
            mainPage.addRawHtml("</td></tr>");

            mainPage.addRawHtml("</table>");



            mainPage.addRawHtml("<p>&nbsp;</p>");

            mainPage.addRawHtml("<table border=\"1\" width='"+width+"'  cellpadding='3'>");

            mainPage.addRawHtml("<tr><td class='header'  colspan='3'><b>B: Populations</b></td></tr>");

            mainPage.addRawHtml("<tr><td width='"+width1+"'><b>Name</b></td>" +
                                "<td  width='100'><b>Elements</b></td>" +
                                "<td><b>Description</b></td></tr>");

            for(String cg: cellGroups)
            {

                String cellType = project.cellGroupsInfo.getCellType(cg);
                String cellPageLoc = getCellTypePage(cellType);

                String regionName = project.cellGroupsInfo.getRegionName(cg);
                Region region = project.regionsInfo.getRegionObject(regionName);
                CellPackingAdapter cpa = project.cellGroupsInfo.getCellPackingAdapter(cg);

                mainPage.addRawHtml("<tr><td><a name=\""+cg+"\"/>"+cg+"</td>" +
                                    "<td><a href = \""+cellPageLoc+"\">"+project.cellGroupsInfo.getCellType(cg)+"</a></td>" +
                                    "<td>"+cpa.toNiceString() +"<br>" +
                                    "In region ("+regionName+"): "+region.toString()+"</td></tr>");
            }


            mainPage.addRawHtml("</table>");

            mainPage.addRawHtml("<p>&nbsp;</p>");

            mainPage.addRawHtml("<table border=\"1\" width='"+width+"' valign='centre' cellpadding='3'>");

            mainPage.addRawHtml("<tr><td class='header'  colspan='4'><b>C: Connectivity</b></td></tr>");


            mainPage.addRawHtml("<tr><td width='"+width1+"'><b>Name</b></td>" +
                    "<td  width='100'><b>Source</b></td>" +
                    "<td  width='100'><b>Target</b></td>" +
                    "<td><b>Pattern</b></td></tr>");

            if (netConns.size()==0)
            {
                mainPage.addRawHtml("<tr>" +
                                    "<td colspan='4'>No network connections in this simulation Configuration</td>" +
                                    "</tr>");
            }

            for(String nc: netConns)
            {
                String src = project.morphNetworkConnectionsInfo.getSourceCellGroup(nc);
                String tgt = project.morphNetworkConnectionsInfo.getTargetCellGroup(nc);
                ConnectivityConditions cc = project.morphNetworkConnectionsInfo.getConnectivityConditions(nc);

                mainPage.addRawHtml("<tr>" +
                                    "<td><a name=\""+nc+"\"/>"+nc+"</td>" +
                                    "<td><a href=\"#"+src+"\">"+src+"</a></td>" +
                                    "<td><a href=\"#"+tgt+"\">"+tgt+"</td>" +
                                    "<td>"+cc+"</td>" +
                                    "</tr>");

            }
            mainPage.addRawHtml("</table>");

            

            mainPage.addRawHtml("<p>&nbsp;</p>");


            mainPage.addRawHtml("<table border=\"1\" width='"+width+"' valign='centre' cellpadding='3'>");

            mainPage.addRawHtml("<tr><td class='header' colspan='4'>D: Neuron and Synapse models</b></tr>");

            mainPage.addRawHtml("<tr><td width='"+width1+"'><b>Name</b></td>" +
                                "<td ><b>Description</b></td>" +
                                "<td  width='50'><b>Details</b></td></tr>");

            for(Cell cell: cells)
            {
                String cellPageLoc = getCellTypePage(cell.getInstanceName());

                mainPage.addRawHtml("<tr>" +
                                    "<td><a name=\""+cell.getInstanceName()+"\"/>"+cell.getInstanceName()+"</td>" +
                                    "<td>"+cell.getCellDescription()+"</a></td>" +
                                    "<td><a href=\""+cellPageLoc+"\">More...</td>" +
                                    "</tr>");

            }

            mainPage.addRawHtml("</table>");
            
            
            mainPage.addRawHtml("<p>&nbsp;</p>");


            mainPage.addRawHtml("<table border=\"1\" width='"+width+"' valign='centre' cellpadding='3'>");

            mainPage.addRawHtml("<tr><td class='header' colspan='4'>E: Inputs</b></tr>");

            mainPage.addRawHtml("<tr><td width='"+width1+"'><b>Name</b></td>" +
                                "<td ><b>Description</b></td>" +
                                "<td  width='50'><b>Details</b></td></tr>");


            mainPage.addRawHtml("</table>");
            
            
            mainPage.addRawHtml("<p>&nbsp;</p>");


            mainPage.addRawHtml("<table border=\"1\" width='"+width+"' valign='centre' cellpadding='3'>");

            mainPage.addRawHtml("<tr><td class='header' colspan='4'>F: Measurements</b></tr>");

            mainPage.addRawHtml("<tr><td width='"+width1+"'><b>Name</b></td>" +
                                "<td ><b>Description</b></td>" +
                                "<td  width='50'><b>Details</b></td></tr>");


            mainPage.addRawHtml("</table>");


            logger.logComment("Going to save: "+ fileToSave.getAbsolutePath(), true);
            mainPage.saveAsFile(fileToSave);
        }

        return new File(dirToCreateIn, getItemPage(mainPageTitle));
        
    }
    
    public static String handleWhitespaces(String text)
    {
        return GeneralUtils.replaceAllTokens(text, "\n", "<br/>");
    }

    public static void main(String[] args)
    {
        new Expand();
        
        try
        {
            //File projFile = new File("examples/Ex6-Cerebellum/Ex6-Cerebellum.neuro.xml");
            //File projFile = new File("C:\\copynCmodels\\TraubEtAl2005\\TraubEtAl2005.neuro.xml");
            //File projFile = new File("nCmodels/Thalamocortical/Thalamocortical.ncx");
            //File projFile = new File("nCmodels/CA1PyramidalCell/CA1PyramidalCell.ncx");
            File projFile = new File("nCmodels/GranuleCell/GranuleCell.ncx");
            //File projFile = new File("nCmodels/GranCellLayer/GranCellLayer.ncx");
            //File projFile = new File("nCexamples/Ex4_HHcell/Ex4_HHcell.ncx");
            //File projFile = new File("/bernal/models/Layer23_names/Layer23_names.neuro.xml");
            

            logger.logComment("Going to create docs for project: "+ projFile.getCanonicalPath(), true);
            
            
            Project testProj = Project.loadProject(projFile, new ProjectEventListener()
                {
                    public void tableDataModelUpdated(String tableModelName)
                    {};
                    
                    public void tabUpdated(String tabName)
                    {};
                    public void cellMechanismUpdated()
                    {};
                });

            File par = new File("../temp/testExpand");
            File f = generateMainPage(testProj, par);
            

            logger.logComment("Created a doc at: "+ f.getCanonicalPath(), true);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}
