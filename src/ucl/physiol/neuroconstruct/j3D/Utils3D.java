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

import java.text.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.*;
import com.sun.j3d.utils.geometry.*;
import ucl.physiol.neuroconstruct.utils.*;
import com.sun.j3d.utils.image.*;
import java.net.*;
import java.util.*;

/**
 * Some handy utilities for adding/printing 3D stuff
 *
 * @author Padraig Gleeson
 *  
 */


public class Utils3D
{

    static ClassLogger logger = new ClassLogger("Utils3D");

    public static final int POS_X_DIRECTION = 0;
    public static final int POS_Y_DIRECTION = 1;
    public static final int POS_Z_DIRECTION = 2;

    public static final int NEG_X_DIRECTION = 3;
    public static final int NEG_Y_DIRECTION = 4;
    public static final int NEG_Z_DIRECTION = 5;

    public static Color X_AXIS_COLOUR = Color.green;
    public static Color Y_AXIS_COLOUR = Color.yellow;
    public static Color Z_AXIS_COLOUR = Color.red;

    public static final Vector3f POS_X_AXIS = new Vector3f(1, 0, 0);
    public static final Vector3f POS_Y_AXIS = new Vector3f(0, 1, 0);
    public static final Vector3f POS_Z_AXIS = new Vector3f(0, 0, 1);
    public static final Vector3f NEG_X_AXIS = new Vector3f(-1, 0, 0);
    public static final Vector3f NEG_Y_AXIS = new Vector3f(0, -1, 0);
    public static final Vector3f NEG_Z_AXIS = new Vector3f(0, 0, -1);


    private static final Color3f black = new Color3f(0.1f, 0.1f, 0.1f);
    private static final Color3f white = new Color3f(1.0f, 1.0f, 1.0f);

    private static LinkedList<Appearance> cachedGenAppearances = new LinkedList<Appearance>();
    private static LinkedList<Color> cachedGenColours = new LinkedList<Color>();
    private static int cacheLimit = 6;


    public Utils3D()
    {
    }

    static
    {
        //logger.setThisClassSilent(true);
    }

