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

import java.beans.*;
import java.io.*;
import java.util.ArrayList;
import ucl.physiol.neuroconstruct.project.stimulation.*;
import ucl.physiol.neuroconstruct.utils.NumberGenerator;
import ucl.physiol.neuroconstruct.project.cellchoice.*;
import ucl.physiol.neuroconstruct.project.segmentchoice.IndividualSegments;
import ucl.physiol.neuroconstruct.project.segmentchoice.SegmentLocationChooser;


/**
 * Settings specifically for NetStim/randomspike like stimulation
 * Note: not the best package for this, but unfortunately the stored XML project files
 * reference this class...
 *
 * @author Padraig Gleeson
 *  
 */


public class RandomSpikeTrainSettings extends StimulationSettings
{
    private RandomSpikeTrain randomSpikeTrain = null;

    public RandomSpikeTrainSettings()
    {
        randomSpikeTrain = new RandomSpikeTrain();
    }

    public RandomSpikeTrainSettings(String reference,
                           String cellGroup,
                           CellChooser cellChooser,
                           int segmentID,
                           NumberGenerator rate,
                           String synapseType)
    {
        super(reference, cellGroup, cellChooser, segmentID);
        randomSpikeTrain = new RandomSpikeTrain(rate, synapseType);
    }

    /*
     * noise not currently supported
     */
    @Deprecated
    public RandomSpikeTrainSettings(String reference,
                           String cellGroup,
                           CellChooser cellChooser,
                           int segmentID,
                           NumberGenerator rate,
                           float noise,
                           String synapseType)
    {
        super(reference, cellGroup, cellChooser, segmentID);
        randomSpikeTrain = new RandomSpikeTrain(rate, synapseType);
    }

    public RandomSpikeTrainSettings(String reference,
                           String cellGroup,
                           CellChooser cellChooser,
                           SegmentLocationChooser segs,
                           NumberGenerator rate,
                           String synapseType)
    {
        super(reference, cellGroup, cellChooser, segs);
        randomSpikeTrain = new RandomSpikeTrain(rate, synapseType);
    }

    /*
     * noise not currently supported
     */
    @Deprecated
    public RandomSpikeTrainSettings(String reference,
                           String cellGroup,
                           CellChooser cellChooser,
                           SegmentLocationChooser segs,
                           NumberGenerator rate,
                           float noise,
                           String synapseType)
    {
        super(reference, cellGroup, cellChooser, segs);
        randomSpikeTrain = new RandomSpikeTrain(rate, noise, synapseType);
    }
    
    
    
    public Object clone()
    {
        RandomSpikeTrain rstOrig = (RandomSpikeTrain)this.getElectricalInput();
        
        RandomSpikeTrain rstClone = (RandomSpikeTrain)rstOrig.clone();
        
        RandomSpikeTrainSettings rsts = new RandomSpikeTrainSettings(this.getReference(),
                                 this.getCellGroup(),
                                 (CellChooser)this.getCellChooser().clone(),
                                 (SegmentLocationChooser)this.getSegChooser().clone(),
                                 rstClone.getRate(),
                                 rstClone.getSynapseType());
        
        return rsts;
                                 
    }



    public ElectricalInput getElectricalInput()
    {
        return randomSpikeTrain;
    };


    public NumberGenerator getRate()
    {
        return randomSpikeTrain.getRate();
    }

    //public float getNoise()
    //{
    //    return randomSpikeTrain.getNoise();
    //}






    /**
     * This is to cope with the old code, where rate was always fixed
     
    public void setRate(float fixedRate)
    {
        //System.out.println("Spiking rate being set at a fixed rate: "+fixedRate);
        NumberGenerator rate = new NumberGenerator(fixedRate);

        randomSpikeTrain.setRate(rate);
    }*/

    public void setRate(NumberGenerator rate)
    {
        //System.out.println("rate: " + rate);
        randomSpikeTrain.setRate(rate);
    }


    //public void setNoise(float noise)
    //{
    //    randomSpikeTrain.setNoise(noise);
    //}


    public String toString()
    {
        return randomSpikeTrain.toString();
    }
    public String getSynapseType()
    {
        return randomSpikeTrain.getSynapseType();
    }
    public void setSynapseType(String synapseType)
    {
        randomSpikeTrain.setSynapseType(synapseType);
    }

    public static void main(String[] args)
    {
        RandomSpikeTrainSettings rsts = new RandomSpikeTrainSettings("ref",
                            "cg",
                            new AllCells(),
                            new IndividualSegments(new ArrayList<Integer>()),
                            new NumberGenerator(11.11f),
                            "synt");

        System.out.println("rsts: "+rsts);

        try
        {
            File f = new File("../temp/rs.xml");
            FileOutputStream fos = new FileOutputStream(f);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            XMLEncoder xmlEncoder = new XMLEncoder(bos);

            xmlEncoder.writeObject(rsts);

            xmlEncoder.flush();
            xmlEncoder.close();

            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis);
            XMLDecoder xmlDecoder = new XMLDecoder(bis);


             Object obj = xmlDecoder.readObject();
             System.out.println("Obj: "+ obj);

        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
            return;
        }
    }

}
