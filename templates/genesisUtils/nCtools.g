/*
 *
 * A neuroConstruct helper file of some simple utilities for checking the
 * morphologies of cells in GENESIS
 *
 * Some of these functions provide NEURON like output, for comparing the simulators
 *
 */

str cellsRoot
cellsRoot = "/cells"

str cellWildcard
cellWildcard = "/cells/#/#"

str plotsRoot
plotsRoot = "/plots"

str stimRoot
stimRoot = "/stim"

str rndspikeRoot
rndspikeRoot = {{stimRoot} @ "/rndspike"}

str pulseRoot
pulseRoot = {{stimRoot} @ "/pulse"}





/*
 * Provides information on all of the created Cells
 */
function allinfo

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"
showfield {cellsRoot}/##[][TYPE=compartment],{cellsRoot}/##[][TYPE=symcompartment] *
echo "+++++++++++++++++++++++++++++++++++++++++++++++++"

end


/*
 * Prints voltage of all of the created compartments
 */
function allv

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"

str compName
foreach compName ({el {cellsRoot}/##[][TYPE=compartment],{cellsRoot}/##[][TYPE=symcompartment]})

    if ({exists {compName}/../solve})
        if ({getfield {compName}/../solve chanmode} > 2)
            echo "Voltage (via findsolvefield) of " {compName} ": " {getfield {compName}/../solve {findsolvefield {compName}/../solve {compName} Vm}}
        else
            echo "Voltage of " {compName} ": " {getfield {compName} Vm}
        end
    else
        echo "Voltage of " {compName} ": " {getfield {compName} Vm}
    end
end

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"

end



/*
 * Prints info on all stimulations
 */
function allstims

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"

str stimName
foreach stimName ({el {pulseRoot}/#})

    echo Stimulation: {stimName}
    showfield {stimName} *
    showmsg {stimName}

end

foreach stimName ({el {rndspikeRoot}/#})

    echo Stimulation: {stimName}
    showfield {stimName} *
    showmsg {stimName}

end

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"

end




/*
 * Prints info on some global variables
 */
function env

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"
echo "Simulation temperature:  " {celsius}

echo "Unit system used:        " {units}
echo "+++++++++++++++++++++++++++++++++++++++++++++++++"

end




/*
 * Provides information on the morphology of the current accessed section
 */
function morph

float totalarea = 0
echo "+++++++++++++++++++++++++++++++++++++++++++++++++"

echo "Not implemented yet..."

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"

end





/*
 * Prints info on all chans
 */
function allchans

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"

str compName
str chanName
foreach compName ({el {cellsRoot}/##[][TYPE=compartment],{cellsRoot}/##[][TYPE=symcompartment]})

    echo "--  Checking compartment: " {compName}


    foreach chanName ({el {compName}/##[][TYPE=hh_channel]})

        showfield {chanName} Ik Gk Ek Gbar surface
    end

    foreach chanName ({el {compName}/##[][TYPE=tabchannel]})

        showfield {chanName} Ik Gk Ek Gbar X Y Z surface
    end

    foreach chanName ({el {compName}/##[][TYPE=tab2Dchannel]})

        showfield {chanName} Ik Gk Ek Gbar X Y Z surface
    end

    foreach chanName ({el {compName}/##[][TYPE=Ca_concen]})

        showfield {chanName} *
    end

    foreach chanName ({el {compName}/##[][TYPE=leakage]})

        showfield {chanName} *
    end

    echo "-----"
end

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"

end


/*
 * Prints info on all synaptic connections
 */
function allsyns

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"

str cellName
str compName
str synName
str sourceName
str sourceType
int i

foreach cellName ({el {cellWildcard}})

    echo "---------    Checking cell: " {cellName}

    foreach compName ({el {cellName}/#[][TYPE=compartment],{cellName}/#[][TYPE=symcompartment]})

        echo "--  Checking compartment: " {compName}

        foreach synName ({el {compName}/#[][TYPE=synchan],{compName}/#[][TYPE=synchan2]})

            echo "--  Synapse object found: " {synName}

            int numIncoming = {getmsg {synName} -in -count}

            for (i = 0; i < numIncoming; i = i + 1)

                sourceType = {getmsg {synName} -in -source {i} -type {i}}

                if ({sourceType}=="SPIKE")

                    sourceName = {getmsg {synName} -in -source {i}}

                    echo "  Presynaptic properties: ("{sourceName}")"
                    echo "    " -nonewline 
                    str fieldname

                    foreach fieldname ( {getfieldnames {sourceName} })
                        // just choose some relevant ones...
                        if (({fieldname} == "name") || ({fieldname} == "rate") ||  ({fieldname} == "state") ||  ({fieldname} == "abs_refract") || ({fieldname} == "reset_value") || ({fieldname} == "min_amp") || ({fieldname} == "thresh") ||  ({fieldname} == "output_amp"))
                            echo {fieldname}: {getfield {sourceName} {fieldname}} " " -nonewline 
                        end
                    end
                    echo " "
                end

            end


            echo "  Postsynaptic properties: "
            echo "    gmax: " {getfield {synName} gmax} ", Ek: " {getfield {synName} Ek} ", tau1: " {getfield {synName} tau1} ", tau2: " {getfield {synName} tau2} ", frequency: " {getfield {synName} frequency}

            int numSyns = {getfield {synName} nsynapses}
            for (i = 0; i < numSyns; i = i + 1)
                echo "    Synapse "{i}":     Weight: " {getfield {synName} synapse[{i}].weight} ",    Delay: " {getfield {synName} synapse[{i}].delay}

            end


        end

    end


    echo "---------  "
    echo "  "

end

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"

end



/*
 * Provides information on the plots. Useful for resizing etc.
 */
function allplots

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"

str plotName
str xgraphName
str dataSetName

foreach plotName ({el {plotsRoot}/##[][TYPE=xform]})

    echo "--  Checking Plot frame: " {plotName}
    echo "--  Title: " {getfield {plotName} title}
    echo "-- "

    foreach xgraphName ({el {plotName}/##[][TYPE=xgraph]})

        echo "--  xgraph element: "  {xgraphName}
        echo "--  Min x: " {getfield {xgraphName} xmin} ", max x: " {getfield {xgraphName} xmax}
        echo "--  Min y: " {getfield {xgraphName} ymin} ", max y: " {getfield {xgraphName} ymax}

        foreach dataSetName ({el {xgraphName}/##[][TYPE=xplot]})

            echo "--     Plot Frame contains graph: " {dataSetName}
        end
    end

    echo "------------------"
    echo ""
end

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"

end






/*
 * Provides information on the total area, etc.
 * Useful as a simple test for comparing cells
 */
function areainfo

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"
int totalnum = 0
float totalarea = 0
float totallength = 0

  str name
  float area
  float PI = 3.14159265

  foreach name ({el {cellsRoot}/##[][TYPE=compartment],{cellsRoot}/##[][TYPE=symcompartment]})

    totalnum = totalnum +1

    //echo len: {getfield {name} len}

    if ({getfield {name} len} == 0)

        area = {PI}*{getfield {name} dia}*{getfield {name} dia}

    else
        area = {PI}*{getfield {name} dia}*{getfield {name} len}
        totallength = totallength + {getfield {name} len}
    end

        totalarea = totalarea + area
        echo "Compartment: " {name} ", area: " {area} ", length: " {getfield {name} len}
  end


    echo "+    Number of compartments:"  {totalnum}
    echo "+    Total area all sections: ", {totalarea}
    echo "+    Total length all sections: ", {totallength}


echo "+++++++++++++++++++++++++++++++++++++++++++++++++"
end





/*
 * Provides information on the current element similar to that produced
 * with psection() in NEURON
 */
function neu

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"

echo "Not implemented yet..."

echo "+++++++++++++++++++++++++++++++++++++++++++++++++"
end







