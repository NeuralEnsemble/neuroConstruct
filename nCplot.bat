@echo off

REM    
REM   A convenient script for plotting data files, etc. using the neuroConstruct 
REM   graphing window. Can be used with any file format which can be imported into 
REM   neuroConstruct via Tools -> Import Data for Plot, i.e. time series data in 
REM   one or more columns
REM   
REM   Might be useful to associate some file types with nCplot.bat, e,g, *.dat so
REM   That double clicking on them plots them
REM 

c:\neuroConstruct\nC.bat -plot %1