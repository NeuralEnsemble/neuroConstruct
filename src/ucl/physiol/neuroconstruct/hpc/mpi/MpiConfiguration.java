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

/**
 * Support for interacting with MPI platform
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

public class MpiConfiguration
{
    private String name = null;
    private ArrayList<MpiHost> hostList = new ArrayList<MpiHost>();

    public static void main(String[] args)
    {
        new MpiConfiguration();
    }

    private MpiConfiguration()
    {
    }

    public MpiConfiguration(String name)
    {
        this.name = name;
    }

    public void setHostList(ArrayList<MpiHost> hostList)
    {
        this.hostList = hostList;
    }


    public ArrayList<MpiHost> getHostList()
    {
        return this.hostList;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getTotalNumProcessors()
    {
        int num = 0;
        for (MpiHost host: hostList)
        {
            num+=host.getNumProcessors();
        }
        return num;

    }




    public String toString()
    {
        StringBuffer info = new StringBuffer("MpiConfiguration: "+ name+"\n");
        for (MpiHost host: hostList)
        {
            info.append("   "+host.toString());
        }
        return info.toString();
    }
}
