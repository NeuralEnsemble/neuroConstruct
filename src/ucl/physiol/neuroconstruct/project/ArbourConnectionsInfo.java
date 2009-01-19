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

import javax.swing.table.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;
/**
 * Stores info on volume/arbourisation based connections
 *
 * @author Padraig Gleeson
 *  
 */

public class ArbourConnectionsInfo extends AbstractTableModel
{

	private static final long serialVersionUID = -3868462712734389810L;

	ClassLogger logger = new ClassLogger("ArbourConnectionsInfo");

    public final static int COL_NUM_COMP_CONN_NAME = 0;
    public final static int COL_NUM_SOURCE = 1;
    public final static int COL_NUM_TARGET = 2;
    public final static int COL_NUM_SYNAPSE_LIST = 3;
    public final static int COL_NUM_SOURCE_REGION = 4;
    public final static int COL_NUM_CONN_CONDS = 5;
    public final static int COL_NUM_AP_SPEED = 6;
    public final static int COL_NUM_INHOMO_EXP = 7;


    final String[] columnNames = new String[8];

    Vector<String> vectorNames = new Vector<String>();
    Vector<String> vectorSource = new Vector<String>();
    Vector<String> vectorTarget = new Vector<String>();
    
    Vector<Vector<SynapticProperties>> vectorSynapseList = new Vector<Vector<SynapticProperties>>();
    Vector<Vector<String>> vectorSourceRegion = new Vector<Vector<String>>();
    Vector<ConnectivityConditions> vectorConnConds = new Vector<ConnectivityConditions>();


    Vector<Float> vectorApSpeed = new Vector<Float>();
    Vector<String> vectorInhExp = new Vector<String>();




    public ArbourConnectionsInfo()
    {
        logger.logComment("New ArbourConnectionsInfo created");
        columnNames[COL_NUM_COMP_CONN_NAME] = new String("Name");


        columnNames[COL_NUM_SOURCE] = new String("Source");
        columnNames[COL_NUM_TARGET] = new String("Target");
        columnNames[COL_NUM_SYNAPSE_LIST] = new String("Synapse List");
        columnNames[COL_NUM_SOURCE_REGION] = new String("Connection region");
        columnNames[COL_NUM_CONN_CONDS] = new String("Connectivity Conditions");
        columnNames[COL_NUM_AP_SPEED] = new String("AP speed");
        columnNames[COL_NUM_INHOMO_EXP] = new String("Connection probabiliy");


    }

    public int getRowCount()
    {
        return vectorNames.size();
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
        logger.logComment("Getting val at: " + rowIndex+", "+ columnIndex);
        switch (columnIndex)
        {
            case COL_NUM_COMP_CONN_NAME:
                return vectorNames.elementAt(rowIndex);
            case COL_NUM_SYNAPSE_LIST:
                return vectorSynapseList.elementAt(rowIndex);
            case COL_NUM_SOURCE:
                return vectorSource.elementAt(rowIndex);
            case COL_NUM_TARGET:
                return vectorTarget.elementAt(rowIndex);
            case COL_NUM_SOURCE_REGION:
                return vectorSourceRegion.elementAt(rowIndex);
            case COL_NUM_CONN_CONDS:
                return vectorConnConds.elementAt(rowIndex);
            case COL_NUM_AP_SPEED:
            {
                if (vectorApSpeed.size()==0)
                {
                    vectorApSpeed.setSize(vectorNames.size());
                    for (int i = 0; i < vectorNames.size(); i++)
                    {
                        vectorApSpeed.setElementAt(Float.MAX_VALUE, i);
                    }
                }
                return vectorApSpeed.elementAt(rowIndex);
            }
            case COL_NUM_INHOMO_EXP:
            {
                if (vectorInhExp.size()==0)
                {
                    vectorInhExp.setSize(vectorNames.size());
                    for (int i = 0; i < vectorNames.size(); i++)
                    {
                        String uniform = new String("1");
                        vectorInhExp.setElementAt(uniform, i);
                    }
                }
                return vectorInhExp.elementAt(rowIndex);
            }




            default:
                return null;
        }
    }

    public void setValueAt(Object value, int row, int col)
    {

        logger.logComment("Setting val at: " + row+", "+ col+" to: "+value);

        switch (col)
        {
            case COL_NUM_COMP_CONN_NAME:
                this.vectorNames.setElementAt((String)value , row);
                break;
            case COL_NUM_SYNAPSE_LIST:
                this.vectorSynapseList.setElementAt((Vector<SynapticProperties>)value , row);
                break;
            case COL_NUM_SOURCE:
                this.vectorSource.setElementAt((String)value , row);
                break;
            case COL_NUM_TARGET:
                this.vectorTarget.setElementAt((String)value , row);
                break;
            case COL_NUM_SOURCE_REGION:
            	Vector<String> v1 = new Vector<String>((Vector<String>)value);

            	
                this.vectorSourceRegion.setElementAt(v1 , row);
                break;
            case COL_NUM_CONN_CONDS:
                this.vectorConnConds.setElementAt((ConnectivityConditions)value , row);
                break;
            case COL_NUM_AP_SPEED:
                this.vectorApSpeed.setElementAt((Float)value , row);
                break;
            case COL_NUM_INHOMO_EXP:
                this.vectorInhExp.setElementAt((String)value , row);
                break;




        }
        fireTableCellUpdated(row, col);
    }


