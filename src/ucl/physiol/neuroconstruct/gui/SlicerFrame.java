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

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import javax.media.j3d.Appearance;
import javax.swing.*;

import javax.swing.event.ChangeEvent;
import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Frame for viewing slices of the 3D network
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class SlicerFrame extends JFrame
{

    private ClassLogger logger = new ClassLogger("SlicerFrame");

    private Project project = null;

    private SimulationInterface controlledPanel;

    JPanel jPanelButtons = new JPanel();
    
    JPanel jPanelSlider = new JPanel();
    JPanel jPanelSliderX = new JPanel();
    JPanel jPanelSliderY = new JPanel();
    JPanel jPanelSliderZ = new JPanel();
    
    JPanel jPanelMoreOptions = new JPanel();
    JSpinner jSpin = new JSpinner();
    SpinnerNumberModel spinMod = new SpinnerNumberModel(0.9, 0, 1, 0.005);
    
    JLabel jLabelSpin = new JLabel("Transparency");
    
    JButton jButtonClose = new JButton("Close");
    JButton jButtonReset = new JButton("Reset");
    
    JPanel jPanelMain = new JPanel();
    
    JSlider jSliderX1 = new JSlider(0, 100, 100); // inverted...
    JSlider jSliderX2 = new JSlider(0,100,100);
    
    JSlider jSliderY1 = new JSlider(0, 100, 100); // inverted...
    JSlider jSliderY2 = new JSlider(0,100,100);
    
    JSlider jSliderZ1 = new JSlider(0, 100, 100); // inverted...
    JSlider jSliderZ2 = new JSlider(0,100,100);
    
    float fractStartX = 0;
    float fractEndX = 1;
    float fractStartY = 0;
    float fractEndY = 1;
    float fractStartZ = 0;
    float fractEndZ = 1;
    
    RectangularBox enclosingBox = null;
    
    private static String X1 = "X1";
    private static String X2 = "X2";
    private static String Y1 = "Y1";
    private static String Y2 = "Y2";
    private static String Z1 = "Z1";
    private static String Z2 = "Z2";
    
    Hashtable<String, ArrayList<Float>> cellsAlignedInY = new Hashtable<String, ArrayList<Float>>();
    
    boolean standalone = false;
    
    Appearance transparency = Utils3D.getTransparentObjectAppearance(Color.white, 0.9f);


    public SlicerFrame(Project project,
                       SimulationInterface simInf,
                       SimConfig simConfig)
    {
        this.project = project;
        controlledPanel = simInf;
        
        //logger.setThisClassVerbose(true);

        logger.logComment("SlicerFrame created...");
        
        this.setTitle("Region slicer");
        
        enclosingBox = project.regionsInfo.getRegionEnclosingAllRegions(project, simConfig); 
        
        try
        {
            enableEvents(AWTEvent.WINDOW_EVENT_MASK);
            jbInit();

        }
        catch(Exception e)
        {
            logger.logComment("Exception starting GUI: "+ e);
        }
    }
    
//    void parsePositions()
//    {
//        for(String cg: project.generatedCellPositions.getNonEmptyCellGroups())
//        {
//            ArrayList<PositionRecord> posRecs = project.generatedCellPositions.getPositionRecords(cg);
//            //ArrayList<Float> yValsOrdered = new ArrayList<Float>();
//            
//            
//        }
//    }

    // needed when the 3D panel is reset...
    public void setSimInterface(SimulationInterface simInf)
    {
        controlledPanel = simInf;
    }


    private void jbInit() throws Exception
    {
        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
        
        jPanelMain.setLayout(new BorderLayout(4, 4));
        
        jPanelMain.add(jPanelSlider, BorderLayout.CENTER);
        jPanelMain.add(jPanelButtons, BorderLayout.SOUTH);
        
        jPanelButtons.add(jButtonClose);
        
        jButtonClose.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                close();
            }
        });
        
        jPanelButtons.add(jButtonReset);
        
        jButtonReset.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                reset();
            }
        });
        
        
        
        jSliderX1.setInverted(true);
        jSliderX1.setName(X1);
        jSliderX2.setName(X2);
        
        jPanelSliderX.add(jSliderX1);
        jPanelSliderX.add(jSliderX2);
        jPanelSliderX.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.green), "X axis"));
        
        
        jSliderX1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                jSlider_mouseReleased(e);
            }
        });
        jSliderX2.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                jSlider_mouseReleased(e);
            }
        });
        jPanelSlider.setLayout(new GridLayout(4,1));
        jPanelSlider.add(jPanelSliderX);
        
        
        
        jSliderY1.setInverted(true);
        jSliderY1.setName(Y1);
        jSliderY2.setName(Y2);
        
        jPanelSliderY.add(jSliderY1);
        jPanelSliderY.add(jSliderY2);
        jPanelSliderY.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.yellow), "Y axis"));
        
        
        jSliderY1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                jSlider_mouseReleased(e);
            }
        });
        jSliderY2.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                jSlider_mouseReleased(e);
            }
        });
        
        jPanelSlider.add(jPanelSliderY);
        
        
        
        
        
        
        jSliderZ1.setInverted(true);
        jSliderZ1.setName(Z1);
        jSliderZ2.setName(Z2);
        
        jPanelSliderZ.add(jSliderZ1);
        jPanelSliderZ.add(jSliderZ2);
        jPanelSliderZ.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.red), "Z axis"));
        
        
        jSliderZ1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                jSlider_mouseReleased(e);
            }
        });
        jSliderZ2.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                jSlider_mouseReleased(e);
            }
        });
        
        jPanelSlider.add(jPanelSliderZ);
        
        jPanelSlider.add(jPanelMoreOptions);
        jPanelMoreOptions.add(new JLabel("Transparency (~0.8 -> 1):"));
        jPanelMoreOptions.add(jSpin);
        jSpin.setModel(spinMod);
        jSpin.setPreferredSize(new Dimension(56, 30));
        
        
        jSpin.addChangeListener((new javax.swing.event.ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                transparency = Utils3D.getTransparentObjectAppearance(Color.white, ((Double)jSpin.getValue()).floatValue());
            }
        }));
   
    }
    
    
    
    void jSlider_mouseReleased(MouseEvent e)
    {
        String relSlider = ((JSlider)e.getSource()).getName();
        logger.logComment("Released slider: "+ relSlider);
       
        if (relSlider.equals(X1))
        {
            int x1 = 100 - jSliderX1.getValue();
            fractStartX = (fractEndX) * x1 /100f;
        }       
        else if (relSlider.equals(X2))
        {
            int x2 = jSliderX2.getValue();
            fractEndX = fractStartX + ((1-fractStartX) * x2 /100f);
        }
        else if (relSlider.equals(Y1))
        {
            int y1 = 100 - jSliderY1.getValue();
            fractStartY = (fractEndY) * y1 /100f;
        }       
        else if (relSlider.equals(Y2))
        {
            int y2 = jSliderY2.getValue();
            fractEndY = fractStartY + ((1-fractStartY) * y2 /100f);
        }
        else if (relSlider.equals(Z1))
        {
            int z1 = 100 - jSliderZ1.getValue();
            fractStartZ = (fractEndZ) * z1 /100f;
        }       
        else if (relSlider.equals(Z2))
        {
            int z2 = jSliderZ2.getValue();
            fractEndZ = fractStartZ + ((1-fractStartZ) * z2 /100f);
        }
        
        logger.logComment("Cutoffs: ("+fractStartX+" -> "+fractEndX+") ("+fractStartY+" -> "+fractEndY+") ("+fractStartZ+" -> "+fractEndZ+")");
        
        float minX = enclosingBox.getLowestXValue() +(fractStartX*enclosingBox.getXExtent());
        float maxX = enclosingBox.getLowestXValue() +(fractEndX*enclosingBox.getXExtent());
        
        logger.logComment("Maxmin x: ("+minX+" -> "+maxX+")");
        
        float minY = enclosingBox.getLowestYValue() +(fractStartY*enclosingBox.getYExtent());
        float maxY = enclosingBox.getLowestYValue() +(fractEndY*enclosingBox.getYExtent());
        
        logger.logComment("Maxmin y: ("+minY+" -> "+maxY+")");
        
        float minZ = enclosingBox.getLowestZValue() +(fractStartZ*enclosingBox.getZExtent());
        float maxZ = enclosingBox.getLowestZValue() +(fractEndZ*enclosingBox.getZExtent());
        
        logger.logComment("Maxmin z: ("+minZ+" -> "+maxZ+")");
        
        for(String cg: project.generatedCellPositions.getNonEmptyCellGroups())
        {
            ArrayList<PositionRecord> posRecs = project.generatedCellPositions.getPositionRecords(cg);
            
            for(PositionRecord pos: posRecs)
            {
                if ( (pos.x_pos<minX || pos.x_pos>maxX) ||
                     (pos.y_pos<minY || pos.y_pos>maxY) ||
                     (pos.z_pos<minZ || pos.z_pos>maxZ) )
                {
                    controlledPanel.setTempAppearance(SimulationData.getCellRef(cg, pos.cellNumber), transparency);
                }
                else
                {
                    controlledPanel.removeTempAppearance(SimulationData.getCellRef(cg, pos.cellNumber));
                }
            }
            
        }
        
    }
    
    
    private void close()
    {
        logger.logComment("Closeed...");
        
        if (standalone)
            System.exit(0);
        else
            dispose();
                
    }
    
    private void reset()
    {
        jSliderX1.setValue(100);
        jSliderX2.setValue(100);
        jSliderY1.setValue(100);
        jSliderY2.setValue(100);
        jSliderZ1.setValue(100);
        jSliderZ2.setValue(100);
        controlledPanel.refreshAll3D();
    }
    


    //Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        //super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            dispose();
            if (standalone)
                System.exit(0);
        }
    }




    public static  void main(String[] args)
    {

        File pf = new File("examples/Ex5-Networks/Ex5-Networks.neuro.xml");

        Project p = null;
        try
        {
            p = Project.loadProject(pf, null);
            System.out.println("Opened: "+ p.getProjectFullFileName());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        File simFile = new File(pf.getParentFile(),"simulations/Sim_1" );
        Main3DPanel m = new Main3DPanel(p, simFile, p.simConfigInfo.getDefaultSimConfig());

        SlicerFrame simRerun = new SlicerFrame(p, m, p.simConfigInfo.getDefaultSimConfig());
        simRerun.standalone = true;
        simRerun.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = simRerun.getSize();
        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;
        }
        simRerun.setLocation( (screenSize.width - frameSize.width) / 2,
                             (screenSize.height - frameSize.height) / 2);

        simRerun.setVisible(true);


    }



}
