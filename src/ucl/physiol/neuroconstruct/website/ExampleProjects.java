/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.website;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

import java.io.*;
import java.text.SimpleDateFormat;
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



    public static void generateMainPage(File mainFile) throws IOException, ProjectFileParsingException
    {
        SimpleXMLElement root = new SimpleXMLElement("document");
        SimpleXMLElement header = new SimpleXMLElement("header");
        root.addChildElement(header);

        SimpleXMLElement title = new SimpleXMLElement("title");
        header.addChildElement(title);

        title.addContent("neuroConstruct example projects");

        SimpleXMLElement body = new SimpleXMLElement("body");
        root.addChildElement(body);
        SimpleXMLElement intro = new SimpleXMLElement("p");
        body.addChildElement(intro);
        
        File targetDownloadDir = new File(mainFile.getParentFile(), "downloads");
        if(!targetDownloadDir.exists())
            targetDownloadDir.mkdir();

        intro.addContent("Downloadable neuroConstruct example projects");
        
        File exsDir = new File("nCexamples");
        for(File exProjDir: exsDir.listFiles())
        {
            File morphDir = new File(exProjDir, "cellMechanisms");
            
            if (morphDir.isDirectory())
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
                secIntro.addContent("Project name: "+ projName);
                
                File projFile = new File(exProjDir, projName+ProjectStructure.getProjectFileExtension());
                
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
                desc.addContent(descShort);
                
                SimpleXMLElement modified = new SimpleXMLElement("p");
                colMid.addChildElement(modified);
                
                SimpleDateFormat formatter = new SimpleDateFormat("EEEE MMMM d, yyyy");

                java.util.Date date = new java.util.Date(projFile.lastModified());
                
                modified.addContent("Project last modified: "+ formatter.format(date));
                
                File zipFile = null;
                String zipFileName = targetDownloadDir.getAbsolutePath()+"/"+ projName+ProjectStructure.getProjectZipFileExtension();
         
                ArrayList<String> ignore = new ArrayList<String> ();
                ArrayList<String> ignoreNone = new ArrayList<String> ();
                ignore.add("i686");
                ignore.add("x86_64");
                ignore.add(".svn");
                ignore.add("simulations");
                ignore.add("generatedNEURON");
                ignore.add("generatedNeuroML");
                ignore.add("generatedGENESIS");
                ignore.add("generatedPyNN");
                ignore.add("generatedPSICS");
                ignore.add("dataSets");

                zipFile = ZipUtils.zipUp(exProjDir, zipFileName, ignore, ignoreNone);

                logger.logComment("The zip file: "+ zipFile.getAbsolutePath() + " ("+zipFile.length()+" bytes)  contains all of the project files");
             
                
                SimpleXMLElement downloads = new SimpleXMLElement("p");
                colRight.addChildElement(downloads);
                downloads.addContent("Downloads:");
                
                SimpleXMLElement downloadProj = new SimpleXMLElement("p");
                colRight.addChildElement(downloadProj);
                
                SimpleXMLElement link = new SimpleXMLElement("a");
                link.addAttribute("href", "downloads/"+zipFile.getName());  
                link.addContent("neuroConstruct project");
                link.addAttribute("title", "Download full project for loading into neuroConstruct");
                downloadProj.addChildElement(link);
                
                ArrayList<String> noNeuroML = new ArrayList<String>();
                noNeuroML.add("Ex3_Morphology");
                
                if(!noNeuroML.contains(projName))
                {
                    project.neuromlPythonFileManager.generateNeuroMLFiles(null, 
                        new OriginalCompartmentalisation(), 1234, false);

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
            File mainFile = new File("docs/XML/xmlForHtml/projects/index.xml");
            

            logger.logComment("Going to create docs at: "+ mainFile.getCanonicalPath(), true);
            
           
            
            generateMainPage(mainFile);
            

            logger.logComment("Created doc at: "+ mainFile.getCanonicalPath(), true);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}
