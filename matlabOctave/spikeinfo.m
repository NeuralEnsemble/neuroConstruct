%
%  This is part of a set of generic tools for analysing simulation data stored by neuroConstruct
% 
%  Suggestions for more functions to add here are welcome (info@neuroConstruct.org)
% 
%  Authors: Padraig Gleeson, Volker Steuber
%
%  SPIKEINFO Returns cell array of spike times for input traces
%
%  spikeinfo(volts, time)
%  spikeinfo(volts, time, threshold)


function [ spiketimes ] = spikeinfo( volts, time, varargin)

	threshold = 0;

	if (nargin == 3)
		threshold = varargin{1};
	end

	[len, numCells] = size(volts);
	fired=0;
    
    for cellIndex=1:numCells,

        numspikes = 1;

		spiketimes{cellIndex} = [];

        for t=1:length(time),

            if (volts(t, cellIndex)>=threshold)

                if (fired == 0)
                    fired=1;
                    time(t);
                    spiketimes{cellIndex}(numspikes)=time(t);
                    numspikes = numspikes + 1;

                else
                    % if the spike has already been counted ignore superthreshold v
                end
            else
                fired=0; % reset after spike
            end
        end	
    end