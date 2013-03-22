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
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package ucl.physiol.neuroconstruct.gui;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
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
 *  
 */

@SuppressWarnings("serial")

public class ChannelMLEditor extends JFrame implements HyperlinkListener
{
    private static ClassLogger logger = new ClassLogger("ChannelMLEditor");

    Project project = null;

    XMLCellMechanism xmlMechanism = null;

    private final static String CHANNELML = "ChannelML";
    private final static String SBML = "SBML";

    private String xmlDialect = CHANNELML;

    private final String SUMMARY_TAB = "Summary of file contents";
    private final String EDIT_TAB = "Edit";
    private String CML_CONTENTS_TAB = "XML file";


    public static final String CELSIUS_PARAMETER = "celsius";
    public static final String TEMP_ADJ_PARAMETER = "temp_adj_";

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
    JButton jButtonPlot = new JButton("Generate associated plots");
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

    public ChannelMLEditor(XMLCellMechanism cmlMechanism, Project project, ProjectEventListener eventIf)
    {
        super();

        this.project = project;
        this.eventIf = eventIf;
        this.xmlMechanism = cmlMechanism;

        if (xmlMechanism instanceof ChannelMLCellMechanism)
            xmlDialect = CHANNELML;
        else if (xmlMechanism instanceof SBMLCellMechanism)
            xmlDialect = SBML;


        CML_CONTENTS_TAB = xmlDialect+" file";

        try
        {
            File cmlFileUsed = cmlMechanism.initialise(project, false);  // in case..

            refreshingInterface = true;
            jbInit();
            refreshingInterface = false;

            this.jLabelFileName.setText("Editing "+xmlDialect+" file: " + cmlFileUsed.getAbsolutePath());

            jLabelFileName.setHorizontalAlignment(JLabel.CENTER);
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

    private boolean isSBMLMechanism()
    {
        return xmlDialect.equals(SBML);
    }

    private void jbInit() throws Exception
    {
        this.setTitle("Editing "+xmlDialect+" Cell Mechanism: "+ this.xmlMechanism.getInstanceName());


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
        jButtonValidate.setToolTipText("Validate the current "+xmlDialect+" file with the current specifications:\n"+GeneralProperties.getChannelMLSchemaFile()+"");

        jButtonSave.setToolTipText("Saves any changes to the values in the Cell Mechanism **made through this interface** in an updated XML file");


        jButtonReload.setToolTipText("Reload the "+xmlDialect+" file (if it has been externally modified) and update information above");

        jButtonCancel.setToolTipText("Close this window without saving any changes");

        jTextAreaMain.setText("  ***  Note: below is an experimental interface for altering the parameters in the "+xmlDialect+" file  ***\n"
                              +"The "+xmlDialect+" file can either be edited by an external text editor (recommended, above), or " +
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
        jTabbedPane1.addTab(EDIT_TAB, null, jPanelEdit, "The parameters in the "+xmlDialect+" file can be changed here, along with the settings file for loading it into neuroConstruct");

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
                            xmlMechanism.getInstanceName());
        }
        else
        {
            // old method...
            return new File(ProjectStructure.getCellProcessesDir(project.getProjectMainDirectory(), false),
                            xmlMechanism.getInstanceName());
        }
    }

    private void refresh()
    {
        //System.out.println("Refreshing the interface...");

        int selectedTab = jTabbedPane1.getSelectedIndex();
        String tab = jTabbedPane1.getTitleAt(selectedTab);

        File cmlFile = this.xmlMechanism.getXMLFile(project);
        File xslDoc = GeneralProperties.getChannelMLReadableXSL();

        if (this.isSBMLMechanism())
        {
            xslDoc = GeneralProperties.getSBMLReadableXSL();
        }


        try
        {
            refreshingInterface = true;
            jTabbedPane1.removeAll();

            jTabbedPane1.addTab(SUMMARY_TAB,
                                null,
                                jPanelCMLSummary,
                                "Summary of contents of "+xmlDialect+" file converted to HTML from XML using: "
                +xslDoc.getAbsolutePath());

            jPanelEdit.add(jPanelEditExt, java.awt.BorderLayout.NORTH);
            jPanelEdit.add(jPanelEditInt, java.awt.BorderLayout.CENTER);

            jTabbedPane1.addTab(CML_CONTENTS_TAB, null, jPanelCMLFile, "Contents of "+xmlDialect+" file: "+cmlFile.getAbsolutePath());

            jTabbedPane1.addTab(EDIT_TAB,
                                null,
                                jPanelEdit,
                                "The parameters in the "+xmlDialect+" file can be changed here, along with the settings file for loading it into neuroConstruct");
            //jPanelEdit.setToolTipText();



            SimpleXMLDocument xmlDoc = xmlMechanism.getXMLDoc();

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
                String readable = XMLUtils.transform(this.xmlMechanism.getXMLDoc().getXMLString("", false),xslDoc);


                //String readable = XMLUtils.transform(cmlFile,xslDoc);
                readable = GeneralUtils.replaceAllTokens(readable, "<br/>", "<br>");

                readable = GeneralUtils.replaceAllTokens(readable, "frame=\"box\" rules=\"all\"", "border = \"1\"");
                //System.out.println(readable);
                this.jEditorPaneCMLSummary.setText(readable);
            }

            ArrayList<SimulatorMapping> simMappings = xmlMechanism.getSimMappings();

            //System.out.println("Done p1...");

            for (int i = 0; i < simMappings.size(); i++)
            {
                SimulatorMapping nextMapping = simMappings.get(i);
                JPanel jPanelImplFile = new JPanel();

                jTabbedPane1.add(jPanelImplFile, nextMapping.getSimEnv() + " mapping");

                if (tab.equals(nextMapping.getSimEnv() + " mapping"))  // only load xml if the tab is shown
                {

                    File dirForFiles = this.getDirForCMLFiles();

                    File implFile = new File(dirForFiles, nextMapping.getMappingFile());

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
                        if (!this.isSBMLMechanism())
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
                        }
                        else
                        {
                            String contents = GeneralUtils.readShortFile(implFile);
                            jEditorPaneImplFile.setText(contents);
                        }

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
        catch (XMLMechanismException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem displaying the "+xmlDialect+" source", ex, this);
        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem displaying one of the "+xmlDialect+" files", ex, this);
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
        ArrayList<SimulatorMapping> simMappings = xmlMechanism.getSimMappings();

        for (int i = 0; i < simMappings.size(); i++)
        {
            if (simMappings.get(i).getSimEnv().equals(simEnv))
            {
                try
                {
                    File dirForFiles = this.getDirForCMLFiles();

                    SimpleXMLDocument xmlDoc = xmlMechanism.getXMLDoc();

                    String xmlString = xmlDoc.getXMLString("", false);

                    File xslFile = new File(dirForFiles, simMappings.get(i).getMappingFile());

                    String transformed = XMLUtils.transform(xmlString, xslFile);

                    SimpleViewer.showString(transformed, "Preview of " + simEnv + " script file", 12, false, false);
                }
                catch (XMLMechanismException ex)
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


            //Project testProj = Project.loadProject(new File("C:/fullCheckout/tempModels/neuroConstruct/Ex7_GranuleCell/Ex7_GranuleCell.neuro.xml"),pel);
            //Project testProj = Project.loadProject(new File("C:\\copynCmodels\\TraubEtAl2005\\TraubEtAl2005.neuro.xml"),pel);
            Project testProj = Project.loadProject(new File("nCmodels/CA1PyramidalCell/CA1PyramidalCell.ncx"),pel);

            //ChannelMLCellMechanism xmlMechanism =(ChannelMLCellMechanism)testProj.cellMechanismInfo.getCellMechanism("NaConductance_CML");
            //ChannelMLCellMechanism xmlMechanism =(ChannelMLCellMechanism)testProj.cellMechanismInfo.getCellMechanism("Gran_CaHVA_98");
            //ChannelMLCellMechanism xmlMechanism =(ChannelMLCellMechanism)testProj.cellMechanismInfo.getCellMechanism("kc_fast");
            ChannelMLCellMechanism cmlMechanism =(ChannelMLCellMechanism)testProj.cellMechanismInfo.getCellMechanism("na3");
            
            ChannelMLEditor.logger.setThisClassVerbose(false);

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
            xmlMechanism.reset(project, false);
        }
        catch (XMLMechanismException ex1)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Error initialising Cell Mechanism: " +xmlMechanism.getInstanceName(),
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

        File cmlFile = this.xmlMechanism.getXMLFile(project);

        File propsFile = new File(cmlFile.getParentFile(), CellMechanismHelper.PROPERTIES_FILENAME);

        SimpleTextInput simpText = SimpleTextInput.showFile(propsFile.getAbsolutePath(), 12, false, false, 0.7f, 0.7f, this);

        if (simpText.isChanged())
        {
            try
            {
                this.xmlMechanism.initPropsFromPropsFile(propsFile);
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

        File cmlFile = this.xmlMechanism.getXMLFile(project);

        String command = editorPath + " " + cmlFile.getAbsolutePath()+"";

        if (GeneralUtils.isWindowsBasedPlatform() && cmlFile.getAbsolutePath().indexOf(" " )>=0)
        {
            command = editorPath + " \"" + cmlFile.getAbsolutePath()+"\"";
        }


        logger.logComment("Going to execute command: " + command);

        try
        {
            GuiUtils.showInfoMessage(logger, "Confirm","Please note that any changes made in an external application to the file will not be\n"+
                                                        "registered here until you press <b>Reload "+xmlDialect+" file</b>", this);

            rt.exec(command);
        }
        catch (IOException ex)
        {
            logger.logError("Error running "+command);
        }

        logger.logComment("Have successfully executed command: " + command);

    }

    private float checkTempInExpression(float prefTemp, String expression)
    {
        if ((expression.indexOf(CELSIUS_PARAMETER)>=0) && Float.isNaN(prefTemp))
        {
            float defaultTemp = project.simulationParameters.getTemperature();
            String res = JOptionPane.showInputDialog(this, "Please enter the preferred temperature at which to plot the expressions", defaultTemp);
            if (res == null)
                return defaultTemp;
            try
            {
                return Float.parseFloat(res);
            }
            catch (NumberFormatException ex)
            {
                return defaultTemp;
            }
        }
        else
        {
            logger.logComment("No temperature dependence in expression");
        }
        return prefTemp;
    }

    /**
     * Rate equation plotter. Not all cases covered yet...
     */
    public void jButtonPlot_actionPerformed(ActionEvent e)
    {
        logger.logComment("Plotting rate equations...");

        this.refresh();

        float minV = -100;
        float maxV = 80;

        String voltUnits = "mV";
        String timeUnits = "ms";

        Variable v = new Variable("v");
        Variable temp = new Variable(CELSIUS_PARAMETER);
        
        Variable[] mainVars = new Variable[]{v, temp};

        float numPoints = maxV - minV +1;

        float prefTemperature = Float.NaN;

        if (xmlMechanism.isChannelMechanism())
        {
            ChannelMLCellMechanism cmlMech = (ChannelMLCellMechanism)xmlMechanism;

            if (cmlMech.getUnitsUsedInFile().equals(ChannelMLConstants.SI_UNITS))
            {
                minV = minV/1000;
                maxV = maxV/1000;
                voltUnits = "V";
                timeUnits = "s";
            }
                       
            try
            {
                boolean postV1_7_3format = false;


                SimpleXMLEntity[] gates = xmlMechanism.getXMLDoc().getXMLEntities(ChannelMLConstants.getHHGateXPath()); // pre v1.7.3 format

                if (gates.length==0)
                {
                    gates = xmlMechanism.getXMLDoc().getXMLEntities(ChannelMLConstants.getPostV1_7_3GatesXPath()); // post v1.7.3 format
                    for(SimpleXMLEntity sxe: gates)
                        logger.logComment("Found gate: "+sxe);

                    if (gates.length>0)
                        postV1_7_3format =true;
                }
                
                SimpleXMLEntity[] q10s = xmlMechanism.getXMLDoc().getXMLEntities(ChannelMLConstants.getQ10SettingsXPath());

                ArrayList<Argument> q10sFound = new ArrayList<Argument>();
                Float tempToUseQ10 = Float.NaN;

                for(SimpleXMLEntity sxe: q10s)
                {
                    logger.logComment("Found parameter: "+sxe);
                    SimpleXMLElement el = (SimpleXMLElement)sxe;
                    String gateAttr = el.getAttributeValue(ChannelMLConstants.Q10_SETTINGS_GATE_ATTR);
                    ArrayList<String> gatesForThis = new ArrayList<String>();

                    if (gateAttr==null)
                    {
                         for (int gateIndex = 0; gateIndex < gates.length; gateIndex++)
                         {
                            if (gates[gateIndex] instanceof SimpleXMLElement)
                            {
                                SimpleXMLElement gate1 = (SimpleXMLElement)gates[gateIndex];

                                if(postV1_7_3format)
                                {
                                    String gateName = gate1.getAttributeValue(ChannelMLConstants.GATE_NAME_ELEMENT);
                                    gatesForThis.add(gateName);
                                }
                            }
                         }
                    }
                    else
                    {
                        gatesForThis.add(gateAttr);
                    }

                    logger.logComment("Gates: "+gatesForThis);

                    float expTemp = Float.parseFloat(el.getAttributeValue(ChannelMLConstants.Q10_SETTINGS_TEMP_ATTR));
                    
                    if(tempToUseQ10.isNaN())
                    {
                        float defaultTemp = project.simulationParameters.getTemperature();
                        String res = JOptionPane.showInputDialog(this, "Please enter the preferred temperature at which to plot the expressions", defaultTemp);
                        try
                        {
                            tempToUseQ10 = Float.parseFloat(res);
                        }
                        catch(Exception ex)
                        {
                            tempToUseQ10 = defaultTemp;
                        }
                    }
                    
                    float eval = Float.NaN;
                        
                    if(el.hasAttributeValue(ChannelMLConstants.Q10_SETTINGS_FIXED_FACTOR_ATTR))
                    {
                        eval = Float.parseFloat(el.getAttributeValue(ChannelMLConstants.Q10_SETTINGS_FIXED_FACTOR_ATTR));
                        if (tempToUseQ10!=expTemp)
                        {
                            GuiUtils.showWarningMessage(logger, "NOTE: there is a fixed Q10 value present in the file which must be used at a temperature of " +
                                +expTemp+", not "+tempToUseQ10+"!" , this);
                        }
                        
                    }
                    else
                    {
                        float factor = Float.parseFloat(el.getAttributeValue(ChannelMLConstants.Q10_SETTINGS_FACTOR_ATTR));


                        eval = (float)Math.pow(factor, (tempToUseQ10-expTemp)/10);
                    }

                    Variable[] mainVarsTemp = new Variable[mainVars.length+gatesForThis.size()];
                    for (int i=0;i<mainVars.length;i++)
                        mainVarsTemp[i] = mainVars[i];

                    for(int i=0;i<gatesForThis.size();i++)
                    {
                        mainVarsTemp[mainVars.length+i] = new Variable(TEMP_ADJ_PARAMETER+gatesForThis.get(i));

                        q10sFound.add(new Argument(TEMP_ADJ_PARAMETER+gatesForThis.get(i), eval));
                    }
                    mainVars = mainVarsTemp;

                }


                SimpleXMLEntity[] parameters = xmlMechanism.getXMLDoc().getXMLEntities(ChannelMLConstants.getChannelParameterXPath());

                Properties paramsFound = new Properties();
                for(SimpleXMLEntity sxe: parameters)
                {
                    logger.logComment("Found parameter: "+sxe);
                    SimpleXMLElement el = (SimpleXMLElement)sxe;
                    String name = el.getAttributeValue(ChannelMLConstants.PARAMETER_NAME_ATTR);
                    String value = el.getAttributeValue(ChannelMLConstants.PARAMETER_VALUE_ATTR);

                    logger.logComment("Parameter: "+name+" = "+ value);

                    paramsFound.setProperty(name, value);
                }

                logger.logComment("paramsFound: "+paramsFound);

                if (paramsFound.size()>0)
                {
                    Variable[] mainVarsTemp = new Variable[mainVars.length+paramsFound.size()];
                    for (int i=0;i<mainVars.length;i++)
                        mainVarsTemp[i] = mainVars[i];

                    Enumeration names  = paramsFound.keys();
                    int count = 0;
                    while (names.hasMoreElements())
                    {
                        String name = (String)names.nextElement();
                        Variable var = new Variable(name);
                        mainVarsTemp[mainVars.length+count] = var;
                        count++;
                    }
                    mainVars = mainVarsTemp;
                }
                
                
                

                if (gates.length==0)
                {
                    logger.logComment("Assuming leak conductance");
                    float gmax = Float.NaN;
                    float revPot = Float.NaN;
                    
                    String newCondRule = xmlMechanism.getXMLDoc().getValueByXPath(ChannelMLConstants.getCondLawXPath());
                    if (newCondRule!=null)
                        postV1_7_3format =true;
                        
                    
                    if(!postV1_7_3format)
                    {
                        SimpleXMLEntity[] ions = xmlMechanism.getXMLDoc().getXMLEntities(ChannelMLConstants.getPreV1_7_3IonsXPath());
                        SimpleXMLElement firstIon = (SimpleXMLElement) ions[0];

                        String revPotString = firstIon.getAttributeValue(ChannelMLConstants.ION_REVERSAL_POTENTIAL_ATTR);

                        SimpleXMLEntity[] conductances = xmlMechanism.getXMLDoc().getXMLEntities(ChannelMLConstants.getConductanceXPath());

                        SimpleXMLElement firstCond = (SimpleXMLElement) conductances[0];
                        String gmaxString = firstCond.getAttributeValue(ChannelMLConstants.DEFAULT_COND_DENSITY_ATTR);

                        gmax = Float.parseFloat(gmaxString);

                        revPot = Float.parseFloat(revPotString);
                    }
                    else
                    {
                        SimpleXMLEntity[] currVoltRel = xmlMechanism.getXMLDoc().getXMLEntities(ChannelMLConstants.getCurrVoltRelXPath());
                        SimpleXMLElement firstCurrVoltRel = (SimpleXMLElement) currVoltRel[0];
                        String revPotString = firstCurrVoltRel.getAttributeValue(ChannelMLConstants.ION_REVERSAL_POTENTIAL_ATTR);
                        String gmaxString = firstCurrVoltRel.getAttributeValue(ChannelMLConstants.DEFAULT_COND_DENSITY_ATTR);
                        
                        gmax = Float.parseFloat(gmaxString);

                        revPot = Float.parseFloat(revPotString);
                    }
                    
                    

                    String expression = gmax +" * (v - ("+revPot+"))";
                    EquationUnit func = Expression.parseExpression(expression, new Variable[]{v});


                    String graphRef = "Plot of leak current due to Cell Mechanism: " + xmlMechanism.getInstanceName();

                    String dsRef = graphRef;

                    String desc = dsRef;

                    desc = desc + "\n\nExpression for graph: " + expression;
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
                        
                        if(postV1_7_3format)
                        {
                            String gateName = gate.getAttributeValue(ChannelMLConstants.GATE_NAME_ELEMENT);
                            
                            boolean voltConcGate = false;
                            SimpleXMLEntity[] concDep = xmlMechanism.getXMLDoc().getXMLEntities(ChannelMLConstants.getCurrVoltRelXPath()+"/"
                                +ChannelMLConstants.CONC_DEP_ELEMENT);
                            
                            String concVarName = null;
                            float concVarVal = -1;
                            float concMin = -1;
                            float concMax = -1;

                                
                            if (concDep!=null && concDep.length>0)
                            {
                                voltConcGate = true;
                                SimpleXMLElement concDepEl = (SimpleXMLElement)concDep[0];
                                
                                concVarName = concDepEl.getAttributeValue(ChannelMLConstants.CONC_DEP_VAR_NAME_ATTR);
                                concMin = Float.parseFloat(concDepEl.getAttributeValue(ChannelMLConstants.CONC_DEP_MIN_CONC_ATTR));
                                concMax = Float.parseFloat(concDepEl.getAttributeValue(ChannelMLConstants.CONC_DEP_MAX_CONC_ATTR));
                            }

                            logger.logComment("Found gate: " + gate.getXMLString("", false));

                            SimpleXMLEntity[] transElements = gate.getXMLEntities(ChannelMLConstants.TRANSITION_ELEMENT);
                            SimpleXMLEntity[] timeCourseElements  = gate.getXMLEntities(ChannelMLConstants.TIME_COURSE_ELEMENT);
                            SimpleXMLEntity[] steadyStateElements  = gate.getXMLEntities(ChannelMLConstants.STEADY_STATE_ELEMENT);
                            
                            SimpleXMLEntity[] gateElements = new SimpleXMLEntity[transElements.length+timeCourseElements.length+steadyStateElements.length];
                            
                            System.arraycopy(transElements, 0, gateElements, 0, transElements.length);
                            System.arraycopy(timeCourseElements, 0, gateElements, transElements.length, timeCourseElements.length);
                            System.arraycopy(steadyStateElements, 0, gateElements, timeCourseElements.length+transElements.length, steadyStateElements.length);
                            
                            for(SimpleXMLEntity sxe: gateElements)
                            {
                                logger.logComment("gateElement: " + sxe);
                            }

                            if (gateElements.length==0)
                            {
                                GuiUtils.showErrorMessage(logger, "Did not find any relevant voltage/conc dependent transitions in gate: "+gateName, null, this);
                            }


                            Hashtable<String, DataSet> rateData = new Hashtable<String,DataSet>();
                                    
                            for (int index = 0; index < gateElements.length; index++)
                            {
                                logger.logComment("- Found contents of gate: "+ gateElements[index]);
                                
                                if (gateElements[index] instanceof SimpleXMLElement)
                                {

                                    SimpleXMLElement rate = (SimpleXMLElement)gateElements[index];
                                    
                                    String rateName = rate.getAttributeValue(ChannelMLConstants.RATE_NAME_ATTR);
                                    
                                    logger.logComment("- Found a SimpleXMLElement: "+ rate);

                                    String expression = null;

                                    EquationUnit mainFunc = null;

                                    DataSet dataSetFound = null;

                                    EquationUnit condPreFunc = null;
                                    EquationUnit condPostFunc = null;
                                    EquationUnit trueFunc = null;
                                    EquationUnit falseFunc = null;

                                    RelationalOperator relationship = null;

                                    String exprForm = rate.getAttributeValue(ChannelMLConstants.EXPR_FORM_ATTR);
                                    
                                    logger.logComment("exprForm: "+exprForm);

                                    if (!exprForm.equals(ChannelMLConstants.GENERIC_ATTR) &&
                                        !exprForm.equals(ChannelMLConstants.TABULATED_ATTR))
                                    {
                                        
                                        float A = Float.NaN;
                                        float B = Float.NaN;
                                        float Vhalf = Float.NaN;
                                        try
                                        {
                                            A = Float.parseFloat(rate.getAttributeValue(ChannelMLConstants.RATE_ATTR));
                                            B = Float.parseFloat(rate.getAttributeValue(ChannelMLConstants.SCALE_ATTR));
                                            Vhalf = Float.parseFloat(rate.getAttributeValue(ChannelMLConstants.MIDPOINT_ATTR));
                                        
                                                      
                                        }
                                        catch (NumberFormatException ex)
                                        {
                                            GuiUtils.showErrorMessage(logger, "Error getting parameter for rate: "+ rate.getName()+" in state "
                                            + gateName+" in Cell Mechanism "+ xmlMechanism.getInstanceName(), ex, this);
                                        }

                                        if (exprForm.equals(ChannelMLConstants.EXP_LINEAR_TYPE))
                                        {
                                            expression = A + " *(  (v - "+Vhalf+") / ("+B+") ) / (1 - exp(-1 * ( (v - "+Vhalf+") / ("+B+") )))";
                                        }
                                        else if (exprForm.equals(ChannelMLConstants.SIGMOID_TYPE))
                                        {
                                            expression = A + " /(1 + exp((v - "+Vhalf+") / ("+B+") ))";
                                        }
                                        else if (exprForm.equals(ChannelMLConstants.EXPONENTIAL_TYPE))
                                        {
                                            expression = A +" * exp((v - "+Vhalf+") / ("+B+") )";
                                        }

                                        mainFunc = Expression.parseExpression(expression, new Variable[]{v});


                                    }
                                    else if (exprForm.equals(ChannelMLConstants.TABULATED_ATTR))
                                    {
                                        logger.logComment("XPaths: "+rate.getXPathLocations(false));

                                        SimpleXMLElement table = (SimpleXMLElement)rate.getXMLEntities(ChannelMLConstants.TABLE_ELEMENT)[0];
                                        
                                        try
                                        {
                                            maxV =  Float.parseFloat(table.getAttributeValue(ChannelMLConstants.TABLE_XMAX));
                                            minV = Float.parseFloat(table.getAttributeValue(ChannelMLConstants.TABLE_XMIN));

                                            SimpleXMLEntity[] entries = table.getXMLEntities(ChannelMLConstants.ENTRY_ELEMENT);

                                            expression = "Tabulated data from: " + minV+" to : "+maxV+" with "+entries.length+" entries";

                                            numPoints = entries.length;

                                            logger.logComment(expression);

                                            dataSetFound = new DataSet("", "", "", "", "", "");

                                            for (int i=0;i<entries.length;i++)
                                            {
                                                float volt = minV + (i* (maxV-minV)/(entries.length-1));
                                                float value = Float.parseFloat(((SimpleXMLElement)entries[i]).getAttributeValue(ChannelMLConstants.ENTRY_VALUE_ATTR));

                                                logger.logComment(volt+" = "+value);

                                                dataSetFound.addPoint(volt, value);

                                            }
                                                      
                                        }
                                        catch (NumberFormatException ex)
                                        {
                                            GuiUtils.showErrorMessage(logger, "Error getting table info from: "+ rate, ex, this);
                                        }


                                    }
                                    else if (exprForm.equals(ChannelMLConstants.GENERIC_ATTR))
                                    {
                                        String expr = rate.getAttributeValue(ChannelMLConstants.EXPR_ATTR);

                                        logger.logComment(" -+- Found generic expr: " + expr+", voltConcGate: "+voltConcGate);

                                        expression = expr;

                                        if (voltConcGate && expr.indexOf(concVarName)>=0)
                                        {
                                            String val = JOptionPane.showInputDialog(this, "A concentration dependent expression for rate: "
                                                + rateName + " in gate " + gateName+" has been found:\n"
                                                    +expr+"\nPlease enter the value for "+concVarName+" to use in the graph. " +
                                                    "Max val: "+concMax+", min val: "+concMin, concMin + (concMax-concMin)/2);

                                            concVarVal = Float.parseFloat(val);

                                            Variable newRateVar = new Variable(concVarName);

                                            Variable[] tempMainVars = new Variable[mainVars.length+1];
                                            for(int i=0; i<mainVars.length;i++) tempMainVars[i] = mainVars[i];
                                            tempMainVars[mainVars.length] = newRateVar;
                                            mainVars = tempMainVars;
                                        }


                                    if (expr.indexOf("?")<0 && expr.indexOf(":")<0)
                                    {
                                        mainFunc = Expression.parseExpression(expr, mainVars);
                                    }
                                    else
                                    {
                                        String condFull = expr.substring(0, expr.indexOf("?")).trim();
                                        String trueExpr = expr.substring(expr.indexOf("?")+1, expr.indexOf(":")).trim();
                                        String falseExpr = expr.substring(expr.indexOf(":")+1).trim();


                                        trueFunc = Expression.parseExpression(trueExpr, mainVars);
                                        falseFunc = Expression.parseExpression(falseExpr, mainVars);

                                        condFull = GeneralUtils.replaceAllTokens(condFull, "&lt;", "<");
                                        condFull = GeneralUtils.replaceAllTokens(condFull, "&gt;", ">");
                                        
                                        logger.logComment("condFull: "+condFull+", trueExpr: "+trueExpr+", falseExpr: "+falseExpr);
                                        


                                        ArrayList<RelationalOperator> allROs = RelationalOperator.allROs;

                                        
                                        boolean done = false;
                                        for(RelationalOperator ro: allROs)
                                        {
                                            if (!done && condFull.indexOf(ro.operator)>0)
                                            {
                                                done = true;
                                                relationship = ro;
                                                String condExpr = condFull.substring(0, condFull.indexOf(ro.operator)).trim();
                                                String evalExpr = condFull.substring(condFull.indexOf(ro.operator)+ro.operator.length()).trim();
                                                condPreFunc = Expression.parseExpression(condExpr, mainVars);
                                                condPostFunc = Expression.parseExpression(evalExpr, mainVars);
                                            }
                                        }

                                    }
                                }
                            

                                String graphRef = "Plots for gating complex "
                                    + gateName + " in Cell Mechanism " + xmlMechanism.getInstanceName();

                                String dsRef = "Plot of: " + rateName + " in gating complex "
                                    + gateName + " in Cell Mechanism " + xmlMechanism.getInstanceName();

                                String desc = dsRef;

                                if (dataSetFound!=null)
                                {
                                    desc = desc + "\n\nGraph source: " + expression;
                                    dataSetFound.setReference(dsRef);
                                    dataSetFound.setDescription(desc);
                                    dataSetFound.setXUnit(voltUnits);
                                    dataSetFound.setXLegend("Membrane Potential");
                                    dataSetFound.setYLegend(gateName);


                                    PlotterFrame pf = PlotManager.getPlotterFrame(graphRef);

                                    pf.addDataSet(dataSetFound);

                                    rateData.put(rateName, dataSetFound);

                                }
                                if (mainFunc!=null)
                                {
                                    if (tempToUseQ10.isNaN())
                                    {
                                        prefTemperature = checkTempInExpression(prefTemperature, mainFunc.getNiceString());
                                    }
                                    else
                                    {
                                        prefTemperature = tempToUseQ10;
                                    }

                                    desc = desc + "\n\nExpression for graph: " + expression;
                                    desc = desc + "\nwhich has been parsed as: " + mainFunc.getNiceString();
                                    desc = desc + ""+getArgsInfo(paramsFound, concVarName, concVarVal, project, prefTemperature, q10sFound);


                                    DataSet ds = new DataSet(dsRef, desc, voltUnits, "", "Membrane Potential", gateName);

                                    for (int i = 0; i < numPoints; i++)
                                    {
                                        float nextVval = minV + ( (maxV - minV) * i / (numPoints));

                                       // Argument[] a0 = new Argument[]
                                       //     {new Argument(v.getName(), nextVval),
                                       //     new Argument(temp.getName(), project.simulationParameters.getTemperature())};

                                        Argument[] a0 = getArgsList(nextVval,
                                                v, temp, rateData, paramsFound, q10sFound,
                                                concVarName, concVarVal, mainFunc.getNiceString(), project, logger, prefTemperature);

                                        ds.addPoint(nextVval, mainFunc.evaluateAt(a0));
                                    }

                                    PlotterFrame pf = PlotManager.getPlotterFrame(graphRef);

                                    pf.addDataSet(ds);

                                    rateData.put(rateName, ds);
                                }
                                else if (relationship!=null)
                                {
                                    String parsed = "IF ("+condPreFunc.getNiceString()+") "+ relationship.operator 
                                    +"("+condPostFunc.getNiceString()+") THEN ("+trueFunc.getNiceString()+") ELSE ("+
                                    falseFunc.getNiceString()+")";

                                    desc = desc + "\n\nExpression for graph: " + expression;
                                    desc = desc + "\nwhich has been parsed as: " + parsed;
                                    desc = desc + "\n"+getArgsInfo(paramsFound, concVarName, concVarVal, project, prefTemperature, q10sFound);


                                    DataSet ds = new DataSet(dsRef, desc, voltUnits, "", "Membrane Potential", gateName);


                                    for (int j = 0; j < numPoints; j++)
                                    {
                                        float nextVval = minV + ( (maxV - minV) * j / (numPoints));

                                        Argument[] a0 = getArgsList(nextVval,
                                                v, temp, rateData, paramsFound, q10sFound,
                                                concVarName, concVarVal, parsed, project, logger, prefTemperature);

                                        double condPreEval = condPreFunc.evaluateAt(a0);
                                        double condPostEval = condPostFunc.evaluateAt(a0);

                                        if (relationship.evaluate(condPreEval, condPostEval))
                                        {
                                            ds.addPoint(nextVval, trueFunc.evaluateAt(a0));
                                        }
                                        else
                                        {
                                            ds.addPoint(nextVval, falseFunc.evaluateAt(a0));
                                        }

                                    }

                                    PlotterFrame pf = PlotManager.getPlotterFrame(graphRef);

                                    pf.addDataSet(ds);

                                    rateData.put(rateName, ds);
                                }


                                Variable newRateVar = new Variable(rateName);

                                Variable[] tempMainVars = new Variable[mainVars.length+1];
                                for(int i=0; i<mainVars.length;i++) tempMainVars[i] = mainVars[i];
                                tempMainVars[mainVars.length] = newRateVar;
                                mainVars = tempMainVars;
                                
                                
                                }
                            }
                            
                            logger.logComment("-       rateData: "+rateData);
                                    

                            if (rateData.containsKey("alpha") && rateData.containsKey("beta"))
                            {
                                DataSet dsAlpha = rateData.get("alpha");
                                DataSet dsBeta = rateData.get("beta");

                                String graphRef = "Plots for tau/inf in gating complex "
                                    + gateName+" in Cell Mechanism "+ xmlMechanism.getInstanceName();

                                if (!rateData.containsKey("tau"))
                                {
                                    String dsRef = "Plot of tau (1/(alpha+beta)) in gating complex "
                                    + gateName+" in Cell Mechanism "+ xmlMechanism.getInstanceName();

                                    DataSet tau = new DataSet(dsRef, dsRef, voltUnits, timeUnits, "Membrane Potential", "tau");

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
                                    String dsRef = "Plot of inf (alpha/(alpha+beta)) in gating complex "
                                    + gateName+" in Cell Mechanism "+ xmlMechanism.getInstanceName();

                                    DataSet inf = new DataSet(dsRef, dsRef, voltUnits, "", "Membrane Potential", "inf");

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
                        else   // postV1_7_3format = false
                        {

                            String gateState = gate.getAttributeValue(ChannelMLConstants.HH_GATE_STATE_ATTR);

                            logger.logComment("Found a gate with state: " + gateState);

                            SimpleXMLEntity[] gateElements = gate.getXMLEntities(ChannelMLConstants.TRANSITION_ELEMENT + "/"
                                + ChannelMLConstants.VOLTAGE_GATE_ELEMENT);

                            boolean voltConcGate = false;

                            if (gateElements.length==0)
                            {
                                gateElements = gate.getXMLEntities(ChannelMLConstants.TRANSITION_ELEMENT + "/"
                                    + ChannelMLConstants.VOLTAGE_CONC_GATE_ELEMENT);

                                voltConcGate = true;
                            }

                            if (gateElements.length==0)
                            {
                                GuiUtils.showErrorMessage(logger, "Did not find any relevant voltage/conc dependent transitions in state: "+gateState, null, this);
                            }


                            for (int vgIndex = 0; vgIndex < gateElements.length; vgIndex++)
                            {
                                if (gateElements[vgIndex] instanceof SimpleXMLElement)
                                {
                                    Hashtable<String, DataSet> rateData = new Hashtable<String,DataSet>();

                                    SimpleXMLElement vg = (SimpleXMLElement)gateElements[vgIndex];
                                    
                                    logger.logComment("Found vg: " + vg);

                                    ArrayList<SimpleXMLEntity> rates = vg.getContents();

                                    String concVarName = null;
                                    float concVarVal = -1;
                                    float concMin = -1;
                                    float concMax = -1;

                                    for (SimpleXMLEntity child: rates)
                                    {

                                        if (child instanceof SimpleXMLElement)
                                        {
                                            //Enumeration<String> prevRate = rateData.keys();

                                            SimpleXMLElement rate = (SimpleXMLElement)child;

                                            logger.logComment("Found: " + rate);

                                            if (rate.getName().equals(ChannelMLConstants.CONC_DEP_ELEMENT))
                                            {
                                                concVarName = rate.getAttributeValue(ChannelMLConstants.CONC_DEP_VAR_NAME_ATTR);
                                                concMin = Float.parseFloat(rate.getAttributeValue(ChannelMLConstants.CONC_DEP_MIN_CONC_ATTR));
                                                concMax = Float.parseFloat(rate.getAttributeValue(ChannelMLConstants.CONC_DEP_MAX_CONC_ATTR));

                                                logger.logComment("It's a volt conc el: " + concVarName+" ("+concMin+" -> "+ concMax+")");
                                            }


                                            String expression = null;

                                            EquationUnit mainFunc = null;
                                            EquationUnit condPreFunc = null;
                                            EquationUnit condPostFunc = null;
                                            EquationUnit trueFunc = null;
                                            EquationUnit falseFunc = null;

                                            RelationalOperator relationship = null;


                                            SimpleXMLEntity[] paramHHPlots = rate.getXMLEntities(ChannelMLConstants.PARAMETERISED_HH_ELEMENT);


                                            for (int paramPlotIndex = 0; paramPlotIndex < paramHHPlots.length; paramPlotIndex++)
                                            {
                                                logger.logComment(" - - Found: "+ paramHHPlots[paramPlotIndex]);

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
                                                        + gateState+" in Cell Mechanism "+ xmlMechanism.getInstanceName(), ex, this);
                                                    }

                                                    if (eqnType.equals(ChannelMLConstants.LINOID_TYPE_OLD))
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



                                                    mainFunc = Expression.parseExpression(expression, new Variable[]{v});


                                                }
                                            }

                                            //SimpleXMLEntity[] genericPlots = rate.getXMLEntities(ChannelMLConstants.PARAMETERISED_HH_ELEMENT);
                                            SimpleXMLEntity[] generic = rate.getXMLEntities(ChannelMLConstants.GENERIC_HH_ELEMENT_OLDER);
                                            if (generic.length==0)
                                                generic = rate.getXMLEntities(ChannelMLConstants.GENERIC_HH_ELEMENT_OLD);


                                            for (int genericIndex = 0; genericIndex < generic.length; genericIndex++)
                                            {
                                                logger.logComment(" - - Found: " + generic[genericIndex]);

                                                if (generic[genericIndex] instanceof SimpleXMLElement)
                                                {
                                                    SimpleXMLElement genericPlot = (SimpleXMLElement) generic[genericIndex];

                                                    String expr = genericPlot.getAttributeValue(ChannelMLConstants.GENERIC_HH_EXPR_ATTR);
                                                    logger.logComment(" -+- Found expr: " + expr+", voltConcGate: "+voltConcGate);

                                                    expression = expr;

                                                    if (voltConcGate && expr.indexOf(concVarName)>=0)
                                                    {
                                                        String val = JOptionPane.showInputDialog(this, "A concentration dependent expression for rate: "+rate.getName() + " in state "
                                                + gateState+" has been found:\n"
                                                                +expr+"\nPlease enter the value for "+concVarName+" to use in the graph. Max val: "+concMax+", min val: "+concMin, concMin + (concMax-concMin)/2);

                                                        concVarVal = Float.parseFloat(val);

                                                        Variable newRateVar = new Variable(concVarName);

                                                        Variable[] tempMainVars = new Variable[mainVars.length+1];
                                                        for(int i=0; i<mainVars.length;i++) tempMainVars[i] = mainVars[i];
                                                        tempMainVars[mainVars.length] = newRateVar;
                                                        mainVars = tempMainVars;
                                                    }


                                                    if (expr.indexOf("?")<0 && expr.indexOf(":")<0)
                                                    {
                                                        mainFunc = Expression.parseExpression(expr, mainVars);
                                                    }
                                                    else
                                                    {
                                                        String condFull = expr.substring(0, expr.indexOf("?")).trim();
                                                        String trueExpr = expr.substring(expr.indexOf("?")+1, expr.indexOf(":")).trim();
                                                        String falseExpr = expr.substring(expr.indexOf(":")+1).trim();


                                                        trueFunc = Expression.parseExpression(trueExpr, mainVars);
                                                        falseFunc = Expression.parseExpression(falseExpr, mainVars);

                                                        condFull = GeneralUtils.replaceAllTokens(condFull, "&lt;", "<");
                                                        condFull = GeneralUtils.replaceAllTokens(condFull, "&gt;", ">");

                                                        logger.logComment("condFull: "+condFull+", trueExpr: "+trueExpr+", falseExpr: "+falseExpr);

                                                        ArrayList<RelationalOperator> allROs = RelationalOperator.allROs;
                                                        
                                                        boolean done = false;
                                                        for(RelationalOperator ro: allROs)
                                                        {
                                                            if (!done && condFull.indexOf(ro.operator)>0)
                                                            {
                                                                relationship = ro;
                                                                String condExpr = condFull.substring(0, condFull.indexOf(ro.operator)).trim();
                                                                String evalExpr = condFull.substring(condFull.indexOf(ro.operator)+ro.operator.length()).trim();
                                                                condPreFunc = Expression.parseExpression(condExpr, mainVars);
                                                                condPostFunc = Expression.parseExpression(evalExpr, mainVars);
                                                                done = true;
                                                            }
                                                        }

                                                    }
                                                }
                                            }

                                            String graphRef = "Plots for state "
                                                + gateState + " in Cell Mechanism " + xmlMechanism.getInstanceName();

                                            String dsRef = "Plot of: " + rate.getName() + " in state "
                                                + gateState + " in Cell Mechanism " + xmlMechanism.getInstanceName();

                                            String desc = dsRef;


                                            if (mainFunc!=null)
                                            {

                                                desc = desc + "\n\nExpression for graph: " + expression;
                                                desc = desc + "\nwhich has been parsed as: " + mainFunc.getNiceString();
                                                desc = desc + "\n"+getArgsInfo(paramsFound, concVarName, concVarVal, project, prefTemperature, q10sFound);


                                                DataSet ds = new DataSet(dsRef, desc, voltUnits, "", "Membrane Potential", gateState);

                                                for (int i = 0; i < numPoints; i++)
                                                {
                                                    float nextVval = minV + ( (maxV - minV) * i / (numPoints));

                                                   // Argument[] a0 = new Argument[]
                                                   //     {new Argument(v.getName(), nextVval),
                                                   //     new Argument(temp.getName(), project.simulationParameters.getTemperature())};

                                                    Argument[] a0 = getArgsList(nextVval,
                                                            v, temp, rateData, paramsFound, q10sFound,
                                                            concVarName, concVarVal, mainFunc.getNiceString(), project, logger, prefTemperature);

                                                    ds.addPoint(nextVval, mainFunc.evaluateAt(a0));
                                                }

                                                PlotterFrame pf = PlotManager.getPlotterFrame(graphRef);

                                                pf.addDataSet(ds);

                                                rateData.put(rate.getName(), ds);
                                            }
                                            else if (relationship!=null)
                                            {
                                                String parsed = "IF ("+condPreFunc.getNiceString()+") "+ relationship.operator 
                                                +"("+condPostFunc.getNiceString()+") THEN ("+trueFunc.getNiceString()+") ELSE ("+
                                                falseFunc.getNiceString()+")";

                                                desc = desc + "\n\nExpression for graph: " + expression;
                                                desc = desc + "\nwhich has been parsed as: " + parsed;
                                                desc = desc + "\n"+getArgsInfo(paramsFound, concVarName, concVarVal, project, prefTemperature, q10sFound);


                                                DataSet ds = new DataSet(dsRef, desc, voltUnits, "", "Membrane Potential", gateState);


                                                for (int j = 0; j < numPoints; j++)
                                                {
                                                    float nextVval = minV + ( (maxV - minV) * j / (numPoints));

                                                    Argument[] a0 = getArgsList(nextVval,
                                                            v, temp, rateData, paramsFound, q10sFound,
                                                            concVarName, concVarVal, parsed, project, logger, prefTemperature);

                                                    double condPreEval = condPreFunc.evaluateAt(a0);
                                                    double condPostEval = condPostFunc.evaluateAt(a0);

                                                    if (relationship.evaluate(condPreEval, condPostEval))
                                                    {
                                                        ds.addPoint(nextVval, trueFunc.evaluateAt(a0));
                                                    }
                                                    else
                                                    {
                                                        ds.addPoint(nextVval, falseFunc.evaluateAt(a0));
                                                    }

                                                }

                                                PlotterFrame pf = PlotManager.getPlotterFrame(graphRef);

                                                pf.addDataSet(ds);

                                                rateData.put(rate.getName(), ds);
                                            }


                                            Variable newRateVar = new Variable(rate.getName());

                                            Variable[] tempMainVars = new Variable[mainVars.length+1];
                                            for(int i=0; i<mainVars.length;i++) tempMainVars[i] = mainVars[i];
                                            tempMainVars[mainVars.length] = newRateVar;
                                            mainVars = tempMainVars;
                                        }



                                    }

                                    if (rateData.containsKey("alpha") && rateData.containsKey("beta"))
                                    {
                                        DataSet dsAlpha = rateData.get("alpha");
                                        DataSet dsBeta = rateData.get("beta");

                                        String graphRef = "Plots for tau/inf in state "
                                            + gateState+" in Cell Mechanism "+ xmlMechanism.getInstanceName();

                                        if (!rateData.containsKey("tau"))
                                        {
                                            String dsRef = "Plot of tau (1/(alpha+beta)) in state "
                                            + gateState+" in Cell Mechanism "+ xmlMechanism.getInstanceName();

                                            DataSet tau = new DataSet(dsRef, dsRef, voltUnits, timeUnits, "Membrane Potential", "tau");

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
                                            + gateState+" in Cell Mechanism "+ xmlMechanism.getInstanceName();

                                            DataSet inf = new DataSet(dsRef, dsRef, voltUnits, "", "Membrane Potential", "inf");

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
            }
            catch (XMLMechanismException ex)
            {
                GuiUtils.showErrorMessage(logger, "Error calculating plots for "+xmlDialect+" process: "+ xmlMechanism, ex, this);
            }
            catch (EquationException ex)
            {
                GuiUtils.showErrorMessage(logger, "Error calculating plots for "+xmlDialect+" process: "+ xmlMechanism, ex, this);
            }
        }
        else if (xmlMechanism.isSynapticMechanism())
        {
            try
            {
                SimpleXMLEntity[] synTypes = xmlMechanism.getXMLDoc().getXMLEntities(ChannelMLConstants.getSynapseTypeXPath());

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

                                Variable t = new Variable("t");
                                
                                double tp = ( (taur * taud) / (taud - taur)) * Math.log(taud / taur);
                                double factor = 1 / (Math.exp( -tp / taud) - Math.exp( -tp / taur));

                                String expression = maxCond + " * "+factor + " * (exp(-1* t / "+taud+") - exp(-1* t / "+taur+"))";
                                
                                float startTime = 0;
                                float endTime = taud*5;
                                
                                if (taur==0)
                                {
                                    expression =  maxCond + " * (exp(-1* t / "+taud+"))";
                                }
                                 
                                if (taur==taud)
                                {
                                    expression =  maxCond + " * ( t / "+taud+") * (exp(1 - t / "+taud+"))";
                                }



                                 EquationUnit func = Expression.parseExpression(expression, new Variable[]{t});


                                String graphRef = "Plots for time course of Synaptic Mechanism " + xmlMechanism.getInstanceName();

                                String desc = graphRef;

                                desc = desc + "\n\nExpression for graph: " + expression;
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
            catch (XMLMechanismException ex)
            {
                GuiUtils.showErrorMessage(logger, "Error calculating plots for "+xmlDialect+" process: " + xmlMechanism, ex, this);
            }
            catch (EquationException ex)
            {
                GuiUtils.showErrorMessage(logger, "Error calculating plots for "+xmlDialect+" process: "+ xmlMechanism, ex, this);
            }


        }
        else
        {
            GuiUtils.showErrorMessage(logger, "Unfortunately there is no information on what would be interesting to plot for"+
                                      " this type of "+xmlDialect+" file at present.", null, this);

            return;

        }

    }
    

    private String getArgsInfo(Properties paramsFound, String concVarName, float concVarVal, Project project, float prefTemperature, ArrayList<Argument> q10sFound)
    {
        StringBuffer info = new StringBuffer();
        if (!Float.isNaN(prefTemperature))
            info.append(" at temperature: "+ prefTemperature);

        if (concVarName!=null && concVarName.length()>0)
            info.append(", "+concVarName+" = "+concVarVal);
        if (paramsFound.size()>0)
        {
            info.append(", and parameters: "+paramsFound);
        }
        for (Argument a: q10sFound)
        {
            info.append(", "+a.getName()+" = "+a.getValue());
        }
        return info.toString();
    }
    
    private  Argument[] getArgsList(float nextVval,
            Variable v, Variable temp, Hashtable<String, DataSet> rateData, Properties paramsFound, ArrayList<Argument> q10sFound,
            String concVarName, float concVarVal, String parsed, Project project, ClassLogger logger, float prefTemperature)
    {
        int numMain = 2;
        Argument concVar = null;
        
        if (concVarName!=null)
        {
            concVar = new Argument(concVarName, concVarVal);
            numMain = 3;
        }
        
        Argument[] a0 = new Argument[numMain + rateData.size()+paramsFound.size()+q10sFound.size()];
        
        a0[0] = new Argument(v.getName(), nextVval);
        
        a0[1] = new Argument(temp.getName(), prefTemperature);

        if (concVarName!=null)
            a0[2] = concVar;
        
        Vector<String> prevRates = new Vector<String>(rateData.keySet());
        
        for(int k =0;k<prevRates.size();k++)
        {
            String argName = prevRates.get(k);
            DataSet argDs = rateData.get(argName);
            
            try
            {
                a0[numMain+k] = new Argument(argName, argDs.getYvalue(nextVval));
            }
            catch (ValueNotPresentException e1)
            {
                GuiUtils.showErrorMessage(logger, 
                        "Unable to determine value of variable: "+argName+" in DataSet:\n"+ argDs.getReference()
                        +"\nto evaluate: "+ parsed, e1, this);
                return null;
            }
        }
        Enumeration names  = paramsFound.keys();
        int index = numMain + rateData.size();
        while (names.hasMoreElements())
        {
            String name = (String)names.nextElement();
            double val = Double.parseDouble(paramsFound.getProperty(name));
            a0[index] = new Argument(name, val);
            index++;
        }

        for(Argument a: q10sFound)
        {
            a0[index] = a;
            index++;
        }
        
        return a0;
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
            if (browserPath==null)
            {
                GuiUtils.showErrorMessage(logger, "Could not start a browser!", null, this);
                return;
            }
            
            Runtime rt = Runtime.getRuntime();

            String command = browserPath + " \"" + url+"\"";
            if (GeneralUtils.isLinuxBasedPlatform())
            {
                command = browserPath + " " + url;
            }

            logger.logComment("Going to execute command: " + command);

            try
            {
                rt.exec(command);

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
                SimpleXMLDocument xmlDoc = xmlMechanism.getXMLDoc();

                this.jTextAreaValue.setText(xmlDoc.getValueByXPath(selected));

                this.jTextAreaXPathString.setText(selected);
            }
            catch (XMLMechanismException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem parsing the "+xmlDialect+" file", ex, this);
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
            SimpleXMLDocument xmlDoc = xmlMechanism.getXMLDoc();

            logger.logComment("Setting: "+ selected+" to: "+ text+", result: "+xmlDoc.setValueByXPath(selected, text));
        }
        catch (XMLMechanismException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem parsing the "+xmlDialect+" file", ex, this);
        }

    }

    public void jTabbedPane1_stateChanged(ChangeEvent e)
    {
        //logger.logComment("Event registered: "+e);

        if (! refreshingInterface) refresh();
    }

    public void jButtonSave_actionPerformed(ActionEvent e)
    {
        logger.logComment("Closing with saving...");
        try
        {
            this.xmlMechanism.saveCurrentState(project);
            this.dispose();
        }
        catch (XMLMechanismException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem saving the "+xmlDialect+" file", ex, this);
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
            GuiUtils.showErrorMessage(logger, "Problem validating the "+xmlDialect+" file. Note that the file was validated against the version of the NeuroML " +
                "schema (v"+GeneralProperties.getNeuroMLVersionNumber()+") included with this distribution of neuroConstruct:\n" 
                + schemaFile.getAbsolutePath()+"\n\n"
                                      + "To validate it against current and past NeuroML schema see:\n"
                                      + GeneralProperties.getWebsiteNMLValidator(), ex, this);

            return;
        }

        GuiUtils.showInfoMessage(logger, "Success", ""+xmlDialect+" file is well formed and valid, according to schema:\n"
                                 + schemaFile.getAbsolutePath()+"\n\nNote: to change the version of the NeuroML schema with which to validate the file, go to:\n" +
                                 "Settings -> General Properties & Project Defaults -> NeuroML version", this);

    }

    private Reader getMyXMLReader() throws IOException, XMLMechanismException
    {
        Reader in = null;

        in = new StringReader(this.xmlMechanism.getXMLDoc().getXMLString("", false));

        return in;
    }
    
    



}
