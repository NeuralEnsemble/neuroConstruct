

function  [volts] = tracefromspikes( spiketimes, time, spikevolt, nospikevolt)


    spikecount = 1;
    insidespike = 0;

	volts = ones(1,length(time));
	volts = volts * nospikevolt;

    for timeStep=1:length(time)
        newTime = time(timeStep);
        if ( (spikecount <= length(spiketimes)) & (newTime >= (spiketimes(spikecount))))

            if (insidespike == 0)
                volts(timeStep) = spikevolt;
                spikecount = spikecount + 1;
                insidespike = 1;
            else
                volts(timeStep) = nospikevolt;
                spikecount = spikecount + 1;
            end;

        else
            volts(timeStep) = nospikevolt;
            insidespike = 0;
        end;
    end;
