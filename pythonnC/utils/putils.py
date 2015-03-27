from .constants import *
import neurotools as nc_neurotools# Interface to NeuralEnsemble NeuroTools.  

import os,subprocess,time,socket,platform,types
import execnet # From PyPI; for communication with jython.  
IMPLEMENTATION = platform.python_implementation()

JYTHON = IMPLEMENTATION == 'Jython'
# True if this module is loaded in Jython, False if CPython.
# If it is being run in Jython, we can use NeuroConstruct's python 
# classes directly.  If it is not than we must use execnet to interact
# with a separate jython instance. The former has the advantage of 
# simplicity and better interaction with the models.  The latter has
# the advantage of being able to use CPython modules like numpy.  
PROC = None
x = 1

def get_environment_variables():
  script_path = os.path.join(NC_HOME,"nC_env.sh")
  PROC = subprocess.Popen([script_path,"-printenv"],stdout=subprocess.PIPE)
  env_vars = PROC.stdout.readlines()
  PROC.terminate()
  env = {}
  for env_var in env_vars:
    if "=" in env_var:
      var,value = env_var.split('=')[:2]
      env[var] = value.replace('\n','')
  return env

def is_socket_open(ip,port):
  s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
  try:
    s.connect((ip, int(port)))
    s.shutdown(2)
    return True
  except:
    return False

def open_gateway(useSocket=True,automatic_socket=AUTOMATIC_SOCKET):
  global PROC 
  #print("PROC is None?",PROC is None)
  if PROC is not None and (automatic_socket or PROC.poll() is None): 
    # A previous PROC is still running.  
    try:
      print("Terminating subprocess used to create previous socket.")
      PROC.terminate()
    except ProcessLookupError:
      pass
  if useSocket:
    print(os.environ['PATH'],236246)
    if automatic_socket:
      script_path = os.path.join(NC_HOME,"nCjython.sh")
      # Will need to close this later.
      now = int(time.time())
      log = open(os.path.join(NC_HOME,"logs/%d.txt" % now),'w')  
      PROC = subprocess.Popen([script_path,"-socket"],
                              stdin=subprocess.PIPE,
                              stdout=log,
                              stderr=subprocess.STDOUT,
                              env={'HOME':HOME,
                                   'NC_HOME':NC_HOME,
                                   'JYTHON_HOME':JYTHON_HOME,
                                   'PATH':os.environ['PATH'],
                                   'PYTHONPATH':os.environ['PYTHONPATH']})
      print(PROC)
      #PROC = subprocess.call(script_path+" -socket",shell=True)  
    tries = 0
    while 1:
      if tries>MAX_TRIES:
        print("Reached maximum number of tries. Socket not open.")
        gw = None
        break
      else:
        try:
          gw = execnet.makegateway("socket=127.0.0.1:%d" % EXECNET_SOCKET_PORT)
        except Exception as e:
          #if tries > 5:
          #raise e
          print("Waiting for socket to be open on port %d..." % \
                  EXECNET_SOCKET_PORT)
          time.sleep(1)
          tries += 1
          pass
        else:
          break
  else:
    env = get_environment_variables()
    os.environ.update(env)
    gw = execnet.makegateway("popen//python=jython")#//%s" % env_str)
  gw.PROC = PROC
  def terminate(self):
    self.PROC.terminate()
  gw.terminate = types.MethodType(terminate,gw)
  return gw


class Channel(object):
    """Generic object to act like an execnet channel, 
    but for us without execnet."""

    def __init__(self):
      self.obj = None
    
    def send(self,obj):
      self.obj = obj
    
    def receive(self):
      obj = self.obj
      self.obj = None
      return obj


def run_sim(project_path=None,
            useSocket=True,
            useNC=True,
            useNeuroTools=True,
            runtime_methods={},
            gw=None,
            **kwargs):
  """Runs a NeuroConstruct simulation by using execnet to call
  jython code that wraps uses the NeuroConstruct simulation manager."""
  
  if JYTHON:
    print("Running in Jython")
    channel = Channel()
    jython_side(channel,
                project_path=project_path,
                useNC=useNC,
                useNeuroTools=useNeuroTools,
                runtime_methods=runtime_methods,
                **kwargs)
  else:
    print("Runtime methods are %s" % runtime_methods)
    global PROC
    if gw is None:
      gw = open_gateway(useSocket=useSocket)
    for key,value in kwargs.items():
      pass
    channel = gw.remote_exec(jython_side,
                             project_path=project_path,
                             useNC=useNC,
                             useNeuroTools=useNeuroTools,
                             runtime_methods=runtime_methods,
                             **kwargs)
    if useNeuroTools:
      print("Receiving sim dir")
      simDir = channel.receive()
      if 'SimulationDataException:' in simDir:
        #print simDir
        raise IOError(simDir)
      print("Received sim dir")
      data = simDir
    else:
      volts = channel.receive()
      data = volts
    if PROC and AUTOMATIC_SOCKET:
      PROC.terminate()
  return data

