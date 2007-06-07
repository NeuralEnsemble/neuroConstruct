/* 
* This file is based on the hhchan.g in the standard GENESIS distribution. See 
* there for more details
* 
* It will allow creation of a generic Hodgkin Huxley like channel. 
* The appropriate values of %Name%, %Max Conductance Density%, etc. will
* be replaced by neuroConstruct when this template is used
* 
*/

int EXPONENTIAL =   1
int SIGMOID     =   2
int LINOID      =   3


function make_%Name%

	str chanpath = "/library/%Name%"
	
	if ({exists {chanpath}})
	    return
	end
	
	// Note: for EXPONENTIAL and SIGMOID the units of the A factor are time^-1
	// But for LINOID they are Voltage^-1 time^-1, hence the conversion factor...
	
	float X_ALPHA_A, X_BETA_A, Y_ALPHA_A, Y_BETA_A
	if (LINOID == %Activation State Function Alpha Form%)
		X_ALPHA_A = %Activation Alpha A variable% * %Linoidal Conversion Factor%
	else
		X_ALPHA_A = %Activation Alpha A variable%
	end	
	if (LINOID == %Activation State Function Beta Form%)
		X_BETA_A = %Activation Beta A variable% * %Linoidal Conversion Factor%
	else
		X_BETA_A = %Activation Beta A variable%
	end	
	if (LINOID == %Inactivation State Function Alpha Form%)
		Y_ALPHA_A = %Inactivation Alpha A variable% * %Linoidal Conversion Factor%
	else
		Y_ALPHA_A = %Inactivation Alpha A variable%
	end	
	if (LINOID == %Inactivation State Function Beta Form%)
		Y_BETA_A = %Inactivation Beta A variable% * %Linoidal Conversion Factor%
	else
		Y_BETA_A = %Inactivation Beta A variable%
	end
	
		
	create		hh_channel	{chanpath}
	
	setfield {chanpath} \
		Ek             %Reversal Potential%                       \ // V
		Gbar           %Max Conductance Density%                  \ // S
		Xpower         %Activation State Variable Power%          \
		Ypower         %Inactivation State Variable Power%        \   
		X_alpha_FORM   %Activation State Function Alpha Form%     \
		X_alpha_A      {X_ALPHA_A}                                \ // 1/sec or 1/V-sec
		X_alpha_B      %Activation Alpha B variable%              \ // V
		X_alpha_V0     %Activation Alpha V0 variable%             \ // V
		X_beta_FORM    %Activation State Function Beta Form%      \
		X_beta_A       {X_BETA_A}                                 \ // 1/sec or 1/V-sec
		X_beta_B       %Activation Beta B variable%               \ // V
		X_beta_V0      %Activation Beta V0 variable%              \ // V
		Y_alpha_FORM   %Inactivation State Function Alpha Form%   \
		Y_alpha_A      {Y_ALPHA_A}                                \ // 1/sec or 1/V-sec
		Y_alpha_B      %Inactivation Alpha B variable%            \ // V
		Y_alpha_V0     %Inactivation Alpha V0 variable%           \ // V
		Y_beta_FORM    %Inactivation State Function Beta Form%    \
		Y_beta_A       {Y_BETA_A}                                 \ // 1/sec or 1/V-sec
		Y_beta_B       %Inactivation Beta B variable%             \ // V
		Y_beta_V0      %Inactivation Beta V0 variable%	            // V
		
end
