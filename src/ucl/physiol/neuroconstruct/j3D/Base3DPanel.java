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

package ucl.physiol.neuroconstruct.j3D;

import javax.media.j3d.*;

import java.awt.*;

import com.sun.j3d.utils.geometry.*;

/**
 * Base class for 3D panels. Note: it's not abstract, simply because the base classes in
 * couldn't be opened in JBuilder if it was...
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class Base3DPanel extends Panel
{
    /**
     * A description of the object viewed (or the object itself)
     * The imp of this should be sufficient so the super class constructor can be called
     * with what's returned from this e.g. new OneCell3DPanel(cell, activeProject, this);
     */
    private Object viewedObject = null;

    public Base3DPanel(){};

    public Transform3D getLastViewingTransform3D(){return null;};

    public void setLastViewingTransform3D(Transform3D lastViewingTransform3D){};

    public void refresh3D(){};


    public void destroy3D(){};


    public void markPrimitiveAsSelected(Primitive prim){};

    public Object getViewedObject()
    {
        return viewedObject;
    };

    public void setViewedObject(Object vo)
    {
        this.viewedObject = vo;
    };


}