'''
def init_sim_gateway():
  """Create a jutils.Sim instance on the jython side."""
  gw = open_gateway()
  gw.remote_exec(jython_init)
  return gw

def jython_init(channel,**kwargs):
  import sys,os
  import jutils
  NC_HOME = os.environ["NC_HOME"] 
  os.chdir(NC_HOME)
  # This path contains the .jar with all the nC java classes.  
  sys.path.append(NC_HOME)
  # This path contains the utils module.  
  sys.path.append(os.path.join(NC_HOME,"pythonnC")) 
    
  from ucl.physiol.neuroconstruct.utils import ClassLogger
  logger = ClassLogger("JythonOut")
  logger.logComment("Starting...")
  
  global sim
  sim = jutils.Sim()
  
  return "Sim initialized."
'''

def jython_side(channel,
                project_path=None,
                useNC=True,
                useNeuroTools=True,
                runtime_methods={},
                **kwargs):
  """A jython function executed on the remote side of the execnet gateway."""
  import sys,os,inspect
  import pythonnC.utils.jutils as j

  NC_HOME = os.environ["NC_HOME"] 
  print('NC_HOME:%s' % NC_HOME)
  os.chdir(NC_HOME)
  # This path contains the .jar with all the nC java classes.  
  sys.path.append(NC_HOME)
  # This path contains the utils module.  
  sys.path.append(os.path.join(NC_HOME,"pythonnC")) 
  
  from ucl.physiol.neuroconstruct.utils import ClassLogger
  from ucl.physiol.neuroconstruct.simulation import SimulationDataException
  logger = ClassLogger("JythonOut")
  logger.logComment("Starting...")
  
  '''
  sim = jutils.Sim(project_path=project_path)
  sim_methods = inspect.getmembers(jutils.Sim, predicate=inspect.ismethod)
  sim_methods = zip(*sim_methods)[0]
  print sim_methods
  print "A"
  for key,value in runtime_methods.items():
    print "B",value,value[0]
    if key in sim_methods:
      print "C"
    # If a jutils.Sim method is one of the keys.  
      method = getattr(sim,key)
      if len(value) == 1:
        print "D"
        if type(value) in (tuple,list):
          print "E"
          args = tuple(value[0])
          print "F"
          kwargs = {}
        elif type(value) is dict:
          
          args = ()
          kwargs = value[0]
      elif len(value) == 2:
        args = tuple(value[0])
        kwargs = value[1]
      print "G"
      logger.logComment("Running method %s with args %s and kwargs %s",
                        key,
                        str(args),
                        str(kwargs),)
      print "Method is ",method
      print "Args are ",args
      print "Kwargs are ",kwargs
      method(*args,**kwargs)
  '''
  print("About to run sim.")
  import os
  print(os.environ)
  print(sys.path)
  j.sim.run()
  print("Just ran sim.")

  if useNeuroTools:
    # If using NeuroTools, just send the directory name and extract data
    # using NeuroTools methods.
    print("Getting sim data.")  
    sim_data = j.sim.get_sim_data(run=False)
    if isinstance(sim_data,SimulationDataException):
      print("Error: %s" % sim_data)
      channel.send(str(sim_data))
    elif not sim_data:
      print("No sim data.")
      channel.send('')
    else:
      print("Got sim data.")  
      sim_dir = sim_data.getSimulationDirectory()
      logger.logComment("Here is the sim directory:")
      logger.logComment(str(sim_dir))
      print("Sending sim dir.")
      channel.send(str(sim_dir))
      logger.logComment("Sent sim directory")
      print("Sent sim dir.")
    if useNC:
      # If using direction communication with NeuroConstruct, return the 
      # voltage array directly.  
      real_volts = j.sim.get_volts(run=False) 
      volts = real_volts 
    else:
      # If not, make some fake voltage data in the same format.  
      import array
      volts = array.array('d',[1.0,2.0,3.0])
    
    # Must convert to list because execnet can't send numpy array.  
    volts = list(volts) 
    logger.logComment("Here are the voltages:")
    logger.logComment(volts.__str__())
    channel.send(volts)
    logger.logComment("Sent volts")
    #sys.exit(0)  

'''
def get_vm(project_path=None,
           sim_path=None,
           population_name=POPULATION_NAME,
           **kwargs):
  
  if sim_path is None: # If no sim path is provided, run the simulation first.  
    sim_path = run_sim(project_path=project_path,
                       useNeuroTools=True,
                       **kwargs)
  vm = nc_neurotools.get_analog_signal(sim_path,population_name) # An AnalogSignal instance.  
  return vm
'''

def plot_vm(vm=None):
  import pylab
  import matplotlib.pyplot

  if vm is None:
    vm = get_vm()
  times = nc_neurotools.get_times()
  pop_name = POPULATION_NAME

  figure = matplotlib.pyplot.figure()
  figure.suptitle(pop_name)
  my_plot = figure.add_subplot(111)
  pylab.ylabel('Membrane potential (mV)')
  pylab.xlabel('Time (ms)')
  volts = vm.signal
  my_plot.plot(times,
               volts,
               '-', 
               label=pop_name, 
               linewidth=1)

  pylab.show()
  return vm

if __name__ == '__main__':
  data = run_sim()
  print(data)
