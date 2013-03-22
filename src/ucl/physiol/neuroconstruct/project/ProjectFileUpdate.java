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
 * Class used to update project files when there is a change in the names of classes used,
 * e.g. class named CellProcess changed to CellMechanism
 *
 * @author Padraig Gleeson
 *  
 */

public class ProjectFileUpdate
{
    private static ClassLogger logger = new ClassLogger("ProjectFileUpdate");

    private static Hashtable<String, String> replacements = new Hashtable<String, String>();

    static
    {

        addReplacement("ucl.physiol.neuroconstruct.processes.AbstractedCellProcess",
                       "ucl.physiol.neuroconstruct.mechanisms.AbstractedCellMechanism");

        addReplacement("ucl.physiol.neuroconstruct.processes.CellProcess",
                       "ucl.physiol.neuroconstruct.mechanisms.CellMechanism");

        addReplacement("ucl.physiol.neuroconstruct.processes.ChannelMLCellProcess",
                       "ucl.physiol.neuroconstruct.mechanisms.ChannelMLCellMechanism");

        addReplacement("ucl.physiol.neuroconstruct.processes.CellProcessInfo",
                       "ucl.physiol.neuroconstruct.mechanisms.CellMechanismInfo");

        addReplacement("ucl.physiol.neuroconstruct.processes.SimXSLMapping",
                       "ucl.physiol.neuroconstruct.mechanisms.SimXSLMapping");

        addReplacement("ucl.physiol.neuroconstruct.processes.SynapticProcess",
                       "ucl.physiol.neuroconstruct.mechanisms.SynapticMechanism");

        addReplacement("ucl.physiol.neuroconstruct.processes.Exp2SynProcess",
                       "ucl.physiol.neuroconstruct.mechanisms.Exp2SynMechanism");

        addReplacement("ucl.physiol.neuroconstruct.processes.ExpSynProcess",
                       "ucl.physiol.neuroconstruct.mechanisms.ExpSynMechanism");

        addReplacement("ucl.physiol.neuroconstruct.processes.FileBasedMembraneProcess",
                       "ucl.physiol.neuroconstruct.mechanisms.FileBasedMembraneMechanism");

        addReplacement("ucl.physiol.neuroconstruct.processes.ProcessImplementation",
                       "ucl.physiol.neuroconstruct.mechanisms.MechanismImplementation");

        addReplacement("ucl.physiol.neuroconstruct.processes.PassiveMembraneProcess",
                       "ucl.physiol.neuroconstruct.mechanisms.PassiveMembraneMechanism");

        addReplacement("ucl.physiol.neuroconstruct.processes.HHMembraneProcess",
                       "ucl.physiol.neuroconstruct.mechanisms.HHMembraneMechanism");

        addReplacement("ucl.physiol.neuroconstruct.processes.KChannelProcess",
                       "ucl.physiol.neuroconstruct.mechanisms.KChannelMechanism");

        addReplacement("ucl.physiol.neuroconstruct.processes.NaChannelProcess",
                       "ucl.physiol.neuroconstruct.mechanisms.NaChannelMechanism");

        addReplacement("ucl.physiol.neuroconstruct.processes.TabHHProcess",
                       "ucl.physiol.neuroconstruct.mechanisms.TabHHMechanism");


        addReplacement("void property=\"allCellProcesses\"",
                       "void property=\"allCellMechanisms\"");

        addReplacement("void property=\"processModel\"",
                       "void property=\"mechanismModel\"");

        addReplacement("void property=\"processType\"",
                       "void property=\"mechanismType\"");

        addReplacement("void property=\"processImpls\"",
                       "void property=\"mechanismImpls\"");



    }


    private static void addReplacement(String oldToken, String newToken)
    {
        logger.logComment("Adding rep for: "+ oldToken);
        replacements.put(oldToken, newToken);
    }


    /**
     * Updates project files when there is a change in the names of classes used,
     * e.g. class named CellProcess changed to CellMechanism
     */
    public static boolean updateProjectFile(File projFile) throws FileNotFoundException, IOException
    {
        Reader in = null;

        StringBuffer newFileString = new StringBuffer();

        in = new FileReader(projFile);

        BufferedReader lineReader = new BufferedReader(in);
        String nextLine = null;

        // As an empty line is produced by the toString of the Elements
        int lineNumber = 0;

        boolean updatePerformed = false;

        boolean userWarned = false;


        while ( (nextLine = lineReader.readLine()) != null)
        {
            lineNumber++;

            //logger.logComment("Looking at line number: " + lineNumber + " (" + nextLine + ")");

            Enumeration<String> oldTokens = replacements.keys();

            while (oldTokens.hasMoreElements())
            {
                String nextOldToken = oldTokens.nextElement();

                //logger.logComment("Looking to replace "+nextOldToken);

                if (nextLine.length() >= nextOldToken.length() &&  // elims some lines...
                    nextLine.indexOf(nextOldToken)>=0)
                {
                    String newToken = replacements.get(nextOldToken);

                    if (!userWarned)
                    {
                        String zipFileName = projFile.getParentFile()
                            + System.getProperty("file.separator")
                            + projFile.getName().substring(0, projFile.getName().indexOf("."))
                            + ProjectStructure.getNewProjectZipFileExtension()
;
                        int goAhead = JOptionPane.showConfirmDialog(null,

                        "Updating the project file format to work with a newer version of neuroConstruct (v"+GeneralProperties.getVersionNumber()+"). This update cannot be undone.\n"
                                                                    +"Do you wish to continue? Note: the old project files will be archived in the file: \n"+zipFileName,
                                                                    "Warning",
                                                                    JOptionPane.YES_NO_OPTION);

                        if (goAhead==JOptionPane.NO_OPTION)
                        {
                            return false;
                        }

                        ProjectManager.zipDirectoryContents(projFile.getParentFile(), zipFileName);

                        userWarned = true;

                    }

                    nextLine = GeneralUtils.replaceAllTokens(nextLine, nextOldToken, newToken);
                    updatePerformed = true;

                    logger.logComment("Replaced with line: "+ nextLine);
                }
            }

            newFileString.append(nextLine+"\n");
        }

        //logger.logComment("New file contents: "+newFileString);

        if (updatePerformed)
        {
            FileWriter fw = new FileWriter(projFile);
            fw.write(newFileString.toString());
            fw.close();
        }
        return true;
    }


    public static void main(String[] args)
    {
        //ProjectFileUpdate projectfileupdate = new ProjectFileUpdate();

        File f = new File("projects/ProcMech/ProcMech.neuro.xml");

        try
        {
            updateProjectFile(f);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
