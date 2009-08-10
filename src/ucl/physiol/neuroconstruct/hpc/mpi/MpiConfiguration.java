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

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import ucl.physiol.neuroconstruct.utils.GeneralUtils;


/**
 * Support for interacting with MPI platform
 *
 *  *** STILL IN DEVELOPMENT! SUBJECT TO CHANGE WITHOUT NOTICE! ***
 *
 * @author Padraig Gleeson
 *
 */

public class MpiConfiguration
{
    private String name = null;

    private RemoteLogin remoteLogin = null;

    private QueueInfo queueInfo = null;
    
    private ArrayList<MpiHost> hostList = new ArrayList<MpiHost>();


    private MpiConfiguration()
    {
    }

    public MpiConfiguration(String name)
    {
        this.name = name;
    }

    public String getQueueSubmitScript(String projName, String simRef, int timeMins)
    {
        if (queueInfo==null)
            return null;
        StringBuffer script = new StringBuffer();

        script.append("#!/bin/bash \n");
        script.append("\n");
        script.append("#PBS -N "+simRef+"_"+projName+"\n");
        script.append("#PBS -A "+queueInfo.getAccount()+"\n");
        script.append("#PBS -j oe\n");
        script.append("#PBS -l qos=parallel\n");
        script.append("\n");
        script.append("#! Number of nodes \n");
        script.append("#! The total number of nodes passed to mpirun will be nodes*ppn \n");
        script.append("#! Second entry: Total amount of wall-clock time (true time). \n");
        script.append("#! 02:00:00 indicates 02 hours\n");
        script.append("\n");

        int nodes = hostList.size();
        int ppn = 0;
        for(MpiHost host: hostList)
        {
            if (host.getNumProcessors()>ppn) ppn = host.getNumProcessors();
        }

        script.append("#PBS -l nodes="+nodes+":ppn="+ppn+",walltime=00:"+timeMins+":00\n");
        script.append("\n");
        script.append("#! Full path to application + application name\n");
        script.append("application=\""+remoteLogin.getNrnivLocation()+"\"\n");
        script.append("\n");
        script.append("#! Work directory\n");
        script.append("workdir=\""+getProjectSimDir(projName)+"/"+simRef+"\"\n");
        script.append("\n");
        script.append("#! Run options for the application\n");
        script.append("options=\"$workdir/"+projName+".hoc\"\n");
        script.append("\n");
        script.append("\n");
        script.append("###############################################################\n");
        script.append("### You should not have to change anything below this line ####\n");
        script.append("###############################################################\n");
        script.append("\n");
        script.append("#! change the working directory (default is home directory)\n");
        script.append("\n");
        script.append("cd $workdir\n");
        script.append("\n");
        script.append("echo Running on the host `hostname`\n");
        script.append("echo Time is `date`\n");
        script.append("echo Directory is `pwd`\n");
        script.append("echo PBS job ID is $PBS_JOBID\n");
        script.append("echo PATH is $PATH\n");
        script.append("echo This job runs on the following machines:\n");
        script.append("echo `cat $PBS_NODEFILE | uniq`\n");
        script.append(" \n");
        script.append("#! Create a machine file for MPI\n");
        script.append("cat $PBS_NODEFILE | uniq > machine.file.$PBS_JOBID\n");
        script.append("\n");
        script.append("numnodes=`wc $PBS_NODEFILE | awk '{ print $1 }'`\n");
        script.append("sleep 1\n");
        script.append("#! Run the parallel MPI executable (nodes*ppn)\n");
        script.append("\n");
        script.append("export LAUNCH_APP=\"$application $options\"\n");
        if (false && queueInfo.getLauncherScript()!=null && queueInfo.getLauncherScript().length()>0)
        {
            script.append("export CVOS_LAUNCHER=\""+queueInfo.getLauncherScript()+"\"\n");
        }
        else
        {
            script.append("export CVOS_LAUNCHER=\"\"\n");
        }
        
        script.append("export MPI_RUN=\"mpirun\"\n");
        script.append("\n");
        script.append("echo \"Running $CVOS_LAUNCHER $MPI_RUN -machinefile machine.file.$PBS_JOBID -np $numnodes  $LAUNCH_APP\"\n");
        script.append("echo \"--------------------------------------------------------------\"\n");
        script.append("echo \"\"\n");
        script.append("\n");
        script.append("$CVOS_LAUNCHER $MPI_RUN -machinefile machine.file.$PBS_JOBID -np $numnodes  $LAUNCH_APP\n");
        script.append("\n");
        script.append("\n");
        script.append("\n");

        return script.toString();
    }

