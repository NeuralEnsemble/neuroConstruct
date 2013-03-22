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

package ucl.physiol.neuroconstruct.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Dialog for creating and editing the older, non ChannelML cell mechs..
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class CellMechanismEditor extends JDialog implements FocusListener
{
	
	ClassLogger logger = new ClassLogger("CellMechanismEditor");
    public boolean cancelled = false;

    Hashtable<String, JTextField> textFieldsForParameters = new Hashtable<String, JTextField>();

    String firstLineCombo = "-- Please select a Cell Mechanism --";

    AbstractedCellMechanism currentCellMechanism = null;
    //CellMechanismPlotInfo currentCellMechPlots = null;

    String defaultNameNewProc = "cellmechanism_1";

    Project project = null;

    Frame parent = null;

    final String choosePlot  = "-- Choose Plot --";


    RecentFiles recentFiles = RecentFiles.getRecentFilesInstance(ProjectStructure.getNeuConRecentFilesFilename());

    JPanel jPanelMain = new JPanel();
    JButton jButtonOK = new JButton();
    JPanel jPanelButtons = new JPanel();
    JButton jButtonCancel = new JButton();
    JPanel jPanelLabel = new JPanel();
    JLabel jLabelMain = new JLabel();
    JTextField jTextFieldName = new JTextField();
    JPanel jPanelSelectType = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelName = new JPanel();
    JLabel jLabelType = new JLabel();
    JComboBox jComboBoxModel = new JComboBox();
    JPanel jPanelParameters = new JPanel();
    JPanel jPanelDescription = new JPanel();
    JTextArea jTextAreaDescription = new JTextArea();
    Border border1;
    BorderLayout borderLayout2 = new BorderLayout();
    JTextField jTextFieldSimEnvs = new JTextField();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel jLabelSimEnvs = new JLabel();
    JTabbedPane jTabbedPaneCellMechInfo = new JTabbedPane();
    Border border2;
    JButton jButtonPlotsFile = new JButton();


    JPanel jPanelMoreInfo = new JPanel();
    JLabel jLabelMoreInfo = new JLabel();

    //JEditorPane jEditPlotPane = null;
    //JLabel jLabelPlotFileInfo = new JLabel("File: ");
    //JButton jButtonPlotWhat = new JButton();
    //JComboBox jComboBoxPlots = new JComboBox();

    //javax.swing.JPanel jPanelPlotButtons = new JPanel();

    JPanel jPanelOKCancel = new JPanel();
    BorderLayout borderLayout3 = new BorderLayout();

    public CellMechanismEditor(Project project, Frame parent) throws HeadlessException
    {
        super(parent, "Cell Mechanism Editor", false);

        logger.logComment("");
        logger.logComment("       ------    New CellMechanismEditor created   -----");

        this.parent = parent;
        this.project = project;

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        try
        {
            jbInit();
            extraInit();

            pack();

            logger.logComment(" ----  Done creating...  ----");
        }
        catch(Exception ex)
        {
            logger.logError("Exception starting GUI:", ex);
        }
    }





    private void jbInit() throws Exception
    {
        border1 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,Color.white,Color.white,new Color(124, 124, 124),new Color(178, 178, 178)),BorderFactory.createEmptyBorder(3,3,3,3));
        border2 = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),BorderFactory.createEmptyBorder(2,2,2,2));
        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOK_actionPerformed(e);
            }
        });
        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCancel_actionPerformed(e);
            }
        });

        jLabelMain.setText("Please enter the settings for Cell Mechanism:");
        jTextFieldName.setText(defaultNameNewProc);
        jTextFieldName.setColumns(12);
        jPanelLabel.setLayout(borderLayout1);
        jLabelType.setRequestFocusEnabled(true);
        jLabelType.setText("Type of Cell Mechanism:");


        jPanelMain.setBorder(border2);
        jPanelMain.setLayout(borderLayout2);
        jTextAreaDescription.setBorder(border1);
        jTextAreaDescription.setMaximumSize(new Dimension(500, 120));
        jTextAreaDescription.setMinimumSize(new Dimension(500, 120));
        jTextAreaDescription.setPreferredSize(new Dimension(500, 120));
        jTextAreaDescription.setToolTipText("");
        jTextAreaDescription.setVerifyInputWhenFocusTarget(true);
        jTextAreaDescription.setEditable(false);
        jTextAreaDescription.setColumns(30);
        jTextAreaDescription.setLineWrap(true);
        jTextAreaDescription.setRows(3);
        jTextAreaDescription.setWrapStyleWord(true);
        jComboBoxModel.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxModel_itemStateChanged(e);
            }
        });
        jTextFieldSimEnvs.setEditable(false);
        jTextFieldSimEnvs.setText("");
        jTextFieldSimEnvs.setColumns(25);
        jPanelDescription.setLayout(gridBagLayout1);
        jLabelSimEnvs.setText("Available for simulators:");
        jTabbedPaneCellMechInfo.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                jTabbedPaneCellMechanismInfo_stateChanged(e);
            }
        }); jButtonPlotsFile.setText("Specify Plots File"); jButtonPlotsFile.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonPlotsFile_actionPerformed(e);
            }
        });
        /*
        jButtonPlotWhat.setText("Plot:");

        jButtonPlotWhat.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonPlotWhat_actionPerformed(e);
            }
        }); */

        jPanelButtons.setLayout(borderLayout3);

        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
        jPanelMain.add(jTabbedPaneCellMechInfo,  BorderLayout.CENTER);
        //jTabbedPaneCellMechInfo.add(jPanelParameters,  "Parameters for the Cell Mechanism");
        this.getContentPane().add(jPanelButtons,  BorderLayout.SOUTH); jPanelOKCancel.
            add(jButtonOK); jPanelOKCancel.
            add(jButtonCancel);

        //jPanelPlotButtons.add(jButtonPlotsFile);


        //jPanelPlotButtons.add(jButtonPlotWhat);
        //jPanelPlotButtons.add(jComboBoxPlots);

        this.getContentPane().add(jPanelLabel, BorderLayout.NORTH);
        jPanelLabel.add(jPanelSelectType, BorderLayout.CENTER);
        jPanelSelectType.add(jLabelType, null);
        jPanelLabel.add(jPanelName, BorderLayout.NORTH);
        jPanelName.add(jLabelMain, null);
        jPanelName.add(jTextFieldName, null);
        jPanelSelectType.add(jComboBoxModel, null);
        jPanelLabel.add(jPanelDescription, BorderLayout.SOUTH);
        jPanelDescription.add(jTextAreaDescription, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
                                                                           , GridBagConstraints.CENTER,
                                                                           GridBagConstraints.NONE, new Insets(0, 0, 0, 0),
                                                                           0, 0));
        jPanelDescription.add(jTextFieldSimEnvs, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                                                                        , GridBagConstraints.CENTER,
                                                                        GridBagConstraints.HORIZONTAL,
                                                                        new Insets(6, 0, 6, 12), 0, 0));
        jPanelDescription.add(jLabelSimEnvs, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                                                    , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                                    new Insets(6, 12, 6, 12), 0, 0));

        jPanelButtons.add(jPanelOKCancel, java.awt.BorderLayout.SOUTH);

        this.jLabelMoreInfo = new JLabel("There is a folder for this mech under cellMechanisms in the project home dir, where the template file, etc. can be changed");
        jPanelMoreInfo.add(jLabelMoreInfo);

        jPanelButtons.add(jPanelMoreInfo, java.awt.BorderLayout.CENTER);
        jPanelParameters.setLayout(new GridBagLayout());



    }


    public void extraInit()
    {
        jComboBoxModel.addItem(firstLineCombo);
        Vector cellMechss = CellMechanismHelper.getCellMechanisms();
        logger.logComment("Adding "+cellMechss.size()+" Cell mechs");

        for (int j = 0; j < cellMechss.size(); j++)
        {
            AbstractedCellMechanism nextCellProc = (AbstractedCellMechanism)cellMechss.elementAt(j);
            String desc = nextCellProc.getMechanismModelAndSims();

            jComboBoxModel.addItem(desc);
        }


        this.jComboBoxModel.setSelectedIndex(1);

        ///jComboBoxPlots.removeAllItems();

        ///jComboBoxPlots.addItem(choosePlot);
    }


    /**
     * For calling externally, specifying the CellMechanisms to show, and disabling the ability to
     * change the cell model
     */
    public void setCellMechanism(AbstractedCellMechanism cellMech)
    {
        logger.logComment("1) Setting cell mechanism to: ");
        cellMech.printDetails();
        currentCellMechanism = cellMech;

        jTextFieldName.setEditable(false);
        jComboBoxModel.setEnabled(false);

        jTextFieldName.setText(cellMech.getInstanceName());

        String fullName = cellMech.getMechanismModelAndSims();

        logger.logComment("Setting selected item to: "+ fullName);

        if (cellMech instanceof FileBasedMembraneMechanism)
        {
            jComboBoxModel.addItem(fullName);
        }
        jComboBoxModel.setSelectedItem(fullName);


        setDisplayedCellMechanism(cellMech);
    }

    //Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            cancelled = true;
            this.dispose();
        }
        super.processWindowEvent(e);
    }

    @Override
    public void dispose()
    {
        if (this.parent==null) 
            System.exit(0);
        project.markProjectAsEdited();
        super.dispose();
    }



    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel button pressed");
        cancelled = true;
        this.dispose();
    }

    void jButtonOK_actionPerformed(ActionEvent e)
    {
        logger.logComment("OK button pressed");
        updateCellMechanism();

        String proposedName = jTextFieldName.getText();

        if (proposedName.indexOf(" ")>0 || !proposedName.trim().equals(proposedName))
        {
            GuiUtils.showErrorMessage(logger, "Please choose a Cell Mechanism name without spaces", null, this);
            return;
        }

        try
        {
            Float.parseFloat(proposedName);
            GuiUtils.showErrorMessage(logger, "Please don't use a number for the Cell Mechanism name", null, this);
            return;
        }
        catch(NumberFormatException ex){};

        if (jComboBoxModel.getSelectedItem().equals(firstLineCombo))
            cancelled = true;


        logger.logComment("Updating Cell Mechanism: "+ currentCellMechanism.toString());


        project.cellMechanismInfo.updateCellMechanism(currentCellMechanism);

        this.dispose();
    }



    public static void main(String[] args)
    {
        String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();
        try
        {
            UIManager.setLookAndFeel(favouredLookAndFeel);

            @SuppressWarnings("unused")
            
			Project testProj = Project.loadProject(new File("projects/exy-mel/exy-mel.neuro.xml"),
                                                   new ProjectEventListener()
            {
                public void tableDataModelUpdated(String tableModelName)
                {};

                public void tabUpdated(String tabName)
                {};
                public void cellMechanismUpdated()
                {
                };

            });
            
            /*
            CellMechanismEditor cpEditor = new CellMechanismEditor(testProj, new Frame());
            
            Exp2SynMechanism exp2 = new Exp2SynMechanism();


            exp2.setPlotInfoFile("../temp/Generic2.xml");

            try
            {

                exp2.setParameter(Exp2SynMechanism.MAX_COND, 10);
                exp2.setParameter(Exp2SynMechanism.TAU_RISE, 0.01f);
            }
            catch (Exception ex1)
            {
                ex1.printStackTrace();
            }

            cpEditor.setVisible(true);

            System.out.println("Shown the dialog");

            AbstractedCellMechanism cp = cpEditor.getFinalCellMechanism();

            System.out.println("Final cell mechanism: ");
            cp.printDetails();*/
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    private void addCellMechanismParameter(InternalPhysicalParameter param, int rowNumber, int totalNumRows)
    {

        int maxTextAreaHeight = 2;
        if (totalNumRows>8) maxTextAreaHeight = 1;


        JTextArea jTextAreaName = new JTextArea(maxTextAreaHeight,16);

        Font fontToUse = jTextAreaName.getFont();
        if (totalNumRows>10)
        {
            fontToUse
                = new Font(fontToUse.getName(),
                           fontToUse.getStyle(),
                           (int) ( (float) fontToUse.getSize()*0.9f));

        }

        jTextAreaName.setFont(fontToUse);
        jTextAreaName.setLineWrap(true);
        jTextAreaName.setWrapStyleWord(true);
        jTextAreaName.setEditable(false);

        jTextAreaName.setBackground((new JPanel()).getBackground());
        jTextAreaName.setText(param.parameterName+": ");

        Insets elementInsets = new Insets(6,6,6,6);
        if (totalNumRows>10) elementInsets = new Insets(2,6,2,6);

        jPanelParameters.add(jTextAreaName,  new GridBagConstraints(0, rowNumber, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, elementInsets, 0, 0));


        JTextArea jTextAreaShortDescription = new JTextArea(maxTextAreaHeight,38);
        jTextAreaShortDescription.setLineWrap(true);
        jTextAreaShortDescription.setWrapStyleWord(true);
        jTextAreaShortDescription.setText(param.parameterDescription);
        jTextAreaShortDescription.setEnabled(false);
        jTextAreaShortDescription.setFont(fontToUse);

        jPanelParameters.add(jTextAreaShortDescription,  new GridBagConstraints(1, rowNumber, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, elementInsets, 0, 0));

        jTextAreaShortDescription.setBorder(BorderFactory.createEtchedBorder());




        JTextField jTextFieldValue = new JTextField();

        jTextFieldValue.setText(""+param.getValue());

        textFieldsForParameters.put(param.parameterName, jTextFieldValue);

        jTextFieldValue.setColumns(6);

        jPanelParameters.add(jTextFieldValue,  new GridBagConstraints(2, rowNumber, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, elementInsets, 0, 0));

        jTextFieldValue.setFont(fontToUse);

        JTextField jTextFieldUnits = new JTextField();

        jTextFieldUnits.setText(""+param.getUnits().getSymbol());

        jTextFieldUnits.setColumns(6);

        jTextFieldUnits.setEditable(false);


        jTextFieldUnits.setFont(fontToUse);

        jPanelParameters.add(jTextFieldUnits,  new GridBagConstraints(3, rowNumber, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, elementInsets, 0, 0));


        jTextFieldValue.addFocusListener(this);


    }


    void jComboBoxModel_itemStateChanged(ItemEvent e)
    {
        //jTextFieldName.setEditable(true);


        logger.logComment("********       Combo ItemEvent: "+ e);

        if (e.getStateChange() != ItemEvent.SELECTED) return;


        String oldDefaultInstName = null;
        if (currentCellMechanism!=null) oldDefaultInstName = currentCellMechanism.getDefaultInstanceName();


        String selected = (String) jComboBoxModel.getSelectedItem();


        ///jComboBoxPlots.removeAllItems();

        if (selected.indexOf("[")>0)
            selected = selected.substring(0, selected.indexOf("[")-1).trim();

        logger.logComment(">>>>>>>>>>>>>>       The combo box has changed to: "+ selected);

        if (selected.equals(firstLineCombo))
        return;


        // this means the use changed it, so start with the default param vals in the instance
        // from CellMechHelper
        if(jComboBoxModel.isEnabled())
        {
            currentCellMechanism = CellMechanismHelper.getCellMechInstance(selected);

            if (currentCellMechanism instanceof PassiveMembraneMechanism)
            {
                PassiveMembraneMechanism pass = (PassiveMembraneMechanism)currentCellMechanism;
                try
                {
                    pass.setParameter(PassiveMembraneMechanism.REV_POTENTIAL, project.simulationParameters.getGlobalVLeak());
                    pass.setParameter(PassiveMembraneMechanism.COND_DENSITY, (1/project.simulationParameters.getGlobalRm()));
                }
                catch(CellMechanismException ex)
                {
                    logger.logError("Error setting parameter in: "+ pass.toString(), ex);
                }
            }
        }
        else
        {
            logger.logComment("Using set cell mechanism...");
        }
        logger.logComment("--- cellMech: " + currentCellMechanism);

        // Some stuff to try to get a decent name for the mechanism
        if (oldDefaultInstName!=null && jTextFieldName.getText().equals(oldDefaultInstName))
            jTextFieldName.setText(currentCellMechanism.getDefaultInstanceName());

        if (jTextFieldName.getText().equals(defaultNameNewProc))
            jTextFieldName.setText(currentCellMechanism.getDefaultInstanceName());

        jTextAreaDescription.setText(currentCellMechanism.getDescription());

        MechanismImplementation[] mechImpls = currentCellMechanism.getMechanismImpls();
        StringBuffer sb = new StringBuffer();

        logger.logComment("There are : " + mechImpls.length + " mechanism impls");

        jTabbedPaneCellMechInfo.removeAll();
        jTabbedPaneCellMechInfo.add(jPanelParameters, "Parameters for the Cell Mechanism");

        jPanelParameters.setLayout(new GridBagLayout());

        Font myFont = new Font("Monospaced", Font.PLAIN, 10);

        for (int i = 0; i < mechImpls.length; i++)
        {
            sb.append(mechImpls[i].getSimulationEnvironment());
            if (i != mechImpls.length - 1) sb.append("; ");

            JPanel jPanelNew = new JPanel();

            jPanelNew.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.
                LOWERED, Color.white, Color.white, new Color(124, 124, 124), new Color(178, 178, 178)),
                                                                   BorderFactory.createEmptyBorder(5, 5, 5, 5)));

            jPanelNew.setLayout(new BorderLayout());

            Dimension prefDim = new Dimension(700, 500);

            JScrollPane jScroller = new JScrollPane();
            JEditorPane jEditPane = new JEditorPane();
            jScroller.getViewport().add(jEditPane);

            jEditPane.setContentType("text/plain");

            jEditPane.setFont(myFont);

            jScroller.setPreferredSize(prefDim);
            jScroller.setMinimumSize(prefDim);
            jScroller.setMaximumSize(prefDim);

            jEditPane.setPreferredSize(prefDim);
            jEditPane.setMinimumSize(prefDim);
            jEditPane.setMaximumSize(prefDim);

            jPanelNew.setPreferredSize(prefDim);
            jPanelNew.setMinimumSize(prefDim);
            jPanelNew.setMaximumSize(prefDim);

            jEditPane.setEditable(false);

            jPanelNew.add(jScroller, "Center");

            JLabel jLabelFileInfo = new JLabel("File: "+ "...");

            jLabelFileInfo.setHorizontalAlignment(SwingConstants.CENTER);
            jLabelFileInfo.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            jPanelNew.add(jLabelFileInfo, "North");

            try
            {
                File implFile = mechImpls[i].getImplementingFileObject(project, currentCellMechanism.getInstanceName());
                if (implFile!=null)
                {
                    jEditPane.setPage(implFile.toURL());
                    jLabelFileInfo.setText("File: "+ implFile.getAbsolutePath());
                }
                else
                {
                jEditPane.setText("Could not find file: " + mechImpls[i].getImplementingFile());
                }
            }
            catch (IOException ex)
            {
                GuiUtils.showErrorMessage(logger,
                                          "Could not find file: " + mechImpls[i].getImplementingFile() + " for template",
                                          ex, null);
                jEditPane.setText("Could not find file: " + mechImpls[i].getImplementingFile());
            }

            jTabbedPaneCellMechInfo.add(jPanelNew, mechImpls[i].getSimulationEnvironment() + " code template");

            logger.logComment("jTabbedPaneCellMechInfo has: " + jTabbedPaneCellMechInfo.getTabCount() +
                              " tabs...");
        }
        jTextFieldSimEnvs.setText(sb.toString());
        //paramList = cellMech.getParameterList();

