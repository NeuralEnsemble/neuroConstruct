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


public class QueueInfo
{

    private int wallTimeMins = -1;

    private String account = null;

    private String launcherScript = "";


    public static final String submitScript = "subjob.sh";


    public QueueInfo()
    {
    }

    public QueueInfo(int wallTimeMins, String account, String launcherScript)
    {
        this.wallTimeMins = wallTimeMins;
        this.account = account;
        this.launcherScript = launcherScript;

    }


    @Override
    public Object clone()
    {
        QueueInfo q2 = new QueueInfo(wallTimeMins, new String(account), new String(launcherScript));
        return q2;

    }



    public String getAccount()
    {
        return account;
    }

    public void setAccount(String account)
    {
        this.account = account;
    }

    public String getLauncherScript()
    {
        return launcherScript;
    }

    public void setLauncherScript(String launcherScript)
    {
        this.launcherScript = launcherScript;
    }


    public int getWallTimeMins()
    {
        return wallTimeMins;
    }

    public void setWallTimeMins(int wallTimeMins)
    {
        this.wallTimeMins = wallTimeMins;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final QueueInfo other = (QueueInfo) obj;
        if (this.wallTimeMins != other.wallTimeMins)
        {
            return false;
        }
        if ((this.account == null) ? (other.account != null) : !this.account.equals(other.account))
        {
            return false;
        }
        if ((this.launcherScript == null) ? (other.launcherScript != null) : !this.launcherScript.equals(other.launcherScript))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 89 * hash + this.wallTimeMins;
        hash = 89 * hash + (this.account != null ? this.account.hashCode() : 0);
        hash = 89 * hash + (this.launcherScript != null ? this.launcherScript.hashCode() : 0);
        return hash;
    }



}
