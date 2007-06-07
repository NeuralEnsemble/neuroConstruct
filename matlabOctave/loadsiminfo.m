%%
%  This is part of a set of generic tools for analysing simulation data stored by neuroConstruct
% 
%  Suggestions for more functions to add here are welcome (info@neuroConstruct.org)
% 
%  Authors: Padraig Gleeson, Volker Steuber
%


function loadsiminfo()

	disp('Loading simulation information...') 
	
	simfilename = 'simulation.props';
	
	simInfoDoc = xmlread(simfilename);
	
	allProps = simInfoDoc.getElementsByTagName('entry');
	
	for k = 0:allProps.getLength-1
	
		nextProp = allProps.item(k);
		
		name = nextProp.getAttribute('key');
		value = nextProp.getTextContent();
		
		disp(sprintf('%s: %s', char(name), char(value) ))
		
	end