    public void addRow(String name,
                       String source,
                       String target,
                       Vector<SynapticProperties> synProps,
                       Vector<String> sourceRegions,
                       ConnectivityConditions connConds,
                       float jumpSpeed,
                       String eqn)  throws NamingException
    {
        int countSoFar = vectorNames.size();

        vectorNames.add(countSoFar, name);
        vectorSynapseList.add(countSoFar, synProps);
        vectorSource.add(countSoFar, source);
        vectorTarget.add(countSoFar, target);
        this.vectorSourceRegion.add(countSoFar, sourceRegions);
        vectorConnConds.add(countSoFar, connConds);
        this.vectorApSpeed.add(jumpSpeed);
        this.vectorInhExp.add(eqn);

        this.fireTableRowsInserted(countSoFar, countSoFar);
    }


    public boolean deleteConn(int index)
    {
        if (index<0 || index>=vectorNames.size()) return false;
        vectorNames.removeElementAt(index);
        vectorSynapseList.removeElementAt(index);
        vectorSource.removeElementAt(index);
        vectorTarget.removeElementAt(index);
        vectorSourceRegion.removeElementAt(index);
        vectorConnConds.removeElementAt(index);
        this.vectorApSpeed.removeElementAt(index);
        this.vectorInhExp.removeElementAt(index);


        this.fireTableRowsDeleted(index, index);
        return true;
    }


    public boolean deleteConn(String complexConnName)
    {
        return deleteConn(vectorNames.indexOf(complexConnName));
    }



    public boolean isCellEditable(int row, int col)
    {
        return false;
    }

    public int getNumConns()
    {
        return vectorNames.size();
    }

    public Vector<String> getAllAAConnNames()
    {
        return new Vector<String>(vectorNames);
    }

    public Vector<SynapticProperties> getSynapseList(String complexConnName)
    {
        int index = vectorNames.indexOf(complexConnName);
        Vector<SynapticProperties> synPropList = (Vector<SynapticProperties>) vectorSynapseList.elementAt(index);
        return synPropList;
    }

    public Vector<String> getSourceConnRegions(String complexConnName)
    {
        int index = vectorNames.indexOf(complexConnName);
        Vector<String> regs = (Vector<String>) vectorSourceRegion.elementAt(index);
        return regs;
    }


    public void setSynapseProperties(String complexConnName, Vector synPropList)
    {
        int index = vectorNames.indexOf(complexConnName);
        this.setValueAt(synPropList, index, COL_NUM_SYNAPSE_LIST);
    }


    public String getSourceCellGroup(String complexConnName)
    {
        if (!vectorNames.contains(complexConnName)) return null;
        int index = vectorNames.indexOf(complexConnName);
        String source = (String) vectorSource.elementAt(index);
        return source;
    }

    public void setSourceCellGroup(String complexConnName, String source)
    {
        int index = vectorNames.indexOf(complexConnName);
        this.setValueAt(source, index, COL_NUM_SOURCE);
    }

    public void setSourceConnRegions(String complexConnName, Vector<String> regs)
    {
        int index = vectorNames.indexOf(complexConnName);
        this.setValueAt(regs, index, COL_NUM_SOURCE_REGION);
    }



    public String getTargetCellGroup(String complexConnName)
    {
        if (!vectorNames.contains(complexConnName)) return null;
        int index = vectorNames.indexOf(complexConnName);
        String target = (String) vectorTarget.elementAt(index);
        return target;
    }


    public void setTargetCellGroup(String complexConnName, String target)
    {
        int index = vectorNames.indexOf(complexConnName);
        this.setValueAt(target, index, COL_NUM_TARGET);
    }
    
    
    public String getGenerationStartCellGroup(String netConnName)
    {
        if (!vectorNames.contains(netConnName)) return null;
        int index = vectorNames.indexOf(netConnName);
        ConnectivityConditions cc = (ConnectivityConditions)vectorConnConds.elementAt(index);
        if (cc.getGenerationDirection()==ConnectivityConditions.SOURCE_TO_TARGET) return getSourceCellGroup(netConnName);
        else  return getTargetCellGroup(netConnName);
    }
    public String getGenerationEndCellGroup(String netConnName)
    {
        if (!vectorNames.contains(netConnName)) return null;
        int index = vectorNames.indexOf(netConnName);
        ConnectivityConditions cc = (ConnectivityConditions)vectorConnConds.elementAt(index);
        if (cc.getGenerationDirection()==ConnectivityConditions.SOURCE_TO_TARGET) return getTargetCellGroup(netConnName);
        else  return getSourceCellGroup(netConnName);
    }



    public float getAPSpeed(String netConnName)
    {
        int index = this.vectorNames.indexOf(netConnName);
        return (Float)this.getValueAt(index, COL_NUM_AP_SPEED);
    }

