# Test class extending a standard cell


simulator = "nest2"

exec("from pyNN.%s import *" % simulator)

class CellTypeA(IF_cond_exp):



    def __init__ (self, parameters): 
        IF_cond_exp.__init__ (self, parameters)
        print "Created new CellTypeA--..."

'''   
ff = CellTypeA([])
ff2 = IF_cond_exp([])

print dir(ff)
print dir(ff2)
'''


    