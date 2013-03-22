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

package ucl.physiol.neuroconstruct.gui.plotter;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import ucl.physiol.neuroconstruct.dataset.*;
import ucl.physiol.neuroconstruct.gui.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Simple Dialog for editing points in Data Set
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class EditPointsDialog extends JDialog implements FocusListener, KeyListener
{
    ClassLogger logger = new ClassLogger("EditPointsDialog");

    int currentPointNumber = 0;

    boolean standAlone = false;

    DataSet dataSet = null;
    JPanel panelMain = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelButtons = new JPanel();
    JPanel jPanelMainEdit = new JPanel();
    JButton jButtonOK = new JButton();
    JButton jButtonCancel = new JButton();
    JPanel jPanelDetails = new JPanel();
    JLabel jLabelRef = new JLabel();
    JTextField jTextFieldRef = new JTextField();
    JLabel jLabelNumber = new JLabel();
    JTextField jTextFieldNum = new JTextField();
    JPanel jPanelEditor = new JPanel();
    JPanel jPanelMove = new JPanel();
    JPanel jPanelParams = new JPanel();


    IntegerInputField inputPointNumber = new IntegerInputField();


    JButton jButtonForward = new JButton();
    JButton jButtonBack = new JButton();
    JLabel jLabelY = new JLabel();
    JLabel jLabelComment = new JLabel();
    JLabel jLabelX = new JLabel();

    JTextField inputFieldComment = new JTextField();
    DoubleInputField inputFieldYval = new DoubleInputField();
    DoubleInputField inputFieldXval = new DoubleInputField();

    BorderLayout borderLayout3 = new BorderLayout();
    JButton jButtonAdd = new JButton();
    JButton jButtonDelete = new JButton();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JButton jButtonTop = new JButton();
    JButton jButtonBottom = new JButton();

    JEditorPane pointDisplay = new JEditorPane();

    JPanel jPanelList = new JPanel();
    BorderLayout borderLayout4 = new BorderLayout();
    Border border1;
    JLabel jLabelPointNum = new JLabel();
    JButton jButtonPlot = new JButton();
    GridBagLayout gridBagLayout3 = new GridBagLayout();
    Border border2;

    public EditPointsDialog(JFrame frame,
                      DataSet dataSet,
                      boolean modal)
    {
        super(frame, "Edit points in Data Set: "+ dataSet.getReference(), modal);

        this.dataSet = dataSet;
        try
        {
            jbInit();
            extraInit();

            inputPointNumber.setIntValue(0);

            update();
            pack();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }


    private void jbInit() throws Exception
    {
        border1 = BorderFactory.createEmptyBorder(5,5,5,5);
        border2 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,Color.white,Color.white,new Color(124, 124, 124),new Color(178, 178, 178)),BorderFactory.createEmptyBorder(3,3,3,3));
        panelMain.setLayout(borderLayout1);
        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOK_actionPerformed(e);
            }
        });
        jButtonCancel.setActionCommand("jButton");
        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCancel_actionPerformed(e);
            }
        });
        jLabelRef.setRequestFocusEnabled(true);
        jLabelRef.setText("Data Set Reference:");
        jTextFieldRef.setEnabled(true);
        jTextFieldRef.setEditable(false);
        jTextFieldRef.setText("...");
        jTextFieldRef.setColumns(12);
        jLabelNumber.setText("Number of points:");
        jTextFieldNum.setEditable(false);
        jTextFieldNum.setText("...");
        jTextFieldNum.setColumns(12);
        jPanelDetails.setLayout(gridBagLayout2);
        jPanelMainEdit.setLayout(gridBagLayout3);
        inputPointNumber.setText("0");
        inputPointNumber.setColumns(6);
        jButtonForward.setText(">");
        jButtonForward.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonForward_actionPerformed(e);
            }
        });
        jButtonBack.setText("<");
        jButtonBack.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonBack_actionPerformed(e);
            }
        });
        jLabelY.setRequestFocusEnabled(true);
        jLabelY.setText("Y value:");
        jLabelComment.setText("Optional comment:");
        jLabelX.setText("X value:");
        jPanelParams.setLayout(gridBagLayout1);
        jPanelEditor.setLayout(borderLayout3);
        inputFieldYval.setText("");
        inputFieldComment.setColumns(18);
        inputFieldComment.setEditable(true);
        inputFieldComment.setText("");
        inputFieldXval.setText("");
        jButtonAdd.setText("Add");
        jButtonAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonAdd_actionPerformed(e);
            }
        });
        jButtonDelete.setEnabled(true);
        jButtonDelete.setText("Delete");
        jButtonDelete.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonDelete_actionPerformed(e);
            }
        });
        jButtonTop.setText(">>");
        jButtonTop.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonTop_actionPerformed(e);
            }
        });
        jButtonBottom.setText("<<");
        jButtonBottom.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonBottom_actionPerformed(e);
            }
        });
        jPanelDetails.setBorder(null);
        jPanelDetails.setMaximumSize(new Dimension(300, 64));
        jPanelDetails.setMinimumSize(new Dimension(300, 64));
        jPanelDetails.setPreferredSize(new Dimension(300, 64));
        //jTextAreaLines.setBorder(BorderFactory.createLoweredBevelBorder());
        pointDisplay.setBorder(border2);
        pointDisplay.setMinimumSize(new Dimension(500, 100));
        pointDisplay.setPreferredSize(new Dimension(500, 100));
        pointDisplay.setEditable(false);
        pointDisplay.setText("...");
        //jTextAreaLines.setc
        //jTextAreaLines.setRows(5);
        jPanelList.setLayout(borderLayout4);
        jPanelList.setBorder(border1);
        jLabelPointNum.setText("Point no:");
        jButtonPlot.setText("Quick plot");
        jButtonPlot.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonPlot_actionPerformed(e);
            }
        });
        jPanelMove.setMinimumSize(new Dimension(285, 40));
        jPanelMove.setPreferredSize(new Dimension(357, 40));
        jPanelEditor.setMaximumSize(new Dimension(510, 300));
        jPanelEditor.setMinimumSize(new Dimension(510, 300));
        jPanelEditor.setPreferredSize(new Dimension(510, 300));
        jPanelMainEdit.setMinimumSize(new Dimension(510, 340));
        jPanelMainEdit.setPreferredSize(new Dimension(510, 340));
        this.setResizable(false);
        getContentPane().add(panelMain);
        panelMain.add(jPanelButtons,  BorderLayout.SOUTH);
        panelMain.add(jPanelMainEdit, BorderLayout.CENTER);
        jPanelMainEdit.add(jPanelDetails,    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        jPanelButtons.add(jButtonOK, null);


        jPanelDetails.add(jLabelRef,        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 14, 6, 14), 0, 0));
        jPanelDetails.add(jTextFieldRef,        new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 14), 100, 0));

        jPanelDetails.add(jLabelNumber,    new GridBagConstraints(0, 1, GridBagConstraints.REMAINDER, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelDetails.add(jTextFieldNum,        new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 14), 0, 0));


        jPanelMainEdit.add(jPanelEditor,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, -71));


        jPanelEditor.add(jPanelMove,  BorderLayout.CENTER);
        jPanelMove.add(jButtonBottom, null);
        jPanelMove.add(jButtonBack, null);
        jPanelMove.add(jLabelPointNum, null);
        jPanelMove.add(inputPointNumber, null);
        jPanelMove.add(jButtonForward, null);
        jPanelMove.add(jButtonTop, null);
        jPanelEditor.add(jPanelParams, BorderLayout.SOUTH);


        jPanelParams.add(jLabelX,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelParams.add(inputFieldXval,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 14), 0,0));
        jPanelParams.add(jLabelY,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0,0));
        jPanelParams.add(inputFieldYval,  new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 14), 0,0));
        jPanelParams.add(jLabelComment,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0,0));
        jPanelParams.add(inputFieldComment,  new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 14), 0,0));
        jPanelEditor.add(jPanelList, BorderLayout.NORTH);
        jPanelList.add(pointDisplay, BorderLayout.CENTER);


        jPanelButtons.add(jButtonAdd, null);
        jPanelButtons.add(jButtonDelete, null);
        jPanelButtons.add(jButtonPlot, null);
        jPanelButtons.add(jButtonCancel, null);
    }


    private void extraInit()
    {
        pointDisplay.setContentType("text/plain");
        pointDisplay.setFont(new Font("Monospaced", Font.PLAIN, 12));


        inputPointNumber.addFocusListener(this);
        inputPointNumber.addKeyListener(this);


        inputFieldXval.addFocusListener(this);
        inputFieldYval.addFocusListener(this);
        inputFieldComment.addFocusListener(this);

    }


    public void focusGained(FocusEvent e)
    {
        logger.logComment("Focus gained on: "+ e.getComponent());

    }


    public void focusLost(FocusEvent e)
    {
        logger.logComment("Focus lost on: "+ e.getComponent());

        compChanged(e);
    }

    public void keyTyped(KeyEvent e)
    {
        logger.logComment("keyTyped on: "+ e.getComponent());
        compChanged(e);
    }

    public void keyPressed(KeyEvent e)
    {
        logger.logComment("keyPressed on: "+ e.getComponent());
        compChanged(e);
    }

    public void keyReleased(KeyEvent e)
    {
        logger.logComment("keyReleased on: "+ e.getComponent());
        compChanged(e);
    }

    private void compChanged(ComponentEvent e)
    {

        JTextField comp = (JTextField)e.getComponent();

        logger.logComment("JTextField's contents: "+ comp.getText());

        if (e.getComponent().equals(inputPointNumber))
        {
            logger.logComment("Point number changed...");
            try
            {
                int newNum = inputPointNumber.getIntValue();

                if (newNum<0)
                    newNum = 0;
                else if (newNum>getHighestPointNumber())
                    newNum = getHighestPointNumber();

                logger.logComment("...to "+ newNum);
                currentPointNumber = newNum;
                update();
            }
            catch(NumberFormatException ex)
            {
                logger.logComment("Not ready yet...");
            }

            //update();
        }

        if (e.getComponent().equals(inputFieldXval))
        {
            logger.logComment("X val changed...");

            try
            {
                double newVal = inputFieldXval.getDoubleValue();
                dataSet.setXValue(inputPointNumber.getIntValue(),
                                  newVal);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Error updating that point: "+ ex.getMessage(), ex, this);
            }
            update();
        }

        if (e.getComponent().equals(inputFieldYval))
        {
            logger.logComment("Y val changed...");

            try
            {
                double newVal = inputFieldYval.getDoubleValue();
                dataSet.setYValue(inputPointNumber.getIntValue(),
                                  newVal);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Error updating that point: "+ ex.getMessage(), ex, this);
            }
            update();
        }


        if (e.getComponent().equals(inputFieldComment))
        {
            logger.logComment("Comment changed...");

            dataSet.setCommentOnPoint(currentPointNumber, inputFieldComment.getText());

            update();
        }


    }


