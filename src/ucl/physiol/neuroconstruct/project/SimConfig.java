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

package ucl.physiol.neuroconstruct.project;

import java.util.ArrayList;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
import ucl.physiol.neuroconstruct.hpc.mpi.*;

/**
 * Class containing info on a simulation configuration. One of these needs to be
 * specified whenever the network is generated
 *
 * @author Padraig Gleeson
 *  
 */


public class SimConfig
{
    ClassLogger logger = new ClassLogger("SimConfig");

    public static final String MAIN_CATEGORY = "Main";
    public static final String TEST_CATEGORY = "Test";

    private String name = null;
    private String description = null;

    private ArrayList<String> cellGroups = new ArrayList<String>();
    private ArrayList<String> netConns = new ArrayList<String>();
    private ArrayList<String> inputs = new ArrayList<String>();
    private ArrayList<String> plots = new ArrayList<String>();

    private float simDuration = 0; // will be reset to the project default sim duration...

    private float simDt = 0; // will be reset to the project default sim dt...
    private String category = MAIN_CATEGORY; 
    
    private MpiConfiguration mpiConf = null;

    /**
     * Default constructor needed for XMLEncoder
     */
    public SimConfig()
    {
    }

    public SimConfig(String name, String description)
    {
        this.name = name;
        this.description = description;
    }


    public String getCategory()
    {
        return category;
    }
    
    public void setCategory(String category)
    {
        this.category = category;
    }


    public MpiConfiguration getMpiConf()
    {
        //logger.logComment("mpiConf being asked: "+ mpiConf);
        
        if (mpiConf == null) mpiConf 
            = GeneralProperties.getMpiSettings().getMpiConfigurations().get(MpiSettings.prefConfig);
        //logger.logComment("mpiConf now: "+ mpiConf);
        
        return (MpiConfiguration)mpiConf.clone();
    }
    
    public void setMpiConf(MpiConfiguration mc)
    {
        
        this.mpiConf = (MpiConfiguration)mc.clone();
        

        logger.logComment("mpiConf being set: "+ this.mpiConf);
    }
    
    

    public String getName()
    {
        if (name==null) return "";
        return name;
    }
    public String getDescription()
    {
        if (description==null) return "";
        return description;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }

    public void addCellGroup(String cellGroup)
    {
        logger.logComment("Adding Cell group: "+ cellGroup + " to Sim Config: "+ this.toString());
        cellGroups.add(cellGroup);
    }

    public void addNetConn(String netConn)
    {
        logger.logComment("Adding netConn: "+ netConn + " to Sim Config: "+ this.toString());
        netConns.add(netConn);
    }

    public void addInput(String input)
    {
        logger.logComment("Adding input: "+ input + " to Sim Config: "+ this.toString());
        inputs.add(input);
    }



    public void addPlot(String plot)
    {
        logger.logComment("Adding plot: "+ plot + " to Sim Config: "+ this.toString());
        plots.add(plot);
    }

    public float getSimDuration()
    {
        return this.simDuration;
    }

    public void setSimDuration(float simDuration)
    {
        this.simDuration = simDuration;
    }


    public float getSimDt()
    {
        return this.simDt;
    }

    public void setSimDt(float simDt)
    {
        this.simDt = simDt;
    }


    public ArrayList<String> getCellGroups()
    {
        return cellGroups;
    }

    public void setCellGroups(ArrayList<String> cellGroups)
    {
        this.cellGroups = cellGroups;
    }

    public ArrayList<String> getNetConns()
    {
        return netConns;
    }

    public void setNetConns(ArrayList<String> netConns)
    {
        this.netConns = netConns;
    }

    public ArrayList<String> getPlots()
    {
        return plots;
    }

    public void setPlots(ArrayList<String> plots)
    {
        this.plots = plots;
    }


    public ArrayList<String> getInputs()
    {
        return inputs;
    }

    public void setInputs(ArrayList<String> inputs)
    {
        this.inputs = inputs;
    }

    @Override
    public String toString()
    {
        return "SimConfig: "+ name;
    }

    public String toLongString()
    {
        return "SimConfig: "+ name+ ", cellGroups: "+ cellGroups
            + ", netConns: "+ netConns
            + ", inputs: "+ inputs
            + ", plots: "+ plots;
    }



}
