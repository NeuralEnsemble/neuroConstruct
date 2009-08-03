#
#   This file can be placed in the simulations directory of any neuroConstruct simulation
#   and all of the data traces found in that directory will be loaded into NeuroTools
#   AnalogSignal objects 
#   (TODO: load *.spike files into SpikeTrain objects)
#
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the
#   Wellcome Trust
#

import os

from NeuroTools.signals.analogs import AnalogSignal
from NeuroTools.plotting import *

## Open the time.dat file & get time points

time_file = open("time.dat", 'r')

times = []

for line in time_file:
    if len(line.strip()) > 0 :
        t = float(line)
        times.append(t)
        
print "There are %i time points"%len(times)   
dt = times[1]-times[0]


## Function for reading an analog file

def getAnalogSignal(v_filename):
  v_file = open(v_filename, 'r')
  volts = []

  for line in v_file:
      if len(line.strip()) > 0 :
        v = float(line)
        volts.append(v)

  return AnalogSignal(volts,dt)



## Read in all traces

allAnalogSignals = {}
  
file_names = os.listdir('.')

populations = []

for file_name in file_names:
  if file_name.endswith('.dat') and file_name.find('_')>0:
    cellRef = file_name[:-4]
    popName = cellRef[:cellRef.rfind('_')]
    
    if populations.count(popName)==0 : populations.append(popName)
    
    sig = getAnalogSignal(file_name) 
    
    allAnalogSignals[cellRef] = sig

print "All populations: "+ str(populations)


## Plot the loaded data
        
import pylab
import matplotlib.pyplot

plots = {}

for pop in populations:
    figure = matplotlib.pyplot.figure()
    figure.suptitle(pop)
    plots[pop] = figure.add_subplot(111)

    pylab.ylabel('Membrane potential (mV)')
    pylab.xlabel('Time (ms)')


print "\nTraces which have been loaded: "
for cellRef in allAnalogSignals.keys():
    
  print "\n%s:\n     %s"%(cellRef, allAnalogSignals[cellRef])

  popName = cellRef[:cellRef.rfind('_')]
    
  plots[popName].plot(times,allAnalogSignals[cellRef].signal,'-', label=cellRef, linewidth=1)
  

pylab.show()

