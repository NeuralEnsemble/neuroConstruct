@echo off

echo Starting check for some OSB projects...

FOR /F "tokens=*" %%i IN (osbRepos) DO call :standardGHProject %%i %1

GOTO :EOF



REM ***********************************
REM *** Function Definitions

:standardGHProject
    echo -----------------------------------------------
    REM echo ---- Params: %~1 %~2 %~3
    if not exist %~2 (
        echo Making new dir: %~2
        mkdir %~2
    )
    
    set BACK_DIR=%cd%
    set TGT_DIR=%cd%\%~2\%~3
    
    if "%3"=="-" (
       set TGT_DIR=%cd%\%~2
    )
    
    if not exist %TGT_DIR% (
        echo Making new dir: %TGT_DIR%
        mkdir %TGT_DIR%
    )
    
    if not exist %TGT_DIR%\%~1 (
        echo Cloning: %~1 to %TGT_DIR%\%~1
        git clone https://github.com/OpenSourceBrain/%~1.git %TGT_DIR%/%~1
    )
    echo.
    echo ------  Checking: %TGT_DIR%\%~1
    cd %TGT_DIR%\%~1
    
    if "%4"=="-u" (
        call git pull
    ) else (
        call git status
        call git fetch --dry-run
    )
    
    cd %BACK_DIR%

    goto :EOF
    
REM *** End :standardGHProject