    /**
     * Adds some handy axes for showing xyz dirs
     */
    public static void addAxes(TransformGroup tg, double scale)
    {
        Utils3D.printTransformGroupDetails(tg, "Adding axes to: ");
        float radius = 0.0013f/(float)scale;
        float length = 1f/(float)scale;

        /** @todo remove cyls here and use static cyl methods */


        addCylinderAtLocation(radius,
                             length,
                             4,
                             4,
                             POS_Y_DIRECTION,
                             new Vector3f(0,length/-2,0),
                             tg,
                             Utils3D.Y_AXIS_COLOUR);

        Transform3D toEndYAxis = new Transform3D();

        toEndYAxis.setTranslation(new Vector3d(0,length/2,0));
        TransformGroup toEndYAxisTG = new TransformGroup(toEndYAxis);

        Cone posYAxisCone = new Cone(radius*3, length/20,
                            Cone.GENERATE_NORMALS |
                            Cone.GENERATE_TEXTURE_COORDS, 4,4,
                            getGeneralObjectAppearance(Utils3D.Y_AXIS_COLOUR));

        tg.addChild(toEndYAxisTG);
        toEndYAxisTG.addChild(posYAxisCone);


        Transform3D toXAxis = new Transform3D();
        toXAxis.rotZ(-1*Math.PI/2d);

        TransformGroup xAxisTG = new TransformGroup(toXAxis);

        Cylinder xAxisCyl = new Cylinder(radius, length,
                            Cylinder.GENERATE_NORMALS |
                            Cylinder.GENERATE_TEXTURE_COORDS, 4,4,
                            getGeneralObjectAppearance(Utils3D.X_AXIS_COLOUR));

        tg.addChild(xAxisTG);
        xAxisTG.addChild(xAxisCyl);

        Transform3D toEndXAxis = new Transform3D();
        toEndXAxis.setTranslation(new Vector3d(0, length / 2, 0));
        TransformGroup toEndXAxisTG = new TransformGroup(toEndXAxis);

        Cone posXAxisCone = new Cone(radius * 3, length / 20,
                                     Cone.GENERATE_NORMALS |
                                     Cone.GENERATE_TEXTURE_COORDS, 4,4,
                                     getGeneralObjectAppearance(Utils3D.X_AXIS_COLOUR));

        xAxisTG.addChild(toEndXAxisTG);
        toEndXAxisTG.addChild(posXAxisCone);



        Transform3D toZAxis = new Transform3D();
        toZAxis.rotX(Math.PI/2d);

        TransformGroup zAxisTG = new TransformGroup(toZAxis);

        Cylinder zAxisCyl = new Cylinder(radius, length,
                            Cylinder.GENERATE_NORMALS |
                            Cylinder.GENERATE_TEXTURE_COORDS, 4,4,
                            getGeneralObjectAppearance(Utils3D.Z_AXIS_COLOUR));

        tg.addChild(zAxisTG);
        zAxisTG.addChild(zAxisCyl);


        Transform3D toEndZAxis = new Transform3D();
        toEndZAxis.setTranslation(new Vector3d(0, length / 2, 0));
        TransformGroup toEndZAxisTG = new TransformGroup(toEndZAxis);

        Cone posZAxisCone = new Cone(radius * 3, length / 20,
                                     Cone.GENERATE_NORMALS |
                                     Cone.GENERATE_TEXTURE_COORDS, 4,4,
                                     getGeneralObjectAppearance(Utils3D.Z_AXIS_COLOUR));

        zAxisTG.addChild(toEndZAxisTG);
        toEndZAxisTG.addChild(posZAxisCone);


        // add some ticks...

        float tickLength = 0.01f/(float)scale;

        LineArray xAxisTicks = new LineArray(40, GeometryArray.COORDINATES
                    | GeometryArray.COLOR_3);
        LineArray yAxisTicks = new LineArray(40, GeometryArray.COORDINATES
                    | GeometryArray.COLOR_3);
        LineArray zAxisTicks = new LineArray(40, GeometryArray.COORDINATES
                    | GeometryArray.COLOR_3);

        for (int i = 0; i < 10; i++)
        {
            xAxisTicks.setCoordinate(i*4, new Point3f(0,(length/20f)*(i+1),tickLength));
            xAxisTicks.setColor(i*4, new Color3f(X_AXIS_COLOUR.darker()));
            xAxisTicks.setCoordinate((i*4)+1, new Point3f(0,(length/20f)*(i+1),0));
            xAxisTicks.setColor((i*4)+1, new Color3f(X_AXIS_COLOUR.darker()));
            xAxisTicks.setCoordinate((i*4) +2, new Point3f(0,-1*(length/20f)*(i+1),tickLength));
            xAxisTicks.setColor((i*4) +2, new Color3f(X_AXIS_COLOUR.darker()));
            xAxisTicks.setCoordinate((i*4)+3, new Point3f(0,-1*(length/20f)*(i+1),0));
            xAxisTicks.setColor((i*4) +3, new Color3f(X_AXIS_COLOUR.darker()));

            yAxisTicks.setCoordinate(i*4, new Point3f((length/20f)*(i+1),0,tickLength));
            yAxisTicks.setColor(i*4, new Color3f(Y_AXIS_COLOUR.darker()));
            yAxisTicks.setCoordinate((i*4)+1, new Point3f((length/20f)*(i+1),0,0));
            yAxisTicks.setColor((i*4)+1, new Color3f(Y_AXIS_COLOUR.darker()));
            yAxisTicks.setCoordinate((i*4) +2, new Point3f(-1*(length/20f)*(i+1),0,tickLength));
            yAxisTicks.setColor((i*4) +2, new Color3f(Y_AXIS_COLOUR.darker()));
            yAxisTicks.setCoordinate((i*4)+3, new Point3f(-1*(length/20f)*(i+1),0,0));
            yAxisTicks.setColor((i*4) +3, new Color3f(Y_AXIS_COLOUR.darker()));


            zAxisTicks.setCoordinate(i*4, new Point3f(0,(length/20f)*(i+1),-1*tickLength));
            zAxisTicks.setColor(i*4, new Color3f(Z_AXIS_COLOUR.darker()));
            zAxisTicks.setCoordinate((i*4)+1, new Point3f(0,(length/20f)*(i+1),0));
            zAxisTicks.setColor((i*4)+1, new Color3f(Z_AXIS_COLOUR.darker()));
            zAxisTicks.setCoordinate((i*4) +2, new Point3f(0,-1*(length/20f)*(i+1),-1*tickLength));
            zAxisTicks.setColor((i*4) +2, new Color3f(Z_AXIS_COLOUR.darker()));
            zAxisTicks.setCoordinate((i*4)+3, new Point3f(0,-1*(length/20f)*(i+1),0));
            zAxisTicks.setColor((i*4) +3, new Color3f(Z_AXIS_COLOUR.darker()));




        }

        xAxisTicks.setCapability(LineArray.ALLOW_COLOR_WRITE);

        Shape3D stickShapeX = new Shape3D(xAxisTicks);
        xAxisTG.addChild(stickShapeX);

        yAxisTicks.setCapability(LineArray.ALLOW_COLOR_WRITE);

        Shape3D stickShapeY = new Shape3D(yAxisTicks);
        xAxisTG.addChild(stickShapeY);

        zAxisTicks.setCapability(LineArray.ALLOW_COLOR_WRITE);

        Shape3D stickShapeZ = new Shape3D(zAxisTicks);
        zAxisTG.addChild(stickShapeZ);



    }

