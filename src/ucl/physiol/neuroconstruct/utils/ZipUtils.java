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

import java.io.*;
import java.util.*;
import java.util.zip.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Utilities for zipping files
 *
 * @author Padraig Gleeson
 *  
 */


public class ZipUtils
{

    static ClassLogger logger = new ClassLogger("ZipUtils");

    /**
     * Zips up the specified directory into the specified zip file...
     * @param sourceDir the directory containing all the files to zip
     * @param destZipFileName name of the zip file to create
     * @param ignoreFileDirs list of files/dirs to ignore
     * @param ignoreExtns list of extensions of files to ignore
     * @return File containing new zip file
     * @throws FileNotFoundException if a FileOutputStream cannot be created for the zip file
     * @throws IOException if the file cannot be written
     */
    public static File zipUp(File sourceDir,
                             String destZipFileName,
                             ArrayList<String> ignoreFileDirs,
                             ArrayList<String> ignoreExtns) throws FileNotFoundException, IOException
    {

        File destZipFile = new File(destZipFileName);
        logger.logComment("Adding contents of: "+ sourceDir+" to zip file: "+ destZipFile.getAbsolutePath());
        FileOutputStream dest = new FileOutputStream(destZipFileName);

        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

        if (destZipFile.getParentFile().getAbsolutePath().equals(sourceDir))
        {
            //Add the zip name
            ignoreFileDirs.add(destZipFile.getName());

        }

        addDirContents("", sourceDir.getAbsolutePath(), ignoreFileDirs,  ignoreExtns, out);

        out.close();

        logger.logComment("Just created zip file: "+ destZipFile.getCanonicalPath());

        return destZipFile;

    }


    private static void addDirContents(String relativeDirInZipFile,
                                       String sourceDir,
                                       ArrayList<String> ignoreFileDirs,
                                       ArrayList<String> ignoreExtns,
                                       ZipOutputStream out) throws FileNotFoundException, IOException
    {
        logger.logComment("Adding contents of: " + sourceDir + " to zip file directory: " + relativeDirInZipFile);

        int BUFFER = 2048;
        byte data[] = new byte[BUFFER];
        BufferedInputStream origin = null;

        File f = new File(sourceDir);
        File files[] = f.listFiles();


        for (int i = 0; i < files.length; i++)
        {
            if (files[i].isDirectory() && !ignoreFileDirs.contains(files[i].getName()))
            {
                addDirContents(relativeDirInZipFile + files[i].getName() +"/", /*Since windows can handle / */
                               files[i].getAbsolutePath(), ignoreFileDirs, ignoreExtns , out);
            }
            else
            {
                if ( !ignoreFileDirs.contains(files[i].getName())  &&
                     !files[i].getName().endsWith(ProjectStructure.getNewProjectZipFileExtension()) &&
                     !files[i].getName().endsWith(ProjectStructure.getOldProjectZipFileExtension()))
                {
                    boolean matchesExtn = false;
                    for (String extn: ignoreExtns)
                    {
                        if (files[i].getName().endsWith(extn)) matchesExtn = true;
                    }
                    if(!matchesExtn)
                    {

                        logger.logComment("Adding: " + files[i]);

                        FileInputStream fi = new FileInputStream(files[i]);
                        origin = new BufferedInputStream(fi, BUFFER);

                        ZipEntry entry = new ZipEntry(relativeDirInZipFile + files[i].getName());
                        out.putNextEntry(entry);
                        int count;
                        while ( (count = origin.read(data, 0,
                                                     BUFFER)) != -1)
                        {
                            out.write(data, 0, count);
                        }
                        origin.close();
                    }
                }
            }
        }
    }


    public static void zipSingleFile(File origFile, File destZipFile, String comment) throws FileNotFoundException, IOException
    {
        int BUFFER = 2048;
        byte data[] = new byte[BUFFER];
        BufferedInputStream origin = null;


        FileOutputStream dest = new FileOutputStream(destZipFile);

        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));


        logger.logComment("Zipping: " + origFile.getAbsolutePath());

        FileInputStream fi = new FileInputStream(origFile);
        origin = new BufferedInputStream(fi, BUFFER);

        ZipEntry entry = new ZipEntry(origFile.getName());
        out.putNextEntry(entry);
        int count;
        while ( (count = origin.read(data, 0,
                                     BUFFER)) != -1)
        {
            out.write(data, 0, count);
        }
        origin.close();

        if (comment!=null) out.setComment(comment);

        out.close();

    }


    public static void zipStringAsFile(String contents, File destZipFile, String internalFilename, String comment) throws FileNotFoundException, IOException
    {
        int BUFFER = 2048;
        byte data[] = new byte[BUFFER];
        BufferedInputStream origin = null;


        FileOutputStream dest = new FileOutputStream(destZipFile);

        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

        ByteArrayInputStream fi = new ByteArrayInputStream(contents.getBytes());
        origin = new BufferedInputStream(fi, BUFFER);


        ZipEntry entry = new ZipEntry(internalFilename);
        out.putNextEntry(entry);
        int count;
         while ( (count = origin.read(data, 0,
                                      BUFFER)) != -1)
         {
             out.write(data, 0, count);
         }
         origin.close();

         if (comment!=null) out.setComment(comment);

         out.close();


    }




    public static void unZip(String destDir, String zipFileName) throws FileNotFoundException, IOException
    {
        final int BUFFER = 2048;
        BufferedOutputStream dest = null;
        FileInputStream fis = new FileInputStream(zipFileName);

        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));

        ZipEntry entry;
        while ( (entry = zis.getNextEntry()) != null)
        {
            logger.logComment("Extracting: " + entry + ", directory? : " + entry.isDirectory());

            int count;
            byte data[] = new byte[BUFFER];

            File fileToCreate = new File(destDir + System.getProperty("file.separator") + entry.getName());

            logger.logComment("Creating file: " + fileToCreate.getAbsolutePath());

            boolean allSubDirsExist = false;
            File parentDir = fileToCreate.getParentFile();

            while (!allSubDirsExist)
            {

                boolean parentExists = parentDir.exists();
                logger.logComment("fileToCreate: "+fileToCreate+". Looking at file: " + parentDir + ". Exists? " + parentExists + ", is a directory? " +
                                   parentDir.isDirectory());

                if (parentExists) allSubDirsExist = true; // for now...

                while (!parentDir.exists())
                {
                    logger.logComment("Parent file: " + parentDir + " doesn't exist...");

                    try
                    {
                        parentDir.mkdirs();
                        logger.logComment("Just made: " + parentDir + ", is a directory? " + parentDir.isDirectory());

                    }
                    catch (Exception e)
                    {
                        logger.logComment("Failed to make: " + parentDir);
                        parentDir = parentDir.getParentFile();
                    }
                }
            }

            if (entry.isDirectory())
            {
                fileToCreate.mkdir();
            }
            else
            {
                FileOutputStream fos = new FileOutputStream(fileToCreate);
                dest = new BufferedOutputStream(fos, BUFFER);
                while ( (count = zis.read(data, 0, BUFFER)) != -1)
                {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
        }
        zis.close();
    }

}
