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
 * Helper class for specifying a single post synaptic
 * mechanism instance corresponding e.g. to an object generated from a mod file and placed
 * on a section in Neuron.
 *
 * @author Padraig Gleeson
 * @version 1.0.3
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