/*
        jTabbedPaneCellMechInfo.add(jPanelPlotInfo, "Plots for Cell Mechanism");
        logger.logComment("-----Adding pane for plots, file: "+ currentCellMechanism.getPlotInfoFile());

        jPanelPlotInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.
            LOWERED, Color.white, Color.white, new Color(124, 124, 124), new Color(178, 178, 178)),
                                                               BorderFactory.createEmptyBorder(5, 5, 5, 5)));



        jPanelPlotInfo.removeAll();
        jPanelPlotInfo.setLayout(new BorderLayout());
        Dimension prefDim = new Dimension(600, 400);

        JScrollPane jScroller = new JScrollPane();
        jEditPlotPane = new JEditorPane();

        jLabelPlotFileInfo.setText("File: " + currentCellMechanism.getPlotInfoFile());
        jScroller.getViewport().add(jEditPlotPane);

        jEditPlotPane.setContentType("text/plain");

        jEditPlotPane.setFont(myFont);

        jScroller.setPreferredSize(prefDim);
        jScroller.setMinimumSize(prefDim);
        jScroller.setMaximumSize(prefDim);*/
/*
        jEditPlotPane.setPreferredSize(prefDim);
        jEditPlotPane.setMinimumSize(prefDim);
        jEditPlotPane.setMaximumSize(prefDim);

        jPanelPlotInfo.setPreferredSize(prefDim);
        jPanelPlotInfo.setMinimumSize(prefDim);
        jPanelPlotInfo.setMaximumSize(prefDim);

        jEditPlotPane.setEditable(false);

        jPanelPlotInfo.add(jScroller, "Center");

        jLabelPlotFileInfo.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelPlotFileInfo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanelPlotInfo.add(jLabelPlotFileInfo, "North");

        try
        {
            if (currentCellMechanism.getPlotInfoFile() == null)
            {
                jEditPlotPane.setText("No plot information file specified");
                jLabelPlotFileInfo.setText("No plot information file specified");
            }

            else
            {
                File plotFile = new File(currentCellMechanism.getPlotInfoFile());
                if (!plotFile.exists())
                {
                    // should be in the importedCellMechs...
                    File cPDir = ProjectStructure.getFileBasedCellProcessesDir(project.getProjectMainDirectory(), false);

                    plotFile = new File(cPDir, currentCellMechanism.getPlotInfoFile());
                }

                jEditPlotPane.setPage(plotFile.toURL());
            }
        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Could not find file: " + currentCellMechanism.getPlotInfoFile() + " for plot information",
                                      ex, null);

            jEditPlotPane.setText("Could not find file: " + currentCellMechanism.getPlotInfoFile());
        }*/

        if (currentCellMechanism != null) setDisplayedCellMechanism(currentCellMechanism);

    }

    private void setDisplayedCellMechanism(AbstractedCellMechanism cellMech)
    {
        logger.logComment(">>>>>>>>>>>>>>     Setting displayed mechanism to: "+ cellMech);
        logger.logComment(">>>>>>>>>>>>>>     Model: "+ cellMech.getMechanismModel());

        //jPanelParameters = new JPanel();


        jPanelParameters.setLayout(new GridBagLayout());

        jPanelParameters.removeAll();

        if (cellMech==null)
        {
            jTextAreaDescription.setText("");
            jTextFieldSimEnvs.setText("");
        }
        else
        {

            jTextAreaDescription.setText(cellMech.getDescription());

            textFieldsForParameters = new Hashtable<String, JTextField>(); // to remove previous items

            logger.logComment("Adding " + cellMech.getPhysicalParameterList().length
                              + " params for this cell mechanism");

            for (int i = 0; i < cellMech.getPhysicalParameterList().length; i++)
            {
                addCellMechanismParameter(cellMech.getPhysicalParameterList()[i], i, cellMech.getPhysicalParameterList().length);
            }

            if (cellMech.getPhysicalParameterList().length ==0)
            {
                jPanelParameters.add(new JLabel("No parameters present in this Cell Mechanism"));
            }

            /*try
            {

                if (currentCellMechanism.getPlotInfoFile() == null)
                {
                    jEditPlotPane.setText("No plot information file specified");
                    jLabelPlotFileInfo.setText("No plot information file specified");
                }

                else
                {

                    logger.logComment("Checking the plot file....");


                    File plotFile = new File(currentCellMechanism.getPlotInfoFile());


                    if (!plotFile.exists())
                    {
                        // should be in the importedCellMechs...
                        File importedCPDir = ProjectStructure.getFileBasedCellProcessesDir(project.getProjectMainDirectory(), false);

                        plotFile = new File(importedCPDir, currentCellMechanism.getPlotInfoFile());
                    }

                    jLabelPlotFileInfo.setText("File: "+ plotFile.getAbsolutePath());
                    //jEditPlotPane.setText("Loading...");

                    logger.logComment("  ---   Setting displayed page to: "+ plotFile.toURL());

                    jEditPlotPane.setPage(plotFile.toURL());

                    logger.logComment("Shown text: "+ jEditPlotPane.getText());

                    jComboBoxPlots.removeAllItems();

                    try
                    {
                        currentCellMechPlots = new CellMechanismPlotInfo(project, plotFile);

                        logger.logComment("Got "+ currentCellMechPlots.getPlots().length+ " plots...");

                        CellMechanismPlot[] plots = currentCellMechPlots.getPlots();


                        for (int i = 0; i < plots.length; i++)
                        {
                            jComboBoxPlots.addItem(plots[i]);
                        }

                        logger.logComment("There are "+ jComboBoxPlots.getItemCount()+ " plot options...");


                    }
                    catch(CellMechanismException ex)
                    {
                        GuiUtils.showErrorMessage(logger, "Exception with Cell Mechanism Plot info", ex, this);
                        jLabelPlotFileInfo.setText("File: "+ plotFile.getAbsolutePath() + "(NOT VALID)");
                    }
                    //jEditPlotPane.repaint();
                }
            }
            catch (IOException ex)
            {
                GuiUtils.showErrorMessage(logger,
                                          "Could not find file: " + currentCellMechanism.getPlotInfoFile() +
                                          " for plot information",
                                          ex, null);

                jEditPlotPane.setText("Could not find file: " + currentCellMechanism.getPlotInfoFile());
            }*/




        }
        this.repaint();
        this.validate();
        this.pack();
        this.validate();

    }

    public void pack()
    {
        super.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();

        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;

        setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);



    }


    private void updateCellMechanism()
    {
        Enumeration paramNames = textFieldsForParameters.keys();

        while (paramNames.hasMoreElements())
        {
            String nextName = (String) paramNames.nextElement();

            JTextField textField = (JTextField) textFieldsForParameters.get(nextName);

            String valueTyped = textField.getText();
            if (!valueTyped.equals(""))
            {
                float value;
                try
                {
                    value = Float.parseFloat(valueTyped);
                    logger.logComment("Setting parameter: " + nextName + " to: " + value);
                    currentCellMechanism.setParameter(nextName, value);
                }
                catch (NumberFormatException ex2)
                {
                    GuiUtils.showErrorMessage(logger, "Please enter a correct value for parameter: " + nextName, null, this);

                }

                catch (CellMechanismException ex2)
                {
                    GuiUtils.showErrorMessage(logger, "Please enter a correct value for parameter: " + nextName, null, this);

                }
            }
        }

        if (jTextFieldName.isEnabled())
            currentCellMechanism.setInstanceName(jTextFieldName.getText());

    }


    public void focusGained(FocusEvent e)
    {
        logger.logComment("Focus gained on: "+ e.toString());
    };

    public void focusLost(FocusEvent e)
    {

        logger.logComment("Focus lost on: "+ e.toString());

        updateCellMechanism();
    }



    public AbstractedCellMechanism getFinalCellMechanism()
    {
        updateCellMechanism();



        return currentCellMechanism;

    }

    void jTabbedPaneCellMechanismInfo_stateChanged(ChangeEvent e)
    {
        logger.logComment(">>>>>>>>>>>   Tabbed panel changed: "+ e);
        this.repaint();
        this.validate();
    }

    public void jButtonPlotsFile_actionPerformed(ActionEvent e)
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);

        String lastCellMechDir = recentFiles.getMyLastCellProcessesDir();

        if (lastCellMechDir == null) lastCellMechDir
            = GeneralProperties.getnCProjectsDir().getAbsolutePath();

        File defaultDir = new File(lastCellMechDir);

        chooser.setCurrentDirectory(defaultDir);
        logger.logComment("Set Dialog dir to: " + defaultDir.getAbsolutePath());

        chooser.setDialogTitle("Choose a file specifying the plots for the Cell Mechanism: " + currentCellMechanism);
        int retval = chooser.showDialog(this, "Choose plot file");

        if (retval == JOptionPane.OK_OPTION)
        {
            File newLocation = ProjectStructure.getFileBasedCellProcessesDir(project.getProjectMainDirectory(), false);

            File newFileName = null;
            try
            {
                newFileName = GeneralUtils.copyFileIntoDir(new File(chooser.getSelectedFile().getAbsolutePath()),
                                                       newLocation);

                //File newFile = new File(newLocation, newFileName, );

                recentFiles.setMyLastCellProcessesDir(chooser.getSelectedFile().getParent());

                currentCellMechanism.setPlotInfoFile(newFileName.getName());


                setCellMechanism(currentCellMechanism);
            }
            catch (IOException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem when including new Cell Mechanism", ex, this);
                return;
            }

        }
    }
