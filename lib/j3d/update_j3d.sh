#!/bin/bash

echo "### Updating j3d jars"
echo "  Backing up current jars to j3d_jars_bkp.tgz"
tar czvf j3d_jars_bkp.tgz *.jar
rm j3dcore.jar j3dutils.jar vecmath.jar

echo "  Fetching the latest jogl jars from jogamp"
for f in *.jar; do curl http://jogamp.org/deployment/jogamp-current/jar/$f > $f; done

echo "  Fetching the latest j3d jars from jogamp. Hardcoded to version 1.6.0-pre9 as of Jan/2014"
for f in j3dcore.jar j3dutils.jar vecmath.jar; do curl http://jogamp.org/deployment/java3d/1.6.0-pre9/$f > $f; done
echo "  done."
exit
