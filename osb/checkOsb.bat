@echo off

echo Starting check for OSB projects...

FOR /F "tokens=*" %%i IN (osbRepos) DO call :standardGHProject %%i %1

GOTO :EOF


REM ***********************************
REM *** Main

call :standardGHProject CA1PyramidalCell hippocampus CA1_pyramidal_neuron %1
call :standardGHProject SolinasEtAl-GolgiCell cerebellum cerebellar_golgi_cell %1

call :standardGHProject CerebellarNucleusNeuron cerebellum cerebellar_nucleus_cell %1

call :standardGHProject GranCellRothmanIf cerebellum cerebellar_granule_cell %1
call :standardGHProject GranCellSolinasEtAl10 cerebellum cerebellar_granule_cell %1
call :standardGHProject GranuleCell cerebellum cerebellar_granule_cell %1
call :standardGHProject GranuleCellVSCS cerebellum cerebellar_granule_cell %1

call :standardGHProject PurkinjeCell cerebellum cerebellar_purkinje_cell %1

call :standardGHProject GranCellLayer cerebellum networks %1
call :standardGHProject VervaekeEtAl-GolgiCellNetwork cerebellum networks %1

call :standardGHProject MainenEtAl_PyramidalCell cerebral_cortex neocortical_pyramidal_neuron %1
call :standardGHProject RothmanEtAl_KoleEtAl_PyrCell cerebral_cortex neocortical_pyramidal_neuron %1

call :standardGHProject IzhikevichModel cerebral_cortex networks %1
call :standardGHProject Thalamocortical cerebral_cortex networks %1

goto :EOF

REM ***********************************
REM *** Function Definitions

:standardGHProject
    echo -----------------------------------------------
    REM echo ---- Params: %~1 %~2 %~3
    if not exist %~2 (
        echo Making new dir: %~2
        mkdir %~2
    )
    if not exist %~2\%~3 (
        echo Making new dir: %~2\%~3
        mkdir %~2\%~3
    )
    
    if not exist %~2\%~3\%~1 (
        echo Cloning: %~1 to %~2\%~3\%~1
        git clone https://github.com/OpenSourceBrain/%~1.git %~2/%~3/%~1
    )
    echo.
    echo ------  Checking: %~2\%~3\%~1
    cd %~2\%~3\%~1
    
    if "%4"=="-u" (
        call git pull
    ) else (
        call git status
        call git fetch --dry-run
    )
    cd ..\..\..

    goto :EOF
    
REM *** End :standardGHProject