    /**
     * Simple default coloured appearance
     */
    public static Appearance getGeneralObjectAppearance(Color c)
    {
        //if (true) return getDullObjectAppearance(c);

        if (cachedGenColours.contains(c))
        {

            int index = cachedGenColours.indexOf(c);
            //logger.logComment("Reusing app..", true);
            return cachedGenAppearances.get(index);
        }

        Appearance app = new Appearance();

        Color3f objColor = new Color3f(c);

        Material m = new Material(objColor, black, objColor, objColor, 80.0f);
        //m.setLightingEnable(false);
        m.setCapability(Material.ALLOW_COMPONENT_READ);
        app.setCapability(Appearance.ALLOW_MATERIAL_READ);
        app.setMaterial(m);

        //logger.logComment("NOT Reusing app..", true);

        if (cachedGenColours.size()>cacheLimit)
        {
            cachedGenColours.removeLast();
        }

        cachedGenColours.addFirst(c);
        cachedGenAppearances.addFirst(app);


        return app;
    }

    /**
     * Simple unshiny coloured appearance
     */
    public static Appearance getDullObjectAppearance(Color c)
    {
        Appearance app = new Appearance();

        Color3f objColor = new Color3f(c);

        Material m = new Material(objColor, objColor, objColor, objColor, 128f);
        //m.setLightingEnable(false);
        m.setCapability(Material.ALLOW_COMPONENT_READ);
        app.setCapability(Appearance.ALLOW_MATERIAL_READ);
        app.setMaterial(m);
        return app;
    }




    /**
     * Appearance for polygons
     */
    public static Appearance getPolygonAppearance(Color c)
    {
        Appearance app = new Appearance();
        Color3f objColor = new Color3f(c);

        Material m = new Material(objColor, black, objColor,
                                  black, 80.0f);

        app.setMaterial(m);
        m.setCapability(Material.ALLOW_COMPONENT_READ);
        app.setCapability(Appearance.ALLOW_MATERIAL_READ);
        app.setMaterial(m);
        return app;
    }


    public static AxisAngle4f getAxisAngle(Vector3f fromVector, Vector3f toVector)
    {
        AxisAngle4f angle4 = new AxisAngle4f();
        float angle = fromVector.angle(toVector);
        /*
                logger.logComment("Vector: "
                                  + Utils3D.getShortVectorForm(fromVector)
                                  + " is at an angle of "
                                  + Utils3D.angleDetails(angle)
                                  + " to "
                                  + Utils3D.getShortVectorForm(toVector));
         */


        // get a vector perpendicular to both

        Vector3f crossProd = new Vector3f();
        crossProd.cross(fromVector, toVector);

                logger.logComment("Cross product of "
                                  + Utils3D.getShortStringDesc(fromVector)
                                  + " and "
                                  + Utils3D.getShortStringDesc(toVector)
                                  + " is "
                                  + Utils3D.getShortStringDesc(crossProd));

        if (angle > 0f && angle < (float) Math.PI)
        {
            //If they're not parallel, get a rotation mapping one to the other
            angle4 = new AxisAngle4f(crossProd, (float) angle);
        }
        else if (angle == (float) Math.PI)
        {
            // case where angle = 180 deg
            angle4 = new AxisAngle4f(new Vector3f(1, 0, 0), angle);
        }
        return angle4;
    }





