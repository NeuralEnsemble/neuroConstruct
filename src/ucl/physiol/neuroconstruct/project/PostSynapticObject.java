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

package ucl.physiol.neuroconstruct.project;


/**
 * Helper class for specifying a single post synaptic
 * mechanism instance corresponding e.g. to an object generated from a mod file and placed
 * on a section in Neuron.
 *
 * @author Padraig Gleeson
 *  
 */

public class PostSynapticObject
{
    protected String netConnName = null;
    protected String synapseType = null;
    protected int cellNumber = -1;
    protected int segmentId = -1;
    protected int synapseIndex = -1;

    private PostSynapticObject()
    {};

    public PostSynapticObject(String netConnName,
                              String synapseType,
                              int cellNumber,
                              int segmentId,
                              int synapseIndex)
    {
        this.netConnName = netConnName;
        this.synapseType = synapseType;
        this.cellNumber = cellNumber;
        this.segmentId = segmentId;
        this.synapseIndex = synapseIndex;
    }

    public String getNetConnName()
    {
        return this.netConnName;
    }

    public String getSynapseType()
    {
        return this.synapseType;
    }

    public int getCellNumber()
    {
        return this.cellNumber;
    }

    public int getSegmentId()
    {
        return this.segmentId;
    }

    public int getSynapseIndex()
    {
        return this.synapseIndex;
    }



    @Override
    public String toString()
    {
        return "PostSynapticObject [netConnName: "+netConnName
            + ", synapseType: " + synapseType
            + ", cellNumber: " + cellNumber
            + ", segmentId: " + segmentId
            + ", synapseIndex: " + synapseIndex+"]";
    }





    /**
     * Used in filename for file storing syn activity
     */
    public String getSynRef()
    {
        return netConnName + "." + synapseType + "." + synapseIndex;
    }

    private static PostSynapticObject parse(String synRef)
    {
        PostSynapticObject pso = new PostSynapticObject();

        int firstPeriod = synRef.indexOf(".");
        int secondPeriod = synRef.indexOf(".", firstPeriod + 1);

        pso.netConnName = synRef.substring(0, firstPeriod);
        pso.synapseType = synRef.substring(firstPeriod + 1, secondPeriod);
        pso.synapseIndex = Integer.parseInt(synRef.substring(secondPeriod + 1));

        return pso;

    }

    public static String getSynapseType(String synRef)
    {
        return parse(synRef).synapseType;
    }

    public static int getSynapseIndex(String synRef)
    {
        return parse(synRef).synapseIndex;
    }

    public static String getNetConnName(String synRef)
    {
        return parse(synRef).netConnName;
    }

}
