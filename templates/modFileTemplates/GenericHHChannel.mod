TITLE Generic channel in HH model

COMMENT
    
    This is a mod file for a generic Hodgkin Huxley like channel. 
    It's based on hh.mod in the NEURON source code. 
    
    The appropriate values of %Name%, %Max Conductance Density%, etc. will
    be replaced by neuroConstruct when this template is used
     
ENDCOMMENT


UNITS {
    (mA) = (milliamp)
    (mV) = (millivolt)
    (S) = (siemens)
}

NEURON {
    SUFFIX %Name%
    USEION %Ion Species% READ e%Ion Species% WRITE i%Ion Species%
    RANGE gmax, gion, Xinf, Yinf, Xtau, Ytau
}

PARAMETER { gmax = %Max Conductance Density% (S/cm2) }

STATE {
    X
    Y
}

ASSIGNED {
    v (mV)
    celsius (degC)
    e%Ion Species% (mV)
    
    gion (S/cm2)
    i%Ion Species% (mA/cm2)
    Xinf
    Yinf
    Xtau (ms)
    Ytau (ms)
}

LOCAL Xexp, Yexp
 
BREAKPOINT {
    SOLVE states METHOD cnexp
    gion = gmax%Activation State Variable Power%%Inactivation State Variable Power%
    i%Ion Species% = gion*(v - e%Ion Species%)
}

INITIAL {
    e%Ion Species% = %Reversal Potential% 
    rates(v)
    X = Xinf
    Y = Yinf
}

DERIVATIVE states {
    rates(v)
    X' =  (Xinf-X)/Xtau
    Y' = (Yinf-Y)/Ytau
}

LOCAL q10
 
PROCEDURE rates(v(mV)) {

    :Call once from HOC to initialize inf at resting v.
    
    LOCAL  alpha, beta, sum, A, B, V0
    
    TABLE Xinf, Xtau, Yinf, Ytau DEPEND celsius FROM -100 TO 100 WITH 200
    
    UNITSOFF
    q10 = 2.3^((celsius - %Experiment Temperature%)/10)
    
    
    
    :"X" activation system
    
    A = %Activation Alpha A variable%
    B = %Activation Alpha B variable%
    V0 = %Activation Alpha V0 variable%
        
    alpha = %Activation State Function Alpha Form%
    
    A = %Activation Beta A variable%
    B = %Activation Beta B variable%
    V0 = %Activation Beta V0 variable%
    
    beta =  %Activation State Function Beta Form%
    
    sum = alpha + beta
    Xtau = 1/(q10*sum)
    Xinf = alpha/sum
    
    
    
    :"Y" inactivation system
     
    A = %Inactivation Alpha A variable%
    B = %Inactivation Alpha B variable%
    V0 = %Inactivation Alpha V0 variable%
        
    alpha = %Inactivation State Function Alpha Form%
 
    A = %Inactivation Beta A variable%
    B = %Inactivation Beta B variable%
    V0 = %Inactivation Beta V0 variable%
    
    
    beta = %Inactivation State Function Beta Form%
    
    sum = alpha + beta
    Ytau = 1/(q10*sum)
    Yinf = alpha/sum
    
    
}

FUNCTION vtrap(VminV0, B) {
    if (fabs(VminV0/B) < 1e-6) {
    vtrap = B*(1 - VminV0/B/2)
    }else{
    vtrap = VminV0/(exp(VminV0/B) - 1)
    }
}

UNITSON