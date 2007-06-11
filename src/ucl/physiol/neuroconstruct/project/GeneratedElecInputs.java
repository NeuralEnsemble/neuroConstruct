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

package ucl.physiol.neuroconstruct.project;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.io.*;
import java.util.*;
import ucl.physiol.neuroconstruct.project.stimulation.*;

/**
 * Storage for the locations and settings of electrical inputs generated when the Generate
 * cell positions... button is pressed
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class GeneratedElecInputs
{
    ClassLogger logger = new ClassLogger("GeneratedElecInputs");

    Hashtable<String, ArrayList> myElecInputs = null;

    public GeneratedElecInputs()
    {
        myElecInputs = new Hashtable<String, ArrayList>();

    }

    public void reset()
    {
        this.myElecInputs.clear();
        logger.logComment("Reset called. Info: "+ this.toString());
    }

    public ArrayList<SingleElectricalInput> getInputLocations(String inputReference)
    {
        if (!myElecInputs.containsKey(inputReference))
            return new ArrayList<SingleElectricalInput>();

        return myElecInputs.get(inputReference);
    }

    public ArrayList<String> getInputReferences()
    {
        return new ArrayList<String>(myElecInputs.keySet());
    }



    public void addSingleInput(String inputReference,
                               String inputType,
                               String cellGroup,
                               int cellNumber,
                               int segmentId,
                               float fractionAlong)
    {
        if (!myElecInputs.containsKey(inputReference))
        {
            ArrayList newInputArrayList = new ArrayList();
            myElecInputs.put(inputReference, newInputArrayList);
        }
        ArrayList<SingleElectricalInput> inputVector = (ArrayList) myElecInputs.get(inputReference);

        inputVector.add(new SingleElectricalInput(inputType,
                                                  cellGroup,
                                                  cellNumber,
                                                  segmentId,
                                                  fractionAlong));
    }



    public void addSingleInput(String inputReference,
                               SingleElectricalInput oneInput)
    {
        if (!myElecInputs.containsKey(inputReference))
        {
            ArrayList newInputArrayList = new ArrayList();
            myElecInputs.put(inputReference, newInputArrayList);
        }
        ArrayList<SingleElectricalInput> inputVector = (ArrayList) myElecInputs.get(inputReference);

        inputVector.add(oneInput);
    }






    public void saveToFile(File inputsFile) throws java.io.IOException
    {
        logger.logComment("Saving "
                          + this.getNumberSingleInputs()
                          + " inputs to file: "
                          + inputsFile.getAbsolutePath());

        // will create the parent dir if it doesn't exist.
        if (!inputsFile.exists())
        {
            logger.logComment("File: "+inputsFile + " doesn't exist.");
            if (!inputsFile.getParentFile().exists())
            {
                logger.logComment("Parent dir: "+inputsFile.getParentFile() + " doesn't exist.");
                //String parentDirName = inputsFile.getParentFile().getCanonicalPath();
                File projectDir = inputsFile.getParentFile().getParentFile();

                if (!projectDir.exists())
                {
                    throw new FileNotFoundException("Project dir doesn't exist: "+ projectDir.getAbsolutePath());
                }
                //logger.logComment("Going to create dir: "+ parentDirName +" in dir :"+ projectDir);

                logger.logComment("Going to create dir: "+ inputsFile.getParentFile());

                inputsFile.getParentFile().mkdir();

                logger.logComment("Success? "+ inputsFile.getParentFile().exists());

            }
        }

        FileWriter fw = new FileWriter(inputsFile);

        Enumeration keys = this.myElecInputs.keys();

        while (keys.hasMoreElements())
        {
            String input = (String)keys.nextElement();
            ArrayList<SingleElectricalInput> inputs = getInputLocations(input);

            fw.write(input+":\n");

            for (int i = 0; i < inputs.size(); i++)
            {
                fw.write(inputs.get(i)+"\n");

            }


        }
        logger.logComment("Finished saving data to file: "+ inputsFile.getAbsolutePath());
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

        String  currentInputRef = null;

        while ( (nextLine = reader.readLine()) != null)
        {
            //logger.logComment("Parsing line: "+ nextLine);

            if (nextLine.endsWith(":"))
            {
                currentInputRef = nextLine.substring(0, nextLine.length()-1);
                logger.logComment("currentInputRef: "+ currentInputRef);
            }
            else
            {
                SingleElectricalInput input = new SingleElectricalInput(nextLine);
                this.addSingleInput(currentInputRef, input);
            }
        }
        in.close();

        logger.logComment("Finished loading cell info. Internal state: "+ this.toString());

    }



    public int getNumberSingleInputs()
    {
        int totalCount = 0;

        Enumeration keys = this.myElecInputs.keys();

        while (keys.hasMoreElements())
        {
            ArrayList inputArrayList = myElecInputs.get( (String) keys.nextElement());
            totalCount = totalCount + inputArrayList.size();
        }

        return totalCount;
    }

    public int getNumberSingleInputs(String inputRef)
    {
        if (!myElecInputs.containsKey(inputRef))return 0;

        ArrayList inputArrayList = myElecInputs.get(inputRef);

        return inputArrayList.size();
    }


    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("GeneratedElecInputs with " + this.getNumberSingleInputs() + " inputs in total\n");

        Enumeration keys = this.myElecInputs.keys();

        while (keys.hasMoreElements())
        {
            String input = (String) keys.nextElement();
            ArrayList<SingleElectricalInput> singleInputList = myElecInputs.get(input);
            sb.append(input + " has " + singleInputList.size()
                      + " entries.\n");
            for (int i = 0; (i < singleInputList.size() && i < 9); i++)
            {
                sb.append("   Input "+i+": "+singleInputList.get(i).toString()+"\n");
            }

        }
        return sb.toString();
    }

/*
    public class SingleElectricalInput
    {
        private String electricalInputType = null;
        private String cellGroup = null;
        private int cellNumber = -1;
        private int segmentId = -1;
        private float fractionAlong = 0.5f;

        private SingleElectricalInput()
        {

        }

        public SingleElectricalInput(String electricalInputType,
                                     String cellGroup,
                                     int cellNumber)
        {
            this(electricalInputType,
                 cellGroup,
                 cellNumber,
                 0,
                 0.5f);

        }


        public SingleElectricalInput(String electricalInputType,
                                     String cellGroup,
                                     int cellNumber,
                                     int segmentId,
                                     float fractionAlong)
        {
            this.electricalInputType = electricalInputType;
            this.cellGroup = cellGroup;
            this.cellNumber = cellNumber;
            this.segmentId = segmentId;
            this.fractionAlong = fractionAlong;
        }

        public String getCellGroup()
        {
            return this.cellGroup;
        }

        public int getCellNumber()
        {
            return this.cellNumber;
        }

        public int getSegmentId()
        {
            return this.segmentId;
        }

        public float getFractionAlong()
        {
            return this.fractionAlong;
        }

        public String getElectricalInputType()
        {
            return this.electricalInputType;
        }




        public String toString()
        {
            return "SingleElectricalInput: [Input: "
                + electricalInputType
                + ", cellGroup: "
                + cellGroup
                + ", cellNumber: "
                + cellNumber
                + ", segmentId: "
                + segmentId
                + ", fractionAlong: "
                + fractionAlong
                + "]";
        }

        public SingleElectricalInput(String stringForm)
        {
            electricalInputType = stringForm.substring(stringForm.indexOf("[Input: ") + 8,
                                                       stringForm.indexOf(","));

            cellGroup = stringForm.substring(stringForm.indexOf(", cellGroup: ") + 13,
                                             stringForm.indexOf(", cellNumber: "));

            cellNumber = Integer.parseInt(stringForm.substring(stringForm.indexOf(", cellNumber: ") + 14,
                                                               stringForm.indexOf(", segmentId: ")));

            segmentId = Integer.parseInt(stringForm.substring(stringForm.indexOf(", segmentId: ") + 13,
                                                              stringForm.indexOf(", fractionAlong: ")));

            fractionAlong = Float.parseFloat(stringForm.substring(stringForm.indexOf(", fractionAlong: ") + 17,
                                                                  stringForm.indexOf("]")));
        }
    }*/

    public static void main(String[] args)
    {
        try
        {
            GeneratedElecInputs gei = new GeneratedElecInputs();

            System.out.println("Internal info: \n"+ gei.toString());

            IClamp ic = new IClamp(2,3,4, true);

            gei.addSingleInput("Input_0", "IClamp", "cg1", 3, 3, 3);
            gei.addSingleInput("Input_0", "IClamp", "cg1", 38, 3, 3);
            gei.addSingleInput("Input_2", "IClamp", "cg3", 3, 38, 39);

            System.out.println("Internal info: \n"+ gei.toString()); ;

            File f = new File("../temp/tempp.txt");

            gei.saveToFile(f);


            GeneratedElecInputs cpr2 = new GeneratedElecInputs();

            cpr2.loadFromFile(f);
            System.out.println("New internal info: \n"+ cpr2.toString()); ;

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }


}
