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
    public static final String SBML_MECHANISM = "SBML mechanism";
    public static final String NEUROML2_ION_CHANNEL = "NeuroML 2 ion channel";
    public static final String NEUROML2_CONC_MODEL = "NeuroML 2 concentration model";
    public static final String NEUROML2_ABSTRACT_CELL = "NeuroML 2 Abstract cell";

    /**
     * Implementation methods
     */
    public static final String CHANNELML_BASED_CELL_MECHANISM = "ChannelML based Cell Mechanism";
    public static final String SBML_BASED_CELL_MECHANISM = "SBML based Cell Mechanism";
    public static final String NEUROML2_BASED_CELL_MECHANISM = "NeuroML 2 Component";
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
        return getMechanismType().equals(CellMechanism.CHANNEL_MECHANISM) || 
                getMechanismType().equals(CellMechanism.NEUROML2_ION_CHANNEL);
    }

    public boolean isPointProcess()
    {
        return getMechanismType().equals(CellMechanism.POINT_PROCESS);
    }



    public boolean isGapJunctionMechanism()
    {
        return getMechanismType().equals(CellMechanism.GAP_JUNCTION);
    }
    
    public boolean isSynapticMechanism()
    {
        return getMechanismType().equals(CellMechanism.SYNAPTIC_MECHANISM);
    }

    public boolean isIonConcMechanism()
    {
        return getMechanismType().equals(CellMechanism.ION_CONCENTRATION) ||
                getMechanismType().equals(CellMechanism.NEUROML2_CONC_MODEL);
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
                                           boolean includeComments,
                                            boolean forceCorrectInit,
                                            boolean parallelMode);

}
