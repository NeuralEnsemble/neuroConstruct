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
import java.util.*;

import ucl.physiol.neuroconstruct.project.ProjectStructure;
import ucl.physiol.neuroconstruct.utils.ClassLogger;


/**
 * Support for interacting with MPI platform
 *
 *  *** STILL IN DEVELOPMENT! SUBJECT TO CHANGE WITHOUT NOTICE! ***
 *
 * @author Padraig Gleeson
 *  
 */


public class MpiSettings
{
    ClassLogger logger = new ClassLogger("MpiSettings");
    
    public static final String MPICH_V1 = "MPICH v1.*";
    public static final String MPICH_V2 = "MPICH v2.*";
    public static final String OPENMPI_V2 = "OPENMPI v2.*";

   
    public static final String DEFAULT_MPI_VERSION = MPICH_V1;

    public static enum KnownSimulators {NEURON, PY_NEURON, GENESIS, MOOSE};
    
    public static final String LOCALHOST = "localhost";

    /*
     * To run on the local machine
     */
    public static final String LOCAL_SERIAL = "Local machine, serial mode";
    public static final String LOCAL_2PROC = "Local machine (2p)";
    public static final String LOCAL_3PROC = "Local machine (3p)";
    public static final String LOCAL_4PROC = "Local machine (4p)";
    public static final String LOCAL_8PROC = "Local machine (8p)";    // for testing only!
    public static final String LOCAL_16PROC = "Local machine (16p)";  // for testing only!


    public static final String CASPUR_8PROC = "Caspur (8p)";
    public static final String CASPUR_16PROC = "Caspur (16p)";
    public static final String CASPUR_32PROC = "Caspur (32p)";
    public static final String CASPUR_64PROC = "Caspur (64p)";


    // These are based on tests on a BlueGene using IBM Load Leveler at the Nencki Institute in Warsaw
    public static final String NOTOS_4PROC = "Notos (4p)";
    public static final String NOTOS_32PROC = "Notos (32p)";
    public static final String NOTOS_64PROC = "Notos (64p)";
    public static final String NOTOS_128PROC = "Notos (128p)";
    public static final String NOTOS_256PROC = "Notos (256p)";
    public static final String NOTOS_512PROC = "Notos (512p)";
    public static final String NOTOS_1024PROC = "Notos (1024p)";

    /*
     * To run on a remote machine and execute directly, i.e. no queue
     * There must be automatic ssh login to this machine
    
    public static final String CLUSTER_1PROC = "Cluster (1p)";
    public static final String CLUSTER_2PROC = "Cluster (1 x 2p)";
    public static final String CLUSTER_4PROC = "Cluster (1 x 4p)"; */

    /*
     * To run on a remote machine, jobs set running using a submit script.
     * Note the UCL Legion cluster is not on offer to neuroConstruct users
     * outside UCL...
     */
    public static final String LEGION_1PROC = "Legion (1 x 1p)";
    public static final String LEGION_2PROC = "Legion (1 x 2p)";
    public static final String LEGION_4PROC = "Legion (1 x 4p)";
    public static final String LEGION_8PROC = "Legion (2 x 4p)";
    public static final String LEGION_16PROC = "Legion (4 x 4p)";
    public static final String LEGION_24PROC = "Legion (6 x 4p)";
    public static final String LEGION_32PROC = "Legion (8 x 4p)";
    
    public static final String LEGION_40PROC = "Legion (10 x 4p)";

    public static final String LEGION_48PROC = "Legion (12 x 4p)";
    public static final String LEGION_64PROC = "Legion (16 x 4p)";
    public static final String LEGION_80PROC = "Legion (20 x 4p)";
    public static final String LEGION_96PROC = "Legion (24 x 4p)";
    public static final String LEGION_112PROC = "Legion (28 x 4p)";
    public static final String LEGION_128PROC = "Legion (32 x 4p)";
    public static final String LEGION_256PROC = "Legion (64 x 4p)";


    /*
     * Matthau & Lemmon consist of 10x and 20x 8 core machines
     */
    public static final String MATTHAU = "matthau-5-";
    public static final String LEMMON = "lemmon-5-";

    public static final String MATLEM_NRN62_1PROC = "MatLem, nrn62 (1 x 1p)";
    public static final String MATLEM_NRN62_4PROC = "MatLem, nrn62 (1 x 4p)";
    public static final String MATLEM_NRN62_8PROC = "MatLem, nrn62 (1 x 8p)";

