
@echo off


REM  
REM
REM  PG:  This is a modified form of startxwin.bat used to launch genesis 
REM  PG:  simulations from neuroConstruct. It assumes cygwin is installed at
REM  PG:  C:\cygwin (get it from http://www.cygwin.com) and genesis is installed 
REM  PG:  at /usr/local/genesis i.e. C:\cygwin\usr\local\genesis (see
REM  PG:  http://www.genesis-sim.org/GENESIS/genesis-cygwin. 
REM  PG:  You'll need to have the xwin/xterm packages installed via cygwin.
REM
REM



SET DISPLAY=127.0.0.1:0.0


REM 
REM The path in the CYGWIN_ROOT environment variable assignment assume
REM that Cygwin is installed in a directory called 'cygwin' in the root
REM directory of the current drive.  You will only need to modify
REM CYGWIN_ROOT if you have installed Cygwin in another directory.  For
REM example, if you installed Cygwin in \foo\bar\baz\cygwin, you will need 
REM to change \cygwin to \foo\bar\baz\cygwin.
REM 
REM This batch file will almost always be run from the same drive (and
REM directory) as the drive that contains Cygwin/X, therefore you will
REM not need to add a drive letter to CYGWIN_ROOT.  For example, you do
REM not need to change \cygwin to c:\cygwin if you are running this
REM batch file from the C drive.
REM 

SET CYGWIN_ROOT=\cygwin
SET RUN=%CYGWIN_ROOT%\bin\run -p /usr/X11R6/bin

SET PATH=.;%CYGWIN_ROOT%\bin;%CYGWIN_ROOT%\usr\X11R6\bin;%PATH%

SET XAPPLRESDIR=/usr/X11R6/lib/X11/app-defaults
SET XCMSDB=/usr/X11R6/lib/X11/Xcms.txt
SET XKEYSYMDB=/usr/X11R6/lib/X11/XKeysymDB
SET XNLSPATH=/usr/X11R6/lib/X11/locale


REM
REM Cleanup after last run.
REM

if not exist %CYGWIN_ROOT%\tmp\.X11-unix\X0 goto CLEANUP-FINISH
attrib -s %CYGWIN_ROOT%\tmp\.X11-unix\X0
del %CYGWIN_ROOT%\tmp\.X11-unix\X0

:CLEANUP-FINISH
if exist %CYGWIN_ROOT%\tmp\.X11-unix rmdir %CYGWIN_ROOT%\tmp\.X11-unix


REM
REM The error "Fatal server error: could not open default font 'fixed'" is
REM caused by using a DOS mode mount for the mount that the Cygwin/X
REM fonts are accessed through.  See the Cygwin/X FAQ for more 
REM information:
REM http://x.cygwin.com/docs/faq/cygwin-x-faq.html#q-error-font-eof
REM

if "%OS%" == "Windows_NT" goto OS_NT

REM Windows 95/98/Me
echo startxwin.bat - Starting on Windows 95/98/Me

goto STARTUP

:OS_NT

REM Windows NT/2000/XP/2003
echo startxwin.bat - Starting on Windows NT/2000/XP/2003

:STARTUP

REM Brief descriptions of XWin-specific options:
REM
REM -screen scr_num [width height]
REM      Enable screen scr_num and optionally specify a width and
REM      height for that screen.
REM      Most importantly, any parameters specified before the first -screen
REM      parameter apply to all screens.  Any options after the first -screen
REM      parameter apply only to the screen that precedes the parameter.
REM      Example:
REM          XWin -fullscreen -screen 0 -screen 1 -depth 8 -screen 2
REM      All screens will be fullscreen, but screen 2 will be depth 8, while
REM      screens 0 and 1 will be the default depth (whatever depth Windows
REM      is currently running at).
REM -multiwindow
REM      Start an integrated Windows-based window manager.  Not to be used
REM      with -rootless nor -fullscreen.
REM -rootless
REM      Use a transparent root window with an external window manager
REM      (such as twm).  Not to be used with -multiwindow nor
REM      with -fullscreen.
REM -fullscreen
REM      Use a window as large as possible on the primary monitor.
REM -multiplemonitors
REM      Create a root window that covers all monitors on a
REM      system with multiple monitors.
REM -clipboard
REM      Enable the integrated version of xwinclip.  Do not use in
REM      conjunction with the xwinclip program.
REM -depth bits_per_pixel
REM      Specify the screen depth to run at (in bits per pixel) using a
REM      DirectDraw-based engine in conjunction with the -fullscreen
REM      option, ignored if the -fullscreen option is not specified.
REM      By default, you will be using a DirectDraw based engine on any
REM      system that supports it.
REM -unixkill
REM      Trap Ctrl+Alt+Backspace as a server shutdown key combination.
REM -nounixkill
REM      Disable Ctrl+Alt+Backspace as a server shutdown key combination (default).
REM      Example:
REM          XWin -unixkill -screen 0 -screen 1 -screen 2 -nounixkill
REM      Screens 0 and 1 will allow Ctrl+Alt+Backspace, but screen 2 will not.
REM -winkill
REM      Trap Alt+F4 as a server shutdown key combination (default).
REM -nowinkill
REM      Disable Alt+F4 as a server shutdown key combination.
REM -scrollbars
REM      Enable resizing of the server display window.  Do not use in conjunction
REM      with -multiwindow nor with -rootless.
REM -nodecoration
REM      Draw the server root window without a title bar or border.
REM      Do not use with -mutliwindow nor with -rootless.
REM -lesspointer
REM      Hide the Windows mouse cursor anytime it is over any part of the
REM      window, even if Cygwin/X is not the window with the focus.
REM -refresh rate_in_Hz
REM      Specify a refresh rate to use when used with the -fullscreen option.
REM -trayicon
REM      Enable the tray icon (default).
REM -notrayicon
REM      Disable the tray icon.
REM      Example:
REM          XWin -notrayicon -screen 0 -screen 1 -screen 2 -trayicon
REM      Screens 0 and 1 will not have tray icons, but screen 2 will.
REM -emulate3buttons [timeout]
REM      Emulate 3 button mouse with an optional timeout in milliseconds.
REM -xf86config
REM      Specify an XF86Config-style configuration file.
REM -keyboard
REM      Specify a keyboard device from the configuration file.


REM
REM Startup the programs
REM


REM Startup the X Server with the integrated Windows-based window manager.
REM WARNING: Do not use 'xwinclip' in conjunction with the ``-clipboard''
REM command-line parameter for XWin.  Doing so would start two clipboard
REM managers, which is never supposed to happen.

%RUN% XWin -multiwindow -clipboard -silent-dup-error


REM Startup an xterm, using bash as the shell.



REM PG:    ****  Only change to the original startxwin.bat ****


%RUN% xterm -e /usr/bin/bash -l -c '/usr/local/genesis/genesis.exe %1'


REM
REM Startup the twm window manager.
REM WARNING: Do not use an external window manager in conjunction with
REM the ``-multiwindow'' command-line parameter for XWin.  Doing so
REM would start two window managers, which is never supposed to happen.
REM

REM %RUN% twm


REM Set a background color.  Only needed when not using -multwindow for XWin.

REM %RUN% xsetroot -solid aquamarine4