    public String getInhomogenousExp(String netConnName)
    {
        int index = this.vectorNames.indexOf(netConnName);
        return (String)this.getValueAt(index, COL_NUM_INHOMO_EXP);
    }



    public void setInhomogenousExp(String netConnName, String exp)
    {
        int index = vectorNames.indexOf(netConnName);
        this.setValueAt(exp, index, COL_NUM_INHOMO_EXP);
    }


    public void setAPSpeed(String netConnName, float aps)
    {
        int index = vectorNames.indexOf(netConnName);
        this.setValueAt(new Float(aps), index, COL_NUM_AP_SPEED);
    }


    public ConnectivityConditions getConnectivityConditions(String complexConnName)
    {
        int index = vectorNames.indexOf(complexConnName);
        return (ConnectivityConditions)vectorConnConds.elementAt(index);
    }


    public void setConnectivityConditions(String complexConnName, ConnectivityConditions cc)
    {
        int index = vectorNames.indexOf(complexConnName);
        this.setValueAt(cc, index, COL_NUM_CONN_CONDS);
    }



    public String getConnNameAt(int index)
    {
        String name = (String) vectorNames.elementAt(index);
        return name;
    }

    /**
     * Gets the complexConns names where cellGroupName is source or target.
     * Used when deleting a cell group...
     */
    public Vector<String> getAAConnsUsingCellGroup(String cellGroupName)
    {
        Vector<String> aaConns = new Vector<String>();

        for (int i = 0; i < vectorSource.size(); i++)
        {
            if (((String)vectorSource.elementAt(i)).equals(cellGroupName))
            {
                String complexConnName = vectorNames.elementAt(i);
                if (!aaConns.contains(complexConnName))
                    aaConns.add(complexConnName);
            }
        }
        for (int i = 0; i < vectorTarget.size(); i++)
        {
            if (((String)vectorTarget.elementAt(i)).equals(cellGroupName))
            {
                String complexConnName = vectorNames.elementAt(i);
                if (!aaConns.contains(complexConnName))
                    aaConns.add(complexConnName);
            }
        }
        return aaConns;
    }


    public boolean isValidVolBasedConn(String aaConnName)
    {
        return vectorNames.contains(aaConnName);
    }


    /**
     * Added to allow storing of data by XMLEncoder
     */
    public Hashtable getInternalData()
    {
        Hashtable<String, Vector> allInfo = new Hashtable<String, Vector>();
        allInfo.put(columnNames[COL_NUM_COMP_CONN_NAME], vectorNames);
        allInfo.put(columnNames[COL_NUM_SOURCE], vectorSource);
        allInfo.put(columnNames[COL_NUM_TARGET], vectorTarget);
        allInfo.put(columnNames[COL_NUM_SYNAPSE_LIST], vectorSynapseList);
        allInfo.put(columnNames[COL_NUM_SOURCE_REGION], vectorSourceRegion);
        allInfo.put(columnNames[COL_NUM_CONN_CONDS], vectorConnConds);
        allInfo.put(columnNames[COL_NUM_AP_SPEED], vectorApSpeed);
        allInfo.put(columnNames[COL_NUM_INHOMO_EXP], this.vectorInhExp);


        return allInfo;
    }


    /**
     * Added to allow storing of data by XMLEncoder. Not actually used, but needed so that "internalData" saved...
     */
    public void setInternalData(Hashtable allInfo)
    {
        Vector<String>  vectorNamesTemp = (Vector)allInfo.get(columnNames[COL_NUM_COMP_CONN_NAME]);
        if (vectorNamesTemp!=null) vectorNames = vectorNamesTemp;

        Vector<String>  vectorSourceTemp = (Vector<String>)allInfo.get(columnNames[COL_NUM_SOURCE]);
        if (vectorSourceTemp!=null) vectorSource = vectorSourceTemp;

        Vector<String> vectorTargetTemp = (Vector<String>)allInfo.get(columnNames[COL_NUM_TARGET]);
        if (vectorTargetTemp!=null) vectorTarget = vectorTargetTemp;

        Vector vectorSynapsePropertiesTemp = (Vector)allInfo.get(columnNames[COL_NUM_SYNAPSE_LIST]);
        if (vectorSynapsePropertiesTemp!=null) vectorSynapseList = vectorSynapsePropertiesTemp;

        Vector<ConnectivityConditions> vectorConnCondsTemp = (Vector)allInfo.get(columnNames[COL_NUM_CONN_CONDS]);
        if (vectorConnCondsTemp!=null) vectorConnConds = vectorConnCondsTemp;

        Vector vectorAPSpeedTemp = (Vector)allInfo.get(columnNames[COL_NUM_AP_SPEED]);
        if (vectorAPSpeedTemp!=null) vectorApSpeed = vectorAPSpeedTemp;

        Vector vectorInhExpTemp = (Vector)allInfo.get(columnNames[COL_NUM_INHOMO_EXP]);
        if (vectorInhExpTemp!=null) vectorInhExp = vectorInhExpTemp;





    }



}
