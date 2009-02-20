#
#   A simple example of a PyNN script which creates some cells and makes connections 
#   (but does not run simulation)
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the
#   Wellcome Trust
#
# 


import sys

simulator = ""

print "Has args: %s" % hasattr(sys,"argv")

# Lines from standard PyNN examples
if hasattr(sys,"argv"):     # run using python
    simulator = sys.argv[-1]
else:
    simulator = "neuron"    # run using nrngui -python
	
exec("from pyNN.%s import *" % simulator)

from pyNN.random import NumpyRNG

rng = NumpyRNG(seed=1234)

print "Running PyNN script in simulator: "+ simulator

from CellTypeA import *

cellNumA = 2
cellNumB = 4
cellType = CellTypeA
connNum = 10

tstop = 200.0

if (simulator == 'neuroml'):
	setup(file="test.xml")
else:
	setup(debug=True)
    
    

cell_params = {'tau_refrac':2.0,'v_thresh':-50.0,'tau_syn_E':2.0, 'tau_syn_I':2.0}

cellsA = Population((cellNumA,), cellType, cell_params, label="Cells_A")
cellsB = Population((cellNumB,), cellType, cell_params, label="Cells_B")

print "ffffffffffffffffff"
print cellsA.__class__

xMin=0
xMax=200
yMin=0
yMax=200
zMin=0
zMax=50


for cell in cellsA:
	cell.position = (xMin+(NumpyRNG.next(rng)*(xMax-xMin)), yMin+(NumpyRNG.next(rng)*(yMax-yMin)), zMin+(NumpyRNG.next(rng)*(zMax-zMin)))

for cell in cellsB:
	cell.position = (xMin+(NumpyRNG.next(rng)*(xMax-xMin)), yMin+(NumpyRNG.next(rng)*(yMax-yMin)), zMin+(NumpyRNG.next(rng)*(zMax-zMin)))

addrsA = cellsA.addresses()
addrsB = cellsB.addresses()

gidsA = []
gidsB = []

for addr in addrsA:
	gid  = cellsA[addr]
	print "Cell %s (id = %d) is at %s" % (addr, gid, cellsA[addr].position)
	gidsA.append(gid)

for addr in addrsB:
	gid  = cellsB[addr]
	print "Cell %s (id = %d) is at %s" % (addr, gid, cellsB[addr].position)
	gidsB.append(gid)
	
	
for i in range(connNum):
	
	src = gidsA[int(NumpyRNG.next(rng) * len(gidsA))]
	tgt = gidsB[int(NumpyRNG.next(rng) * len(gidsB))]
	
	print "Connecting cell %s in %s to cell %s in %s" % (src, cellsA.label, tgt, cellsB.label)
	connect(cellsA[cellsA.locate(src)], cellsB[cellsB.locate(tgt)], weight=1.0)

if simulator != "nest2":
        
    voltDistr = RandomDistribution('uniform',[-80,-50],rng)
    
    cellsA.randomInit(voltDistr)
    cellsB.randomInit(voltDistr)

freq = 50 # Hz

number = int(tstop*freq/1000.0)

print "Number of spikes expected in %d ms at %dHz: %d"%(tstop, freq, number)

spike_times = numpy.add.accumulate(numpy.random.exponential(1000.0/freq, size=number))

print spike_times

input_population  = Population(cellNumA, SpikeSourceArray, {'spike_times': spike_times }, "inputsToA")


for i in range(0,cellNumA):
    connect(input_population[(i,)], cellsA[(i,)], weight=1.0)

cellsA.record_v()
cellsB.record_v()
input_population.record()

print "---- Running the simulation ----"
run(tstop)

#cellsA.i_offset = 200
#run(60)
#cellsA.i_offset = 0
#run(20)

cellsA.print_v("cellsA.dat")
cellsB.print_v("cellsB.dat")
input_population.printSpikes("inputs.dat")

'''
for addr in cellsA.addresses():
    fileName = 'Cell_%d.dat' % (cellsA[addr])
    print "Saving v of cell %s in: %s" % (addr, fileName)
    print (dir)
    cellsA[addr].print_v(fileName)
'''


	
end()