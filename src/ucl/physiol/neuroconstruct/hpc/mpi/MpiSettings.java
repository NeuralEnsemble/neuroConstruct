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

package ucl.physiol.neuroconstruct.hpc.mpi;

import java.util.*;

import ucl.physiol.neuroconstruct.utils.ClassLogger;


/**
 * Support for interacting with MPI platform
 *
 * @author Padraig Gleeson
 *  
 */


public class MpiSettings
{
    ClassLogger logger = new ClassLogger("MpiSettings");
    
    public static String MPI_V1 = "MPI v1.*";
    public static String MPI_V2 = "MPI v2.*";

    private String version = MPI_V1;

    public static int prefConfig = 0;


    private ArrayList<MpiConfiguration> configurations = new ArrayList<MpiConfiguration>();

    public MpiSettings()
    {
        String localConfig = "Local machine, serial mode";
        String multiConfig = "MultiHostConf";
        String testConfig = "TestConf";
        String testConfig22 = "TestConf22";


        if (getMpiConfiguration(localConfig)==null)
        {
            MpiConfiguration def = new MpiConfiguration(localConfig);
            def.getHostList().add(new MpiHost("localhost", 1, 1));
            configurations.add(def);
        }


        if (getMpiConfiguration(multiConfig)==null)
        {
            MpiConfiguration p = new MpiConfiguration(multiConfig);
            //p.getHostList().add(new MpiHost("padraigneuro", 1, 1));
            p.getHostList().add(new MpiHost("avicenna",1, 1));
            p.getHostList().add(new MpiHost("bernal", 4, 1));
            configurations.add(p);
        }

        if (getMpiConfiguration(testConfig)==null)
        {
            MpiConfiguration p = new MpiConfiguration(testConfig);
            p.getHostList().add(new MpiHost("localhost",4, 1));
            configurations.add(p);
        }

        if (getMpiConfiguration(testConfig22)==null)
        {
            MpiConfiguration p22 = new MpiConfiguration(testConfig22);
            p22.getHostList().add(new MpiHost("localhost",22, 1));
            configurations.add(p22);
        }

    }


    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getVersion()
    {
        return this.version;
    }

    public ArrayList<MpiConfiguration> getMpiConfigurations()
    {
        return this.configurations;
    }

    public void setMpiConfigurations(ArrayList<MpiConfiguration> confs)
    {
        this.configurations = confs;
    }

    public MpiConfiguration getMpiConfiguration(String name)
    {
        for (MpiConfiguration config: configurations)
        {
            if (config.getName().equals(name)) return config;
        }

        return null;
    }

    public static void main(String[] args)
    {
        new MpiSettings();
    }

}




