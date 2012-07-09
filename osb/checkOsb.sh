
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

    if [ ! -d $1 ]; then
        git clone git@github.com:OpenSourceBrain/$1.git
    fi

    pushd $1 > /dev/null
    if $pull; then
        git pull
    else
        git status
        git fetch --dry-run
    fi

    popd > /dev/null
}


standardGHProject 'CA1PyramidalCell'
standardGHProject 'CerebellarNucleusNeuron'
standardGHProject 'GranCellLayer'
standardGHProject 'GranCellRothmanIf'
standardGHProject 'GranCellSolinasEtAl10'
standardGHProject 'GranuleCell'
standardGHProject 'GranuleCellVSCS'
standardGHProject 'IzhikevichModel'
standardGHProject 'MainenEtAl_PyramidalCell'
standardGHProject 'PurkinjeCell'
standardGHProject 'RothmanEtAl_KoleEtAl_PyrCell'
standardGHProject 'SolinasEtAl-GolgiCell'
#standardGHProject 'Thalamocortical'
standardGHProject 'VervaekeEtAl-GolgiCellNetwork'

popd > /dev/null



