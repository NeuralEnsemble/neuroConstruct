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


startDir=$(pwd)

prefix='git@github.com:'


if [ -z "$USE_SSH_FOR_GITHUB" ]; then
    echo
    echo "If you use SSH to access GitHub, set the environment variable USE_SSH_FOR_GITHUB to 1"
    echo "This will clone GitHub repos using SSH. Using HTTPS instead"
    echo
    prefix='https://github.com/'
fi

prefixBB='https://bitbucket.org/'


standardGHProject()
{
    echo
    echo "-----  Checking:" $2/$1
    tput setaf 1

    if [ ! -d $2 ]; then
        parent=$2
        parent=${parent%/*}
        if [ ! -d $parent ]; then
            mkdir $parent
            echo "Making new directory: " $parent
            
        fi
        mkdir $2
        echo "Making new directory: " $2
        
    fi
    tgtDir=$startDir/$2/$1
    

    if [ ! -d $tgtDir ]; then
        echo "Cloning to: "$tgtDir
        if [ $# == 3 ]; then
            echo "Using repo: "$prefix$3/$1.git
            git clone $prefix$3/$1.git $tgtDir
        else
            osbOrg='OpenSourceBrain'
            echo "Using repo: "$prefix$osbOrg/$1.git
            git clone $prefix$osbOrg/$1.git $tgtDir
        fi
    fi

    cd $tgtDir
    if $pull; then
        git pull
    else
        git status
        tput setaf 3
        git fetch --dry-run
    fi
    tput sgr0

    cd $startDir
}


standardBBProject()
{
    echo
    echo "-----  Checking:" $2/$1
    tput setaf 1

    if [ ! -d $2 ]; then
        parent=$2
        parent=${parent%/*}
        if [ ! -d $parent ]; then
            mkdir $parent
            echo "Making new directory: " $parent

        fi
        mkdir $2
        echo "Making new directory: " $2

    fi
    tgtDir=$startDir/$2/$1


    if [ ! -d $tgtDir ]; then
        echo "Cloning to: "$tgtDir
        if [ $# == 3 ]; then
            echo "Using repo: "$prefixBB$3/$1
            hg clone $prefixBB$3/$1 $tgtDir
        else
            osbOrg='OpenSourceBrain'
            echo "Using repo: "$prefixBB$osbOrg/$1
            hg clone $prefixBB$osbOrg/$1 $tgtDir
        fi
    fi

    cd $tgtDir
    if $pull; then
        hg pull -u
    else
        hg status
        tput setaf 3
        hg in
    fi
    tput sgr0

    cd $startDir
}


standardGHProject 'NeuroElectroSciUnit' 'showcase'
standardGHProject 'NengoNeuroML' 'showcase'
standardGHProject 'NeuroMorpho' 'showcase'
standardGHProject 'NSGPortalShowcase' 'showcase'
standardGHProject 'NIFShowcase' 'showcase'
standardGHProject 'simulator-test-data' 'showcase' 'mikehulluk'
standardGHProject 'NEURONShowcase' 'showcase' 

standardGHProject 'CSAShowcase' 'showcase'
standardGHProject 'neuroConstructShowcase' 'showcase'
standardGHProject 'osb_vfb_showcase' 'showcase' 'jefferis'
standardGHProject 'ghk-nernst' 'showcase'

standardGHProject 'CElegansNeuroML' 'invertebrate/celegans' 'openworm'
#standardGHProject 'CelegansNeuromechanicalGaitModulation' 'invertebrate/celegans'
standardGHProject 'muscle_model' 'invertebrate/celegans' 'openworm'

standardGHProject 'PyloricNetwork' 'invertebrate/lobster'
standardGHProject 'MorrisLecarModel' 'invertebrate/barnacle'
standardGHProject 'Drosophila_Projection_Neuron' 'invertebrate/drosophila'

standardGHProject 'CA1PyramidalCell' 'hippocampus/CA1_pyramidal_neuron'
standardGHProject 'DentateGyrus2005' 'dentate_gyrus/networks'
standardBBProject 'dentate' 'dentate_gyrus/networks' 'mbezaire'
standardBBProject 'nc_ca1' 'hippocampus/networks' 'mbezaire'

standardGHProject 'StriatalSpinyProjectionNeuron' 'basal_ganglia/striatal_spiny_neuron'

standardGHProject 'SolinasEtAl-GolgiCell' 'cerebellum/cerebellar_golgi_cell'

standardGHProject 'CerebellarNucleusNeuron' 'cerebellum/cerebellar_nucleus_cell'

standardGHProject 'GranCellRothmanIf' 'cerebellum/cerebellar_granule_cell'
standardGHProject 'GranCellSolinasEtAl10' 'cerebellum/cerebellar_granule_cell'
standardGHProject 'GranuleCell' 'cerebellum/cerebellar_granule_cell'
standardGHProject 'GranuleCellVSCS' 'cerebellum/cerebellar_granule_cell'
standardGHProject 'cereb_grc_mc' 'cerebellum/cerebellar_granule_cell'


standardGHProject 'PurkinjeCell' 'cerebellum/cerebellar_purkinje_cell'

standardGHProject 'GranCellLayer' 'cerebellum/networks'
standardGHProject 'VervaekeEtAl-GolgiCellNetwork' 'cerebellum/networks'

standardGHProject 'MainenEtAl_PyramidalCell' 'cerebral_cortex/neocortical_pyramidal_neuron'
standardGHProject 'RothmanEtAl_KoleEtAl_PyrCell' 'cerebral_cortex/neocortical_pyramidal_neuron'
standardGHProject 'L5bPyrCellHayEtAl2011' 'cerebral_cortex/neocortical_pyramidal_neuron'
standardGHProject 'LarkumEtAl2009' 'cerebral_cortex/neocortical_pyramidal_neuron'
standardGHProject 'korngreen-pyramidal' 'cerebral_cortex/neocortical_pyramidal_neuron'

standardGHProject 'IzhikevichModel' 'cerebral_cortex/networks'
standardGHProject 'Thalamocortical' 'cerebral_cortex/networks'
standardGHProject 'ACnet2' 'cerebral_cortex/networks'
standardGHProject 'Brunel2000' 'cerebral_cortex/networks'
standardGHProject 'VogelsEtAl2011' 'cerebral_cortex/networks'
standardGHProject 'PospischilEtAl2008' 'cerebral_cortex/multiple'

cd $startDir



