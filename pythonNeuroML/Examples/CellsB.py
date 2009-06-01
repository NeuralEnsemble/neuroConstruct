# Test class extending a standard cell

f = open("my_simulator", mode='r')

my_simulator = f.read()
exec("from pyNN.%s import *" % my_simulator)
f.close()

class CellsB(IF_cond_alpha):


    def __init__ (self, parameters): 
        if parameters == None:
            parameters = {}

        if not parameters.has_key('v_rest'): 
            parameters['v_rest'] = -60.0 

        if not parameters.has_key('tau_m'): 
            parameters['tau_m'] = 2.0 

        if not parameters.has_key('e_rev_E'): 
            parameters['e_rev_E'] = 0.0 

        if not parameters.has_key('tau_refrac'): 
            parameters['tau_refrac'] = 2.0 

        if not parameters.has_key('v_thresh'): 
            parameters['v_thresh'] = -50.0 

        if not parameters.has_key('tau_syn_E'): 
            parameters['tau_syn_E'] = 2.0 

        if not parameters.has_key('v_reset'): 
            parameters['v_reset'] = -60.0 

        if not parameters.has_key('cm'): 
            parameters['cm'] = 0.010002

        if not parameters.has_key('v_init'): 
            parameters['v_init'] = -55.0 
        IF_cond_alpha.__init__ (self, parameters)
        
        print "Created cell class on simulator: "+str(simulator)
