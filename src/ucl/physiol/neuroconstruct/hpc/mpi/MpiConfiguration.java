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

import java.util.*;

/**
 * Support for interacting with MPI platform
 *
 * @author Padraig Gleeson
 *  
 */

public class MpiConfiguration
{
    private String name = null;
    
    private ArrayList<MpiHost> hostList = new ArrayList<MpiHost>();
    


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
    
    public boolean isParallel()
    {
        return getTotalNumProcessors()>1;
        
    }

    public int getNumProcessorsOnHost(String hostname)
    {
        for (MpiHost host: hostList)
        {
            if (host.getHostname().equals(hostname)) return host.getNumProcessors();
        }
        return -1;
    }

    public String getHostForGlobalId(int globId)
    {
        int traversed = 0;
        
        for (MpiHost host: hostList)
        {
            if (traversed + host.getNumProcessors()>globId) return host.getHostname();//globId-traversed;
            
            traversed+=host.getNumProcessors();
        }
        
        return null;
    }
    public int getProcForGlobalId(int globId)
    {
        int traversed = 0;
        
        for (MpiHost host: hostList)
        {
            if (traversed + host.getNumProcessors()>globId) return globId-traversed;
            
            traversed+=host.getNumProcessors();
        }
        
        return -1;
    }
    
    public boolean equals(Object other)
    {
        if (!(other instanceof MpiConfiguration)) return false;
        
        MpiConfiguration mcOther = (MpiConfiguration)other;
        
        if (!mcOther.getName().equals(getName())) return false;
        
        if (mcOther.getHostList().size()!=this.getHostList().size()) return false;

        for (int i=0;i<getHostList().size();i++)
        {
            if (!mcOther.getHostList().get(i).equals(getHostList().get(i))) return false;
        }
        
        return true;
    }
    
    
    public Object clone()
    {
        MpiConfiguration mc2 = new MpiConfiguration(new String(name));
        ArrayList<MpiHost> mh2 = new ArrayList<MpiHost>();
        for(MpiHost mh: this.hostList)
        {
            mh2.add((MpiHost)mh.clone());
        }
        mc2.setHostList(mh2);
        
        return mc2;
    }


    public String toString()
    {
        int totHosts = 0;
        int totProcs = 0;
        for (MpiHost host: hostList)
        {
            //info.append("   "+host.toString());
            totHosts++;
            totProcs+=host.getNumProcessors();
        }
        String hosts = totHosts+" hosts, ";
        String procs = totProcs+" processors";
        if (totHosts==1) hosts = "1 host, ";
        if (totProcs==1) procs = "1 processor";

        StringBuffer info = new StringBuffer(name+" with "+hosts+procs+"\n");
        return info.toString();
    }
    

    public static void main(String[] args)
    {
        MpiConfiguration mc = new MpiConfiguration("Tester");
        mc.getHostList().add(new MpiHost("host3", 3, 1));
        mc.getHostList().add(new MpiHost("host4", 4, 1));
        mc.getHostList().add(new MpiHost("host5", 5, 1));

        for(int num=0;num<15;num++)
        {
        System.out.println("Glob id: "+ num+" is on "+mc.getHostForGlobalId(num) +", "+ mc.getProcForGlobalId(num));
        }
    }
}
