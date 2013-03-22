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

package ucl.physiol.neuroconstruct.website;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.ArrayList;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.OriginalCompartmentalisation;
import ucl.physiol.neuroconstruct.utils.xml.*;

/**
 * Class for generating HTML representation of some of the example neuroConstruct projects
 *
 * @author Padraig Gleeson
 *  
 */

public class ExampleProjects
{
    private static ClassLogger logger = new ClassLogger("ExampleProjects");
    
    
    public ExampleProjects()
    {
    }



    public static void generateMainPage(File mainFile, File sourceProjDir) throws IOException, ProjectFileParsingException
    {
        SimpleXMLElement root = new SimpleXMLElement("document");
        SimpleXMLElement header = new SimpleXMLElement("header");
        root.addChildElement(header);

        SimpleXMLElement title = new SimpleXMLElement("title");
        header.addChildElement(title);


        SimpleXMLElement body = new SimpleXMLElement("body");
        root.addChildElement(body);
        SimpleXMLElement intro = new SimpleXMLElement("p");
        body.addChildElement(intro);
        
        if(!mainFile.getParentFile().exists())
            mainFile.getParentFile().mkdir();
        
        File targetDownloadDir = new File(mainFile.getParentFile(), "downloads");
        if(!targetDownloadDir.exists())
            targetDownloadDir.mkdir();
            
        
        if (sourceProjDir.getName().indexOf("examples")>=0)
        {
            title.addContent("neuroConstruct example projects");
        
            intro.addContent( "Downloadable neuroConstruct example projects. These <strong>illustrate the core " +
            "functionality of neuroConstruct</strong>, as opposed to providing electrophysiologically accurate " +
            "models. Projects based on published conductance based models can be found <a href=\"../models/index.html\">here</a>");
        }
        if (sourceProjDir.getName().indexOf("models")>=0)
        {
            title.addContent("neuroConstruct projects based on published neuronal and network models");
            
            intro.addContent("Downloadable neuroConstruct projects <strong>based on published conductance based models</strong>. " +
            "Some examples to illustrate the core functionality of neuroConstruct, as opposed to " +
            "providing electrophysiologically accurate models can be found <a href=\"../samples/index.html\">here</a>."
                    + "<p>Note: These models are currently being moved to a repository to allow open source, collaborative development of NeuroML models.</p>"
                    + "<p>See the <a href=\"http://www.opensourcebrain.org\">Open Source Brain</a> website for full details.&nbsp;&nbsp;&nbsp;&nbsp;"
                    + "<img alt=\"Open Source Brain\" src=\"http://www.opensourcebrain.org/images/logo.png\"/></p>");
        }
        File[] fileArray = sourceProjDir.listFiles();

        fileArray = GeneralUtils.reorderAlphabetically(fileArray, true);
        
        ArrayList<File> files = GeneralUtils.toArrayList(fileArray);
        //if (files.contains(""))


        ArrayList<String> toIgnore = new ArrayList<String>();
        //toIgnore.add("Thalamocortical"); // temporarily
        //toIgnore.add("CA1PyramidalCell"); // temporarily
        //toIgnore.add("SolinasEtAl_GolgiCell"); // temporarily
        
        for(File exProjDir: files)
        {
            File morphDir = new File(exProjDir, "cellMechanisms");
            
            if (morphDir.isDirectory() && !toIgnore.contains(exProjDir.getName()))
            {
                String projName = exProjDir.getName();
                SimpleXMLElement section = new SimpleXMLElement("section");
                body.addChildElement(section);
                
                SimpleXMLElement secTitle = new SimpleXMLElement("title");
                section.addChildElement(secTitle);
                secTitle.addContent(projName);
                
                SimpleXMLElement anchor = new SimpleXMLElement("anchor");
                section.addChildElement(anchor);
                anchor.addAttribute("id", projName);
                
                SimpleXMLElement table = new SimpleXMLElement("table");
                
                section.addChildElement(table);
                
                SimpleXMLElement row = new SimpleXMLElement("tr");
                table.addChildElement(row);
                
                
                String largeImg = "large.png";
                String smallImg = "small.png";
                
                File targetImageDir = new File(mainFile.getParentFile(), "images");
                if (!targetImageDir.exists())
                    targetImageDir.mkdir();
                
                File targetProjImageDir = new File(targetImageDir, projName);
                
                if (!targetProjImageDir.exists())
                    targetProjImageDir.mkdir();
                
                File smallImgFile = new File(exProjDir, "images/"+smallImg);
                File largeImgFile = new File(exProjDir, "images/"+largeImg);
                
                if (smallImgFile.exists())
                {
                    GeneralUtils.copyFileIntoDir(smallImgFile, targetProjImageDir);
                    
                    SimpleXMLElement col2 = new SimpleXMLElement("td");
                    row.addChildElement(col2);
                    col2.addAttribute("width","120");
                                    
                    SimpleXMLElement secImg = new SimpleXMLElement("p");
                    col2.addChildElement(secImg);
                    
                    SimpleXMLElement img = new SimpleXMLElement("img");
                    img.addAttribute("src", "images/"+projName+"/small.png");
                    img.addAttribute("alt", "Screenshot of "+projName);
                    
                    if(largeImgFile.exists())
                    {
                        GeneralUtils.copyFileIntoDir(largeImgFile, targetProjImageDir);
                        
                        SimpleXMLElement imgRef = new SimpleXMLElement("a");
                        img.addAttribute("title", "Click to enlarge");
                        imgRef.addAttribute("href", "images/"+projName+"/"+largeImg);  
                        imgRef.addChildElement(img);
                        secImg.addChildElement(imgRef);
                    }
                    else
                    {
                        secImg.addChildElement(img);
                    }
                }
                
                SimpleXMLElement secIntro = new SimpleXMLElement("p");
                SimpleXMLElement colMid = new SimpleXMLElement("td");
                SimpleXMLElement colRight = new SimpleXMLElement("td");
                row.addChildElement(colMid);
                row.addChildElement(colRight);
                colRight.addAttribute("width","150");
                colMid.addChildElement(secIntro);
                secIntro.addContent("Project name: <strong>"+ projName+"</strong>");
                
                File projFile = ProjectStructure.findProjectFile(exProjDir);
                
                Project project = Project.loadProject(projFile, null);
                String descFull = project.getProjectDescription();
                String breakpoint = "\n\n";
                String descShort = new String(descFull);
                
                
                if (descFull.indexOf(breakpoint)>0)
                {
                    descShort = descFull.substring(0, descFull.indexOf(breakpoint));
                }
                
                SimpleXMLElement desc = new SimpleXMLElement("p");
                colMid.addChildElement(desc);
                desc.addContent(GeneralUtils.parseForHyperlinks(descShort));
                
                SimpleXMLElement modified = new SimpleXMLElement("p");
                colMid.addChildElement(modified);
                
                SimpleDateFormat formatter = new SimpleDateFormat("EEEE MMMM d, yyyy");

                java.util.Date date = new java.util.Date(projFile.lastModified());
                
                modified.addContent("Project last modified: "+ formatter.format(date));
                
                File zipFile = null;
                String zipFileName = targetDownloadDir.getAbsolutePath()+"/"+ projName+ProjectStructure.getNewProjectZipFileExtension();
         
                ArrayList<String> ignore = new ArrayList<String> ();
                ArrayList<String> ignoreNone = new ArrayList<String> ();
                ArrayList<String> ignoreExtns = new ArrayList<String> ();

                ignore.add("i686");
                ignore.add("x86_64");
                ignore.add(".svn");
                ignore.add("simulations");
                ignore.add("generatedNEURON");
                ignore.add("generatedNeuroML");
                ignore.add("generatedGENESIS");
                ignore.add("generatedMOOSE");
                ignore.add("generatedPyNN");
                ignore.add("generatedPSICS");
                ignore.add("dataSets");
                ignoreExtns.add("bak");

                zipFile = ZipUtils.zipUp(exProjDir, zipFileName, ignore, ignoreExtns);

                logger.logComment("The zip file: "+ zipFile.getAbsolutePath() + " ("+zipFile.length()+" bytes)  contains all of the project files");
             
                
                SimpleXMLElement downloads = new SimpleXMLElement("p");
                colRight.addChildElement(downloads);
                downloads.addContent("Downloads<a href=\"#downloadInfo\">*</a>:");
                
                SimpleXMLElement downloadProj = new SimpleXMLElement("p");
                colRight.addChildElement(downloadProj);
                
                SimpleXMLElement link = new SimpleXMLElement("a");
                link.addAttribute("href", "downloads/"+zipFile.getName());  
                link.addContent("neuroConstruct project");
                link.addAttribute("title", "Download full project for loading into neuroConstruct");
                downloadProj.addChildElement(link);
                
                ArrayList<String> noNeuroML = new ArrayList<String>();
                noNeuroML.add("Ex3_Morphology");
                noNeuroML.add("DentateGyrus");
                noNeuroML.add("RothmanEtAl_KoleEtAl_PyrCell");
                
                if(!noNeuroML.contains(projName))
                {
                    project.neuromlFileManager.generateNeuroMLFiles(null,  new OriginalCompartmentalisation(), 1234, false);

                    File neuroMLDir = ProjectStructure.getNeuroMLDir(project.getProjectMainDirectory());

                    String nmlZipFileName = targetDownloadDir.getAbsolutePath()+"/"+ projName+"_NeuroML.zip";

                    zipFile = ZipUtils.zipUp(neuroMLDir, nmlZipFileName, ignoreNone, ignoreNone);

                    SimpleXMLElement downloadNml = new SimpleXMLElement("p");
                    colRight.addChildElement(downloadNml);
                    //downloadNml.addContent("Download project as pure NeuroML: ");

                    SimpleXMLElement img = new SimpleXMLElement("img");
                    img.addAttribute("src", "../images/NeuroMLSmall.png");
                    String info = "Download core project elements in NeuroML format";
                    img.addAttribute("alt", info);


                    SimpleXMLElement imgRef = new SimpleXMLElement("a");
                    img.addAttribute("title", info);
                    imgRef.addAttribute("href", "downloads/"+zipFile.getName());  
                    imgRef.addChildElement(img);

                    downloadNml.addChildElement(imgRef);
                }

                
                
            }
        }

        
        
        SimpleXMLElement end = new SimpleXMLElement("p");
        body.addChildElement(end);
        end.addContent("&nbsp;");
        
        SimpleXMLElement infoDlanchor = new SimpleXMLElement("anchor");
        body.addChildElement(infoDlanchor);
        end.addAttribute("id", "downloadInfo");
        
        SimpleXMLElement infoDl = new SimpleXMLElement("p");
        body.addChildElement(infoDl);
        end.addContent("* Note: neuroConstruct project downloads (most of which are included with the standard software distribution) " +
            "can be loaded directly into neuroConstruct to generate cell and network scripts for NEURON, GENESIS, etc.," +
            " but NeuroML downloads just consist of the core elements of the project" +
            " (morphologies, channels, etc.) which have been exported in NeuroML format. The latter can be useful for testing NeuroML compliant applications. " +
            "If no NeuroML download link is present, this usually indicates that the model is mainly implemented using channel/synapse mechanisms in a simulator's " +
            "native language (e.g. mod files) which have not fully been converted to ChannelML yet.");
        
        
        SimpleXMLElement end2 = new SimpleXMLElement("p");
        body.addChildElement(end2);
        end2.addContent("&nbsp;");


        FileWriter fw = null;
        try
        {
            
            fw = new FileWriter(mainFile);
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");  // quick hack, todo: add to SimpleXMLDoc...

            fw.write("<!DOCTYPE document PUBLIC \"-//APACHE//DTD Documentation V2.0//EN\" \"http://forrest.apache.org/dtd/document-v20.dtd\">\n\n");
            fw.write(root.getXMLString("", false));


            fw.flush();
            fw.close();

        }
        catch (IOException ex)
        {
            logger.logError("Problem: ",ex);
            fw.close();          

        }
         
         /*
           <header>
    <title>Examples of neuroConstruct in use</title>
  </header>
  <body>
      <p>Some screenshots of neuroConstruct in action are given below.
      Click on the thumbnails to see a full size version of the screenshots</p>

    <section>
      <title>Examples included with distribution</title>*/
        
    }
    
    public static String handleWhitespaces(String text)
    {
        return GeneralUtils.replaceAllTokens(text, "\n", "<br/>");
    }

    public static void main(String[] args)
    {
        new ExampleProjects();
        
        try
        {
            
            File exsDir = new File("nCexamples");
            
            File mainFile = new File("docs/XML/xmlForHtml/samples/index.xml");
            
            logger.logComment("Going to create docs at: "+ mainFile.getCanonicalPath(), true);
            
            
            generateMainPage(mainFile, exsDir);
            
            logger.logComment("Created doc at: "+ mainFile.getCanonicalPath(), true);
            
            File modelsDir = new File("nCmodels");
            mainFile = new File("docs/XML/xmlForHtml/models/index.xml");
            
            
            generateMainPage(mainFile, modelsDir);

            logger.logComment("Created doc at: "+ mainFile.getCanonicalPath(), true);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}
