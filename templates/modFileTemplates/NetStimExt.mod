COMMENT

Modification of NetStim by Volker Steuber with flat distribution of output events. 
Extended by Padraig Gleeson for use in neuroConstruct.

Note spike number becomes meaningless, use duration instead

Feb 2009: Fixed bug so that no spikes happen after time del + dur

Matteo Farinella, June 2011: added condition to suppress first event if it is later than del+dur (in case ISI is longer than del)


ENDCOMMENT


NEURON  {

    ARTIFICIAL_CELL NetStimExt
    RANGE y
    RANGE interval, number
    RANGE noise, del, dur, repeat
  
}

PARAMETER {

    interval = 10 (ms) <1e-9,1e9>   : time between spikes (msec)
    number  = 10 <0,1e9>            : number of spikes
    noise = 0 <0,1>                 : amount of randomeaness (0.0 - 1.0)
    dur = 10000 (ms)                : burst duration
    del = 0  (ms)                   : delay before onset of burst
    repeat = 0                      : 0 = burst once, 1 = repeat burst after del ms
}

ASSIGNED {

    y
    event (ms)
    on
    end (ms)
        
}

PROCEDURE seed(x) {

    set_seed(x)
        
}


INITIAL {

    on = 0
    y = 0
    if (noise < 0) {
            noise = 0
    }
    if (noise > 1) {
            noise = 1
    }
    if (del >= 0 && number > 0) {
    
            : randomize the first spike so on average it occurs at
            : start + noise*interval
            event = del + invl(interval) - interval*(1. - noise)
            
            : but not earlier than 0
            if (event < 0) {
                    event = 0
            }
            
            : nor later than del+dur
            if (event < (del+dur)) {
                    net_send(event, 3)
            }
    }
        
}


PROCEDURE init_sequence(t(ms)) {

    if (number > 0) {
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
    }else{
            invl = (1. - noise)*mean + noise*mean*exprand(1)
    }
        
}


PROCEDURE event_time() {

    if (number > 0) {
            event = event + invl(interval)
    }
    if (event > end) {
	if (repeat == 1) {
		event = event + del
		end = end + del + dur
	}else{
		on = 0
	}
    }
        
}


NET_RECEIVE (w) {

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
            y = 2
            net_event(t)
            event_time()
            if (on == 1) {
                    net_send(event - t, 1)
            }
            net_send(.1, 2)
    }
    if (flag == 2) {
            y = 0
    }
        
}

