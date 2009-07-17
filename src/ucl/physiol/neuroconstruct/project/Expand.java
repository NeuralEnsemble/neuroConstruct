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

import ucl.physiol.neuroconstruct.mechanisms.CellMechanism;
import ucl.physiol.neuroconstruct.mechanisms.ChannelMLCellMechanism;
import ucl.physiol.neuroconstruct.mechanisms.ChannelMLException;
import ucl.physiol.neuroconstruct.utils.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Vector;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import ucl.physiol.neuroconstruct.mechanisms.XMLMechanismException;

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

    public static File generateMainPage(Project project, File parentFile)
    {
        String mainPageTitle = "index";

        ArrayList<String> pages = new ArrayList<String>();
        pages.add(mainPageTitle);

        for (String sc: project.simConfigInfo.getAllSimConfigNames())
            pages.add(sc);

        for(String title: pages)
        {
            File fileToSave = new File(parentFile, getItemPage(title));
            
            SimpleHtmlDoc mainPage = new SimpleHtmlDoc();

            mainPage.setMainFontSize(10);

            mainPage.addTaggedElement("neuroConstruct project: "+ project.getProjectName(), "h1");

            mainPage.addTaggedElement("Simulation configurations", "h2");
            
            
            for (String sc: project.simConfigInfo.getAllSimConfigNames())
            {
                String scFile = getItemPage(sc);
                mainPage.addTaggedElement(mainPage.getLinkedText(sc, scFile), "b");
            }

            String desc = project.getProjectDescription();

            Vector<Cell> cells = new Vector<Cell>();

            Vector<String> cellMechs = new Vector<String>();

            if(title.equals(mainPageTitle))
            {
                cells = project.cellManager.getAllCells();

                cellMechs = project.cellMechanismInfo.getAllCellMechanismNames();
            }
            else
            {
                SimConfig sc = project.simConfigInfo.getSimConfig(title);
                desc=sc.getDescription();
                //cells.removeAllElements();
                //cellMechs.removeAllElements();

                for(String cg: sc.getCellGroups())
                {
                    String cellType = project.cellGroupsInfo.getCellType(cg);
                    Cell cell = project.cellManager.getCell(cellType);

                    System.out.println("cg: "+ cg);
                    System.out.println("cellType: "+ cellType);
                    System.out.println("celz: "+ project.cellManager.getAllCellTypeNames());
                    System.out.println("Cell: "+ cell);
                    
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
                
            mainPage.addTaggedElement(""+ handleWhitespaces(desc), "p");
 

            mainPage.addTaggedElement("Cell Types present in the project", "h2");



            mainPage.addRawHtml("<table border=\"1\" >");

            for (Cell cell: cells)
            {
                mainPage.addRawHtml("<tr><td>"+cell.getInstanceName());
                mainPage.addRawHtml("</td><td>"+cell.getCellDescription()+"</td><td>");

                String cellPageLoc = getCellTypePage(cell.getInstanceName());
                mainPage.addRawHtml("<a href = "+cellPageLoc	+">Cell details</a></td></tr>");


                SimpleHtmlDoc cellPage = new SimpleHtmlDoc();

                cellPage.addRawHtml(CellTopologyHelper.printDetails(cell, project, true, true, true));


                cellPage.saveAsFile(new File(fileToSave.getParentFile(), cellPageLoc));

            }
            mainPage.addRawHtml("</table>");

            mainPage.addTaggedElement("Cell Mechanisms present in the project", "h2");

            mainPage.addRawHtml("<table border=\"1\">");


            for (String cmName: cellMechs)
            {
                CellMechanism cm = project.cellMechanismInfo.getCellMechanism(cmName);


                String cmPageLoc = getCellMechPage(cm.getInstanceName());

                mainPage.addRawHtml("<tr><td>"+cm.getInstanceName());
                mainPage.addRawHtml("</td><td>"+cm.getDescription()+"</td><td>");


                mainPage.addRawHtml("</td><td>"+cm.getMechanismModel()+"</td><td>");
                mainPage.addRawHtml("</td><td>"+cm.getMechanismType()+"</td><td>");

                mainPage.addRawHtml("<a href = "+cmPageLoc	+">Full details</a></td></tr>");

                File xslDoc = GeneralProperties.getChannelMLReadableXSL();



                SimpleHtmlDoc cmPage = new SimpleHtmlDoc();

                if (cm instanceof ChannelMLCellMechanism)
                {
                    ChannelMLCellMechanism cmlCm = (ChannelMLCellMechanism)cm;


                    try
                    {
                        String readable = XMLUtils.transform(cmlCm.getXMLDoc().getXMLString("", false),xslDoc);

                        cmPage.addRawHtml(readable);
                    }
                    catch (XMLMechanismException e)
                    {
                        cmPage.addTaggedElement("Unable to generate HTML representation of: "+ cm.getInstanceName(), "b");
                    }
                    String cmXmlPageLoc = getCellMechPage(cm.getInstanceName()+".channelml");

                    SimpleHtmlDoc cmXmlPage = new SimpleHtmlDoc();


                    try
                    {
                        cmXmlPage.addRawHtml(cmlCm.getXMLDoc().getXMLString("", true));
                    }
                    catch (XMLMechanismException e)
                    {
                        cmXmlPage.addTaggedElement("Unable to generate ChannelML representation of: "+ cm.getInstanceName(), "b");
                    }

                    cmXmlPage.saveAsFile(new File(fileToSave.getParentFile(), cmXmlPageLoc));

                    mainPage.addRawHtml("</td><td><a href = "+cmXmlPageLoc	+">ChannelML file</td><td>");
                }
                else
                {

                }


                cmPage.saveAsFile(new File(fileToSave.getParentFile(), cmPageLoc));

            }

            mainPage.addRawHtml("</table>");
            logger.logComment("Going to save: "+ fileToSave.getAbsolutePath(), true);
            mainPage.saveAsFile(fileToSave);
        }
        return new File(getItemPage(mainPageTitle));
        
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
            File projFile = new File("nCmodels/CA1PyramidalCell/CA1PyramidalCell.ncx");
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
            

            logger.logComment("Created doc at: "+ f.getCanonicalPath(), true);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}
