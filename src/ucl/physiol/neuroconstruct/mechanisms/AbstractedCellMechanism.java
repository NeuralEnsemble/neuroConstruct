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

package ucl.physiol.neuroconstruct.mechanisms;

import java.io.*;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;

/**
 * Base class for all NON-ChannelML Cell Mechanisms. These are Cell Mechanism where a number of
 * parameters are specified for the mechanism and these can be edited through the
 * GUI and then plugged into the MechanismImplementations to create the native
 * script file.
 *                **Strongly advised to use ChannelML instead**
 *
 * @author Padraig Gleeson
 *  
 *
 */

public abstract class AbstractedCellMechanism extends CellMechanism
{
    ClassLogger logger = new ClassLogger("AbstractedCellMechanism");


    private String defaultInstanceName = null;

    InternalPhysicalParameter[] physParamList = new InternalPhysicalParameter[]{};

    MechanismImplementation[] mechanismImpls = new MechanismImplementation[]{};

    String plotInfoFile = null;

    // Needs to be public for XMLEncoder...
    public AbstractedCellMechanism()
    {

    }



    /**
     * Calling this ensures the files are copied from the native cell mech templates directory
     */
    public void initialise(Project project)
    {

        for (int i = 0; i < mechanismImpls.length; i++)
        {
            File testFile = mechanismImpls[i].getImplementingFileObject(project, this.getInstanceName());

            logger.logComment("Got a file "+testFile.getAbsolutePath()+" for cell mech "+ this.getInstanceName());
        }

    }


    /**
     * This function is needed for automatic storage of the Parameters by XMLEncoder.
     * If the internal functioning of the subclasses only use these params, they don't need to worry
     * about data saving.
     */
    public InternalPhysicalParameter[] getPhysicalParameterList()
    {
        return physParamList;
    };

    /**
     * Sub classes should know what the parameters mean, and so this function needs
     * should be overridden to check values
    */
   public boolean setParameter(String parameterName, float parameterValue) throws CellMechanismException
   {
       logger.logComment("Setting: "+ parameterName);

       for (int i = 0; i < physParamList.length; i++)
       {
           //System.out.println("Comparing: "+physParamList[i].getParameterName()+" to "+parameterName);
           if(physParamList[i].getParameterName().equals(parameterName))
           {
               physParamList[i].setValue(parameterValue);
               return true;
           }
       }

       logger.logComment("Unknown parameter...");
       return false;

   };




   public boolean parameterExists(String parameterName)
   {
       for (int i = 0; i < physParamList.length; i++)
       {
           if(physParamList[i].getParameterName().equals(parameterName))
               return true;
       }
       return false;

   };


   public float getParameter(String parameterName) throws CellMechanismException
   {
       logger.logComment("Getting param: "+ parameterName);
       for (int i = 0; i < physParamList.length; i++)
       {
        //   System.out.println("Comparing: "+physParamList[i].getParameterName()+" to "+parameterName);
           if(physParamList[i].getParameterName().equals(parameterName))
               return physParamList[i].getValue();
       }
       throw new CellMechanismException("There is no paramter by the name: "+ parameterName);

   };

   public Units getParameterUnits(String parameterName) throws CellMechanismException
   {
       for (int i = 0; i < physParamList.length; i++)
       {
           if(physParamList[i].getParameterName().equals(parameterName))
               return physParamList[i].getUnits();
       }
       throw new CellMechanismException("There is no paramter by the name: "+ parameterName);

   };


    public void setParameterDefault(String parameterName, float defaultValue) throws CellMechanismException
    {
        for (int i = 0; i < physParamList.length; i++)
        {
            if(physParamList[i].getParameterName().equals(parameterName))
                physParamList[i].setDefaultValue(defaultValue);
        }

    };

    public void setPhysicalParameterList(InternalPhysicalParameter[] physParamList)
    {
        this.physParamList = physParamList;
    }

    public void addNewParameter(String parameterName,
                                   String description,
                                   float defaultValue,
                                   Units units)
    {
        InternalPhysicalParameter paramNew
            = new InternalPhysicalParameter(parameterName,
                                            description,
                                            defaultValue,
                                            units);

        InternalPhysicalParameter[] tempParams
            = new InternalPhysicalParameter[physParamList.length+1];

        for (int i = 0; i < physParamList.length; i++)
        {
            tempParams[i] =    physParamList[i];
        }
        tempParams[physParamList.length] = paramNew;
        physParamList = tempParams;
    }

