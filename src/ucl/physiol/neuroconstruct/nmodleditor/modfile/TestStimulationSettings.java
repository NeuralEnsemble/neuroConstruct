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

package ucl.physiol.neuroconstruct.nmodleditor.modfile;

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

public class TestStimulationSettings
{

    public TestStimulationSettings(float delay, float duration, float amplitude)
    {
        this.delay = delay;
        this.duration = duration;
        this.amplitude = amplitude;
    }

    public float delay;
    public float duration;
    public float amplitude;


}