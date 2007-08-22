/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2007 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
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