    /**
     * Simple default transparent appearance
     */
    public static Appearance getTransparentObjectAppearance(Color c, float transparency)
    {
        Appearance app = new Appearance();

        app.setCapability(Appearance.ALLOW_MATERIAL_READ);
        Color3f objColor = new Color3f(c);
        TransparencyAttributes ta = new TransparencyAttributes();
        ta.setTransparencyMode(TransparencyAttributes.BLENDED);
        ta.setTransparency(transparency);
        app.setTransparencyAttributes(ta);

        PolygonAttributes pa = new PolygonAttributes();
        pa.setCullFace(PolygonAttributes.CULL_NONE);
        app.setPolygonAttributes(pa);
        Material mat =  new Material(objColor, black, objColor, black, 1.0f);
        mat.setCapability(Material.ALLOW_COMPONENT_READ);
        app.setMaterial(mat);

        return app;
    }
    
    /*
     * Check if the given appearance is transparent.
     * TODO: get a quicker way of doing this..
     */
    public static boolean isTransparent(Appearance app)
    {
        if (app==null) return false;
        
        TransparencyAttributes ta = app.getTransparencyAttributes();
        if (ta==null) return false;
        if (ta.getTransparencyMode()==TransparencyAttributes.BLENDED)
            return true;
        else
            return false;
    }



    /**
     * Textured object appearance
     */
    public static Appearance getTexturedObjectAppearance(URL url, Component observer)
    {

        Appearance app = new Appearance();
        TextureLoader tex = new TextureLoader(url, observer);
        app.setTexture(tex.getTexture());

        TextureAttributes texAttr = new TextureAttributes();
        texAttr.setTextureMode(TextureAttributes.MODULATE);
        app.setTextureAttributes(texAttr);

        // Set up the material properties
        app.setMaterial(new Material(white, black, white, black, 1.0f));

        return app;
    }



    /**
     * Simple coloured background
     */
    public static void addBackground(BoundingSphere bounds, BranchGroup branchGroup, Color c)
    {
        Color3f bgColor = new Color3f(c);

        Background bg = new Background(bgColor);
        bg.setApplicationBounds(bounds);
        branchGroup.addChild(bg);
    }

    /**
     * Basic lights
     */
    public static void addBackgroundLights(BoundingSphere bounds, BranchGroup branchGroup)
    {
        // Set up the global lights
        Color3f lColor1 = new Color3f(0.8f, 0.8f, 0.8f);
        Vector3f lDir1 = new Vector3f( -1.0f, -1.0f, -1.0f);
        Color3f alColor = new Color3f(0.2f, 0.2f, 0.2f);


        AmbientLight aLgt = new AmbientLight(alColor);
        aLgt.setInfluencingBounds(bounds);
        DirectionalLight lgt1 = new DirectionalLight(lColor1, lDir1);
        lgt1.setInfluencingBounds(bounds);
        branchGroup.addChild(aLgt);
        branchGroup.addChild(lgt1);
    }

    /**
     * A grid in the X-Y plane a la 80's 3D stuff...
     */
    public static javax.media.j3d.Shape3D createLand(int numberOfLinesEachSide, float width)
    {
        int numberOfLinesTotal = numberOfLinesEachSide + 1 + numberOfLinesEachSide;
        int totalNumPoints = numberOfLinesTotal *4;

        float totalExtentFromOrigin = width * numberOfLinesEachSide;

        LineArray landGeom = new LineArray(totalNumPoints , GeometryArray.COORDINATES
                                            | GeometryArray.COLOR_3);

        float distanceFromOrigin = -1 * width * numberOfLinesEachSide;

        for(int c = 0; c < totalNumPoints; c+=4)
        {
            landGeom.setCoordinate( c+0, new Point3f( totalExtentFromOrigin, distanceFromOrigin, 0.0f));
            landGeom.setCoordinate( c+1, new Point3f( -1*totalExtentFromOrigin,  distanceFromOrigin, 0.0f ));
            landGeom.setCoordinate( c+2, new Point3f(   distanceFromOrigin   , -1*totalExtentFromOrigin, 0.0f ));
            landGeom.setCoordinate( c+3, new Point3f(   distanceFromOrigin  ,  totalExtentFromOrigin, 0.0f ));
            distanceFromOrigin+=width;
        }
        Color3f c = new Color3f(0.1f, 0.8f, 0.1f);

        for(int i = 0; i < totalNumPoints; i++)
            landGeom.setColor( i, c);

        return new javax.media.j3d.Shape3D(landGeom);
    }


