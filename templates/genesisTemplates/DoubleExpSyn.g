/* 
* This GENESIS template will allow creation of a Double Exponential Synapse
*
* The file is used to create a customised channel based on the values entered in neuroConstruct
* The appropriate values of %Name%, %Max Conductance Density%, etc. will
* be replaced by neuroConstruct when this template is used
*
*/

function makechannel_%Name%(compartment, name)

    str compartment   
    str name
    
    if (!({exists {compartment}/{name}}))
        
        create      synchan               {compartment}/{name}
        setfield    ^ \
                Ek                      {%Reversal Potential%} \
                tau1                    {%Tau Decay%} \
                tau2                    {%Tau Rise%} \
                gmax                    {%Max Conductance%}
    
        addmsg   {compartment}/{name}   {compartment} CHANNEL Gk Ek
        addmsg   {compartment}   {compartment}/{name} VOLTAGE Vm
    
    end
    
end