    private String getProjectSimDir(String projName)
    {
        String hostname = null;
        try
        {
            InetAddress localMachine = InetAddress.getLocalHost();
            hostname = localMachine.getHostName();
        }
        catch (UnknownHostException ex)
        {
            hostname = "UnknownHost";
        }

        return remoteLogin.getWorkDir() + "/" + projName + "_" + hostname;
    }



    public String getPushScript(String projName, String simRef)
    {

        StringBuffer scriptText = new StringBuffer();

        scriptText.append("#!/bin/bash \n\n");
        scriptText.append("\n");
        scriptText.append("export simRef=\""+simRef+"\"\n");
        scriptText.append("export projName=\""+projName+"\"\n");
        scriptText.append("\n");
        scriptText.append("export remoteHost=\""+remoteLogin.getHostname()+"\"\n");
        scriptText.append("export remoteUser=\""+remoteLogin.getUserName()+"\"\n");
        scriptText.append("export nrnivLocation=\""+remoteLogin.getNrnivLocation()+"\"\n");
        scriptText.append("\n");
        scriptText.append("projDir="+getProjectSimDir(projName)+"\n");
        scriptText.append("simDir=$projDir\"/\"$simRef\n");
        scriptText.append("\n");
        scriptText.append("echo \"Going to send files to dir: \"$simDir\" on \"$remoteHost\n");
        scriptText.append("\n");
        scriptText.append("echo \"mpirun -map ");

        for (int i = 0; i < getHostList().size(); i++)
        {
            for (int j = 0; j < getHostList().get(i).getNumProcessors(); j++)
            {
                if (!(i==0 && j==0)) scriptText.append(":");

                scriptText.append(getHostList().get(i).getHostname());
            }

        }


        scriptText.append(" \"$nrnivLocation\" \"$simDir\"/\"$projName\".hoc\">runmpi.sh\n");
        scriptText.append("\n");
        scriptText.append("chmod u+x runmpi.sh\n");
        scriptText.append("\n");
        scriptText.append("ssh $remoteUser@$remoteHost \"mkdir $projDir\"\n");
        scriptText.append("ssh $remoteUser@$remoteHost \"rm -rf $simDir\"\n");
        scriptText.append("ssh $remoteUser@$remoteHost \"mkdir $simDir\"\n");
        scriptText.append("\n");
        scriptText.append("zipFile=$simRef\".tar.gz\"\n");
        scriptText.append("\n");
        scriptText.append("echo \"Going to zip files into \"$zipFile\n");
        scriptText.append("\n");
        scriptText.append("tar czf $zipFile *.mod *.hoc *.props *.dat *.sh *.py *.xml *.h5\n");
        scriptText.append("\n");

        scriptText.append("echo \"Going to send to: $simDir on $remoteUser@$remoteHost\"\n");

        boolean useScp = false;
        if (useScp)
        {
            scriptText.append("scp $zipFile $remoteUser@$remoteHost:$simDir\n");
        }
        else
        {
            scriptText.append("echo -e \"put $zipFile\">putFile.b\n");
            scriptText.append("sftp -b putFile.b $remoteUser@$remoteHost\n");
            scriptText.append("ssh $remoteUser@$remoteHost \"mv ~/$zipFile $simDir/$zipFile\"\n");
        }

        scriptText.append("\n");
        scriptText.append("ssh $remoteUser@$remoteHost \"cd $simDir;tar xzf $zipFile; rm $zipFile\"\n");
        scriptText.append("ssh $remoteUser@$remoteHost \"cd $simDir;/bin/bash -ic nrnivmodl\"\n");
        if (queueInfo==null)
        {
            scriptText.append("ssh $remoteUser@$remoteHost \"cd $simDir;./runmpi.sh\"\n");
        }
        else
        {
            scriptText.append("ssh $remoteUser@$remoteHost \"cd $simDir;/bin/bash -ic 'qsub "+QueueInfo.submitScript+"'\"\n");
            scriptText.append("ssh $remoteUser@$remoteHost \"echo 'Submitted job!';/bin/bash -ic 'qstat -u $remoteUser'\"\n");
            scriptText.append("sleep 6\n"); // Quick snooze to see result of qsub...
        }
        scriptText.append("\n");
        scriptText.append("\n");

        return scriptText.toString();
    }


