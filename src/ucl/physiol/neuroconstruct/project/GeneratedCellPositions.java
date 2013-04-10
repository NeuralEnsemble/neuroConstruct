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
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USAval
 *
 */

package ucl.physiol.neuroconstruct.project;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.ArrayList;
import javax.vecmath.*;

import ucl.physiol.neuroconstruct.gui.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.project.packing.OneDimRegSpacingPackingAdapter;
import ucl.physiol.neuroconstruct.project.packing.RandomCellPackingAdapter;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.xml.*;
import ucl.physiol.neuroconstruct.neuroml.NeuroMLConstants.*;

/**
 * Storage for the positions generated when the Generate cell positions... button
 * is pressed
 *
 * @author Padraig Gleeson
 *  
 */

public class GeneratedCellPositions
{
    private ClassLogger logger = new ClassLogger("GeneratedCellPositions");

    private Hashtable<String, ArrayList<PositionRecord>> myCellGroupPosns = null;

    private Project project = null;

    /**
     * The random seed used to generate the network
     */
    private long randomSeed = Long.MIN_VALUE;


    public GeneratedCellPositions(Project project)
    {
        this.project = project;
        myCellGroupPosns = new Hashtable<String, ArrayList<PositionRecord>>();
    }

    public void reset()
    {
        this.myCellGroupPosns.clear();
        logger.logComment("Reset called. Info: "+ this.toString());
        cachedCellPosition1 = null;
        cachedCellPosition2 = null;
        cachedCellPosition3 = null;
    }

    public void setRandomSeed(long rs)
    {
        this.randomSeed = rs;
    }

    public long getRandomSeed()
    {
        return this.randomSeed;
    }



    public void addPosition(String cellGroupName,
                            int cellIndex,
                            float xPos,
                            float yPos,
                            float zPos)
    {
        PositionRecord posRec = new PositionRecord(cellIndex,
                                                   xPos,
                                                   yPos,
                                                   zPos);

        addPosition(cellGroupName, posRec);
        cachedCellPosition1 = null;
        cachedCellPosition2 = null;
        cachedCellPosition3 = null;
    }


    public void addPosition(String cellGroupName,
                            PositionRecord posRecord)
    {

        if (!myCellGroupPosns.containsKey(cellGroupName))
        {
            ArrayList<PositionRecord> newCellGroupArrayList = new ArrayList<PositionRecord>();
            myCellGroupPosns.put(cellGroupName, newCellGroupArrayList);
        }
        ArrayList<PositionRecord> cellGroupVector = myCellGroupPosns.get(cellGroupName);

        cellGroupVector.add(posRecord);
        cachedCellPosition1 = null;
        cachedCellPosition2 = null;
        cachedCellPosition3 = null;
    }


    public ArrayList<PositionRecord> getPositionRecords(String cellGroupName)
    {
        if (!myCellGroupPosns.containsKey(cellGroupName))
        {
            return new ArrayList<PositionRecord>();
        }
        ArrayList<PositionRecord> cellGroupArrayList = myCellGroupPosns.get(cellGroupName);

        return cellGroupArrayList;
    }

    public ArrayList<PositionRecord> getAllPositionRecords()
    {
        
        ArrayList<PositionRecord> all = new ArrayList<PositionRecord>();
        
        Enumeration<ArrayList<PositionRecord>> posLists =  myCellGroupPosns.elements();
        while (posLists.hasMoreElements())
        {
            all.addAll(posLists.nextElement());
        }
        return all;
    }

    public Iterator<String> getNamesGeneratedCellGroups()
    {
        return myCellGroupPosns.keySet().iterator();
    }
    
    public ArrayList<String> getNonEmptyCellGroups()
    {
        ArrayList<String> cgs = new ArrayList<String>();
        for (String cg: myCellGroupPosns.keySet())
        {
            if (myCellGroupPosns.get(cg).size()>0)
                cgs.add(cg);
        }
        return cgs;
    }



    public int getNumberInAllCellGroups()
    {
        int total = 0;
        Enumeration<ArrayList<PositionRecord>> posLists =  myCellGroupPosns.elements();

        while (posLists.hasMoreElements())
        {
            ArrayList posList = posLists.nextElement();
            total= total + posList.size();
        }
        return total;
    }

    public int getNumberNonEmptyCellGroups()
    {
        int total = 0;
        Enumeration<ArrayList<PositionRecord>> posLists =  myCellGroupPosns.elements();

        while (posLists.hasMoreElements())
        {
            ArrayList posList = posLists.nextElement();
            if (!posList.isEmpty()) total+=1;
        }
        return total;
    }


