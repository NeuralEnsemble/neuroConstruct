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

set CLASSPATH=neuroConstruct_1.0.9.jar;lib/hdf5/jhdf.jar;lib/hdf5/jhdf4obj.jar;lib/hdf5/jhdf5.jar;lib/hdf5/jhdf5obj.jar;lib/hdf5/jhdfobj.jar
set JAVA_LIBRARY_PATH=lib/hdf5/win

@echo on
java -Xmx700M  -Dsun.java2d.noddraw=true -cp %CLASSPATH% -Djava.library.path=%JAVA_LIBRARY_PATH% ucl.physiol.neuroconstruct.gui.MainApplication %1 %2 %3 %4 %5
