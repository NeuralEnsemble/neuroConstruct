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


/**
 * Interface to let the main frame know when the generation of cell positions,
 * network connection, etc. is finished
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public interface GenerationReport
{
    public void giveGenerationReport(String report, String generatorType, SimConfig SimConfig);

    public void majorStepComplete();

    public void giveUpdate(String update);
}
