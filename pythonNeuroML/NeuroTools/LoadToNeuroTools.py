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
from NeuroTools.signals.spikes import SpikeTrain
from NeuroTools.signals.spikes import SpikeList
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

def get_spike_train(filename):
  file = open(filename, 'r')
  spiketimes = []

  for line in file:
      if len(line.strip()) > 0 :
        st = float(line)
        spiketimes.append(st)

  return SpikeTrain(spiketimes,t_start = times[0], t_stop=times[-1])



## Function for reading an analog file

def get_analog_signal(filename):
  file = open(filename, 'r')
  volts = []

  for line in file:
      if len(line.strip()) > 0 :
        v = float(line)
        volts.append(v)

  return AnalogSignal(volts,dt)



## Read in all traces

all_analog_signals = {}
all_spike_lists = {}
  
file_names = os.listdir('.')

populations = []

for file_name in file_names:
    
  if file_name.endswith('.dat') and file_name.find('_')>0:
    cell_ref = file_name[:-4]
    pop_name = cell_ref[:cell_ref.rfind('_')]
    
    if populations.count(pop_name)==0 : populations.append(pop_name)
    
    sig = get_analog_signal(file_name)
    
    all_analog_signals[cell_ref] = sig
    
  if file_name.endswith('.spike') and file_name.find('_')>0:
  
    cell_ref = file_name[:file_name.find('.')]
    id = int(cell_ref[cell_ref.find('_')+1:])
    pop_name = cell_ref[:cell_ref.rfind('_')]
    
    #print "Found cell %i in population %s"%(id, popName)
    
    if populations.count(pop_name)==0 : populations.append(popName)
    
    sig = get_spike_train(file_name)
    
    if not all_spike_lists.has_key(pop_name):
        all_spike_lists[pop_name] = SpikeList([], [], t_start = times[0], t_stop=times[-1])
        
    all_spike_lists[pop_name].append(id, sig)
    

print "All populations with signals: "+ str(populations)

for popName in all_spike_lists.keys():
    print "Spikes found for cell ids in population %s: %s"%(popName, all_spike_lists[popName].id_list())


## Plot the loaded data
        
import pylab
import matplotlib.pyplot

plots = {}

for pop in populations:
    if all_spike_lists.has_key(pop):
        all_spike_lists[pop].raster_plot(kwargs={'label':pop})
        pylab.ylabel('Neuron # in population '+pop)
    else:
        figure = matplotlib.pyplot.figure()
        figure.suptitle(pop)
        plots[pop] = figure.add_subplot(111)
    
        pylab.ylabel('Membrane potential (mV)')
        pylab.xlabel('Time (ms)')


print "\nTraces which have been loaded: "

for cell_ref in all_analog_signals.keys():
    
  #print "\n%s:\n     %s"%(cell_ref, allAnalogSignals[cell_ref])

  pop_name = cell_ref[:cell_ref.rfind('_')]
    
  plots[pop_name].plot(times,all_analog_signals[cell_ref].signal,'-', label=cell_ref, linewidth=1)
  

pylab.show()

