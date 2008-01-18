/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2007 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.mechanisms;

import ucl.physiol.neuroconstruct.utils.*;
import java.io.File;
import ucl.physiol.neuroconstruct.project.Project;

/**
 * Base class for all Cell Mechanisms (Abstract or ChannelML)
 *
 * @author Padraig Gleeson
 *  
 */

public abstract class CellMechanism
{
    protected static ClassLogger logger = new ClassLogger("CellMechanism");

    /**
     *  Mechanism types
     */
    public static final String CHANNEL_MECHANISM = "Channel mechanism";
    public static final String SYNAPTIC_MECHANISM = "Synaptic mechanism";
    public static final String ION_CONCENTRATION = "Ion concentration";
    public static final String POINT_PROCESS = "Point process";
    public static final String GAP_JUNCTION = "Gap junction";

    /**
     * Implementation methods
     */
    public static final String CHANNELML_BASED_CELL_MECHANISM = "ChannelML based Cell Mechanism";
    public static final String ABSTRACTED_CELL_MECHANISM = "Abstracted Cell Mechanism";
    public static final String FILE_BASED_CELL_MECHANISM = "File based Cell Mechanism";


    protected String instanceName = null;
    protected String description = null;
    protected String mechanismModel = null;

    /**
     * Channel or Synaptic mechanism or Ion concentration
     */
    protected String mechanismType = null;

    // Needs to be public for XMLEncoder...
    public CellMechanism()
    {

    }


    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getInstanceName()
    {
        return instanceName;
    }
    public void setInstanceName(String instanceName)
    {
        this.instanceName = instanceName;
    }

    public String getMechanismModel()
   {
       return mechanismModel;
   }

   /**
    * Channel or Synaptic mechanism or Ion concentration
    */
   public String getMechanismType()
   {
       return mechanismType;
   }
   
   
    public boolean isChannelMechanism()
    {
        return getMechanismType().equals(CellMechanism.CHANNEL_MECHANISM);
    }

    public boolean isPointProcess()
    {
        return getMechanismType().equals(CellMechanism.POINT_PROCESS);
    }



    public boolean isSynapticMechanism()
    {
        return getMechanismType().equals(CellMechanism.SYNAPTIC_MECHANISM);
    }

    public boolean isIonConcMechanism()
    {
        return getMechanismType().equals(CellMechanism.ION_CONCENTRATION);
    }


   public void setMechanismModel(String mechanismModel)
   {
       this.mechanismModel = mechanismModel;
   }

   /**
    * Channel or Synaptic mechanism or Ion concentration
    */
   public void setMechanismType(String mechanismType)
   {
       this.mechanismType = mechanismType;
   }


   public abstract boolean createImplementationFile(String targetEnv,
                                           int unitsSystem,
                                           File fileToGenerate,
                                           Project project,
                                           boolean requiresCompilation,
                                           boolean includeComments);

}
