%%
%  This is part of a set of generic tools for analysing simulation data stored by neuroConstruct
% 
%  Suggestions for more functions to add here are welcome (info@neuroConstruct.org)
% 
%  Authors: Padraig Gleeson, Volker Steuber
%
%  RASTER   Rasterplot of spiking cells
%
%  raster(volts, time)
%  raster(volts, time, threshold)
%  raster(volts, time, threshold, fighandle)
%  raster(volts, time, threshold, fighandle, toplot)
 


function  raster( volts, time, varargin)


	if (nargin >= 3)
		threshold = varargin{1};
	else
		threshold = 0;
	end;	


	if (nargin >= 4)
		fighandle = varargin{2};
	else
		fighandle = figure('Name','Rasterplot');
	end;


	if (nargin >= 5)
		toplot = varargin{3};
	else
		toplot = '.k';
	end;



	hold on


	spiketimes = spikeinfo(volts,time,threshold);

	[len, numCells] = size(volts);

	axis([time(1) time(length(time)) 0 numCells+1])

    
    for cellIndex=1:numCells,

        for spikeindex=1:length(spiketimes{cellIndex}),

			%disp(sprintf('Plotting point: (%f, %f), spikeindex %f', spiketimes{cellIndex}(spikeindex), cellIndex, spikeindex))
          
			plot(get(fighandle, 'CurrentAxes'), spiketimes{cellIndex}(spikeindex),  cellIndex, toplot)
         
        end	
    end