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

import java.util.Hashtable;
import ucl.physiol.neuroconstruct.hpc.mpi.MpiSettings.KnownSimulators;


/**
 * Support for interacting with MPI platform
 *
 *  *** STILL IN DEVELOPMENT! SUBJECT TO CHANGE WITHOUT NOTICE! ***
 *
 * @author Padraig Gleeson
 *
 */


public class RemoteLogin
{
    private String hostname = null;
    private String userName = null;
    private String workDir = null;

    private Hashtable<KnownSimulators, String> executables = new Hashtable<KnownSimulators, String>();

    public static final String remotePullScriptName = "pullsim.sh";

    public RemoteLogin()
    {

    }

    public RemoteLogin(String hostname, String userName, String workDir, Hashtable<KnownSimulators, String> executables)
    {
        this.hostname = hostname;
        this.userName = userName;
        this.workDir = workDir;
        this.executables = executables;
    }


    public Hashtable<KnownSimulators, String> getExecutables()
    {
        return executables;
    }

    public String getExecutableForSimulator(KnownSimulators simulator)
    {
        return executables.get(simulator);
    }

    public void setExecutables(Hashtable<KnownSimulators, String> executables)
    {
        this.executables = executables;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }


    public String getHostname()
    {
        return hostname;
    }

    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }

    public String getWorkDir()
    {
        return workDir;
    }

    public void setWorkDir(String workDir)
    {
        this.workDir = workDir;
    }


    @Override
    public Object clone()
    {
        RemoteLogin rl2 = new RemoteLogin(new String(hostname), new String(userName), new String(workDir), new Hashtable<KnownSimulators, String>(executables));
        return rl2;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteLogin other = (RemoteLogin) obj;
        if ((this.hostname == null) ? (other.hostname != null) : !this.hostname.equals(other.hostname)) {
            return false;
        }
        if ((this.userName == null) ? (other.userName != null) : !this.userName.equals(other.userName)) {
            return false;
        }
        if ((this.workDir == null) ? (other.workDir != null) : !this.workDir.equals(other.workDir)) {
            return false;
        }
        if (this.executables != other.executables && (this.executables == null || !this.executables.equals(other.executables))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.hostname != null ? this.hostname.hashCode() : 0);
        hash = 13 * hash + (this.userName != null ? this.userName.hashCode() : 0);
        hash = 13 * hash + (this.workDir != null ? this.workDir.hashCode() : 0);
        hash = 13 * hash + (this.executables != null ? this.executables.hashCode() : 0);
        return hash;
    }

    


    


}
