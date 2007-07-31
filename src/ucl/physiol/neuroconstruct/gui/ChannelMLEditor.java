
/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2007 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.gui;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.xml.sax.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.XMLUtils;
import ucl.physiol.neuroconstruct.utils.xml.*;
import javax.swing.event.*;
import javax.xml.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.dataset.*;
import ucl.physiol.neuroconstruct.gui.plotter.*;
import ucl.physiol.neuroconstruct.utils.equation.*;

/**
 * Dialog for creating and editing ChannelML based Cell Mechanism
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

public class ChannelMLEditor extends JFrame implements HyperlinkListener
{

    ClassLogger logger = new ClassLogger("ChannelMLEditor");

    Project project = null;

    ChannelMLCellMechanism cmlMechanism = null;

    private final String SUMMARY_TAB = "Summary of file contents";
    private final String EDIT_TAB = "Edit";
    private final String CML_CONTENTS_TAB = "ChannelML file";

    /**
     * To avoid events when refreshing interface
     */
    boolean refreshingInterface = false;

    JTextArea jTextAreaMain = new JTextArea();
    JPanel jPanelMain = new JPanel();
    JTabbedPane jTabbedPane1 = new JTabbedPane();


    JPanel jPanelCMLFile = new JPanel();
    JPanel jPanelCMLSummary = new JPanel();


    JPanel jPanel3 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JButton jButtonCancel = new JButton("Close");

    JButton jButtonReload = new JButton("Reload ChannelML file");
    JButton jButtonPlot = new JButton("Generate relevant plots");
    JButton jButtonEditExt = new JButton("Edit ChannelML file externally");

    JButton jButtonEditProps = new JButton("Cell Mechanism properties file");

    JPanel jPanelTop = new JPanel();
    JPanel jPanelMid = new JPanel();
    JPanel jPanelBot= new JPanel();
    JPanel jPanelEdit = new JPanel();
    JPanel jPanelEditExt = new JPanel();
    JPanel jPanelEditInt = new JPanel();



    JScrollPane jScrollPaneList = new JScrollPane();

    JScrollPane jScrollPaneCMLFile = new JScrollPane();
    BorderLayout borderLayout2 = new BorderLayout();
    JEditorPane jEditorPaneCMLFile = new JEditorPane();

    JScrollPane jScrollPaneCMLSummary = new JScrollPane();
    BorderLayout borderLayoutSummary2 = new BorderLayout();
    JEditorPane jEditorPaneCMLSummary = new JEditorPane();



    Border border1 = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    Border border2 = BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white, Color.white,
                                                     new Color(103, 101, 98), new Color(148, 145, 140));

    Border border3 = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    Border border2s = BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white, Color.white,
                                                     new Color(103, 101, 98), new Color(148, 145, 140));

    Border border3s = BorderFactory.createEmptyBorder(5, 5, 5, 5);



    JTextArea jTextAreaValue = new JTextArea();
    JPanel jPanelList = new JPanel();
    JPanel jPanelInput = new JPanel();
    BorderLayout borderLayout3 = new BorderLayout();
    BorderLayout borderLayout4 = new BorderLayout();

    private DefaultListModel listModelXPaths = new DefaultListModel();

    private JList jListXPaths = new JList();
    JButton jButtonSave = new JButton();

    JButton jButtonValidate = new JButton();

    JTextArea jTextAreaXPathString = new JTextArea();

    JPanel jPanel1 = new JPanel();
    JPanel jPanel2 = new JPanel();
    BorderLayout borderLayout44 = new BorderLayout();
    Border border4 = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(Color.white,
        new Color(178, 178, 178)), BorderFactory.createEmptyBorder(3, 3, 3, 3));
    Border border5 = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(Color.white,
        new Color(178, 178, 178)), BorderFactory.createEmptyBorder(3, 3, 3, 3));


    JLabel jLabelFileName = new JLabel();

    ProjectEventListener eventIf = null;

    private ChannelMLEditor()
    {
        try
        {
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public ChannelMLEditor(ChannelMLCellMechanism cmlMechanism, Project project, ProjectEventListener eventIf)
    {
        super();

        this.project = project;
        this.eventIf = eventIf;
        this.cmlMechanism = cmlMechanism;


        try
        {
            File cmlFileUsed = cmlMechanism.initialise(project, false);  // in case..

            refreshingInterface = true;
            jbInit();
            refreshingInterface = false;

            this.jLabelFileName.setText("Editing ChannelML file: " + cmlFileUsed.getAbsolutePath());

            jLabelFileName.setHorizontalAlignment(jLabelFileName.CENTER);
            jLabelFileName.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            refresh();

            pack();

            logger.logComment(" ----  Done creating...  ----");
        }
        catch (Exception ex)
        {
            logger.logError("Exception starting GUI:", ex);
        }

    }

    private void jbInit() throws Exception
    {
        this.setTitle("Editing ChannelML Cell Mechanism: "+ this.cmlMechanism.getInstanceName());


        jListXPaths.setModel(listModelXPaths);
        jPanelMain.setLayout(borderLayout1);
        jButtonCancel.setText("Cancel");

        this.jEditorPaneCMLSummary.addHyperlinkListener(this);



        jButtonCancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonClose_actionPerformed(e);
            }
        });

        jButtonReload.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonReload_actionPerformed(e);
            }
        });

        jButtonPlot.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonPlot_actionPerformed(e);
            }
        });

        jButtonEditExt.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonEditExt_actionPerformed(e);
            }
        });

        jButtonEditProps.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonEditProps_actionPerformed(e);
            }
        });



        /** @todo Update and put to glossary */
        jButtonPlot.setToolTipText("Generates plots for various functions defined by the Cell Mechanism, e.g. rate equations. \n"
                                   +"Limited set of plots implemented at this time");


        /** @todo Put to glossary */
        jButtonEditExt.setToolTipText("Edits the file in an external text editor");


        /** @todo Put to glossary */
        jButtonEditProps.setToolTipText("Edits the file used for this Cell Mechanism's properties");


        /** @todo Put to glossary */
        jButtonValidate.setToolTipText("Validate the current ChannelML file with the current specifications:\n"+GeneralProperties.getChannelMLSchemaFile()+"");

        jButtonSave.setToolTipText("Saves any changes to the values in the Cell Mechanism **made through this interface** in an updated XML file");


        jButtonReload.setToolTipText("Reload the ChannelML file (if it has been externally modified) and update information above");

        jButtonCancel.setToolTipText("Close this window without saving any changes");

        jTextAreaMain.setText("  ***  Note: below is an experimental interface for altering the parameters in the ChannelML file  ***\n"
                              +"The ChannelML file can either be edited by an external text editor (recommended, above), or " +
                              "the values listed below can be altered and the XML file regenerated automatically. Note, the " +
                              "structure of the XML file can only be altered by the former means.");
        //jTextAreaMain.set

        Dimension dim = new Dimension(400,70);
        jTextAreaMain.setColumns(60);
        jTextAreaMain.setEditable(false);
        this.jTextAreaXPathString.setColumns(60);
        this.jTextAreaXPathString.setRows(2);
        jTextAreaXPathString.setEditable(false);


        jTextAreaMain.setPreferredSize(dim);
        jTextAreaMain.setMaximumSize(dim);
        jTextAreaMain.setWrapStyleWord(true);
        jTextAreaMain.setLineWrap(true);

        jTextAreaMain.setBackground((new Button()).getBackground());

        jPanelCMLFile.setLayout(borderLayout2);
        jPanelCMLSummary.setLayout(borderLayoutSummary2);

        jEditorPaneCMLFile.setBorder(border3);
        jEditorPaneCMLFile.setText(CML_CONTENTS_TAB);
        jScrollPaneCMLFile.setBorder(border2);

        jEditorPaneCMLSummary.setBorder(border3s);
        jEditorPaneCMLSummary.setText(SUMMARY_TAB);
        jScrollPaneCMLSummary.setBorder(border2s);

        jEditorPaneCMLFile.setEditable(false);
        jEditorPaneCMLSummary.setEditable(false);

        jTextAreaValue.setBorder(border4);

        jTextAreaValue.setText("...");

        this.jTextAreaXPathString.setBorder(border5);
        //this.jTextAreaXPathString.setEnabled(false);
        //Font oldFont = jTextAreaXPathString.getFont();

        //jTextAreaXPathString.setFont(Font.);

        jTextAreaValue.setColumns(60);
        jTextAreaValue.setRows(4);

        //jTextAreaValue

        jTextAreaValue.getDocument().addDocumentListener(new DocumentListener()
        {
            public void insertUpdate(DocumentEvent e)
            {
                jTextFieldValue_keyTyped(e);
            }

            public void removeUpdate(DocumentEvent e)
            {
                jTextFieldValue_keyTyped(e);
            }

            public void changedUpdate(DocumentEvent e)
            {
                jTextFieldValue_keyTyped(e);
            }
        });

        this.jListXPaths.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                jListXPath_listChanged(e);
            }
        });

        jPanelEdit.setLayout(borderLayout3);
        jPanelEditInt.setLayout(borderLayout44);

        jTabbedPane1.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                jTabbedPane1_stateChanged(e);
            }
        });

        jButtonSave.setText("Save");

        jButtonSave.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSave_actionPerformed(e);
            }
        });

        jButtonValidate.setText("Validate file");

        jButtonValidate.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonValidate_actionPerformed(e);
            }
        });



        jTextAreaXPathString.setText("--- XPath string ---");

        jPanelBot.setLayout(borderLayout4);

        this.getContentPane().add(jPanelMain, java.awt.BorderLayout.CENTER);
        jPanelMain.add(this.jLabelFileName, java.awt.BorderLayout.NORTH);
        jPanelMain.add(jTabbedPane1, java.awt.BorderLayout.CENTER);
        jPanelMain.add(jPanel3, java.awt.BorderLayout.SOUTH);

        jPanel3.add(jButtonValidate);

        jPanel3.add(jButtonPlot);
        //jPanel3.add(jButtonEditExt);
        jPanel3.add(jButtonSave);


        jPanel3.add(jButtonReload);
        jPanel3.add(jButtonCancel);

        this.getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

        jTabbedPane1.addTab(SUMMARY_TAB, null, jPanelCMLSummary, "Conversion of the XML file to a readable format using an XSL file");

        jPanelEdit.add(jPanelEditExt, java.awt.BorderLayout.NORTH);
        jPanelEdit.add(jPanelEditInt, java.awt.BorderLayout.CENTER);

        jPanelInput.add(jTextAreaValue);
        jPanelList.add(jScrollPaneList);

        jScrollPaneList.getViewport().add(jListXPaths);

        jTabbedPane1.addTab(CML_CONTENTS_TAB, null, jPanelCMLFile, "Contents of the XML file");
        jTabbedPane1.addTab(EDIT_TAB, null, jPanelEdit, "The parameters in the ChannelML file can be changed here, along with the settings file for loading it into neuroConstruct");

        jPanelCMLFile.add(jScrollPaneCMLFile, java.awt.BorderLayout.CENTER);
        jScrollPaneCMLFile.getViewport().add(jEditorPaneCMLFile);

        jPanelCMLSummary.add(jScrollPaneCMLSummary, java.awt.BorderLayout.CENTER);
        jScrollPaneCMLSummary.getViewport().add(jEditorPaneCMLSummary);


        jPanelTop.add(jTextAreaMain);
        jPanelEditExt.add(jButtonEditExt);
        jPanelEditExt.add(jButtonEditProps);
        jPanelMid.add(jPanelList);
        jPanel1.add(jTextAreaXPathString); jPanelBot.setMinimumSize(new Dimension(600, 300));
        jPanelBot.setPreferredSize(new Dimension(600, 300));

        jPanelEditInt.add(jPanelTop, java.awt.BorderLayout.NORTH);
        jPanelEditInt.add(jPanelMid, java.awt.BorderLayout.CENTER);
        jPanelEditInt.add(jPanelBot, java.awt.BorderLayout.SOUTH);
        jPanelBot.add(jPanelInput, java.awt.BorderLayout.CENTER);

        jPanelBot.add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanelCMLFile.setMinimumSize(new Dimension(600, 500));
        jPanelCMLFile.setPreferredSize(new Dimension(700, 600));

        jPanelCMLSummary.setMinimumSize(new Dimension(600, 500));
        jPanelCMLSummary.setPreferredSize(new Dimension(700, 600));

    }

    private File getDirForCMLFiles()
    {
        File dir = ProjectStructure.getCellMechanismDir(project.getProjectMainDirectory(), false);
        if (dir!=null && dir.exists())
        {
            return new File(ProjectStructure.getCellMechanismDir(project.getProjectMainDirectory()),
                            cmlMechanism.getInstanceName());
        }
        else
        {
            // old method...
            return new File(ProjectStructure.getCellProcessesDir(project.getProjectMainDirectory(), false),
                            cmlMechanism.getInstanceName());
        }
    }

    private void refresh()
    {
        //System.out.println("Refreshing the interface...");

        int selectedTab = jTabbedPane1.getSelectedIndex();
        String tab = jTabbedPane1.getTitleAt(selectedTab);

        File cmlFile = this.cmlMechanism.getChannelMLFile(project);
        File xslDoc = GeneralProperties.getChannelMLReadableXSL();



        try
        {
            refreshingInterface = true;
            jTabbedPane1.removeAll();

            jTabbedPane1.addTab(SUMMARY_TAB,
                                null,
                                jPanelCMLSummary,
                                "Summary of contents of ChannelML file converted to HTML from XML using: "
                +xslDoc.getAbsolutePath());

            jPanelEdit.add(jPanelEditExt, java.awt.BorderLayout.NORTH);
            jPanelEdit.add(jPanelEditInt, java.awt.BorderLayout.CENTER);

            jTabbedPane1.addTab(CML_CONTENTS_TAB, null, jPanelCMLFile, "Contents of ChannelML file: "+cmlFile.getAbsolutePath());

            jTabbedPane1.addTab(EDIT_TAB,
                                null,
                                jPanelEdit,
                                "The parameters in the ChannelML file can be changed here, along with the settings file for loading it into neuroConstruct");
            //jPanelEdit.setToolTipText();



            SimpleXMLDocument xmlDoc = cmlMechanism.getXMLDoc();

            String htmlString = xmlDoc.getXMLString("", true);

            jEditorPaneCMLFile.setContentType("text/html");

            /** @todo Look at better size.. */
            jEditorPaneCMLFile.setText("<body><span style=\"font-size:16\">"+htmlString+"</span></body>");

            jEditorPaneCMLSummary.setContentType("text/html");
            jEditorPaneCMLSummary.setText("<b>???</b>");

            listModelXPaths.removeAllElements();

            ArrayList<String> locs = xmlDoc.getXPathLocations(true);

            for (int i = 0; i < locs.size(); i++)
            {
                this.listModelXPaths.add(i,locs.get(i));
            }

            if (tab.equals(SUMMARY_TAB))  // only transform xml if the tab is shown
            {
                String readable = XMLUtils.transform(this.cmlMechanism.getXMLDoc().getXMLString("", false),xslDoc);


                //String readable = XMLUtils.transform(cmlFile,xslDoc);
                readable = GeneralUtils.replaceAllTokens(readable, "<br/>", "<br>");

                readable = GeneralUtils.replaceAllTokens(readable, "frame=\"box\" rules=\"all\"", "border = \"1\"");
                //System.out.println(readable);
                this.jEditorPaneCMLSummary.setText(readable);
            }

            ArrayList<SimXSLMapping> simMappings = cmlMechanism.getSimMappings();

            //System.out.println("Done p1...");

            for (int i = 0; i < simMappings.size(); i++)
            {
                SimXSLMapping nextMapping = simMappings.get(i);
                JPanel jPanelImplFile = new JPanel();

                jTabbedPane1.add(jPanelImplFile, nextMapping.getSimEnv() + " mapping");

                if (tab.equals(nextMapping.getSimEnv() + " mapping"))  // only load xml if the tab is shown
                {

                    File dirForFiles = this.getDirForCMLFiles();

                    File implFile = new File(dirForFiles, nextMapping.getXslFile());

                    JLabel jLabelName = new JLabel("File: " + implFile.getAbsolutePath());
                    jLabelName.setHorizontalAlignment(JLabel.CENTER);
                    jLabelName.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                    jPanelImplFile.setLayout(new BorderLayout());
                    JEditorPane jEditorPaneImplFile = new JEditorPane();

                    Border borderScrool = BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white, Color.white,
                                                                          new Color(103, 101, 98), new Color(148, 145, 140));
                    Border borderEditor = BorderFactory.createEmptyBorder(5, 5, 5, 5);

                    jEditorPaneImplFile.setBorder(borderEditor);
                    JScrollPane jScrollPaneImplFile = new JScrollPane();
                    jScrollPaneImplFile.setBorder(borderScrool);

                    jEditorPaneImplFile.setEditable(false);

                    jPanelImplFile.add(jScrollPaneImplFile, java.awt.BorderLayout.CENTER);
                    jPanelImplFile.add(jLabelName, java.awt.BorderLayout.NORTH);

                    jScrollPaneImplFile.getViewport().add(jEditorPaneImplFile);

                    FileInputStream instream = null;
                    InputSource is = null;
                    try
                    {
                        SAXParserFactory spf = SAXParserFactory.newInstance();
                        spf.setNamespaceAware(true);
                        XMLReader xmlReader = spf.newSAXParser().getXMLReader();

                        SimpleXMLReader docBuilder = new SimpleXMLReader();
                        xmlReader.setContentHandler(docBuilder);

                        instream = new FileInputStream(implFile);

                        is = new InputSource(instream);

                        xmlReader.parse(is);

                        xmlDoc = docBuilder.getDocRead();
                        jEditorPaneImplFile.setContentType("text/html");

                        jEditorPaneImplFile.setText(xmlDoc.getXMLString("", true));

                        jEditorPaneImplFile.setCaretPosition(0);

                    }
                    catch (ParserConfigurationException e)
                    {
                        GuiUtils.showErrorMessage(logger, "Error when parsing XML file: " + implFile, e, this);
                    }
                    catch (SAXException e)
                    {
                        GuiUtils.showErrorMessage(logger, "Error when parsing XML file: " + implFile, e, this);
                    }

                    //jEditorPaneImplFile.setPage(implFile.toURL());

                    jPanelImplFile.setMinimumSize(new Dimension(700, 600));
                    jPanelImplFile.setPreferredSize(new Dimension(700, 600));

                    JButton jButtonPreview = new JButton("Preview " + nextMapping.getSimEnv() + " script file");

                    //Str

                    jButtonPreview.addActionListener(new PreviewListener(nextMapping.getSimEnv()));

                    JPanel jPanelButtons = new JPanel();

                    jPanelButtons.add(jButtonPreview);
                    jPanelImplFile.add(jPanelButtons, java.awt.BorderLayout.SOUTH);
                }
            }

        }
        catch (ChannelMLException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem displaying the ChannelML source", ex, this);
        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem displaying one of the ChannelML files", ex, this);
        }


        jTabbedPane1.setSelectedIndex(selectedTab);

        this.jEditorPaneCMLFile.setCaretPosition(0);
        this.jEditorPaneCMLSummary.setCaretPosition(0);



        //System.out.println("Done refreshing the interface...");
        refreshingInterface = false;
    }

    private class PreviewListener implements ActionListener
    {
        String simEnv = null;

        public PreviewListener(String simEnv)
        {
            this.simEnv = simEnv;
        }


        public void actionPerformed(ActionEvent e)
        {
            previewNativeScript(simEnv);
        }
    }



    private void previewNativeScript(String simEnv)
    {
        logger.logComment("Looking for native env mapping: "+ simEnv);
        ArrayList<SimXSLMapping> simMappings = cmlMechanism.getSimMappings();

        for (int i = 0; i < simMappings.size(); i++)
        {
            if (simMappings.get(i).getSimEnv().equals(simEnv))
            {
                try
                {
                    File dirForFiles = this.getDirForCMLFiles();

                    SimpleXMLDocument xmlDoc = cmlMechanism.getXMLDoc();

                    String xmlString = xmlDoc.getXMLString("", false);

                    File xslFile = new File(dirForFiles, simMappings.get(i).getXslFile());

                    String transformed = XMLUtils.transform(xmlString, xslFile);

                    SimpleViewer.showString(transformed, "Preview of " + simEnv + " script file", 12, false, false);
                }
                catch (ChannelMLException ex)
                {
                    GuiUtils.showErrorMessage(logger, "Error transforming XML content", ex, this);
                }

            }
        }

    }



    public static void main(String[] args)
    {
        String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();
        try
        {
            UIManager.setLookAndFeel(favouredLookAndFeel);
            ProjectEventListener pel = new ProjectEventListener()
            {
                public void tableDataModelUpdated(String tableModelName)
                {};

                public void tabUpdated(String tabName)
                {};
                public void cellMechanismUpdated()
                {
                };

            };


            Project testProj = Project.loadProject(new File("examples\\Ex4-NEURONGENESIS\\Ex4-NEURONGENESIS.neuro.xml"),
                                                   pel);

            //ChannelMLCellMechanism cmlMechanism =(ChannelMLCellMechanism)testProj.cellMechanismInfo.getCellMechanism("NaConductance_CML");
            ChannelMLCellMechanism cmlMechanism =(ChannelMLCellMechanism)testProj.cellMechanismInfo.getCellMechanism("DoubExpSyn");

            ChannelMLEditor frame = new ChannelMLEditor(cmlMechanism, testProj, pel);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            Dimension frameSize = frame.getSize();
            if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
            if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;
            frame.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);


            frame.setVisible(true);

            //System.out.println("Shown the dialog");


        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void jButtonClose_actionPerformed(ActionEvent e)
    {
        logger.logComment("Closing without saving...");
        this.dispose();
    }

    public void jButtonReload_actionPerformed(ActionEvent e)
    {
        logger.logComment("Reloading file...");
        //this.dispose();

        try
        {
            cmlMechanism.reset(project, false);
        }
        catch (ChannelMLException ex1)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Error initialising Cell Mechanism: " +cmlMechanism.getInstanceName(),
                                      ex1,
                                      null);

        }

        this.refresh();

        if (eventIf!=null)
        eventIf.cellMechanismUpdated();


    }

    public void jButtonEditProps_actionPerformed(ActionEvent e)
    {
        logger.logComment("Editing props");

        File cmlFile = this.cmlMechanism.getChannelMLFile(project);

        File propsFile = new File(cmlFile.getParentFile(), CellMechanismHelper.PROPERTIES_FILENAME);

        SimpleTextInput simpText = SimpleTextInput.showFile(propsFile.getAbsolutePath(), 12, false, false, 0.7f, 0.7f, this);

        if (simpText.isChanged())
        {
            try
            {
                this.cmlMechanism.initPropsFromPropsFile(propsFile);
            }
            catch (IOException ex)
            {
                GuiUtils.showErrorMessage(logger, "Error initialising that Cell Mechanism with properties file: "
                                          + propsFile.getAbsolutePath(), ex, this);

            }

            if (eventIf != null)
                eventIf.cellMechanismUpdated();
        }
    }


    public void jButtonEditExt_actionPerformed(ActionEvent e)
    {
        logger.logComment("Editing externally");
        String editorPath = GeneralProperties.getEditorPath(true);

        Runtime rt = Runtime.getRuntime();

        File cmlFile = this.cmlMechanism.getChannelMLFile(project);

        String command = editorPath + " " + cmlFile.getAbsolutePath()+"";

        if (GeneralUtils.isWindowsBasedPlatform() && cmlFile.getAbsolutePath().indexOf(" " )>=0)
        {
            command = editorPath + " \"" + cmlFile.getAbsolutePath()+"\"";
        }


        logger.logComment("Going to execute command: " + command);

        try
        {
            GuiUtils.showInfoMessage(logger, "Confirm","Please note that any changes made in an external application to the file will not be\n"+
                                                        "registered here until you press <b>Reload ChannelML file</b>", this);

            Process currentProcess = rt.exec(command);
        }
        catch (IOException ex)
        {
            logger.logError("Error running "+command);
        }

        logger.logComment("Have successfully executed command: " + command);

    }

    /**
     * Preliminary rate equation plotter...
     */
    public void jButtonPlot_actionPerformed(ActionEvent e)
    {
        logger.logComment("Plotting rate equations...");

        this.refresh();

        float minV = -100;
        float maxV = 60;

        float minT = 0;
        float maxT = 20;


        Variable v = new Variable("v");
        Variable temp = new Variable("celsius");

        float numPoints = 161;

        if (cmlMechanism.isChannelMechanism())
        {
            if (cmlMechanism.getUnitsUsedInFile().equals(ChannelMLConstants.SI_UNITS))
            {
                minV = -0.1f;
                maxV = 0.1f;
            };

            try
            {
                SimpleXMLEntity[] gates = cmlMechanism.getXMLDoc().getXMLEntities(ChannelMLConstants.getHHGateXPath());

                if (gates.length==0)
                {
                    logger.logComment("Assuming leak conductance");
                    SimpleXMLEntity[] ions = cmlMechanism.getXMLDoc().getXMLEntities(ChannelMLConstants.getIonsXPath());
                    SimpleXMLElement firstIon = (SimpleXMLElement) ions[0];
                    String revPotString = firstIon.getAttributeValue(ChannelMLConstants.ION_REVERSAL_POTENTIAL_ATTR);
                    float revPot = Float.parseFloat(revPotString);
                    SimpleXMLEntity[] conductances = cmlMechanism.getXMLDoc().getXMLEntities(ChannelMLConstants.getConductanceXPath());


                    SimpleXMLElement firstCond = (SimpleXMLElement) conductances[0];
                    String gmaxString = firstCond.getAttributeValue(ChannelMLConstants.DEFAULT_COND_DENSITY_ATTR);
                    float gmax = Float.parseFloat(gmaxString);

                    String expression = gmax +" * (v - ("+revPot+"))";
                    EquationUnit func = Expression.parseExpression(expression, new Variable[]{v});


                    String graphRef = "Plot of leak current due to Cell Mechanism: " + cmlMechanism.getInstanceName();

                    String dsRef = graphRef;

                    String desc = dsRef;

                    desc = desc + "\n\n Expression for graph: " + expression;
                    desc = desc + "\nwhich has been parsed as: " + func.getNiceString();

                    DataSet ds = new DataSet(dsRef, desc, "", "", "", "");

                    for (int i = 0; i < numPoints; i++)
                    {
                        float nextVval = minV + ( (maxV - minV) * i / (numPoints));

                        Argument[] a0 = new Argument[]
                            {new Argument(v.getName(), nextVval)};

                        ds.addPoint(nextVval, func.evaluateAt(a0));
                    }

                    PlotterFrame pf = PlotManager.getPlotterFrame(graphRef);

                    pf.addDataSet(ds);

                }
                for (int gateIndex = 0; gateIndex < gates.length; gateIndex++)
                {
                    if (gates[gateIndex] instanceof SimpleXMLElement)
                    {
                        SimpleXMLElement gate = (SimpleXMLElement)gates[gateIndex];

                        String gateState = gate.getAttributeValue(ChannelMLConstants.HH_GATE_STATE_ATTR);

                        System.out.println("Found gate with state: " + gateState);

                        SimpleXMLEntity[] voltageGateElements = gate.getXMLEntities(ChannelMLConstants.TRANSITION_ELEMENT + "/"
                            + ChannelMLConstants.VOLTAGE_GATE_ELEMENT);

                        for (int vgIndex = 0; vgIndex < voltageGateElements.length; vgIndex++)
                        {
                            if (voltageGateElements[vgIndex] instanceof SimpleXMLElement)
                            {
                                Hashtable<String, DataSet> rateData = new Hashtable<String,DataSet>();

                                SimpleXMLElement vg = (SimpleXMLElement)voltageGateElements[vgIndex];

                                ArrayList<SimpleXMLEntity> rates = vg.getContents();

                                for (SimpleXMLEntity child: rates)
                                {

                                    if (child instanceof SimpleXMLElement)
                                    {
                                        SimpleXMLElement rate = (SimpleXMLElement)child;

                                        System.out.println("Found: " + rate);


                                        String expression = null;

                                        EquationUnit func = null;


                                        SimpleXMLEntity[] paramHHPlots = rate.getXMLEntities(ChannelMLConstants.PARAMETERISED_HH_ELEMENT);


                                        for (int paramPlotIndex = 0; paramPlotIndex < paramHHPlots.length; paramPlotIndex++)
                                        {
                                            System.out.println(" - - Found: "+ paramHHPlots[paramPlotIndex]);

                                            if (paramHHPlots[paramPlotIndex] instanceof SimpleXMLElement)
                                            {
                                                SimpleXMLElement paramPlot = (SimpleXMLElement)paramHHPlots[paramPlotIndex];

                                               String eqnType = paramPlot.getAttributeValue(ChannelMLConstants.PARAMETERISED_HH_TYPE_ATTR);

                                                float A = Float.NaN;
                                                float k = Float.NaN;
                                                float d = Float.NaN;

                                                try
                                                {
                                                    SimpleXMLEntity[] param = paramPlot.getXMLEntities(ChannelMLConstants.PARAMETER_ELEMENT);

                                                    for (int paramIndex = 0; paramIndex < param.length; paramIndex++)
                                                    {
                                                        if (param[paramIndex] instanceof SimpleXMLElement)
                                                        {
                                                            SimpleXMLElement oneParam = (SimpleXMLElement)param[paramIndex];

                                                            if (oneParam.getAttributeValue(ChannelMLConstants.PARAMETER_NAME_ATTR)!=null)
                                                            {
                                                                if (oneParam.getAttributeValue(ChannelMLConstants.PARAMETER_NAME_ATTR).equals("A"))
                                                                {
                                                                    A = Float.parseFloat(oneParam.getAttributeValue(ChannelMLConstants.PARAMETER_VALUE_ATTR));
                                                                }
                                                                else if (oneParam.getAttributeValue(ChannelMLConstants.PARAMETER_NAME_ATTR).equals("k"))
                                                                {
                                                                    k = Float.parseFloat(oneParam.getAttributeValue(ChannelMLConstants.PARAMETER_VALUE_ATTR));
                                                                }
                                                                else if (oneParam.getAttributeValue(ChannelMLConstants.PARAMETER_NAME_ATTR).equals("d"))
                                                                {
                                                                    d = Float.parseFloat(oneParam.getAttributeValue(ChannelMLConstants.PARAMETER_VALUE_ATTR));
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                catch (NumberFormatException ex)
                                                {
                                                    GuiUtils.showErrorMessage(logger, "Error getting parameter for rate: "+ rate.getName()+" in state "
                                                    + gateState+" in Cell Mechanism "+ cmlMechanism.getInstanceName(), ex, this);
                                                }

                                                if (eqnType.equals(ChannelMLConstants.LINOID_TYPE))
                                                {
                                                    expression = A + " *( "+k+" * (v - "+d+")) / (1 - exp(-1 * ("+k+" * (v - "+d+"))))";
                                                }
                                                else if (eqnType.equals(ChannelMLConstants.SIGMOID_TYPE))
                                                {
                                                    expression = A + " /(1 + exp("+k+" *(v - "+d+")))";
                                                }
                                                else if (eqnType.equals(ChannelMLConstants.EXPONENTIAL_TYPE))
                                                {
                                                    expression = A +" * exp("+k+" *(v - "+d+"))";
                                                }



                                                func = Expression.parseExpression(expression, new Variable[]{v});


                                            }
                                        }

                                        //SimpleXMLEntity[] genericPlots = rate.getXMLEntities(ChannelMLConstants.PARAMETERISED_HH_ELEMENT);
                                        SimpleXMLEntity[] generic = rate.getXMLEntities(ChannelMLConstants.GENERIC_HH_ELEMENT);


                                        for (int genericIndex = 0; genericIndex < generic.length; genericIndex++)
                                        {
                                            System.out.println(" - - Found: " + generic[genericIndex]);

                                            if (generic[genericIndex] instanceof SimpleXMLElement)
                                            {
                                                SimpleXMLElement genericPlot = (SimpleXMLElement) generic[genericIndex];

                                                String expr = genericPlot.getAttributeValue(ChannelMLConstants.GENERIC_HH_EXPR_ATTR);
                                                System.out.println(" - - Found expr: " + expr);

                                                func = Expression.parseExpression(expr, new Variable[]{v, temp});
                                            }
                                        }

                                        if (func!=null)
                                        {

                                            String graphRef = "Plots for state "
                                                + gateState + " in Cell Mechanism " + cmlMechanism.getInstanceName();

                                            String dsRef = "Plot of rate: " + rate.getName() + " in state "
                                                + gateState + " in Cell Mechanism " + cmlMechanism.getInstanceName();

                                            String desc = dsRef;

                                            desc = desc + "\n\n Expression for graph: " + expression;
                                            desc = desc + "\nwhich has been parsed as: " + func.getNiceString();

                                            DataSet ds = new DataSet(dsRef, desc, "mV", "", "Membrane Potential", gateState);



                                            for (int i = 0; i < numPoints; i++)
                                            {
                                                float nextVval = minV + ( (maxV - minV) * i / (numPoints));

                                                Argument[] a0 = new Argument[]
                                                    {new Argument(v.getName(), nextVval),
                                                    new Argument(temp.getName(), project.simulationParameters.getTemperature())};

                                                ds.addPoint(nextVval, func.evaluateAt(a0));
                                            }

                                            PlotterFrame pf = PlotManager.getPlotterFrame(graphRef);

                                            pf.addDataSet(ds);

                                            rateData.put(rate.getName(), ds);
                                        }
                                    }

                                }

                                if (rateData.containsKey("alpha") && rateData.containsKey("beta"))
                                {
                                    DataSet dsAlpha = rateData.get("alpha");
                                    DataSet dsBeta = rateData.get("beta");

                                    String graphRef = "Plots for tau/inf in state "
                                        + gateState+" in Cell Mechanism "+ cmlMechanism.getInstanceName();



                                    if (!rateData.containsKey("tau"))
                                    {
                                        String dsRef = "Plot of tau (1/(alpha+beta)) in state "
                                        + gateState+" in Cell Mechanism "+ cmlMechanism.getInstanceName();

                                        DataSet tau = new DataSet(dsRef, dsRef, "mV", "ms", "Membrane Potential", "tau");

                                        for (int i = 0; i < numPoints; i++)
                                        {
                                            float nextVval = minV + ( (maxV - minV) * i / (numPoints));


                                            tau.addPoint(nextVval, 1 / (dsAlpha.getYValues()[i] + dsBeta.getYValues()[i]));
                                        }

                                        PlotterFrame pf = PlotManager.getPlotterFrame(graphRef);

                                        pf.addDataSet(tau);
                                    }
                                    if (!rateData.containsKey("inf"))
                                    {
                                        String dsRef = "Plot of inf (alpha/(alpha+beta)) in state "
                                        + gateState+" in Cell Mechanism "+ cmlMechanism.getInstanceName();

                                        DataSet inf = new DataSet(dsRef, dsRef, "mV", "","Membrane Potential", "inf");

                                        for (int i = 0; i < numPoints; i++)
                                        {
                                            float nextVval = minV + ( (maxV - minV) * i / (numPoints));

                                            inf.addPoint(nextVval, dsAlpha.getYValues()[i] / (dsAlpha.getYValues()[i] + dsBeta.getYValues()[i]));
                                        }

                                        PlotterFrame pf = PlotManager.getPlotterFrame(graphRef);

                                        pf.addDataSet(inf);
                                    }



                                }

                            }
                        }
                    }
                }
            }
            catch (ChannelMLException ex)
            {
                GuiUtils.showErrorMessage(logger, "Error calculating plots for ChannelML process: "+ cmlMechanism, ex, this);
            }
            catch (EquationException ex)
            {
                GuiUtils.showErrorMessage(logger, "Error calculating plots for ChannelML process: "+ cmlMechanism, ex, this);
            }


        }
        else if (cmlMechanism.isSynapticMechanism())
        {
            try
            {
                SimpleXMLEntity[] synTypes = cmlMechanism.getXMLDoc().getXMLEntities(ChannelMLConstants.getSynapseTypeXPath());

                for (int synTypeIndex = 0; synTypeIndex < synTypes.length; synTypeIndex++)
                {
                    logger.logComment("Found a synapse type...");
                    if (synTypes[synTypeIndex] instanceof SimpleXMLElement)
                    {
                        SimpleXMLElement synType = (SimpleXMLElement)synTypes[synTypeIndex];
                        SimpleXMLEntity[] doubExpSyns = synType.getXMLEntities(ChannelMLConstants.DOUB_EXP_SYN_ELEMENT);

                        // ..
                        for (int doubSynIndex = 0; doubSynIndex < doubExpSyns.length; doubSynIndex++)
                        {
                            logger.logComment("Found a doub exp syn type...");
                            if (doubExpSyns[doubSynIndex] instanceof SimpleXMLElement)
                            {
                                SimpleXMLElement des = (SimpleXMLElement)doubExpSyns[doubSynIndex];
                                float maxCond = Float.parseFloat(des.getAttributeValue(ChannelMLConstants.DES_MAX_COND_ATTR));
                                float taur = Float.parseFloat(des.getAttributeValue(ChannelMLConstants.DES_RISE_TIME_ATTR));
                                float taud = Float.parseFloat(des.getAttributeValue(ChannelMLConstants.DES_DECAY_TIME_ATTR));

                                double tp = ( (taur * taud) / (taud - taur)) * Math.log(taud / taur);
                                double factor = 1 / (Math.exp( -tp / taud) - Math.exp( -tp / taur));

                                float startTime = 0;
                                float endTime = taud*5;

                                Variable t = new Variable("t");

                                 String expression = maxCond + " * "+factor + " * (exp(-1* t / "+taud+") - exp(-1* t / "+taur+"))";

                                 EquationUnit func = Expression.parseExpression(expression, new Variable[]{t});


                                String graphRef = "Plots for time course of Synaptic Mechanism " + cmlMechanism.getInstanceName();

                                String desc = graphRef;

                                desc = desc + "\n\n Expression for graph: " + expression;
                                desc = desc + "\nwhich has been parsed as: " + func.getNiceString();

                                DataSet ds = new DataSet(graphRef, desc, "ms", "mS/\u03bcm\u00b2", "Time", "Conductance Density");

                                for (int i = 0; i < numPoints; i++)
                                {
                                    float nextTval = startTime + ( (endTime - startTime) * i / (numPoints));

                                    Argument[] a0 = new Argument[]
                                        {new Argument(t.getName(), nextTval)};

                                    ds.addPoint(nextTval, func.evaluateAt(a0));
                                }

                                PlotterFrame pf = PlotManager.getPlotterFrame(graphRef);

                                pf.addDataSet(ds);



                            }

                        }
                    }
                }

            }
            catch (ChannelMLException ex)
            {
                GuiUtils.showErrorMessage(logger, "Error calculating plots for ChannelML process: " + cmlMechanism, ex, this);
            }
            catch (EquationException ex)
            {
                GuiUtils.showErrorMessage(logger, "Error calculating plots for ChannelML process: "+ cmlMechanism, ex, this);
            }


        }
        else
        {
            GuiUtils.showErrorMessage(logger, "Unfortunately there is no information on what would be interesting to plot for"+
                                      " this type of ChannelML file at present.", null, this);

            return;

        }

    }

    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        logger.logComment("Hyperlink!!");

        if (e.getEventType() != null
            && e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
        {

            String url = e.getURL().toExternalForm();
            logger.logComment("Loading: " + url);

            String browserPath = GeneralProperties.getBrowserPath(true);
            Runtime rt = Runtime.getRuntime();

            String command = browserPath + " \"" + url+"\"";
            if (GeneralUtils.isLinuxBasedPlatform())
            {
                command = browserPath + " " + url;
            }

            logger.logComment("Going to execute command: " + command);

            try
            {
                Process currentProcess = rt.exec(command);

                logger.logComment("Have successfully executed command: " + command);

            }
            catch (IOException ex)
            {
                logger.logError("Error running " + command);
            }

        }

    }


    public void jListXPath_listChanged(ListSelectionEvent e)
    {
        logger.logComment("ItemEvent "+e);

        if (e.getValueIsAdjusting())
        {
            String selected = (String)this.jListXPaths.getSelectedValue();

            logger.logComment("Selected item: " + selected);

            try
            {
                SimpleXMLDocument xmlDoc = cmlMechanism.getXMLDoc();

                this.jTextAreaValue.setText(xmlDoc.getValueByXPath(selected));

                this.jTextAreaXPathString.setText(selected);
            }
            catch (ChannelMLException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem parsing the ChannelML file", ex, this);
            }
        }
    }

    public void jTextFieldValue_keyTyped(DocumentEvent e)
    {
        logger.logComment("Doc event: "+ e);

        String text = jTextAreaValue.getText();
        String selected = (String)jListXPaths.getSelectedValue();

        try
        {
            SimpleXMLDocument xmlDoc = cmlMechanism.getXMLDoc();

            ;

            logger.logComment("Setting: "+ selected+" to: "+ text+", result: "+xmlDoc.setValueByXPath(selected, text));
        }
        catch (ChannelMLException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem parsing the ChannelML file", ex, this);
        }

    }

    public void jTabbedPane1_stateChanged(ChangeEvent e)
    {
        logger.logComment("Event registered: "+e);

        if (! refreshingInterface) refresh();
    }

    public void jButtonSave_actionPerformed(ActionEvent e)
    {
        logger.logComment("Closing with saving...");
        try
        {
            this.cmlMechanism.saveCurrentState(project);
            this.dispose();
        }
        catch (ChannelMLException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem saving the ChannelML file", ex, this);
        }
    }


    public void jButtonValidate_actionPerformed(ActionEvent e)
    {
        logger.logComment("Validating...");

        File schemaFile = GeneralProperties.getChannelMLSchemaFile();
        try
        {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            logger.logComment("Found the XSD file: " + schemaFile.getAbsolutePath());

            Source schemaFileSource = new StreamSource(schemaFile);
            Schema schema = factory.newSchema(schemaFileSource);

            Validator validator = schema.newValidator();

            Source xmlFileSource = new StreamSource(getMyXMLReader());

            validator.validate(xmlFileSource);

        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem validating the ChannelML file. Note that the file was validated\n"
                                      + "against the version of the ChannelML schema (v"+GeneralProperties.getNeuroMLVersionNumber()+") included with this distribution\n"
                                      + "of neuroConstruct. To validate it against current and past schema see:\n"
                                      + GeneralProperties.getWebsiteNMLValidator(), ex, this);

            return;
        }

        GuiUtils.showInfoMessage(logger, "Success", "ChannelML file is well formed and valid, according to schema:\n"
                                 + schemaFile, this);

    }

    private Reader getMyXMLReader() throws IOException, ChannelMLException
    {
        Reader in = null;

        in = new StringReader(this.cmlMechanism.getXMLDoc().getXMLString("", false));

        return in;
    }



}
