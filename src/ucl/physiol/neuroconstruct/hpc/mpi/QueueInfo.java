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

import java.util.ArrayList;
import javax.swing.JOptionPane;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.Section;
import ucl.physiol.neuroconstruct.project.Project;
import ucl.physiol.neuroconstruct.project.SimConfig;



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

    public static enum QueueType {PBS, SGE, LL};

    private QueueType queueType = null;

    private String mpirunPath = null;

    protected ArrayList<String> additionalSubOptions = new ArrayList<String>();

    public static final String submitScript = "subjob.sh";


    public QueueInfo()
    {
    }

    public QueueInfo(int wallTimeMins, String account, String launcherScript, QueueType queueType, String mpirunPath)
    {
        this.wallTimeMins = wallTimeMins;
        this.account = account;
        this.launcherScript = launcherScript;
        this.queueType = queueType;
        this.mpirunPath = mpirunPath;

    }


    @Override
    public Object clone()
    {
        QueueInfo q2 = new QueueInfo(wallTimeMins, account, launcherScript, queueType, mpirunPath);
        q2.additionalSubOptions.addAll(additionalSubOptions);
        return q2;

    }

    public String getSubmitCommand()
    {
        if (this.queueType.equals(QueueType.LL))
            return "llsubmit";
        else
            return "qsub";

    }
    public String getUserQueueStatusCommand()
    {
        if (this.queueType.equals(QueueType.LL))
            return "llq -u";
        else 
            return "qstat -u";

    }

    public static int getWallTimeSeconds(Project project, SimConfig simConfig)
    {
        int numCells = project.generatedCellPositions.getNumberInAllCellGroups();

        int totNseg = 0;

        for (String cg: simConfig.getCellGroups())
        {
            int numCellsHere = project.generatedCellPositions.getNumberInCellGroup(cg);

            if (numCellsHere>0)
            {
                int totNsegHere = 0;
                Cell c = project.cellManager.getCell(project.cellGroupsInfo.getCellType(cg));
                for(Section s: c.getAllSections())
                {
                    totNsegHere+=s.getNumberInternalDivisions();
                }
                totNseg +=(numCellsHere*totNsegHere);

            }
        }

        boolean check = true;
        int time = 5;

        while (simConfig.getMpiConf().getQueueInfo()!=null && check)
        {
            time = simConfig.getMpiConf().getQueueInfo().getWallTimeMins();

            String res = JOptionPane.showInputDialog("Simulation: "+project.simulationParameters.getReference()+
                    " to run for "+simConfig.getSimDuration()+" ms with dt: "+project.simulationParameters.getDt()+" ms" +
                    " on "+simConfig.getMpiConf().getTotalNumProcessors()+" processor cores.\n" +
                    "Total number of cells: "+numCells+"\n" +
                    "Total number of nseg for all sections: "+totNseg+"\n" +
                    "nseg per core: "+totNseg/(float)simConfig.getMpiConf().getTotalNumProcessors()+"\n\n" +
                "Please enter the time in minutes of the run: ", time);

            if (res ==null)
            {
                return 5;
            }

            try
            {
                float val = Float.parseFloat(res);

                time = Math.max(1,(int)val);

                check = false;
            }
            catch (Exception e)
            {
                check = true;
            }
        }
        return time;
    }

    public String getMpirunPath() {
        return mpirunPath;
    }


    public void addAdditionalSubOptions(String line)
    {
        additionalSubOptions.add(line);
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

    public QueueType getQueueType()
    {
        return queueType;
    }

    public void setQueueType(QueueType queueType)
    {
        this.queueType = queueType;
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
        if (this.queueType != other.queueType)
        {
            return false;
        }
        if (!this.additionalSubOptions.equals(other.additionalSubOptions))
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

    public static void main(String[] args)
    {
        QueueInfo qi1 = new QueueInfo(123, "submitScript", "submitScript", QueueInfo.QueueType.PBS, "submitScript");
        QueueInfo qi2 = new QueueInfo(123, "submitScript", "submitScript", QueueInfo.QueueType.PBS, "submitScript");

        System.out.println("Eq "+qi1.equals(qi2));
        System.out.println(qi1.hashCode()+ ", "+qi2.hashCode());

        qi1.addAdditionalSubOptions("fff");
        qi2.addAdditionalSubOptions("fff");

        System.out.println("Eq "+qi1.equals(qi2));
        System.out.println(qi1.hashCode()+ ", "+qi2.hashCode());

        QueueInfo qi3 = (QueueInfo)qi1.clone();
        System.out.println("Eq "+qi1.equals(qi3));
        System.out.println(qi1.hashCode()+ ", "+qi3.hashCode());

    }


}