    /**
     * Adds a box to the TG with its corner at the point specified
     */
    public static com.sun.j3d.utils.geometry.Box addBoxAtLocation(float xdim,
                                                                  float ydim,
                                                                  float zdim,
                                                                  Vector3f pos,
                                                                  TransformGroup tg,
                                                                  Appearance app)
    {
        com.sun.j3d.utils.geometry.Box box
             = new com.sun.j3d.utils.geometry.Box(xdim/2f,
                                                  ydim/2f,
                                                  zdim/2f,
                                                  Box.GENERATE_NORMALS,
                                                  app);

       Transform3D trans = new Transform3D();

       Vector3f transformedPos = new Vector3f(pos);
       transformedPos.add(new Vector3f(xdim/2f,ydim/2f,zdim/2f));

       trans.setTranslation(transformedPos);
       TransformGroup tgTranslated = new TransformGroup(trans);

       tgTranslated.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

       tgTranslated.addChild(box);
       tg.addChild(tgTranslated);

       return box;
   }


   /**
    * Adds a box to the TG with its corner at the point specified
    */
   public static com.sun.j3d.utils.geometry.Box addBoxAtLocation(float xdim, float ydim, float zdim, Vector3f pos, TransformGroup tg, Color color)
   {
      return addBoxAtLocation(xdim,
                              ydim,
                              zdim,
                              pos,
                              tg,
                              getGeneralObjectAppearance(color));
  }






   /**
    * Adds a sphere to the TG with its centre at the point specified
    */
   public static Sphere addSphereAtLocation(float radius, Vector3f pos, TransformGroup tg, Color color)
   {
       return addSphereAtLocation(radius,
                                  pos,
                                  tg,
                                  getGeneralObjectAppearance(color));
   }



  /**
   * Adds a sphere to the TG with its centre at the point specified
   */
  public static Sphere addSphereAtLocation(float radius, Vector3f pos, TransformGroup tg, Appearance app)
  {
      Sphere sphere = new Sphere(radius,
                                      Sphere.GENERATE_NORMALS |
                                      Sphere.GENERATE_TEXTURE_COORDS |
                                      Sphere.ENABLE_APPEARANCE_MODIFY, 30,
                                      app);


     Transform3D trans = new Transform3D();

     trans.setTranslation(pos);
     TransformGroup tgTranslated = new TransformGroup(trans);

     tgTranslated.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
     tgTranslated.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

     tgTranslated.addChild(sphere);
     tg.addChild(tgTranslated);

     return sphere;
 }


  /**
   * Adds a cylinder to the TG with one end at the point specified
   */
    public static Cylinder addCylinderAtLocation(float radius,
                                                 float height,
                                                 int direction,
                                                 Vector3f pos,
                                                 TransformGroup tg,
                                                 Color color)
    {
        return addCylinderAtLocation(radius,
                                     height,
                                     30,
                                     30,
                                     direction,
                                     pos,
                                     tg,
                                     color);

    }

    /**
     * Adds a cone to the TG with ends specified
     */
    public static Cone addCone(float baseRadius,
                               Point3f startPos,
                               Point3f endPos,
                               int xDiv,
                               int yDiv,
                               TransformGroup tg,
                               Appearance app)
    {
        float height = startPos.distance(endPos);

        Vector3f pos = new Vector3f(startPos);

        logger.logComment("Base pos: "+ pos);

        Cone cone = new Cone(baseRadius,
                                    height,
                                    Sphere.GENERATE_NORMALS |
                                    Sphere.GENERATE_TEXTURE_COORDS |
                                    Sphere.ENABLE_APPEARANCE_MODIFY, xDiv, yDiv,
                                    app);

        Transform3D transAlong = new Transform3D();
        Transform3D transRot = new Transform3D();

        Vector3f halfAlong = new Vector3f(0,height / 2f, 0);

        transAlong.setTranslation(halfAlong);
        TransformGroup tgAlong = new TransformGroup(transAlong);


        Vector3f norm = new Vector3f(endPos.x - startPos.x,
                                     endPos.y - startPos.y,
                                     endPos.z - startPos.z);

        norm.normalize();
        logger.logComment("Norm: "+ norm);


        Vector3f yAxis = new Vector3f(0, 1, 0);

        AxisAngle4f angle4 = Utils3D.getAxisAngle(yAxis, norm);

        //System.out.println("angle4: "+ angle4);

        transRot.setRotation(angle4);

        TransformGroup tgRot = new TransformGroup(transRot);

        Transform3D transToStart = new Transform3D();

        transToStart.setTranslation(pos);

        TransformGroup tgTranslated = new TransformGroup(transToStart);


        tgTranslated.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgTranslated.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);