    public int getNumberInCellGroup(String cellGroupName)
    {
        if (!myCellGroupPosns.containsKey(cellGroupName))
        {
            return 0;
        }
        ArrayList cellGroupVector = myCellGroupPosns.get(cellGroupName);

        return cellGroupVector.size();
    }
    
    // As there is often multiple requests for the same cells, e.g. srcCell1 -> tgtCell1,  srcCell1 -> tgtCell2...
    private CachedCellPosition cachedCellPosition1 = null;
    private CachedCellPosition cachedCellPosition2 = null;
    private CachedCellPosition cachedCellPosition3 = null;
    
    public class CachedCellPosition
    {
        String cellGroupName;
        int index;
        Point3f point;
    }


    public Point3f getOneCellPosition(String cellGroupName, int index)
    {
        //logger.logComment("Being requested for posn of cell num: "+ index+ " in group: "+ cellGroupName, true);
        if (cachedCellPosition1!=null && 
            cachedCellPosition1.index == index && 
            cachedCellPosition1.cellGroupName.equals(cellGroupName))
            return new Point3f(cachedCellPosition1.point.x, cachedCellPosition1.point.y,cachedCellPosition1.point.z);
        
        if (cachedCellPosition2!=null && 
            cachedCellPosition2.index == index && 
            cachedCellPosition2.cellGroupName.equals(cellGroupName))
            return new Point3f(cachedCellPosition2.point.x, cachedCellPosition2.point.y,cachedCellPosition2.point.z);
        
        if (cachedCellPosition3!=null && 
            cachedCellPosition3.index == index && 
            cachedCellPosition3.cellGroupName.equals(cellGroupName))
            return new Point3f(cachedCellPosition3.point.x, cachedCellPosition3.point.y,cachedCellPosition3.point.z);
        
        if (!myCellGroupPosns.containsKey(cellGroupName))
        {
            return null;
        }
        ArrayList<PositionRecord> cellGroupArrayList = myCellGroupPosns.get(cellGroupName);

        for (int i = 0; i < cellGroupArrayList.size(); i++)
        {
            PositionRecord posRec = cellGroupArrayList.get(i);
            if (posRec.cellNumber==index)
            {
                Point3f p = new Point3f(posRec.x_pos,posRec.y_pos,posRec.z_pos);
                CachedCellPosition cp = new CachedCellPosition();
                cp.index = index;
                cp.cellGroupName = cellGroupName;
                cp.point = new Point3f(p);
                cachedCellPosition3 = cachedCellPosition2;
                cachedCellPosition2 = cachedCellPosition1;
                cachedCellPosition1 = cp;
                return p;
            }
        }
        logger.logComment("No record of cell with index: "+ index);
        return null;

    }




    public int getNumberPositionRecords()
    {
        int totalCount = 0;

        Enumeration keys = myCellGroupPosns.keys();

        while(keys.hasMoreElements())
        {
            ArrayList cellGroupArrayList = myCellGroupPosns.get((String)keys.nextElement());
            totalCount = totalCount+ cellGroupArrayList.size();
        }

        return totalCount;
    }


    public String getHtmlReport()
    {
        StringBuilder generationReport = new StringBuilder();

        Iterator<String> names = getNamesGeneratedCellGroups();

        while(names.hasNext())
        {
            String cellGroup = names.next();
            String cellType = project.cellGroupsInfo.getCellType(cellGroup);
            String region = project.cellGroupsInfo.getRegionName(cellGroup);
            generationReport.append("<b>" + ClickProjectHelper.getCellGroupLink(cellGroup) 
                + "</b> (" + ClickProjectHelper.getCellTypeLink(cellType) 
                + " in "+ClickProjectHelper.getRegionLink(region)+")<br>");
            
            int num = project.generatedCellPositions.getNumberInCellGroup(cellGroup);
            generationReport.append("Number in cell group: <b>"
                      + num
                      + "</b><br>");
            
            if(project.cellGroupsInfo.getCellPackingAdapter(cellGroup) instanceof RandomCellPackingAdapter)
            {
                RandomCellPackingAdapter ra = (RandomCellPackingAdapter)project.cellGroupsInfo.getCellPackingAdapter(cellGroup);
                int max = ra.getMaxNumberCells();
                if(num<max)
                {
                    generationReport.append(GeneralUtils.getBoldColouredString("Warning, fewer than "
                        +max+" cells in this cell group! " +
                        "Check packing algorithm ("+ra+") and 3D regions.<br>", ValidityStatus.VALIDATION_COLOUR_WARN, true));
                }
            }
            else if(project.cellGroupsInfo.getCellPackingAdapter(cellGroup) instanceof OneDimRegSpacingPackingAdapter)
            {
                OneDimRegSpacingPackingAdapter od = (OneDimRegSpacingPackingAdapter)project.cellGroupsInfo.getCellPackingAdapter(cellGroup);
                int max = od.getNumberCells();
                if(num<max)
                {
                    generationReport.append(GeneralUtils.getBoldColouredString("Warning, fewer than "
                        +max+" cells in this cell group! " +
                        "Check packing algorithm ("+od+") and 3D regions.<br>", ValidityStatus.VALIDATION_COLOUR_WARN, true));
                }
            }
                       
            generationReport.append("<br>");
        }

        return generationReport.toString();

    }



    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("GeneratedCellPositions with "+getNumberPositionRecords() +" cell positions in total\n");

