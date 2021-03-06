COMMENT

   **************************************************
   File generated by: neuroConstruct v1.4.1 
   **************************************************


ENDCOMMENT


?  This is a NEURON mod file generated from a ChannelML file

?  Unit system of original ChannelML file: SI Units

COMMENT
    A channel from Maex, R and De Schutter, E. Synchronization of Golgi and Granule Cell Firing in a 
    Detailed Network Model of the Cerebellar Granule Cell Layer
ENDCOMMENT

TITLE Channel: Gran_NaF_98

COMMENT
    Fast inactivating Na+ channel
ENDCOMMENT


UNITS {
    (mA) = (milliamp)
    (mV) = (millivolt)
    (S) = (siemens)
    (um) = (micrometer)
    (molar) = (1/liter)
    (mM) = (millimolar)
    (l) = (liter)
}


    
NEURON {
      

    SUFFIX Gran_NaF_98
    USEION na READ ena WRITE ina VALENCE 1  ? reversal potential of ion is read, outgoing current is written
           
        
    RANGE gmax, gion
    
    RANGE minf, mtau
    
    RANGE hinf, htau
    
}

PARAMETER { 
      

    gmax = 0.05463010000000001 (S/cm2)  ? default value, should be overwritten when conductance placed on cell
    
}



ASSIGNED {
      

    v (mV)
    
    celsius (degC)
          

    ? Reversal potential of na
    ena (mV)
    ? The outward flow of ion: na calculated by rate equations...
    ina (mA/cm2)
    
    
    gion (S/cm2)
    minf
    mtau (ms)
    hinf
    htau (ms)
    
}

BREAKPOINT { 
                        
    SOLVE states METHOD cnexp
         

    gion = gmax*((m)^3)*((h)^1)      

    ina = gion*(v - ena)
            

}



INITIAL {
    
    ena = 55
        
    rates(v)
    m = minf
        h = hinf
        
    
}
    
STATE {
    m
    h
    
}

DERIVATIVE states {
    rates(v)
    m' = (minf - m)/mtau
    h' = (hinf - h)/htau
    
}

