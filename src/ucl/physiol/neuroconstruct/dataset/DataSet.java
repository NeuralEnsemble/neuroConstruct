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

package ucl.physiol.neuroconstruct.dataset;


import  java.util.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.awt.*;
import ucl.physiol.neuroconstruct.gui.plotter.*;
import java.io.*;

/**
 * Storage for the set of points to be plotted. Basically a wrapper for 2
 * double arrays for the x and y values. Also contained here is some metadata for
 * the preferred graph format/colour, some comments associated with the points and
 * a reference to the file to store the data in.
 *
 * @author Padraig Gleeson
 *  
 */



public class DataSet
{
    private static ClassLogger logger = new ClassLogger("DataSet");

    private int initialCapacity = 10;
    private double capacityGrowthFactor = 0.5;

    private double[] xValues = new double[initialCapacity];
    private double[] yValues = new double[initialCapacity];

    private int numberValidPoints = 0;

    private String reference = null;
    private String description = null;

    private Color graphColour = Color.BLACK;
    private String graphFormat = PlotCanvas.USE_LINES_FOR_PLOT;

    private String xLegend = "";
    private String yLegend = "";
    private String xUnit = "";
    private String yUnit = "";
    
    public static int xDim = 0;
    public static int yDim = 1;
    


    /**
     * means each subsequent x val GREATER than previous
     */
    private boolean xValsStrictlyInc = true;


    /**
     * Only stores String comment against (Integer) point num for points which have one
     */
    private Hashtable<Integer, String> comments = new Hashtable<Integer, String>();

    /**
     * Quick summary on whether point has comment, to avoid looking up Hashtable every time
     */
    private boolean[] pointHasComment = new boolean[initialCapacity];

    /**
     * Used if this Data Set is saved in a project
     */
    private File dataSetFile = null;

    private DataSet()
    {

    }

    private DataSet(String refrence, String description)
    {
        this.reference = refrence;
        this.description = description;
    }

    public DataSet(String refrence, String description, String xUnit, String yUnit, String xLegend, String yLegend)
    {
        this.reference = refrence;
        this.description = description;

        this.xUnit = xUnit;
        this.yUnit = yUnit;

        this.xLegend = xLegend;
        this.yLegend = yLegend;
    }


    public File getDataSetFile()
    {
        return dataSetFile;
    }

    public double[] getPoint(int index)
    {
        if (index >= numberValidPoints || index<0) return null;
        double[] nextPoint
            = new double[]
            {xValues[index],
            yValues[index]};

        return nextPoint;
    }

    public void updateCommentArray()
    {
        logger.logComment("Making sure the pointHasComment array is up to date...");
        pointHasComment = new boolean[numberValidPoints];

        for (int i = 0; i < numberValidPoints; i++)
        {
            pointHasComment[i] = false;
        }
        Enumeration enumeration = comments.keys();

        while (enumeration.hasMoreElements())
        {

            Integer next = (Integer) enumeration.nextElement();
            logger.logComment("Comment found at: " + next);
            pointHasComment[next.intValue()] = true;
        }

    }

    public void setCommentOnPoint(int pointNum, String comment)
    {
        if (pointNum >= pointHasComment.length)
        {
            // just to keep everything up to date...
            updateCommentArray();
        }
        logger.logComment("Setting comment: " + comment + " on point: "
                          + pointNum + " of my " + numberValidPoints + " points");

        if (comment.trim().length() == 0)
        {
            comments.remove(new Integer(pointNum));
        }
        else
        {
            comments.put(new Integer(pointNum), comment);
        }
        updateCommentArray();
    }

    public String getComment(int pointNum)
    {
        if (pointNum >= pointHasComment.length)
        {
            // just to keep everything up to date...
            updateCommentArray();
            return null;
        }
        if (!pointHasComment[pointNum])return null;
        return comments.get(new Integer(pointNum));
    }

    public int getNumberPoints()
    {
        return numberValidPoints;
    }

