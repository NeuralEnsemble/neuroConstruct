/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.project;

import java.io.*;
import java.util.*;
import javax.vecmath.*;

import ucl.physiol.neuroconstruct.gui.ClickProjectHelper;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.xml.*;

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
    }


    public void addPosition(String cellGroupName,
                            PositionRecord posRecord)
    {

        if (!myCellGroupPosns.containsKey(cellGroupName))
        {
            ArrayList<PositionRecord> newCellGroupArrayList = new ArrayList<PositionRecord>();
            myCellGroupPosns.put(cellGroupName, newCellGroupArrayList);
        }
        ArrayList<PositionRecord> cellGroupVector = (ArrayList<PositionRecord>)myCellGroupPosns.get(cellGroupName);

        cellGroupVector.add(posRecord);
    }


    public ArrayList<PositionRecord> getPositionRecords(String cellGroupName)
    {
        if (!myCellGroupPosns.containsKey(cellGroupName))
        {
            return new ArrayList<PositionRecord>();
        }
        ArrayList<PositionRecord> cellGroupArrayList = (ArrayList<PositionRecord>)myCellGroupPosns.get(cellGroupName);

        return cellGroupArrayList;
    }

    public Iterator<String> getNamesGeneratedCellGroups()
    {
        return myCellGroupPosns.keySet().iterator();
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


    public int getNumberInCellGroup(String cellGroupName)
    {
        if (!myCellGroupPosns.containsKey(cellGroupName))
        {
            return 0;
        }
        ArrayList cellGroupVector = (ArrayList)myCellGroupPosns.get(cellGroupName);

        return cellGroupVector.size();
    }


    public Point3f getOneCellPosition(String cellGroupName, int index)
    {
        //logger.logComment("Being requested for posn of cell num: "+ index+ " in group: "+ cellGroupName);
        if (!myCellGroupPosns.containsKey(cellGroupName))
        {
            return null;
        }
        ArrayList cellGroupArrayList = (ArrayList)myCellGroupPosns.get(cellGroupName);

        for (int i = 0; i < cellGroupArrayList.size(); i++)
        {
            PositionRecord posRec = (PositionRecord)cellGroupArrayList.get(i);
            if (posRec.cellNumber==index)
            {
                return new Point3f(posRec.x_pos,posRec.y_pos,posRec.z_pos);
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

        StringBuffer generationReport = new StringBuffer();

        Iterator<String> names = getNamesGeneratedCellGroups();

        while(names.hasNext())
        {
            String cellGroup = names.next();
            String cellType = project.cellGroupsInfo.getCellType(cellGroup);
            generationReport.append("<b>" + ClickProjectHelper.getCellGroupLink(cellGroup) + "</b> (Cell type: " + ClickProjectHelper.getCellTypeLink(cellType) + ")<br>");
            generationReport.append("Number in cell group: <b>"
                      + project.generatedCellPositions.getNumberInCellGroup(cellGroup)
                      + "</b><br><br>");
        }

        return generationReport.toString();

    }



    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("GeneratedCellPositions with "+getNumberPositionRecords() +" positions in total\n");

        Enumeration keys = myCellGroupPosns.keys();

        while(keys.hasMoreElements())
        {
            String cellGroupName = (String)keys.nextElement();
            ArrayList cellGroupArrayList = myCellGroupPosns.get(cellGroupName);
            sb.append(cellGroupName+" has "+cellGroupArrayList.size()
                      + " entries. First: "+cellGroupArrayList.get(0)+"\n");
        }
        return sb.toString();
    }


    public void saveToFile(File positionFile) throws java.io.IOException
    {
        logger.logComment("Saving "
                          + getNumberPositionRecords()
                          + " position records to file: "
                          + positionFile.getAbsolutePath());

        // will create the parent dir if it doesn't exist.
        if (!positionFile.exists())
        {
            logger.logComment("File: "+positionFile + " doesn't exist.");
            if (!positionFile.getParentFile().exists())
            {
                logger.logComment("Parent dir: "+positionFile.getParentFile() + " doesn't exist.");
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

        SimpleXMLElement populationsElement = null;
        try
        {
            logger.logComment("Going to save file in NeuroML format: " + this.getNumberInAllCellGroups() +
                              " cells in total");

            populationsElement = new SimpleXMLElement(NetworkMLConstants.POPULATIONS_ELEMENT);

            Enumeration keys = myCellGroupPosns.keys();

            while (keys.hasMoreElements())
            {
                String cellGroup = (String) keys.nextElement();
                ArrayList<PositionRecord> cellsHere = getPositionRecords(cellGroup);
                logger.logComment("Adding " + cellsHere.size() + " cells in: " + cellGroup);

                String type = project.cellGroupsInfo.getCellType(cellGroup);


                SimpleXMLElement populationElement = new SimpleXMLElement(NetworkMLConstants.POPULATION_ELEMENT);

                populationElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.POP_NAME_ATTR, cellGroup));

                ////Pre v1.7.1 specification
                //////populationElement.addChildElement(new SimpleXMLElement(NetworkMLConstants.CELLTYPE_ELEMENT, type));

                populationElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.CELLTYPE_ATTR, type));

                SimpleXMLElement instancesElement = new SimpleXMLElement(NetworkMLConstants.INSTANCES_ELEMENT);

                instancesElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INSTANCES_SIZE_ATTR, cellsHere.size()+""));

                for (int i = 0; i < cellsHere.size(); i++)
                {
                    PositionRecord posRec = cellsHere.get(i);

                    SimpleXMLElement instanceElement = new SimpleXMLElement(NetworkMLConstants.INSTANCE_ELEMENT);

                    instanceElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INSTANCE_ID_ATTR, i+""));
                    
                    if (posRec.nodeId!=PositionRecord.NO_NODE_ID)
                    {
                        instanceElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.NODE_ID_ATTR, posRec.nodeId+""));
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
                populationsElement.addChildElement(populationElement);

            }
            logger.logComment("Finished saving data to populations element");

        }

        catch (Exception ex)
        {
            throw new NeuroMLException("Problem creating populations element file", ex);
        }
        return populationsElement;

    }

    public static void main(String[] args)
    {
        try
        {
            Project testProj = Project.loadProject(new File("projects/NetworkML/NetworkML.neuro.xml"),
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

            System.out.println("Internal info: \n"+ cpr.toString()); ;

            cpr.addPosition("CGone", 3, 2.2f,3.3f,4.4f);
            cpr.addPosition("CGone", 4, 2.77f,37.3f,47.4f);
            cpr.addPosition("CGtwo", 5, 2.2f,3.3f,4.4f);
            cpr.addPosition("CGtwo", 6, 2.2f,3.3f,4.4f);
            cpr.addPosition("CGtwo", 7, 2.2f,3.3f,4.4f);

            System.out.println("Internal info: \n"+ cpr.toString()); ;

            File f = new File("c:\\temp\\try2\\tempp.txt");

            cpr.saveToFile(f);


            GeneratedCellPositions cpr2 = new GeneratedCellPositions(testProj);

            cpr2.loadFromFile(f);
            System.out.println("New internal info: \n"+ cpr2.toString()); ;

            SimpleXMLElement pops = cpr2.getNetworkMLElement();

            System.out.println("Pops: "+pops.getXMLString("", false));





        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }


}
