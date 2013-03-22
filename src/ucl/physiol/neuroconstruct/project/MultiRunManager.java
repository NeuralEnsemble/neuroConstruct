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

package ucl.physiol.neuroconstruct.project;

import ucl.physiol.neuroconstruct.utils.SequenceGenerator;
import java.util.ArrayList;
import java.io.File;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.neuron.NeuronFileManager;
import ucl.physiol.neuroconstruct.genesis.*;
import javax.swing.JOptionPane;

/**
 * Helper class for managing multiple runs
 *
 * @author Padraig Gleeson
 *  
 */

public class MultiRunManager
{
    Project project = null;

    ArrayList<ParameterRange> paramRanges = new ArrayList<ParameterRange>();

    SimConfig simConfig = null;

    public MultiRunManager(Project project, SimConfig simConfig, String simBaseName)
    {
        ArrayList<String> inputs = simConfig.getInputs();

        this.project = project;

        this.simConfig = simConfig;

        for (String stimName: inputs)
        {
            StimulationSettings stim = project.elecInputInfo.getStim(stimName);
            if (stim instanceof IClampSettings)
            {
                IClampSettings iclamp = (IClampSettings)stim;

                SequenceGenerator delSeqGen =  iclamp.getDelay();
                if (delSeqGen.getNumInSequence()>1)
                {
                    ParameterRange pr = new ParameterRange(iclamp.getReference(),
                                                           iclamp.getDelay(),
                                                           ParameterRange.STIM_DELAY);
                    paramRanges.add(pr);
                }
                SequenceGenerator durSeqGen =  iclamp.getDuration();
                if (durSeqGen.getNumInSequence()>1)
                {
                    ParameterRange pr = new ParameterRange(iclamp.getReference(),
                                                           iclamp.getDuration(),
                                                           ParameterRange.STIM_DURATION);
                    paramRanges.add(pr);
                }
                SequenceGenerator ampSeqGen =  iclamp.getAmplitude();
                if (ampSeqGen.getNumInSequence()>1)
                {
                    ParameterRange pr = new ParameterRange(iclamp.getReference(),
                                                           iclamp.getAmplitude(),
                                                           ParameterRange.STIM_AMPLITUDE);
                    paramRanges.add(pr);
                }
            }
        }
    }

    public boolean isMultiRunSimulation()
    {
        return !this.paramRanges.isEmpty();
    }

    public String getMultiRunPreScript(String simEnv)
    {
        StringBuilder response = new StringBuilder();
        if (isMultiRunSimulation())
        {
            addComment("Adding info for multiple runs", simEnv, response);
            StringBuilder simNameFormat = new StringBuilder();
            StringBuilder simNameFormatVals = new StringBuilder();

            if (simEnv.equals(SimEnvHelper.NEURON))
            {
                response.append("strdef origSimReference\n");
            }
            else if (simEnv.equals(SimEnvHelper.GENESIS))
            {
                response.append("str origSimReference\n");

            }
            response.append("origSimReference = simReference\n\n");


            for (ParameterRange pr: paramRanges)
            {
                String comment = pr.getReference() + " with values: "
                    + pr.getSeqGen() + " (" + pr.getSeqGen().getNumInSequence() + " distinct vals)";

                this.addComment(comment, simEnv, response);

                String indexName = pr.getIndexName();

                simNameFormat.append("_%d");

                if (simEnv.equals(SimEnvHelper.NEURON))
                {
                    simNameFormatVals.append(", "+ indexName);

                    response.append("for " + indexName + " = 0, " + (pr.getSeqGen().getNumInSequence() - 1) + " {\n\n");

                    response.append("for i = 0, " + pr.getObjectArraySizeName() + "-1 {\n");
                    response.append("    " + pr.getObjectArrayName() + "[i]."+pr.getVariableName(simEnv)+" = " + pr.getSeqGen().getStart()
                                    + " + (" + indexName + " * " + pr.getSeqGen().getInterval() + ")\n");
                    response.append("}\n\n");
                }
                else if (simEnv.equals(SimEnvHelper.GENESIS))
                {
                    simNameFormatVals.append(" @ \"_\" @ "+ indexName);

                    response.append("int " + indexName+"\n");
                    response.append("for (" + indexName + " = 0; " + indexName + " <= " + (pr.getSeqGen().getNumInSequence() - 1) + "; " + indexName + " = " + indexName + " + 1)\n\n");

                    //response.append("for i = 0, " + pr.getObjectArraySizeName() + "-1 {\n");
                    //response.append("    " + pr.getObjectArrayName() + "[i]."+pr.getVariableName(simEnv)+" = " + pr.getSeqGen().getStart()
                    //                + " + (" + indexName + " * " + pr.getSeqGen().getInterval() + ")\n");
                    //response.append("end\n\n");


                }
            }

            if (simEnv.equals(SimEnvHelper.NEURON))
            {
                response.append("sprint(simReference, \"%s__" + simNameFormat.toString().substring(1)
                                + "\", origSimReference, " + simNameFormatVals.toString().substring(1) + ")\n\n");

                response.append("sprint(targetDir, \"%s%s/\", simsDir, simReference)\n\n");
            }
            else if (simEnv.equals(SimEnvHelper.GENESIS))
            {
                response.append("simReference = {origSimReference} @ \"__\" "
                                + simNameFormatVals.toString() + "\n\n");


                response.append("targetDir = {strcat {simsDir} {simReference}}\n\n");

            }

            addConsoleOut("Simulation reference:     ", "simReference", simEnv, response);
            addConsoleOut("Simulation target dir:    ", "targetDir", simEnv, response);

        }
        else
        {
            addComment("Single simulation run...", simEnv, response);

        }
        return response.toString();

    }

