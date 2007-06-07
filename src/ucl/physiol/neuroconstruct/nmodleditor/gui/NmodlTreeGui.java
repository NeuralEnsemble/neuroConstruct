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

import ucl.physiol.neuroconstruct.utils.*;
import javax.swing.*;
import ucl.physiol.neuroconstruct.nmodleditor.modfile.*;
import java.awt.*;

/**
 * nmodlEditor application software. Main Frame of application
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */


public class NmodlTreeGui extends JFrame
{

    ClassLogger logger = new ClassLogger("NmodlTreeGui");


    // whether it's run alone or via neuroConstruct...
    boolean standAlone = false;

    String titleString = new String("nmodlEditor");

    ModFile currentModFile = null;

// Only needed if launched from neuroConstruct project
    String projectMainDir = null;
    JPanel jPanelMain = new JPanel();


    public NmodlTreeGui(String projectMainDir)
    {
        try
        {
            jbInit();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        if (projectMainDir == null)
        {
            this.standAlone = true;
        }
        else
        {
            this.projectMainDir = projectMainDir;
            this.standAlone = false;
        }

    }
    private void jbInit() throws Exception
    {
        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
    }

    public void editModFile(String modFileName, boolean readonly)
    {
    }


}
