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

import javax.swing.table.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;
import java.io.*;
import ucl.physiol.neuroconstruct.nmodleditor.modfile.*;

/**
 * Extension of AbstractTableModel for storing Channel Mechanism info
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class ChannelMechanismInfo extends AbstractTableModel
{
    ClassLogger logger = new ClassLogger("ChannelMechanismInfo");

    public final static int COL_NUM_NAME = 0;
    public final static int COL_NUM_DESC = 1;
    public final static int COL_NUM_FILE = 2;
    public final static int COL_NUM_TYPE = 3;

    final String[] columnNames = new String[4];

    Vector allModFiles = new Vector();

    File myProjectNeuronCodeDirectory = null;
    File myNmodlSourceDirectory = null;

    public ChannelMechanismInfo()
    {
        columnNames[COL_NUM_NAME] = new String("Channel mechanism");
        columnNames[COL_NUM_DESC] = new String("Description");
        columnNames[COL_NUM_FILE] = new String("File");
        columnNames[COL_NUM_TYPE] = new String("Type");
    }


    public void setDirectories(File projectNeuronCodeDirectory, File nmodlSourceDirectory)
    {
        this.myProjectNeuronCodeDirectory = projectNeuronCodeDirectory;
        this.myNmodlSourceDirectory = nmodlSourceDirectory;
        this.parseDirectory();
    }


    public void parseDirectory()
    {
        allModFiles.removeAllElements();

        if (myNmodlSourceDirectory==null || myProjectNeuronCodeDirectory==null)
        {
            logger.logError("Directories not set...");
            return;
        }

        if (!myNmodlSourceDirectory.isDirectory())
        {
            logger.logError("Problem getting mod file directory: "+ myNmodlSourceDirectory);
            return;
        }

        ModFile[] inbuiltModFiles = ModFileHelper.getChannelMechModFilesInDir(myNmodlSourceDirectory);

        logger.logComment("There are " + inbuiltModFiles.length + " files in dir: " +
                          myNmodlSourceDirectory.getAbsolutePath());

        for (int i = 0; i < inbuiltModFiles.length; i++)
        {
            allModFiles.add(inbuiltModFiles[i]);
        }


        if (!myProjectNeuronCodeDirectory.isDirectory())
        {
            logger.logError("Problem getting mod file directory: "+ myProjectNeuronCodeDirectory);
            return;
        }

        ModFile[] customModFiles = ModFileHelper.getChannelMechModFilesInDir(myProjectNeuronCodeDirectory);

        logger.logComment("There are " + customModFiles.length + " files in dir: " +
                          myProjectNeuronCodeDirectory.getAbsolutePath());

        for (int i = 0; i < customModFiles.length; i++)
        {
            allModFiles.add(customModFiles[i]);
        }




        this.fireTableDataChanged();
    }

    public int getRowCount()
    {
        return allModFiles.size();
    }
    public int getColumnCount()
    {
        return columnNames.length;
    }
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        ModFile modFile = (ModFile)allModFiles.elementAt(rowIndex);
        switch (columnIndex)
        {
            case COL_NUM_NAME:
                return modFile.myNeuronElement.getProcessName();
            case COL_NUM_DESC:
                return modFile.getTitle();
            case COL_NUM_FILE:
                return modFile.getFullFileName();
            case COL_NUM_TYPE:
            {
                try
                {
                    if (modFile.getCurrentFile().getParentFile().getCanonicalPath() ==
                        myNmodlSourceDirectory.getCanonicalPath())
                    {
                        return "Inbuilt channel mechanism";
                    }
                    else
                        return "Custom channel mechanism";
                }
                catch (IOException ex)
                {
                    return "Unknown channel mechanism";
                }
            }


            default:
                return null;
        }
    }



    public ArrayList<String> getAllChannelMechanismNames()
    {
        ArrayList<String> allNames = new ArrayList<String>();
        for (int i = 0; i < getRowCount(); i++)
        {
               allNames.add((String)getValueAt(i, COL_NUM_NAME));
        }
        return allNames;
    }

}
