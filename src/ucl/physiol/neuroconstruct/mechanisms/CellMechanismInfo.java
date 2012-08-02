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

import java.util.*;

import javax.swing.table.*;

import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Extension of AbstractTableModel for storing Cell Mechanism info
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class CellMechanismInfo extends AbstractTableModel
{
    ClassLogger logger = new ClassLogger("CellMechanismInfo");

    public final static int COL_NUM_INSTANCE_NAME = 0;
    public final static int COL_NUM_MECHANISM_TYPE = 1;
    public final static int COL_NUM_MECHANISM_MODEL = 2;
    public final static int COL_NUM_DESC = 3;
    public final static int COL_NUM_SIM_ENVS = 4;

    final String[] columnNames = new String[5];

    @SuppressWarnings("UseOfObsoleteCollectionType")
    Vector<CellMechanism> allCellMechanisms = new Vector<CellMechanism>();


    public CellMechanismInfo()
    {
        columnNames[COL_NUM_INSTANCE_NAME] = "Mechanism Instance Name";
        columnNames[COL_NUM_MECHANISM_TYPE] = "Mechanism Type";
        columnNames[COL_NUM_MECHANISM_MODEL] = "Mechanism Model";
        columnNames[COL_NUM_DESC] = "Description";
        columnNames[COL_NUM_SIM_ENVS] = "Simulation Environments";
    }


    public int getRowCount()
    {
        return allCellMechanisms.size();
    }
    public int getColumnCount()
    {
        return columnNames.length;
    }
    
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        // there are two of these here to support pre-ChannelML type cell mechs
        if (allCellMechanisms.elementAt(rowIndex) instanceof AbstractedCellMechanism)
        {
            AbstractedCellMechanism cellMechanism = (AbstractedCellMechanism) allCellMechanisms.elementAt(rowIndex);

            if (cellMechanism == null) return null;

            switch (columnIndex)
            {
                case COL_NUM_INSTANCE_NAME:
                    return cellMechanism.getInstanceName();
                case COL_NUM_MECHANISM_TYPE:
                    return cellMechanism.getMechanismType();

                case COL_NUM_MECHANISM_MODEL:
                    return cellMechanism.getMechanismModel();

                case COL_NUM_DESC:
                    return cellMechanism.getDescription();

                case COL_NUM_SIM_ENVS:
                {
                    MechanismImplementation[] mechImpls = cellMechanism.getMechanismImpls();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mechImpls.length; i++)
                    {
                        sb.append(mechImpls[i].getSimulationEnvironment());
                        if (i < mechImpls.length - 1) sb.append(", ");
                    }
                    return sb.toString();
                }
                default:
                    return null;
            }
        }
        else if (allCellMechanisms.elementAt(rowIndex) instanceof XMLCellMechanism)
        {
            XMLCellMechanism xmlMech = (XMLCellMechanism) allCellMechanisms.elementAt(rowIndex);

            if (xmlMech == null) return null;

            switch (columnIndex)
            {
                case COL_NUM_INSTANCE_NAME:
                    return xmlMech.getInstanceName();
                case COL_NUM_MECHANISM_TYPE:
                    return xmlMech.getMechanismType();
                case COL_NUM_MECHANISM_MODEL:
                    return xmlMech.getMechanismModel();
                case COL_NUM_DESC:
                    return xmlMech.getDescription();

                case COL_NUM_SIM_ENVS:
                {

                    ArrayList<SimulatorMapping> simMappings = xmlMech.getSimMappings();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < simMappings.size(); i++)
                    {
                        sb.append(simMappings.get(i).getSimEnv());
                        if (i < simMappings.size() - 1) sb.append(", ");
                    }
                    return sb.toString();

                }
                default:
                    return null;
            }
        }

        return "<<< Problem getting cell mech info at ("+rowIndex+", "+columnIndex+") >>>";

    }


    public void addCellMechanism(CellMechanism cellMech)
    {
        logger.logComment("Adding cell mech: " + cellMech);
        allCellMechanisms.add(cellMech);

        this.fireTableStructureChanged();
    }






    public void reinitialiseCMLMechs(Project project) throws XMLMechanismException
    {
        logger.logComment("reinitialiseCMLMechs...");
        for (int i = 0; i < getRowCount(); i++)
        {
            CellMechanism nextCellMech = getCellMechanismAt(i);
            if (nextCellMech instanceof XMLCellMechanism)
            {
                ((XMLCellMechanism)nextCellMech).initialise(project, false);
            }
        }
    }

    public ArrayList<String> getAllCellMechanismNames()
    {
        ArrayList<String> allNames = new ArrayList<String>();
        for (int i = 0; i < getRowCount(); i++)
        {
               allNames.add((String)getValueAt(i, COL_NUM_INSTANCE_NAME));
        }
        return allNames;
    }




    public Vector<String> getAllChannelMechanismNames()
    {
        Vector<String> allNames = new Vector<String>();
        for (int i = 0; i < getRowCount(); i++)
        {
                CellMechanism nextCellMech = getCellMechanismAt(i);
                logger.logComment("-------     Checking cell mechanism: " + nextCellMech);
                if (nextCellMech.getMechanismType().indexOf(CellMechanism.CHANNEL_MECHANISM)>=0)
                    allNames.add(nextCellMech.getInstanceName());

        }
        return allNames;
    }

    public Vector<String> getChanMechsAndIonConcs()
    {
        Vector<String> allNames = new Vector<String>();
        for (int i = 0; i < getRowCount(); i++)
        {
                CellMechanism nextCellMech = getCellMechanismAt(i);
                logger.logComment("-------     Checking cell mechanism: " + nextCellMech);
                if (nextCellMech.getMechanismType().indexOf(CellMechanism.CHANNEL_MECHANISM)>=0 ||
                    nextCellMech.isIonConcMechanism())
                    allNames.add(nextCellMech.getInstanceName());

        }
        return allNames;
    }

    public Vector<String> getSBMLMechs()
    {
        Vector<String> allNames = new Vector<String>();
        for (int i = 0; i < getRowCount(); i++)
        {
                CellMechanism nextCellMech = getCellMechanismAt(i);
                logger.logComment("-------     Checking cell mechanism: " + nextCellMech);
                if (nextCellMech.getMechanismType().equals(CellMechanism.SBML_MECHANISM))
                    allNames.add(nextCellMech.getInstanceName());

        }
        return allNames;
    }

    public Vector<String> getPointProcessess()
    {
        Vector<String> allNames = new Vector<String>();
        for (int i = 0; i < getRowCount(); i++)
        {
                CellMechanism nextCellMech = getCellMechanismAt(i);
                
                if (nextCellMech.getMechanismType().equals(CellMechanism.POINT_PROCESS) ||
                        nextCellMech.getMechanismType().equals(CellMechanism.NEUROML2_ABSTRACT_CELL))
                    allNames.add(nextCellMech.getInstanceName());

        }
        return allNames;
    }


    public Vector<String> getAllChemSynMechNames()
    {
        Vector<String> allNames = new Vector<String>();
        for (int i = 0; i < getRowCount(); i++)
        {
            CellMechanism nextCellMech = getCellMechanismAt(i);
            logger.logComment("-------     Checking cell mechanism: " + nextCellMech);
            if (nextCellMech.getMechanismType().indexOf(CellMechanism.SYNAPTIC_MECHANISM)>=0)
                allNames.add(nextCellMech.getInstanceName());
        }
        return allNames;
    }


    public Vector<String> getAllChemElecSynMechNames()
    {
        Vector<String> allNames = new Vector<String>();
        for (int i = 0; i < getRowCount(); i++)
        {
            CellMechanism nextCellMech = getCellMechanismAt(i);
            logger.logComment("-------     Checking cell mechanism: " + nextCellMech);
            if (nextCellMech.getMechanismType().indexOf(CellMechanism.SYNAPTIC_MECHANISM)>=0 ||
                nextCellMech.getMechanismType().indexOf(CellMechanism.GAP_JUNCTION)>=0)
                allNames.add(nextCellMech.getInstanceName());
        }
        return allNames;
    }

    public Vector<String> getAllElecSynMechNames()
    {
        Vector<String> allNames = new Vector<String>();
        for (int i = 0; i < getRowCount(); i++)
        {
            CellMechanism nextCellMech = getCellMechanismAt(i);
            logger.logComment("-------     Checking cell mechanism: " + nextCellMech);
            if (nextCellMech.getMechanismType().indexOf(CellMechanism.GAP_JUNCTION)>=0)
                allNames.add(nextCellMech.getInstanceName());
        }
        return allNames;
    }



    public CellMechanism getCellMechanismAt(int index)
    {
        return allCellMechanisms.elementAt(index);
    }


    public CellMechanism getCellMechanism(String instanceName)
    {
        for (int i = 0; i < getRowCount(); i++)
        {
            CellMechanism nextCellMech = getCellMechanismAt(i);
            if (nextCellMech.getInstanceName().equals(instanceName))
                return nextCellMech;
        }
        logger.logComment("Channel mech: "+ instanceName+" not found...");
        return null;
    }


    public void updateCellMechanism(CellMechanism cellMechUpdated)
    {
        for (int i = 0; i < allCellMechanisms.size(); i++)
        {
            CellMechanism nextCellMech = allCellMechanisms.elementAt(i);

            if (nextCellMech!=null &&
            cellMechUpdated.getInstanceName()!=null &&
                cellMechUpdated.getInstanceName().equals(nextCellMech.getInstanceName()))
            {
                logger.logComment("Updating cell mechanism:");
                allCellMechanisms.setElementAt(cellMechUpdated, i);
                this.fireTableStructureChanged();
                return;
            }
        }
        // if it's not found
        addCellMechanism(cellMechUpdated);
    }

    public void deleteCellMechanism(CellMechanism cellMechToDelete)
    {
        allCellMechanisms.remove(cellMechToDelete);
        this.fireTableStructureChanged();
        return;

    }



    /**
     * Added to allow storing of data by XMLEncoder. Should not normally be called!!!
     */
    public Vector<CellMechanism> getAllCellMechanisms()
    {
        return allCellMechanisms;
    }



    /**
     * Added to allow storing of data by XMLEncoder. Should not normally be called!!!
     */
    public void setAllCellMechanisms(Vector<CellMechanism> allCellMechs)
    {
        this.allCellMechanisms = allCellMechs;
    }


}
