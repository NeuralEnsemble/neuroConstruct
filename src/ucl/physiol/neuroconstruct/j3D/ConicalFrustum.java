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

import com.sun.j3d.utils.geometry.*;
import javax.vecmath.*;
import javax.media.j3d.*;

/**
 * For creating a Primitive shape in the form of a truncated cone
 *
 * @author Padraig Gleeson
 *  
 */


public class ConicalFrustum extends Primitive
{
    float startRadius;
    float endRadius;
    float height;
    int divisions;

    static final int DEFAULT_DIVISIONS = 15;


    public static final int BODY = 0;
    public static final int TOP = 1;
    public static final int BOTTOM = 2;

    private static final Vector3f topDiskNormal = new Vector3f(0,1,0);
    private static final Vector3f bottomDiskNormal = new Vector3f(0,-1,0);


    /**
     * Primitive flags.
     */
    int flags;
    /**
     * Returns the flags of primitive (generate normal, textures, caching, etc).
     */

    @Override
    public int getPrimitiveFlags()
    {
      return flags;
    }



    public ConicalFrustum()
    {
        this(1.0f,0.5f, 2.0f, GENERATE_NORMALS, DEFAULT_DIVISIONS, null);
    }


    public ConicalFrustum (float startRadius, float endRadius, float height)
    {
        this(startRadius, endRadius, height, GENERATE_NORMALS, DEFAULT_DIVISIONS, null);
    }


    public ConicalFrustum (float startRadius, float endRadius, float height, Appearance ap)
    {
        this(startRadius, endRadius, height, GENERATE_NORMALS, DEFAULT_DIVISIONS, ap);
    }

    public ConicalFrustum (float startRadius, float endRadius, float height, int primflags, Appearance ap)
    {
        this(startRadius, endRadius, height, primflags, DEFAULT_DIVISIONS, ap);
    }


    public Shape3D getShape(int partId)
    {
        if (partId > BOTTOM || partId < BODY) return null;
        return (Shape3D) getChild(partId);
    }


    public void setAppearance(Appearance ap)
    {
        ((Shape3D)getChild(BODY)).setAppearance(ap);
        ((Shape3D)getChild(TOP)).setAppearance(ap);
        ((Shape3D)getChild(BOTTOM)).setAppearance(ap);
    }


    public Appearance getAppearance(int partId)
    {
        if (partId > BOTTOM || partId < BODY)return null;
        return getShape(partId).getAppearance();
    }



