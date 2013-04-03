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

import ucl.physiol.neuroconstruct.cell.converters.MorphologyException;
import ucl.physiol.neuroconstruct.utils.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Vector;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.converters.MorphMLConverter;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import ucl.physiol.neuroconstruct.mechanisms.CellMechanism;
import ucl.physiol.neuroconstruct.mechanisms.ChannelMLCellMechanism;
import ucl.physiol.neuroconstruct.mechanisms.FileBasedMembraneMechanism;
import ucl.physiol.neuroconstruct.mechanisms.MechanismImplementation;
import ucl.physiol.neuroconstruct.mechanisms.XMLMechanismException;
import ucl.physiol.neuroconstruct.neuroml.NeuroMLConstants.NeuroMLLevel;
import ucl.physiol.neuroconstruct.neuroml.NeuroMLConstants.NeuroMLVersion;
import ucl.physiol.neuroconstruct.project.packing.CellPackingAdapter;
import ucl.physiol.neuroconstruct.simulation.StimulationSettings;
import ucl.physiol.neuroconstruct.utils.xml.SimpleXMLDocument;
import ucl.physiol.neuroconstruct.utils.xml.SimpleXMLReader;

/**
 * Class for generating HTML representation of neuroConstruct project
 *
 * @author Padraig Gleeson
 *  
 */
public class Expand {

    private static ClassLogger logger = new ClassLogger("Expand");
    static String CELL_TYPES = "cellTypes";
    static String CELL_MECHANISMS = "cellMechanisms";
    private static int fontSize = 10;
    public static final String COLOUR_AMPA = "#FF0000";
    public static final String COLOUR_NMDA = "#FF9900";
    public static final String COLOUR_GABA = "#0000FF";
    public static final String COLOUR_GAP = "#669966";

    public static final String ccsInfo = "<link href=\"http://opensourcebrain.org:8080/themes/alternate/stylesheets/application.css?1300382035\" media=\"all\" rel=\"stylesheet\" type=\"text/css\" />";

    public static final String tableStyle = "style='list' width='700' frame='box' rules='all' ";
    public static final String tableStyleNoBorder = "style='list' width='700' ";
    public static final String headerStyle = "style='font-weight:bold; background-color:black; color:white'";

    public static final String preXML = "<table border='0'><tr><td style='width:90%; background-color:white'>";
    public static final String postXML = "</td></tr></table>";
    

    public Expand() {
    }

    public static void generateProjectView(Project project, File dir) {
    }

    public static String getItemPage(String origName) {
        return GeneralUtils.replaceAllTokens(origName, " ", "_") + ".html";
    }

    public static String getCellTypePage(String cellTypeName) {
        return CELL_TYPES + "/" + getItemPage(cellTypeName);
    }

    public static String getCellMechPage(String cellMechName) {
        return CELL_MECHANISMS + "/" + getItemPage(cellMechName);
    }

    public static String getNetConnInfo(Project project, String nc) {
        StringBuilder sb = new StringBuilder();
        //String src = project.morphNetworkConnectionsInfo.getSourceCellGroup(nc);
        //String tgt = project.morphNetworkConnectionsInfo.getTargetCellGroup(nc);

        if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(nc)) {
            ConnectivityConditions cc = project.morphNetworkConnectionsInfo.getConnectivityConditions(nc);

            NumberGenerator start = cc.getNumConnsInitiatingCellGroup();
            int maxFin = cc.getMaxNumInitPerFinishCell();

            if (cc.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET) {
                String max = maxFin == Integer.MAX_VALUE ? "X" : "max " + maxFin;
                sb.append(start.toShortString() + " -> " + max);
            } else if (cc.getGenerationDirection() == ConnectivityConditions.TARGET_TO_SOURCE) {
                String max = maxFin == Integer.MAX_VALUE ? "X" : "max " + maxFin;
                sb.append(max + " -> " + start.toShortString());
            }
            Vector<SynapticProperties> syns = project.morphNetworkConnectionsInfo.getSynapseList(nc);

            sb.append("<br/>");
            for (SynapticProperties syn : syns) {
                String synRef = syn.getSynapseType();
                if (synRef.length() > 12) {
                    if (synRef.indexOf("AMPA") >= 0) {
                        synRef = "AMPA ";
                    } else if (synRef.indexOf("NMDA") >= 0) {
                        synRef = "NMDA ";
                    } else if (synRef.indexOf("GABAA") >= 0) {
                        synRef = "GABAA ";
                    } else if (synRef.indexOf("GABA") >= 0) {
                        synRef = "GABA ";
                    } else if (synRef.indexOf("Elect") >= 0) {
                        synRef = "Gap J ";
                    } else if (synRef.indexOf("Gap") >= 0) {
                        synRef = "Gap J ";
                    } else {
                        synRef = "syn ";
                    }
                }
                sb.append("<a href = \"" + getCellMechPage(syn.getSynapseType()) + "\">" + synRef + "</a> ");

            }

            //sb.append(")");
        } else {
            ConnectivityConditions cc = project.volBasedConnsInfo.getConnectivityConditions(nc);
            sb.append(cc.toString());
        }

