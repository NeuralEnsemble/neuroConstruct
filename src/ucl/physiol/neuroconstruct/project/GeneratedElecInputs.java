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

import java.io.*;
import java.util.*;


import ucl.physiol.neuroconstruct.gui.ClickProjectHelper;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.project.stimulation.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.UnitConverter;
import ucl.physiol.neuroconstruct.utils.units.Units;
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
    private ClassLogger logger = new ClassLogger("GeneratedElecInputs");

    private Hashtable<String, ArrayList<SingleElectricalInput>> myElecInputs = null;

    private Project project = null;
    
    public GeneratedElecInputs(Project project)
    {
        this.project = project;
        myElecInputs = new Hashtable<String, ArrayList<SingleElectricalInput>>();

    }

    public void reset()
    {
        this.myElecInputs.clear();

        //logger.logComment("Reset called. Info: "+ this.toString()+"\nEmpty: "+myElecInputs.isEmpty(), true);
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
        addSingleInput(inputReference,
                               inputType,
                               cellGroup,
                               cellNumber,
                               segmentId,
                               fractionAlong);
    }
    
    public void addSingleInput(String inputReference,
                               String inputType,
                               String cellGroup,
                               int cellNumber,
                               int segmentId,
                               float fractionAlong,
                               InputInstanceProps ip)
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
                                                  fractionAlong,
                                                  ip));
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

    public ArrayList<String> getNonEmptyInputRefs()
    {
        ArrayList<String> neir = new ArrayList<String>();

        Enumeration<String> keys = this.myElecInputs.keys();

        while (keys.hasMoreElements())
        {
            String ref = keys.nextElement();
            ArrayList inputArrayList = myElecInputs.get(ref);
            if(inputArrayList.size()>0) 
                neir.add(ref);
        }

        return neir;
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
        StringBuilder sb = new StringBuilder();

        sb.append("GeneratedElecInputs with " + this.getNumberSingleInputs() + " inputs in total\n");

        Enumeration keys = this.myElecInputs.keys();

        while (keys.hasMoreElements())
        {
            String input = (String) keys.nextElement();
            ArrayList<SingleElectricalInput> singleInputList = myElecInputs.get(input);
            StimulationSettings ss = project.elecInputInfo.getStim(input);
            sb.append(input + " ("+ss.toLongString()+") has " + singleInputList.size()
                      + " entries.\n");
            for (int i = 0; (i < singleInputList.size() && i < 9); i++)
            {
                SingleElectricalInput sei = singleInputList.get(i);
                sb.append("   Input "+i+": "+sei.toString());
                if (sei.getInstanceProps()!=null)
                    sb.append(" ("+sei.getInstanceProps().details(false)+")");
                    
                sb.append("\n");
            }

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
        StringBuilder sb = new StringBuilder();

        sb.append("Network contains " + GeneralUtils.getBold(this.getNumberSingleInputs(), html) + " inputs in total"+GeneralUtils.getEndLine(html)+GeneralUtils.getEndLine(html));

        Enumeration keys = this.myElecInputs.keys();

        String indent = "    ";
        if (html) indent = "&nbsp;&nbsp;&nbsp;&nbsp;";

        while (keys.hasMoreElements())
        {
            String input = (String) keys.nextElement();
            ArrayList<SingleElectricalInput> singleInputList = myElecInputs.get(input);
            sb.append("Input: "+GeneralUtils.getBold(input, html) + " has " + GeneralUtils.getBold(singleInputList.size(), html)
                      + " entries"+GeneralUtils.getEndLine(html));
            for (int i = 0; (i < singleInputList.size()); i++)
            {
                sb.append("Input "+i+": "+singleInputList.get(i).details(html)+GeneralUtils.getEndLine(html));
                if (singleInputList.get(i).getInstanceProps()!=null)
                {
                    sb.append(indent+"Input specific properties: "+singleInputList.get(i).getInstanceProps().details(html)+GeneralUtils.getEndLine(html));
                }
            }
            sb.append(GeneralUtils.getEndLine(html));

        }
        return sb.toString();
    }



    public SimpleXMLEntity getNetworkMLElement(int unitSystem) throws NeuroMLException
    {
        return getNetworkMLEntities(unitSystem, NeuroMLConstants.NeuroMLVersion.NEUROML_VERSION_1, null).get(0);
    }

    public ArrayList<SimpleXMLEntity> getNetworkMLEntities(int unitSystem, NeuroMLConstants.NeuroMLVersion version, SimpleXMLElement topLevelCompElement) throws NeuroMLException
    {
        ArrayList<SimpleXMLEntity> entities = new ArrayList<SimpleXMLEntity>();

        Units timeUnits = UnitConverter.timeUnits[unitSystem];
        Units currentUnits = UnitConverter.currentUnits[unitSystem];

        SimpleXMLElement inputsElement = null;
        try
        {
            logger.logComment("Going to save file in NeuroML format: " + this.getNumberSingleInputs() +
                              " inputs in total");
            
            if (getNumberSingleInputs()==0)
            {
                SimpleXMLComment comm = new SimpleXMLComment("There are no electrical inputs present in the network");
                entities.add(comm);
                return entities;
            }

            boolean nml2 = version.isVersion2();

            if (!nml2)
            {
                inputsElement = new SimpleXMLElement(NetworkMLConstants.INPUTS_ELEMENT);
                entities.add(inputsElement);

                if (unitSystem == UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS)
                {
                    inputsElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.UNITS_ATTR, NetworkMLConstants.UNITS_PHYSIOLOGICAL));
                }
                else if (unitSystem == UnitConverter.GENESIS_SI_UNITS)
                {
                    inputsElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.UNITS_ATTR, NetworkMLConstants.UNITS_SI));
                }
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
                    
                    float delay = ic.getDel().getNominalNumber();
                    float duration = ic.getDur().getNominalNumber();     
                    float amplitude = ic.getAmp().getNominalNumber();
                            
                    SimpleXMLElement inputTypeElement = new SimpleXMLElement(NetworkMLConstants.PULSEINPUT_ELEMENT);
                    
                    float del = (float)UnitConverter.getTime(delay, UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem);
                    float dur = (float)UnitConverter.getTime(duration, UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem);
                    float amp = (float)UnitConverter.getCurrent(amplitude, UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem);
                    
                    inputTypeElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_DELAY_ATTR, del+""));
                    
                    inputTypeElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_DUR_ATTR, dur +""));
                    
                    inputTypeElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_AMP_ATTR, amp +""));
                    
                    inputElement.addChildElement(inputTypeElement);
                    
                    inputElement.addContent("\n        ");

                    if (nml2)
                    {
                        SimpleXMLElement pulseGenElement = new SimpleXMLElement(NetworkMLConstants.NEUROML2_PULSE_GEN_ELEMENT);
                        pulseGenElement.addAttribute(NeuroMLConstants.NEUROML_ID_V2, inputReference);
                        pulseGenElement.addAttribute(NetworkMLConstants.INPUT_DELAY_ATTR, del+timeUnits.getNeuroML2Symbol());
                        pulseGenElement.addAttribute(NetworkMLConstants.INPUT_DUR_ATTR, dur+timeUnits.getNeuroML2Symbol());
                        pulseGenElement.addAttribute(NetworkMLConstants.INPUT_AMP_ATTR, amp+currentUnits.getNeuroML2Symbol());

                        topLevelCompElement.addContent("\n\n    ");
                        topLevelCompElement.addChildElement(pulseGenElement);
                        topLevelCompElement.addContent("\n\n    ");
                    }

                }
                else if (myElectricalInput instanceof RandomSpikeTrain)
                {
                    RandomSpikeTrain rst = (RandomSpikeTrain)myElectricalInput;
                    
                    float stimFreq = rst.getRate().getNominalNumber();
                    String stimMech = rst.getSynapseType();
                   
                    SimpleXMLElement inputTypeElement = new SimpleXMLElement(NetworkMLConstants.RANDOMSTIM_ELEMENT);
                    inputTypeElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.RND_STIM_FREQ_ATTR, 
                            (float)UnitConverter.getRate(stimFreq, UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+""));
                    
                    inputTypeElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.RND_STIM_MECH_ATTR, stimMech));
                    inputElement.addChildElement(inputTypeElement);
                    inputElement.addContent("\n        ");
                }
                else
                {
                    throw new NeuroMLException("Error trying to save input "+inputReference+". Cannot save in NeuroML an input of type: "+ myElectricalInput.getType());

                }
                
                SimpleXMLElement inputTargetElement = new SimpleXMLElement(NetworkMLConstants.INPUT_TARGET_ELEMENT);
                
                inputTargetElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_TARGET_POPULATION_ATTR, nextStim.getCellGroup()));

                inputElement.addChildElement(inputTargetElement);
                inputTargetElement.addContent("\n            ");
                
                SimpleXMLElement inputTargetSitesElement = new SimpleXMLElement(NetworkMLConstants.INPUT_TARGET_SITES_ELEMENT); 
                
                inputTargetSitesElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_SITES_SIZE_ATTR,inputsHere.size()+""));

                inputTargetElement.addChildElement(inputTargetSitesElement);

                if (version.isVersion2betaOrLater()) {

                    SimpleXMLElement inputListElement = new SimpleXMLElement(NetworkMLConstants.NEUROML2_INPUT_LIST_ELEMENT);
                    entities.add(inputListElement);
                    inputListElement.addAttribute(NeuroMLConstants.NEUROML_ID_V2, nextStim.getReference());
                    inputListElement.addAttribute(NetworkMLConstants.NEUROML2_INPUT_COMPONENT, inputReference);
                    inputListElement.addAttribute(NetworkMLConstants.NEUROML2_INPUT_POPULATION, nextStim.getCellGroup());

                    //inputElement.addContent("\n    ");
                    inputTargetSitesElement = inputListElement;

                }
               
                // Iterate around the list of sites
                for (int i=0; i < inputsHere.size() ; i++)
                {
                    inputTargetSitesElement.addContent("\n                ");

                    SingleElectricalInput sei = inputsHere.get(i);
                    
                    SimpleXMLElement inputTargetSiteElement = new SimpleXMLElement(NetworkMLConstants.INPUT_TARGET_SITE_ELEMENT);

                    inputTargetSiteElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_SITE_CELLID_ATTR, sei.getCellNumber()+""));
                    inputTargetSiteElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_SITE_SEGID_ATTR, sei.getSegmentId()+""));
                    inputTargetSiteElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_SITE_FRAC_ATTR, sei.getFractionAlong()+""));
                    
                    if (!nml2) inputTargetSitesElement.addChildElement(inputTargetSiteElement);


                    if(sei.getInstanceProps()!=null)
                    {
                        inputTargetSiteElement.addContent("\n                ");
                        inputTargetSiteElement.addComment("Adding the site specific props");

                        if (sei.getInstanceProps() instanceof IClampInstanceProps)
                        {
                            IClampInstanceProps ic = (IClampInstanceProps)sei.getInstanceProps();

                            float delay = (float)UnitConverter.getTime(ic.getDelay(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem);
                            float duration = (float)UnitConverter.getTime(ic.getDuration(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem);
                            float amp = (float)UnitConverter.getCurrent(ic.getAmplitude(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem);

                            if (!nml2)
                            {
                                SimpleXMLElement inputTypeElement = new SimpleXMLElement(NetworkMLConstants.PULSEINPUT_INSTANCE_ELEMENT);

                                inputTypeElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_DELAY_ATTR,delay+""));

                                inputTypeElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_DUR_ATTR, duration+""));

                                //System.out.println("Converted "+amp+" to "+ a);
                                inputTypeElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.INPUT_AMP_ATTR, amp+""));

                                inputTargetSiteElement.addContent("                    ");
                                inputTargetSiteElement.addChildElement(inputTypeElement);

                                inputTargetSiteElement.addContent("\n                ");
                            }
                            else
                            {
                                SimpleXMLElement pulseGenElement = new SimpleXMLElement(NetworkMLConstants.NEUROML2_PULSE_GEN_ELEMENT);
                                pulseGenElement.addAttribute(NeuroMLConstants.NEUROML_ID_V2, inputReference+"__"+i);
                                pulseGenElement.addAttribute(NetworkMLConstants.INPUT_DELAY_ATTR, delay+timeUnits.getNeuroML2Symbol());
                                pulseGenElement.addAttribute(NetworkMLConstants.INPUT_DUR_ATTR, duration+timeUnits.getNeuroML2Symbol());
                                pulseGenElement.addAttribute(NetworkMLConstants.INPUT_AMP_ATTR, amp+currentUnits.getNeuroML2Symbol());

                                topLevelCompElement.addContent("\n\n    ");
                                topLevelCompElement.addChildElement(pulseGenElement);
                                topLevelCompElement.addContent("\n\n    ");

                                if (version.isVersion2alpha())
                                {
                                    String target = nextStim.getCellGroup()+"["+sei.getCellNumber()+"]";
                                    SimpleXMLElement expInputElement = new SimpleXMLElement(NetworkMLConstants.NEUROML2_EXP_INPUT_ELEMENT);
                                    expInputElement.addAttribute(NetworkMLConstants.NEUROML2_EXP_INPUT_TARGET_ATTR, target);
                                    expInputElement.addAttribute(NetworkMLConstants.NEUROML2_EXP_INPUT_INPUT_ATTR, inputReference+"__"+i);

                                    entities.add(expInputElement);
                                }
                                else
                                {
                                    String target = "../"+nextStim.getCellGroup()+"/"+sei.getCellNumber()+"/"+project.cellGroupsInfo.getCellType(nextStim.getCellGroup());
                                    SimpleXMLElement expInputElement = new SimpleXMLElement(NetworkMLConstants.NEUROML2_INPUT_LIST_ELEMENT);
                                    expInputElement.addAttribute(NetworkMLConstants.NEUROML2_EXP_INPUT_TARGET_ATTR, target);
                                    expInputElement.addAttribute(NetworkMLConstants.NEUROML2_EXP_INPUT_INPUT_ATTR, inputReference+"__"+i);

                                    entities.add(expInputElement);
                                }
                            }
                        }
                        else if (sei.getInstanceProps() instanceof RandomSpikeTrainInstanceProps)
                        {
                            RandomSpikeTrainInstanceProps rst = (RandomSpikeTrainInstanceProps)sei.getInstanceProps();

                            float stimFreq = rst.getRate();
                            //String stimMech = rst.get;

                            SimpleXMLElement inputTypeElement = new SimpleXMLElement(NetworkMLConstants.RANDOMSTIM_INSTANCE_ELEMENT);

                            inputTypeElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.RND_STIM_FREQ_ATTR,
                                    (float)UnitConverter.getRate(stimFreq, UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+""));

                            //inputTypeElement.addAttribute(new SimpleXMLAttribute(NetworkMLConstants.RND_STIM_MECH_ATTR, stimMech));

                            inputTargetSiteElement.addContent("                    ");
                            inputTargetSiteElement.addChildElement(inputTypeElement);
                            inputTargetSiteElement.addContent("\n                ");
                        }
                        else
                        {
                            throw new NeuroMLException("Error trying to save input "+inputReference+". Cannot save in NeuroML an input of type: "+ myElectricalInput.getType());

                        }
                    }
                    else
                    {
                        if (nml2)
                        {
                            if (version.isVersion2alpha())
                            {
                                String target = nextStim.getCellGroup()+"["+sei.getCellNumber()+"]";
                                SimpleXMLElement expInputElement = new SimpleXMLElement(NetworkMLConstants.NEUROML2_EXP_INPUT_ELEMENT);
                                expInputElement.addAttribute(NetworkMLConstants.NEUROML2_EXP_INPUT_TARGET_ATTR, target);
                                expInputElement.addAttribute(NetworkMLConstants.NEUROML2_EXP_INPUT_INPUT_ATTR, inputReference);

                                entities.add(expInputElement);
                            }
                            else
                            {
                                String target = "../"+nextStim.getCellGroup()+"/"+sei.getCellNumber()+"/"+project.cellGroupsInfo.getCellType(nextStim.getCellGroup());
                                SimpleXMLElement expInputElement = new SimpleXMLElement(NetworkMLConstants.NEUROML2_INPUT_ELEMENT);
                                expInputElement.addAttribute(NeuroMLConstants.NEUROML_ID_V2, i+"");
                                expInputElement.addAttribute(NetworkMLConstants.NEUROML2_EXP_INPUT_TARGET_ATTR, target);
                                expInputElement.addAttribute(NetworkMLConstants.NEUROML2_INPUT_DESTINATION, NetworkMLConstants.NEUROML2_INPUT_DESTINATION_DEFAULT);

                                inputTargetSitesElement.addChildElement(expInputElement);
                            }
                        }
                    }

                    if (i == inputsHere.size()-1) 
                        inputTargetSitesElement.addContent("\n            ");
                        
                // Next Site
                }                
                inputTargetElement.addContent("\n        ");      

                if (!nml2)
                {
                    inputsElement.addChildElement(inputElement);
                    inputElement.addContent("\n    ");
                }
              
            }
            logger.logComment("Finished saving data to inputs element");

        }

        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new NeuroMLException("Problem creating inputs element file", ex);
        }
        return entities;

    }
        
        
    public String getHtmlReport()
    {    
        StringBuilder generationReport = new StringBuilder();
        
        Enumeration<String> e = myElecInputs.keys();
        
        while (e.hasMoreElements())
        {
            String elecInputName = e.nextElement();
            
            StimulationSettings s = project.elecInputInfo.getStim(elecInputName);

            generationReport.append("<b>" + ClickProjectHelper.getElecInputLink(elecInputName) + "</b> ("+s.getElectricalInput().toLinkedString()
                                    +" on "+s.getCellChooser().toNiceString()+" of "
                                    + ClickProjectHelper.getCellGroupLink(s.getCellGroup())
                                    +", segs: "+s.getSegChooser()+")<br>");

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
            Project p = Project.loadProject(new File(ProjectStructure.getnCExamplesDir(), "Ex5_Networks/Ex5_Networks.ncx"), null);

            GeneratedElecInputs gei = new GeneratedElecInputs(p);

            System.out.println("Internal info: \n"+ gei.toString());

            //IClamp ic = new IClamp(2,3,4, true);

            gei.addSingleInput("Input_0", "IClamp", "cg1", 3, 3, 3, null);
            gei.addSingleInput("Input_0", "IClamp", "cg1", 38, 3, 3, null);
            gei.addSingleInput("Input_2", "IClamp", "cg3", 3, 38, 39, null);

            System.out.println("Internal info: \n"+ gei.toString()); 

            File f = new File("../temp/tempp.txt");

            gei.saveToFile(f);


            GeneratedElecInputs cpr2 = new GeneratedElecInputs(null);



            System.out.println("----  v1.8.1: \n"+gei.getNetworkMLElement(UnitConverter.GENESIS_SI_UNITS).getXMLString("    ", false));

            ArrayList<SimpleXMLEntity> els = gei.getNetworkMLEntities(UnitConverter.GENESIS_SI_UNITS, NeuroMLConstants.NeuroMLVersion.NEUROML_VERSION_2_ALPHA, null);

            System.out.println("----  v2.0 alpha: ");
            for (SimpleXMLEntity el: els)
                System.out.println(el.getXMLString("    ", false));

            els = gei.getNetworkMLEntities(UnitConverter.GENESIS_SI_UNITS, NeuroMLConstants.NeuroMLVersion.NEUROML_VERSION_2_BETA, null);

            System.out.println("----  v2.0 beta: ");
            for (SimpleXMLEntity el: els)
                System.out.println(el.getXMLString("    ", false));

            System.out.println("-------------- ");

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }


}
