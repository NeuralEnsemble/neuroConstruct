%%
%  This is part of a set of generic tools for analysing simulation data stored by neuroConstruct
% 
%  Suggestions for more functions to add here are welcome (info@neuroConstruct.org)
% 
%  Authors: Padraig Gleeson, Volker Steuber
%
%  HISTOGRAM  histogram of spiking cells
% 
%  histogram(volts, time)
%  histogram( volts, time, threshold)
%  histogram( volts, time, threshold, nbins)


function  histogram( volts, time, varargin)

	threshold = 0;

	if (nargin >= 3)
		threshold = varargin{1};
	else
		threshold = 0;
	end



	if (nargin >= 4)
		nbins = varargin{2};
	else
		nbins = 100
	end


	if (nargin >= 5)
		fighandle = varargin{3};
	else
		fighandle = figure('Name','Histogram');
	end;


	if (nargin >= 6)
		barcolour = varargin{4};
	else
		barcolour = 'r';
	end;





	spiketimes = spikeinfo(volts,time,threshold);

	[len, numCells] = size(volts);

	hold on;


	binsize = (time(length(time)) - time(1))/nbins

    allspiketimes = [spiketimes{:}]


	bintimes = time(1) + (binsize/2):binsize:(time(length(time))-(binsize/2))

	set(findobj(gca,'Type','patch'), 'FaceColor', barcolour,'EdgeColor',barcolour)

	hist(get(fighandle, 'CurrentAxes'), allspiketimes, bintimes);

