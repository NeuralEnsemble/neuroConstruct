@echo off

REM    
REM   Use an altered version of the line below to run the application with extra memory; type java -X for more..
REM   Choosing a max java heap size of about half your total physical memory is best, errors have been thrown 
REM   (when 3D view is shown) when app is run with more than this.
REM 

@echo on
java -Xmx700M  -cp neuroConstruct_1.0.4.jar ucl.physiol.neuroconstruct.gui.MainApplication %1 %2 %3 %4 %5