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

package ucl.physiol.neuroconstruct.cell.compartmentalisation;

import java.util.*;


/**
 * Provides information on available morphological compartmentalisations
 *
 * @author Padraig Gleeson
 *  
 *
 */

public class CompartmentalisationManager
{
    private static ArrayList<MorphCompartmentalisation> morphProjections = new ArrayList<MorphCompartmentalisation>();

    private static int origMorphIndex = 0;

    static
    {
        morphProjections.add(origMorphIndex, new OriginalCompartmentalisation());
        morphProjections.add(new GenesisCompartmentalisation());
        //morphProjections.add(new SimpleCompartmentalisation());
    }


    public static MorphCompartmentalisation getOrigMorphCompartmentalisation()
    {
        return morphProjections.get(origMorphIndex);
    }

    public static ArrayList<MorphCompartmentalisation> getAllMorphProjections()
    {
        return morphProjections;
    }

    public static void main(String[] args)
    {
        ArrayList<MorphCompartmentalisation> mp = CompartmentalisationManager.getAllMorphProjections();

        for (int i = 0; i < mp.size(); i++)
        {
            System.out.println("Morph Proj: "+ mp.get(i));
        }
    }


}
