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

import java.util.ArrayList;

import javax.swing.event.TreeModelListener;
import javax.swing.table.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.*;
import ucl.physiol.neuroconstruct.neuron.*;
import ucl.physiol.neuroconstruct.genesis.*;
import ucl.physiol.neuroconstruct.hpc.mpi.RemoteLogin;
import ucl.physiol.neuroconstruct.hpc.utils.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;

/**
 * Helper class for displaying multiple simulations in a table or JTree
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class SimulationsInfo extends AbstractTableModel implements TreeModel
{
    ClassLogger logger = new ClassLogger("SimulationsInfo");

    public final static int COL_NUM_NAME = 0;
    public final static int COL_NUM_DATE = 1;

    public static final String COL_NAME_NAME = "Name";
    public static final String COL_NAME_DATE = "Date modified";

    private int minNumberColumns = 2;// due to COL_NUM_NAME and COL_NUM_DATE

    Vector<String> allColumns = new Vector<String>(minNumberColumns);
    Vector<String> columnsShown = new Vector<String>(minNumberColumns);

    public static final String simSummaryFileName = "simulation.props";

    private static String oldSimSummaryFileName = "sim_summary";

    public static final String simulatorPropsFileName = "simulator.props";

    public static final String psicsLogFile = "log.txt";

    Vector<SimulationData> simDataObjs = new Vector<SimulationData>();

    Vector<Properties> extraColumns = new Vector<Properties>();

    File simulationsDir = null;

    ProcessFeedback pf = null;

    private static HashMap<String, String> extraSimProperties = new HashMap<String, String>();


    private TreeMap<String, TreeMap<String, TreeMap<Integer, ArrayList<DataStore>>>> dataInSims
            = new TreeMap<String, TreeMap<String, TreeMap<Integer, ArrayList<DataStore>>>>();


    public SimulationsInfo(File simulationsDir, Vector<String> preferredColumns)
    {
        logger.logComment("New SimulationsInfo created");
        this.simulationsDir = simulationsDir;
        this.columnsShown = preferredColumns;

        pf = new ProcessFeedback()
        {

            public void comment(String comment)
            {
                logger.logComment("ProcessFeedback comment: "+comment);
            }

            public void error(String comment)
            {
                logger.logComment("ProcessFeedback - error: "+comment);
            }
        };

        refresh(false);
    }


    /////////////////////////  Methods for Tree Model  /////////////////////////


    private ArrayList<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();

    public class AllSimRoot extends DefaultMutableTreeNode
    {
        AllSimRoot()
        {
            super("All simulations");
        }
    }
    public class SimNode extends DefaultMutableTreeNode
    {
        String info = null;

        SimNode(SimulationData sd)
        {
            super(sd);
            info = sd.getSimFullInfo();
        }


        @Override
        public String toString()
        {
            return info;
        }
    }

    public class CellGroupNode extends DefaultMutableTreeNode
    {
        private String cellGroupName = null;
        private String simRef = null;

        CellGroupNode(String cgn, String simRef)
        {
            super(cgn);
            this.cellGroupName = cgn;
            this.simRef = simRef;
        }

        @Override
        public String toString()
        {
            return "Cell Group: "+ cellGroupName;
        }

        public String getCellGroupName()
        {
            return cellGroupName;
        }
        public String getSimRef()
        {
            return simRef;
        }
    }
    /*
    public class SegmentNode extends DefaultMutableTreeNode
    {
        SegmentNode(Integer seg)
        {
            super(seg);
        }
    }*/
    public class CellNode extends DefaultMutableTreeNode
    {
        private String cellGroupName = null;
        private String simRef = null;
        private int index;

        CellNode(Integer index, String cgn, String simRef)
        {
            super(index);
            this.cellGroupName = cgn;
            this.simRef = simRef;
            this.index = index;
        }


        @Override
        public String toString()
        {
            return "Cell: "+ index;
        }

        public String getCellGroupName()
        {
            return cellGroupName;
        }
        public String getSimRef()
        {
            return simRef;
        }
        public int getIndex()
        {
            return index;
        }
    }

    public class DataStoreNode extends DefaultMutableTreeNode
    {
        DataStoreNode(DataStore ds)
        {
            super(ds);
        }
    }

    public Object getRoot()
    {
        return new AllSimRoot();
    };


    private void checkTreeMapForSim(SimulationData sd)
    {
        if (dataInSims.get(sd.getSimulationName())==null)
        {
            TreeMap<String, TreeMap<Integer, ArrayList<DataStore>>> treeCG =
                    new TreeMap<String, TreeMap<Integer, ArrayList<DataStore>>>();

            for(DataStore ds: sd.getAllLoadedDataStores())
            {
                String cellGroupName = ds.getCellGroupName();

                int cellIndex = ds.getCellNumber();
                //int segId = ds.getAssumedSegmentId();

                if(treeCG.get(cellGroupName)==null)
                {
                    TreeMap<Integer, ArrayList<DataStore>> treeIndex = new TreeMap<Integer, ArrayList<DataStore>>();
                    treeCG.put(cellGroupName, treeIndex);
                }
                TreeMap<Integer, ArrayList<DataStore>> treeIndex = treeCG.get(cellGroupName);

                if(treeIndex.get(cellIndex)==null)
                {
                    ArrayList<DataStore> ads = new ArrayList<DataStore>();

                    treeIndex.put(cellIndex, ads);
                }

                ArrayList<DataStore> ads = treeIndex.get(cellIndex);

                ads.add(ds);
            }
            dataInSims.put(sd.getSimulationName(), treeCG);

        }
    }

    public Object getChild(Object parent, int index)
    {
        if (parent instanceof AllSimRoot)
        {
            SimulationData sd = simDataObjs.get(index);
            SimNode sn = new SimNode(sd);
            //sn.
            return sn;
        }
        if (parent instanceof SimNode)
        {
            SimulationData sd = (SimulationData)(((SimNode)parent).getUserObject());
            try
            {
                sd.initialise();

                checkTreeMapForSim(sd);

                TreeMap<String, TreeMap<Integer, ArrayList<DataStore>>> treeCG = dataInSims.get(sd.getSimulationName());
                Vector v = new Vector(treeCG.keySet());
                return new CellGroupNode((String)v.get(index), sd.getSimulationName());
            }
            catch (SimulationDataException ex)
            {
                return new DefaultMutableTreeNode(ex.getMessage());
            }
        }
        if (parent instanceof CellGroupNode)
        {
            CellGroupNode cellGroupNode = (CellGroupNode)parent;

            try
            {
                TreeMap<String, TreeMap<Integer, ArrayList<DataStore>>> treeCG = dataInSims.get(cellGroupNode.getSimRef());
                TreeMap<Integer, ArrayList<DataStore>> treeIndex = treeCG.get(cellGroupNode.getCellGroupName());
                Vector v = new Vector(treeIndex.keySet());
                return new CellNode((Integer)v.get(index), cellGroupNode.getCellGroupName(), cellGroupNode.getSimRef());
            }
            catch (Exception ex)
            {
                return new DefaultMutableTreeNode(ex.getMessage());
            }
        }
        if (parent instanceof CellNode)
        {
            CellNode cellNode = (CellNode)parent;
            try
            {
                TreeMap<String, TreeMap<Integer, ArrayList<DataStore>>> treeCG = dataInSims.get(cellNode.getSimRef());
                TreeMap<Integer, ArrayList<DataStore>> treeIndex = treeCG.get(cellNode.getCellGroupName());
                ArrayList<DataStore> ads = treeIndex.get(cellNode.getIndex());
                DataStore ds = ads.get(index);
                return new DataStoreNode(ds);
            }
            catch (Exception ex)
            {
                return new DefaultMutableTreeNode(ex.getMessage());
            }
        }
        return null;
    }


    public int getChildCount(Object parent)
    {
        if (parent instanceof AllSimRoot)
        {
            return simDataObjs.size();
        }
        if (parent instanceof SimNode)
        {
            SimulationData sd = (SimulationData)(((SimNode)parent).getUserObject());
            try
            {
                sd.initialise();

                checkTreeMapForSim(sd);
                TreeMap<String, TreeMap<Integer, ArrayList<DataStore>>> treeCG = dataInSims.get(sd.getSimulationName());

                return treeCG.keySet().size();
            }
            catch (SimulationDataException ex)
            {
                return 1;
            }
        }

        if (parent instanceof CellGroupNode)
        {
            CellGroupNode cellGroupNode = (CellGroupNode)parent;

            try
            {
                TreeMap<String, TreeMap<Integer, ArrayList<DataStore>>> treeCG = dataInSims.get(cellGroupNode.getSimRef());
                TreeMap<Integer, ArrayList<DataStore>> treeIndex = treeCG.get(cellGroupNode.getCellGroupName());

                return treeIndex.size();
            }
            catch (Exception ex)
            {
                return 1;
            }
        }

        if (parent instanceof CellNode)
        {
            CellNode cellNode = (CellNode)parent;
            try
            {
                TreeMap<String, TreeMap<Integer, ArrayList<DataStore>>> treeCG = dataInSims.get(cellNode.getSimRef());
                TreeMap<Integer, ArrayList<DataStore>> treeIndex = treeCG.get(cellNode.getCellGroupName());
                ArrayList<DataStore> ads = treeIndex.get(cellNode.getIndex());
                return ads.size();
            }
            catch (Exception ex)
            {
                return 1;
            }
        }
        return 0;
    }


    public boolean isLeaf(Object node)
    {
        if (node instanceof DataStoreNode)
            return true;
        return false;
    }


    public void valueForPathChanged(TreePath path, Object newValue)
    {

    }

    public int getIndexOfChild(Object parent, Object child)
    {
        //System.out.println("aaaaaaaaaaaaaaaaaaaggghhhhhhhhhhhhhh");
        return -1;
    }

    public void addTreeModelListener(TreeModelListener tml)
    {
        treeModelListeners.add(tml);
    }

    public void removeTreeModelListener(TreeModelListener tml)
    {
        treeModelListeners.remove(tml);
    }

    ////////////////////////////////////////////////////////////////////////////

    public enum ListStyle
    {
        Alphabetic, Date;

    }

    public ListStyle listStyle = ListStyle.Date;

    public void setListStyle(ListStyle ls)
    {
        listStyle = ls;
    }


    public final void refresh(boolean checkRemote)
    {
        logger.logComment("Refreshing the contents of table model");

        allColumns.removeAllElements();
        simDataObjs.removeAllElements();
        extraColumns.removeAllElements();

        dataInSims
            = new TreeMap<String, TreeMap<String, TreeMap<Integer, ArrayList<DataStore>>>>();

        allColumns.add(COL_NUM_NAME, COL_NAME_NAME);
        allColumns.add(COL_NUM_DATE, COL_NAME_DATE);

        File[] childrenDirs = simulationsDir.listFiles();


        // Quick reorder...
        if (childrenDirs!=null && childrenDirs.length>1)
        {

            logger.logComment("There are " + childrenDirs.length + " files in dir: " +
                          simulationsDir.getAbsolutePath());
            if (listStyle.equals(listStyle.Date))
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
            else if (listStyle.equals(listStyle.Alphabetic))
            {
                childrenDirs = GeneralUtils.reorderAlphabetically(childrenDirs, true);
            }
        }

        int rowNumber = 0;

        if (childrenDirs!=null)
        {
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
                            String toRun = pullRemoteScript.getAbsolutePath();

                            if (GeneralUtils.isWindowsBasedPlatform())
                            {
                                String cygwinFriendlyFile = GeneralUtils.convertToCygwinPath(pullRemoteScript.getAbsolutePath());

                                toRun = "bash -c "+cygwinFriendlyFile; // Assumes cygwin installed...
                            }

                            String res = ProcessManager.runCommand(toRun, pf, 3000);
                            logger.logComment("Result of executing pullRemoteScript file: " + pullRemoteScript+": "+ res);
                        }
                        catch (Exception ex)
                        {
                            logger.logComment("Error running: "+ pullRemoteScript+ex);
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
        return columnsShown.get(col);
    }


    public SimulationData getSimulationData(int row)
    {
        return simDataObjs.get(row);
    }


    public Object getValueAt(int row, int col)
    {
        SimulationData sim = simDataObjs.get(row);

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

                String value = propsForSim.getProperty(colName);

                if(colName.indexOf("Time")>=0 || colName.indexOf("time")>=0)
                {
                    return GeneralUtils.getNiceStringForSeconds(value);
                }

                return value;
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

    /*
     * Add an extra properti which will be added to the list of properties in simulation.props
     * and so will be shown in the SimulationBrowser interface. Mostly useful for recording
     * extra information in a Python script
     */
    public static void addExtraSimProperty(String name, String value)
    {
        extraSimProperties.put(name, value);
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

        for(String name: extraSimProperties.keySet())
        {
            props.setProperty(name, extraSimProperties.get(name));
        }


        props.setProperty("neuroConstruct random seed", ProjectManager.getRandomGeneratorSeed()+"");



        StringBuilder simConfigInfo = new StringBuilder(simConfig.getName()+ " (");
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
        StringBuilder pops = new StringBuilder();
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

                StringBuilder info = new StringBuilder("[");
                Cell cell = project.cellManager.getCell(cellType);

                ArrayList allChanMechs = cell.getAllUniformChanMechs(true);

                allChanMechs = (ArrayList)GeneralUtils.reorderAlphabetically(allChanMechs, true);

                for (int j = 0; j < allChanMechs.size(); j++)
                {
                    ChannelMechanism chanMech = (ChannelMechanism)allChanMechs.get(j);
                    Vector<String> groups = cell.getGroupsWithChanMech(chanMech);

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

                ArrayList allSynapses = cell.getAllAllowedSynapseTypes();
                allSynapses = (ArrayList)GeneralUtils.reorderAlphabetically(allSynapses, true);
                for (int k = 0; k < allSynapses.size(); k++)
                {
                    String syn = (String)allSynapses.get(k);
                    Vector groups = cell.getGroupsWithSynapse(syn);
                    info.append(syn.toString() + " on: " + groups + ", ");
                }

                String cellMechs = info.toString();

                if (cellMechs.endsWith(", ")) cellMechs = cellMechs.substring(0, cellMechs.length() - 2);
                cellMechs = cellMechs + "]";
                props.setProperty("Cell Mechanisms on " + cell.getInstanceName(), cellMechs);

                


                props.setProperty("Morph summary " + cell.getInstanceName(), cell.getMorphSummary());

            }
        }

        props.setProperty("Populations", pops.toString());

        StringBuilder conns = new StringBuilder();
        for (String netConn: project.generatedNetworkConnections.getNamesNonEmptyNetConns())
        {
            conns.append(netConn+" ("+project.generatedNetworkConnections.getSynapticConnections(netConn).size()+")  ");
        }

        props.setProperty("Net connections", conns.toString());

        props.setProperty("Simulator", simulator);

        props.setProperty("Simulation temp", project.simulationParameters.getTemperature()+"");

        
        if (simulator.toLowerCase().indexOf("pynn")>=0)
        {
            props.setProperty("Unit system", UnitConverter.getUnitSystemDescription(UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS));
        }
        else if (simulator.equals("LEMS"))
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
            props.setProperty("GUI Mode", project.genesisSettings.getGraphicsMode()+"");

            props.setProperty("spikegen compatibility mode",
                    (project.genesisSettings.getAbsRefractSpikegen()>=0) ?
                        "GENESIS 2 behaviour, abs_refract = "+ project.genesisSettings.getAbsRefractSpikegen() :
                        "NEURON compatible behaviour");

            
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
            props.setProperty("GUI Mode", project.neuronSettings.getGraphicsMode().toString());

            if (project.neuronSettings.isVarTimeStep())
            {
                props.setProperty("Num integration method","CVODE");
                props.setProperty("Variable timestep absolute tolerance", project.neuronSettings.getVarTimeAbsTolerance()+"");
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


    public static String getSimProps(File simulationDir, boolean html)
    {
        return getSimProps(simulationDir, html, false);
    }

    /**
     * Gets the properties from the file named 'simSummaryFileName' in the specified dir
     * @return a nicely formatted string representation of the properties
     */
    public static String getSimProps(File simulationDir, boolean html, boolean summary)
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

        String[] mainProperties = new String[]{"Simulator", "Unit system", "Populations", "Duration", "dt","Sim Config", "RealSimulationTime", "neuroConstruct random seed", "Parallel configuration"};

        for (int i = 0; i < mainProperties.length; i++)
        {
            String value = props.getProperty(mainProperties[i]);
            if(value!=null)
            {
                if(mainProperties[i].indexOf("Time")>=0 || mainProperties[i].indexOf("time")>=0)
                {
                    value = GeneralUtils.getNiceStringForSeconds(value);
                }
                if(mainProperties[i].equals("Populations"))
                {
                    value = GeneralUtils.replaceAllTokens(value, ";","<br>");
                }
                sb.append(createLine(mainProperties[i], value, html));
            }
            allPropNames.remove(mainProperties[i]);
        }

        if (!summary)
        {
            allPropNames = (ArrayList)GeneralUtils.reorderAlphabetically(allPropNames, true);

            // Do the rest
            for (int i = 0; i < allPropNames.size(); i++)
            {
                String propName = (String)allPropNames.get(i);
                String val = props.getProperty(propName);

                if(propName.indexOf("Time")>=0 || propName.indexOf("time")>=0)
                {
                    val = GeneralUtils.getNiceStringForSeconds(val);
                }

                sb.append(createLine(propName, val, html));
            }
        }
        if (html) sb.append("</table>");

        return sb.toString();

    }


    /**
     * Compares 2 sim prop directories
     */
    public static String compareSims(File simulationDir1, File simulationDir2, boolean html)
    {
        Properties props1 = getSimulationProperties(simulationDir1);
        if (props1==null) return "Problem getting simulation properties from directory: "+ simulationDir1;
        Properties props2 = getSimulationProperties(simulationDir2);
        if (props2==null) return "Problem getting simulation properties from directory: "+ simulationDir2;

        StringBuffer sb = new StringBuffer();

        ArrayList<Object> allPropNames = new ArrayList<Object>(props1.keySet());
        for(Object name: props2.keySet())
        {
            if (!allPropNames.contains(name)) allPropNames.add(name);
        }

        String info = "Comparison of simulations";
        
        if (html)
        {
            sb.append("<p>"+info+": </p><br></br>\n<table border=\"1\">\n");

            sb.append(createLine("<b><i>Name of property</i></b>",
                    "<b><i>"+simulationDir1.getName()+"</i></b>",
                    "<b><i>"+simulationDir2.getName()+"</i></b>", html));
        }
        else sb.append(info+": \n\n");


        allPropNames = (ArrayList)GeneralUtils.reorderAlphabetically(allPropNames, true);

        // Do the rest
        for (int i = 0; i < allPropNames.size(); i++)
        {
            String propName = (String)allPropNames.get(i);
            String val1 = props1.getProperty(propName);
            String val2 = props2.getProperty(propName);

            if (val1==null || val2==null)
            {
                if (val1==null) val1 = "-- Property not present --";
                if (val2==null) val2 = "-- Property not present --";
            }
            else if (val1.equals(val2))
            {
                val1="=";
                val2="=";
            }



            if(propName.indexOf("Time")>=0 || propName.indexOf("time")>=0)
            {
                val1 = GeneralUtils.getNiceStringForSeconds(val1);
                val2 = GeneralUtils.getNiceStringForSeconds(val2);
            }


            sb.append(createLine(propName, val1, val2, html));
        }

        if (html) sb.append("</table>");

        return sb.toString();

    }





    private static String createLine(String name, String val, boolean html)
    {
        return createLine(name, val, null, html);
    }

    private static String createLine(String name, String val1, String val2, boolean html)
    {
        int idealPropNameWidth = 30;
        int idealTotalWidth = 120;

        if (html)
        {
            val1 = GeneralUtils.replaceAllTokens(val1, "\n", "<br></br>");
            val1 = GeneralUtils.replaceAllTokens(val1, "  ", "&nbsp;&nbsp;");
            
            String res = "<tr><td>" + name + "</td><td><b>" + val1 + "</b></td>";
            if (val2!=null)
            {
                val2 = GeneralUtils.replaceAllTokens(val2, "\n", "<br></br>");
                val2 = GeneralUtils.replaceAllTokens(val2, "  ", "&nbsp;&nbsp;");
                res = res + "<td><b>" + val2 + "</b></td>";
            }
            res = res + "</tr>\n";

            return res;
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
            String res = name + val1;
            if (val2!=null)
            {
                res = name + val1+", "+val2;
            }
            return GeneralUtils.wrapLine(res, "\n",idealTotalWidth) + "\n";
        }

    };




}
