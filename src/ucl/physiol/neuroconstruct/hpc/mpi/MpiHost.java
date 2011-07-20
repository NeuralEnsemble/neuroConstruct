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

public class MpiHost
{
    private String hostname = null;
    private int numProcessors = 1;
    private float weight = 1;

    // needed for XML Encoder...
    public MpiHost(){};

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
    
    @Override
    public Object clone()
    {
        return new MpiHost(new String(hostname), numProcessors, weight);
    }

    
    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof MpiHost)) return false;
        
        MpiHost mhOther = (MpiHost)other;
        if (! mhOther.getHostname().equals(this.getHostname())) return false;
        if (mhOther.getWeight() != this.getWeight()) return false;
        if (mhOther.getNumProcessors() !=this.getNumProcessors()) return false;
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.hostname != null ? this.hostname.hashCode() : 0);
        hash = 29 * hash + this.numProcessors;
        hash = 29 * hash + Float.floatToIntBits(this.weight);
        return hash;
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





    @Override
    public String toString()
    {
        return "Host: "+hostname+", num of processors: "+numProcessors+", weight: "+weight;
    }
}
