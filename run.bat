@echo off

REM    
REM   Use an altered value with -Xmx in the line below to run the application with extra 
REM   memory; type java -X for more details. Choosing a max java heap size slightly less 
REM   than half your total physical memory is best. 
REM   *** ASKING FOR MORE MEMORY THAN THIS ON WINDOWS HAS LED TO THE APPLICATION CRASHING ***  
REM
REM   Note: the -Dsun.java2d.noddraw=true has been added to solve problems with excessive 
REM   flickering of the Swing components when showing 3D on some Windows systems
REM
REM   See note under lib\hdf5 if you're using a 64bit Windows machine
REM 


REM Change this line to your install location
set NC_HOME=C:\neuroConstruct

set NC_MAX_MEMORY=450M 



REM The rest of the settings below shouldn't have to change


set NC_VERSION=1.1.4

REM Determine 32bit or 64bit architecture for JDK
set JDK_ARCH=32
 
if [ %PROCESSOR_ARCHITEW6432% == "AMD64" ] (
    echo "Assuming using a 64bit JDK"
    set JDK_ARCH=64
)



REM Location of jars and native libraries for HDF5
set H5_DIR=%NC_HOME%/lib/hdf5
set H5_JARS=%H5_DIR%/jhdf.jar;%H5_DIR%/jhdf4obj.jar;%H5_DIR%/jhdf5.jar;%H5_DIR%/jhdf5obj.jar;%H5_DIR%/jhdfobj.jar


REM Location of jars and native libraries for Java 3D
set J3D_DIR=%NC_HOME%/lib/j3d
set J3D_JARS=%J3D_DIR%/j3dcore.jar;%J3D_DIR%/j3dutils.jar;%J3D_DIR%/vecmath.jar


set CLASSPATH=%NC_HOME%/neuroConstruct_%NC_VERSION%.jar;%H5_JARS%;%J3D_JARS%;%NC_HOME%/lib/jython/jython.jar


set JAVA_LIBRARY_PATH=%H5_DIR%/win%JDK_ARCH%;%J3D_DIR%/win%JDK_ARCH%


@echo on
java -Xmx%NC_MAX_MEMORY%  -Dsun.java2d.noddraw=true -cp %CLASSPATH% -Djava.library.path=%JAVA_LIBRARY_PATH% ucl.physiol.neuroconstruct.gui.MainApplication %1 %2 %3 %4 %5
