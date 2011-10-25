# -*- coding: utf-8 -*-
#
#   A simple example of a PyNN script which creates some cells and makes connections 
#
#   This standalone file should be runnable on any PyNN simulator and uses most of the functions 
#   that neuroConstruct generated PyNN code would use. Note some of the more advanced functionality
#   e.g. synaptic plasticity, is turned off for some less well tested simulators (all except Nest & NEURON)
#
#   To run this example type:
#       python Ex4_SimplePyNN.py neuron
#   or replace neuron ith another PyNN simulator
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the
#   Wellcome Trust
#
# 


import sys

my_simulator = ""

print "Has args: %s" % hasattr(sys,"argv")


if hasattr(sys,"argv") and len(sys.argv) >1:     # run using python
    my_simulator = sys.argv[-1]
else:
    my_simulator = "neuron"    # run using nrngui -python
	
print "Running PyNN script in simulator: "+ my_simulator

exec("from pyNN.%s import *" % my_simulator)

from pyNN.random import NumpyRNG

seed=1234
rng = NumpyRNG(seed)

'''
cellTypeToUse = IF_cond_exp
#cellTypeToUse = IF_cond_alpha

class CellTypeA(cellTypeToUse):

    def __init__ (self, parameters): 
        cellTypeToUse.__init__ (self, parameters)
'''

cellNumA = 2
cellNumB = 4
cellType = IF_cond_exp
connNum = 10

tstop = 200.0
dt=0.01

if (my_simulator == 'neuroml'):
	setup(file="test.xml")
else:
	setup(timestep=dt)
   

cell_params = {'tau_refrac':2.0,'v_thresh':-50.0,'tau_syn_E':2.0, 'tau_syn_I':2.0}

cellsA = Population((cellNumA,), cellType, cell_params, label="Cells_A")
cellsB = Population((cellNumB,), cellType, cell_params, label="Cells_B")


xMin=0
xMax=200
yMin=0
yMax=200
zMin=0
zMax=50


for cell in cellsA:
    cell.position[0] = xMin+(NumpyRNG.next(rng)*(xMax-xMin))
    cell.position[1] = yMin+(NumpyRNG.next(rng)*(yMax-yMin))
    cell.position[2] = zMin+(NumpyRNG.next(rng)*(zMax-zMin))

for cell in cellsB:
    cell.position[0] = xMin+(NumpyRNG.next(rng)*(xMax-xMin))
    cell.position[1] = yMin+(NumpyRNG.next(rng)*(yMax-yMin))
    cell.position[2] = zMin+(NumpyRNG.next(rng)*(zMax-zMin))



indicesA = []
indicesB = []

for idA in cellsA:
	index  = cellsA.id_to_index(idA)
	print " - Cell id %s in cellsA (index = %d) is at %s" % (idA, index, idA.position)
	indicesA.append(index)

for idB in cellsB:
	index  = cellsB.id_to_index(idB)
	print " - Cell id %s in cellsB (index = %d) is at %s" % (idB, index, idB.position)
	indicesB.append(index)
	
print indicesA
print indicesB

syn_dynam = None

if my_simulator == 'neuron' or my_simulator == 'nest' :
    syn_dynam = SynapseDynamics(fast=TsodyksMarkramMechanism(U=0.4, tau_rec=100.0, tau_facil=1000.0))

#syn_dynam = SynapseDynamics(slow=STDPMechanism(timing_dependence=SpikePairRule(tau_plus=20.0, tau_minus=20.0),
#                           weight_dependence=AdditiveWeightDependence(w_min=0, w_max=0.4,
#                                                                      A_plus=0.01, A_minus=0.012)))


projConns = []

for i in range(connNum):	
    src = indicesA[int(NumpyRNG.next(rng) * len(indicesA))]
    tgt = indicesB[int(NumpyRNG.next(rng) * len(indicesB))]

    print "-- Connecting cell %s (gid %s) in %s to cell %s (gid %s) in %s" % (src, str(cellsA[src]) ,cellsA.label, tgt, str(cellsB[tgt]), cellsB.label)

    projConns.append([src, tgt, 0.01, 1.0])
    

connector= FromListConnector(projConns)               
proj = Projection(cellsA, cellsB, connector, target='excitatory', label='TestProj' ''',synapse_dynamics=syn_dynam''')


if my_simulator == 'neuron' or my_simulator == 'nest' :
    voltDistr = RandomDistribution('uniform',[-65,-50],rng)

    cellsA.randomInit(voltDistr)
    cellsB.randomInit(voltDistr)

freq = 150 # Hz

number = int(tstop*freq/1000.0)

print "Number of spikes expected in %d ms at %dHz: %d"%(tstop, freq, number)


from NeuroTools.stgen import StGen
stgen = StGen()
stgen.seed(seed)

spike_times = stgen.poisson_generator(rate=freq, t_stop=tstop, array=True)

input_population  = Population(cellNumA, SpikeSourceArray, {'spike_times': spike_times}, label="inputsToA") 

for i in input_population:
    i.spike_times = stgen.poisson_generator(rate=freq, t_stop=tstop, array=True)
    print "spike_times: " +str(i.spike_times)


inputConns = []

for i in range(0,cellNumA):
    inputConns.append([i, i, 0.1, 3.0])

connector2= FromListConnector(inputConns)               
input_proj = Projection(input_population, cellsA, connector2, target='excitatory', label='InputProj' ,synapse_dynamics=None)


cellsA.record_v()
cellsB.record_v()

cellsA.record_gsyn()
cellsB.record_gsyn()

input_population.record()

print "---- Running the simulation ----"
run(tstop)


cellsA.print_v("cellsA.dat")
cellsB.print_v("cellsB.dat")


cellsA.print_gsyn("cellsAgsyn.dat",  compatible_output=True)
cellsB.print_gsyn("cellsBgsyn.dat",  compatible_output=True)

input_population.printSpikes("inputs.dat")


print cellsA.describe()
print cellsB.describe()
print proj.describe()
print input_population.describe()
print input_proj.describe()

print get_time_step()
	
end()

print "Successfully executed PyNN script in simulator: "+ my_simulator