    public String getMultiRunPostScript(String simEnv)
    {
        StringBuilder response = new StringBuilder();
        if (isMultiRunSimulation())
        {

            addComment("Finished adding info for multiple runs", simEnv, response);
            for (ParameterRange pr : paramRanges)
            {
                if (simEnv.equals(SimEnvHelper.NEURON))
                {
                    response.append("propsFile.printf(\"" + pr.getReference() + "." + pr.getVariableName(simEnv)
                                    + "=%g\\n\", " + pr.getObjectArrayName() + "[0]." + pr.getVariableName(simEnv) +
                                    ")\n");
                }
                else if (simEnv.equals(SimEnvHelper.GENESIS))
                {

                }
            }

            for (ParameterRange pr: paramRanges)
            {

                this.addComment("End of loop for: "+pr, simEnv, response);
                if (simEnv.equals(SimEnvHelper.NEURON))
                {
                    response.append("}\n\n");
                }
                else if (simEnv.equals(SimEnvHelper.GENESIS))
                {
                    response.append("end\n\n");
                }

            }
        }
        return response.toString();

    }


    /**
     * Generate a warning based on the number of parameters described by SequenceGenerators
     */
    public boolean checkMultiJobSettings()
    {
        ArrayList<String> sequences = new ArrayList<String>();

        int totalNumberJobsToRun = 1;

        if (isMultiRunSimulation())
        {
            for (ParameterRange pr : paramRanges)
            {
                totalNumberJobsToRun = totalNumberJobsToRun * pr.getSeqGen().getNumInSequence();
                sequences.add(pr.getReference()+":"+pr.getParamType()+" has value: "+ pr.getSeqGen() +" ("+pr.getSeqGen().getNumInSequence()+" distinct vals)");

            }
        }
        else
        {
            return true;
        }

        if (totalNumberJobsToRun>1)
        {
;           StringBuilder mess = new StringBuilder("Please note that this will result in "+totalNumberJobsToRun+" separate simulations.\n");
            mess.append("The parameters over which the simulation will be run are: \n\n");
            for (String next: sequences)
            {
                mess.append(next+"\n");
            }
            mess.append("\nGenerate the simulation files?\n");

            int cont = JOptionPane.showConfirmDialog(null, mess.toString(), "Confirm multiple job runs", JOptionPane.YES_NO_OPTION);

            return (cont==JOptionPane.YES_OPTION);
        }

        return true;
    }



    public ArrayList<String> getGeneratedSimReferences()
    {
        ArrayList<String> dirList = new ArrayList<String>();

        if (isMultiRunSimulation())
        {
            ArrayList<String> prevList = new ArrayList<String>();
            prevList.add(project.simulationParameters.getReference()+"_");

            for (ParameterRange pr: paramRanges)
            {
                ArrayList<String> nextList = new ArrayList<String>();

                for (String prevDir: prevList)
                {
                    for (int i = 0; i < pr.getSeqGen().getNumInSequence(); i++)
                    {
                        nextList.add(prevDir+"_"+i);
                    }
                }
                prevList = nextList;
            }
            dirList = prevList;
        }
        else
        {
            dirList.add(project.simulationParameters.getReference());
        }
        return dirList;

    }


