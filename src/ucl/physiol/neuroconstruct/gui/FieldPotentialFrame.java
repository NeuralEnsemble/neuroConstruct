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

package ucl.physiol.neuroconstruct.gui;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.*;

import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.simulation.SimulationDataException;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.converters.*;
import ucl.physiol.neuroconstruct.dataset.DataSet;
import ucl.physiol.neuroconstruct.gui.plotter.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.simulation.SimulationData;

/**
 * Dialog for Field Pot properties
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class FieldPotentialFrame extends JFrame
{
    ClassLogger logger = new ClassLogger("NewCellTypeDialog");
    
    private Project project = null;
    
    private SimulationData simData = null;
    
    boolean standalone = false;
    
    
    JPanel jPanelMain = new JPanel();
    
    JLabel jLabelMain = new JLabel();
    
    JPanel jPanelOptions = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JButton jButtonCancel = new JButton("Cancel");
    JButton jButtonCreate = new JButton("Create");
    
    ArrayList<JCheckBox> allCellGroupCBs = new ArrayList<JCheckBox>();
    
    private FieldPotentialFrame()
    {
        
    }
            
            
    public FieldPotentialFrame(Project project, SimulationData simData, boolean standalone)
    {
        this.project = project;
        this.simData = simData;
        
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        
        this.standalone = standalone;

        jbInit();
        pack();
        
    }
    
    
    private void jbInit()
    { 
        
        jPanelMain.setLayout(new BorderLayout(4,4));
        
        jLabelMain.setText("Properties for Field Potential generation");
        jLabelMain.setHorizontalAlignment(JLabel.CENTER);
        jLabelMain.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        jPanelMain.add(jLabelMain, BorderLayout.NORTH);
        jPanelMain.add(jPanelOptions, BorderLayout.CENTER);
        jPanelMain.add(jPanelButtons, BorderLayout.SOUTH);
        
        
        
        jButtonCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                cancelled();
            }
        });
        
        jButtonCreate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                create();
            }
        });
        
        
        
        addOptions();
        
        jPanelButtons.add(jButtonCreate);
        jPanelButtons.add(jButtonCancel);
        
        
        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
        
        
    }
    
    

    private void addOptions()
    {
        Iterator<String> i = project.generatedCellPositions.getNamesGeneratedCellGroups();
        
        while (i.hasNext())
        {
            String cellGroup = i.next();
            
            logger.logComment("Adding check box for: "+ cellGroup);
            
            JCheckBox cb = new JCheckBox(cellGroup);
            cb.setSelected(true);
            
            jPanelOptions.add(cb);
            
            allCellGroupCBs.add(cb);
            
        }
    }
    
    
        //Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        //super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            cancelled();
        }
        //super.processWindowEvent(e);
    }
    
    
    private void cancelled()
    {
        logger.logComment("Cancelled...");
        
        if (standalone)
            System.exit(0);
        else
            dispose();
                
    }
    
    
    private void create()
    {
        logger.logComment("Creating the field potential plot");
            
        ArrayList<String> cellGroupsToAdd = new ArrayList<String>();
        
        for (JCheckBox cb : allCellGroupCBs)
        {
            logger.logComment("Next check box: "+ cb);
            if (cb.isSelected())
                    cellGroupsToAdd.add(cb.getText());
        }
        
        String desc = "Warning!! Still in development!!";
        
        String name = "Very simple field potential";
        
        DataSet ds = new DataSet(name, desc, 
                "ms", "", "Time", "Arbitrary units");
        try
        {
            double[] times = simData.getAllTimes();

            double[] totals = new double[times.length];
            
            int numAdded = 0;

            for(String cellGroup: cellGroupsToAdd)
            {
                logger.logComment("Adding all soma traces for cell group: "+ cellGroup);
                int numInCG = project.generatedCellPositions.getNumberInCellGroup(cellGroup);

                for (int i=0;i<numInCG;i++)
                {
                    String ref = SimulationData.getCellRef(cellGroup, i);


                    double[] volts = simData.getVoltageAtAllTimes(ref);

                    for(int j=0;j<volts.length;j++)
                    {
                        totals[j] +=volts[j];
                    }
                    numAdded++;

                }
            }
            
            for(int j=0;j<totals.length;j++)
            {
                ds.addPoint(times[j], -1*totals[j]/(float)numAdded);
            }
            
        } 

        catch (SimulationDataException ex)
        {
            GuiUtils.showErrorMessage(logger, "Trouble loading cell voltage traces" , ex, this);
        }
        
        PlotterFrame pf = PlotManager.getPlotterFrame(name);
        
        pf.addDataSet(ds);
        
        GuiUtils.centreWindow(pf);
        
        pf.setVisible(true);
        
        this.dispose();  
    }
    
    public static void main(String[] args)
    { 
        try
        {
            File projFile = new File("examples/Ex6-Cerebellum/Ex6-Cerebellum.neuro.xml");

            ProjectManager pm = new ProjectManager();

            Project proj = pm.loadProject(projFile);
            
            System.out.println("Number of cells generated: "+ proj.generatedCellPositions.getNumberInAllCellGroups());
            
            pm.doGenerate(proj.simConfigInfo.getDefaultSimConfig().getName(), 123);
                
            Thread.sleep(1000);
            
            while (pm.isGenerating())
            {
                Thread.sleep(1000);
            }
            
            System.out.println("Number of cells generated: "+ proj.generatedCellPositions.getNumberInAllCellGroups());
            
            
            File simsDir = ProjectStructure.getSimulationsDir(proj.getProjectMainDirectory());
            
            SimulationData sd = new SimulationData(new File(simsDir, "Sim_39"), true);
            
            sd.initialise();
            
                
            FieldPotentialFrame fpf = new FieldPotentialFrame(proj, sd, true);


            GuiUtils.centreWindow(fpf);
            fpf.setVisible(true);
        } 
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
    }
    
    
    
}