    /**
     * Adds a point and returns the pointNumber
     */
    public int addPoint(double x, double y)
    {
        if (numberValidPoints == xValues.length)
        {
            int newCapacity = (int) (xValues.length * (1 + capacityGrowthFactor));
            //logger.logComment("Increasing capacity to: " + newCapacity);
            double[] tempXValues = new double[newCapacity];
            double[] tempYValues = new double[newCapacity];

            System.arraycopy(xValues, 0, tempXValues, 0, xValues.length);
            System.arraycopy(yValues, 0, tempYValues, 0, yValues.length);
            xValues = tempXValues;
            yValues = tempYValues;
        }
        xValues[numberValidPoints] = x;
        yValues[numberValidPoints] = y;

        if (numberValidPoints>1)
        {
            boolean inc = xValues[numberValidPoints] > xValues[numberValidPoints - 1];
            xValsStrictlyInc = xValsStrictlyInc && (inc);

            if (!inc)
            {
                //logger.logComment("xValues["+numberValidPoints+"] <= " + "xValues["+(numberValidPoints-1)+"]");
                //logger.logComment(xValues[numberValidPoints] +" <= " + xValues[(numberValidPoints-1)]);
                //logger.logComment("val: "+(xValues[numberValidPoints] > xValues[numberValidPoints - 1]));
                //logger.logComment("xValsStrictlyInc: " + xValsStrictlyInc);
            }

        }

        numberValidPoints++;

        return numberValidPoints - 1;

    }

    public boolean areXvalsStrictlyIncreasing()
    {
        return this.xValsStrictlyInc;
    }

    public void deletePoint(int pointNum) throws DataSetException
    {
        logger.logComment("Deleting point " + pointNum + " from " + numberValidPoints + " valid points");

        if (pointNum >= 0 && pointNum < numberValidPoints)
        {
            for (int i = pointNum; i < numberValidPoints - 1; i++)
            {
                logger.logComment("Replacing points at " + i + " with points at " + (i + 1));
                xValues[i] = xValues[i + 1];
                yValues[i] = yValues[i + 1];

                if (pointHasComment[i + 1])
                {
                    setCommentOnPoint(i, getComment(i + 1));
                    updateCommentArray();
                }
            }
            numberValidPoints--;

        }
        else
        {
            throw new DataSetException("The point number " + pointNum + " doesn't exist. Only " +
                            numberValidPoints + " valid points");
        }
        recheckStrictlyInc();

    }

    /**
     * Gets the spacing between each X value. If the x values are not a common
     * distance apart (or if numberValidPoints<2), returns -1
     * NOTE: Only checks spacings difference < 1/500, so this function should
     * only be used for graphing purposes
     *
     */
    public double getXSpacing() throws DataSetException
    {
        if (numberValidPoints < 2) throw new DataSetException("There are "+numberValidPoints+" so not enough for an X spacing");

        double currentSpacing = -1;
        for (int i = 1; i < numberValidPoints; i++)
        {
            double prevX = xValues[i - 1];
            double thisX = xValues[i];

            double diffSpacing = (thisX - prevX) - currentSpacing;

            if (i > 1 && diffSpacing > currentSpacing / 500)
            {
                throw new DataSetException("Not evenly spaced " + i + ": " + prevX + ", " + thisX
                                  + ", currentSpacing: " + currentSpacing
                                  + ", (thisX-prevX): " + (thisX - prevX)
                                  + ", diffSpacing: " + diffSpacing);
            }

            currentSpacing = (thisX - prevX);
        }
        return currentSpacing;
    }

    public double getYvalue(double x) throws ValueNotPresentException
    {
        for (int i = 0; i < numberValidPoints; i++)
        {
            if (xValues[i] == x)return yValues[i];
        }
        throw new ValueNotPresentException();
    }

    public double[] getXValues()
    {
        double[] justValidPoints = new double[numberValidPoints];
        System.arraycopy(xValues, 0, justValidPoints, 0, numberValidPoints);
        return justValidPoints;
    }

