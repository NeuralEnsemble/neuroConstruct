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

package ucl.physiol.neuroconstruct.utils;

import java.beans.*;
import java.io.*;
import java.util.*;

/**
 * Handy class for storing recently opened files, and other goodies such as last directories in which
 * various files were opened.
 *
 * @author Padraig Gleeson
 *  
 */


public class RecentFiles
{
    private static ClassLogger logger = new ClassLogger("RecentFiles");

    private static Hashtable<String, RecentFiles> recentFilesInstances = new Hashtable<String, RecentFiles>();

    private LinkedList<String> fileList = new LinkedList<String>();
    private int maxFiles = 11;

    private File myRecentFilesFile = null;

    private String myLastMorphologiesDir = null;
    private String myLastCellProcessesDir = null;
    private String myLastExportPointsDir = null;

    /**
     * The interesting columns to show in the simulation browser
     */
    private Vector<String> preferredSimBrowserCols = null;

    /**
     * Don't use! Should be private, but XMLEncoder doesn't like that...
     */
    public RecentFiles()
    {
        //System.out.println("New RecentFiles created");
    }

    public void addToList(String fileName)
    {
        logger.logComment("Adding file: "+ fileName);
        File file = new File(fileName);
        fileName = file.getAbsolutePath();

        for (int i = 0; i < fileList.size(); i++)
        {
            String tempFile = fileList.get(i);
            if (tempFile.equals(fileName))
            {
                logger.logComment("File: "+ fileName+" already present");
                fileList.remove(tempFile);
                fileList.addFirst(fileName);
                return;
            }
            else
                logger.logComment("File: "+ fileName+" not equal to: "+ tempFile);
        }

        fileList.addFirst(fileName);
        if (fileList.size()> maxFiles)
        {
            fileList.removeLast();
        }
    }


    public void removeFromList(File f)
    {
        if (fileList.contains(f.getAbsolutePath()))
        {
            fileList.remove(f.getAbsolutePath());
            return;
        }
    }

    public String[] getFileNames()
    {
        /** @todo Use collections framework... */
        String[] files = new String[fileList.size()];
        for (int i = 0; i < fileList.size(); i++)
        {
            files[i] = fileList.get(i);
        }
        return files;
    }

    public void printDetails()
    {
        logger.logComment("RecentFiles: "+ fileList);
        logger.logComment("myLastMorphologiesDir: "+ myLastMorphologiesDir);
    }


    public void saveToFile()
    {
        try
        {
            /*
            FileWriter fw = new FileWriter(myRecentFilesFile);
            Object[] files = fileList.toArray();
            for (int i = 0; i < files.length; i++)
            {
                File nextFilename = (File)files[i];
                fw.write(nextFilename.getAbsolutePath()+"\n");
            }
            fw.flush();
            fw.close();
        */
           FileOutputStream fos = new FileOutputStream(myRecentFilesFile);
           BufferedOutputStream bos = new BufferedOutputStream(fos);
           XMLEncoder xmlEncoder = new XMLEncoder(bos);

           xmlEncoder.flush();
           String message = new String("\n<!-- This is a neuroConstruct file for storing information on recently opened files. It's best to open this\nfile with neuroConstruct, as opposed to editing it directly. -->\n\n");

           fos.write(message.getBytes());

           xmlEncoder.flush();
           /* -- Writing basic info -- */
           xmlEncoder.writeObject(this);

           xmlEncoder.close();

        }
        catch (Exception ex)
        {
            logger.logError("Error writing to recent files file: "+ myRecentFilesFile, ex);
        }
    }



    public static RecentFiles getRecentFilesInstance(String recentFilesFilename)
    {
        //System.out.println("Existing rec files: "+ recentFilesInstances);
        //System.out.println("Checking for "+ recentFilesFilename);

        if (recentFilesInstances.containsKey(recentFilesFilename))
        {
            logger.logComment("Using existing one");
            return recentFilesInstances.get(recentFilesFilename);
        }
        RecentFiles newRecentFiles = null;

        try
        {

            FileInputStream fis = new FileInputStream(recentFilesFilename);
            BufferedInputStream bis = new BufferedInputStream(fis);
            XMLDecoder xmlDecoder = new XMLDecoder(bis);

            Object nextReadObject = xmlDecoder.readObject();

            newRecentFiles = (RecentFiles)nextReadObject;

            newRecentFiles.myRecentFilesFile = new File(recentFilesFilename);

            if (newRecentFiles.fileList != null)
            {
                recentFilesInstances.put(recentFilesFilename, newRecentFiles);
                ///System.out.println("Existing recent files: "+ recentFilesInstances);

                return newRecentFiles;
            }
        }
        catch (Exception ex)
        {
            ///System.out.println("error: "+ex);
            // continue...
        }

        logger.logError("Error reading from recent files file: " + recentFilesFilename);

        newRecentFiles = new RecentFiles();
        newRecentFiles.myRecentFilesFile = new File(recentFilesFilename);

        // try loading up the contents of the examples dir...
/*
        File examplesDirectory = ProjectStructure.getExamplesDirectory();

        if (!examplesDirectory.exists() || !examplesDirectory.isDirectory())
        {
            logger.logComment("Problem with examples dir: " + examplesDirectory.getAbsolutePath());
        }
        File[] subDirs = examplesDirectory.listFiles();
        subDirs = GeneralUtils.reorderAlphabetically(subDirs, true);

        ArrayList<File> allExes = new ArrayList<File>();
        ArrayList<File> addAfter = new ArrayList<File>();

        for (int i = 0; i < subDirs.length; i++)
        {
            if (subDirs[i].getName().equals("Ex10-MainenEtAl"))
            {
                addAfter.add(subDirs[i]);
            }
            else if (subDirs[i].getName().equals("Ex11-3DDiffusion"))
            {
                addAfter.add(subDirs[i]);
            }


            else
            {
                allExes.add(subDirs[i]);
            } 
        }
        for (File f:addAfter) allExes.add(f);

        for (File f:allExes)
        {
            if (f.isDirectory())
            {
                String possProjName = f.getName() + ProjectStructure.getProjectFileExtension();
                //logger.logComment("Looking for: "+ possProjName);
                File[] subFiles = f.listFiles();
                for (int j = 0; j < subFiles.length; j++)
                {
                    if (subFiles[j].getName().equals(possProjName))
                    {
                        logger.logComment("Found: " + subFiles[j].getAbsolutePath());
                        newRecentFiles.fileList.addLast(subFiles[j].getAbsolutePath()); // due to the order they get put into the file...
                    }
                }

            }
        }


        recentFilesInstances.put(recentFilesFilename, newRecentFiles);
        ///System.out.println("Existing rec files: "+ recentFilesInstances);
*/
        return newRecentFiles;


    }

