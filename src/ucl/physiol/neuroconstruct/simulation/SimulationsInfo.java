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

package ucl.physiol.neuroconstruct.simulation;

import java.io.*;
import java.util.*;

import javax.swing.table.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.*;
import ucl.physiol.neuroconstruct.neuron.*;
import ucl.physiol.neuroconstruct.genesis.*;
//import ucl.physiol.neuroconstruct.hpc.mpi.*;
import ucl.physiol.neuroconstruct.hpc.mpi.RemoteLogin;
import ucl.physiol.neuroconstruct.hpc.utils.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;

/**
 * Extension of AbstractTableModel to store the info on multiple simulations
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class SimulationsInfo extends AbstractTableModel
{
    ClassLogger logger = new ClassLogger("SimulationsInfo");

    public final static int COL_NUM_NAME = 0;
    public final static int COL_NUM_DATE = 1;

    public static final String COL_NAME_NAME = "Name";
    public static final String COL_NAME_DATE = "Date modified";

    private int minNumberColumns = 2;// due to COL_NUM_NAME and COL_NUM_DATE

    Vector<String> allColumns = new Vector<String>(minNumberColumns);
    Vector<String> columnsShown = new Vector<String>(minNumberColumns);

    public static final String simSummaryFileName = new String("simulation.props");

    private static String oldSimSummaryFileName = new String("sim_summary");

    public static final String simulatorPropsFileName = new String("simulator.props");

    public static final String psicsLogFile = new String("log.txt");

    Vector<SimulationData> simDataObjs = new Vector<SimulationData>();

    Vector<Properties> extraColumns = new Vector<Properties>();

    File simulationsDir = null;

    ProcessFeedback pf = null;


    public SimulationsInfo(File simulationsDir, Vector<String> preferredColumns)
    {
        logger.logComment("New SimulationsInfo created");
        this.simulationsDir = simulationsDir;
        this.columnsShown = preferredColumns;



        pf = new ProcessFeedback()
        {

            public void comment(String comment)
            {
                logger.logComment("ProcessFeedback comment: "+comment, true);
            }

            public void error(String comment)
            {
                logger.logComment("ProcessFeedback - error: "+comment);
            }
        };

        refresh(false);
    }


    public void refresh(boolean checkRemote)
    {
        logger.logComment("Refreshing the contents of table model");

        allColumns.removeAllElements();
        simDataObjs.removeAllElements();
        extraColumns.removeAllElements();

        allColumns.add(COL_NUM_NAME, COL_NAME_NAME);
        allColumns.add(COL_NUM_DATE, COL_NAME_DATE);

        File[] childrenDirs = simulationsDir.listFiles();

        logger.logComment("There are " + childrenDirs.length + " files in dir: " +
                          simulationsDir.getAbsolutePath());

        // Quick reorder...
        if (childrenDirs.length>1)
        {
            for (int j = 1; j < childrenDirs.length; j++)
            {
                for (int k = 0; k < j; k++)
                {
                    if (childrenDirs[j].lastModified()<childrenDirs[k].lastModified())
                    {
                        File earlierFile = childrenDirs[j];
                        File laterFile = childrenDirs[k];
                        childrenDirs[j] = laterFile;
                        childrenDirs[k] = earlierFile;
                    }
                }
            }
        }

        int rowNumber = 0;

        for (int i = 0; i < childrenDirs.length; i++)
        {
            if (childrenDirs[i].isDirectory())
            {
                logger.logComment("Looking at directory: " + childrenDirs[i].getAbsolutePath());

                SimulationData simData = null;

                File timeFile = SimulationData.getTimesFile(childrenDirs[i]);

                File pullRemoteScript = new File(childrenDirs[i], RemoteLogin.remotePullScriptName);

                File simSummaryFile = new File(childrenDirs[i], simSummaryFileName);

                if (!simSummaryFile.exists())
                {
                    logger.logComment("Trying for the legacy name...");
                    simSummaryFile = new File(childrenDirs[i], oldSimSummaryFileName);
                }

                logger.logComment("Simulation summary file: " + simSummaryFile+", exists: "+ simSummaryFile.exists());

                if (pullRemoteScript.exists() && !timeFile.exists() && checkRemote)
                {

                    try
                    {
                        String res = ProcessManager.runCommand(pullRemoteScript.getAbsolutePath(), pf, 3000);
                        logger.logComment("Result of executing pullRemoteScript file: " + pullRemoteScript+": "+ res, true);
                    }
                    catch (Exception ex)
                    {
                        logger.logComment("Error running: "+ pullRemoteScript+ex, true);
                    }
                }

                try
                {
                    if (simSummaryFile.exists() && timeFile.exists())
                    {
                        simData = new SimulationData(childrenDirs[i].getAbsoluteFile(), true);

                        simDataObjs.add(simData);

                        extraColumns.setSize(rowNumber + 1);

                        Properties simProps = new Properties();

                        try
                        {
                            logger.logComment("Row number: " + rowNumber);

                            //FileInputStream fis = new FileInputStream(simSummaryFile);
                            //simProps.load(fis);
                            //fis.close();

                            simProps = getSimulationProperties(simSummaryFile.getParentFile());
                            logger.logComment("simProps at: " + simSummaryFile.getParentFile()+": "+simProps.keySet());


                            extraColumns.setElementAt(simProps, rowNumber);
                            logger.logComment("extraColumns: " + extraColumns);

                            Enumeration simPropNames = simProps.propertyNames();
                            while (simPropNames.hasMoreElements())
                            {
                                String nextSimProp = (String) simPropNames.nextElement();
                                if (!allColumns.contains(nextSimProp))
                                    allColumns.add(nextSimProp);
                            }

                            rowNumber++;

                            logger.logComment("That's a valid simulation dir...");

                        }
                        catch (Exception ex)
                        {
                            logger.logError("Problem reading the sim summary from file: " + simSummaryFile, ex);
                        }

                    }
                    else if (simSummaryFile.exists() && pullRemoteScript.exists())
                    {

                        simData = new SimulationData(childrenDirs[i].getAbsoluteFile(), false);

                        simData.setDataAtRemoteLocation(true);

                        simDataObjs.add(simData);

                        extraColumns.setSize(rowNumber + 1);

                        logger.logComment("pullRemoteScript file: " + pullRemoteScript+", exists: "+ pullRemoteScript.exists());
                        Properties simProps = new Properties();
                        try
                        {
                            simProps = getSimulationProperties(simSummaryFile.getParentFile());

                            extraColumns.setElementAt(simProps, rowNumber);
                            logger.logComment("extraColumns: " + extraColumns);

                            Enumeration simPropNames = simProps.propertyNames();
                            while (simPropNames.hasMoreElements())
                            {
                                String nextSimProp = (String) simPropNames.nextElement();
                                if (!allColumns.contains(nextSimProp))
                                    allColumns.add(nextSimProp);
                            }


                            rowNumber++;

                            logger.logComment("That's a valid simulation dir...");
                        }
                        catch (Exception ex)
                        {
                            logger.logError("Problem reading the sim summary from file: " + simSummaryFile, ex);
                        }
                    }

                }
                catch (SimulationDataException ex1)
                {
                    logger.logComment("That's not a valid simulation dir...");
                }

            }
        }
        this.fireTableStructureChanged();

    }


    public int getColumnCount()
    {
        return columnsShown.size();
    }

    public int getRowCount()
    {
        return simDataObjs.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnsShown.elementAt(col);
    }


    public SimulationData getSimulationData(int row)
    {
        return simDataObjs.elementAt(row);
    }


    public Object getValueAt(int row, int col)
    {
        SimulationData sim = simDataObjs.elementAt(row);

        switch (col)
        {
            case COL_NUM_NAME:
                return sim.getSimulationName();

            case COL_NUM_DATE:
            {
                if (sim.isDataAtRemoteLocation() && !sim.getTimesFile().exists())
                {
                    return "Remote simulation";
                }
                return sim.getDateModified();
            }
            default:
            {
                String colName = columnsShown.elementAt(col);
                Properties propsForSim = extraColumns.elementAt(row);
                
                if (propsForSim==null) return "-- n/a --";

                return propsForSim.getProperty(colName);
            }
        }
    }


    @Override
    public boolean isCellEditable(int row, int col)
    {
        return false;
    }


    public Vector<String> getAllShownColumns()
    {
        return columnsShown;
    }

    public Vector getAllColumns()
    {
        return allColumns;
    }

    public void addShownColumn(String propName)
    {
        if (!allColumns.contains(propName)) return;
        if (!columnsShown.contains(propName))
            columnsShown.add(propName);
    }

    public void removeShownColumn(String propName)
    {
        columnsShown.remove(propName);
    }



    public void deleteSimulation(int index)
    {
        /** @todo impl */
        logger.logComment("Not yet implemented...");

    }


    /**
     * Creates a record of the main simulation parameters in a properties file in the
     * simulation data directory. In theory this info can be retrieved from the generated
     * hoc/GENESIS files, but this method is more simulator neutral, and easier to browse through
     */
    public static void recordSimulationSummary(Project project,
                                               SimConfig simConfig,
                                               File dirForSummary,
                                               String simulator,
                                               MorphCompartmentalisation mc) throws IOException
    {
        Properties props = new Properties();

        props.setProperty("Duration", simConfig.getSimDuration()+"");
        props.setProperty("dt", project.simulationParameters.getDt()+"");
        props.setProperty("Global Cm", project.simulationParameters.getGlobalCm()+"");
        props.setProperty("Global Rm", project.simulationParameters.getGlobalRm()+"");
        props.setProperty("Global Ra", project.simulationParameters.getGlobalRa()+"");


        props.setProperty("neuroConstruct random seed", ProjectManager.getRandomGeneratorSeed()+"");



        StringBuffer simConfigInfo = new StringBuffer(simConfig.getName()+ " (");
        ArrayList<String> allElements = new ArrayList<String>();

        allElements.addAll(simConfig.getCellGroups());

        allElements.addAll(simConfig.getNetConns());
        allElements.addAll(simConfig.getInputs());
        allElements.addAll(simConfig.getPlots());

        for (int i = 0; i < allElements.size(); i++)
        {
            simConfigInfo.append(allElements.get(i));
            if (i<allElements.size()-1) simConfigInfo.append(", ");
            else simConfigInfo.append(")");
        }

        if (mc != null && !mc.getName().equals(OriginalCompartmentalisation.ORIG_COMP))
        {
            props.setProperty("Compartmentalisation", mc.getName());
        }

        props.setProperty("Sim Config", simConfigInfo.toString());
        

        props.setProperty("Parallel configuration", simConfig.getMpiConf().toString());


        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();
        StringBuffer pops = new StringBuffer();
        for (int i = 0; i < cellGroupNames.size(); i++)
        {
            String cellGroupName = cellGroupNames.get(i);

            // No point including info if it's not included in the sim config...

            if (simConfig.getCellGroups().contains(cellGroupName))
            {
                String cellType = project.cellGroupsInfo.getCellType(cellGroupName);
                int num = project.generatedCellPositions.getNumberInCellGroup(cellGroupName);
                pops.append(cellGroupName + " ("+cellType+"): " + num);
                if (i < cellGroupNames.size() - 1) pops.append("; ");

                StringBuffer info = new StringBuffer("[");
                Cell cell = project.cellManager.getCell(cellType);

                ArrayList<ChannelMechanism> allChanMechs = cell.getAllUniformChanMechs(true);

                for (int j = 0; j < allChanMechs.size(); j++)
                {
                    ChannelMechanism chanMech = allChanMechs.get(j);
                    Vector groups = cell.getGroupsWithChanMech(chanMech);

                    info.append(chanMech.getName() + " (" + chanMech.getDensity() + ")" +
                                " on: " + groups + ", ");
                }
                Iterator<VariableMechanism> vMechs = cell.getVarMechsVsParaGroups().keySet().iterator();
                while(vMechs.hasNext())
                {
                    VariableMechanism vm = vMechs.next();
                    ParameterisedGroup pg = cell.getVarMechsVsParaGroups().get(vm);
                    
                    info.append(vm + " present on: " + pg + ", ");
                }

                ArrayList<String> allSynapses = cell.getAllAllowedSynapseTypes();
                for (int k = 0; k < allSynapses.size(); k++)
                {
                    String syn = allSynapses.get(k);
                    Vector groups = cell.getGroupsWithSynapse(syn);
                    info.append(syn.toString() + " on: " + groups + ", ");
                }
                String procs = info.toString();

                if (procs.endsWith(", ")) procs = procs.substring(0, procs.length() - 2);
                procs = procs + "]";
                props.setProperty("Cell Processes on " + cell.getInstanceName(), procs);

                


                props.setProperty("Morph summary " + cell.getInstanceName(), cell.getMorphSummary());

            }
        }

        props.setProperty("Populations", pops.toString());

        props.setProperty("Simulator", simulator);

        props.setProperty("Simulation temp", project.simulationParameters.getTemperature()+"");

        
        if (simulator.toLowerCase().indexOf("pynn")>=0)
        {
            props.setProperty("Unit system", UnitConverter.getUnitSystemDescription(UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS));
        }
        else if (simulator.equals("PSICS"))
        {
            props.setProperty("Unit system", UnitConverter.getUnitSystemDescription(UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS));
            props.setProperty("Single channel conductance", project.psicsSettings.getSingleChannelCond()+"");
            props.setProperty("Spatial discretisation", project.psicsSettings.getSpatialDiscretisation()+"");
        }
        else if (simulator.equals("GENESIS") || simulator.equals("MOOSE"))
        {
            props.setProperty("Num integration method", project.genesisSettings.getNumMethod().toString());
            props.setProperty("Unit system", UnitConverter.getUnitSystemDescription(project.genesisSettings.getUnitSystemToUse()));
            props.setProperty("Symmetric compartments", project.genesisSettings.isSymmetricCompartments()+"");
            props.setProperty(simulator+" random seed", project.genesisFileManager.getCurrentRandomSeed()+"");
            props.setProperty("No GUI Mode", !project.genesisSettings.isGraphicsMode()+"");

            
            for (ScriptLocation sl: ScriptLocation.allLocations)
            {
                String text = project.genesisSettings.getNativeBlock(sl);
                text = NativeCodeLocation.parseForSimConfigSpecifics(text, simConfig.getName());
                if (text.trim().length()>0)
                {
                    text = GeneralUtils.replaceAllTokens(text, "\n", " \n"); //to make reading it in the table easier...
                    props.setProperty(simulator+" extra script, Type "+sl.getPositionReference() ,
                            text);
                }
            }
            

            props.setProperty("Script format", simulator+" Script");
            

            props.setProperty("Script generation time",
                    project.genesisFileManager.getCurrentGenTime()+"");

        }
        else if (simulator.equals("NEURON"))
        {
            props.setProperty("Unit system", UnitConverter.getUnitSystemDescription(UnitConverter.NEURON_UNITS));
            props.setProperty("NEURON random seed", project.neuronFileManager.getCurrentRandomSeed()+"");
            props.setProperty("No GUI Mode", !project.neuronSettings.isGraphicsMode()+"");

            if (project.neuronSettings.isVarTimeStep())
            {
                props.setProperty("Num integration method","CVODE");
            }
            else
            {
                props.setProperty("Num integration method","Fixed time step");
            }
            
            for (NativeCodeLocation ncl: NativeCodeLocation.allLocations)
            {
                String text = project.neuronSettings.getNativeBlock(ncl);
                text = NativeCodeLocation.parseForSimConfigSpecifics(text, simConfig.getName());
                if (text.trim().length()>0)
                {
                    text = GeneralUtils.replaceAllTokens(text, "\n", " \n"); //to make reading it in the table easier...
                    props.setProperty("NEURON extra hoc, Type "+ncl.getPositionReference() ,
                            text);
                }
            }

            props.setProperty("Script generation time",
                    project.neuronFileManager.getCurrentGenTime()+"");

            if (project.neuronFileManager.getCurrentRunMode()==NeuronFileManager.RUN_HOC)
            {
                props.setProperty("Script format", "Hoc");
            }
            else if (project.neuronFileManager.getCurrentRunMode()==NeuronFileManager.RUN_PYTHON_XML)
            {
                props.setProperty("Script format", "Python/XML");
                
            }  
            else if (project.neuronFileManager.getCurrentRunMode()==NeuronFileManager.RUN_PYTHON_HDF5)
            {
                props.setProperty("Script format", "Python/HDF5");
                
            }  
            else if (project.neuronFileManager.getCurrentRunMode()==NeuronFileManager.RUN_VIA_CONDOR)
            {
                props.setProperty("Run mode",
                                  "Run via Condor");
                
            }   



        }

        Vector stims = project.elecInputInfo.getAllStims();

        for (int i = 0; i < stims.size(); i++)
        {
           StimulationSettings stim = (StimulationSettings)stims.elementAt(i);
           if (simConfig.getInputs().contains(stim.getReference()))
           {
               props.setProperty("Stimulation: " + i, stim.toString() +" on "+stim.getCellGroup());
           }
       }


       Vector simpNetCons = project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames();

       for (int i = 0; i < simpNetCons.size(); i++)
       {
           String next = (String)simpNetCons.elementAt(i);
          if (simConfig.getNetConns().contains(next))
          {
              props.setProperty("Net Conn: " + i, project.morphNetworkConnectionsInfo.getSummary(next));
          }
       }



        props.setProperty("neuroConstruct version", GeneralProperties.getVersionNumber()+"");



        File summaryFile = new File(dirForSummary, simSummaryFileName);
        FileOutputStream fos = new FileOutputStream(summaryFile);

        props.storeToXML(fos, "This is a summary of the simulation parameters"+
                    " to assist reviewing of saved simulations in neuroConstruct.");

       //props.store(fos, "This is a summary of the simulation parameters"+
       //             " to assist reviewing of saved simulations in neuroConstruct. Saved with version "
       //             + GeneralProperties.getVersionNumber());

        fos.close();
    }


    /**
     * Gets the properties from the file named 'simSummaryFileName' in the specified dir
     * @return the Properties object containing the simulation parameters, or null if not found
     */
    public static Properties getSimulationProperties(File simulationDir)
    {
        Properties props = new Properties();
        File simulationPropsFile = new File(simulationDir, simSummaryFileName);

        if (!simulationPropsFile.exists())
        {
            //logger.logComment("Trying for the legacy name...");
            simulationPropsFile = new File(simulationDir, oldSimSummaryFileName);
        }

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(simulationPropsFile);

            try
            {
                props.loadFromXML(fis);  // In case stored in XML

                fis.close();
            }
            catch (IOException ex)
            {
                fis = new FileInputStream(simulationPropsFile);

                try
                {
                    props.load(fis);  // In case stored in ordinary props file

                    fis.close();

                    return props;
                }
                catch (IOException ex2)
                {
                    return null;

                }
            }
        }
        catch (FileNotFoundException ex)
        {
            // this props file is necessary...
            return null;
        }


        Properties simulatorProps = new Properties();
        File simulatorPropsFile = new File(simulationDir, simulatorPropsFileName);

        FileInputStream fis2 = null;
        try
        {
            fis2 = new FileInputStream(simulatorPropsFile);

            try
            {
                simulatorProps.loadFromXML(fis2);
                fis2.close();

            }
            catch (IOException ex)
            {
                fis2 = new FileInputStream(simulatorPropsFile);
                try
                {
                    simulatorProps.load(fis2);
                    fis2.close();
                }
                catch (IOException ex2)
                {
                    // this props file is not necessary...
                }
            }
        }
        catch (FileNotFoundException ex)
        {
            // this props file is not necessary...
        }

        Enumeration simulatorPropEnum = simulatorProps.propertyNames();

        while (simulatorPropEnum.hasMoreElements())
        {
            String next = (String)simulatorPropEnum.nextElement();
            props.setProperty(next, simulatorProps.getProperty(next));
        }

        if (props.getProperty("Simulator")!=null && props.getProperty("Simulator").equals("PSICS"))
        {
            File psicsInfo = new File(simulationDir, psicsLogFile);
            String contents = GeneralUtils.readShortFile(psicsInfo);
            String timeTag = "ppp: psics-out";

            int index = contents.indexOf(timeTag);
            if(index>=0)
            {
                String simTime = contents.substring(index+timeTag.length(), contents.indexOf("\n", index+timeTag.length()));
                try
                {
                    float time = (float)Double.parseDouble(simTime.trim());

                    props.setProperty("RealSimulationTime", time+"");
                }
                catch(NumberFormatException e)
                {
                    props.setProperty("RealSimulationTime", "Could not extract from: "+ psicsInfo.getAbsolutePath());
                }
            }
        }

        return props;
    }

    /**
     * Gets the properties from the file named 'simSummaryFileName' in the specified dir
     * @return a nicely formatted string representation of the properties
     */
    public static String getSimProps(File simulationDir, boolean html)
    {
        Properties props = getSimulationProperties(simulationDir);
        if (props==null) return "Problem getting simulation properties from directory: "+ simulationDir;

        StringBuffer sb = new StringBuffer();

        Set<Object> names = props.keySet();

        ArrayList<Object> allPropNames = new ArrayList<Object>();
        allPropNames.addAll(names);

        if (html)
        {
            sb.append("<p>Parameters of simulation : </p><br></br>\n<table border=\"1\">\n");
        }
        else sb.append("Parameters of simulation : \n\n");


        // Show some of the main props first, as the props can come out in undetermined order

        String[] mainProperties = new String[]{"Simulator", "Unit system", "Populations", "Duration", "dt","Sim Config"};

        for (int i = 0; i < mainProperties.length; i++)
        {
            String value = props.getProperty(mainProperties[i]);
            if(value!=null)
                sb.append(createLine(mainProperties[i], value, html));
            allPropNames.remove(mainProperties[i]);
        }

        // Do the rest
        for (int i = 0; i < allPropNames.size(); i++)
        {
            String propName = (String)allPropNames.get(i);
            String val = props.getProperty(propName);

            sb.append(createLine(propName, val, html));
        }
        if (html) sb.append("</table>");

        return sb.toString();

    }

    private static String createLine(String name, String val, boolean html)
    {
        int idealPropNameWidth = 30;
        int idealTotalWidth = 120;

        if (html)
        {
            val = GeneralUtils.replaceAllTokens(val, "\n", "<br></br>");
            val = GeneralUtils.replaceAllTokens(val, "  ", "&nbsp;&nbsp;");
            return "<tr><td>" + name + "</td><td><b>" + val + "</b></td></tr>\n";
        }
        else
        {
            name = name + ": ";

            if (name.length() <= idealPropNameWidth)
            {

                for (int i = name.length(); i <= idealPropNameWidth; i++)
                {
                    name = name + " ";
                }
            }
            return GeneralUtils.wrapLine(name + val, "\n",idealTotalWidth) + "\n";
        }

    };




}
