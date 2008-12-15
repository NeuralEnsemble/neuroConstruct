#
#   A simple example of a PyNN script which creates some cells and makes connections 
#   (but does not run simulation)
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the Wellcome Trust
#
# 


import sys

# Lines from standard PyNN examples
if hasattr(sys,"argv"):     # run using python
    simulator = sys.argv[-1]
else:
    simulator = "neuron"    # run using nrngui -python
	
exec("from pyNN.%s import *" % simulator)

from pyNN.random import NumpyRNG

rng = NumpyRNG(seed=1234)

print "Running PyNN script in simulator: "+ simulator

cellNumA = 5
cellNumB = 6
cellType = IF_cond_alpha
connNum = 10

if (simulator == 'neuroml'):
	setup(file="test.xml")
else:
	setup()

cellsA = Population((cellNumA,), cellType, label="Cells_A")
cellsB = Population((cellNumB,), cellType, label="Cells_B")

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
	connect(cellsA[cellsA.locate(src)], cellsB[cellsB.locate(tgt)])

	
	
end()