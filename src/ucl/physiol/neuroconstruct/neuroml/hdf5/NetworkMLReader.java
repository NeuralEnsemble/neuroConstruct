 /*
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.neuroml.hdf5;


import ncsa.hdf.object.*;
import ncsa.hdf.object.h5.*;
import java.io.File;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.util.ArrayList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import ucl.physiol.neuroconstruct.neuroml.NetworkMLConstants;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Utilities file for reading NetworkML HDF5 files
 *
 * @author Padraig Gleeson
 *
 */
public class NetworkMLReader 
{    
    private static ClassLogger logger = new ClassLogger("NetworkMLReader");
        
    
    private GeneratedCellPositions cellPos = null;

    private GeneratedNetworkConnections netConns = null;
    
    
    boolean inPopulations = false;
    
    String currentCellGroup = null;

    public NetworkMLReader(GeneratedCellPositions cellPos, GeneratedNetworkConnections netConns)
    {
        this.cellPos = cellPos;
        this.netConns = netConns;

    }
    
    
    public void parse(File hdf5File) throws Hdf5Exception
    {
        H5File h5File = Hdf5Utils.openForRead(hdf5File);
        
        Group root = Hdf5Utils.getRootGroup(h5File);
        
        parseGroup(root);
    }
        
    
        
    public void startGroup(Group g) throws Hdf5Exception
    {
        logger.logComment("-----   Going into a group: "+g.getFullName());
        
        ArrayList<Attribute> attrs = Hdf5Utils.parseGroupForAttributes(g);
            
        for (Attribute attribute : attrs) 
        {
            //attribute.
            logger.logComment("Group: "+g.getName()+ " has attribute: "+ attribute.getName()+" = "+ Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName()));
            

        }
        
        if (g.getName().equals(NetworkMLConstants.ROOT_ELEMENT))
        {
            logger.logComment("Found the main group");

        }
        else if (g.getName().equals(NetworkMLConstants.POPULATIONS_ELEMENT))
        {
            logger.logComment("Found the pops group");
            inPopulations = true;

        }
        else if (g.getName().startsWith(NetworkMLConstants.POPULATION_ELEMENT) && inPopulations)
        {
            String name = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.POP_NAME_ATTR);
            
            logger.logComment("Found a population: "+ name);
            currentCellGroup = name;
        }
        
    }
    
    
    
    
    public void endGroup(Group g) throws Hdf5Exception
    {
        logger.logComment("-----   Going out of a group: "+g.getFullName());
        
        if (g.getName().equals(NetworkMLConstants.POPULATIONS_ELEMENT))
        {
            inPopulations = false;
        }
        else if (g.getName().startsWith(NetworkMLConstants.POPULATION_ELEMENT) && inPopulations)
        {
            currentCellGroup = null;
        }
        
    }
    
    
    public void dataSet(Dataset d) throws Hdf5Exception
    {
        logger.logComment("-----   Looking through dataset: "+d);
        
        float[][] data = Hdf5Utils.parse2Ddataset(d);
        
        logger.logComment("Data has size: ("+data.length+", "+data[0].length+")");
        
        if (currentCellGroup!=null)
        {
            for(int i = 0;i<data.length;i++)
            {
                int id = (int)data[i][0];
                float x = data[i][1];
                float y = data[i][2];
                float z = data[i][3];
                
                
                PositionRecord posRec = new PositionRecord(id,x,y,z);
                
                this.cellPos.addPosition(currentCellGroup, posRec);
            }
        }
        
        
    }
        
        
    public void parseGroup(Group g) throws Hdf5Exception
    {
        startGroup(g);
                
        java.util.List members = g.getMemberList();

       
        for (int j=0; j<members.size(); j++)
        {
            HObject obj = (HObject)members.get(j);
            
            if (obj instanceof Group)
            {
                Group subGroup = (Group)obj;
                
                logger.logComment("Found a sub group: "+subGroup.getName());
                
                parseGroup(subGroup);
            }
            else if (obj instanceof Dataset)
            {
                Dataset ds = (Dataset)obj;
                
                logger.logComment("Found a dataset: "+ds.getName());
                
                dataSet(ds);
            }
        }
        
        endGroup(g);
    }    
            
            
    
    
    
    public static void main(String args[])
    {

        try
        {
            
            logger.logComment("Sys prop: "+System.getProperty("java.library.path"), true);
            
            //Project testProj = Project.loadProject(new File("projects/Parall/Parall.neuro.xml"),null);
            Project testProj = Project.loadProject(new File("examples/Ex5-Networks/Ex5-Networks.neuro.xml"),null);

            File f = new File("examples/Ex5-Networks/savedNetworks/hh.h5");

            logger.logComment("Loading netml cell from "+ f.getAbsolutePath(), true);

            GeneratedCellPositions gcp = new GeneratedCellPositions(testProj);
            GeneratedNetworkConnections gnc = new GeneratedNetworkConnections(testProj);

          
            NetworkMLReader nmlReader = new NetworkMLReader(gcp, gnc);
            
            nmlReader.parse(f);

            logger.logComment("Contents: "+gcp.toString());
            logger.logComment("Net conns: "+gnc.toNiceString());



        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }

}
