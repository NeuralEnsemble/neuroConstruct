#!/bin/bash

pull=false

function gitss () {
    tput setaf 5
    git rev-parse --abbrev-ref HEAD
    tput setaf 3
    git status -s
    tput setaf 2
    git fetch --dry-run
    git stash list
    tput setaf 9
}

function gitpp () {
    tput setaf 2
    git pull -p
    tput setaf 9
}


function hgss () {
    tput setaf 1
    hg status
    tput setaf 5
    hg in
    tput setaf 9
}

function hgpp () {
    tput setaf 3
    hg pull -u
    tput setaf 9
}


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
        gitpp
    else
        gitss
    fi

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
        hgpp
    else
        hgss
    fi

    cd $startDir
}


standardGHProject 'NeuroElectroSciUnit' 'showcase'
standardGHProject 'NengoNeuroML' 'showcase'
standardGHProject 'NeuroMorpho' 'showcase'
standardGHProject 'NSGPortalShowcase' 'showcase'
standardGHProject 'NIFShowcase' 'showcase'
standardGHProject 'simulator-test-data' 'showcase' 'mikehulluk'
standardGHProject 'NEURONShowcase' 'showcase' 
standardGHProject 'NESTShowcase' 'showcase' 
standardGHProject 'PyNNShowcase' 'showcase'
standardGHProject 'NetPyNEShowcase' 'showcase'
standardGHProject 'SBMLShowcase' 'showcase'
standardGHProject 'BrianShowcase' 'showcase'
standardGHProject 'BlueBrainProjectShowcase' 'showcase'
standardGHProject 'AllenInstituteNeuroML' 'showcase'
standardGHProject 'StochasticityShowcase' 'showcase'


standardGHProject 'FitzHugh-Nagumo' 'generic'
standardGHProject 'hodgkin_huxley_tutorial' 'generic' 'openworm'

standardGHProject 'CSAShowcase' 'showcase'
standardGHProject 'neuroConstructShowcase' 'showcase'
standardGHProject 'osb_vfb_showcase' 'showcase' 'jefferis'
standardGHProject 'ghk-nernst' 'showcase'

standardGHProject 'CElegansNeuroML' 'invertebrate/celegans' 'openworm'
standardGHProject 'CelegansNeuromechanicalGaitModulation' 'invertebrate/celegans'
standardGHProject 'muscle_model' 'invertebrate/celegans' 'openworm'

standardGHProject 'PyloricNetwork' 'invertebrate/lobster'
standardGHProject 'MorrisLecarModel' 'invertebrate/barnacle'
standardGHProject 'Drosophila_Projection_Neuron' 'invertebrate/drosophila'


standardGHProject 'CA1PyramidalCell' 'hippocampus/CA1_pyramidal_neuron'
standardGHProject 'FergusonEtAl2014-CA1PyrCell' 'hippocampus/CA1_pyramidal_neuron'
standardGHProject 'PinskyRinzelModel' 'hippocampus/CA3_pyramidal_neuron'
standardGHProject 'FergusonEtAl2013-PVFastFiringCell' 'hippocampus/interneurons'
standardGHProject 'WangBuzsaki1996' 'hippocampus/interneurons'



standardGHProject 'DentateGyrus2005' 'dentate_gyrus/networks'
standardBBProject 'dentate' 'dentate_gyrus/networks' 'mbezaire'
standardBBProject 'nc_ca1' 'hippocampus/networks' 'mbezaire'
standardBBProject 'nc_superdeep' 'hippocampus/networks' 'mbezaire'

standardGHProject 'StriatalSpinyProjectionNeuron' 'basal_ganglia/striatal_spiny_neuron'

standardGHProject 'MiglioreEtAl14_OlfactoryBulb3D' 'olfactorybulb/networks'
standardGHProject 'OlfactoryTest' 'olfactorybulb/multiple'

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
standardGHProject 'Cerebellum3DDemo' 'cerebellum/networks'
standardGHProject 'GranularLayerSolinasNieusDAngelo2010' 'cerebellum/networks'
standardGHProject 'BillingsEtAl2014_GCL_Models' 'cerebellum/networks' 'epiasini'

standardGHProject 'MainenEtAl_PyramidalCell' 'cerebral_cortex/neocortical_pyramidal_neuron'
standardGHProject 'RothmanEtAl_KoleEtAl_PyrCell' 'cerebral_cortex/neocortical_pyramidal_neuron'
standardGHProject 'L5bPyrCellHayEtAl2011' 'cerebral_cortex/neocortical_pyramidal_neuron'
standardGHProject 'LarkumEtAl2009' 'cerebral_cortex/neocortical_pyramidal_neuron'
standardGHProject 'FarinellaEtAl_NMDAspikes' 'cerebral_cortex/neocortical_pyramidal_neuron'
standardGHProject 'korngreen-pyramidal' 'cerebral_cortex/neocortical_pyramidal_neuron'
standardGHProject 'SmithEtAl2013-L23DendriticSpikes' 'cerebral_cortex/neocortical_pyramidal_neuron'
standardGHProject 'dLGNinterneuronHalnesEtAl2011' 'thalamus/lgn_interneuron'

standardGHProject 'IzhikevichModel' 'cerebral_cortex/networks'
standardGHProject 'Thalamocortical' 'cerebral_cortex/networks'
standardGHProject 'V1NetworkModels' 'cerebral_cortex/networks'

standardGHProject 'VERTEXShowcase' 'cerebral_cortex/networks'

standardGHProject 'ACnet2' 'cerebral_cortex/networks'
standardGHProject 'Brunel2000' 'cerebral_cortex/networks'
standardGHProject 'VogelsEtAl2011' 'cerebral_cortex/networks'
standardGHProject 'PospischilEtAl2008' 'cerebral_cortex/multiple'
standardGHProject 'WeilerEtAl08-LaminarCortex' 'cerebral_cortex/networks'
standardGHProject 'PotjansDiesmann2014' 'cerebral_cortex/networks'

cd $startDir