    private void recheckStrictlyInc()
    {
        if (numberValidPoints==0)  
        {
            xValsStrictlyInc = false;
            return;
        }
        if (numberValidPoints==1)
        {
            xValsStrictlyInc = true;
            return;
        }
        Double diff = xValues[1] - xValues[0];
        for (int i = 1; i < numberValidPoints; i++)
        {
            double newDiff = xValues[i] - xValues[i-1];
            if (newDiff!=diff)
            {
                this.xValsStrictlyInc=false;
                return;
            }
        }
        this.xValsStrictlyInc=true;

    }

    public void setXValue(int pointNum, double value) throws DataSetException
    {
        if (pointNum >= numberValidPoints) throw new DataSetException("Index out of bounds: " + pointNum
                                                                     + ", only " + numberValidPoints +
                                                                     " points.");
        this.xValues[pointNum] = value;
        recheckStrictlyInc();
    }

    public void setYValue(int pointNum, double value) throws DataSetException
    {
        if (pointNum >= numberValidPoints)throw new DataSetException("Index out of bounds: " + pointNum
                                                                     + ", only " + numberValidPoints +
                                                                     " points.");
        this.yValues[pointNum] = value;
    }

    public double[] getYValues()
    {
        double[] justValidPoints = new double[numberValidPoints];
        System.arraycopy(yValues, 0, justValidPoints, 0, numberValidPoints);
        return justValidPoints;
    }

