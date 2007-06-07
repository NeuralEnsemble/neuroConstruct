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

package ucl.physiol.neuroconstruct.nmodleditor.modfile;

import java.io.*;
import java.util.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class ModFileHelper
{
    static ClassLogger logger = new ClassLogger("ModFileHelper");



    private ModFileHelper()
    {
    }

/*
    ///**
    // * Gets the Channel mechanisms which come as standard with NEURON
    //
    public static String[] getInbuiltChannelMechanisms()
    {
        return new String[]{"hh", "pas"};
    }


   // /**
   //  * Gets the Synaptic mechanisms which come as standard with NEURON
   //
    public static String[] getInbuiltSynapticProcesses()
    {
        return new String[]{"ExpSyn", "Exp2Syn"};
    }

  //  /**
  //   * Gets the decription of the inbuilt mechanisms
  //
    public static String getDescriptionInbuiltProcess(String processName)
    {
        if (processName.equals("hh")) return "Standard Hodgkin Huxley mechanism"
    }
*/


    public static ModFile[] getModFilesInDirectory(File modFilesDir)
    {
        logger.logComment("Looking for mod files in directory: "+ modFilesDir.getAbsolutePath());
        Vector mods = new Vector();
        File[] contents = modFilesDir.listFiles();


        for (int i = 0; i < contents.length; i++)
        {
            if (contents[i].getName().endsWith(".mod"))
            {
                try
                {
                    ModFile modFile = new ModFile(contents[i].getAbsolutePath());

                    mods.add(modFile);
                }
                catch (ModFileException ex)
                {
                }
            }
        }
        ModFile[] modFileArray = new ModFile[mods.size()];
        mods.toArray(modFileArray);
        return modFileArray;

    }

    public static ModFile[] getSynapseModFilesInDir(File modFilesDir)
    {
        logger.logComment("Looking for Synaptic mod files in directory: "+ modFilesDir.getAbsolutePath());

        Vector mods = new Vector();
        File[] contents = modFilesDir.listFiles();


        for (int i = 0; i < contents.length; i++)
        {
            if (contents[i].getName().endsWith(".mod"))
            {
                try
                {
                    ModFile modFile = new ModFile(contents[i].getAbsolutePath());

                    if (modFile.myNetReceiveElement != null &&
                        modFile.myNetReceiveElement.getUnformattedLines().length > 0)
                    {
                        mods.add(modFile);
                    }
                }
                catch (ModFileException ex)
                {
                }
            }
        }
        ModFile[] modFileArray = new ModFile[mods.size()];
        mods.toArray(modFileArray);
        return modFileArray;
    }


    public static ModFile[] getChannelMechModFilesInDir(File modFilesDir)
    {
        logger.logComment("Looking for Channel mechanism mod files in directory: "+ modFilesDir.getAbsolutePath());

        Vector mods = new Vector();
        File[] contents = modFilesDir.listFiles();

        for (int i = 0; i < contents.length; i++)
        {
            if (contents[i].getName().endsWith(".mod"))
            {
                try
                {
                    ModFile modFile = new ModFile(contents[i].getAbsolutePath());

                    if (modFile.myNeuronElement != null &&
                        modFile.myNeuronElement.getProcess() == NeuronElement.DENSITY_MECHANISM)
                    {
                        mods.add(modFile);
                    }
                }
                catch (ModFileException ex)
                {
                }
            }
        }
        ModFile[] modFileArray = new ModFile[mods.size()];
        mods.toArray(modFileArray);
        return modFileArray;
    }


}
