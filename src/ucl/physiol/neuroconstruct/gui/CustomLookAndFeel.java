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

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;


/**
 * Customised look and feel, modifying the default "Ocean" look and feel of Java 1.5
 * (Tabs didn't look great with the default settings)
 *
 * @author Padraig Gleeson
 *  
 */


public class CustomLookAndFeel extends OceanTheme
{

    @Override
    public String getName() { return "Custom Look and Feel for neuroConstruct"; }


    Color MAIN_CONTROL_COLOUR = new Color(218, 218, 218);
    ColorUIResource MAIN_CONTROL_COLOUR_UIR = new ColorUIResource(MAIN_CONTROL_COLOUR);

    Color LIGHTER_CONTROL_COLOUR = new Color(230, 230, 230);
    ColorUIResource LIGHTER_CONTROL_COLOUR_UIR = new ColorUIResource(LIGHTER_CONTROL_COLOUR);

    Color MAIN_BACKGROUND_COLOUR = new Color(232, 232, 232);
    ColorUIResource MAIN_BACKGROUND_COLOUR_UIR = new ColorUIResource(MAIN_BACKGROUND_COLOUR);

    Color DARKER_BACKGROUND_COLOUR = new Color(202, 202, 202);
    ColorUIResource DARKER_BACKGROUND_COLOUR_UIR = new ColorUIResource(DARKER_BACKGROUND_COLOUR);

    Color MENU_BACKGROUND_COLOUR = new Color(182, 182, 182);
    ColorUIResource MENU_BACKGROUND_COLOUR_UIR = new ColorUIResource(MENU_BACKGROUND_COLOUR);




    ColorUIResource primary1 = MENU_BACKGROUND_COLOUR_UIR; // Some (tab) button surrounds, image parts (tabs on folders)

    ColorUIResource primary2 = MENU_BACKGROUND_COLOUR_UIR; // Button surrounds...

    ColorUIResource primary3 = MAIN_BACKGROUND_COLOUR_UIR; // image parts, tooltext

    ColorUIResource secondary1 = new ColorUIResource(MENU_BACKGROUND_COLOUR.darker()); // main component borders
    ColorUIResource secondary2 = DARKER_BACKGROUND_COLOUR_UIR;   // main disabled stuff
    ColorUIResource secondary3 = LIGHTER_CONTROL_COLOUR_UIR; // main background

    // the functions overridden from the base class => DefaultMetalTheme

    @Override
    protected ColorUIResource getPrimary1()
    {
        return primary1;
    }

    @Override
    protected ColorUIResource getPrimary2()
    {
        return primary2;
    }

    @Override
    protected ColorUIResource getPrimary3()
    {
        return primary3;
    }

    @Override
    protected ColorUIResource getSecondary1()
    {
        return secondary1;
    }

    @Override
    protected ColorUIResource getSecondary2()
    {
        return secondary2;
    }

    @Override
    protected ColorUIResource getSecondary3()
    {
        return secondary3;
    }

    ColorUIResource black = new ColorUIResource(10, 10, 10); // main text
    ColorUIResource white = new ColorUIResource(250, 250, 255); // open areas text fields etc.

    @Override
    protected ColorUIResource getBlack()
    {
        return black;
    }

    @Override
    protected ColorUIResource getWhite()
    {
        return white;
    }

    public void printThemeInfo()
    {
        System.out.println("ControlTextFont: "+getControlTextFont());
        System.out.println("getSystemTextFont: "+getSystemTextFont());
        System.out.println("getUserTextFont: "+getUserTextFont());
        System.out.println("getMenuTextFont: "+getMenuTextFont());
        System.out.println("getWindowTitleFont: "+getWindowTitleFont());
        System.out.println("getSubTextFont: "+getSubTextFont());
    }


    private static int mainFontSize = 12;
    private static int smallFontSize = 10;
    
    private static String  mainFont = "Arial";
    private static String userFont = "Arial";

    public static int getMainFontSize()
    {
        return mainFontSize;
    }
    public static String getMainFont()
    {
        return mainFont;
    }
    

    private final FontUIResource controlFont = new FontUIResource(mainFont, Font.BOLD, mainFontSize);
    private final FontUIResource systemFont = new FontUIResource(mainFont, Font.PLAIN, mainFontSize);
    private final FontUIResource windowTitleFont = new FontUIResource(mainFont, Font.BOLD,mainFontSize);

    private final FontUIResource userUFont = new FontUIResource(userFont, Font.PLAIN, mainFontSize);
    private final FontUIResource smallFont = new FontUIResource(mainFont, Font.PLAIN, smallFontSize);

    @Override
    public FontUIResource getControlTextFont() { return controlFont;}
    @Override
    public FontUIResource getSystemTextFont() { return systemFont;}
    @Override
    public FontUIResource getUserTextFont() { return userUFont;}
    @Override
    public FontUIResource getMenuTextFont() { return controlFont;}
    @Override
    public FontUIResource getWindowTitleFont() { return windowTitleFont;}
    @Override
    public FontUIResource getSubTextFont() { return smallFont;}


