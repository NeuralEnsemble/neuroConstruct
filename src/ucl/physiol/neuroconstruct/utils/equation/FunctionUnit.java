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

public abstract class FunctionUnit extends EquationUnit
{
    static final long serialVersionUID = -97991769713623L;
                     
    protected EquationUnit internalEqn = null;

    public FunctionUnit(String name, EquationUnit internalEqn)
    {
        super(name);
        this.internalEqn = internalEqn;
    }

    public FunctionUnit()
    {
    }
    

    public abstract double evaluateAt(Argument[] args) throws EquationException;
    
    
   @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof FunctionUnit)
        {
            FunctionUnit fu = (FunctionUnit)obj;
            
            if (!fu.getName().equals(getName())) return false;
            if (!fu.internalEqn.equals(internalEqn)) return false;
            return true;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + getName().hashCode();
        hash = 53 * hash + internalEqn.hashCode();
        return hash;
    }


    public String getNiceString()
    {
        return getName() + "("+ internalEqn.getNiceString()+ ")";
    }

    public String toString()
    {
        return getNiceString();
    }

    public EquationUnit getInternalEqn()
    {
        return internalEqn;
    }

    public void setInternalEqn(EquationUnit internalEqn)
    {
        this.internalEqn = internalEqn;
    }
    
    @Override
    public void setName(String name)
    {
        super.setName(name);
    }




}
