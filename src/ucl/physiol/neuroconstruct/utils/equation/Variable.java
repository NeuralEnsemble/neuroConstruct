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


package ucl.physiol.neuroconstruct.utils.equation;


/**
 * Helper class for parsing equations
 *
 * @author Padraig Gleeson
 *  
 */

public class Variable extends EquationUnit
{
    static final long serialVersionUID = -47164166L;

    public Variable(String name)
    {
        super(name);
    }

    public double evaluateAt(Argument[] args) throws EquationException
    {
        for (int i = 0; i < args.length; i++)
        {
            //logger.logComment("Checking if "+ args[i].getName()
            //    + " equals "+ getName());

            if (args[i].getName().equals(getName()))
                return args[i].getValue();
        }
        throw new EquationException("Asked to evaluate variable "
                                    + getName() + " with: "+
                                    Argument.toString(args) +
                                    " but can't find variable in arguments");
    };


    public String getNiceString()
    {
        return getName();
    }

    public String toString()
    {
        return getName();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof Variable)
        {
            Variable v = (Variable)obj;
            if (v.getName().equals(getName())) return true;
        }
        return false;
    }
    
    @Override
    public Object clone()
    {
        Variable v2 = new Variable(new String(getName()));
        return v2;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + (getName() != null ? getName().hashCode() : 0);
        return hash;
    }
    

    /**
     * Default constructor is needed for XMLEncoder.
     */
    public Variable()
    {
    }
    
    @Override
    public void setName(String name)
    {
        super.setName(name);
    }
    
}
