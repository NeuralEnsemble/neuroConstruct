@echo off

REM    
REM   Use an altered value with -Xmx in the line below to run the application with extra 
REM   memory; type java -X for more details. Choosing a max java heap size of about half 
REM   your total physical memory is best, errors have been thrown (when 3D view is shown)
REM   when the application is run with more than this.
REM
REM   Note: the -Dsun.java2d.noddraw=true has been added to solve problems with excessive 
REM   flickering of the Swing components when showing 3D on some Windows systems
REM 


REM Change this line to your install location
set NC_HOME=C:\neuroConstruct

set H5_JAR_DIR=%NC_HOME%/lib/hdf5
set H5_JARS=%H5_JAR_DIR%/jhdf.jar;%H5_JAR_DIR%/jhdf4obj.jar;%H5_JAR_DIR%/jhdf5.jar;%H5_JAR_DIR%/jhdf5obj.jar;%H5_JAR_DIR%/jhdfobj.jar

set CLASSPATH=%NC_HOME%/neuroConstruct_1.1.0.jar;%H5_JARS%;%NC_HOME%/lib/jython/jython.jar
set JAVA_LIBRARY_PATH=%H5_JAR_DIR%/win

@echo on
java -Xmx700M  -Dsun.java2d.noddraw=true -cp %CLASSPATH% -Djava.library.path=%JAVA_LIBRARY_PATH% ucl.physiol.neuroconstruct.gui.MainApplication %1 %2 %3 %4 %5
