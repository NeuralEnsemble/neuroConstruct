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
import ncsa.hdf.object.*;
import ncsa.hdf.object.h5.*;



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
        try 
        {
            File f = new File("../../temp/net.h5");
            System.out.println("Reading a HDF5 file: " + f.getCanonicalPath());

            H5File h5file = Hdf5Utils.openH5file(f);
            
            Hdf5Utils.open(h5file);
            
            System.out.println("h5file: "+h5file.getRootNode());
            
            Group g = Hdf5Utils.getRootGroup(h5file);
            
            
               //printGroup(g, "--"); 
                
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }

    }
    
    /*
        private static void printGroup(Group g, String indent) throws Exception
    {
        if (g == null)
            return;

        java.util.List members = g.getMemberList();

        int n = members.size();
        indent += "    ";
        HObject obj = null;
        for (int i=0; i<n; i++)
        {
            obj = (HObject)members.get(i);
            System.out.println(indent+obj);
            if (obj instanceof Group)
            {
                printGroup((Group)obj, indent);
            }
        }
    }*/

        
        


}
