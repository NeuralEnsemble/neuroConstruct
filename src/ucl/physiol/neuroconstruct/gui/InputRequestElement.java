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

package ucl.physiol.neuroconstruct.gui;



/**
 * Used to build generic input requests
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */


public class InputRequestElement
{
    private String ref = null;
    private String request = null;
    private String toolTip = null;
    private String value = null;
    private String units = null;

    public InputRequestElement(String ref, String request, String toolTip, String initialVal, String units)
    {
        this.ref = ref;
        this.request = request;
        this.toolTip = toolTip;
        this.value = initialVal;
        this.units = units;
    }

    public void setValue(String val)
    {
        this.value = val;
    }

    public String getRef()
    {
        return this.ref;
    }

    public String getRequest()
    {
        return this.request;
    }

    public String getToolTip()
    {
        return this.toolTip;
    }

    public String getValue()
    {
        return this.value;
    }
    public String getUnits()
    {
        return this.units;
    }





}
