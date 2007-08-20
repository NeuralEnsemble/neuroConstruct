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

package ucl.physiol.neuroconstruct.simulation;

import java.io.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;
import java.text.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.project.SimPlot;
import ucl.physiol.neuroconstruct.project.PostSynapticObject;

/**
 * Class which stores all the data from a simulation, including references to data files
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

public class SimulationData
{
    private ClassLogger logger = new ClassLogger("SimulationData");


    private ArrayList<DataStore> dataSources = new ArrayList<DataStore>();

    private ArrayList<String> allCellRefsCached = null;
    private ArrayList<String> allVoltCellRefsCached = null;

    private float[] times = null;

    public static final String TIME_DATA_FILE = "time."+SimPlot.CONTINUOUS_DATA_EXT;
    public static final String POSITION_DATA_FILE = "CellPositions."+SimPlot.CONTINUOUS_DATA_EXT;
    public static final String NETCONN_DATA_FILE = "NetworkConnections."+SimPlot.CONTINUOUS_DATA_EXT;
    public static final String ELEC_INPUT_DATA_FILE = "ElectricalInputs."+SimPlot.CONTINUOUS_DATA_EXT;

    private boolean dataLoaded = false;

    private File dataDirectory = null;
    private File timesDataFile = null;

    private int suggestedInitCapData = 1000;
    private int suggestedInitCapSpikes = 50;

    /**
     * Gives an indication whether the voltages are present for just the soma or lots of segs...
     */
    private boolean onlySomaValues = true;


    private SimulationData()
    {
    }


    public SimulationData(File dataDirectory) throws SimulationDataException
    {
        //logger.setThisClassVerbose(true);

        logger.logComment("New SimulationData created with data dir: " + dataDirectory);

        if (!dataDirectory.exists() || !dataDirectory.isDirectory())
            throw new SimulationDataException(dataDirectory.getAbsolutePath()+ " is not a directory");

        this.dataDirectory = dataDirectory;

        timesDataFile = new File(dataDirectory.getAbsolutePath()
                                      + System.getProperty("file.separator")
                                      + TIME_DATA_FILE);


        if (!timesDataFile.exists())
            throw new SimulationDataException(timesDataFile.getAbsolutePath()+ " (time data) not found");


    }

    public void reset()
    {
        this.allCellRefsCached = null;
        this.allVoltCellRefsCached = null;
        this.dataSources.clear();
        times = null;
        dataLoaded = false;
    }

    public void initialise()  throws SimulationDataException
    {
        if (dataLoaded)
        {
            logger.logComment("initialise() called but dataLoaded...");
            return;
        }
        this.reset();

        logger.logComment("+++   Initialising SimulationData "+this.hashCode()+" with directory: "+ dataDirectory.getAbsolutePath());

        //GeneralUtils.timeCheck("Starting reading times file");

        Properties props = SimulationsInfo.getSimulationProperties(dataDirectory);

        float timeConversionFactor = 1;

        String unitSystemDesc = props.getProperty("Unit system");

        int unitSystem = UnitConverter.getUnitSystemIndex(unitSystemDesc);

        timeConversionFactor = (float)UnitConverter.getTime(timeConversionFactor,
                                                     unitSystem,
                                                     UnitConverter.NEUROCONSTRUCT_UNITS);


        times = readDataFileToArray(timesDataFile, timeConversionFactor);

        //GeneralUtils.timeCheck("Starting reading voltages");

        File[] cellDataFiles = dataDirectory.listFiles(
            new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                if ( (name.endsWith("." + SimPlot.CONTINUOUS_DATA_EXT) || name.endsWith("." + SimPlot.SPIKE_EXT))
                    && !name.equals(TIME_DATA_FILE)
                    && !name.equals(POSITION_DATA_FILE)
                    && !name.equals(NETCONN_DATA_FILE)
                    && !name.equals(ELEC_INPUT_DATA_FILE))
                {
                    logger.logComment("-----   Taking " + name);
                    return true;
                }
                else
                {
                    logger.logComment("-----   Rejecting " + name);
                    return false;
                }

            }
        });

        logger.logComment("Found: " + cellDataFiles.length);

        for (int fileIndex = 0; fileIndex < cellDataFiles.length; fileIndex++)
        {
            logger.logComment("-----   Looking at "+fileIndex+": "+cellDataFiles[fileIndex]);

            String dataSourceName = null;

            if (cellDataFiles[fileIndex].getName().indexOf("."+SimPlot.CONTINUOUS_DATA_EXT)>0)
            {
                dataSourceName = cellDataFiles[fileIndex].getName().substring(0,
                                                                      (int) cellDataFiles[fileIndex].getName().length()
                                                                      - ("." + SimPlot.CONTINUOUS_DATA_EXT).length());
            }
            else if (cellDataFiles[fileIndex].getName().indexOf("."+SimPlot.SPIKE_EXT)>0)
            {
                dataSourceName = cellDataFiles[fileIndex].getName().substring(0,
                                                                      (int) cellDataFiles[fileIndex].getName().length()
                                                                      - ("." + SimPlot.SPIKE_EXT).length());
            }

            logger.logComment("dataSourceName: "+dataSourceName);

            String variable = getVariable(dataSourceName);

            logger.logComment("variable: "+variable);

            String cellSegRef = getCellSegRef(dataSourceName);

            logger.logComment("cellSegRef: "+cellSegRef);


            PostSynapticObject pso = getPostSynapticObject(dataSourceName);

            logger.logComment("PostSynapticObject: "+pso);

            int cellNum = getCellNum(cellSegRef);

            int segId = getSegmentId(cellSegRef);

            String cellGroup = getCellGroup(cellSegRef);


            logger.logComment("Looking at file: "+ cellDataFiles[fileIndex].getName()
                              + " for cellGroup: "+ cellGroup
                              +", cellNum: "+cellNum
                              +", segId: "+segId
                              +", variable: "+variable
                              +", cellSegRef: "+ cellSegRef
                              +", PostSynapticObject; "+pso);

            if (segId>0)
            {
                onlySomaValues = false;
            }
            float conversionFactor = 1;


            String xUnit = "";
            String yUnit = "";


            if (variable.equals(SimPlot.VOLTAGE) || variable.equals(SimPlot.REV_POT))
            {
                conversionFactor = (float) UnitConverter.getVoltage(conversionFactor,
                                                                    unitSystem,
                                                                    UnitConverter.NEUROCONSTRUCT_UNITS);

                yUnit = UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol();
            }
            else if (variable.indexOf(SimPlot.SPIKE)>=0)
            {
                conversionFactor = 1;
            }
            else if (variable.indexOf(SimPlot.CURRENT) >= 0)
            {
                conversionFactor = (float) UnitConverter.getCurrentDensity(conversionFactor,
                                                                           unitSystem,
                                                                           UnitConverter.NEUROCONSTRUCT_UNITS);

                yUnit = UnitConverter.currentUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol();
            }
            else if (variable.indexOf(SimPlot.CONCENTRATION) >= 0)
            {
                conversionFactor = (float) UnitConverter.getConcentration(conversionFactor,
                                                                          unitSystem,
                                                                          UnitConverter.NEUROCONSTRUCT_UNITS);
                System.out.println("Conc conv factor: "+ conversionFactor);

                yUnit = UnitConverter.concentrationUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol();
            }
            else if (variable.indexOf(SimPlot.COND_DENS) >= 0)
            {
                conversionFactor = (float) UnitConverter.getConductanceDensity(conversionFactor,
                                                                               unitSystem,
                                                                               UnitConverter.NEUROCONSTRUCT_UNITS);

                yUnit = UnitConverter.conductanceDensityUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol();
            }
            else if (variable.indexOf(SimPlot.SYN_COND) >= 0)
            {
                conversionFactor = (float) UnitConverter.getConductance(conversionFactor,
                                                                               unitSystem,
                                                                               UnitConverter.NEUROCONSTRUCT_UNITS);

                yUnit = UnitConverter.conductanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol();
            }




            if (yUnit.length()>0)
            {
                xUnit =  "ms";
            }

            if (variable.indexOf(SimPlot.SPIKE)<0)
            {
                float[] dataArray = readDataFileToArray(cellDataFiles[fileIndex], conversionFactor);
                DataStore ds = new DataStore(dataArray, cellGroup, cellNum, segId, variable, xUnit, yUnit, pso);

                dataSources.add(ds);

            }
            else
            {
                float[] spikeArray = readSpikesToArray(cellDataFiles[fileIndex], times, timeConversionFactor);
                DataStore ds = new DataStore(spikeArray, cellGroup, cellNum, segId, variable, xUnit, yUnit, pso);

                dataSources.add(ds);
            }

        }

        //GeneralUtils.timeCheck("Finished reading voltages");

        logger.logComment("There have been "+dataSources.size()+" data stores loaded:");

        for (DataStore ds: dataSources)
        {
            logger.logComment(ds.toString());
        }

        dataLoaded = true;
    }