    @Override
    public void addCustomEntriesToTable(UIDefaults table)
    {
        Object focusBorder
            = new UIDefaults.ProxyLazyValue(
                        "javax.swing.plaf.BorderUIResource$LineBorderUIResource",
                        new Object[] {getPrimary1()});


          java.util.List buttonGradient
              = Arrays.asList(new Object[]
                              {new Float(.3f),
                              new Float(0f),
                              MAIN_BACKGROUND_COLOUR_UIR,
                              LIGHTER_CONTROL_COLOUR.brighter(),
                              MAIN_BACKGROUND_COLOUR});

        Color anImportantColor1 = new ColorUIResource(0xCCCCCC);
        //Color anImportantColor2 = new ColorUIResource(Color.GREEN);

        //Color anImportantColor3 = new ColorUIResource(Color.red);


        java.util.List sliderGradient
            = Arrays.asList(new Object[]{new Float(.3f),
                                         new Float(.2f),
                                         MAIN_CONTROL_COLOUR,
                                         getWhite(),
                                         new ColorUIResource(getSecondary2())});


        Object[] defaults = new Object[]
            {
            "Button.gradient", buttonGradient,
            "Button.rollover", Boolean.TRUE,
            "Button.toolBarBorderBackground", getInactiveControlTextColor(),
            "Button.disabledToolBarBorderBackground", anImportantColor1,
            "Button.rolloverIconType", "ocean",
            "CheckBox.rollover", Boolean.TRUE,
            "CheckBox.gradient", buttonGradient,
            "CheckBoxMenuItem.gradient", buttonGradient,

                  /*
            // home2
            "FileChooser.homeFolderIcon",

            getIconResource("icons/ocean/homeFolder.gif"),
            // directory2
            "FileChooser.newFolderIcon",
            getIconResource("icons/ocean/newFolder.gif"),
            // updir2
            "FileChooser.upFolderIcon",
            getIconResource("icons/ocean/upFolder.gif"),
            // computer2
            "FileView.computerIcon",
            getIconResource("icons/ocean/computer.gif"),
            "FileView.directoryIcon", directoryIcon,
            // disk2
            "FileView.hardDriveIcon",
            getIconResource("icons/ocean/hardDrive.gif"),
            "FileView.fileIcon", fileIcon,
            // floppy2
            "FileView.floppyDriveIcon",
            getIconResource("icons/ocean/floppy.gif"),
*/
            "Label.disabledForeground", getInactiveControlTextColor(),

            "Menu.opaque", Boolean.FALSE,

            "MenuBar.gradient", Arrays.asList(new Object[]
                                              {
                                              new Float(1f), new Float(0f),
                                              getWhite(), MAIN_CONTROL_COLOUR,
                                              MAIN_CONTROL_COLOUR_UIR}),
            "MenuBar.borderColor", anImportantColor1,

            "InternalFrame.activeTitleGradient", buttonGradient,


            "List.focusCellHighlightBorder", focusBorder,

            "MenuBarUI", "javax.swing.plaf.metal.MetalMenuBarUI",
/*
            "OptionPane.errorIcon",
            getIconResource("icons/ocean/error.png"),
            "OptionPane.informationIcon",
            getIconResource("icons/ocean/info.png"),
            "OptionPane.questionIcon",
            getIconResource("icons/ocean/question.png"),
            "OptionPane.warningIcon",
            getIconResource("icons/ocean/warning.png"),
*/
            "RadioButton.gradient", buttonGradient,
            "RadioButton.rollover", Boolean.TRUE,

            "RadioButtonMenuItem.gradient", buttonGradient,

            "ScrollBar.gradient", buttonGradient,

            "Slider.altTrackColor", new ColorUIResource(0xD2E2EF),
            "Slider.gradient", sliderGradient,
            "Slider.focusGradient", sliderGradient,

            "SplitPane.oneTouchButtonsOpaque", Boolean.FALSE,
            "SplitPane.dividerFocusColor", DARKER_BACKGROUND_COLOUR_UIR,
    /*
            "TabbedPane.borderHightlightColor", getPrimary1(),
            "TabbedPane.contentAreaColor", c8ddf2,
            "TabbedPane.contentBorderInsets", new Insets(4, 2, 3, 3),
            "TabbedPane.selected", c8ddf2,
            "TabbedPane.tabAreaBackground", dadada,
            "TabbedPane.tabAreaInsets", new Insets(2, 2, 0, 6),
            "TabbedPane.unselectedBackground", getSecondary3(),
*/
            "TabbedPane.borderHightlightColor", getPrimary1(),
            "TabbedPane.contentAreaColor", MAIN_BACKGROUND_COLOUR_UIR,
            "TabbedPane.contentBorderInsets", new Insets(4, 2, 3, 3),
            "TabbedPane.selected", MAIN_CONTROL_COLOUR_UIR,
            "TabbedPane.tabAreaBackground", MAIN_BACKGROUND_COLOUR_UIR,
            "TabbedPane.tabAreaInsets", new Insets(2, 2, 0, 6),
            "TabbedPane.unselectedBackground", getSecondary3(),

            "Table.focusCellHighlightBorder", focusBorder,
            "Table.gridColor", getSecondary1(),

            "ToggleButton.gradient", buttonGradient,

            "ToolBar.borderColor", anImportantColor1,
            "ToolBar.isRollover", Boolean.TRUE,
/*
            "Tree.closedIcon", directoryIcon,

            "Tree.expandedIcon",
            getIconResource("icons/ocean/expanded.gif"),
            "Tree.leafIcon", fileIcon,
            "Tree.openIcon", directoryIcon,*/
            "Tree.selectionBorderColor", getPrimary1()
            } ;
        table.putDefaults(defaults);
    }



}
