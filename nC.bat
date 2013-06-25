@echo off

REM    
REM   neuroConstruct run script for Windows
REM 
 


REM ##########################################################################

REM Change this line to your install location
REM Use quotes if there is a space in the directory name, e.g.
REM set NC_HOME="C:\Program Files\neuroConstruct_X.X.X"
set NC_HOME=C:\neuroConstruct

REM   Use an altered value in the line below to run the application with extra 
REM   memory; type java -X for more details. Choosing a max java heap size slightly less 
REM   than half your total physical memory is best. 
REM   *** ASKING FOR MORE MEMORY THAN HALF MAX ON WINDOWS HAS LED TO THE APPLICATION CRASHING ***  
set NC_MAX_MEMORY=500M 

set NC_VERSION=1.7.0
set LIB_NEUROML_VERSION=2.0.0
set LEMS_VERSION=0.8.3

set JNEUROML_VERSION=0.3.3

REM ##########################################################################


REM The rest of the settings below shouldn't have to change



if not exist %NC_HOME% goto WARN_UPDATE_PATHS


REM Determine 32bit or 64bit architecture for JDK
set JDK_ARCH=32
 

if /I %PROCESSOR_ARCHITECTURE% == AMD64  (
    echo Assuming using a 64bit JDK, processor architecture: %PROCESSOR_ARCHITECTURE%
    set JDK_ARCH=64
)



REM *** See note under lib\hdf5 if you're using a 64bit Windows machine ***
REM Location of jars and native libraries for HDF5
set H5_DIR=%NC_HOME%/lib/hdf5
set H5_JARS=%H5_DIR%/jhdf.jar;%H5_DIR%/jhdf4obj.jar;%H5_DIR%/jhdf5.jar;%H5_DIR%/jhdf5obj.jar;%H5_DIR%/jhdfobj.jar


REM Location of jars and native libraries for Java 3D
set J3D_DIR=%NC_HOME%/lib/j3d
set J3D_JARS=%J3D_DIR%/j3dcore.jar;%J3D_DIR%/j3dutils.jar;%J3D_DIR%/vecmath.jar

REM Location of jars for LEMS
set LIB_NEUROML_JAR=%NC_HOME%/lib/neuroml2/libNeuroML-%LIB_NEUROML_VERSION%.jar
set LEMS_JAR=%NC_HOME%/lib/neuroml2/lems-%LEMS_VERSION%.jar

set JNML_JAR=%NC_HOME%/jNeuroMLJar/jNeuroML-%JNEUROML_VERSION%-jar-with-dependencies.jar

set CLASSPATH=%NC_HOME%/neuroConstruct_%NC_VERSION%.jar;%H5_JARS%;%J3D_JARS%;%NC_HOME%/lib/jython/jython.jar;%LEMS_JAR%;%LIB_NEUROML_JAR%;%JNML_JAR%


set JAVA_LIBRARY_PATH=%H5_DIR%/win%JDK_ARCH%;%J3D_DIR%/win%JDK_ARCH%

if "%1"=="-make" (
	echo Building the application from source...
	mkdir classes
	@echo on
	javac  -sourcepath src -d classes -classpath %CLASSPATH%  %NC_HOME%/src/ucl/physiol/neuroconstruct/gui/MainApplication.java
	@echo off
	copy %NC_HOME%\src\ucl\physiol\neuroconstruct\gui\* %NC_HOME%\classes\ucl\physiol\neuroconstruct\gui  
  jar -cf neuroConstruct_%NC_VERSION%.jar -C classes .
  echo Have created neuroConstruct_%NC_VERSION%.jar
	goto END
)


REM   Note: the -Dsun.java2d.noddraw=true has been added to solve problems with excessive 
REM   flickering of the Swing components when showing 3D on some Windows systems
@echo on
java -Xmx%NC_MAX_MEMORY%  -Dsun.java2d.noddraw=true -cp %CLASSPATH% -Djava.library.path=%JAVA_LIBRARY_PATH% ucl.physiol.neuroconstruct.gui.MainApplication %1 %2 %3 %4 %5
@echo off

goto END



:WARN_UPDATE_PATHS
echo The path %NC_HOME% does not exist! Please set the variable NC_HOME to the correct neuroConstruct install location in nC.bat
goto END


:END
