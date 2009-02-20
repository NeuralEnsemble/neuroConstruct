import sys

simulator = ""

print "Has args: %s" % hasattr(sys,"argv")

# Lines from standard PyNN examples
if hasattr(sys,"argv"):     # run using python
    simulator = sys.argv[-1]
else:
    simulator = "neuron"    # run using nrngui -python
    
exec("from pyNN.%s import *" % simulator)

size = 4
inputName = "Input1"
freq = 0.01
maxSimLength =500
nhost=1

setup(debug=True)

numberExp = int(float(maxSimLength)*freq)


spike_times = numpy.add.accumulate(numpy.random.exponential(1/freq, size=numberExp))
print spike_times

input_population  = Population(size, SpikeSourceArray, {'spike_times': spike_times }, inputName)

# Spike times would be the same for all cells in population!!

for ip in input_population:
    
    newSpikes = numpy.add.accumulate(numpy.random.exponential(1/freq, size=numberExp))
    
    print ip.cellclass
    print ip.spike_times.__class__
    
    print ip.get_parameters()
    
    
    ip.spike_times = newSpikes
    
    print ip.get_parameters()