/*
    public void changedUpdate(DocumentEvent e)
    {
       logger.logComment("changedUpdate called. Current val: "
                         + inputPointNumber.getIntValue()
                         + ", source: "+ e.get);
       update();
    }

    public void insertUpdate(DocumentEvent e)
    {
       logger.logComment("insertUpdate called. Current val: "+ inputPointNumber.getIntValue());
       update();
    }

    public void removeUpdate(DocumentEvent e)
    {
       logger.logComment("removeUpdate called. Current val: "+ inputPointNumber.getIntValue());
       update();
    }
*/


    private void setSelectedPoint(int point)
    {
        if (point>getHighestPointNumber()) return;

        //inputPointNumber.setIntValue(point);

        double[] thisPoint = dataSet.getPoint(point);
        inputFieldXval.setDoubleValue(thisPoint[0]);
        inputFieldYval.setDoubleValue(thisPoint[1]);
        String comment = dataSet.getComment(point);
        if (comment==null) comment = "";
        inputFieldComment.setText(comment);
    }


    private void update()
    {
        logger.logComment("---- Updating the GUI to show point " + currentPointNumber);

        this.jTextFieldRef.setText(dataSet.getReference());
        this.jTextFieldNum.setText(dataSet.getNumberPoints()+"");

        inputPointNumber.setIntValue(currentPointNumber);

        setSelectedPoint(currentPointNumber);


        pointDisplay.setText(getPointsSummaryText());

        logger.logComment("---- Finished updating");


    }

    private int getHighestPointNumber()
    {
        return dataSet.getNumberPoints() -1;
    }


    private String getPointsSummaryText()
    {
        StringBuffer sb = new StringBuffer();

        int startLine = 0;
        int numLinesToShow = 5;
        int linesEachSide = 2;

        if (numLinesToShow > dataSet.getNumberPoints())
        {
            numLinesToShow =  dataSet.getNumberPoints();
        }
        else
        {
            if (currentPointNumber > linesEachSide) startLine = currentPointNumber-linesEachSide;
        }

        for (int i = startLine; i < (startLine +numLinesToShow); i++)
        {
            if (i<=getHighestPointNumber())
            {
                logger.logComment("Adding line for number " + i + ", selected? " + (i == currentPointNumber));
                sb.append(getFormattedLine(i, (i == currentPointNumber)) + "\n");
            }
        }
        String stringToShow = sb.toString();
        if (stringToShow.endsWith("\n")) stringToShow = stringToShow.substring(0, stringToShow.length()-1);

        logger.logComment("stringToShow: ("+stringToShow+")");
        return stringToShow;
    }


    private String getFormattedLine(int pointNumber, boolean highlighted)
    {
        int numlineWidth = 6;
        String pointNumString = pointNumber+ "";
        while (pointNumString.length()<numlineWidth) pointNumString = pointNumString+" ";

        double[] point = dataSet.getPoint(pointNumber);

        String comment = dataSet.getComment(pointNumber);

        if (comment==null) comment = "";
        else comment = DataSetManager.DATA_SET_COMMENT + " " + comment;


        if (highlighted) return pointNumString + " --> : " + point[0] + ", "+ point[1] + " "+ comment;
        else return pointNumString + "     : " + point[0] + ", "+ point[1] + " "+ comment;
    }


    public static void main(String[] args)
    {
        DataSet ds = new DataSet("Tester", "Nothing", "", "", "", "");
        ds.addPoint(0,1);
        ds.addPoint(1,2);
        ds.addPoint(06,5);
        ds.addPoint(8,13);
        ds.addPoint(55,5);
        ds.addPoint(66,13);
        ds.addPoint(77,5);
        ds.addPoint(88,13);

        EditPointsDialog dlg = new EditPointsDialog(null, ds, true);
        Dimension dlgSize = dlg.getPreferredSize();
        //Dimension frmSize = getSize();
        //Point loc = getLocation();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();


        dlg.setLocation( (screenSize.width - dlgSize.width) / 2 ,
                        (screenSize.height - dlgSize.height) / 2 );
        dlg.setModal(true);
        dlg.pack();

        dlg.standAlone = true;

        dlg.setVisible(true);



    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        this.dispose();
        if (standAlone) System.exit(0);
    }

    void jButtonBack_actionPerformed(ActionEvent e)
    {
        if (currentPointNumber==0) return;

        if (currentPointNumber>getHighestPointNumber())
            currentPointNumber = getHighestPointNumber();
        else if (currentPointNumber<0)
            currentPointNumber=0;
        else
            currentPointNumber--;
        update();

    }

    void jButtonForward_actionPerformed(ActionEvent e)
    {
        if (currentPointNumber==getHighestPointNumber()) return;

        if (currentPointNumber>getHighestPointNumber())
            currentPointNumber = getHighestPointNumber();
        else if (currentPointNumber<0)
            currentPointNumber=0;
        else
            currentPointNumber++;
        update();
    }

    void jButtonBottom_actionPerformed(ActionEvent e)
    {
        currentPointNumber = 0;
        update();
    }

    void jButtonTop_actionPerformed(ActionEvent e)
    {
        currentPointNumber = getHighestPointNumber();
        update();
    }

    void jButtonAdd_actionPerformed(ActionEvent e)
    {
        int newPointNum = dataSet.getNumberPoints();
        dataSet.addPoint(0,0);

        //inputPointNumber.setIntValue(newPointNum);

        currentPointNumber = newPointNum;

        update();

    }

    void jButtonPlot_actionPerformed(ActionEvent e)
    {
        PlotterFrame frame = PlotManager.getPlotterFrame("Quick Plot of data points in: "
                                                         + dataSet.getReference());

        frame.addDataSet(dataSet);

        frame.setVisible(true);
    }

    void jButtonOK_actionPerformed(ActionEvent e)
    {
        if (dataSet.getDataSetFile()!=null)
        {
            DataSetManager.saveDataSet(dataSet);
        }

        this.dispose();
        if (standAlone) System.exit(0);
    }

    void jButtonDelete_actionPerformed(ActionEvent e)
    {
        try
        {
            dataSet.deletePoint(currentPointNumber);
        }
        catch(DataSetException ex)
        {
            GuiUtils.showErrorMessage(logger, "Point "+currentPointNumber+" does not exist!", ex, this);
            return;
        }

        if (currentPointNumber>getHighestPointNumber())
            currentPointNumber = getHighestPointNumber();

        update();
    }
}
