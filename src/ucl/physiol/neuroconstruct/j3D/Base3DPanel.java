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

package ucl.physiol.neuroconstruct.j3D;

import javax.media.j3d.*;

import java.awt.*;

import com.sun.j3d.utils.geometry.*;

/**
 * Base class for 3D panels. Note: it's not abstract, simply because the base classes in
 * couldn't be opened in JBuilder if it was...
 *
 * @author Padraig Gleeson
 * @version 1.0.6
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
