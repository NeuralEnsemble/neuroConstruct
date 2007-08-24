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


/**
 * Support for interacting with MPI platform
 *
 * @author Padraig Gleeson
 *  
 */

public class MpiHost
{
    private String hostname = null;
    private int numProcessors = 1;
    private float weight = 1;

    private MpiHost(){};

    public MpiHost(String hostname)
    {
        this.hostname = hostname;
    }
    public MpiHost(String hostname, int numProcessors, float weight)
    {
        this.hostname = hostname;
        this.numProcessors = numProcessors;
        this.weight = weight;
    }

    public String getHostname()
    {
        return this.hostname;
    }

    public int getNumProcessors()
    {
        return this.numProcessors;
    }

    public float getWeight()
    {
        return this.weight;
    }

    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }

    public void setNumProcessors(int numProcessors)
    {
        this.numProcessors = numProcessors;
    }

    public void setWeight(float weight)
    {
        this.weight = weight;
    }





    public String toString()
    {
        return "Host: "+hostname+", num of processors: "+numProcessors+", weight: "+weight;
    }
}