        tgAlong.addChild(cone);
        tgRot.addChild(tgAlong);
        tgTranslated.addChild(tgRot);
        tg.addChild(tgTranslated);

        return cone;

    }





    /**
     * Adds a cylinder to the TG with one end at the point specified
     */
    public static Cylinder addCylinder(float radius,
                                                 Point3f startPos,
                                                 Point3f endPos,
                                                 int xDiv,
                                                 int yDiv,
                                                 TransformGroup tg,
                                                 Appearance app)
    {
        float height = startPos.distance(endPos);

        Vector3f pos = new Vector3f(startPos);

        logger.logComment("Start pos: "+ pos);

        Cylinder cyl = new Cylinder(radius,
                                    height,
                                    Sphere.GENERATE_NORMALS |
                                    Sphere.GENERATE_TEXTURE_COORDS |
                                    Sphere.ENABLE_APPEARANCE_MODIFY, xDiv, yDiv,
                                    app);

        Transform3D transAlong = new Transform3D();
        Transform3D transRot = new Transform3D();

        Vector3f halfAlong = new Vector3f(0,height / 2f, 0);

        transAlong.setTranslation(halfAlong);
        TransformGroup tgAlong = new TransformGroup(transAlong);


        Vector3f norm = new Vector3f(endPos.x - startPos.x,
                                     endPos.y - startPos.y,
                                     endPos.z - startPos.z);

        norm.normalize();
        logger.logComment("Norm: "+ norm);


        Vector3f yAxis = new Vector3f(0, 1, 0);

        AxisAngle4f angle4 = Utils3D.getAxisAngle(yAxis, norm);


        //System.out.println("angle4: "+ angle4);

        transRot.setRotation(angle4);

        TransformGroup tgRot = new TransformGroup(transRot);

        Transform3D transToStart = new Transform3D();

        transToStart.setTranslation(pos);

        TransformGroup tgTranslated = new TransformGroup(transToStart);


        tgTranslated.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgTranslated.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);



        tgAlong.addChild(cyl);
        tgRot.addChild(tgAlong);
        tgTranslated.addChild(tgRot);
        tg.addChild(tgTranslated);

        return cyl;

    }








    /**
     * Adds a cylinder to the TG with one end at the point specified
     */
    public static Cylinder addCylinderAtLocation(float radius,
                                                 float height,
                                                 int xDiv,
                                                 int yDiv,
                                                 int direction,
                                                 Vector3f pos,
                                                 TransformGroup tg,
                                                 Color color)
    {
        Cylinder cyl = new Cylinder(radius,
                                    height,
                                    Sphere.GENERATE_NORMALS |
                                    Sphere.GENERATE_TEXTURE_COORDS |
                                    Sphere.ENABLE_APPEARANCE_MODIFY, xDiv, yDiv,
                                    getGeneralObjectAppearance(color));

        Transform3D trans1 = new Transform3D();
        switch (direction)
        {
            case (POS_X_DIRECTION):
                trans1.setRotation(new AxisAngle4f(POS_Z_AXIS, (float)Math.PI/ -2));
                pos.add(new Vector3f(height/2f, 0,0));
                trans1.setTranslation(pos);
                break;
            case (POS_Y_DIRECTION):
                // leave it unrotated...
                pos.add(new Vector3f(0, height/2f,0));
                trans1.setTranslation(pos);
                break;
            case (POS_Z_DIRECTION):
                trans1.setRotation(new AxisAngle4f(POS_X_AXIS, (float)Math.PI / 2));
                pos.add(new Vector3f(0,0,height/2f));
                trans1.setTranslation(pos);
                break;
            case (NEG_X_DIRECTION):
                trans1.setRotation(new AxisAngle4f(POS_X_AXIS, (float)Math.PI/ 2));
                pos.add(new Vector3f(height/-2f, 0,0));
                trans1.setTranslation(pos);
                break;
            case (NEG_Y_DIRECTION):
                trans1.setRotation(new AxisAngle4f(POS_Z_AXIS, (float)Math.PI));
                pos.add(new Vector3f(0, height/-2f,0));
                trans1.setTranslation(pos);

                break;
            case (NEG_Z_DIRECTION):
                trans1.setRotation(new AxisAngle4f(POS_X_AXIS, (float)Math.PI / -2));
                pos.add(new Vector3f(0,0,height/-2f));
                trans1.setTranslation(pos);
                break;

        }


        TransformGroup tgTranslated = new TransformGroup(trans1);

        tgTranslated.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgTranslated.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

        tgTranslated.addChild(cyl);

        tg.addChild(tgTranslated);

        return cyl;
    }






    public static void printTransformGroupDetails(TransformGroup tg, String name)
    {
        Transform3D transPoint = new Transform3D();
        tg.getTransform(transPoint);
        printTransform3DDetails(transPoint, name);
    }

    public static void printTransform3DDetails(Transform3D t3d, String name)
    {
        Point3d origin = new Point3d(0, 0, 0);
        Vector3d xAxis = new Vector3d(1, 0, 0);
        Vector3d yAxis = new Vector3d(0, 1, 0);
        Vector3d zAxis = new Vector3d(0, 0, 1);
        t3d.transform(origin);
        t3d.transform(xAxis);
        t3d.transform(yAxis);
        t3d.transform(zAxis);

        logger.logComment("Transform: " + name + " maps O -> " + getShortStringDesc(origin) +
                          ", X -> " + getShortStringDesc(xAxis) + ", Y -> " +
                          getShortStringDesc(yAxis) + ", Z -> " + getShortStringDesc(zAxis));
   }

   public static String getShortStringDesc(Tuple3d tuple)
   {
       if (tuple==null) return "(-- null --)";
       return "("+trimDouble(tuple.x)+", "+trimDouble(tuple.y)+", "+trimDouble(tuple.z)+")";
   }


   public static String getShortStringDesc(Tuple3f tuple)
   {
       if (tuple==null) return "(-- null --)";
       return "("+trimDouble(tuple.x)+", "+trimDouble(tuple.y)+", "+trimDouble(tuple.z)+")";
   }

   public static String getShortStringDesc(Tuple3d tuple, int numDecimal)
   {
       if (tuple==null) return "(-- null --)";
       return "("+trimDouble(tuple.x, numDecimal)+", "
           +trimDouble(tuple.y, numDecimal)
           +", "+trimDouble(tuple.z, numDecimal)+")";
   }


   public static String getShortStringDesc(Tuple3f tuple, int numDecimal)
   {
       if (tuple==null) return "(-- null --)";
       return "("+trimDouble(tuple.x, numDecimal)+", "
           +trimDouble(tuple.y, numDecimal)+", "
           +trimDouble(tuple.z, numDecimal)+")";
   }




    public static String trimDouble(double val)
    {
        return trimDouble(val, 3);
    }

    public static String trimDouble(double val, int numDecimal)
    {
        if (val >= Double.MAX_VALUE) return "Infinity";

        if (numDecimal <=0) return "" + val;

        String newVal = null;

        try
        {
            //DecimalFormat firstFormatter = new DecimalFormat("0.########E0");

            StringBuffer formatString = new StringBuffer("0.");

            for (int i = 0; i < numDecimal-1; i++)
            {
                formatString.append("#");
            }
            formatString.append("E0");

            DecimalFormat firstFormatter = new DecimalFormat(formatString.toString());
            String firstString = firstFormatter.format(val);

            //logger.logComment("firstString:"+ firstString+ ", format: "+ formatString);

            String sigDigits = firstString.substring(0, firstString.indexOf("E"));


            String exp = firstString.substring(firstString.indexOf("E") + 1);


            if (numDecimal > 1) numDecimal = numDecimal + 1; // for the decimal point

            if (sigDigits.indexOf(".") < 0) sigDigits = sigDigits + ".0";

            for (int i = 0; i < numDecimal; i++) {
                sigDigits = sigDigits + "0"; // not all of these may be needed...
            }

            newVal = sigDigits.substring(0, numDecimal) + "E" + exp;

            double newValDouble = firstFormatter.parse(newVal).doubleValue();

            String numToReturn = newValDouble+"";

            if (numToReturn.endsWith(".0")) numToReturn = numToReturn.substring(0,numToReturn.length()-2);

            return numToReturn;
        }
        catch (ParseException pe)
        {
            return new String("Problem parsing: ("+val+") converted into ("+newVal+")");
        }
        catch (Exception e)
        {
            return new String("Unknown value:("+val+")");
        }




    }



    public static String angleDetails(double angle)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(trimDouble(Math.toDegrees(angle)) + " degrees, ");
        sb.append(trimDouble(angle) +" radians (= PI/"+trimDouble(Math.PI/angle)+") ");
        return sb.toString();
    }


    public static boolean checkIntersectCylinderSphere(Point3f sphereLoc,
                                                       float sphereRadius,
                                                       Point3f cylinderStartLoc,
                                                       Point3f cylinderEndLoc,
                                                       float cylinderRadius)
    {

        //logger.setThisClassSilent(false);

        logger.logComment("Checking whether cylinder, radius: "+ cylinderRadius
                          + " from "+cylinderStartLoc
                          + " to "+ cylinderEndLoc
                          + " collides with sphere radius: "+ sphereRadius
                          + " at " + sphereLoc);

        float cylinderLength = cylinderStartLoc.distance(cylinderEndLoc);

        // first, move relative positions to origin...

        Point3f translatedCylEndPointLocation = new Point3f(cylinderEndLoc);
        translatedCylEndPointLocation.sub(cylinderStartLoc);
        Vector3f cylinderVectorFromTheOrigin = new Vector3f(translatedCylEndPointLocation);
        Vector3f normalCylinderVector = new Vector3f(cylinderVectorFromTheOrigin);
        normalCylinderVector.normalize();

        logger.logComment("cylinderVectorFromTheOrigin: "+ cylinderVectorFromTheOrigin);

        Point3f translatedSphereLoc = new Point3f(sphereLoc);
        translatedSphereLoc.sub(cylinderStartLoc);
        Vector3f translatedSpherePositionVector = new Vector3f(translatedSphereLoc);


        logger.logComment("translatedSpherePositionVector: "+ translatedSpherePositionVector);

        // case: if sphere is (sphereRadius + cylinderRadius) away in perp. direction from
        // cylinderVectorFromTheOrigin, then they definitely don't intersect

        Vector3f crossProduct = new Vector3f();
        crossProduct.cross(translatedSpherePositionVector, normalCylinderVector);
        float perpendicularDistFromVector = crossProduct.length();


        logger.logComment("Perp Distance is "
                           + perpendicularDistFromVector);

        if (perpendicularDistFromVector > sphereRadius + cylinderRadius)
        {
            logger.logComment("Sphere is too far from vector parallel to cylinder...");
            return false;
        }
        else
        {
           logger.logComment("Sphere is close to vector parallel to cylinder...");
        }

        float lengthAlongUnitVect = normalCylinderVector.dot(translatedSpherePositionVector);

        if (lengthAlongUnitVect<(-1*sphereRadius))
        {
            logger.logComment("Behind cylinder");
            return false;
        }
        else if (lengthAlongUnitVect>(sphereRadius + cylinderLength))
        {
            logger.logComment("Ahead of cylinder");
            return false;
        }
        return true;

    }

    private static void testDoubleTrim(double d)
    {
        logger.logComment("Original: "+ d);
        logger.logComment("Trimmed: "+ trimDouble(d) + " (value: "+ Double.parseDouble(trimDouble(d))+ ")");
        logger.logComment("Trimmed 1: "+ trimDouble(d, 1)+ " (value: "+ Double.parseDouble(trimDouble(d,1))+ ")");
        logger.logComment("Trimmed 10: "+ trimDouble(d, 10)+ " (value: "+ Double.parseDouble(trimDouble(d,10))+ ")");
    }

    public static void main(String[] args)
    {



        System.out.println(angleDetails(180));
        System.out.println(angleDetails(1.570796326794f));


        System.out.println("Result don't: "
            + Utils3D.checkIntersectCylinderSphere(new Point3f(2,0,0),
                                                   1,
                                                   new Point3f(0,0,0),
                                                   new Point3f(0,0,1),
                                                   .9f));

         System.out.println("-------------------------------------");


/*
        System.out.println("Result: "
                           + Utils3D.checkIntersectCylinderSphere(new Point3f(0,1,-0.1f),
                                                   1,
                                                   new Point3f(0,0,0),
                                                   new Point3f(0,0,1),
                                                   1f));

        System.out.println("-------------------------------------");

        System.out.println("Result: "
                              + Utils3D.checkIntersectCylinderSphere(new Point3f(0,0,3.1f),
                                                      1,
                                                      new Point3f(0,0,0),
                                                      new Point3f(0,0,1),
                                                      1f));
*/
testDoubleTrim(9.1234567);
testDoubleTrim(900.1234567);
testDoubleTrim(1230.0000112345678);

    }



}
