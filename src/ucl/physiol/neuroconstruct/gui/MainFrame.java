 /**
 *  neuroConstruct
 *  Software for developing large scale 3D networks of biologically realistic neurons
 * 
 *  Copyright (c) 2009 Padraig Gleeson
 *  UCL Department of Neuroscience, Physiology and Pharmacology
 *
 *  Development of this software was made possible with funding from the
 *  Medical Research Council and the Wellcome Trust
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.va

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *+
 */

package ucl.physiol.neuroconstruct.gui;

import java.beans.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.zip.*;
import java.lang.management.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import javax.xml.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;


import javax.swing.text.Document;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.*;
import ucl.physiol.neuroconstruct.cell.converters.*;
import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.dataset.*;
import ucl.physiol.neuroconstruct.genesis.*;
import ucl.physiol.neuroconstruct.psics.*;
import ucl.physiol.neuroconstruct.pynn.*;
import ucl.physiol.neuroconstruct.gui.plotter.*;
import ucl.physiol.neuroconstruct.hpc.condor.*;
import ucl.physiol.neuroconstruct.hpc.mpi.*;
import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.neuroml.NeuroMLConstants.*;
import ucl.physiol.neuroconstruct.neuroml.LemsConstants.*;
import ucl.physiol.neuroconstruct.neuroml.hdf5.Hdf5Exception;
import ucl.physiol.neuroconstruct.neuron.*;

import ucl.physiol.neuroconstruct.nmodleditor.processes.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.*;
import ucl.physiol.neuroconstruct.project.cellchoice.*;
import ucl.physiol.neuroconstruct.project.packing.*;
import ucl.physiol.neuroconstruct.pynn.PynnFileManager.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.utils.xml.*;

/**
 * The big class, the main neuroConstruct frame, lots of GUI stuff. A lot of the
 * non gui specific stuff should be moved to ProjectManager
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class MainFrame extends JFrame implements ProjectEventListener, GenerationReport, HyperlinkListener
{
    ClassLogger logger = new ClassLogger("MainFrame");

    public ProjectManager projManager = new ProjectManager(this, this);

    Base3DPanel base3DPanel = null;

    boolean initialisingProject = false;

    ToolTipHelper toolTipText = ToolTipHelper.getInstance();

    RecentFiles recentFiles = RecentFiles.getRecentFilesInstance(ProjectStructure.getNeuConRecentFilesFilename());

    String PROJECT_INFO_TAB = "Project";
    String CELL_TYPES_TAB = "Cell Types";
    String REGIONS_TAB = "Regions";
    String CELL_GROUPS_TAB = "Cell Groups";
    String CELL_MECHANISM_TAB = "Cell Mechanisms";
    String NETWORK_TAB = "Network";
    String GENERATE_TAB = "Generate";
    String VISUALISATION_TAB = "Visualisation";
    String EXPORT_TAB = "Export";

    String SIMULATOR_SETTINGS_TAB = "Common Simulation Settings";
    String INPUT_OUTPUT_TAB = "Input/Output";

    String NEURON_SIMULATOR_TAB = "NEURON";
    String NEURON_TAB_GENERATE = "Generate code";
    String NEURON_TAB_EXTRA = "Extra hoc code";


    String GENESIS_SIMULATOR_TAB = "GENESIS";
    String GENESIS_TAB_GENERATE = "Generate code";
    String GENESIS_TAB_EXTRA = "Extra GENESIS code";
    
    String PSICS_SIMULATOR_TAB = "PSICS";
    
    String PYNN_SIMULATOR_TAB = "PyNN";

    String MORPHML_TAB = "NeuroML";

    OptionsFrame optFrame = null;

    // needed for changing panel at stim settings...
    // IClamp stuff:
    JTextField jTextFieldIClampAmplitude = new JTextField(12);
    JTextField jTextFieldIClampDuration = new JTextField(12);
    // NetStim stuff:
    JTextField jTextFieldNetStimNumber = new JTextField(12);
    JTextField jTextFieldNetStimNoise = new JTextField(12);

    String defaultAnalyseCellGroupString = "-- Please select a Cell Group --";
    String defaultAnalyseNetConnString = "-- Please select a Network Connection --";
    
    String generatePleaseWait = "Generating cell positions and network connections. Please wait...";


    // For figuring out whether the positions were generated anew, or were from prev simulations
    private final static int NO_POSITIONS_LOADED = 0;
    private final static int GENERATED_POSITIONS = 1;
    //private final static int RECORDED_POSITIONS = 2;
    //private final static int STORED_POSITIONS = 3;
    private final static int RELOADED_POSITIONS = 19;
    private final static int NETWORKML_POSITIONS = 21;

    int sourceOfCellPosnsInMemory = NO_POSITIONS_LOADED;

    //String currentlyLoadedSimRef = null;


    public static final String LATEST_GENERATED_POSITIONS = "Latest Generated Positions";

    String choice3DChoiceMain  = "     -- Please select: --";
    String choice3DSingleCells = "     -- Cell Types: --";
    String choice3DPrevSims    = "     -- NEURON Simulations: --";


    String defaultCellTypeToView = "-- Select a Cell Type --";

    String noNeuroMLFilesFound = "-- No NeuroML files found --";

    String defaultNeuronFilesText = "-- Select NEURON file to view --";
    String defaultGenesisFilesText = "-- Select GENESIS file to view --";
    String defaultPyNNFilesText = "-- Select PyNN file to view --";
    String defaultPsicsFilesText = "-- Select PSICS file to view --";

    //private int neuronRunMode = NeuronFileManager.RUN_HOC;


    private String welcomeText =
        "\nNo neuroConstruct project loaded.\n\n"+
        "To create a new project select: File -> New Project... in the main menu.\n\n"
        +"To open an existing project select: File -> Open Project... or choose one of the projects listed at the bottom of that menu.\n\n"+
        "For tutorials on neuroConstruct select menu: Help -> Help and follow the link for the tutorials.\n\n"+
        "Note to Vista users: to view the included examples, you may need to give extra permissions to the files" +
        " in the examples folder at C:\\Program Files\\neuroConstruct_xxx. Browse to the folder with My Computer/Explorer, right click on the folder, change settings via Properties -> Security.\n\n";

    // JBuilder added stuff...

    JPanel contentPane;
    JMenuBar jMenuBar1 = new JMenuBar();
    JMenu jMenuFile = new JMenu();
    JMenuItem jMenuFileExit = new JMenuItem();
    JMenu jMenuHelp = new JMenu();
    JMenuItem jMenuHelpAbout = new JMenuItem();
    JToolBar jToolBar = new JToolBar();
    JButton jButtonOpenProject = new JButton();
    JButton jButtonSaveProject = new JButton();
    JButton jButtonPreferences = new JButton();
    JButton jButtonCloseProject = new JButton();
    JButton jButtonToggleTips = new JButton();
    JButton jButtonToggleConsoleOut = new JButton();

    ImageIcon imageNewProject;
    ImageIcon imageOpenProject;
    ImageIcon imageSaveProject;
    ImageIcon imageCloseProject;
    ImageIcon imageProjectPrefs;
    ImageIcon imageTips;
    ImageIcon imageNoTips;
    ImageIcon imageConsoleOut;
    ImageIcon imageNoConsoleOut;

    JLabel statusBar = new JLabel();
    BorderLayout borderLayout1 = new BorderLayout();
    JMenuItem jMenuItemFileOpen = new JMenuItem();
    JMenuItem jMenuItemSaveProject = new JMenuItem();
    JMenu jMenuSettings = new JMenu();
    JMenuItem jMenuItemProjProperties = new JMenuItem();
    JMenu jMenuTools = new JMenu();
    JMenuItem jMenuItemNmodlEditor = new JMenuItem();
    JMenuItem jMenuItemNewProject = new JMenuItem();
    JButton jButtonNewProject = new JButton();
    JButton jButtonValidate = new JButton();
    JTabbedPane jTabbedPaneMain = new JTabbedPane();
    JTabbedPane jTabbedPaneExportFormats = new JTabbedPane();

    JPanel jPanelExportNeuron = new JPanel();
    JPanel jPanelExportGenesis = new JPanel();
    JPanel jPanelExportPsics = new JPanel();
    JPanel jPanelExportPynn = new JPanel();
    //JPanel jPanelExportNeosim = new JPanel();


    JPanel jPanelProjInfo = new JPanel();
    JPanel jPanelRegions = new JPanel();
    JPanel jPanelCellTypes = new JPanel();
    JPanel jPanelCellGroupDetails = new JPanel();
    JPanel jPanel3DDemo = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    JButton jButtonCellTypeNew = new JButton();
    JPanel jPanelRegionsButtons = new JPanel();
    JButton jButtonRegionNew = new JButton();
    JPanel jPanelRegionsTable = new JPanel();
    BorderLayout borderLayout4 = new BorderLayout();
    JPanel jPanel3DMain = new JPanel();
    JPanel jPanel3DButtons = new JPanel();
    BorderLayout borderLayout5 = new BorderLayout();
    JButton jButton3DView = new JButton();
   // JPanel jPanelCellTypeAddNew = new JPanel();
    JPanel jPanelCellTypeDetails = new JPanel();
    JTree jTreeCellDetails = null;

    JPanel jPanelHocFile1Buttons = new JPanel();
    JPanel jPanelHocFile2Buttons = new JPanel();
    JPanel jPanelPsicsFileView = new JPanel();
    JPanel jPanelPsicsPostOptions = new JPanel();
    JPanel jPanelPsicsDiscOptions = new JPanel();
    JPanel jPanelPynnFileView = new JPanel();
    
    
    JPanel jPanelCellGroupsMainPanel = new JPanel();
    BorderLayout borderLayout6 = new BorderLayout();
    JButton jButtonNeuronRun = new JButton();
    JButton jButtonNeuronCreateLocal = new JButton();
    JButton jButton3DDestroy = new JButton();
    //JLabel jLabelWidth = new JLabel();
    //JTextField jTextFieldWidth = new JTextField();
    //JLabel jLabelDepth = new JLabel();
    //JTextField jTextFieldDepth = new JTextField();
    JScrollPane jScrollPaneRegions = new JScrollPane();

    JTable jTable3DRegions = new JTable();

    JPanel jPanelCellGroupButtons = new JPanel();
    JButton jButtonCellGroupsNew = new JButton();
    JScrollPane jScrollPaneCellGroups = new JScrollPane();
    JTable jTableCellGroups = new JTable();
    BorderLayout borderLayout7 = new BorderLayout();
    BorderLayout borderLayout8 = new BorderLayout();
    JPanel jPanelMainInfo = new JPanel();
    JLabel jLabelName = new JLabel();
    JLabel jLabelMainNumCells = new JLabel();
    JLabel jLabelProjDescription = new JLabel();
    JTextField jTextFieldProjName = new JTextField();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JTextArea jTextAreaProjDescription = new JTextArea();

    JScrollPane jScrollPaneProjDesc = new JScrollPane();

    ClickPanel jPanelSimConfigClicks = new ClickPanel();

    JLabel jLabelSimConfigs = new JLabel();
    //JTextField jTextFieldNumCells = new JTextField();
    ClickPanel jPanelCellClicks = new ClickPanel();


   // JComboBox jComboBox3DCellToView = new JComboBox();
    JPanel jPanelNetworkSettings = new JPanel();
    JMenuItem jMenuItemCloseProject = new JMenuItem();
    JLabel jLabelTitle = new JLabel();
    JPanel jPanelExport = new JPanel();
    BorderLayout borderLayout10 = new BorderLayout();
    JLabel jLabelSimDefDur = new JLabel();
    JTextField jTextFieldSimDefDur = new JTextField();
    JPanel jPanelNetSetSimple = new JPanel();

    String defaultCellGroupStimulation = "-- No Stimulation --";
    String cellComboPrompt = "-- Please select a cell type --";

    String neuronBlockPrompt = "-- Please select where to put NEURON code --";
    String genesisBlockPrompt = "-- Please select where to put GENESIS script --";

    JPanel jPanelSimSettings = new JPanel();
    JLabel jLabelSimDT = new JLabel();
    JTextField jTextFieldSimDT = new JTextField();

    JPanel jPanelSimValsInput = new JPanel();
    BorderLayout borderLayout12 = new BorderLayout();
    JLabel jLabelSimSummary = new JLabel();
    JPanel jPanelCellTypeInfo = new JPanel();
    JButton jButton3DSettings = new JButton();
    JButton jButton3DHelp = new JButton();
    
    
    JButton jButtonNeuronView = new JButton();
    JButton jButtonPsicsView = new JButton("View:");
    JButton jButtonPynnView = new JButton("View:");
    
    JPanel jPanelSimStorage = new JPanel();
    JLabel jLabelSimRef = new JLabel();
    JTextField jTextFieldSimRef = new JTextField();
    JRadioButton jRadioButtonNeuronSimSaveToFile = new JRadioButton();
    ButtonGroup buttonGroupSimSavePreference = new ButtonGroup();
    JPanel jPanelNetSetControls = new JPanel();
    JButton jButtonNetSetAddNew = new JButton();
    JPanel jPanelNetSetTable = new JPanel();
    BorderLayout borderLayout13 = new BorderLayout();
    JButton jButtonCellTypeViewCell = new JButton();
    JButton jButtonCellTypeViewCellChans = new JButton();
    JLabel jLabelExistingCellTypes = new JLabel();
    JComboBox jComboBoxCellTypes = new JComboBox();

    JComboBox jComboBoxNeuronExtraBlocks = new JComboBox();
    JPanel jPanelCBNeuronExtraBlocks = new JPanel();

    JComboBox jComboBoxGenesisExtraBlocks = new JComboBox();
    JPanel jPanelCBGenesisExtraBlocks = new JPanel();

    JScrollPane scrollerCellTypeInfo = new JScrollPane();
    JEditorPane jEditorPaneCellTypeInfo = new JEditorPane();
    JEditorPane jEditorPaneGenerateInfo = new JEditorPane();
    JPanel jPanelGenerate = new JPanel();
    JPanel jPanelGenerateMain = new JPanel();
    JPanel jPanelGenerateButtonsDesc = new JPanel();
    JPanel jPanelGenerateLoadSave = new JPanel();
    JPanel jPanelGenerateButtons = new JPanel();
    JPanel jPanelGenerateDesc = new JPanel();
    BorderLayout borderLayout14 = new BorderLayout();
    JButton jButtonGenerate = new JButton();
    JComboBox jComboBoxSimConfig = new JComboBox();

    JButton jButtonGenerateSave = new JButton();
    JButton jButtonGenerateLoad = new JButton();
    //JCheckBox jCheckBoxGenerateZip = new JCheckBox();
    JRadioButton jRadioButtonNMLSavePlainText = new JRadioButton("XML");
    JRadioButton jRadioButtonNMLSaveZipped = new JRadioButton("Zipped XML");
    JRadioButton jRadioButtonNMLSaveHDF5 = new JRadioButton("HDF5 (beta impl)");
    ButtonGroup buttonGroupNMLSave = new ButtonGroup();
    
    JCheckBox jCheckBoxGenerateExtraNetComments = new JCheckBox();


    JButton jButtonSimConfigEdit = new JButton();
    JScrollPane scrollerGenerate = new JScrollPane();
    JButton jButtonRegionRemove = new JButton();
    JButton jButtonCellGroupsDelete = new JButton();
    JScrollPane jScrollPaneNetConnects = new JScrollPane();
    JScrollPane jScrollPaneAAConns = new JScrollPane();
    JTable jTableNetConns = new JTable();
    JTable jTableAAConns = new JTable();


    BorderLayout borderLayout15 = new BorderLayout();
    JButton jButtonNetConnDelete = new JButton();
    JRadioButton jRadioButtonNeuronSimDontRecord = new JRadioButton();
    ImageIcon imageNeuroConstruct = null;
    Border border1;
    GridBagLayout gridBagLayout4 = new GridBagLayout();
    JPanel jPanelNmodl = new JPanel();
    JPanel jPanelSynapseButtons = new JPanel();
    JPanel jPanelSynapseMain = new JPanel();
    ///JButton jButtonSynapseAdd = new JButton();
    JScrollPane jScrollPaneSynapses = new JScrollPane();
    JScrollPane jScrollPaneChanMechs = new JScrollPane();

    JTable jTableSynapses = new JTable();
    JTable jTableChanMechs = new JTable();

    BorderLayout borderLayout16 = new BorderLayout();
    JPanel jPanelStims = new JPanel();
    BorderLayout borderLayout17 = new BorderLayout();
    ButtonGroup buttonGroupStim = new ButtonGroup();
    ButtonGroup buttonGroupStimCellChoice = new ButtonGroup();
    JPanel jPanelGenerateAnalyse = new JPanel();
    JLabel jLabelGenAnalyse = new JLabel();
    JComboBox jComboBoxAnalyseCellGroup = new JComboBox();
    JComboBox jComboBoxAnalyseNetConn = new JComboBox();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JComboBox jComboBoxView3DChoice = new JComboBox();
    JButton jButtonSynapseEdit = new JButton();
    JMenuItem jMenuItemGeneralProps = new JMenuItem();
    JButton jButtonGenerateStop = new JButton();
    JTextArea jTextAreaSimConfigDesc = new JTextArea();


    JPanel jPanelRandomGen =  new JPanel();
    JLabel jLabelRandomGenDesc = new JLabel("Random seed for generation:");
    JTextField jTextFieldRandomGen = new JTextField();
    JCheckBox jCheckBoxRandomGen = new JCheckBox("Recalculate before network generation");

    JPanel jPanelNeuronNumInt =  new JPanel();
    JCheckBox jCheckBoxNeuronNumInt = new JCheckBox("Use variable time step");
    JCheckBox jCheckBoxNeuronGenAllMod = new JCheckBox("Generate all mod files");
    JCheckBox jCheckBoxNeuronCopySimFiles = new JCheckBox("Copy files to sims dir");
    JCheckBox jCheckBoxGenesisCopySimFiles = new JCheckBox("Copy files to sims dir");
    JCheckBox jCheckBoxGenesisMooseMode = new JCheckBox("MOOSE test mode (beta)     ");

    JCheckBox jCheckBoxGenesisReload = new JCheckBox("Attempt reload sim after");
    JLabel jLabelGenesisReload = new JLabel("secs     ");
    JTextField jTextFieldGenesisReload = new JTextField("10", 3);


    JCheckBox jCheckBoxGenesisAbsRefract = new JCheckBox("Spike compat mode or");
    JLabel jLabelGenesisAbsRefract = new JLabel("ms refract time");
    JTextField jTextFieldGenesisAbsRefract = new JTextField("5", 3);


    

    JCheckBox jCheckBoxNeuronForceCorrInit = new JCheckBox("Force correct ChannelML init");
    JCheckBox jCheckBoxNeuronModSilent = new JCheckBox("Silent mod compile");

    ButtonGroup buttonGroupNeuronFormat = new ButtonGroup();
    JLabel jLabelNeuronFormat = new JLabel(" Save data as:");
    JRadioButton jRadioButtonNeuronFormatText = new JRadioButton("Text files");
    JRadioButton jRadioButtonNeuronFormatHDF5 = new JRadioButton("HDF5");

    JPanel jPanelNeuronRandomGen =  new JPanel();
    JLabel jLabelNeuronRandomGenDesc = new JLabel("Random seed for NEURON:");
    JTextField jTextFieldNeuronRandomGen = new JTextField();
    JCheckBox jCheckBoxNeuronRandomGen = new JCheckBox("Recalculate before generating files");
    
    
    JLabel jLabelPyNNRandomGenDesc = new JLabel("Random seed for PyNN:");
    JTextField jTextFieldPyNNRandomGen = new JTextField("1234");
    JCheckBox jCheckBoxPyNNRandomGen = new JCheckBox("Recalculate before creating script files", false);
    
    JCheckBox jCheckBoxPsicsShowHtml = new JCheckBox("Show HTML summary", false);
    JCheckBox jCheckBoxPsicsShowPlot = new JCheckBox("Quick plot after run", true);
    JCheckBox jCheckBoxPsicsConsole = new JCheckBox("Show console", false);

    JLabel jLabelPsicsSpatDisc = new JLabel("Structural discretization (\u03bcm):");
    JTextField jTextFieldPsicsSpatDisc = new JTextField();

    JLabel jLabelPsicsSingleCond = new JLabel("Default single channel cond (mS):");
    JTextField jTextFieldPsicsSingleCond = new JTextField();
    
    
    JPanel jPanelNeuronExtraLinks =  new JPanel();
    JLabel jLabelNeuronExtraLinks = new JLabel("Extra code blocks:");
    
    JPanel jPanelGenesisExtraLinks =  new JPanel();
    JLabel jLabelGenesisExtraLinks = new JLabel("Extra code blocks:");

    JPanel jPanelGenesisRandomGen =  new JPanel();
    JPanel jPanelGenesisComps =  new JPanel();
    JLabel jLabelGenesisCompsDesc = new JLabel("Compartmentalisation to use:");

    JLabel jLabelGenesisRandomGenDesc = new JLabel("Random seed for GENESIS:");
    JTextField jTextFieldGenesisRandomGen = new JTextField();
    JCheckBox jCheckBoxGenesisRandomGen = new JCheckBox("Recalculate before creating GENESIS scripts");


    JButton jButtonNetConnEdit = new JButton();
    JButton jButtonAnalyseConnLengths = new JButton();
    JButton jButtonAnalyseNumConns = new JButton();
    
    
    JButton jButtonNetworkFullInfo = new JButton();

    JButton jButtonAnalyseCellDensities = new JButton();

    JPanel jPanelGenerateComboBoxes = new JPanel();
    JPanel jPanelGenerateAnalyseButtons = new JPanel();
    BorderLayout borderLayout18 = new BorderLayout();
    //JPanel jPanelSynapticProcesses = new JPanel();
    BorderLayout borderLayout19 = new BorderLayout();
    JPanel jPanelChannelMechsInnerTab = new JPanel();
    JPanel jPanelChannelMechsMain = new JPanel();
    //JPanel jPanelChanMechsButtons = new JPanel();
    //JButton jButtonChanMechAdd = new JButton();
    ///JButton jButtonChanMechEdit = new JButton();
    BorderLayout borderLayout20 = new BorderLayout();
    GridLayout gridLayout1 = new GridLayout();
    BorderLayout borderLayout11 = new BorderLayout();
    JLabel jLabelChanMechTitle = new JLabel();
    Border border2;
    JLabel jLabelSynapseTitle = new JLabel();
    JMenuItem jMenuItemZipUp = new JMenuItem();
    JLabel jLabelNumCellGroups = new JLabel();


    //JTextField jTextFieldNumCellGroups = new JTextField();
    ClickPanel jPanelCellGroupClicks = new ClickPanel();

    JLabel jLabelProjFileVersion = new JLabel();
    JTextField jTextFieldProjFileVersion = new JTextField();
    JMenuItem jMenuItemUnzipProject = new JMenuItem();
    JMenuItem jMenuItemImportLevel123 = new JMenuItem();
    
    //JMenu jMenuExamples = new JMenu();
    //JMenu jMenuModels = new JMenu();
    JMenu jMenuOsbModels = new JMenu();
    
    
    
    JPanel jPanelAllNetSettings = new JPanel();
    JPanel jPanelNetSetAA = new JPanel();
    BorderLayout borderLayout9 = new BorderLayout();
    GridLayout gridLayout2 = new GridLayout();
    GridLayout gridLayout3 = new GridLayout();
    JPanel jPanelSynapseButtonsOnly = new JPanel();
    BorderLayout borderLayout21 = new BorderLayout();
    JPanel jPanelChanMechsButtonsOnly = new JPanel();
    BorderLayout borderLayout22 = new BorderLayout();
    JPanel jPanelNetConnButtonsOnly = new JPanel();
    JLabel jLabelNetConnSimpleConn = new JLabel();
    BorderLayout borderLayout23 = new BorderLayout();
    JPanel jPanelNetSetAAControls = new JPanel();
    JPanel jPanelNetSetAATable = new JPanel();
    JLabel jLabelNetSetAA = new JLabel();
    JButton jButtonNetAAAdd = new JButton();
    JPanel jPanelNetSetAAConButtons = new JPanel();
    BorderLayout borderLayout24 = new BorderLayout();
    BorderLayout borderLayout25 = new BorderLayout();
    JButton jButtonCellGroupsEdit = new JButton();
    GridLayout gridLayout4 = new GridLayout();
    JButton jButtonNetAADelete = new JButton();
    JButton jButtonNetAAEdit = new JButton();
    JMenuItem jMenuItemViewProjSource = new JMenuItem();
    JPanel jPanelCellTypeMainInfo = new JPanel();
    JButton jButtonCellTypeViewCellInfo = new JButton();
    JPanel jPanelGenesisMain = new JPanel();
    JPanel jPanelPsicsMain = new JPanel();
    JPanel jPanelPynnMain = new JPanel();
    JCheckBox jCheckBoxPynnShowTraces = new JCheckBox();
    JPanel jPanelPynnOptions1 = new JPanel();
    JPanel jPanelPynnOptions2 = new JPanel();
    BorderLayout borderLayout26 = new BorderLayout();
    JLabel jLabelGenesisMain = new JLabel();
    JLabel jLabelPsicsMain = new JLabel();
    JLabel jLabelPynnMain = new JLabel();
    JPanel jPanelSimNeosimMain = new JPanel();
    JLabel jLabelSimulatorNeosimMain = new JLabel();
    BorderLayout borderLayout27 = new BorderLayout();
    JButton jButtonCellTypeDelete = new JButton();
    JButton jButtonCellTypeCompare = new JButton();
    JPanel jPanelCellTypeMainButtons = new JPanel();
    //JPanel jPanelCellTypesAddNew = new JPanel();
    BorderLayout borderLayout29 = new BorderLayout();
    BorderLayout borderLayout30 = new BorderLayout();
    JPanel jPanelCellTypesAddNewCell = new JPanel();
    JPanel jPanelCellTypesComboBox = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    JButton jButtonCellTypeCopy = new JButton();
    JPanel jPanelCellTypesButtonsInfo = new JPanel();
    JButton jButtonCellTypesMoveToOrigin = new JButton();
    JPanel jPanelCellTypeManageNumbers = new JPanel();
    JPanel jPanelCellTypesModify = new JPanel();
    JButton jButtonCellTypesConnect = new JButton();
    JButton jButtonCellTypesMakeSimpConn = new JButton();
    FlowLayout flowLayout3 = new FlowLayout();
    JLabel jLabelMainLastModified = new JLabel();
    JTextField jTextFieldMainLastModified = new JTextField();
    JMenuItem jMenuItemGlossaryShow = new JMenuItem();
    JMenuItem jMenuItemReleaseNotes = new JMenuItem();
    JMenuItem jMenuItemCheckUpdates = new JMenuItem();
    JPanel jPanelExportHeader = new JPanel();
    JLabel jLabelExportMain = new JLabel();
    //JPanel jPanelExportMorphML = new JPanel();
    JTabbedPane jTabbedPaneNeuron = new JTabbedPane();
    JPanel jPanelNeuronMainSettings = new JPanel();
    GridLayout gridLayout5 = new GridLayout();
    JPanel jPanelNeuroML = new JPanel();

    JButton jButtonNeuroML1Export = new JButton();
    JButton jButtonNeuroML2aExport = new JButton();
    JButton jButtonNeuroML2bExport = new JButton();

    JCheckBox jCheckBoxSedMl = new JCheckBox("Include SED-ML");

    JButton jButtonNeuroML2Lems = new JButton();
    JButton jButtonNeuroML2Graph = new JButton();
    JButton jButtonNeuroML2NineML = new JButton();
    
    JRadioButton jRadioButtonNeuroMLLevel1 = new JRadioButton();
    JRadioButton jRadioButtonNeuroMLLevel2 = new JRadioButton();
    JRadioButton jRadioButtonNeuroMLLevel3 = new JRadioButton();
    JRadioButton jRadioButtonNeuroMLV2 = new JRadioButton();
    //JRadioButton jRadioButtonNeuroMLCellChan = new JRadioButton();

    ButtonGroup buttonGroupNeuroML = new ButtonGroup();
    
    //JButton jButtonNeuroMLExportLevel2 = new JButton();
    //JButton jButtonNeuroMLExportCellLevel3 = new JButton();
    //JButton jButtonNeuroMLExportNetLevel3 = new JButton();

    JLabel jLabelNeuroMLMain = new JLabel();
    JPanel jPanelNeuroMLL3Exp = new JPanel();
    JPanel jPanelNeuroMLHeader = new JPanel();


    JComboBox jComboBoxNeuroMLComps = new JComboBox();
    JTextArea jTextAreaNeuroMLCompsDesc = new JTextArea();


    JComboBox jComboBoxGenesisComps = new JComboBox();
    JTextArea jTextAreaGenesisCompsDesc = new JTextArea();



    BorderLayout borderLayout31 = new BorderLayout();
    GridLayout gridLayout6 = new GridLayout();
    JPanel jPanelNeuroMLMain = new JPanel();
    JLabel jLabelNeuroMLGeneratedFiles = new JLabel();
    JComboBox jComboBoxNeuroML = new JComboBox();
    JButton jButtonNeuroMLViewPlain = new JButton();
    JButton jButtonNeuroMLGenSim = new JButton();
    JCheckBox jCheckBoxNeuroMLGenNet = new JCheckBox();
    JCheckBox jCheckBoxNeuroMLneuroCobjects = new JCheckBox();
    JButton jButtonNeuroMLViewFormatted = new JButton();
    GridBagLayout gridBagLayout3 = new GridBagLayout();
    GridBagLayout gridBagLayout33 = new GridBagLayout();
    JPanel jPanelGenesisButtons = new JPanel();
    JPanel jPanelGenesisView = new JPanel();
    JPanel jPanelGenesisSettings = new JPanel();
    JButton jButtonGenesisGenerate = new JButton();
    JButton jButtonGenesisRun = new JButton();
    JPanel jPanelPsicsButtons = new JPanel();
    JPanel jPanelPynnSimOptions = new JPanel();
    
    JRadioButton jRadioButtonPynnNeuron = new JRadioButton(PynnFileManager.PynnSimulator.NEURON.name);
    JRadioButton jRadioButtonPynnNest2 = new JRadioButton(PynnFileManager.PynnSimulator.NEST.name);
    JRadioButton jRadioButtonPynnPcsim = new JRadioButton(PynnFileManager.PynnSimulator.PCSIM.name);
    JRadioButton jRadioButtonPynnBrian = new JRadioButton(PynnFileManager.PynnSimulator.BRIAN.name);
    JRadioButton jRadioButtonPynnPyMoose = new JRadioButton(PynnFileManager.PynnSimulator.PYMOOSE.name);
    
    ButtonGroup buttonGroupPynn = new ButtonGroup();
    
    JButton jButtonPsicsGenerate = new JButton();
    JButton jButtonPsicsRun = new JButton();
    JPanel jPanelPynnButtons = new JPanel();
    JButton jButtonPynnGenerate = new JButton();
    JButton jButtonPynnRun = new JButton();
    BorderLayout borderLayout32 = new BorderLayout();
    JButton jButtonGenesisView = new JButton();
    JButton jButtonRegionsEdit = new JButton();
    JPanel jPanelNeuronExtraHoc = new JPanel();
    JPanel jPanelNeuronExtraHocBlock = new JPanel();


    //JPanel jPanelNeuronExtraHocAfter = new JPanel();
    GridLayout gridLayout7 = new GridLayout();

    JPanel jPanelNeuronBlockDesc = new JPanel();
    JPanel jPanelGenesisBlockDesc = new JPanel();

    JTextArea jTextAreaNeuronBlockDesc = new JTextArea();
    JTextArea jTextAreaGenesisBlockDesc = new JTextArea();


    JScrollPane jScrollPaneNeuronBlock = new JScrollPane();
    JScrollPane jScrollPaneGenesisBlock = new JScrollPane();


    Border border4;
    JTextArea jTextAreaNeuronBlock = new JTextArea();
    JTextArea jTextAreaGenesisBlock = new JTextArea();
    ////JTextArea jTextAreaNeuronAfter = new JTextArea();
    FlowLayout flowLayout4 = new FlowLayout();
    FlowLayout flowLayout5 = new FlowLayout();
    JComboBox jComboBoxNeuronFileList = new JComboBox();
    JCheckBox jCheckBoxNeuronLineNums = new JCheckBox("Show line numbers");
    JCheckBox jCheckBoxGenesisLineNums = new JCheckBox("Show line numbers");
    
    JComboBox jComboBoxPynnFileList = new JComboBox();
    JCheckBox jCheckBoxPynnLineNums = new JCheckBox("Show line numbers");
    JComboBox jComboBoxPsicsFileList = new JComboBox();
    JCheckBox jCheckBoxPsicsLineNums = new JCheckBox("Show line numbers");
    
    Border border5;
    Border border6;
    JMenuItem jMenuItemCondorMonitor = new JMenuItem();


    JMenuItem jMenuItemPlotEquation = new JMenuItem();
    JMenuItem jMenuItemPlotImport = new JMenuItem();
    JMenuItem jMenuItemPlotImportHDF5 = new JMenuItem();

    JMenuItem jMenuItemMPIMonitor = new JMenuItem();
    JCheckBox jCheckBoxSpecifySimRef = new JCheckBox();
    JCheckBox jCheckBoxNeuronSaveHoc = new JCheckBox();
    JButton jButtonNeuronCreateCondor = new JButton();
    /////////////JButton jButtonNeuronCreateMPIHoc = new JButton();
    JButton jButtonNeuronCreatePythonXML = new JButton();
    JButton jButtonNeuronCreatePyHDF5 = new JButton();
    
    JComboBox jComboBoxGenesisFiles = new JComboBox();
    JPanel jPanelSimGeneral = new JPanel();
    JPanel jPanelInputOutput = new JPanel();
    BorderLayout borderLayout33 = new BorderLayout();
    JLabel jLabelNeuronMainLabel = new JLabel();
    Border border7;
    JPanel jPanelSimulationParams = new JPanel();
    JLabel jLabelSimulationGlobRa = new JLabel();
    JTextField jTextFieldSimulationGlobRa = new JTextField();
    JLabel jLabelSimulationGlobCm = new JLabel();
    JTextField jTextFieldSimulationGlobCm = new JTextField();
    JTabbedPane jTabbedPaneGenesis = new JTabbedPane();
    //JPanel jPanelGenesisExtraBefore = new JPanel();
    //JLabel jLabelGenesisExtraBefore = new JLabel();
    FlowLayout flowLayout6 = new FlowLayout();
    //JTextArea jTextAreaGenesisAfter = new JTextArea();
    //JPanel jPanelGenesisExtraAfter = new JPanel();
    JPanel jPanelGenesisExtra = new JPanel();

    JPanel jPanelGenesisExtraBlock = new JPanel();
    FlowLayout flowLayout7 = new FlowLayout();
    GridLayout gridLayout8 = new GridLayout();
    //JScrollPane jScrollPaneGenesisAfter = new JScrollPane();
    //JLabel jLabelGenesisExtraAfter = new JLabel();
    //JScrollPane jScrollPaneGenesisBefore = new JScrollPane();
    //JTextArea jTextAreaGenesisBefore = new JTextArea();
    JPanel jPanelSimulationGlobal = new JPanel();
    JButton jButton3DPrevSimuls = new JButton();
    JButton jButton3DQuickSims = new JButton();
    JLabel jLabelSimulationGlobRm = new JLabel();
    JTextField jTextFieldSimulationGlobRm = new JTextField();
    JLabel jLabelSimulationInitVm = new JLabel();
    JTextField jTextFieldSimulationInitVm = new JTextField();
    JLabel jLabelSimulationVLeak = new JLabel();
    JTextField jTextFieldSimulationVLeak = new JTextField();
    JCheckBox jCheckBoxGenesisSymmetric = new JCheckBox();
    BorderLayout borderLayout35 = new BorderLayout();
    Border border8;
    JPanel jPanelNeuronGraphOptions = new JPanel();


    GridBagLayout gridBagLayout6 = new GridBagLayout();
    GridBagLayout gridBagLayoutGen = new GridBagLayout();


    JCheckBox jCheckBoxNeuronShowShapePlot = new JCheckBox();
    JCheckBox jCheckBoxNeuronSumatra = new JCheckBox();

    //JCheckBox jCheckBoxNeuronNoGraphicsMode = new JCheckBox();
    JRadioButton jRadioButtonNeuronAllGUI = new JRadioButton();
    JRadioButton jRadioButtonNeuronNoPlots = new JRadioButton();
    JRadioButton jRadioButtonNeuronNoConsole = new JRadioButton();
    ButtonGroup buttonGroupNeuronGUI = new ButtonGroup();
    JLabel jLabelNeuronGUI  = new JLabel();


    JRadioButton jRadioButtonGenesisAllGUI = new JRadioButton();
    JRadioButton jRadioButtonGenesisNoPlots = new JRadioButton();
    JRadioButton jRadioButtonGenesisNoConsole = new JRadioButton();
    ButtonGroup buttonGroupGenesisGUI = new ButtonGroup();
    JLabel jLabelGenesisGUI  = new JLabel();

    //JCheckBox jCheckBoxGenesisNoGraphicsMode = new JCheckBox();


    JPanel jPanelSimWhatToRecord = new JPanel();
    JRadioButton jRadioButtonSimSomaOnly = new JRadioButton();
    JLabel jLabelSimWhatToRecord = new JLabel();
    JRadioButton jRadioButtonSimAllSegments = new JRadioButton();
    ButtonGroup buttonGroupSimWhatToRecord = new ButtonGroup();
    JRadioButton jRadioButtonSimAllSections = new JRadioButton();
    ButtonGroup buttonGroupGenesisUnits = new ButtonGroup();
    JPanel jPanelGenesisUnits = new JPanel();
    JRadioButton jRadioButtonGenesisPhy = new JRadioButton();
    JRadioButton jRadioButtonGenesisSI = new JRadioButton();
    JPanel jPanelCellMechanisms = new JPanel();
    JPanel jPanelProcessButtons = new JPanel();
    JPanel jPanelProcessButtonsTop = new JPanel();
    JPanel jPanelProcessButtonsBottom = new JPanel();
    JPanel jPanelMechanismMain = new JPanel();
    JPanel jPanelMechanismLabel = new JPanel();
    BorderLayout borderLayout28 = new BorderLayout();
    JLabel JLabelMechanismMain = new JLabel();
    JScrollPane jScrollPaneMechanisms = new JScrollPane();
    BorderLayout borderLayout36 = new BorderLayout();
    JTable jTableMechanisms = new JTable();
    JButton jButtonMechanismDelete = new JButton();
    JButton jButtonMechanismCopy = new JButton();
    JButton jButtonCompareMechanism = new JButton();
    JButton jButtonMechanismEditIt = new JButton();
    JButton jButtonMechanismUpdateMaps = new JButton();
    JButton jButtonMechanismReloadFile = new JButton();
    
    JButton jButtonMechanismAbstract = new JButton();
    JButton jButtonMechanismTemplateCML = new JButton();
    BorderLayout borderLayout37 = new BorderLayout();
    Border border9;
    JMenuItem jMenuItemUnits = new JMenuItem();
    JPanel jPanelGenesisChoices = new JPanel();
    BorderLayout borderLayout38 = new BorderLayout();
    JButton jButtonMechanismFileBased = new JButton();
    JButton jButtonMechanismNewCML = new JButton();
    JLabel jLabelSimTemp = new JLabel();
    
    JTextField jTextFieldSimulationTemp = new JTextField();
    JLabel jLabelElectroLenMax = new JLabel();
    JTextField jTextFieldElectroLenMax = new JTextField();
    JLabel jLabelElectroLenMin = new JLabel();
    JTextField jTextFieldElectroLenMin = new JTextField();
    
    JCheckBox jCheckBoxGenesisComments = new JCheckBox();
    JCheckBox jCheckBoxGenesisShapePlot = new JCheckBox();

    JLabel jLabelSimStimDesc = new JLabel();

    JPanel jPanelGenesisCheckBoxes0 = new JPanel();
    JPanel jPanelGenesisCheckBoxes1 = new JPanel();
    JPanel jPanelGenesisCheckBoxes2 = new JPanel();
    JProgressBar jProgressBarGenerate = new JProgressBar();
    JPanel jPanelGenesisNumMethod = new JPanel();
    JLabel jLabelGenesisNumMethod = new JLabel();
    JButton jButtonGenesisNumMethod = new JButton();
    JMenuItem jMenuItemCopyProject = new JMenuItem();
    JPanel jPanelSimPlot = new JPanel();
    BorderLayout borderLayout39 = new BorderLayout();
    JPanel jPanelSimRecordWhere = new JPanel();
    BorderLayout borderLayout40 = new BorderLayout();
    JPanel jPanelSimTotalTime = new JPanel();
    BorderLayout borderLayout41 = new BorderLayout();
    Border border10;
    JPanel jPanelSimDT = new JPanel();
    //JTextField jTextFieldNeuronDuration1 = new JTextField();
    //JLabel jLabelNeuronDuration1 = new JLabel();

    JTextField jTextFieldSimTotalTimeUnits = new JTextField();
    JTextField jTextFieldSimDTUnits = new JTextField();
    JScrollPane jScrollPaneSimPlot = new JScrollPane();
    BorderLayout borderLayout42 = new BorderLayout();
    JPanel jPanelSimPlotButtons = new JPanel();
    JButton jButtonSimPlotAdd = new JButton();
    JTable jTableSimPlot = new JTable();
    JTable jTableStims = new JTable();

    JButton jButtonSimPlotDelete = new JButton();
    JButton jButtonSimPlotEdit = new JButton();
    JButton jButtonSimPlotCopy = new JButton();
    JPanel jPanelSimStimButtons = new JPanel();
    JScrollPane jScrollPaneSimStims = new JScrollPane();
    JButton jButtonSimStimAdd = new JButton();
    JButton jButtonSimStimDelete = new JButton();
    JButton jButtonSimStimEdit = new JButton();
    JButton jButtonSimStimCopy = new JButton();
    
    
    GridBagLayout gridBagLayout5 = new GridBagLayout();
    JTextField jTextFieldSimUnitInitVm = new JTextField();
    JTextField jTextFieldSimUnitVLeak = new JTextField();
    BorderLayout borderLayout34 = new BorderLayout();
    JTextField jTextFieldSimUnitGlobRa = new JTextField();
    JTextField jTextFieldSimUnitGlotCm = new JTextField();
    JTextField jTextFieldSimUnitGlobRm = new JTextField();
    JTextField jTextFieldSimUnitsTemp = new JTextField();
    JTextField jTextFieldElecLenMaxUnits = new JTextField();
    JTextField jTextFieldElecLenMinUnits = new JTextField();
    JMenuItem jMenuItemHelpMain = new JMenuItem();
    JMenuItem jMenuItemHelpRelNotes = new JMenuItem();
    JCheckBox jCheckBoxNeuronComments = new JCheckBox();
    JButton jButtonCellTypeBioPhys = new JButton();
    JButton jButtonCellTypeEditDesc = new JButton();

    JButton jButtonCellTypeOtherProject = new JButton();
    JMenuItem jMenuItemJava = new JMenuItem();
    JMenu jMenuProject = new JMenu();
    JMenuItem jMenuItemGenNetwork = new JMenuItem();
    JMenuItem jMenuItemGenNeuronHoc = new JMenuItem();
    JMenuItem jMenuItemGenNeuronPyXML = new JMenuItem();
    JMenuItem jMenuItemGenNeuronPyHDF5 = new JMenuItem();
    JMenuItem jMenuItemGenGenesis = new JMenuItem();
    JMenuItem jMenuItemGenPsics = new JMenuItem();
    JMenuItem jMenuItemGenPynn = new JMenuItem();
    JMenuItem jMenuItemPrevSims = new JMenuItem();
    JMenuItem jMenuItemDataSets = new JMenuItem();
    JMenuItem jMenuItemListSims = new JMenuItem();


   // JButton jButtonSimulationRecord = new JButton();

    public MainFrame()
    {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try
        {
            jbInit();
            extraInit();

            addToolTips();

            refreshAll();

            doCheckForLogFiles();
        }
        catch (Exception e)
        {
            logger.logError("Exception starting GUI: ", e);
        }
    }

    private void jbInit() throws Exception
    {
        border2 = BorderFactory.createEmptyBorder(5,0,5,0);

        border4 = BorderFactory.createEmptyBorder(8,8,8,8);
        border5 = BorderFactory.createEmptyBorder(5,5,5,5);
        border6 = BorderFactory.createEmptyBorder(5,5,5,5);
        border7 = BorderFactory.createEmptyBorder(5,5,5,5);
        border8 = BorderFactory.createEmptyBorder(10,10,10,10);
        border9 = BorderFactory.createEmptyBorder(10,10,10,10);
        border10 = BorderFactory.createEmptyBorder(6,6,6,6);


        jPanelGenerateAnalyse.setLayout(borderLayout18);
      /*  jPanelSynapticProcesses.setLayout(borderLayout19);

        jButtonChanMechAdd.setText("Add Channel Mechanism");
        jButtonChanMechAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonChanMechAdd_actionPerformed(e);
            }
        });
        jButtonChanMechEdit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonChanMechEdit_actionPerformed(e);
            }
        });
        jButtonChanMechEdit.setText("Edit selected Channel Mechanism");
        jPanelChannelMechsInnerTab.setLayout(borderLayout20);
        jPanelChanMechsButtons.setBorder(BorderFactory.createEtchedBorder());
        jPanelChanMechsButtons.setLayout(borderLayout22);
        jPanelChannelMechsInnerTab.setBorder(null);
        jPanelChannelMechsMain.setBorder(BorderFactory.createEtchedBorder());
        jPanelChannelMechsMain.setLayout(borderLayout11);*/
        gridLayout1.setColumns(1);
        gridLayout1.setRows(2);
        jLabelChanMechTitle.setBorder(border2);
        jLabelChanMechTitle.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelChanMechTitle.setText("The following are the channel mechanisms available to cells in this " +
    "project");
        jLabelSynapseTitle.setText("The following are the synaptic mechanisms available when building " +
    "networks");
        jLabelSynapseTitle.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelSynapseTitle.setBorder(border2);
        jMenuItemZipUp.setText("Zip this Project...");
        jMenuItemZipUp.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemZipUp_actionPerformed(e);
            }
        });
        jLabelNumCellGroups.setEnabled(false);
        jLabelNumCellGroups.setText("Cell Groups:");
        //jPanelCellGroupClicks.setEditable(false);
        jLabelProjFileVersion.setEnabled(false);
        jLabelProjFileVersion.setText("Project File Version:");
        jTextFieldProjFileVersion.setEditable(false);
        jTextFieldProjFileVersion.setText("");
        jMenuItemUnzipProject.setText("Import Zipped Project...");
        jMenuItemImportLevel123.setText("Import NeuroML Levels 1-3...");

        jMenuItemUnzipProject.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemUnzipProject_actionPerformed(e);
            }
        });
        
       jMenuItemImportLevel123.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemImportLevel123_actionPerformed(e);
            }
        });
        
        
        //jMenuExamples.setText("Load Example Project");
        //jMenuModels.setText("Load Detailed Model");
        jMenuOsbModels.setText("Open Source Brain Project");


        jComboBoxView3DChoice.addPopupMenuListener(new javax.swing.event.PopupMenuListener()
        {
            public void popupMenuCanceled(PopupMenuEvent e)
            {
            }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
            {
            }
            public void popupMenuWillBecomeVisible(PopupMenuEvent e)
            {
                jComboBoxView3DChoice_popupMenuWillBecomeVisible(e);
            }
        });
        jPanelNetSetAA.setBorder(BorderFactory.createEtchedBorder());
        jPanelNetSetAA.setLayout(borderLayout24);
        jPanelAllNetSettings.setLayout(gridLayout2);
        gridLayout2.setColumns(1);
        gridLayout2.setRows(2);
        jPanelNetSetTable.setLayout(gridLayout3);
        jPanelSynapseButtons.setLayout(borderLayout21);
        jLabelNetConnSimpleConn.setMaximumSize(new Dimension(289, 25));
        jLabelNetConnSimpleConn.setMinimumSize(new Dimension(289, 25));
        jLabelNetConnSimpleConn.setPreferredSize(new Dimension(289, 25));
        jLabelNetConnSimpleConn.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelNetConnSimpleConn.setText("Morphology Based Connections");
        jPanelNetSetControls.setBorder(BorderFactory.createEtchedBorder());
        jPanelNetSetControls.setMaximumSize(new Dimension(2147483647, 2147483647));
        jPanelNetSetControls.setMinimumSize(new Dimension(968, 64));
        jPanelNetSetControls.setPreferredSize(new Dimension(968, 64));
        jPanelNetSetControls.setLayout(borderLayout23);
        jPanelNetConnButtonsOnly.setMinimumSize(new Dimension(664, 35));
        jLabelNetSetAA.setMaximumSize(new Dimension(149, 25));
        jLabelNetSetAA.setMinimumSize(new Dimension(149, 25));
        jLabelNetSetAA.setPreferredSize(new Dimension(149, 25));
        jLabelNetSetAA.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelNetSetAA.setText("Volume Based Connections");
        jButtonNetAAAdd.setEnabled(false);
        jButtonNetAAAdd.setText("Add Volume Based Conn");
        jButtonNetAAAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNetAAAdd_actionPerformed(e);
            }
        });
        jPanelNetSetAAControls.setBorder(BorderFactory.createEtchedBorder());
        jPanelNetSetAAControls.setMinimumSize(new Dimension(153, 64));
        jPanelNetSetAAControls.setPreferredSize(new Dimension(153, 64));
        jPanelNetSetAAControls.setLayout(borderLayout25);
        jButtonCellGroupsEdit.setEnabled(false);
        jButtonCellGroupsEdit.setText("Edit selected Cell Group");
        jButtonCellGroupsEdit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellGroupsEdit_actionPerformed(e);
            }
        });
        jPanelNetSetAATable.setLayout(gridLayout4);
        jButtonNetAADelete.setEnabled(false);
        jButtonNetAADelete.setText("Delete selected Volume Based Conn");
        jButtonNetAADelete.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNetAADelete_actionPerformed(e);
            }
        });
        jButtonNetAAEdit.setEnabled(false);
        jButtonNetAAEdit.setText("Edit selected Volume Based Conn");
        jButtonNetAAEdit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNetAAEdit_actionPerformed(e);
            }
        });
        jMenuItemViewProjSource.setText("View Project File Source");
        jMenuItemViewProjSource.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemViewProjSource_actionPerformed(e);
            }
        });
        jButtonCellTypeViewCellInfo.setEnabled(false);
        jButtonCellTypeViewCellInfo.setText("View full Cell Info");
        jButtonCellTypeViewCellInfo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellTypeViewInfo_actionPerformed(e);
            }
        });
        jButtonToggleConsoleOut.setEnabled(true);
        jPanelExportGenesis.setLayout(borderLayout26);
        
        jPanelExportPsics.setLayout(new BorderLayout());
        jPanelExportPynn.setLayout(new BorderLayout());
        
        
        jLabelGenesisMain.setEnabled(false);
        jLabelGenesisMain.setBorder(border8);
        jLabelGenesisMain.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelGenesisMain.setText("Generate code for the GENESIS simulation");
        jPanelExportGenesis.setDebugGraphicsOptions(0);
        jLabelSimulatorNeosimMain.setText("To be continued...");
        //jPanelExportNeosim.setLayout(borderLayout27);
        //jPanelExportNeosim.setBorder(BorderFactory.createEtchedBorder());
        jButtonCellTypeDelete.setEnabled(false);
        jButtonCellTypeDelete.setText("Delete Cell Type");
        jButtonCellTypeDelete.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellTypeDelete_actionPerformed(e);
            }
        });
        jButtonCellTypeCompare.setEnabled(false);
        jButtonCellTypeCompare.setText("Compare Cell...");
        jButtonCellTypeCompare.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellTypeCompare_actionPerformed(e);
            }
        });

        jPanelCellTypeMainInfo.setLayout(flowLayout1);
        jPanelCellTypeDetails.setDebugGraphicsOptions(0);
        jPanelCellTypeInfo.setBorder(BorderFactory.createEtchedBorder());
        jPanelCellTypeMainButtons.setMinimumSize(new Dimension(390, 115));
        jPanelCellTypeMainButtons.setPreferredSize(new Dimension(390, 115));
        jPanelCellTypeMainButtons.setLayout(flowLayout3);
        jButtonCellTypeCopy.setEnabled(false);
        jButtonCellTypeCopy.setText("Create copy of Cell Type");
        jButtonCellTypeCopy.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellTypeCopy_actionPerformed(e);
            }
        });
        jButtonCellTypesMoveToOrigin.setEnabled(false);
        jButtonCellTypesMoveToOrigin.setText("Translate cell to origin");
        jButtonCellTypesMoveToOrigin.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellTypesMoveToOrigin_actionPerformed(e);
            }
        });
        jButtonCellTypesConnect.setEnabled(false);
        jButtonCellTypesConnect.setText("Ensure segs connected to parents");
        jButtonCellTypesConnect.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellTypesConnect_actionPerformed(e);
            }
        });
        jButtonCellTypesMakeSimpConn.setEnabled(false);
        jButtonCellTypesMakeSimpConn.setText("Make Simply Connected");
        jButtonCellTypesMakeSimpConn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellTypesMakeSimpConn_actionPerformed(e);
            }
        });
        jPanelCellTypeManageNumbers.setMinimumSize(new Dimension(600, 35));
        jPanelCellTypeManageNumbers.setPreferredSize(new Dimension(600, 35));
        flowLayout3.setHgap(0);
        flowLayout3.setVgap(0);
        jPanelCellTypesModify.setMinimumSize(new Dimension(700, 35));
        jPanelCellTypesModify.setPreferredSize(new Dimension(700, 35));
        jLabelMainLastModified.setEnabled(false);
        jLabelMainLastModified.setText("Last modified:");
        //jTextFieldMainLastModified.setEnabled(false);
        jTextFieldMainLastModified.setEditable(false);
        jMenuItemGlossaryShow.setText("Glossary");
        jMenuItemGlossaryShow.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemGlossary_actionPerformed(e);
            }
        });
        jMenuItemReleaseNotes.setText("Release Notes");
        jMenuItemReleaseNotes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemRelNotes_actionPerformed(e);
            }
        });
        
        jMenuItemCheckUpdates.setText("Check for Updates");
        jMenuItemCheckUpdates.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemCheckUpdates_actionPerformed(e);
            }
        });
        
        
        jLabelExportMain.setText("Set the main simulation parameters and select the format in which " + "to generate the network");
        jPanelExportHeader.setMinimumSize(new Dimension(625, 35));
        jPanelExportHeader.setPreferredSize(new Dimension(625, 35));
        jPanelNeuronMainSettings.setLayout(gridBagLayout6);

        //jPanelNeuronMainSettings.setLayout(new GridLayout(4,1));
        
        //jPanelHocFileButtons.setBorder(null);
        jPanelHocFile1Buttons.setMinimumSize(new Dimension(709, 60));
        jPanelHocFile1Buttons.setPreferredSize(new Dimension(473, 60));
        jPanelHocFile2Buttons.setMinimumSize(new Dimension(709, 60));
        jPanelHocFile2Buttons.setPreferredSize(new Dimension(473, 60));
        
        
        jTabbedPaneNeuron.setPreferredSize(new Dimension(478, 615));

        jRadioButtonNeuroMLLevel1.setText("Level 1 (Anatomy only)");
        jRadioButtonNeuroMLLevel2.setText("Level 2 (L1 & cell biophysics)");
        jRadioButtonNeuroMLLevel3.setText("Level 3 (L2 & network aspects)");

        jRadioButtonNeuroMLV2.setText("NeuroML v2.0 (alpha)");

        jRadioButtonNeuroMLV2.setForeground(Color.red);
        //jRadioButtonNeuroMLCellChan.setText("L3 & channel details");

        buttonGroupNeuroML.add(jRadioButtonNeuroMLLevel1);
        buttonGroupNeuroML.add(jRadioButtonNeuroMLLevel2);
        buttonGroupNeuroML.add(jRadioButtonNeuroMLLevel3);
        buttonGroupNeuroML.add(jRadioButtonNeuroMLV2);
        //buttonGroupNeuroML.add(jRadioButtonNeuroMLCellChan);

        jButtonNeuroML1Export.setEnabled(false);
        jButtonNeuroML1Export.setText("Export all Cell Types");
        jButtonNeuroML1Export.setToolTipText("<html>Export only cells, including just morphologies (MorphML/Level 1), morphologies & passive electrical<br>" +
                "properties & channel densities (Level 2) or all of previous & allowed synaptic connection locations (Level 3)</html>");

        jButtonNeuroML1Export.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (jRadioButtonNeuroMLLevel1.isSelected())
                    jButtonNeuroMLExport_actionPerformed(e, NeuroMLLevel.NEUROML_LEVEL_1, NeuroMLVersion.NEUROML_VERSION_1);
                else if (jRadioButtonNeuroMLLevel2.isSelected())
                    jButtonNeuroMLExport_actionPerformed(e, NeuroMLLevel.NEUROML_LEVEL_2, NeuroMLVersion.NEUROML_VERSION_1);
                else if (jRadioButtonNeuroMLLevel3.isSelected())
                    jButtonNeuroMLExport_actionPerformed(e, NeuroMLLevel.NEUROML_LEVEL_3, NeuroMLVersion.NEUROML_VERSION_1);
            }
        });
        jButtonNeuroML2aExport.setEnabled(false);
        jButtonNeuroML2aExport.setText("Export all to NeuroML v2alpha");

        jButtonNeuroML2aExport.setToolTipText("<html>...</html>");

        jButtonNeuroML2aExport.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {

                jButtonNeuroMLGenSim_actionPerformed(e, NeuroMLVersion.NEUROML_VERSION_2_ALPHA, LemsOption.NONE);
                    //jButtonNeuroMLExport_actionPerformed(e, NeuroMLLevel.NEUROML_VERSION_2_SPIKING_CELL, NeuroMLVersion.NEUROML_VERSION_2);

            }
        });
        jButtonNeuroML2bExport.setEnabled(false);
        jButtonNeuroML2bExport.setText("Export all to NeuroML v2beta (unstable)");

        jButtonNeuroML2bExport.setToolTipText("<html>...</html>");

        jButtonNeuroML2bExport.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {

                jButtonNeuroMLGenSim_actionPerformed(e, NeuroMLVersion.NEUROML_VERSION_2_BETA, LemsOption.EXECUTE_MODEL);
                    //jButtonNeuroMLExport_actionPerformed(e, NeuroMLLevel.NEUROML_VERSION_2_SPIKING_CELL, NeuroMLVersion.NEUROML_VERSION_2);

            }
        });

        jButtonNeuroML2Lems.setEnabled(false);
        jButtonNeuroML2Lems.setText("Generate NeuroML v2alpha & run with LEMS");

        jButtonNeuroML2Lems.setToolTipText("<html>Generate NeuroML 2 Components for the structure of the network and execute using the LEMS interpreter</html>");

        jButtonNeuroML2Lems.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {

                jButtonNeuroMLGenSim_actionPerformed(e, NeuroMLVersion.NEUROML_VERSION_2_ALPHA, LemsOption.EXECUTE_MODEL);
                    //jButtonNeuroMLExport_actionPerformed(e, NeuroMLLevel.NEUROML_VERSION_2_SPIKING_CELL, NeuroMLVersion.NEUROML_VERSION_2);

            }
        });

        jButtonNeuroML2Graph.setEnabled(false);
        jButtonNeuroML2Graph.setText("Generate Graph of network");


        jButtonNeuroML2Graph.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNeuroMLGenSim_actionPerformed(e, NeuroMLVersion.NEUROML_VERSION_2_ALPHA, LemsOption.GENERATE_GRAPH);

            }
        });

        jButtonNeuroML2NineML.setEnabled(false);
        jButtonNeuroML2NineML.setText("Generate NineML v0.1 equivalent of network");


        jButtonNeuroML2NineML.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNeuroMLGenSim_actionPerformed(e, NeuroMLVersion.NEUROML_VERSION_2_ALPHA, LemsOption.GENERATE_NINEML);

                refreshTabNeuroML();
            }
        });
        



        jLabelNeuroMLMain.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelNeuroMLMain.setHorizontalTextPosition(SwingConstants.CENTER);
        jLabelNeuroMLMain.setText("Export elements of this project to NeuroML format");
        jPanelNeuroML.setLayout(borderLayout31);
        jPanelNeuroMLHeader.setMinimumSize(new Dimension(391, 35));
        jPanelNeuroMLHeader.setPreferredSize(new Dimension(391, 35));
        jPanelNeuroMLHeader.setLayout(gridLayout6);
        jPanelSimSettings.setEnabled(true);
        jPanelSimSettings.setDoubleBuffered(true);
        jLabelNeuroMLGeneratedFiles.setText("Generated NeuroML files:");

        jButtonNeuroMLViewPlain.setEnabled(false);
        jButtonNeuroMLViewPlain.setText("View selected file");
        jButtonNeuroMLViewPlain.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonMorphMLView_actionPerformed(e, false);
            }
        });
        
        jCheckBoxNeuroMLGenNet.setSelected(false);
        jCheckBoxNeuroMLGenNet.setText("Generate single NeuroML Level 3 file");
        jCheckBoxNeuroMLGenNet.setToolTipText("Generate just one Level 3 file containing all cells, channel & synaptic mechanisms, network structure");
        
        jCheckBoxNeuroMLneuroCobjects.setSelected(false);
        jCheckBoxNeuroMLneuroCobjects.setText("Add neuroConstruct annotations");
        jCheckBoxNeuroMLneuroCobjects.setToolTipText("<html>Add annotations in the NeuroML file related to the current neuroConstruct project. These will allow the Level 3 file to be <br>" +
                "reloaded into a new empty neuroConstruct project. Note that the NeuroML file generated will still be valid Level 3<br>" +
                "NeuroML which any compliant application should be able to read. Use of annotations for application specific data is<br>" +
                "common in SBML applications, e.g. CellDesigner, where some extra information (e.g. layout of graphical elements on the<br>" +
                "screen) is embedded in SBML based project files.</html>");
        jCheckBoxNeuroMLneuroCobjects.setEnabled(jCheckBoxNeuroMLGenNet.isSelected());
        
        jCheckBoxNeuroMLGenNet.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                 jCheckBoxNeuroMLGenNet_actionPerformed(e);
            }
            void jCheckBoxNeuroMLGenNet_actionPerformed(ActionEvent e)
           {
                 jCheckBoxNeuroMLneuroCobjects.setEnabled(jCheckBoxNeuroMLGenNet.isSelected());
           }

        });
        
        
        
        jButtonNeuroMLGenSim.setEnabled(false);
        jButtonNeuroMLGenSim.setText("Generate all NeuroML scripts");
        jButtonNeuroMLGenSim.setToolTipText("Generate NeuroML file(s) with all model elements currently within the scope of NeuroML: cells, channel & synaptic mechanisms, network structure");
        
        jButtonNeuroMLGenSim.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNeuroMLGenSim_actionPerformed(e, NeuroMLVersion.NEUROML_VERSION_1, LemsOption.NONE);
            }
        });

        jButtonNeuroMLViewFormatted.setEnabled(false);
        jButtonNeuroMLViewFormatted.setText("View selected file, formatted");
        jButtonNeuroMLViewFormatted.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonMorphMLView_actionPerformed(e, true);
            }
        });


        jPanelNeuroMLMain.setLayout(new BorderLayout());

        jPanelNeuroMLMain.setBorder(BorderFactory.createEtchedBorder());
        
        jPanelNeuroMLL3Exp.setLayout(gridBagLayout33);
        //jPanelNeuroMLPySim.setBorder(BorderFactory.createEtchedBorder());

        jButtonGenesisGenerate.setEnabled(false);
        jButtonGenesisGenerate.setActionCommand("Create GENESIS files");
        jButtonGenesisGenerate.setText("Create GENESIS files");
        jButtonGenesisGenerate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonGenesisGenerate_actionPerformed(e);
            }
        });
        jButtonGenesisRun.setEnabled(false);
        jButtonGenesisRun.setText("Run GENESIS simulation");
        jButtonGenesisRun.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonGenesisRun_actionPerformed(e);
            }
        });
        
        jButtonPsicsRun.setEnabled(false);
        jButtonPsicsRun.setText("Run PSICS Simulation");
        jButtonPsicsRun.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonPsicsRun_actionPerformed(e);
            }
        });
        
        jButtonPsicsGenerate.setEnabled(false);
        jButtonPsicsGenerate.setText("Generate PSICS files");
        jButtonPsicsGenerate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonPsicsGenerate_actionPerformed(e);
            }
        });
        
        jButtonPynnRun.setEnabled(false);
        jButtonPynnRun.setText("Run PyNN Simulation");
        jButtonPynnRun.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonPynnRun_actionPerformed(e);
            }
        });
        
        jButtonPynnGenerate.setEnabled(false);
        jButtonPynnGenerate.setText("Generate PyNN files");
        jButtonPynnGenerate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonPynnGenerate_actionPerformed(e);
            }
        });
        
        
        //jPanelGenesisMain.setLayout(borderLayout32);
        jButtonGenesisView.setEnabled(false);
        jButtonGenesisView.setDoubleBuffered(false);
        jButtonGenesisView.setText("View:");
        jButtonGenesisView.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonGenesisView_actionPerformed(e);
            }
        });
        jButtonRegionsEdit.setEnabled(false);
        jButtonRegionsEdit.setText("Edit Selected Region");
        jButtonRegionsEdit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonRegionsEdit_actionPerformed(e);
            }
        });
        jPanelNeuronExtraHoc.setLayout(gridLayout7);
        gridLayout7.setColumns(1);
        gridLayout7.setRows(1);
        //jPanelNeuronExtraHocBlock.setBorder(BorderFactory.createEtchedBorder());
        jPanelNeuronExtraHocBlock.setLayout(new BorderLayout());
        jPanelGenesisExtraBlock.setLayout(new BorderLayout());

        jTextAreaNeuronBlockDesc.setBorder(BorderFactory.createEtchedBorder());
        jTextAreaNeuronBlockDesc.setEditable(false);
        jTextAreaNeuronBlockDesc.setMinimumSize(new Dimension(600, 40));
        jTextAreaNeuronBlockDesc.setPreferredSize(new Dimension(600, 40));
        jTextAreaNeuronBlockDesc.setWrapStyleWord(true);
        jTextAreaNeuronBlockDesc.setLineWrap(true);

        jTextAreaGenesisBlockDesc.setBorder(BorderFactory.createEtchedBorder());
        jTextAreaGenesisBlockDesc.setEditable(false);
        jTextAreaGenesisBlockDesc.setMinimumSize(new Dimension(600, 40));
        jTextAreaGenesisBlockDesc.setPreferredSize(new Dimension(600, 40));
        jTextAreaGenesisBlockDesc.setWrapStyleWord(true);
        jTextAreaGenesisBlockDesc.setLineWrap(true);



        //jTextAreaNeuronBlockDesc.setHorizontalAlignment(SwingConstants.CENTER);
        jTextAreaNeuronBlockDesc.setText("This code will be included before creation of the cell groups");


        jTextAreaNeuronBlock.setEnabled(false);
        jTextAreaNeuronBlock.setBorder(border5);
        jTextAreaNeuronBlock.setEditable(true);

        jTextAreaGenesisBlock.setEnabled(false);
        jTextAreaGenesisBlock.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        jTextAreaGenesisBlock.setEditable(true);

        jScrollPaneNeuronBlock.setEnabled(false);

        jScrollPaneNeuronBlock.setMinimumSize(new Dimension(400, 430));
        jScrollPaneNeuronBlock.setPreferredSize(new Dimension(400, 430));



        jScrollPaneGenesisBlock.setMinimumSize(new Dimension(400, 430));
        jScrollPaneGenesisBlock.setPreferredSize(new Dimension(400, 430));

        ////jScrollPaneNeuronAfter.setMaximumSize(new Dimension(440, 450));
        ////jScrollPaneNeuronAfter.setMinimumSize(new Dimension(440, 450));
        ////jScrollPaneNeuronAfter.setPreferredSize(new Dimension(440, 450));
        jComboBoxNeuronFileList.setEnabled(false);
        jComboBoxPynnFileList.setEnabled(false);
        jComboBoxPsicsFileList.setEnabled(false);
        this.jCheckBoxNeuronLineNums.setEnabled(false);
        this.jCheckBoxGenesisLineNums.setEnabled(false);

        jMenuItemPlotEquation.setText("Create Data Set from Expression");
        jMenuItemPlotEquation.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemPlotEquation_actionPerformed(e);
            }
        });

        jMenuItemPlotImport.setText("Import Data for Plot (Text File)");
        jMenuItemPlotImport.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemPlotImport_actionPerformed(e);
            }
        });
        jMenuItemPlotImportHDF5.setText("Import Data for Plot (HDF5 File)");
        
        jMenuItemPlotImportHDF5.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemPlotImportHDF5_actionPerformed(e);
            }
        });

        jMenuItemCondorMonitor.setText("Condor Monitor");
        jMenuItemCondorMonitor.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemCondorMonitor_actionPerformed(e);
            }
        });




        jMenuItemMPIMonitor.setText("Parallel Monitor");
        jMenuItemMPIMonitor.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemMPIMonitor_actionPerformed(e);
            }
        });





        jRadioButtonNeuronSimDontRecord.setEnabled(false);
        jRadioButtonNeuronSimDontRecord.setSelected(true);
        jRadioButtonNeuronSimSaveToFile.setEnabled(false);
        jRadioButtonNeuronSimSaveToFile.setSelected(false);
        jLabelSimRef.setEnabled(false);
        jTextFieldSimRef.setEnabled(false);
        jCheckBoxSpecifySimRef.setEnabled(false);
        jCheckBoxSpecifySimRef.setText("Overwrite");
        jCheckBoxNeuronSaveHoc.setEnabled(false);
        jCheckBoxNeuronSaveHoc.setText("Save copy of hoc files");
        jLabelSimDefDur.setEnabled(false);
        jLabelSimDefDur.setForeground(Color.black);
        jTextFieldSimDefDur.setEnabled(false);
        jLabelSimDT.setEnabled(false);
        jTextFieldSimDT.setEnabled(false);
        jLabelSimSummary.setEnabled(false);
        jLabelSimSummary.setBorder(border10);
        jLabelSimSummary.setHorizontalAlignment(SwingConstants.CENTER);

        jButtonNeuronCreateCondor.setEnabled(false);
        jButtonNeuronCreateCondor.setText("Create files for sending to Condor");
        jButtonNeuronCreateCondor.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNeuronCreateCondor_actionPerformed(e);
            }
        });

/*
        jButtonNeuronCreateMPIHoc.setEnabled(false);
        jButtonNeuronCreateMPIHoc.setText("Create hoc for MPI based execution");
        jButtonNeuronCreateMPIHoc.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNeuronCreateMPI_actionPerformed(e);
            }
        });*/

        String warnPyH5 = "<html>These are EXPERIMENTAL features for generating NEURON code using Python (and HDF5) to load the network<br>" +
                "structure from NetworkML. Only tested on Linux so far. Works fine, if NEURON, Python & HDF5 installed correctly,<br>" +
                "but no documentation so far...</html>";

        jButtonNeuronCreatePythonXML.setEnabled(false);
        jButtonNeuronCreatePythonXML.setText("Create Python/XML/hoc sim (beta)");
        jButtonNeuronCreatePythonXML.setToolTipText(warnPyH5);
        
        jButtonNeuronCreatePythonXML.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNeuronCreatePythonXML_actionPerformed(e);
            }
        });

        jButtonNeuronCreatePyHDF5.setEnabled(false);
        jButtonNeuronCreatePyHDF5.setText("Create Python/HDF5 sim (alpha)");
        jButtonNeuronCreatePyHDF5.setToolTipText(warnPyH5);
        
        
        jButtonNeuronCreatePyHDF5.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNeuronCreatePyHDF5_actionPerformed(e);
            }
        });



        jComboBoxGenesisFiles.setEnabled(false);

        jPanelSimGeneral.setLayout(borderLayout33);
        jLabelNeuronMainLabel.setBorder(border7);
        jLabelNeuronMainLabel.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelNeuronMainLabel.setText("Generate code for the NEURON simulator");
        jLabelSimulationGlobRa.setText("Default specific axial resistance:");
        jTextFieldSimulationGlobRa.setText("1");
        jTextFieldSimulationGlobRa.setColumns(6);
        jLabelSimulationGlobCm.setText("Default specific membrane capacitance:");
        jTextFieldSimulationGlobCm.setText("2");
        jTextFieldSimulationGlobCm.setColumns(6);

        /*
        jPanelGenesisExtraBefore.setLayout(flowLayout7);
        jPanelGenesisExtraBefore.setBorder(BorderFactory.createEtchedBorder());

        jLabelGenesisExtraBefore.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelGenesisExtraBefore.setText("This code will be included before creation of the cell " +
    "groups");
        jTextAreaGenesisAfter.setEnabled(false);
        jTextAreaGenesisAfter.setBorder(border6);
        jPanelGenesisExtraAfter.setBorder(BorderFactory.createEtchedBorder());
        jPanelGenesisExtraAfter.setLayout(flowLayout6);

        */
        jPanelGenesisExtra.setLayout(gridLayout8);
        gridLayout8.setColumns(2);
        gridLayout8.setRows(1);
        /*
        jScrollPaneGenesisAfter.setMaximumSize(new Dimension(440, 450));
        jScrollPaneGenesisAfter.setMinimumSize(new Dimension(440, 450));
        jScrollPaneGenesisAfter.setPreferredSize(new Dimension(440, 450));
        jLabelGenesisExtraAfter.setBorder(border4);
        jLabelGenesisExtraAfter.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelGenesisExtraAfter.setText("This code will be included after creation of the cell " +
    "groups");
        jScrollPaneGenesisBefore.setEnabled(false);
        jScrollPaneGenesisBefore.setMaximumSize(new Dimension(440, 450));
        jScrollPaneGenesisBefore.setMinimumSize(new Dimension(440, 450));
        jScrollPaneGenesisBefore.setPreferredSize(new Dimension(440, 450));
        jTextAreaGenesisBefore.setEditable(true);
        jTextAreaGenesisBefore.setBorder(border5);
        jTextAreaGenesisBefore.setEnabled(false);
*/
        jPanelSimulationParams.setBorder(null);
        jPanelSimulationParams.setLayout(borderLayout34);
        jPanelSimulationGlobal.setBorder(BorderFactory.createEtchedBorder());
        jPanelSimulationGlobal.setMaximumSize(new Dimension(790, 60));
        jPanelSimulationGlobal.setMinimumSize(new Dimension(790, 60));
        jPanelSimulationGlobal.setPreferredSize(new Dimension(790, 60));
        jPanelSimulationGlobal.setLayout(gridBagLayout5);
        jButton3DPrevSimuls.setEnabled(false);
        jButton3DPrevSimuls.setSelected(false);
        jButton3DPrevSimuls.setText("View Prev Sims in 3D...");
        jButton3DPrevSimuls.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton3DPrevSims_actionPerformed(e);
            }
        });
        jButton3DQuickSims.setEnabled(false);
        jButton3DQuickSims.setSelected(false);
        jButton3DQuickSims.setText("Quick Plot...");
        jButton3DQuickSims.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton3DQuickSims_actionPerformed(e);
            }
        });
        jLabelSimulationGlobRm.setText("Default specific membrane resistance:");
        jTextFieldSimulationGlobRm.setText("50");
        jTextFieldSimulationGlobRm.setColumns(6);
        jLabelSimulationInitVm.setText("Default initial membrane potential:");
        jTextFieldSimulationInitVm.setText("-60");
        jTextFieldSimulationInitVm.setColumns(6);
        jLabelSimulationVLeak.setText("Default membrane leakage potential:");
        jTextFieldSimulationVLeak.setText("-54.6");
        jTextFieldSimulationVLeak.setColumns(6);
        jCheckBoxGenesisSymmetric.setEnabled(false);
        jCheckBoxGenesisSymmetric.setHorizontalAlignment(SwingConstants.CENTER);
        jCheckBoxGenesisSymmetric.setText("Symm compts");
        jPanelGenesisSettings.setLayout(borderLayout35);
        //borderLayout35.setHgap(5);
        //borderLayout35.setVgap(5);
     //   jButtonSimulationRecord.setEnabled(false);
     //   jButtonSimulationRecord.setSelected(false);
    //    jButtonSimulationRecord.setText("Change...");

        jCheckBoxNeuronSumatra.setEnabled(false);
        jCheckBoxNeuronShowShapePlot.setEnabled(false);// jPanelSimulationParams.add(jPanelSimulationWhatToRec,  BorderLayout.NORTH);

        this.jRadioButtonNeuronAllGUI.setEnabled(false);
        this.jRadioButtonNeuronAllGUI.setText("Show all");
        this.jRadioButtonNeuronNoConsole.setEnabled(false);
        this.jRadioButtonNeuronNoConsole.setText("No console");
        this.jRadioButtonNeuronNoPlots.setEnabled(false);
        this.jRadioButtonNeuronNoPlots.setText("No plots");

        buttonGroupNeuronGUI.add(jRadioButtonNeuronAllGUI);
        buttonGroupNeuronGUI.add(jRadioButtonNeuronNoConsole);
        buttonGroupNeuronGUI.add(jRadioButtonNeuronNoPlots);


        this.jRadioButtonGenesisAllGUI.setEnabled(false);
        this.jRadioButtonGenesisAllGUI.setText("Show all");
        this.jRadioButtonGenesisNoConsole.setEnabled(false);
        this.jRadioButtonGenesisNoConsole.setText("No console");
        this.jRadioButtonGenesisNoPlots.setEnabled(false);
        this.jRadioButtonGenesisNoPlots.setText("No plots");

        buttonGroupGenesisGUI.add(jRadioButtonGenesisAllGUI);
        buttonGroupGenesisGUI.add(jRadioButtonGenesisNoConsole);
        buttonGroupGenesisGUI.add(jRadioButtonGenesisNoPlots);

        //jCheckBoxGenesisNoGraphicsMode.setEnabled(false);

        jCheckBoxNeuronSumatra.setText("Sumatra support");
        jCheckBoxNeuronShowShapePlot.setText("Show 3D potential");
        jLabelNeuronGUI.setText("  GUI mode: ");
        jLabelGenesisGUI.setText("GUI mode: ");


        //jCheckBoxGenesisNoGraphicsMode.setText("No GUI mode");

   //     jPanelSimulationWhatToRec.add(jButtonSimulationRecord, null);
        jRadioButtonSimSomaOnly.setSelected(true);
        jRadioButtonSimSomaOnly.setText("Soma of each cell");
        jLabelSimWhatToRecord.setText("Record potential at:");
        jLabelSimWhatToRecord.setEnabled(false);
        jRadioButtonSimAllSegments.setText("Every segment of every cell");
        jRadioButtonSimAllSections.setText("Every section of every cell");
        jRadioButtonGenesisPhy.setEnabled(false);
        jRadioButtonGenesisPhy.setDoubleBuffered(false);
        jRadioButtonGenesisPhy.setSelected(true);
        jRadioButtonGenesisPhy.setText("Generate in Physiological units");
        jRadioButtonGenesisSI.setEnabled(false);
        jRadioButtonGenesisSI.setText("Generate in SI units");
        jPanelCellMechanisms.setLayout(borderLayout28);
        JLabelMechanismMain.setBorder(border9);
        JLabelMechanismMain.setHorizontalAlignment(SwingConstants.CENTER);
        JLabelMechanismMain.setText("The following mechanisms are available for placing on the Cells in " +
    "the project");
        jPanelMechanismMain.setBorder(BorderFactory.createEtchedBorder());
        jPanelMechanismMain.setLayout(borderLayout36);
        jButtonMechanismDelete.setText("Delete selected");
        jButtonMechanismDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButtonMechanismDelete_actionPerformed(e);
            }
        });

    jButtonMechanismEditIt.setText("Edit selected Cell Mechanism");
    
    jButtonMechanismEditIt.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jButtonMechanismEdit_actionPerformed(e);
      }
    });
    
    jButtonMechanismUpdateMaps.setText("Update mappings");
    
    jButtonMechanismUpdateMaps.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jButtonMechanismUpdateMaps_actionPerformed(e);
      }
    });
    
    jButtonMechanismReloadFile.setText("Reload ChannelML file");
    
    jButtonMechanismReloadFile.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jButtonMechanismReloadFile_actionPerformed(e);
      }
    });
    
    jButtonMechanismCopy.setText("Create copy of selected Cell Mechanism");
    
    jButtonMechanismCopy.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
          jButtonMechanismCopy_actionPerformed(e);
      }
    });
    
    jButtonCompareMechanism.setText("Compare Mechanism...");
    
    jButtonCompareMechanism.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
          jButtonCompareMechanism_actionPerformed(e);
      }
    });
    
    jButtonMechanismAbstract.setEnabled(false);
    jButtonMechanismTemplateCML.setEnabled(false);
    jButtonMechanismFileBased.setEnabled(false);
    jButtonMechanismNewCML.setEnabled(false);
    jButtonMechanismAbstract.setText("Add Abstracted Cell Mechanism");
    jButtonMechanismTemplateCML.setText("Add ChannelML from Template");
    jButtonMechanismAbstract.addActionListener(new java.awt.event.ActionListener()
    {
        public void actionPerformed(ActionEvent e)
        {
            jButtonMechanismAdd_actionPerformed(e);
        }
        });
        jPanelMechanismLabel.setLayout(borderLayout37);
        jMenuItemUnits.setText("Units Used");
        jPanelGenesisChoices.setLayout(borderLayout38);
        jButtonMechanismFileBased.setText("Create File Based Mechanism");
        jButtonMechanismNewCML.setText("Create ChannelML Mechanism");
        jButtonMechanismFileBased.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonMechanismFileBased_actionPerformed(e);
            }
        });
        jButtonMechanismNewCML.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonMechanismNewCML_actionPerformed(e);
            }
        });

        this.jButtonMechanismTemplateCML.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonMechanismTemplateCML_actionPerformed(e);
            }
        });


        jLabelSimTemp.setText("Temperature:");
        jTextFieldSimulationTemp.setText("25");
        jTextFieldSimulationTemp.setColumns(6);

        jLabelElectroLenMax.setText("Max electrotonic length:");
        jTextFieldElectroLenMax.setText("25");
        jTextFieldElectroLenMax.setColumns(6);
        jLabelElectroLenMin.setText("Min electrotonic length:");
        jTextFieldElectroLenMin.setText("25");
        jTextFieldElectroLenMin.setColumns(6);
        
        
        jCheckBoxGenesisComments.setEnabled(false);
        jCheckBoxGenesisComments.setText("Generate comments");

        jCheckBoxGenesisShapePlot.setEnabled(false);
        jCheckBoxGenesisShapePlot.setText("Show 3D plot");


        jProgressBarGenerate.setEnabled(false);
        jProgressBarGenerate.setMinimumSize(new Dimension(200, 21));
        jProgressBarGenerate.setPreferredSize(new Dimension(300, 21));
        jProgressBarGenerate.setMinimum(0);
        jProgressBarGenerate.setStringPainted(true);
        jLabelGenesisNumMethod.setEnabled(false);
        jLabelGenesisNumMethod.setText("Num integration method");
        jButtonGenesisNumMethod.setEnabled(false);
        jButtonGenesisNumMethod.setText("Change...");
        jButtonGenesisNumMethod.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonGenesisNumMethod_actionPerformed(e);
            }
        });
        jMenuItemCopyProject.setText("Copy Project (Save As)...");
        jMenuItemCopyProject.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemCopyProject_actionPerformed(e);
            }
        });
        jPanelSimPlot.setBorder(BorderFactory.createEtchedBorder());

        //jPanelSimPlot.setMinimumSize(new Dimension(300, 250));
        jPanelSimPlot.setPreferredSize(new Dimension(300,350));
        jPanelStims.setPreferredSize(new Dimension(300,300));
        jPanelStims.setMaximumSize(new Dimension(300,300));
        jPanelSimPlot.setLayout(borderLayout42);
        jPanelInputOutput.setLayout(borderLayout39);
        jPanelSimWhatToRecord.setLayout(borderLayout40);
        jPanelSimWhatToRecord.setBorder(BorderFactory.createEtchedBorder());
        jPanelSimValsInput.setLayout(borderLayout41);


        jTextFieldSimTotalTimeUnits.setEditable(false);
        jTextFieldSimTotalTimeUnits.setText("");
        jTextFieldSimTotalTimeUnits.setColumns(6);
        jTextFieldSimDTUnits.setEditable(false);
        jTextFieldSimDTUnits.setSelectionStart(11);
        jTextFieldSimDTUnits.setText("");
        jTextFieldSimDTUnits.setColumns(6);
        jButtonSimPlotAdd.setText("Specify new variable to plot/save");
        jButtonSimPlotAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSimPlotAdd_actionPerformed(e);
            }
        });
        jButtonSimPlotDelete.setText("Delete selected plot");
        jButtonSimPlotDelete.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSimPlotDelete_actionPerformed(e);
            }
        });
        //jButtonSimPlotEdit.setToolTipText("");
        //jButtonSimPlotEdit.setActionCommand("jButtonSimPlotEdit");
        jButtonSimPlotEdit.setText("Edit selected plot");
        jButtonSimPlotEdit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSimPlotEdit_actionPerformed(e);
            }
        });
        jButtonSimPlotCopy.setText("Copy selected plot");
        jButtonSimPlotCopy.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSimPlotCopy_actionPerformed(e);
            }
        });
        jButtonSimStimAdd.setText("Add electrophysiological input");
        jButtonSimStimAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSimStimAdd_actionPerformed(e);
            }
        });
        jButtonSimStimDelete.setText("Delete selected input");
        jButtonSimStimDelete.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSimStimDelete_actionPerformed(e);
            }
        });
        jButtonSimStimEdit.setText("Edit selected input");
        jButtonSimStimEdit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSimStimEdit_actionPerformed(e);
            }
        });
        jButtonSimStimCopy.setText("Copy selected input");
        
        jButtonSimStimCopy.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            
                logger.logComment("jButtonSimStimCopy_actionPerformed0...");
                jButtonSimStimCopy_actionPerformed(e);
            }
        });
        
        jTextFieldSimUnitGlobRa.setEditable(false);
        jTextFieldSimUnitGlobRa.setText("");
        jTextFieldSimUnitGlobRa.setColumns(7);
        jTextFieldSimUnitGlotCm.setEditable(false);
        jTextFieldSimUnitGlotCm.setText("");
        jTextFieldSimUnitGlotCm.setColumns(7);

        jTextFieldSimUnitGlobRm.setEditable(false);
        jTextFieldSimUnitGlobRm.setText("");
        jTextFieldSimUnitGlobRm.setColumns(7);
        jTextFieldSimUnitInitVm.setEditable(false);
        jTextFieldSimUnitInitVm.setText("");
        jTextFieldSimUnitInitVm.setColumns(7);
        jTextFieldSimUnitVLeak.setEditable(false);
        jTextFieldSimUnitVLeak.setText("");
        jTextFieldSimUnitVLeak.setColumns(7);

        jTextFieldSimUnitsTemp.setEditable(false);
        jTextFieldSimUnitsTemp.setText("");
        jTextFieldSimUnitsTemp.setColumns(7);

        jTextFieldElecLenMaxUnits.setEditable(false);
        jTextFieldElecLenMaxUnits.setText("");
        jTextFieldElecLenMaxUnits.setColumns(7);
        
        jTextFieldElecLenMinUnits.setEditable(false);
        jTextFieldElecLenMinUnits.setText("");
        jTextFieldElecLenMinUnits.setColumns(7);

        jMenuItemHelpMain.setText("Help");
        jMenuItemHelpMain.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemHelp_actionPerformed(e);
            }
        });
        
        jMenuItemHelpRelNotes.setText("Release Notes");
        jMenuItemHelpRelNotes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemHelpRelNotes_actionPerformed(e);
            }
        });
        
        
        jCheckBoxNeuronComments.setEnabled(false);
        jCheckBoxNeuronComments.setText("Generate comments");
        jButtonCellTypeBioPhys.setEnabled(false);
        jButtonCellTypeBioPhys.setText("Edit Init Potential");
        jButtonCellTypeBioPhys.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellTypeBioPhys_actionPerformed(e);
            }
        });



        jButtonCellTypeEditDesc.setEnabled(false);
        jButtonCellTypeEditDesc.setText("Edit Description");
        jButtonCellTypeEditDesc.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellTypeEditDesc_actionPerformed(e);
            }
        });
        jButtonCellTypeOtherProject.setEnabled(false);
        jButtonCellTypeOtherProject.setDoubleBuffered(false);
        jButtonCellTypeOtherProject.setText("Add Cell Type from another Project..");
        jButtonCellTypeOtherProject.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellTypeOtherProject_actionPerformed(e);
            }
        });
        jMenuItemJava.setText("Java & System Properties");
        jMenuItemJava.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemJava_actionPerformed(e);
            }
        });
        jMenuProject.setText("Project");
        jMenuItemGenNetwork.setText("Generate Positions & Network");
        jMenuItemGenNetwork.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemGenNetwork_actionPerformed(e);
            }
        });
        jMenuItemGenNeuronHoc.setText("Generate NEURON (hoc)");
        jMenuItemGenNeuronHoc.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemGenNeuronHoc_actionPerformed(e);
            }
        });
        jMenuItemGenNeuronPyXML.setText("Generate NEURON (Python/XML)");
        jMenuItemGenNeuronPyXML.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemGenNeuronPyXML_actionPerformed(e);
            }
        });
        jMenuItemGenNeuronPyHDF5.setText("Generate NEURON (Python/HDF5)");
        jMenuItemGenNeuronPyHDF5.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemGenNeuronPyHDF5_actionPerformed(e);
            }
        });

        jMenuItemGenGenesis.setText("Generate GENESIS");
        jMenuItemGenGenesis.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemGenGenesis_actionPerformed(e);
            }
        });
        jMenuItemGenPsics.setText("Generate PSICS");
        jMenuItemGenPsics.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemGenPsics_actionPerformed(e);
            }
        });
        jMenuItemGenPynn.setText("Generate PyNN");
        jMenuItemGenPynn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemGenPynn_actionPerformed(e);
            }
        });

        jMenuItemPrevSims.setText("List Previous Simulations");
        jMenuItemPrevSims.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemPrevSims_actionPerformed(e);
            }
        });

        jMenuItemDataSets.setText("Data Set Manager");
        jMenuItemDataSets.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemDataSets_actionPerformed(e);
            }
        });
        
        JPanel jPanelNeuroMLExpButtons = new JPanel();

        jPanelNeuroMLExpButtons.add(jButtonNeuroML1Export);
        jPanelNeuroMLExpButtons.add(jRadioButtonNeuroMLLevel1);
        jPanelNeuroMLExpButtons.add(jRadioButtonNeuroMLLevel2);
        jPanelNeuroMLExpButtons.add(jRadioButtonNeuroMLLevel3);

        jRadioButtonNeuroMLLevel2.setSelected(true);
        
        
        JPanel nmlV1 = new JPanel(new BorderLayout());

        nmlV1.setBorder(BorderFactory.createTitledBorder("NeuroML v1.x"));

        //nml1.setPreferredSize(new Dimension(1000, 50));

        nmlV1.add(jPanelNeuroMLExpButtons, BorderLayout.NORTH);
        nmlV1.add(jPanelNeuroMLL3Exp, BorderLayout.CENTER);



        JPanel nmlV2 = new JPanel();

        nmlV2.setBorder(BorderFactory.createTitledBorder("NeuroML v2.0"));


        nmlV2.add(jButtonNeuroML2aExport);
        nmlV2.add(jButtonNeuroML2Lems);
        nmlV2.add(jButtonNeuroML2Graph);
        nmlV2.add(jButtonNeuroML2bExport);
        ///////////////////////////////////nmlV2.add(jButtonNeuroML2NineML);
        jCheckBoxSedMl.setSelected(false);
        jCheckBoxSedMl.setToolTipText("Generate a SED-ML simulation description. Note not currently used by LEMS, but this option "
                +"should provide sufficient information to run this simulation in a NeuroML 2 & SED-ML compliant application...");
        
        nmlV2.add(jCheckBoxSedMl);

        Dimension dd = new Dimension(400, 100);
        nmlV2.setPreferredSize(dd);
        nmlV2.setMinimumSize(dd);


        JPanel nmlN = new JPanel(new BorderLayout());
        nmlN.add(nmlV1, BorderLayout.NORTH);
        nmlN.add(nmlV2, BorderLayout.CENTER);


        

        jPanelNeuroMLMain.add(nmlN, BorderLayout.NORTH);
        /*
        jPanelNeuroMLView.add(jPanelNeuroMLL3Exp,
                              new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                                                     GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(6, 0, 6, 0), 0, 0));*/

        JPanel jPanelNeuroMLComp = new JPanel();

        jPanelNeuroMLComp.add(new JLabel("Compartmentalisation to use: "));

        jPanelNeuroMLComp.add(jComboBoxNeuroMLComps);




        

        JPanel jPanelNeuroMLCompsDesc = new JPanel();
        jPanelNeuroMLCompsDesc.add(jTextAreaNeuroMLCompsDesc);


        JPanel nmlC = new JPanel(new BorderLayout());
        nmlC.add(jPanelNeuroMLComp, BorderLayout.NORTH);
        nmlC.add(jPanelNeuroMLCompsDesc, BorderLayout.CENTER);

        jPanelNeuroMLMain.add(nmlC, BorderLayout.CENTER);

        

        JPanel jPanelNeuroMLGenFiles = new JPanel();

        jPanelNeuroMLGenFiles.add(new JLabel("Generated files:"));
        //jComboBoxNeuroML.setPreferredSize(new Dimension(500, 24));

        /*
        ListCellRenderer renderer = new DefaultListCellRenderer();

        ( (JLabel) renderer ).setHorizontalAlignment( SwingConstants.RIGHT );
        //( (JLabel) renderer ).set

        jComboBoxNeuroML.setRenderer(renderer);*/
        
        //jComboBoxNeuroML.set
        jPanelNeuroMLGenFiles.add(jComboBoxNeuroML);


        JPanel nmlS = new JPanel(new BorderLayout());
        nmlS.add(jPanelNeuroMLGenFiles, BorderLayout.NORTH);

        

        JPanel jPanelNeuroMLViewFiles = new JPanel();
        jPanelNeuroMLViewFiles.add(jButtonNeuroMLViewPlain);      
        jPanelNeuroMLViewFiles.add(jButtonNeuroMLViewFormatted);

        nmlS.add(jPanelNeuroMLViewFiles, BorderLayout.CENTER);

        JPanel spacer = new JPanel();
        //spacer.setBackground(Color.red);
        spacer.setPreferredSize(new Dimension(100,200));
        
        nmlS.add(spacer, BorderLayout.SOUTH);

        jPanelNeuroMLMain.add(nmlS, BorderLayout.SOUTH);
        
        
        jPanelNeuroMLL3Exp.add(jButtonNeuroMLGenSim,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.NONE,
                        new Insets(6, 0, 12, 0), 0, 0));
        
        jPanelNeuroMLL3Exp.add(jCheckBoxNeuroMLGenNet,
                new GridBagConstraints(15, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.NONE,
                        new Insets(6, 0, 12, 0), 0, 0));
        
         jPanelNeuroMLL3Exp.add(jCheckBoxNeuroMLneuroCobjects,
                new GridBagConstraints(17, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.NONE,
                        new Insets(6, 0, 12, 0), 0, 0));
/*

        jPanelNeuroMLView.add(jLabelNeuroMLGeneratedFiles,
                              new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
                                                     GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(6, 0, 12, 0), 0, 0));

        jPanelNeuroMLView.add(jComboBoxNeuroML,
                              new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0,
                                                     GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(6, 0, 12, 0), 0, 0));

        jPanelNeuroMLView.add(jButtonNeuroMLViewPlain,
                              new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                                                     GridBagConstraints.EAST,
                                                     GridBagConstraints.NONE,
                                                     new Insets(6, 0, 12, 0), 0, 0));

        jPanelNeuroMLView.add(jButtonNeuroMLViewFormatted,
                              new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                                                     GridBagConstraints.WEST,
                                                     GridBagConstraints.NONE,
                                                     new Insets(6, 0, 12, 0), 0, 0));

        jPanelNeuroMLView.add(jComboBoxNeuroMLComps,
                              new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0,
                                                     GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(6, 0, 12, 0), 0, 0));

        jPanelNeuroMLView.add(jTextAreaNeuroMLCompsDesc,
                              new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0,
                                                     GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(6, 0, 12, 0), 0, 0));


        jPanelNeuroMLView.add(jButtonNeuroMLExportLevel1,
                              new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0,
                                                     GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(6, 0, 12, 0), 0, 0));

        jPanelNeuroMLView.add(jButtonNeuroMLExportLevel2,
                              new GridBagConstraints(0, 7, 2, 1, 0.0, 0.0,
                                                     GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(6, 0, 12, 0), 0, 0));

        jPanelNeuroMLView.add(jButtonNeuroMLExportCellLevel3,
                              new GridBagConstraints(0, 8, 2, 1, 0.0, 0.0,
                                                     GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(6, 0, 12, 0), 0, 0));
*/




        jPanelCellTypeMainButtons.add(jPanelCellTypesButtonsInfo, null);
        jPanelCellTypeMainButtons.add(jPanelCellTypesModify, null);
        jPanelCellTypesModify.add(jButtonCellTypesMoveToOrigin, null);
        //jPanelCellTypesModify.add(jButtonCellTypesConnect, null);
        jPanelCellTypesModify.add(jButtonCellTypesMakeSimpConn, null);
        jPanelCellTypeMainButtons.add(jPanelCellTypeManageNumbers, null);
        jPanelNetSetAAControls.add(jPanelNetSetAAConButtons,  BorderLayout.SOUTH);
        jPanelNetSetAAConButtons.add(jButtonNetAAAdd, null);
        jPanelNetSetAAConButtons.add(jButtonNetAAEdit, null);
        jPanelNetSetAAConButtons.add(jButtonNetAADelete, null);
        jPanelNetSetControls.add(jLabelNetConnSimpleConn, BorderLayout.NORTH);
        ///////jPanelSynapseButtonsOnly.add(jButtonSynapseAdd, null);
        /////jPanelChanMechsButtonsOnly.add(jButtonChanMechAdd, null);
        ////////jPanelChanMechsButtonsOnly.add(jButtonChanMechEdit, null);
        jPanelNetSetControls.add(jPanelNetConnButtonsOnly, BorderLayout.CENTER);
        jPanelNetConnButtonsOnly.add(jButtonNetSetAddNew, null);
        jPanelNetConnButtonsOnly.add(jButtonNetConnEdit, null);
        jPanelNetConnButtonsOnly.add(jButtonNetConnDelete, null);

        jPanelNetSetTable.add(jScrollPaneNetConnects, BorderLayout.CENTER);

        jPanelNetSetAATable.add(jScrollPaneAAConns, BorderLayout.CENTER);

        jPanelNetSetSimple.setBorder(BorderFactory.createEtchedBorder());
        jPanelNetSetSimple.setLayout(borderLayout13);

        jPanelNetSetSimple.add(jPanelNetSetTable, BorderLayout.CENTER);

        jPanelNetSetSimple.add(jPanelNetSetControls, BorderLayout.NORTH);
        //jPanelNetSetMain.add(jPanelNetSetTable, BorderLayout.NORTH);

        jScrollPaneNetConnects.getViewport().add(jTableNetConns, null);
        jScrollPaneAAConns.getViewport().add(jTableAAConns, null);



        jPanelAllNetSettings.add(jPanelNetSetSimple);

        jPanelAllNetSettings.add(jPanelNetSetAA, null);
        jPanelGenerateAnalyse.add(jPanelGenerateComboBoxes, BorderLayout.NORTH);
        jPanelGenerateAnalyse.add(jPanelGenerateAnalyseButtons,  BorderLayout.CENTER);

        jPanelGenerateAnalyseButtons.add(jButtonAnalyseCellDensities, null);
        jPanelGenerateAnalyseButtons.add(jButtonAnalyseNumConns, null);
        jPanelGenerateAnalyseButtons.add(jButtonAnalyseConnLengths, null);
        jPanelGenerateAnalyseButtons.add(jButtonNetworkFullInfo, null);


        imageNewProject = new ImageIcon(ucl.physiol.neuroconstruct.gui.MainFrame.class.getResource(
            "New24.gif"));
        imageOpenProject = new ImageIcon(ucl.physiol.neuroconstruct.gui.MainFrame.class.getResource(
            "Open24.gif"));
        imageSaveProject = new ImageIcon(ucl.physiol.neuroconstruct.gui.MainFrame.class.getResource(
            "Save24.gif"));
        imageCloseProject = new ImageIcon(ucl.physiol.neuroconstruct.gui.MainFrame.class.
                                          getResource("Close24.gif"));
        imageProjectPrefs = new ImageIcon(ucl.physiol.neuroconstruct.gui.MainFrame.class.
                                          getResource("Preferences24.gif"));
        imageNeuroConstruct = new ImageIcon(ucl.physiol.neuroconstruct.gui.MainFrame.class.
                                            getResource("small.png"));

        imageTips = new ImageIcon(ucl.physiol.neuroconstruct.gui.MainFrame.class.
                                          getResource("ToggleTips24.GIF"));
        imageNoTips = new ImageIcon(ucl.physiol.neuroconstruct.gui.MainFrame.class.
                                          getResource("ToggleTipsNone24.GIF"));

        imageConsoleOut = new ImageIcon(ucl.physiol.neuroconstruct.gui.MainFrame.class.
                                                getResource("ConsoleOutOn24.GIF"));
        imageNoConsoleOut = new ImageIcon(ucl.physiol.neuroconstruct.gui.MainFrame.class.
                                                getResource("ConsoleOutOff24.GIF"));


        contentPane = (JPanel)this.getContentPane();
        border1 = BorderFactory.createBevelBorder(BevelBorder.LOWERED, new Color(228, 228, 228),
                                                  new Color(228, 228, 228), new Color(93, 93, 93),
                                                  new Color(134, 134, 134));
        contentPane.setLayout(borderLayout1);

        this.setSize(new Dimension(1000, 800));
        this.setTitle("neuroConstruct v"+GeneralProperties.getVersionNumber());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setText(" ");
        jMenuFile.setText("File");
        jMenuFileExit.setText("Exit");
        jMenuFileExit.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuFileExit_actionPerformed(e);
            }
        });
        jMenuHelp.setText("Help");
        jMenuHelpAbout.setText("About");
        jMenuHelpAbout.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuHelpAbout_actionPerformed(e);
            }
        });

        jMenuItemUnits.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemUsed_actionPerformed(e);
            }
        });

        jButtonOpenProject.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOpenProject_actionPerformed(e);
            }
        });
        jButtonOpenProject.setEnabled(true);
        jButtonOpenProject.setIcon(imageOpenProject);
        jButtonOpenProject.setMargin(new Insets(0, 0, 0, 0));

        jButtonSaveProject.setIcon(imageSaveProject);
        jButtonSaveProject.setMargin(new Insets(0, 0, 0, 0));
        jButtonSaveProject.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSaveProject_actionPerformed(e);
            }
        });



        jButtonValidate.setText("Validate");
        jButtonValidate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonValidate_actionPerformed(e);
            }
        });


        jButtonSaveProject.setEnabled(false);

        jButtonCloseProject.setIcon(imageCloseProject);
        jButtonCloseProject.setMargin(new Insets(0, 0, 0, 0));
        jButtonCloseProject.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCloseProject_actionPerformed(e);
            }
        });
        jButtonCloseProject.setEnabled(false);

        jButtonPreferences.setIcon(imageProjectPrefs);
        jButtonPreferences.setMargin(new Insets(0, 0, 0, 0));
        jButtonPreferences.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonPreferences_actionPerformed(e);
            }
        });
        jButtonPreferences.setEnabled(false);

        //jButtonToggleTips.setIcon(imageTips);

        if (ToolTipManager.sharedInstance().isEnabled())
        {
            jButtonToggleTips.setIcon(imageTips);
        }
        else
        {
            jButtonToggleTips.setIcon(imageNoTips);
        }

        jButtonToggleTips.setMargin(new Insets(0, 0, 0, 0));
        jButtonToggleTips.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonToggleTips_actionPerformed(e);
            }
        });
        jButtonToggleTips.setToolTipText("Turn off/on Tool Tips");

        jButtonToggleConsoleOut.setIcon(imageConsoleOut);
        jButtonToggleConsoleOut.setMargin(new Insets(0, 0, 0, 0));
        jButtonToggleConsoleOut.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonToggleConsoleOut_actionPerformed(e);
            }
        });
        jButtonToggleConsoleOut.setToolTipText("Turn on/off console output. Note: turning this on can diminish application performance");


        contentPane.setBorder(BorderFactory.createEtchedBorder());
        jToolBar.setBorder(BorderFactory.createEtchedBorder());
        jMenuItemFileOpen.setText("Open Project...");
        jMenuItemFileOpen.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemFileOpen_actionPerformed(e);
            }
        });
        jMenuItemSaveProject.setEnabled(false);
        jMenuItemSaveProject.setActionCommand("Save Project");
        jMenuItemSaveProject.setText("Save Project");
        jMenuItemSaveProject.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemSaveProject_actionPerformed(e);
            }
        });

        jMenuSettings.setText("Settings");
        jMenuItemProjProperties.setEnabled(false);
        jMenuItemProjProperties.setText("Project Properties");
        jMenuItemProjProperties.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemProjProperties_actionPerformed(e);
            }
        });
        jMenuTools.setText("Tools");
        /*
        jMenuItemNmodlEditor.setText("nmodlEditor...");
        jMenuItemNmodlEditor.setEnabled(false);
        jMenuItemNmodlEditor.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemNmodlEditor_actionPerformed(e);
            }
        });*/
        jMenuItemNewProject.setText("New Project...");
        jMenuItemNewProject.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemNewProject_actionPerformed(e);
            }
        });
        jButtonNewProject.setText("");
        jButtonNewProject.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNewProject_actionPerformed(e);
            }
        });
        jButtonNewProject.setEnabled(true);
        jButtonNewProject.setMaximumSize(new Dimension(35, 33));
        jButtonNewProject.setIcon(imageNewProject);
        jButtonNewProject.setMargin(new Insets(0, 0, 0, 0));
        //jPanelProjInfo.setAlignmentX( (float) 0.5);
        //jPanelProjInfo.setDebugGraphicsOptions(0);
        jPanelProjInfo.setLayout(gridBagLayout4);
        jTextAreaProjDescription.setBorder(BorderFactory.createEtchedBorder());
        jTextAreaProjDescription.setMinimumSize(new Dimension(300, 750));
        jTextAreaProjDescription.setPreferredSize(new Dimension(300, 750));
        jTextAreaProjDescription.setEditable(false);
        jTextAreaProjDescription.setText(welcomeText);
        jTextAreaProjDescription.setColumns(50);
        jTextAreaProjDescription.setLineWrap(true);
        jTextAreaProjDescription.setRows(20);
        jTextAreaProjDescription.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));

        this.jTextAreaSimConfigDesc.setBorder(BorderFactory.createEtchedBorder());

        jPanelCellTypes.setLayout(borderLayout2);
        jButtonCellTypeNew.setText("New Cell Type");
        //jPanelCellTypeAddNew.setBorder(BorderFactory.createEtchedBorder());

       // jComboBox3DCellToView.addItem(defaultCellTypeToView);

        jButtonCellTypeNew.setEnabled(false);
        jButtonCellTypeNew.setText("Add New Cell Type to Project...");
        jButtonCellTypeNew.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellTypeNew_actionPerformed(e);
            }
        });
        jButtonRegionNew.setEnabled(false);
        jButtonRegionNew.setText("Add New Region...");
        jButtonRegionNew.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonAddRegion_actionPerformed(e);
            }
        });
        jPanelRegionsButtons.setBorder(BorderFactory.createEtchedBorder());
        jPanelRegionsButtons.setDebugGraphicsOptions(0);
        jPanelRegions.setLayout(borderLayout4);
        jPanelRegionsButtons.setBorder(BorderFactory.createEtchedBorder());
        jPanel3DDemo.setLayout(borderLayout5);
        jPanel3DButtons.setBorder(BorderFactory.createEtchedBorder());
        jPanel3DMain.setBorder(BorderFactory.createLoweredBevelBorder());
        jButton3DView.setEnabled(false);
        jButton3DView.setVerifyInputWhenFocusTarget(true);
        jButton3DView.setActionCommand("Construct 3D Demo of Network");
        jButton3DView.setText("View:");
        jButton3DView.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton3DView_actionPerformed(e);
            }
        });
        //jPanel1.setBorder(BorderFactory.createEtchedBorder());
        //jLabel1.setText("Details");
        jPanelCellTypeDetails.setLayout(borderLayout29);
        jPanelCellTypeDetails.setBorder(BorderFactory.createEtchedBorder());
        jPanelCellGroupDetails.setDebugGraphicsOptions(0);
        jPanelCellGroupDetails.setLayout(borderLayout6);
        jButtonNeuronRun.setEnabled(false);
        jButtonNeuronRun.setText("Run simulation");
        jButtonNeuronRun.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNeuronRun_actionPerformed(e);
            }
        });
        jButtonNeuronCreateLocal.setEnabled(false);
        jButtonNeuronCreateLocal.setText("Create hoc simulation");
        jButtonNeuronCreateLocal.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNeuronCreateLocal_actionPerformed(e);
            }
        });
        jPanelHocFile1Buttons.setEnabled(true);
        jPanelHocFile2Buttons.setEnabled(true);
        jPanelCellGroupsMainPanel.setBorder(BorderFactory.createEtchedBorder());
        jPanelCellGroupsMainPanel.setLayout(borderLayout7);
        jButton3DDestroy.setEnabled(false);
        jButton3DDestroy.setText("Stop 3D");
        jButton3DDestroy.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton3DDestroy_actionPerformed(e);
            }
        });
        /*
        jLabelWidth.setText("Width of 3D region:");
        jTextFieldWidth.setColumns(5);
        jLabelWidth.setText("Depth of 3D region:");
        jTextFieldDepth.setColumns(5);
        jLabelDepth.setText("Width of 3D region:");*/

 ///////////////////       jTable3DRegions.setModel(ProjectManager.getCurrentProject().regionsInfo);
        jPanelCellGroupButtons.setBorder(BorderFactory.createEtchedBorder());
        jButtonCellGroupsNew.setEnabled(false);
        jButtonCellGroupsNew.setText("New Cell Group...");
        jButtonCellGroupsNew.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellGroupNew_actionPerformed(e);
            }
        });



        jPanelRegionsTable.setLayout(borderLayout8);
        jLabelName.setEnabled(false);
        jLabelName.setText("Project Name:");
        jLabelMainNumCells.setEnabled(false);
        jLabelMainNumCells.setText("Cell Types in project:");
        /*
        jTextFieldNumCells.setMinimumSize(new Dimension(240, 21));
        jTextFieldNumCells.setPreferredSize(new Dimension(240, 21));
        jTextFieldNumCells.setEditable(false);
        jTextFieldNumCells.setText("");
        jTextFieldNumCells.setColumns(30);*/


        jLabelProjDescription.setEnabled(false);
        jLabelProjDescription.setText("Project Description:");

        jTextFieldProjName.setEnabled(true);
        jTextFieldProjName.setMinimumSize(new Dimension(240, 21));
        jTextFieldProjName.setOpaque(true);
        jTextFieldProjName.setPreferredSize(new Dimension(389, 21));
        jTextFieldProjName.setEditable(false);
        jTextFieldProjName.setText("");
        jTextFieldProjName.setColumns(35);
        jPanelMainInfo.setLayout(gridBagLayout2);
        Border bordEtched = BorderFactory.createEtchedBorder();
        Border bordPad = BorderFactory.createEmptyBorder(5, 5,5,5);
        //bord.
        jPanelMainInfo.setBorder(BorderFactory.createCompoundBorder(bordEtched, bordPad));
        
        //jPanelMainInfo.setMaximumSize(new Dimension(850, 470));
        //jPanelMainInfo.setMinimumSize(new Dimension(900, 490));
        //jPanelMainInfo.setPreferredSize(new Dimension(900, 490));
        
        
        //jScrollPaneProjDesc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        Dimension dimScroll = new Dimension(100, 100);
        jScrollPaneProjDesc.setMaximumSize(dimScroll);
        jScrollPaneProjDesc.setMinimumSize(dimScroll);
        jScrollPaneProjDesc.setPreferredSize(dimScroll);
        
        jLabelSimConfigs.setEnabled(false);
        jLabelSimConfigs.setText("Simulation Configurations:");


        jMenuItemCopyProject.setEnabled(false);
        jPanelNetworkSettings.setLayout(borderLayout9);
        jMenuItemCloseProject.setEnabled(false);
        jMenuItemCloseProject.setText("Close Project");
        jMenuItemCloseProject.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemCloseProject_actionPerformed(e);
            }
        });
        jLabelTitle.setEnabled(true);
        jLabelTitle.setFont(new java.awt.Font("Dialog", 1, 24));
        jLabelTitle.setForeground(new Color(50, 50, 50));
        jLabelTitle.setBorder(border1);
        jLabelTitle.setVerifyInputWhenFocusTarget(true);
        jLabelTitle.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelTitle.setIcon(imageNeuroConstruct);
        jLabelTitle.setText("");
        jPanelExport.setLayout(borderLayout10);
        jLabelSimDefDur.setText("Default Simulation Duration: ");
        //jTextFieldDuration.setText("50");
        jTextFieldSimDefDur.setColumns(6);
        jTextFieldSimDefDur.setHorizontalAlignment(SwingConstants.RIGHT);
        jTextFieldSimDefDur.addKeyListener(new java.awt.event.KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
               //////////// jTextFieldDuration_keyReleased(e);
            }
        });

        //jPanelGenSimSettings.setBackground(Color.lightGray);
        jPanelSimSettings.setBorder(BorderFactory.createEtchedBorder());
        jPanelSimSettings.setLayout(borderLayout12);
        jLabelSimDT.setText("Simulation time step (dt):");
        //jTextFieldDT.setText("0.025");
        jTextFieldSimDT.setColumns(6);
        jTextFieldSimDT.setHorizontalAlignment(SwingConstants.RIGHT);
        jTextFieldSimDT.addKeyListener(new java.awt.event.KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                jTextFieldDT_keyReleased(e);
            }
        });

        jLabelSimSummary.setText("Simulation Summary...");
        jButton3DSettings.setEnabled(false);
        jButton3DSettings.setText("3D Settings");
        jButton3DSettings.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton3DSettings_actionPerformed(e);
            }
        });
        
        jButton3DHelp.setText("?");
        

        jButton3DHelp.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton3DHelp_actionPerformed(e);
            }
        });
        
        jButtonNeuronView.setEnabled(false);
        jButtonNeuronView.setText("View:");
        jButtonNeuronView.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNeuronView_actionPerformed(e);
            }
        });
        jButtonPynnView.setEnabled(false);
        jButtonPynnView.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonPynnView_actionPerformed(e);
            }
        });
        jButtonPsicsView.setEnabled(false);
        jButtonPsicsView.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonPsicsView_actionPerformed(e);
            }
        });
        jLabelSimRef.setText("Simulation Reference:");
        jTextFieldSimRef.setText("Sim_1");
        jTextFieldSimRef.setColumns(10);
        jRadioButtonNeuronSimSaveToFile.setText("Save to file");
        jPanelCellTypeInfo.setLayout(borderLayout30);
        jButtonNetSetAddNew.setEnabled(false);
        jButtonNetSetAddNew.setText("Add Morphology Connection");
        jButtonNetSetAddNew.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNetSetAddNew_actionPerformed(e);
            }
        });
        jButtonCellTypeViewCell.setEnabled(false);
        jButtonCellTypeViewCell.setText("View/edit morphology");
        jButtonCellTypeViewCell.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellTypeViewCell_actionPerformed(e);
            }
        });

        jButtonCellTypeViewCellChans.setEnabled(false);
        jButtonCellTypeViewCellChans.setText("Edit membrane mechanisms");
        jButtonCellTypeViewCellChans.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellTypeViewCellChans_actionPerformed(e);
            }
        });

        jLabelExistingCellTypes.setText("Cell Types included in project:  ");
        jComboBoxCellTypes.setEnabled(false);
        jComboBoxCellTypes.setMaximumSize(new Dimension(32767, 32767));
        jComboBoxCellTypes.setMinimumSize(new Dimension(240, 21));
        jComboBoxCellTypes.setPreferredSize(new Dimension(240, 21));
        jComboBoxCellTypes.setEditable(false);
        jComboBoxCellTypes.setMaximumRowCount(12);
        jComboBoxCellTypes.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxCellTypes_itemStateChanged(e);
            }
        });
        
        
        
        this.jComboBoxNeuroMLComps.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxNeuroMLComps_itemStateChanged(e);
            }
        });
        jTextAreaNeuroMLCompsDesc.setMinimumSize(new Dimension(700, 50));
        jTextAreaNeuroMLCompsDesc.setPreferredSize(new Dimension(700, 50));
        jTextAreaNeuroMLCompsDesc.setBorder(BorderFactory.createEtchedBorder());
        jTextAreaNeuroMLCompsDesc.setLineWrap(true);
        jTextAreaNeuroMLCompsDesc.setWrapStyleWord(true);
        jTextAreaNeuroMLCompsDesc.setEditable(false);


        this.jComboBoxGenesisComps.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxGenesisComps_itemStateChanged(e);
            }
        });
        jTextAreaGenesisCompsDesc.setMinimumSize(new Dimension(700, 50));
        jTextAreaGenesisCompsDesc.setPreferredSize(new Dimension(700, 50));
        jTextAreaGenesisCompsDesc.setBorder(BorderFactory.createEtchedBorder());
        jTextAreaGenesisCompsDesc.setLineWrap(true);
        jTextAreaGenesisCompsDesc.setWrapStyleWord(true);
        jTextAreaGenesisCompsDesc.setEditable(false);





        jComboBoxNeuronExtraBlocks.setEnabled(false);
        jComboBoxNeuronExtraBlocks.setEditable(false);
        jComboBoxNeuronExtraBlocks.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxNeuronExtraBlocks_itemStateChanged(e);
            }
        });
        jComboBoxNeuronExtraBlocks.addItem(this.neuronBlockPrompt);

        ArrayList<NativeCodeLocation> hocLocs = NativeCodeLocation.getAllKnownLocations();

        for (int i = 0; i < hocLocs.size(); i++)
        {
            jComboBoxNeuronExtraBlocks.addItem(hocLocs.get(i));
        }

        jComboBoxGenesisExtraBlocks.setEnabled(false);
        jComboBoxGenesisExtraBlocks.setEditable(false);
        jComboBoxGenesisExtraBlocks.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxGenesisExtraBlocks_itemStateChanged(e);
            }
        });
        jComboBoxGenesisExtraBlocks.addItem(this.genesisBlockPrompt);

        ArrayList<ScriptLocation> scriptlocs = ScriptLocation.getAllKnownLocations();

        for (int i = 0; i < scriptlocs.size(); i++)
        {
            jComboBoxGenesisExtraBlocks.addItem(scriptlocs.get(i));
        }





        jPanelGenerate.setLayout(borderLayout14);
        jComboBoxSimConfig.setEnabled(false);
        jButtonSimConfigEdit.setEnabled(false);
        jButtonGenerateSave.setEnabled(false);
        jButtonGenerateLoad.setEnabled(false);
        jRadioButtonNMLSaveHDF5.setEnabled(false);
        jRadioButtonNMLSavePlainText.setEnabled(false);
        jRadioButtonNMLSaveZipped.setEnabled(false);
        
        jRadioButtonNMLSavePlainText.setSelected(true);
        
        buttonGroupNMLSave.add(jRadioButtonNMLSaveHDF5);
        buttonGroupNMLSave.add(jRadioButtonNMLSavePlainText);
        buttonGroupNMLSave.add(jRadioButtonNMLSaveZipped);
        
        jCheckBoxGenerateExtraNetComments.setEnabled(false);
        jButtonGenerate.setEnabled(false);
        jButtonGenerate.setText("Generate Cell Positions and Connections");
        jButtonGenerateSave.setText("Save NetworkML");
        jButtonGenerateLoad.setText("Load NetworkML");

        //jCheckBoxGenerateZip.setText("Compress");
        jCheckBoxGenerateExtraNetComments.setText("Extra comments");

        jButtonGenerateSave.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonGenerateSave_actionPerformed(e);
            }
        });

        jButtonGenerateLoad.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonGenerateLoad_actionPerformed(e);
            }
        });




        jButtonSimConfigEdit.setText("Edit Simulation Configs");

        jButtonGenerate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonGenerate_actionPerformed(e);
            }
        });
        this.jButtonSimConfigEdit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSimConfigEdit_actionPerformed(e);
            }
        });


        scrollerGenerate.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        Dimension scrollGenDim = new Dimension(700, 290);
        scrollerGenerate.setMaximumSize(scrollGenDim);
        scrollerGenerate.setMinimumSize(scrollGenDim);
        scrollerGenerate.setPreferredSize(scrollGenDim);
        
        jPanelGenerateLoadSave.setBorder(BorderFactory.createEtchedBorder());
        jPanelGenerateButtonsDesc.setBorder(BorderFactory.createEtchedBorder());
        jPanelGenerateMain.setBorder(BorderFactory.createEtchedBorder());
        jPanelGenerateMain.setLayout(gridBagLayout1);
        jButtonRegionRemove.setText("Delete selected Region");
        jButtonRegionRemove.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonRegionRemove_actionPerformed(e);
            }
        });
        jButtonRegionRemove.setEnabled(false);
        jButtonCellGroupsDelete.setEnabled(false);
        jButtonCellGroupsDelete.setText("Delete selected Cell Group");
        jButtonCellGroupsDelete.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellGroupDelete_actionPerformed(e);
            }
        });
        
        jButtonNetConnDelete.setEnabled(false);
        jButtonNetConnDelete.setText("Delete selected Morph Conn");
        jButtonNetConnDelete.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNetConnDelete_actionPerformed(e);
            }
        });
        jRadioButtonNeuronSimDontRecord.setText("Don\'t save");

        jPanelNmodl.setLayout(gridLayout1);
        jPanelSynapseButtons.setBorder(BorderFactory.createEtchedBorder());
        jPanelSynapseMain.setBorder(BorderFactory.createEtchedBorder());
        jPanelSynapseMain.setLayout(borderLayout16);
        /*
        jButtonSynapseAdd.setText("Add New Custom Synapse Type");
        jButtonSynapseAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSynapseAdd_actionPerformed(e);
            }
        });*/
        jPanelStims.setLayout(borderLayout17);
        jLabelGenAnalyse.setText("Analyse:");
        jPanelGenerateAnalyse.setMaximumSize(new Dimension(200, 40));
        jPanelGenerateAnalyse.setMinimumSize(new Dimension(200, 40));
        jPanelGenerateAnalyse.setPreferredSize(new Dimension(200, 40));
        jComboBoxAnalyseCellGroup.setEnabled(false);
        jComboBoxAnalyseNetConn.setEditable(false);


/*
        jButtonSynapseEdit.setText("Edit Selected Synapse Type");
        jButtonSynapseEdit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSynapseEdit_actionPerformed(e);
            }
        });*/
        jMenuItemGeneralProps.setText("General Properties & Project Defaults");
        jMenuItemGeneralProps.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemGeneralProps_actionPerformed(e);
            }
        });

        jButtonGenerateStop.setEnabled(false);
        jTextAreaSimConfigDesc.setEditable(false);
        jTextAreaSimConfigDesc.setEnabled(false);
        jButtonGenerateStop.setText("Stop Generation");
        jButtonGenerateStop.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonGenerateStop_actionPerformed(e);
            }
        });
        jButtonNetConnEdit.setEnabled(false);
        jButtonNetConnEdit.setText("Edit selected Morph Conn");
        jButtonNetConnEdit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNetConnEdit_actionPerformed(e);
            }
        });
        jButtonAnalyseConnLengths.setEnabled(false);
        jButtonAnalyseConnLengths.setText("Analyse connection lengths");
        jButtonAnalyseConnLengths.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonAnalyseConns_actionPerformed(e);
            }
        });
        jButtonNetworkFullInfo.setEnabled(false);
        jButtonNetworkFullInfo.setText("Full net info");
        jButtonNetworkFullInfo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNetworkFullInfo_actionPerformed(e);
            }
        });
        
        
        jComboBoxAnalyseNetConn.setEnabled(false);


        jComboBoxAnalyseNetConn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //System.out.println("e: " + e);
                //if (e.getID()== ItemEvent.SELECTED)
                //{
                    jComboBoxAnalyseCellGroup.removeAllItems();
                    String sel = (String) jComboBoxAnalyseNetConn.getSelectedItem();
                    String selCellGroup = (String) jComboBoxAnalyseCellGroup.getSelectedItem();

                    jComboBoxAnalyseCellGroup.addItem(defaultAnalyseCellGroupString);

                    if (projManager.getCurrentProject() != null)
                    {
                        if (projManager.getCurrentProject().volBasedConnsInfo.isValidVolBasedConn(sel))
                        {
                            String src = projManager.getCurrentProject().volBasedConnsInfo.
                                                              getSourceCellGroup(sel);
                            String tgt = projManager.getCurrentProject().volBasedConnsInfo.
                                                              getTargetCellGroup(sel);
                            if(src.equals(tgt))
                            {
                                src = src+" (source)";
                                tgt = tgt+" (target)";
                                
                            }
                            jComboBoxAnalyseCellGroup.addItem(src);
                            jComboBoxAnalyseCellGroup.addItem(tgt);
                        }
                        if (projManager.getCurrentProject().morphNetworkConnectionsInfo.isValidSimpleNetConn(sel))
                        {
                            String src = projManager.getCurrentProject().morphNetworkConnectionsInfo.
                                                              getSourceCellGroup(sel);
                            String tgt = projManager.getCurrentProject().morphNetworkConnectionsInfo.
                                                              getTargetCellGroup(sel);
                            if(src.equals(tgt))
                            {
                                src = src+" (source)";
                                tgt = tgt+" (target)";
                            }
                            jComboBoxAnalyseCellGroup.addItem(src);
                            jComboBoxAnalyseCellGroup.addItem(tgt);
                        }

                        if (jComboBoxAnalyseCellGroup.getItemCount() > 1)
                        {
                            jComboBoxAnalyseCellGroup.setSelectedIndex(1);
                        }
                        if (selCellGroup!=null && !selCellGroup.equals(defaultAnalyseCellGroupString))
                        {
                            jComboBoxAnalyseCellGroup.setSelectedItem(selCellGroup); // Will do nothing if not in list...
                        }
                    }
                //}
            }
            });

            jButtonAnalyseNumConns.setEnabled(false);
            jButtonAnalyseNumConns.setText("Analyse number of connections");
            jButtonAnalyseNumConns.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    jButtonAnalyseNumConns_actionPerformed(e);
                }
            });

            this.jButtonAnalyseCellDensities.setEnabled(false);
            jButtonAnalyseCellDensities.setText("Analyse cell densities");
            jButtonAnalyseCellDensities.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    jButtonAnalyseCellDensities_actionPerformed(e);
                }
            });



        jPanelRegions.add(jPanelRegionsTable, BorderLayout.CENTER);
        jPanelRegions.add(jPanelRegionsButtons, BorderLayout.NORTH);
        jPanelRegionsButtons.add(jButtonRegionNew, null);
        jPanelRegionsButtons.add(jButtonRegionsEdit, null);
        jToolBar.add(jButtonNewProject, null);
        jToolBar.add(jButtonOpenProject);
        jToolBar.add(jButtonSaveProject);
        jToolBar.add(jButtonCloseProject);
        jToolBar.add(jButtonPreferences);
        jToolBar.addSeparator();
        jToolBar.addSeparator();
        jToolBar.add(jButtonToggleTips);
        jToolBar.add(jButtonToggleConsoleOut);
        jToolBar.addSeparator();
        jToolBar.addSeparator();
        jToolBar.add(jButtonValidate);


        addStandardFileMenuItems();
        
        
        jMenuFile.add(jMenuFileExit);

        jMenuHelp.add(jMenuItemHelpMain);
        jMenuHelp.add(jMenuItemGlossaryShow);
        jMenuHelp.add(jMenuItemReleaseNotes);
        //////////////jMenuHelp.add(jMenuItemHelpRelNotes);
        jMenuHelp.add(jMenuItemUnits);
        jMenuHelp.add(jMenuItemJava);
        jMenuHelp.add(jMenuHelpAbout);
        
        jMenuHelp.addSeparator();
        jMenuHelp.add(jMenuItemCheckUpdates);

        jMenuBar1.add(jMenuFile);
        jMenuBar1.add(jMenuProject);
        jMenuBar1.add(jMenuSettings);
        jMenuBar1.add(jMenuTools);
        jMenuBar1.add(jMenuHelp);

        this.setJMenuBar(jMenuBar1);
        contentPane.add(jToolBar, BorderLayout.NORTH);
        contentPane.add(statusBar, BorderLayout.SOUTH);
        contentPane.add(jTabbedPaneMain, BorderLayout.CENTER);
        jMenuSettings.add(jMenuItemProjProperties);
        jMenuSettings.add(jMenuItemGeneralProps);


        //jMenuTools.add(jMenuItemNmodlEditor);
        jMenuTools.add(jMenuItemViewProjSource);
        jMenuTools.addSeparator();
        //jMenuTools.add(jMenuItemCondorMonitor);
        if (!GeneralUtils.isWindowsBasedPlatform())
        {
            jMenuTools.add(jMenuItemMPIMonitor);
            jMenuTools.addSeparator();
        }
            
        jMenuTools.add(jMenuItemPlotImport);
        jMenuTools.add(jMenuItemPlotImportHDF5);
        jMenuTools.add(jMenuItemPlotEquation);





        jPanelSynapseButtons.add(jLabelSynapseTitle,  BorderLayout.NORTH);
        jPanelSynapseButtons.add(jPanelSynapseButtonsOnly, BorderLayout.CENTER);
        jPanelSynapseButtonsOnly.add(jButtonSynapseEdit, null);
        jPanelSynapseMain.add(jScrollPaneSynapses, BorderLayout.CENTER);
        jPanelChannelMechsMain.add(jScrollPaneChanMechs, BorderLayout.CENTER);
        jPanelNmodl.add(jPanelChannelMechsInnerTab, null);
        jScrollPaneSynapses.getViewport().add(jTableSynapses, null);
        jScrollPaneChanMechs.getViewport().add(jTableChanMechs, null);
        /*
        jPanelSynapticProcesses.add(jPanelSynapseMain, BorderLayout.CENTER);
        jPanelNmodl.add(jPanelSynapticProcesses, null);
        jPanelSynapticProcesses.add(jPanelSynapseButtons,  BorderLayout.NORTH);*/

        jPanelCellTypesButtonsInfo.add(jButtonCellTypeViewCellInfo, null);
        jPanelCellTypesButtonsInfo.add(jButtonCellTypeEditDesc, null);
        jPanelCellTypesButtonsInfo.add(jButtonCellTypeBioPhys, null);
        jPanelCellTypesButtonsInfo.add(jButtonCellTypeViewCell, null);
        jPanelCellTypesButtonsInfo.add(jButtonCellTypeViewCellChans, null);

        jPanelCellTypeInfo.add(jPanelCellTypesComboBox, BorderLayout.NORTH);
        jPanelCellTypesComboBox.add(jLabelExistingCellTypes, null);
        jPanelCellTypesComboBox.add(jComboBoxCellTypes, null);
        jPanelCellTypes.add(jPanelCellTypeDetails, BorderLayout.CENTER);
        jPanelCellTypeDetails.add(jPanelCellTypesAddNewCell,  BorderLayout.NORTH);
        jPanelCellTypesAddNewCell.add(jButtonCellTypeNew, null);
        jPanelCellTypesAddNewCell.add(jButtonCellTypeOtherProject, null);
        jPanelCellTypeDetails.add(jPanelCellTypeInfo, BorderLayout.CENTER);
        //jPanelCellTypeTree.add(jTreeCellDetails, null);

        //jPanelCellTypes.add(jPanel2,  BorderLayout.WEST);

        jPanelGenerate.add(jPanelGenerateButtonsDesc, BorderLayout.NORTH);
        jPanelGenerate.add(jPanelGenerateLoadSave, BorderLayout.SOUTH);

        jPanelGenerateButtons.add(jButtonGenerate, null);
        jPanelGenerateButtons.add(jComboBoxSimConfig, null);

        jPanelGenerateLoadSave.add(jButtonGenerateSave);
        jPanelGenerateLoadSave.add(jRadioButtonNMLSavePlainText);
        jPanelGenerateLoadSave.add(jRadioButtonNMLSaveZipped);
        jPanelGenerateLoadSave.add(jRadioButtonNMLSaveHDF5);
        jPanelGenerateLoadSave.add(this.jCheckBoxGenerateExtraNetComments);
        jPanelGenerateLoadSave.add(jButtonGenerateLoad);

        jComboBoxSimConfig.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxSimConfig_itemStateChanged(e);
            }
        });


        jPanelGenerateButtons.add(jButtonSimConfigEdit, null);
        jPanelGenerateButtons.add(jButtonGenerateStop, null);

        jPanelGenerateDesc.add(jTextAreaSimConfigDesc, null);

        jPanelGenerateButtonsDesc.setLayout(new BorderLayout());

        ///jPanelGenerateLoadSave.setLayout(new BorderLayout());


        jPanelRandomGen.add(jLabelRandomGenDesc);
        jPanelRandomGen.add(jTextFieldRandomGen);
        jTextFieldRandomGen.setColumns(12);
        jTextFieldRandomGen.setText("12345");
        jCheckBoxRandomGen.setSelected(true);
        jPanelRandomGen.add(jCheckBoxRandomGen);

        jPanelNeuronNumInt.add(this.jCheckBoxNeuronNumInt);
        jPanelNeuronNumInt.add(this.jCheckBoxNeuronGenAllMod);
        jPanelNeuronNumInt.add(this.jCheckBoxNeuronCopySimFiles);
        jPanelNeuronNumInt.add(jCheckBoxNeuronForceCorrInit);
        jPanelNeuronNumInt.add(jCheckBoxNeuronModSilent);


        jCheckBoxNeuronForceCorrInit.setSelected(true);


        jCheckBoxNeuronForceCorrInit.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                flagModsToBeRegenerated(e);
            }
        });

        jCheckBoxNeuronModSilent.setSelected(true);

        
        
        jCheckBoxNeuronGenAllMod.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                flagModsToBeRegenerated(e);
            }
        });

        jPanelNeuronRandomGen.setLayout(new FlowLayout());

        jPanelNeuronRandomGen.add(jLabelNeuronRandomGenDesc);

        Dimension dim = new Dimension(600,50);
        jPanelNeuronRandomGen.setPreferredSize(dim);
        jPanelNeuronRandomGen.setMinimumSize(dim);
        jPanelNeuronRandomGen.add(jTextFieldNeuronRandomGen);

        jTextFieldNeuronRandomGen.setColumns(12);
        jTextFieldNeuronRandomGen.setText("12345");
        jCheckBoxNeuronRandomGen.setSelected(true);
        jPanelNeuronRandomGen.add(jCheckBoxNeuronRandomGen);

        jPanelNeuronRandomGen.add(jLabelNeuronFormat);

        jPanelNeuronRandomGen.add(jRadioButtonNeuronFormatText);
        jRadioButtonNeuronFormatText.setSelected(true);
        jPanelNeuronRandomGen.add(jRadioButtonNeuronFormatHDF5);
        buttonGroupNeuronFormat.add(jRadioButtonNeuronFormatText);
        buttonGroupNeuronFormat.add(jRadioButtonNeuronFormatHDF5);

        BorderLayout blComp = new BorderLayout();
        blComp.setHgap(12);
        blComp.setVgap(12);
        this.jPanelGenesisComps.setLayout(blComp);
        this.jPanelGenesisComps.add(jLabelGenesisCompsDesc, BorderLayout.WEST);
        this.jPanelGenesisComps.add(this.jComboBoxGenesisComps, BorderLayout.CENTER);
        this.jPanelGenesisComps.add(this.jTextAreaGenesisCompsDesc, BorderLayout.SOUTH);




        jPanelGenesisRandomGen.add(jLabelGenesisRandomGenDesc);
        jPanelGenesisRandomGen.add(jTextFieldGenesisRandomGen);
        jTextFieldGenesisRandomGen.setColumns(12);
        jTextFieldGenesisRandomGen.setText("12345");
        jCheckBoxGenesisRandomGen.setSelected(true);
        jPanelGenesisRandomGen.add(jCheckBoxGenesisRandomGen);



        jPanelGenerateButtonsDesc.add(jPanelGenerateButtons, BorderLayout.NORTH);
        jPanelGenerateButtonsDesc.add(jPanelGenerateDesc, BorderLayout.CENTER);
        jPanelGenerateButtonsDesc.add(jPanelRandomGen, BorderLayout.SOUTH);

        jTextAreaSimConfigDesc.setSize(600,100);
        jTextAreaSimConfigDesc.setRows(3);
        jTextAreaSimConfigDesc.setWrapStyleWord(true);
        jTextAreaSimConfigDesc.setLineWrap(true);

        jPanelGenerate.add(jPanelGenerateMain, BorderLayout.CENTER);

        jPanelExport.add(jTabbedPaneExportFormats, BorderLayout.CENTER);
        jPanelInputOutput.add(jPanelSimPlot,  BorderLayout.SOUTH);
        jPanelSimPlot.add(jScrollPaneSimPlot, BorderLayout.CENTER);
        jScrollPaneSimPlot.getViewport().add(jTableSimPlot, null);
        jScrollPaneSimStims.getViewport().add(jTableStims, null);

        jPanelSimPlot.add(jPanelSimPlotButtons, BorderLayout.NORTH);
        jPanelSimPlotButtons.add(jButtonSimPlotAdd, null);
        jPanelSimPlotButtons.add(jButtonSimPlotEdit, null);
        jPanelSimPlotButtons.add(jButtonSimPlotCopy, null);

        jPanelInputOutput.add(jPanelStims, BorderLayout.CENTER);

        jLabelSimStimDesc.setText("The following stimulations are applied to the network");

        //jPanelSimulationChoices.add(jLabelSimStimDesc,  BorderLayout.NORTH);
        jPanelStims.add(jPanelSimStimButtons,  BorderLayout.NORTH);

       // jPanelSimRecord.add(jPanelSimWhatToRecord, BorderLayout.NORTH);

        jPanelGenesisSettings.add(jLabelGenesisMain,  BorderLayout.NORTH);
        
        jPanelExportPsics.add(jPanelPsicsMain);
        jPanelExportPynn.add(jPanelPynnMain);

        jPanelPsicsMain.setLayout(new GridBagLayout());
        jLabelPsicsMain.setText("Generate code for the PSICS simulator (beta)");
        
        jRadioButtonPynnNest2.setSelected(true);
                
        jPanelPynnSimOptions.add(jRadioButtonPynnNeuron);
        jPanelPynnSimOptions.add(jRadioButtonPynnNest2);
        jPanelPynnSimOptions.add(jRadioButtonPynnPcsim);
        jPanelPynnSimOptions.add(jRadioButtonPynnBrian);
        jPanelPynnSimOptions.add(jRadioButtonPynnPyMoose);
        
        buttonGroupPynn.add(jRadioButtonPynnNeuron);
        buttonGroupPynn.add(jRadioButtonPynnNest2);
        buttonGroupPynn.add(jRadioButtonPynnPcsim);
        buttonGroupPynn.add(jRadioButtonPynnBrian);
        buttonGroupPynn.add(jRadioButtonPynnPyMoose);
        
        jPanelPsicsPostOptions.add(jCheckBoxPsicsShowHtml);
        jPanelPsicsPostOptions.add(jCheckBoxPsicsShowPlot);
        jPanelPsicsPostOptions.add(jCheckBoxPsicsConsole);

        jPanelPsicsDiscOptions.add(jLabelPsicsSpatDisc);
        jPanelPsicsDiscOptions.add(jTextFieldPsicsSpatDisc);
        jTextFieldPsicsSpatDisc.setColumns(8);
        jPanelPsicsDiscOptions.add(jLabelPsicsSingleCond);
        jPanelPsicsDiscOptions.add(jTextFieldPsicsSingleCond);
        jTextFieldPsicsSingleCond.setColumns(8);
        
        
        jPanelPsicsMain.add(jLabelPsicsMain,
                              new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                     ,GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(20, 0, 0, 0), 20, 20));
        jPanelPsicsMain.add(jPanelPsicsPostOptions,
                              new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                                     ,GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(20, 0, 0, 0), 20, 20));
        jPanelPsicsMain.add(jPanelPsicsDiscOptions,
                              new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                                     ,GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(20, 0, 0, 0), 20, 20));
        jPanelPsicsMain.add(jPanelPsicsButtons,
                              new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                                                     ,GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(20, 0, 0, 0), 20, 20));
        jPanelPsicsMain.add(jPanelPsicsFileView,
                              new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
                                                     ,GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(20, 0, 80, 0), 20, 20));
        
        jPanelPynnMain.setLayout(new GridBagLayout());
        jLabelPynnMain.setText("Generate code for a PyNN simulator (alpha)");
        
        jCheckBoxPynnShowTraces.setText("Show traces after execution");
        jCheckBoxPynnShowTraces.setSelected(true);
        
        
        
        
        
        jPanelPynnOptions1.add(jCheckBoxPynnShowTraces);
        jTextFieldPyNNRandomGen.setColumns(12);
        
        jPanelPynnOptions2.add(jLabelPyNNRandomGenDesc);
        jPanelPynnOptions2.add(jTextFieldPyNNRandomGen);
        jPanelPynnOptions2.add(jCheckBoxPyNNRandomGen);
        
        jPanelPynnMain.add(jLabelPynnMain,
                              new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                     ,GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(20, 0, 20, 0), 20, 20));
        jPanelPynnMain.add(jPanelPynnSimOptions,
                              new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                                     ,GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(20, 0, 20, 0), 20, 20));
        jPanelPynnMain.add(jPanelPynnOptions1,
                              new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                                     ,GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(20, 0, 20, 0), 20, 20));
        jPanelPynnMain.add(jPanelPynnOptions2,
                              new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                                                     ,GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(20, 0, 20, 0), 20, 20));
        jPanelPynnMain.add(jPanelPynnButtons,
                              new GridBagConstraints(0,4, 1, 1, 0.0, 0.0
                                                     ,GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(20, 0, 20, 0), 20, 20));
        jPanelPynnMain.add(jPanelPynnFileView,
                              new GridBagConstraints(0,5, 1, 1, 0.0, 0.0
                                                     ,GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(20, 0, 100, 0), 20, 20));
        
        
        jPanelGenesisMain.setLayout(this.gridBagLayoutGen);

        jPanelGenesisMain.add(jPanelGenesisSettings,
                              new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                     ,GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(20, 0, 0, 0), 0, 30));

        jPanelGenesisMain.add(this.jPanelGenesisRandomGen,
                              new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                                     , GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(0, 0, 20, 0), 0, 0));
        
        jPanelGenesisMain.add(this.jPanelGenesisExtraLinks,
                              new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                                     , GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(0, 0, 20, 0), 0, 0));

        jPanelGenesisMain.add(this.jPanelGenesisComps,
                              new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                                                     , GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(0, 0, 0, 0), 20, 0));

        jPanelGenesisMain.add(jPanelGenesisButtons,
                              new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
                                                     , GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(0, 0, 0, 0), 0, 0));
        
        jPanelGenesisMain.add(jPanelGenesisView,
                              new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
                                                     , GridBagConstraints.CENTER,
                                                     GridBagConstraints.NONE,
                                                     new Insets(0, 0, 120, 0), 0, 0));



        

        jPanelExportNeuron.setLayout(gridLayout5);


        jPanelHocFile1Buttons.add(jButtonNeuronCreateLocal, null);

        //jPanelHocFileButtons.add(jButto nNeuronCreateCondor, null);

        //if (GeneralUtils.isParallelFuncStable())
        //{
            jPanelHocFile1Buttons.add(jButtonNeuronCreatePythonXML, null);
            jPanelHocFile1Buttons.add(jButtonNeuronCreatePyHDF5, null);
            
        //}

        jPanelHocFile2Buttons.add(jButtonNeuronView, null);
        jPanelHocFile2Buttons.add(jComboBoxNeuronFileList, null);
        jPanelHocFile2Buttons.add(jCheckBoxNeuronLineNums, null);
        
        jPanelPynnFileView.add(jButtonPynnView, null);
        jPanelPynnFileView.add(jComboBoxPynnFileList, null);
        jPanelPynnFileView.add(jCheckBoxPynnLineNums, null);
        
        jPanelPsicsFileView.add(jButtonPsicsView, null);
        jPanelPsicsFileView.add(jComboBoxPsicsFileList, null);
        jPanelPsicsFileView.add(jCheckBoxPsicsLineNums, null);
        
        jPanelHocFile1Buttons.add(jButtonNeuronRun, null);
        
        jPanelNeuronExtraLinks.setBorder(BorderFactory.createEtchedBorder());
        
        Dimension d = new Dimension(600, 38);
        jPanelNeuronExtraLinks.setMinimumSize(d);
        jPanelNeuronExtraLinks.setPreferredSize(d);
        

        jPanelGenesisExtraLinks.setBorder(BorderFactory.createEtchedBorder());
        
        Dimension d2 = new Dimension(600, 32);
        jPanelGenesisExtraLinks.setMinimumSize(d2);
        jPanelGenesisExtraLinks.setPreferredSize(d2);
        


        
        jPanelNeuronMainSettings.add(jLabelNeuronMainLabel,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 673, 20));


        jPanelNeuronMainSettings.add(jPanelNeuronGraphOptions,    new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 20));


        jPanelNeuronMainSettings.add(jPanelNeuronNumInt,    new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 20));

        jPanelNeuronMainSettings.add(this.jPanelNeuronRandomGen,    new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 12));

        jPanelNeuronMainSettings.add(this.jPanelNeuronExtraLinks,    new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 0, 6, 0), 0, 12));


        jPanelNeuronMainSettings.add(jPanelHocFile1Buttons,        new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 12));

        jPanelNeuronMainSettings.add(jPanelHocFile2Buttons,        new GridBagConstraints(0, 6, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 150, 0), 0, 70));


  /*      jPanelNeuronMainSettings.add(jLabelNeuronMainLabel);

        jPanelNeuronMainSettings.add(jPanelNeuronGraphOptions);

        jPanelNeuronMainSettings.add(this.jPanelNeuronRandomGen);

        jPanelNeuronMainSettings.add(jPanelHocFileButtons);  */



        jPanelSimGeneral.add(jPanelSimSettings, BorderLayout.NORTH);
        jPanelExportNeuron.add(jTabbedPaneNeuron, null); //jTabbedPaneNeuron.addTab("NMODL mechanisms", null, jPanelNmodl, toolTipText.getToolTip("NMODL"));




        jPanelCellGroupDetails.add(jPanelCellGroupsMainPanel, BorderLayout.CENTER);
        jPanelCellGroupsMainPanel.add(jScrollPaneCellGroups, BorderLayout.CENTER);

        jScrollPaneCellGroups.getViewport().add(jTableCellGroups, null);

        jPanelCellGroupDetails.add(jPanelCellGroupButtons, BorderLayout.NORTH);
        jPanelCellGroupButtons.add(jButtonCellGroupsNew, null);
        jPanelCellGroupButtons.add(jButtonCellGroupsEdit, null);
        jPanelCellGroupButtons.add(jButtonCellGroupsDelete, null);

        
        
        jPanelProjInfo.add(jLabelTitle, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
        		GridBagConstraints.CENTER, GridBagConstraints.NONE, 
        		new Insets(20, 0, 0, 0), 0, 0));

        jPanelProjInfo.add(jPanelMainInfo,    new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
        		GridBagConstraints.CENTER, GridBagConstraints.NONE, 
        		new Insets(0, 0, 0, 0), 0, 0));
        
        

        jPanelMainInfo.add(jLabelName,     new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
        		GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 20, 6, 0), 0, 0));
        
        jPanelMainInfo.add(jTextFieldProjName,      new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 6, 20), 0, 0));
        
        
        
        jPanelMainInfo.add(jLabelProjDescription,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));

        jPanelMainInfo.add(jScrollPaneProjDesc,              new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 0, 6, 0), 580,130));




        jPanelMainInfo.add(jLabelMainNumCells, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,	 
        		GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 20, 6, 0), 0, 0));

        jPanelMainInfo.add(jLabelNumCellGroups, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, 
        		GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 20, 6, 0), 0, 0));




        jPanelMainInfo.add(this.jPanelCellClicks, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, 
        		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 6, 20), 0, 0));


        jPanelMainInfo.add(jPanelCellGroupClicks, new GridBagConstraints(2, 3, 1, 1, 1.0, 0.0, 
        		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 6, 20), 0, 0));
        
        


        jPanelMainInfo.add(jLabelSimConfigs, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, 	
        		GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 20, 6, 6), 0, 0));
        
        jPanelMainInfo.add(jPanelSimConfigClicks, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, 
        		GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 6, 20), 0, 0));
        
        

        jPanelMainInfo.add(jLabelProjFileVersion,     new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 20, 6, 0), 0, 0));
        
        jPanelMainInfo.add(jTextFieldProjFileVersion,       new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 6, 20), 0, 0));
        
        
        
        jPanelMainInfo.add(jLabelMainLastModified,  new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 20, 6, 0), 0, 0));
        
        jPanelMainInfo.add(jTextFieldMainLastModified,   new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 6, 20), 0, 0));




        jPanelCellGroupClicks.setBorder(BorderFactory.createEtchedBorder());
        
        
        this.jPanelSimConfigClicks.setBorder(BorderFactory.createEtchedBorder());
        jPanelCellClicks.setBorder(BorderFactory.createEtchedBorder());
        
        
        //jScrollPaneProjDesc.setViewportView(jTextAreaProjDescription);

        //jPanel1.add(jComboBox1, null);
        //jPanel1.add(jButton1, null);
        jPanel3DDemo.add(jPanel3DButtons, BorderLayout.NORTH);
    //    jPanel3DButtons.add(jComboBox3DCellToView, null);
        jPanel3DButtons.add(jButton3DView, null);
        jPanel3DButtons.add(jComboBoxView3DChoice, null);
        jPanel3DButtons.add(jButton3DDestroy, null);
        jPanel3DButtons.add(jButton3DSettings, null);
        jPanel3DButtons.add(jButton3DHelp, null);

        jPanel3DButtons.add(new JLabel("         "), null);

        jPanel3DButtons.add(jButton3DPrevSimuls, null);
        jPanel3DButtons.add(jButton3DQuickSims, null);


        jPanel3DDemo.add(jPanel3DMain, BorderLayout.CENTER);
/*
        jPanelRegionsButtons.add(jLabelWidth, null);
        jPanelRegionsButtons.add(jTextFieldWidth, null);
        jPanelRegionsButtons.add(jLabelDepth, null);
        jPanelRegionsButtons.add(jTextFieldDepth, null);
      */
        jPanelRegionsButtons.add(jButtonRegionRemove, null);
        jPanelRegionsTable.add(jScrollPaneRegions, BorderLayout.CENTER);
        jScrollPaneRegions.getViewport().add(jTable3DRegions, null);
        /*
         jPanelMainInfo.add(jTextFieldNumCells,  new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
         ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 0, 12, 20), 0, 0));
         */
        //jPanelSimMain.add(jLabel1,      new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
        //   ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 0));


        jPanelSimSettings.add(jPanelSimValsInput, BorderLayout.CENTER);
        jPanelSimSettings.add(jPanelSimStorage, BorderLayout.SOUTH);

        jPanelSimValsInput.add(jLabelSimSummary,  BorderLayout.SOUTH);
        jPanelSimValsInput.add(jPanelSimTotalTime, BorderLayout.NORTH);
        jPanelSimTotalTime.add(jLabelSimDefDur, null);
        jPanelSimTotalTime.add(jTextFieldSimDefDur, null);
        jPanelSimTotalTime.add(jTextFieldSimTotalTimeUnits, null);
        jPanelSimDT.add(jLabelSimDT, null);
        jPanelSimDT.add(jTextFieldSimDT, null);
        jPanelSimDT.add(jTextFieldSimDTUnits, null);

        jTextFieldSimTotalTimeUnits.setText(
             UnitConverter.timeUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol());
         jTextFieldSimDTUnits.setText(
                      UnitConverter.timeUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol());

        //jPanelSimStorage.add(jRadioButtonNeuronSimDontRecord, null);
        //jPanelSimStorage.add(jRadioButtonNeuronSimSaveToFile, null);
        jPanelSimStorage.add(jLabelSimRef, null);
        jPanelSimStorage.add(jTextFieldSimRef, null);
        jPanelSimStorage.add(jCheckBoxSpecifySimRef, null);


        buttonGroupSimSavePreference.add(jRadioButtonNeuronSimSaveToFile);
        buttonGroupSimSavePreference.add(jRadioButtonNeuronSimDontRecord);

        jPanelNetworkSettings.add(jPanelAllNetSettings, BorderLayout.CENTER);



        jPanelCellTypeMainInfo.add(scrollerCellTypeInfo, null);

        jPanelCellTypeInfo.add(jPanelCellTypeMainButtons, BorderLayout.SOUTH);

        jPanelCellTypeInfo.add(jPanelCellTypeMainInfo, BorderLayout.CENTER);

        jComboBoxCellTypes.addItem(cellComboPrompt);

        JViewport vpCellType = scrollerCellTypeInfo.getViewport();
        vpCellType.add(jEditorPaneCellTypeInfo);

        JViewport vpGenerate = scrollerGenerate.getViewport();

        vpGenerate.add(jEditorPaneGenerateInfo);
        
        jEditorPaneGenerateInfo.addHyperlinkListener(this);
        jEditorPaneCellTypeInfo.addHyperlinkListener(this);

        jPanelGenerateMain.add(jPanelGenerateAnalyse,
                               new GridBagConstraints(0, 2, 1, 1,
                                                      1.0, 1.0
                                                      ,GridBagConstraints.CENTER,
                                                      GridBagConstraints.BOTH,
                                                      new Insets(0, 0, 0, 0), 0,0));


        jPanelSimGeneral.add(jPanelSimulationParams,  BorderLayout.CENTER);
        //jPanelSimGeneral.add(this.jPanelSimWhatToRecord,  BorderLayout.SOUTH);
        jEditorPaneGenerateInfo.setContentType("text/html");
        jEditorPaneCellTypeInfo.setContentType("text/html");
        jEditorPaneCellTypeInfo.setEditable(false);

        jEditorPaneCellTypeInfo.setMinimumSize(new Dimension(700,400));
        jEditorPaneCellTypeInfo.setPreferredSize(new Dimension(700,400));


        scrollerCellTypeInfo.setMinimumSize(new Dimension(700, 400));
        scrollerCellTypeInfo.setPreferredSize(new Dimension(700,400));
        scrollerCellTypeInfo.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


        jEditorPaneGenerateInfo.setEditable(false);
        jPanelGenerateComboBoxes.add(jLabelGenAnalyse, null);
        jPanelGenerateComboBoxes.add(jComboBoxAnalyseNetConn, null);
        jPanelGenerateComboBoxes.add(jComboBoxAnalyseCellGroup, null);

        jPanelGenerateMain.add(scrollerGenerate,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        jPanelGenerateMain.add(jProgressBarGenerate,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(12, 0, 6, 0), 400, 0));
    /*
        jPanelChannelMechsInnerTab.add(jPanelChanMechsButtons,  BorderLayout.NORTH);
        jPanelChanMechsButtons.add(jLabelChanMechTitle,  BorderLayout.NORTH);
        jPanelChanMechsButtons.add(jPanelChanMechsButtonsOnly,  BorderLayout.CENTER);

        jPanelChannelMechsInnerTab.add(jPanelChannelMechsMain, BorderLayout.CENTER);*/




        jPanelNetSetAA.add(jPanelNetSetAAControls,  BorderLayout.NORTH);
        jPanelNetSetAAControls.add(jLabelNetSetAA,  BorderLayout.NORTH);
        jPanelNetSetAA.add(jPanelNetSetAATable, BorderLayout.CENTER);


        jPanelSimNeosimMain.add(jLabelSimulatorNeosimMain, null);
        jPanelNeuroML.add(jPanelNeuroMLHeader,  BorderLayout.NORTH);
        jPanelNeuroMLHeader.add(jLabelNeuroMLMain, null);
        
        jPanelNeuroML.add(jPanelNeuroMLMain,  BorderLayout.CENTER);

        //if (GeneralUtils.isParallelFuncStable())
            
        //jPanelNeuroML.add(jPanelNeuroMLPySim, BorderLayout.SOUTH);
        
        jPanelExport.add(jPanelExportHeader, BorderLayout.NORTH);
        jPanelExportHeader.add( jLabelExportMain, null);

        jPanelCellTypeManageNumbers.add(jButtonCellTypeDelete, null);
        jPanelCellTypeManageNumbers.add(jButtonCellTypeCompare, null);

        jPanelCellTypeManageNumbers.add(jButtonCellTypeCopy, null);
        
        jPanelGenesisButtons.add(jButtonGenesisGenerate, null);
        jPanelGenesisButtons.add(jButtonGenesisRun, null);
        
        
        jPanelPsicsButtons.add(jButtonPsicsGenerate, null);
        
        jPanelPsicsButtons.add(jButtonPsicsRun, null);
        
        jPanelPynnButtons.add(jButtonPynnGenerate, null);
        
        jPanelPynnButtons.add(jButtonPynnRun, null);
        
        
        jPanelGenesisView.add(jButtonGenesisView, null);
        jPanelGenesisView.add(jComboBoxGenesisFiles, null);
        jPanelGenesisView.add(jCheckBoxGenesisLineNums, null);
        
        
        
        jPanelExportGenesis.add(jTabbedPaneGenesis,  BorderLayout.CENTER);
        jTabbedPaneGenesis.add(jPanelGenesisMain,   GENESIS_TAB_GENERATE);
        jPanelNeuronExtraHoc.add(jPanelNeuronExtraHocBlock, null);
        jPanelGenesisExtra.add(jPanelGenesisExtraBlock, null);



        jPanelNeuronBlockDesc.add(this.jTextAreaNeuronBlockDesc);
        jPanelGenesisBlockDesc.add(this.jTextAreaGenesisBlockDesc);

        jPanelCBNeuronExtraBlocks.add(this.jComboBoxNeuronExtraBlocks);
        jPanelCBGenesisExtraBlocks.add(this.jComboBoxGenesisExtraBlocks);

        jPanelNeuronExtraHocBlock.add(jPanelCBNeuronExtraBlocks, BorderLayout.NORTH);
        jPanelNeuronExtraHocBlock.add(jPanelNeuronBlockDesc, BorderLayout.CENTER);
        jPanelNeuronExtraHocBlock.add(jScrollPaneNeuronBlock, BorderLayout.SOUTH);

        jPanelGenesisExtraBlock.add(jPanelCBGenesisExtraBlocks, BorderLayout.NORTH);
        jPanelGenesisExtraBlock.add(jPanelGenesisBlockDesc, BorderLayout.CENTER);
        jPanelGenesisExtraBlock.add(jScrollPaneGenesisBlock, BorderLayout.SOUTH);



        jScrollPaneNeuronBlock.getViewport().add(jTextAreaNeuronBlock, null);
        jScrollPaneGenesisBlock.getViewport().add(jTextAreaGenesisBlock, null);


        ///jPanelNeuronExtraHoc.add(jPanelNeuronExtraHocAfter, null);
        ///jPanelNeuronExtraHocAfter.add(jLabelNeuronExtraAfter, null);
        ///jPanelNeuronExtraHocAfter.add(jScrollPaneNeuronAfter, null);
        ////jScrollPaneNeuronAfter.getViewport().add(jTextAreaNeuronAfter, null);



        jPanelSimulationParams.add(jPanelSimulationGlobal,  BorderLayout.CENTER);

        jPanelSimulationGlobal.add(new JLabel("Default values of biophysical properties of new cells and simulation temperature"),      new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(new JLabel("Note: each cell can have its own initial potential, axial resistance, etc."),      new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));

        jPanelSimulationGlobal.add(jLabelSimulationInitVm,      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldSimulationInitVm,          new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldSimUnitInitVm,  new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 14), 0, 0));



        jPanelSimulationGlobal.add(jLabelSimulationVLeak,      new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldSimulationVLeak,        new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldSimUnitVLeak,     new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 14), 0, 0));



        jPanelSimulationGlobal.add(jLabelSimulationGlobRa,     new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldSimulationGlobRa,         new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldSimUnitGlobRa, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 14), 0, 0));


        jPanelSimulationGlobal.add(jLabelSimulationGlobCm,     new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldSimulationGlobCm,        new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldSimUnitGlotCm, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 14), 0, 0));



        jPanelSimulationGlobal.add(jLabelSimulationGlobRm,     new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldSimulationGlobRm,        new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldSimUnitGlobRm,    new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 14), 0, 0));



        jPanelSimulationGlobal.add(jLabelSimTemp,      new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldSimulationTemp,        new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldSimUnitsTemp,   new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 14), 0, 0));


        jPanelSimulationGlobal.add(jLabelElectroLenMax,      new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldElectroLenMax,        new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldElecLenMaxUnits,   new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 14), 0, 0));


        jPanelSimulationGlobal.add(jLabelElectroLenMin,      new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldElectroLenMin,        new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, 0));
        jPanelSimulationGlobal.add(jTextFieldElecLenMinUnits,   new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 14), 0, 0));


        jComboBoxAnalyseCellGroup.addItem(defaultAnalyseCellGroupString);
        jComboBoxAnalyseNetConn.addItem(defaultAnalyseNetConnString);

        //jComboBoxView3DChoice.addItem(choice3DChoiceMain);
        jComboBoxView3DChoice.addItem(LATEST_GENERATED_POSITIONS);
        //jComboBoxPositionsChoice.set

        jComboBoxView3DChoice.setEnabled(false);
        jTabbedPaneGenesis.setSelectedComponent(jPanelGenesisMain);

        jTabbedPaneGenesis.add(jPanelGenesisExtra,  GENESIS_TAB_EXTRA);
        if (MainApplication.isSumatraTestMode())
            jPanelNeuronGraphOptions.add(jCheckBoxNeuronSumatra, null);
        jPanelNeuronGraphOptions.add(jCheckBoxNeuronShowShapePlot, null);
        jPanelNeuronGraphOptions.add(jCheckBoxNeuronComments, null);
        jPanelNeuronGraphOptions.add(this.jLabelNeuronGUI, null);
        jPanelNeuronGraphOptions.add(this.jRadioButtonNeuronAllGUI, null);
        jPanelNeuronGraphOptions.add(this.jRadioButtonNeuronNoPlots, null);
        jPanelNeuronGraphOptions.add(this.jRadioButtonNeuronNoConsole, null);


        jPanelSimWhatToRecord.add(jPanelSimRecordWhere,  BorderLayout.SOUTH);
        jPanelSimRecordWhere.add(jLabelSimWhatToRecord, null);
        jPanelSimRecordWhere.add(jRadioButtonSimSomaOnly, null);
        jPanelSimRecordWhere.add(jRadioButtonSimAllSegments, null);
        jPanelSimRecordWhere.add(jRadioButtonSimAllSections, null);
        ////jPanelSimWhatToRecord.add(jPanelSimStorage, BorderLayout.CENTER);
        buttonGroupSimWhatToRecord.add(jRadioButtonSimSomaOnly);
        buttonGroupSimWhatToRecord.add(jRadioButtonSimAllSegments);
        buttonGroupSimWhatToRecord.add(jRadioButtonSimAllSections);
        jPanelGenesisUnits.add(jRadioButtonGenesisSI, null);
        jPanelGenesisUnits.add(jRadioButtonGenesisPhy, null);
        jPanelGenesisSettings.add(jPanelGenesisChoices, BorderLayout.SOUTH);
        buttonGroupGenesisUnits.add(jRadioButtonGenesisSI);
        buttonGroupGenesisUnits.add(jRadioButtonGenesisPhy);

        jPanelCellMechanisms.add(jPanelMechanismLabel, BorderLayout.NORTH);
        jPanelCellMechanisms.add(jPanelMechanismMain,  BorderLayout.CENTER);

        jPanelMechanismMain.add(jScrollPaneMechanisms,  BorderLayout.CENTER);


        jScrollPaneMechanisms.getViewport().add(jTableMechanisms, null);


        jPanelProcessButtons.setLayout(new BorderLayout());

        jPanelProcessButtons.add(jPanelProcessButtonsTop, BorderLayout.CENTER);
        jPanelProcessButtons.add(jPanelProcessButtonsBottom, BorderLayout.SOUTH);

        //////////jPanelProcessButtonsTop.add(jButtonMechanismAbstract, null);
        jPanelProcessButtonsTop.add(jButtonMechanismFileBased, null);
        jPanelProcessButtonsTop.add(this.jButtonMechanismNewCML, null);
        jPanelProcessButtonsTop.add(this.jButtonMechanismTemplateCML, null);


        jPanelProcessButtonsBottom.add(jButtonMechanismEditIt, null);
        jPanelProcessButtonsBottom.add(jButtonMechanismReloadFile, null);
        jPanelProcessButtonsBottom.add(jButtonMechanismUpdateMaps, null);
        
        //// not enough time to finish this..//// jPanelProcessButtonsBottom.add(jButtonMechanismCopy, null);
        jPanelProcessButtonsBottom.add(jButtonMechanismDelete, null);
        jPanelProcessButtonsBottom.add(jButtonCompareMechanism, null);

        jPanelMechanismLabel.add(jPanelProcessButtons,  BorderLayout.SOUTH);
        jPanelMechanismLabel.add(JLabelMechanismMain,  BorderLayout.NORTH);

        jPanelGenesisChoices.add(jPanelGenesisUnits, BorderLayout.NORTH);
        jPanelGenesisChoices.add(jPanelGenesisNumMethod,  BorderLayout.SOUTH);

        jPanelGenesisSettings.add(jPanelGenesisCheckBoxes0, BorderLayout.CENTER);
        jPanelGenesisCheckBoxes0.setLayout(new BorderLayout());
        jPanelGenesisCheckBoxes0.add(jPanelGenesisCheckBoxes1, BorderLayout.NORTH);
        jPanelGenesisCheckBoxes0.add(jPanelGenesisCheckBoxes2, BorderLayout.CENTER);

        jPanelGenesisCheckBoxes1.add(jCheckBoxGenesisShapePlot, null);
        jPanelGenesisCheckBoxes1.add(jCheckBoxGenesisSymmetric, null);
        jPanelGenesisCheckBoxes1.add(jCheckBoxGenesisComments, null);


        jPanelGenesisCheckBoxes1.add(jLabelGenesisGUI, null);
        jPanelGenesisCheckBoxes1.add(jRadioButtonGenesisAllGUI, null);
        jPanelGenesisCheckBoxes1.add(jRadioButtonGenesisNoPlots, null);
        jPanelGenesisCheckBoxes1.add(jRadioButtonGenesisNoConsole, null);

        jPanelGenesisCheckBoxes1.add(this.jCheckBoxGenesisCopySimFiles);

        jPanelGenesisCheckBoxes2.add(this.jCheckBoxGenesisMooseMode);

        jPanelGenesisCheckBoxes2.add(this.jCheckBoxGenesisReload);
        jPanelGenesisCheckBoxes2.add(this.jTextFieldGenesisReload);

        jPanelGenesisCheckBoxes2.add(this.jLabelGenesisReload);


        jPanelGenesisCheckBoxes2.add(this.jCheckBoxGenesisAbsRefract);
        jPanelGenesisCheckBoxes2.add(this.jTextFieldGenesisAbsRefract);
        jPanelGenesisCheckBoxes2.add(this.jLabelGenesisAbsRefract);

        jCheckBoxGenesisMooseMode.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if (((JCheckBox)e.getItem()).isSelected())
                {
                    jButtonGenesisGenerate.setText("Create MOOSE files");
                    jButtonGenesisRun.setText("Run MOOSE simulation");
                }
                else
                {
                    jButtonGenesisGenerate.setText("Create GENESIS files");
                    jButtonGenesisRun.setText("Run GENESIS simulation");
                }
                //if (e.getStateChange().)pr
            }
        });


        jPanelGenesisNumMethod.add(jLabelGenesisNumMethod, null);
        jPanelGenesisNumMethod.add(jButtonGenesisNumMethod, null);
        jPanelSimValsInput.add(jPanelSimDT,  BorderLayout.CENTER);
        jPanelSimPlotButtons.add(jButtonSimPlotDelete, null);
        jPanelStims.add(jScrollPaneSimStims, BorderLayout.CENTER);
        jPanelSimStimButtons.add(jButtonSimStimAdd, null);
        jPanelSimStimButtons.add(jButtonSimStimEdit, null);
        ///////////////jPanelSimStimButtons.add(jButtonSimStimCopy, null);
        jPanelSimStimButtons.add(jButtonSimStimDelete, null);



        jMenuProject.add(jMenuItemGenNetwork);
        jMenuProject.add(jMenuItemGenNeuronHoc);
        
        if (GeneralUtils.includeParallelFunc())
        {
            jMenuProject.add(jMenuItemGenNeuronPyXML);
            jMenuProject.add(jMenuItemGenNeuronPyHDF5);
        }
        
        jMenuProject.add(jMenuItemGenGenesis);
        
        jMenuProject.add(jMenuItemGenPsics);
        
        

        jTabbedPaneMain.addTab(PROJECT_INFO_TAB, null, jPanelProjInfo, toolTipText.getToolTip("Project Info Tab"));
        jTabbedPaneMain.addTab(CELL_TYPES_TAB, null, jPanelCellTypes, toolTipText.getToolTip("Cell Type Tab"));
        jTabbedPaneMain.addTab(REGIONS_TAB, null, jPanelRegions, toolTipText.getToolTip("Region"));
        jTabbedPaneMain.addTab(CELL_GROUPS_TAB, null, jPanelCellGroupDetails, toolTipText.getToolTip("Cell Groups Tab"));
        jTabbedPaneMain.addTab(CELL_MECHANISM_TAB, null, jPanelCellMechanisms, toolTipText.getToolTip("Cell Mechanism"));

        jTabbedPaneMain.addTab(NETWORK_TAB, null, jPanelNetworkSettings, toolTipText.getToolTip("Network Tab"));

        jTabbedPaneMain.addTab(INPUT_OUTPUT_TAB, null, jPanelInputOutput, toolTipText.getToolTip("Input Output Tab"));

        jTabbedPaneMain.addTab(GENERATE_TAB, null, jPanelGenerate, toolTipText.getToolTip("Generate Tab"));
        jTabbedPaneMain.addTab(VISUALISATION_TAB, null, jPanel3DDemo, toolTipText.getToolTip("Visualisation Tab"));
        jTabbedPaneMain.addTab(EXPORT_TAB, null, jPanelExport, toolTipText.getToolTip("Export Tab"));


        jTabbedPaneExportFormats.add(jPanelSimGeneral, SIMULATOR_SETTINGS_TAB);
        jTabbedPaneExportFormats.add(jPanelExportNeuron, NEURON_SIMULATOR_TAB);
        jTabbedPaneNeuron.add(jPanelNeuronMainSettings, NEURON_TAB_GENERATE);
        jTabbedPaneNeuron.add(jPanelNeuronExtraHoc, NEURON_TAB_EXTRA);
        jTabbedPaneExportFormats.add(jPanelExportGenesis, GENESIS_SIMULATOR_TAB);
        
        jTabbedPaneExportFormats.add(jPanelExportPsics, PSICS_SIMULATOR_TAB);
        

        jTabbedPaneExportFormats.add(jPanelExportPynn, PYNN_SIMULATOR_TAB);
        jMenuProject.add(jMenuItemGenPynn);
  
        
        jMenuProject.addSeparator();
        jMenuProject.add(jMenuItemPrevSims);
        jMenuProject.add(jMenuItemDataSets);


        jTabbedPaneExportFormats.add(jPanelNeuroML, MORPHML_TAB);



        jTextFieldSimUnitInitVm.setText(UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol());
        jTextFieldSimUnitVLeak.setText(UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol());
        jTextFieldSimUnitGlobRa.setText(UnitConverter.specificAxialResistanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol());
        jTextFieldSimUnitGlotCm.setText(UnitConverter.specificCapacitanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol());
        jTextFieldSimUnitGlobRm.setText(UnitConverter.specificMembraneResistanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol());
        jTextFieldSimUnitsTemp.setText(Units.CELSIUS.getSymbol());


    }
    
    
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        logger.logComment("HyperlinkEvent: "+ e.getDescription());
        
        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        {
            String part = e.getDescription().substring(e.getDescription().indexOf("//")+2);
            String type = part.substring(0, part.indexOf("="));
            String instance = part.substring( part.indexOf("=")+1);


            logger.logComment("Going to: "+ instance+", which is a "+ type);

            if (type.equals(ClickProjectHelper.CELL_GROUP))
            {
                jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(CELL_GROUPS_TAB));

                int index = projManager.getCurrentProject().cellGroupsInfo.getAllCellGroupNames().indexOf(instance);
                jTableCellGroups.setRowSelectionInterval(index, index);
            }
            if (type.equals(ClickProjectHelper.REGION))
            {
                jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(REGIONS_TAB));

                int index = projManager.getCurrentProject().regionsInfo.getRegionIndex(instance);
                jTable3DRegions.setRowSelectionInterval(index, index);
            }
            if (type.equals(ClickProjectHelper.CELL_TYPE))
            {
                jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(CELL_TYPES_TAB));

                int index = projManager.getCurrentProject().cellManager.getAllCellTypeNames().indexOf(instance);
                //jTable.setRowSelectionInterval(index, index);
                jComboBoxCellTypes.setSelectedIndex(index);
            }
            if (type.equals(ClickProjectHelper.NET_CONNECTION))
            {
                jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(NETWORK_TAB));
                
                int index =  -1;
                
                if (projManager.getCurrentProject().morphNetworkConnectionsInfo.isValidSimpleNetConn(instance))
                {
                    index = projManager.getCurrentProject().morphNetworkConnectionsInfo.getAllSimpleNetConnNames().indexOf(instance);
                    jTableNetConns.setRowSelectionInterval(index, index);
                }
                    
                if (projManager.getCurrentProject().volBasedConnsInfo.isValidVolBasedConn(instance))
                {
                    index = projManager.getCurrentProject().volBasedConnsInfo.getAllAAConnNames().indexOf(instance);
                    jTableAAConns.setRowSelectionInterval(index, index);
                }
            }
            if (type.equals(ClickProjectHelper.CELL_MECHANISM))
            {
                jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(CELL_MECHANISM_TAB));

                int index = projManager.getCurrentProject().cellMechanismInfo.getAllCellMechanismNames().indexOf(instance);
                jTableMechanisms.setRowSelectionInterval(index, index);
            }
            if (type.equals(ClickProjectHelper.ELEC_INPUT))
            {
                jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(INPUT_OUTPUT_TAB));

                int index = projManager.getCurrentProject().elecInputInfo.getAllStimRefs().indexOf(instance);
                jTableStims.setRowSelectionInterval(index, index);
            }
            if (type.equals(ClickProjectHelper.PLOT_SAVE))
            {
                jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(INPUT_OUTPUT_TAB));

                int index = projManager.getCurrentProject().simPlotInfo.getAllSimPlotRefs().indexOf(instance);
                jTableSimPlot.setRowSelectionInterval(index, index);
            }
        }
        
    }


    /**
     * Extra initiation stuff, not automatically added by the IDE
     */
    private void extraInit()
    {
        // Make sure menus come to front (especially in front of 3D panel...)
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        jComboBoxView3DChoice.setLightWeightPopupEnabled(false);
        //jComboBox.setLightWeightPopupEnabled(false);

        ToolTipManager ttm = ToolTipManager.sharedInstance();
        ttm.setDismissDelay(12000);
        ttm.setReshowDelay(100);

        jTextFieldIClampAmplitude.setText("0.4");
        jTextFieldIClampDuration.setText("100");

        jTextFieldNetStimNumber.setText("10");
        jTextFieldNetStimNoise.setText("0.7");

        jTabbedPaneMain.setBackground(Color.lightGray);

        JViewport vp = jScrollPaneProjDesc.getViewport();

        vp.add(jTextAreaProjDescription);
        jTextAreaProjDescription.setWrapStyleWord(true);

        ArrayList<MorphCompartmentalisation> mcs = CompartmentalisationManager.getAllMorphProjections();

        for (int i = 0; i < mcs.size(); i++)
        {
            this.jComboBoxNeuroMLComps.addItem(mcs.get(i));
            this.jComboBoxGenesisComps.addItem(mcs.get(i));
            if (mcs.get(i).getName().toUpperCase().indexOf("GENESIS")>=0)
            {
                jComboBoxGenesisComps.setSelectedIndex(i);
            }
        }
        //jComboBoxGenesisComps.setSelectedItem();


        addNamedDocumentListner(PROJECT_INFO_TAB, jTextAreaProjDescription);

        // addNamedDocumentListner(REGIONS_TAB, jTextFieldDepth);
        //   addNamedDocumentListner(REGIONS_TAB, jTextFieldWidth);



        addCheckBoxListner(NEURON_SIMULATOR_TAB, jCheckBoxNeuronShowShapePlot);
        addRadioButtonListner(NEURON_SIMULATOR_TAB, this.jRadioButtonNeuronAllGUI);
        addRadioButtonListner(NEURON_SIMULATOR_TAB, this.jRadioButtonNeuronNoConsole);
        addRadioButtonListner(NEURON_SIMULATOR_TAB, this.jRadioButtonNeuronNoPlots);

        addCheckBoxListner(NEURON_SIMULATOR_TAB, jCheckBoxNeuronComments);

        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextFieldSimRef);
        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextFieldSimDefDur);
        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextFieldSimDT);


        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextFieldIClampAmplitude);
        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextFieldIClampDuration);
        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextFieldNetStimNoise);
        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextFieldNetStimNumber);

        addRadioButtonListner(NEURON_SIMULATOR_TAB, jRadioButtonNeuronSimSaveToFile);
        addRadioButtonListner(NEURON_SIMULATOR_TAB, jRadioButtonNeuronSimDontRecord);


        addRadioButtonListner(NEURON_SIMULATOR_TAB, jRadioButtonSimAllSegments);
        addRadioButtonListner(NEURON_SIMULATOR_TAB, jRadioButtonSimSomaOnly);
        addRadioButtonListner(NEURON_SIMULATOR_TAB, jRadioButtonSimAllSections);




        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextFieldSimulationGlobCm);
        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextFieldSimulationGlobRa);
        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextFieldSimulationGlobRm);
        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextFieldSimulationVLeak);
        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextFieldSimulationInitVm);
        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextFieldSimulationTemp);
        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextFieldElectroLenMax);
        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextFieldElectroLenMin);


        addCheckBoxListner(NEURON_SIMULATOR_TAB, jCheckBoxSpecifySimRef);
        addCheckBoxListner(NEURON_SIMULATOR_TAB, jCheckBoxNeuronSaveHoc);
        addCheckBoxListner(NEURON_SIMULATOR_TAB, jCheckBoxNeuronNumInt);
        addCheckBoxListner(NEURON_SIMULATOR_TAB, jCheckBoxNeuronForceCorrInit);
        addCheckBoxListner(NEURON_SIMULATOR_TAB, jCheckBoxNeuronModSilent);
        addRadioButtonListner(NEURON_SIMULATOR_TAB, jRadioButtonNeuronFormatText);
        addRadioButtonListner(NEURON_SIMULATOR_TAB, jRadioButtonNeuronFormatHDF5);
       
        addCheckBoxListner(NEURON_SIMULATOR_TAB, jCheckBoxNeuronGenAllMod);
        addCheckBoxListner(NEURON_SIMULATOR_TAB, jCheckBoxNeuronCopySimFiles);

        addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextAreaNeuronBlock);
        ////addNamedDocumentListner(NEURON_SIMULATOR_TAB, jTextAreaNeuronAfter);




        //////addNamedDocumentListner(GENESIS_SIMULATOR_TAB, jTextAreaGenesisBefore);
        ////////addNamedDocumentListner(GENESIS_SIMULATOR_TAB, jTextAreaGenesisAfter);


        addCheckBoxListner(GENESIS_SIMULATOR_TAB, jCheckBoxGenesisSymmetric);
        addCheckBoxListner(GENESIS_SIMULATOR_TAB, jCheckBoxGenesisComments);
        //addCheckBoxListner(GENESIS_SIMULATOR_TAB, jCheckBoxGenesisVoltPlot);
        addCheckBoxListner(GENESIS_SIMULATOR_TAB, jCheckBoxGenesisShapePlot);


        addCheckBoxListner(GENESIS_SIMULATOR_TAB, jCheckBoxGenesisReload);
        addNamedDocumentListner(GENESIS_SIMULATOR_TAB, jTextFieldGenesisReload);

        addCheckBoxListner(GENESIS_SIMULATOR_TAB, jCheckBoxGenesisAbsRefract);
        addNamedDocumentListner(GENESIS_SIMULATOR_TAB, jTextFieldGenesisAbsRefract);

        addCheckBoxListner(GENESIS_SIMULATOR_TAB, jCheckBoxGenesisCopySimFiles);
        addCheckBoxListner(GENESIS_SIMULATOR_TAB, jCheckBoxGenesisMooseMode);

        addRadioButtonListner(GENESIS_SIMULATOR_TAB, jRadioButtonGenesisAllGUI);
        addRadioButtonListner(GENESIS_SIMULATOR_TAB, jRadioButtonGenesisNoPlots);
        addRadioButtonListner(GENESIS_SIMULATOR_TAB, jRadioButtonGenesisNoConsole);


        addRadioButtonListner(GENESIS_SIMULATOR_TAB, jRadioButtonGenesisPhy);
        addRadioButtonListner(GENESIS_SIMULATOR_TAB, jRadioButtonGenesisSI);
        addNamedDocumentListner(GENESIS_SIMULATOR_TAB, jTextAreaGenesisBlock);



        addCheckBoxListner(PSICS_SIMULATOR_TAB, jCheckBoxPsicsShowHtml);
        addCheckBoxListner(PSICS_SIMULATOR_TAB, jCheckBoxPsicsShowPlot);
        addCheckBoxListner(PSICS_SIMULATOR_TAB, jCheckBoxPsicsConsole);

        addNamedDocumentListner(PSICS_SIMULATOR_TAB, jTextFieldPsicsSpatDisc);
        addNamedDocumentListner(PSICS_SIMULATOR_TAB, jTextFieldPsicsSingleCond);


    }




    private void addToolTips()
    {
        jButtonSaveProject.setToolTipText("Save Project");
        jButtonOpenProject.setToolTipText("Open Project");
        jButtonCloseProject.setToolTipText("Close Project");
        jButtonNewProject.setToolTipText("Project");
        jButtonPreferences.setToolTipText("Preferences");

        jButtonValidate.setToolTipText(toolTipText.getToolTip("Project validity"));


        jButtonCellTypeNew.setToolTipText(toolTipText.getToolTip("Add New Cell Type"));

        jButtonCellTypeOtherProject.setToolTipText(toolTipText.getToolTip("Add New Cell Type From Other Project"));

        jButtonCellTypeBioPhys.setToolTipText(toolTipText.getToolTip("Edit Cell Biophysics"));

        jButtonRegionNew.setToolTipText(toolTipText.getToolTip("Add New Region"));

        jButtonCellGroupsNew.setToolTipText(toolTipText.getToolTip("Cell Group"));

        jButtonMechanismAbstract.setToolTipText(toolTipText.getToolTip("Abstracted Cell Mechanisms"));
        jButtonMechanismFileBased.setToolTipText(toolTipText.getToolTip("File based Cell Mechanisms"));

        jMenuItemNewProject.setToolTipText(toolTipText.getToolTip("Project"));
        jMenuItemFileOpen.setToolTipText(toolTipText.getToolTip("Open project"));
        jMenuItemSaveProject.setToolTipText(toolTipText.getToolTip("Save project"));
        jMenuItemCloseProject.setToolTipText(toolTipText.getToolTip("Close project"));
        jMenuItemCopyProject.setToolTipText(toolTipText.getToolTip("Copy project"));
        jMenuItemZipUp.setToolTipText(toolTipText.getToolTip("Zipped Project"));
        jMenuItemUnzipProject.setToolTipText(toolTipText.getToolTip("Import project"));
//        jMenuItemImportLevel123.setToolTipText(toolTipText.getToolTip("Import Level 3 Network"));

        jMenuItemDataSets.setToolTipText(toolTipText.getToolTip("Data Set Manager"));

        jMenuItemProjProperties.setToolTipText(toolTipText.getToolTip("Project properties"));
        jMenuItemGeneralProps.setToolTipText(toolTipText.getToolTip("General properties"));


        jMenuItemNmodlEditor.setToolTipText(toolTipText.getToolTip("nmodlEditor menu item"));
        jMenuItemViewProjSource.setToolTipText(toolTipText.getToolTip("Project file source"));
        jMenuItemCondorMonitor.setToolTipText(toolTipText.getToolTip("Condor monitor"));
        jMenuItemGlossaryShow.setToolTipText(toolTipText.getToolTip("Glossary menu item"));
        jMenuItemUnits.setToolTipText(toolTipText.getToolTip("Units menu item"));


        ///jPanelChanMechsButtons.setToolTipText(toolTipText.getToolTip("Channel Mechanism"));
        jPanelSynapseButtons.setToolTipText(toolTipText.getToolTip("Synaptic Mechanism"));
        jPanelNetSetControls.setToolTipText(toolTipText.getToolTip("Network Connection"));

        //jPanelNetSetComplexControls.setToolTipText(toolTipText.getToolTip("Complex Network Connection"));

        jButtonNetSetAddNew.setToolTipText(toolTipText.getToolTip("Network Connection"));

        //jButtonNetAAAdd.setToolTipText(toolTipText.getToolTip("Complex Network Connection"));

        jButton3DView.setToolTipText(toolTipText.getToolTip("View 3D"));
        this.jComboBoxView3DChoice.setToolTipText(toolTipText.getToolTip("View 3D"));

        this.jButton3DSettings.setToolTipText("Settings for displaying cells and networks in 3D. "
                                              +"For more go to Help -> Glossary -> 3D View of Cells.");

        this.jButton3DDestroy.setToolTipText("Close the 3D view.");



        jButton3DPrevSimuls.setToolTipText(toolTipText.getToolTip("Previous Simulations"));
        jMenuItemPrevSims.setToolTipText(toolTipText.getToolTip("Previous Simulations"));


        jButton3DQuickSims.setToolTipText("<html>New simulation browser GUI to quickly plot simulation results without viewing full network in 3D<br>" +
                "Note: this can also be run in standalone mode using: <b>./nC.sh -sims projectpath/projectname.ncx</b></html>");

        jButtonSimStimAdd.setToolTipText(toolTipText.getToolTip("Elec Input"));

        String newSavingTip = "Note: to save values calculated during a simulation, go to Input/Output";

        jLabelSimWhatToRecord.setToolTipText(newSavingTip);
        jRadioButtonSimAllSegments.setToolTipText(newSavingTip);
        jRadioButtonSimSomaOnly.setToolTipText(newSavingTip);

        this.jRadioButtonNeuronSimDontRecord.setToolTipText(newSavingTip);

        this.jRadioButtonNeuronSimSaveToFile.setToolTipText(newSavingTip);


        jLabelNeuronFormat.setToolTipText(toolTipText.getToolTip("HDF5 NEURON Save"));
        jRadioButtonNeuronFormatText.setToolTipText(toolTipText.getToolTip("HDF5 NEURON Save"));
        jRadioButtonNeuronFormatHDF5.setToolTipText(toolTipText.getToolTip("HDF5 NEURON Save"));

        jCheckBoxGenesisShapePlot.setToolTipText(toolTipText.getToolTip("GENESIS 3D"));
        this.jCheckBoxGenesisSymmetric.setToolTipText(toolTipText.getToolTip("GENESIS Symmetric"));
        this.jRadioButtonGenesisPhy.setToolTipText(toolTipText.getToolTip("GENESIS Units"));
        this.jRadioButtonGenesisSI.setToolTipText(toolTipText.getToolTip("GENESIS Units"));
        this.jLabelGenesisNumMethod.setToolTipText(toolTipText.getToolTip("GENESIS Num Integration method"));
        
        String reload = toolTipText.getToolTip("GENESIS reload");

        jCheckBoxGenesisReload.setToolTipText(reload);
        jLabelGenesisReload.setToolTipText(reload);
        jTextFieldGenesisReload.setToolTipText(reload);

        String abs_refract = toolTipText.getToolTip("GENESIS abs_refract");


        jCheckBoxGenesisAbsRefract.setToolTipText(abs_refract);
        jLabelGenesisAbsRefract.setToolTipText(abs_refract);
        jTextFieldGenesisAbsRefract.setToolTipText(abs_refract);

        this.jButtonSimConfigEdit.setToolTipText(toolTipText.getToolTip("Simulation Configuration"));

        this.jLabelSimConfigs.setToolTipText(toolTipText.getToolTip("Simulation Configuration"));
        this.jPanelSimConfigClicks.setToolTipText(toolTipText.getToolTip("Simulation Configuration"));



        jButtonCellTypeCopy.setToolTipText(toolTipText.getToolTip("Cell Type Copy"));
        jButtonCellTypeDelete.setToolTipText(toolTipText.getToolTip("Cell Type Delete"));
        jButtonCellTypeCompare.setToolTipText(toolTipText.getToolTip("Cell Type Compare"));
        jButtonCellTypesMoveToOrigin.setToolTipText(toolTipText.getToolTip("Cell Type Move"));
        jButtonCellTypeViewCellInfo.setToolTipText(toolTipText.getToolTip("Cell Type View Cell Info"));
        jButtonCellTypeViewCell.setToolTipText(toolTipText.getToolTip("Cell Type View Cell in 3D"));

        jButtonCellTypeViewCellChans.setToolTipText(toolTipText.getToolTip("Cell Type View Memb Mechs"));

        //this.jbutt
        jButtonCellTypesConnect.setToolTipText(toolTipText.getToolTip("Cell Type Connect"));
        jButtonCellTypesMakeSimpConn.setToolTipText(toolTipText.getToolTip("Cell Type Make Simply Connected"));

        jTextFieldSimRef.setToolTipText(toolTipText.getToolTip("Simulation Reference"));
        jLabelSimRef.setToolTipText(toolTipText.getToolTip("Simulation Reference"));
        jCheckBoxSpecifySimRef.setToolTipText(toolTipText.getToolTip("Specify Reference"));


        //this.jCheckBoxGenesisNoGraphicsMode.setToolTipText(toolTipText.getToolTip("No Graphics Mode"));

        this.jButtonMechanismNewCML.setToolTipText(toolTipText.getToolTip("File Based ChannelML"));
        this.jButtonMechanismTemplateCML.setToolTipText(toolTipText.getToolTip("Template Based ChannelML"));
        this.jButtonMechanismUpdateMaps.setToolTipText(toolTipText.getToolTip("Update ChannelML Mechanism"));
        this.jButtonMechanismReloadFile.setToolTipText(toolTipText.getToolTip("Reload Cell Mechanism file"));


        jLabelSimulationInitVm.setToolTipText(toolTipText.getToolTip("Initial Membrane Potential"));
        jTextFieldSimulationInitVm.setToolTipText(toolTipText.getToolTip("Initial Membrane Potential"));
        jLabelSimulationVLeak.setToolTipText(toolTipText.getToolTip("Global Membrane Leakage Potential"));
        jTextFieldSimulationVLeak.setToolTipText(toolTipText.getToolTip("Global Membrane Leakage Potential"));
        jLabelSimulationGlobRa.setToolTipText(toolTipText.getToolTip("Global specific axial resistance"));
        jTextFieldSimulationGlobRa.setToolTipText(toolTipText.getToolTip("Global specific axial resistance"));
        jLabelSimulationGlobCm.setToolTipText(toolTipText.getToolTip("Global specific membrane capacitance"));
        jTextFieldSimulationGlobCm.setToolTipText(toolTipText.getToolTip("Global specific membrane capacitance"));
        jLabelSimulationGlobRm.setToolTipText(toolTipText.getToolTip("Global specific membrane resistance"));
        jTextFieldSimulationGlobRm.setToolTipText(toolTipText.getToolTip("Global specific membrane resistance"));
        jTextFieldSimulationTemp.setToolTipText(toolTipText.getToolTip("Simulation Temperature"));
        jTextFieldElectroLenMax.setToolTipText(toolTipText.getToolTip("Electrotonic length"));
        jTextFieldElectroLenMin.setToolTipText(toolTipText.getToolTip("Electrotonic length"));

        this.jCheckBoxGenesisComments.setToolTipText(toolTipText.getToolTip("Generate comments"));
        this.jCheckBoxNeuronComments.setToolTipText(toolTipText.getToolTip("Generate comments"));
        this.jCheckBoxNeuronShowShapePlot.setToolTipText(toolTipText.getToolTip("NEURON 3D"));

        jCheckBoxNeuronNumInt.setToolTipText(toolTipText.getToolTip("NeuronNumInt"));
        jCheckBoxNeuronGenAllMod.setToolTipText(toolTipText.getToolTip("NeuronGenAllMod"));
        jCheckBoxNeuronCopySimFiles.setToolTipText(toolTipText.getToolTip("NeuronCopySimFiles"));
        jCheckBoxNeuronForceCorrInit.setToolTipText(toolTipText.getToolTip("NeuronForceCorrInit"));
        jCheckBoxNeuronModSilent.setToolTipText(toolTipText.getToolTip("NeuronModSilent"));
        

        jCheckBoxGenesisCopySimFiles.setToolTipText(toolTipText.getToolTip("GenesisCopySimFiles"));
        jCheckBoxGenesisMooseMode.setToolTipText(toolTipText.getToolTip("GenesisMooseMode"));

        jLabelSimDefDur.setToolTipText(toolTipText.getToolTip("Simulation def duration"));
        this.jTextFieldSimDefDur.setToolTipText(toolTipText.getToolTip("Simulation def duration"));

        this.jLabelSimDT.setToolTipText(toolTipText.getToolTip("Simulation dt"));
        this.jTextFieldSimDT.setToolTipText(toolTipText.getToolTip("Simulation dt"));


        this.jButtonGenerateSave.setToolTipText(toolTipText.getToolTip("Save NetworkML"));
        this.jButtonGenerateLoad.setToolTipText(toolTipText.getToolTip("Load NetworkML"));
        
        this.jRadioButtonNMLSavePlainText.setToolTipText(toolTipText.getToolTip("Plaintext NetworkML"));
        this.jRadioButtonNMLSaveZipped.setToolTipText(toolTipText.getToolTip("Compress NetworkML"));
        this.jRadioButtonNMLSaveHDF5.setToolTipText(toolTipText.getToolTip("HDF5 NetworkML"));
        
        this.jCheckBoxGenerateExtraNetComments.setToolTipText(toolTipText.getToolTip("Extra comments NetworkML"));

        this.jLabelGenesisCompsDesc.setToolTipText(toolTipText.getToolTip("Compartmentalisation"));
        this.jComboBoxGenesisComps.setToolTipText(toolTipText.getToolTip("Compartmentalisation"));

    }

    @SuppressWarnings("serial")
    private void enableTableCellEditingFunctionality()
    {
        DefaultTableCellRenderer colorRenderer = new DefaultTableCellRenderer()
        {
            @Override
            public void setValue(Object value)
            {
                if (value instanceof Color)
                {
                    setBackground( (Color) value);
                }
                else
                {
                    super.setValue(value);
                }
            }
        };

        TableColumn colorColumn
            = jTableCellGroups.getColumn(jTableCellGroups.getColumnName(CellGroupsInfo.
            COL_NUM_COLOUR));

        colorColumn.setCellRenderer(colorRenderer);

        colorColumn.setCellEditor(new CellGroupColourEditor());

        DefaultTableCellRenderer regionColourRenderer = new DefaultTableCellRenderer()
        {
            @Override
            public void setValue(Object value)
            {
                if (value instanceof Color)
                {
                    setBackground( (Color) value);
                }
                else
                {
                    super.setValue(value);
                }
            }
        };

        TableColumn regionColorColumn
            = jTable3DRegions.getColumn(jTable3DRegions.getColumnName(RegionsInfo.
                                                                        COL_NUM_COLOUR));

        regionColorColumn.setCellRenderer(regionColourRenderer);
        regionColorColumn.setMaxWidth(80);


        regionColorColumn.setCellEditor(new RegionColourEditor());



        TableColumn adapterColumn
            = jTableCellGroups.getColumn(jTableCellGroups.getColumnName(CellGroupsInfo.
            COL_NUM_PACKING_ADAPTER));

      //  adapterColumn.setCellRenderer(adapterRenderer);

        adapterColumn.setCellEditor(new AdapterEditor(this));


        jTableCellGroups.getColumn(jTableCellGroups.getColumnName(CellGroupsInfo.COL_NUM_COLOUR)).setPreferredWidth(60);



        jTableCellGroups.getColumn(jTableCellGroups.getColumnName(CellGroupsInfo.COL_NUM_PACKING_ADAPTER)).setMinWidth(220);

        DropDownObjectCellEditor enabledEditor = new DropDownObjectCellEditor();
        enabledEditor.addValue(Boolean.TRUE);
        enabledEditor.addValue(Boolean.FALSE);



        ///jTableCellGroups.getColumn(jTableCellGroups.getColumnName(CellGroupsInfo.COL_NUM_ENABLED)).setCellEditor(enabledEditor);

        this.jTableMechanisms.getColumn(jTableMechanisms.getColumnName(CellMechanismInfo.COL_NUM_DESC)).setMinWidth(220);

    }

    public void tableDataModelUpdated(String tableModelName)
    {
        logger.logComment("Being told table :(" + tableModelName + ") is being updated...");
        if (initialisingProject)
        {
            logger.logComment("Ignoring because the project is being loaded...");
        }
        else
        {
            logger.logComment("Refreshing...");
            this.refreshGeneral();
            //if (base3DPanel!=null) refreshTab3D();
        }

    }

    public void cellMechanismUpdated()
    {
        try
        {
            projManager.getCurrentProject().cellMechanismInfo.reinitialiseCMLMechs(projManager.getCurrentProject());
        }
        catch (XMLMechanismException ex1)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Error reinitialising Cell Mechanisms: " +
                                      ex1.getMessage(),
                                      ex1,
                                      null);

        }

        projManager.getCurrentProject().markProjectAsEdited();
        this.refreshTabCellMechanisms();
    }

    public void tabUpdated(String tableModelName)
    {
        logger.logComment("Tab updated: " + tableModelName);

        if (projManager.projectLoaded() && !initialisingProject &&
            projManager.getCurrentProject().getProjectStatus() != Project.PROJECT_NOT_INITIALISED)
        {
            logger.logComment("Updating stuff in that table...");

            if (tableModelName.equals(PROJECT_INFO_TAB))
            {
                if (!updatingTabProjectInfo)
                    projManager.getCurrentProject().setProjectDescription(jTextAreaProjDescription.getText());
            }
            else if (tableModelName.equals(REGIONS_TAB))
            {
                /*
                if (jTextFieldDepth.getText().length() > 0)
                {
                    projManager.getCurrentProject().regionsInfo.setRegionDepth(Float.parseFloat(jTextFieldDepth.
                        getText()));
                }
                if (jTextFieldWidth.getText().length() > 0)
                {
                    projManager.getCurrentProject().regionsInfo.setRegionWidth(Float.parseFloat(jTextFieldWidth.
                        getText()));
                }
*/
                this.refreshTabRegionsInfo();
            }
            else if (tableModelName.equals(GENESIS_SIMULATOR_TAB))
            {
                //projManager.getCurrentProject().genesisSettings.setTextAfterCellCreation(jTextAreaGenesisAfter.getText());
                //projManager.getCurrentProject().genesisSettings.setTextBeforeCellCreation(jTextAreaGenesisBefore.getText());

                if (!jComboBoxGenesisExtraBlocks.getSelectedItem().equals(this.genesisBlockPrompt))
                {
                    ScriptLocation currNcl = (ScriptLocation)this.jComboBoxGenesisExtraBlocks.getSelectedItem();
                    projManager.getCurrentProject().genesisSettings.setNativeBlock(currNcl, jTextAreaGenesisBlock.getText());
                }


                if (jRadioButtonGenesisAllGUI.isSelected())
                {
                    projManager.getCurrentProject().genesisSettings.setGraphicsMode(GenesisSettings.GraphicsMode.ALL_SHOW);
                }
                else if (jRadioButtonGenesisNoPlots.isSelected())
                {
                    projManager.getCurrentProject().genesisSettings.setGraphicsMode(GenesisSettings.GraphicsMode.NO_PLOTS);
                }
                else if (jRadioButtonGenesisNoConsole.isSelected())
                {
                    projManager.getCurrentProject().genesisSettings.setGraphicsMode(GenesisSettings.GraphicsMode.NO_CONSOLE);
                }

                if (jCheckBoxGenesisReload.isSelected())
                {
                    logger.logComment("Updating jCheckBoxGenesisReload...");

                    jTextFieldGenesisReload.setEnabled(true);
                    //jTextFieldGenesisReload.setEditable(true);
                    try
                    {
                        float wait = Float.parseFloat(jTextFieldGenesisReload.getText());
                        if (wait<=0)
                        {
                            jTextFieldGenesisReload.setBackground(Color.red);
                            jTextFieldGenesisReload.repaint();
                            return;
                        }
                        projManager.getCurrentProject().genesisSettings.setReloadSimAfterSecs(wait);
                        jTextFieldGenesisReload.setBackground(Color.white);
                        jTextFieldGenesisReload.repaint();
                    }
                    catch (NumberFormatException ex)
                    {

                        jTextFieldGenesisReload.setBackground(Color.red);
                        jTextFieldGenesisReload.repaint();

                        return;
                    }
                }
                else
                {
                    logger.logComment("Updating setReloadSimAfterSecs2...");
                    projManager.getCurrentProject().genesisSettings.setReloadSimAfterSecs(-1);
                    jTextFieldGenesisReload.setEnabled(false);
                    //jTextFieldGenesisReload.setEditable(false);
                    jTextFieldGenesisReload.repaint();
                }




                if (!jCheckBoxGenesisAbsRefract.isSelected())
                {
                    logger.logComment("Updating jCheckBoxGenesisAbsRefract...");

                    jTextFieldGenesisAbsRefract.setEnabled(true);
                    try
                    {
                        float absRefract = Float.parseFloat(jTextFieldGenesisAbsRefract.getText());
                        if (absRefract<0)
                        {
                            jTextFieldGenesisAbsRefract.setBackground(Color.red);
                            jTextFieldGenesisAbsRefract.repaint();
                            return;
                        }
                        projManager.getCurrentProject().genesisSettings.setAbsRefractSpikegen(absRefract);
                        jTextFieldGenesisAbsRefract.setBackground(Color.white);
                        jTextFieldGenesisAbsRefract.repaint();
                    }
                    catch (NumberFormatException ex)
                    {
                        jTextFieldGenesisAbsRefract.setBackground(Color.red);
                        jTextFieldGenesisAbsRefract.repaint();
                        return;
                    }
                }
                else
                {
                    logger.logComment("Updating setAbsRefract...");
                    projManager.getCurrentProject().genesisSettings.setAbsRefractSpikegen(-1);
                    jTextFieldGenesisAbsRefract.setEnabled(false);
                    jTextFieldGenesisAbsRefract.repaint();
                }




                projManager.getCurrentProject().genesisSettings.setSymmetricCompartments(jCheckBoxGenesisSymmetric.isSelected());
                projManager.getCurrentProject().genesisSettings.setGenerateComments(jCheckBoxGenesisComments.isSelected());
                projManager.getCurrentProject().genesisSettings.setShowShapePlot(jCheckBoxGenesisShapePlot.isSelected());

                projManager.getCurrentProject().genesisSettings.setCopySimFiles(this.jCheckBoxGenesisCopySimFiles.isSelected());
                projManager.getCurrentProject().genesisSettings.setMooseCompatMode(this.jCheckBoxGenesisMooseMode.isSelected());

                if (jRadioButtonGenesisPhy.isSelected())
                    projManager.getCurrentProject().genesisSettings.setUnitSystemToUse(UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS);
                if (jRadioButtonGenesisSI.isSelected())
                    projManager.getCurrentProject().genesisSettings.setUnitSystemToUse(UnitConverter.GENESIS_SI_UNITS);

                projManager.getCurrentProject().markProjectAsEdited();

                this.refreshTabGenesis();

            }
            else if (tableModelName.equals(PSICS_SIMULATOR_TAB))
            {
                projManager.getCurrentProject().psicsSettings.setShowHtmlSummary(jCheckBoxPsicsShowHtml.isSelected());
                projManager.getCurrentProject().psicsSettings.setShowPlotSummary(jCheckBoxPsicsShowPlot.isSelected());
                projManager.getCurrentProject().psicsSettings.setShowConsole(jCheckBoxPsicsConsole.isSelected());

                try
                {
                    projManager.getCurrentProject().psicsSettings.setSpatialDiscretisation(Float.parseFloat(jTextFieldPsicsSpatDisc.getText()));
                    jTextFieldPsicsSpatDisc.setBackground(Color.white);

                }
                catch (NumberFormatException ex)
                {
                    jTextFieldPsicsSpatDisc.setBackground(Color.red);
                    return;
                }
                try
                {
                    projManager.getCurrentProject().psicsSettings.setSingleChannelCond(Float.parseFloat(jTextFieldPsicsSingleCond.getText()));
                    jTextFieldPsicsSingleCond.setBackground(Color.white);
                }
                catch (NumberFormatException ex)
                {

                    jTextFieldPsicsSingleCond.setBackground(Color.red);

                    return;
                }

                projManager.getCurrentProject().markProjectAsEdited();
            }
            else if (tableModelName.equals(NEURON_SIMULATOR_TAB))
            {

                projManager.getCurrentProject().neuronSettings.setShowShapePlot(jCheckBoxNeuronShowShapePlot.isSelected());

               // projManager.getCurrentProject().neuronSettings.setGraphicsMode(!this.jCheckBoxNeuronNoGraphicsMode.isSelected());
                if (jRadioButtonNeuronAllGUI.isSelected())
                     projManager.getCurrentProject().neuronSettings.setGraphicsMode(NeuronSettings.GraphicsMode.ALL_SHOW);
                if (jRadioButtonNeuronNoPlots.isSelected())
                     projManager.getCurrentProject().neuronSettings.setGraphicsMode(NeuronSettings.GraphicsMode.NO_PLOTS);
                if (jRadioButtonNeuronNoConsole.isSelected())
                     projManager.getCurrentProject().neuronSettings.setGraphicsMode(NeuronSettings.GraphicsMode.NO_CONSOLE);

                projManager.getCurrentProject().neuronSettings.setGenerateComments(jCheckBoxNeuronComments.isSelected());

                //System.out.println("ID: "+projManager.getCurrentProject().stimulationSettings.getSegmentID());
                projManager.getCurrentProject().simulationParameters.setReference(jTextFieldSimRef.getText());

                projManager.getCurrentProject().simulationParameters.setSpecifySimName(jCheckBoxSpecifySimRef.isSelected());
                projManager.getCurrentProject().simulationParameters.setSaveCopyGenSimFiles(jCheckBoxNeuronSaveHoc.isSelected());


                ////projManager.getCurrentProject().neuronSettings.setTextAfterCellCreation(jTextAreaNeuronAfter.getText());
                //projManager.getCurrentProject().neuronSettings.setTextBeforeCellCreation(jTextAreaNeuronBlock.getText());

                if (!jComboBoxNeuronExtraBlocks.getSelectedItem().equals(this.neuronBlockPrompt))
                {
                    NativeCodeLocation currNcl = (NativeCodeLocation)this.jComboBoxNeuronExtraBlocks.getSelectedItem();
                    projManager.getCurrentProject().neuronSettings.setNativeBlock(currNcl, jTextAreaNeuronBlock.getText());
                }

                projManager.getCurrentProject().neuronSettings.setVarTimeStep(this.jCheckBoxNeuronNumInt.isSelected());


                projManager.getCurrentProject().neuronSettings.setGenAllModFiles(this.jCheckBoxNeuronGenAllMod.isSelected());


                projManager.getCurrentProject().neuronSettings.setCopySimFiles(this.jCheckBoxNeuronCopySimFiles.isSelected());

                projManager.getCurrentProject().neuronSettings.setForceCorrectInit(this.jCheckBoxNeuronForceCorrInit.isSelected());
                projManager.getCurrentProject().neuronSettings.setModSilentMode(this.jCheckBoxNeuronModSilent.isSelected());

                if (this.jRadioButtonNeuronFormatText.isSelected())
                {
                    projManager.getCurrentProject().neuronSettings.setDataSaveFormat(NeuronSettings.DataSaveFormat.TEXT_NC);
                }
                else if (this.jRadioButtonNeuronFormatHDF5.isSelected())
                {
                    projManager.getCurrentProject().neuronSettings.setDataSaveFormat(NeuronSettings.DataSaveFormat.HDF5_NC);
                }


                try
                {
                    String dur = jTextFieldSimDefDur.getText();
                    try
                    {
                        String dt = jTextFieldSimDT.getText();


                        if(dt.length()>0)
                            projManager.getCurrentProject().simulationParameters.setDt(Float.parseFloat(dt));
                        else
                            projManager.getCurrentProject().simulationParameters.setDt(0f);

                        jTextFieldSimDT.setBackground(Color.white);
                    }
                    catch (NumberFormatException ex)
                    {
                        jTextFieldSimDT.setBackground(Color.red);
                        return;
                    }

                    String globCm = jTextFieldSimulationGlobCm.getText();
                    String globRa = jTextFieldSimulationGlobRa.getText();
                    String globRm = jTextFieldSimulationGlobRm.getText();
                    String initVm = jTextFieldSimulationInitVm.getText();
                    String globVLeak = jTextFieldSimulationVLeak.getText();
                    String temp = jTextFieldSimulationTemp.getText();
                    String max = jTextFieldElectroLenMax.getText();
                    String min = jTextFieldElectroLenMin.getText();

                    if (globCm.length() > 0 && !globCm.equals("-"))
                        projManager.getCurrentProject().simulationParameters.setGlobalCm(Float.parseFloat(globCm));
                    else
                        projManager.getCurrentProject().simulationParameters.setGlobalCm(0);

                    if (globRa.length() > 0 && !globRa.equals("-"))
                        projManager.getCurrentProject().simulationParameters.setGlobalRa(Float.parseFloat(globRa));
                    else
                        projManager.getCurrentProject().simulationParameters.setGlobalRa(0);

                    if (globRm.length() > 0 && !globRm.equals("-"))
                        projManager.getCurrentProject().simulationParameters.setGlobalRm(Float.parseFloat(globRm));
                    else
                        projManager.getCurrentProject().simulationParameters.setGlobalRm(0);

                    if (initVm.length() > 0 && !initVm.equals("-"))
                        projManager.getCurrentProject().simulationParameters.setInitVm(Float.parseFloat(initVm));
                    else
                        projManager.getCurrentProject().simulationParameters.setInitVm(0);

                    if (globVLeak.length() > 0 && !globVLeak.equals("-"))
                        projManager.getCurrentProject().simulationParameters.setGlobalVLeak(Float.parseFloat(globVLeak));
                    else
                        projManager.getCurrentProject().simulationParameters.setGlobalVLeak(0);


                    if (temp.length() > 0 && !temp.equals("-"))
                        projManager.getCurrentProject().simulationParameters.setTemperature(Float.parseFloat(temp));
                    else
                        projManager.getCurrentProject().simulationParameters.setTemperature(0);


                    
                    if (max.length() > 0 && !max.equals("-"))
                        projManager.getCurrentProject().simulationParameters.setMaxElectroLen(Float.parseFloat(max));
                    else
                        projManager.getCurrentProject().simulationParameters.setMaxElectroLen(SimulationParameters.PREF_MAX_ELECT_LEN);



                    
                    if (min.length() > 0 && !min.equals("-"))
                        projManager.getCurrentProject().simulationParameters.setMinElectroLen(Float.parseFloat(min));
                    else
                        projManager.getCurrentProject().simulationParameters.setMinElectroLen(SimulationParameters.PREF_MIN_ELECT_LEN);

                    
                    

                    if (dur.length()>0)
                        projManager.getCurrentProject().simulationParameters.setDuration(Float.parseFloat(dur));
                    else
                        projManager.getCurrentProject().simulationParameters.setDuration(0);


                }
                catch (NumberFormatException ex)
                {
                    GuiUtils.showErrorMessage(logger,
                                              "Problem in reading the Simulation parameters: " +
                                              ex.getMessage(), ex, this);
                    return;
                }
/*
                if (jRadioButtonNeuronSimSaveToFile.isSelected())
                {
                    projManager.getCurrentProject().simulationParameters.setRecordingMode(SimulationParameters.
                        SIMULATION_RECORD_TO_FILE);
                    logger.logComment("recording to file...");
                }
                else if (jRadioButtonNeuronSimDontRecord.isSelected())
                {
                    projManager.getCurrentProject().simulationParameters.setRecordingMode(SimulationParameters.
                        SIMULATION_NOT_RECORDED);
                    logger.logComment("NOT recording to file...");
                }
                else
                {
                    logger.logError("No radio button set!!");
                }*/

                if (jRadioButtonSimSomaOnly.isSelected())
                {
                    projManager.getCurrentProject().simulationParameters.setWhatToRecord(SimulationParameters.RECORD_ONLY_SOMA);
                }/*
                else if (jRadioButtonSimAllSections.isSelected())
                {
                    projManager.getCurrentProject().simulationParameters.whatToRecord =
                        SimulationParameters.RECORD_EVERY_SECTION;

                }*/
                else if (jRadioButtonSimAllSegments.isSelected())
                {
                    projManager.getCurrentProject().simulationParameters.setWhatToRecord(
                                        SimulationParameters.RECORD_EVERY_SEGMENT);
                }


                this.createSimulationSummary();
                projManager.getCurrentProject().markProjectAsEdited();
                refreshTabNeuron();
            }
        }
        else
        {
            logger.logComment("Not updating stuff in that table, as the project is initialising...");
        }

        this.refreshGeneral();
    }


    /*

    if (this.getProjectMainDirectory().getAbsolutePath().equals(ProjectStructure.getExamplesDirectory().getAbsolutePath()))
    {
        int yesNo = GuiUtils.showYesNoMessage(logger, "Note: the project:\n"+this.getProjectFile()
                                  +"\nis one of the included example project in neuroConstruct. These are referenced in the documentation and paper.\n"
                                  +"Are you sure you want to save it? Select No to save under another name.", null);

        if (yesNo!=JOptionPane.YES_OPTION)
        {

        }


    }*/



    /**
     * Carries out all the actions needed when a button or menu item is selected
     * for creating a new project
     *
     */
    private void doNewProject(boolean offerSamples)
    {
        doNewProject(offerSamples, null, null);
    }

    public void doNewProject(boolean offerSamples, File projParDir, String projName)
    {
        boolean continueClosing = checkToSave();
        if (!continueClosing) return;
        closeProject();

        logger.logComment(">>>>>>>>>>>>>>>    Creating new project...");

        if (projParDir==null || projName==null)
        {

            NewProjectDialog dlg = new NewProjectDialog(this,
                "New neuroConstruct project", false);
            Dimension dlgSize = dlg.getPreferredSize();
            Dimension frmSize = getSize();
            Point loc = getLocation();
            dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                            (frmSize.height - dlgSize.height) / 2 + loc.y);
            dlg.setModal(true);
            dlg.pack();
            dlg.setVisible(true);


            if (dlg.cancelled)
            {
                logger.logComment("Cancel pressed...");
                return;
            }

            projName = dlg.getProjFileName();
            String projDirName = dlg.getProjDirName();

            if (projName.length() == 0 || projDirName.length() == 0)
            {
                GuiUtils.showErrorMessage(logger, "No project name/directory entered", null, this);
                return;
            }

            projParDir = new File(projDirName);
        }

        File projDir = new File(projParDir, projName);

        logger.logComment(":::  projParentDir: "+ projParDir.getAbsolutePath()+", projName: "+ projName+", projDir: "+ projDir);



        if (projDir.listFiles()!=null && projDir.listFiles().length >0)
        {
            logger.logComment("The file " + projDir.getAbsolutePath() + " is not empty!");
            Object[] options =
                {"OK", "Cancel"};

            JOptionPane option = new JOptionPane(
                "This project directory: " + projDir.getAbsolutePath() + " already exists and is not empty. Overwrite?\n" +
                "NOTE: This will remove all files in the directory!",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]);

            JDialog dialog = option.createDialog(this, "Warning");
            dialog.setVisible(true);

            Object choice = option.getValue();
            logger.logComment("User has chosen: " + choice);
            if (choice.equals("Cancel"))
            {
                logger.logComment("User has changed their mind...");
                return;
            }
            cleanseDirectory(projDir);

        }
        else
        {
            logger.logComment("The file " + projDir.getAbsolutePath() + " doesn't already exist");
        }
        this.initialisingProject = true;

        Project proj = Project.createNewProject(projDir.getAbsolutePath(), projName, this);

        projManager.setCurrentProject(proj);


        recentFiles.addToList(proj.getProjectFile().getAbsolutePath());

        try
        {
            projManager.getCurrentProject().saveProject();
            logger.logComment("Project saved");
        }
        catch (Exception ex)
        {
            logger.logError("Error when saving project", ex);
        }

        if (offerSamples)
        {
            int yesNo = JOptionPane.showConfirmDialog(this, "Would you like to add some example Cell Types/Regions/Cell Mechanisms to the project?"
                                                      + "\nThese can all be removed later.", "Add sample items", JOptionPane.YES_NO_OPTION);

            if (yesNo==JOptionPane.YES_OPTION)
            {

                addSampleItems();
            }
        }

        this.refreshAll();
        enableTableCellEditingFunctionality();

        this.initialisingProject = false;
        createSimulationSummary();
        projManager.getCurrentProject().markProjectAsEdited();

        logger.logComment(">>>>>>>>>>>>>>>    Finished creating new project");

        jTabbedPaneMain.setSelectedIndex(0); // main tab...

        this.doSave();

    }


    private void addSampleItems()
    {
        String newCellName = "SampleCell";
        String newRegionName = "SampleRegion";
        String newCellGroupName = "SampleCellGroup";
        String newStim = "SampleIClamp";
        String newPlot = "SamplePlot";

        SimpleCell simpleCell = new SimpleCell(newCellName);

        Cell cell = (Cell)simpleCell.clone();

        try
        {
            File cmlTemplateDir = ProjectStructure.getCMLTemplatesDir();

            File pasTemplate = new File(cmlTemplateDir, "LeakConductance");

            ChannelMLCellMechanism pas
                = ChannelMLCellMechanism.createFromTemplate(pasTemplate, this.projManager.getCurrentProject());

            File naTemplate = new File(cmlTemplateDir, "NaConductance");

            ChannelMLCellMechanism na
                = ChannelMLCellMechanism.createFromTemplate(naTemplate, this.projManager.getCurrentProject());

            File kTemplate = new File(cmlTemplateDir, "KConductance");

            ChannelMLCellMechanism k
                = ChannelMLCellMechanism.createFromTemplate(kTemplate, this.projManager.getCurrentProject());

            File desTemplate = new File(cmlTemplateDir, "DoubleExpSyn");

            ChannelMLCellMechanism des
                = ChannelMLCellMechanism.createFromTemplate(desTemplate, this.projManager.getCurrentProject());



            pas.initialise(projManager.getCurrentProject(), true);

            String condDensPas = pas.getValue("//@"+ChannelMLConstants.DEFAULT_COND_DENSITY_ATTR);

            logger.logComment("condDensPas: "+condDensPas);

            String unitsUsed = pas.getValue(ChannelMLConstants.getUnitsXPath());

            double condDensDouble = Double.parseDouble(condDensPas);

            if (unitsUsed!=null)
            {
                if (unitsUsed.equals(ChannelMLConstants.SI_UNITS))
                {
                    condDensDouble = UnitConverter.getConductanceDensity(condDensDouble,
                                                                       UnitConverter.GENESIS_SI_UNITS,
                                                                       UnitConverter.NEUROCONSTRUCT_UNITS);
                }
                else if (unitsUsed.equals(ChannelMLConstants.PHYSIOLOGICAL_UNITS))
                {
                    condDensDouble = UnitConverter.getConductanceDensity(condDensDouble,
                                                                       UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS,
                                                                       UnitConverter.NEUROCONSTRUCT_UNITS);
                }
            }


            ChannelMechanism pasChan = new ChannelMechanism(pas.getInstanceName(),
                                                           (float)condDensDouble);


            cell.associateGroupWithChanMech(Section.ALL, pasChan);


            projManager.getCurrentProject().cellMechanismInfo.addCellMechanism(pas);



            na.initialise(projManager.getCurrentProject(), true);

            String condDensNa = na.getValue("//@"+ChannelMLConstants.DEFAULT_COND_DENSITY_ATTR);

            condDensDouble = Double.parseDouble(condDensNa);

            if (unitsUsed!=null)
            {
                if (unitsUsed.equals(ChannelMLConstants.SI_UNITS))
                {
                    condDensDouble = UnitConverter.getConductanceDensity(condDensDouble,
                                                                       UnitConverter.GENESIS_SI_UNITS,
                                                                       UnitConverter.NEUROCONSTRUCT_UNITS);
                }
                else if (unitsUsed.equals(ChannelMLConstants.PHYSIOLOGICAL_UNITS))
                {
                    condDensDouble = UnitConverter.getConductanceDensity(condDensDouble,
                                                                       UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS,
                                                                       UnitConverter.NEUROCONSTRUCT_UNITS);
                }
            }


            ChannelMechanism naChan = new ChannelMechanism(na.getInstanceName(),
                                                           (float)condDensDouble);

            cell.associateGroupWithChanMech(Section.ALL, naChan);


            projManager.getCurrentProject().cellMechanismInfo.addCellMechanism(na);


            k.initialise(projManager.getCurrentProject(), true);

            String condDensK = k.getValue("//@"+ChannelMLConstants.DEFAULT_COND_DENSITY_ATTR);

            condDensDouble = Double.parseDouble(condDensK);

            if (unitsUsed!=null)
            {
                if (unitsUsed.equals(ChannelMLConstants.SI_UNITS))
                {
                    condDensDouble = UnitConverter.getConductanceDensity(condDensDouble,
                                                                       UnitConverter.GENESIS_SI_UNITS,
                                                                       UnitConverter.NEUROCONSTRUCT_UNITS);
                }
                else if (unitsUsed.equals(ChannelMLConstants.PHYSIOLOGICAL_UNITS))
                {
                    condDensDouble = UnitConverter.getConductanceDensity(condDensDouble,
                                                                       UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS,
                                                                       UnitConverter.NEUROCONSTRUCT_UNITS);
                }
            }


            ChannelMechanism kChan = new ChannelMechanism(k.getInstanceName(),
                                                           (float)condDensDouble);

            projManager.getCurrentProject().cellMechanismInfo.addCellMechanism(k);

            cell.associateGroupWithChanMech(Section.ALL, kChan);


            cell.associateGroupWithSpecAxRes(Section.ALL, projManager.getCurrentProject().simulationParameters.getGlobalRa());

            cell.associateGroupWithSpecCap(Section.ALL, projManager.getCurrentProject().simulationParameters.getGlobalCm());

            des.initialise(projManager.getCurrentProject(), true);


            cell.associateGroupWithSynapse(Section.ALL, des.getInstanceName());


            projManager.getCurrentProject().cellMechanismInfo.addCellMechanism(des);

            projManager.getCurrentProject().cellManager.addCellType(cell);


            RectangularBox box = new RectangularBox(0,0,0, 100, 50 ,100);

            projManager.getCurrentProject().regionsInfo.addRow(newRegionName, box, Color.white);

            RandomCellPackingAdapter randAdapter = new RandomCellPackingAdapter();

            int numCellGroups = projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups();

            projManager.getCurrentProject().cellGroupsInfo.addRow(newCellGroupName,
                                                            newCellName,
                                                            newRegionName,
                                                            Color.red,
                                                            randAdapter,
                                                            10 - numCellGroups);

            projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().getCellGroups().add(newCellGroupName);


            IClampSettings stim = new IClampSettings(newStim,
                                                     newCellGroupName,
                                                     new AllCells(),
                                                     0,
                                                     20,
                                                     60,
                                                     0.2f,
                                                     false);

            projManager.getCurrentProject().elecInputInfo.addStim(stim);

            projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().getInputs().add(newStim);


            SimPlot plot = new SimPlot(newPlot,
                                       "SampleGraph",
                                       newCellGroupName,
                                       "*",
                                       "0",
                                       SimPlot.VOLTAGE,
                                       -90,
                                       50,
                                       SimPlot.PLOT_AND_SAVE);

            projManager.getCurrentProject().simPlotInfo.addSimPlot(plot);


            projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().getPlots().add(newPlot);

            NumericalMethod numMethod = new NumericalMethod();
            numMethod.setMethodNumber(11);
            numMethod.setChanMode(0);
            numMethod.setHsolve(true);


            projManager.getCurrentProject().genesisSettings.setNumMethod(numMethod);


            projManager.getCurrentProject().simulationParameters.setDt(0.02f);

            projManager.getCurrentProject().setProjectDescription(
                             "This is a simple project with a single cell placed randomly in a 3D rectangular box.\n\n"
                             +"Go to tab Generate, press Generate Cell Positions and Connections, and then to visualise"
                             +" the results, go to tab Visualisation and press View, with Latest Generated Positions selected in the drop down box.\n\n"
                                     +"If NEURON or GENESIS are installed, the cell can be simulated via tab Export.");

        }
        catch (ChannelMLException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem adding sample items to project: "+ex.getMessage(), ex, this);
            return;
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Error adding sample items to project", ex, this);
            return;
        }

  



    }

    private void cleanseDirectory(File dir)
    {
        if (!dir.isDirectory()) return;
        File[] contents = dir.listFiles();

        logger.logComment("ABOUT TO REMOVE ALL FILES IN: "+ dir.getAbsolutePath(), true);

        for (int i = 0; i < contents.length; i++)
        {
            logger.logComment("Removing: "+ contents[i]);
            if (contents[i].isDirectory())
            {
                cleanseDirectory(contents[i]);

            }
            contents[i].delete();
        }
    }

    /**
     * Carries out all the actions needed when a button or menu item is selected
     * for creating a new 3D region
     *
     */

    private void doNewRegion()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        logger.logComment("Creating a new Region...");

        String regionName = null;
        Region newRegion = null;


        int currentNumberRegions = projManager.getCurrentProject().regionsInfo.getRowCount();

        RectangularBox rect = projManager.getCurrentProject().regionsInfo.getRegionEnclosingAllRegions(null, null);

        if (rect.getLowestXValue() == rect.getHighestXValue())
        {
            rect.setParameter(RectangularBox.WIDTH_PARAM, GeneralProperties.getDefaultRegionWidth());
        }
        if (rect.getLowestZValue() == rect.getHighestZValue())
        {
            rect.setParameter(RectangularBox.DEPTH_PARAM, GeneralProperties.getDefaultRegionDepth());
        }




        Region suggestedRegion
            = new RectangularBox(rect.getLowestXValue(),
                                 rect.getHighestYValue(),
                                 rect.getLowestZValue(),
                                 rect.getHighestXValue(),
                                 GeneralProperties.getDefaultRegionHeight(),
                                 rect.getHighestZValue());

        //if (suggestedRegion.getl)

        String suggestedName = "Regions_" + (currentNumberRegions + 1);

        RegionsInfoDialog dlg = new RegionsInfoDialog(this,
                                                      suggestedRegion,
                                                      suggestedName);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);
        regionName = dlg.getRegionName();
        newRegion = dlg.getFinalRegion();

        if (dlg.cancelled)
        {
            logger.logComment("The action was cancelled...");
            return;
        }

        try
        {
            this.projManager.getCurrentProject().regionsInfo.addRow(regionName, newRegion, Color.white);
        }
        catch (NamingException ex1)
        {
            GuiUtils.showErrorMessage(logger, ex1.getMessage(), ex1, this);
            return;
        }

        jButtonRegionRemove.setEnabled(true);

        this.refreshAll();
    }



    /**
     * Edits selected region
     *
     */

    private void doEditRegion()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        int selectedRow = jTable3DRegions.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }
        String regionName =
            (String)projManager.getCurrentProject().regionsInfo.getValueAt(selectedRow, RegionsInfo.COL_NUM_REGIONNAME);

        Region region = projManager.getCurrentProject().regionsInfo.getRegionObject(regionName);
        Color colour = projManager.getCurrentProject().regionsInfo.getRegionColour(regionName);


        RegionsInfoDialog dlg = new RegionsInfoDialog(this,
                                                      region,
                                                      regionName);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);

        regionName = dlg.getRegionName();
        Region newRegion = dlg.getFinalRegion();

        if (dlg.cancelled)
        {
            logger.logComment("The action was cancelled...");
            return;
        }

        try
        {
            this.projManager.getCurrentProject().regionsInfo.updateRow(regionName, newRegion, colour);
        }
        catch (NamingException ex1)
        {
            GuiUtils.showErrorMessage(logger, ex1.getMessage(), ex1, this);
            return;
        }

        jButtonRegionRemove.setEnabled(true);

        this.refreshAll();
    }


    /**
     * Carries out all the actions needed when a button or menu item is selected
     * for saving the current project
     *
     */
    private void doSave()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded!!");
            return;
        }

        String projectName = this.projManager.getCurrentProject().getProjectName();

        if (projManager.getCurrentProject().getProjectMainDirectory().getParentFile().getAbsolutePath().equals(
                        ProjectStructure.getnCExamplesDir().getAbsolutePath()))
        {
            boolean goOngoOngoOn = GuiUtils.showYesNoMessage(logger, "Note: the project: "+projManager.getCurrentProject().getProjectFile()
                                      +"\nis one of the included example projects in neuroConstruct. These are referenced in the documentation and paper.\n"
                                      +"Are you sure you want to save over it?\n\nSelect No to save the project in a different location.", null);

            if (!goOngoOngoOn)
            {
                doSaveAs();
                return;
            }

        }

        logger.logComment("Going to save project: " + projectName);

        try
        {
            projManager.getCurrentProject().saveProject();
            logger.logComment("Project saved");

        }
        catch (Exception ex)
        {
            logger.logError("Error when saving project", ex);
        }
        this.refreshAll();
    }


    public void doReloadLastProject()
    {
        if (recentFiles.getFileNames().length>0)
        {
            doLoadProject(recentFiles.getFileNames()[0]);
        }
    }

    public void doLoadProject(String projectName)
    {
        logger.logComment(">>>>>>>>>>>   Loading a project at startup...");

        this.jTextAreaProjDescription.setText("\n   Loading project: "+projectName+"...");

        File projFile = new File(projectName);

        if (!projFile.exists())
        {
            GuiUtils.showErrorMessage(logger,
                                      "Cannot find startup file: " + projFile.getAbsolutePath(), null, this);
            return;
        }


        initialisingProject = true;
        try
        {
            projManager.setCurrentProject(Project.loadProject(projFile, this));
        }
        catch (ProjectFileParsingException ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
            closeProject();
        }

        recentFiles.addToList(projFile.getAbsolutePath());
        refreshAll();
        enableTableCellEditingFunctionality();

        logger.logComment("<<<<<<<<<<   --------------   *Finished loading the project*   --------------");
        initialisingProject = false;
        createSimulationSummary();
    }

    /**
     * Carries out all the actions needed when a button or menu item is selected
     * for loading a new project
     *
     */
    private void loadProject()
    {
        File defaultDir = null;
        
        if (projManager.getCurrentProject()!=null)
        {
            defaultDir = projManager.getCurrentProject().getProjectMainDirectory();
        }
        else
        {
            defaultDir = GeneralProperties.getnCProjectsDir();
        }

        boolean continueClosing = checkToSave();
        if (!continueClosing) return;
        closeProject();


        Frame frame = (Frame)this;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);

        try
        {
            chooser.setCurrentDirectory(defaultDir);
            logger.logComment("Set Dialog dir to: " + defaultDir);
        }
        catch (Exception ex)
        {
            logger.logError("Problem with default dir setting: " + defaultDir, ex);
        }
        SimpleFileFilter fileFilter = ProjectStructure.getProjectFileFilter();

        chooser.setFileFilter(fileFilter);

        int retval = chooser.showDialog(frame, null);

        if (retval == JFileChooser.APPROVE_OPTION)
        {
            logger.logComment("User approved...");
            initialisingProject = true;
            try
            {
                logger.logComment(">>>>>>>>  Loading project: "+ chooser.getSelectedFile());
                //projManager.getCurrentProject() = Project.loadProject(chooser.getSelectedFile(), this);
                projManager.loadProject(chooser.getSelectedFile());
                logger.logComment("<<<<<<<<  Loaded project: "+ projManager.getCurrentProject().getProjectFileName());
            }
            catch (ProjectFileParsingException ex2)
            {
                GuiUtils.showErrorMessage(logger, ex2.getMessage(), ex2, this);
                initialisingProject = false;
                closeProject();
                return;
            }

            recentFiles.addToList(projManager.getCurrentProject().getProjectFile().getAbsolutePath());
            refreshAll();

            enableTableCellEditingFunctionality();


            initialisingProject = false;
            createSimulationSummary();

            jTabbedPaneMain.setSelectedIndex(0); // main tab...
        }
    }



    /*
     * Carries out all the actions needed when an active project is closed
     */
    private void closeProject()
    {

        logger.logComment("Closing down the project...");
        if (projManager.getCurrentProject() == null)
        {
            logger.logComment("No project loaded to close...");
            return;
        }

        sourceOfCellPosnsInMemory = NO_POSITIONS_LOADED;
        jPanel3DMain.removeAll();

        this.doDestroy3D();


        this.projManager.doCloseProject();

        jTabbedPaneMain.setSelectedIndex(0); // main tab...

        refreshAll();


    }


    /**
     * pops up the create new cell type dialog
     *
     */

    private void doNewCellType()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        int currentNumberCellTypes = projManager.getCurrentProject().cellManager.getNumberCellTypes();

        //String morphologyDir = recentFiles.getMyLastMorphologiesDir();

        //if (morphologyDir==null) morphologyDir =(new File(".")).getAbsolutePath(); // pwd...

        NewCellTypeDialog dlg
            = new NewCellTypeDialog(this,
                                    "New Cell Type",
                                    "CellType_" + (currentNumberCellTypes + 1),
                                    projManager.getCurrentProject());

        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);

        if (dlg.createCancelled)
        {
            logger.logComment("They've changed their mind...");
            return;
        }

        Cell newCell = dlg.getChosenCell();


     try
     {
        if (!dlg.wasBasedOnMorphML())
        {
            NumberGenerator initPot = new NumberGenerator(0);
            initPot.initialiseAsFixedFloatGenerator(projManager.getCurrentProject().simulationParameters.getInitVm());
            newCell.setInitialPotential(initPot);


            newCell.associateGroupWithSpecCap(Section.ALL, projManager.getCurrentProject().simulationParameters.getGlobalCm());

            newCell.associateGroupWithSpecAxRes(Section.ALL, projManager.getCurrentProject().simulationParameters.getGlobalRa());


        }

         projManager.getCurrentProject().cellManager.addCellType(newCell);

     }
     catch (NamingException ex2)
     {
         GuiUtils.showErrorMessage(logger, "Problem with the name of that Cell Type", ex2, this);
     }

     projManager.getCurrentProject().markProjectAsEdited();
     this.refreshAll();


         jComboBoxCellTypes.setSelectedItem(newCell);


    }







    private void doNewAAConn()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        int num = projManager.getCurrentProject().volBasedConnsInfo.getNumConns();
        num++;
        String newName = "AAConn_" + num;

        Vector synapticTypes =  projManager.getCurrentProject().cellMechanismInfo.getAllChemElecSynMechNames();

        if (synapticTypes.isEmpty())
        {
            GuiUtils.showErrorMessage(logger, "There are no synaptic cell mechanisms in the project.\n"
                                      + "Please add some before creating network connections.", null, this);

            return;
        }



        VolBasedConnDialog dlg = new VolBasedConnDialog(this,
            projManager.getCurrentProject(),
            newName);



        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);


        dlg.pack();
        dlg.setVisible(true);


        if (dlg.cancelled)
        {
            logger.logComment("They've changed their mind...");
            return;
        }

        try
        {
            logger.logComment("Trying to add that new net connection...");

            projManager.getCurrentProject().volBasedConnsInfo.addRow(dlg.getAAConnName(),
                                                                         dlg.getSourceCellGroup(),
                                                                         dlg.getTargetCellGroup(),
                                                                         dlg.getSynapticProperties(),
                                                                         dlg.getSourceRegions(),
                                                                         dlg.getConnectivityConditions(),
                                                                         dlg.getAPSpeed(),
                                                                         dlg.getInhomogenousExp().getNiceString());

            refreshTabNetSettings();


           if (this.projManager.getCurrentProject().simConfigInfo.getNumSimConfigs()==1)
           {
               projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().addNetConn(dlg.getAAConnName());
               logger.logComment("Now netConnss in default SimConfig: "+ projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().getNetConns());
           }
           else
           {
               GuiUtils.showInfoMessage(logger, "Added Network Connection", "There is more than one Simulation Configurations. To include this Network Connection in one of them, go to tab Generate.", this);
           }

        }
        catch (NamingException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem adding the Network Connection", ex, this);
            return;
        }
    }

    /**
     * Creates a new connection between 2 cell regions
     *
     */
    private void doNewNetConnection()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        int num = projManager.getCurrentProject().morphNetworkConnectionsInfo.getNumSimpleNetConns();
        num++;
        String newName = "NetConn_" + num;

        Vector synapticTypes =  projManager.getCurrentProject().cellMechanismInfo.getAllChemElecSynMechNames();

        if (synapticTypes.isEmpty())
        {
            GuiUtils.showErrorMessage(logger, "There are no synaptic cell mechanisms in the project.\n"
                                      + "Please add some before creating network connections.", null, this);

            return;
        }


        NetworkConnectionDialog dlg = new NetworkConnectionDialog(this,
            projManager.getCurrentProject(),
            newName);



        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);


        dlg.pack();
        dlg.setVisible(true);


        if (dlg.cancelled)
        {
            logger.logComment("They've changed their mind...");
            return;
        }

        try
        {
            logger.logComment("Trying to add that new net connection...");

            projManager.getCurrentProject().morphNetworkConnectionsInfo.addRow(dlg.getNetworkConnName(),
                dlg.getSourceCellGroup(),
                dlg.getTargetCellGroup(),
                dlg.getSynapticPropsList(),
                dlg.getSearchPattern(),
                dlg.getMaxMinLength(),
                dlg.getConnectivityConditions(),
                dlg.getAPSpeed());

           refreshTabNetSettings();


           if (this.projManager.getCurrentProject().simConfigInfo.getNumSimConfigs()==1)
           {
               projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().addNetConn(dlg.getNetworkConnName());
               logger.logComment("Now netConnss in default SimConfig: "+ projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().getNetConns());
           }
           else
           {
               GuiUtils.showInfoMessage(logger, "Added Network Connection", "There is more than one Simulation Configurations. To include this Network Connection in one of them, go to tab Generate.", this);
           }

        }
        catch (NamingException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem adding the Network Connection", ex, this);
            return;
        }

    }


    /**
     * Creates the 3D representation of a single cell
     *
     */
    void doCreate3DCell(Cell cell)
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        logger.logComment("Constructing model of one cell: " + cell.getInstanceName());

        logger.logComment("Internal info of cell:");
        logger.logComment(CellTopologyHelper.printDetails(cell, null));

        jPanel3DMain.removeAll();

        jPanel3DMain.setLayout(new BorderLayout());
        Panel panel = new Panel();
        panel.setLayout(new BorderLayout());

        base3DPanel = new OneCell3DPanel(cell, projManager.getCurrentProject(), this);
        //base3DPanel.setViewedObject();

        /////base3DPanel.setDisplayStatus(Base3DPanel.ONE_CELL_DISPLAYED);

        panel.add("Center", base3DPanel);
        jPanel3DMain.add("Center", panel);

        this.validate();

        this.jButton3DDestroy.setEnabled(true);

    }

    /*
     * Creates the 3D representation of the regions
     *
     */
    void doCreate3D(Object selectedObjectToView)
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        if (selectedObjectToView == null)
        {
            logger.logError("selectedObjectToView is null...");
            return;
        }


        try
        {
            Cell cell = (Cell) selectedObjectToView;
            doCreate3DCell(cell);
            return;
        }
        catch (ClassCastException ex)
        {

        }


        if (selectedObjectToView.equals(LATEST_GENERATED_POSITIONS)
            && (sourceOfCellPosnsInMemory==RELOADED_POSITIONS))
        {

            GuiUtils.showInfoMessage(logger, "Info",
                                     "You've requested to view the \"Latest Generated Positions\", however a previously recorded simulation is in memory.\n"+
                                     "Please press Generate again to create a new set of positions, or select Previous Simulations to select the recorded \n"+
                                     "simulation you wish to view.", this);
            doDestroy3D();
            jComboBoxView3DChoice.setSelectedItem(choice3DChoiceMain);
            return;
        }


        SimulationData simData = null;

        if (selectedObjectToView!=LATEST_GENERATED_POSITIONS)
        {
            File simDataFile = (File)selectedObjectToView;
            simData = projManager.reloadSimulation(simDataFile.getName());

            sourceOfCellPosnsInMemory = RELOADED_POSITIONS;



        }
        else
        {
            if ((projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups() > 0 &&
                projManager.getCurrentProject().generatedCellPositions.getNumberPositionRecords() == 0)
                || sourceOfCellPosnsInMemory == NO_POSITIONS_LOADED)
            {
                GuiUtils.showErrorMessage(logger,
                                          "Please generate the cell positions before proceeding", null, this);
                return;
            }

        }

        this.jButton3DDestroy.setEnabled(true);

        jPanel3DMain.setLayout(new BorderLayout());
        Panel panel = new Panel();
        panel.setLayout(new BorderLayout());

        if (sourceOfCellPosnsInMemory == GENERATED_POSITIONS || sourceOfCellPosnsInMemory == NETWORKML_POSITIONS )
        {
            SimConfig simConfig = getSelectedSimConfig();
            // viewing the generated positions
            base3DPanel = new Main3DPanel(this.projManager.getCurrentProject(), null, simConfig);
        }
        else if (sourceOfCellPosnsInMemory == RELOADED_POSITIONS)
        {
            base3DPanel = new Main3DPanel(this.projManager.getCurrentProject(), simData.getSimulationDirectory(), null);
        }
        panel.add("Center", base3DPanel);
        jPanel3DMain.add("Center", panel);

        this.validate();
    }

    /**
     * Checks if cell posns were reloaded & asks to use these or regenerate
     * @return true to continue, false to stop
     *
     */
    private boolean checkReloadOrRegenerate()
    {
        if (sourceOfCellPosnsInMemory==RELOADED_POSITIONS)
        {
            String warning = "The set of positions, connections and inputs in memory have been reloaded from a saved simulation."
                +"\nThere is not any information on what to plot/save during a simulation associated with this.\n"
                +"To use current information in memory to generate the NEURON network select Continue.\n"
                +"Alternatively, select Regenerate to recreate the network with the appropriate Simulation Configuration.";
                    
           Object[] opts = new Object[]{"Continue", "Regenerate network", "Cancel"};
        
        
           String picked = (String)JOptionPane.showInputDialog(this, 
                                   warning,
                                   "Reuse or regenerate cell positions?", 
                                   JOptionPane.WARNING_MESSAGE , 
                                   null,
                                   opts, 
                                   opts[1]);
        
           
           
           if (picked == null || picked.equals(opts[2])) return false;
           
        
           if (picked.equals(opts[1]))
           {
               jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(this.GENERATE_TAB));
               
               this.doGenerate();
               
               return false;
           }
        }
        return true;
    }
    

    /**
     * Creates the *.hoc file for the project
     *
     */
    public void doCreateHoc(int runMode)
    {
        //neuronRunMode = runMode;


        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        if (projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups() == 0 ||
            (projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups() > 0 &&
            projManager.getCurrentProject().generatedCellPositions.getNumberPositionRecords() == 0))
        {
            GuiUtils.showErrorMessage(logger,
                                      "Please generate the cell positions before proceeding", null, this);
            return;
        }

        boolean cont = this.checkReloadOrRegenerate();
        
        if (!cont) return;


        if (this.jCheckBoxNeuronRandomGen.isSelected())
        {
            Random tempRandom = new Random();
            this.jTextFieldNeuronRandomGen.setText(Math.abs(tempRandom.nextInt())+"");
        }

        long seed = 0;

        //long startTime = System.currentTimeMillis();


        GeneralUtils.timeCheck("Starting generating the hoc code...");



        try
        {
            seed = Long.parseLong(jTextFieldNeuronRandomGen.getText());
        }
        catch (NumberFormatException ex)
        {
            GuiUtils.showErrorMessage(logger, "Please enter a valid integer into the"
                                      +" field for the NEURON random number generator seed", ex, this);
            return;
        }

        refreshSimulationName();


        //projManager.getCurrentProject().neuronFileManager.reset();

        try
        {
            MultiRunManager multiRunManager = new MultiRunManager(this.projManager.getCurrentProject(),
                                                  getSelectedSimConfig(),
                                                  projManager.getCurrentProject().simulationParameters.getReference());


            cont = multiRunManager.checkMultiJobSettings();

            if (!cont)
            {
                logger.logComment("User cancelled");
                return;
            }


            projManager.getCurrentProject().neuronFileManager.generateTheNeuronFiles(this.getSelectedSimConfig(), 
                                                                                     multiRunManager, 
                                                                                     runMode, 
                                                                                     seed);
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem generating the NEURON files",
                                      ex, this);

            setNeuronRunEnabled(false, false);
            return;
        }

        if (projManager.getCurrentProject().neuronFileManager.getGeneratedFilenames().isEmpty())
        {
            logger.logError("No files generated...");
            setNeuronRunEnabled(false, false);
            return;
        }

        Vector allModFiles = projManager.getCurrentProject().neuronFileManager.getModFilesToCompile();


        logger.logComment("--- Neuron mod files to compile: "+allModFiles);


        GeneralUtils.timeCheck("Neuron files all generated...");

        boolean compileSuccess = true;
        
        
        updateNeuronFileList();
        
        if (allModFiles.size()>0)
        {
            try
            {
                File hocDir = projManager.getCurrentProject().neuronFileManager.getMainHocFile().getParentFile();

                // Note: asking to compile one file will compile the whole dir
                File[] modFiles = hocDir.listFiles(new SimpleFileFilter(new String[]{""}, ""));

                if (modFiles!=null && modFiles.length>0)
                {
                    ProcessManager compileProcess = new ProcessManager(modFiles[0]);

                    logger.logComment("Trying to compile the files in dir: " + modFiles[0].getParentFile());
                    

                    compileSuccess = compileProcess.compileFileWithNeuron(projManager.getCurrentProject().neuronSettings.isForceModFileRegeneration(),
                        !projManager.getCurrentProject().neuronSettings.isModSilentMode());
                }
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem compiling the mod files\n"+ex.getMessage(), ex, this);
                setNeuronRunEnabled(false, true);
                return;
            }

            logger.logComment("Compiled hoc file...");

        }
        else
        {
            logger.logComment("No mod files to compile...");
            
        }

        if (!compileSuccess)
        {
            logger.logComment("Problem compiling...");

            setNeuronRunEnabled(false, true);
            return;
        }

        //logger.logComment("Created the hoc code in " +(System.currentTimeMillis() - startTime)+" ms", true);


        setNeuronRunEnabled(true, true);
        

        // need this to update list of 3d position files..?
        //this.refreshAll();
        this.refreshTabNeuron();

        logger.logComment("Finished compiling all of the mod files in: "+ ProjectStructure.getNeuronCodeDir(projManager.getCurrentProject().getProjectMainDirectory()));
    }
    
    private void updateNeuronFileList()
    {
        jComboBoxNeuronFileList.removeAllItems();

        String[] types = new String[]{".hoc", ".mod", ".nrn", ".py", ".xml", ".sh"};
        SimpleFileFilter filter = new SimpleFileFilter(types, "Any NEURON/Python/XML file");


        File[] genFiles = ProjectStructure.getNeuronCodeDir(projManager.getCurrentProject().getProjectMainDirectory()).listFiles(filter);


        for (int i = 0; i < genFiles.length; i++)
        {
            logger.logComment("----    Checking file to add to file viewing list: "+ genFiles[i]);
            
            if (!genFiles[i].isDirectory() && !genFiles[i].getName().equals("README"))
            {
                jComboBoxNeuronFileList.addItem(genFiles[i].getName());
            }
        }
    }
    
    private void updatePsicsFileList()
    {
        jComboBoxPsicsFileList.removeAllItems();

        String[] types = new String[]{".py", ".xml", ".sh"};
        SimpleFileFilter filter = new SimpleFileFilter(types, "Any Python/XML file");


        File[] genFiles = ProjectStructure.getPsicsCodeDir(projManager.getCurrentProject().getProjectMainDirectory()).listFiles(filter);

        for (int i = 0; i < genFiles.length; i++)
        {
            logger.logComment("----    Checking file to add to file viewing list: "+ genFiles[i]);
            
            if (!genFiles[i].isDirectory() && !genFiles[i].getName().equals("README"))
            {
                jComboBoxPsicsFileList.addItem(genFiles[i].getName());
            }
        }
    }
    
    private void updatePynnFileList()
    {
        jComboBoxPynnFileList.removeAllItems();

        String[] types = new String[]{".py", ".xml", ".sh"};
        SimpleFileFilter filter = new SimpleFileFilter(types, "Any Python/XML file");


        File[] genFiles = ProjectStructure.getPynnCodeDir(projManager.getCurrentProject().getProjectMainDirectory()).listFiles(filter);

        for (int i = 0; i < genFiles.length; i++)
        {
            logger.logComment("----    Checking file to add to file viewing list: "+ genFiles[i]);
            
            if (!genFiles[i].isDirectory() && !genFiles[i].getName().equals("README"))
            {
                jComboBoxPynnFileList.addItem(genFiles[i].getName());
            }
        }
    }

    private void setNeuronRunEnabled(boolean runEnabled, boolean viewEnabled)
    {

        this.jButtonNeuronRun.setEnabled(runEnabled);
        this.jButtonNeuronView.setEnabled(viewEnabled);
        this.jComboBoxNeuronFileList.setEnabled(viewEnabled);
        this.jCheckBoxNeuronLineNums.setEnabled(viewEnabled);

    }


    /**
     * Creates the GENESIS files for the project
     *
     */
    protected void doCreateGenesis()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        if (projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups() == 0 ||
            (projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups() > 0 &&
            projManager.getCurrentProject().generatedCellPositions.getNumberPositionRecords() == 0))
        {
            GuiUtils.showErrorMessage(logger,
                                      "Please generate the cell positions before proceeding", null, this);
            return;
        }


        boolean cont = this.checkReloadOrRegenerate();
        
        if (!cont) return;



        if (this.jCheckBoxGenesisRandomGen.isSelected())
        {
            Random tempRandom = new Random();
            this.jTextFieldGenesisRandomGen.setText(Math.abs(tempRandom.nextInt())+"");
        }

        int seed = 0;
        try
        {
            seed = Integer.parseInt(jTextFieldGenesisRandomGen.getText());
        }
        catch (NumberFormatException ex)
        {
            GuiUtils.showErrorMessage(logger, "Please enter a valid integer into the"
                                      +" field for the GENESIS random number generator seed", ex, this);
            return;
        }


        refreshSimulationName();
        projManager.getCurrentProject().genesisFileManager.reset();

        MorphCompartmentalisation mc = (MorphCompartmentalisation)this.jComboBoxGenesisComps.getSelectedItem();

        MultiRunManager multiRunManager = new MultiRunManager(this.projManager.getCurrentProject(),
                                              getSelectedSimConfig(),
                                              projManager.getCurrentProject().simulationParameters.getReference());

        cont = multiRunManager.checkMultiJobSettings();

        if (!cont)
        {
            logger.logComment("User cancelled");
            return;
        }

        setGenesisRunEnabled(false);

        try
        {
            projManager.getCurrentProject().genesisFileManager.generateTheGenesisFiles(this.getSelectedSimConfig(), multiRunManager, mc, seed);
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Error when generating the files: " + ex.getMessage(), ex, this);

            return;
        }

        setGenesisRunEnabled(true);

        File[] genFiles = ProjectStructure.getGenesisCodeDir(projManager.getCurrentProject().getProjectMainDirectory()).listFiles();
        if (projManager.getCurrentProject().genesisSettings.isMooseCompatMode())
            genFiles = ProjectStructure.getMooseCodeDir(projManager.getCurrentProject().getProjectMainDirectory()).listFiles();

        jComboBoxGenesisFiles.removeAllItems();

        for (int i = 0; i < genFiles.length; i++)
        {
            if(!genFiles[i].isDirectory() && !genFiles[i].getName().equals("README"))
            {
                jComboBoxGenesisFiles.addItem(genFiles[i].getName());
            }
        }

        // need this to update list of 3d position files...
        refreshAll();

    }
    
    private void setGenesisRunEnabled(boolean enabled)
    {
        this.jButtonGenesisRun.setEnabled(enabled);
        this.jButtonGenesisView.setEnabled(enabled);
        this.jComboBoxGenesisFiles.setEnabled(enabled);
        this.jCheckBoxGenesisLineNums.setEnabled(enabled);
    }
    
    private void setPsicsRunEnabled(boolean enabled)
    {
        this.jButtonPsicsRun.setEnabled(enabled);
        this.jButtonPsicsView.setEnabled(enabled);
        jComboBoxPsicsFileList.setEnabled(enabled);
        jCheckBoxPsicsLineNums.setEnabled(enabled);
    }
    
    private void setPynnRunEnabled(boolean enabled)
    {
        this.jButtonPynnRun.setEnabled(enabled);
        this.jButtonPynnView.setEnabled(enabled);
        jComboBoxPynnFileList.setEnabled(enabled);
        jCheckBoxPynnLineNums.setEnabled(enabled);
    }

    private void doDestroy3D()
    {

        try
        {
            Main3DPanel main3Dpanel = (Main3DPanel) base3DPanel;
            SimulationRerunFrame simFrame = main3Dpanel.getSimulationFrame();
            simFrame.dispose();
        }
        catch(Exception e)
        {
            // could be class cast or null pointer.
            // either way, ignore..
        }

        jPanel3DMain.removeAll();
        logger.logComment("Removing base3DPanel: " + base3DPanel);
        if (base3DPanel!=null) base3DPanel.destroy3D();
        base3DPanel = null;

        System.gc();
        System.gc();


        this.validate();
        refreshTab3D();
    }



    /**
     * Runs the *.hoc file for the project
     *
     */
    protected void doRunNeuron()
    {
        logger.logComment("Running NEURON code...");
        
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }
        projManager.doRunNeuron(getSelectedSimConfig()); 
/*
        File genNeuronDir = ProjectStructure.getNeuronCodeDir(projManager.getCurrentProject().
                                                                        getProjectMainDirectory());

        //File networkMLFile = new File(genNeuronDir, NetworkMLConstants.DEFAULT_NETWORKML_FILENAME);

        String primarySimDirName = projManager.getCurrentProject().simulationParameters.getReference();


        File positionsFile = new File(genNeuronDir, SimulationData.POSITION_DATA_FILE);
        File netConnsFile = new File(genNeuronDir, SimulationData.NETCONN_DATA_FILE);
        File elecInputFile = new File(genNeuronDir, SimulationData.ELEC_INPUT_DATA_FILE);

       ///////////// if (!networkMLFile.exists())
       /////////// {
            try
            {
                projManager.getCurrentProject().generatedCellPositions.saveToFile(positionsFile);
                projManager.getCurrentProject().generatedNetworkConnections.saveToFile(netConnsFile);
                projManager.getCurrentProject().generatedElecInputs.saveToFile(elecInputFile);
            }
            catch (IOException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem saving generated positions in file: "+ positionsFile.getAbsolutePath(), ex, null);
                return;
            }
       //////////////// }

        // Saving summary of the simulation params
        try
        {
            SimulationsInfo.recordSimulationSummary(projManager.getCurrentProject(),
                                                    getSelectedSimConfig(), genNeuronDir, "NEURON", null);
        }
        catch (IOException ex2)
        {
            GuiUtils.showErrorMessage(logger, "Error when trying to save a summary of the simulation settings in dir: "
                                      + genNeuronDir +
                                      "\nThere will be less info on this simulation in the previous simulation browser dialog",
                                      ex2, null);
        }


        File[] generatedNeuronFiles = genNeuronDir.listFiles();


        ArrayList<String> simDirsToCreate = projManager.getCurrentProject().neuronFileManager.getGeneratedSimReferences();

        simDirsToCreate.add(primarySimDirName);

        for (String simRef : simDirsToCreate)
        {
            File dirForSimFiles = ProjectStructure.getDirForSimFiles(simRef, projManager.getCurrentProject());

            if (dirForSimFiles.exists())
            {
                SimpleFileFilter sff = new SimpleFileFilter(new String[]
                                                            {".dat"}, null);
                File[] files = dirForSimFiles.listFiles(sff);
                for (int i = 0; i < files.length; i++)
                {
                    files[i].delete();
                }
                logger.logComment("Directory " + dirForSimFiles + " being cleansed");
            }
            else
            {
                GuiUtils.showErrorMessage(logger, "Directory " + dirForSimFiles + " doesn't exist...", null, null);
                return;
            }

        
                
                for (int i = 0; i < generatedNeuronFiles.length; i++)
                {
                    String fn = generatedNeuronFiles[i].getName();
                    
                    if (fn.endsWith(".dat")||
                        fn.endsWith(".props") ||
                            fn.endsWith(".py")||
                            fn.endsWith(".xml") ||
                        (projManager.getCurrentProject().neuronSettings.isCopySimFiles() &&
                            (fn.endsWith(".hoc") ||
                            fn.endsWith(".mod") ||
                            fn.endsWith(".dll"))))
                    {
                        try
                        {
                            //System.out.println("Saving a copy of file: " + generatedNeuronFiles[i]
                            //                  + " to dir: " +
                            //                  dirForSimFiles);

                            GeneralUtils.copyFileIntoDir(generatedNeuronFiles[i],
                                                         dirForSimFiles);
                        }
                        catch (IOException ex)
                        {
                            GuiUtils.showErrorMessage(logger, "Error copying file: " + ex.getMessage(), ex, this);
                            return;
                        }
                    }
                    else if (projManager.getCurrentProject().neuronSettings.isCopySimFiles() &&
                             (generatedNeuronFiles[i].getName().equals(GeneralUtils.DIR_I686) || 
                             generatedNeuronFiles[i].getName().equals(GeneralUtils.DIR_64BIT)))
                    {
                        File toDir = new File(dirForSimFiles, generatedNeuronFiles[i].getName());
                        toDir.mkdir();
                        logger.logComment("Saving the linux libs from the compiled mods of file: " +
                                          generatedNeuronFiles[i]
                                          + " to dir: " +
                                          toDir);

                        try
                        {
                            GeneralUtils.copyDirIntoDir(generatedNeuronFiles[i], toDir, true, true);
                        }
                        catch (IOException ex1)
                        {
                            GuiUtils.showErrorMessage(logger,
                                                      "Error while saving the linux libs from the compiled mods from  of file: " +
                                                      generatedNeuronFiles[i]
                                                      + " to dir: " + dirForSimFiles, ex1, this);

                            return;
                        }
                    }
                    else if (projManager.getCurrentProject().neuronSettings.isCopySimFiles() && 
                             generatedNeuronFiles[i].isDirectory() && 
                             (generatedNeuronFiles[i].getName().equals(ProjectStructure.neuroMLPyUtilsDir) ||
                              generatedNeuronFiles[i].getName().equals(ProjectStructure.neuronPyUtilsDir)))
                    {
                        File toDir = new File(dirForSimFiles, generatedNeuronFiles[i].getName());
                        toDir.mkdir();

                        try
                        {
                            GeneralUtils.copyDirIntoDir(generatedNeuronFiles[i], toDir, true, true);
                        }
                        catch (IOException ex1)
                        {
                            GuiUtils.showErrorMessage(logger,
                                                      "Error while copying file: " +
                                                      generatedNeuronFiles[i]
                                                      + " to dir: " + dirForSimFiles, ex1, this);

                            return;
                        }
                    }

            }

            if (GeneralProperties.getGenerateMatlab())
            {
                MatlabOctave.createSimulationLoader(projManager.getCurrentProject(), getSelectedSimConfig(), simRef);
            }

            if ((GeneralUtils.isWindowsBasedPlatform() || GeneralUtils.isMacBasedPlatform()) 
                && GeneralProperties.getGenerateIgor())
            {
                IgorNeuroMatic.createSimulationLoader(projManager.getCurrentProject(), getSelectedSimConfig(), simRef);
            }
        }

        File simRunDir = ProjectStructure.getDirForSimFiles(projManager.getCurrentProject().simulationParameters.getReference(),
                                                                projManager.getCurrentProject());
        
        if (!projManager.getCurrentProject().neuronSettings.isCopySimFiles())
        {
            simRunDir = new File(genNeuronDir.getAbsolutePath());
        }

        try
        {
            File newMainHocFile = new File(simRunDir,
                               projManager.getCurrentProject().neuronFileManager.getMainHocFile().getName());


            logger.logComment("Going to run file: "+ newMainHocFile);

            projManager.getCurrentProject().neuronFileManager.runNeuronFile(newMainHocFile);
        }
        catch (NeuronException ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
            return;
        }
*/

        refreshTabNeuron();
        refreshTab3D();

    }


    protected final void doCheckForLogFiles()
    {
        File logFileDir = GeneralProperties.getLogFileDir();

        if(logFileDir.exists()&&GeneralProperties.getLogFileSaveToFilePolicy())
        {
            File[] subfiles = logFileDir.listFiles();
            int totFileCount = 0;
            long totBytes = 0;
            for(File f: subfiles)
            {
                if (f.getName().endsWith(Logger.LOG_FILE_SUFFIX))
                {
                    totFileCount++;
                    totBytes += f.length();
                }
            }
            if (totFileCount%10==0)
            {
                String info = "Please note that saving to log files in folder: "+ logFileDir.getAbsolutePath()+" is enabled and\n"+
                    "this folder contains "+totFileCount+" files totalling "+totBytes+" bytes. Note that logging to file can be turned off\n" +
                    "via Settings -> General Properties & Project Defaults -> Logging";

                GuiUtils.showWarningMessage(logger, info, this);
            }
        }
    }

    
    /**
     * Generates the Pynn files for the project
     *
     */
    protected void doGeneratePynn()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }
        

        boolean cont = this.checkReloadOrRegenerate();
        
        if (!cont) return;
        
        logger.logComment("Generating the PyNN files for the project...");
        
        refreshSimulationName();
        
        
        if (this.jCheckBoxPyNNRandomGen.isSelected())
        {
            Random tempRandom = new Random();
            this.jTextFieldPyNNRandomGen.setText(Math.abs(tempRandom.nextInt())+"");
        }
        
        int seed = Integer.parseInt(jTextFieldPyNNRandomGen.getText());
        
        projManager.getCurrentProject().pynnFileManager.reset();
        
        
        try
        {
            PynnSimulator sim = null;
            
            if (jRadioButtonPynnNeuron.isSelected())
                sim = PynnFileManager.PynnSimulator.NEURON;
            else if (jRadioButtonPynnNest2.isSelected())
                sim = PynnFileManager.PynnSimulator.NEST;
            else if (jRadioButtonPynnPcsim.isSelected())
                sim = PynnFileManager.PynnSimulator.PCSIM;
            else if (jRadioButtonPynnBrian.isSelected())
                sim = PynnFileManager.PynnSimulator.BRIAN;
            else if (jRadioButtonPynnPyMoose.isSelected())
                sim = PynnFileManager.PynnSimulator.PYMOOSE;
            
            projManager.getCurrentProject().pynnFileManager.generateThePynnFiles(this.getSelectedSimConfig(), sim, seed);
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Error when generating the files: " + ex.getMessage(), ex, this);

            return;
        }
        
        updatePynnFileList();
        setPynnRunEnabled(true);
        
        refreshTabPynn();
        
        
    }

    /**
     * Generates the PSICS files for the project
     *
     */
    protected void doGeneratePsics()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }
        
        logger.logComment("Generating the PSICS files for the project...");
        
        

        if (projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups() == 0 ||
            (projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups() > 0 &&
            projManager.getCurrentProject().generatedCellPositions.getNumberPositionRecords() == 0))
        {
            GuiUtils.showErrorMessage(logger,
                                      "Please generate the cell positions before proceeding", null, this);
            return;
        }

        boolean cont = this.checkReloadOrRegenerate();
        
        if (!cont) return;
        
        int seed = 0;
        /*try
        {
            seed = Integer.parseInt(jTextFieldGenesisRandomGen.getText());
        }
        catch (NumberFormatException ex)
        {
            GuiUtils.showErrorMessage(logger, "Please enter a valid integer into the"
                                      +" field for the PSICS random number generator seed", ex, this);
            return;
        }*/
        
        
        refreshSimulationName();
        
        projManager.getCurrentProject().psicsFileManager.reset();



        try
        {
            projManager.getCurrentProject().psicsFileManager.generateThePsicsFiles(this.getSelectedSimConfig(), seed);
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Error when generating the files: " + ex.getMessage(), ex, this);

            return;
        }
        updatePsicsFileList();
        setPsicsRunEnabled(true);
        
        refreshTabPsics();
        
    }
    
    
    /**
     * Runs the PSICS files for the project
     *
     */
    protected void doRunPynn()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }
        
        String simRef = this.jTextFieldSimRef.getText();
        

        logger.logComment("Running the Pynn files for the project for simulation ref: "+simRef+"...");
        
        try
        {
            if (GeneralProperties.getGenerateMatlab())
            {
                MatlabOctave.createSimulationLoader(projManager.getCurrentProject(), getSelectedSimConfig(), simRef);
            }

            if ((GeneralUtils.isWindowsBasedPlatform() || GeneralUtils.isMacBasedPlatform())
                && GeneralProperties.getGenerateIgor())
            {
                IgorNeuroMatic.createSimulationLoader(projManager.getCurrentProject(), getSelectedSimConfig(),
                                                      simRef);
            }
            
            projManager.getCurrentProject().pynnFileManager.runFile(true);
            
            if (jCheckBoxPynnShowTraces.isSelected())
            {
                File simResDir = new File(ProjectStructure.getSimulationsDir(projManager.getCurrentProject().getProjectMainDirectory()), simRef);
                File simTimeFile = new File(simResDir, SimulationData.getStandardTimesFilename());
                int maxWaitSecs = 10;
               
                long startWait = System.currentTimeMillis();
                while (System.currentTimeMillis()-startWait < maxWaitSecs*1000)
                {
                    try 
                    {
                        Thread.sleep(500);
                        logger.logComment("Waiting for "+ simTimeFile.getAbsolutePath()+" to be generated...");
                        if(simTimeFile.exists())
                        {
                            String info = "Saved traces from simulation "+simRef+" in project "+ projManager.getCurrentProject().getProjectName();
                            PlotterFrame pf = PlotManager.getPlotterFrame(info);
                            
                            ArrayList<String> cgs =projManager.getCurrentProject().generatedCellPositions.getNonEmptyCellGroups();
                            
                            for(String cellGroup: cgs)
                            {
                                File cgDataFile = new File(simResDir, cellGroup+".dat");
                                ArrayList<DataSet> dss = DataSetManager.loadFromDataSetFile(cgDataFile, false, DataSetManager.DataReadFormat.NUMBERED_TRACES);
                                
                                for(DataSet ds: dss)
                                {
                                    pf.addDataSet(ds);
                                }
                            }
                            
                            return;
                        }
                    } 
                    catch (InterruptedException ex) 
                    {
                        GuiUtils.showErrorMessage(logger, "Error waiting for generation of file: "+ simTimeFile.getAbsolutePath(), ex, this);
                        return;
                    }
                    catch (DataSetException ex) 
                    {
                        GuiUtils.showErrorMessage(logger, "Error extracting data sets", ex, this);
                        return;
                    }
                }
                        
                GuiUtils.showWarningMessage(logger, "Have waited "+maxWaitSecs+" seconds for completion of simulation "+simRef
                        +" but the file: "+ simTimeFile.getAbsolutePath()+" has not yet been generated", this);
                
                
            }
        }
        catch (PynnException ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
            return;
        }
        
    }

    /**
     * Runs the PSICS files for the project
     *
     */
    protected void doRunPsics()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        logger.logComment("Running the PSICS files for the project...");
        
        try
        {
            if (GeneralProperties.getGenerateMatlab())
            {
                MatlabOctave.createSimulationLoader(projManager.getCurrentProject(), getSelectedSimConfig(), this.jTextFieldSimRef.getText());
            }

            if ((GeneralUtils.isWindowsBasedPlatform() || GeneralUtils.isMacBasedPlatform())
                && GeneralProperties.getGenerateIgor())
            {
                IgorNeuroMatic.createSimulationLoader(projManager.getCurrentProject(), getSelectedSimConfig(),
                                                      this.jTextFieldSimRef.getText());
            }
            
            projManager.getCurrentProject().psicsFileManager.runFile(true, 
                                                jCheckBoxPsicsShowHtml.isSelected(),
                                                jCheckBoxPsicsShowPlot.isSelected(),
                                                jCheckBoxPsicsConsole.isSelected());
        }
        catch (PsicsException ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
            return;
        }
        
    }
    
    

    /**
     * Runs the GENESIS files for the project
     *
     */
    protected void doRunGenesis()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        try
        {
            if (GeneralProperties.getGenerateMatlab())
            {
                MatlabOctave.createSimulationLoader(projManager.getCurrentProject(), getSelectedSimConfig(), this.jTextFieldSimRef.getText());
            }

            if ((GeneralUtils.isWindowsBasedPlatform() || GeneralUtils.isMacBasedPlatform())
                && GeneralProperties.getGenerateIgor())
            {
                IgorNeuroMatic.createSimulationLoader(projManager.getCurrentProject(), getSelectedSimConfig(),
                                                      this.jTextFieldSimRef.getText());
            }
            
            projManager.getCurrentProject().genesisFileManager.runGenesisFile();
        }
        catch (GenesisException ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
            return;
        }

        //GuiUtils.showInfoMessage(logger,
       //                          "Running simulation in NEURON...",
       //                          "A new simulation entitled "+jTextFieldSimRef.getText()+" is being run in NEURON. Please wait ", this);
        refreshTab3D();

    }


    /**
     * Creates a new Cell Group
     *
     */
    private void doNewCellGroup()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        logger.logComment("Creating a new Cell Group...");

        String cellGroupName = null;
        String regionName = null;
        String cellType = null;
        Color cellGroupColour = null;
        CellPackingAdapter adapter = null;
        int priority = 10 - projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups();

        if (projManager.getCurrentProject().cellManager.getNumberCellTypes() == 0)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Please create one or more Cell Types before proceeding", null, this);
            return;
        }


        if (projManager.getCurrentProject().regionsInfo.getNumberRegions() == 0)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Please create one or more 3D Regions before proceeding", null, this);
            return;
        }
        int numCellGroups = this.projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups();

        CellGroupDialog dlg = new CellGroupDialog(this, "New Cell Group",
            "CellGroup_" + (numCellGroups + 1), priority, projManager.getCurrentProject());

        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);

        regionName = dlg.getRegionName();
        cellGroupName = dlg.getCellGroupName();
        cellType = dlg.getCellType();
        cellGroupColour = dlg.getCellGroupColour();

        adapter = dlg.getCellPackingAdapter();

        priority = dlg.getPriority();
    ///    enabled = dlg.isCellGroupEnabled();

        if (dlg.cancelled)
        {
            logger.logComment("The action was cancelled...");
            return;
        }
        //this.cellGroupModel.addRow(cellGroupName, cellType, regionName, cellGroupColour, density);
        try
        {
            this.projManager.getCurrentProject().cellGroupsInfo.addRow(cellGroupName,
                                                                       cellType,
                                                                       regionName,
                                                                       cellGroupColour,
                                                                       adapter,
                                                                       priority);
        }
        catch (NamingException ex)
        {
            GuiUtils.showErrorMessage(logger, "Please select another name for the cell group", ex, this);
            return;
        }

        int save = JOptionPane.showConfirmDialog(this, "Would you like the membrane potential of this Cell Group recorded and plotted during simulations?\n"
                                                 +"Note: this can be changed later by altering the Simulation Configuration or deleting\n"
                                                 +"the variable at tab Input/Output", "Save Cell Group voltage?", JOptionPane.YES_NO_OPTION);

        if (save == JOptionPane.YES_OPTION)
        {
            String plotRef = cellGroupName+"_v";
            SimPlot simPlot = new SimPlot(plotRef,
                                          cellGroupName+"_v",
                                          cellGroupName,
                                          "*",
                                          "0",
                                          SimPlot.VOLTAGE,
                                          -90,
                                          50,
                                          SimPlot.PLOT_AND_SAVE);


            projManager.getCurrentProject().simPlotInfo.addSimPlot(simPlot);

            if (this.projManager.getCurrentProject().simConfigInfo.getNumSimConfigs() == 1)
            {
                projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().addPlot(plotRef);
            }
            else
            {
                GuiUtils.showInfoMessage(logger, "Variable to save/plot added",
                    "There is more than one Simulation Configuration. To specify that this variable to save/plot should be included in one of them (reference: "+plotRef+"), go to tab Generate.", this);
            }

        }

        projManager.getCurrentProject().markProjectAsEdited();
        jButtonCellGroupsDelete.setEnabled(true);

        if (this.projManager.getCurrentProject().simConfigInfo.getNumSimConfigs()==1)
        {
            projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().addCellGroup(cellGroupName);
            logger.logComment("Now cell groups in default SimConfig: "+ projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().getCellGroups());
        }
        else
        {
            GuiUtils.showInfoMessage(logger, "Added Cell Group", "There are more than one Simulation Configurations. To add this Cell Group to one of them, go to tab Generate.", this);
        }

        this.refreshTabCellGroupInfo();
    }


    /**
     * Shows the project options dialog box
     *
     * @param tabToSelect which tab to initially set as selected
     * @param mode OptionsFrame.PROJECT_PROPERTIES_MODE etc.
     */

    private void doOptionsPane(String tabToSelect, int mode)
    {

        if (OptionsFrame.isOptionsFrameCurrentlyDisplayed())
        {
            logger.logComment("OptionsFrame is already being displayed...");
            optFrame.toFront();
            return;
        }

        String title = null;
        if (mode==OptionsFrame.PROJECT_PROPERTIES_MODE)
        {
            if (projManager.getCurrentProject() == null)
            {
                logger.logError("No project loaded...");
                return;
            }
            title = "Preferences for project: " + projManager.getCurrentProject().getProjectName();
        }
        else
        {
            title = "General Preferences and Project Default Settings";
        }
        optFrame = new OptionsFrame(this, title, mode);
        optFrame.selectTab(tabToSelect);
        Dimension dlgSize = optFrame.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        optFrame.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                             (frmSize.height - dlgSize.height) / 2 + loc.y);

        optFrame.pack();
        optFrame.setVisible(true);

        if (optFrame.somethingAlteredInProject && mode==OptionsFrame.PROJECT_PROPERTIES_MODE)
        {
            logger.logComment("Something's been altered in the properties...");
            projManager.getCurrentProject().markProjectAsEdited();
        }
        //this.refreshAll();
        optFrame.toFront(); // because the 3D panel is greedy...

    }




    /**
     * Unzips a *.neuro.zip file and opens as a new proj...
     */
    protected void doUnzipProject()
    {
        boolean allOk = checkToSave();

        if (!allOk) return; // i.e. cancelled...

        this.closeProject();


        File defaultDir = GeneralProperties.getnCProjectsDir();

        logger.logComment("Unzipping a project...");

        JFileChooser zipFileChooser = new JFileChooser();

        zipFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        zipFileChooser.setCurrentDirectory(defaultDir);

        zipFileChooser.setDialogTitle("Open zipped neuroConstruct project");

        SimpleFileFilter fileFilter = ProjectStructure.getZippedProjectFileFilter();

        zipFileChooser.setFileFilter(fileFilter);

        int retval = zipFileChooser.showDialog(this, "Import project");

        if (retval == JFileChooser.APPROVE_OPTION)
        {
            File chosenZipFile = zipFileChooser.getSelectedFile();

            logger.logComment("File chosen: "+ chosenZipFile);

            if (!(chosenZipFile.getName().endsWith(ProjectStructure.getOldProjectZipFileExtension()) ||
                chosenZipFile.getName().endsWith(ProjectStructure.getNewProjectZipFileExtension())))
            {
                GuiUtils.showErrorMessage(logger, "The zip file does not seem to have been generated by neuroConstruct", null, this);
                return;
            }

            JFileChooser destDirChooser = new JFileChooser();

            destDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            String newProjName = chosenZipFile.getName().substring(0,
                  chosenZipFile.getName().indexOf("."));

            destDirChooser.setDialogTitle("Please select a directory for the project: "+ newProjName);


            destDirChooser.setCurrentDirectory(GeneralProperties.getnCProjectsDir());

            boolean validEmptyDirectory = false;

            while (!validEmptyDirectory)
            {

                retval = destDirChooser.showDialog(this,
                                                   "Create new folder " + newProjName + System.getProperty("file.separator") +
                                                   " for project here");

                if (retval == JFileChooser.APPROVE_OPTION)
                {

                    File chosenParentDir = destDirChooser.getSelectedFile();

                    File projDirToCreate = new File (chosenParentDir.getAbsolutePath()
                                                     + System.getProperty("file.separator")
                                                     + newProjName);

                    logger.logComment("Dir chosen: " + chosenParentDir);

                    if (!projDirToCreate.exists()) projDirToCreate.mkdir();

                    if (projDirToCreate.listFiles().length>0)
                    {
                        GuiUtils.showErrorMessage(logger, "The directory: "+ projDirToCreate +" contains files. Please select a directory with no "+newProjName+" folder.", null, this);

                    }
                    else
                    {
                        validEmptyDirectory = true;
                        try
                        {
                            ZipUtils.unZip(projDirToCreate.getAbsolutePath(), chosenZipFile.getAbsolutePath());

                            String nameOfNewProjectFile = projDirToCreate.getAbsolutePath()
                                                     + System.getProperty("file.separator")
                                                     + newProjName
                                                     + ProjectStructure.getNewProjectFileExtension();

                            if (!(new File(nameOfNewProjectFile)).exists())
                            {

                                String nameOfOldProjectFile = projDirToCreate.getAbsolutePath()
                                                     + System.getProperty("file.separator")
                                                     + newProjName
                                                     + ProjectStructure.getOldProjectFileExtension();

                                if (!(new File(nameOfOldProjectFile)).exists())
                                {
                                    GuiUtils.showErrorMessage(logger, "The expected project file: "+ nameOfNewProjectFile
                                            + " was not found (neither was: "+nameOfOldProjectFile+")! ", null, this);
                                    projDirToCreate.delete();
                                    return;
                                }
                                else
                                {
                                    nameOfNewProjectFile = nameOfOldProjectFile;
                                }
                            }

                            doLoadProject(nameOfNewProjectFile);
                        }
                        catch (Exception ex)
                        {
                            GuiUtils.showErrorMessage(logger, "Problem extracting the zipped file: " + chosenZipFile + " to "+ projDirToCreate,
                                                      ex, this);
                        }

                    }

                }
                else
                {
                    logger.logComment("User has changed their mind...");
                    return;

                }
            }


        }
        else
        {
            logger.logComment("User has changed their mind...");
            return;
        }

    }


    public void giveUpdate(String update)
    {
        logger.logComment("giveUpdate called with: "+update);

        if (jProgressBarGenerate.getValue()<jProgressBarGenerate.getMaximum())
            this.jProgressBarGenerate.setString(update);

    }
    
    private void setGeneratorInfo(String text)
    {
        Document doc = jEditorPaneGenerateInfo.getEditorKit().createDefaultDocument();
        jEditorPaneGenerateInfo.setDocument(doc);

        SimpleHtmlDoc ht = new SimpleHtmlDoc();
        ht.addRawHtml(text);

        String text2 = ht.toHtmlString();

        jEditorPaneGenerateInfo.setText(text2);
        
        //System.out.println("info: "+text2);
    }
    


    public void giveGenerationReport(String report, String generatorType, SimConfig simConfig)
    {
        logger.logComment("giveGenerationReport called by: "+ generatorType+ ", report: "+ report);

        if (generatorType.equals(CellPositionGenerator.myGeneratorType))
        {
            setGeneratorInfo(report);
            try 
            {
                Thread.sleep(500); // to ensure updated info screen
            } 
            catch (InterruptedException ex) {
                // go on...
            }
            //Document doc = jEditorPaneGenerateInfo.getEditorKit().createDefaultDocument();
            //doc.insertString(0, report, null);
            
            if (projManager.getCurrentProject().generatedCellPositions.getNumberPositionRecords() == 0)
            {
                GuiUtils.showErrorMessage(logger,
                    "No cell positions generated. Please ensure the cell bodies will fit in the selected regions.", null, this);
                return;
            }

            if (report.indexOf("Generation interrupted")>0)
            {
                logger.logComment("It seems the generation of cell positions was interrupted...");
                return;
            }
            projManager.netConnGenerator = new MorphBasedConnGenerator(projManager.getCurrentProject(), this);

            projManager.netConnGenerator.setSimConfig(simConfig);

            projManager.netConnGenerator.start();

        }
        else if (generatorType.equals(MorphBasedConnGenerator.myGeneratorType))
        {
            
            String currentReport = jEditorPaneGenerateInfo.getText();
            
            if (currentReport.length()==0 || currentReport.equals(generatePleaseWait))
            {
                try 
                {
                    Thread.sleep(1500);
                    currentReport = jEditorPaneGenerateInfo.getText();
                } 
                catch (InterruptedException ex) {
                    // go on...
                }
            }
            String update = currentReport.substring(0, currentReport.lastIndexOf("</body>")) + report;

            setGeneratorInfo(update + "  ");

            if (report.indexOf("Generation interrupted") > 0) {
                logger.logComment("It seems the generation of connections was interrupted...");
                return;
            }

            projManager.arbourConnectionGenerator = new VolumeBasedConnGenerator(projManager.getCurrentProject(), this);

            projManager.arbourConnectionGenerator.setSimConfig(simConfig);

            projManager.arbourConnectionGenerator.start();



        }

        else if (simConfig.getMpiConf().isParallelOrRemote()
                   && generatorType.equals(VolumeBasedConnGenerator.myGeneratorType))
        {
            String currentReport = jEditorPaneGenerateInfo.getText();

            String update = currentReport.substring(0,currentReport.lastIndexOf("</body>")) // as the jEditorPane returns html...
                                       +report;
            
            setGeneratorInfo(update);

            if (report.indexOf("Generation interrupted")>0)
            {
                logger.logComment("It seems the generation of connections was interrupted...");
                return;
            }

            projManager.compNodeGenerator = new CompNodeGenerator(projManager.getCurrentProject(), this);

            projManager.compNodeGenerator.setSimConfig(simConfig);

            projManager.compNodeGenerator.start();
        }

        else if ((!(simConfig.getMpiConf().isParallelOrRemote())
                && (generatorType.equals(VolumeBasedConnGenerator.myGeneratorType))
                || generatorType.equals(CompNodeGenerator.myGeneratorType)))
        {
            String currentReport = jEditorPaneGenerateInfo.getText();

            String update = currentReport.substring(0,currentReport.lastIndexOf("</body>")) // as the jEditorPane returns html...
                                       +report;

            setGeneratorInfo(update);



            if (report.indexOf("Generation interrupted")>0)
            {
                logger.logComment("It seems the generation of compute nodes was interrupted...");
                return;
            }


            projManager.elecInputGenerator = new ElecInputGenerator(projManager.getCurrentProject(), this);

            projManager.elecInputGenerator.setSimConfig(simConfig);

            projManager.elecInputGenerator.start();

        }


        else if (generatorType.equals(ElecInputGenerator.myGeneratorType))
        {
            String currentReport = jEditorPaneGenerateInfo.getText();

            String update = currentReport.substring(0, currentReport.lastIndexOf("</body>")) // as the jEditorPane returns html...
                                       + report;
            
            setGeneratorInfo(update);

            if (report.indexOf("Generation interrupted") > 0)
            {
                logger.logComment("It seems the generation of cell positions was interrupted...");
                return;
            }


            projManager.plotSaveGenerator = new PlotSaveGenerator(projManager.getCurrentProject(), this);

            projManager.plotSaveGenerator.setSimConfig(simConfig);

            projManager.plotSaveGenerator.start();


        }

        else if (generatorType.equals(PlotSaveGenerator.myGeneratorType))
        {

            String currentReport = jEditorPaneGenerateInfo.getText();

            String update = currentReport.substring(0, currentReport.lastIndexOf("</body>")) + report;

            setGeneratorInfo(update);

            if (report.indexOf("Generation interrupted") > 0)
            {
                logger.logComment("It seems the generation of cell positions was interrupted...");
                return;
            }

            projManager.cellInitialiser = new CellInitialiser(projManager.getCurrentProject(), this);

            projManager.cellInitialiser.setSimConfig(simConfig);

            projManager.cellInitialiser.start();


        }

        else if (generatorType.equals(CellInitialiser.myGeneratorType))
        {


            this.jButtonGenerateStop.setEnabled(false);

            refreshTabGenerate();
        }
        else
        {
            logger.logComment("Don't know the type of that generation report!!: " + generatorType);
        }



    };


    public void majorStepComplete()
    {
        int currentValue = jProgressBarGenerate.getValue();
        int newVal = currentValue+100;

        logger.logComment("currentValue: "+ currentValue + ", newVal: "+ newVal+ ", max: "+ jProgressBarGenerate.getMaximum());
        jProgressBarGenerate.setValue(newVal);

        if (jProgressBarGenerate.getValue()>=jProgressBarGenerate.getMaximum())
        {
            String info = jEditorPaneGenerateInfo.getText();
            if (info.indexOf("Warning") >=0 )
            {
                jProgressBarGenerate.setString("Network generated, with warnings!");
                jProgressBarGenerate.setForeground(ValidityStatus.VALIDATION_COLOUR_WARN_OBJ);
            }
            else
            {
                jProgressBarGenerate.setString("Network generated");
            }
        }
    }




    /**
     * Removes which ever region is selected in table jTable3DRegions
     *
     */

    private void doRemoveRegion()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        int selectedRow = jTable3DRegions.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }
        String regionName = (String)projManager.getCurrentProject().regionsInfo.getValueAt(selectedRow, RegionsInfo.COL_NUM_REGIONNAME);

        Vector cellGroupsUsingIt = projManager.getCurrentProject().cellGroupsInfo.getCellGroupsInRegion(regionName);

        if (cellGroupsUsingIt.size() > 0)
        {
            StringBuilder errorString = new StringBuilder("The Cell Group");
            if (cellGroupsUsingIt.size() > 1) errorString.append("s: ");
            else errorString.append(": ");

            for (int i = 0; i < cellGroupsUsingIt.size(); i++)
            {
                errorString.append(" " + cellGroupsUsingIt.elementAt(i));
                if (i < cellGroupsUsingIt.size() - 1) errorString.append(", ");
            }
            String buttonText = null;
            if (cellGroupsUsingIt.size() > 1)
            {
                errorString.append(" are in the Region: " + regionName + ". Delete these too?");
                buttonText = "Delete Cell Groups";
            }
            else
            {
                errorString.append(" is in the Region: " + regionName + ". Delete this too?");
                buttonText = "Delete Cell Group";
            }

            Object[] options =
                {buttonText, "Cancel All"};

            JOptionPane option = new JOptionPane(errorString.toString(),
                                                 JOptionPane.DEFAULT_OPTION,
                                                 JOptionPane.WARNING_MESSAGE,
                                                 null,
                                                 options,
                                                 options[0]);

            JDialog dialog = option.createDialog(this, "Warning");
            dialog.setVisible(true);

            Object choice = option.getValue();
            logger.logComment("User has chosen: " + choice);
            if (choice.equals("Cancel All"))
            {
                logger.logComment("User has changed their mind...");
                return;
            }

            for (int i = 0; i < cellGroupsUsingIt.size(); i++)
            {
                String nextCellGroup = (String) cellGroupsUsingIt.elementAt(i);
                logger.logComment("Deleting: " + nextCellGroup);
                //projManager.getCurrentProject().networkConnectionsInfo.deleteNetConn(nextNetConn);

                doRemoveCellGroup(nextCellGroup);

            }
        }

        projManager.getCurrentProject().regionsInfo.deleteRegion(selectedRow);
        logger.logComment("Removed row: " + selectedRow);
        refreshTabRegionsInfo();
    }

    /**
     * Removes whichever cell group is selected in table jTable3DRegions
     *
     */
    private void doRemoveCellGroup(String cellGroupName)
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        Vector<String> netConnsUsingIt = projManager.getCurrentProject().morphNetworkConnectionsInfo.getNetConnsUsingCellGroup(cellGroupName);
        Vector<String> aaNetConnsUsingIt = projManager.getCurrentProject().volBasedConnsInfo.getAAConnsUsingCellGroup(cellGroupName);
        netConnsUsingIt.addAll(aaNetConnsUsingIt);



        if (netConnsUsingIt.size()>0)
        {
            StringBuilder errorString = new StringBuilder("The Network Connection");
            if (netConnsUsingIt.size()>1) errorString.append("s: ");
                else errorString.append(": ");


            for (int i = 0; i < netConnsUsingIt.size(); i++)
            {
                  errorString.append(" "+ netConnsUsingIt.elementAt(i));
                  if (i<netConnsUsingIt.size()-1) errorString.append(", ");
            }
            String buttonText = null;
            if (netConnsUsingIt.size()>1)
            {
                errorString.append(" use the Cell Group: " + cellGroupName + ". Delete these too?");
                buttonText = "Delete Network Connections";
            }
            else
            {
                errorString.append(" uses the Cell Group: " + cellGroupName + ". Delete this too?");
                buttonText = "Delete Network Connection";
            }


            Object[] options = {buttonText, "Cancel All"};

            JOptionPane option = new JOptionPane(errorString.toString(),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]);

            JDialog dialog = option.createDialog(this, "Warning");
            dialog.setVisible(true);

            Object choice = option.getValue();
            logger.logComment("User has chosen: " + choice);
            if (choice.equals("Cancel All"))
            {
                logger.logComment("User has changed their mind...");
                return;
            }

            for (int i = 0; i < netConnsUsingIt.size(); i++)
            {
                String nextNetConn = netConnsUsingIt.elementAt(i);
                logger.logComment("Deleting: "+ nextNetConn);
                boolean res = projManager.getCurrentProject().morphNetworkConnectionsInfo.deleteNetConn(nextNetConn);
                if (!res) projManager.getCurrentProject().volBasedConnsInfo.deleteConn(nextNetConn);
            }


        }



        projManager.getCurrentProject().cellGroupsInfo.deleteCellGroup(cellGroupName);
        logger.logComment("Removed row: " + cellGroupName);

        projManager.getCurrentProject().simConfigInfo.validateStoredSimConfigs(projManager.getCurrentProject());

        refreshTabCellGroupInfo();
        refreshTabExport(); // due to list of cell groups being shown there...
    }


    /**
     * Edits whichever Cell Group is selected
     *
     */
    private void doEditCellGroup()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        int selectedRow = jTableCellGroups.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }
        String editCellGroupName = projManager.getCurrentProject().cellGroupsInfo.getCellGroupNameAt(selectedRow);

        CellGroupDialog dlg = new CellGroupDialog(this, "Edit Cell Group",
                                                        editCellGroupName,
                                                        projManager.getCurrentProject().cellGroupsInfo.getCellType(editCellGroupName),
                                                        projManager.getCurrentProject().cellGroupsInfo.getRegionName(editCellGroupName),
                                                        projManager.getCurrentProject().cellGroupsInfo.getColourOfCellGroup(editCellGroupName),
                                                        projManager.getCurrentProject().cellGroupsInfo.getCellPackingAdapter(editCellGroupName),
                                                        projManager.getCurrentProject().cellGroupsInfo.getPriority(editCellGroupName),
                                                        projManager.getCurrentProject());

        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);


        if (dlg.cancelled)
        {
            logger.logComment("The action was cancelled...");
            return;
        }

        projManager.getCurrentProject().cellGroupsInfo.setCellType(editCellGroupName, dlg.getCellType());
        projManager.getCurrentProject().cellGroupsInfo.setRegion(editCellGroupName, dlg.getRegionName());

        projManager.getCurrentProject().cellGroupsInfo.setColourOfCellGroup(editCellGroupName, dlg.getCellGroupColour());
        projManager.getCurrentProject().cellGroupsInfo.setAdapter(editCellGroupName, dlg.getCellPackingAdapter());
        projManager.getCurrentProject().cellGroupsInfo.setPriority(editCellGroupName, dlg.getPriority());

       /// projManager.getCurrentProject().cellGroupsInfo.setCellGroupEnabled(editCellGroupName, dlg.isCellGroupEnabled());

        logger.logComment("Set all alteres parameters");

        projManager.getCurrentProject().markProjectAsEdited();

        this.refreshTabCellGroupInfo();


    }

    /**
     * Edits whichever network connection is selected in table jTableNetConns
     *
     */
    private void doEditNetConn()
    {
        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        int selectedRow = jTableNetConns.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }
        //projManager.getCurrentProject().networkConnectionsInfo.deleteNetConn(selectedRow);

        String editNetConnName = projManager.getCurrentProject().morphNetworkConnectionsInfo.getNetConnNameAt(selectedRow);


        NetworkConnectionDialog dlg
            = new NetworkConnectionDialog(this,
                                         projManager.getCurrentProject(),
                                         editNetConnName,
                                         projManager.getCurrentProject().morphNetworkConnectionsInfo.getSourceCellGroup(editNetConnName),
                                         projManager.getCurrentProject().morphNetworkConnectionsInfo.getTargetCellGroup(editNetConnName),
                                         projManager.getCurrentProject().morphNetworkConnectionsInfo.getSynapseList(editNetConnName),
                                         projManager.getCurrentProject().morphNetworkConnectionsInfo.getSearchPattern(editNetConnName),
                                         /*projManager.getCurrentProject().simpleNetworkConnectionsInfo.getGrowMode(editNetConnName),*/
                                         projManager.getCurrentProject().morphNetworkConnectionsInfo.getMaxMinLength(editNetConnName),
                                         projManager.getCurrentProject().morphNetworkConnectionsInfo.getConnectivityConditions(editNetConnName),
                                         projManager.getCurrentProject().morphNetworkConnectionsInfo.getAPSpeed(editNetConnName));

        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);

        dlg.pack();
        dlg.setVisible(true);


        if (dlg.cancelled)
        {
            logger.logComment("They've changed their mind...");
            return;
        }

        projManager.getCurrentProject().morphNetworkConnectionsInfo.setSourceCellGroup(editNetConnName, dlg.getSourceCellGroup());
        projManager.getCurrentProject().morphNetworkConnectionsInfo.setTargetCellGroup(editNetConnName, dlg.getTargetCellGroup());
        projManager.getCurrentProject().morphNetworkConnectionsInfo.setSynapseList(editNetConnName, dlg.getSynapticPropsList());
        projManager.getCurrentProject().morphNetworkConnectionsInfo.setSearchPattern(editNetConnName, dlg.getSearchPattern());
        //projManager.getCurrentProject().simpleNetworkConnectionsInfo.setGrowMode(editNetConnName, dlg.getGrowMode());
        projManager.getCurrentProject().morphNetworkConnectionsInfo.setMaxMinLength(editNetConnName, dlg.getMaxMinLength());
        projManager.getCurrentProject().morphNetworkConnectionsInfo.setConnectivityConditions(editNetConnName, dlg.getConnectivityConditions());
        projManager.getCurrentProject().morphNetworkConnectionsInfo.setAPSpeed(editNetConnName, dlg.getAPSpeed());


       /// this.projManager.getCurrentProject().markProjectAsEdited();
      ///  refreshGeneral();

  }


  /**
   * Edits whichever network connection is selected in table jTableAAConns
   **/

  private void doEditVolConn()
  {
      if (projManager.getCurrentProject() == null)
      {
          logger.logError("No project loaded...");
          return;
      }

      int selectedRow = jTableAAConns.getSelectedRow();

      if (selectedRow < 0)
      {
          logger.logComment("No row selected...");
          return;
      }

      String editNetConnName = projManager.getCurrentProject().volBasedConnsInfo.getConnNameAt(selectedRow);


      VolBasedConnDialog dlg
          = new VolBasedConnDialog(this,
                                       projManager.getCurrentProject(),
                                       editNetConnName,
                                       projManager.getCurrentProject().volBasedConnsInfo.getSourceCellGroup(editNetConnName),
                                       projManager.getCurrentProject().volBasedConnsInfo.getTargetCellGroup(editNetConnName),
                                       projManager.getCurrentProject().volBasedConnsInfo.getSynapseList(editNetConnName),
                                       projManager.getCurrentProject().volBasedConnsInfo.getSourceConnRegions(editNetConnName),
                                       projManager.getCurrentProject().volBasedConnsInfo.getConnectivityConditions(editNetConnName),
                                       projManager.getCurrentProject().volBasedConnsInfo.getAPSpeed(editNetConnName),
                                       projManager.getCurrentProject().volBasedConnsInfo.getInhomogenousExp(editNetConnName));


      Dimension dlgSize = dlg.getPreferredSize();
      Dimension frmSize = getSize();
      Point loc = getLocation();
      dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                      (frmSize.height - dlgSize.height) / 2 + loc.y);
      dlg.setModal(true);

      dlg.pack();
      dlg.setVisible(true);


      if (dlg.cancelled)
      {
          logger.logComment("They've changed their mind...");
          return;
      }

      projManager.getCurrentProject().volBasedConnsInfo.setSourceCellGroup(editNetConnName, dlg.getSourceCellGroup());
      projManager.getCurrentProject().volBasedConnsInfo.setTargetCellGroup(editNetConnName, dlg.getTargetCellGroup());
      projManager.getCurrentProject().volBasedConnsInfo.setSynapseProperties(editNetConnName, dlg.getSynapticProperties());
      projManager.getCurrentProject().volBasedConnsInfo.setSourceConnRegions(editNetConnName, dlg.getSourceRegions());
      projManager.getCurrentProject().volBasedConnsInfo.setConnectivityConditions(editNetConnName, dlg.getConnectivityConditions());
      projManager.getCurrentProject().volBasedConnsInfo.setAPSpeed(editNetConnName, dlg.getAPSpeed());
      projManager.getCurrentProject().volBasedConnsInfo.setInhomogenousExp(editNetConnName, dlg.getInhomogenousExp().getNiceString());

}

  /**
   * Removes whichever network connection is selected in table jTableNetConns
   *
   */
  private void doRemoveNetConn()
  {
      if (projManager.getCurrentProject() == null)
      {
          logger.logError("No project loaded...");
          return;
      }

      int selectedRow = jTableNetConns.getSelectedRow();

      if (selectedRow < 0)
      {
          logger.logComment("No row selected...");
          return;
      }
      projManager.getCurrentProject().morphNetworkConnectionsInfo.deleteNetConn(selectedRow);
      logger.logComment("Removed row: " + selectedRow);

      projManager.getCurrentProject().simConfigInfo.validateStoredSimConfigs(projManager.getCurrentProject());

      refreshTabNetSettings();
  }


  /**
   * Removes whichever network connection is selected in table jTableAAConns
   **/

  private void doRemoveAANetConn()
  {
      if (projManager.getCurrentProject() == null)
      {
          logger.logError("No project loaded...");
          return;
      }

      int selectedRow = jTableAAConns.getSelectedRow();

      if (selectedRow < 0)
      {
          logger.logComment("No row selected...");
          return;
      }
      projManager.getCurrentProject().volBasedConnsInfo.deleteConn(selectedRow);
      logger.logComment("Removed row: " + selectedRow);

      projManager.getCurrentProject().simConfigInfo.validateStoredSimConfigs(projManager.getCurrentProject());

      refreshTabNetSettings();
  }



      private void doAnalyseCellDensities()
      {
          if (projManager.getCurrentProject() == null)
          {
              logger.logError("No project loaded...");
              return;
          }

          String info =  projManager.getCellDensitiesReport(true);

          logger.logComment("info: "+info);

          SimpleViewer.showString(info,
                        "Information on Cell Densities in project:" + projManager.getCurrentProject().getProjectName(),
                      12,
                      false,
                      true);


      }

      private void doAnalyseNumConns()
      {

        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        String selCellGroup = (String)jComboBoxAnalyseCellGroup.getSelectedItem();
        String selectedNetConn = (String)jComboBoxAnalyseNetConn.getSelectedItem();

        if (selectedNetConn.equals(defaultAnalyseNetConnString) ||
            selCellGroup.equals(defaultAnalyseCellGroupString))
        {
            GuiUtils.showErrorMessage(logger, "Please select the Network Connection whose Cell Group connectivity you would like to analyse", null, this);
            return;
        }

        boolean isSourceCellGroup = selCellGroup.indexOf("source")>0; // usually false
        boolean isTargetCellGroup = selCellGroup.indexOf("target")>0; // usually false
        
        if (selCellGroup.indexOf(" ")>0)
        {
            selCellGroup = selCellGroup.substring(0, selCellGroup.indexOf(" "));
        }
        
        //String thisCellGroup = null;
        String theOtherCellGroup = null;

        if (projManager.getCurrentProject().morphNetworkConnectionsInfo.isValidSimpleNetConn(selectedNetConn))
        {
            String src = projManager.getCurrentProject().morphNetworkConnectionsInfo.getSourceCellGroup(selectedNetConn);
            String tgt = projManager.getCurrentProject().morphNetworkConnectionsInfo.getTargetCellGroup(selectedNetConn);

            if (!isSourceCellGroup && !isTargetCellGroup)
                isSourceCellGroup = selCellGroup.indexOf(src)>=0;
            if (!isTargetCellGroup && !isSourceCellGroup)            
                isTargetCellGroup = selCellGroup.indexOf(tgt)>=0;

             if (isSourceCellGroup) theOtherCellGroup = tgt;
             else theOtherCellGroup = src;
        }
        else if (projManager.getCurrentProject().volBasedConnsInfo.isValidVolBasedConn(selectedNetConn))
        {
            String src = projManager.getCurrentProject().volBasedConnsInfo.getSourceCellGroup(selectedNetConn);
            String tgt = projManager.getCurrentProject().volBasedConnsInfo.getTargetCellGroup(selectedNetConn);

            if (!isSourceCellGroup && !isTargetCellGroup)
                isSourceCellGroup = selCellGroup.indexOf(src)>=0;
            if (!isTargetCellGroup && !isSourceCellGroup)            
                isTargetCellGroup = selCellGroup.indexOf(tgt)>=0;

            if (isSourceCellGroup) theOtherCellGroup = tgt;
            else theOtherCellGroup = src;

        }

        if (!isSourceCellGroup && !isTargetCellGroup)
        {
            GuiUtils.showErrorMessage(logger, "The cell group " +
                                      selCellGroup
                                      + " is not involved in Network Connection "
                                      + selectedNetConn, null, this);
            return;
        }

        //ArrayList<SingleSynapticConnection> netConns = projManager.getCurrentProject().generatedNetworkConnections.getSynapticConnections(selectedNetConn);
        
        int[][] mx = projManager.getCurrentProject().generatedNetworkConnections.getConnectionMatrix(selectedNetConn, projManager.getCurrentProject());

        String desc = "No. of conns on "
                                          + selCellGroup + " in "
                                          + selectedNetConn;

        PlotterFrame frame = PlotManager.getPlotterFrame(desc);
        String dirInfo = selCellGroup + " to "+theOtherCellGroup;
        
        if (selCellGroup.equals(theOtherCellGroup))
        {
            if (isSourceCellGroup)
                dirInfo = selCellGroup + " (source) to "+theOtherCellGroup+" (target)";
            else if (isTargetCellGroup)
                dirInfo = selCellGroup + " (target) to "+theOtherCellGroup+" (source)";
        }

        DataSet dataSet1 = new DataSet(dirInfo +" in "+selectedNetConn,
                                      desc, "", "", "Cell index", "Number of conns");
        
        DataSet dataSet2 = new DataSet(dirInfo +" (only num conns to UNIQUE cells) in "+selectedNetConn,
                                      desc, "", "", "Cell index", "Number of unique conns");

        dataSet1.setGraphFormat(PlotCanvas.USE_BARCHART_FOR_PLOT);
        dataSet2.setGraphFormat(PlotCanvas.USE_BARCHART_FOR_PLOT);

        frame.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW);

        int numInCellGroup = projManager.getCurrentProject().generatedCellPositions.getNumberInCellGroup(selCellGroup);

        int[] numberConnections = new int[numInCellGroup];
        int[] numberUniqueConnections = new int[numInCellGroup];
        
        for(int i =0;i<mx.length;i++)
        {
            for(int j =0;j<mx[i].length;j++)
            {
                int totConns = mx[i][j];
                //System.out.println("x(i,j) = x("+i+","+j+") = "+mx[i][j]);
                if (isSourceCellGroup)
                {
                    numberConnections[i] = numberConnections[i]+ totConns;
                    if (totConns>0)
                    numberUniqueConnections[i]++;
                }
                else if (isTargetCellGroup)
                {
                    numberConnections[j] = numberConnections[j]+ totConns;
                    if (totConns>0)
                    numberUniqueConnections[j]++;
                }
                
            }
        }
        
/*
        for (int i = 0; i < netConns.size(); i++)
        {
            SingleSynapticConnection oneConn = netConns.get(i);

            if (isSourceCellGroup)
            {
                // add 1 for the entry correspondign to the cell number of this single conn...
                  numberConnections[oneConn.sourceEndPoint.cellNumber]++;
            }
            else if (isTargetCellGroup)
            {
                // add 1 for the entry correspondign to the cell number of this single conn...
                  numberConnections[oneConn.targetEndPoint.cellNumber]++;
            }
        }*/
        
        
        
        for (int i = 0; i < numInCellGroup; i++)
        {
            dataSet1.addPoint(i, numberConnections[i]);
            dataSet2.addPoint(i, numberUniqueConnections[i]);
        }

        frame.addDataSet(dataSet1);
        frame.addDataSet(dataSet2);
        frame.repaint();



    }


    /**
     * Sets buttons and menu items as enabled when a new project is loaded
     *
     */

    //private void enableItemsDueToprojManager.getCurrentProject()()
   // {




    //}

    /**
     * Refreshes the tabs, frame title, etc. when major changes are made, e.g.
     * when a new project is loaded
     *
     */

    protected final void refreshAll()
    {
        logger.logComment("----------------    *  Refreshing all  *    ----------------");
        int tabCurrentlySelected = jTabbedPaneMain.getSelectedIndex();

        String nameSelected = jTabbedPaneMain.getTitleAt(tabCurrentlySelected);

        if (!nameSelected.equals(this.VISUALISATION_TAB))
        {
            doDestroy3D();
        }

        if (projManager.getCurrentProject()!=null)
        {
            projManager.getCurrentProject().simConfigInfo.validateStoredSimConfigs(projManager.getCurrentProject());
        }

        this.refreshGeneral();
        this.refreshMenusAndToolbars();
        this.refreshTabProjectInfo();
        this.refreshTabRegionsInfo();
        this.refreshTabCellTypes();
        this.refreshTabCellGroupInfo();
        this.refreshTabCellMechanisms();
        this.refreshTabNetSettings();
        this.refreshTabGenerate();
        this.refreshTabExport();
        this.refreshTabNeuron();
        this.refreshTabGenesis();
        this.refreshTabPsics();
        this.refreshTabPynn();
        this.refreshTab3D();
        logger.logComment("----------------    *  Done refreshing all  *    ----------------");

    }

    /**
     * Refreshes the frame title, etc.
     *
     */

    private void refreshGeneral()
    {
        logger.logComment("> Refreshing the general panel of the application...");
        StringBuffer mainFrameTitle = new StringBuffer();

        if (projManager.getCurrentProject() != null &&
            this.projManager.getCurrentProject().getProjectStatus() != Project.PROJECT_NOT_INITIALISED)
        {
            mainFrameTitle.append("neuroConstruct v"+ GeneralProperties.getVersionNumber());


            try
            {
                mainFrameTitle = mainFrameTitle.append(" - " + projManager.getCurrentProject().getProjectFullFileName());
            }
            catch (NoProjectLoadedException ex)
            {
                logger.logError("Problem getting file name", ex);
                return;
            }
            if (projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_EDITED_NOT_SAVED)
            {
                mainFrameTitle = mainFrameTitle.append("*");
            }
        }
        else
        {
            mainFrameTitle.append("-- No neuroConstruct project loaded --");
        }

        this.setTitle(mainFrameTitle.toString());
    }

    private void addStandardFileMenuItems()
    {
        jMenuFile.add(jMenuItemNewProject);
        jMenuFile.add(jMenuItemFileOpen);

        jMenuFile.add(jMenuItemSaveProject);

        jMenuFile.add(jMenuItemCloseProject);
        jMenuFile.addSeparator();
        jMenuFile.add(jMenuItemCopyProject);
        jMenuFile.add(jMenuItemZipUp);
        jMenuFile.add(jMenuItemUnzipProject);
        jMenuFile.addSeparator();
        jMenuFile.add(jMenuItemImportLevel123);
        jMenuFile.addSeparator();

        /*
        jMenuFile.add(jMenuExamples);

        jMenuExamples.removeAll();
        
        File[] exProjs = ProjectStructure.getnCExamplesDir().listFiles();

        exProjs = GeneralUtils.reorderAlphabetically(exProjs, true);

        for(File ex: exProjs)
        {
            File projFile = ProjectStructure.findProjectFile(ex);

            if (projFile!=null && projFile.exists())
            {
                JMenuItem jMenuRecentFileItem = new JMenuItem();
                jMenuRecentFileItem.setText(projFile.getAbsolutePath());
                jMenuExamples.add(jMenuRecentFileItem);

                jMenuRecentFileItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        jMenuRecentFile_actionPerformed(e);
                    }
                });
            }
        }*/
        /*
        jMenuFile.add(jMenuModels);

        jMenuModels.removeAll();

        File[] modProjs = ProjectStructure.getnCModelsDir().listFiles();
        for(File ex: modProjs)
        {
            File projFile = ProjectStructure.findProjectFile(ex);

            if (projFile!=null && projFile.exists())
            {
                JMenuItem jMenuRecentFileItem = new JMenuItem();
                jMenuRecentFileItem.setText(projFile.getAbsolutePath());
                jMenuModels.add(jMenuRecentFileItem);

                jMenuRecentFileItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        jMenuRecentFile_actionPerformed(e);
                    }
                });
            }
        }
         
        jMenuFile.addSeparator();
         */


        if (GeneralUtils.includeOsbProjects())
        {
            jMenuFile.add(jMenuOsbModels);

            jMenuOsbModels.removeAll();

            ArrayList<File> osbProjs = new ArrayList<File>();
            ProjectStructure.findProjectFile(ProjectStructure.getOsbProjsDir(), osbProjs);

            HashMap<String, JMenu> menu1 = new HashMap<String, JMenu>();
            HashMap<String, JMenu> menu2 = new HashMap<String, JMenu>();
            
            for(File projFile: osbProjs)
            {
                    //File projFile = ProjectStructure.findProjectFile(ex, true);
                    logger.logComment("Found proj file: "+projFile);

                    if (projFile!=null && projFile.exists())
                    {
                        JMenuItem jMenuRecentFileItem = new JMenuItem();

                        jMenuRecentFileItem.addActionListener(new ActionListener()
                        {
                            public void actionPerformed(ActionEvent e)
                            {
                                jMenuRecentFile_actionPerformed(e);
                            }
                        });
                        jMenuRecentFileItem.setToolTipText(projFile.getAbsolutePath());

                        String prefName = projFile.getName();
                        File tempFile = new File(projFile.getParent());
                        //System.out.println("----tempFile: "+tempFile);
                        if (tempFile.getName().equals("neuroConstruct")) {
                            tempFile = tempFile.getParentFile();
                            prefName = tempFile.getName();
                        }
                        //System.out.println("tempFile: "+tempFile);

                        jMenuRecentFileItem.setText(prefName);

                        if (tempFile.equals(ProjectStructure.getOsbProjsDir()))
                        {
                            jMenuOsbModels.add(jMenuRecentFileItem);

                        }
                        else
                        {
                            String menu1Name = tempFile.getParentFile().getName();
                            String menu2Name = tempFile.getParentFile().getParentFile().getName();
                            String path = menu2Name+"/"+menu1Name;
                            
                            if (!menu2.containsKey(menu2Name)) {
                                JMenu jMenu2 = new JMenu(betterName(menu2Name));
                                menu2.put(menu2Name, jMenu2);
                                jMenuOsbModels.add(jMenu2);
                            }
                            JMenu jMenu2 = menu2.get(menu2Name);
                            if (!menu1.containsKey(path)) {
                                JMenu jMenu1 = new JMenu(betterName(menu1Name));
                                menu1.put(path, jMenu1);
                                jMenu2.add(jMenu1);
                            }
                            JMenu jMenu1 = menu1.get(path);

                            jMenu1.add(jMenuRecentFileItem);

                        }


                    }
                
            }
        }

        jMenuFile.addSeparator();
    
        
    }

    /*
     * Improves name of folders in OSB
     */
    private String betterName(String dirName)
    {
        String[] words = dirName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word: words)
        {
            String caps = word.substring(0, 1).toUpperCase()+ word.substring(1);
            if (word.startsWith("neuroConstruct"))
                caps = "neuroConstruct " + word.substring(14);
            sb.append(caps+" ");
        }
        return sb.toString();
    }

    /**
     * Refreshes the menus
     *
     */
    private void refreshMenusAndToolbars()
    {
        logger.logComment("> Refreshing the menus...");

        recentFiles.printDetails();

        String[] recentFileList = recentFiles.getFileNames();

        if(recentFileList.length==0)
        {
            logger.logComment("No recent files found...");
            return;
        }
        
        jMenuFile.removeAll();
        
        addStandardFileMenuItems();


        for (int i = 0; i < recentFileList.length; i++)
        {
            JMenuItem jMenuRecentFileItem = new JMenuItem();
            jMenuRecentFileItem.setText(recentFileList[i]);
            jMenuFile.add(jMenuRecentFileItem);

            jMenuRecentFileItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    jMenuRecentFile_actionPerformed(e);
                }
            });

        }

        jMenuFile.addSeparator();
        jMenuFile.add(jMenuFileExit);

        if (projManager.getCurrentProject() == null ||
            projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
        {
            this.jButtonPreferences.setEnabled(false);
            this.jButtonSaveProject.setEnabled(false);
            this.jButtonValidate.setEnabled(false);
            this.jButtonCloseProject.setEnabled(false);
            this.jMenuItemProjProperties.setEnabled(false);
            this.jMenuItemViewProjSource.setEnabled(false);

            jMenuItemCopyProject.setEnabled(false);
            this.jMenuItemSaveProject.setEnabled(false);
            this.jMenuItemCloseProject.setEnabled(false);
            this.jMenuItemZipUp.setEnabled(false);

            this.jMenuProject.setEnabled(false);

        }
        else
        {
            this.jMenuItemSaveProject.setEnabled(true);
            this.jMenuItemZipUp.setEnabled(true);
            this.jMenuItemUnzipProject.setEnabled(true);
            this.jMenuItemImportLevel123.setEnabled(true);

            jMenuItemCopyProject.setEnabled(true);

            this.jMenuItemCloseProject.setEnabled(true);
            this.jMenuItemProjProperties.setEnabled(true);
            this.jMenuItemViewProjSource.setEnabled(true);

            this.jButtonPreferences.setEnabled(true);
            this.jButtonValidate.setEnabled(true);
            this.jButtonSaveProject.setEnabled(true);
            this.jButtonCloseProject.setEnabled(true);

            this.jMenuProject.setEnabled(true);

            if (GeneralProperties.getLogFilePrintToScreenPolicy())
            {
                jButtonToggleConsoleOut.setIcon(imageConsoleOut);
            }
            else
            {
                jButtonToggleConsoleOut.setIcon(imageNoConsoleOut);
            }

        }

        updateConsoleOutState();
    }


    boolean updatingTabProjectInfo = false;

    /**
     * Refreshes the tab related to general project info
     *
     */

    private void refreshTabProjectInfo()
    {
        logger.logComment("> Refreshing the Tab for project info...");

        if (initialisingProject && projManager.getCurrentProject() != null)
        {
            this.jTextAreaProjDescription.setText(projManager.getCurrentProject().getProjectDescription());
            jTextAreaProjDescription.setCaretPosition(0);
        }

        if (projManager.getCurrentProject() == null ||
            projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
        {
            this.jPanelCellClicks.removeAll();
            this.jPanelSimConfigClicks.removeAll();
            this.jPanelCellGroupClicks.removeAll();

            logger.logComment("Removed all...");
            jPanelMainInfo.repaint();


            this.jLabelMainNumCells.setEnabled(false);
            this.jLabelSimConfigs.setEnabled(false);
            this.jTextAreaProjDescription.setText(welcomeText);

            this.jLabelProjDescription.setEnabled(false);
            this.jTextFieldProjName.setText("");
            this.jLabelName.setEnabled(false);

            this.jLabelNumCellGroups.setEnabled(false);
            this.jLabelProjFileVersion.setEnabled(false);
            this.jLabelMainLastModified.setEnabled(false);

            this.jTextFieldProjFileVersion.setText("");
            this.jTextFieldMainLastModified.setText("");


            this.jTextAreaProjDescription.setEditable(false);
            Button tempButton = new Button();
            tempButton.setEnabled(false);
            jTextAreaProjDescription.setBackground(tempButton.getBackground());
            jTextAreaProjDescription.setForeground(Color.darkGray);
            this.jScrollPaneProjDesc.setEnabled(false);

        }
        else
        {
            updatingTabProjectInfo = true;
            this.jLabelMainNumCells.setEnabled(true);
            this.jLabelSimConfigs.setEnabled(true);
            this.jLabelProjDescription.setEnabled(true);
            this.jLabelName.setEnabled(true);
            this.jLabelNumCellGroups.setEnabled(true);
            this.jLabelProjFileVersion.setEnabled(true);
            this.jLabelMainLastModified.setEnabled(true);

            this.jTextAreaProjDescription.setEditable(true);

            this.jTextFieldProjName.setText(projManager.getCurrentProject().getProjectName());

            this.jTextAreaProjDescription.setText(projManager.getCurrentProject().getProjectDescription());

            //this.jTextFieldNumRegions.setText(this.projManager.getCurrentProject().regionsInfo.getRowCount() + "");
            //this.jTextFieldNumCells.setText(this.projManager.getCurrentProject().cellManager.getNumberCellTypes() + "");

            //this.jTextFieldNumCellGroups.setText(this.projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups() + "");


            jPanelSimConfigClicks.removeAll();
            int leftToDo = 6;
            
            for (SimConfig simConfig: projManager.getCurrentProject().simConfigInfo.getAllSimConfigs())
            {
                String linkText = simConfig.getName();
                String tip = simConfig.getDescription();
                
                final String seeAll = "(See all "+projManager.getCurrentProject().simConfigInfo.getNumSimConfigs()+" Sim Configs...)";
                
                if (leftToDo == 0)
                {
                    linkText = seeAll;
                    tip = "Click to go to Generate tab with list of all Simulation Configurations";
                }

                if (leftToDo>=0)
                {
                    ClickLink cl = new ClickLink(linkText, tip);
    
                    this.jPanelSimConfigClicks.add(cl);
    
    
                    cl.addMouseListener(new MouseListener()
                    {
                        //String cellGroup = cellGroup;
                        public void mouseClicked(MouseEvent e)
                        {
    
                            jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(GENERATE_TAB));
    
                            String clicked = e.getComponent().getName();
                            
                            if (!clicked.equals(seeAll))
                            {
                                jComboBoxSimConfig.setSelectedItem(clicked);
                                doGenerate();   
                            }
                        };
    
                        public void mousePressed(MouseEvent e) {};
    
                        public void mouseReleased(MouseEvent e) {};
    
                        public void mouseEntered(MouseEvent e) {};
    
                        public void mouseExited(MouseEvent e) {};
    
                    });
                    leftToDo--;
                }
            }



            jPanelCellClicks.removeAll();

            leftToDo = 9;
            for (Cell cell: projManager.getCurrentProject().cellManager.getAllCells())
            {

            	String linkText = cell.getInstanceName();
            	String tip = cell.getCellDescription();
            	
            	if (leftToDo == 0)
            	{
            		linkText = "(See all "+projManager.getCurrentProject().cellManager.getNumberCellTypes()+" Cells...)";
            		tip = "Click to view list of all Cells";
            	}
            	
            	if (leftToDo>=0)
            	{
	                ClickLink cl = new ClickLink(linkText, tip);
	                this.jPanelCellClicks.add(cl);
	
	
	                cl.addMouseListener(new MouseListener()
	                {
	                    //String cellGroup = cellGroup;
	                    public void mouseClicked(MouseEvent e)
	                    {
	
	                        jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(CELL_TYPES_TAB));
	
	                        String clicked = e.getComponent().getName();
	
	                        Cell clickedCell = projManager.getCurrentProject().cellManager.getCell(clicked);
	
	                        jComboBoxCellTypes.setSelectedItem(clickedCell);
	
	                      //  logger.logComment("Name: "+ clicked, true);
	                      //  int index = projManager.getCurrentProject().cellGroupsInfo.getAllCellGroupNames().indexOf(clicked);
	                      //  jTableCellGroups.setRowSelectionInterval(index, index);
	
	                        //System.out.println("mouseClicked");
	                        //setText("Ouch");
	                    };
	
	                    public void mousePressed(MouseEvent e)
	                    {};
	
	                    public void mouseReleased(MouseEvent e)
	                    {};
	
	                    public void mouseEntered(MouseEvent e)
	                    {};
	
	                    public void mouseExited(MouseEvent e)
	                    {};
	
	                });
            	}
            	leftToDo--;
            }

            jPanelCellGroupClicks.removeAll();
            leftToDo = 20;
            
            for (String cellGroup: projManager.getCurrentProject().cellGroupsInfo.getAllCellGroupNames())
            {
            	String linkText = cellGroup;
            	String tip = "Cell Group: "+cellGroup+"<br>"+
                "Cell Type: "+projManager.getCurrentProject().cellGroupsInfo.getCellType(cellGroup);
            	
            	if (leftToDo == 0)
            	{
            		linkText = "(See all "+projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups()+" Cell Groups...)";
            		tip = "Click to view list of Cell Groups";
            	}
            	
            	if (leftToDo >= 0)
            	{
            		
            		ClickLink cl = new ClickLink(linkText, tip);
	                this.jPanelCellGroupClicks.add(cl);
	
	
	                cl.addMouseListener(new MouseListener()
	                {
	                    //String cellGroup = cellGroup;
	                    public void mouseClicked(MouseEvent e)
	                    {
	
	                        jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(CELL_GROUPS_TAB));
	
	                        String clicked = e.getComponent().getName();
	                        logger.logComment("Name: "+ clicked);
	                        int index = projManager.getCurrentProject().cellGroupsInfo.getAllCellGroupNames().indexOf(clicked);
	                        jTableCellGroups.setRowSelectionInterval(index, index);
	
	                        //System.out.println("mouseClicked");
	                        //setText("Ouch");
	                    };
	
	                    public void mousePressed(MouseEvent e)
	                    {};
	
	                    public void mouseReleased(MouseEvent e)
	                    {};
	
	                    public void mouseEntered(MouseEvent e)
	                    {};
	
	                    public void mouseExited(MouseEvent e)
	                    {};
	
	                });
            	}
                
            	leftToDo--;
            }



            this.jTextFieldProjFileVersion.setText(projManager.getCurrentProject().getProjectFileVersion());

            long timeModified = projManager.getCurrentProject().getProjectFile().lastModified();

            SimpleDateFormat formatter = new SimpleDateFormat("H:mm:ss, EEEE MMMM d, yyyy");

            java.util.Date modified = new java.util.Date(timeModified);

            this.jTextFieldMainLastModified.setText(formatter.format(modified));

            this.jTextAreaProjDescription.setEditable(true);

            jTextAreaProjDescription.setBackground((new JTextArea()).getBackground());
            jTextAreaProjDescription.setForeground((new JTextArea()).getForeground());

            this.jScrollPaneProjDesc.setEnabled(true);

            updatingTabProjectInfo = false;
        }
    }

    /**
     * Refreshes the tab related to regions info
     *
     */

    private void refreshTabRegionsInfo()
    {
        logger.logComment("> Refreshing the Tab for region info...");
        try
        {
            if (projManager.getCurrentProject() == null || projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
            {
                this.jButtonRegionNew.setEnabled(false);
                this.jButtonRegionsEdit.setEnabled(false);
                jButtonRegionRemove.setEnabled(false);
                //jTextFieldDepth.setText("");
                //jTextFieldWidth.setText("");

                jTable3DRegions.setModel(new RegionsInfo());
            }
            else
            {
                this.jButtonRegionNew.setEnabled(true);
                //jTable3DRegions = new JTable();
                
                /*
                 * projManager.getCurrentProject().cellMechanismInfo)
            {
                @Override
                public String getToolTipText(MouseEvent e) {
                  String tip = null;
                  java.awt.Point p = e.getPoint();
                  int rowIndex = rowAtPoint(p);
                  //int colIndex = columnAtPoint(p);
                  
                  //int realColumnIndex = convertColumnIndexToModel(colIndex);

                  //if (realColumnIndex==CellMechanismInfo.COL_NUM_DESC)
                  {
                      String desc = projManager.getCurrentProject().cellMechanismInfo.getCellMechanismAt(rowIndex).getDescription();
                      tip = "<html><b>"+GeneralUtils.replaceAllTokens(desc, "  ", " ")+"</b></html>";
                  }
                  return tip;
                }
            };
                 */

                jTable3DRegions.setModel(projManager.getCurrentProject().regionsInfo);

                if (projManager.getCurrentProject().regionsInfo.getNumberRegions() > 0)
                {
                    jButtonRegionRemove.setEnabled(true);
                    this.jButtonRegionsEdit.setEnabled(true);
                }
                else
                {
                    jButtonRegionRemove.setEnabled(false);
                    this.jButtonRegionsEdit.setEnabled(false);
                }
                //ArrayList<String> usedRegions = projManager.getCurrentProject().cellGroupsInfo.getUsedRegionNames();
                //for(String nextRegion: projManager.getCurrentProject().regionsInfo.getAllRegionNames())
                //{
                    //jTable3DRegions.set
                //}
                
                /*
                if (jTextFieldWidth.getText().equals(""))
                {
                    jTextFieldWidth.setText(this.projManager.getCurrentProject().regionsInfo.getRegionWidth() + "");
                }
                if (jTextFieldDepth.getText().equals(""))
                {
                    jTextFieldDepth.setText(this.projManager.getCurrentProject().regionsInfo.getRegionDepth() + "");
                }*/

            }
            this.jTable3DRegions.validate();
        }
        catch (java.lang.IllegalStateException ex)
        {
            // This happens when the tab gets updated when a project is closed...
            logger.logComment("Tab being updated whilst project closing down...");
        }
        catch (Exception ex1)
        {
            logger.logError("Error updating Tab Region info", ex1);
        }
    }

    /**
     * Refreshes the tab related to cell group info
     *
     */

    private void refreshTabCellGroupInfo()
    {
        logger.logComment("> Refreshing the Tab for cell group info...");

        if (projManager.getCurrentProject() == null || projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
        {
            this.jButtonCellGroupsNew.setEnabled(false);
            jButtonCellGroupsDelete.setEnabled(false);
            jButtonCellGroupsEdit.setEnabled(false);

            jTableCellGroups.setModel(new CellGroupsInfo());
        }
        else
        {
            this.jButtonCellGroupsNew.setEnabled(true);

            jTableCellGroups.setModel(projManager.getCurrentProject().cellGroupsInfo);

            if (projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups() > 0)
            {
                jButtonCellGroupsDelete.setEnabled(true);
                jButtonCellGroupsEdit.setEnabled(true);
            }
            else
            {
                jButtonCellGroupsDelete.setEnabled(false);
                jButtonCellGroupsEdit.setEnabled(false);
            }
        }
        this.jTable3DRegions.validate();
    }

    /**
     * Refreshes the tab related to cell info
     *
     */
    private void refreshTabCellTypes()
    {
        logger.logComment("> Refreshing the Tab for cell types...");
        int currentlySelected = jComboBoxCellTypes.getSelectedIndex();
        this.jComboBoxCellTypes.removeAllItems();

        if (projManager.getCurrentProject() == null ||
            projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
        {
            this.jComboBoxCellTypes.addItem(cellComboPrompt);
            this.jButtonCellTypeNew.setEnabled(false);
            this.jButtonCellTypeOtherProject.setEnabled(false);
            this.jComboBoxCellTypes.setEnabled(false);
            this.jButtonCellTypeViewCell.setEnabled(false);
            this.jButtonCellTypeViewCellChans.setEnabled(false);
            this.jButtonCellTypeViewCellInfo.setEnabled(false);
            this.jButtonCellTypeEditDesc.setEnabled(false);
            this.jButtonCellTypeBioPhys.setEnabled(false);
            this.jButtonCellTypeDelete.setEnabled(false);
            this.jButtonCellTypeCompare.setEnabled(false);
            this.jButtonCellTypeCopy.setEnabled(false);
            this.jButtonCellTypesMoveToOrigin.setEnabled(false);
            this.jButtonCellTypesConnect.setEnabled(false);
            this.jButtonCellTypesMakeSimpConn.setEnabled(false);
            return;
        }
        else
        {
            this.jButtonCellTypeNew.setEnabled(true);
            this.jButtonCellTypeOtherProject.setEnabled(true);
            this.jComboBoxCellTypes.setEnabled(true);
            this.jButtonCellTypeViewCell.setEnabled(true);
            this.jButtonCellTypeViewCellChans.setEnabled(true);
            this.jButtonCellTypeViewCellInfo.setEnabled(true);
            this.jButtonCellTypeEditDesc.setEnabled(true);
            this.jButtonCellTypeBioPhys.setEnabled(true);
            this.jButtonCellTypeDelete.setEnabled(true);
            this.jButtonCellTypeCompare.setEnabled(true);
            this.jButtonCellTypeCopy.setEnabled(true);
            this.jButtonCellTypesMoveToOrigin.setEnabled(true);
            //////////this.jButtonCellTypesConnect.setEnabled(true);
            /////////this.jButtonCellTypesMakeSimpConn.setEnabled(true);
        }

        try
        {
            Vector<Cell> cells = this.projManager.getCurrentProject().cellManager.getAllCells();

            GeneralUtils.reorderAlphabetically(cells, true);

            for (Cell cell : cells)
            {
                this.jComboBoxCellTypes.addItem(cell);
            }
        }
        catch (Exception ex)
        {
            logger.logError("Error updating Tab for cell types", ex);
        }
        if (jComboBoxCellTypes.getItemCount() == 0)
        {
            this.jComboBoxCellTypes.addItem(cellComboPrompt);
        }

        if (currentlySelected > 0 && jComboBoxCellTypes.getItemCount()>currentlySelected)
        {
            jComboBoxCellTypes.setSelectedIndex(currentlySelected);
        }
    }



    /**
     * Refreshes the tab related to synapses
     *
     */
    private void refreshTabCellMechanisms()
    {
        logger.logComment("> Refreshing the Tab for CellMechanisms...");

        if (projManager.getCurrentProject() == null ||
            projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
        {
            this.jButtonMechanismAbstract.setEnabled(false);
            this.jButtonMechanismEditIt.setEnabled(false);
            this.jButtonMechanismUpdateMaps.setEnabled(false);
            this.jButtonMechanismReloadFile.setEnabled(false);
            this.jButtonMechanismCopy.setEnabled(false);
            this.jButtonCompareMechanism.setEnabled(false);
            this.jButtonMechanismDelete.setEnabled(false);
            jButtonMechanismFileBased.setEnabled(false);
            jButtonMechanismNewCML.setEnabled(false);
            jButtonMechanismTemplateCML.setEnabled(false);

            jTableMechanisms = new JTable((new CellMechanismInfo()));

            jScrollPaneMechanisms.getViewport().removeAll();
            jScrollPaneMechanisms.getViewport().add(jTableMechanisms, null);

        }
        else
        {

            this.jButtonMechanismAbstract.setEnabled(true);
            this.jButtonMechanismEditIt.setEnabled(true);
            this.jButtonMechanismUpdateMaps.setEnabled(true);
            this.jButtonMechanismReloadFile.setEnabled(true);
            this.jButtonMechanismCopy.setEnabled(true);
            this.jButtonCompareMechanism.setEnabled(true);
            this.jButtonMechanismDelete.setEnabled(true);
            jButtonMechanismFileBased.setEnabled(true);
            jButtonMechanismNewCML.setEnabled(true);
            jButtonMechanismTemplateCML.setEnabled(true);
            
        
            jTableMechanisms = new JTable(projManager.getCurrentProject().cellMechanismInfo)
            {
                @Override
                public String getToolTipText(MouseEvent e) {
                  String tip = null;
                  java.awt.Point p = e.getPoint();
                  int rowIndex = rowAtPoint(p);
                  //int colIndex = columnAtPoint(p);
                  
                  //int realColumnIndex = convertColumnIndexToModel(colIndex);

                  //if (realColumnIndex==CellMechanismInfo.COL_NUM_DESC)
                  {
                      String desc = projManager.getCurrentProject().cellMechanismInfo.getCellMechanismAt(rowIndex).getDescription();
                      tip = "<html><b>"+GeneralUtils.replaceAllTokens(desc, "  ", " ")+"</b></html>";
                  }
                  return tip;
                }
            };
            

            jScrollPaneMechanisms.getViewport().add(jTableMechanisms, null);

        }

    }

    /**
     * Refreshes the tab containing the NMODL files
     *

    private void refreshTabNmodl()
    {

        logger.logComment("> Refreshing the Tab for synapses & chan mechs...");

        if (projManager.getCurrentProject() == null ||
            projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
        {
            this.jButtonSynapseAdd.setEnabled(false);
            this.jButtonSynapseEdit.setEnabled(false);
            this.jButtonChanMechAdd.setEnabled(false);
            this.jButtonChanMechEdit.setEnabled(false);

            jTableSynapses.setModel(new SynapticProcessInfo());
            jTableChanMechs.setModel(new ChannelMechanismInfo());
        }
        else
        {
            jTableSynapses.setModel(projManager.getCurrentProject().synapticProcessInfo);
            jTableChanMechs.setModel(projManager.getCurrentProject().channelMechanismInfo);

            projManager.getCurrentProject().synapticProcessInfo.parseDirectory();
            projManager.getCurrentProject().channelMechanismInfo.parseDirectory();

            this.jButtonSynapseAdd.setEnabled(true);
            this.jButtonSynapseEdit.setEnabled(true);
            this.jButtonChanMechAdd.setEnabled(true);
            this.jButtonChanMechEdit.setEnabled(true);

        }

        this.jTableSynapses.validate();
        this.jTableChanMechs.validate();



        TableColumn synNameColumn
            = jTableSynapses.getColumn(jTableSynapses.getColumnName(projManager.getCurrentProject().synapticProcessInfo.
            COL_NUM_NAME));


        synNameColumn.setWidth(10);


    }; */

    /**
     * Refreshes the tab related to network settings
     *
     */
    private void refreshTabNetSettings()
    {
        logger.logComment("> Refreshing the Tab for network settings...");

        if (projManager.getCurrentProject() == null ||
            projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
        {
            jButtonNetConnDelete.setEnabled(false);
            jButtonNetConnEdit.setEnabled(false);
            jButtonNetSetAddNew.setEnabled(false);
            jButtonNetAAAdd.setEnabled(false);
            jButtonNetAADelete.setEnabled(false);
            jButtonNetAAEdit.setEnabled(false);

            jTableNetConns.setModel(new SimpleNetworkConnectionsInfo());
            jTableAAConns.setModel(new ArbourConnectionsInfo());
        }
        else
        {
            jButtonNetSetAddNew.setEnabled(true);

            jButtonNetAAAdd.setEnabled(true);

            jTableNetConns.setModel(projManager.getCurrentProject().morphNetworkConnectionsInfo);
            jTableAAConns.setModel(projManager.getCurrentProject().volBasedConnsInfo);

            logger.logComment("All net conns: "+ projManager.getCurrentProject().morphNetworkConnectionsInfo.getAllSimpleNetConnNames());
            if (projManager.getCurrentProject().morphNetworkConnectionsInfo.getNumSimpleNetConns() > 0)
            {
                jButtonNetConnDelete.setEnabled(true);
                jButtonNetConnEdit.setEnabled(true);
            }
            else
            {
                jButtonNetConnDelete.setEnabled(false);
                jButtonNetConnEdit.setEnabled(false);
            }

            if (projManager.getCurrentProject().volBasedConnsInfo.getNumConns() > 0)
            {
                jButtonNetAAEdit.setEnabled(true);
                jButtonNetAADelete.setEnabled(true);
            }
            else
            {
                jButtonNetAAEdit.setEnabled(false);
                jButtonNetAADelete.setEnabled(false);
            }
        }
    }

    /**
     * Refreshes the tab for generating positions
     *
     */

    private void refreshTabGenerate()
    {
        logger.logComment("> Refreshing the Tab for generating positions...");

        if (projManager.getCurrentProject() == null ||
            projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
        {
            this.jButtonGenerate.setEnabled(false);
            this.jComboBoxSimConfig.setEnabled(false);
            this.jButtonSimConfigEdit.setEnabled(false);
            this.jButtonGenerateSave.setEnabled(false);
            jButtonGenerateLoad.setEnabled(false);
            jRadioButtonNMLSaveHDF5.setEnabled(false);
            jRadioButtonNMLSavePlainText.setEnabled(false);
            jRadioButtonNMLSaveZipped.setEnabled(false);
        
            this.jCheckBoxGenerateExtraNetComments.setEnabled(false);
            this.jEditorPaneGenerateInfo.setText("");
            this.jComboBoxAnalyseCellGroup.setEnabled(false);
            this.jComboBoxAnalyseNetConn.setEnabled(false);
            this.jButtonAnalyseConnLengths.setEnabled(false);
            this.jButtonNetworkFullInfo.setEnabled(false);
            this.jButtonAnalyseNumConns.setEnabled(false);
            this.jButtonAnalyseCellDensities.setEnabled(false);

            jProgressBarGenerate.setValue(0);
            jProgressBarGenerate.setString("Generation progress");

        }
        else
        {
            this.jButtonGenerate.setEnabled(true);
            this.jComboBoxSimConfig.setEnabled(true);
            this.jButtonSimConfigEdit.setEnabled(true);

            this.jButtonGenerateSave.setEnabled(true);
            jButtonGenerateLoad.setEnabled(true);
            jRadioButtonNMLSaveHDF5.setEnabled(true);
            jRadioButtonNMLSavePlainText.setEnabled(true);
            jRadioButtonNMLSaveZipped.setEnabled(true);
        
            jCheckBoxGenerateExtraNetComments.setEnabled(true);
            ArrayList<String> cellGroupNames = projManager.getCurrentProject().cellGroupsInfo.getAllCellGroupNames();
            Vector<String> netConnNames = projManager.getCurrentProject().morphNetworkConnectionsInfo.getAllSimpleNetConnNames();
            Vector<String> moreNetConnNames = projManager.getCurrentProject().volBasedConnsInfo.getAllAAConnNames();

            netConnNames.addAll(moreNetConnNames);

            int selectedSimConfig = jComboBoxSimConfig.getSelectedIndex();
            String selectedSimConfigName = (String)jComboBoxSimConfig.getSelectedItem();
            
            jComboBoxSimConfig.removeAllItems();
            
            ArrayList simConfigs = projManager.getCurrentProject().simConfigInfo.getAllSimConfigNames();
            for (int i = 0; i < simConfigs.size(); i++)
            {
                jComboBoxSimConfig.addItem(simConfigs.get(i));
            }
            if (selectedSimConfig>=0 
                    && jComboBoxSimConfig.getItemCount()>(selectedSimConfig)
                    && ((String)jComboBoxSimConfig.getItemAt(selectedSimConfig)).equals(selectedSimConfigName))
                jComboBoxSimConfig.setSelectedIndex(selectedSimConfig);


            if (cellGroupNames.size() > 0 &&
                projManager.getCurrentProject().generatedCellPositions.getNumberPositionRecords() > 0)
            {
                this.jComboBoxAnalyseCellGroup.setEnabled(true);
                this.jButtonAnalyseCellDensities.setEnabled(true);
                this.jButtonNetworkFullInfo.setEnabled(true);

                if (jComboBoxAnalyseCellGroup.getItemCount()>1 &&
                    jComboBoxAnalyseCellGroup.getSelectedIndex()==0)
                {
                    jComboBoxAnalyseCellGroup.setSelectedIndex(1);
                }

            }
            else
            {
                this.jComboBoxAnalyseCellGroup.setEnabled(false);
                this.jButtonAnalyseCellDensities.setEnabled(false);

            }


            if (netConnNames.size() > 0 &&
                projManager.getCurrentProject().generatedNetworkConnections.getNumberSynapticConnections(GeneratedNetworkConnections.ANY_NETWORK_CONNECTION) > 0)
            {
                this.jComboBoxAnalyseNetConn.setEnabled(true);
                this.jButtonAnalyseConnLengths.setEnabled(true);
                this.jButtonNetworkFullInfo.setEnabled(true);
                this.jButtonAnalyseNumConns.setEnabled(true);

                jComboBoxAnalyseNetConn.removeAllItems();
                jComboBoxAnalyseNetConn.addItem(defaultAnalyseNetConnString);

                Iterator names = projManager.getCurrentProject().generatedNetworkConnections.getNamesNetConnsIter();

                while (names.hasNext())
                {
                    jComboBoxAnalyseNetConn.addItem(names.next());
                }

                if (jComboBoxAnalyseNetConn.getItemCount()>1 &&
                    jComboBoxAnalyseNetConn.getSelectedIndex()==0)
                {
                    jComboBoxAnalyseNetConn.setSelectedIndex(1);
                }


            }
            else
            {
                this.jComboBoxAnalyseNetConn.setEnabled(false);
                this.jButtonAnalyseConnLengths.setEnabled(false);
                this.jButtonAnalyseNumConns.setEnabled(false);
            }
        }
    }

    /**
     * Refreshes the tab related to export formats
     *
     */
    private void refreshTabExport()
    {
        refreshTabInputOutput();
        refreshTabNeuron();
        refreshTabGenesis();
        refreshTabNeuroML();
    }



    /**
     * Refreshes the tab related to saving parameters, stims, etrc.
     *
     */
    private void refreshTabInputOutput()
    {
        logger.logComment("> Refreshing the Tab for Input/Output...");


        if (projManager.getCurrentProject() == null ||
           projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
       {
           jTableSimPlot.setModel(new SimPlotInfo());
           jTableStims.setModel(new ElecInputInfo());

           this.jButtonSimPlotAdd.setEnabled(false);
           this.jButtonSimPlotDelete.setEnabled(false);
           this.jButtonSimPlotEdit.setEnabled(false);
           this.jButtonSimPlotCopy.setEnabled(false);
           

           this.jButtonSimStimAdd.setEnabled(false);
           this.jButtonSimStimDelete.setEnabled(false);
           this.jButtonSimStimEdit.setEnabled(false);
           this.jButtonSimStimCopy.setEnabled(false);

       }
       else
       {
           jTableSimPlot.setModel(projManager.getCurrentProject().simPlotInfo);
           jTableStims.setModel(projManager.getCurrentProject().elecInputInfo);

           TableColumn nextColumn = jTableStims.getColumnModel().getColumn(ElecInputInfo.COL_NUM_CELL_INFO);
           nextColumn.setMinWidth(350);


           TableColumn nextColumn2 = jTableSimPlot.getColumnModel().getColumn(SimPlotInfo.COL_NUM_VALUE);
           nextColumn2.setMinWidth(220);
           

           this.jButtonSimPlotAdd.setEnabled(true);
           this.jButtonSimPlotDelete.setEnabled(true);
           this.jButtonSimPlotEdit.setEnabled(true);
           this.jButtonSimPlotCopy.setEnabled(true);
           

           this.jButtonSimStimAdd.setEnabled(true);
           this.jButtonSimStimDelete.setEnabled(true);
           this.jButtonSimStimEdit.setEnabled(true);
           this.jButtonSimStimCopy.setEnabled(true);

       }

    }

    

    /**
     * Refreshes the tab related to Pynn
     *
     */
    private void refreshTabPynn()
    {
        logger.logComment("> Refreshing the Tab for Pynn...");
        
        if (projManager.getCurrentProject() == null ||
            projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
        {
            jButtonPynnGenerate.setEnabled(false);
            jButtonPynnRun.setEnabled(false);
            jButtonPynnView.setEnabled(false);
            jComboBoxPynnFileList.setEnabled(false);
            jCheckBoxPynnLineNums.setEnabled(false);
            
            
            jComboBoxPynnFileList.removeAllItems();
            jComboBoxPynnFileList.addItem(defaultPyNNFilesText);
        }
        else
        {
            jButtonPynnGenerate.setEnabled(true);
        }

    }

    

    /**
     * Refreshes the tab related to PSICS
     *
     */
    private void refreshTabPsics()
    {
        logger.logComment("> Refreshing the Tab for PSICS...");
        
        if (projManager.getCurrentProject() == null ||
            projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
        {
            jButtonPsicsGenerate.setEnabled(false);
            jButtonPsicsRun.setEnabled(false);
            jButtonPsicsView.setEnabled(false);
            jComboBoxPsicsFileList.setEnabled(false);
            jCheckBoxPsicsLineNums.setEnabled(false);
            jComboBoxPsicsFileList.removeAllItems();
            jComboBoxPsicsFileList.addItem(defaultPsicsFilesText);
        }
        else
        {
            jButtonPsicsGenerate.setEnabled(true);

            if (initialisingProject)
            {
                jCheckBoxPsicsShowHtml.setSelected(projManager.getCurrentProject().psicsSettings.isShowHtmlSummary());
                jCheckBoxPsicsShowPlot.setSelected(projManager.getCurrentProject().psicsSettings.isShowPlotSummary());
                jCheckBoxPsicsConsole.setSelected(projManager.getCurrentProject().psicsSettings.isShowConsole());

                jTextFieldPsicsSpatDisc.setText(projManager.getCurrentProject().psicsSettings.getSpatialDiscretisation()+"");
                jTextFieldPsicsSingleCond.setText(projManager.getCurrentProject().psicsSettings.getSingleChannelCond()+"");
            }
        }

    }

    /**
     * Refreshes the tab related to GENESIS
     *
     */
    private void refreshTabGenesis()
    {
        logger.logComment("> Refreshing the Tab for GENESIS...");



        if (projManager.getCurrentProject() == null ||
           projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
       {
           jButtonGenesisGenerate.setEnabled(false);
           jButtonGenerateStop.setEnabled(false);
           this.jTextAreaSimConfigDesc.setEnabled(false);
           jButtonGenesisView.setEnabled(false);
           jButtonGenesisNumMethod.setEnabled(false);
           jLabelGenesisNumMethod.setEnabled(false);
           jButtonGenesisRun.setEnabled(false);
           jComboBoxGenesisFiles.setEnabled(false);


           jCheckBoxGenesisSymmetric.setEnabled(false);
           jCheckBoxGenesisComments.setEnabled(false);
           this.jRadioButtonGenesisAllGUI.setEnabled(false);
           this.jRadioButtonGenesisNoConsole.setEnabled(false);
           this.jRadioButtonGenesisNoPlots.setEnabled(false);
           //////////jCheckBoxGenesisVoltPlot.setEnabled(false);
           jCheckBoxGenesisShapePlot.setEnabled(false);

           jLabelGenesisMain.setEnabled(false);
           jRadioButtonGenesisPhy.setEnabled(false);
           jRadioButtonGenesisSI.setEnabled(false);

           jComboBoxGenesisComps.setEnabled(false);
           jLabelGenesisCompsDesc.setEnabled(false);
           jCheckBoxGenesisCopySimFiles.setEnabled(false);
           jCheckBoxGenesisMooseMode.setEnabled(false);
           jCheckBoxGenesisReload.setEnabled(false);
           jLabelGenesisReload.setEnabled(false);
           jTextFieldGenesisReload.setEnabled(false);

           jTextFieldGenesisAbsRefract.setEnabled(false);
           jLabelGenesisAbsRefract.setEnabled(false);
           jCheckBoxGenesisAbsRefract.setEnabled(false);

           Button b = new Button();
           b.setEnabled(false);



           jComboBoxGenesisFiles.removeAllItems();
           jComboBoxGenesisFiles.addItem(defaultGenesisFilesText);


           jComboBoxGenesisExtraBlocks.setEnabled(false);
           jComboBoxGenesisExtraBlocks.setSelectedIndex(0);



           this.jTextAreaGenesisBlockDesc.setText("");
           this.jTextAreaGenesisBlock.setText("");
           this.jTextAreaGenesisBlockDesc.setEnabled(false);
           this.jTextAreaGenesisBlock.setEnabled(false);


           this.jTextAreaGenesisBlock.setBackground(b.getBackground());
           this.jTextAreaGenesisBlockDesc.setBackground(b.getBackground());

            this.jLabelGenesisRandomGenDesc.setEnabled(false);
            this.jTextFieldGenesisRandomGen.setEnabled(false);
            this.jCheckBoxGenesisRandomGen.setEnabled(false);
            this.jCheckBoxGenesisLineNums.setEnabled(false);
       }
       else
       {
           jButtonGenesisGenerate.setEnabled(true);
           jButtonGenerateStop.setEnabled(true);


           jComboBoxGenesisComps.setEnabled(true);
           jLabelGenesisCompsDesc.setEnabled(true);
           jCheckBoxGenesisCopySimFiles.setEnabled(true);
           jCheckBoxGenesisMooseMode.setEnabled(true);
           jCheckBoxGenesisReload.setEnabled(true);
           jLabelGenesisReload.setEnabled(true);
           //jTextFieldGenesisReload.setEnabled(true);


           //jTextFieldGenesisAbsRefract.setEnabled(true);
           jLabelGenesisAbsRefract.setEnabled(true);
           jCheckBoxGenesisAbsRefract.setEnabled(true);


           jCheckBoxGenesisSymmetric.setEnabled(true);
           jCheckBoxGenesisComments.setEnabled(true);
           this.jRadioButtonGenesisAllGUI.setEnabled(true);
           this.jRadioButtonGenesisNoConsole.setEnabled(true);
           this.jRadioButtonGenesisNoPlots.setEnabled(true);
           this.jTextAreaSimConfigDesc.setEnabled(true);

           
           jCheckBoxGenesisShapePlot.setEnabled(true);

           jButtonGenesisNumMethod.setEnabled(true);
           jLabelGenesisNumMethod.setEnabled(true);


           jLabelGenesisMain.setEnabled(true);
           jRadioButtonGenesisPhy.setEnabled(true);
           jRadioButtonGenesisSI.setEnabled(true);

           jComboBoxGenesisExtraBlocks.setEnabled(true);

           this.jTextAreaGenesisBlockDesc.setEnabled(true);
           this.jTextAreaGenesisBlock.setEnabled(true);

           this.jLabelGenesisRandomGenDesc.setEnabled(true);
           this.jTextFieldGenesisRandomGen.setEnabled(true);
            this.jCheckBoxGenesisRandomGen.setEnabled(true);


            this.jTextAreaGenesisBlock.setBackground(Color.white);
            this.jTextAreaGenesisBlockDesc.setBackground(Color.white);




           if(initialisingProject)
           {
               ////////////jTextAreaGenesisAfter.setText(projManager.getCurrentProject().genesisSettings.getNativeBlock(ScriptLocation.BEFORE_FINAL_RESET));


               //////////jTextAreaGenesisBefore.setText(projManager.getCurrentProject().genesisSettings.getNativeBlock(ScriptLocation.BEFORE_CELL_CREATION));

               jCheckBoxGenesisSymmetric.setSelected(projManager.getCurrentProject().genesisSettings.isSymmetricCompartments());
               jCheckBoxGenesisComments.setSelected(projManager.getCurrentProject().genesisSettings.isGenerateComments());
               
               //jCheckBoxGenesisNoGraphicsMode.setSelected(!projManager.getCurrentProject().genesisSettings.isGraphicsMode());
               
                if (projManager.getCurrentProject().genesisSettings.getGraphicsMode().equals(GenesisSettings.GraphicsMode.ALL_SHOW))
                {
                    jRadioButtonGenesisAllGUI.setSelected(true);
                }
                else if (projManager.getCurrentProject().genesisSettings.getGraphicsMode().equals(GenesisSettings.GraphicsMode.NO_PLOTS))
                {
                    jRadioButtonGenesisNoPlots.setSelected(true);
                }

                else if (projManager.getCurrentProject().genesisSettings.getGraphicsMode().equals(GenesisSettings.GraphicsMode.NO_CONSOLE))
                {
                    jRadioButtonGenesisNoConsole.setSelected(true);
                }

               //////////jCheckBoxGenesisVoltPlot.setSelected(projManager.getCurrentProject().genesisSettings.isShowVoltPlot());
               jCheckBoxGenesisShapePlot.setSelected(projManager.getCurrentProject().genesisSettings.isShowShapePlot());
               

               jCheckBoxGenesisCopySimFiles.setSelected(projManager.getCurrentProject().genesisSettings.isCopySimFiles());
               jCheckBoxGenesisMooseMode.setSelected(projManager.getCurrentProject().genesisSettings.isMooseCompatMode());

               if (projManager.getCurrentProject().genesisSettings.getUnitSystemToUse() == UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS)
                   jRadioButtonGenesisPhy.setSelected(true);
               if (projManager.getCurrentProject().genesisSettings.getUnitSystemToUse() == UnitConverter.GENESIS_SI_UNITS)
                   jRadioButtonGenesisSI.setSelected(true);

               if (projManager.getCurrentProject().genesisSettings.getReloadSimAfterSecs()>0)
               {
                   jTextFieldGenesisReload.setText(projManager.getCurrentProject().genesisSettings.getReloadSimAfterSecs()+"");
                   jTextFieldGenesisReload.setEnabled(true);
                   //jTextFieldGenesisReload.setEditable(true);
                   jCheckBoxGenesisReload.setSelected(true);
               }
               else
               {
                   jTextFieldGenesisReload.setText("10");
                   jTextFieldGenesisReload.setEnabled(false);
                   //jTextFieldGenesisReload.setEditable(false);
                   jCheckBoxGenesisReload.setSelected(false);

               }

               if (projManager.getCurrentProject().genesisSettings.getAbsRefractSpikegen()<0)
               {
                   jTextFieldGenesisAbsRefract.setText("10");
                   jTextFieldGenesisAbsRefract.setEnabled(false);
                   jCheckBoxGenesisAbsRefract.setSelected(true);
               }
               else
               {
                   jTextFieldGenesisAbsRefract.setText(projManager.getCurrentProject().genesisSettings.getAbsRefractSpikegen()+"");
                   jTextFieldGenesisAbsRefract.setEnabled(true);
                   jCheckBoxGenesisAbsRefract.setSelected(false);

               }

           }
       
            ArrayList<ScriptLocation> ncls = ScriptLocation.getAllKnownLocations();
            jPanelGenesisExtraLinks.removeAll();
            
            jPanelGenesisExtraLinks.add(jLabelGenesisExtraLinks);

            for(int i=0;i<ncls.size();i++)
            {
                final ScriptLocation ncl = ncls.get(i);

                String text = projManager.getCurrentProject().genesisSettings.getNativeBlock(ncl);
              
                ClickLink cl = new ClickLink(ncl.toShortString(), ncl.getShortDescription());

                if(text==null || text.trim().length()==0)
                    cl.setForeground(Color.lightGray);

                jPanelGenesisExtraLinks.add(cl);

                cl.addMouseListener(new MouseListener()
                {
                    //String cellGroup = cellGroup;
                    public void mouseClicked(MouseEvent e)
                    {
                        //jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(GENESIS_SIMULATOR_TAB));
                        jTabbedPaneGenesis.setSelectedIndex(jTabbedPaneGenesis.indexOfTab(GENESIS_TAB_EXTRA));

                        jComboBoxGenesisExtraBlocks.setSelectedItem(ncl);

                    };

                    public void mousePressed(MouseEvent e) {};

                    public void mouseReleased(MouseEvent e) {};

                    public void mouseEntered(MouseEvent e) {};

                    public void mouseExited(MouseEvent e) {};

                });
            

           }

           jLabelGenesisNumMethod.setText(projManager.getCurrentProject().genesisSettings.getNumMethod().toString());


           //if()
       }

    }




    /**
     * Refreshes the tab related to NeuroML
     *
     */
    private void refreshTabNeuroML()
    {
        logger.logComment("> Refreshing the Tab for NeuroML...");

        if (projManager.getCurrentProject() == null ||
           projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
       {
           jButtonNeuroML1Export.setEnabled(false);
           jButtonNeuroML2aExport.setEnabled(false);
           jButtonNeuroML2bExport.setEnabled(false);
           jButtonNeuroML2Lems.setEnabled(false);
           jButtonNeuroML2Graph.setEnabled(false);
           jButtonNeuroML2NineML.setEnabled(false);
           //jButtonNeuroMLExportLevel2.setEnabled(false);
           //jButtonNeuroMLExportCellLevel3.setEnabled(false);
           //jButtonNeuroMLExportNetLevel3.setEnabled(false);
           jButtonNeuroMLViewPlain.setEnabled(false);
           jButtonNeuroMLViewFormatted.setEnabled(false);
           jButtonNeuroMLGenSim.setEnabled(false);

           jComboBoxNeuroML.removeAllItems();
           jComboBoxNeuroML.setEnabled(false);
       }
       else
       {
           jButtonNeuroML1Export.setEnabled(true);
           jButtonNeuroML2aExport.setEnabled(true);
           jButtonNeuroML2bExport.setEnabled(true);
           jButtonNeuroML2Lems.setEnabled(true);
           jButtonNeuroML2Graph.setEnabled(true);
           jButtonNeuroML2NineML.setEnabled(true);
           //jButtonNeuroMLExportLevel2.setEnabled(true);
           //jButtonNeuroMLExportCellLevel3.setEnabled(true);
         //  jButtonNeuroMLExportNetLevel3.setEnabled(true);
           jComboBoxNeuroML.setEnabled(true);
           jButtonNeuroMLGenSim.setEnabled(true);

           File genNeuroMLDir = ProjectStructure.getNeuroMLDir(projManager.getCurrentProject().getProjectMainDirectory());

           jComboBoxNeuroML.removeAllItems();
           if (genNeuroMLDir.exists())
           {

               File[] contents = GeneralUtils.reorderAlphabetically(genNeuroMLDir.listFiles(), true);

               for (int i = 0; i < contents.length; i++)
               {
                   if (!contents[i].getName().equals("README") && !contents[i].isDirectory())
                       jComboBoxNeuroML.addItem(contents[i].getAbsolutePath()
                                                + " ("
                                                + contents[i].length()
                                                + " bytes)");
               }
           }
           if (jComboBoxNeuroML.getItemCount() == 0) jComboBoxNeuroML.addItem(noNeuroMLFilesFound);
           else
           {
               jButtonNeuroMLViewPlain.setEnabled(true);
               jButtonNeuroMLViewFormatted.setEnabled(true);
           }


       }
        logger.logComment("> Done refreshing the Tab for MorphML...");

    }


    /**
     * Refreshes the tab related to NEURON
     *
     */
    private void refreshTabNeuron()
    {
        ///////this.refreshTabNmodl();

        logger.logComment(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Refreshing the Tab for Neuron...");

        if (projManager.getCurrentProject()!=null && initialisingProject &&
            projManager.getCurrentProject().getProjectStatus() != Project.PROJECT_NOT_INITIALISED)
        {
            logger.logComment("Putting in the params: " + projManager.getCurrentProject().simulationParameters);

            //projManager.getCurrentProject().neuronSettings.simParams.reference = projManager.getCurrentProject().getProjectName();

            jTextFieldSimRef.setText(this.projManager.getCurrentProject().simulationParameters.getReference());

            jTextFieldSimDefDur.setText(this.projManager.getCurrentProject().simulationParameters.getDuration() + "");
            jTextFieldSimDT.setText(this.projManager.getCurrentProject().simulationParameters.getDt() + "");

            jTextFieldSimulationGlobCm.setText(projManager.getCurrentProject().simulationParameters.getGlobalCm()+"");
            jTextFieldSimulationGlobRm.setText(projManager.getCurrentProject().simulationParameters.getGlobalRm()+"");
            jTextFieldSimulationGlobRa.setText(projManager.getCurrentProject().simulationParameters.getGlobalRa()+"");
            jTextFieldSimulationInitVm.setText(projManager.getCurrentProject().simulationParameters.getInitVm()+"");
            
            
            jTextFieldSimulationTemp.setText(projManager.getCurrentProject().simulationParameters.getTemperature()+"");

            jTextFieldElectroLenMax.setText(projManager.getCurrentProject().simulationParameters.getMaxElectroLen()+"");
            jTextFieldElectroLenMin.setText(projManager.getCurrentProject().simulationParameters.getMinElectroLen()+"");
            
                       
            jTextFieldSimulationVLeak.setText(projManager.getCurrentProject().simulationParameters.getGlobalVLeak()+"");


            jCheckBoxSpecifySimRef.setSelected(this.projManager.getCurrentProject().simulationParameters.isSpecifySimName());
            jCheckBoxNeuronSaveHoc.setSelected(this.projManager.getCurrentProject().simulationParameters.isSaveCopyGenSimFiles());

           // refreshSimulationName();
/*
           Hashtable<NativeCodeLocation, String> nativeBlocks = projManager.getCurrentProject().neuronSettings.getNativeBlocks();

           Enumeration<NativeCodeLocation> locsUsed = nativeBlocks.keys();

           while (locsUsed.hasMoreElements())
           {
               NativeCodeLocation nextLoc = locsUsed.nextElement();
               System.out.println("next "+ nextLoc);
               if (nextLoc.equals(NativeCodeLocation.BEFORE_INITIAL))
               {
                   jTextAreaNeuronAfter.setText(nativeBlocks.get(nextLoc));
               }
               else if (nextLoc.equals(NativeCodeLocation.BEFORE_CELL_CREATION))
               {
                   jTextAreaNeuronBefore.setText(nativeBlocks.get(nextLoc));
               }

               //String text = nativeBlocks.get(nextLoc);
           }*/
            ////this.jTextAreaNeuronAfter.setText(projManager.getCurrentProject().neuronSettings.getNativeBlock(NativeCodeLocation.BEFORE_INITIAL));


            //this.jTextAreaNeuronBlock.setText(projManager.getCurrentProject().neuronSettings.getNativeBlock(NativeCodeLocation.BEFORE_CELL_CREATION));

            //this.jTextAreaNeuronBefore.setText(nativeBlocks.get(NativeCodeLocation.BEFORE_CELL_CREATION));

            //System.out.println("nativeBlocks: "+nativeBlocks);
            //System.out.println("nativeBlocks.get("+NativeCodeLocation.BEFORE_INITIAL+"): "+ nativeBlocks.get(NativeCodeLocation.BEFORE_INITIAL));






/*

            if (projManager.getCurrentProject().simulationParameters.getRecordingMode() ==
                SimulationParameters.SIMULATION_NOT_RECORDED)
            {
                jRadioButtonNeuronSimDontRecord.setSelected(true);
            }
            else if (projManager.getCurrentProject().simulationParameters.getRecordingMode() ==
                     SimulationParameters.SIMULATION_RECORD_TO_FILE)
            {
                jRadioButtonNeuronSimSaveToFile.setSelected(true);

            }

            if (projManager.getCurrentProject().simulationParameters.getWhatToRecord() ==
                SimulationParameters.RECORD_ONLY_SOMA)
            {
                jRadioButtonSimSomaOnly.setSelected(true);

            else if (projManager.getCurrentProject().simulationParameters.whatToRecord ==
                     SimulationParameters.RECORD_EVERY_SECTION)
            {
                jRadioButtonSimAllSections.setSelected(true);

            }
            else if (projManager.getCurrentProject().simulationParameters.getWhatToRecord() ==
                     SimulationParameters.RECORD_EVERY_SEGMENT)
            {
                jRadioButtonSimAllSegments.setSelected(true);
            }*/

/*

            if (projManager.getCurrentProject().stimulationSettings != null)
            {

                logger.logComment("There is a stimSettings: "+ projManager.getCurrentProject().stimulationSettings);
                jTextFieldStimDelay.setText(projManager.getCurrentProject().stimulationSettings.delay + "");

                if (projManager.getCurrentProject().stimulationSettings.stimExtent == StimulationSettings.ONE_CELL)
                {
                    jRadioButtonStimOneCell.setSelected(true);
                    jTextFieldStimSomaNum.setText(projManager.getCurrentProject().stimulationSettings.cellNumber + "");
                }
                else
                {
                    jRadioButtonStimPercentage.setSelected(true);
                    jTextFieldStimPercentage.setText(projManager.getCurrentProject().stimulationSettings.percentage + "");
                }
                if (projManager.getCurrentProject().stimulationSettings.type.equals(IClampSettings.type))
                {
                    jRadioButtonStimIClamp.setSelected(true);
                    IClampSettings icStim = (IClampSettings) projManager.getCurrentProject().stimulationSettings;
                    logger.logComment("Adding stuff for IClamp...");
                    jPanelSimParams.removeAll();
                    JPanel tempPanel1 = new JPanel();
                    tempPanel1.setSize(500, 30);
                    tempPanel1.add(new JLabel("Amplitude of stimulation: "));
                    tempPanel1.add(jTextFieldIClampAmplitude);
                    jTextFieldIClampAmplitude.setText(icStim.amplitude + "");
                    jPanelSimParams.add(tempPanel1);
                    JPanel tempPanel2 = new JPanel();
                    tempPanel2.setSize(500, 30);
                    tempPanel2.add(new JLabel("Duration of stimulation: "));
                    tempPanel2.add(jTextFieldIClampDuration);
                    jTextFieldIClampDuration.setText(icStim.duration + "");
                    jPanelSimParams.add(tempPanel2);
                    jPanelSimParams.validate();
                }
                else if (projManager.getCurrentProject().stimulationSettings.type.equals(NetStimSettings.type))
                {
                    jRadioButtonStimNetStim.setSelected(true);
                    NetStimSettings netStim = (NetStimSettings) projManager.getCurrentProject().stimulationSettings;
                    logger.logComment("Adding stuff for NetStim...");
                    jPanelSimParams.removeAll();
                    JPanel tempPanel1 = new JPanel();
                    tempPanel1.setSize(500, 30);
                    tempPanel1.add(new JLabel("Average number of spikes"));
                    tempPanel1.add(jTextFieldNetStimNumber);
                    jTextFieldNetStimNumber.setText(netStim.avgNumSpikes + "");
                    jPanelSimParams.add(tempPanel1);
                    JPanel tempPanel2 = new JPanel();
                    tempPanel2.setSize(500, 30);
                    tempPanel2.add(new JLabel("Noise (0: no noise -> 1: noisiest)"));
                    tempPanel2.add(jTextFieldNetStimNoise);
                    jTextFieldNetStimNoise.setText(netStim.noise + "");
                    jPanelSimParams.add(tempPanel2);
                    jPanelSimParams.validate();
                }
            }
            else
            {
                jRadioButtonStimNone.setSelected(true);
                jPanelSimParams.removeAll();
                jPanelSimParams.validate();
            }
            logger.logComment("done...");*/
        }

        if (projManager.getCurrentProject() == null ||
            projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
        {
            logger.logComment("No project or project not initialised");

            this.jButtonNeuronCreateLocal.setEnabled(false);
            jButtonNeuronCreateCondor.setEnabled(false);
            ///////////////jButtonNeuronCreateMPIHoc.setEnabled(false);
            jButtonNeuronCreatePythonXML.setEnabled(false);
            jButtonNeuronCreatePyHDF5.setEnabled(false);
            this.jButtonNeuronView.setEnabled(false);
            this.jButtonNeuronRun.setEnabled(false);

            this.jCheckBoxNeuronSumatra.setEnabled(false);
            this.jCheckBoxNeuronShowShapePlot.setEnabled(false);

            this.jRadioButtonNeuronAllGUI.setEnabled(false);
            this.jRadioButtonNeuronNoConsole.setEnabled(false);
            this.jRadioButtonNeuronNoPlots.setEnabled(false);

            this.jCheckBoxNeuronComments.setEnabled(false);

            this.jCheckBoxNeuronNumInt.setEnabled(false);
            this.jCheckBoxNeuronGenAllMod.setEnabled(false);

            this.jComboBoxNeuronFileList.setEnabled(false);
            this.jCheckBoxNeuronLineNums.setEnabled(false);
            this.jTextFieldSimDT.setText("0");
            this.jTextFieldSimDefDur.setText("0");
            this.jTextFieldSimRef.setText("");
            this.jTextFieldSimDT.setEnabled(false);
            this.jTextFieldSimDefDur.setEnabled(false);
            this.jTextFieldSimRef.setEnabled(false);
            this.jRadioButtonNeuronSimDontRecord.setEnabled(false);
            this.jRadioButtonNeuronSimSaveToFile.setEnabled(false);

            jComboBoxNeuronExtraBlocks.setEnabled(false);
            jComboBoxNeuronExtraBlocks.setSelectedIndex(0);



            jLabelSimDefDur.setEnabled(false);
            jLabelSimSummary.setEnabled(false);
            jLabelSimDefDur.setEnabled(false);
            jTextFieldSimDefDur.setEnabled(false);
            jLabelSimDT.setEnabled(false);
            jTextFieldSimDT.setEnabled(false);
            jRadioButtonNeuronSimDontRecord.setEnabled(false);
            jRadioButtonNeuronSimSaveToFile.setEnabled(false);

            jRadioButtonSimAllSegments.setEnabled(false);
            jRadioButtonSimAllSections.setEnabled(false);
            jRadioButtonSimSomaOnly.setEnabled(false);
            jRadioButtonSimSomaOnly.setSelected(true);


            this.jLabelSimSummary.setText("");

            Button b = new Button();
            b.setEnabled(false);

            this.jTextAreaNeuronBlock.setEditable(false);
            this.jTextAreaNeuronBlock.setEnabled(false);
            this.jTextAreaNeuronBlock.setText("");
            this.jTextAreaNeuronBlockDesc.setText("");


            jLabelSimRef.setEnabled(false);
            jTextFieldSimRef.setEnabled(false);
            jCheckBoxSpecifySimRef.setEnabled(false);
            jCheckBoxNeuronSaveHoc.setEnabled(false);


            ////this.jTextAreaNeuronAfter.setText("");
            this.jTextAreaNeuronBlock.setText("");


            ////this.jTextAreaNeuronAfter.setBackground(b.getBackground());
            this.jTextAreaNeuronBlock.setBackground(b.getBackground());
            this.jTextAreaNeuronBlockDesc.setBackground(b.getBackground());

            jComboBoxNeuronFileList.removeAllItems();
            jComboBoxNeuronFileList.addItem(defaultNeuronFilesText);

            this.jTextFieldNeuronRandomGen.setEnabled(false);
            this.jCheckBoxNeuronRandomGen.setEnabled(false);
            this.jTextAreaNeuronBlockDesc.setEnabled(false);
            this.jLabelNeuronRandomGenDesc.setEnabled(false);



            /** @todo make stim buttons uninitialised, etc. */
        }
        else
        {
            logger.logComment("Project is initialised, updating stuff...");
            this.jButtonNeuronCreateLocal.setEnabled(true);
            //////////////////////////////////////////////////jButtonNeuronCreateCondor.setEnabled(true);
            /////////////////////////////////////////////////jButtonNeuronCreatePVM.setEnabled(true);


            //////////jButtonNeuronCreateMPIHoc.setEnabled(true);
            jButtonNeuronCreatePythonXML.setEnabled(true);
            jButtonNeuronCreatePyHDF5.setEnabled(true);


            this.jCheckBoxNeuronSumatra.setEnabled(true);
            this.jCheckBoxNeuronShowShapePlot.setEnabled(true);
            this.jRadioButtonNeuronAllGUI.setEnabled(true);
            this.jRadioButtonNeuronNoConsole.setEnabled(true);
            this.jRadioButtonNeuronNoPlots.setEnabled(true);
            this.jCheckBoxNeuronComments.setEnabled(true);
            this.jCheckBoxNeuronNumInt.setEnabled(true);
            this.jCheckBoxNeuronGenAllMod.setEnabled(true);


            this.jCheckBoxNeuronShowShapePlot.setSelected(projManager.getCurrentProject().neuronSettings.isShowShapePlot());
            


            if (projManager.getCurrentProject().neuronSettings.getGraphicsMode().equals(NeuronSettings.GraphicsMode.ALL_SHOW))
                jRadioButtonNeuronAllGUI.setSelected(true);
            if (projManager.getCurrentProject().neuronSettings.getGraphicsMode().equals(NeuronSettings.GraphicsMode.NO_PLOTS))
                jRadioButtonNeuronNoPlots.setSelected(true);
            if (projManager.getCurrentProject().neuronSettings.getGraphicsMode().equals(NeuronSettings.GraphicsMode.NO_CONSOLE))
                jRadioButtonNeuronNoConsole.setSelected(true);



            this.jCheckBoxNeuronComments.setSelected(projManager.getCurrentProject().neuronSettings.isGenerateComments());
            this.jCheckBoxNeuronNumInt.setSelected(projManager.getCurrentProject().neuronSettings.isVarTimeStep());

            this.jCheckBoxNeuronGenAllMod.setSelected(projManager.getCurrentProject().neuronSettings.isGenAllModFiles());
            this.jCheckBoxNeuronCopySimFiles.setSelected(projManager.getCurrentProject().neuronSettings.isCopySimFiles());
            this.jCheckBoxNeuronForceCorrInit.setSelected(projManager.getCurrentProject().neuronSettings.isForceCorrectInit());
            this.jCheckBoxNeuronModSilent.setSelected(projManager.getCurrentProject().neuronSettings.isModSilentMode());

            this.jRadioButtonNeuronFormatText.setSelected(projManager.getCurrentProject().neuronSettings.getDataSaveFormat().equals(NeuronSettings.DataSaveFormat.TEXT_NC));
            this.jRadioButtonNeuronFormatHDF5.setSelected(projManager.getCurrentProject().neuronSettings.getDataSaveFormat().equals(NeuronSettings.DataSaveFormat.HDF5_NC));

            jComboBoxNeuronExtraBlocks.setEnabled(true);


            ////this.jTextAreaNeuronAfter.setEditable(true);
            this.jTextAreaNeuronBlock.setEditable(true);

            ////this.jTextAreaNeuronAfter.setEnabled(true);
            this.jTextAreaNeuronBlock.setEnabled(true);

            ////this.jTextAreaNeuronAfter.setBackground((new JTextArea()).getBackground());
            this.jTextAreaNeuronBlock.setBackground((new JTextArea()).getBackground());

            if (!jComboBoxNeuronExtraBlocks.getSelectedItem().equals(this.neuronBlockPrompt))
            {
                NativeCodeLocation ncl = (NativeCodeLocation) jComboBoxNeuronExtraBlocks.getSelectedItem();
                //System.out.println("NativeCodeLocation selected: " + ncl);
                this.projManager.getCurrentProject().neuronSettings.setNativeBlock(ncl, jTextAreaNeuronBlock.getText());
            }


            ////////jRadioButtonSimAllSegments.setEnabled(true);
           //////// jRadioButtonSimAllSections.setEnabled(true);
            /////jRadioButtonSimSomaOnly.setEnabled(true);


            this.jTextFieldSimDT.setEnabled(true);
            this.jTextFieldSimDefDur.setEnabled(true);
            this.jTextFieldSimRef.setEnabled(true);

            ///////////this.jRadioButtonNeuronSimDontRecord.setEnabled(true);
            ///////////this.jRadioButtonNeuronSimSaveToFile.setEnabled(true);


            jLabelSimDefDur.setEnabled(true);
            jLabelSimSummary.setEnabled(true);
            jLabelSimDefDur.setEnabled(true);
            jTextFieldSimDefDur.setEnabled(true);
            jLabelSimDT.setEnabled(true);
            jTextFieldSimDT.setEnabled(true);
            ////////jRadioButtonNeuronSimDontRecord.setEnabled(true);
            ////////jRadioButtonNeuronSimSaveToFile.setEnabled(true);

            //refreshSimulationName();

            this.jTextFieldNeuronRandomGen.setEnabled(true);
            this.jCheckBoxNeuronRandomGen.setEnabled(true);
            this.jTextAreaNeuronBlockDesc.setEnabled(true);
            this.jLabelNeuronRandomGenDesc.setEnabled(true);


            this.jTextAreaNeuronBlock.setBackground(Color.white);
            this.jTextAreaNeuronBlockDesc.setBackground(Color.white);
            

            ArrayList<NativeCodeLocation> ncls = NativeCodeLocation.getAllKnownLocations();
            jPanelNeuronExtraLinks.removeAll();
            
            jPanelNeuronExtraLinks.add(jLabelNeuronExtraLinks);

            for(int i=0;i<ncls.size();i++)
            {
                final NativeCodeLocation ncl = ncls.get(i);

                String text = projManager.getCurrentProject().neuronSettings.getNativeBlock(ncl);
              
                ClickLink cl = new ClickLink(ncl.toShortString(), ncl.getShortDescription());

                if(text==null || text.trim().length()==0)
                    cl.setForeground(Color.lightGray);

                jPanelNeuronExtraLinks.add(cl);

                cl.addMouseListener(new MouseListener()
                {
                    //String cellGroup = cellGroup;
                    public void mouseClicked(MouseEvent e)
                    {
                        //jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(GENESIS_SIMULATOR_TAB));
                        jTabbedPaneNeuron.setSelectedIndex(jTabbedPaneNeuron.indexOfTab(NEURON_TAB_EXTRA));

                        jComboBoxNeuronExtraBlocks.setSelectedItem(ncl);

                    };

                    public void mousePressed(MouseEvent e) {};

                    public void mouseReleased(MouseEvent e) {};

                    public void mouseEntered(MouseEvent e) {};

                    public void mouseExited(MouseEvent e) {};

                });
            }




            try
            {

                createSimulationSummary();

                jLabelSimRef.setEnabled(true);
                jTextFieldSimRef.setEnabled(true);
                jCheckBoxSpecifySimRef.setEnabled(true);

                if (jCheckBoxSpecifySimRef.isSelected())
                    jTextFieldSimRef.setEnabled(true);
                else
                    jTextFieldSimRef.setEnabled(false);


/*
                if (jRadioButtonNeuronSimDontRecord.isSelected())
                {
                    jLabelNeuronSimRef.setEnabled(false);
                    jTextFieldSimRef.setEnabled(false);
                    jCheckBoxNeuronSpecifySimRef.setEnabled(false);
                    jCheckBoxNeuronSaveHoc.setEnabled(false);
                }
                else if (jRadioButtonNeuronSimSaveToFile.isSelected())
                {
                    jLabelNeuronSimRef.setEnabled(true);
                    //jTextFieldNeuronSimRef.setEnabled(true);
                    jCheckBoxNeuronSpecifySimRef.setEnabled(true);
                    jCheckBoxNeuronSaveHoc.setEnabled(true);

                    if (jCheckBoxNeuronSpecifySimRef.isSelected())
                        jTextFieldSimRef.setEnabled(true);
                    else
                        jTextFieldSimRef.setEnabled(false);
                }*/



            }
            catch (Exception ex)
            {
                logger.logError("Error updating Tab for 3D", ex);
            }
        }
    }


    private void refreshSimulationName()
    {
        /** @todo Replace this, just put here so updating jTextFieldNeuronSimRef
         * wouldn't cause another refresh... */

        boolean currInitialisingState = initialisingProject;
        this.initialisingProject = true;
        if (this.projManager.getCurrentProject().simulationParameters.isSpecifySimName())
        {
            //System.out.println("Holding sim name: in textarea: "+ jTextFieldNeuronSimRef.getText()
           //                    +" stored: "+projManager.getCurrentProject().neuronSettings.simParams.reference);
            jTextFieldSimRef.setText(this.projManager.getCurrentProject().simulationParameters.getReference());
        }
        else
        {
            logger.logComment("Not holding sim name");
            String ref = projManager.getCurrentProject().simulationParameters.getReference();
            //String newRef = null;

            File simDir = ProjectStructure.getSimulationsDir(projManager.getCurrentProject().getProjectMainDirectory());

            logger.logComment("Sim dir: "+ simDir);

            while ((new File(simDir, ref)).exists())
            {
                logger.logComment("Trying ref: " + ref);
                if (ref.indexOf("_") > 0)
                {
                    try
                    {
                        String numPart = ref.substring(ref.indexOf("_") + 1);
                        int oldNum = Integer.parseInt(numPart);
                        logger.logComment("Old num: " + oldNum);
                        ref = ref.substring(0, ref.indexOf("_")) + "_"+ (oldNum + 1);
                    }
                    catch (NumberFormatException ex)
                    {
                        ref = ref + "_1";
                    }
                }
                else
                {
                    ref = ref + "_1";
                }
            }
            jTextFieldSimRef.setText(ref);
            projManager.getCurrentProject().simulationParameters.setReference(ref);
        }
        this.initialisingProject = currInitialisingState;

    }

    public void applyNew3DSettings()
    {
        projManager.getCurrentProject().markProjectAsEdited();
        this.refreshTab3D();
    }

    /**
     * Refreshes the tab related to cell info
     *
     */
    private void refreshTab3D()
    {
        logger.logComment("> Refreshing the Tab for 3D...");


        if (projManager.getCurrentProject()!=null)
            logger.logComment("Status of project: "+ projManager.getCurrentProject().getProjectStatus());
        else
            logger.logComment("Project is set to null...");

        if (projManager.getCurrentProject()==null ||
            projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_NOT_INITIALISED)
        {
            // no project loaded...
            this.jButton3DView.setEnabled(false);
            this.jButton3DPrevSimuls.setEnabled(false);
            this.jButton3DQuickSims.setEnabled(false);
            this.jButton3DDestroy.setEnabled(false);
            this.jComboBoxView3DChoice.setEnabled(false);
            this.jButton3DSettings.setEnabled(false);

            jPanel3DMain.removeAll();
        }
        else
        {
            refreshView3DComboBox();
            Object itemSelected = jComboBoxView3DChoice.getSelectedItem();

            logger.logComment("refreshView3DComboBox itemSelected: "+itemSelected);

            if (projManager.getCurrentProject().regionsInfo.getRowCount() > 0 ||
                projManager.getCurrentProject().cellManager.getNumberCellTypes() >0)
            {
                this.jButton3DView.setEnabled(true);
                this.jButton3DPrevSimuls.setEnabled(true);
                this.jButton3DQuickSims.setEnabled(true);

                this.jButton3DSettings.setEnabled(true);
                this.jComboBoxView3DChoice.setEnabled(true);
            }


            // refresh the 3d panel...
            if (base3DPanel != null)
            {
                Transform3D transf = base3DPanel.getLastViewingTransform3D();

                Object previouslyViewedObj = base3DPanel.getViewedObject();

                if (base3DPanel instanceof Main3DPanel)
                {
                    Main3DPanel main3Dpanel = (Main3DPanel) base3DPanel;

                    SimulationRerunFrame simFrame = main3Dpanel.getSimulationFrame();
                    SlicerFrame sliFrame = main3Dpanel.getSlicerFrame();

                    boolean transparency = main3Dpanel.getTransparencySelected();
                    String cellGroup = main3Dpanel.getSelectedCellGroup();
                    //String cellNumber = main3Dpanel.getSelectedCellNumString()+"";

                    logger.logComment("Transparency? "+ transparency);
                    logger.logComment("cellGroup "+ cellGroup);

                    jPanel3DMain.removeAll();

                    this.doCreate3D(previouslyViewedObj);// could be LATEST_GENERATED_POSITIONS or dir

                    base3DPanel.setLastViewingTransform3D(transf);

                    main3Dpanel = (Main3DPanel) base3DPanel;// as its a new object

                    if (simFrame != null) main3Dpanel.setSimulationFrame(simFrame);
                    if (sliFrame != null) main3Dpanel.setSlicerFrame(sliFrame);

                    base3DPanel.refresh3D();


                    main3Dpanel.setTransparencySelected(transparency);
                    main3Dpanel.setSelectedCellGroup(cellGroup);
                    //main3Dpanel.setSelectedCellNumber(cellNumber);

                   // simFrame.setc


                }
                else if (base3DPanel instanceof OneCell3DPanel)
                {
                    jPanel3DMain.removeAll();

                    this.doCreate3DCell((Cell)previouslyViewedObj);
                    base3DPanel.setLastViewingTransform3D(transf);
                    base3DPanel.refresh3D();
                }



            }
        }
    }


    private void refreshView3DComboBox()
    {
        Object itemSelected = jComboBoxView3DChoice.getSelectedItem();
        logger.logComment("refreshView3DComboBox itemSelected: "+itemSelected);
        String stringRepresentation = itemSelected.toString();

        jComboBoxView3DChoice.removeAllItems();

      //  jComboBoxView3DChoice.addItem(choice3DChoiceMain);
        jComboBoxView3DChoice.addItem(LATEST_GENERATED_POSITIONS);
    //    jComboBoxView3DChoice.addItem(choice3DPrevSims);
/*
        File simulationsDir = new File(projManager.getCurrentProject().getProjectMainDirectory(),
                                       GeneralProperties.getDirForSimulations());

        if (!simulationsDir.exists() || !simulationsDir.isDirectory())
        {
            simulationsDir.mkdir();
        }
        File[] childrenDirs = simulationsDir.listFiles();

        logger.logComment("There are " + childrenDirs.length + " files in dir: " +
                          simulationsDir.getAbsolutePath());

        for (int i = 0; i < childrenDirs.length; i++)
        {
            if (childrenDirs[i].isDirectory())
            {
                logger.logComment("Looking at directory: " + childrenDirs[i].getAbsolutePath());

                SimulationData simDataForDir = null;
                try
                {
                    simDataForDir = new SimulationData(childrenDirs[i].getAbsoluteFile());

                    jComboBoxView3DChoice.addItem(simDataForDir);
                    logger.logComment("That's a valid simulation dir...");
                }
                catch (SimulationDataException ex1)
                {
                    logger.logComment("That's not a valid simulation dir...");
                }

            }
        }

        jComboBoxView3DChoice.addItem(choice3DSingleCells);
*/
        try
        {
            Vector<Cell> cells = this.projManager.getCurrentProject().cellManager.getAllCells();

            GeneralUtils.reorderAlphabetically(cells, true);

            for (Cell cell : cells)
            {
                jComboBoxView3DChoice.addItem(cell);
            }
        }
        catch (Exception ex)
        {
            logger.logError("Error updating Tab for 3D", ex);
        }

        for (int i = 0; i < jComboBoxView3DChoice.getItemCount(); i++)
        {
            Object nextItem = jComboBoxView3DChoice.getItemAt(i);
            // do it this way since the SimulationData objects won't be equal between refreshed...
            if (nextItem.toString().equals(stringRepresentation))
                jComboBoxView3DChoice.setSelectedIndex(i);
        }

    }

    /**
     * Asks the user if they wish to save the currently open project
     * @return true if all went well (saved or unsaved), or if there was no
     * project loaded, false if cancelled
     */
    private boolean checkToSave()
    {
        logger.logComment("Checking whether to save..");
        if (projManager.getCurrentProject()==null)
        {
            logger.logComment("No project loaded...");
            return true;
        }
        if (projManager.getCurrentProject().getProjectStatus() == Project.PROJECT_EDITED_NOT_SAVED)
        {
            logger.logComment("Trying to close without saving...");
            String projName = projManager.getCurrentProject().getProjectName();

            SaveBeforeExitDialog dlg = new SaveBeforeExitDialog(this,
                "Save project: " + projName + "?", false);
            Dimension dlgSize = dlg.getPreferredSize();
            Dimension frmSize = getSize();
             Point loc = getLocation();
            dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                            (frmSize.height - dlgSize.height) / 2 + loc.y);
            dlg.setModal(true);
            dlg.pack();
            dlg.setVisible(true);

            if (dlg.cancelPressed)
            {
                logger.logComment("Chosen to cancel the exit...");
                return false;
            }

            if (dlg.saveTheProject)
            {
                logger.logComment("Chosen to save the project...");
                doSave();
                return true;
            }
        }
        else
        {
            logger.logComment("No need to ask about saving...");

        }
        return true;
    }

    private void addNamedDocumentListner(String tabName, javax.swing.text.JTextComponent comp)
    {
        GeneralComponentListener newListner = new GeneralComponentListener(tabName, this);
        comp.getDocument().addDocumentListener(newListner);
    }

    private void addRadioButtonListner(String tabName, JRadioButton comp)
    {
        GeneralComponentListener newListner = new GeneralComponentListener(tabName, this);
        comp.addItemListener(newListner);
    }

    private void addCheckBoxListner(String tabName, JCheckBox comp)
    {
        GeneralComponentListener newListner = new GeneralComponentListener(tabName, this);
        comp.addItemListener(newListner);
    }



    private class GeneralComponentListener implements DocumentListener, ItemListener
    {
        // This will (hopefully) prevent double listening for events...
        protected boolean listeningEnabled = true;

        String myRef = null;
        ProjectEventListener myEventListner = null;

        GeneralComponentListener(String ref, ProjectEventListener eventListner)
        {
            myRef = ref;
            myEventListner = eventListner;
        }

        private void registerChange()
        {
            if (listeningEnabled) myEventListner.tabUpdated(myRef);
        }

        public void changedUpdate(DocumentEvent e)
        {
            registerChange();
        }

        public void insertUpdate(DocumentEvent e)
        {
            registerChange();
        }

        public void removeUpdate(DocumentEvent e)
        {
            registerChange();
        }

        public void itemStateChanged(ItemEvent e)
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                registerChange();
            }
            if (e.getItem() instanceof JCheckBox &&
                e.getStateChange() == ItemEvent.DESELECTED)
            {
                registerChange();
            }

        }
    }

    private void createSimulationSummary()
    {
        if (initialisingProject)
        {
            return;
        }
        float simDuration = 0;
        float  simDT = 0;
        try
        {
            simDuration = Float.parseFloat(jTextFieldSimDefDur.getText());
            simDT = Float.parseFloat(jTextFieldSimDT.getText());
        }
        catch(NumberFormatException e)
        {
            logger.logError("NumberFormatException reading ("+jTextFieldSimDefDur.getText()+") and/or ("
                            + jTextFieldSimDT.getText()+")");

            this.jLabelSimSummary.setText("");
            return;
        }

        float numStepsPerMS = (float) (1d / (double)simDT);

        int numStepsTotal = Math.round(simDuration / simDT) + 1;

        StringBuilder comm = new StringBuilder("There will be " + Utils3D.trimDouble(numStepsPerMS, 6) +
                                             " steps per millisecond. The whole simulation will have "
                                             + numStepsTotal);

        comm.append(" steps.");

        this.jLabelSimSummary.setText(comm.toString());

    }

    ///////////////////////////////////////////
    ///
    ///  Functions added by JBuilder...
    ///
    ///////////////////////////////////////////


    //File | Exit action performed
    public void jMenuFileExit_actionPerformed(ActionEvent e)
    {
        boolean proceed = checkToSave();
        if (proceed)
        {
            Logger.getLogger().closeLogFile();
            GeneralProperties.saveToSettingsFile();
            recentFiles.saveToFile();
            System.exit(0);
        }
    }

    //Help | About action performed
    public void jMenuHelpAbout_actionPerformed(ActionEvent e)
    {
        MainFrame_AboutBox dlg = new MainFrame_AboutBox(this);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);
    }

    //Help | Units action performed
    public void jMenuItemUsed_actionPerformed(ActionEvent e)
    {
        logger.logComment("Giving info about the units...");

        ImageIcon unitsImage = new ImageIcon(ucl.physiol.neuroconstruct.gui.MainFrame.class.getResource("UpdatedUnits.png"));

        JOptionPane.showMessageDialog(this,
                                      "",
                                      "Units used in neuroConstruct and simulators",
                                      JOptionPane.INFORMATION_MESSAGE,
                                      unitsImage);
    }

    //Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        //super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            jMenuFileExit_actionPerformed(null);
        }
        //super.processWindowEvent(e);
    }

    void jButtonOpenProject_actionPerformed(ActionEvent e)
    {
        logger.logComment("Opening a project via a button being pressed...");

        this.loadProject();
    }

    void jButtonSaveProject_actionPerformed(ActionEvent e)
    {
        logger.logComment("Saving the active Project via a button being pressed...");

        this.doSave();

    }

    void jButtonValidate_actionPerformed(ActionEvent e)
    {

        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded!!");
            return;
        }
        logger.logComment("Validating the project...");

        this.projManager.doValidate(true);

    }

    void jButtonCloseProject_actionPerformed(ActionEvent e)
    {
        logger.logComment("Closing the active Project via a button being pressed...");
        boolean continueClosing = checkToSave();
        if (continueClosing) this.closeProject();
    }

    void jMenuItemSaveProject_actionPerformed(ActionEvent e)
    {
        logger.logComment("Attempting to save via a menu item being selected...");

        this.doSave();

    }

    void jMenuItemNewProject_actionPerformed(ActionEvent e)
    {
        logger.logComment("Attempting to create a new project via a menu item being selected...");
        doNewProject(true);
    }

    void jButtonNewProject_actionPerformed(ActionEvent e)
    {
        logger.logComment("Attempting to create a new project via a button being pressed...");

        doNewProject(true);
    }

    /*
        void jTextAreaProjDescription_focusLost(FocusEvent e)
        {
            logger.logComment("Changing the project description");
            projManager.getCurrentProject().setProjectDescription(this.jTextAreaProjDescription.getText());
            this.refreshAll();

        }
     */
    void jMenuItemFileOpen_actionPerformed(ActionEvent e)
    {
        logger.logComment("Opening a project via a menu item being selected...");

        this.loadProject();

    }

    void jButtonCellTypeNew_actionPerformed(ActionEvent e)
    {
        logger.logComment("Creating a new cell type...");
        doNewCellType();

    }

    void flagModsToBeRegenerated(ItemEvent e)
    {
        logger.logComment("jCheckBoxNeuronForceCorrInit or jCheckBoxNeuronGenAllMod changed, will need to re compile mods...");
        
        File modsDir = ProjectStructure.getNeuronCodeDir(projManager.getCurrentProject().getProjectMainDirectory());
        File forceRegenerateFile = new File(modsDir, NeuronFileManager.FORCE_REGENERATE_MODS_FILENAME);
        
        try
        {
            FileWriter fw = new FileWriter(forceRegenerateFile);
            fw.write("A file to flag to neuroConstruct that these mod files should be regenerated when a new simulation is generated.\n" +
                "May be due to option being changed in GUI making these mods out of date.");
            fw.close();
        }
        catch (IOException ex)
        {
            logger.logError("Exception creating file: "+forceRegenerateFile+"...");
        }

    }

    void jComboBoxSimConfig_itemStateChanged(ItemEvent e)
    {
        SimConfig simConfig = this.getSelectedSimConfig();

        if (simConfig!=null)
        {
            this.jTextAreaSimConfigDesc.setText(simConfig.getDescription());
        }
    }


    void jComboBoxNeuronExtraBlocks_itemStateChanged(ItemEvent e)
    {
        logger.logComment("State change: "+ e.getItem());

        if (e.getItem() == this.neuronBlockPrompt)
        {
            this.jTextAreaNeuronBlock.setText("");

            return;
        }
        if (this.projManager.getCurrentProject()==null) return;

        if (e.getStateChange() == ItemEvent.DESELECTED)
        {
                NativeCodeLocation nclDeselected = (NativeCodeLocation) e.getItem();

                logger.logComment("nclDeselected: " + nclDeselected);

                projManager.getCurrentProject().neuronSettings.setNativeBlock(nclDeselected, this.jTextAreaNeuronBlock.getText());
        }

        else if (e.getStateChange() == ItemEvent.SELECTED)
        {

            NativeCodeLocation ncl = (NativeCodeLocation) e.getItem();

                logger.logComment("ncl selected: " + ncl);

            this.jTextAreaNeuronBlockDesc.setText(ncl.getUsage());

            String text = projManager.getCurrentProject().neuronSettings.getNativeBlock(ncl);

            if (text != null)
            {
                this.jTextAreaNeuronBlock.setText(text);
            }
            else
            {
                logger.logComment("No text found for the location: " + ncl);
                jTextAreaNeuronBlock.setText("");
            }
        }
    }




    void jComboBoxGenesisExtraBlocks_itemStateChanged(ItemEvent e)
    {
        logger.logComment("State change: "+ e.getItem());

        if (e.getItem() == this.genesisBlockPrompt)
        {
            this.jTextAreaGenesisBlock.setText("");

            return;
        }
        if (this.projManager.getCurrentProject()==null) return;

        if (e.getStateChange() == ItemEvent.DESELECTED)
        {
                ScriptLocation nclDeselected = (ScriptLocation) e.getItem();

                logger.logComment("nclDeselected: " + nclDeselected);

                projManager.getCurrentProject().genesisSettings.setNativeBlock(nclDeselected, this.jTextAreaGenesisBlock.getText());
        }

        else if (e.getStateChange() == ItemEvent.SELECTED)
        {

            ScriptLocation ncl = (ScriptLocation) e.getItem();

                logger.logComment("ncl selected: " + ncl);

            this.jTextAreaGenesisBlockDesc.setText(ncl.getUsage());

            String text = projManager.getCurrentProject().genesisSettings.getNativeBlock(ncl);

            if (text != null)
            {
                this.jTextAreaGenesisBlock.setText(text);
            }
            else
            {
                logger.logComment("No text found for the location: " + ncl);
                jTextAreaGenesisBlock.setText("");
            }
        }
    }

    void jComboBoxNeuroMLComps_itemStateChanged(ItemEvent e)
    {
        MorphCompartmentalisation mc = (MorphCompartmentalisation)jComboBoxNeuroMLComps.getSelectedItem();

        this.jTextAreaNeuroMLCompsDesc.setText(mc.getDescription());
    }

    void jComboBoxGenesisComps_itemStateChanged(ItemEvent e)
    {
        MorphCompartmentalisation mc = (MorphCompartmentalisation)this.jComboBoxGenesisComps.getSelectedItem();

        this.jTextAreaGenesisCompsDesc.setText(mc.getDescription());
    }



    void jComboBoxCellTypes_itemStateChanged(ItemEvent e)
    {
        if (e.getItem() == this.cellComboPrompt)
        {
            jEditorPaneCellTypeInfo.setText("");

            return;
        }

        logger.logComment("Combo box state changed: " + e.getItem().toString());

        Cell cell = (Cell) e.getItem();

        if (cell != null)
        {
            logger.logComment("Item selected: " + cell.getInstanceName());
/*
            StringBuffer fullInfo = new StringBuffer();
            fullInfo.append("<b>Cell Type: </b>"+cell.getInstanceName() + "<br>");
            fullInfo.append("<b>Description:</b><br>");

            if (cell.getCellDescription()!=null)
            fullInfo.append(GeneralUtils.replaceAllTokens(cell.getCellDescription(), "\n", "<br>"));

            if (!fullInfo.toString().endsWith("<br>")) fullInfo.append("<br>");
            fullInfo.append("<br>");

            fullInfo.append("<b>Initial potential: </b>"+cell.getInitialPotential().toShortString() +
                            " "+ UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()
                            + "<br>");

            fullInfo.append("<b>Specific Axial Resistance: </b>"+cell.getSpecAxRes().toShortString() +
                " "+ UnitConverter.specificAxialResistanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()
                + "<br>");

            fullInfo.append("<b>Specific Capacitance: </b>"+cell.getSpecCapacitance().toShortString() +
                            " " + UnitConverter.specificCapacitanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()
                            + "<br><br>");


            fullInfo.append("<b>Sections:</b>" + cell.getAllSections().size()+ "<br>");

            fullInfo.append("<b>Segments: total: </b>" + cell.getAllSegments().size()
                            + ", <b>somatic: </b> "+cell.getOnlySomaSegments().size()
                            + ", <b>dendritic: </b> "+cell.getOnlyDendriticSegments().size()
                            + ", <b>axonal: </b> "+cell.getOnlyAxonalSegments().size() + "<br>");


            if (cell.getFirstSomaSegment()!=null)
            {
                fullInfo.append("<br><b>Soma's primary section diameter: </b>"
                                + (cell.getFirstSomaSegment().getRadius() * 2) + "<br><br>");
            }
            else
            {
                fullInfo.append("<br><b>Problem getting Soma's primary section diameter: </b><br><br>");
            }


            ArrayList<ChannelMechanism> allChanMechs = cell.getAllChannelMechanisms();
            for (int i = 0; i < allChanMechs.size(); i++)
            {
                ChannelMechanism chanMech =  null;


                chanMech = allChanMechs.get(i);
                Vector groups = cell.getGroupsWithChanMech(chanMech);
                fullInfo.append("-  Channel Mechanism: " + chanMech + " is present on: " + groups + "<br>\n");
            }

            ArrayList<String> allSynapses = cell.getAllAllowedSynapseTypes();
            for (int i = 0; i < allSynapses.size(); i++)
            {
                String syn = allSynapses.get(i);
                Vector groups = cell.getGroupsWithSynapse(syn);
                fullInfo.append("-  Synapse: " + syn + " is allowed on: " + groups + "<br>\n");
            }



            fullInfo.append("<br><b>Length in X direction: </b>"+ CellTopologyHelper.getXExtentOfCell(cell) + "<br>");

            fullInfo.append("<b>Length in Y direction: </b>"+CellTopologyHelper.getYExtentOfCell(cell) + "<br>");

            fullInfo.append("<b>Length in Z direction: </b>"+ CellTopologyHelper.getZExtentOfCell(cell) + "<br>");

            float totalSurfaceArea = 0;
            Vector<Segment> segs = cell.getAllSegments();
            for (int i = 0; i < segs.size(); i++)
            {
                totalSurfaceArea = totalSurfaceArea + segs.elementAt(i).getSegmentSurfaceArea();
            }

            fullInfo.append("<b>Total surface area of all segments: </b>"+ totalSurfaceArea + " "
                      + UnitConverter.areaUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+ "<br>\n");

            fullInfo.append("<br><b>Status of cell: </b>");

            ValidityStatus status = CellTopologyHelper.getValidityStatus(cell);


                fullInfo.append("<br><font color=\""+status.getColour()+"\">" +
                                GeneralUtils.replaceAllTokens(status.getMessage(),
                                                              "\n",
                                                              "<br>")
                                + "</font><br>");


            ValidityStatus bioStatus = CellTopologyHelper.getBiophysicalValidityStatus(cell, this.projManager.getCurrentProject());


                fullInfo.append("<br><font color=\""+bioStatus.getColour()+"\">" +
                                GeneralUtils.replaceAllTokens(bioStatus.getMessage(),
                                                              "\n",
                                                              "</font><br>")
                                + "<br>");

*/

            String text = CellTopologyHelper.printDetails(cell, this.projManager.getCurrentProject(), true, false, true);
            SimpleHtmlDoc ht = new SimpleHtmlDoc();
            ht.addRawHtml(text);

            text = ht.toHtmlString();

            jEditorPaneCellTypeInfo.setText(text);

            jEditorPaneCellTypeInfo.setCaretPosition(0);

        }

        /*
                 //this.jTreeCellDetails.removeAll();

                 CellPrototype cell = (CellPrototype)e.getItem();

                 this.projManager.getCurrentProject().cellManager.setSelectedCellType(cell.getInstanceName());
                 this.jPanelCellTypes.updateUI();
         */

    }
    
    void jButtonNeuronCreateLocal_actionPerformed(ActionEvent e)
    {
        logger.logComment("Create hoc file button pressed...");

        doCreateHoc(NeuronFileManager.RUN_HOC);

    }

    void jButtonNeuronRun_actionPerformed(ActionEvent e)
    {
        logger.logComment("Run hoc file button pressed...");

        doRunNeuron();

    }

    void jButton3DView_actionPerformed(ActionEvent e)
    {
        GeneralUtils.timeCheck("Pressed view 3d");
        doDestroy3D();
        this.doCreate3D(jComboBoxView3DChoice.getSelectedItem());
        GeneralUtils.timeCheck("Done view 3d");
    }

    void jButton3DDestroy_actionPerformed(ActionEvent e)
    {
        doDestroy3D();
    }

    void jButtonAddRegion_actionPerformed(ActionEvent e)
    {
        logger.logComment("Adding new Region...");

        doNewRegion();

    }

    void jButtonCellGroupNew_actionPerformed(ActionEvent e)
    {
        logger.logComment("Creating new Cell Group...");

        doNewCellGroup();
    }

 //   void jButtonConstructCell3DDemo_actionPerformed(ActionEvent e)
 //   {
 //       logger.logComment("Constructing model of one cell");
  //      doCreate3DCell();
 //   }

    /*
        void jTextAreaProjDescription_keyPressed(KeyEvent e)
        {
            logger.logComment("Key Pressed in project Description...");
            projManager.getCurrentProject().setProjectDescription(this.jTextAreaProjDescription.getText());
            this.refreshGeneral();

        }
     */
    void jMenuItemCloseProject_actionPerformed(ActionEvent e)
    {
        logger.logComment("Closing current project");
        boolean continueClosing = checkToSave();
        if (!continueClosing) return;
        closeProject();
    }

    void jButtonPreferences_actionPerformed(ActionEvent e)
    {
        logger.logComment("Preferences button pressed...");
        doOptionsPane(OptionsFrame.PROJ_PREFERENCES, OptionsFrame.PROJECT_PROPERTIES_MODE);
    }


    void jButtonToggleTips_actionPerformed(ActionEvent e)
    {
        logger.logComment("Toggle Tips button pressed...");

        GeneralUtils.printMemory(true);

        ToolTipManager ttm = ToolTipManager.sharedInstance();

        if (ttm.isEnabled())
        {
            ttm.setEnabled(false);
            logger.logComment("Turning off tool tips");
            jButtonToggleTips.setIcon(imageNoTips);
        }
        else
        {
            ttm.setEnabled(true);
            logger.logComment("Turning on tool tips");
            jButtonToggleTips.setIcon(imageTips);
        }

        System.gc();
        System.gc();

    }


    void jButtonToggleConsoleOut_actionPerformed(ActionEvent e)
    {
        logger.logComment("Toggle Console out button pressed...");

        if (GeneralProperties.getLogFilePrintToScreenPolicy())
        {
            GeneralProperties.setLogFilePrintToScreenPolicy(false);
            jButtonToggleConsoleOut.setIcon(imageNoConsoleOut);
        }
        else
        {
            GeneralProperties.setLogFilePrintToScreenPolicy(true);
            jButtonToggleConsoleOut.setIcon(imageConsoleOut);
        }

    }


    /**
     * Needed when tool tips state changes in OptionsFrame...
     */
    protected void alertChangeToolTipsState()
    {
        ToolTipManager ttm = ToolTipManager.sharedInstance();

        if (ttm.isEnabled())
        {
            ttm.setEnabled(true);
            jButtonToggleTips.setIcon(imageTips);
        }
        else
        {
            ttm.setEnabled(false);
            jButtonToggleTips.setIcon(imageNoTips);
        }
    }


    /**
     * Needed when console out state changes in OptionsFrame...
     */
    protected void updateConsoleOutState()
    {
        if (GeneralProperties.getLogFilePrintToScreenPolicy())
        {
            jButtonToggleConsoleOut.setIcon(imageConsoleOut);
        }
        else
        {
            jButtonToggleConsoleOut.setIcon(imageNoConsoleOut);
        }

    }



    void jMenuItemProjProperties_actionPerformed(ActionEvent e)
    {
        logger.logComment("Project Preferences menu item selected...");
        doOptionsPane(OptionsFrame.PROJ_PREFERENCES, OptionsFrame.PROJECT_PROPERTIES_MODE);
    }
/*
    void jTextFieldDuration_keyReleased(KeyEvent e)
    {
        logger.logComment("Duration of simulation changed...");

        try
        {
            String text = jTextFieldDuration.getText();
            if (text.length() == 0)
            {
                logger.logComment("They've removed the text...");
                jTextFieldDuration.setText("0");
            }
            else
            {
                Float.parseFloat(text);
            }
            createSimulationSummary();
        }
        catch (NumberFormatException ex)
        {
            GuiUtils.showErrorMessage(logger,
                "Please enter a correctly formatted number for the simulation duration.", ex, this);
        }

    }
*/
    void jTextFieldDT_keyReleased(KeyEvent e)
    {
        logger.logComment("Value of DT changed...");

        try
        {
            Float.parseFloat(jTextFieldSimDT.getText());
            createSimulationSummary();
        }
        catch (NumberFormatException ex)
        {
            // Will be set red by tabUpdated...
        }

    }


    void jButton3DSettings_actionPerformed(ActionEvent e)
    {
        logger.logComment("Setting 3D options...");
        this.doOptionsPane(OptionsFrame.PROJ_PREFERENCES, OptionsFrame.PROJECT_PROPERTIES_MODE);

    }
    
    void jButton3DHelp_actionPerformed(ActionEvent e)
    {
        logger.logComment("Help for 3D requested");
   
        HelpFrame.showGlossaryItem("3D View of Cells");
       
    }

    void jButtonNeuronView_actionPerformed(ActionEvent e)
    {
        logger.logComment("Going to display the selected hoc file...");

        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        try
        {
            String selectedFile = (String)jComboBoxNeuronFileList.getSelectedItem();
            File file = new File(ProjectStructure.getNeuronCodeDir(projManager.getCurrentProject().getProjectMainDirectory()), selectedFile);
            SimpleViewer.showFile(file.getAbsolutePath(), 12, false, false, this.jCheckBoxNeuronLineNums.isSelected());
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Hoc file not yet generated", ex, this);
        }
    }

    void jButtonPynnView_actionPerformed(ActionEvent e)
    {
        logger.logComment("Going to display the selected Pynn file...");

        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }
            
        String selectedFile = (String)jComboBoxPynnFileList.getSelectedItem();
        
        if (selectedFile.equals(defaultPyNNFilesText))
            return;

        try
        {
            File file = new File(ProjectStructure.getPynnCodeDir(projManager.getCurrentProject().getProjectMainDirectory()), selectedFile);
            if (file.getName().endsWith("xml"))
            {
                showHighlightedXML(file, false);
            }
            else
            {
                SimpleViewer.showFile(file.getAbsolutePath(), 12, false, false, this.jCheckBoxPynnLineNums.isSelected());
            }
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Cannot display PyNN file: "+selectedFile, ex, this);
        }
    }

    void jButtonPsicsView_actionPerformed(ActionEvent e)
    {
        logger.logComment("Going to display the selected PSICS file...");

        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }
            
        String selectedFile = (String)jComboBoxPsicsFileList.getSelectedItem();

        if (selectedFile.equals(defaultPsicsFilesText))
            return;
        try
        {
            File file = new File(ProjectStructure.getPsicsCodeDir(projManager.getCurrentProject().getProjectMainDirectory()), selectedFile);
            if (file.getName().endsWith("xml"))
            {
                showHighlightedXML(file, false);
            }
            else
            {
                SimpleViewer.showFile(file.getAbsolutePath(), 12, false, false, this.jCheckBoxPsicsLineNums.isSelected());
            }
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Cannot display PSICS file: "+selectedFile, ex, this);
        }
    }

    void jButtonNetSetAddNew_actionPerformed(ActionEvent e)
    {
        logger.logComment("Adding new Net connection...");

        doNewNetConnection();

    }

    void jButtonCellTypeViewCell_actionPerformed(ActionEvent e)
    {

        Cell selectedCell = (Cell) jComboBoxCellTypes.getSelectedItem();
        if (selectedCell.getInstanceName().equals(cellComboPrompt))
        {
            return;
        }

        logger.logComment("Changing to the view for cell: " + selectedCell.getInstanceName());

        jPanel3DMain.removeAll();
        base3DPanel = null;

        this.validate();

        jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(this.VISUALISATION_TAB));

        jComboBoxView3DChoice.setSelectedItem(selectedCell);

        //this.jButtonConstructCell3DDemo_actionPerformed(null);
        doCreate3D(selectedCell);
    }

    void jButtonCellTypeViewCellChans_actionPerformed(ActionEvent e)
    {

        Cell selectedCell = (Cell) jComboBoxCellTypes.getSelectedItem();
        if (selectedCell.getInstanceName().equals(cellComboPrompt))
        {
            return;
        }

        logger.logComment("Changing to the view for cell: " + selectedCell.getInstanceName());

        jPanel3DMain.removeAll();
        base3DPanel = null;

        this.validate();

        jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(this.VISUALISATION_TAB));

        jComboBoxView3DChoice.setSelectedItem(selectedCell);

        //this.jButtonConstructCell3DDemo_actionPerformed(null);
        doCreate3D(selectedCell);

        if (base3DPanel instanceof OneCell3DPanel)
        {
            ((OneCell3DPanel)base3DPanel).setHighlighted(OneCell3DPanel.highlightDensMechs);
        }

    }





    void jButtonGenerate_actionPerformed(ActionEvent e)
    {
        doGenerate();
    }


    void jButtonGenerateLoad_actionPerformed(ActionEvent e)
    {
        logger.logComment("Loading a previously generated network...");

        final JFileChooser chooser = new JFileChooser();


        chooser.setCurrentDirectory(ProjectStructure.getSavedNetworksDir(projManager.getCurrentProject().getProjectMainDirectory()));

        chooser.setFileFilter(new SimpleFileFilter(new String[]{ProjectStructure.getNeuroMLFileExtension(),
                                                   ProjectStructure.getXMLFileExtension(),
                                                   ProjectStructure.getNeuroMLCompressedFileExtension(),
                                                   ProjectStructure.getHDF5FileExtension()}, "(Plaintext, compressed or HDF5) NetworkML files", true));

        logger.logComment("chooser.getCurrentDirectory(): "+chooser.getCurrentDirectory());

        chooser.setDialogTitle("Choose (text/zipped/HDF5) NetworkML file to load");

        final JTextArea summary = new JTextArea(12,40);
        summary.setMargin(new Insets(5,5,5,5));
        summary.setEditable(false);
        JPanel addedPanel = new JPanel();
        addedPanel.setLayout(new BorderLayout());
        
        JScrollPane jScrollPane = new JScrollPane(summary);
        //jScrollPane.setBorder(BorderFactory.createEtchedBorder());
        addedPanel.add(jScrollPane, BorderLayout.NORTH);

        JPanel viewPanel = new JPanel();
        final JLabel sizeInfo = new JLabel("");
        final JButton openButton = new JButton("View file");
        final JButton editButton = new JButton("Edit externally");

        viewPanel.add(sizeInfo);
        viewPanel.add(openButton);
        viewPanel.add(editButton);
        openButton.setEnabled(false);
        editButton.setEnabled(false);
        addedPanel.add(viewPanel, BorderLayout.SOUTH);
        
        openButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                File newFile = chooser.getSelectedFile();
                if (newFile!=null)
                {
                    chooser.cancelSelection();
                    //System.out.println("Opening file: "+newFile);
                    SimpleViewer.showFile(newFile.getAbsolutePath(), 12, false, false, false);
                }
                
            }
        });
        editButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                File newFile = chooser.getSelectedFile();
                if (newFile!=null)
                {
                    chooser.cancelSelection();
                    
                    String editorPath = GeneralProperties.getEditorPath(true);

                    Runtime rt = Runtime.getRuntime();

                    String command = editorPath + " " + newFile.getAbsolutePath()+"";

                    if (GeneralUtils.isWindowsBasedPlatform() && newFile.getAbsolutePath().indexOf(" " )>=0)
                    {
                        command = editorPath + " \"" + newFile.getAbsolutePath()+"\"";
                    }


                    logger.logComment("Going to execute command: " + command);

                    try
                    {

                        rt.exec(command);
                    }
                    catch (IOException ex)
                    {
                        logger.logError("Error running "+command);
                    }

                    logger.logComment("Have successfully executed command: " + command);
                }
                
            }
        });


        chooser.addPropertyChangeListener(new PropertyChangeListener(){

            public void propertyChange(PropertyChangeEvent e)
            {
                logger.logComment("propertyChange: " + e);
                logger.logComment("getPropertyName: " + e.getPropertyName());
                
                if (e.getPropertyName().equals("SelectedFileChangedProperty"))
                {
                    File newFile = chooser.getSelectedFile();
                    logger.logComment("Looking at: " + newFile);
                    if (newFile!=null && !newFile.isDirectory())
                    {
                        try
                        {
                            if (newFile.getName().endsWith(ProjectStructure.getNeuroMLCompressedFileExtension()))
                            {
                                openButton.setEnabled(false);

                                ZipInputStream zf = new ZipInputStream(new FileInputStream( newFile));
                                ZipEntry ze = null;

                                //summary.setText("Comment: "+zf.getNextEntry().getComment());
                                while ((ze=zf.getNextEntry())!=null)
                                {
                                    logger.logComment("Entry: " +ze );
                                    summary.setText("Contains: "+ze);
                                }
                                summary.setCaretPosition(0);


                            }
                            else
                            {
                                openButton.setEnabled(true);
                                editButton.setEnabled(true);

                                FileReader fr = null;

                                fr = new FileReader(newFile);

                                LineNumberReader reader = new LineNumberReader(fr);
                                String nextLine = null;

                                StringBuilder sb = new StringBuilder();
                                int count = 0;
                                int maxlines = 100;

                                while (count <= maxlines && (nextLine = reader.readLine()) != null)
                                {
                                    sb.append(nextLine + "\n");
                                    count++;
                                }
                                if (count >= maxlines) sb.append("\n\n  ... NetworkML file continues ...");
                                reader.close();
                                fr.close();
                                summary.setText(sb.toString());
                                summary.setCaretPosition(0);

                                sizeInfo.setText("Size: "+ newFile.length()+" bytes");

                            }
                        }
                        catch (Exception ex)
                        {
                            summary.setText("Error loading contents of file: " + newFile);
                        }
                    }
                }

            }

            });

        chooser.setAccessory(addedPanel);

        int retval = chooser.showDialog(this, "Choose network");

        if (retval == JOptionPane.OK_OPTION)
        {
            long start = System.currentTimeMillis();
            try
            {
                
                projManager.getCurrentProject().resetGenerated();

                logger.logComment("Removing 3D network, as it's no longer relevant...");
                doDestroy3D();

                if (this.jCheckBoxRandomGen.isSelected())
                {
                    Random tempRandom = new Random();
                    this.jTextFieldRandomGen.setText(tempRandom.nextInt() + "");
                }

                NetworkMLnCInfo extraInfo = projManager.doLoadNetworkML(chooser.getSelectedFile(), false);
                
                logger.logComment("Elec inputs read: "+ projManager.getCurrentProject().generatedElecInputs);
                
                String prevSimConfig = extraInfo.getSimConfig();
                long randomSeed = extraInfo.getRandomSeed();

                if (randomSeed!=Long.MIN_VALUE)
                {
                    this.jTextFieldRandomGen.setText(randomSeed+"");
                    ProjectManager.setRandomGeneratorSeed(randomSeed);
                    ProjectManager.reinitialiseRandomGenerator();
                }
                if (prevSimConfig!=null)
                {
                    this.jComboBoxSimConfig.setSelectedItem(prevSimConfig);
                }               
                

            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Error loading network info from: "+chooser.getSelectedFile(), ex, this);
                return;
            }
            long end = System.currentTimeMillis();

            SimConfig simConfig = getSelectedSimConfig();
            
            
            Hashtable<String, ArrayList<Integer>> hostsVsProcs = CompNodeGenerator.getHostsVsNumOnProcs(projManager.getCurrentProject(), simConfig);
            
            String compNodesReport = CompNodeGenerator.generateCompNodesReport(hostsVsProcs, simConfig.getMpiConf(), -1);
            
            
            boolean elecInputsReadFromFile = projManager.getCurrentProject().generatedElecInputs.getNumberSingleInputs()>0;
            
            String inputReport = "";
            if (elecInputsReadFromFile)
            {
                inputReport = projManager.getCurrentProject().generatedElecInputs.getHtmlReport();
            }
            String note = "<center><b>NOTE: The following elements have been generated based on Simulation Configuration: "+simConfig.getName()+"</b></center><br>";


            setGeneratorInfo("Cell positions and network connections loaded from: <b>"+chooser.getSelectedFile()+"</b> in "+((end-start)/1000.0)+" seconds<br><br>"
                                            +"<center><b>Cell Groups:</b></center>"
                                            +projManager.getCurrentProject().generatedCellPositions.getHtmlReport()
                                            +"<center><b>Network Connections:</b></center>"
                +projManager.getCurrentProject().generatedNetworkConnections.getHtmlReport(
                                        GeneratedNetworkConnections.ANY_NETWORK_CONNECTION,simConfig)
                                        + compNodesReport+inputReport+"<br>"+note);
            
            

            if (elecInputsReadFromFile)
            {
                projManager.plotSaveGenerator = new PlotSaveGenerator(projManager.getCurrentProject(), this);
                projManager.plotSaveGenerator.setSimConfig(simConfig);
                projManager.plotSaveGenerator.start();
            }
            else
            {
                projManager.elecInputGenerator = new ElecInputGenerator(projManager.getCurrentProject(), this);
                projManager.elecInputGenerator.setSimConfig(simConfig);
                projManager.elecInputGenerator.start();
            }

            sourceOfCellPosnsInMemory = NETWORKML_POSITIONS;

            jComboBoxView3DChoice.setSelectedItem(LATEST_GENERATED_POSITIONS);

        }


    }
    
    void jButtonGenerateSave_actionPerformed(ActionEvent e)
    {
        logger.logComment("Saving the currently generated network");

        logger.logComment("saving the network in NeuroML form...");

        String origText = jButtonGenerateSave.getText();
        jButtonGenerateSave.setText("Saving...");
        jButtonGenerateSave.setEnabled (false);

        File savedNetsDir = ProjectStructure.getSavedNetworksDir(projManager.getCurrentProject().getProjectMainDirectory());

        String timeInfo = GeneralUtils.getCurrentDateAsNiceString() +"_"+GeneralUtils.getCurrentTimeAsNiceString();
        timeInfo = GeneralUtils.replaceAllTokens(timeInfo, ":", "-");

        /*
        boolean warnedReInput=false;
        for(String inptRef: projManager.getCurrentProject().generatedElecInputs.getInputReferences())
        {
            for(SingleElectricalInput sei: projManager.getCurrentProject().generatedElecInputs.getInputLocations(inptRef))
            {
                if (sei.getInstanceProps()!=null && !warnedReInput)
                {
                    GuiUtils.showWarningMessage(logger, "Note that not all of the generated information in this network can currently be saved to NetworkML.\n" +
                            "For example input "+projManager.getCurrentProject().elecInputInfo.getStim(inptRef)+" has some unique values for individual conections, e.g.:\n" +
                            ""+sei+": "+sei.getInstanceProps().details(false)+"\n" +
                            "which cannot be saved and reloaded in NetworkML.", this);
                    warnedReInput = true;
                }
            }
        }*/

        String fileName = "Net_" +timeInfo;

        fileName = JOptionPane.showInputDialog("Please enter the name of the NetworkML file (without extension)",fileName);

        if (fileName == null) return;

        String fullName = fileName;
        
        if (jRadioButtonNMLSavePlainText.isSelected())
            fullName = fileName+ ProjectStructure.getNeuroMLFileExtension();
        if (jRadioButtonNMLSaveZipped.isSelected())
            fullName = fileName+ ProjectStructure.getNeuroMLCompressedFileExtension();
        else if(jRadioButtonNMLSaveHDF5.isSelected())
            fullName = fileName+ ProjectStructure.getHDF5FileExtension();

        File networkFile = new File(savedNetsDir, fullName);

        if (networkFile.exists())
        {

            int goOn = JOptionPane.showConfirmDialog(this, "File: "+ networkFile+" already exists! Overwrite?",
                                                     "Overwrite file?", JOptionPane.YES_NO_OPTION);

            if (goOn == JOptionPane.NO_OPTION)
            {

                jButtonGenerateSave.setText(origText);
                jButtonGenerateSave.setEnabled(true);
                return;

            }
        }
        long start = System.currentTimeMillis();
        File fileSaved = null;

        try
        {
            if (jRadioButtonNMLSavePlainText.isSelected())
            {
                fileSaved = ProjectManager.saveNetworkStructureXML(projManager.getCurrentProject(),
                                                                 networkFile,
                                                                 false,
                                                                 this.jCheckBoxGenerateExtraNetComments.isSelected(),
                                                                 getSelectedSimConfig().getName(),
                                                                 NetworkMLConstants.UNITS_PHYSIOLOGICAL);
            }
            else if (jRadioButtonNMLSaveZipped.isSelected())
            {
                fileSaved = ProjectManager.saveNetworkStructureXML(projManager.getCurrentProject(),
                                                                 networkFile,
                                                                 true,
                                                                 this.jCheckBoxGenerateExtraNetComments.isSelected(),
                                                                 getSelectedSimConfig().getName(),
                                                                 NetworkMLConstants.UNITS_PHYSIOLOGICAL);
            }
            else if(jRadioButtonNMLSaveHDF5.isSelected())
            {
                    fileSaved = ProjectManager.saveNetworkStructureHDF5(projManager.getCurrentProject(),
                                                                  networkFile,
                                                                  getSelectedSimConfig().getName(),
                                                                  NetworkMLConstants.UNITS_PHYSIOLOGICAL);

            }
        }
        catch (Hdf5Exception ex1)
        {
            GuiUtils.showErrorMessage(logger, "Problem saving network in HDF5 form of NetworkML\n" +
                "Note that the jar files for HDF5 (jhdf.jar etc.) should be in the java classpath:\n    "+System.getProperty("java.class.path")
                +"\n and the location of the libraries (libjhdf5.so etc. for Linux, jhdf5.dll etc. for Win)\n" +
                "should be specified in the java.library.path:\n    " +System.getProperty("java.library.path")+
                "\nvariable. \n\nNote also that on a 64 bit Windows system, a 32 bit JVM should be used when using any HDF5 functionality, as the HDF5 dlls are 32bit." +
                "\n\nIt might be best to alter and use the nC.bat/nC.sh files in the install directory.", ex1, this);
        }
        catch (Exception ex1)
        {
            GuiUtils.showErrorMessage(logger, "Problem saving network in NetworkML", ex1, this);
        }
        
        long end = System.currentTimeMillis();
        
        float secs = (end-start)/1000f;



        jButtonGenerateSave.setText(origText);
        jButtonGenerateSave.setEnabled(true);


        refreshTabGenerate();
        
        GuiUtils.showInfoMessage(logger, "NetworkML file saved",
            "The structure of the network has been saved in:\n"+fileSaved.getAbsolutePath()
            +" ("+fileSaved.length()+" bytes)\nin "+secs+" seconds", this);

        return;

    }
    
    

    void jButtonSimConfigEdit_actionPerformed(ActionEvent e)
    {
        SimConfigManager dlg
            = new SimConfigManager(projManager.getCurrentProject().simConfigInfo,
                                   this,
                                   projManager.getCurrentProject());

        dlg.expandToScreen();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = dlg.getSize();

        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;

        dlg.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);


        dlg.setVisible(true);

        dlg.setSelectedSimConfig(getSelectedSimConfig().getName());

        //this.refreshAll();

    }

    private SimConfig getSelectedSimConfig()
    {
        String selectedSimConfig = (String)this.jComboBoxSimConfig.getSelectedItem();

        return  projManager.getCurrentProject().simConfigInfo.getSimConfig(selectedSimConfig);

    }


    private void doGenerate()
    {
        logger.logComment("Going to generate network...");
        
        if (!projManager.projectLoaded()) return;

        projManager.getCurrentProject().simConfigInfo.validateStoredSimConfigs(projManager.getCurrentProject());

        SimConfig simConfig = getSelectedSimConfig();

        if (simConfig.getCellGroups().isEmpty())
        {
            GuiUtils.showErrorMessage(logger,
                                      "Please add one or more Cell groups to Sim Config: "
                                      +simConfig.getName()+" before proceeding", null, this);

        }

        jProgressBarGenerate.setEnabled(true);
        JProgressBar tempPb = new JProgressBar();
        jProgressBarGenerate.setForeground(tempPb.getForeground()); // reset

        logger.logComment("simConfig: "+simConfig.getName());
        logger.logComment("getCellGroups: "+simConfig.getCellGroups());
        logger.logComment("getNetConns: "+simConfig.getNetConns());


        int totalSteps = simConfig.getCellGroups().size()
            + simConfig.getNetConns().size()
            + simConfig.getInputs().size();
        
        if (GeneralUtils.includeParallelFunc() && simConfig.getMpiConf().isParallelOrRemote())
            totalSteps = totalSteps + simConfig.getCellGroups().size(); // for the compute node gen...

        jProgressBarGenerate.setMaximum(totalSteps * 100);
        jProgressBarGenerate.setValue(0);
        jProgressBarGenerate.setString("progress...");

        this.jButtonGenerateStop.setEnabled(true);

        logger.logComment("Removing 3D network, as it's no longer relevant...");
        doDestroy3D();

        if (this.jCheckBoxRandomGen.isSelected())
        {
            Random tempRandom = new Random();
            this.jTextFieldRandomGen.setText(tempRandom.nextInt()+"");
        }

        long seed = 0;
        try
        {
            seed = Long.parseLong(jTextFieldRandomGen.getText());
        }
        catch (NumberFormatException ex)
        {
            GuiUtils.showErrorMessage(logger, "Please enter a valid long integer into the"
                                      +" field for the random number generator seed", ex, this);
            return;
        }

        projManager.doGenerate(simConfig.getName(), seed);

        this.jButton3DView.setEnabled(true);
        this.jButton3DPrevSimuls.setEnabled(true);
        this.jButton3DQuickSims.setEnabled(true);
        setGeneratorInfo(generatePleaseWait);

        sourceOfCellPosnsInMemory = GENERATED_POSITIONS;

        jComboBoxView3DChoice.setSelectedItem(LATEST_GENERATED_POSITIONS);

        // need this to update list of 3d position files...
        refreshTab3D();
        
        //jComboBoxSimConfig.repaint();
        jComboBoxSimConfig.setSelectedItem(simConfig.getName());
        
        
        
        logger.logComment("Finished generating network!");
        logger.logComment("Num cells generated: "+ projManager.getCurrentProject().generatedCellPositions.getNumberInAllCellGroups());
        

    }

    void jButtonRegionRemove_actionPerformed(ActionEvent e)
    {
        logger.logComment("Going to remove region...");

        doRemoveRegion();

        refreshTabRegionsInfo();

    }

    void jButtonCellGroupDelete_actionPerformed(ActionEvent e)
    {
        logger.logComment("Going to remove cell group...");
        int selectedRow = jTableCellGroups.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }

        String cellGroupName = (String) projManager.getCurrentProject().cellGroupsInfo.getValueAt(selectedRow,
            CellGroupsInfo.COL_NUM_CELLGROUPNAME);

        doRemoveCellGroup(cellGroupName);
        refreshTabCellGroupInfo();

    }

    void jButtonNetConnDelete_actionPerformed(ActionEvent e)
    {
        logger.logComment("Going to remove net conn...");
        doRemoveNetConn();
        refreshTabNetSettings();

    }
    /*
    void jMenuItemNmodlEditor_actionPerformed(ActionEvent e)
    {
        logger.logComment("nmodlEditor to run...");

        String dir = null;

        if (projManager.getCurrentProject()!=null)
            dir = projManager.getCurrentProject().getProjectMainDirectory().getAbs````olutePath();
        else
            dir = GeneralProperties.getnCProjectsDir().getAbsolutePath();

        new NmodlEditorApp(dir);
    }

    void jButtonSynapseAdd_actionPerformed(ActionEvent e)
    {
        logger.logComment("Adding new Synapse Type");
        doCreateNewSynapseType();
        refreshTabNmodl();
    }*/
/*
    void jRadioButtonStimIClamp_itemStateChanged(ItemEvent e)
    {
        logger.logComment("jRadioButtonStimIClamp altered: ");
        if (e.getStateChange() == ItemEvent.SELECTED)
        {

            logger.logComment("Adding stuff for IClamp...");
            jPanelSimParams.removeAll();
            JPanel tempPanel1 = new JPanel();
            tempPanel1.setSize(500, 30);
            tempPanel1.add(new JLabel("Amplitude of stimulation: "));
            tempPanel1.add(jTextFieldIClampAmplitude);
            jPanelSimParams.add(tempPanel1);
            JPanel tempPanel2 = new JPanel();
            tempPanel2.setSize(500, 30);
            tempPanel2.add(new JLabel("Duration of stimulation: "));
            tempPanel2.add(jTextFieldIClampDuration);
            jPanelSimParams.add(tempPanel2);
            jPanelSimParams.validate();
        }
    }

    void jRadioButtonStimNone_itemStateChanged(ItemEvent e)
    {
        logger.logComment("jRadioButtonStimNone altered: ");
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
            jPanelSimParams.removeAll();
            JPanel tempPanel1 = new JPanel();
            tempPanel1.setSize(600, 100);
            jPanelSimParams.add(tempPanel1);

            jPanelSimParams.validate();
        }

    }

    void jRadioButtonStimNetStim_itemStateChanged(ItemEvent e)
    {
        logger.logComment("jRadioButtonStimNetStim altered: ");
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
            logger.logComment("Adding stuff for NetStim...");
            jPanelSimParams.removeAll();
            JPanel tempPanel1 = new JPanel();
            tempPanel1.setSize(500, 30);
            tempPanel1.add(new JLabel("Average number of spikes"));
            tempPanel1.add(jTextFieldNetStimNumber);
            jPanelSimParams.add(tempPanel1);
            JPanel tempPanel2 = new JPanel();
            tempPanel2.setSize(500, 30);
            tempPanel2.add(new JLabel("Noise (0: no noise -> 1: noisiest)"));
            tempPanel2.add(jTextFieldNetStimNoise);
            jPanelSimParams.add(tempPanel2);
            jPanelSimParams.validate();
        }

    }
*/
/*
    void jButtonSynapseEdit_actionPerformed(ActionEvent e)
    {
        int selectedRow = jTableSynapses.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }
        String type = (String)projManager.getCurrentProject().synapticProcessInfo.getValueAt(selectedRow, SynapticProcessInfo.COL_NUM_TYPE);


        String filename = (String)projManager.getCurrentProject().synapticProcessInfo.getValueAt(selectedRow, SynapticProcessInfo.COL_NUM_FILE);

        logger.logComment("Filename corresponding to row "+selectedRow+": "+ filename);

        NmodlEditorApp nmodlApp = new NmodlEditorApp(projManager.getCurrentProject().getProjectMainDirectory().getAbsolutePath());
        if (type.equals("Inbuilt synapse"))
        {
            GuiUtils.showInfoMessage(logger, "Information", "Note that this is an inbuilt NEURON synapse, and cannot be changed./nTo create a synapse <i>like</i> this select Add Custom Synapse and select this file as a template", this);
            nmodlApp.editModFile(filename, true);
        }
        else nmodlApp.editModFile(filename, false);
    }
*/
    void jMenuItemGeneralProps_actionPerformed(ActionEvent e)
    {
        logger.logComment("General Preferences menu item selected...");
        doOptionsPane(OptionsFrame.GENERAL_PREFERENCES, OptionsFrame.GENERAL_PROPERTIES_MODE);

    }


    public void jMenuRecentFile_actionPerformed(ActionEvent e)
    {
        logger.logComment("Action event: "+e);
        JMenuItem menuItem =(JMenuItem)e.getSource();
        String recentFileName = menuItem.getText();
        if (menuItem.getToolTipText()!=null && menuItem.getToolTipText().length()>0)
            recentFileName = menuItem.getToolTipText();
        logger.logComment("Opening recent file: "+recentFileName);

        File recentFile = new File(recentFileName);

        if (!recentFile.exists())
        {
            String tryFilename = GeneralUtils.replaceAllTokens(recentFileName, "neuro.xml", "ncx");
            File recentncxFile = new File(tryFilename);
            
            if(recentncxFile.exists())
            {
                recentFile = recentncxFile;
            }
            else
            {
                GuiUtils.showErrorMessage(logger, "The file: "+recentFileName+" doesn't exist...", null, this);
                recentFiles.removeFromList(recentFile);
                refreshAll();
                return;
            }
        }

        boolean continueClosing = checkToSave();
        if (!continueClosing) return;
        closeProject();

        initialisingProject = true;

        try
        {
            projManager.setCurrentProject(Project.loadProject(recentFile, this));
            logger.logComment("---------------  Proj status: "+ projManager.getCurrentProject().getProjectStatusAsString());
        }
        catch (ProjectFileParsingException ex2)
        {
            recentFiles.removeFromList(recentFile);
            GuiUtils.showErrorMessage(logger, ex2.getMessage(), ex2, this);
            initialisingProject = false;
            closeProject();
            return;
        }

        // to make sure it's first...
        recentFiles.addToList(recentFile.getAbsolutePath());

        refreshAll();
        enableTableCellEditingFunctionality();


        initialisingProject = false;
        createSimulationSummary();

        jTabbedPaneMain.setSelectedIndex(0); // main tab...

    }

    void jButtonGenerateStop_actionPerformed(ActionEvent e)
    {
        logger.logComment("Telling the cell posn generator to stop...");
        projManager.cellPosnGenerator.stopGeneration();
        if (projManager.netConnGenerator!=null)
        {
            logger.logComment("Telling the net conn generator to stop...");
            this.projManager.netConnGenerator.stopGeneration();
        }
        if (projManager.arbourConnectionGenerator!=null)
        {
            logger.logComment("Telling the arbourConnectionGenerator to stop...");
            this.projManager.arbourConnectionGenerator.stopGeneration();
        }

        if (projManager.elecInputGenerator!=null)
        {
            logger.logComment("Telling the elecInputGenerator to stop...");
            this.projManager.elecInputGenerator.stopGeneration();
        }



        jProgressBarGenerate.setValue(0);
        jProgressBarGenerate.setString("Generation stopped");

    }

    void jButtonNetConnEdit_actionPerformed(ActionEvent e)
    {
        logger.logComment("Going to edit net conn...");
        doEditNetConn();
        refreshTabNetSettings();
    }

    void jButtonAnalyseConns_actionPerformed(ActionEvent e)
    {
        logger.logComment("Analyse lengths pressed...");
        String selectedItem = (String) jComboBoxAnalyseNetConn.getSelectedItem();
        if (selectedItem.equals(defaultAnalyseNetConnString))
        {
            GuiUtils.showErrorMessage(logger,
                                      "Please select which Network Connection whose lengths you would like to analyse", null, this);
            return;
        }

        projManager.doAnalyseLengths(selectedItem);
    }
    
    
    void jButtonNetworkFullInfo_actionPerformed(ActionEvent e)
    {
        logger.logComment("jButtonNetworkFullInfo pressed...");
        
        StringBuilder netInfo = new StringBuilder();
        
        netInfo.append(projManager.getCurrentProject().generatedCellPositions.toLongString(true));
        netInfo.append(projManager.getCurrentProject().generatedNetworkConnections.details(true));
        netInfo.append(projManager.getCurrentProject().generatedElecInputs.details(true));
        
        SimpleViewer sv = SimpleViewer.showString(netInfo.toString(),
                                "Current network info for project: "+ projManager.getCurrentProject().getProjectFileName(),
                                      12,
                                      false,
                                      true);
        
        sv.addHyperlinkListener(this);
        
        sv.repaint();
        
    }

    void jButtonAnalyseNumConns_actionPerformed(ActionEvent e)
    {
        logger.logComment("Analyse numbers of connections pressed...");

        doAnalyseNumConns();


    }

    void jButtonAnalyseCellDensities_actionPerformed(ActionEvent e)
    {
        logger.logComment("Analyse cell densities pressed...");

        doAnalyseCellDensities();

    }


/*
    void jButtonChanMechEdit_actionPerformed(ActionEvent e)
    {
        int selectedRow = jTableChanMechs.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }
        String filename = (String)projManager.getCurrentProject().channelMechanismInfo.getValueAt(selectedRow, ChannelMechanismInfo.COL_NUM_FILE);
        String type = (String)projManager.getCurrentProject().channelMechanismInfo.getValueAt(selectedRow, ChannelMechanismInfo.COL_NUM_TYPE);

        logger.logComment("Filename corresponding to row "+selectedRow+": "+ filename);


        NmodlEditorApp nmodlApp = new NmodlEditorApp(projManager.getCurrentProject().getProjectMainDirectory().getAbsolutePath());
        if (type.equals("Inbuilt channel mechanism"))
        {
            GuiUtils.showInfoMessage(logger, "Information", "Note that this is an inbuilt NEURON channel mechanism, and cannot be changed.\nTo create a channel mechanism <i>like</i> this select Add Channel Mechanism and select this file as a template", this);
            nmodlApp.editModFile(filename, true);
        }
        else nmodlApp.editModFile(filename, false);


    }

    void jButtonChanMechAdd_actionPerformed(ActionEvent e)
    {
        logger.logComment("Adding new Chan Mech");
        doCreateNewChannelMechanism();
        ///refreshTabNmodl();

    }*/

    void jMenuItemZipUp_actionPerformed(ActionEvent e)
    {
        boolean allOk = checkToSave();

        if (!allOk) return; // i.e. cancelled...

        logger.logComment("Zipping up the project...");
        String nameOfZippedFile = null;
        //String projectDir = null;

        nameOfZippedFile = projManager.getCurrentProject().getProjectMainDirectory()
            + System.getProperty("file.separator")
            + projManager.getCurrentProject().getProjectName()
            + ProjectStructure.getNewProjectZipFileExtension();

        ProjectManager.zipDirectoryContents(projManager.getCurrentProject().getProjectMainDirectory(),
            nameOfZippedFile);
    }


    void jMenuItemUnzipProject_actionPerformed(ActionEvent e)
    {
        doUnzipProject();
    }
    
    void  jMenuItemImportLevel123_actionPerformed(ActionEvent e)
    {
        logger.logComment("Loading elements from NeuroML files levels 1-3...");

        boolean freshProject = projManager.getCurrentProject()==null ||
            (projManager.getCurrentProject().cellManager.getNumberCellTypes()==0 &&
            projManager.getCurrentProject().cellMechanismInfo.getAllCellMechanismNames().isEmpty());

        if (projManager.getCurrentProject() == null)
        {
            int yesNo = JOptionPane.showConfirmDialog(this,"There is no project currently loaded. Would you like to create a new project to load the NeuroML file into?"
                , "Create new project for NeuroML?", JOptionPane.YES_NO_OPTION);

            if (yesNo==JOptionPane.YES_OPTION)
            {
                doNewProject(false);
            }
            else
            {
                return;
            }
        }


        final JFileChooser chooser = new JFileChooser();

        chooser.setCurrentDirectory(ProjectStructure.getNeuroMLDir(projManager.getCurrentProject().getProjectMainDirectory()));

//        chooser.setFileFilter(new SimpleFileFilter(new String[]{".net.xml"}, "Level 3 NeuroML Network files", true));

        logger.logComment("chooser.getCurrentDirectory(): "+chooser.getCurrentDirectory());

        chooser.setDialogTitle("Choose NeuroML file to load");

        final JTextArea summary = new JTextArea(12,40);
        summary.setMargin(new Insets(5,5,5,5));
        summary.setEditable(false);
        JPanel addedPanel = new JPanel();
        addedPanel.setLayout(new BorderLayout());
        
        JScrollPane jScrollPane = new JScrollPane(summary);
        //jScrollPane.setBorder(BorderFactory.createEtchedBorder());
        addedPanel.add(jScrollPane, BorderLayout.NORTH);

        JPanel viewPanel = new JPanel();
        final JLabel sizeInfo = new JLabel("");
        final JButton openButton = new JButton("View file");
        final JButton editButton = new JButton("Edit externally");

        viewPanel.add(sizeInfo);
        viewPanel.add(openButton);
        viewPanel.add(editButton);
        openButton.setEnabled(false);
        editButton.setEnabled(false);
        addedPanel.add(viewPanel, BorderLayout.SOUTH);
        
        openButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                File newFile = chooser.getSelectedFile();
                if (newFile!=null)
                {
                    chooser.cancelSelection();
                    //System.out.println("Opening file: "+newFile);
                    SimpleViewer.showFile(newFile.getAbsolutePath(), 12, false, false, false);
                }
                
            }
        });
        editButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                File newFile = chooser.getSelectedFile();
                if (newFile!=null)
                {
                    chooser.cancelSelection();
                    
                    String editorPath = GeneralProperties.getEditorPath(true);

                    Runtime rt = Runtime.getRuntime();

                    String command = editorPath + " " + newFile.getAbsolutePath()+"";

                    if (GeneralUtils.isWindowsBasedPlatform() && newFile.getAbsolutePath().indexOf(" " )>=0)
                    {
                        command = editorPath + " \"" + newFile.getAbsolutePath()+"\"";
                    }


                    logger.logComment("Going to execute command: " + command);

                    try
                    {

                        rt.exec(command);
                    }
                    catch (IOException ex)
                    {
                        logger.logError("Error running "+command);
                    }

                    logger.logComment("Have successfully executed command: " + command);
                }
                
            }
        });


        chooser.addPropertyChangeListener(new PropertyChangeListener(){

            public void propertyChange(PropertyChangeEvent e)
            {
                logger.logComment("propertyChange: " + e);
                logger.logComment("getPropertyName: " + e.getPropertyName());
                
                if (e.getPropertyName().equals("SelectedFileChangedProperty"))
                {
                    File newFile = chooser.getSelectedFile();
                    logger.logComment("Looking at: " + newFile);
                    try
                    {
                        if (newFile.getName().endsWith(ProjectStructure.getNeuroMLCompressedFileExtension()))
                        {
                            openButton.setEnabled(false);
                            
                            ZipInputStream zf = new ZipInputStream(new FileInputStream( newFile));
                            ZipEntry ze = null;

                            //summary.setText("Comment: "+zf.getNextEntry().getComment());
                            while ((ze=zf.getNextEntry())!=null)
                            {
                                logger.logComment("Entry: " +ze );
                                summary.setText("Contains: "+ze);
                            }
                            summary.setCaretPosition(0);


                        }
                        else
                        {
                            openButton.setEnabled(true);
                            editButton.setEnabled(true);

                            FileReader fr = null;

                            fr = new FileReader(newFile);

                            LineNumberReader reader = new LineNumberReader(fr);
                            String nextLine = null;

                            StringBuilder sb = new StringBuilder();
                            int count = 0;
                            int maxlines = 100;

                            while (count <= maxlines && (nextLine = reader.readLine()) != null)
                            {
                                sb.append(nextLine + "\n");
                                count++;
                            }
                            if (count >= maxlines) sb.append("\n\n  ... NetworkML file continues ...");
                            reader.close();
                            fr.close();
                            summary.setText(sb.toString());
                            summary.setCaretPosition(0);
                            
                            sizeInfo.setText("Size: "+ newFile.length()+" bytes");

                        }
                    }
                    catch (Exception ex)
                    {
                        summary.setText("Error loading contents of file: " + newFile);
                    }
                }

            }

            });

        chooser.setAccessory(addedPanel);

        int retval = chooser.showDialog(this, "Choose NeuroML file");

        if (retval == JOptionPane.OK_OPTION)
        {
            File nmlFile = chooser.getSelectedFile();
            doImportNeuroML(nmlFile, false, freshProject);
        }
    }

    protected void doImportNeuroML(File nmlFile, boolean acceptDefaults, boolean confirmImport)
    {
        if (nmlFile==null || !nmlFile.exists())
        {
            GuiUtils.showErrorMessage(logger, "Problem locating file: "+ nmlFile, null, this);
        }
        else
        {
            long start = System.currentTimeMillis();

            try
            {
                projManager.getCurrentProject().resetGenerated();

                logger.logComment("Removing 3D network, as it's no longer relevant...");
                doDestroy3D();

                if (this.jCheckBoxRandomGen.isSelected())
                {
                    Random tempRandom = new Random();
                    this.jTextFieldRandomGen.setText(tempRandom.nextInt() + "");
                }

                NetworkMLnCInfo extraInfo = projManager.doLoadNetworkML(nmlFile, acceptDefaults);
                
                logger.logComment("Elec inputs read: "+ projManager.getCurrentProject().generatedElecInputs);
                
                String prevSimConfig = extraInfo.getSimConfig();
                long randomSeed = extraInfo.getRandomSeed();

                if (randomSeed!=Long.MIN_VALUE)
                {
                    this.jTextFieldRandomGen.setText(randomSeed+"");
                    ProjectManager.setRandomGeneratorSeed(randomSeed);
                    ProjectManager.reinitialiseRandomGenerator();
                }
                if (prevSimConfig!=null)
                {
                    this.jComboBoxSimConfig.setSelectedItem(prevSimConfig);
                }               
                

            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Error loading network info from: "+nmlFile, ex, this);
                return;
            }
            long end = System.currentTimeMillis();

            SimConfig simConfig = getSelectedSimConfig();
            
            
            Hashtable<String, ArrayList<Integer>> hostsVsProcs = CompNodeGenerator.getHostsVsNumOnProcs(projManager.getCurrentProject(), simConfig);
            
            String compNodesReport = CompNodeGenerator.generateCompNodesReport(hostsVsProcs, simConfig.getMpiConf(), -1);
            
            
            boolean elecInputsReadFromFile = projManager.getCurrentProject().generatedElecInputs.getNumberSingleInputs()>0;
            
            String inputReport = "";
            if (elecInputsReadFromFile)
            {
                inputReport = projManager.getCurrentProject().generatedElecInputs.getHtmlReport();
            }
            String note = "<center><b>NOTE: The following elements have been generated based on Simulation Configuration: "+simConfig.getName()+"</b></center><br>";

            setGeneratorInfo("Cell positions and network connections loaded from: <b>"+nmlFile.getAbsolutePath()+"</b> in "+((end-start)/1000.0)+" seconds<br><br>"
                                            +"<center><b>Cell Groups:</b></center>"
                                            +projManager.getCurrentProject().generatedCellPositions.getHtmlReport()
                                            +"<center><b>Network Connections:</b></center>"
                +projManager.getCurrentProject().generatedNetworkConnections.getHtmlReport(
                                        GeneratedNetworkConnections.ANY_NETWORK_CONNECTION,simConfig)
                                        + compNodesReport+inputReport+"<br>"+note);
            
            

            if (elecInputsReadFromFile)
            {
                projManager.plotSaveGenerator = new PlotSaveGenerator(projManager.getCurrentProject(), this);
                projManager.plotSaveGenerator.setSimConfig(simConfig);
                projManager.plotSaveGenerator.start();
            }
            else
            {
                projManager.elecInputGenerator = new ElecInputGenerator(projManager.getCurrentProject(), this);
                projManager.elecInputGenerator.setSimConfig(simConfig);
                projManager.elecInputGenerator.start();
            }

            sourceOfCellPosnsInMemory = NETWORKML_POSITIONS;

            jComboBoxView3DChoice.setSelectedItem(LATEST_GENERATED_POSITIONS);

            if (confirmImport)
            {
                projManager.getCurrentProject().setProjectDescription("neuroConstruct project generated from contents" +
                    " of file: "+ nmlFile+"\n\nThe cell positions & network connections in memory reflect the " +
                    "instances in the NetworkML elements of the imported file. Regenerating the network in " +
                    "neuroConstruct nmay lead to a different network structure.");
            }
            else
            {
                logger.logComment("There had been other elements in this project");
            }

            logger.logComment("Refreshing all...");
            refreshTabCellTypes();
            refreshTabProjectInfo();

            refreshAll();
        }


    }


    void jComboBoxView3DChoice_popupMenuWillBecomeVisible(PopupMenuEvent e)
    {

        logger.logComment("popupMenuWillBecomeVisible pressed...");
        refreshView3DComboBox();

    }

    void jButtonCellGroupsEdit_actionPerformed(ActionEvent e)
    {
        logger.logComment("Editing a cell group...");

        doEditCellGroup();
    }

    void jButtonNetAAAdd_actionPerformed(ActionEvent e)
    {
        logger.logComment("Adding new AA net con...");

        doNewAAConn();
    }

    void jButtonNetAAEdit_actionPerformed(ActionEvent e)
    {
        logger.logComment("Going to edit net conn...");
        doEditVolConn();
        refreshTabNetSettings();

    }

    void jButtonNetAADelete_actionPerformed(ActionEvent e)
    {
        logger.logComment("Going to remove net conn...");
        doRemoveAANetConn();
        refreshTabNetSettings();

    }

    void jMenuItemViewProjSource_actionPerformed(ActionEvent e)
    {
        logger.logComment("Going to show project file...");
        boolean proceed = checkToSave();
        if (proceed)
        {
            try
            {
                SimpleXMLDocument sourceXML = SimpleXMLReader.getSimpleXMLDoc(projManager.getCurrentProject().getProjectFile());

                SimpleViewer.showString(sourceXML.getXMLString("    ", true),
                                        "neuroConstruct project file:" + projManager.getCurrentProject().getProjectFullFileName(),
                                      12,
                                      false,
                                      true);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem showing project source file...", ex, this);
            }
        }

    }

    void jMenuItemGlossary_actionPerformed(ActionEvent e)
    {
        logger.logComment("Going to show glossary...");

        HelpFrame.showGlossaryItem("");

    }
    void jMenuItemRelNotes_actionPerformed(ActionEvent e)
    {
        logger.logComment("Going to show rel notes...");


        File f = ProjectStructure.getRelNotesFile();
        
        try
        {
            HelpFrame.showFrame(f.toURI().toURL(), "neuroConstruct Release notes", false);
        }
        catch (MalformedURLException m)
        {
            
        }

    }
    
    void jMenuItemCheckUpdates_actionPerformed(ActionEvent e)
    {
        logger.logComment("Check for updates...");
        
        String browserPath = GeneralProperties.getBrowserPath(true);
        if (browserPath==null)
        {
            GuiUtils.showErrorMessage(logger, "Could not start a browser!", null, this);
            return;
        }
            
        
        Runtime rt = Runtime.getRuntime();

        String command = browserPath + " " + ProjectStructure.getUpdateCheckUrl()+GeneralProperties.getVersionNumber();

        logger.logComment("Going to execute command: " + command);

        try
        {
            rt.exec(command);
        }
        catch (IOException ex)
        {
            logger.logError("Error running " + command);
        }

        logger.logComment("Have successfully executed command: " + command);
    }
    



    void jButtonCellTypeViewInfo_actionPerformed(ActionEvent e)
    {
        Cell cell = (Cell)jComboBoxCellTypes.getSelectedItem();

        boolean html = true;


        if (cell.getAllSegments().size()>500) html = false;

        SimpleViewer.showString(CellTopologyHelper.printDetails(cell, this.projManager.getCurrentProject(), html),
                                "Cell Info for: "+ cell.toString(), 11, false, html);


    }


    void jButtonCellTypeCompare_actionPerformed(ActionEvent e)
    {

        Cell cellTypeToComp = (Cell) jComboBoxCellTypes.getSelectedItem();
        
        String[] otherProject = new String[2];
        otherProject[0] = "this project";
        otherProject[1] = "another project";
        String project = (String) JOptionPane.showInputDialog(this,
                                                                "Compare the cell " +
                                                                cellTypeToComp.getInstanceName() + " with a cell from:",
                                                                "Select project",
                                                                JOptionPane.QUESTION_MESSAGE,
                                                                null,
                                                                otherProject,
                                                                otherProject[0]);    
        
        if (project.equals(otherProject[0]))
        {
            ArrayList<String> names = projManager.getCurrentProject().cellManager.getAllCellTypeNames();         

            if (names.size()==1)
            {
                GuiUtils.showErrorMessage(logger, "There is only a single cell in this project, nothing to compare it to.", null, this);
                return;
            }

            String[] otherNames = new String[names.size()-1];
            int count = 0;
            for (int i = 0; i < names.size(); i++)
            {
                if (!names.get(i).equals(cellTypeToComp.getInstanceName()))
                {
                    otherNames[count] = names.get(i);
                    count++;
                }
            }

            String selection = (String) JOptionPane.showInputDialog(this,
                                                                    "Please select the Cell Type to compare " +
                                                                    cellTypeToComp.getInstanceName() + " to",
                                                                    "Select Cell Type",
                                                                    JOptionPane.QUESTION_MESSAGE,
                                                                    null,
                                                                    otherNames,
                                                                    otherNames[0]);

            Cell otherCell = projManager.getCurrentProject().cellManager.getCell(selection);

            String comp = CellTopologyHelper.compare(cellTypeToComp, otherCell, true);

            SimpleViewer.showString(comp, "Comparison of "+cellTypeToComp+" with "+ otherCell, 12, false, true);
        }
        
        else if (project.equals(otherProject[1]))
        {
            // set to parent of project dir...
            File defaultDir = projManager.getCurrentProject().getProjectFile().getParentFile().getParentFile();            

            Frame frame = (Frame)this;
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setDialogTitle("Choose neuroConstruct project");

            try
            {
                chooser.setCurrentDirectory(defaultDir);
                logger.logComment("Set Dialog dir to: " + defaultDir);
            }
            catch (Exception ex)
            {
                logger.logError("Problem with default dir setting: " + defaultDir, ex);
            }
            SimpleFileFilter fileFilter = ProjectStructure.getProjectFileFilter();

            chooser.setFileFilter(fileFilter);

            int retval = chooser.showDialog(frame, null);

            if (retval == JFileChooser.APPROVE_OPTION)
            {
                try
                {
                    Project firstProj = projManager.loadProject(projManager.getCurrentProject().getProjectFile());
                       
                    logger.logComment(">>>>  Loading project: " + chooser.getSelectedFile());

                    Project otherProj = Project.loadProject(chooser.getSelectedFile(), this);

                    logger.logComment("<<<<  Loaded project: " + otherProj.getProjectFileName());

                    ArrayList<String> otherCellTypes = otherProj.cellManager.getAllCellTypeNames();

                    if (otherCellTypes.isEmpty())
                    {
                        GuiUtils.showErrorMessage(logger, "No Cell Types found in that project.", null, this);
                        return;
                    }

                    Object selection = JOptionPane.showInputDialog(this,
                                "Please select the Cell Type from project "+otherProj.getProjectName()+" with which to compare "+cellTypeToComp.getInstanceName(),
                                "Select Cell Type",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                otherCellTypes.toArray(),
                                otherCellTypes.get(0));

                    if (selection==null)
                    {
                        logger.logComment("No selection made...");
                        return;
                    }
                    logger.logComment("Selection: "+ selection);

                    Cell comparedCell = otherProj.cellManager.getCell((String)selection);

                    String comparedCellTypeName = comparedCell.getInstanceName();
                    
                    Cell otherCell = projManager.getCurrentProject().cellManager.getCell(comparedCellTypeName);

                    String comp = CellTopologyHelper.compare(cellTypeToComp, otherCell, true, firstProj, otherProj);

                    SimpleViewer.showString(comp, "Comparison of "+cellTypeToComp+" with "+ otherCell, 12, false, true);
                    
                }
                catch (Exception ex2)
                {
                    GuiUtils.showErrorMessage(logger, "Problem comparing the Cell", ex2, this);

                    return;
                }
            }

        }
      

    }


    void jButtonCellTypeDelete_actionPerformed(ActionEvent e)
    {

        Cell cellTypeToDelete = (Cell) jComboBoxCellTypes.getSelectedItem();

        logger.logComment("Deleting cell: "+ cellTypeToDelete);

        if (cellTypeToDelete==null)
        {
            GuiUtils.showErrorMessage(logger, "Problem deleting that Cell Type", null, this);
            return;
        }

        Object[] options1 = {"Continue", "Cancel"};

        JOptionPane option1 = new JOptionPane("Warning, if you delete this Cell Type:\n"+cellTypeToDelete+",\nrecorded simulations which use this Cell Type will not work anymore!",
                                             JOptionPane.DEFAULT_OPTION,
                                             JOptionPane.WARNING_MESSAGE,
                                             null,
                                             options1,
                                             options1[0]);

        JDialog dialog1 = option1.createDialog(this, "Warning");
        dialog1.setVisible(true);

        Object choice = option1.getValue();
        logger.logComment("User has chosen: " + choice);
        if (choice.equals("Cancel")) return;

        Vector cellGroupsUsingIt = projManager.getCurrentProject().cellGroupsInfo.getCellGroupsUsingCellType(cellTypeToDelete.getInstanceName());

        if (cellGroupsUsingIt.size() > 0)
        {
            StringBuilder errorString = new StringBuilder("The Cell Group");
            if (cellGroupsUsingIt.size() > 1) errorString.append("s: ");
            else errorString.append(": ");
            String buttonText = null;

            for (int i = 0; i < cellGroupsUsingIt.size(); i++)
            {
                errorString.append(" " + cellGroupsUsingIt.elementAt(i));
                if (i < cellGroupsUsingIt.size() - 1) errorString.append(", ");
            }
            if (cellGroupsUsingIt.size() > 1)
            {
                errorString.append(" use Cell Type: " + cellTypeToDelete.getInstanceName() + ". Delete these too?");
                buttonText = "Delete Cell Groups";
            }
            else
            {
                errorString.append(" uses Cell Type: " + cellTypeToDelete.getInstanceName() + ". Delete this too?");
                buttonText = "Delete Cell Group";
            }

            Object[] options2 =
                {buttonText, "Cancel All"};

            JOptionPane option2 = new JOptionPane(errorString.toString(),
                                                 JOptionPane.DEFAULT_OPTION,
                                                 JOptionPane.WARNING_MESSAGE,
                                                 null,
                                                 options2,
                                                 options2[0]);

            JDialog dialog2 = option2.createDialog(this, "Warning");
            dialog2.setVisible(true);

            Object choice2 = option2.getValue();
            logger.logComment("User has chosen: " + choice2);
            if (choice2.equals("Cancel All"))
            {
                logger.logComment("User has changed their mind...");
                return;
            }

            for (int i = 0; i < cellGroupsUsingIt.size(); i++)
            {
                String nextCellGroup = (String) cellGroupsUsingIt.elementAt(i);
                logger.logComment("Deleting: " + nextCellGroup);

                doRemoveCellGroup(nextCellGroup);

            }
        }

        projManager.getCurrentProject().cellManager.deleteCellType(cellTypeToDelete);

        File cellFileXml = new File(ProjectStructure.getMorphologiesDir(projManager.getCurrentProject().
                                                                        getProjectMainDirectory())
                                    , cellTypeToDelete.getInstanceName() + ProjectStructure.getJavaXMLFileExtension());

        cellFileXml.delete();

        File cellFileObj = new File(ProjectStructure.getMorphologiesDir(projManager.getCurrentProject().
                                                                        getProjectMainDirectory())
                                    , cellTypeToDelete.getInstanceName() + ProjectStructure.getJavaObjFileExtension());

        cellFileObj.delete();



        projManager.getCurrentProject().markProjectAsEdited();

        refreshTabCellTypes();
    }

    void jButtonCellTypeCopy_actionPerformed(ActionEvent e)
    {
        Cell cellTypeToCopy= (Cell) jComboBoxCellTypes.getSelectedItem();

        logger.logComment("Copying cell: "+ cellTypeToCopy);

        if (cellTypeToCopy == null)
        {
            GuiUtils.showErrorMessage(logger, "Problem copying that Cell Type", null, this);
            return;
        }
        String proposedName = null;
        if (cellTypeToCopy.getInstanceName().indexOf("_")>0)
        {
            int underScoreIndex = cellTypeToCopy.getInstanceName().lastIndexOf("_");
            try
            {
                String val = cellTypeToCopy.getInstanceName().substring(underScoreIndex+1);
                int newVal = Integer.parseInt(val) + 1;
                proposedName
                    = cellTypeToCopy.getInstanceName().substring(0,underScoreIndex)+"_"+newVal;
            }
            catch(NumberFormatException nfe)
            {
                proposedName = cellTypeToCopy.getInstanceName()+"_1";
            }
        }
        else
        {
            proposedName = cellTypeToCopy.getInstanceName()+"_1";
        }
        String newCellName = JOptionPane.showInputDialog(this, "Please enter the name of the new Cell Type", proposedName);


        if (newCellName == null) return; // cancelled...

        Cell newCell = (Cell)cellTypeToCopy.clone();
        newCell.setInstanceName(newCellName);

        try
        {
            projManager.getCurrentProject().cellManager.addCellType(newCell);
        }
        catch (NamingException ex2)
        {
            GuiUtils.showErrorMessage(logger, "Problem with the name of that Cell Type", ex2, this);
            return;
        }
        projManager.getCurrentProject().markProjectAsEdited();

        refreshTabCellTypes();

        jComboBoxCellTypes.setSelectedItem(newCell);
    }

    void jButtonCellTypesMoveToOrigin_actionPerformed(ActionEvent e)
    {
        Cell cellTypeToMove= (Cell) jComboBoxCellTypes.getSelectedItem();

        logger.logComment("Moving to origin cell: "+ cellTypeToMove);

        if (cellTypeToMove == null)
        {
            GuiUtils.showErrorMessage(logger, "Problem moving the Cell Type", null, this);
            return;
        }

        Point3f oldStartPos = cellTypeToMove.getFirstSomaSegment().getStartPointPosition();

        CellTopologyHelper.translateAllPositions(cellTypeToMove,
                                                 new Vector3f(oldStartPos.x*-1f,
                                                              oldStartPos.y*-1f,
                                                              oldStartPos.z*-1f));
        
        projManager.getCurrentProject().markProjectAsEdited();

        refreshTabCellTypes();

    }

    void jButtonCellTypesConnect_actionPerformed(ActionEvent e)
    {

        Cell cellTypeToConnect= (Cell) jComboBoxCellTypes.getSelectedItem();

        logger.logComment("Reconnecting cell: "+ cellTypeToConnect);

        if (cellTypeToConnect == null || true)
        {
            GuiUtils.showErrorMessage(logger, "Problem reconnecting the Cell Type", null, this);
            return;
        }

        ////////////////////////////CellTopologyHelper.moveSectionsToConnPointsOnParents(cellTypeToConnect);

        projManager.getCurrentProject().markProjectAsEdited();

        refreshTabCellTypes();

    }

    void jButtonCellTypesMakeSimpConn_actionPerformed(ActionEvent e)
    {
        Cell cellType = (Cell) jComboBoxCellTypes.getSelectedItem();

        logger.logComment("Making cell: "+ cellType+" simply connected");

        if (cellType == null)
        {
            GuiUtils.showErrorMessage(logger, "Problem making Cell Type Simply Connected", null, this);
            return;
        }

        boolean result = false;//////////////////CellTopologyHelper.makeSimplyConnected(cellType);

        if(!result)
        {
            GuiUtils.showErrorMessage(logger, "Problem making Cell Type Simply Connected. Note that the cell segments\n"
                                      + "must be properly connected to their parents before completing this step (try\n"
                                      + " pressing \""+jButtonCellTypesConnect.getText()+"\" button)", null, this);
            return;
        }

        projManager.getCurrentProject().markProjectAsEdited();

        refreshTabCellTypes();

    }

    void jButtonNeuroMLExport_actionPerformed(ActionEvent e, NeuroMLLevel level, NeuroMLVersion version)
    {

        logger.logComment("Saving the cell morphologies in NeuroML form...");
        
        Project proj = projManager.getCurrentProject();
                
        File neuroMLDir = ProjectStructure.getNeuroMLDir(proj.getProjectMainDirectory());

        GeneralUtils.removeAllFiles(neuroMLDir, false, false, false);


        MorphCompartmentalisation mc = (MorphCompartmentalisation)jComboBoxNeuroMLComps.getSelectedItem();

        try
        {
            MorphMLConverter.saveAllCellsInNeuroML(proj, 
                                                   mc,
                                                   level,
                                                   version,
                                                   null,
                                                   neuroMLDir);
        }
        catch (MorphologyException ex1)
        {
            GuiUtils.showErrorMessage(logger, "Problem saving cells " , ex1, this);
        }


        refreshTabNeuroML();
        //}
    

    }

    
    void jButtonNeuroMLGenSim_actionPerformed(ActionEvent e, NeuroMLVersion version, LemsOption lemsOption)
    {

        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        if (projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups() == 0 ||
            (projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups() > 0 &&
            projManager.getCurrentProject().generatedCellPositions.getNumberPositionRecords() == 0))
        {
            GuiUtils.showInfoMessage(logger, "No network generated",
                                      "Note: there is no generated network yet. All cells & channels in the project will be exported", this);
  
        }


        boolean cont = this.checkReloadOrRegenerate();
        
        if (!cont) return;
        
        String origText = jButtonNeuroMLGenSim.getText();
        
        jButtonNeuroMLGenSim.setText("Generating...");
        jButtonNeuroMLGenSim.repaint();
        jButtonNeuroMLGenSim.validate();

        refreshSimulationName();
        

        MorphCompartmentalisation mc = (MorphCompartmentalisation)jComboBoxNeuroMLComps.getSelectedItem();

        boolean genSingleFile = jCheckBoxNeuroMLGenNet.isSelected();
        boolean nCannots = jCheckBoxNeuroMLneuroCobjects.isSelected();

        if (version.isVersion2())
        {
            genSingleFile = false;
            nCannots = false;
        }

        try
        {
            projManager.getCurrentProject().neuromlFileManager.reset();
            
            projManager.getCurrentProject().neuromlFileManager.generateNeuroMLFiles(this.getSelectedSimConfig(),
                                                                                      version,
                                                                                      lemsOption,
                                                                                      mc,
                                                                                      1234,
                                                                                      genSingleFile,
                                                                                      nCannots);

            if (jCheckBoxSedMl.isSelected())
            {
                File sedmlFile = new File(ProjectStructure.getNeuroMLDir(projManager.getCurrentProject().getProjectMainDirectory()), projManager.getCurrentProject().getProjectName()+ ".sedml");
                projManager.generateSedML(projManager.getCurrentProject(), sedmlFile, this.getSelectedSimConfig(), version);
            }
        }
        catch(IOException ex)
        {
            GuiUtils.showErrorMessage(logger , "Error when generating NeuroML", ex, this);
            return;
        }

        jButtonNeuroMLGenSim.setText(origText);

        
        
        this.refreshAll();
        
        
    }


    void jButtonMorphMLView_actionPerformed(ActionEvent e, boolean formatted)
    {
        String fileToView = (String)jComboBoxNeuroML.getSelectedItem();

        if (fileToView.equals(noNeuroMLFilesFound)) return;

        fileToView = fileToView.substring(0,fileToView.indexOf("(")).trim();

        File file = new File(fileToView);

        if (file.getName().endsWith(".png") ||
            file.getName().endsWith(".jpg") ||
            file.getName().endsWith(".jpeg") ||
            file.getName().endsWith(".svn") ||
            file.getName().endsWith(".gig"))
        {
            try {
                GuiUtils.showImage(file);
            } catch (FileNotFoundException ex) {
                GuiUtils.showErrorMessage(logger, "Problem displaying file: "+file, ex, this);
            }
        }
        else
        {

            if (!formatted)
            {
                SimpleViewer.showFile(fileToView, 12, false, false, this, false, false, "Validate", new ActionListener(){
                    public void actionPerformed(ActionEvent e)
                    {
                        jButtonNeuroMLValidate_actionPerformed(e);
                    }
                }
                );
                return;
            }
            else
            {
                showHighlightedXML(file, true);
            }
        }
        
    }
    
    private void showHighlightedXML(File file, boolean validateNeuroMLButton)
    {
        SimpleXMLDocument doc = null;
        String stringToView = "";
        try
        {
            doc = SimpleXMLReader.getSimpleXMLDoc(file);
            stringToView = doc.getXMLString("", true);
        }
        catch (Exception ex)
        {
            //GuiUtils.showErrorMessage(logger, "Error showing that XML file", ex, this);
            //return;
            stringToView = "<span style=\"color:red\"><h3>Warning: could not successfully parse "+ file.getAbsolutePath()+"</h3></span>";
        }
        String title = "Viewing XML file: "+ file.getAbsolutePath();
        if (validateNeuroMLButton)
        {
            SimpleViewer.showString(stringToView , title,12, false, true, .9f, .9f,this, false, "Validate", new ActionListener(){
                public void actionPerformed(ActionEvent e)
                {
                    jButtonNeuroMLValidate_actionPerformed(e);
                }
            }
            );
        }
        else
        {
            SimpleViewer.showString(stringToView , title,12, false, true, .9f);
        }
    }
    
    

    void jButtonGenesisGenerate_actionPerformed(ActionEvent e)
    {
        logger.logComment("Create GENESIS button pressed...");
        doCreateGenesis();

    }

    void jButtonGenesisRun_actionPerformed(ActionEvent e)
    {
        logger.logComment("Run GENESIS button pressed...");
        doRunGenesis();
    }
    
    void jButtonPsicsGenerate_actionPerformed(ActionEvent e)
    {
        logger.logComment("Create Psics button pressed...");
        doGeneratePsics();

    }

    void jButtonPsicsRun_actionPerformed(ActionEvent e)
    {
        logger.logComment("Run Psics button pressed...");
        doRunPsics();
    }
    
    void jButtonPynnGenerate_actionPerformed(ActionEvent e)
    {
        logger.logComment("Create Pynn button pressed...");
        doGeneratePynn();

    }

    void jButtonPynnRun_actionPerformed(ActionEvent e)
    {
        logger.logComment("Run Pynn button pressed...");
        doRunPynn();
    }

    void jButtonGenesisView_actionPerformed(ActionEvent e)
    {
        logger.logComment("Viewing a genesis file");

        if (projManager.getCurrentProject() == null)
        {
            logger.logError("No project loaded...");
            return;
        }

        String selected = (String)jComboBoxGenesisFiles.getSelectedItem();



        File selectedFile = new File(ProjectStructure.getGenesisCodeDir(projManager.getCurrentProject().getProjectMainDirectory()),
                                 selected);

        if (projManager.getCurrentProject().genesisSettings.isMooseCompatMode())
            selectedFile = new File(ProjectStructure.getMooseCodeDir(projManager.getCurrentProject().getProjectMainDirectory()),
                                 selected);

        logger.logComment("Viewing genesis file: "+selectedFile);

        SimpleViewer.showFile(selectedFile.getAbsolutePath(), 12, false, false, jCheckBoxGenesisLineNums.isSelected());


    }

    void jButtonRegionsEdit_actionPerformed(ActionEvent e)
    {
        logger.logComment("Editing selected region");
        doEditRegion();
    }

    void jMenuItemCondorMonitor_actionPerformed(ActionEvent e)
    {
        logger.logComment("CondorMonitor to run...");

        new CondorApp(false);

    }


    void jMenuItemPlotEquation_actionPerformed(ActionEvent e)
    {
        logger.logComment("jMenuItemPlotEquation_actionPerformed...");

        DataSet ds = PlotterFrame.addManualPlot(100, 0, 10, this);

        PlotterFrame frame = PlotManager.getPlotterFrame("New Data Set plot", false, false);

        frame.addDataSet(ds);

        frame.setVisible(true);

    }



    void jMenuItemPlotImport_actionPerformed(ActionEvent e)
    {
        logger.logComment("jMenuItemPlotImport_actionPerformed...");

        String lastDir = recentFiles.getMyLastExportPointsDir();

        if (lastDir == null) lastDir
            = projManager.getCurrentProject().getProjectMainDirectory().getAbsolutePath();

        File defaultDir = new File(lastDir);

        ArrayList<DataSet> dss = PlotterFrame.addNewDataSet(defaultDir, this);
        

        if (dss != null)
        {
            PlotterFrame frame = PlotManager.getPlotterFrame("Imported Data Set plot", false, false);
            for(DataSet ds: dss)
                frame.addDataSet(ds);
            
            frame.setVisible(true);
        }
    }
    
    void jMenuItemPlotImportHDF5_actionPerformed(ActionEvent e)
    {
        logger.logComment("jMenuItemPlotImportHDF5_actionPerformed...");

        String lastDir = recentFiles.getMyLastExportPointsDir();

        if (lastDir == null) lastDir
            = projManager.getCurrentProject().getProjectMainDirectory().getAbsolutePath();

        File defaultDir = new File(lastDir);
        
        PlotterFrame.addHDF5DataSets(defaultDir, this);
    }



    void jMenuItemMPIMonitor_actionPerformed(ActionEvent e)
    {
        logger.logComment("MPIMonitor to run...");

        MpiFrame mpiFrame = new MpiFrame(this.projManager.getCurrentProject().getProjectMainDirectory(), false);

        GuiUtils.centreWindow(mpiFrame);

        mpiFrame.setVisible(true);

    }


    void jButtonNeuronCreateCondor_actionPerformed(ActionEvent e)
    {
        logger.logComment("Creating Condor ready hoc");
        doCreateHoc(NeuronFileManager.RUN_VIA_CONDOR);
    }
/*
    void jButtonNeuronCreateMPI_actionPerformed(ActionEvent e)
    {
        logger.logComment("Creating Parallel ready hoc");
        doCreateHoc(NeuronFileManager.RUN_PARALLEL_HOC);
    }*/

    void jButtonNeuronCreatePythonXML_actionPerformed(ActionEvent e)
    {
        logger.logComment("Creating Python/XML scripts");
        doCreateHoc(NeuronFileManager.RUN_PYTHON_XML);
    }

    
    void jButtonNeuronCreatePyHDF5_actionPerformed(ActionEvent e)
    {
        logger.logComment("Creating Python/HDF5 scripts");
        doCreateHoc(NeuronFileManager.RUN_PYTHON_HDF5);
    }


    void jButton3DQuickSims_actionPerformed(ActionEvent e)
    {
        logger.logComment("Loading quick plots...");

        new SimulationTreeFrame(this.projManager.getCurrentProject().getProjectFile(), false).setVisible(true);

    }


    void jButton3DPrevSims_actionPerformed(ActionEvent e)
    {
        logger.logComment("Loading a previous simulation...");
        doViewPrevSimulations();

    }

    void doViewPrevSimulations()
    {
        this.doDestroy3D();
        File simDir = ProjectStructure.getSimulationsDir(projManager.getCurrentProject().getProjectMainDirectory());

        SimulationBrowser dlg = new SimulationBrowser(simDir, this);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);

        SimulationData selected  =  null;

        if (dlg.cancelled)
        {
            logger.logComment("User cancelled...");
            return;
        }

        try
        {
            selected = dlg.getSelectedSimulation();
        }
        catch (SimulationDataException ex)
        {
            GuiUtils.showErrorMessage(logger, "There was a problem loading that simulation", ex, this);
            return;
        }
        doCreate3D(selected.getSimulationDirectory());

        logger.logComment("Selected sim: "+ selected);

        setGeneratorInfo("<p>Network has been reloaded from simulation: <b>"+selected+"</b></p>"+"<br>"
                                             +"<center><b>Cell Groups:</b></center>"
                                             +projManager.getCurrentProject().generatedCellPositions.getHtmlReport()+"<br>"
                                            +"<center><b>Network Connections:</b></center>"

            +projManager.getCurrentProject().generatedNetworkConnections.getHtmlReport(GeneratedNetworkConnections.ANY_NETWORK_CONNECTION,
            null)+"");

        this.refreshTabGenerate();

    }



/*
    void jButtonStimWhereToStim_actionPerformed(ActionEvent e)
    {
        if (jRadioButtonStimNone.isSelected())
        {
            GuiUtils.showErrorMessage(logger, "Please select the type of stimulation above first", null, this);
            return;
        }
        String cellGroupToStim = projManager.getCurrentProject().stimulationSettings.cellGroup;

        String cellType = projManager.getCurrentProject().cellGroupsInfo.getCellType(cellGroupToStim);
        Cell cellForSelectedGroup = projManager.getCurrentProject().cellManager.getCellType(cellType);

        SegmentSelector dlg = new SegmentSelector(this, cellForSelectedGroup, false, true);

        Vector segments = cellForSelectedGroup.getAllSegments();
        Segment segToStim = (Segment)segments.elementAt(projManager.getCurrentProject().stimulationSettings.segmentID);

        dlg.setSelectedSegment(segToStim);


//Center the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = dlg.getSize();
        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;
        }
        dlg.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        dlg.show();

        //dlg.setSelectedSegment( (Segment) p.getAllSegments().elementAt(1));

        if(dlg.cancelled) return;

        if (dlg.getSelectedSegment()==null) return;

        projManager.getCurrentProject().markProjectAsEdited();

        projManager.getCurrentProject().stimulationSettings.setSegmentID(dlg.getSelectedSegment().getSegmentId());

        jTextFieldStimWhere.setText(dlg.getSelectedSegment().getSegmentName()
                                    + " (ID: "
                                    +dlg.getSelectedSegment().getSegmentId()
                                    + ")");

    }
*/


    void jButtonMechanismAdd_actionPerformed(ActionEvent e)
    {
        logger.logComment("Adding a new cell Mechanism...");

        GuiUtils.showWarningMessage(logger, "Note: use of this type of Cell Mechanism is not advised. Use a ChannelML Mechanism instead.", this);

        CellMechanismEditor cellProcEditor = new CellMechanismEditor(projManager.getCurrentProject(), this);

        cellProcEditor.pack();
        cellProcEditor.setVisible(true);

        /*
        if (cellProcEditor.cancelled) return;

        CellProcess suggestedCellProc = cellProcEditor.getFinalCellProcess();

        while (projManager.getCurrentProject().cellProcessInfo.getAllCellProcessNames().contains(suggestedCellProc.getInstanceName()))
        {
            GuiUtils.showErrorMessage(logger, "That name: "+suggestedCellProc.getInstanceName()+" has already been used. Please select another.", null, this);
            logger.logComment("Reshowing the dialog...");
            cellProcEditor.setVisible(true);
           if (cellProcEditor.cancelled) return;
           suggestedCellProc = cellProcEditor.getFinalCellProcess();
        }

        projManager.getCurrentProject().cellProcessInfo.addCellProcess(cellProcEditor.getFinalCellProcess());
*/

    }
    


    void jButtonMechanismCopy_actionPerformed(ActionEvent e)
    {
        logger.logComment("----------------------------         Copying a cell mechanism...");

            if (true) return;

        int selectedRow = jTableMechanisms.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }
        CellMechanism cellMechPre = projManager.getCurrentProject().cellMechanismInfo.getCellMechanismAt(selectedRow);

        
        String cellMechanismName = JOptionPane.showInputDialog(this,
                                    "Please enter the name of the Channel Mechanism which is copied from "+ cellMechPre.getInstanceName(),
                                    "New ChannelML Cell Mechanism",
                                    JOptionPane.QUESTION_MESSAGE);

        logger.logComment("Input: "+ cellMechanismName);

        //File cellMechDir = ProjectStructure.getCellProcessesDir(projManager.getCurrentProject().getProjectMainDirectory());


        if (cellMechanismName==null || cellMechanismName.trim().length()==0)
        {
            logger.logError("No cellMechanismName inputted...");
            return;
        }

        if (cellMechanismName.indexOf(" ")>=0)
        {
            GuiUtils.showErrorMessage(logger, "Please type a Cell Mechanism name without spaces", null, this);
            jButtonMechanismCopy_actionPerformed(e);
            return;
        }

        File dirForCMLFiles = ProjectStructure.getDirForCellMechFiles(projManager.getCurrentProject(), cellMechanismName);

        if (dirForCMLFiles.exists())
        {
            GuiUtils.showErrorMessage(logger, "The Cell Mechanism name: "+ cellMechanismName +" is already being used.", null, this);
            jButtonMechanismCopy_actionPerformed(e);
            return;

        }
        dirForCMLFiles.mkdir();
        
        //CellMechanism cmPost = new CellMechanism();
        
    }
    
    void jButtonCompareMechanism_actionPerformed(ActionEvent e)
    {
        
        Project thisProj;
        try
        {             
            thisProj = projManager.loadProject(projManager.getCurrentProject().getProjectFile());
        }
        catch (Exception ex2)
        {
            GuiUtils.showErrorMessage(logger, "Problem comparing the Cell", ex2, this);

            return;
        }
        
        ArrayList<String> currentMechs = thisProj.cellMechanismInfo.getAllCellMechanismNames();
        
        String selection = (String) JOptionPane.showInputDialog(this,
                                "Please select the mechanism that you want to compare: ",
                                "Select mechanisms",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                currentMechs.toArray(),
                                currentMechs.get(0));
        
        CellMechanism mechToComp = thisProj.cellMechanismInfo.getCellMechanism(selection);
        
        String[] otherProject = new String[2];
        otherProject[0] = "this project";
        otherProject[1] = "other project";
        
        String project = (String) JOptionPane.showInputDialog(this,
                                                                "Compare mechanism " +
                                                                mechToComp.getInstanceName() + " with a mechanism from:",
                                                                "Select project",
                                                                JOptionPane.QUESTION_MESSAGE,
                                                                null,
                                                                otherProject,
                                                                otherProject[0]);
                
        logger.logComment("no option");
        
        if (project.equals("this project"))
        {
            
            ArrayList<String> names = projManager.getCurrentProject().cellMechanismInfo.getAllCellMechanismNames();

            if (names.size()==1)
            {
                GuiUtils.showErrorMessage(logger, "There is only one mechanism in this project, nothing to compare it to.", null, this);
                return;
            }

            String[] otherNames = new String[names.size()-1];
            int count = 0;
            for (int i = 0; i < names.size(); i++)
            {
                if (!names.get(i).equals(mechToComp.getInstanceName()))
                {
                    otherNames[count] = names.get(i);
                    count++;
                }
            }

            selection = (String) JOptionPane.showInputDialog(this,
                                                                    "Please select the mechanism to compare " +
                                                                    mechToComp.getInstanceName() + " to",
                                                                    "Select mechanism",
                                                                    JOptionPane.QUESTION_MESSAGE,
                                                                    null,
                                                                    otherNames,
                                                                    otherNames[0]);

            CellMechanism otherMech = projManager.getCurrentProject().cellMechanismInfo.getCellMechanism(selection);          

            String chanComp = CellTopologyHelper.compareChannelMech(mechToComp.getInstanceName(), otherMech.getInstanceName(), true, thisProj, thisProj);

            SimpleViewer.showString(chanComp, "Comparison of "+mechToComp+" with "+ otherMech, 12, false, true);
               
        }
        
        else if (project.equals("other project"))
        {
            // set to parent of project dir...
            File defaultDir = projManager.getCurrentProject().getProjectFile().getParentFile().getParentFile();            

            Frame frame = (Frame)this;
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setDialogTitle("Choose neuroConstruct project");

            try
            {
                chooser.setCurrentDirectory(defaultDir);
                logger.logComment("Set Dialog dir to: " + defaultDir);
            }
            catch (Exception ex)
            {
                logger.logError("Problem with default dir setting: " + defaultDir, ex);
            }
            SimpleFileFilter fileFilter = ProjectStructure.getProjectFileFilter();

            chooser.setFileFilter(fileFilter);

            //chooser.sett

            int retval = chooser.showDialog(frame, null);

            if (retval == JFileChooser.APPROVE_OPTION)
            {
                try
                {
                    Project firstProj = projManager.loadProject(projManager.getCurrentProject().getProjectFile());
                       
                    logger.logComment(">>>>  Loading project: " + chooser.getSelectedFile());

                    Project otherProj = Project.loadProject(chooser.getSelectedFile(), this);

                    logger.logComment("<<<<  Loaded project: " + otherProj.getProjectFileName());

                    ArrayList<String> mechanismsList = otherProj.cellMechanismInfo.getAllCellMechanismNames();

                    if (mechanismsList.isEmpty())
                    {
                        GuiUtils.showErrorMessage(logger, "No Cell Mechanisms found in that project.", null, this);
                        return;
                    }

                    selection = (String) JOptionPane.showInputDialog(this,
                                "Please select the Cell Mechanism from project "+otherProj.getProjectName()+" which you want to compare to one in this project ",
                                "Select Cell Mechanism",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                mechanismsList.toArray(),
                                mechanismsList.get(0));

                    if (selection==null)
                    {
                        logger.logComment("No selection made...");
                        return;
                    }
                    logger.logComment("Selection: "+ selection);

                    CellMechanism otherMech = otherProj.cellMechanismInfo.getCellMechanism(selection);

                    String comp = CellTopologyHelper.compareChannelMech(mechToComp.getInstanceName(), otherMech.getInstanceName(), true, thisProj, otherProj);

                    SimpleViewer.showString(comp, "Comparison of "+mechToComp.getInstanceName()+" with "+ otherMech.getInstanceName(), 12, false, true);
            
                } 
                catch (ProjectFileParsingException ex)
                {
                    GuiUtils.showErrorMessage(logger, "Problem comparing the Cell", ex, this);

                    return;
                }
            }                
        }
    }
    
      
    void jButtonMechanismReloadFile_actionPerformed(ActionEvent e)
    {
        logger.logComment("----------------------------         Reloading cell mechanism file...");
        
        int[] selectedRows = jTableMechanisms.getSelectedRows();

        if (selectedRows.length < 0)
        {
            logger.logComment("No rows selected...");
            GuiUtils.showErrorMessage(logger,"Please select one of the ChannelML based cell mechanisms to reload the XML file.", null, this);
            return;
        }
        for(int row: selectedRows)
        {
            CellMechanism cellMech = projManager.getCurrentProject().cellMechanismInfo.getCellMechanismAt(row);

            if (cellMech instanceof XMLCellMechanism)
            {
                XMLCellMechanism xmlMechanism = (XMLCellMechanism)cellMech;

                try
                {
                    xmlMechanism.reset(projManager.getCurrentProject(), false);

                    SimpleDateFormat formatter = new SimpleDateFormat("H:mm:ss, EEEE MMMM d, yyyy");

                    File implFile = xmlMechanism.getXMLFile(projManager.getCurrentProject());
                    java.util.Date modified = new java.util.Date(implFile.lastModified());



                    GuiUtils.showInfoMessage(logger,"Success","Reloaded cell mechanism: "+ xmlMechanism.getInstanceName()
                        +" from file: "+implFile.getAbsolutePath() +" (modified "+formatter.format(modified)+")", this);
                }
                catch (XMLMechanismException ex1)
                {
                    GuiUtils.showErrorMessage(logger,
                                              "Error initialising Cell Mechanism: " +xmlMechanism.getInstanceName(),
                                              ex1,
                                              null);

                }
            }
            else
            {
                GuiUtils.showErrorMessage(logger,"Unable to reload the implementing ChannelML file of: "+ cellMech, null, this);
            }
        }
        
    }
        
    
    void jButtonMechanismUpdateMaps_actionPerformed(ActionEvent e)
    {
        logger.logComment("----------------------------         Updating a cell mechanism...");
        
        String info = "Use this button to update the selected XML based cell mechanism to use the current version of the NEURON/GENESIS\n"
                     +"mapping XSL files (for NeuroML version "+GeneralProperties.getNeuroMLVersionNumber()+"). Note, ChannelML files with older XSL mappings may work perfectly well,\n"
                     +"but the latest XSL mappings will always be the most well tested and should be considered for any valid ChannelML file.\n\n" +
                     "Note this will also update SBML Python mappings (alpha)";
        
        int[] selectedRows = jTableMechanisms.getSelectedRows();

        if (selectedRows.length < 0)
        {
            logger.logComment("No row selected...");
            GuiUtils.showErrorMessage(logger,info, null, this);
            return;
        }
        
        SimpleDateFormat formatter = new SimpleDateFormat("H:mm:ss, EEEE MMMM d, yyyy");

        boolean cleanup = GuiUtils.showYesNoMessage(logger, "Would you like to also clean up any old XSL mapping files found which are not being used by the " +
                "Cell Mechanism (e.g. from a previous version of NeuroML)?", this);
        
        
        for (int row: selectedRows)
        {
            CellMechanism cellMech = projManager.getCurrentProject().cellMechanismInfo.getCellMechanismAt(row);

            if (!(cellMech instanceof ChannelMLCellMechanism) && !(cellMech instanceof SBMLCellMechanism))
            {
                GuiUtils.showErrorMessage(logger,info, null, this);
            }
            else if (cellMech instanceof SBMLCellMechanism)
            {
                SBMLCellMechanism sbmlMech = (SBMLCellMechanism) cellMech;
                File implFile = sbmlMech.getXMLFile(projManager.getCurrentProject());

                for (SimulatorMapping map: sbmlMech.getSimMappings())
                {
                    String simEnv = map.getSimEnv();

                    if (simEnv.equals(SimEnvHelper.NEURON))
                    {
                        File sbml2neuron = ProjectStructure.getSbml2NeuronFile();
                        
                        File oldMapFile = new File (implFile.getParent(), map.getMappingFile());

                        java.util.Date modifiedOld = new java.util.Date(oldMapFile.lastModified());
                        java.util.Date modifiedNew = new java.util.Date(sbml2neuron.lastModified());


                    int ans = JOptionPane.showConfirmDialog(this, "Would you like to replace mapping file: \n\n"+oldMapFile.getAbsolutePath() + " ("+formatter.format(modifiedOld)+")"
                        +"\n\nfor environment "+simEnv+", SBML mech: " +sbmlMech.getInstanceName()
                            + " with a copy of file:\n\n"+ sbml2neuron.getAbsolutePath()+ " ("+formatter.format(modifiedNew)+")"+"?" ,
                            "Replace mapping?", JOptionPane.YES_NO_CANCEL_OPTION);

                    if (ans==JOptionPane.CANCEL_OPTION)
                        return;
                    if (ans==JOptionPane.YES_OPTION)
                    {
                        try
                        {
                            GeneralUtils.copyFileIntoDir(sbml2neuron, implFile.getParentFile());
                            map.setMappingFile(sbml2neuron.getName());
                            projManager.getCurrentProject().markProjectAsEdited();
                        }
                        catch (IOException ex)
                        {
                            GuiUtils.showErrorMessage(logger,"Problem copying that file into the project", ex, this);
                        }
                    }
                    }
                }
                
            }
            else if (cellMech instanceof ChannelMLCellMechanism)
            {
                File xslDir = GeneralProperties.getChannelMLSchemataDir();
                ChannelMLCellMechanism cmlMech = (ChannelMLCellMechanism) cellMech;
                File implFile = cmlMech.getXMLFile(projManager.getCurrentProject());

                for (SimulatorMapping map: cmlMech.getSimMappings())
                {
                    String simEnv = map.getSimEnv();
                    File oldXsl = new File (implFile.getParent(), map.getMappingFile());
                    File newXsl = null;

                    if (simEnv.equals(SimEnvHelper.NEURON))
                    {
                        newXsl = new File(xslDir, "ChannelML_v"+GeneralProperties.getNeuroMLVersionNumber()+"_NEURONmod.xsl");
                    }
                    else if (simEnv.equals(SimEnvHelper.GENESIS))
                    {
                        newXsl = new File(xslDir, "ChannelML_v"+GeneralProperties.getNeuroMLVersionNumber()+"_GENESIStab.xsl");
                    }
                    else if (simEnv.equals(SimEnvHelper.PSICS))
                    {
                        newXsl = new File(xslDir, "ChannelML_v"+GeneralProperties.getNeuroMLVersionNumber()+"_PSICS.xsl");
                    }


                    java.util.Date modifiedOld = new java.util.Date(oldXsl.lastModified());
                    java.util.Date modifiedNew = new java.util.Date(newXsl.lastModified());


                    int ans = JOptionPane.showConfirmDialog(this, "Would you like to replace mapping file: \n\n"+oldXsl.getAbsolutePath() + " ("+formatter.format(modifiedOld)+")"
                        +"\n\nfor environment "+simEnv+", cell mech: " +cmlMech.getInstanceName()
                            + " with a copy of file:\n\n"+ newXsl.getAbsolutePath()+ " ("+formatter.format(modifiedNew)+")"+"?" , "Replace mapping?", JOptionPane.YES_NO_CANCEL_OPTION);

                    if (ans==JOptionPane.CANCEL_OPTION)
                        return;
                    if (ans==JOptionPane.YES_OPTION)
                    {
                        try 
                        {
                            GeneralUtils.copyFileIntoDir(newXsl, implFile.getParentFile());
                            map.setMappingFile(newXsl.getName());
                            projManager.getCurrentProject().markProjectAsEdited();
                        } 
                        catch (IOException ex) 
                        {
                            GuiUtils.showErrorMessage(logger,"Problem copying that file into the project", ex, this);
                        }
                    }
                }
                
                for(String simEnv:SimEnvHelper.currentSimEnvironments)
                {
                    if (cmlMech.getSimMapping(simEnv)==null)
                    {
                        File newXsl = null;

                        if (simEnv.equals(SimEnvHelper.NEURON))
                        {
                            newXsl = new File(xslDir, "ChannelML_v"+GeneralProperties.getNeuroMLVersionNumber()+"_NEURONmod.xsl");
                        }
                        else if (simEnv.equals(SimEnvHelper.GENESIS))
                        {
                            newXsl = new File(xslDir, "ChannelML_v"+GeneralProperties.getNeuroMLVersionNumber()+"_GENESIStab.xsl");
                        }
                        else if (simEnv.equals(SimEnvHelper.PSICS))
                        {
                            newXsl = new File(xslDir, "ChannelML_v"+GeneralProperties.getNeuroMLVersionNumber()+"_PSICS.xsl");
                        }
                        
                        int ans = JOptionPane.showConfirmDialog(this, "There is currently no mapping for environment "+simEnv+", cell mech: " +cmlMech.getInstanceName()
                            + "\nWould you like to add the following XSL mapping for this cell mechanism:\n\n"+ newXsl.getAbsolutePath()
                            + " ("+formatter.format(new java.util.Date(newXsl.lastModified()))+")"+"?" , "Add mapping?", JOptionPane.YES_NO_CANCEL_OPTION);

                        if (ans==JOptionPane.CANCEL_OPTION)
                            return;
                        if (ans==JOptionPane.YES_OPTION)
                        {
                            try 
                            {
                                GeneralUtils.copyFileIntoDir(newXsl, implFile.getParentFile());
                                SimulatorMapping map = new SimulatorMapping(newXsl.getName(), simEnv, (simEnv.equals(SimEnvHelper.NEURON)));
                                
                                cmlMech.addSimMapping(map);
                                try
                                {
                                    cmlMech.reset(projManager.getCurrentProject(), false);
                                    logger.logComment("New cml mech: "+ cmlMech);
                                }
                                catch (Exception ex)
                                {
                                    GuiUtils.showErrorMessage(logger,"Problem updating mechanism to support mapping to simulator: "+ simEnv, ex, this);
                                }
                                //map.setXslFile(newXsl.getName());
                                refreshTabCellMechanisms();
                                projManager.getCurrentProject().markProjectAsEdited();
                            } 
                            catch (IOException ex) 
                            {
                                GuiUtils.showErrorMessage(logger,"Problem copying that file into the project", ex, this);
                            }
                        }
                        
                    }
                }

                if (cleanup)
                {
                    ArrayList<String> filesToKeep = new ArrayList<String>();
                    filesToKeep.add(implFile.getName());
                    filesToKeep.add(CellMechanismHelper.PROPERTIES_FILENAME);
                    for (SimulatorMapping map: cmlMech.getSimMappings())
                    {
                        String simEnv = map.getSimEnv();
                        File xsl = new File (implFile.getParent(), map.getMappingFile());
                        filesToKeep.add(xsl.getName());
                    }
                    for(File f: implFile.getParentFile().listFiles())
                    {
                        if (!filesToKeep.contains(f.getName()) && !f.getName().startsWith(".") && !GeneralUtils.isVersionControlDir(f.getName()))
                        {
                             logger.logComment("Deleting: "+ f.getAbsolutePath());
                             f.delete();
                        }
                    }
                }
            }
        }


        boolean save = GuiUtils.showYesNoMessage(logger, "It's best to save the project immediately to make sure the changes are saved. Save now?", this);
        if (save)
        {
            doSave();
        }
        
    }

    

    void jButtonMechanismEdit_actionPerformed(ActionEvent e)
    {
        logger.logComment("----------------------------         Editing a cell mechanism...");

        int selectedRow = jTableMechanisms.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }
        CellMechanism cellMech = projManager.getCurrentProject().cellMechanismInfo.getCellMechanismAt(selectedRow);


        if (cellMech instanceof AbstractedCellMechanism)
        {

            logger.logComment("Cell mechanism:");
            ((AbstractedCellMechanism)cellMech).printDetails();

            CellMechanismEditor cellMechEditor = new CellMechanismEditor(projManager.getCurrentProject(), this);

            cellMechEditor.setCellMechanism((AbstractedCellMechanism)cellMech);

            cellMechEditor.pack();
            cellMechEditor.setVisible(true);
            if (cellMechEditor.cancelled)return;

            AbstractedCellMechanism cp = cellMechEditor.getFinalCellMechanism();
            cp.printDetails();

            projManager.getCurrentProject().cellMechanismInfo.updateCellMechanism(cp);
        }
        else if (cellMech instanceof XMLCellMechanism)
        {
            ChannelMLEditor cmlEditor
                = new ChannelMLEditor( (XMLCellMechanism) cellMech,
                                      projManager.getCurrentProject(),
                                      this);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            Dimension frameSize = cmlEditor.getSize();
            if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
            if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;
            cmlEditor.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

            cmlEditor.setVisible(true);

            //System.out.println("Shown the dialog");


        }
    }



    void jButtonMechanismDelete_actionPerformed(ActionEvent e)
    {
        logger.logComment("Deleting a cell mech...");
        int selectedRow = jTableMechanisms.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }
        CellMechanism cellMech = projManager.getCurrentProject().cellMechanismInfo.getCellMechanismAt(selectedRow);


        File dirForFiles = ProjectStructure.getDirForCellMechFiles(projManager.getCurrentProject(), cellMech.getInstanceName());
        GeneralUtils.removeAllFiles(dirForFiles, true, true, true);


        projManager.getCurrentProject().cellMechanismInfo.deleteCellMechanism(cellMech);

        projManager.getCurrentProject().markProjectAsEdited();

    }

    void jButtonMechanismFileBased_actionPerformed(ActionEvent e)
    {
        String cellMechanismName = JOptionPane.showInputDialog(this,
                                    "Please enter the name of the new Mechanism",
                                    "New File Based Cell Mechanism",
                                    JOptionPane.QUESTION_MESSAGE);

        logger.logComment("Input: "+ cellMechanismName);

        if (cellMechanismName==null || cellMechanismName.trim().length()==0)
        {
            logger.logError("No cellMechanismName inputted...");
            return;
        }

        if (cellMechanismName.indexOf(" ")>=0 ||
            cellMechanismName.indexOf(".")>=0 ||
            cellMechanismName.indexOf(",")>=0 ||
            cellMechanismName.indexOf("-")>=0 ||
            cellMechanismName.indexOf("/")>=0 ||
            cellMechanismName.indexOf("\\")>=0)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Please type a Cell Mechanism name with just "
                                      +"letters and digits and underscores, no spaces", null, this);
            jButtonMechanismFileBased_actionPerformed(e);
        }

        if (this.projManager.getCurrentProject().cellMechanismInfo.getAllCellMechanismNames().contains(cellMechanismName))
        {
            GuiUtils.showErrorMessage(logger,
                                      "The Cell Mechanism name: "+cellMechanismName+" has already been taken", null, this);
            jButtonMechanismFileBased_actionPerformed(e);


        }


        FileBasedMembraneMechanism fmp = new FileBasedMembraneMechanism();

        fmp.setInstanceName(cellMechanismName);


        Object[] options = {AbstractedCellMechanism.CHANNEL_MECHANISM,
            AbstractedCellMechanism.SYNAPTIC_MECHANISM,
            AbstractedCellMechanism.ION_CONCENTRATION,
            AbstractedCellMechanism.POINT_PROCESS,
            AbstractedCellMechanism.GAP_JUNCTION};

        JOptionPane option = new JOptionPane("Please select the type of Cell Mechanism",
                                             JOptionPane.DEFAULT_OPTION,
                                             JOptionPane.PLAIN_MESSAGE,
                                             null,
                                             options,
                                             options[0]);

        JDialog dialog = option.createDialog(this, "Select type of Cell Mechanism");
        dialog.setVisible(true);

        Object choice = option.getValue();
        logger.logComment("User has chosen: " + choice);

        this.projManager.getCurrentProject().markProjectAsEdited();

        fmp.specifyMechanismType( (String) choice);

        String[] simEnvs
            = new String[]
            {SimEnvHelper.NEURON,
            SimEnvHelper.GENESIS};


        //File proposedLoc = ProjectStructure.getFileBasedCellProcessesDir(projManager.getCurrentProject().getProjectMainDirectory(), true);

        File proposedLoc = ProjectStructure.getDirForCellMechFiles(projManager.getCurrentProject(), cellMechanismName);

        for (int i = 0; i < simEnvs.length; i++)
        {

            int proceed = JOptionPane.showConfirmDialog(this,
                 "Will there be a "+simEnvs[i]+" implementation of this Cell Mechanism?\n"
                 + "(Selected file will be imported into project in directory "
                 + proposedLoc.getAbsolutePath() + ")\n\n"
                 +"Note that the file needs to have the name of the process replaced by "
                 + MechanismImplementation.getNamePlaceholder()
                 + "\nSee examples in the templates/ directory.",
                 "New File Based Cell Mechanism Implementation",
                 JOptionPane.YES_NO_OPTION);

            logger.logComment("Input: " + proceed);

            if(proceed == JOptionPane.YES_OPTION)
            {

                JFileChooser chooser = new JFileChooser();
                chooser.setDialogType(JFileChooser.OPEN_DIALOG);

                String lastCellMechDir = recentFiles.getMyLastCellProcessesDir();

                if (lastCellMechDir == null) lastCellMechDir
                    = GeneralProperties.getnCProjectsDir().getAbsolutePath();

                File defaultDir = new File(lastCellMechDir);

                chooser.setCurrentDirectory(defaultDir);
                logger.logComment("Set Dialog dir to: " + defaultDir.getAbsolutePath());
                chooser.setDialogTitle("Choose "+simEnvs[i]+" file for Cell Mechanism: "
                        + cellMechanismName);
                int retval = chooser.showDialog(this, "Choose "+simEnvs[i]+" file");

                if (retval == JOptionPane.OK_OPTION)
                {

                    File newFile = null;
                    try
                    {
                        newFile = GeneralUtils.copyFileIntoDir(new File(chooser.getSelectedFile().getAbsolutePath()),
                                                        proposedLoc);

                        recentFiles.setMyLastCellProcessesDir(chooser.getSelectedFile().getParent());
                    }
                    catch (IOException ex)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem when including new Cell Mechanism", ex, this);
                        return;
                    }


                    fmp.specifyNewImplFile(simEnvs[i],
                                           newFile.getName());
                }
            }

        }


        projManager.getCurrentProject().cellMechanismInfo.addCellMechanism(fmp);
    }



    void jButtonMechanismNewCML_actionPerformed(ActionEvent e)
    {
        String cellMechanismName = JOptionPane.showInputDialog(this,
                                    "Please enter the name of the new ChannelML based Mechanism",
                                    "New ChannelML Cell Mechanism",
                                    JOptionPane.QUESTION_MESSAGE);

        logger.logComment("Input: "+ cellMechanismName);

        //File cellMechDir = ProjectStructure.getCellProcessesDir(projManager.getCurrentProject().getProjectMainDirectory());


        if (cellMechanismName==null || cellMechanismName.trim().length()==0)
        {
            logger.logError("No cellMechanismName inputted...");
            return;
        }

        if (cellMechanismName.indexOf(" ")>=0)
        {
            GuiUtils.showErrorMessage(logger, "Please type a Cell Mechanism name without spaces", null, this);
            jButtonMechanismNewCML_actionPerformed(e);
            return;
        }

        File dirForCMLFiles = ProjectStructure.getDirForCellMechFiles(projManager.getCurrentProject(), cellMechanismName);

        if (dirForCMLFiles.exists())
        {
            GuiUtils.showErrorMessage(logger, "The Cell Mechanism name: "+ cellMechanismName +" is already being used.", null, this);
            jButtonMechanismNewCML_actionPerformed(e);
            return;

        }
        dirForCMLFiles.mkdir();


        ChannelMLCellMechanism cmlm = new ChannelMLCellMechanism();

        cmlm.setInstanceName(cellMechanismName);



        Object[] options = {CellMechanism.CHANNEL_MECHANISM,
                            CellMechanism.SYNAPTIC_MECHANISM,
                            CellMechanism.ION_CONCENTRATION,
                            CellMechanism.POINT_PROCESS,
                            CellMechanism.GAP_JUNCTION,
                            CellMechanism.SBML_MECHANISM};

        JOptionPane option = new JOptionPane("Please select the type of Cell Mechanism",
                                             JOptionPane.DEFAULT_OPTION,
                                             JOptionPane.PLAIN_MESSAGE,
                                             null,
                                             options,
                                             options[0]);

        JDialog dialog = option.createDialog(this, "Select type of Cell Mechanism");
        dialog.setVisible(true);

        Object choice = option.getValue();
        logger.logComment("User has chosen: " + choice);

        cmlm.setMechanismType( (String) choice);


        JFileChooser cmlFileChooser = new JFileChooser();

        String lastCellMechDir = recentFiles.getMyLastCellProcessesDir();

        if (lastCellMechDir == null) lastCellMechDir
            = ProjectStructure.getCMLExamplesDir().getAbsolutePath();

        //System.out.println("lastCellMechDir: " + lastCellMechDir);

        File defaultDir = new File(lastCellMechDir);

        cmlFileChooser.setCurrentDirectory(defaultDir);


        cmlFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);


        cmlFileChooser.setDialogTitle("Choose ChannelML (*.xml) file");
        int retvalCML = cmlFileChooser.showDialog(this, "Choose ChannelML file for Cell Mechanism: " + cellMechanismName);
        File cmlFile = null;

        if (retvalCML == JOptionPane.OK_OPTION)
        {
            try
            {
                cmlFile = GeneralUtils.copyFileIntoDir(cmlFileChooser.getSelectedFile(),
                                                              dirForCMLFiles);

                cmlm.setXMLFile(cmlFileChooser.getSelectedFile().getName());

                //cmlm.initialise(this.projManager.getCurrentProject(), true);

            }
            catch (IOException ex1)
            {
                GuiUtils.showErrorMessage(logger, "Problem copying the Cell Mechanism file: "+ cmlFileChooser.getSelectedFile() +
                                          " into the project at: "+ dirForCMLFiles, null, this);
                return;

            }


            recentFiles.setMyLastCellProcessesDir(cmlFileChooser.getSelectedFile().getParent());
        }


        cmlm.setMechanismModel("ChannelML based process");
        cmlm.setDescription("Cell Mechanism based on ChannelML file: "+ cmlFile.getName());



        String[] simEnvs
            = new String[]
            {SimEnvHelper.NEURON,
            SimEnvHelper.GENESIS};

        for (int i = 0; i < simEnvs.length; i++)
        {

            int proceed = JOptionPane.showConfirmDialog(this,
                                                        "Is there a mapping (*.xsl) of this Cell Mechanism to "+ simEnvs[i] +
                                                        "?\n\n"
                                                        +"Note that the latest XSL mappings for NEURON and GENESIS "
                                                        +"can be found in:\n" +GeneralProperties.getChannelMLSchemataDir().getAbsolutePath() +"",
                                                        "Mapping to "+ simEnvs[i]+"?",
                                                        JOptionPane.YES_NO_OPTION);

            logger.logComment("Input: " + proceed);

            if (proceed == JOptionPane.YES_OPTION)
            {

                JFileChooser chooser = new JFileChooser();
                chooser.setDialogType(JFileChooser.OPEN_DIALOG);

                //String lastCellMappingDir = ProjectStructure.getCMLSchemasDir().getAbsolutePath();

                defaultDir = GeneralProperties.getChannelMLSchemataDir();

                chooser.setCurrentDirectory(defaultDir);
                logger.logComment("Set Dialog dir to: " + defaultDir.getAbsolutePath());
                chooser.setDialogTitle("Choose " + simEnvs[i] + " mapping file for Cell Mechanism: " + cellMechanismName);
                int retval = chooser.showDialog(this, "Choose " + simEnvs[i] + " file");

                if (retval == JOptionPane.OK_OPTION)
                {
                    //File cpDir = ProjectStructure.getCellProcessesDir(projManager.getCurrentProject().getProjectMainDirectory());

                    //File newLocation = new File(cpDir, cmlm.getInstanceName());

                    File newFile = null;
                    try
                    {
                        newFile = GeneralUtils.copyFileIntoDir(chooser.getSelectedFile(),
                                                               dirForCMLFiles);

                        recentFiles.setMyLastCellProcessesDir(chooser.getSelectedFile().getParent());
                    }
                    catch (IOException ex)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem when including new Cell Mechanism", ex, this);
                        return;
                    }

                    /** @todo Put in dialog asking if it's a mod mapping for neuron, and set real val for requiresCompilation */

                    SimulatorMapping sxm = new SimulatorMapping(newFile.getName(), simEnvs[i], true);
                    cmlm.addSimMapping(sxm);


                }
            }

        }


        try
        {
            // This is to make sure the files have been copied into the correct dirs..
            Thread.sleep(1000);
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }


        projManager.getCurrentProject().cellMechanismInfo.addCellMechanism(cmlm);


        try
        {
            cmlm.initialise(projManager.getCurrentProject(), true);
        }
        catch (XMLMechanismException ex1)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Error initialising Cell Mechanism: " +cmlm.getInstanceName() +", "+
                                      ex1.getMessage(),
                                      ex1,
                                      null);

        }


        ChannelMLEditor frame = new ChannelMLEditor(cmlm, projManager.getCurrentProject(), this);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;
        frame.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

        frame.setVisible(true);

        this.projManager.getCurrentProject().markProjectAsEdited();

        //System.out.println("Shown the dialog");

    }



    void jButtonMechanismTemplateCML_actionPerformed(ActionEvent e)
    {

        File cmlTemplateDir = ProjectStructure.getCMLTemplatesDir();

        File[] dirs = cmlTemplateDir.listFiles();

        Vector<String> possibilities = new Vector<String>();

        for (int i = 0; i < dirs.length; i++)
        {
            if (dirs[i].isDirectory() && !GeneralUtils.isVersionControlDir(dirs[i]) && !dirs[i].getName().equals("old"))
            {
                String name = dirs[i].getName();
                Properties props = new Properties();
                try
                {
                    props.load(new FileInputStream(new File(dirs[i], "properties")));
                    name = name + ": " + props.getProperty("Description");
                }
                catch (IOException ex)
                {
                    logger.logError("Problem getting properties", ex);
                    //ignore...
                }

                possibilities.add(name);
            }
        }

        String chosen = (String)JOptionPane.showInputDialog(this,
                    "Please select the ChannelML template for the new Cell Mechanism",
                    "Choose template",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    possibilities.toArray(),
                    possibilities.firstElement());

        if (chosen == null)
        {
            logger.logComment("Null chosen. user cancelled...");
            return;
        }
        if (chosen.indexOf(":")>0 )  // trim description
            chosen = chosen.substring(0,chosen.indexOf(":"));


        File fromDir = new File(cmlTemplateDir, chosen);

        Properties props = new Properties();

        try
        {
            props.load(new FileInputStream(new File(fromDir, "properties")));
        }
        catch (IOException ex)
        {
            logger.logError("Problem getting properties", ex);
            //ignore...
        }


        boolean goodName = false;

        File dirForCMLFiles = null;

        String propCellMechanismName = chosen;
        if (props.getProperty("DefaultName") != null)
            propCellMechanismName = props.getProperty("DefaultName");

        ArrayList allCellProcs = this.projManager.getCurrentProject().cellMechanismInfo.getAllCellMechanismNames();

        while (!goodName)
        {
            if (allCellProcs.contains(propCellMechanismName))
            {
                if (propCellMechanismName.indexOf("_")>0)
                {
                    String num = propCellMechanismName.substring(propCellMechanismName.lastIndexOf("_")+1);
                    try
                    {
                        int nextNum = Integer.parseInt(num) +1;

                        propCellMechanismName
                            = propCellMechanismName.substring(0, propCellMechanismName.lastIndexOf("_"))
                            + "_" + nextNum;
                    }
                    catch (NumberFormatException nfe)
                    {
                        propCellMechanismName = propCellMechanismName + "_1";
                    }
                }
                else
                {
                    propCellMechanismName = propCellMechanismName +"_1";
                }
            }

             propCellMechanismName = JOptionPane.showInputDialog(this, "Please enter the name of the new ChannelML based Mechanism",
                                                               propCellMechanismName);

            goodName = true;

            logger.logComment("Input: " + propCellMechanismName);

            //File cellProcDir = ProjectStructure.getCellProcessesDir(projManager.getCurrentProject().getProjectMainDirectory());

            if (propCellMechanismName == null || propCellMechanismName.trim().length() == 0)
            {
                logger.logError("No cellMechanismName inputted...");
                return;
            }

            if (propCellMechanismName.indexOf(" ") >= 0)
            {
                GuiUtils.showErrorMessage(logger, "Please type a Cell Mechanism name without spaces", null, this);
                goodName = false;
            }

            dirForCMLFiles = ProjectStructure.getDirForCellMechFiles(projManager.getCurrentProject(), propCellMechanismName);

            if (dirForCMLFiles.exists() || allCellProcs.contains(propCellMechanismName))
            {
                GuiUtils.showErrorMessage(logger, "The Cell Mechanism name: " + propCellMechanismName + " is already being used.", null, this);
                goodName = false;

            }
        }

        dirForCMLFiles.mkdir();

         ChannelMLCellMechanism cmlMech = new ChannelMLCellMechanism();

         cmlMech.setInstanceName(propCellMechanismName);

        if (props.getProperty("CellProcessType")!=null)
        {
            cmlMech.setMechanismType(props.getProperty("CellProcessType"));
        }
        else
        {

            Object[] options =
                {CellMechanism.CHANNEL_MECHANISM,
                CellMechanism.SYNAPTIC_MECHANISM,
                CellMechanism.ION_CONCENTRATION,
                CellMechanism.POINT_PROCESS,
                CellMechanism.GAP_JUNCTION};

            JOptionPane option = new JOptionPane("Please select the type of Cell Mechanism",
                                                 JOptionPane.DEFAULT_OPTION,
                                                 JOptionPane.PLAIN_MESSAGE,
                                                 null,
                                                 options,
                                                 options[0]);

            JDialog dialog = option.createDialog(this, "Select type of Cell Mechanism");
            dialog.setVisible(true);

            Object choice = option.getValue();
            logger.logComment("User has chosen: " + choice);

            cmlMech.setMechanismType( (String) choice);
        }


        logger.logComment("Checking files in: "+ fromDir);

        File[] contents = fromDir.listFiles();
        try
        {
            if (props.getProperty("ChannelMLFile")!=null)
            {
                String relativeFile = props.getProperty("ChannelMLFile");

                File absFile = new File(fromDir, relativeFile);
                absFile = absFile.getCanonicalFile();

                logger.logComment("ChannelML file found in props to be: "+ absFile);

                File newFile = GeneralUtils.copyFileIntoDir(absFile, dirForCMLFiles);
                cmlMech.setXMLFile(newFile.getName());
            }
            if (props.getProperty("MappingNEURON")!=null)
            {
                String relativeFile = props.getProperty("MappingNEURON");

                File absFile = new File(fromDir, relativeFile);
                absFile = absFile.getCanonicalFile();

                logger.logComment("MappingNEURON file found in props to be: "+ absFile);

                File newFile = GeneralUtils.copyFileIntoDir(absFile, dirForCMLFiles);

                SimulatorMapping mapping = new SimulatorMapping(newFile.getName(),
                                                          SimEnvHelper.NEURON, true); // can be reset later

                cmlMech.addSimMapping(mapping);

            }

            if (props.getProperty("MappingGENESIS")!=null)
            {
                String relativeFile = props.getProperty("MappingGENESIS");

                File absFile = new File(fromDir, relativeFile);
                absFile = absFile.getCanonicalFile();

                logger.logComment("MappingGENESIS file found in props to be: "+ absFile);

                File newFile = GeneralUtils.copyFileIntoDir(absFile, dirForCMLFiles);

                SimulatorMapping mapping = new SimulatorMapping(newFile.getName(),
                                                          SimEnvHelper.GENESIS, false);

                cmlMech.addSimMapping(mapping);

            }

            if (props.getProperty("MappingPSICS")!=null)
            {
                String relativeFile = props.getProperty("MappingPSICS");

                File absFile = new File(fromDir, relativeFile);
                absFile = absFile.getCanonicalFile();

                logger.logComment("MappingPSICS file found in props to be: "+ absFile);

                File newFile = GeneralUtils.copyFileIntoDir(absFile, dirForCMLFiles);

                SimulatorMapping mapping = new SimulatorMapping(newFile.getName(),
                                                          SimEnvHelper.PSICS, false);

                cmlMech.addSimMapping(mapping);

            }



            // If not found by the properties, look in the dir itself...

            for (int i = 0; i < contents.length; i++)
            {
                if (props.getProperty("ChannelMLFile")==null &&
                    contents[i].getName().endsWith(ChannelMLConstants.DEFAULT_FILE_EXTENSION))
                {
                    File newFile = GeneralUtils.copyFileIntoDir(contents[i], dirForCMLFiles);
                    cmlMech.setXMLFile(newFile.getName());

                }
                if (contents[i].getName().endsWith(ChannelMLConstants.DEFAULT_MAPPING_EXTENSION))
                {
                    File newFile = GeneralUtils.copyFileIntoDir(contents[i], dirForCMLFiles);
                    //cmlMech.setChannelMLFile(newFile.getName());

                    if (props.getProperty("MappingNEURON")==null &&
                        newFile.getName().startsWith(SimEnvHelper.NEURON))
                    {
                        SimulatorMapping mapping = new SimulatorMapping(newFile.getName(),
                                                                  SimEnvHelper.NEURON, true); // true can be reset later

                        cmlMech.addSimMapping(mapping);
                    }
                    if (props.getProperty("MappingGENESIS")==null &&
                        newFile.getName().startsWith(SimEnvHelper.GENESIS))
                    {
                        SimulatorMapping mapping = new SimulatorMapping(newFile.getName(),
                                                                  SimEnvHelper.GENESIS, false);

                        cmlMech.addSimMapping(mapping);
                    }
                }
            }

            //cmlMech.initialise(projManager.getCurrentProject());

            if (cmlMech.getXMLFile()==null)
            {
                GuiUtils.showErrorMessage(logger,
                                          "Error finding the "
                                          +ChannelMLConstants.DEFAULT_FILE_EXTENSION
                                          +" file for the Cell Mechanism: " + propCellMechanismName, null, this);

                return;
            }

            if (props.getProperty("NEURONNeedsCompilation") != null)
            {
                Boolean b = Boolean.parseBoolean(props.getProperty("NEURONNeedsCompilation"));
                cmlMech.getSimMapping(SimEnvHelper.NEURON).setRequiresCompilation(b.booleanValue());
            }

            if (props.getProperty("Description") != null)
            {
                cmlMech.setDescription(props.getProperty("Description"));
            }
            else
            {
                cmlMech.setDescription("Template based ChannelML file");
            }
            cmlMech.setMechanismModel("Template based ChannelML file");

            projManager.getCurrentProject().cellMechanismInfo.addCellMechanism(cmlMech);

            logger.logComment("Added CML process with main file: "+ cmlMech.getXMLFile());




            try
            {
                // This is to make sure the files have been copied into the correct dirs..
                Thread.sleep(1000);
            }
            catch (InterruptedException ex)
            {
                ex.printStackTrace();
            }

            //System.out.println("Chan mech: "+cmlMech.toString());

            cmlMech.initialise(projManager.getCurrentProject(), true);

            try
            {
                if (cmlMech.isPassiveNonSpecificCond())
                {
                    double revPot = projManager.getCurrentProject().simulationParameters.
                        getGlobalVLeak();

                    double condDens = 1 /
                        this.projManager.getCurrentProject().simulationParameters.getGlobalRm();

                    if (cmlMech.getUnitsUsedInFile().equals(ChannelMLConstants.SI_UNITS))
                    {
                        revPot = UnitConverter.getVoltage(revPot, UnitConverter.NEUROCONSTRUCT_UNITS, UnitConverter.GENESIS_SI_UNITS);
                        condDens = UnitConverter.getConductanceDensity(condDens, UnitConverter.NEUROCONSTRUCT_UNITS, UnitConverter.GENESIS_SI_UNITS);
                    }
                    else if (cmlMech.getUnitsUsedInFile().equals(ChannelMLConstants.PHYSIOLOGICAL_UNITS))
                    {
                        revPot = UnitConverter.getVoltage(revPot, UnitConverter.NEUROCONSTRUCT_UNITS, UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS);
                        condDens = UnitConverter.getConductanceDensity(condDens, UnitConverter.NEUROCONSTRUCT_UNITS, UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS);
                    }


                    cmlMech.getXMLDoc().setValueByXPath(ChannelMLConstants.getIonRevPotXPath(),
                                                        (float)revPot + "");
                    
                    cmlMech.getXMLDoc().setValueByXPath(ChannelMLConstants.getPostV1_7_3CondDensXPath(),
                                                        (float)condDens + "");

                }
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger,
                    "Error setting the default membrane resistance and leak potential on that passive conductance", ex, this);
            }



            ChannelMLEditor frame = new ChannelMLEditor(cmlMech, projManager.getCurrentProject(), this);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            Dimension frameSize = frame.getSize();
            if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
            if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;
            frame.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);


            cmlMech.saveCurrentState(projManager.getCurrentProject());

            frame.setVisible(true);

            this.projManager.getCurrentProject().markProjectAsEdited();

            //System.out.println("Shown the dialog");

        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Problem adding the Cell Mechanism from "+ fromDir.getAbsolutePath()
                                      +" into the project",
                                      ex, this);
            return;
        }
        catch (XMLMechanismException ex)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Problem adding the Cell Mechanism from "+ fromDir.getAbsolutePath()
                                      +" into the project",
                                      ex, this);
            return;
        }



    }

    void jButtonGenesisNumMethod_actionPerformed(ActionEvent e)
    {
        String request = "Please enter the new numerical integration method (-1, 0, 2, 3, 4, 5, 10 or 11)";

        String inputValue = JOptionPane.showInputDialog(this,
                                                        request,
                                                        projManager.getCurrentProject().genesisSettings.getNumMethod().getMethodNumber()+"");
        if (inputValue==null) return;
        try
        {
            int newNumMeth = Integer.parseInt(inputValue);
            projManager.getCurrentProject().genesisSettings.getNumMethod().setMethodNumber(newNumMeth);
        }
        catch (NumberFormatException ex)
        {
            GuiUtils.showErrorMessage(logger, "Please enter a valid numerical integration method number (-1, 0, 2, 3, 4, 5, 10 or 11)", ex, this);
            return;
        }

        if (projManager.getCurrentProject().genesisSettings.getNumMethod().getMethodNumber() ==10
            || projManager.getCurrentProject().genesisSettings.getNumMethod().getMethodNumber() ==11)
        {

            projManager.getCurrentProject().genesisSettings.getNumMethod().setHsolve(true);

            request = "Assuming use of hsolve. Please enter the chanmode (0 to 3 inclusive)";

            inputValue = JOptionPane.showInputDialog(this,
                                                            request,
                                                            projManager.getCurrentProject().genesisSettings.getNumMethod().
                                                            getChanMode() + "");
            if (inputValue == null)return;
            try
            {
                int newNumMeth = Integer.parseInt(inputValue);
                projManager.getCurrentProject().genesisSettings.getNumMethod().setChanMode(newNumMeth);
            }
            catch (NumberFormatException ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a valid chanmode number", ex, this);
                return;
            }
        }
        else
        {
            projManager.getCurrentProject().genesisSettings.getNumMethod().setHsolve(false);
        }

        projManager.getCurrentProject().markProjectAsEdited();
        refreshTabGenesis();
        //
    }

    void jMenuItemCopyProject_actionPerformed(ActionEvent e)
    {
        if (!checkToSave()) return;

        doSaveAs();

    }

    public void doSaveAs()
    {

        String oldProjName = projManager.getCurrentProject().getProjectName();
        File oldProjFile = projManager.getCurrentProject().getProjectFile();

        logger.logComment("Creating copy of project...");


        JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose directory for copy of project");

        try
        {
            chooser.setCurrentDirectory(GeneralProperties.getnCProjectsDir());
        }
        catch (Exception ex)
        {
            logger.logError("Problem with default dir setting: "
                            + projManager.getCurrentProject().getProjectMainDirectory(), ex);
        }


        int retval = chooser.showDialog(this, "Select");

        if (retval != JFileChooser.APPROVE_OPTION)
        {
            logger.logComment("User cancelled...");

            return;
        }



        String newProjectName = JOptionPane.showInputDialog(this,
                                    "Please enter the name of the new project",
                                    oldProjName+"_copy");


        File newProjdir = new File(chooser.getSelectedFile(), newProjectName);



        if (newProjdir.exists())
        {
            GuiUtils.showErrorMessage(logger, "The file " + newProjdir
                                      + " already exists. Please use another name for the copy of this project", null, this);
            return;
        }

        this.closeProject();

        newProjdir.mkdir();

        File newProjectFile = null;

        try
        {
            File tempProjectFile = GeneralUtils.copyFileIntoDir(oldProjFile, newProjdir);

            newProjectFile  = new File(tempProjectFile.getParentFile(),
                                       newProjectName
                                       + ProjectStructure.getNewProjectFileExtension());

            tempProjectFile.renameTo(newProjectFile);



            GeneralUtils.copyDirIntoDir(ProjectStructure.getMorphologiesDir(oldProjFile.getParentFile()),
                                        ProjectStructure.getMorphologiesDir(newProjdir), false, true);

        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem creating copy of the project", ex, this);
            return;
        }
        try
        {
            File impMorphDir = ProjectStructure.getImportedMorphologiesDir(oldProjFile.getParentFile(), false);

            if (impMorphDir!=null)
            {
                GeneralUtils.copyDirIntoDir(impMorphDir,
                                            ProjectStructure.getImportedMorphologiesDir(newProjdir, true),
                    false, true);
            }
        }
        catch (IOException ex)
        {
            logger.logError("Problem copying DirForImportedMorphologies", ex);
            // continuing...
        }

        File cellMechDir = ProjectStructure.getCellMechanismDir(oldProjFile.getParentFile(), false);
        if (cellMechDir!=null)
        {
            try
            {
                GeneralUtils.copyDirIntoDir(cellMechDir,
                                            ProjectStructure.getCellMechanismDir(newProjdir), true, true);
            }
            catch (IOException ex)
            {
                logger.logError("Problem copying DirFormportedCellProcesses", ex);
                // continuing...
            }
        }

        File oldCellProcDir = ProjectStructure.getCellProcessesDir(oldProjFile.getParentFile(), false);
        if (oldCellProcDir!=null)
        {
            try
            {
                GeneralUtils.copyDirIntoDir(oldCellProcDir,
                                            ProjectStructure.getCellProcessesDir(newProjdir, true), true, true);

            }
            catch (IOException ex)
            {
                logger.logError("Problem copying DirFormportedCellProcesses", ex);
                // continuing...
            }
        }


        File pythonDir = ProjectStructure.getPythonScriptsDir(oldProjFile.getParentFile(), false);
        if (pythonDir!=null)
        {
            try
            {
                GeneralUtils.copyDirIntoDir(pythonDir,
                                            ProjectStructure.getPythonScriptsDir(newProjdir, true), true, true);
            }
            catch (IOException ex)
            {
                logger.logError("Problem copying python dirs", ex);
                // continuing...
            }
        }



        doLoadProject(newProjectFile.getAbsolutePath());

        projManager.getCurrentProject().setProjectName(newProjectName);

        ProjectStructure.getSimulationsDir(projManager.getCurrentProject().getProjectMainDirectory());

        projManager.getCurrentProject().markProjectAsEdited();
        doSave();


    }


    void jButtonSimPlotAdd_actionPerformed(ActionEvent e)
    {
        if (projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups()==0)
        {
            GuiUtils.showErrorMessage(logger, "Please add one or more Cell Groups before proceeding", null, this);
            return;
        }

        Vector allSimRefs = projManager.getCurrentProject().simPlotInfo.getAllSimPlotRefs();
        logger.logComment("All refs: "+ allSimRefs);
        int suggestedNum = 0;
        String suggestedRef = "Var_"+ suggestedNum;

        while (allSimRefs.contains(suggestedRef))
        {
            suggestedNum++;
            suggestedRef = "Var_"+ suggestedNum;
        }


        SimPlotDialog dlg
            = new SimPlotDialog(this,suggestedRef,
                                    projManager.getCurrentProject());

        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);

        if (dlg.cancelled)
        {
            logger.logComment("They've changed their mind...");
            return;
        }
        projManager.getCurrentProject().markProjectAsEdited();
        projManager.getCurrentProject().simPlotInfo.addSimPlot(dlg.getFinalSimPlot());
        refreshTabInputOutput();


        if (this.projManager.getCurrentProject().simConfigInfo.getNumSimConfigs()==1)
        {
            projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().addPlot(dlg.getFinalSimPlot().getPlotReference());
            logger.logComment("Now plots in default SimConfig: "+ projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().getPlots());
        }
        else
        {
            GuiUtils.showInfoMessage(logger, "Added variable to plot/save", "There is more than one Simulation Configuration. To include this variable to plot/save in one of them, go to tab Generate.", this);
        }


    }


    void jButtonSimPlotDelete_actionPerformed(ActionEvent e)
    {
        int selectedRow = jTableSimPlot.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }
        projManager.getCurrentProject().simPlotInfo.deleteSimPlot(selectedRow);

        projManager.getCurrentProject().markProjectAsEdited();
        logger.logComment("Removed row: " + selectedRow);

        projManager.getCurrentProject().simConfigInfo.validateStoredSimConfigs(projManager.getCurrentProject());


        refreshTabInputOutput();

    }
    
    
    void jButtonSimPlotEdit_actionPerformed(ActionEvent e)
    {
        int selectedRow = jTableSimPlot.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }

        SimPlot selectedSimPlot = projManager.getCurrentProject().simPlotInfo.getSimPlot(selectedRow);

        SimPlotDialog dlg
            = new SimPlotDialog(this, selectedSimPlot.getPlotReference(),
                                projManager.getCurrentProject());

        dlg.setSimPlot(selectedSimPlot);

        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);

        if (dlg.cancelled)
        {
            logger.logComment("They've changed their mind...");
            return;
        }
        projManager.getCurrentProject().markProjectAsEdited();
        projManager.getCurrentProject().simPlotInfo.updateSimPlot(dlg.getFinalSimPlot());
        refreshTabInputOutput();
    }
    
    
    void jButtonSimPlotCopy_actionPerformed(ActionEvent e)
    {
        int selectedRow = jTableSimPlot.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }

        SimPlot selSimPlot = projManager.getCurrentProject().simPlotInfo.getSimPlot(selectedRow);
        
        SimPlot newSimPlot = (SimPlot)selSimPlot.clone();
        
        String newName = GeneralUtils.incrementName(selSimPlot.getPlotReference());
        
        while (projManager.getCurrentProject().simPlotInfo.getSimPlot(newName)!=null)
        {
            newName = GeneralUtils.incrementName(newName);
            logger.logComment("Testing name: "+ newName);
        }
        
        newSimPlot.setPlotReference(newName);

        SimPlotDialog dlg
            = new SimPlotDialog(this, newSimPlot.getPlotReference(),
                                projManager.getCurrentProject());

        dlg.setSimPlot(newSimPlot);

        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);

        if (dlg.cancelled)
        {
            logger.logComment("They've changed their mind...");
            return;
        }
        projManager.getCurrentProject().markProjectAsEdited();
        projManager.getCurrentProject().simPlotInfo.addSimPlot(dlg.getFinalSimPlot());
        refreshTabInputOutput();
        
        if (this.projManager.getCurrentProject().simConfigInfo.getNumSimConfigs()==1)
        {
            projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().addPlot(dlg.getFinalSimPlot().getPlotReference());
            logger.logComment("Now plots in default SimConfig: "+ projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().getPlots());
        }
        else
        {
            GuiUtils.showInfoMessage(logger, "Added variable to plot/save", "There is more than one Simulation Configuration. To include this variable to plot/save in one of them, go to tab Generate.", this);
        }
    }
    
    
    
    

    void jButtonSimStimAdd_actionPerformed(ActionEvent e)
    {
        if (projManager.getCurrentProject().cellGroupsInfo.getNumberCellGroups()==0)
        {
            GuiUtils.showErrorMessage(logger, "Please add one or more Cell Groups before proceeding", null, this);
            return;
        }
        Vector allStimRefs = projManager.getCurrentProject().elecInputInfo.getAllStimRefs();
        logger.logComment("All refs: "+ allStimRefs);
        int suggestedNum = 0;
        String suggestedRef = "Input_"+ suggestedNum;

        while (allStimRefs.contains(suggestedRef))
        {
            suggestedNum++;
            suggestedRef = "Input_"+ suggestedNum;
        }
        
        projManager.getCurrentProject().neuronFileManager.forceNextModRecompile();


        StimDialog dlg
            = new StimDialog(this,suggestedRef,
                                    projManager.getCurrentProject());

        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);

        if (dlg.cancelled)
        {
            logger.logComment("They've changed their mind...");
            return;
        }
        projManager.getCurrentProject().markProjectAsEdited();
        projManager.getCurrentProject().elecInputInfo.addStim(dlg.getFinalStim());
        refreshTabInputOutput();


        if (this.projManager.getCurrentProject().simConfigInfo.getNumSimConfigs()==1)
        {
            projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().addInput(dlg.getFinalStim().getReference());
            logger.logComment("Now inputs in default SimConfig: "+ projManager.getCurrentProject().simConfigInfo.getDefaultSimConfig().getInputs());
        }
        else
        {
            GuiUtils.showInfoMessage(logger, "Added Input", "There is more than one Simulation Configuration. To include this Input in one of them, go to tab Generate.", this);
        }


    }



    void jButtonSimStimDelete_actionPerformed(ActionEvent e)
    {
        int selectedRow = jTableStims.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }
        projManager.getCurrentProject().elecInputInfo.deleteStim(selectedRow);

        projManager.getCurrentProject().markProjectAsEdited();

        logger.logComment("Removed row: " + selectedRow);

        projManager.getCurrentProject().simConfigInfo.validateStoredSimConfigs(projManager.getCurrentProject());


        refreshTabInputOutput();

    }


    void jButtonSimStimCopy_actionPerformed(ActionEvent e)
    {
            logger.logComment("jButtonSimStimCopy_actionPerformed...");
        int selectedRow = jTableStims.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }
            logger.logComment("jButtonSimStimCopy_actionPerformed...");
            
        //boolean proceed = GuiUtils.showYesNoMessage(logger, "Warning, have you saved the project recently??", this);

        
        //if (!proceed) return;
        
        StimulationSettings selectedStim = projManager.getCurrentProject().elecInputInfo.getStim(selectedRow);
        
        
        projManager.getCurrentProject().neuronFileManager.forceNextModRecompile();

        StimDialog dlg
            = new StimDialog(this,selectedStim.getReference(),
                                    projManager.getCurrentProject());


        dlg.setStim(selectedStim);

        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);

        if (dlg.cancelled)
        {
            logger.logComment("They've changed their mind...");
            return;
        }
        
        projManager.getCurrentProject().markProjectAsEdited();
        projManager.getCurrentProject().elecInputInfo.updateStim(dlg.getFinalStim());
        refreshTabInputOutput();

    }
    
    
    

    void jButtonSimStimEdit_actionPerformed(ActionEvent e)
    {
        int selectedRow = jTableStims.getSelectedRow();

        if (selectedRow < 0)
        {
            logger.logComment("No row selected...");
            return;
        }

        this.projManager.getCurrentProject().markProjectAsEdited();
        
        projManager.getCurrentProject().neuronFileManager.forceNextModRecompile();

        StimulationSettings selectedStim = projManager.getCurrentProject().elecInputInfo.getStim(selectedRow);

        StimDialog dlg
            = new StimDialog(this,selectedStim.getReference(),
                                    projManager.getCurrentProject());

        dlg.setStim(selectedStim);

        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);

        if (dlg.cancelled)
        {
            logger.logComment("They've changed their mind...");
            return;
        }
        projManager.getCurrentProject().markProjectAsEdited();
        projManager.getCurrentProject().elecInputInfo.updateStim(dlg.getFinalStim());
        refreshTabInputOutput();

    }

    void jMenuItemHelp_actionPerformed(ActionEvent e)
    {

        logger.logComment("Going to show help menu...");

        File f = ProjectStructure.getMainHelpFile();
        
        try
        {
            HelpFrame.showFrame(f.toURI().toURL(), "neuroConstruct Help documentation", false);
        }
        catch (MalformedURLException m)
        {
            
        }
        
    }
    
    void jMenuItemHelpRelNotes_actionPerformed(ActionEvent e)
    {

        logger.logComment("Going to show help menu...");

        File f = ProjectStructure.getMainHelpFile();
        
        try
        {
            HelpFrame.showFrame(f.toURI().toURL(), "neuroConstruct Help documentation", false);
        }
        catch (MalformedURLException m)
        {
            
        }
        
    }
    
    
    
    


    public void jButtonNeuroMLValidate_actionPerformed(ActionEvent e)
    {
        logger.logComment("Validating...");


        File v1schemaFile = GeneralProperties.getNeuroMLSchemaFile();

        try
        {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            logger.logComment("Found the XSD file: " + v1schemaFile.getAbsolutePath());

            Source schemaFileSource = new StreamSource(v1schemaFile);
            Schema schema = factory.newSchema(schemaFileSource);

            Validator validator = schema.newValidator();

            String filename = jComboBoxNeuroML.getSelectedItem().toString();
            filename = filename.substring(0,filename.indexOf("(")).trim();
            Source xmlFileSource = new StreamSource(new File(filename));

            validator.validate(xmlFileSource);

            GuiUtils.showInfoMessage(logger, "Valid NeuroML v1.x file", "NeuroML file is well formed and valid, according to schema:\n"
                                 + v1schemaFile.getAbsolutePath()+"\n\nNote: to change the version of the NeuroML schema with which to validate the file, go to:\n" +
                                 "Settings -> General Properties & Project Defaults -> NeuroML version", this);

            return;

        }
        catch (Exception ex)
        {

        }

        File v2alphaSchemaFile = GeneralProperties.getNeuroMLv2alphaSchemaFile();

        try
        {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            logger.logComment("Found the XSD file: " + v2alphaSchemaFile.getAbsolutePath());

            Source schemaFileSource = new StreamSource(v2alphaSchemaFile);
            Schema schema = factory.newSchema(schemaFileSource);

            Validator validator = schema.newValidator();

            String filename = jComboBoxNeuroML.getSelectedItem().toString();
            filename = filename.substring(0,filename.indexOf("(")).trim();
            Source xmlFileSource = new StreamSource(new File(filename));

            validator.validate(xmlFileSource);

            GuiUtils.showInfoMessage(logger, "Valid NeuroML v2alpha file", "NeuroML file is well formed and valid, according to schema:\n"
                                 + v2alphaSchemaFile.getAbsolutePath()+"", this);

            return;

        }
        catch (Exception ex)
        {

        }

        File v2betaSchemaFile = GeneralProperties.getNeuroMLv2betaSchemaFile();

        try
        {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            logger.logComment("Found the XSD file: " + v2betaSchemaFile.getAbsolutePath());

            Source schemaFileSource = new StreamSource(v2betaSchemaFile);
            Schema schema = factory.newSchema(schemaFileSource);

            Validator validator = schema.newValidator();

            String filename = jComboBoxNeuroML.getSelectedItem().toString();
            filename = filename.substring(0,filename.indexOf("(")).trim();
            Source xmlFileSource = new StreamSource(new File(filename));

            validator.validate(xmlFileSource);

            GuiUtils.showInfoMessage(logger, "Valid NeuroML v2beta file", "NeuroML file is well formed and valid, according to schema:\n"
                                 + v2betaSchemaFile.getAbsolutePath()+"", this);

            return;

        }
        catch (Exception ex)
        {

        }

        GuiUtils.showErrorMessage(logger, "Problem validating the NeuroML file. Note that the file was validated\n"
                                  + "against the following schemas:\n"
                                  +"    "+v1schemaFile+"\n"
                                  +"    "+v2alphaSchemaFile+"\n"
                                  +"    "+v2betaSchemaFile, null, this);


    }


    void jButtonCellTypeEditDesc_actionPerformed(ActionEvent e)
    {
        //int selIndex = jComboBoxCellTypes.getSelectedIndex();
        Cell cell = (Cell)jComboBoxCellTypes.getSelectedItem();

        String oldDecs = cell.getCellDescription();

        SimpleTextInput sti = SimpleTextInput.showString(cell.getCellDescription(),
                                                         "Description of Cell: " + cell.getInstanceName(),
                                                         12,
                                                         false,
                                                         false,
                                                         .4f,
                                                         this);

        String newDesc = sti.getString();
        cell.setCellDescription(newDesc);


        if (!oldDecs.equals(newDesc))
        {
            projManager.getCurrentProject().markProjectAsEdited();
            this.refreshTabCellTypes();
        }


    }

    void jButtonCellTypeBioPhys_actionPerformed(ActionEvent e)
    {



        Cell cell = (Cell)jComboBoxCellTypes.getSelectedItem();
        NumberGenerator ngInitPot = NumberGeneratorDialog.showDialog(this,"Initial Membrane Potential",
                                                                     "Initial Membrane Potential, units: "+
                                                                     UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol(),
                                                                     cell.getInitialPotential());

        cell.setInitialPotential(ngInitPot);
/*
        NumberGenerator ngSpecAxRes = NumberGeneratorDialog.showDialog(this,
                                                                       "Specific Axial Resistance, units: "+
                                                                     UnitConverter.specificAxialResistanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol(),
                                                                     cell.getSpecAxRes());
      cell.setSpecAxRes(ngSpecAxRes);

      NumberGenerator ngSpecCap = NumberGeneratorDialog.showDialog(this,
                                                                   "Specific Capacitance, units: "+
                                                                     UnitConverter.specificCapacitanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol(),
                                                                   cell.getSpecCapacitance());
      cell.setSpecCapacitance(ngSpecCap);

*/
       projManager.getCurrentProject().markProjectAsEdited();

        refreshTabCellTypes();
    }

    void jButtonCellTypeOtherProject_actionPerformed(ActionEvent e)
    {
        // set to parent of project dir...
        File defaultDir = projManager.getCurrentProject().getProjectFile().getParentFile().getParentFile();

        Frame frame = (Frame)this;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setDialogTitle("Choose neuroConstruct project from which to import Cell Type");

        try
        {
            chooser.setCurrentDirectory(defaultDir);
            logger.logComment("Set Dialog dir to: " + defaultDir);
        }
        catch (Exception ex)
        {
            logger.logError("Problem with default dir setting: " + defaultDir, ex);
        }
        SimpleFileFilter fileFilter = ProjectStructure.getProjectFileFilter();

        chooser.setFileFilter(fileFilter);

        //chooser.sett

        int retval = chooser.showDialog(frame, null);

        if (retval == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                logger.logComment(">>>>  Loading project: " + chooser.getSelectedFile());

                Project otherProj = Project.loadProject(chooser.getSelectedFile(), this);

                logger.logComment("<<<<  Loaded project: " + otherProj.getProjectFileName());

                ArrayList<String> otherCellTypes = otherProj.cellManager.getAllCellTypeNames();

                if (otherCellTypes.isEmpty())
                {
                    GuiUtils.showErrorMessage(logger, "No Cell Types found in that project.", null, this);
                    return;
                }

                Object selection = JOptionPane.showInputDialog(this,
                            "Please select the Cell Type to import from project "+otherProj.getProjectName(),
                            "Select Cell Type",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            otherCellTypes.toArray(),
                            otherCellTypes.get(0));

                if (selection==null)
                {
                    logger.logComment("No selection made...");
                    return;
                }
                logger.logComment("Selection: "+ selection);

                Cell importedCell = otherProj.cellManager.getCell((String)selection);

                String originalCellTypeName = importedCell.getInstanceName();

                if (projManager.getCurrentProject().cellManager.getCell((String)selection)!=null)
                {
                    String suggestedName = importedCell.getInstanceName()+"_"+otherProj.getProjectName();
                    String newName = JOptionPane.showInputDialog(this, "This project already contains a Cell Type "+ importedCell.getInstanceName()
                                                                 +". Please enter\nanother name for the Cell Type", suggestedName);

                    if (newName==null)
                    {
                        logger.logComment("User cancelled...");
                        return;
                    }



                    importedCell.setInstanceName(newName);
                }
                projManager.getCurrentProject().cellManager.addCellType(importedCell);



                jComboBoxCellTypes.setSelectedItem(importedCell);

                ArrayList<Object> cellMechNames = new ArrayList<Object>();
                cellMechNames.addAll(importedCell.getAllChanMechNames(true));
                
                ArrayList<String> synapses = importedCell.getAllAllowedSynapseTypes();

                cellMechNames.addAll(synapses);

                Vector allStims = otherProj.elecInputInfo.getAllStims();
                for (int i = 0; i < allStims.size(); i++)
                {
                    StimulationSettings next = (StimulationSettings)allStims.elementAt(i);
                    logger.logComment("Investigating stim on other proj: "+ next);
                    //if ()
                    String cellType = otherProj.cellGroupsInfo.getCellType(next.getCellGroup());

                    if (cellType != null &&
                        cellType.equals(originalCellTypeName) &&
                        next instanceof RandomSpikeTrainSettings)
                    {
                        String spikeCPName = ((RandomSpikeTrainSettings)next).getSynapseType();
                        cellMechNames.add(spikeCPName);
                    }
                }

                logger.logComment("Cell mechs on imported cell: "+ cellMechNames);

                for (int i = 0; i < cellMechNames.size(); i++)
                {
                    String cellMechName = null;
                    if (cellMechNames.get(i) instanceof ChannelMechanism)
                    {
                        ChannelMechanism nextChanMech = (ChannelMechanism)cellMechNames.get(i);
                        cellMechName = nextChanMech.getName();
                    }
                    else
                    {
                        cellMechName = (String)cellMechNames.get(i);
                    }


                    CellMechanism importedCellMech = otherProj.cellMechanismInfo.getCellMechanism(cellMechName);

                    logger.logComment("---  Imported cell mech: ");

                    if (projManager.getCurrentProject().cellMechanismInfo.getAllCellMechanismNames().contains(cellMechName))
                    {
                        String oldName = cellMechName;


                        int useExisting = JOptionPane.showConfirmDialog(this, "This project already contains a Cell Mechanism called "
                                                          + importedCellMech.getInstanceName()
                                                          +". Do you want to use the current project's "
                                                          +importedCellMech.getInstanceName()+" on the Cell:\n"
                                                          + importedCell.getInstanceName()
                                                          +"?\n\nSelect No to import and rename the Cell Mechanism as used in project: "
                                                          + otherProj.getProjectName() + "?",
                                                          "Use current Cell Mechanism?",
                                                          JOptionPane.YES_NO_CANCEL_OPTION);

                        if (useExisting==JOptionPane.CANCEL_OPTION)
                        {
                            logger.logComment("User cancelled...");
                            return;
                        }
                        else if (useExisting==JOptionPane.NO_OPTION)
                        {
                            String suggestedName = importedCellMech.getInstanceName()+"_"+otherProj.getProjectName();
                            String newName = JOptionPane.showInputDialog(this,
                                "Please enter another name for the Cell Mechanism which is present on the Cell: "
                                + importedCell.getInstanceName(), suggestedName);

                            if (newName==null)
                            {
                                logger.logComment("User cancelled...");
                                return;
                            }


                            ///importedCellMech.setInstanceName(newName);

                            Hashtable chanMechVsGroups = importedCell.getChanMechsVsGroups();
                            Enumeration enumeration = chanMechVsGroups.keys();
                            while (enumeration.hasMoreElements())
                            {
                                ChannelMechanism next = (ChannelMechanism) enumeration.nextElement();
                                if (next.getName().equals(oldName)) next.setName(newName);
                            }

                            Hashtable<String, Vector<String>> synapsesVsGroups = importedCell.getSynapsesVsGroups();
                            enumeration = synapsesVsGroups.keys();
                            while (enumeration.hasMoreElements())
                            {
                                String next = (String) enumeration.nextElement();
                                if (next.equals(oldName))
                                {
                                    synapsesVsGroups.put(newName, synapsesVsGroups.get(oldName));
                                    synapsesVsGroups.remove(oldName);
                                }
                            }

                            if (importedCellMech instanceof FileBasedMembraneMechanism)
                            {
                                logger.logComment("Copying the impl file into the project");

                                FileBasedMembraneMechanism fbmp = (FileBasedMembraneMechanism) importedCellMech;
                                MechanismImplementation procImpl[] = fbmp.getMechanismImpls();

                                for (int j = 0; j < procImpl.length; j++)
                                {
                                    File nextFile = procImpl[j].getImplementingFileObject(otherProj, fbmp.getInstanceName());

                                    logger.logComment("Copying file : "+nextFile);

                                    File newLocation = ProjectStructure.getDirForCellMechFiles(projManager.getCurrentProject(), newName, true);

                                    logger.logComment("Into location: "+ newLocation);

                                    File newFile = null;
                                    try
                                    {
                                        newFile = GeneralUtils.copyFileIntoDir(nextFile, newLocation);
                                        ((FileBasedMembraneMechanism)importedCellMech).getMechanismImpls()[j].setImplementingFile(newFile.getName());
                                        importedCellMech.setInstanceName(newName);
                                    }
                                    catch (IOException ex)
                                    {
                                        GuiUtils.showErrorMessage(logger, "Problem when including new Cell Mechanism", ex, this);
                                        return;
                                    }
                                }
                            }
                            else if (importedCellMech instanceof ChannelMLCellMechanism)
                            {
                                logger.logComment("Copying the ChannelMLCellMechanism files into the project");

                                File otherProjCellMechFilesLoc = new File(
                                    ProjectStructure.getCellMechanismDir(otherProj.getProjectMainDirectory()),
                                    oldName);

                                logger.logComment("Copying file : "+otherProjCellMechFilesLoc);

                                File thisProjCellMechFilesLoc = new File(
                                    ProjectStructure.getCellMechanismDir(projManager.getCurrentProject().getProjectMainDirectory()),
                                    newName);

                                thisProjCellMechFilesLoc.mkdir();

                                logger.logComment("Into location: "+ thisProjCellMechFilesLoc);

                                GeneralUtils.copyDirIntoDir(otherProjCellMechFilesLoc, thisProjCellMechFilesLoc, false, true);
                                
                                File propsFile = new File(thisProjCellMechFilesLoc, CellMechanismHelper.PROPERTIES_FILENAME);
                                
                                logger.logComment("Initialising cell mech with props from: "+propsFile.getAbsolutePath());

                                ChannelMLCellMechanism cmlCm = new ChannelMLCellMechanism();

                                cmlCm.initPropsFromPropsFile(propsFile);
                                
                                try
                                {
                                    cmlCm.setInstanceName(newName);

                                    cmlCm.initialise(projManager.getCurrentProject(), true);


                                    importedCellMech = cmlCm;

                                   logger.logComment("CML Channel mech: "+ cmlCm.toString());
                                }
                                catch (XMLMechanismException ex1)
                                {
                                    GuiUtils.showErrorMessage(logger,
                                                              "Error creating implementation of Cell Mechanism: " +
                                                              newName,
                                                              ex1,
                                                              this);
                                }


                            }

                            projManager.getCurrentProject().cellMechanismInfo.addCellMechanism(importedCellMech);


                        }
                        else
                        {
                            logger.logComment("Using existing Cell Mech...");
                        }


                    }
                    else
                    {
                        logger.logComment("Cell Mech: "+ importedCellMech.getInstanceName()+ " not already in proj...");
                        projManager.getCurrentProject().cellMechanismInfo.addCellMechanism(importedCellMech);


                        if (importedCellMech instanceof FileBasedMembraneMechanism)
                        {

                            FileBasedMembraneMechanism fbmp = (FileBasedMembraneMechanism) importedCellMech;

                            logger.logComment("Copying the impl files from: "+fbmp.getInstanceName()+" into the project");

                            MechanismImplementation procImpl[] = fbmp.getMechanismImpls();

                            for (int j = 0; j < procImpl.length; j++)
                            {
                                logger.logComment("Looking at sim env: "+ procImpl[j].getSimulationEnvironment()
                                                  + ", file: "+ procImpl[j].getImplementingFile());

                                File nextFile = procImpl[j].getImplementingFileObject(otherProj, fbmp.getInstanceName());

                                File newLocation = ProjectStructure.getFileBasedCellProcessesDir(projManager.getCurrentProject().getProjectMainDirectory(), true);

                                File newFile = null;
                                try
                                {
                                    newFile = GeneralUtils.copyFileIntoDir(nextFile,
                                                                           newLocation);

                                    ((FileBasedMembraneMechanism)importedCellMech).getMechanismImpls()[j].setImplementingFile(newFile.getName());


                                }
                                catch (IOException ex)
                                {
                                    GuiUtils.showErrorMessage(logger, "Problem when including new Cell Mechanism", ex, this);
                                    return;
                                }

                            }
                        }
                        else if (importedCellMech instanceof ChannelMLCellMechanism)
                        {
                            logger.logComment("Copying the ChannelMLCellProcess files into the project");
                            //ChannelMLCellMechanism cmlp = (ChannelMLCellMechanism) importedCellProc;

                            File otherProjCellMechFilesLoc = new File(ProjectStructure.getCellMechanismDir(otherProj.getProjectMainDirectory()),
                                cellMechName);

                            File thisProjCellMechFilesLoc = new File(ProjectStructure.getCellMechanismDir(projManager.getCurrentProject().getProjectMainDirectory()),
                                cellMechName);

                            thisProjCellMechFilesLoc.mkdir();

                            GeneralUtils.copyDirIntoDir(otherProjCellMechFilesLoc, thisProjCellMechFilesLoc, false, true);
                        }

                    }



                }
                float thisTemp = projManager.getCurrentProject().simulationParameters.getTemperature();
                float otherTemp = otherProj.simulationParameters.getTemperature();

                if (thisTemp!=otherTemp)
                {
                    GuiUtils.showInfoMessage(logger, "Warning",
                                             "Please note that the imported cell's project simulation temperature was "+otherTemp
                                             +" whereas this project is set to run simulations at "+thisTemp+".\n This will lead to different behaviours of the cell"
                                             + " if there are channels with temperature dependent rate equations.", this);
                }

                projManager.getCurrentProject().markProjectAsEdited();
                this.refreshAll();

                GuiUtils.showInfoMessage(logger, "Warning",
                                         "Cell successfully imported. You should save the project to make sure all new files are correctly stored!", this);

            }
            catch (Exception ex2)
            {
                GuiUtils.showErrorMessage(logger, "Problem adding the Cell", ex2, this);

                return;
            }
        }

    }

    void jMenuItemJava_actionPerformed(ActionEvent e)
    {
        Properties props = System.getProperties();
        Enumeration names = props.propertyNames();
        ArrayList ordered = GeneralUtils.getOrderedList(names, true);

        int idealPropNameWidth = 30;
        int idealTotalWidth = 120;

        StringBuilder sb= new StringBuilder();

        sb.append("    Java properties:\n\n");

        for(Object next: ordered)
        {
            String propName = (String) next;
            String val = props.getProperty(propName);
            propName = propName+": ";
            if (propName.length()<=idealPropNameWidth)
            {

                for (int i = propName.length(); i <= idealPropNameWidth ; i++)
                {
                        propName = propName + " ";
                }
            }
            sb.append(GeneralUtils.wrapLine(propName + val, "\n", idealTotalWidth) + "\n");
        }
        
        
        sb.append("\n\n    System properties:\n\n");
        
        Map<String,String> envProps = System.getenv();
        Set<String> set = envProps.keySet();
        AbstractList allEnv = new ArrayList<String>(set);
        allEnv = GeneralUtils.reorderAlphabetically(allEnv, true);
        
        for(Object prop: allEnv)
        {
            Object val = envProps.get((String)prop);
            String propName = prop.toString();
            if (propName.length()<=idealPropNameWidth)
            {

                for (int i = propName.length(); i <= idealPropNameWidth ; i++)
                {
                        propName = propName + " ";
                }
            }
            sb.append(GeneralUtils.wrapLine(propName + val, "\n", idealTotalWidth) + "\n");
        }
        

        //sb.append("\nMemory usage:\n\n");
        
        sb.append("\n\n    Further system information:\n\n");
        
        MemoryMXBean mxbean = ManagementFactory.getMemoryMXBean();
        MemoryUsage muh = mxbean.getHeapMemoryUsage();
        sb.append("Heap Memory usage: "+muh+"\n");
        
        
        RuntimeMXBean rbean = ManagementFactory.getRuntimeMXBean();
        
        
        
        sb.append("Number available processors: "+ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors()+"\n");
        sb.append("JVM name: "+rbean.getName()+"\n");
        //sb.append("Boot classpath: "+rbean.getBootClassPath()+"\n\n");
        sb.append("Args to JVM: "+rbean.getInputArguments()+"\n");

        boolean useHtml = false;

        SimpleViewer.showString(sb.toString(), "Java system properties", 12, false, useHtml);

    }

    void jMenuItemGenNetwork_actionPerformed(ActionEvent e)
    {
        if (!projManager.projectLoaded()) return;

        jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(this.GENERATE_TAB));

        doGenerate();

    }


    void jMenuItemGenNeuronHoc_actionPerformed(ActionEvent e)
    {
        if (!projManager.projectLoaded()) return;

        jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(this.EXPORT_TAB));
        jTabbedPaneExportFormats.setSelectedIndex(jTabbedPaneExportFormats.indexOfTab(this.NEURON_SIMULATOR_TAB));
        jTabbedPaneNeuron.setSelectedIndex(jTabbedPaneNeuron.indexOfTab(this.NEURON_TAB_GENERATE));

        doCreateHoc(NeuronFileManager.RUN_HOC);

    }

    void jMenuItemGenNeuronPyXML_actionPerformed(ActionEvent e)
    {
        if (!projManager.projectLoaded()) return;

        jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(this.EXPORT_TAB));
        jTabbedPaneExportFormats.setSelectedIndex(jTabbedPaneExportFormats.indexOfTab(this.NEURON_SIMULATOR_TAB));
        jTabbedPaneNeuron.setSelectedIndex(jTabbedPaneNeuron.indexOfTab(this.NEURON_TAB_GENERATE));

        doCreateHoc(NeuronFileManager.RUN_PYTHON_XML);

    }
    void jMenuItemGenNeuronPyHDF5_actionPerformed(ActionEvent e)
    {
        if (!projManager.projectLoaded()) return;

        jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(this.EXPORT_TAB));
        jTabbedPaneExportFormats.setSelectedIndex(jTabbedPaneExportFormats.indexOfTab(this.NEURON_SIMULATOR_TAB));
        jTabbedPaneNeuron.setSelectedIndex(jTabbedPaneNeuron.indexOfTab(this.NEURON_TAB_GENERATE));

        doCreateHoc(NeuronFileManager.RUN_PYTHON_HDF5);

    }



    void jMenuItemGenPsics_actionPerformed(ActionEvent e)
    {
        if (!projManager.projectLoaded()) return;

        jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(this.EXPORT_TAB));
        jTabbedPaneExportFormats.setSelectedIndex(jTabbedPaneExportFormats.indexOfTab(this.PSICS_SIMULATOR_TAB));

        doGeneratePsics();

    }


    void jMenuItemGenPynn_actionPerformed(ActionEvent e)
    {
        if (!projManager.projectLoaded()) return;

        jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(this.EXPORT_TAB));
        jTabbedPaneExportFormats.setSelectedIndex(jTabbedPaneExportFormats.indexOfTab(this.PYNN_SIMULATOR_TAB));

        doGeneratePynn();

    } 

    void jMenuItemGenGenesis_actionPerformed(ActionEvent e)
    {
        if (!projManager.projectLoaded()) return;

        jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(this.EXPORT_TAB));
        jTabbedPaneExportFormats.setSelectedIndex(jTabbedPaneExportFormats.indexOfTab(this.GENESIS_SIMULATOR_TAB));
        jTabbedPaneGenesis.setSelectedIndex(jTabbedPaneGenesis.indexOfTab(this.GENESIS_TAB_GENERATE));

        doCreateGenesis();

    }
    
    
    


    void jMenuItemPrevSims_actionPerformed(ActionEvent e)
    {
        if (!projManager.projectLoaded()) return;

        jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(this.VISUALISATION_TAB));

        doViewPrevSimulations();
    }


    void jMenuItemDataSets_actionPerformed(ActionEvent e)
    {
        if (!projManager.projectLoaded()) return;

        jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.indexOfTab(this.VISUALISATION_TAB));

        doShowDataSets();

    }

    void doShowDataSets()
    {
        //File dataSetDir = ProjectStructure.getDataSetsDir(projManager.getCurrentProject().getProjectMainDirectory());

        DataSetManager frame = new DataSetManager(projManager.getCurrentProject(), false);

        Dimension dlgSize = frame.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        frame.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);


        frame.pack();
        frame.setVisible(true);

    }




}