/*
    public boolean dataOnlyForSoma()
    {
        return onlySomaValues;
    }*/


    public int getNumberTimeSteps()  throws SimulationDataException
    {
        if (!dataLoaded) throw new SimulationDataException("Data not yet loaded from files!");
        return times.length;
    }
/*
    public int getNumberVoltageSources()  throws SimulationDataException
    {
        if (!dataLoaded) throw new SimulationDataException("Data not yet loaded from files!");
        return this.getCellSegRefs(true).size();
    }*/


    public float getStartTime()  throws SimulationDataException
    {
        if (!dataLoaded) throw new SimulationDataException("Data not yet loaded from files!");
        return times[0];
    }


    public float getEndTime()  throws SimulationDataException
    {
        if (!dataLoaded) throw new SimulationDataException("Data not yet loaded from files!");
        return times[times.length-1];
    }

    public float getSimulationTime(int timeStep)  throws SimulationDataException
    {
        if (!dataLoaded) throw new SimulationDataException("Data not yet loaded from files!");
        return times[timeStep];
    }

    /**
     * Gets the references to the voltage source. Could be just a cell ref or a cell and segment reference
     * (i.e. CellRef_123 or CellRef_123.SegRef)
     */
    public ArrayList<String> getCellSegRefs(boolean voltageOnly)
    {
        ArrayList<String> refs = new ArrayList<String>();

        if (voltageOnly)
        {
            if (this.allVoltCellRefsCached == null)
            {
                for (DataStore ds : dataSources)
                {
                    if (ds.variable.equals(SimPlot.VOLTAGE))
                    {
                        refs.add(ds.getCellSegRef());
                    }
                }
                allVoltCellRefsCached = refs;
            }
            else
            {
                refs = allVoltCellRefsCached;
            }

        }
        else
        {
            if (this.allCellRefsCached == null)
            {
                for (DataStore ds : dataSources)
                {
                    refs.add(ds.getCellSegRef());
                }
                allCellRefsCached = refs;
            }
            else
            {
                refs = allCellRefsCached;
            }
        }
        //System.out.println("Refs: "+refs);
        return refs;
    }


