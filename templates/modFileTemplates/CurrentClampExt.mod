COMMENT

This is based on stim.mod from the NEURON source code. Extended by Padraig Gleeson for
use in neuroConstruct.

The original IClamp object in that has been extended to allow continuous
repetition of the current pulse, as is possible with the GENESIS pulsegen
object (http://www.genesis-sim.org/GENESIS/gum-tutorials/beeman/Hyperdoc/Manual-26.html#ss26.49). 
Note: that object allows up to two individual pulses to be applied 
and repeated, but it's just as easy to add a second Input to a cell group
in neuroConstruct, so only one pulse is supported here.


Original comment in stim.mod:

Since this is an electrode current, positive values of i depolarize the cell
and in the presence of the extracellular mechanism there will be a change
in vext since i is not a transmembrane current but a current injected
directly to the inside of the cell.

ENDCOMMENT

NEURON {

    POINT_PROCESS CurrentClampExt
    RANGE del, dur, amp, repeat, i
    ELECTRODE_CURRENT i
    
}

UNITS {

    (nA) = (nanoamp)
    
}


PARAMETER {

    del (ms)
    dur (ms)    <0,1e9>
    amp (nA)
    repeat = 0
    
}

ASSIGNED { 
    
    i (nA) 
    
}

INITIAL {

    i = 0
    
}

BREAKPOINT {

    LOCAL shiftedTime, beginNextCycle
    
    shiftedTime = t
    
    at_time(del)
    at_time(del+dur)
    
    if (repeat == 1)
    {
        beginNextCycle = 0
        
        while (shiftedTime > del + dur)
        {
            shiftedTime = shiftedTime - (del + dur)
            beginNextCycle = beginNextCycle + (del + dur)
        }
        
        at_time(beginNextCycle + del)       ? to inform CVODE about a future discontinuity
        at_time(beginNextCycle + del+dur)
    }

    if (shiftedTime < del + dur && shiftedTime >= del) {
        i = amp
    }else{
        i = 0
    }
    
    
}
