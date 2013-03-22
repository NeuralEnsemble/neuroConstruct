TITLE Single Exponential Synapse

COMMENT

This code is based on the single exponential synapes in the standard 
NEURON distribution.     

The appropriate values of %Name%, %Tau% etc. will
be replaced by neuroConstruct when this template is used.

ENDCOMMENT

NEURON {
	POINT_PROCESS %Name%
	RANGE tau, e, i
	NONSPECIFIC_CURRENT i
} 

UNITS {
	(nA) = (nanoamp)
	(mV) = (millivolt)
	(uS) = (microsiemens)
}

PARAMETER {
	tau = %Tau% (ms) <1e-9,1e9>
	e = 0	(mV)
}

ASSIGNED {
	v (mV)
	i (nA)
}

STATE {
	g (uS)
}

INITIAL {
	g=0
}

BREAKPOINT {
	SOLVE state METHOD cnexp
	i = g*(v - e)
}

DERIVATIVE state {
	g' = -g/tau
}

NET_RECEIVE(weight (uS)) {
	state_discontinuity(g, g + weight)
}
