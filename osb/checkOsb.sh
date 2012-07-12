#!/bin/bash

pull=false


if [ $# -eq 1 ] ; then
    if [ $1 == '-u' ]; then
        pull=true
        echo "Pulling latest OSB..."
    else
        echo "Checking latest OSB..."
    fi
fi


pushd $HOME/neuroConstruct/osb > /dev/null


standardGHProject()
{
    echo
    echo "-----  Checking:" $1
    tput setaf 1

    if [ ! -d $2 ]; then
        parent=$2
        parent=${parent%/*}
        if [ ! -d $parent ]; then
            mkdir $parent
        fi
        mkdir $2
        
    fi
    tgtDir=$2/$1
    
    if [ ! -d $tgtDir ]; then
        git clone git@github.com:OpenSourceBrain/$1.git $tgtDir
    fi

    pushd $tgtDir > /dev/null
    if $pull; then
        git pull
    else
        git status
        tput setaf 2
        git fetch --dry-run
    fi
    tput sgr0

    popd > /dev/null
}


standardGHProject 'CA1PyramidalCell' 'hippocampus/CA1_pyramidal_neuron'

standardGHProject 'SolinasEtAl-GolgiCell' 'cerebellum/cerebellar_golgi_cell'

standardGHProject 'CerebellarNucleusNeuron' 'cerebellum/cerebellar_nucleus_cell'

standardGHProject 'GranCellRothmanIf' 'cerebellum/cerebellar_granule_cell'
standardGHProject 'GranCellSolinasEtAl10' 'cerebellum/cerebellar_granule_cell'
standardGHProject 'GranuleCell' 'cerebellum/cerebellar_granule_cell'
standardGHProject 'GranuleCellVSCS' 'cerebellum/cerebellar_granule_cell'

standardGHProject 'PurkinjeCell' 'cerebellum/cerebellar_purkinje_cell'

standardGHProject 'GranCellLayer' 'cerebellum/networks'
standardGHProject 'VervaekeEtAl-GolgiCellNetwork' 'cerebellum/networks'

standardGHProject 'MainenEtAl_PyramidalCell' 'cerebral_cortex/neocortical_pyramidal_neuron'
standardGHProject 'RothmanEtAl_KoleEtAl_PyrCell' 'cerebral_cortex/neocortical_pyramidal_neuron'

standardGHProject 'IzhikevichModel' 'cerebral_cortex/networks'
standardGHProject 'Thalamocortical' 'cerebral_cortex/networks'


popd > /dev/null



