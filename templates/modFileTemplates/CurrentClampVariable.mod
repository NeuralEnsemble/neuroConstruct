COMMENT

This is based on stim.mod from the NEURON source code. Extended by Padraig Gleeson for
use in neuroConstruct.

The original IClamp object in that has been extended to allow the amplitude to vary 
as a function of time. 

*** NOTE: This implementation assumes a smoothly varying amplitude! ***

If the amplitude function varies rapidly or has discontinuities this may cause problems 
with the numerical integration. This is intended for e.g. sinusoidally varying input currents
with period >> dt


Original comment in stim.mod:

Since this is an electrode current, positive values of i depolarize the cell
and in the presence of the extracellular mechanism there will be a change
in vext since i is not a transmembrane current but a current injected
directly to the inside of the cell.

ENDCOMMENT

NEURON {

    POINT_PROCESS CurrentClampVariable
    RANGE del, dur, amp, i
    ELECTRODE_CURRENT i
    
}

UNITS {

    (nA) = (nanoamp)
    
}


PARAMETER {

    del = 100(ms)
    dur = 800(ms)    <0,1e9>
    amp = 0.2 (nA)
    
}

ASSIGNED { 
    
    i (nA) 
    
}

INITIAL {

    i = 0
    
}

BREAKPOINT {
    
    at_time(del)
    at_time(del+dur)


    if (t < del + dur && t >= del) {
        
        i = %AMPLITUDE_EXPRESSION%  : Will be replaced by real expression in neuroConstruct

    } else {
        i = 0
    }
    
    
}