/*
    private float getVoltageAtTimeStep(int timeStep, String cellSegRef)  throws SimulationDataException
    {
        if (!dataLoaded) throw new SimulationDataException("Data not yet loaded from files!");

        float[] volts = getVoltageAtAllTimes(cellSegRef);
        return volts[timeStep];
    }



    public float getValueAtTimeStep(int timeStep, String cellSegRef, String var) throws SimulationDataException
    {
        if (!dataLoaded)throw new SimulationDataException("Data not yet loaded from files!");

        if (var.equals(SimPlot.VOLTAGE) || var.indexOf(SimPlot.SPIKE) >= 0)
            return getVoltageAtTimeStep(timeStep, cellSegRef);

        float[] values = getDataAtAllTimes(cellSegRef, var).getDataPoints();
        return values[timeStep];
    }

*/
    public float[] getVoltageAtAllTimes(String cellSegRef)  throws SimulationDataException
    {
        if (!dataLoaded) throw new SimulationDataException("Data not yet loaded from files!");

        ArrayList<DataStore> datas = this.getDataForCellSegRef(cellSegRef, false);

        DataStore volts = null;


        for (DataStore ds: datas)
        {
            String var = ds.getVariable();

            if (var.equals(SimPlot.VOLTAGE))
            {
                volts = getDataAtAllTimes(cellSegRef, SimPlot.VOLTAGE, true);
                return volts.dataPoints;
            }

            if (var.indexOf(SimPlot.SPIKE)>=0)
            {
                volts = getDataAtAllTimes(cellSegRef, var, true);
                return volts.dataPoints;
            }
        }
        throw new SimulationDataException("Problem finding voltage data in "+cellSegRef+"");


    }


    public DataStore getDataAtAllTimes(String cellItemRef,
                                       String variable,
                                       boolean incSpikeOrVoltage)  throws SimulationDataException
    {
        if (!dataLoaded) throw new SimulationDataException("Data not yet loaded from files!");

        String cellSegRef = getCellSegRef(cellItemRef);

        for (DataStore ds : dataSources)
        {
           // logger.logComment("Checking getDataAtAllTimes: "+ds+" against "+ cellItemRef
                 //             +", var: "+variable+", cellSegRef: "+cellSegRef);
          //


            if ( (ds.variable.equals(variable) ||
                  (incSpikeOrVoltage &&
                   (variable.indexOf(SimPlot.SPIKE) >= 0 || variable.equals(SimPlot.VOLTAGE)) &&
                   (ds.getVariable().indexOf(SimPlot.SPIKE) >= 0 || ds.getVariable().equals(SimPlot.VOLTAGE))))
                 && cellSegRefsEqual(cellSegRef, ds.getCellSegRef()))
            {
                return ds;
            }
        }

        throw new SimulationDataException("Problem loading data for "+variable+" in "+cellItemRef+"");
    }


    public float[] getAllTimes()  throws SimulationDataException
    {
        if (!dataLoaded) throw new SimulationDataException("Data not yet loaded from files!");
        return times;
    }



    private float[] readDataFileToArray(File dataFile, float scaleFactor) throws SimulationDataException
    {
        String nextLine = null;
        float[] data = null;
        try
        {
            Reader in = new FileReader(dataFile);
            LineNumberReader reader = new LineNumberReader(in);

            /** @todo check if there's a quicker way to do this */

            ArrayList<Float> tempList = new ArrayList<Float>(suggestedInitCapData);

            while ( (nextLine = reader.readLine()) != null && nextLine.length()>0)
            {
                if (!nextLine.startsWith("//") && nextLine.trim().length()>0)
                {
                    float nextEntry = Float.parseFloat(nextLine);
                    tempList.add(scaleFactor * nextEntry);
                }
            }

            if (tempList.size() > suggestedInitCapData) suggestedInitCapData = tempList.size();

            in.close();
            data = new float[tempList.size()];

            for (int i = 0; i < tempList.size(); i++)
            {
                data[i] = tempList.get(i);
            }
            //System.out.println("Read in "+data.length+" values. First: "+data[0]+", last: "+data[data.length-1] );
            return data;
        }
        catch (IOException e)
        {
            throw new SimulationDataException("Error reading from file: "+ dataFile.getAbsolutePath(), e);
        }
        catch (NumberFormatException ne)
        {
            throw new SimulationDataException("Error reading line: ("+nextLine+") from file: "+ dataFile.getAbsolutePath(), ne);
        }

    }



    private float[] readSpikesToArray(File spikeFile, float[] times, float timeConversionFactor) throws SimulationDataException
    {
        logger.logComment("Reading spikes from file: "+ spikeFile);

        float nonSpikingVal = -100;
        float spikingVal = 100;


        String nextLine = null;
        float[] data = null;
        try
        {

            Reader in = new FileReader(spikeFile);
            LineNumberReader reader = new LineNumberReader(in);


            ArrayList<Float> spikeTimes = new ArrayList<Float>(suggestedInitCapSpikes);

            while ( (nextLine = reader.readLine()) != null && nextLine.length()>0)
            {
                if (!nextLine.startsWith("//") && nextLine.trim().length()>0)
                {
                    float nextEntry = Float.parseFloat(nextLine);
                    //logger.logComment("Found line: "+ nextEntry);
                    spikeTimes.add(nextEntry);
                }
            }

            in.close();

            if (suggestedInitCapSpikes< spikeTimes.size()) suggestedInitCapSpikes = spikeTimes.size();

            data = new float[times.length];
            int spikeCount = 0;

            boolean insideSpike = false;

            for (int timeStep = 0; timeStep < times.length; timeStep++)
            {
                float time = times[timeStep];

                if (spikeCount < spikeTimes.size() && time >= (spikeTimes.get(spikeCount) * timeConversionFactor))
                {
                    if (!insideSpike)
                    {
                        data[timeStep] = spikingVal;
                        spikeCount++;

                        insideSpike = true;
                    }
                    else
                    {
                        data[timeStep] = nonSpikingVal;
                        spikeCount++;
                    }
                }
                else
                {
                    data[timeStep] = nonSpikingVal;
                    insideSpike = false;
                }
            }


            //System.out.println("Read in "+data.length+" values. First: "+data[0]+", last: "+data[data.length-1] );
            return data;
        }
        catch (IOException e)
        {
            throw new SimulationDataException("Error reading from file: "+ spikeFile.getAbsolutePath(), e);
        }
        catch (NumberFormatException ne)
        {
            throw new SimulationDataException("Error reading line: ("+nextLine+") from file: "+ spikeFile.getAbsolutePath(), ne);
        }

    }



    public String getDateModified()
    {
        long timeModified = timesDataFile.lastModified();

        SimpleDateFormat formatter = new SimpleDateFormat("H:mm:ss (MMM d, yy)");
        java.util.Date modified = new java.util.Date(timeModified);


        return formatter.format(modified);

    }


    public String toString()
    {
        return getSimulationName() + ": recorded at "+ getDateModified();
    }

    public String getSimulationName()
    {
        return dataDirectory.getName();
    }


    public File getCellPositionsFile()
    {
        return new File(dataDirectory.getAbsolutePath()
                        + System.getProperty("file.separator")
                        + POSITION_DATA_FILE);

    }


    public File getSimulationDirectory()
    {
        return dataDirectory;

    }


    public Properties getSimulationProperties()
    {
        return SimulationsInfo.getSimulationProperties(dataDirectory);
    }



    public File getNetConnectionsFile()
    {
        return new File(dataDirectory.getAbsolutePath()
                        + System.getProperty("file.separator")
                        + NETCONN_DATA_FILE);

    }



    public File getElecInputsFile()
    {
        return new File(dataDirectory.getAbsolutePath()
                        + System.getProperty("file.separator")
                        + ELEC_INPUT_DATA_FILE);

    }

    public ArrayList<DataStore> getDataForCellSegRef(String cellSegRef, boolean inclSynapses)
    {
        if (!dataLoaded) return null;

        ArrayList<DataStore> vars = new ArrayList<DataStore>();

        for (DataStore ds : dataSources)
        {
            logger.logComment("Checking getDataForCellSegRef: "+ds+" against "+ cellSegRef);

            if (cellSegRefsEqual(ds.getCellSegRef(), cellSegRef))
            {
                logger.logComment("Equals...: " );
                vars.add(ds);
            }
            if (inclSynapses && ds.getCellRef().equals(getCellOnlyReference(cellSegRef)))
            {
                if (ds.isSynapticMechData())
                {
                    logger.logComment("Found a syn mech...: " );
                    vars.add(ds);
                }
            }
        }
        return vars;
    }


    public ArrayList<String> getVariablesForAny()
    {
        if (!dataLoaded) return null;

        ArrayList<String> vars = new ArrayList<String>();

        for (DataStore ds : dataSources)
        {

            if (!vars.contains(ds.variable))
            {
                vars.add(ds.variable);
            }
        }
        return vars;
    }


    /**
     * Gets all the CellSegRefs relevant for that CellRefs, i.e. all recorded segments for that cell
     */
    public ArrayList<String> getCellSegRefsForCellRef(String cellRef)
    {
        if (!dataLoaded) return null;

        ArrayList<String> cellSegRefs = new ArrayList<String>();

        for (DataStore ds : dataSources)
        {
            logger.logComment("Checking getCellSegRefsForCellRef: "+ds+" against "+ cellRef);

            if (ds.getCellRef().equals(cellRef))
            {
                if (!cellSegRefs.contains(ds.getCellSegRef()))
                    cellSegRefs.add(ds.getCellSegRef());
            }
        }
        return cellSegRefs;
    }



    public ArrayList<String> getCellItemRefsForVar(String variable, boolean incSpikeOrVoltage)
    {
        if (!dataLoaded) return null;

        ArrayList<String> cellItemRefs = new ArrayList<String> ();

        for (DataStore ds : dataSources)
        {
            logger.logComment("Checking getCellItemRefsForVar: " + ds + " against " + variable);

            if (ds.getVariable().equals(variable) ||
                (incSpikeOrVoltage &&
                 (variable.indexOf(SimPlot.SPIKE)>=0 || variable.equals(SimPlot.VOLTAGE))&&
                 (ds.getVariable().indexOf(SimPlot.SPIKE)>=0 || ds.getVariable().equals(SimPlot.VOLTAGE))))
            {
                String ref = ds.getCellSegRef();

                if (ds.pso!=null)
                {
                    ref = ref +"."+ ds.pso.getSynRef();
                }

                if (!cellItemRefs.contains(ref))
                    cellItemRefs.add(ref);
            }
        }
        logger.logComment("cellItemRefs: "+cellItemRefs);

        return cellItemRefs;
    }





    public static String getCellRef(String cellGroupName, int cellNumber)
    {
        return cellGroupName + "_" + cellNumber;
    }


    public static String getCellSegRef(String cellGroupName, int cellNumber, int segId)
    {
        return getCellSegRef(cellGroupName, cellNumber, segId, false);
    }


    public boolean isOnlySomaValues()
    {
        return onlySomaValues;
    }

    public static boolean cellSegRefsEqual(String cellSegRefA, String cellSegRefB)
    {
        if (cellSegRefA.equals(cellSegRefB)) return true;

        if(cellSegRefA.endsWith(".0"))
        {
            if (cellSegRefB.equals(cellSegRefA.substring(0,cellSegRefA.length()-2)))
                return true;
        }
        if(cellSegRefB.endsWith(".0"))
        {
            if (cellSegRefA.equals(cellSegRefB.substring(0,cellSegRefB.length()-2)))
                return true;
        }

        return false;
    }


    public static String getCellSegRef(String cellGroupName, int cellNumber, int segId, boolean onlySomaValues)
    {
        if (segId<0) return cellGroupName + "_" + cellNumber;

        if (onlySomaValues)
        {
            return cellGroupName + "_" + cellNumber;
        }
        return cellGroupName + "_" + cellNumber+"."+segId;
    }


    public static String getCellGroup(String cellRefOrCellSegRef)
    {
        String cellRef = getCellRef(cellRefOrCellSegRef);
        return cellRef.substring(0, cellRef.lastIndexOf("_"));
    }

    private static String getCellRef(String cellRefOrCellSegRef)
    {
        String cellRef = null;

        if (cellRefOrCellSegRef.indexOf(".")>=0)
        {
            cellRef = cellRefOrCellSegRef.substring(0,cellRefOrCellSegRef.indexOf("."));
        }
        else
        {
            cellRef = cellRefOrCellSegRef;
        }
        //System.out.println("cellRef: "+cellRef);
        return cellRef;

    }


    public static int getCellNum(String cellRefOrCellSegRef)
    {
        String cellRef = getCellRef(cellRefOrCellSegRef);

        String num = cellRef.substring(cellRef.lastIndexOf("_") + 1);

        try
        {
            return Integer.parseInt(num);
        }
        catch (Exception ex) // could also be null...
        {
            return -1;
        }

    }




    /**
     * cellSegReference used to be of form SingleGranuleCell_0.gcdend1_2_seg (when individual segs
     * are recorded) or SingleGranuleCell_0 for only soma recorded
     */
    public static String getSegmentName(String cellSegReference)
    {
        if (cellSegReference.indexOf(".")<0)
        {
            return null;
        }
        else
        {
            return cellSegReference.substring(cellSegReference.indexOf(".") + 1);
        }
    }


    /**
     * Input: SampleCellGroup_0.0, output SampleCellGroup_0
     */
    public static String getCellOnlyReference(String cellSegReference)
    {
        if (cellSegReference.indexOf(".")<0) // shouldn't be
            return cellSegReference;

        return cellSegReference.substring(0, cellSegReference.indexOf("."));
    }



    /**
     * dataSourceName be in form:
     *     SampleCellGroup_0.0.NetConn_1.DoubExpSyn.6.COND for Syn obj
     */
    public static PostSynapticObject getPostSynapticObject(String dataSourceName)
    {
        String[] broken = dataSourceName.split("\\.");

        if (broken.length<5) return null;

        PostSynapticObject pso = new PostSynapticObject(broken[2],
                                                        broken[3],
                                                        getCellNum(broken[0]),
                                                        Integer.parseInt(broken[1]),
                                                        Integer.parseInt(broken[4]));

        return pso;
    }


    /**
     * dataSourceName be in form:
     *     SampleCellGroup_0.0 or
     *     SampleCellGroup_0.0.NaConductance_m or
     *     SampleCellGroup_0.NaConductance_m
     *     SampleCellGroup_0
     *     or
     *     SampleCellGroup_0.0.NetConn_1.DoubExpSyn.6.COND for Syn obj
     */
    public static String getVariable(String dataSourceName)
    {
        if (dataSourceName.indexOf(".")<0)
            return SimPlot.VOLTAGE;

        //String lastPart = dataSourceName.substring(dataSourceName.lastIndexOf(".")+1);

        String[] broken = dataSourceName.split("\\.");

        if (broken.length==2)
        {
            try
            {
                Integer.parseInt(broken[1]); // will succeed if dataSourceName is just SampleCellGroup_0.0
                return SimPlot.VOLTAGE;
            }
            catch (NumberFormatException ex)
            {
                return broken[1];
            }
        }

        return broken[broken.length-1];

    }

    public static boolean isSynDataSource(String dataSourceName)
    {
        return dataSourceName.split("\\.").length >3;
    }


    /**
     * dataSourceName be in form:
     *     SampleCellGroup_0.0 or
     *     SampleCellGroup_0.0.NaConductance_m or
     *     SampleCellGroup_0.NaConductance_m
     *     SampleCellGroup_0
     *     or
     *     SampleCellGroup_0.0.NetConn_1.DoubExpSyn.6.COND for Syn obj

     *
     * @return SampleCellGroup_0 or SampleCellGroup_0.0 depending on dataSourceName
     */
    public static String getCellSegRef(String dataSourceName)
    {
        if (dataSourceName.indexOf(".")<0) return dataSourceName;

        //String lastPart = dataSourceName.substring(dataSourceName.lastIndexOf(".")+1);


        String[] broken = dataSourceName.split("\\.");

        if (broken.length == 2)
        {
            try
            {
                Integer.parseInt(broken[1]); // will succeed if dataSourceName is just SampleCellGroup_0.0
                return dataSourceName;
            }
            catch (NumberFormatException ex) // will be thrown if dataSourceName is like SampleCellGroup_0.NaConductance_m
            {
                return dataSourceName.substring(0, dataSourceName.lastIndexOf("."));
            }
        }

        return broken[0]+"."+broken[1];


    }



    public static int getSegmentId(String cellSegReference)
    {
        if (cellSegReference.indexOf(".")<0)
            return -1;

        String id = cellSegReference.substring(cellSegReference.indexOf(".") + 1);

        if (id.indexOf(".") > 0)
        {
            id = id.substring(0, id.lastIndexOf("."));
        }

        try
        {
            return Integer.parseInt(id);
        }
        catch (NumberFormatException ex)
        {
            return -1;
        }

    }




    public static void main(String[] args)
    {
        //File f = new File("projects/PlotSave/simulations/Sim_43/");
        File f = new File("projects/Spiky/simulations/Sim_33");
        SimulationData simulationData1 = null;
        try
        {

            //System.out.println("Info: "+ SimulationsInfo.getSimProps(f, false));
            simulationData1 = new SimulationData(f);
            simulationData1.initialise();

            //System.out.println("dataOnlyForSoma: "+simulationData1.dataOnlyForSoma());
            //System.out.println("Num steps: "+simulationData1.getNumberTimeSteps());
            //System.out.println("Cell value: "+simulationData1.getVoltageAtTimeStep(33, "CellGroup_2_3.0"));



        }
        catch (SimulationDataException ex)
        {
            System.out.println("Error...");
            ex.printStackTrace();
        }

    }


    /**
     * Could do with a better name...
     */
    public class DataStore
    {
        private float[] dataPoints;

        private String cellGroupName = null;
        private int cellNumber = -1;
        private int segId = -1;
        private String variable = SimPlot.VOLTAGE;

        private String xUnit = "";
        private String yUnit = "";

        private float maxVal = -1* Float.MAX_VALUE;
        private float minVal = Float.MAX_VALUE;

        /**
         * Duplication of data here...
         */
        private PostSynapticObject pso = null;



        public DataStore(float[] dataPoints,
                         String cellGroupName,
                         int cellNumber,
                         int segId,
                         String variable,
                         String xUnit,
                         String yUnit,
                         PostSynapticObject pso)
        {
            this.dataPoints = dataPoints;
            this.cellGroupName = cellGroupName;
            this.cellNumber = cellNumber;
            this.segId = segId;
            this.variable = variable;
            this.xUnit = xUnit;
            this.yUnit = yUnit;
            this.pso = pso;

            for (int i = 0; i < dataPoints.length; i++)
            {
                if (dataPoints[i]>maxVal) maxVal = dataPoints[i];
                if (dataPoints[i]<minVal) minVal = dataPoints[i];
            }

        }

        public String getCellRef()
        {
            return SimulationData.getCellRef(cellGroupName, cellNumber);
        }

        public String getCellSegRef()
        {
            return SimulationData.getCellSegRef(cellGroupName, cellNumber, segId);
        }

        public String getCellGroupName()
        {
            return this.cellGroupName;
        }
        public String getVariable()
        {
            return this.variable;
        }

        public PostSynapticObject getPostSynapticObject()
        {
            return this.pso;
        }

        public boolean isSynapticMechData()
        {
            return pso != null;
        }




        public int getCellNumber()
        {
            return this.cellNumber;
        }

        public float[] getDataPoints()
        {
            return dataPoints;
        }


        public int getAssumedSegmentId()
        {
            if (segId<0) return 0;
            return segId;
        }


        public float getMaxVal()
        {
            return this.maxVal;
        }

        public float getMinVal()
        {
            return this.minVal;
        }




        public boolean isSegmentSpecified()
        {
            return segId>0;
        }



        public String getXUnit()
        {
            return this.xUnit;
        }

        public String getYUnit()
        {
            return this.yUnit;
        }


        public String toString()
        {
            String synInfo = "";
            if (pso!=null) synInfo =  " (synapse: "+pso.getSynRef()+")";
            String info = "DataStore"+synInfo+" for "+variable+" on segment: "+segId+" on "+ getCellRef();

            info = info + ": ("+dataPoints[0]+", "+dataPoints[1]+", ... ["+dataPoints.length+" entries])";

            return info;
        }
    }


}
