## Installation

To install neuroConstruct from source for any of these platforms you'll need:

- Java (http://www.oracle.com/technetwork/java/javase/downloads/index.html)

### Installation using binary release

1) Get the latest binary (including precompiled jar file) release from https://github.com/NeuralEnsemble/neuroConstruct/releases

2) Unzip the file (e.g. **neuroConstruct_1.7.2.zip**) to your C:\ (Windows) or home directory (Linux/Mac). Make sure the directory is named **neuroConstruct** not **neuroConstruct_1.7.2** etc.

3) Open a terminal (Start menu and type cmd in Windows; Terminal under Applications/Utilities in Mac; Linux your favourite terminal)

4) Go to the neuroConstruct directory and run the nC script. On Windows:
```
cd C:\neuroConstruct
nC.bat
```
You may aso be able to run the application directly by double clicking the file nC (or nC.bat) in C:\neuroConstruct in the file explorer.

On Mac or Linux:
```
cd ~/neuroConstruct
bash nC.sh
```
 

### Installation from source

- Ant (http://ant.apache.org/)
- Git (http://git-scm.com/)
- Subversion (http://subversion.apache.org/)

To run simulations generated from neuroConstruct, try

- NEURON (easiest to install, http://www.neuron.yale.edu/neuron)

or

- GENESIS (http://genesis-sim.org)
- MOOSE (http://moose.sourceforge.net)
- PSICS (http://www.psics.org)


### Linux/Mac

Mac Pre-requisites: 
- XCode command line tools downloaded and installed (https://developer.apple.com/xcode/)

Quick install:

	git clone git://github.com/NeuralEnsemble/neuroConstruct.git
	cd neuroConstruct
	./updatenC.sh

To get the Open Source Brain models:

	cd osb
	./checkOsb.sh -u

To build & run (using Apache Ant)

	ant run

To build & run (without Ant)

	<< open nC.sh and change NC_HOME >>
	./nC.sh -make
	./nC.sh

### Windows

Install the packages listed above or check that they're already installed on your system

Quick install:

    git clone git://github.com/NeuralEnsemble/neuroConstruct.git
    cd neuroConstruct
    updatenC.bat

To get the Open Source Brain models:

    cd osb
    checkOsb.bat -u

To build & run (using Apache Ant)

    ant run

To build & run (without Ant)

    << open nC.bat and change NC_HOME >>
    nC.bat -make
    nC.bat


 

