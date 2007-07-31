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

package ucl.physiol.neuroconstruct.gui;

import java.awt.Frame;

/**
 *  Dialog for checking to save before opening a new project
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

@SuppressWarnings("serial")

public class SaveBeforeNewProject extends SaveBeforeExitDialog
{
        /** @todo remove this altogether and replace with JOptionPane.showOptionDialog() */

    public SaveBeforeNewProject()
    {
    }

    public SaveBeforeNewProject(Frame frame, String title, boolean modal)
    {
        super(frame, title, modal);

        jLabelMain.setText("Project has been altered. Save before opening a new Project?");

    }
}