    public static final String MATLEM_1PROC = "MatLem (1 x 1p)";
    public static final String MATLEM_2PROC = "MatLem (1 x 2p)";
    public static final String MATLEM_4PROC = "MatLem (1 x 4p)";
    public static final String MATLEM_8PROC = "MatLem (1 x 8p)";
    
    public static final String MATLEM_16PROC = "MatLem (2 x 8p)";
    public static final String MATLEM_32PROC = "MatLem (4 x 8p)";
    public static final String MATLEM_48PROC = "MatLem (6 x 8p)";
    public static final String MATLEM_64PROC = "MatLem (8 x 8p)";
    public static final String MATLEM_96PROC = "MatLem (12 x 8p)";
    public static final String MATLEM_128PROC = "MatLem (16 x 8p)";
    public static final String MATLEM_160PROC = "MatLem (20 x 8p)";
    public static final String MATLEM_192PROC = "MatLem (24 x 8p)";
    public static final String MATLEM_200PROC = "MatLem (25 x 8p)";
    public static final String MATLEM_216PROC = "MatLem (27 x 8p)";
    public static final String MATLEM_240PROC = "MatLem (30 x 8p)";

    public static final String MATLEM_DIRECT = "Matthau_Lemmon_Test_MANY";
    
    
    /*
     * UCSD NSG Portal infrastructure: http://www.nsgportal.org/
     * This is temporary configuration until the NSG portal adds a REST API for 
     * submitting jobs to the portal to run. Contact p.gleeson if you're interested in helping 
     * test this
     */
    public static final String NSG_MAIN = "NSG Portal [trestles]";
    public static final String NSG_1PROC = NSG_MAIN+" (1 x 1p)";
    public static final String NSG_8PROC = NSG_MAIN+" (1 x 8p)";

    
    public static final String MACHINE_FILE = "machinesToUse";
    
    
    /*
     * Index of "preferred" configuration in configurations
     */
    public static int prefConfig = 0;
    
    private ArrayList<MpiConfiguration> configurations = new ArrayList<MpiConfiguration>();

