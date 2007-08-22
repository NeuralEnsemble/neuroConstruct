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

public class CellMechanismInfo extends AbstractTableModel
{
    ClassLogger logger = new ClassLogger("CellMechanismInfo");

    public final static int COL_NUM_INSTANCE_NAME = 0;
    public final static int COL_NUM_MECHANISM_TYPE = 1;
    public final static int COL_NUM_MECHANISM_MODEL = 2;
    public final static int COL_NUM_DESC = 3;
    public final static int COL_NUM_SIM_ENVS = 4;

    final String[] columnNames = new String[5];

    Vector allCellMechanisms = new Vector();


    public CellMechanismInfo()
    {
        columnNames[COL_NUM_INSTANCE_NAME] = new String("Mechanism Instance Name");
        columnNames[COL_NUM_MECHANISM_TYPE] = new String("Mechanism Type");
        columnNames[COL_NUM_MECHANISM_MODEL] = new String("Mechanism Model");
        columnNames[COL_NUM_DESC] = new String("Description");
        columnNames[COL_NUM_SIM_ENVS] = new String("Simulation Environments");
    }


    public int getRowCount()
    {
        return allCellMechanisms.size();
    }
    public int getColumnCount()
    {
        return columnNames.length;
    }
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
                    StringBuffer sb = new StringBuffer();
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
        if (allCellMechanisms.elementAt(rowIndex) instanceof ChannelMLCellMechanism)
        {
            ChannelMLCellMechanism cmlMech = (ChannelMLCellMechanism) allCellMechanisms.elementAt(rowIndex);

            if (cmlMech == null) return null;

            switch (columnIndex)
            {
                case COL_NUM_INSTANCE_NAME:
                    return cmlMech.getInstanceName();
                case COL_NUM_MECHANISM_TYPE:
                    return cmlMech.getMechanismType();
                case COL_NUM_MECHANISM_MODEL:
                    return cmlMech.getMechanismModel();
                case COL_NUM_DESC:
                    return cmlMech.getDescription();

                case COL_NUM_SIM_ENVS:
                {

                    ArrayList<SimXSLMapping> simMappings = cmlMech.getSimMappings();
                    StringBuffer sb = new StringBuffer();
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



        return null;

    }


    public void addCellMechanism(CellMechanism cellMech)
    {
        logger.logComment("Adding cell mech: " + cellMech);
        allCellMechanisms.add(cellMech);

        this.fireTableStructureChanged();
    }






    public void reinitialiseCMLMechs(Project project) throws ChannelMLException
    {
        logger.logComment("reinitialiseCMLMechs...");
        for (int i = 0; i < getRowCount(); i++)
        {
            CellMechanism nextCellMech = (CellMechanism) getCellMechanismAt(i);
            if (nextCellMech instanceof ChannelMLCellMechanism)
            {
                ((ChannelMLCellMechanism)nextCellMech).initialise(project, false);
            }
        }
    }

    public Vector<String> getAllCellMechanismNames()
    {
        Vector<String> allNames = new Vector();
        for (int i = 0; i < getRowCount(); i++)
        {
               allNames.add((String)getValueAt(i, COL_NUM_INSTANCE_NAME));
        }
        return allNames;
    }




    public Vector getAllChannelMechanismNames()
    {
        Vector allNames = new Vector();
        for (int i = 0; i < getRowCount(); i++)
        {
                CellMechanism nextCellMech = (CellMechanism)getCellMechanismAt(i);
                logger.logComment("-------     Checking cell mechanism: " + nextCellMech);
                if (nextCellMech.getMechanismType().equals(CellMechanism.CHANNEL_MECHANISM))
                    allNames.add(nextCellMech.getInstanceName());

        }
        return allNames;
    }

    public Vector getChanMechsAndIonConcs()
    {
        Vector allNames = new Vector();
        for (int i = 0; i < getRowCount(); i++)
        {
                CellMechanism nextCellMech = (CellMechanism)getCellMechanismAt(i);
                logger.logComment("-------     Checking cell mechanism: " + nextCellMech);
                if (nextCellMech.getMechanismType().equals(CellMechanism.CHANNEL_MECHANISM) ||
                    nextCellMech.getMechanismType().equals(CellMechanism.ION_CONCENTRATION))
                    allNames.add(nextCellMech.getInstanceName());

        }
        return allNames;
    }


    public Vector getAllSynMechNames()
    {
        Vector allNames = new Vector();
        for (int i = 0; i < getRowCount(); i++)
        {
            CellMechanism nextCellMech = (CellMechanism) getCellMechanismAt(i);
            logger.logComment("-------     Checking cell mechanism: " + nextCellMech);
            if (nextCellMech.getMechanismType().equals(CellMechanism.SYNAPTIC_MECHANISM))
                allNames.add(nextCellMech.getInstanceName());
        }
        return allNames;
    }



    public CellMechanism getCellMechanismAt(int index)
    {
        return (CellMechanism)allCellMechanisms.elementAt(index);
    }


    public CellMechanism getCellMechanism(String instanceName)
    {
        for (int i = 0; i < getRowCount(); i++)
        {
            CellMechanism nextCellMech = (CellMechanism)getCellMechanismAt(i);
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
            CellMechanism nextCellMech = (CellMechanism)allCellMechanisms.elementAt(i);

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
    public Vector getAllCellMechanisms()
    {
        return allCellMechanisms;
    }



    /**
     * Added to allow storing of data by XMLEncoder. Should not normally be called!!!
     */
    public void setAllCellMechanisms(Vector allCellMechs)
    {
        this.allCellMechanisms = allCellMechs;
    }


}
