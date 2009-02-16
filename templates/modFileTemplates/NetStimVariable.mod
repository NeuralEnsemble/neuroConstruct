COMMENT

Modification of NetStim by Padraig Gleeson & Matteo Farinella for use in neuroConstruct.

Used to provide a time varying frequency of stimulation to a synaptic input. Actually varies the 
interval until the next spike as function of time.


ENDCOMMENT


NEURON  {

    ARTIFICIAL_CELL NetStimVariable
    RANGE minFrequency
    RANGE noise, del, dur
  
}

PARAMETER {

    minFrequency = 0.0005 (1/ms) <1e-9,1e9>  : min frequency of stimulation, since frequency = 0 means no future spikes
    noise = 0 <0,1>                          : amount of randomeness (0.0 - 1.0)
    dur = 800 (ms)                           : burst duration
    del = 100  (ms)                          : delay before onset of burst
}

ASSIGNED {

    event (ms)
    on
    end (ms)
        
}

PROCEDURE seed(x) {

    set_seed(x)
        
}

FUNCTION getFrequency() {
    LOCAL evalFreq

    evalFreq = %RATE_EXPRESSION%  : Will be replaced by real expression in neuroConstruct

    if(evalFreq < minFrequency) {
        getFrequency = minFrequency
    } else {
        getFrequency = evalFreq
    }
}


INITIAL {
    LOCAL interval
    on = 0

    if (noise < 0) {
            noise = 0
    }
    if (noise > 1) {
            noise = 1
    }

    if (del >= 0 ) {

            interval = 1/getFrequency()

    
            : randomize the first spike so on average it occurs at
            : del + noise*interval
            event = del + invl(interval) - interval*(1. - noise)

            :printf("----- First event will be at: %g\n", event)
            
            : but not earlier than 0
            if (event < 0) {
                    event = 0
            }
            
            net_send(event, 3)
    }
        
}


PROCEDURE init_sequence(t(ms)) {

    if (getFrequency() > 0) {
            on = 1
            event = t
            end = del + dur
    }
        
}


FUNCTION invl(mean (ms)) (ms) {

    if (mean <= 0.) {
            mean = .01 (ms) : I would worry if it were 0.
    }
    if (noise == 0) {
            invl = mean
    } else {
            invl = (1. - noise)*mean + noise*mean*exprand(1)
    }
        
}


PROCEDURE event_time() {

    if (getFrequency() > 0) {
            event = event + invl(1/getFrequency())
    }
    if (event > end) {
            on = 0
    }
        
}


NET_RECEIVE (w) {

    :printf("----- NET_RECEIVE at: %g, flag: %g, on: %g\n", t, flag, on)

    if (flag == 0) { : external event

            if (w > 0 && on == 0) { : turn on spike sequence
                    init_sequence(t)
                    net_send(0, 1)
            } else if (w < 0 && on == 1) { : turn off spiking
                    on = 0
            }
    }
    if (flag == 3) { : from INITIAL
            if (on == 0) {
                    init_sequence(t)
                    net_send(0, 1)
            }
    }
    if (flag == 1 && on == 1) {
            net_event(t)
            event_time()
            if (on == 1) {
                    :printf("----- Next event time: %g\n", event)
                    net_send(event - t, 1)
            }
    }
        
}

