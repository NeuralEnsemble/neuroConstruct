@echo off

REM    
REM   Use an altered version of the line below to run the application with extra memory; type java -X for more..
REM   Choosing a max java heap size of about half your total physical memory is best, errors have been thrown 
REM   (when 3D view is shown) when app is run with more than this.
REM
REM   Note: if there are problems with excessive flickering when showing 3D, try updating the drivers for your
REM   graphics card, or adding -Dsun.java2d.noddraw=true to the command below.
REM 

@echo on
java -Xmx700M  -cp neuroConstruct_1.0.6.jar ucl.physiol.neuroconstruct.gui.MainApplication %1 %2 %3 %4 %5
