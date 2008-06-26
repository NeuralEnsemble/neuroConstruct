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

//import ucl.physiol.neuroconstruct.gui.*;
import ucl.physiol.neuroconstruct.gui.ClickProjectHelper;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.project.stimulation.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.UnitConverter;
import ucl.physiol.neuroconstruct.utils.xml.*;

/**
 * Storage for the locations and settings of electrical inputs generated when the Generate
 * cell positions... button is pressed
 *
 * @author Padraig Gleeson
 *  
 */

public class GeneratedElecInputs
{
    ClassLogger logger = new ClassLogger("GeneratedElecInputs");

    Hashtable<String, ArrayList<SingleElectricalInput>> myElecInputs = null;

    private Project project = null;
    
    public GeneratedElecInputs(Project project)
    {
        this.project = project;
        myElecInputs = new Hashtable<String, ArrayList<SingleElectricalInput>>();

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
            ArrayList<SingleElectricalInput> newInputArrayList = new ArrayList<SingleElectricalInput>();
            myElecInputs.put(inputReference, newInputArrayList);
        }
        ArrayList<SingleElectricalInput> inputVector = myElecInputs.get(inputReference);

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
            ArrayList<SingleElectricalInput> newInputArrayList = new ArrayList<SingleElectricalInput>();
            myElecInputs.put(inputReference, newInputArrayList);
        }
        ArrayList<SingleElectricalInput> inputVector = myElecInputs.get(inputReference);

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
    
    
    public Iterator<String> getElecInputsItr()
    {
        return myElecInputs.keySet().iterator();
    }    
    
    @Override
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
    
    
    public String details(boolean html)
    {
        StringBuffer sb = new StringBuffer();

        sb.append("Network contains " + GeneralUtils.getBold(this.getNumberSingleInputs(), html) + " inputs in total"+GeneralUtils.getEndLine(html)+GeneralUtils.getEndLine(html));

        Enumeration keys = this.myElecInputs.keys();

        while (keys.hasMoreElements())
        {
            String input = (String) keys.nextElement();
            ArrayList<SingleElectricalInput> singleInputList = myElecInputs.get(input);
            sb.append("Input: "+GeneralUtils.getBold(input, html) + " has " + GeneralUtils.getBold(singleInputList.size(), html)
                      + " entries"+GeneralUtils.getEndLine(html));
            for (int i = 0; (i < singleInputList.size() && i < 9); i++)
            {
                sb.append("   Input "+i+": "+singleInputList.get(i).details(html)+GeneralUtils.getEndLine(html));
            }
            sb.append(GeneralUtils.getEndLine(html));

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

    public SimpleXMLEntity getNetworkMLElement(int unitSystem) throws NeuroMLException
    {

        SimpleXMLElement inputsElement = null;
        try
        {
            logger.logComment("Going to save file in NeuroML format: " + this.getNumberSingleInputs() +
                              " inputs in total");
            
            if (getNumberSingleInputs()==0)
            {
                return new SimpleXMLComment("There are no electrical inputs present in the network");
            }
            
            inputsElement = new SimpleXMLElement(NetworkMLConstants.INPUTS_ELEMENT);
                            
            
            if (unitSystem == UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS)
            {
                inputsElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.UNITS_ATTR, NetworkMLConstants.UNITS_PHYSIOLOGICAL));
            }
            else if (unitSystem == UnitConverter.GENESIS_SI_UNITS)
            {
                inputsElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.UNITS_ATTR, NetworkMLConstants.UNITS_SI));
            }
            

            Enumeration keys = myElecInputs.keys();

            while (keys.hasMoreElements())
            {
                String inputReference = (String) keys.nextElement();
                ArrayList<SingleElectricalInput> inputsHere = getInputLocations(inputReference);
                
                logger.logComment("Adding " + inputsHere.size() + " inputs");
                
                StimulationSettings nextStim = project.elecInputInfo.getStim(inputReference);
                
                ElectricalInput myElectricalInput = nextStim.getElectricalInput();
                
                SimpleXMLElement inputElement = new SimpleXMLElement(NetworkMLConstants.INPUT_ELEMENT);
                             
                inputElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_NAME_ATTR, inputReference));
                
                if (myElectricalInput instanceof IClamp)
                {
                    IClamp ic = (IClamp)myElectricalInput;
                    
                    /*todo: remove this when Seq Generators removed!*/
                    ic.getDelay().reset();
                    float delay = ic.getDelay().getNumber();
                    ic.getDuration().reset();
                    float duration = ic.getDuration().getNumber();      
                    ic.getAmplitude().reset();
                    float amp = ic.getAmplitude().getNumber();
                            
                    SimpleXMLElement inputTypeElement = new SimpleXMLElement(NetworkMLConstants.PULSEINPUT_ELEMENT);

                    inputTypeElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_DELAY_ATTR, 
                            UnitConverter.getTime(delay, UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+""));
                    
                    inputTypeElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_DUR_ATTR, 
                            UnitConverter.getTime(duration, UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+""));
                    
                    inputTypeElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_AMP_ATTR, 
                            UnitConverter.getCurrent(amp, UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+""));
                    
                    inputElement.addChildElement(inputTypeElement);
                    inputElement.addContent("\n        ");
                }
                else if (myElectricalInput instanceof RandomSpikeTrain)
                {
                    RandomSpikeTrain rst = (RandomSpikeTrain)myElectricalInput;
                    
                    float stimFreq = rst.getRate().getFixedNum();
                    String stimMech = rst.getSynapseType();
                   
                    SimpleXMLElement inputTypeElement = new SimpleXMLElement(NetworkMLConstants.RANDOMSTIM_ELEMENT);
                    inputTypeElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.RND_STIM_FREQ_ATTR, 
                            UnitConverter.getRate(stimFreq, UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+""));
                    
                    inputTypeElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.RND_STIM_MECH_ATTR, stimMech));
                    inputElement.addChildElement(inputTypeElement);
                    inputElement.addContent("\n        ");
                }
                
                SimpleXMLElement inputTargetElement = new SimpleXMLElement(NetworkMLConstants.INPUT_TARGET_ELEMENT);
                
                inputTargetElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_TARGET_CELLGROUP_ATTR, nextStim.getCellGroup()));

                inputElement.addChildElement(inputTargetElement);
                inputTargetElement.addContent("\n            ");
                
                SimpleXMLElement inputTargetSitesElement = new SimpleXMLElement(NetworkMLConstants.INPUT_TARGET_SITES_ELEMENT);                                

                inputTargetElement.addChildElement(inputTargetSitesElement);                
               
                // Iterate around the list of sites
                for (int i=0; i < inputsHere.size() ; i++)
                {
                        
                    inputTargetSitesElement.addContent("\n                ");
                    
                    SimpleXMLElement inputTargetSiteElement = new SimpleXMLElement(NetworkMLConstants.INPUT_TARGET_SITE_ELEMENT);

                    inputTargetSiteElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_SITE_CELLID_ATTR, inputsHere.get(i).getCellNumber()+""));
                    inputTargetSiteElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_SITE_SEGID_ATTR, inputsHere.get(i).getSegmentId()+""));
                    inputTargetSiteElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_SITE_FRAC_ATTR, inputsHere.get(i).getFractionAlong()+""));                               
                    inputTargetSitesElement.addChildElement(inputTargetSiteElement);

                    if (i == inputsHere.size()-1) 
                        inputTargetSitesElement.addContent("\n            ");
                        
                // Next Site
                }                
                inputTargetElement.addContent("\n        ");      
                
                inputsElement.addChildElement(inputElement); 
                inputElement.addContent("\n    ");    
              
            }
            logger.logComment("Finished saving data to inputs element");

        }

        catch (Exception ex)
        {
            throw new NeuroMLException("Problem creating inputs element file", ex);
        }
        return inputsElement;

    }
        
        
    public String getHtmlReport()
    {    
        StringBuffer generationReport = new StringBuffer();
        
        Enumeration<String> e = myElecInputs.keys();
        
        while (e.hasMoreElements())
        {
            String elecInputName = e.nextElement();
            
            StimulationSettings s = project.elecInputInfo.getStim(elecInputName);

            generationReport.append("<b>" + ClickProjectHelper.getElecInputLink(elecInputName) + "</b> ("+s.getElectricalInput().toLinkedString()
                                    +" on "+s.getCellChooser().toNiceString()+" of "
                                    + ClickProjectHelper.getCellGroupLink(s.getCellGroup())
                                    +", seg: "+s.getSegmentID()+")<br>");

            generationReport.append("Number of individual inputs: <b>"+
                                    project.generatedElecInputs.getNumberSingleInputs(elecInputName)
                                    + "</b><br><br>");
        }
        
        if (generationReport.toString().length() == 0)
        {
            generationReport.append("No Electrical Inputs generated<br><br>");

        }

        return generationReport.toString();

    }
    
    public static void main(String[] args)
    {
        try
        {
            GeneratedElecInputs gei = new GeneratedElecInputs(null);

            System.out.println("Internal info: \n"+ gei.toString());

            //IClamp ic = new IClamp(2,3,4, true);

            gei.addSingleInput("Input_0", "IClamp", "cg1", 3, 3, 3);
            gei.addSingleInput("Input_0", "IClamp", "cg1", 38, 3, 3);
            gei.addSingleInput("Input_2", "IClamp", "cg3", 3, 38, 39);

            System.out.println("Internal info: \n"+ gei.toString()); 

            File f = new File("../temp/tempp.txt");

            gei.saveToFile(f);


            GeneratedElecInputs cpr2 = new GeneratedElecInputs(null);

            cpr2.loadFromFile(f);
            System.out.println("New internal info: \n"+ cpr2.toString()); 

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }


}