    static int count =0;
    static int goodCount =0;

    public static void main(String[] args)
    {
        File f = new File("src/ucl/physiol/neuroconstruct");
        try
        {

            updateCopyright(f);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        System.out.println("Dealt with "+count+" files");
        System.out.println("Correctly modified: "+goodCount+" files");

    }
    
    private static void updateCopyright(File file) throws FileNotFoundException, IOException
    {
        File[] sub = file.listFiles();
        
        
        String old = "/**\n * neuroConstruct\n *\n * Software for developing large scale 3D networks of biologically realistic neurons\n * Copyright (c) 2008 Padraig Gleeson\n * UCL Department of Physiology\n *\n * Development of this software was made possible with funding from the\n * Medical Research Council";
                
        String newSt = "/**\n *  neuroConstruct\n *  Software for developing large scale 3D networks of biologically realistic neurons\n * \n *  Copyright (c) 2009 Padraig Gleeson\n *  UCL Department of Neuroscience, Physiology and Pharmacology\n *\n *  Development of this software was made possible with funding from the\n *  " +
            "Medical Research Council and the Wellcome Trust\n *  \n *  This program is free software; you can redistribute it and/or modify\n *  it under the terms of the GNU General Public License as published by\n *  the Free Software Foundation; either version 2 of the License, or\n *  (at your option) any later version.\n " +
            "*  \n *  This program is distributed in the hope that it will be useful,\n *  but WITHOUT ANY WARRANTY; without even the implied warranty of\n *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n *  GNU General Public License for more details.\n\n *  You should have received a copy of the GNU General " +
            "Public License\n *  along with this program; if not, write to the Free Software\n *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA";

        for(File f: sub)
        {
            if (f.isDirectory())
            {
                updateCopyright(f);
            }
            if(f.getName().endsWith(".java"))
            {
                System.out.println("Looking at: "+f);
                
                FileReader in = new FileReader(f);
                
                BufferedReader lineReader = new BufferedReader(in);
                String nextLine = null;

                StringBuffer sb = new StringBuffer();

                int lineNumber = 0;

                while ( (nextLine = lineReader.readLine()) != null)
                {
                    lineNumber++;

                    sb.append(nextLine+"\n");

                }
                System.out.println("File was: "+lineNumber+" lines long");
                String oldCont = sb.toString();
                
                //if (sb.toString().startsWith("/**\n *  neuroConstruct\n *\n * Software for developing large scale 3D networks of biologically realistic neurons\n * Copyright (c) 2008 Padraig Gleeson\n * UCL Department of Physiology\n *\n * Development of this software was made possible with funding from the\n * Medical Research Council"))
                if (oldCont.startsWith(old))
                {
                    String cont = newSt + oldCont.substring(old.length());
                    FileWriter fw = new FileWriter(f);
                    fw.write(cont);
                    fw.close();
                    count++;
                }
                if (oldCont.startsWith(newSt))
                {
                    System.out.println("good...");
                    goodCount++;
                }
            }
        }
    }
    
    
    public String getMyLastMorphologiesDir()
    {
        return this.myLastMorphologiesDir;
    }

    public void setMyLastMorphologiesDir(String dir)
    {
        ///System.out.println("Setting MyLastMorphologiesDir to: "+ dir);
        this.myLastMorphologiesDir = new String(dir);

        //saveToFile();
    }

    public String getMyLastExportPointsDir()
    {
        return myLastExportPointsDir;
    }

    public void setMyLastExportPointsDir(String myLastExportPointsDir)
    {
        this.myLastExportPointsDir = myLastExportPointsDir;

        //saveToFile();
    }


    public void setFileNames(String[] files)
    {
        this.fileList = new LinkedList<String>();
        for (int i = 0; i < files.length; i++)
        {
            fileList.add(files[i]);
        }
    }
    public String getMyLastCellProcessesDir()
    {
        return myLastCellProcessesDir;
    }
    public void setMyLastCellProcessesDir(String myLastCellProcessesDir)
    {
        this.myLastCellProcessesDir = myLastCellProcessesDir;
        //saveToFile();
    }


    public Vector<String> getPreferredSimBrowserCols()
    {
        return preferredSimBrowserCols;
    }
    
    public void setPreferredSimBrowserCols(Vector<String> preferredCols)
    {
        this.preferredSimBrowserCols = preferredCols;
    }


}
