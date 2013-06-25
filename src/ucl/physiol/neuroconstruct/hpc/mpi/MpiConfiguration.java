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
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import ucl.physiol.neuroconstruct.hpc.mpi.MpiSettings.KnownSimulators;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
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
    private static ClassLogger logger = new ClassLogger("MpiConfiguration");

    private String name = null;

    private RemoteLogin remoteLogin = null;

    private QueueInfo queueInfo = null;
    
    private ArrayList<MpiHost> hostList = new ArrayList<MpiHost>();

    private String mpiVersion = null;

    private boolean useScp = false;



    private MpiConfiguration()
    {
    }

    public MpiConfiguration(String name)
    {
        this.name = name;
    }

    public String getMpiVersion()
    {
        if (this.mpiVersion==null)
        {
            return MpiSettings.getMPIVersion();
        }
        else
        {
            return this.mpiVersion;
        }
    }

    public void setMpiVersion(String mpiVersion)
    {
        this.mpiVersion = mpiVersion;
    }

    public boolean isUseScp()
    {
        return useScp;
    }

    public void setUseScp(boolean useScp)
    {
        this.useScp = useScp;
    }




    public String getQueueSubmitScript(String projName, String simRef, int timeMins, KnownSimulators simulator)
    {
        boolean isNeuron = simulator.equals(KnownSimulators.NEURON) || simulator.equals(KnownSimulators.PY_NEURON) ;

        if (queueInfo==null)
            return null;
        StringBuilder script = new StringBuilder();

        if (this.queueInfo.getQueueType().equals(QueueInfo.QueueType.PBS))
        {

            script.append("#!/bin/bash -l\n");
            script.append("\n");
            script.append("#PBS -N "+simRef+"_"+projName+"\n");
            script.append("#PBS -A "+queueInfo.getAccount()+"\n");
            script.append("#PBS -j oe\n");

            for (String line: queueInfo.additionalSubOptions)
            {
                script.append(line+"\n");

            }


            int nodes = hostList.size();
            int ppn = 0;
            int totalProcs = 0;
            for(MpiHost host: hostList)
            {
                totalProcs = totalProcs + host.getNumProcessors();

                if (host.getNumProcessors()>ppn) ppn = host.getNumProcessors();
            }

            if (remoteLogin.getHostname().indexOf("legion")>=0 && totalProcs<=4 && timeMins<=6)
            {
                script.append("# As job is small, using Test queue on Legion...\n");
                script.append("#PBS -q Test  \n");
            }

            script.append("\n");
            script.append("#! Number of nodes \n");
            script.append("#! The total number of nodes passed to mpirun will be nodes*ppn \n");
            script.append("#! Second entry: Total amount of wall-clock time (true time). \n");
            script.append("#! 02:00:00 indicates 02 hours\n");
            script.append("\n");


            script.append("#PBS -l nodes="+nodes+":ppn="+ppn+",walltime=00:"+timeMins+":00\n");
            script.append("\n");
            script.append("#! Full path to application + application name\n");
            script.append("application=\""+remoteLogin.getExecutableForSimulator(simulator)+"\"\n");
            script.append("\n");
            script.append("#! Work directory\n");
            script.append("workdir=\""+getProjectSimDir(projName)+"/"+simRef+"\"\n");
            script.append("\n");
            script.append("#! Run options for the application\n");

            if(isNeuron)
            {
                String mpiFlag = "";

                if (getMpiVersion().equals(MpiSettings.OPENMPI_V2)) mpiFlag = "-mpi ";

                script.append("options=\""+mpiFlag+"$workdir/"+projName+".hoc\"\n");
            }
            else
                script.append("options=\"$workdir/"+projName+".g\"\n");

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
            script.append("export LAUNCH_APP=\"$application\"\n");
            script.append("export LAUNCH_APP_OP=\"$application $options\"\n");

            if (queueInfo.getLauncherScript()!=null && queueInfo.getLauncherScript().length()>0)
            {
                script.append("export CVOS_LAUNCHER=\""+queueInfo.getLauncherScript()+"\"\n");
            }
            else
            {
                script.append("export CVOS_LAUNCHER=\"\"\n");
            }

            script.append("export MPI_RUN=\"mpirun\"\n");
            script.append("\n");
            String exe = "$CVOS_LAUNCHER $MPI_RUN -machinefile machine.file.$PBS_JOBID -np $numnodes  $LAUNCH_APP_OP";
            script.append("echo \"Running: "+exe+"\"\n");
            script.append("echo \"--------------------------------------------------------------\"\n");
            script.append("echo \"\"\n");
            script.append("\n");
            script.append(exe+"\n");
            script.append("\n");
            script.append("\n");
            script.append("\n");
        }
        else if (queueInfo.getQueueType().equals(QueueInfo.QueueType.SGE))
        {
            script.append("#!/bin/bash -l\n");
            script.append("\n");
            String workDir = getProjectSimDir(projName)+"/"+simRef;
            script.append("#$ -l  vf=2G\n");
            script.append("#$ -l  h_vmem=1.9G\n");
            script.append("#$ -l  tmem=1.9G\n");
            script.append("#$ -l  h_rt=0:"+timeMins+":0\n\n");
            script.append("#$ -wd  "+workDir+"\n");
            script.append("#$ -o  "+workDir+"/log\n");
            script.append("#$ -j y\n");
            script.append("#$ -N  "+simRef+"_"+projName+"\n\n");


            for (String line: queueInfo.additionalSubOptions)
            {
                script.append(line+"\n");
            }

            String parallelEnv = "smp";
            String procInfo = "-np $NSLOTS";

            if (hostList.size()>1)
            {
                parallelEnv = "openmpi";
                //procInfo = " -n $NSLOTS -bynode -machinefile $TMP/machines";
            }

            script.append("#$ -pe "+parallelEnv+"  "+getTotalNumProcessors()+"\n\n");

            String exec = workDir+"/"+projName+".hoc";

            if (simulator.equals(KnownSimulators.PY_NEURON))
            {
                exec = workDir+"/run_"+projName+".py";
            }
            else if(simulator.equals(KnownSimulators.GENESIS) || simulator.equals(KnownSimulators.MOOSE))
            {
                exec = workDir+"/"+projName+".g";
            }
            //script.append("source ~/.nrnpympienv\n");
            //script.append("/opt/sun-ct/bin/mpirun -np "+getTotalNumProcessors()+" "+remoteLogin.getExecutableForSimulator(simulator)+" -mpi "+exec+"\n");

            String mpiFlag = "";

            if (isNeuron) mpiFlag = "-mpi ";

            String mpirunPath = this.queueInfo.getMpirunPath();
            if (mpirunPath==null)
                mpirunPath = "/opt/sun-ct/bin/mpirun";

            if (isNeuron)
            {
                script.append("/bin/bash -ic '"+getNrnivmodl()+"'\n");
            }

            script.append("/bin/bash -ic '"+mpirunPath+" "+procInfo+" "+remoteLogin.getExecutableForSimulator(simulator)+" "+mpiFlag+exec+"'\n");

        }
        else if (queueInfo.getQueueType().equals(QueueInfo.QueueType.LL))
        {
            script.append("\n");
            String workDir = getProjectSimDir(projName)+"/"+simRef;


            // These settings are based on tests on a BlueGene using IBM Load Leveler at the Nencki Institute in Warsaw

            script.append("# @ job_name = "+simRef+"_"+projName+"\n");
            script.append("# @ account_no = "+queueInfo.getAccount()+"\n");
            script.append("# @ class = powiew\n");
            script.append("# @ error = nrn.err\n");
            script.append("# @ output = nrn.out\n");
            script.append("# @ environment = COPY_ALL\n");
            script.append("# @ wall_clock_limit = 00:"+timeMins+":00\n");
            script.append("# @ job_type = bluegene\n");
            script.append("# @ bg_size = "+getTotalNumProcessors()+"\n");
            script.append("# @ queue\n\n");



            for (String line: queueInfo.additionalSubOptions)
            {
                script.append(line+"\n");
            }

            String parallelEnv = "smp";
            String procInfo = "-np "+getTotalNumProcessors();

            if (hostList.size()>1)
            {
                parallelEnv = "openmpi";
                //procInfo = " -n $NSLOTS -bynode -machinefile $TMP/machines";
            }

            //script.append("#$ -pe "+parallelEnv+"  "+getTotalNumProcessors()+"\n\n");

            String exec = workDir+"/"+projName+".hoc";

            if (simulator.equals(KnownSimulators.PY_NEURON))
            {
                exec = workDir+"/run_"+projName+".py";
            }
            else if(simulator.equals(KnownSimulators.GENESIS) || simulator.equals(KnownSimulators.MOOSE))
            {
                exec = workDir+"/"+projName+".g";
            }
            //script.append("source ~/.nrnpympienv\n");
            //script.append("/opt/sun-ct/bin/mpirun -np "+getTotalNumProcessors()+" "+remoteLogin.getExecutableForSimulator(simulator)+" -mpi "+exec+"\n");

            String mpiFlag = "";

            if (isNeuron) mpiFlag = "-mpi ";


    

            //script.append("/bin/bash -ic '"+mpirunPath+" "+procInfo+" "+remoteLogin.getExecutableForSimulator(simulator)+" "+mpiFlag+exec+"'\n");
            script.append("time mpirun -cwd "+workDir+" -mode SMP "+procInfo+" "+workDir+"/powerpc64/special \""+mpiFlag+exec+"\"\n");

        }

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

    private String getNrnivmodl()
    {
        String nrnivmodl = "nrnivmodl";

        String[] simLocStrings = remoteLogin.getExecutableForSimulator(KnownSimulators.NEURON).split(" ");
        for(String s: simLocStrings)
        {
            if (s.endsWith("nrniv")) // i.e. full path to nrniv
            {
                nrnivmodl = s+"modl"; // i.e. full path to nrnivmodl
            }
        }
        return nrnivmodl;
    }


    public String getPushScript(String projName, String simRef, KnownSimulators simulator, File dirToRunIn)
    {
        logger.logComment("getPushScript for "+projName+", simRef: "+simRef+", simulator: "+simulator+", dirToRunIn: "+ dirToRunIn );
        boolean isNeuron = simulator.equals(KnownSimulators.NEURON) || simulator.equals(KnownSimulators.PY_NEURON) ;

        StringBuilder scriptText = new StringBuilder();

        scriptText.append("#!/bin/bash -l\n\n");
        scriptText.append("\n");
        scriptText.append("cd "+dirToRunIn.getAbsolutePath()+"\n\n");
        scriptText.append("\n");
        scriptText.append("export simRef=\""+simRef+"\"\n");
        scriptText.append("export projName=\""+projName+"\"\n");
        scriptText.append("\n");
        scriptText.append("export remoteHost=\""+remoteLogin.getHostname()+"\"\n");
        scriptText.append("export remoteUser=\""+remoteLogin.getUserName()+"\"\n");
        scriptText.append("export simulatorLocation=\""+remoteLogin.getExecutableForSimulator(simulator)+"\"\n");
        scriptText.append("\n");
        scriptText.append("projDir="+getProjectSimDir(projName)+"\n");
        scriptText.append("simDir=$projDir\"/\"$simRef\n");
        scriptText.append("\n");
        scriptText.append("echo \"Going to send files to dir: \"$simDir\" on \"$remoteHost\n");
        scriptText.append("echo \"Local dir: \"$PWD\n");
        scriptText.append("\n");

        String hostFlag = "-map ";
        String hostSeperator = ":";
        String mainCmd = "mpirun";
        String mpiFlags = "";

        if (getMpiVersion().equals(MpiSettings.OPENMPI_V2))
        {
            hostFlag = "-host ";
            hostSeperator = ",";
            mpiFlags = "-mpi ";
            mainCmd = "mpiexec";
        }
        else if (getMpiVersion().equals(MpiSettings.MPICH_V2))
        {
            hostSeperator = ",";
            mpiFlags = "-mpi ";
            mainCmd = "mpiexec";
        }

        if (queueInfo!=null && queueInfo.getMpirunPath()!=null)
            mainCmd = queueInfo.getMpirunPath(); // Override any other...


        scriptText.append("echo \"cd $simDir;"+mainCmd+" "+hostFlag+" ");

        for (int i = 0; i < getHostList().size(); i++)
        {
            for (int j = 0; j < getHostList().get(i).getNumProcessors(); j++)
            {
                if (!(i==0 && j==0)) scriptText.append(hostSeperator);

                scriptText.append(getHostList().get(i).getHostname());
            }

        }


        scriptText.append(" \"$simulatorLocation\" "+mpiFlags+" \"$simDir\"/\"$projName\".hoc\">runmpi.sh\n");
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
        scriptText.append("tar czvf $zipFile *.mod *.hoc *.p *.g *.props *.dat *.sh *.py *.xml *.h5 *Utils\n");
        scriptText.append("\n");

        scriptText.append("echo \"Going to send to: $simDir on $remoteUser@$remoteHost\"\n");
        
        if (isUseScp())
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

        if (isNeuron)
        {
            String nrnivmodl = getNrnivmodl();

            if (!this.queueInfo.getQueueType().equals(QueueInfo.QueueType.LL))
            {
                scriptText.append("# ssh $remoteUser@$remoteHost \"cd $simDir;/bin/bash -ic "+nrnivmodl+"\" # Now run on compute node\n");
            }
            else
            {
                scriptText.append("ssh $remoteUser@$remoteHost \"cd $simDir;module load mpi_default;/bin/bash -ic "+nrnivmodl+"\"\n");
            }
        }

        if (queueInfo==null)
        {
            //scriptText.append("ssh $remoteUser@$remoteHost \"cd $simDir;./runmpi.sh\"\n");

            scriptText.append("ssh $remoteUser@$remoteHost \"cd $simDir;ssh "+getHostList().get(0).getHostname()+" /bin/bash -ic  $simDir/runmpi.sh\"\n");
        }
        else
        {
            if (true || isNeuron)
            {
                scriptText.append("ssh $remoteUser@$remoteHost \"cd $simDir;/bin/bash -ic '"+queueInfo.getSubmitCommand()+" "+QueueInfo.submitScript+"'\"\n");
                scriptText.append("ssh $remoteUser@$remoteHost \"echo 'Submitted job!';/bin/bash -ic '"+queueInfo.getUserQueueStatusCommand()+" $remoteUser'\"\n");
                scriptText.append("rm -rf $zipFile\n");
                scriptText.append("sleep 15\n"); // Quick snooze to see result of qsub...
            }
            else
            {
                scriptText.append("ssh $remoteUser@$remoteHost \"echo 'NOTSubmitted job!'\"\n");
                scriptText.append("sleep 60\n"); // Quick snooze to see result of qsub...
            }
        }
        scriptText.append("\n");
        scriptText.append("\n");
        scriptText.append("rm checkingRemote\n");
        scriptText.append("\n");

        return scriptText.toString();
    }


    public String getPullScript(String projName, String simRef, File localDir) throws IOException
    {

        StringBuilder pullScriptText = new StringBuilder();

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
        String localPath = localDir.getCanonicalPath();
        if (GeneralUtils.isWindowsBasedPlatform())
        {
            localPath = GeneralUtils.convertToCygwinPath(localPath);
        }

        pullScriptText.append("export localDir="+localPath+"\"/\"$simRef\"/\"\n");
        pullScriptText.append("\n");


        pullScriptText.append("remoteTimeFile=$simDir\"/time.dat\"\n");
        pullScriptText.append("localTimeFile=$localDir\"time.dat_temp\"\n");

        pullScriptText.append("scp $remoteUser@$remoteHost:$remoteTimeFile $localTimeFile\n");

        pullScriptText.append("if [ -e $localTimeFile ] \n");
        pullScriptText.append("then\n");

        pullScriptText.append("\n");
        pullScriptText.append("rm $localTimeFile\n");
        pullScriptText.append("\n");
        pullScriptText.append("checkingRemoteFile=$localDir\"checkingRemote\"\n\n");
        pullScriptText.append("echo \"Temporary file indicating check is in progress...\">$checkingRemoteFile\n\n");
        pullScriptText.append("\n");
        pullScriptText.append("echo \"Going to get files from dir: \"$simDir\" on \"$remoteHost\" and place them locally on \"$localDir\n");
        pullScriptText.append("\n");
        pullScriptText.append("zipFile=$simRef\".tar.gz\"\n");
        pullScriptText.append("\n");
        pullScriptText.append("echo \"Going to zip files into \"$zipFile\n");
        pullScriptText.append("\n");
        pullScriptText.append("ssh $remoteUser@$remoteHost \"cd $simDir;tar czvf $zipFile *.dat *.h5 *.props *.spike log*\"\n");
        pullScriptText.append("\n");

        //pullScriptText.append("scp  $remoteUser@$remoteHost:$simDir\"/\"$zipFile $localDir\n");


        pullScriptText.append("cd $localDir\n");

        if (isUseScp())
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

        if (!isUseScp()) pullScriptText.append("rm pullFile.b\n\n");

        pullScriptText.append("ssh $remoteUser@$remoteHost \"cd $simDir;rm $zipFile\"\n");

        pullScriptText.append("rm $checkingRemoteFile\n\n");
        
        pullScriptText.append("else\n");

        pullScriptText.append("echo \"Simulation not finished yet...\"\n");

        pullScriptText.append("fi\n");

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
        if (this.mpiVersion != other.mpiVersion && (this.mpiVersion == null || !this.mpiVersion.equals(other.mpiVersion)))
        {
            return false;
        }

        if (this.isUseScp() != other.isUseScp() )
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
        hash = 29 * hash + (this.mpiVersion != null ? this.mpiVersion.hashCode() : 0);
        hash = 29 * hash + (isUseScp() ?  1:0);
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
        if (mpiVersion!=null)
        {
            mc2.setMpiVersion(new String(mpiVersion));
        }
        mc2.setUseScp(this.isUseScp());
        
        return mc2;
    }

    public String toLongString()
    {
        StringBuilder info = new StringBuilder(procsInfo()+"\n\n");
        if (this.mpiVersion!=null)
        {
            info.append("MPI version: "+mpiVersion+"\n");
        }
        info.append("Use scp: "+useScp+"\n\n");
        
        if (this.remoteLogin!=null)
        {
            info.append("-  Remote Login details:\n");
            info.append(this.remoteLogin+"\n");
        }
        if (this.queueInfo!=null)
        {
            info.append("-  Queue info\n");
            info.append(this.queueInfo+"\n");
        }
        return info.toString();
    }
    
    public String procsInfo()
    {
        int totHosts = 0;
        int totProcs = 0;
        for (MpiHost host: hostList)
        {
            totHosts++;
            totProcs+=host.getNumProcessors();
        }
        String hosts = totHosts+" hosts, ";
        String procs = totProcs+" processors";
        if (totHosts==1) hosts = "1 host, ";
        if (totProcs==1) procs = "1 processor";

        return name+" with "+hosts+procs;
    }

    @Override
    public String toString()
    {

        StringBuilder info = new StringBuilder(procsInfo());

        if (this.remoteLogin!=null)
        {
            info.append(" on "+remoteLogin.getHostname());
        }
        if (this.mpiVersion!=null)
        {
            info.append(" ("+mpiVersion+")");
        }
        if (this.isUseScp())
        {
            info.append(" (scp)");
        }
        
        info.append("\n");
        return info.toString();
    }
    

    public static void main(String[] args)
    {
        MpiConfiguration mc0 = new MpiConfiguration("Tester");
        mc0.getHostList().add(new MpiHost("host3", 3, 1));
        mc0.getHostList().add(new MpiHost("host4", 4, 1));
        mc0.getHostList().add(new MpiHost("host5", 5, 1));

        for(int num=0;num<15;num++)
        {
            System.out.println("Glob id: "+ num+" is on "+mc0.getHostForGlobalId(num) +", "+ mc0.getProcForGlobalId(num));
        }

        System.out.println(mc0.toString());

        MpiSettings ms = new MpiSettings();
        MpiConfiguration mc1 = ms.getMpiConfiguration(ms.MATLEM_8PROC);
        MpiConfiguration mc2 = (MpiConfiguration)mc1.clone();

        System.out.println("Eq? "+mc1.equals(mc2));
        System.out.println(mc1.hashCode()+",  "+mc2.hashCode());



    }
}
