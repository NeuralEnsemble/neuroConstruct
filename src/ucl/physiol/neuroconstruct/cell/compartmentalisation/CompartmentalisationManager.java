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
