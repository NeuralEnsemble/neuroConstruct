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

package ucl.physiol.neuroconstruct.hpc.mpi;

import java.io.File;
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
        String local2Config = "Local machine (2p)";
        String local4Config = "Local machine (4p)";
        String local8Config = "Local machine (8p)";
        String local32Config = "Local machine (32p)";
        String local128Config = "Local machine (128p)";
        String multiConfig = "TestConf";
        //String testConfig22 = "TestConfMore";


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
            p.getHostList().add(new MpiHost("eriugena",1, 1));
            p.getHostList().add(new MpiHost("bernal", 1, 1));
            configurations.add(p);
        }

        if (getMpiConfiguration(local2Config)==null)
        {
            MpiConfiguration p = new MpiConfiguration(local2Config);
            p.getHostList().add(new MpiHost("localhost",2, 1));
            configurations.add(p);
        }
        if (getMpiConfiguration(local4Config)==null)
        {
            MpiConfiguration p = new MpiConfiguration(local4Config);
            p.getHostList().add(new MpiHost("localhost",4, 1));
            configurations.add(p);
        }
        if (getMpiConfiguration(local8Config)==null)
        {
            MpiConfiguration p = new MpiConfiguration(local8Config);
            p.getHostList().add(new MpiHost("localhost",8, 1));
            configurations.add(p);
        }
        if (getMpiConfiguration(local32Config)==null)
        {
            MpiConfiguration p = new MpiConfiguration(local32Config);
            p.getHostList().add(new MpiHost("localhost",32, 1));
            configurations.add(p);
        }
        if (getMpiConfiguration(local128Config)==null)
        {
            MpiConfiguration p = new MpiConfiguration(local128Config);
            p.getHostList().add(new MpiHost("localhost",128, 1));
            configurations.add(p);
        }


    }


    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getVersion()
    {
        File mpiV1flag = new File("MPI1");
        File mpiV2flag = new File("MPI2");
        if (mpiV1flag.exists()) return MPI_V1;
        if (mpiV2flag.exists()) return MPI_V2;
        
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




