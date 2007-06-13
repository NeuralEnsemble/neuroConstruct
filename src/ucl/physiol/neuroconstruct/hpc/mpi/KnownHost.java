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
 * Host information to use in deciding cell distribution
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class KnownHost
{
    String hostname = null;
    String ip = null;
    float cpuSpeed = -1;
    float memory = -1;

    private KnownHost()
    {
    }

    public KnownHost(String hostname)
    {
        this.hostname = hostname;
    }

    public KnownHost(String hostname, String ip, float cpuSpeed, float memory)
    {
        this.hostname = hostname;
        this.ip = ip;
        this.cpuSpeed = cpuSpeed;
        this.memory = memory;
    }


    public static void main(String[] args)
    {

        KnownHost k1 = new KnownHost("comp1");

        System.out.println("k1: "+ k1);

        KnownHost k2 = new KnownHost("comp2", "192.192.190.45", 3.4f, 2.0f);

        System.out.println("k2: "+ k2);



    }

    public String toString()
    {
        String info = hostname;
        if (ip!=null) info = info + " ("+ ip+")";
        if
            (cpuSpeed>0) info = info + ", Speed: "+ cpuSpeed+" GHz";
        else
            info = info + ", Speed unknown";

        if
            (memory>0) info = info + ", Physical memory: "+ memory+" GB";
        else
            info = info + ", Physical memory unknown";

        return info;
    }

}