    public String getPullScript(String projName, String simRef, File localDir)
    {

        StringBuffer pullScriptText = new StringBuffer();

        pullScriptText.append("#!/bin/bash \n\n");
        pullScriptText.append("\n");
        pullScriptText.append("export simRef=\""+simRef+"\"\n");
        pullScriptText.append("export projName=\""+projName+"\"\n");
        pullScriptText.append("\n");
        pullScriptText.append("export targetDir=\""+remoteLogin.getWorkDir()+"\"\n");
        pullScriptText.append("export remoteHost=\""+remoteLogin.getHostname()+"\"\n");
        pullScriptText.append("export remoteUser=\""+remoteLogin.getUserName()+"\"\n");
        pullScriptText.append("\n");

        pullScriptText.append("\n");
        pullScriptText.append("projDir="+getProjectSimDir(projName)+"\n");
        pullScriptText.append("simDir=$projDir\"/\"$simRef\n");
        pullScriptText.append("\n");
        String localPath = localDir.getAbsolutePath();
        if (GeneralUtils.isWindowsBasedPlatform())
        {
            localPath = GeneralUtils.convertToCygwinPath(localPath);
        }

        pullScriptText.append("export localDir="+localPath+"\"/\"$simRef\"/\"\n");
        pullScriptText.append("\n");
        pullScriptText.append("\n");
        pullScriptText.append("echo \"Going to get files from dir: \"$simDir\" on \"$remoteHost\" and place them locally on \"$localDir\n");
        pullScriptText.append("\n");
        pullScriptText.append("zipFile=$simRef\".tar.gz\"\n");
        pullScriptText.append("\n");
        pullScriptText.append("echo \"Going to zip files into \"$zipFile\n");
        pullScriptText.append("\n");
        pullScriptText.append("ssh $remoteUser@$remoteHost \"cd $simDir;tar czf $zipFile *.*\"\n");
        pullScriptText.append("\n");

        pullScriptText.append("scp  $remoteUser@$remoteHost:$simDir\"/\"$zipFile $localDir\n");


        pullScriptText.append("cd $localDir\n");

        if (false)
        {
            pullScriptText.append("scp $remoteUser@$remoteHost:$simDir\"/\"$zipFile $localDir\n");
        }
        else
        {
            pullScriptText.append("echo -e \"get $simDir\"/\"$zipFile\">pullFile.b\n");
            pullScriptText.append("sftp -b pullFile.b $remoteUser@$remoteHost\n");
        }


        pullScriptText.append("\n");
        pullScriptText.append("tar xzf $zipFile\n");
        pullScriptText.append("rm $zipFile\n");
        pullScriptText.append("rm pullFile.b\n\n");
        pullScriptText.append("ssh $remoteUser@$remoteHost \"cd $simDir;rm $zipFile\"\n");

        return pullScriptText.toString();
    }


    public RemoteLogin getRemoteLogin()
    {
        return remoteLogin;
    }

    public void setRemoteLogin(RemoteLogin remoteLogin)
    {
        this.remoteLogin = remoteLogin;
    }

    public QueueInfo getQueueInfo()
    {
        return queueInfo;
    }

    public void setQueueInfo(QueueInfo queueInfo)
    {
        this.queueInfo = queueInfo;
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

    public boolean isParallelNet()
    {
        return getTotalNumProcessors()>1;

    }
    public boolean isParallelOrRemote()
    {
        return isParallelNet()||isRemotelyExecuted();

    }

    public boolean isRemotelyExecuted()
    {
        return this.remoteLogin!=null;

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
        final MpiConfiguration other = (MpiConfiguration) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
        {
            return false;
        }
        if (this.remoteLogin != other.remoteLogin && (this.remoteLogin == null || !this.remoteLogin.equals(other.remoteLogin)))
        {
            return false;
        }
        if (this.queueInfo != other.queueInfo && (this.queueInfo == null || !this.queueInfo.equals(other.queueInfo)))
        {
            return false;
        }
        if (this.hostList != other.hostList && (this.hostList == null || !this.hostList.equals(other.hostList)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 29 * hash + (this.remoteLogin != null ? this.remoteLogin.hashCode() : 0);
        hash = 29 * hash + (this.queueInfo != null ? this.queueInfo.hashCode() : 0);
        hash = 29 * hash + (this.hostList != null ? this.hostList.hashCode() : 0);
        return hash;
    }


    
    
    @Override
    public Object clone()
    {
        MpiConfiguration mc2 = new MpiConfiguration(new String(name));
        ArrayList<MpiHost> mh2 = new ArrayList<MpiHost>();
        for(MpiHost mh: this.hostList)
        {
            mh2.add((MpiHost)mh.clone());
        }
        mc2.setHostList(mh2);
        if (remoteLogin!=null)
        {
            mc2.setRemoteLogin((RemoteLogin)remoteLogin.clone());
        }
        if (queueInfo!=null)
        {
            mc2.setQueueInfo((QueueInfo)queueInfo.clone());
        }
        
        return mc2;
    }


    @Override
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

        StringBuffer info = new StringBuffer(name+" with "+hosts+procs);

        if (this.remoteLogin!=null)
        {
            info.append(" on "+remoteLogin.getHostname());
        }
        
        info.append("\n");
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