    public String addComment(String comment, String simEnv, StringBuilder response)
    {

        if (simEnv.equals(SimEnvHelper.NEURON))
            NeuronFileManager.addHocComment(response, comment);

        else if (simEnv.equals(SimEnvHelper.GENESIS))
            GenesisFileManager.addComment(response, comment);

        return response.toString();
    }

    public String addConsoleOut(String comment, String simEnv, StringBuilder response)
    {

        if (simEnv.equals(SimEnvHelper.NEURON))
        {
            if (project.neuronSettings.isGenerateComments())
                response.append("print \"" + comment + "\"\n\n");
        }
        else if (simEnv.equals(SimEnvHelper.GENESIS))
        {
            if (project.genesisSettings.isGenerateComments())
                response.append("echo \"" + comment + "\"\n\n");
        }
        return response.toString();
    }

    public String addConsoleOut(String comment,String variableName, String simEnv, StringBuilder response)
    {

        if (simEnv.equals(SimEnvHelper.NEURON))
        {
            if (project.neuronSettings.isGenerateComments())
                response.append("print \"" + comment + "\", "+variableName+"\n\n");
        }
        else if (simEnv.equals(SimEnvHelper.GENESIS))
        {
            if (project.genesisSettings.isGenerateComments())
                response.append("echo \"" + comment + "\" "+variableName+"\n\n");
        }
        return response.toString();
    }



    public class ParameterRange
    {
        public static final String STIM_AMPLITUDE = "Amplitude";
        public static final String STIM_DELAY = "Delay";
        public static final String STIM_DURATION = "Duration";

        String paramType = null;
        String reference = null;
        SequenceGenerator seqGen = null;

        private ParameterRange()
        {

        }

        public ParameterRange(String reference,
                              SequenceGenerator seqGen,
                              String paramType)
        {
            this.reference = reference;
            this.seqGen = seqGen;
            this.paramType = paramType;
        }



        public String toString()
        {
            return "ParameterRange "+paramType+": " +reference+" "+ seqGen;
        }

        public String getIndexName()
        {
            return "index_" + getParamType() + "_" + getReference();
        }

        public String getObjectArrayName()
        {
            return "stim_" + getReference();
        }
        public String getObjectArraySizeName()
        {
            return "n_stim_" + getReference();
        }



        public String getVariableName(String simEnv)
        {
            if (simEnv.equals(SimEnvHelper.NEURON))
            {
                if (paramType.equals(STIM_AMPLITUDE))
                {
                    return "amp";
                }
                else if (paramType.equals(STIM_DELAY))
                {
                    return "del";
                }
                else if (paramType.equals(STIM_DURATION))
                {
                    return "dur";
                }
            }
            else if (simEnv.equals(SimEnvHelper.GENESIS))
            {
                if (paramType.equals(STIM_AMPLITUDE))
                {
                    return "amp";
                }
                else if (paramType.equals(STIM_DELAY))
                {
                    return "del";
                }
                else if (paramType.equals(STIM_DURATION))
                {
                    return "dur";
                }
            }
            return null;

        }



        public String getReference()
        {
            return reference;
        }

        public String getParamType()
        {
            return paramType;
        }

        public SequenceGenerator getSeqGen()
        {
            return seqGen;
        }




    }

    public static void main(String[] args)
    {
        //MultiRunManager multirunmanager = new MultiRunManager();

        try
        {
            Project testProj = Project.loadProject(new File("projects/ggg/ggg.neuro.xml"),
                                                   new ProjectEventListener()
            {
                public void tableDataModelUpdated(String tableModelName)
                {
                };

                public void tabUpdated(String tabName)
                {
                };
                public void cellMechanismUpdated()
                {
                };

            });

            MultiRunManager multirunmanager = new MultiRunManager(testProj, testProj.simConfigInfo.getDefaultSimConfig(), "TestSim");

            System.out.println("multirunmanager: "+ multirunmanager.paramRanges);
            System.out.println("dirs for sims: "+ multirunmanager.getGeneratedSimReferences());



        }
        catch (ProjectFileParsingException ex)
        {
            ex.printStackTrace();
        }

    }
}