    public void printDetails()
    {
        logger.logComment("---------------------------------------------------------------");
        logger.logComment("Info on CellMechanism: "+ mechanismModel);
        logger.logComment("MechanismType        : "+ mechanismType);
        logger.logComment("Instance             : "+ instanceName);
        logger.logComment("Description          : "+ description);
        logger.logComment(" ");
        logger.logComment("Number of parameters : "+ physParamList.length);
        for (int i = 0; i < physParamList.length; i++)
        {
            logger.logComment("Parameter "+i+": "
                              + physParamList[i].getParameterName()
                              + ", value: "+ physParamList[i].getValue() + " "
                              + physParamList[i].units.getSymbol()
                              + " (default: "+ physParamList[i].getDefaultValue()+" "

                              + physParamList[i].units.getSymbol()+")");
        }
        logger.logComment(" ");
        for (int i = 0; i < mechanismImpls.length; i++)
        {
            logger.logComment("Mechanism Implementation "+i+", Simulation env: "
                              + mechanismImpls[i].getSimulationEnvironment() + ", File: "
                              + mechanismImpls[i].getImplementingFile());
        }
        logger.logComment(    "File for plots    : "+ getPlotInfoFile());

        logger.logComment("---------------------------------------------------------------");
    }

    public boolean createImplementationFile(String targetEnv,
                                            int unitsSystem,
                                            File fileToGenerate,
                                            Project project,
                                            boolean requiresCompilation,
                                            boolean includeComments,
                                            boolean forceCorrectInit,
                                            boolean parallelMode)
    {
        logger.logComment("Creating file for env: "+targetEnv+" file: "+ fileToGenerate);
        for (int i = 0; i < mechanismImpls.length; i++)
        {
            if(mechanismImpls[i].getSimulationEnvironment().equals(targetEnv))
            {
                logger.logComment("Found suitable sim env");
                try
                {
                    mechanismImpls[i].createMechanismFile(this, unitsSystem, fileToGenerate, project);
                    return true;
                }
                catch (Exception ex)
                {
                    GuiUtils.showErrorMessage(logger,
                                              "Error creating implementation of Cell Mechanism: "+this.getInstanceName(),
                                              ex,
                                              null);
                    return false;
                }
            }
        }
        return false;
    }


    /**
     * To ensure the base classes clone the object properly
     */
    public abstract Object clone();

    public String toString()
    {
        return getClass().getName().substring(getClass().getName().lastIndexOf(".")+1)
            + " [Instance name: " + this.instanceName
            + ", type: " + this.mechanismType
            + ", model: " + this.mechanismModel+"]";
    }


    public static void main(String[] args)
    {

    }

    public MechanismImplementation[] getMechanismImpls()
    {
        return mechanismImpls;
    }

    public void setMechanismImpls(MechanismImplementation[] mechImpls)
    {
        this.mechanismImpls = mechImpls;
    }


    public void specifyNewImplFile(String simEnv, String implFile)
    {
        logger.logComment("Adding implementation file "+implFile+" for env: "+ simEnv);
        MechanismImplementation newMechImpl = new MechanismImplementation(simEnv, implFile);

        MechanismImplementation[] tempMechImpls = new MechanismImplementation[mechanismImpls.length + 1];
        for (int i = 0; i < mechanismImpls.length; i++)
        {
            if (mechanismImpls[i].getSimulationEnvironment().equals(simEnv))
            {
                mechanismImpls[i] = newMechImpl;
                return;
            }
            tempMechImpls[i] = mechanismImpls[i];
        }
        tempMechImpls[mechanismImpls.length] = newMechImpl;
        mechanismImpls = tempMechImpls;
    }





    /**
     * Returns getMechanismModel() with a list of available simulators attached
     */
    public String getMechanismModelAndSims()
    {
        String modelAndSims = new String(getMechanismModel());

        for (int k = 0; k < mechanismImpls.length; k++)
        {
            if (k == 0) modelAndSims = modelAndSims + " [";
            MechanismImplementation next = (MechanismImplementation) mechanismImpls[k];
            modelAndSims = modelAndSims + next.getSimulationEnvironment();
            if (k == mechanismImpls.length - 1)
                modelAndSims = modelAndSims + "]";
            else
                modelAndSims = modelAndSims + ", ";
        }

        return mechanismModel;
    }


    public String getDefaultInstanceName()
    {
        return defaultInstanceName;
    }

    public void setDefaultInstanceName(String defInstName)
    {
        if (this.getInstanceName()==null) this.instanceName = defInstName;
        defaultInstanceName = defInstName;
    }


    public String getPlotInfoFile()
    {
        return plotInfoFile;
    }
    public void setPlotInfoFile(String plotInfoFile)
    {
        this.plotInfoFile = plotInfoFile;
    }



}
