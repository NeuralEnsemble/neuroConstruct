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

import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * The general parameters needed for running a simulation
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */
public class SimulationParameters
{

    public static final int RECORD_ONLY_SOMA = 2;
    public static final int RECORD_EVERY_SEGMENT = 4;

    private String reference;
    private float duration;
    private float dt;

    /**
     * pretty much deprecated, what to save set in tab Input and Output instead...
     *
     * private int recordingMode = SIMULATION_NOT_RECORDED;
    public static final int SIMULATION_NOT_RECORDED = 0;
    public static final int SIMULATION_RECORD_TO_FILE = 1;
     */


    /**
     * deprecated, what to save set in tab Input and Output instead...
     */
    private int whatToRecord;

    private float globalCm = 1e-8f;  // units uS/um2
    private float globalRa = 300;
    private float globalRm = 3.3333333e8f;
    private float initVm = -60;
    private float globalVLeak = -59.4f;
    private float temperature = 6.3f;

    /**
     * True implies that the simulation name will be taken from the text field,
     * false means the application will generate a new sim ref every time
     */
    private boolean specifySimName;
    /**
     * True leads to a copy being made in the simulation directory of the original
     * hoc/genesis files which were used to generate the simulation data
     */
    private boolean saveCopyGenSimFiles;

    public SimulationParameters()
    {

    }

    public SimulationParameters(String simulationReference,
                                float simulationDuration,
                                float dt,
                                int whatToRecord,
                                boolean holdSimName,
                                boolean saveCopyHoc)
    {
        this.reference = simulationReference;
        this.duration = simulationDuration;
        this.dt = dt;
        this.whatToRecord = whatToRecord;
        this.specifySimName = holdSimName;
        this.saveCopyGenSimFiles = saveCopyHoc;
    }

    /**
     * Gets the defaults from GeneralProperties. This is put in a separate
     * function (a.o.t the constructor) to allow the XMLEncoder to record the
     * correct values at the time of saving
     */
    public void initialiseDefaultValues()
    {
        reference = "Sim_1";
        duration = GeneralProperties.getDefaultSimulationDuration();
        dt = GeneralProperties.getDefaultSimulationDT();
        whatToRecord =  SimulationParameters.RECORD_ONLY_SOMA;
        specifySimName = false;
        saveCopyGenSimFiles = true;
    }


    public String toString()
    {
        return "SimulationParameters [Reference: " + reference
            + ", duration: " + duration
            + ", dt: " + dt
            +", holdSimName: " + specifySimName
            + ", saveCopyHoc: " + saveCopyGenSimFiles + "]";
    }

    public float getDt()
    {
        return dt;
    }
    public float getDuration()
    {
        return duration;
    }

    /**
     * deprecated...

    public int getRecordingMode()
    {
        return recordingMode;
    }*/
    public String getReference()
    {
        return reference;
    }
    public void setDt(float dt)
    {
        this.dt = dt;
    }
    public void setDuration(float duration)
    {
        this.duration = duration;
    }
    public void setReference(String reference)
    {
        this.reference = reference;
    }
    public boolean isSpecifySimName()
    {
        return specifySimName;
    }
    public void setSpecifySimName(boolean specifySimName)
    {
        this.specifySimName = specifySimName;
    }
    public boolean isSaveCopyGenSimFiles()
    {
        return saveCopyGenSimFiles;
    }
    public void setSaveCopyGenSimFiles(boolean saveCopyGenSimFiles)
    {
        this.saveCopyGenSimFiles = saveCopyGenSimFiles;
    }
    public float getGlobalCm()
    {
        return globalCm;
    }
    public float getGlobalRa()
    {
        return globalRa;
    }
    public void setGlobalCm(float globalCm)
    {
        this.globalCm = globalCm;
    }
    public void setGlobalRa(float globalRa)
    {
        this.globalRa = globalRa;
    }
    public float getGlobalRm()
    {
        return globalRm;
    }
    public void setGlobalRm(float globalRm)
    {
        this.globalRm = globalRm;
    }
    public float getGlobalVLeak()
    {
        return globalVLeak;
    }
    public float getInitVm()
    {
        return initVm;
    }
    public void setGlobalVLeak(float globalVLeak)
    {
        this.globalVLeak = globalVLeak;
    }
    public void setInitVm(float initVm)
    {
        this.initVm = initVm;
    }
    public int getWhatToRecord()
    {
        return whatToRecord;
    }
    public void setWhatToRecord(int whatToRecord)
    {
        this.whatToRecord = whatToRecord;
    }
    public float getTemperature()
    {
        return temperature;
    }
    public void setTemperature(float temperature)
    {
        this.temperature = temperature;
    }

}
