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

time_file = open("time.dat", 'r')

times = []

for line in time_file:
    if len(line.strip()) > 0 :
        t = float(line)
        times.append(t)
        
print "There are %i time points"%len(times)   

def getAnalogSignal(v_filename):
  v_file = open(v_filename, 'r')
  volts = []
  
  for line in v_file:
      if len(line.strip()) > 0 :
        v = float(line)
        volts.append(v)
        
  
  return AnalogSignal(volts,0.1)
  
allSignals = {}
  
file_names = os.listdir('.')

for file_name in file_names:
  if file_name.endswith('.dat') and file_name.find('_')>0:
    sig = getAnalogSignal(file_name) 
    
    allSignals[file_name[:-4]] = sig
    
    
        
import pylab

pylab.figure(num = 1)
pylab.ylabel('Membrane potential (mV)')
pylab.xlabel('Time (ms)')


print "\nTraces which have been loaded: "
for sig_name in allSignals.keys():
  print "\n%s:\n     %s"%(sig_name, allSignals[sig_name])
  pylab.plot(times,allSignals[sig_name].signal,'-', label=sig_name, linewidth=1)
  


pylab.show()

