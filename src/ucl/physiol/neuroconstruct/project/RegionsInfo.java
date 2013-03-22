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

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.util.*;

import javax.swing.table.*;

import ucl.physiol.neuroconstruct.utils.*;

/**
 * Extension of AbstractTableModel to store the info on the regions
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class RegionsInfo extends AbstractTableModel
{
    ClassLogger logger = new ClassLogger("RegionsInfo");

    public final static int COL_NUM_REGIONNAME = 0;
    public final static int COL_NUM_REGION = 1;
    public final static int COL_NUM_COLOUR= 2;

    final String[] columnNames = new String[3];

    Vector<String> vectorNames = new Vector<String>();
    Vector<Region> vectorRegionObjects = new Vector<Region>();
    Vector<Color> vectorColours = new Vector<Color>();


    //public final static int COL_NUM_REGION = 1;


    public RegionsInfo()
    {
        logger.logComment("New RegionsInfo created");
        columnNames[COL_NUM_REGIONNAME] = new String("Name");
        columnNames[COL_NUM_REGION] = new String("Description");
        columnNames[COL_NUM_COLOUR] = new String("Colour");
    }


    public int getColumnCount()
    {
        return columnNames.length;
    }

    public int getRowCount()
    {
        if (vectorNames.size()>vectorRegionObjects.size())
        {
            logger.logComment("Resizing the ");
            // This can be caused by old versions of the project file...
            vectorNames.setSize(vectorRegionObjects.size());
        }
        return vectorNames.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }


    public Object getValueAt(int row, int col)
    {
        switch (col)
        {
            case COL_NUM_REGIONNAME:
                return vectorNames.elementAt(row);

            case COL_NUM_REGION:
            {
                return vectorRegionObjects.elementAt(row);
            }
            case COL_NUM_COLOUR:
            {
                if (vectorColours.size()<=row)
                {
                    for (int i = vectorColours.size(); i <= row; i++)
                    {
                        vectorColours.add(new Color(Color.white.getRGB()));
                    }
                }
                return vectorColours.elementAt(row);
            }

            default:
                return null;
        }
    }


    @Override
    public boolean isCellEditable(int row, int col)
    {
        if (col==COL_NUM_COLOUR) return true;
        return false;
    }

    @Override
    public void setValueAt(Object value, int row, int col)
    {
        logger.logComment("Setting row: "+row+", col: "+col);

        switch (col)
        {
            case COL_NUM_REGIONNAME:
                vectorNames.setElementAt((String)value, row);
                break;
            case COL_NUM_REGION:
                vectorRegionObjects.setElementAt((Region)value, row);
                break;
            case COL_NUM_COLOUR:
                vectorColours.setElementAt((Color)value, row);
                break;


        }
        fireTableCellUpdated(row, col);
    }

    public void addRow(String name, Region region, Color colour) throws NamingException
    {
        logger.logComment("Adding row for: "+name);
        if (vectorNames.contains(name))
            throw new NamingException("The region name: "+name+" has already been used");


        int nameCount = vectorNames.size();
        int regionObjectCount = vectorRegionObjects.size();

        // Should all be the same but take the max anyway...
        int maxCount = Math.max(nameCount, regionObjectCount);

        vectorNames.add(maxCount, name);
        vectorRegionObjects.add(maxCount, region);
        vectorColours.add(maxCount, colour);

        this.fireTableRowsInserted(maxCount, maxCount);
    }


    public void updateRow(String name, Region region, Color colour) throws NamingException
    {
        logger.logComment("Updating row for: "+name);

        if (vectorNames.contains(name))
        {
            int index = vectorNames.indexOf(name);
            vectorRegionObjects.setElementAt(region, index);
            vectorColours.setElementAt(colour, index);
            this.fireTableDataChanged();
        }
        else
        {
            logger.logComment("Doesn't exist, so adding...");
            addRow(name, region, colour);
        }

    }




    public void deleteRegion(int index)
    {
        vectorRegionObjects.removeElementAt(index);
        vectorColours.removeElementAt(index);
        vectorNames.removeElementAt(index);

        this.fireTableRowsDeleted(index, index);

    }


    public String[] getAllRegionNames()
    {
        logger.logComment("Returning "+vectorNames.size()+" region names");
        String[] regionNames = new String[vectorNames.size()];

        for (int i = 0; i < vectorNames.size(); i++)
        {
            regionNames[i] = vectorNames.elementAt(i);
        }
        return regionNames;
    }
    
    public int getRegionIndex(String regionName)
    {
        for (int i = 0; i < vectorNames.size(); i++)
        {
            if (regionName.equals(vectorNames.elementAt(i)))
            {
                return i;
            }
        }
        return -1;
    }

    public int getNumberRegions()
    {
        return vectorNames.size();
    }




    public RectangularBox getRegionEnclosingAllRegions(Project project, SimConfig simConfig)
    {
        if (vectorRegionObjects.size()==0)
            return new RectangularBox(0,0,0,0,0,0);
        
        Vector<Region> regions = new Vector<Region>();
        
        if (project==null && simConfig==null)
        {
            regions = vectorRegionObjects;
        }
        else if (simConfig==null)
        {
            Iterator<String> cgs = project.generatedCellPositions.getNamesGeneratedCellGroups();
            while (cgs.hasNext())
            {
                String cellGroup = cgs.next();
                if (project.generatedCellPositions.getNumberInCellGroup(cellGroup)>0)
                {
                    Region nextRegion = getRegionObject(project.cellGroupsInfo.getRegionName(cellGroup));
                    regions.add(nextRegion);
                }
            }
        }
        else
        {
            for (String cellGroup: simConfig.getCellGroups())
            {
                Region nextRegion = getRegionObject(project.cellGroupsInfo.getRegionName(cellGroup));
                regions.add(nextRegion);
            }
        }

        float maxX = -1*Float.MAX_VALUE;
        float maxY = -1*Float.MAX_VALUE;
        float maxZ = -1*Float.MAX_VALUE;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;

        for (int i = 0; i < regions.size(); i++)
        {
             Region nextRegion =   regions.get(i);
             
             if(nextRegion.getLowestXValue()<minX) minX = nextRegion.getLowestXValue();
             if(nextRegion.getLowestYValue()<minY) minY = nextRegion.getLowestYValue();
             if(nextRegion.getLowestZValue()<minZ) minZ = nextRegion.getLowestZValue();
             if(nextRegion.getHighestXValue()>maxX) maxX = nextRegion.getHighestXValue();
             if(nextRegion.getHighestYValue()>maxY) maxY = nextRegion.getHighestYValue();
             if(nextRegion.getHighestZValue()>maxZ) maxZ = nextRegion.getHighestZValue();
        }
        
        if (project!=null)
        {
            ArrayList<PositionRecord> allPositions = project.generatedCellPositions.getAllPositionRecords();

            for(PositionRecord pos: allPositions)
            {
                 if(pos.x_pos<minX) minX = pos.x_pos;
                 if(pos.y_pos<minY) minY = pos.y_pos;
                 if(pos.z_pos<minZ) minZ = pos.z_pos;
                 if(pos.x_pos>maxX) maxX = pos.x_pos;
                 if(pos.y_pos>maxY) maxY = pos.y_pos;
                 if(pos.z_pos>maxZ) maxZ = pos.z_pos;
            }
        }
        
        
        return new RectangularBox(minX, minY, minZ,
                                  (maxX-minX),
                                  (maxY-minY),
                                  (maxZ-minZ));
    }



    public void printDetails()
    {
        logger.logComment("Size: "+vectorNames.size());
    }


    public Region getRegionObject(String regionName)
    {
        int index = vectorNames.indexOf(regionName);
        if (index<0) return null;
        return vectorRegionObjects.elementAt(index);
    }

    public Color getRegionColour(String regionName)
    {
        int index = vectorNames.indexOf(regionName);
        return (Color)getValueAt(index, COL_NUM_COLOUR);
    }




    /**
     * Added to allow storing of data by XMLEncoder
     */
    public Hashtable getInternalData()
    {
        logger.logComment("Internal data being got...");
        Hashtable<String, Vector> allInfo = new Hashtable<String, Vector>();
        allInfo.put(columnNames[COL_NUM_REGIONNAME], vectorNames);
        allInfo.put(columnNames[COL_NUM_REGION], vectorRegionObjects);
        allInfo.put(columnNames[COL_NUM_COLOUR], vectorColours);
        return allInfo;
    }


    /**
     * Added to allow storing of data by XMLEncoder.
     * The function needs to be here but isn't called!! getInternalData() is called instead
     */
    public void setInternalData(Hashtable allInfo)
    {
        logger.logComment("Internal dat being set...");
    }

    public static void main(String[] args)
    {
        try
        {
            File f = new File("c:\\temp\\ri.xml");
            FileOutputStream fos = new FileOutputStream(f);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            XMLEncoder xmlEncoder = new XMLEncoder(bos);


            RegionsInfo ri = new RegionsInfo();

            ri.addRow("boxy", new RectangularBox(0,0,0,30,30,30), Color.red);

            xmlEncoder.writeObject(ri);

            xmlEncoder.flush();
            xmlEncoder.close();

            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis);
            XMLDecoder xmlDecoder = new XMLDecoder(bis);

            Object obj = xmlDecoder.readObject();
            System.out.println("Obj: " + obj);
            System.out.println("Boxy: " + ((RegionsInfo)obj).getRegionObject("boxy").toString());

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return;
        }

    }

}
