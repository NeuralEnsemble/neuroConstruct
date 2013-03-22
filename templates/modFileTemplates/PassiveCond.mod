TITLE Passive membrane channel
 
COMMENT
    
    This is a mod file for a simple leak conductance on a cell membrane.
    The appropriate values of %Name%, %Max Conductance Density%, etc. will
    be replaced by neuroConstruct when this template is used
     
ENDCOMMENT

UNITS {
	(mV) = (millivolt)
	(mA) = (milliamp)
	(S) = (siemens)
} 

NEURON {
	SUFFIX %Name%
	NONSPECIFIC_CURRENT i
	RANGE gmax, e
}

PARAMETER {
	gmax = %Max Conductance Density%	(S/cm2)	<0,1e9>
	e = %Reversal Potential%	(mV)
}

ASSIGNED {v (mV)  i (mA/cm2)}

BREAKPOINT {
	i = gmax*(v - e)
}
