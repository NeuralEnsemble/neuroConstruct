# Test class extending a standard cell


class CellTypeA(IF_cond_exp):


    def __init__ (self, parameters): 
        exec("from pyNN.%s import *" % parameters['simulator'])
        IF_cond_exp.__init__ (self, parameters)
        print "Created new CellTypeA on simulator: "+str(simulator)

'''   
ff = CellTypeA([])
ff2 = IF_cond_exp([])

print dir(ff)
print dir(ff2)
'''


    