        Enumeration<String> keys = myCellGroupPosns.keys();

        while(keys.hasMoreElements())
        {
            String cellGroupName = (String)keys.nextElement();
            ArrayList<PositionRecord> cellGroupArrayList = myCellGroupPosns.get(cellGroupName);
            sb.append(cellGroupName+" has "+cellGroupArrayList.size()
                      + " entries. First: "+cellGroupArrayList.get(0)+"\n");
        }
        return sb.toString();
    }
    

    /*
     * Useful for Python interface
     */
    public String details()
    {
        return details(false);
    }

    public String details(boolean html)
    {
        return toLongString(html);
    }
    
    public String toLongString(boolean html)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Network contains "+ GeneralUtils.getBold(getNumberPositionRecords()+"", html) +" cells in total"+GeneralUtils.getEndLine(html)+GeneralUtils.getEndLine(html));

        Enumeration keys = myCellGroupPosns.keys();

        while(keys.hasMoreElements())
        {
            String cellGroupName = (String)keys.nextElement();
            ArrayList<PositionRecord> cellGroupArrayList = myCellGroupPosns.get(cellGroupName);
            
            String cell_s = cellGroupArrayList.size()>1 ? "cells": "cell";
            
            sb.append("Cell Group: "+ GeneralUtils.getBold(cellGroupName, html)+" has "+GeneralUtils.getBold(cellGroupArrayList.size()+"", html)
                      + " "+cell_s+GeneralUtils.getEndLine(html));
            
            for (int i = 0; i < cellGroupArrayList.size(); i++)
            {
                PositionRecord posRec = cellGroupArrayList.get(i);
                if (html)
                    sb.append(posRec.toHtmlString()+GeneralUtils.getEndLine(html));
                else
                    sb.append(posRec+GeneralUtils.getEndLine(html));

            }
            sb.append(GeneralUtils.getEndLine(html));
        }
        return sb.toString();
    }


    public void saveToFile(File positionFile) throws java.io.IOException
    {
        logger.logComment("Saving "
                          + getNumberPositionRecords()
                          + " position records to file: "
                          + positionFile.getAbsolutePath(), true);

        // will create the parent dir if it doesn't exist.
        if (!positionFile.exists())
        {
            logger.logComment("File: "+positionFile + " doesn't exist.");
            if (!positionFile.getParentFile().exists())
            {
                logger.logComment("Parent dir: "+positionFile.getParentFile() + " doesn't exist.", true);
                //String parentDirName = positionFile.getParentFile().getCanonicalPath();
                File projectDir = positionFile.getParentFile().getParentFile();

                if (!projectDir.exists())
                {
                    throw new FileNotFoundException("Project dir doesn't exist: "+ projectDir.getAbsolutePath());
                }
                //logger.logComment("Going to create dir: "+ parentDirName +" in dir :"+ projectDir);

                logger.logComment("Going to create dir: "+ positionFile.getParentFile());

                positionFile.getParentFile().mkdir();

                logger.logComment("Success? "+ positionFile.getParentFile().exists());

            }
        }

        FileWriter fw = new FileWriter(positionFile);

        Enumeration keys = myCellGroupPosns.keys();

        while (keys.hasMoreElements())
        {
            String cellGroup = (String)keys.nextElement();
            ArrayList<PositionRecord> cellsHere = getPositionRecords(cellGroup);
            logger.logComment("Adding "+cellsHere.size()+" cells in: "+ cellGroup);

            fw.write(cellGroup+":\n");

            for (int i = 0; i < cellsHere.size(); i++)
            {
                PositionRecord posRec = cellsHere.get(i);
                fw.write(posRec+"\n");

            }


        }
        logger.logComment("Finished saving data to file: "+ positionFile.getAbsolutePath());
        fw.flush();
        fw.close();
    }

    public void loadFromFile(File positionFile) throws java.io.IOException
    {
        logger.logComment("Loading position records from file: "
                          + positionFile.getAbsolutePath());

        this.reset();

        Reader in = new FileReader(positionFile);
        LineNumberReader reader = new LineNumberReader(in);
        String nextLine = null;

        String  currentCellGroupName = null;

        while ( (nextLine = reader.readLine()) != null)
        {
            //logger.logComment("Parsing line: "+ nextLine);

            if (nextLine.endsWith(":"))
            {
                currentCellGroupName = nextLine.substring(0, nextLine.length()-1);
                logger.logComment("Current cell group: "+ currentCellGroupName);
            }
            else
            {
                PositionRecord posRecord = new PositionRecord(nextLine);
                addPosition(currentCellGroupName, posRecord);
            }
        }
        in.close();

        logger.logComment("Finished loading cell info. Internal state: "+ this.toString());

    }


    public SimpleXMLElement getNetworkMLElement() throws NeuroMLException
    {
        return getNetworkMLElements(NeuroMLVersion.NEUROML_VERSION_1).get(0);
    }

    public ArrayList<SimpleXMLElement> getNetworkMLElements(NeuroMLVersion version) throws NeuroMLException
    {
        ArrayList<SimpleXMLElement> elements = new ArrayList<SimpleXMLElement>();

        SimpleXMLElement populationsElement = null;

        boolean nml2 = version.isVersion2();
        try
        {
            logger.logComment("Going to save file in NeuroML format: "+version+", " + this.getNumberInAllCellGroups() +
                              " cells in total");

            String v1Root = NetworkMLConstants.POPULATIONS_ELEMENT;

            populationsElement = new SimpleXMLElement(v1Root);

            if (!nml2) elements.add(populationsElement);

            Enumeration keys = myCellGroupPosns.keys();

            while (keys.hasMoreElements())
            {
                String cellGroup = (String) keys.nextElement();
                ArrayList<PositionRecord> cellsHere = getPositionRecords(cellGroup);
                logger.logComment("Adding " + cellsHere.size() + " cells in: " + cellGroup);

                String type = project.cellGroupsInfo.getCellType(cellGroup);


                SimpleXMLElement populationElement = new SimpleXMLElement(NetworkMLConstants.POPULATION_ELEMENT);

                if (nml2)
                {
                    populationElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.NEUROML_ID_V2, cellGroup));
                    populationElement.addAttribute(new SimpleXMLAttribute("component", type));//TODO...
                    //populationElement.addContent("\n");

                    if (version.isVersion2beta())
                    {
                        populationElement.addAttribute(new SimpleXMLAttribute("type", NetworkMLConstants.NEUROML2_POPULATION_LIST));

                        for (int i = 0; i < cellsHere.size(); i++)
                        {
                            PositionRecord posRec = cellsHere.get(i);

                            SimpleXMLElement instanceElement = new SimpleXMLElement(NetworkMLConstants.INSTANCE_ELEMENT);

                            instanceElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INSTANCE_ID_ATTR, i+""));

                            if (posRec.getNodeId()!=PositionRecord.NO_NODE_ID)
                            {
                                instanceElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.NODE_ID_ATTR, posRec.getNodeId()+""));
                            }


                            SimpleXMLElement locationElement = new SimpleXMLElement(NetworkMLConstants.LOCATION_ELEMENT);

                            populationElement.addContent("\n            ");
                            instanceElement.addContent("\n                ");
                            instanceElement.addChildElement(locationElement);
                            instanceElement.addContent("\n            ");

                            locationElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.LOC_X_ATTR, posRec.x_pos+""));
                            locationElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.LOC_Y_ATTR, posRec.y_pos+""));
                            locationElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.LOC_Z_ATTR, posRec.z_pos+""));

                            populationElement.addChildElement(instanceElement);


                        }
                        populationElement.addContent("\n        ");
                    }
                    else
                    {
                       populationElement.addAttribute(new SimpleXMLAttribute("size", cellsHere.size()+""));//TODO...
                    }
                }
                else
                {

                    populationElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.POP_NAME_ATTR, cellGroup));

                    ////Pre v1.7.1 specification
                    //////populationElement.addChildElement(new SimpleXMLElement(NetworkMLConstants.CELLTYPE_ELEMENT, type));

                    populationElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.CELLTYPE_ATTR, type));





                    Color c = project.cellGroupsInfo.getColourOfCellGroup(cellGroup);

                    if (c!=null)
                    {
                        SimpleXMLElement props = new SimpleXMLElement(MetadataConstants.PREFIX + ":" +
                                                                  MorphMLConstants.PROPS_ELEMENT);
                        populationElement.addChildElement(props);
                        MetadataConstants.addProperty(props,
                                                  "color",
                                                  (c.getRed()/256.0)+" "+ (c.getGreen()/256.0)+" "+(c.getBlue()/256.0),
                                                  "            ",
                                                  version);
                        props.addContent("\n        ");
                    }

                    SimpleXMLElement instancesElement = new SimpleXMLElement(NetworkMLConstants.INSTANCES_ELEMENT);

                    instancesElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INSTANCES_SIZE_ATTR, cellsHere.size()+""));

                    for (int i = 0; i < cellsHere.size(); i++)
                    {
                        PositionRecord posRec = cellsHere.get(i);

                        SimpleXMLElement instanceElement = new SimpleXMLElement(NetworkMLConstants.INSTANCE_ELEMENT);

                        instanceElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INSTANCE_ID_ATTR, i+""));

                        if (posRec.getNodeId()!=PositionRecord.NO_NODE_ID)
                        {
                            instanceElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.NODE_ID_ATTR, posRec.getNodeId()+""));
                        }


                        SimpleXMLElement locationElement = new SimpleXMLElement(NetworkMLConstants.LOCATION_ELEMENT);

                        instanceElement.addChildElement(locationElement);
                        instanceElement.addContent("\n            ");

                        locationElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.LOC_X_ATTR, posRec.x_pos+""));
                        locationElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.LOC_Y_ATTR, posRec.y_pos+""));
                        locationElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.LOC_Z_ATTR, posRec.z_pos+""));

                        instancesElement.addChildElement(instanceElement);
                    }

                    populationElement.addChildElement(instancesElement);
                }

                if (!nml2)
                {
                    populationsElement.addChildElement(populationElement);
                }
                else
                {
                    elements.add(populationElement);
                }

            }
            logger.logComment("Finished saving data to populations element");

        }

        catch (Exception ex)
        {
            throw new NeuroMLException("Problem creating populations element file", ex);
        }

        return elements;

    }

    public static void main(String[] args)
    {
        try
        {
            Project testProj = Project.loadProject(new File("testProjects/TestNetworkML/TestNetworkML.neuro.xml"),
                                                   new ProjectEventListener()
            {
                public void tableDataModelUpdated(String tableModelName)
                {};

                public void tabUpdated(String tabName)
                {};
                public void cellMechanismUpdated()
                {
                };

            });


            GeneratedCellPositions cpr = new GeneratedCellPositions(testProj);

            System.out.println("Internal info: \n"+ cpr.toString()); 

            cpr.addPosition("CGone", 3, 2.2f,3.3f,4.4f);
            cpr.addPosition("CGone", 4, 2.77f,37.3f,47.4f);
            cpr.addPosition("CGtwo", 5, 2.2f,3.3f,4.4f);
            cpr.addPosition("CGtwo", 6, 2.2f,3.3f,4.4f);
            cpr.addPosition("CGtwo", 7, 2.2f,3.3f,4.4f);

            System.out.println("Internal info: \n"+ cpr.toString()); 

            File f = new File("../temp/tempp.txt");

            cpr.saveToFile(f);


            GeneratedCellPositions cpr2 = new GeneratedCellPositions(testProj);

            cpr2.loadFromFile(f);
            System.out.println("New internal info: \n"+ cpr2.toString()); 

            ArrayList<SimpleXMLElement> pops = cpr2.getNetworkMLElements(NeuroMLVersion.NEUROML_VERSION_2_BETA);

            for (SimpleXMLElement pop: pops)
            {
                System.out.println("------ Pop: ------\n"+pop.getXMLString("", false));
            }





        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }


}
