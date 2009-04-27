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

my_simulator = ""

print "Has args: %s" % hasattr(sys,"argv")


# Lines from standard PyNN examples
if hasattr(sys,"argv") and len(sys.argv) >1:     # run using python
    print "Args: "+str(sys.argv)
    my_simulator = sys.argv[-1]
else:
    my_simulator = "neuron2"    # run using nrngui -python
	
print "Running PyNN script in simulator: "+ my_simulator

exec("from pyNN.%s import *" % my_simulator)

from pyNN.random import NumpyRNG

seed=1234
rng = NumpyRNG(seed)

class CellTypeA(IF_cond_exp):

    def __init__ (self, parameters): 
        IF_cond_exp.__init__ (self, parameters)
        #print "Created new CellTypeA"


cellNumA = 2
cellNumB = 4
cellType = CellTypeA
connNum = 10

tstop = 200.0

if (my_simulator == 'neuroml'):
	setup(file="test.xml")
else:
	setup()
   

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
	
print gidsA
print gidsB
	
for i in range(connNum):	
    src = gidsA[int(NumpyRNG.next(rng) * len(gidsA))]
    tgt = gidsB[int(NumpyRNG.next(rng) * len(gidsB))]

    print "Connecting cell %s (%s) in %s to cell %s (%s) in %s" % (src, str(cellsA.locate(src)) ,cellsA.label, tgt, str(cellsB.locate(tgt)), cellsB.label)
    #print dir(connect)
    #print connect.func_doc
    connect(cellsA[cellsA.locate(src)], cellsB[cellsB.locate(tgt)], weight=1.0)

if my_simulator != "nest2":
        
    voltDistr = RandomDistribution('uniform',[-80,-50],rng)
    
    cellsA.randomInit(voltDistr)
    cellsB.randomInit(voltDistr)

freq = 50 # Hz

number = int(tstop*freq/1000.0)

print "Number of spikes expected in %d ms at %dHz: %d"%(tstop, freq, number)

#spike_times = numpy.add.accumulate(numpy.random.exponential(1000.0/freq, size=number))

#print spike_times

from NeuroTools.stgen import StGen
stgen = StGen()
stgen.seed(seed)

spike_times = stgen.poisson_generator(rate=freq, t_stop=tstop, array=True)

print "spike_times: " +str(spike_times)
print dir(spike_times)
print "spike_times: " +str(spike_times.tolist()[0].__class__)

input_population  = Population(cellNumA, SpikeSourceArray, {'spike_times': spike_times.tolist()}, "inputsToA")



#for i in range(0,cellNumA):
    #connect(input_population[(i,)], cellsA[(i,)], weight=1.0)

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