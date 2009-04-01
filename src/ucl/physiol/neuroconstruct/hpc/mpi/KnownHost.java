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

package ucl.physiol.neuroconstruct.hpc.mpi;


/**
 * Support for interacting with MPI platform
 *
 *  *** STILL IN DEVELOPMENT! SUBJECT TO CHANGE WITHOUT NOTICE! ***
 *
 * @author Padraig Gleeson
 *
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