    /**
     * returns String containing bracketed points, e.g.
     * [(0, 0), (1, 1), ...]
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("DataSet: "+this.getReference()+" [");

        if (numberValidPoints>=1)
        {
            sb.append(pointToString(0));
        }
        if (numberValidPoints>=2)
        {
            sb.append(", "+pointToString(1));
        }
        if (numberValidPoints==3)
        {
            sb.append(", "+pointToString(2));
        }
        else if (numberValidPoints>3)
        {
            sb.append(", ... , "+pointToString(numberValidPoints-1));

        }
        sb.append("] ("+numberValidPoints+" points)\n");
        return sb.toString();
    }
    

    public String pointToString(int i)
    {
        return "(" + xValues[i] + ", " + yValues[i] + ")";
    }

    public double[] getMaxX()
    {
        if (getNumberPoints()==0) return new double[]{0,0};

        int index = -1;

        double maxVal = -1* Double.MAX_VALUE;

        for (int i = 0; i < numberValidPoints; i++)
        {
            double nextVal = xValues[i];
            if ( nextVal > maxVal)
            {
                index = i;
                maxVal = nextVal;
            }
        }
        return getPoint(index);
    }


    public double[] getMinX()
    {
        if (getNumberPoints()==0) return new double[]{0,0};

        int index = -1;

        double minVal = Double.MAX_VALUE;

        for (int i = 0; i < numberValidPoints; i++)
        {
            double nextVal = xValues[i];
            if ( nextVal < minVal)
            {
                index = i;
                minVal = nextVal;
            }
        }
        return getPoint(index);
    }


    public double[] getMaxY()
    {
        if (getNumberPoints()==0) return new double[]{0,0};

        int index = -1;

        double maxVal = -1* Double.MAX_VALUE;

        for (int i = 0; i < numberValidPoints; i++)
        {
            double nextVal = yValues[i];
            if ( nextVal > maxVal)
            {
                index = i;
                maxVal = nextVal;
            }
        }
        return getPoint(index);
    }


    public double[] getMinY()
    {
        if (getNumberPoints()==0) return new double[]{0,0};

        int index = -1;

        double minVal = Double.MAX_VALUE;

        for (int i = 0; i < numberValidPoints; i++)
        {
            double nextVal = yValues[i];
            if ( nextVal < minVal)
            {
                index = i;
                minVal = nextVal;
            }
        }
        return getPoint(index);
    }




    public String getXUnit()
    {
        return this.xUnit;
    }

    public String getYUnit()
    {
        return this.yUnit;
    }

    public void setUnits(String xUnit, String yUnit)
    {
        this.xUnit = xUnit;
        this.yUnit = yUnit;
    }

    public void setXUnit(String xUnit)
    {
        this.xUnit = xUnit;
    }
    public void setYUnit(String yUnit)
    {
        this.yUnit = yUnit;
    }

    public void setXLegend(String xLegend)
    {
        this.xLegend = xLegend;
    }
    public void setYLegend(String yLegend)
    {
        this.yLegend = yLegend;
    }




    public String getXLegend()
    {
        return this.xLegend;
    }

    public String getYLegend()
    {
        return this.yLegend;
    }

    public void setLegends(String xLegend, String yLegend)
    {
        this.xLegend = xLegend;
        this.yLegend = yLegend;
    }


    public String getDescription()
    {
        return description;
    }



    public void setDescription(String description)
    {
        this.description = description;
    }

    public Color getGraphColour()
    {
        return graphColour;
    }

    public void setGraphColour(Color graphColour)
    {
        this.graphColour = graphColour;
    }

    public String getGraphFormat()
    {
        return graphFormat;
    }

    public void setGraphFormat(String graphFormat)
    {
        this.graphFormat = graphFormat;
    }

    public String getReference()
    {
        return reference;
    }


    public String getSafeReference()
    {
        String refNew = reference;
        refNew = GeneralUtils.replaceAllTokens(refNew, " ", "_");
        refNew = GeneralUtils.replaceAllTokens(refNew, "(", "");
        refNew = GeneralUtils.replaceAllTokens(refNew, ")", "");
        refNew = GeneralUtils.replaceAllTokens(refNew, "-", "_");
        refNew = GeneralUtils.replaceAllTokens(refNew, ":", "_");
        refNew = GeneralUtils.replaceAllTokens(refNew, "+", "_");
        refNew = GeneralUtils.replaceAllTokens(refNew, "/", "_");
        refNew = GeneralUtils.replaceAllTokens(refNew, ".", "_");
        return refNew;
    }

    public void setReference(String reference)
    {
        this.reference = reference;
    }


    public void setDataSetFile(File dataSetFile)
    {
        this.dataSetFile = dataSetFile;
    }

    public static void main(String[] args) throws DataSetException
    {
        GeneralUtils.printMemory(true);

        double minX = 3;
        double maxX = 8;
        int numPoints = 100000;

        DataSet data1 = new DataSet("data1", "test data");

        data1.setGraphColour(Color.CYAN);

        data1.setGraphFormat(PlotCanvas.USE_LINES_FOR_PLOT);

        long start = System.currentTimeMillis();

        //System.

        for (int i = 0; i < numPoints; i++)
        {
            double x = minX + (maxX-minX)*((double)i/(numPoints-1));
            double y1 = 3*Math.sin(x*13);
            data1.addPoint(x,y1);
        }

        GeneralUtils.printMemory(true);



        System.out.println("Time taken to build data set: "+(System.currentTimeMillis()-start));

        System.out.println("First point: "+ data1.getPoint(0)[0]+", "+ data1.getPoint(0)[1]);
        System.out.println("Second point: "+ data1.getPoint(1)[0]+", "+ data1.getPoint(1)[1]);
        System.out.println("Max x: "+ data1.getMaxX()[0]);
        System.out.println("Max y: "+ data1.getMaxY()[1]);
        System.out.println("Min x: "+ data1.getMinX()[0]);
        System.out.println("Min y: "+ data1.getMinY()[1]);
      //  System.out.println("toString: " + data1.toString());
        System.out.println("getXSpacing: "+ data1.getXSpacing());


        start = System.currentTimeMillis();

        int numPointsInData = data1.getNumberPoints();

        for (int i = 0; i < numPointsInData; i++)
        {
            double[] nextPoint = data1.getPoint(i);
            //System.out.println("nextPoint: ("+ nextPoint[0]+", "+ nextPoint[1]+"]");
        }

        System.out.println("Time taken to parse data set: "+(System.currentTimeMillis()-start));

        PlotterFrame plotFrame = PlotManager.getPlotterFrame("Test Plotter Frame");

        GeneralUtils.printMemory(true);

        plotFrame.addDataSet(data1);

        GeneralUtils.printMemory(true);
    }

}
