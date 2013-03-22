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

package ucl.physiol.neuroconstruct.neuroml.hdf5;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
            File f = new File("../python/hdf5/ep0601aa.hdf5");
            //File f = new File("/angus_server/Padraig/temp/voltageOutput.h5");
            System.out.println("Reading a HDF5 file: " + f.getCanonicalPath());

            H5File h5file = Hdf5Utils.openH5file(f);
            
            Hdf5Utils.open(h5file);
            
            System.out.println("h5file: "+h5file.getRootNode());
            
            Group g = Hdf5Utils.getRootGroup(h5file);
            
            
            Hdf5Utils.printGroup(g, "--");
                
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }

    }
    


        


}