    public MpiSettings()
    {

        Hashtable<KnownSimulators, String> simulatorExecutables = new Hashtable<KnownSimulators, String>();
        Hashtable<KnownSimulators, String> simulatorExecutablesML = new Hashtable<KnownSimulators, String>();
        Hashtable<KnownSimulators, String> simulatorExecutablesMLnrn62 = new Hashtable<KnownSimulators, String>();

        Hashtable<KnownSimulators, String> simulatorExecutablesCaspur = new Hashtable<KnownSimulators, String>();
        Hashtable<KnownSimulators, String> simulatorExecutablesNotos = new Hashtable<KnownSimulators, String>();
        Hashtable<KnownSimulators, String> simulatorExecutablesNsg = new Hashtable<KnownSimulators, String>();

        simulatorExecutables.put(KnownSimulators.NEURON, "/home/ucgbpgl/nrnmpi/x86_64/bin/nrniv");
        simulatorExecutables.put(KnownSimulators.GENESIS, "/home/ucgbpgl/gen23/genesis");
        simulatorExecutables.put(KnownSimulators.MOOSE, "/home/ucgbpgl/moose/moose");

        
        simulatorExecutablesMLnrn62.put(KnownSimulators.NEURON, "/share/apps/neuro/nrn62_pympi/x86_64/bin/nrniv");
        simulatorExecutablesMLnrn62.put(KnownSimulators.PY_NEURON, "/share/apps/neuro/nrn62_pympi/x86_64/bin/nrniv -python");
        
        simulatorExecutablesML.put(KnownSimulators.NEURON, "/share/apps/neuro/nrn71/x86_64/bin/nrniv");
        simulatorExecutablesML.put(KnownSimulators.PY_NEURON, "/share/apps/neuro/nrn71/x86_64/bin/nrniv -python");

        simulatorExecutablesML.put(KnownSimulators.MOOSE, "/share/apps/neuro/moose/moose");
        simulatorExecutablesML.put(KnownSimulators.GENESIS, "/share/apps/neuro/genesis-2.3/genesis/genesis");


        simulatorExecutablesCaspur.put(KnownSimulators.NEURON, "/home/sergio/nrn7.0/x86_64/bin/nrniv");
        simulatorExecutablesNotos.put(KnownSimulators.NEURON, "/opt/neuron/powerpc64/bin/nrniv");
        
        simulatorExecutablesNsg.put(KnownSimulators.NEURON, "/opt/neuron/powerpc64/bin/nrniv");

        // This is a 4 processor Linux machine in our lab. Auto ssh login is enabled to it from the
        // machine on which neuroConstruct is running. Jobs are set running directly on this machine
        RemoteLogin directLogin = new RemoteLogin("192.168.15.70",
                                                  "padraig",
                                                  "/home/padraig/nCsims",
                                                  simulatorExecutables);

        // Login node for Matthau/Lemmon cluster
        RemoteLogin matlemLogin = new RemoteLogin("128.16.14.177",
                                                  "ucgbpgl",
                                                  "/home/ucgbpgl/nCsims",
                                                  simulatorExecutablesML);
        // Login node for Matthau/Lemmon cluster
        RemoteLogin matlemLoginNrn62 = new RemoteLogin("128.16.14.177",
                                                  "ucgbpgl",
                                                  "/home/ucgbpgl/nCsims",
                                                  simulatorExecutablesMLnrn62);


        // Login node for Casper cluster
        RemoteLogin caspurLogin = new RemoteLogin("matrix.caspur.it",
                                                  "sergio",
                                                  "/home/sergio/scratch/",
                                                  simulatorExecutablesCaspur);
        // Login node for Notos cluster
        RemoteLogin notosLogin = new RemoteLogin("notos",
                                                  "hela",
                                                  "/home/users/hela/nCsims",
                                                  simulatorExecutablesNotos);

        // Legion is the UCL supercomputing cluster. Legion operates the Torque batch queueing system
        // and the Moab scheduler, i.e. jobs aren't executed directly, but submitted to a queue and will
        // be run when the requested resources are available.
        RemoteLogin legionLogin = new RemoteLogin("legion.rc.ucl.ac.uk",
                                                  "ucgbpgl",
                                                  "/home/ucgbpgl/nCsims",
                                                  simulatorExecutables);

        RemoteLogin legionSerialLogin = new RemoteLogin("legion.rc.nucl.ac.uk",
                                                  "ucgbpgl",
                                                  "/home/ucgbpgl/nCsims",
                                                  simulatorExecutables);
        
        
        // Login node for NSG cluster
        RemoteLogin nsgLogin = new RemoteLogin("trestles-login.sdsc.edu",
                                                  "pgleeson",
                                                  "/home/pgleeson",
                                                  simulatorExecutablesNsg);

        //QueueInfo legionQueueCvos = new QueueInfo(6, "ucl/NeuroSci/neuroconst", "cvos-launcher");

        QueueInfo legionQueue = new QueueInfo(6, "ucl/NeuroSci/neuroconst", "cvos-launcher", QueueInfo.QueueType.PBS, "mpirun");
        legionQueue.addAdditionalSubOptions("#PBS -l qos=parallel");

        QueueInfo matlemQueue = new QueueInfo(6, "", "", QueueInfo.QueueType.SGE, "/opt/SUNWhpc/HPC8.2.1/gnu/bin/mpirun --mca btl openib,self");
        matlemQueue.addAdditionalSubOptions("#$ -l fc=yes");
        matlemQueue.addAdditionalSubOptions("#$ -R y");
        //matlemQueue.addAdditionalSubOptions("#$ -S /bin/bash");

        QueueInfo caspurQueue = new QueueInfo(6, "std10-300", "time", QueueInfo.QueueType.PBS, "mpirun");
        QueueInfo notosQueue = new QueueInfo(6, "G43-4", "time", QueueInfo.QueueType.LL, "mpirun");
        
        QueueInfo nsgQueue = new QueueInfo(6, "TG-IBN120011", "time", QueueInfo.QueueType.PBS, "mpirun");
        nsgQueue.addAdditionalSubOptions("#PBS -q normal");
        //nsgQueue.addAdditionalSubOptions("#PBS -v QOS=2");
        nsgQueue.addAdditionalSubOptions("#PBS -M  p.gleeson@ucl.ac.uk"); // CHANGE THIS!!!
        nsgQueue.addAdditionalSubOptions("#PBS -m ae");

        if (getMpiConfiguration(LOCAL_SERIAL)==null)
        {
            MpiConfiguration def = new MpiConfiguration(LOCAL_SERIAL);
            def.getHostList().add(new MpiHost(LOCALHOST, 1, 1));
            configurations.add(def);
        }


        if (getMpiConfiguration(LOCAL_2PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LOCAL_2PROC);
            p.getHostList().add(new MpiHost(LOCALHOST,2, 1));
            configurations.add(p);
        }
        if (getMpiConfiguration(LOCAL_3PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LOCAL_3PROC);
            p.getHostList().add(new MpiHost(LOCALHOST,3, 1));
            configurations.add(p);
        }
        if (getMpiConfiguration(LOCAL_4PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LOCAL_4PROC);
            p.getHostList().add(new MpiHost(LOCALHOST, 4, 1));
            configurations.add(p);
        }
        if (getMpiConfiguration(LOCAL_8PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LOCAL_8PROC);
            p.getHostList().add(new MpiHost(LOCALHOST, 8, 1));
            configurations.add(p);
        }
        if (getMpiConfiguration(LOCAL_16PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LOCAL_16PROC);
            p.getHostList().add(new MpiHost(LOCALHOST, 16, 1));
            configurations.add(p);
        }


        if (getMpiConfiguration(CASPUR_8PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(CASPUR_8PROC);

            for(int i=0;i<1;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(caspurLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(caspurQueue);
            configurations.add(p);
        }


        if (getMpiConfiguration(CASPUR_16PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(CASPUR_16PROC);

            for(int i=0;i<2;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(caspurLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(caspurQueue);
            configurations.add(p);
        }

        if (getMpiConfiguration(CASPUR_32PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(CASPUR_32PROC);

            for(int i=0;i<4;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(caspurLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(caspurQueue);
            configurations.add(p);
        }

        if (getMpiConfiguration(CASPUR_64PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(CASPUR_64PROC);

            for(int i=0;i<8;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(caspurLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(caspurQueue);
            configurations.add(p);
        }



        if (getMpiConfiguration(NOTOS_4PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(NOTOS_4PROC);

            for(int i=0;i<1;i++)
                p.getHostList().add(new MpiHost("node"+i,4, 1));

            p.setRemoteLogin(notosLogin);
            p.setMpiVersion(MpiSettings.MPICH_V2);
            p.setUseScp(true);
            p.setQueueInfo(notosQueue);
            configurations.add(p);
        }

        if (getMpiConfiguration(NOTOS_32PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(NOTOS_32PROC);

            for(int i=0;i<4;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(notosLogin);
            p.setMpiVersion(MpiSettings.MPICH_V2);
            p.setUseScp(true);
            p.setQueueInfo(notosQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(NOTOS_64PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(NOTOS_64PROC);

            for(int i=0;i<8;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(notosLogin);
            p.setMpiVersion(MpiSettings.MPICH_V2);
            p.setUseScp(true);
            p.setQueueInfo(notosQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(NOTOS_128PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(NOTOS_128PROC);

            for(int i=0;i<16;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(notosLogin);
            p.setMpiVersion(MpiSettings.MPICH_V2);
            p.setUseScp(true);
            p.setQueueInfo(notosQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(NOTOS_256PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(NOTOS_256PROC);

            for(int i=0;i<32;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(notosLogin);
            p.setMpiVersion(MpiSettings.MPICH_V2);
            p.setUseScp(true);
            p.setQueueInfo(notosQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(NOTOS_512PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(NOTOS_512PROC);

            for(int i=0;i<64;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(notosLogin);
            p.setMpiVersion(MpiSettings.MPICH_V2);
            p.setUseScp(true);
            p.setQueueInfo(notosQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(NOTOS_1024PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(NOTOS_1024PROC);

            for(int i=0;i<128;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(notosLogin);
            p.setMpiVersion(MpiSettings.MPICH_V2);
            p.setUseScp(true);
            p.setQueueInfo(notosQueue);
            configurations.add(p);
        }


        if (getMpiConfiguration(MATLEM_NRN62_1PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_NRN62_1PROC);

            p.getHostList().add(new MpiHost("localhost", 1, 1));
            p.setRemoteLogin(matlemLoginNrn62);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_NRN62_4PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_NRN62_4PROC);

            for(int i=0;i<1;i++)
                p.getHostList().add(new MpiHost("node"+i,4, 1));

            p.setRemoteLogin(matlemLoginNrn62);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_NRN62_8PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_NRN62_8PROC);

            for(int i=0;i<1;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(matlemLoginNrn62);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }


        if (getMpiConfiguration(MATLEM_1PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_1PROC);

            p.getHostList().add(new MpiHost("node0", 1, 1));
            p.setRemoteLogin(matlemLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_2PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_2PROC);

            for(int i=0;i<1;i++)
                p.getHostList().add(new MpiHost("node"+i,2, 1));

            p.setRemoteLogin(matlemLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_4PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_4PROC);

            for(int i=0;i<1;i++)
                p.getHostList().add(new MpiHost("node"+i,4, 1));

            p.setRemoteLogin(matlemLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_8PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_8PROC);

            for(int i=0;i<1;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(matlemLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_16PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_16PROC);

            for(int i=0;i<2;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(matlemLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_32PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_32PROC);

            for(int i=0;i<4;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(matlemLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_48PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_48PROC);

            for(int i=0;i<6;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(matlemLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_64PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_64PROC);

            for(int i=0;i<8;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(matlemLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_96PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_96PROC);

            for(int i=0;i<12;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(matlemLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_128PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_128PROC);

            for(int i=0;i<16;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(matlemLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_160PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_160PROC);

            for(int i=0;i<20;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(matlemLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_192PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_192PROC);

            for(int i=0;i<24;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(matlemLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_200PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_200PROC);

            for(int i=0;i<25;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(matlemLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_216PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_216PROC);

            for(int i=0;i<27;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(matlemLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(MATLEM_240PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(MATLEM_240PROC);

            for(int i=0;i<30;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(matlemLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(matlemQueue);
            configurations.add(p);
        }
        
/*
        if (getMpiConfiguration(multiConfig)==null)
        {
            MpiConfiguration p = new MpiConfiguration(multiConfig);
            //p.getHostList().add(new MpiHost("padraigneuro", 1, 1));
            p.getHostList().add(new MpiHost("eriugena",4, 1));
            p.getHostList().add(new MpiHost("bernal", 4, 1));
            configurations.add(p);
        }
        
        

        if (getMpiConfiguration(CLUSTER_1PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(CLUSTER_1PROC);

            p.getHostList().add(new MpiHost(directLogin.getHostname(),1, 1));
            p.setRemoteLogin(directLogin);
            configurations.add(p);
        }

        if (getMpiConfiguration(CLUSTER_2PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(CLUSTER_2PROC);

            p.getHostList().add(new MpiHost(directLogin.getHostname(),2, 1));
            p.setRemoteLogin(directLogin);
            configurations.add(p);
        }

        if (getMpiConfiguration(CLUSTER_4PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(CLUSTER_4PROC);

            p.getHostList().add(new MpiHost(directLogin.getHostname(),4, 1));
            p.setRemoteLogin(directLogin);
            configurations.add(p);
        }*/


        if (getMpiConfiguration(LEGION_1PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LEGION_1PROC);

            p.getHostList().add(new MpiHost("localhost", 1, 1));
            p.setRemoteLogin(legionLogin);
            p.setQueueInfo(legionQueue);
            configurations.add(p);
        }

        if (getMpiConfiguration(LEGION_2PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LEGION_2PROC);

            p.getHostList().add(new MpiHost("localhost", 2, 1));
            p.setRemoteLogin(legionLogin);
            p.setQueueInfo(legionQueue);
            configurations.add(p);
        }

        if (getMpiConfiguration(LEGION_4PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LEGION_4PROC);

            p.getHostList().add(new MpiHost("localhost",4, 1));
            p.setRemoteLogin(legionLogin);
            p.setQueueInfo(legionQueue);
            configurations.add(p);
        }


        if (getMpiConfiguration(LEGION_8PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LEGION_8PROC);

            for(int i=0;i<2;i++)
                p.getHostList().add(new MpiHost("node"+i,4, 1));

            p.setRemoteLogin(legionLogin);
            p.setQueueInfo(legionQueue);
            configurations.add(p);
        }


        if (getMpiConfiguration(LEGION_16PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LEGION_16PROC);

            for(int i=0;i<4;i++)
                p.getHostList().add(new MpiHost("node"+i,4, 1));

            p.setRemoteLogin(legionLogin);
            p.setQueueInfo(legionQueue);
            configurations.add(p);
        }

        if (getMpiConfiguration(LEGION_24PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LEGION_24PROC);

            for(int i=0;i<6;i++)
                p.getHostList().add(new MpiHost("node"+i,4, 1));

            p.setRemoteLogin(legionLogin);
            p.setQueueInfo(legionQueue);
            configurations.add(p);
        }


        if (getMpiConfiguration(LEGION_32PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LEGION_32PROC);

            for(int i=0;i<8;i++)
                p.getHostList().add(new MpiHost("node"+i,4, 1));

            p.setRemoteLogin(legionLogin);
            p.setQueueInfo(legionQueue);
            configurations.add(p);
        }


        if (getMpiConfiguration(LEGION_40PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LEGION_40PROC);

            for(int i=0;i<10;i++)
                p.getHostList().add(new MpiHost("node"+i,4, 1));

            p.setRemoteLogin(legionLogin);
            p.setQueueInfo(legionQueue);
            configurations.add(p);
        }


        if (getMpiConfiguration(LEGION_48PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LEGION_48PROC);

            for(int i=0;i<12;i++)
                p.getHostList().add(new MpiHost("node"+i,4, 1));

            p.setRemoteLogin(legionLogin);
            p.setQueueInfo(legionQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(LEGION_64PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LEGION_64PROC);

            for(int i=0;i<16;i++)
                p.getHostList().add(new MpiHost("node"+i,4, 1));

            p.setRemoteLogin(legionLogin);
            p.setQueueInfo(legionQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(LEGION_80PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LEGION_80PROC);

            for(int i=0;i<20;i++)
                p.getHostList().add(new MpiHost("node"+i,4, 1));

            p.setRemoteLogin(legionLogin);
            p.setQueueInfo(legionQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(LEGION_96PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LEGION_96PROC);

            for(int i=0;i<24;i++)
                p.getHostList().add(new MpiHost("node"+i,4, 1));

            p.setRemoteLogin(legionLogin);
            p.setQueueInfo(legionQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(LEGION_112PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LEGION_112PROC);

            for(int i=0;i<28;i++)
                p.getHostList().add(new MpiHost("node"+i,4, 1));

            p.setRemoteLogin(legionLogin);
            p.setQueueInfo(legionQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(LEGION_128PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LEGION_128PROC);

            for(int i=0;i<32;i++)
                p.getHostList().add(new MpiHost("node"+i,4, 1));

            p.setRemoteLogin(legionLogin);
            p.setQueueInfo(legionQueue);
            configurations.add(p);
        }
        if (getMpiConfiguration(LEGION_256PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(LEGION_256PROC);

            for(int i=0;i<64;i++)
                p.getHostList().add(new MpiHost("node"+i,4, 1));

            p.setRemoteLogin(legionLogin);
            p.setQueueInfo(legionQueue);
            configurations.add(p);
        }




/*
        int[] lemNodes = new int[]{14,15};

        int[] lemProcs = new int[]{4,8};


        for (int nodeNum: lemNodes)
        {
            for (int totalProcs: lemProcs)
            {
                String name = "Lemmon_"+nodeNum+"_"+totalProcs+"PROCS";

                if (getMpiConfiguration(name)==null)
                {
                    MpiConfiguration p = new MpiConfiguration(name);

                    p.getHostList().add(new MpiHost(LEMMON+nodeNum, totalProcs, 1));

                    p.setRemoteLogin(matlemLogin);
                    p.setMpiVersion(MpiSettings.OPENMPI_V2);
                    p.setUseScp(true);
                    configurations.add(p);

                }
            }
        }

        int[] mattNodes = new int[]{2,3, 4, 5, 6};

        int[] mattProcs = new int[]{4,8};


        for (int nodeNum: mattNodes)
        {
            for (int totalProcs: mattProcs)
            {
                String name = "Matthau_"+nodeNum+"_"+totalProcs+"PROCS";

                if (getMpiConfiguration(name)==null)
                {
                    MpiConfiguration p = new MpiConfiguration(name);

                    p.getHostList().add(new MpiHost(MATTHAU+nodeNum, totalProcs, 1));

                    p.setRemoteLogin(matlemLogin);
                    p.setMpiVersion(MpiSettings.OPENMPI_V2);
                    p.setUseScp(true);
                    configurations.add(p);

                }
            }
        }

        String name_16 = "Matthau_Lemmon_Test_16";
        MpiConfiguration p_16 = new MpiConfiguration(name_16);
        p_16.getHostList().add(new MpiHost(MATTHAU+5, 8, 1));
        p_16.getHostList().add(new MpiHost(LEMMON+14, 8, 1));

        p_16.setRemoteLogin(matlemLogin);
        p_16.setMpiVersion(MpiSettings.OPENMPI_V2);
        p_16.setUseScp(true);
        configurations.add(p_16);

        String name_56l = "Matthau_Lemmon_Test_56l";
        MpiConfiguration p_56l = new MpiConfiguration(name_56l);

        p_56l.getHostList().add(new MpiHost(LEMMON+10, 8, 1));
        p_56l.getHostList().add(new MpiHost(LEMMON+11, 8, 1));
        p_56l.getHostList().add(new MpiHost(LEMMON+12, 8, 1));
        p_56l.getHostList().add(new MpiHost(LEMMON+13, 8, 1));
        p_56l.getHostList().add(new MpiHost(LEMMON+14, 8, 1));
        p_56l.getHostList().add(new MpiHost(LEMMON+15, 8, 1));
        p_56l.getHostList().add(new MpiHost(LEMMON+16, 8, 1));
        //p_56l.getHostList().add(new MpiHost(LEMMON+17, 8, 1));
        //p_56l.getHostList().add(new MpiHost(LEMMON+18, 8, 1));
        //p_56l.getHostList().add(new MpiHost(LEMMON+19, 8, 1));


        p_56l.setRemoteLogin(matlemLogin);
        p_56l.setMpiVersion(MpiSettings.OPENMPI_V2);
        p_56l.setUseScp(true);
        configurations.add(p_56l);

        String name_56 = "Matthau_Lemmon_Test_56";
        MpiConfiguration p_56 = new MpiConfiguration(name_56);

        p_56.getHostList().add(new MpiHost(MATTHAU+3, 8, 1));
        p_56.getHostList().add(new MpiHost(MATTHAU+4, 8, 1));
        p_56.getHostList().add(new MpiHost(MATTHAU+5, 8, 1));
        p_56.getHostList().add(new MpiHost(MATTHAU+6, 8, 1));
        p_56.getHostList().add(new MpiHost(MATTHAU+7, 8, 1));
        p_56.getHostList().add(new MpiHost(MATTHAU+8, 8, 1));
        p_56.getHostList().add(new MpiHost(MATTHAU+9, 8, 1));


        p_56.setRemoteLogin(matlemLogin);
        p_56.setMpiVersion(MpiSettings.OPENMPI_V2);
        p_56.setUseScp(true);
        configurations.add(p_56);
*/

        MpiConfiguration p_ML = new MpiConfiguration(MATLEM_DIRECT);


        p_ML.getHostList().add(new MpiHost("matthau-5-1", 8, 1));
        p_ML.getHostList().add(new MpiHost("matthau-5-2", 8, 1));
        //p_ML.getHostList().add(new MpiHost("matthau-5-3", 8, 1));
        p_ML.getHostList().add(new MpiHost("matthau-5-4", 8, 1));

        p_ML.setRemoteLogin(matlemLogin);
        p_ML.setMpiVersion(MpiSettings.OPENMPI_V2);
        p_ML.setUseScp(true);
        configurations.add(p_ML);


        int[] lemProcs = new int[]{1,2,4};

        for(int proc: lemProcs)
        {
            MpiConfiguration n_ML = new MpiConfiguration("");

            n_ML.getHostList().add(new MpiHost("lemmon-5-"+proc, proc, 1));

            n_ML.setName("LEMMON_"+n_ML.getTotalNumProcessors());
            n_ML.setRemoteLogin(matlemLogin);
            n_ML.setMpiVersion(MpiSettings.OPENMPI_V2);
            n_ML.setUseScp(true);
            configurations.add(n_ML);
        }

        ArrayList<int[]> lemNodes = new ArrayList<int[]>();
        lemNodes.add(new int[]{19});        //8
        lemNodes.add(new int[]{17,18});      //16
        lemNodes.add(new int[]{5,6,7});    //24
        lemNodes.add(new int[]{8,9,10,11});//32
        lemNodes.add(new int[]{12,13,14,15,16});   // 40
        
        lemNodes.add(new int[]{1,3,4,5,6,7});      // 48
        lemNodes.add(new int[]{1,3,4,5,6,7,8,9});  // 64
        lemNodes.add(new int[]{1,3,4,5,6,7,8,9,10,11});                          // 80
        lemNodes.add(new int[]{1,3,4,5,6,7,8,9,10,11,12,13});                    // 96
        lemNodes.add(new int[]{1,3,4,5,6,7,8,9,10,11,12,13,14,15});              // 112
        lemNodes.add(new int[]{1,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17});        // 128
        lemNodes.add(new int[]{1,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19});  // 144

        for(int[] nodes: lemNodes)
        {
            MpiConfiguration n_ML = new MpiConfiguration("");

            for (int node: nodes)
                n_ML.getHostList().add(new MpiHost("lemmon-5-"+node, 8, 1));

            n_ML.setName("LEMMON_"+n_ML.getTotalNumProcessors());
            n_ML.setRemoteLogin(matlemLogin);
            n_ML.setMpiVersion(MpiSettings.OPENMPI_V2);
            n_ML.setUseScp(true);
            configurations.add(n_ML);
        }

        int[] matProcs = new int[]{1,2,4};

        for(int proc: matProcs)
        {
            MpiConfiguration n_ML = new MpiConfiguration("");

            n_ML.getHostList().add(new MpiHost("matthau-5-"+proc, proc, 1));

            n_ML.setName("MATTHAU_"+n_ML.getTotalNumProcessors());
            n_ML.setRemoteLogin(matlemLogin);
            n_ML.setMpiVersion(MpiSettings.OPENMPI_V2);
            n_ML.setUseScp(true);
            configurations.add(n_ML);
        }

        ArrayList<int[]> matNodes = new ArrayList<int[]>();
        matNodes.add(new int[]{10});
        matNodes.add(new int[]{8,9});
        matNodes.add(new int[]{5,6,7});
        matNodes.add(new int[]{1,2,4,5});
        matNodes.add(new int[]{1,2,4,5,6});
        matNodes.add(new int[]{1,2,4,5,6,7});
        matNodes.add(new int[]{1,2,4,5,6,7,8});
        matNodes.add(new int[]{1,2,4,5,6,7,8,9});
        matNodes.add(new int[]{1,2,4,5,6,7,8,9,10});

        for(int[] nodes: matNodes)
        {
            MpiConfiguration n_ML = new MpiConfiguration("");

            for (int node: nodes)
                n_ML.getHostList().add(new MpiHost("matthau-5-"+node, 8, 1));

            n_ML.setName("MATTHAU_"+n_ML.getTotalNumProcessors());
            n_ML.setRemoteLogin(matlemLogin);
            n_ML.setMpiVersion(MpiSettings.OPENMPI_V2);
            n_ML.setUseScp(true);
            configurations.add(n_ML);
        }
        
        /*p_ML.getHostList().add(new MpiHost("matthau-5-5", 8, 1));
        p_ML.getHostList().add(new MpiHost("matthau-5-6", 8, 1));
        p_ML.getHostList().add(new MpiHost("matthau-5-7", 8, 1));
        p_ML.getHostList().add(new MpiHost("matthau-5-8", 8, 1));
        p_ML.getHostList().add(new MpiHost("matthau-5-9", 8, 1));
        p_ML.getHostList().add(new MpiHost("matthau-5-10", 8, 1));

        p_ML.getHostList().add(new MpiHost("lemmon-5-1", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-2", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-3", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-4", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-5", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-6", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-7", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-8", 8, 1));
        //p_ML.getHostList().add(new MpiHost("lemmon-5-9", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-10", 8, 1));

        p_ML.getHostList().add(new MpiHost("lemmon-5-11", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-12", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-13", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-14", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-15", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-16", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-17", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-18", 8, 1));
        p_ML.getHostList().add(new MpiHost("lemmon-5-19", 8, 1));
        //p_ML.getHostList().add(new MpiHost("lemmon-5-20", 8, 1));*/

        /*
        for(int i=1;i<=10;i++)
        {
            p_ML.getHostList().add(new MpiHost(MATTHAU+i, 8, 1));
        }

        for(int i=1;i<=20;i++)
        {
            p_ML.getHostList().add(new MpiHost(LEMMON+i, 8, 1));
        }*/
        
        
        
        if (getMpiConfiguration(NSG_1PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(NSG_1PROC);

            for(int i=0;i<1;i++)
                p.getHostList().add(new MpiHost("node"+i,1, 1));

            p.setRemoteLogin(nsgLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(nsgQueue);
            configurations.add(p);
        }
        
        if (getMpiConfiguration(NSG_8PROC)==null)
        {
            MpiConfiguration p = new MpiConfiguration(NSG_8PROC);

            for(int i=0;i<1;i++)
                p.getHostList().add(new MpiHost("node"+i,8, 1));

            p.setRemoteLogin(nsgLogin);
            p.setMpiVersion(MpiSettings.OPENMPI_V2);
            p.setUseScp(true);
            p.setQueueInfo(nsgQueue);
            configurations.add(p);
        }



    }

    /*
    public void setVersion(String version)
    {
        this.version = version;
    }*/

    protected static String getMPIVersion()
    {
        File mpichV1flag = new File(ProjectStructure.getnCHome(), "MPICH1");
        File mpichV2flag = new File(ProjectStructure.getnCHome(), "MPICH2");
        File openmpiV2flag = new File(ProjectStructure.getnCHome(), "MPI2");
        
        if (mpichV1flag.exists()) return MPICH_V1;
        if (mpichV2flag.exists()) return MPICH_V2;
        if (openmpiV2flag.exists()) return OPENMPI_V2;
        
        return DEFAULT_MPI_VERSION;
    }

    public ArrayList<MpiConfiguration> getMpiConfigurations()
    {
        return this.configurations;
    }

    public void setMpiConfigurations(ArrayList<MpiConfiguration> confs)
    {
        this.configurations = confs;
    }

    public final MpiConfiguration getMpiConfiguration(String name)
    {
        for (MpiConfiguration config: configurations)
        {
            if (config.getName().equals(name)) return config;
        }

        return null;
    }

    public static void main(String[] args)
    {
        new MpiSettings();
    }

}