    public ConicalFrustum(float startRadius, float endRadius, float height, int primflags,
            int divisions, Appearance ap)
    {
      super();
      this.flags = primflags;

      this.startRadius = startRadius;
      this.endRadius = endRadius;
      this.height = height;
      this.divisions = divisions;

      Point3f[] bodyVerts = new Point3f[divisions*2*3]; // 2 for double triangle, 3 for verts on each triangle

      Point3f[] topVerts = new Point3f[divisions +2];
      Point3f[] bottomVerts = new Point3f[divisions +2];


      Point3f topCentre = new Point3f(0,height/2f,0);
      Point3f bottomCentre = new Point3f(0,height/-2f,0);

      topVerts[0] = topCentre;
      bottomVerts[0] = bottomCentre;

      Shape3D shape[] = new Shape3D[3];

      double angleToMove = 2.0 * Math.PI / divisions;

      for (int i = 0; i <= divisions; i++)
      {
          double angle = i * angleToMove;

          double sin = Math.sin(angle);
          double cos = Math.cos(angle);

          double sinNext = Math.sin(angle + angleToMove);
          double cosNext = Math.cos(angle + angleToMove);


          Point3f topVertForDisk = new Point3f(topCentre.x -(float)(endRadius*cos),
                                      topCentre.y,
                                      topCentre.z +(float)(endRadius*sin));

          Point3f bottomVertForDisk = new Point3f(bottomCentre.x -(float)(startRadius*cos),
                                         bottomCentre.y,
                                         bottomCentre.z -(float)(startRadius*sin));

          Point3f firstTopVertexForBody = topVertForDisk;

          Point3f firstBottomVertexForBody = new Point3f(bottomCentre.x -(float)(startRadius*cos),
                                                         bottomCentre.y,
                                                         bottomCentre.z +(float)(startRadius*sin));



          Point3f secondTopVertexForBody = new Point3f(topCentre.x -(float)(endRadius*cosNext),
                                topCentre.y,
                                topCentre.z +(float)(endRadius*sinNext));

          Point3f secondBottomVertexForBody = new Point3f(bottomCentre.x -(float)(startRadius*cosNext),
                                   bottomCentre.y,
                                   bottomCentre.z +(float)(startRadius*sinNext));



          if (i < divisions)
          {
              // Adding 2 triangs...

              bodyVerts[(i*6)] = firstTopVertexForBody;
              bodyVerts[(i*6)+1] = firstBottomVertexForBody;
              bodyVerts[(i*6)+2] = secondBottomVertexForBody;

              bodyVerts[(i*6)+3] = secondTopVertexForBody;
              bodyVerts[(i*6)+4] = firstTopVertexForBody;
              bodyVerts[(i*6)+5] = secondBottomVertexForBody;
          }

          topVerts[i+1] = topVertForDisk;
          bottomVerts[i+1] = bottomVertForDisk;

      }


      TriangleFanArray triFanArrayTop = new TriangleFanArray(divisions + 2,
          TriangleFanArray.COORDINATES |
          TriangleFanArray.NORMALS,
          new int[]{divisions + 2});

      TriangleFanArray triFanArrayBottom = new TriangleFanArray(divisions + 2,
          TriangleFanArray.COORDINATES |
          TriangleFanArray.NORMALS,
          new int[]{divisions + 2});

      TriangleArray bodyTriArray
          = new TriangleArray(divisions*2*3,
                              TriangleArray.COORDINATES |
                              TriangleArray.NORMALS |
                              TriangleArray.TEXTURE_COORDINATE_2);

      bodyTriArray.setCoordinates(0, bodyVerts);
      triFanArrayTop.setCoordinates(0, topVerts);
      triFanArrayBottom.setCoordinates(0, bottomVerts);


      for (int triangFan = 0; triangFan < divisions + 2; triangFan++)
      {
          triFanArrayTop.setNormal(triangFan, topDiskNormal);
          triFanArrayBottom.setNormal(triangFan, bottomDiskNormal);

      }

        Vector3f normal = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Point3f[] points = new Point3f[3];

        for (int i = 0; i < 3; i++)
        {
            points[i] = new Point3f();
        }


        for (int triangleNum = 0; triangleNum < divisions*2; triangleNum++)
        {
            bodyTriArray.getCoordinates(triangleNum*3, points); // as 0 is the centre
            /*
            System.out.println("Coords of triangle "+triangleNum+": ");
            System.out.println(Utils3D.getShortStringDesc(points[0]) + " ->"+
                               Utils3D.getShortStringDesc(points[1]) + " ->"+
                               Utils3D.getShortStringDesc(points[2]));
            */
            v1.sub(points[1], points[0]);
            v2.sub(points[2], points[0]);

            normal.cross(v1, v2);
            normal.normalize();
            for (int i = 0; i < 3; i++)
            {
                bodyTriArray.setNormal( (triangleNum * 3 + i), normal);
            }
        }



      shape[BODY] = new Shape3D(bodyTriArray);
      this.addChild(shape[BODY]);

      if ( (flags & ENABLE_APPEARANCE_MODIFY) != 0)
      {
          (shape[BODY]).setCapability(Shape3D.ALLOW_APPEARANCE_READ);
          (shape[BODY]).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
      }

      if ( (flags & ENABLE_GEOMETRY_PICKING) != 0)
      {
          (shape[BODY]).setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      }


      shape[TOP] = new Shape3D(triFanArrayTop);
      this.addChild(shape[TOP]);

      if ( (flags & ENABLE_APPEARANCE_MODIFY) != 0)
      {
          (shape[TOP]).setCapability(Shape3D.ALLOW_APPEARANCE_READ);
          (shape[TOP]).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
      }

      if ( (flags & ENABLE_GEOMETRY_PICKING) != 0)
      {
          (shape[TOP]).setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      }


      shape[BOTTOM] = new Shape3D(triFanArrayBottom);
      this.addChild(shape[BOTTOM]);

      if ( (flags & ENABLE_APPEARANCE_MODIFY) != 0)
      {
          (shape[BOTTOM]).setCapability(Shape3D.ALLOW_APPEARANCE_READ);
          (shape[BOTTOM]).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
      }

      if ( (flags & ENABLE_GEOMETRY_PICKING) != 0)
      {
          (shape[BOTTOM]).setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      }


      /*

      /*
      flags = primflags;
      boolean outside = (flags & GENERATE_NORMALS_INWARD) == 0;

      // Create many body of the cylinder.
      Quadrics q = new Quadrics();
      GeomBuffer gbuf = null;
      Shape3D shape[] = new Shape3D[3];

      GeomBuffer cache = getCachedGeometry(Primitive.CYLINDER,
                          (float)BODY, radius, height,
                          xdivision, ydivision, primflags);

      if (cache != null){
// 	  System.out.println("using cached geometry");
    shape[BODY] = new Shape3D(cache.getComputedGeometry());
    numVerts += cache.getNumVerts();
    numTris += cache.getNumTris();
      }
      else {
      gbuf = q.cylinder((double)height, (double)radius,
                xdivision, ydivision, outside);
      shape[BODY] = new Shape3D(gbuf.getGeom(flags));
      numVerts += gbuf.getNumVerts();
      numTris += gbuf.getNumTris();
      if ((primflags & Primitive.GEOMETRY_NOT_SHARED) == 0)
          cacheGeometry(Primitive.CYLINDER,
                (float)BODY, radius, height,
                xdivision, ydivision, primflags, gbuf);
      }

      if ((flags & ENABLE_APPEARANCE_MODIFY) != 0) {
      (shape[BODY]).setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      (shape[BODY]).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
      }

      if ((flags & ENABLE_GEOMETRY_PICKING) != 0) {
          (shape[BODY]).setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      }

      this.addChild(shape[BODY]);

      // Create top of cylinder
      cache = getCachedGeometry(Primitive.TOP_DISK, radius, radius,
                height/2.0f, xdivision, xdivision, primflags);
      if (cache != null) {
// 	  System.out.println("using cached top");
      shape[TOP] = new Shape3D(cache.getComputedGeometry());
      numVerts += cache.getNumVerts();
      numTris += cache.getNumTris();
      }
      else {
      gbuf = q.disk((double)radius, xdivision, height/2.0,
            outside);
      shape[TOP] = new Shape3D(gbuf.getGeom(flags));
      numVerts += gbuf.getNumVerts();
      numTris += gbuf.getNumTris();
      if ((primflags & Primitive.GEOMETRY_NOT_SHARED) == 0) {
          cacheGeometry(Primitive.TOP_DISK, radius, radius,
                height/2.0f, xdivision, xdivision,
                primflags, gbuf);
      }
      }

      if ((flags & ENABLE_APPEARANCE_MODIFY) != 0) {
      (shape[TOP]).setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      (shape[TOP]).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
      }

      if ((flags & ENABLE_GEOMETRY_PICKING) != 0) {
          (shape[TOP]).setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      }

      this.addChild(shape[TOP]);

      // Create bottom
      cache = getCachedGeometry(Primitive.BOTTOM_DISK, radius, radius,
                -height/2.0f, xdivision, xdivision,
                primflags);
      if (cache != null) {
// 	  System.out.println("using cached bottom");
      shape[BOTTOM] = new Shape3D(cache.getComputedGeometry());
      numVerts += cache.getNumVerts();
      numTris += cache.getNumTris();
      }
      else {
      gbuf = q.disk((double)radius, xdivision, -height/2.0, !outside);
      shape[BOTTOM] = new Shape3D(gbuf.getGeom(flags));
      numVerts += gbuf.getNumVerts();
      numTris += gbuf.getNumTris();
      if ((primflags & Primitive.GEOMETRY_NOT_SHARED) == 0) {
          cacheGeometry(Primitive.BOTTOM_DISK, radius, radius,
                -height/2.0f, xdivision, xdivision,
                primflags, gbuf);
      }
      }

      if ((flags & ENABLE_APPEARANCE_MODIFY) != 0) {
      (shape[BOTTOM]).setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      (shape[BOTTOM]).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
      }

      if ((flags & ENABLE_GEOMETRY_PICKING) != 0) {
          (shape[BOTTOM]).setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      }

      this.addChild(shape[BOTTOM]);
*/
// Set Appearance
      if (ap == null)
      {
          setAppearance();
      }
      else setAppearance(ap);
 }


    public Node cloneNode(boolean forceDuplicate) {
        ConicalFrustum c = new ConicalFrustum(startRadius, endRadius, height, flags, divisions, getAppearance());
        c.duplicateNode(this, forceDuplicate);
        return c;
    }


    public void duplicateNode(Node originalNode, boolean forceDuplicate) {
        super.duplicateNode(originalNode, forceDuplicate);
    }

    public float getStartRadius() {
    return startRadius;
    }

    public float getEndRadius() {
    return startRadius;
    }

    /**
     * Returns the height of the cylinder
     *
     * @since Java 3D 1.2.1
     */
    public float getHeight() {
    return height;
    }
}
