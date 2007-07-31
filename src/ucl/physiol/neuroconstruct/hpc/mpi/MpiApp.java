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

package ucl.physiol.neuroconstruct.hpc.mpi;

import java.awt.*;
import javax.swing.*;

import ucl.physiol.neuroconstruct.gui.*;
import java.io.File;

/**
 * Support for interacting with MPI platform
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public class MpiApp
{
    boolean packFrame = false;
    boolean standalone = true;

    //Construct the application
    public MpiApp(boolean standalone)
    {

        String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();
        try
        {
            UIManager.setLookAndFeel(favouredLookAndFeel);
        }
        catch (Exception ex)
        {
            System.out.println("Error with Look and Feel: " + favouredLookAndFeel);
        }

        MpiFrame frame = new MpiFrame(new File("."), standalone);

        if (packFrame)
        {
            frame.pack();
        }
        else
        {
            frame.validate();
        }
        //Center the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);
    }

    //Main method
    public static void main(String[] args)
    {

        new MpiApp(true);
    }
}