        return sb.toString();
    }

    public static void generateModelDescriptions(ArrayList<String> projPaths, String dirName) {

        logger.logComment("Going to create documentation at " + dirName + " for " + projPaths, true);

        File descriptionsDir = new File(dirName);
        ArrayList<Project> projects = new ArrayList<Project>();

        String indexTitle = "index.html";
        GeneralUtils.removeAllFiles(descriptionsDir, false, false, false);
        
        SimpleHtmlDoc indexPage = new SimpleHtmlDoc("Model descriptions", fontSize);
        indexPage.addToHead(ccsInfo);

        //
        File osbDir = new File(descriptionsDir, "osb");
        osbDir.mkdir();
        File fileToSave = new File(osbDir, indexTitle);


        indexPage.addTaggedElement("Project summaries", "h2");
        indexPage.addRawHtml("This is a collection of automatically generated descriptions for projects in the Open Source Brain Repository.");

        indexPage.addRawHtml("<table "+tableStyleNoBorder+">");


        for (String projPath : projPaths) {
            try {
                File projFile = new File(projPath);
                Project project = Project.loadProject(projFile, new ProjectEventListener() {

                    public void tableDataModelUpdated(String tableModelName) {
                    }

                    ;

                    public void tabUpdated(String tabName) {
                    }

                    ;

                    public void cellMechanismUpdated() {
                    }

                    ;
                });
                projects.add(project);

                String projName = project.getProjectName();
                String projNameStripped = GeneralUtils.replaceAllTokens(projName, " ", "_");
                String projNameLowercase = GeneralUtils.replaceAllTokens(GeneralUtils.replaceAllTokens(projName, "-", ""), "_", "").toLowerCase();
                if (projName.equals("SolinasEtAl-GolgiCell"))
                {
                    projNameLowercase = "cerebellum--cerebellar-golgi-cell--solinasetal-golgicell";
                }

                File projSpecificDir = new File(descriptionsDir, projNameLowercase);
                if (!projSpecificDir.exists()) {
                    projSpecificDir.mkdir();
                }


                File f = generateMainPage(project, projSpecificDir);
                File index = new File (f.getParentFile(), "index.html");
                GeneralUtils.copyFile(f, index);
                
                File projRelativePath = new File("../"+projNameLowercase, f.getName());

                indexPage.addRawHtml("<tr>");
                indexPage.addTaggedElement(indexPage.getLinkedText(projName, projRelativePath.toString()), "td");

                String desc = project.getProjectDescription();
                desc = desc.substring(0, desc.indexOf(".  ")+2);

                File imgDir = new File(project.getProjectMainDirectory(), "images");
                File imgTgt = new File(projSpecificDir, "images");
                imgDir.mkdir();
                GeneralUtils.copyDirIntoDir(imgDir, imgTgt, true, true);


                //indexPage.addRawHtml("</br>");
                indexPage.addTaggedElement(GeneralUtils.parseForHyperlinks(desc), "td");
                indexPage.addTaggedElement("<a href=\""+projRelativePath.toString()+"\"><img src=\"../"+projNameLowercase+"/images/small.png\" align=\"centre\"/></a>", "td");
                //indexPage.addRawHtml("</br>");
                //indexPage.addRawHtml("</br>");

                //indexPage.addRawHtml("<img src=\""+projNameStripped+"/images/small.png\"/>");

                /*File smallImg = new File(projFile.getParentFile(), "images/small.png");
                if (smallImg.exists()) {
                    File smallImgInDoc = new File(projSpecificDir, "images");
                    File smallImgInDocRelative = new File(projNameStripped, "images/small.png");
                    if (!smallImgInDoc.exists()) {
                        smallImgInDoc.mkdir();
                    }
                    GeneralUtils.copyFileIntoDir(smallImg, smallImgInDoc);
                    indexPage.addRawHtml("<img src=\"" + smallImgInDocRelative.toString() + "\" align=\"right\">");
                } else {
                    System.out.println("Project image not found at " + smallImg.toString());
                }*/

                indexPage.addRawHtml("</tr>");

                logger.logComment("Created a doc at: " + f.getCanonicalPath(), true);


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        indexPage.addRawHtml("</table>");
        indexPage.saveAsFile(fileToSave);

    }

    public static String generateSimplePage(String title, String html_elem_1) {
        SimpleHtmlDoc mainPage = new SimpleHtmlDoc(title, fontSize);
        mainPage.addToHead(ccsInfo);
        mainPage.addRawHtml("</br>");
        mainPage.addRawHtml("<img src=\""+html_elem_1+"\" align=\"centre\"/>");
        mainPage.addRawHtml("</br>");

        return mainPage.toHtmlString();
    }

    public static File generateMainPage(Project project, File dirToCreateIn) {
        String mainPageTitle = project.getProjectFileName();

        ArrayList<String> pages = new ArrayList<String>();
        pages.add(mainPageTitle);

        GeneralUtils.removeAllFiles(dirToCreateIn, false, false, false);

        for (String sc : project.simConfigInfo.getAllSimConfigNames()) {
            pages.add(sc);
        }



        for (String title : pages) {
            boolean isProjSummaryPage = title.equals(mainPageTitle);
            
            File fileToSave = new File(dirToCreateIn, getItemPage(title));

            SimpleHtmlDoc mainPage = new SimpleHtmlDoc(project.getProjectName() + ": " + title, fontSize);
            
            mainPage.addToHead(ccsInfo);

            //mainPage.addTaggedElement("" + project.getProjectName(), "h2");
            

            mainPage.addLinkedText("<b>"+project.getProjectName()+"</b>", getItemPage(mainPageTitle));

            if (!isProjSummaryPage)
            {
                mainPage.addRawHtml(" / ");
                mainPage.addLinkedText("Sim Config: "+title, getItemPage(title));
            }

            
            if (isProjSummaryPage)
                mainPage.addTaggedElement("<img src=\"images/large.png\" width=\"600\" />", "p");

            mainPage.addBreak();
            
            String desc = project.getProjectDescription();

            if (isProjSummaryPage)
            {
                mainPage.addBreak();
                mainPage.addTaggedElement("Description", "h3");

                mainPage.addRawHtml(handleWhitespaces(GeneralUtils.parseForHyperlinks(desc)));
                mainPage.addBreak();
            }

            ArrayList<Cell> cells = new ArrayList<Cell>();

            ArrayList<String> cellMechs = new ArrayList<String>();
            ArrayList<String> cellGroups = new ArrayList<String>();
            ArrayList<String> netConns = new ArrayList<String>();
            ArrayList<String> inputs = new ArrayList<String>();
            ArrayList<String> plots = new ArrayList<String>();

            if (isProjSummaryPage) {

                cells.addAll(project.cellManager.getAllCells());

                cellMechs.addAll(project.cellMechanismInfo.getAllCellMechanismNames());

                cellGroups = project.cellGroupsInfo.getAllCellGroupNames();

                netConns.addAll(project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames());
                netConns.addAll(project.volBasedConnsInfo.getAllAAConnNames());

                inputs.addAll(project.elecInputInfo.getAllStimRefs());

                plots.addAll(project.simPlotInfo.getAllSimPlotRefs());
            } 
            else
            {
                SimConfig sc = project.simConfigInfo.getSimConfig(title);
                desc = sc.getDescription();
                //cells.removeAllElements();
                //cellMechs.removeAllElements();

                cellGroups = sc.getCellGroups();
                netConns = sc.getNetConns();
                inputs = sc.getInputs();
                plots = sc.getPlots();

                for (String cg : sc.getCellGroups()) {
                    String cellType = project.cellGroupsInfo.getCellType(cg);
                    Cell cell = project.cellManager.getCell(cellType);

                    if (!cells.contains(cell)) {
                        cells.add(cell);
                    }

                    ArrayList<String> cms = cell.getAllChanMechNames(true);

                    cms.addAll(cell.getAllAllowedSynapseTypes());
                    for (String cm : cms) {
                        if (!cellMechs.contains(cm)) {
                            cellMechs.add(cm);
                        }
                    }

                }
            }
            cellMechs = (ArrayList<String>)GeneralUtils.reorderAlphabetically(cellMechs, true);
            cellGroups = (ArrayList<String>)GeneralUtils.reorderAlphabetically(cellGroups, true);
            netConns = (ArrayList<String>)GeneralUtils.reorderAlphabetically(netConns, true);
            inputs = (ArrayList<String>)GeneralUtils.reorderAlphabetically(inputs, true);
            cells = (ArrayList<Cell>)GeneralUtils.reorderAlphabetically(cells, true);
            plots = (ArrayList<String>)GeneralUtils.reorderAlphabetically(plots, true);


            if (isProjSummaryPage) {
                mainPage.addTaggedElement("Simulation Configurations", "h3");

                for (String sc : project.simConfigInfo.getAllSimConfigNames()) {
                    String scFile = getItemPage(sc);
                    mainPage.addTaggedElement(mainPage.getLinkedText(sc, scFile), "b");
                    /*cellPage.addTaggedElement(mainPage.getLinkedText(sc, scFile), "b");
                    chanPage.addTaggedElement(mainPage.getLinkedText(sc, scFile), "b");
                    xmlPage.addTaggedElement(mainPage.getLinkedText(sc, scFile), "b");*/
                    String scDesc = project.simConfigInfo.getSimConfig(sc).getDescription();
                    mainPage.addRawHtml("<i>"+handleWhitespaces(scDesc)+"</i>");
                    mainPage.addBreak();

                }
            }

            mainPage.addBreak();

            if (!isProjSummaryPage)
                mainPage.addTaggedElement("Summary of Simulation Configuration: "+title, "h3");


            if (!isProjSummaryPage && !netConns.isEmpty()) {

                if (netConns.size()>=150)
                {
                    if (!isProjSummaryPage)
                        mainPage.addTaggedElement("(Connectivity Matrix not generated, "+netConns.size()+" network connections...)", "i");
                }
                else
                {
                    //if (!isProjSummaryPage)
                    //    mainPage.addTaggedElement("Connectivity Matrix", "h3");

                    String connTitle = "Connectivity matrix of: " + title;
                    if (isProjSummaryPage) {
                        connTitle = "Connectivity in " + SimConfigInfo.DEFAULT_SIM_CONFIG_NAME;
                    }


                    ArrayList<String> orderedCellGroups = new ArrayList<String>();
                    String gap = "_____";

                    for (String cg : cellGroups) {
                        Region reg = project.regionsInfo.getRegionObject(project.cellGroupsInfo.getRegionName(cg));
                        orderedCellGroups.add((reg.getHighestYValue() + 100000) + gap + cg);
                    }

                    orderedCellGroups = (ArrayList<String>) GeneralUtils.reorderAlphabetically(orderedCellGroups, false);

                    for (int i = 0; i < orderedCellGroups.size(); i++) {
                        String old = orderedCellGroups.get(i);
                        orderedCellGroups.set(i, old.substring(old.indexOf(gap) + gap.length()));
                    }

                    String mFilename = getItemPage(connTitle);
                    File mFile = new File(fileToSave.getParentFile(), mFilename);

                    mainPage.addTaggedElement(mainPage.getLinkedText(connTitle, mFilename), "b");

                    SimpleHtmlDoc matrixPage = new SimpleHtmlDoc(connTitle, fontSize);

                    matrixPage.addTaggedElement(connTitle, "h2");


                    matrixPage.addRawHtml("<table "+tableStyle+">");

                    matrixPage.addRawHtml("<tr>");
                    matrixPage.addRawHtml("<td   colspan='2'>&nbsp;</td>");

                    for (String preCG : orderedCellGroups) {
                        String preRef = preCG;

                        if (preCG.startsWith("CG3D_")) {
                            preRef = preCG.substring(5);
                        }
                        matrixPage.addRawHtml("<td "+headerStyle+"  colspan='2'>" + preRef + "</td>");
                    }
                    matrixPage.addRawHtml("</tr>");
                    for (String postCG : orderedCellGroups) {
                        matrixPage.addRawHtml("<tr>");
                        String postRef = postCG;

                        if (postCG.startsWith("CG3D_")) {
                            postRef = postCG.substring(5);
                        }
                        matrixPage.addRawHtml("<td "+headerStyle+"  colspan='2'>" + postRef + "</td>");

                        for (String preCG : orderedCellGroups) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("<table>");
                            for (String nc : netConns) {
                                String src = project.morphNetworkConnectionsInfo.getSourceCellGroup(nc);
                                String tgt = project.morphNetworkConnectionsInfo.getTargetCellGroup(nc);
                                if (preCG.equals(src) && postCG.equals(tgt)) {
                                    String info = getNetConnInfo(project, nc);
                                    String bgCol = "";
                                    String fgCol = " style=\"color:#FFFFFF\"";

                                    if (info.toUpperCase().indexOf("AMPA") >= 0 || info.toUpperCase().indexOf("NMDA") >= 0 || info.toUpperCase().indexOf("EXC") >= 0 || info.toUpperCase().indexOf("DOUBEXPSYN") >= 0) {
                                        bgCol = "bgcolor=\"" + COLOUR_AMPA + "\" " + fgCol;
                                        //info = "<font color=\""+COLOUR_AMPA+"\">"+info+"</font>";
                                    } else if (info.toUpperCase().indexOf("GABA") >= 0 || info.toUpperCase().indexOf("INH") >= 0) {
                                        bgCol = "bgcolor=\"" + COLOUR_GABA + "\" " + fgCol;
                                        //info = "<font color=\""+COLOUR_GABA+"\">"+info+"</font>";
                                    } else if (info.toUpperCase().indexOf("GAP") >= 0 || info.indexOf("ELECT") >= 0) {
                                        bgCol = "bgcolor=\"" + COLOUR_GAP + "\" " + fgCol;
                                        //info = "<font color=\""+COLOUR_GAP+"\">"+info+"</font>";
                                    }
                                    sb.append("<tr><td " + bgCol + ">" + info + "</td></td>");
                                }
                            }

                            sb.append("</table>");
                            matrixPage.addRawHtml("<td   colspan='2'>" + sb.toString() + "</td>");
                        }
                        matrixPage.addRawHtml("</tr>");
                    }
                    matrixPage.addRawHtml("</table>");

                    mainPage.addRawHtml("<p>&nbsp;</p>");


                    matrixPage.saveAsFile(mFile);
                }
            }

            int width = 700;
            int width1 = 140;

            if (isProjSummaryPage)
            {

                mainPage.addTaggedElement("Cells in this project", "h3");
                for (Cell cell : cells) {
                    String cellPageLoc = getCellTypePage(cell.getInstanceName());
                    String ref = isProjSummaryPage ? cellPageLoc : "#"+cell.getInstanceName();
                    String link = "<a href = \"" + ref + "\"><b>" + cell.getInstanceName() + "</b></a>";

                    if (!cell.equals(cells.get(cells.size() - 1))) {
                        link = link +", ";
                    }
                    mainPage.addRawHtml(link);
                }

                mainPage.addBreak();
                mainPage.addBreak();

                mainPage.addTaggedElement("Ion channels in this project", "h3");
                for (String cmName : cellMechs) {

                    CellMechanism cm = project.cellMechanismInfo.getCellMechanism(cmName);

                    String cmPageLoc = getCellMechPage(cm.getInstanceName());
                    String link = "<b><a href = \"" + cmPageLoc + "\">" + cm.getInstanceName() + "</a></b>";

                    if (!cmName.equals(cellMechs.get(cellMechs.size() - 1))) {
                        link = link +", ";
                    }
                    if (cm.isChannelMechanism())
                        mainPage.addRawHtml(link);
                }

                mainPage.addBreak();
                mainPage.addBreak();

                mainPage.addTaggedElement("Synapses in this project", "h3");
                for (String cmName : cellMechs) {

                    CellMechanism cm = project.cellMechanismInfo.getCellMechanism(cmName);


                    String cmPageLoc = getCellMechPage(cm.getInstanceName());
                    String link = "<b><a href = \"" + cmPageLoc + "\">" + cm.getInstanceName() + "</a></b>";

                    if (!cmName.equals(cellMechs.get(cellMechs.size() - 1))) {
                        link = link +", ";
                    }
                    if (cm.isGapJunctionMechanism() || cm.isSynapticMechanism())
                        mainPage.addRawHtml(link);
                }

                mainPage.addBreak();
                mainPage.addBreak();
            }


            if (!isProjSummaryPage)
            {

                mainPage.addRawHtml("<table "+tableStyle+">");

                mainPage.addRawHtml("<tr><td "+headerStyle+"  colspan='2'><b>A: Model Summary</b></td></tr>");
                mainPage.addRawHtml("<tr><td width='" + width1 + "'><b>Description</b></td><td>" + handleWhitespaces(GeneralUtils.parseForHyperlinks(desc)) + "</td></tr>");
                mainPage.addRawHtml("<tr><td><b>Populations</b></td><td>");

                for (String cg : cellGroups) {
                    String cellType = project.cellGroupsInfo.getCellType(cg);
                    String cellPageLoc = getCellTypePage(cellType);
                    String ref = isProjSummaryPage ? cellPageLoc : "#"+cg;
                    String link = "<a href=\"" + ref + "\">" + cg + "</a>";

                    if (!cg.equals(cellGroups.get(cellGroups.size() - 1))) {
                        link = link +", ";
                    }
                    mainPage.addRawHtml(link);
                }

                mainPage.addRawHtml("</td></tr>");
                mainPage.addRawHtml("<tr><td><b>Topology</b></td><td>Network of neurons positioned & connected in 3D space</td></tr>");

                mainPage.addRawHtml("<tr><td><b>Connectivity</b></td><td>");
                if (netConns.isEmpty()) {
                    mainPage.addRawHtml("No network connections in this Simulation Configuration");
                }

                for (String nc : netConns) {
                    mainPage.addRawHtml("<a href=\"#" + nc + "\">" + nc + "</a>");
                    if (!nc.equals(netConns.get(netConns.size() - 1))) {
                        mainPage.addRawHtml(", ");
                    }
                }
                mainPage.addRawHtml("</td></tr>");


                mainPage.addRawHtml("<tr><td><b>Neuron models</b></td><td>");
            }
            for (Cell cell : cells) {
                //mainPage.addRawHtml(cell.getInstanceName());

                String cellPageLoc = getCellTypePage(cell.getInstanceName());
                String ref = isProjSummaryPage ? cellPageLoc : "#"+cell.getInstanceName();
                String link =  "<a href = \"" + ref + "\">" + cell.getInstanceName() + "</a>";

                if (!cell.equals(cells.get(cells.size() - 1))) {
                    link = link +", ";
                }
                if (!isProjSummaryPage)
                    mainPage.addRawHtml(link);

                SimpleHtmlDoc cellPage = new SimpleHtmlDoc(project.getProjectName() + ": " + title, fontSize);
                SimpleHtmlDoc nml1Page = new SimpleHtmlDoc(project.getProjectName() + ": " + title, fontSize);
                SimpleHtmlDoc nml2Page = new SimpleHtmlDoc(project.getProjectName() + ": " + title, fontSize);

                String nml1PageLoc = getCellTypePage(cell.getInstanceName() + ".morph");
                File nml1PageFile = new File(fileToSave.getParentFile(), nml1PageLoc);

                String nml2PageLoc = getCellTypePage(cell.getInstanceName() + ".nml");
                File nml2PageFile = new File(fileToSave.getParentFile(), nml2PageLoc);

                ArrayList<SimpleHtmlDoc> versions = new ArrayList<SimpleHtmlDoc>();
                versions.add(cellPage);
                versions.add(nml1Page);
                versions.add(nml2Page);

                for (SimpleHtmlDoc page: versions){

                    page.addLinkedText("<b>"+project.getProjectName()+"</b>", "../"+getItemPage(mainPageTitle));

                    page.addRawHtml(" / ");

                    page.addLinkedText("Cell: "+ cell.getInstanceName(), "../"+getCellTypePage(cell.getInstanceName()));

                    page.addBreak();
                    page.addBreak();

                    page.setTitle("Cell: " + cell.getInstanceName());


                    page.addLinkedText("Summary", "../"+cellPageLoc);
                    page.addRawHtml(" | ");
                    page.addLinkedText("NeuroML v1.8.1", "../"+nml1PageLoc);
                    page.addRawHtml(" | ");
                    page.addLinkedText("NeuroML v2.0", "../"+nml2PageLoc);

                    String suffix = "";
                    if (project.projProperties.getPreferredSaveFormat().equals(ProjectStructure.JAVA_OBJ_FORMAT))
                        suffix = ProjectStructure.getJavaObjFileExtension();
                    if (project.projProperties.getPreferredSaveFormat().equals(ProjectStructure.JAVA_XML_FORMAT))
                        suffix = ProjectStructure.getJavaXMLFileExtension();
                    if (project.projProperties.getPreferredSaveFormat().equals(ProjectStructure.NEUROML1_FORMAT))
                        suffix = ProjectStructure.getNeuroMLFileExtension();

                    String repoLoc = "../../../projects/"+project.getProjectName()+"/repository/changes/neuroConstruct/morphologies/"+
                            cell.getInstanceName()+suffix+"?rev=master";
                    page.addRawHtml(" | ");

                    page.addLinkedText("Source in repository", repoLoc);

                    page.addBreak();
                    page.addBreak();
                }

                //cellPage.addTaggedElement("Details of cell: "+cell.getInstanceName(), "h3");

                cellPage.addRawHtml(CellTopologyHelper.printDetails(cell, project, true, true, false, true, true));

                File cellFile = new File(fileToSave.getParentFile(), cellPageLoc);
                if (!cellFile.exists()) {
                    cellPage.saveAsFile(cellFile);
                }

                int maxSeg = 200;
                try
                {

                    if (cell.getAllSegments().size() > maxSeg)
                    {
                        nml1Page.addRawHtml("Cell: " + cell.getInstanceName()+" has "+cell.getAllSegments().size()+" segments.<br/><br/>"
                                + "This is too large to display as NeruoML v1.x. <br/><br/>"
                                + "Please clone the project and/or download neuroConstruct to view the file and visualise the morphology. ");
                    }
                    else
                    {
                        String nml1 = MorphMLConverter.getCellInNeuroMLFormat(cell, project, NeuroMLLevel.NEUROML_LEVEL_3, NeuroMLVersion.NEUROML_VERSION_1, true);
                        nml1Page.addBreak();
                        nml1Page.addRawHtml(preXML);
                        nml1Page.addRawHtml(nml1);
                        nml1Page.addRawHtml(postXML);
                    }
                }
                catch (MorphologyException ex) {

                    nml1Page.addTaggedElement("Unable to generate HTML representation of: " + cell.getInstanceName(), "b");

                }
                try
                {
                    if (cell.getAllSegments().size() > maxSeg)
                    {
                        nml2Page.addRawHtml("Cell: " + cell.getInstanceName()+" has "+cell.getAllSegments().size()+" segments.<br/><br/>"
                                + "This is too large to display as NeuroML v2.x. <br/><br/>"
                                + "Please clone the project and/or download neuroConstruct to view the file and visualise the morphology. ");
                    }
                    else
                    {
                        String nml2 = MorphMLConverter.getCellInNeuroMLFormat(cell, project, NeuroMLLevel.NEUROML_VERSION_2_SPIKING_CELL, NeuroMLVersion.NEUROML_VERSION_2_BETA, true);
                        nml2Page.addBreak();
                        nml2Page.addRawHtml(preXML);
                        nml2Page.addRawHtml(nml2);
                        nml2Page.addRawHtml(postXML);
                    }
                }
                catch (MorphologyException ex) {

                    nml2Page.addTaggedElement("Unable to generate HTML representation of: " + cell.getInstanceName()+"\n"+ex.getMessage(), "b");
                    ex.printStackTrace();

                }

                if (!nml1PageFile.exists()) {
                    nml1Page.saveAsFile(nml1PageFile);
                }

                if (!nml2PageFile.exists()) {
                    nml2Page.saveAsFile(nml2PageFile);
                }



            }
            if (!isProjSummaryPage)
                mainPage.addRawHtml("</td></tr>");


            StringBuffer cmInfo = new StringBuffer();
            StringBuffer synInfo = new StringBuffer();

            //cellM

            for (String cmName : cellMechs) {
                StringBuffer mechInfo = cmInfo;

                CellMechanism cm = project.cellMechanismInfo.getCellMechanism(cmName);

                if (cm==null)
                    logger.logError("Problem with "+cmName+" in project "+ project.getProjectName(), true);

                if (cm.isSynapticMechanism() || cm.isGapJunctionMechanism()) {
                    mechInfo = synInfo;
                }

                String cmPageLoc = getCellMechPage(cm.getInstanceName());
                File cmPageFile = new File(fileToSave.getParentFile(), cmPageLoc);

                mechInfo.append("<a href = \"" + cmPageLoc + "\">" + cm.getInstanceName() + "</a>");

                if (!cmName.equals(cellMechs.get(cellMechs.size() - 1))) {
                    mechInfo.append(", ");
                }

                File xslDoc = GeneralProperties.getChannelMLReadableXSL();

                SimpleHtmlDoc summaryPage = new SimpleHtmlDoc(project.getProjectName() + ": " + title, fontSize);
                SimpleHtmlDoc channelmlPage = new SimpleHtmlDoc(project.getProjectName() + ": " + title, fontSize);
                SimpleHtmlDoc nml2Page = new SimpleHtmlDoc(project.getProjectName() + ": " + title, fontSize);

                String cmXmlPageLoc = getCellMechPage(cm.getInstanceName() + ".channelml");
                File cmXmlPageFile = new File(fileToSave.getParentFile(), cmXmlPageLoc);

                String nml2PageLoc = getCellMechPage(cm.getInstanceName() + ".nml");
                File nml2PageFile = new File(fileToSave.getParentFile(), nml2PageLoc);


                ArrayList<SimpleHtmlDoc> versions = new ArrayList<SimpleHtmlDoc>();
                versions.add(summaryPage);
                versions.add(channelmlPage);
                versions.add(nml2Page);

                for (SimpleHtmlDoc page: versions){
                    //page.addTaggedElement("" + project.getProjectName(), "h2");
                    page.setTitle(cm.getMechanismType()+": " + cm.getInstanceName());
                    //page.addTaggedElement("Details of "+cm.getMechanismType()+": "+cm.getInstanceName(), "h3");

                    page.addLinkedText("<b>"+project.getProjectName()+"</b>", "../"+getItemPage(mainPageTitle));

                    page.addRawHtml(" / ");

                    page.addLinkedText(""+ cm.getInstanceName(), "../"+getCellMechPage(cm.getInstanceName()));

                    page.addBreak();
                    page.addBreak();

                    page.addLinkedText("Summary", "../"+cmPageLoc);
                    if (cm instanceof ChannelMLCellMechanism) {
                        page.addRawHtml(" | ");
                        page.addLinkedText("ChannelML", "../"+cmXmlPageLoc);
                        page.addRawHtml(" | ");
                        page.addLinkedText("NeuroML v2.0", "../"+nml2PageLoc);

                        ChannelMLCellMechanism cmlCm = (ChannelMLCellMechanism) cm;
                        String repoLoc = "../../../projects/"+project.getProjectName()+"/repository/changes/neuroConstruct/cellMechanisms/"+
                                cm.getInstanceName()+"/"+cmlCm.getXMLFile()+"?rev=master";
                        page.addRawHtml(" | ");

                        page.addLinkedText("Source in repository", repoLoc);
                    }
                    page.addBreak();
                    page.addBreak();
                }

                if (cm instanceof ChannelMLCellMechanism) {
                    ChannelMLCellMechanism cmlCm = (ChannelMLCellMechanism) cm;


                    if (!cmXmlPageFile.exists() || !cmPageFile.exists() || !nml2PageFile.exists()) {
                        logger.logComment("Writing ChannelML files for "+cmName, true);
                        try {
                            String readable = XMLUtils.transform(cmlCm.getXMLDoc().getXMLString("", false), xslDoc);

                            summaryPage.addRawHtml(readable);
                        } catch (XMLMechanismException e) {
                            summaryPage.addTaggedElement("Unable to generate HTML representation of: " + cm.getInstanceName(), "b");
                        }

                        channelmlPage.setTitle("Cell Mechanism: " + cm.getInstanceName() + " in ChannelML");
                        channelmlPage.addTaggedElement("Cell Mechanism: " + cm.getInstanceName() + " in ChannelML", "h3");
                        channelmlPage.addBreak();

                        channelmlPage.addRawHtml(preXML);

                        try {
                            String cmlString = cmlCm.getXMLDoc().getXMLString("", true);
                            channelmlPage.addRawHtml(cmlString);
                        } catch (XMLMechanismException e) {
                            channelmlPage.addTaggedElement("Unable to generate ChannelML representation of: " + cm.getInstanceName(), "b");
                        }

                        channelmlPage.addRawHtml(postXML);

                        nml2Page.setTitle("Cell Mechanism: " + cm.getInstanceName() + " in NeuroML v2.0");
                        nml2Page.addTaggedElement("Cell Mechanism: " + cm.getInstanceName() + " in NeuroML v2.0", "h3");

                        nml2Page.addRawHtml(preXML);

                        try {

                            String nml2string = XMLUtils.transform(cmlCm.getXMLDoc().getXMLString("", false),
                                                                 ProjectStructure.getChannelML2NeuroML2beta());

                            SimpleXMLDocument nml2Doc = SimpleXMLReader.getSimpleXMLDoc(nml2string);


                            nml2Page.addRawHtml(nml2Doc.getXMLString("", true));

                        } catch (Exception e) {
                            nml2Page.addTaggedElement("Unable to generate NeuroML v2.0 representation of: " + cm.getInstanceName(), "b");
                        }

                        nml2Page.addRawHtml(postXML);

                        channelmlPage.saveAsFile(cmXmlPageFile);
                        summaryPage.saveAsFile(cmPageFile);
                        nml2Page.saveAsFile(nml2PageFile);
                    }



                } else if (cm instanceof FileBasedMembraneMechanism) {
                    FileBasedMembraneMechanism fm = (FileBasedMembraneMechanism)cm;
                    for (MechanismImplementation mi: fm.getMechanismImpls())
                    {
                        summaryPage.addTaggedElement("Implementation of channel "+cm.getInstanceName()+" in <b>"+mi.getSimulationEnvironment()+"</b>", "h3");

                        String contents = GeneralUtils.readShortFile(mi.getImplementingFileObject(project, cmName));

                        summaryPage.addRawHtml(handleWhitespaces(contents));
                        summaryPage.saveAsFile(cmPageFile);
                    }
                }
                else {
                    //...
                }

            }
            String cmString = cmInfo.toString();
            if (cmString.endsWith(", "))
                cmString = cmString.substring(0, cmString.length()-3);
            String synString = synInfo.toString();
            if (synString.endsWith(", "))
                synString = synString.substring(0, synString.length()-3);

            if (!isProjSummaryPage)
            {
                mainPage.addRawHtml("<tr><td><b>Channel models</b></td><td>" + cmString + "</td></tr>");
                if (synInfo.length() == 0) {
                    synInfo.append("No synapses present in this Simulation Configuration");
                }
                mainPage.addRawHtml("<tr><td><b>Synapse models</b></td><td>" + synString + "</td></tr>");


                mainPage.addRawHtml("<tr><td><b>Inputs</b></td><td>");

                for (String in : inputs) {
                    String cg = project.elecInputInfo.getStim(in).getCellGroup();

                    mainPage.addRawHtml(" <a href=\"#" + in + "\">" + in + "</a> (to <a href=\"#" + cg + "\">" + cg + "</a>)");
                }
                mainPage.addRawHtml("</td></tr>");

                mainPage.addRawHtml("</table>");


                mainPage.addRawHtml("<p>&nbsp;</p>");
            }

            if (!isProjSummaryPage) {

                mainPage.addRawHtml("<table "+tableStyle+">");

                mainPage.addRawHtml("<tr><td "+headerStyle+"  colspan='3'><b>B: Populations</b></td></tr>");

                mainPage.addRawHtml("<tr><td width='" + width1 + "'><b>Name</b></td>"
                        + "<td  width='100'><b>Elements</b></td>"
                        + "<td><b>Description</b></td></tr>");

                for (String cg : cellGroups) {

                    String cellType = project.cellGroupsInfo.getCellType(cg);
                    String cellPageLoc = getCellTypePage(cellType);

                    String regionName = project.cellGroupsInfo.getRegionName(cg);
                    Region region = project.regionsInfo.getRegionObject(regionName);
                    CellPackingAdapter cpa = project.cellGroupsInfo.getCellPackingAdapter(cg);

                    mainPage.addRawHtml("<tr><td>" + cg + "<a name=\"" + cg + "\"/></td>"
                            + "<td><a href = \"" + cellPageLoc + "\">" + project.cellGroupsInfo.getCellType(cg) + "</a></td>"
                            + "<td>" + cpa.toNiceString() + "<br>"
                            + "In region (" + regionName + "): " + region.toString() + "</td></tr>");
                }


                mainPage.addRawHtml("</table>");

                mainPage.addRawHtml("<p>&nbsp;</p>");

                mainPage.addRawHtml("<table "+tableStyle+">");

                mainPage.addRawHtml("<tr><td "+headerStyle+"  colspan='4'><b>C: Connectivity</b></td></tr>");


                mainPage.addRawHtml("<tr><td width='" + width1 + "'><b>Name</b></td>"
                        + "<td  width='100'><b>Source</b></td>"
                        + "<td  width='100'><b>Target</b></td>"
                        + "<td><b>Pattern</b></td></tr>");

                if (netConns.isEmpty()) {
                    mainPage.addRawHtml("<tr>"
                            + "<td colspan='4'>No network connections in this Simulation Configuration</td>"
                            + "</tr>");
                }

                for (String nc : netConns) {
                    String src = project.morphNetworkConnectionsInfo.getSourceCellGroup(nc);
                    String tgt = project.morphNetworkConnectionsInfo.getTargetCellGroup(nc);
                    //ConnectivityConditions cc = project.morphNetworkConnectionsInfo.getConnectivityConditions(nc);

                    mainPage.addRawHtml("<tr>"
                            + "<td>" + nc + "<a name=\"" + nc + "\"/></td>"
                            + "<td><a href=\"#" + src + "\">" + src + "</a></td>"
                            + "<td><a href=\"#" + tgt + "\">" + tgt + "</td>"
                            + "<td>" + getNetConnInfo(project, nc) + "</td>"
                            + "</tr>");

                }
                mainPage.addRawHtml("</table>");



                mainPage.addRawHtml("<p>&nbsp;</p>");


                mainPage.addRawHtml("<table "+tableStyle+">");

                mainPage.addRawHtml("<tr><td "+headerStyle+" colspan='4'>D: Neuron and Synapse models</b></tr>");

                mainPage.addRawHtml("<tr><td width='" + width1 + "'><b>Name</b></td>"
                        + "<td ><b>Description</b></td>"
                        + "<td  width='50'><b>Details</b></td></tr>");

                for (Cell cell : cells) {
                    String cellPageLoc = getCellTypePage(cell.getInstanceName());

                    mainPage.addRawHtml("<tr>"
                            + "<td><a href=\"" + cellPageLoc + "\" name=\"" + cell.getInstanceName() + "\">" + cell.getInstanceName() + "</a></td>"
                            + "<td>" + cell.getCellDescription() + "</a></td>"
                            + "<td><a href=\"" + cellPageLoc + "\">More...</td>"
                            + "</tr>");

                }

                mainPage.addRawHtml("</table>");


                mainPage.addRawHtml("<p>&nbsp;</p>");


                mainPage.addRawHtml("<table "+tableStyle+">");

                mainPage.addRawHtml("<tr><td "+headerStyle+" colspan='4'>E: Inputs</b></tr>");

                mainPage.addRawHtml("<tr><td width='" + width1 + "'><b>Name</b></td>"
                        + "<td width='250'><b>Description</b></td>"
                        + "</tr>");

                if (inputs.isEmpty())
                {
                    mainPage.addRawHtml("<tr>"
                            + "<td colspan='2'>No electrical inputs present</td>"
                            + "</tr>");
                }

                for (String input: inputs)
                {
                    StimulationSettings stim = project.elecInputInfo.getStim(input);
                    mainPage.addRawHtml("<tr>"
                            + "<td>" + input + "<a name=\"" + input + "\"></a></td>"
                            + "<td>" + stim + " on " + stim.getSegChooser().toNiceString() + " of <a href=\"#" + stim.getCellGroup() + "\">" + stim.getCellGroup() + "</a></a></td>"
                            + "</tr>");
                }

                mainPage.addRawHtml("</table>");


                mainPage.addRawHtml("<p>&nbsp;</p>");


                mainPage.addRawHtml("<table "+tableStyle+">");

                mainPage.addRawHtml("<tr><td "+headerStyle+" colspan='4'>F: Measurements</b></tr>");

                mainPage.addRawHtml("<tr><td width='" + width1 + "'><b>Name</b></td>"
                        + "<td width='250'><b>Description</b></td>"
                        + "</tr>");

                for (String plot: plots)
                {
                    SimPlot sp = project.simPlotInfo.getSimPlot(plot);
                    String info = sp.getPlotSaveString();
                    String toPlot = sp.getValuePlotted();
                    if (toPlot.indexOf(":")>0)
                    {
                        String mech = toPlot.substring(0,toPlot.indexOf(":"));
                        String cmPageLoc = getCellMechPage(mech);
                        toPlot = "<a href=\"" + cmPageLoc + "\">"+mech+"</a>"+toPlot.substring(toPlot.indexOf(":"));
                    }

                    info = info + " " + toPlot+ " on seg "+sp.getSegmentId() +" of cell(s) "+ sp.getCellNumber()
                            +" in <a href=\"#" + sp.getCellGroup() + "\">" + sp.getCellGroup() + "</a>";

                    mainPage.addRawHtml("<tr>"
                            + "<td>" + plot + "</td>"
                            + "<td>" + info + "</a></td>"
                            + "</tr>");
                }


                mainPage.addRawHtml("</table>");
                
            }


            logger.logComment("Going to save: " + fileToSave.getAbsolutePath(), true);
            mainPage.saveAsFile(fileToSave);
        }

        return new File(dirToCreateIn, getItemPage(mainPageTitle));

    }

    public static String handleWhitespaces(String text) {
        return GeneralUtils.replaceAllTokens(text, "\n", "<br/>");
    }

    public static void main(String[] args) {

        //String osbLocal =

        ArrayList<String> paths = new ArrayList<String>();
        //paths.add("examples/Ex6-Cerebellum/Ex6-Cerebellum.neuro.xml");
        //paths.add("nCmodels/Thalamocortical/Thalamocortical.ncx");

        paths.add("osb/cerebellum/cerebellar_nucleus_cell/CerebellarNucleusNeuron/neuroConstruct/CerebellarNucleusNeuron.ncx");
        paths.add("osb/cerebellum/networks/GranCellLayer/neuroConstruct/GranCellLayer.ncx");
        paths.add("osb/hippocampus/CA1_pyramidal_neuron/CA1PyramidalCell/neuroConstruct/CA1PyramidalCell.ncx");
            paths.add("osb/invertebrate/celegans/CElegansNeuroML/CElegans/CElegans.ncx");

        //paths.add("models/LarkumEtAl2009/LarkumEtAl2009.ncx");

        boolean all = false;

        all = true;
        if (all)
        {
            paths.add("osb/cerebellum/cerebellar_granule_cell/GranCellSolinasEtAl10/neuroConstruct/GranCellSolinasEtAl10.ncx");

            paths.add("osb/cerebral_cortex/neocortical_pyramidal_neuron/MainenEtAl_PyramidalCell/neuroConstruct/MainenEtAl_PyramidalCell.ncx");
            paths.add("osb/cerebral_cortex/neocortical_pyramidal_neuron/RothmanEtAl_KoleEtAl_PyrCell/neuroConstruct/RothmanEtAl_KoleEtAl_PyrCell.ncx");
            paths.add("osb/cerebellum/cerebellar_purkinje_cell/PurkinjeCell/neuroConstruct/PurkinjeCell.ncx");
            paths.add("osb/cerebellum/networks/VervaekeEtAl-GolgiCellNetwork/neuroConstruct/VervaekeEtAl-GolgiCellNetwork.ncx");
            paths.add("osb/cerebral_cortex/networks/Thalamocortical/neuroConstruct/Thalamocortical.ncx");

            paths.add("osb/cerebellum/cerebellar_granule_cell/GranuleCell/neuroConstruct/GranuleCell.ncx");
            paths.add("osb/cerebellum/cerebellar_golgi_cell/SolinasEtAl-GolgiCell/neuroConstruct/SolinasEtAl-GolgiCell.ncx");
            paths.add("osb/cerebellum/cerebellar_granule_cell/GranuleCellVSCS/neuroConstruct/GranuleCellVSCS.ncx");
            
        }

        //paths.add("nCmodels/SolinasEtAl_GolgiCell/SolinasEtAl-GolgiCell.ncx");
        //paths.add("nCexamples/Ex4_HHcell/Ex4_HHcell.ncx");
        //paths.add("/bernal/models/Layer23_names/Layer23_names.neuro.xml");
        //paths.add("../copyNcModels/Parallel/Parallel.neuro.xml");
        //paths.add("nCmodels/Thalamocortical/Thalamocortical.ncx");
        //paths.add("nCexamples/Ex6_CerebellumDemo/Ex6_CerebellumDemo.ncx");
        //paths.add("nCexamples/Ex5_Networks/Ex5_Networks.ncx");
        //paths.add("/home/eugenio/phd/code/osb/add_more_models/osbModels/PurkinjeCell/PurkinjeCell.ncx");

        paths = (ArrayList<String>) GeneralUtils.reorderAlphabetically(paths, true);
        generateModelDescriptions(paths, "/home/svnsvn/doc/nCGenerated");

    }
}
