
# Base python modules.  
import sys,os

# Neuroconstruct stuff.  
from constants import * # Neurounit constants.  
sys.path.append(os.path.join(NC_HOME,
	                         "pythonNeuroML",
	                         "nCUtils"))
from java.io import File
import ncutils
from ucl.physiol.neuroconstruct.neuron import NeuronException
from ucl.physiol.neuroconstruct.simulation import SimulationDataException

class Sim():
	"""Sane wrapper for the SimulationManager class in NeuroConstruct.
	Does not subclass. 
	Instead the SimulationManager is initialized to self.manager."""
	
	def __init__(self,project_path=None,sim_config_name=None):
		"""Assigns a NeuroConstruct SimulationManager instance to self.manager."""
		self.manager = self.get_sim_manager(project_path=project_path)
		if sim_config_name is None:
			sim_config_name = SIM_CONFIGS[0]
		self.set_config(sim_config_name)
		self.set_sim_dir()

	@classmethod
	def get_ncx_file(self,project_path):
		# Get a list of .ncx (neuroConstruct) files.  Should be only one for most projects.  
		ncx_files = [f for f in os.listdir(project_path) if f[-4:]=='.ncx']  
		ncx_file = os.path.join(project_path,ncx_files[0]) # Get full path to .ncx file.  
		return ncx_file

	@classmethod
	def get_sim_manager(self,project_path=None):
		"""Instantiates a NeuroConstruct SimulationManager class."""
		if project_path is None:
			project_path = os.path.join(OSB_MODELS, 
			                        	CELL_TYPE, 
			                        	MODEL_NAME,
			                        	"neuroConstruct") # Default OSB path. 
		'''
			file_name = MODEL_NAME + '.ncx' 

		else:
			project_path = os.path.abspath(project_path)
			base_path,project_dir = os.path.split(project_path)
			if project_dir == 'neuroConstruct':
				base_path,project_dir = os.path.split(base_path)
			file_name = project_dir + '.ncx'
		'''
		file_name = self.get_ncx_file(project_path)
		file_path = os.path.join(project_path,  
			                     file_name) # Location of an nC file.  
		f = File(file_path) 
		sim = ncutils.SimulationManager(f)
		return sim

	def set_config(self,sim_config_name):
		self.sim_config_name = sim_config_name

	def set_sim_dir(self):
		"""Returns a simulation directory."""
		sim_config_info = self.manager.project.simConfigInfo
		self.sim_config = sim_config_info.getSimConfig(self.sim_config_name) 
		sim_path = (self.sim_config_name+"_").replace(' ', '').replace(':', '')
		sim_path += '_'+SIMULATORS[0][0]
		main_dir = self.manager.project.getProjectMainDirectory()
		self.sim_dir = File(os.path.join(str(main_dir),
			                       "simulations",
			                       sim_path))
		
	def run(self):
		print "Running simulation."
		try:
			# Run with only one sim.
			self.manager.runMultipleSims(simulators=SIMULATORS,
										 runInBackground=True)   
		except NeuronException,e:
			print str(e)
		except Exception,e:
			raise e
		return True # Success.

	def get_sim_data(self,run=True):
		if run:
			self.run()
		err = ''
		if not os.path.isdir(self.sim_dir.path):
			sim_data = SimulationDataException('The simulation directory %s does not exist.' % self.sim_dir)
		else:
			try:
				sim_data = ncutils.SimulationData(self.sim_dir)
				sim_data.initialise()
			except Exception,e:
				raise e
		return sim_data

	def get_data_set(self,run=True):
		sim_data = self.get_sim_data(run=run)
		if sim_data:
			data_store = sim_data.getAllLoadedDataStores()[0] # Assume only one sim.  
			data_set = sim_data.getDataSet(data_store.getCellSegRef(), 
			                         data_store.getVariable(), 
			                         False)
		else:
			data_set = None
		return data_set

	"""Begin model interaction methods."""

	def get_volts(self,run=True):
		"""Gets a membrane potential trace."""
		try:
			data_set = self.get_data_set(run=run)
		except Exception,e:
			raise e
		volts = data_set.getYValues()
		return volts

	def set_current_ampl(self,current_ampl):
		"""Sets the amplitude of the injected current."""
		err = ""
		print "Current amplitude is ",current_ampl
		from ucl.physiol.neuroconstruct.utils import NumberGenerator
		sim_config_info = self.manager.project.simConfigInfo
		sim_config = sim_config_info.getSimConfig(self.sim_config_name)
		inputs = sim_config.getInputs()
		if len(inputs) < 1:
			err = "This sim config has no inputs"
		else:
			input_0 = inputs.get(0)
			stim = self.manager.project.elecInputInfo.getStim(input_0)
			if hasattr(stim,'setAmp'):
				stim.setAmp(NumberGenerator(current_ampl))
				self.manager.project.elecInputInfo.updateStim(stim)
			else:
				err = "This stim has no attribute 'setAmp'"
		print err
		return err

#if __name__ == "__main__":
#	volts = get_volts()

