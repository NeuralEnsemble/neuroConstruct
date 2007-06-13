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

package ucl.physiol.neuroconstruct.nmodleditor.gui;

import java.awt.*;

import javax.swing.*;

import ucl.physiol.neuroconstruct.gui.*;

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class NmodlEditorApp
{
    boolean packFrame = false;

    NmodlEditorFrame frame = null;

    //Construct the application
    public NmodlEditorApp(String projectMainDir)
    {
        //MetalTheme theme = new CustomLookAndFeel();
        //MetalLookAndFeel.setCurrentTheme(theme);

        String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();
        try
        {
            UIManager.setLookAndFeel(favouredLookAndFeel);
        }
        catch (Exception ex)
        {
            System.out.println("Error with Look and Feel: " + favouredLookAndFeel);
        }

        frame = new NmodlEditorFrame(projectMainDir);

        //frame = new NmodlTreeGui(projectMainDir);

        //Validate frames that have preset sizes
        //Pack frames that have useful preferred size info, e.g. from their layout
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

    public void editModFile(String modFileName, boolean readonly)
    {

        frame.editModFile(modFileName, readonly);
    }


    //Main method
    public static void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        new NmodlEditorApp(null);
    }
}
