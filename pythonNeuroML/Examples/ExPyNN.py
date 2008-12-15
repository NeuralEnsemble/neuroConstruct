#
#   A simple example of a PyNN script which creates some cells and makes connections 
#   (but does not run simulation)
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and Wellcome Trust
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

cellNum = 5
cellType = IF_cond_alpha
connNum = 10

if (simulator == 'neuroml'):
	setup(file="test.xml")
else:
	setup()

cells = Population((cellNum,), cellType)

xMin=0
xMax=200
yMin=0
yMax=200
zMin=0
zMax=50

for cell in cells:
	cell.position = (xMin+(NumpyRNG.next(rng)*(xMax-xMin)), yMin+(NumpyRNG.next(rng)*(yMax-yMin)), zMin+(NumpyRNG.next(rng)*(zMax-zMin)))

addrs = cells.addresses()

gids = []

for addr in addrs:
	gid  = cells[addr]
	print "Cell %s (id = %d) is at %s" % (addr, gid, cells[addr].position)
	gids.append(gid)
	
	
for i in range(connNum):
	
	src = gids[int(NumpyRNG.next(rng) * len(gids))]
	tgt = gids[int(NumpyRNG.next(rng) * len(gids))]
	
	print "Connecting cell %s to cell %s" % (src, tgt)
	connect(cells[cells.locate(src)], cells[cells.locate(tgt)])

	
	
end()