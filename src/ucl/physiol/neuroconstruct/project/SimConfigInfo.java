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

import java.util.*;
import ucl.physiol.neuroconstruct.utils.ClassLogger;

/**
 * Class containing info on simulation configurations in a single project, which handles the
 * default sim config
 *
 * @author Padraig Gleeson
 *  
 */

public class SimConfigInfo
{
    ClassLogger logger = new ClassLogger("SimConfigInfo");

    /**
     * Name of the sim config which always has to be present...
     */
    public static final String DEFAULT_SIM_CONFIG_NAME = "Default Simulation Configuration";

    /**
     * The default description for the default sim config.
     * Note, this can be changed to something more project specific
     */
    public static final String DEFAULT_SIM_CONFIG_DESC = "This is the default configuration of the Cell Groups, "
        +"stimulations, plots, etc for this project";


    private ArrayList<SimConfig> simConfigs = new ArrayList<SimConfig>();

    public SimConfigInfo()
    {
        //checkForDefaultSimConfig();
    }

    public void add(SimConfig simConfig)
    {
        simConfigs.add(simConfig);
    }

    public int getNumSimConfigs()
    {
        return this.simConfigs.size();
    }


    public void remove(SimConfig simConfig)
    {
        simConfigs.remove(simConfig);
    }

    private void checkForDefaultSimConfig()
    {
        boolean defaultPresent = false;

        for (int i = 0; i < simConfigs.size(); i++)
        {
            if (simConfigs.get(i).getName().equals(DEFAULT_SIM_CONFIG_NAME))
            {
                defaultPresent = true;
            }

        }
        if (!defaultPresent)
        {
            logger.logComment("---- Adding the default sim config!!");
            SimConfig def = new SimConfig(DEFAULT_SIM_CONFIG_NAME,
                                                             DEFAULT_SIM_CONFIG_DESC);
            add(def);
        }
    }

    public SimConfig getDefaultSimConfig()
    {
        logger.logComment("Checking for default sim config");
        SimConfig def = getSimConfig( DEFAULT_SIM_CONFIG_NAME);
        if (def ==null)
        {
            checkForDefaultSimConfig();
            def = getDefaultSimConfig();
        }
        logger.logComment("returning: "+ def);
        return def;
    }



    public SimConfig getSimConfig(String simConfigName)
    {
        logger.logComment("Being asked for sim config: "+ simConfigName);
        for (int i = 0; i < simConfigs.size(); i++)
        {
            if (simConfigs.get(i).getName().equals(simConfigName))
            {
                logger.logComment("Found it: "+ simConfigs.get(i));
                return simConfigs.get(i);
            }

        }
        logger.logError("Not found!!");
        return null;
    }

    /**
     * Can be called after cell group, plot, etc is deleted, to
     * check consistency of stored sim configs with project info
     */
    public void validateStoredSimConfigs(Project project)
    {
        for (int j = 0; j < simConfigs.size(); j++)
        {
            SimConfig simConfig = simConfigs.get(j);

            if (simConfig.getSimDuration()<=0)
            {
                simConfig.setSimDuration(project.simulationParameters.getDuration());
            }

            ArrayList<String> cellGroups = new ArrayList<String>(); // create copy
            cellGroups.addAll(simConfig.getCellGroups());
            ArrayList<String> addedCellGroups = new ArrayList<String>(); // to eliminate doubles...

            for (int k = 0; k < cellGroups.size(); k++)
            {
                if (!project.cellGroupsInfo.getAllCellGroupNames().contains(cellGroups.get(k)) ||
                    addedCellGroups.contains(cellGroups.get(k)))
                    simConfig.getCellGroups().remove(cellGroups.get(k));

                addedCellGroups.add(cellGroups.get(k));
            }
            ArrayList<String> netConns = new ArrayList<String>(simConfig.getNetConns()); // create copy
            for (int k = 0; k < netConns.size(); k++)
            {
                if (!project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames().contains(netConns.get(k))
                    &&
                    !project.volBasedConnsInfo.getAllAAConnNames().contains(netConns.get(k)))
                    simConfig.getNetConns().remove(netConns.get(k));
            }

            ArrayList<String> inputs = new ArrayList<String>(simConfig.getInputs()); // create copy
            for (int k = 0; k < inputs.size(); k++)
            {
                if (!project.elecInputInfo.getAllStimRefs().contains(inputs.get(k)))
                    simConfig.getInputs().remove(inputs.get(k));
            }

            ArrayList<String> plots = new ArrayList<String>(simConfig.getPlots()); // create copy
            for (int k = 0; k < plots.size(); k++)
            {
                if (!project.simPlotInfo.getAllSimPlotRefs().contains(plots.get(k)))
                    simConfig.getPlots().remove(plots.get(k));
            }
        }
    }



    /**
     * Has to be here for XML Encoder...
     */
    public ArrayList<SimConfig> getAllSimConfigs()
    {
        //checkForDefaultSimConfig();
        return simConfigs;
    }

    /**
     * Has to be here for XML Encoder...
     */
    public void setAllSimConfigs(ArrayList<SimConfig> simConfigs)
    {
        this.simConfigs = simConfigs;
        checkForDefaultSimConfig();

    }


    public ArrayList<String> getAllUsedCellGroups()
    {
        ArrayList<String> usedCellGroups = new ArrayList<String>();

        for (SimConfig sc: simConfigs)
        {
            for(String cg: sc.getCellGroups())
            {
                if (!usedCellGroups.contains(cg))
                    usedCellGroups.add(cg);
            }
        }
        return usedCellGroups;
    }
    
    
    public ArrayList<String> getAllUsedNetConns()
    {
        ArrayList<String> used = new ArrayList<String>();

        for (SimConfig sc: simConfigs)
        {
            for(String nc: sc.getNetConns())
            {
                if (!used.contains(nc))
                    used.add(nc);
            }
        }
        return used;
    }
    
    public ArrayList<String> getAllUsedElectInputs()
    {
        ArrayList<String> used = new ArrayList<String>();

        for (SimConfig sc: simConfigs)
        {
            for(String in: sc.getInputs())
            {
                if (!used.contains(in))
                    used.add(in);
            }
        }
        return used;
    }
    
    
    public ArrayList<String> getAllUsedPlots()
    {
        ArrayList<String> used = new ArrayList<String>();

        for (SimConfig sc: simConfigs)
        {
            for(String p: sc.getPlots())
            {
                if (!used.contains(p))
                    used.add(p);
            }
        }
        return used;
    }

    public ArrayList<String> getAllSimConfigNames()
    {
        checkForDefaultSimConfig();
        ArrayList<String> names = new ArrayList<String>();
        for (int i = 0; i < simConfigs.size(); i++)
        {
            SimConfig next = simConfigs.get(i);
            names.add(next.getName());
        }
        return names;
    }


}
