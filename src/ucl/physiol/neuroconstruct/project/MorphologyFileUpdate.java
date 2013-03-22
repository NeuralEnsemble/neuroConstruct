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

import java.io.*;
import java.util.*;

import javax.swing.*;

import ucl.physiol.neuroconstruct.utils.*;

/**
 * Class used to update morphology files when there is a change in the names of classes/fields used
 *
 * @author Padraig Gleeson
 *  
 */

public class MorphologyFileUpdate
{
    private static ClassLogger logger = new ClassLogger("MorphologyFileUpdate");

    private static Hashtable<String, String> replacements = new Hashtable<String, String>();

    static
    {


        // to handle move from global cm to specific capacitance defined by groups
        addReplacement("void property=\"specCapacitance\"",
                       "void property=\"oldGlobalSpecCapacitance\"");



        // to handle move from global ra to specific ax res defined by groups
        addReplacement("void property=\"specAxRes\"",
                       "void property=\"oldGlobalSpecAxRes\"");



    }


    private static void addReplacement(String oldToken, String newToken)
    {
        logger.logComment("Adding rep for: "+ oldToken);
        replacements.put(oldToken, newToken);
    }

    public static boolean updateMorphologyFile(File morphFile) throws FileNotFoundException, IOException
    {
        Reader in = null;

        StringBuffer newFileString = new StringBuffer();

        in = new FileReader(morphFile);

        BufferedReader lineReader = new BufferedReader(in);
        String nextLine = null;

        // As an empty line is produced by the toString of the Elements
        int lineNumber = 0;

        boolean userWarned = false;


        while ( (nextLine = lineReader.readLine()) != null)
        {
            lineNumber++;

            Enumeration<String> oldTokens = replacements.keys();

            while (oldTokens.hasMoreElements())
            {
                String nextOldToken = oldTokens.nextElement();

                if (nextLine.length() >= nextOldToken.length() &&  // elims some lines...
                    nextLine.indexOf(nextOldToken)>=0)
                {
                    String newToken = replacements.get(nextOldToken);

                    if (!userWarned)
                    {
                        String zipFileName = morphFile.getParentFile()
                            + System.getProperty("file.separator")
                            + morphFile.getName().substring(0, morphFile.getName().indexOf("."))
                            + ".zip";

                        int goAhead = JOptionPane.showConfirmDialog(null,

                        "Updating the morphology file format to work with a newer version of neuroConstruct (v"+GeneralProperties.getVersionNumber()+"). This update cannot be undone.\n"
                                                                    +"Do you wish to continue? Note: the old morphology files will be archived in the file: \n"+zipFileName,
                                                                    "Warning",
                                                                    JOptionPane.YES_NO_OPTION);

                        if (goAhead==JOptionPane.NO_OPTION)
                        {
                            return false;
                        }

                        //ProjectManager.zipDirectoryContents(morphFile.getParentFile(), zipFileName);
                        ArrayList<String> ignore = new ArrayList<String> ();
                        ArrayList<String> ignoreExtn = new ArrayList<String> ();
                        ignoreExtn.add("zip");

                        ZipUtils.zipUp(morphFile.getParentFile(), zipFileName, ignore, ignoreExtn);


                        userWarned = true;

                    }

                    nextLine = GeneralUtils.replaceAllTokens(nextLine, nextOldToken, newToken);

                    logger.logComment("Replaced with line: "+ nextLine);
                }
            }

            newFileString.append(nextLine+"\n");
        }


        FileWriter fw = new FileWriter(morphFile);
        fw.write(newFileString.toString());
        fw.close();

        return true;
    }


    public static void main(String[] args)
    {
        //ProjectFileUpdate projectfileupdate = new ProjectFileUpdate();

        File f = new File("projects/ProcMech/ProcMech.neuro.xml");

        try
        {
            updateMorphologyFile(f);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