PROCEDURE rates(v(mV)) {  
    
    ? Note: not all of these may be used, depending on the form of rate equations
    LOCAL  alpha, beta, tau, inf, gamma, zeta, temp_adj_m, A_alpha_m, B_alpha_m, Vhalf_alpha_m, A_beta_m, B_beta_m, Vhalf_beta_m, A_tau_m, B_tau_m, Vhalf_tau_m, temp_adj_h, A_alpha_h, B_alpha_h, Vhalf_alpha_h, A_beta_h, B_beta_h, Vhalf_beta_h, A_tau_h, B_tau_h, Vhalf_tau_h
        
    TABLE minf, mtau,hinf, htau
 DEPEND celsius
 FROM -100 TO 100 WITH 4000
    
    
    UNITSOFF
    
    ? There is a Q10 factor which will alter the tau of the gates 
                 

    temp_adj_m = 3^((celsius - 17.350264793)/10)     

    temp_adj_h = 3^((celsius - 17.350264793)/10)
    
    ? There is a voltage offset of 0.010. This will shift the dependency of the rate equations 
    v = v - (10)
    
            
                
           

        
    ?      ***  Adding rate equations for gate: m  ***
        
    ? Found a parameterised form of rate equation for alpha, using expression: A*exp((v-Vhalf)/B)
    A_alpha_m = 1500
    B_alpha_m = 0.012345679
    Vhalf_alpha_m = -0.039   
    
    ? Unit system in ChannelML file is SI units, therefore need to convert these to NEURON quanities...
    
    A_alpha_m = A_alpha_m * 0.001   ? 1/ms
    B_alpha_m = B_alpha_m * 1000   ? mV
    Vhalf_alpha_m = Vhalf_alpha_m * 1000   ? mV
          
                     
    alpha = A_alpha_m * exp((v - Vhalf_alpha_m) / B_alpha_m)
    
    
    ? Found a parameterised form of rate equation for beta, using expression: A*exp((v-Vhalf)/B)
    A_beta_m = 1500
    B_beta_m = -0.0151515
    Vhalf_beta_m = -0.039   
    
    ? Unit system in ChannelML file is SI units, therefore need to convert these to NEURON quanities...
    
    A_beta_m = A_beta_m * 0.001   ? 1/ms
    B_beta_m = B_beta_m * 1000   ? mV
    Vhalf_beta_m = Vhalf_beta_m * 1000   ? mV
          
                     
    beta = A_beta_m * exp((v - Vhalf_beta_m) / B_beta_m)
    
     
    ? Found a generic form of the rate equation for tau, using expression: 1/(alpha + beta) < 0.00005 ? 0.00005 : 1/(alpha + beta)
    
    ? Note: Equation (and all ChannelML file values) in SI Units so need to convert v first...
    
    v = v * 0.001   ? temporarily set v to units of equation...
            
    
    ? Equation depends on alpha/beta, so converting them too...
    alpha = alpha * 1000  
    beta = beta * 1000
    
    if (1/(alpha + beta) < 0.00005 ) {
        tau =  0.00005 
    } else {
        tau =  1/(alpha + beta)
    }
    ? Set correct units of tau for NEURON
    tau = tau * 1000 
    
    v = v * 1000   ? reset v
        
    mtau = tau/temp_adj_m
    minf = alpha/(alpha + beta)
          
       
    
    ?     *** Finished rate equations for gate: m ***
    

    
            
                
           

        
    ?      ***  Adding rate equations for gate: h  ***
        
    ? Found a parameterised form of rate equation for alpha, using expression: A*exp((v-Vhalf)/B)
    A_alpha_h = 120
    B_alpha_h = -0.01123596
    Vhalf_alpha_h = -0.05   
    
    ? Unit system in ChannelML file is SI units, therefore need to convert these to NEURON quanities...
    
    A_alpha_h = A_alpha_h * 0.001   ? 1/ms
    B_alpha_h = B_alpha_h * 1000   ? mV
    Vhalf_alpha_h = Vhalf_alpha_h * 1000   ? mV
          
                     
    alpha = A_alpha_h * exp((v - Vhalf_alpha_h) / B_alpha_h)
    
    
    ? Found a parameterised form of rate equation for beta, using expression: A*exp((v-Vhalf)/B)
    A_beta_h = 120
    B_beta_h = 0.01123596
    Vhalf_beta_h = -0.05   
    
    ? Unit system in ChannelML file is SI units, therefore need to convert these to NEURON quanities...
    
    A_beta_h = A_beta_h * 0.001   ? 1/ms
    B_beta_h = B_beta_h * 1000   ? mV
    Vhalf_beta_h = Vhalf_beta_h * 1000   ? mV
          
                     
    beta = A_beta_h * exp((v - Vhalf_beta_h) / B_beta_h)
    
     
    ? Found a generic form of the rate equation for tau, using expression: 1/(alpha + beta) < 0.000225 ? 0.000225 : 1/(alpha + beta)
    
    ? Note: Equation (and all ChannelML file values) in SI Units so need to convert v first...
    
    v = v * 0.001   ? temporarily set v to units of equation...
            
    
    ? Equation depends on alpha/beta, so converting them too...
    alpha = alpha * 1000  
    beta = beta * 1000
    
    if (1/(alpha + beta) < 0.000225 ) {
        tau =  0.000225 
    } else {
        tau =  1/(alpha + beta)
    }
    ? Set correct units of tau for NEURON
    tau = tau * 1000 
    
    v = v * 1000   ? reset v
        
    htau = tau/temp_adj_h
    hinf = alpha/(alpha + beta)
          
       
    
    ?     *** Finished rate equations for gate: h ***
    

         

}


UNITSON


