/* 
* This file is based on the hh_tchan.g in the standard GENESIS distribution. See 
* there for more details
* 
* It will allow creation of a generic Hodgkin Huxley like channel. 
* The appropriate values of %Name%, %Max Conductance Density%, etc. will
* be replaced by neuroConstruct when this template is used
*
*/


float EREST_ACT = -0.060

function make_%Name%
	str chanpath = "/library/%Name%"
	if ({exists {chanpath}})
	    return
	end

		
		
	create tabchannel {chanpath}
	
	setfield {chanpath}                                                \
		Ek             	%Reversal Potential%                       \ // V
		Gbar           	%Max Conductance Density%                  \ // S
		Ik 		0 					   \
		Gk 		0                                          \
		Xpower  	%Activation State Variable Power%          \
		Ypower         	%Inactivation State Variable Power%        \
		Zpower 		0

		
	setupalpha {chanpath} X  \
			%AAX%    \
			%ABX%    \
			%ACX%    \
			%ADX%    \
			%AFX%    \
			%BAX%    \
			%BBX%    \
			%BCX%    \
			%BDX%    \
			%BFX%    

	setupalpha {chanpath} Y  \
			%AAY%    \
			%ABY%    \
			%ACY%    \
			%ADY%    \
			%AFY%    \
			%BAY%    \
			%BBY%    \
			%BCY%    \
			%BDY%    \
			%BFY%    
		
end
