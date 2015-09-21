import os
import matplotlib.pyplot as plt
import quantities as pq
from neo.core import AnalogSignal,SpikeTrain

#SIM_PATH = '' # Must set this to use.  
#SIM_PATH = '/Users/rgerkin/neuroConstruct/osb/hippocampus/'
#SIM_PATH += 'CA1_pyramidal_neuron/CA1PyramidalCell/neuroConstruct/'
#SIM_PATH += 'simulations/DefaultSimulationConfiguration__N'

def get_times(sim_path):
  '''
  Reads a time.dat file in neuroConstruct format and returns 
  a list of times corresponding to simulation time steps.
  '''
  file_path = os.path.join(sim_path,"time.dat")
  time_file = open(file_path, 'r')
  times = []

  for line in time_file:
      if len(line.strip()) > 0 :
          t = float(line)
          times.append(t)
        
  return times

def get_dt(sim_path):
  times = get_times(sim_path)
  return times[1]-times[0]

def get_spike_train(sim_path,file_name):
  '''
  Reads a spike train from a file in neuroConstruct format and returns
  a SpikeTrain object
  '''
  if not file_name.endswith('.spike'):
    file_name += '.spike'
  file_path = os.path.join(sim_path,file_name)
  with open(file_path, 'r') as data_file:
    spike_times = []

    for line in data_file:
      if len(line.strip()) > 0 :
        st = float(line)
        spike_times.append(st)

  times = get_times()
  return SpikeTrain(spike_times,t_start = times[0], t_stop=times[-1])

def get_spike_trains(sim_path):
  '''
  Finds spike trains in the SIM_PATH and organizes them by cell,
  returning a dict of dicts of SpikeTrains.
  '''
  all_spike_lists = {}
  spike_file_names = [i for i in get_data_file_names(sim_path) if i.endswith('.spike')]
  for file_name in spike_file_names:
    cell_ref = file_name[:file_name.find('.')] # Cell reference in nC.  
    id = int(cell_ref[cell_ref.find('_')+1:]) # Cell id.  
    pop_name = cell_ref[:cell_ref.rfind('_')] # Cell population name.  
    sig = get_spike_train(file_name) # The spike train.  
    if not all_spike_lists.has_key(pop_name):
        all_spike_lists[pop_name] = {}
    all_spike_lists[pop_name][id] = sig
  for popName in all_spike_lists.keys():
    pass
  return all_spike_lists

def get_analog_signal(sim_path,file_name):
  '''
  Reads a analog signal from a file in neuroConstruct format and returns
  an AnalogSignal object
  '''
  if not file_name.endswith('.dat'):
    file_name += '.dat'
  file_path = os.path.join(sim_path,file_name)
  with open(file_path, 'r') as data_file:
    volts = []

    for line in data_file:
        if len(line.strip()) > 0 :
          v = float(line)
          volts.append(v)

  dt = get_dt(sim_path)
  return AnalogSignal(volts*pq.mV, sampling_period=dt*pq.ms)

def get_analog_signals(sim_path):
  '''
  Finds analog signals in the SIM_PATH and organizes them by cell,
  returning a dict of AnalogSignals.
  '''
  all_analog_signals = {}
  analog_file_names = [i for i in get_data_file_names(sim_path) if i.endswith('.dat')] 
  for file_name in analog_file_names:
    cell_ref = file_name[:-4]
    sig = get_analog_signal(file_name)
    
    all_analog_signals[cell_ref] = sig
  return all_analog_signals

def get_data_file_names(sim_path):
  file_names = os.listdir(sim_path)
  data_file_names = []
  for file_name in file_names:
    if file_name.find('_')>0:
      data_file_names.append(file_name)
  return data_file_names
    
def plot_spike_trains(spike_trains):   
  '''
  Takes a dict from get_spike_trains and plots all the spike trains
  '''     
  
  for pop_name,spike_list in spike_trains.items():
    spike_trains[pop].raster_plot(kwargs={'label':pop_name})
    plt.ylabel('Neuron # in population '+pop_name)
      
  plt.show()

def plot_analog_signals(analog_signals):
  '''
  Takes a dict from get_analog_signals and plots all the analog signals
  '''     
  
  times = get_times()
  plots = {}
    
  for cell_ref in analog_signals.keys():  
    pop_name = cell_ref[:cell_ref.rfind('_')]
    figure = plt.figure()
    figure.suptitle(pop_name)
    plots[pop_name] = figure.add_subplot(111)
    if '.' not in cell_ref: # The only signals that is likely to be Vm.  
      plt.ylabel('Membrane potential (mV)')
    plt.xlabel('Time (ms)')
    volts = analog_signals[cell_ref].signal
    plots[pop_name].plot(times,
                         volts,
                         '-', 
                         label=cell_ref, 
                         linewidth=1)

  plt.show()
