/**
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

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import ncsa.hdf.object.*;
import ncsa.hdf.object.h5.*;
import ucl.physiol.neuroconstruct.dataset.DataSet;



/**
 * Exception related to HDF5 functionality
 *
 * @author Padraig Gleeson
 *
 */

@SuppressWarnings("serial")

public class Hdf5Exception extends Exception
{
    private Hdf5Exception()
    {
    }

    public Hdf5Exception(String message)
    {
        super(message);
    }

    public Hdf5Exception(String comment, Throwable t)
    {
        super(comment, t);
    }
    
    public static void main(String[] args) throws IOException
    {
        
        System.out.println("Sys prop: "+System.getProperty("user.dir"));
        System.out.println("Sys prop: "+System.getProperty("java.library.path"));
        
        try 
        {
            //File f = new File("../../temp/net.h5");
            //File f = new File("/angus_server/Padraig/Datas/HDF5/ep0601aa.hdf5");
            File f = new File("Y:/Padraig/Datas/HDF5/ep0601aa.hdf5");
            //File f = new File("/angus_server/Padraig/temp/voltageOutput.h5");
            System.out.println("Reading a HDF5 file: " + f.getCanonicalPath());

            H5File h5file = Hdf5Utils.openH5file(f);
            
            Hdf5Utils.open(h5file);
            
            System.out.println("h5file: "+h5file.getRootNode());
            
            Group g = Hdf5Utils.getRootGroup(h5file);
            
            
            printGroup(g, "--"); 
                
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }

    }
    

    
    private static void printGroup(Group g, String indent) throws Exception
    {
        if (g == null)
            return;

        java.util.List members = g.getMemberList();
        
        //System.out.println("---- Group: "+ g.getName());
        
        
            Properties p = new Properties();

        int n = members.size();
        indent += "    ";
        HObject obj = null;
        for (int i=0; i<n; i++)
        {
            obj = (HObject)members.get(i);
            System.out.println(indent+obj+": "+ obj.getPath());
            
            java.util.List stuff = obj.getMetadata();
            
            for (Object m: stuff)
            {
                if (m instanceof Attribute)
                {
                    Hdf5Utils.parseAttribute((Attribute)m, indent, p);

                }
            }
            
            if (obj instanceof Group)
            {
                printGroup((Group)obj, indent);
            }
            
            if (obj instanceof Dataset)
            {
                Dataset d = (Dataset)obj;
                
                if (d.getDims().length==1)
                {
                    System.out.println(indent+"Dimensions: "+d.getDims()[0]);
                    if (d.getDims()[0]>1)
                    {
                        DataSet ds = Hdf5Utils.parseDataset(d, true, p);
                        System.out.println(indent+ds.toString());
                    }
                    
                }
                if (d.getDims().length==2)
                {
                    System.out.println(indent+"Dimensions: "+d.getDims()[0]+", "+d.getDims()[1]);
                }
                if (d.getDims().length==3)
                {
                    System.out.println(indent+"Dimensions: "+d.getDims()[0]+", "+d.getDims()[1]+", "+d.getDims()[2]);
                }
            }
        }
    }

        
        


}
