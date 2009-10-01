#
#   This file can be placed in the simulations directory of any neuroConstruct simulation
#   and all of the data traces found in that directory will be loaded into NeuroTools
#   AnalogSignal and SpikeTrain objects 
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
from NeuroTools.signals.spikes import *
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


## Function for reading a spike train from a file in neuroConstruct format

def getSpikeTrain(s_filename):
  s_file = open(s_filename, 'r')
  spiketimes = []

  for line in s_file:
      if len(line.strip()) > 0 :
        st = float(line)
        spiketimes.append(st)

  return SpikeTrain(spiketimes,t_start = times[0], t_stop=times[-1])



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
allSpikeLists = {}
  
file_names = os.listdir('.')

populations = []

for file_name in file_names:
    
  if file_name.endswith('.dat') and file_name.find('_')>0:
    cellRef = file_name[:-4]
    popName = cellRef[:cellRef.rfind('_')]
    
    if populations.count(popName)==0 : populations.append(popName)
    
    sig = getAnalogSignal(file_name) 
    
    allAnalogSignals[cellRef] = sig
    
  if file_name.endswith('.spike') and file_name.find('_')>0:
    cellRef = file_name[:file_name.find('.')]
    id = int(cellRef[cellRef.find('_')+1:])
    popName = cellRef[:cellRef.rfind('_')]
    
    #print "Found cell %i in population %s"%(id, popName)
    
    if populations.count(popName)==0 : populations.append(popName)
    
    sig = getSpikeTrain(file_name) 
    
    if not allSpikeLists.has_key(popName):
        allSpikeLists[popName] = SpikeList([], [], t_start = times[0], t_stop=times[-1])
        
    allSpikeLists[popName].append(id, sig)
    

print "All populations with signals: "+ str(populations)

for popName in allSpikeLists.keys():
    print "Spikes found for cell ids in population %s: %s"%(popName, allSpikeLists[popName].id_list())


## Plot the loaded data
        
import pylab
import matplotlib.pyplot

plots = {}

for pop in populations:
    if allSpikeLists.has_key(pop):
        allSpikeLists[pop].raster_plot(kwargs={'label':pop})
        pylab.ylabel('Neuron # in population '+pop)
    else:
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

