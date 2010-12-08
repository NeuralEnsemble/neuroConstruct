# -*- coding: utf-8 -*-
'''

This file can be placed in the simulations directory of a neuroConstruct project and
when run it will search in all subdirectories for time.dat, and if it doesn't find it,
will try running pullsim.sh, which will attempt to retrieve the saved data from a remotely
executed simulation

'''

import os
import subprocess

path="."
pullSimFilename = "pullsim.sh"

dirList=os.listdir(path)

for fname in dirList:
    if os.path.isdir(fname):
        print "\n------   Checking directory: " + fname
        timeFile = fname+"/time.dat"
        pullsimFile = fname+"/"+pullSimFilename

        if os.path.isfile(timeFile):
            print "Time file exists! Simulation was successful."
        else:
            print "Time file doesn't exist!"

            if os.path.isfile(pullsimFile):
                print pullSimFilename+" exists and will be executed..."

                process = subprocess.Popen("cd "+fname+";./"+pullSimFilename, shell=True, stdout=subprocess.PIPE)
                stdout_value = process.communicate()[0]

                process.wait()
                print "Process has finished with return code: "+str(process.returncode)
                output = repr(stdout_value)
                formatted = output.replace("\\n", "\n\t")
                print 'Output from running '+pullSimFilename+':\n\t', formatted

                if os.path.isfile(timeFile):
                    print "Time file %s now exists, and so simulation was successful!"%timeFile
                else:
                    print "Time file doesn't exist! Simulation hasn't successfully finished yet."
            else:
                print "No "+pullsimFile+", so cannot proceed further..."