/*
    public void jButtonPlotWhat_actionPerformed(ActionEvent e)
    {
        logger.logComment("Checking what to plot...");

        if (jComboBoxPlots.getSelectedItem().equals(choosePlot)) return;

        CellMechanismPlot plot = (CellMechanismPlot)jComboBoxPlots.getSelectedItem();


        logger.logComment("Asking to plot: "+ plot);


        Variable v = new Variable(plot.getIndependentVariable());

        //Variable[] vars = new Variable[]{v};

        int numPoints = 200;


        try
        {
            Hashtable parameters = plot.getParameters();

            Enumeration paramSymbols = parameters.keys();
            Vector allArgs = new Vector();
            Vector allParamsAsVars = new Vector();
            //allVars.add(v);

            String paramDesc = new String("Parameters present in function:\n");

            while (paramSymbols.hasMoreElements())
            {
                String nextSymbol = (String) paramSymbols.nextElement();
                String paramName = (String) parameters.get(nextSymbol);

                logger.logComment("Found param: " + paramName);
                float value = currentCellMechanism.getParameter(paramName);

                Argument nextArg = new Argument(nextSymbol, value);
                allArgs.add(nextArg);
                allParamsAsVars.add(new Variable(nextSymbol));

                paramDesc = paramDesc + paramName+ " = "
                       + value+ " " + currentCellMechanism.getParameterUnits(paramName).getSymbol()
                       + " ("+nextSymbol+")\n";

            }

            Variable[] vars = new Variable[allParamsAsVars.size()+1];

            Argument[] args = new Argument[allParamsAsVars.size()+1];

            vars[0] = v;
            args[0] = new Argument(v.getName(), 0);
            for (int i = 0; i < allParamsAsVars.size(); i++)
            {
                vars[i+1] = (Variable)allParamsAsVars.elementAt(i);
                args[i+1] = (Argument)allArgs.elementAt(i);
            }

            EquationUnit eqn = Expression.parseExpression(plot.getExpression(), vars);



            String plotName = "Plot of "+ plot.getPlotName()+": f(" + v.getNiceString() + ") = " + eqn.getNiceString();
            String plotDesc = "Plot of \"" + plot.getPlotName()
                + "\"\nRepresented by function:\nf(" + v.getNiceString() + ") = " + eqn.getNiceString()+"\n"
                +"evaluated from "+plot.getMinValue() +" to "+plot.getMaxValue()+"\n\n"
                + paramDesc;


            DataSet ds = new DataSet(plotName, plotDesc, "", "", "", "");

            ds.setGraphColour(Color.red);

            logger.logComment("All arguments: " + allArgs);

            for (int i = 0; i < numPoints; i++)
            {

                double nextXval = plot.getMinValue() + ( ( (plot.getMaxValue() - plot.getMinValue()) / (numPoints - 1)) * i);

                args[0].setValue(nextXval);

                ds.addPoint(nextXval, eqn.evaluateAt( args));
            }

            PlotterFrame frame = PlotManager.getPlotterFrame(plotName);

            frame.addDataSet(ds);

            frame.setVisible(true);
        }
        catch(EquationException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem with the form of the equation: "+ plot, ex, this);
        }
        catch(CellMechanismException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem plotting the graph for cell mechanism: "+ currentCellMechanism, ex, this);
        }


    }*/
}
