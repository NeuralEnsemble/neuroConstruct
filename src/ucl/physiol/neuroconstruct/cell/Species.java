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

package ucl.physiol.neuroconstruct.cell;
 

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;

 /**
  * A class representing the properties associated with a species
  *
  * @author Padraig Gleeson
  *  
  *
  */

@SuppressWarnings("serial")

public class Species implements Serializable
{
    static final long serialVersionUID = -27530166332L;
    
    private static transient ClassLogger logger = new ClassLogger("SpeciesProperties");
    
    private String name = null;
    
    private Properties props = new Properties();

    public Species()
    {
    }

    public Species(String name)
    {
        this.name = name;
    }


    @Override
    public String toString()
    {
        return name + " "+props;

    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    

    

    public void setProperty(String propName, String propValue)
    {
        props.setProperty(propName, propValue);
    }


    public static void main(String[] args)
    {
        Cell cell = new OneSegment("OneSegCell");

        System.out.println("Created cell: "+ cell);

        cell.associateGroupWithChanMech(Section.ALL, new ChannelMechanism("pas", 1e-7f));
        cell.associateGroupWithChanMech(Section.ALL, new ChannelMechanism("NaCond", 1e-5f));
        cell.associateGroupWithChanMech(Section.ALL, new ChannelMechanism("KCond", 1e-4f));

        Species sp1 = new Species("ca");
        sp1.setProperty("e", "66.66");
        Vector<String> grps1 =  new Vector<String>();
        grps1.add(Section.SOMA_GROUP);
        cell.getSpeciesVsGroups().put(sp1, grps1);


        Species sp2 = new Species("na");
        sp2.setProperty("e", "55.5");
        Vector<String> grps2 =  new Vector<String>();
        grps2.add(Section.ALL);
        cell.getSpeciesVsGroups().put(sp2, grps2);

        Species sp3 = new Species("k");
        sp3.setProperty("e", "-77.7");
        Vector<String> grps3 =  new Vector<String>();
        grps3.add(Section.ALL);
        cell.getSpeciesVsGroups().put(sp3, grps3);

        System.out.println(CellTopologyHelper.printDetails(cell, null));


    }

}
