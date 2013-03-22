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

package ucl.physiol.neuroconstruct.utils;



/**
 * Generates a fixed set of numbers in a sequence
 *
 * @author Padraig Gleeson
 *  
 */

public class SequenceGenerator
{
    ClassLogger logger = new ClassLogger("SequenceGenerator");
    private float start = 0;
    private float end = 10;
    private float interval = 1;

    //private float lastNumReturned = Float.NaN;

    int numReturned = 0;


    private SequenceGenerator()
    {
    }

    public SequenceGenerator(float fixedVal)
    {
        start = fixedVal;
        end = fixedVal;
        interval = 0;
    }

    public SequenceGenerator(float start, float end, float interval)
    {
        this.start = start;
        this.end = end;
        this.interval = interval;
    }
    
    @Override
    public Object clone()
    {
        SequenceGenerator sg = new SequenceGenerator(this.start,this.end,this.interval);
        return sg;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final SequenceGenerator other = (SequenceGenerator) obj;
        if (this.start != other.start)
        {
            return false;
        }
        if (this.end != other.end)
        {
            return false;
        }
        if (this.interval != other.interval)
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 97 * hash + Float.floatToIntBits(this.start);
        hash = 97 * hash + Float.floatToIntBits(this.end);
        hash = 97 * hash + Float.floatToIntBits(this.interval);
        return hash;
    }
    
    
    

    public float getInterval()
    {
        return this.interval;
    }
    public float getStart()
    {
        return this.start;
    }
    public float getEnd()
    {
        return this.end;
    }

    public void setStart(float start)
    {
        this.start = start;
    }
    public void setEnd(float end)
    {
        this.end = end;
    }
    public void setInterval(float interval)
    {
        this.interval = interval;
    }







    @Override
    public String toString()
    {
        int num = getNumInSequence();

        logger.logComment("Num in sequence: "+ getNumInSequence());

        if (num<0) return "**Bad SequenceGenerator** [start: "+start+", end :"+end+", interval: "+interval+"]";

        if (num==1) return start+"";


        if (num==2) return "["+start+", "+(start+interval)+"]";
        if (num==3) return "["+start+", "+(start+interval)+", "+(start+interval+interval)+"]";

        return "["+start+", "+(start+interval)+",..., "+(start+(interval* (getNumInSequence()-1)))+"]";
    }

    public int getNumInSequence()
    {
        if (interval==0 || start==end) return 1;

        /** @todo Get better solution for correcting for double/float error */

        return (int)Math.floor(1.000001 * (float)(end-start)/(float)interval) +1; //
    }



    public float getNumber() throws EndOfSequenceException
    {
        /*
        if (Double.isNaN(lastNumReturned))
        {
            lastNumReturned = (float)start;
        }
        else
        {

            if ((lastNumReturned + interval)>end) throw new EndOfSequenceException();

            lastNumReturned = lastNumReturned + (float)interval;

            logger.logComment("lastNumReturned: "+lastNumReturned+", interval: "+ interval, true);
        }

        return (float)lastNumReturned;
*/

        if (!hasMoreNumbers())
            throw new EndOfSequenceException();

        float nextNum = start + (numReturned * interval);
        numReturned++;

        return nextNum;

    }

    public void reset()
    {
        //this.lastNumReturned = Float.NaN;
         numReturned = 0;
    }

    public boolean hasMoreNumbers()
    {
        //System.out.println("lastNumReturned: "+lastNumReturned);
        if (numReturned>=getNumInSequence()) return false;
        return true;
       // return (lastNumReturned + interval)<=end;
    }




    public static void main(String[] args)
    {
        SequenceGenerator sg1 = new SequenceGenerator(3);
        System.out.println("sg1: "+sg1);
        SequenceGenerator sg2 = new SequenceGenerator(1,2.1f,.1f);
        System.out.println("sg2: "+sg2);
        SequenceGenerator sg3 = new SequenceGenerator(.2f,2,.2f);
        System.out.println("sg3: "+sg3);


        SequenceGenerator usedSg = sg2;

        System.out.println("Num in seq: "+ usedSg.getNumInSequence());

       int  count = 0;
        try
        {
            while (usedSg.hasMoreNumbers())
            {
                System.out.println("Number "+count+": " + usedSg.getNumber());
                count++;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }


    }


    @SuppressWarnings("serial")
    
    public class EndOfSequenceException extends Exception
    {

    }
}
