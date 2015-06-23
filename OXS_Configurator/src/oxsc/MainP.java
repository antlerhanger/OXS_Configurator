/****************************************************************************
 **                                                                         **
 **  OpenXsensor Configurator: GUI for generating oXs_config.h file         **
 **  Copyright (C) 2015   David LABURTHE                                    **
 **                                                                         **
 **  This program is free software: you can redistribute it and/or modify   **
 **  it under the terms of the GNU General Public License as published by   **
 **  the Free Software Foundation, either version 3 of the License, or      **
 **  (at your option) any later version.                                    **
 **                                                                         **
 **  This program is distributed in the hope that it will be useful,        **
 **  but WITHOUT ANY WARRANTY; without even the implied warranty of         **
 **  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          **
 **  GNU General Public License for more details.                           **
 **                                                                         **
 **  You should have received a copy of the GNU General Public License      **
 **  along with this program.  If not, see <http://www.gnu.org/licenses/>.  **
 **                                                                         **
 *****************************************************************************
 **                        Author: David LABURTHE                           **
 **                      Contact: dlaburthe@free.fr                         **
 **                           Date: 04.01.2015                              **
 ****************************************************************************/

package oxsc;

import gui.TabAirSpeed;
import gui.TabCurrent;
import gui.TabData;
import gui.TabGeneralSettings;
import gui.TabPPM;
import gui.TabVario;
import gui.TabVoltage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.data.StringList;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.DropdownList;
import controlP5.Group;
import controlP5.Numberbox;
import controlP5.Slider;
import controlP5.Textarea;
import controlP5.Textfield;

public class MainP extends PApplet {

	/**
	 * ??
	 */
	private static final long serialVersionUID = 1L;

	public boolean tempActive = false; // Define temperature sensor availability

	static final String oxsVersion = "v2.x";
	static final String oxsCversion = "v2.1";

	String day = (day() < 10) ? "0" + day() : "" + day();
	String month = (month() < 10) ? "0" + month() : "" + month();

	public static String oxsDirectory = "";
	PrintWriter output;
	public static String outputConfigDir = "";

	public static final int tabGray = 0xFFC8C8C8; // gray 200
	public static final int backDdlGray = 0xFFFFFFFF; // gray 190
	public static final int topBottomGray = 0xFF969696; // gray 150
	public static final int darkBackGray = 0xFF464646; // gray 70
	public static final int blueAct = 0xFF0FA5FF; // color(15, 165, 255);
	public static final int lightBlue = 0xFFB5CFDB; // color(181, 207, 219);
	public static final int orangeAct = 0xFFFF8000; // color(255, 128, 0);
	public static final int lightOrange = 0xFFDBBFB5; // color(219, 191, 181);
	public static final int grayedColor = 0xFF9B9B9B; // color(155);
	public static final int okColor = 0xFF18D018; // color(24, 208, 24);
	public static final int warnColor = 0xFFFFB700; // color(255, 183, 0);
	public static final int errorColor = 0xFFFF2323; // color(255, 35, 35);
	public static final int white = 0xFFFFFFFF;

	PShape oxsI;
	PShape oxsL;

	public PFont fontLabel; // = createFont("arial.ttf", 12, false) ;
	PFont fontItalic; // = createFont("arial italic", 12, false) ;
	public PFont fontCells; // = createFont("arial bold", 12, false) ;
	PFont font16; // = createFont("arial", 16, false) ;
	PFont font20; // = createFont("arial", 20, false) ;

	public ControlP5 cp5;

	Group messageBox; // TODO later
	// Popup message; // TODO later

	// Tabs declaration
	TabGeneralSettings tabGenSet;
	public TabPPM tabPPM;
	TabVario tabVario;
	TabAirSpeed tabAirSpeed;
	TabVoltage tabVoltage;
	TabCurrent tabCurrent;
//	TabTemperature tab5;
	public static TabData tabData;

	public static String[] analogPins = new String[8]; // Analog pins array

	public static StringList messageList = new StringList(); // TODO later

	static boolean numPinsValid;
	static boolean analogPinsValid;
	static int vSpeedValid; // 0 -> not valid 1 -> warning 2 -> valid
	static boolean cellsValid;
	static boolean sentDataValid;

	static int versionValid; // 0 -> not valid 1 -> warning 2 -> valid	
	static int allValid; // 0 -> not valid 1 -> warning 2 -> valid

	// Variables to set the controllers data type
	int vSpeedMin;
	int vSpeedMax;
	int sensMinMax;
	int vSpeedMinMax;
	int ppmRngMinMax;
	int ppmRngMin;
	int ppmRngMax;
	int varioHysteresis;
	int ppmRngSensMinMax;
	int ppmSensMinMax;
	int ppmVspeedSwMin;
	int ppmVspeedSwMax;
	int outClimbRateMinMax;
	int aSpeedReset;
	int ppmRngCompMinMax;
	int ppmCompMinMax;

	int mBoxWidth = 400; // TODO later
	int mBoxHeight = 320; // TODO later

	public static Protocol protocol;

	static Sensor vario;
	static Sensor vario2;
	static Sensor airSpeed;
	public static Sensor[] aVolt = new Sensor[TabVoltage.getVoltnbr() + 1];
	static Sensor current;
	static Sensor rpm;
	static Sensor ppm;


	public void setup() {
		
		size(450, 460) ;
		noStroke() ;

		cp5 = new ControlP5(this) ;

		cp5.getProperties().addSet("Preset");

		//Alt+mouseDragged to move controllers on the screen
		//Alt+Shift+h to show/hide controllers
		//Alt+Shift+s to save properties (what are properties? have a look at the properties examples)
		//Alt+Shift+l to load properties
		//cp5.enableShortcuts() ;

		oxsI = loadShape("OXSc_Icon.svg") ;
		oxsL = loadShape("OXSc_Logo.svg") ;

		PGraphics icon = createGraphics(64, 64, JAVA2D) ;
		icon.beginDraw() ;
		icon.shape(oxsI, 0, 0, 64, 64) ;
		icon.endDraw() ;
		frame.setIconImage(icon.image) ;
		frame.setTitle("oXs Configurator");

		fontLabel = createFont("arial.ttf", 12, false) ;
		fontItalic = createFont("ariali.ttf", 12, false) ;
		fontCells = createFont("arialbd.ttf", 12, false) ;
		font16 = createFont("arial.ttf", 16, false) ;
		font20 = createFont("arial.ttf", 20, false) ;

		for ( int i = 0; i < analogPins.length; i++ ) {
			analogPins[i] = ("A" + i ) ;
		}

		cp5.setFont(fontLabel, 12) ;

		//message = new Popup(this, cp5);           // TODO later popup




		// ------------------------ TABS definition ------------------------
		// By default all controllers are stored inside Tab 'default'
		cp5.getWindow().setPositionOfTabs(0, 80) ;

		// About
		cp5.addButton("about")
		   .setCaptionLabel("About")
		   .setPosition(380, 14)
		   .setSize(40, 15)
		   .setColorCaptionLabel(0x000000)
		   .setColorBackground(topBottomGray)
		   .setColorForeground(blueAct)
		   .setColorActive(orangeAct)
		   .setTab("global")
		   ;
		cp5.getController("about").getCaptionLabel().toUpperCase(false) ;

		// ----------------------- Tab 0 : GENERAL SETTINGS ----------------------
		tabGenSet = new TabGeneralSettings(this, cp5) ;

		// ---------------- PPM settings : Tabs Vario + Air Speed ----------------
		tabPPM = new TabPPM(this, cp5) ;

		// ---------------------------- Tab 1 : Vario settings ------------------------------
		tabVario = new TabVario(this, cp5) ;

		// ---------------------------- Tab 2 : Air Speed settings ------------------------------
		tabAirSpeed = new TabAirSpeed(this, cp5) ;

		// ------------------------------ Tab 3 : Voltage settings ------------------------------
		tabVoltage = new TabVoltage(cp5) ;

		// ------------------------------ Tab 4 : Current settings ------------------------------
		tabCurrent = new TabCurrent(cp5) ;

		// ------------------------------ Tab 5 : Temperature settings ------------------------------
		//tab5 = new TabTemperature(this, cp5) ;

		// ------------------------------ Tab 6 : RPM settings ------------------------------   needed ?
		cp5.getTab("rpm")
		   .setHeight(20)
		   .setColorForeground(tabGray)
		   .setColorBackground(darkBackGray)
		   .setColorActive(blueAct)
		   .setLabel("RPM")
		   .setId(6)
		   .hide()
		   ;
		cp5.getTab("rpm").getCaptionLabel().toUpperCase(false) ;

		// ------------------------------ Tab 7 : DATA to send ------------------------------
		tabData = new TabData(cp5) ;

		// ------------------------------ File dialog ------------------------------

		// Load preset button
		cp5.addButton("loadButton")
		   .setColorForeground(blueAct)
		   .setCaptionLabel("Load Preset")
		   .setPosition(20, 419)
		   .setSize(100, 25)
		   .setTab("global")
		   ;
		cp5.getController("loadButton").getCaptionLabel().setFont(font16) ;
		cp5.getController("loadButton").getCaptionLabel().toUpperCase(false) ;
		cp5.getController("loadButton").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER) ;

		// Save preset button
		cp5.addButton("saveButton")
		   .setColorForeground(orangeAct)
		   .setColorActive(blueAct)
		   .setCaptionLabel("Save Preset")
		   .setPosition(140, 419)
		   .setSize(100, 25)
		   .setTab("global")
		   ;
		cp5.getController("saveButton").getCaptionLabel().setFont(font16) ;
		cp5.getController("saveButton").getCaptionLabel().toUpperCase(false) ;
		cp5.getController("saveButton").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER) ;


		// Write button
		cp5.addButton("writeConfButton")
		   .setColorForeground(orangeAct)
		   .setColorActive(blueAct)
		   .setCaptionLabel("Write Config")
		   .setPosition(300, 416)
		   .setSize(120, 30)
		   .setTab("data")
		   ;
		cp5.getController("writeConfButton").getCaptionLabel().setFont(font20) ;
		cp5.getController("writeConfButton").getCaptionLabel().toUpperCase(false) ;
		cp5.getController("writeConfButton").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER) ;

		// --------------------------------------------------------------------------

		// dropdownlist overlap
		//cp5.getGroup("tempPin").bringToFront() ;

		// Tooltips general settings
		cp5.getTooltip().setDelay(1000) ;
		cp5.getTooltip().getLabel().toUpperCase(false) ;

		createMessageBox() ;                     	     //  Message box creation
		TabGeneralSettings.getProtocolDdl().setValue(1); // Set the protocol ddl value after telemetry fields creation
		new OXSdata("----------", "----------", "noSensor", null) ;

	}

	public void draw() {

		background(topBottomGray) ;
		// Main screen background
		fill(tabGray) ;
		rect(0, 100, width, 300) ;
		fill(MainP.darkBackGray) ;
		rect(0, 97, width, 3) ;

		// Compatibility subtitle
		fill(0) ;
		textFont(fontLabel) ;
		text("For OXS " + oxsVersion, 75, 65) ;

		// OXS Configurator version display
		textFont(fontItalic) ;
		text(oxsCversion, 377, 68) ;

		// Logo display
		shapeMode(CENTER) ;
		shape(oxsL, width/2, 38, 300, 300) ;

		// File dialog Zone
		fill(topBottomGray) ;
		rect(0, height, width, -60) ;
		fill(darkBackGray) ;
		rect(0, height-60, width, 3) ;

		// Show preset buttons
		cp5.getController("loadButton").show() ;
		cp5.getController("saveButton").show() ;

		// ------------ Tabs specific display ------------

		int currentTabId = cp5.getWindow(this).getCurrentTab().getId() ;

		switch( currentTabId ) {

		case 0 :                                        // TAB GENERAL Settings
			tabGenSet.draw(this);

			break ;

		case 1 :                                                 // TAB Vario

			TabVario.draw(this);

			break ;

		case 2 :                                                            // TAB Air Speed sensor

			stroke(blueAct) ;     // blue border
			strokeWeight(3) ;
			noFill() ;
			rect(4, 106, 442, 148) ;
			line(4, 142, 446, 142) ;
			strokeWeight(1) ;
			noStroke() ;

			TabPPM.drawPPMzone(this) ;

			// separation lines
			stroke(MainP.darkBackGray) ;
			line(10, 184, 440, 184) ;
			noStroke() ;

			if ( cp5.getController("ppm").getValue() == 0 ) {
				cp5.getController("aSpeedReset").lock() ;
				cp5.getController("aSpeedReset").setColorBackground(grayedColor) ;
				cp5.getController("aSpeedReset").setColorForeground(grayedColor) ;
				cp5.getController("aSpeedReset").setColorValueLabel(grayedColor) ;
				cp5.getController("aSpeedReset").setColorCaptionLabel(grayedColor) ;

				cp5.getController("ppmRngCompL").setColorValueLabel(grayedColor) ;
				cp5.getController("ppmRngCompMinMax").lock() ;
				cp5.getController("ppmRngCompMinMax").setColorForeground(grayedColor) ;
				cp5.getController("ppmRngCompMinMax").setColorBackground(grayedColor) ;
				cp5.getController("ppmRngCompMinMax").setColorValueLabel(grayedColor) ;
				cp5.getController("ppmRngCompMinMax").setColorCaptionLabel(grayedColor) ;

				cp5.getController("ppmCompL").setColorValueLabel(grayedColor) ;
				cp5.getController("ppmCompMinMax").lock() ;
				cp5.getController("ppmCompMinMax").setColorForeground(grayedColor) ;
				cp5.getController("ppmCompMinMax").setColorBackground(grayedColor) ;
				cp5.getController("ppmCompMinMax").setColorValueLabel(grayedColor) ;
				cp5.getController("ppmCompMinMax").setColorCaptionLabel(grayedColor) ;

			} else {
				cp5.getController("aSpeedReset").unlock() ;
				cp5.getController("aSpeedReset").setColorBackground(MainP.darkBackGray) ;
				cp5.getController("aSpeedReset").setColorValueLabel(white) ;
				cp5.getController("aSpeedReset").setColorCaptionLabel(color(0)) ;

				cp5.getController("ppmRngCompL").setColorValueLabel(color(0)) ;
				cp5.getController("ppmRngCompMinMax").unlock() ;
				cp5.getController("ppmRngCompMinMax").setColorForeground(blueAct) ;
				cp5.getController("ppmRngCompMinMax").setColorBackground(MainP.darkBackGray) ;
				cp5.getController("ppmRngCompMinMax").setColorValueLabel(white) ;
				cp5.getController("ppmRngCompMinMax").setColorCaptionLabel(color(0)) ;

				cp5.getController("ppmCompL").setColorValueLabel(color(0)) ;
				cp5.getController("ppmCompMinMax").unlock() ;
				cp5.getController("ppmCompMinMax").setColorForeground(blueAct) ;
				cp5.getController("ppmCompMinMax").setColorBackground(MainP.darkBackGray) ;
				cp5.getController("ppmCompMinMax").setColorValueLabel(white) ;
				cp5.getController("ppmCompMinMax").setColorCaptionLabel(color(0)) ;
			}

			break ;

		case 3 :                                                 // TAB Voltage / Other

			TabVoltage.draw(this);
			break ;

		case 4 :                                                            // TAB Current sensor
			/*
		    if ( cp5.getController("currentDir").value() == 0 ) {             // Current grayed switch
		      cp5.getController("currentDirL").setColorValue(grayedColor) ;
		      cp5.getController("currentDir").setColorCaptionLabel(color(0)) ;
		    } else {
		      cp5.getController("currentDirL").setColorValue(color(0)) ;
		      cp5.getController("currentDir").setColorCaptionLabel(grayedColor) ;
		    }
			 */
			break ;

		case 7 :                                                            // TAB DATA sent  dataSentDdlOpen
			fill(10) ;
			rect(298, 414, 124, 34) ;

			for ( int i = 1 ; i <= TabData.getDataSentFieldNbr() ; i++ ) {            // Load and Save preset buttons hide
				if ( TabData.getSentDataField(i).isOpen() || TabData.getTargetDataField(i).isOpen() ) {
					cp5.getController("loadButton").hide() ;
					cp5.getController("saveButton").hide() ;
					break ;
				} else {
					cp5.getController("loadButton").show() ;
					cp5.getController("saveButton").show() ;
				}
			}
			/*
		    // Grayed multiplier + divider + offset if Telemetry data field == DEFAULT
		    for ( int i = 1 ; i <= dataSentFieldNbr ; i++ ) {
		      if ( cp5.getGroup("protocolChoice").value() == 1 ) {
		        if ( cp5.getGroup("hubDataField" + i).value() == 1 ) {
		          cp5.getController("dataMultiplier" + i).lock() ;
		          cp5.getController("dataMultiplier" + i).setColorBackground(grayedColor) ;
		          cp5.getController("dataMultiplier" + i).setColorValueLabel(grayedColor) ;
		          cp5.getController("dataDivider" + i).lock() ;
		          cp5.getController("dataDivider" + i).setColorBackground(grayedColor) ;
		          cp5.getController("dataDivider" + i).setColorValueLabel(grayedColor) ;
		          cp5.getController("dataOffset" + i).lock() ;
		          cp5.getController("dataOffset" + i).setColorBackground(grayedColor) ;
		          cp5.getController("dataOffset" + i).setColorValueLabel(grayedColor) ;
		        } else {
		          cp5.getController("dataMultiplier" + i).unlock() ;
		          cp5.getController("dataMultiplier" + i).setColorBackground(OXSConfigurator._Gray) ;
		          cp5.getController("dataMultiplier" + i).setColorValueLabel(white) ;
		          cp5.getController("dataDivider" + i).unlock() ;
		          cp5.getController("dataDivider" + i).setColorBackground(OXSConfigurator._Gray) ;
		          cp5.getController("dataDivider" + i).setColorValueLabel(white) ;
		          cp5.getController("dataOffset" + i).unlock() ;
		          cp5.getController("dataOffset" + i).setColorBackground(OXSConfigurator._Gray) ;
		          cp5.getController("dataOffset" + i).setColorValueLabel(white) ;
		        }
		      } else if ( cp5.getGroup("protocolChoice").value() == 2 ) {
		        if ( TabData.targetDataField[i].value() == 1 ) {
		          cp5.getController("dataMultiplier" + i).lock() ;
		          cp5.getController("dataMultiplier" + i).setColorBackground(grayedColor) ;
		          cp5.getController("dataMultiplier" + i).setColorValueLabel(grayedColor) ;
		          cp5.getController("dataDivider" + i).lock() ;
		          cp5.getController("dataDivider" + i).setColorBackground(grayedColor) ;
		          cp5.getController("dataDivider" + i).setColorValueLabel(grayedColor) ;
		          cp5.getController("dataOffset" + i).lock() ;
		          cp5.getController("dataOffset" + i).setColorBackground(grayedColor) ;
		          cp5.getController("dataOffset" + i).setColorValueLabel(grayedColor) ;
		        } else {
		          cp5.getController("dataMultiplier" + i).unlock() ;
		          cp5.getController("dataMultiplier" + i).setColorBackground(OXSConfigurator._Gray) ;
		          cp5.getController("dataMultiplier" + i).setColorValueLabel(white) ;
		          cp5.getController("dataDivider" + i).unlock() ;
		          cp5.getController("dataDivider" + i).setColorBackground(OXSConfigurator._Gray) ;
		          cp5.getController("dataDivider" + i).setColorValueLabel(white) ;
		          cp5.getController("dataOffset" + i).unlock() ;
		          cp5.getController("dataOffset" + i).setColorBackground(OXSConfigurator._Gray) ;
		          cp5.getController("dataOffset" + i).setColorValueLabel(white) ;
		        }
		      }
		    }
			 */
			break ;
		}

		// ---------------- End TAB specific display ---------------

		// Load and Save preset buttons deco
		if ( cp5.getController("loadButton").isVisible() ) {
			fill(blueAct) ;
			rect(19, 418, 102, 27) ;
		}

		if ( cp5.getController("saveButton").isVisible() ) {
			fill(orangeAct) ;
			rect(139, 418, 102, 27) ;
		}

		// ----------------- Texfield and Numberbox mouse-over -----------------

		if ( cp5.isMouseOver( tabGenSet.getOxsDir() )  ) {
			tabGenSet.getOxsDir().setColorForeground(blueAct) ;
		} else {
			tabGenSet.getOxsDir().setColorForeground(tabGray) ;
		}

		if ( cp5.isMouseOver ( cp5.getController("arduinoVccNb") ) ) {
			cp5.getController("arduinoVccNb").setColorForeground(blueAct) ;
		} else {
			cp5.getController("arduinoVccNb").setColorForeground(grayedColor) ;
		}

		if ( cp5.isMouseOver ( cp5.getController("ppmRngMin") ) ) {
			cp5.getController("ppmRngMin").setColorForeground(orangeAct) ;
		} else {
			cp5.getController("ppmRngMin").setColorForeground(grayedColor) ;
		}

		if ( cp5.isMouseOver ( cp5.getController("ppmRngMax") ) ) {
			cp5.getController("ppmRngMax").setColorForeground(orangeAct) ;
		} else {
			cp5.getController("ppmRngMax").setColorForeground(grayedColor) ;
		}

		if ( cp5.isMouseOver ( cp5.getController("vSpeedMin") ) ) {
			cp5.getController("vSpeedMin").setColorForeground(orangeAct) ;
		} else {
			cp5.getController("vSpeedMin").setColorForeground(grayedColor) ;
		}

		if ( cp5.isMouseOver ( cp5.getController("vSpeedMax") ) ) {
			cp5.getController("vSpeedMax").setColorForeground(orangeAct) ;
		} else {
			cp5.getController("vSpeedMax").setColorForeground(grayedColor) ;
		}

		if ( cp5.isMouseOver ( cp5.getController("ppmVspeedSwMin") ) ) {
			cp5.getController("ppmVspeedSwMin").setColorForeground(orangeAct) ;
		} else {
			cp5.getController("ppmVspeedSwMin").setColorForeground(grayedColor) ;
		}

		if ( cp5.isMouseOver ( cp5.getController("ppmVspeedSwMax") ) ) {
			cp5.getController("ppmVspeedSwMax").setColorForeground(orangeAct) ;
		} else {
			cp5.getController("ppmVspeedSwMax").setColorForeground(grayedColor) ;
		}

		if ( cp5.isMouseOver ( cp5.getController("aSpeedReset") ) ) {
			cp5.getController("aSpeedReset").setColorForeground(orangeAct) ;
		} else {
			cp5.getController("aSpeedReset").setColorForeground(grayedColor) ;
		}

		for ( int i = 1; i <= TabVoltage.getVoltnbr(); i++ ) {
			if ( cp5.isMouseOver ( cp5.getController( "dividerVolt" + i ) ) ) {
				cp5.getController( "dividerVolt" + i ).setColorForeground(orangeAct) ;
			} else {
				cp5.getController( "dividerVolt" + i ).setColorForeground(color(170)) ;
			}
		}

		for ( int i = 1; i <= TabVoltage.getVoltnbr(); i++ ) {
			if ( cp5.isMouseOver ( cp5.getController( "offsetVolt" + i ) ) ) {
				cp5.getController( "offsetVolt" + i ).setColorForeground(orangeAct) ;
			} else {
				cp5.getController( "offsetVolt" + i ).setColorForeground(color(170)) ;
			}
		}
		/*
		  if ( cp5.isMouseOver ( cp5.getController( "currentVccNb" ) ) ) {
		    cp5.getController( "currentVccNb" ).setColorForeground(orangeAct) ;
		  } else {
		    cp5.getController( "currentVccNb" ).setColorForeground(color(170)) ;
		  }
		 */

		if ( cp5.isMouseOver ( cp5.getController( "currentOutSensNb" ) ) ) {
			cp5.getController( "currentOutSensNb" ).setColorForeground(orangeAct) ;
		} else {
			cp5.getController( "currentOutSensNb" ).setColorForeground(color(170)) ;
		}

		if ( cp5.isMouseOver ( cp5.getController( "currentOutOffsetNb" ) ) ) {
			cp5.getController( "currentOutOffsetNb" ).setColorForeground(orangeAct) ;
		} else {
			cp5.getController( "currentOutOffsetNb" ).setColorForeground(color(170)) ;
		}

		if ( cp5.isMouseOver ( cp5.getController( "currentOutOffsetMA" ) ) ) {
			cp5.getController( "currentOutOffsetMA" ).setColorForeground(orangeAct) ;
		} else {
			cp5.getController( "currentOutOffsetMA" ).setColorForeground(color(170)) ;
		}

		if ( cp5.isMouseOver ( cp5.getController( "currentDivNb" ) ) ) {
			cp5.getController( "currentDivNb" ).setColorForeground(orangeAct) ;
		} else {
			cp5.getController( "currentDivNb" ).setColorForeground(color(170)) ;
		}
		/*
		  if ( cp5.isMouseOver ( cp5.getController( "tempOffset" ) ) ) {
		    cp5.getController( "tempOffset" ).setColorForeground(orangeAct) ;
		  } else {
		    cp5.getController( "tempOffset" ).setColorForeground(OXSConfigurator.tabGray) ;
		  }
		 */
		// TODO in 1 loop ?? later
		for ( int i = 1; i <= TabData.getDataSentFieldNbr(); i++ ) {
			if ( cp5.isMouseOver ( cp5.getController( "dataMultiplier" + i ) ) ) {
				cp5.getController( "dataMultiplier" + i ).setColorForeground(orangeAct) ;
			} else {
				cp5.getController( "dataMultiplier" + i ).setColorForeground(grayedColor) ;
			}
		}

		for ( int i = 1; i <= TabData.getDataSentFieldNbr(); i++ ) {
			if ( cp5.isMouseOver ( cp5.getController( "dataDivider" + i ) ) ) {
				cp5.getController( "dataDivider" + i ).setColorForeground(orangeAct) ;
			} else {
				cp5.getController( "dataDivider" + i ).setColorForeground(grayedColor) ;
			}
		}

		for ( int i = 1; i <= TabData.getDataSentFieldNbr(); i++ ) {
			if ( cp5.isMouseOver ( cp5.getController( "dataOffset" + i ) ) ) {
				cp5.getController( "dataOffset" + i ).setColorForeground(orangeAct) ;
			} else {
				cp5.getController( "dataOffset" + i ).setColorForeground(grayedColor) ;
			}
		}

		// ----------------- Dropdownlist: mouse pressed elsewhere closes list -----------------

		if ( !cp5.isMouseOver ( TabGeneralSettings.getProtocolDdl() ) ) {
			if (mousePressed == true) {
				TabGeneralSettings.getProtocolDdl().close() ;
			}
		}

		if ( !cp5.isMouseOver ( TabGeneralSettings.getSerialPinDdl() ) ) {
			if (mousePressed == true) {
				TabGeneralSettings.getSerialPinDdl().close() ;
			}
		}

		if ( !cp5.isMouseOver ( cp5.getGroup( "sensorID" ) ) ) {
			if (mousePressed == true) {
				cp5.getGroup( "sensorID" ).close() ;
			}
		}

		if ( !cp5.isMouseOver ( cp5.getGroup( "voltRefChoice" ) ) ) {
			if (mousePressed == true) {
				cp5.getGroup( "voltRefChoice" ).close() ;
			}
		}

		if ( !cp5.isMouseOver ( cp5.getGroup( "resetButtonPin" ) ) ) {
			if (mousePressed == true) {
				cp5.getGroup( "resetButtonPin" ).close() ;
			}
		}

		if ( !cp5.isMouseOver ( TabPPM.getPpmPin() ) ) {
			if (mousePressed == true) {
				TabPPM.getPpmPin().close() ;
			}
		}

		if ( !cp5.isMouseOver ( cp5.getGroup( "vSpeed1" ) ) ) {
			if (mousePressed == true) {
				cp5.getGroup( "vSpeed1" ).close() ;
			}
		}

		if ( !cp5.isMouseOver ( cp5.getGroup( "vSpeed2" ) ) ) {
			if (mousePressed == true) {
				cp5.getGroup( "vSpeed2" ).close() ;
			}
		}

		if ( !cp5.isMouseOver ( TabVario.getClimbPin() ) ) {
			if (mousePressed == true) {
				TabVario.getClimbPin().close() ;
			}
		}

		for ( int i = 1; i <= TabVoltage.getVoltnbr(); i++ ) {
			if ( !cp5.isMouseOver ( cp5.getGroup( "ddlVolt" + i ) ) ) {
				if (mousePressed == true) {
					cp5.getGroup( "ddlVolt" + i ).close() ;
				}
			}
		}

		if (!cp5.isMouseOver(TabVoltage.getDdlNbrCells())) {
			if (mousePressed == true) {
				TabVoltage.getDdlNbrCells().close();
			}
		}

		if ( !cp5.isMouseOver ( cp5.getGroup( "currentPin") ) ) {
			if (mousePressed == true) {
				cp5.getGroup( "currentPin" ).close() ;
			}
		}
		/*
		  if ( !cp5.isMouseOver ( cp5.getGroup( "tempPin") ) ) {
		    if (mousePressed == true) {
		      cp5.getGroup( "tempPin" ).close() ;
		    }
		  }
		 */
		for ( int i = 1; i <= TabData.getDataSentFieldNbr(); i++ ) {
			if ( !cp5.isMouseOver ( TabData.getSentDataField(i) ) ) {
				if (mousePressed == true) {
					TabData.getSentDataField(i).close() ;
				}
			}
		}

		/*
		  for ( int i = 1; i <= dataSentFieldNbr; i++ ) {
		    if ( !cp5.isMouseOver ( cp5.getGroup( "hubDataField" + i ) ) ) {
		      if (mousePressed == true) {
		        cp5.getGroup( "hubDataField" + i ).close() ;
		      }
		    }
		  }
		 */

		for ( int i = 1; i <= TabData.getDataSentFieldNbr(); i++ ) {
			if ( !cp5.isMouseOver ( TabData.getTargetDataField(i) ) ) {
				if (mousePressed == true) {
					TabData.getTargetDataField(i).close() ;
				}
			}
		}

		// ----------------- TAB DATA sent display -----------------

		if (vario != null || airSpeed != null
				|| cp5.getController("voltage").getValue() == 1
				|| current != null /* || temperature != null */|| rpm != null) {
			cp5.getTab("data").show();
		} else {
			cp5.getTab("data").hide();
		}

	}

	public void controlEvent(ControlEvent theEvent) {
		// DropdownList is of type ControlGroup.
		// A controlEvent will be triggered from inside the ControlGroup class.
		// therefore you need to check the originator of the Event with if (theEvent.isGroup()) to avoid an error message thrown by controlP5.

		if ( theEvent.isFrom(cp5.getTab("vario")) ) {           //  Tab vario : display PPM parameters
			cp5.getController("ppm").setTab("vario") ;
			cp5.getController("ppm").show() ;
			cp5.getController("ppmPinL").setTab("vario") ;
			cp5.getController("ppmPinL").show() ;
			cp5.getController("ppmRngL").setTab("vario") ;
			cp5.getController("ppmRngL").show() ;
			cp5.getController("ppmRngMin").setTab("vario") ;
			cp5.getController("ppmRngMin").show() ;
			cp5.getController("ppmRngMax").setTab("vario") ;
			cp5.getController("ppmRngMax").show() ;
			cp5.getGroup("ppmPin").setTab("vario") ;
		} else if ( theEvent.isFrom(cp5.getTab("airSpeed")) ) {  //  Tab Air Speed : display PPM parameters
			cp5.getController("ppm").setTab("airSpeed") ;
			cp5.getController("ppm").show() ;
			cp5.getController("ppmPinL").setTab("airSpeed") ;
			cp5.getController("ppmPinL").show() ;
			cp5.getController("ppmRngL").setTab("airSpeed") ;
			cp5.getController("ppmRngL").show() ;
			cp5.getController("ppmRngMin").setTab("airSpeed") ;
			cp5.getController("ppmRngMin").show() ;
			cp5.getController("ppmRngMax").setTab("airSpeed") ;
			cp5.getController("ppmRngMax").show() ;
			cp5.getGroup("ppmPin").setTab("airSpeed") ;
		}

		if ( theEvent.isFrom(cp5.getController("vSpeedMax")) || theEvent.isFrom(cp5.getController("vSpeedMin")) ) {   //  V speed sensitivity range interaction
			cp5.getController("vSpeedMax").setBroadcast(false) ;
			cp5.getController("vSpeedMin").setBroadcast(false) ;
			cp5.get(Numberbox.class, "vSpeedMax").setRange( cp5.getController("vSpeedMin").getValue(), 1000 ) ;
			cp5.get(Numberbox.class, "vSpeedMin").setRange( 0, cp5.getController("vSpeedMax").getValue() ) ;
			cp5.getController("vSpeedMin").setBroadcast(true) ;
			cp5.getController("vSpeedMax").setBroadcast(true) ;
		}

		// Voltages sensor activation/desactivation
		for (int i = 1; i <= TabVoltage.getVoltnbr(); i++) {
			if (theEvent.isFrom(cp5.getController("volt" + i))) {
				switch ((int) theEvent.getController().getValue()) {
				case 1:
					if (aVolt[i] == null) {
						aVolt[i] = new Volt(this, cp5, "volt" + i);
						TabVoltage.populateNbrCells();
					}
					break;
				case 0:
					if (aVolt[i] != null) {
						aVolt[i].removeSensor();
						aVolt[i] = null;
						TabVoltage.populateNbrCells();
						if ( i == 1 ) {
							cp5.getController("cells").setValue(0);
						}
					}
					break;
				}
			}
		}

		//  Current sensor output offset interaction
		if ( theEvent.isFrom(cp5.getController("currentOutOffsetNb") ) /*|| theEvent.isFrom(cp5.getController("currentOutSensNb"))*/ ) {
			cp5.getController("currentOutOffsetMA").setBroadcast(false) ;
			float currentOutSens = cp5.getController("currentOutSensNb").getValue() ;
			float currentOutOffsetMV = cp5.getController("currentOutOffsetNb").getValue() ;
			cp5.getController("currentOutOffsetMA").setValue( ( currentOutOffsetMV / currentOutSens ) * 1000 ) ;
			cp5.getController("currentOutOffsetMA").setBroadcast(true) ;
		}
		if ( theEvent.isFrom(cp5.getController("currentOutOffsetMA")) ) {
			cp5.getController("currentOutOffsetNb").setBroadcast(false) ;
			float currentOutSens = cp5.getController("currentOutSensNb").getValue() ;
			float currentOutOffsetMA = cp5.getController("currentOutOffsetMA").getValue() ;
			cp5.getController("currentOutOffsetNb").setValue( ( currentOutOffsetMA / 1000 ) * currentOutSens ) ;
			cp5.getController("currentOutOffsetNb").setBroadcast(true) ;
		}

		// Protocol selection - Showing right Telemetry data list in fields
		if (theEvent.isFrom(TabGeneralSettings.getProtocolDdl())) {
			switch ((int) theEvent.getGroup().getValue()) {
			case 1:
					protocol = Protocol.createProtocol("FrSky");
				break;
			case 2:
					protocol = Protocol.createProtocol("Multiplex");
				break;
			}
		}

		// Selecting DEFAULT automatically in Telemetry data fields
		// TODO group controllers ?
		for (int i = 1; i <= TabData.getDataSentFieldNbr(); i++) {

			if (theEvent.isFrom(TabData.getSentDataField(i))) {

				switch (TabData.getSentDataField(i).getCaptionLabel().getText()) {
				case "Altitude":
				case "Vertical Speed":
				case "Altitude 2":
				case "Vertical Speed 2":
				case "Air Speed":
				case "Prandtl dTE":
				case "PPM V.Speed":
				case "Current (mA)":
				case "Consumption (mAh)":
				case "Cells monitoring":
				case "RPM":
					TabData.getTargetDataField(i).setValue(1);
					break;
				}

				/*
				 * println("oxsdataList : " +
				 * TabData.getSentDataField(i).getCaptionLabel() .getText());
				 */
			}
		}
		/*
		  if (theEvent.isGroup()) {
		    // check if the Event was triggered from a ControlGroup
		    println("event from group : "+theEvent.getGroup().getValue()+" from "+theEvent.getGroup()) ;
		  } else if (theEvent.isController()) {
		    println("event from controller : "+theEvent.getController().getValue()+" from "+theEvent.getController()) ;
		  }

		  if (theEvent.isTab()) {
		    // check if the Event was triggered from a Tab
		    println("event from tab : " + theEvent.getTab().getName() ) ;
		  }
		 */
	}

	public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {  // Mouse wheel support for scroll bars !!
		super.mouseWheelMoved(e) ;
		cp5.setMouseWheelRotation(e.getWheelRotation()) ;
	}

	void oxsDirButton(int theValue) {
		//println("oxsDir button: "+theValue) ;
		selectFolder("Select OXS source folder:", "folderSelected") ;
	}

	public void vario(boolean theFlag) {
		if (theFlag==true && vario == null) {
			vario = new Vario(this, cp5, "vario") ;
			cp5.getTab("vario").show() ;

		} else if (theFlag==false && vario != null) {
			if ( vario2 != null )
				//vario2.removeSensor() ;
				//vario2 = null ;
				cp5.getController("vario2").setValue(0) ;
			if ( ppm != null && airSpeed == null ) {
				cp5.getController("ppm").setValue(0) ;
				//ppm.removeSensor() ;
				//ppm = null ;      
			}
			vario.removeSensor() ;
			vario = null ;
			cp5.getTab("vario").hide() ;
		}
		//println("a toggle event.") ;
	}

	void vario2(boolean theFlag) {
		if (theFlag==true && vario2 == null) {
			vario2 = new Vario(this, cp5, "vario2") ;
			//cp5.getTab("vario").show() ;

		} else if ( theFlag==false && vario2 != null ) {
			vario2.removeSensor() ;
			vario2 = null ;
			//cp5.getTab("vario").hide() ;
			//cp5.getController("vario2").setValue(0) ;
		}
		//println("a toggle event.") ;
	}

	void airSpeed(boolean theFlag) {
		if (theFlag==true && airSpeed == null) {
			airSpeed = new AirSpeed(this, cp5, "airSpeed") ;
			cp5.getTab("airSpeed").show() ;
		} else if ( theFlag==false && airSpeed != null ) {
			airSpeed.removeSensor() ;
			airSpeed = null ;
			cp5.getTab("airSpeed").hide() ;
			if ( ppm != null && vario == null )
				cp5.getController("ppm").setValue(0) ;

		}
	}

	void voltage(boolean theFlag) {
		if (theFlag == true) {
			cp5.getTab("voltage").show();
		} else {
			for (int i = 1; i <= TabVoltage.getVoltnbr(); i++) {
				cp5.getController("volt" + i).setValue(0);
			}
			cp5.getTab("voltage").hide();
		}
	}

	void cells(boolean theFlag) {  // TODO clean
		if ( theFlag == true && aVolt[1] != null ) {
			new OXSdata("CELLS", "Cells monitoring", "voltCells", null) ;
			TabData.populateSentDataFields() ;
		} else {
			OXSdata.removeFromList("voltCells") ;  // TODO remove "Cells Monotoring" from ddl display
			TabData.resetSentDataFields() ;
//			TabData.populateSentDataFields() ;
		}
	}

	void current(boolean theFlag) {
		if (theFlag == true && current == null) {
			current = new Current(this, cp5, "current");
			cp5.getTab("current").show();
		} else if (theFlag == false && current != null) {
			current.removeSensor();
			current = null;
			cp5.getTab("current").hide();
		}
	}

	void temperature(boolean theFlag) {
		if (theFlag==true) {
			cp5.getTab("temperature").show() ;
		} else {
			cp5.getTab("temperature").hide() ;
		}
		//println("a toggle event.") ;
	}

	// RPM TAB display
	void rpm(boolean theFlag) {
		if (theFlag==true && rpm == null) {
			rpm = new Rpm(this, cp5, "rpm") ;
			//cp5.getTab("rpm").show() ;
		} else if ( theFlag==false && rpm != null ) {
			rpm.removeSensor() ;
			rpm = null ;
			//cp5.getTab("rpm").hide() ;
		}
		//println("a toggle event.") ;
	}

	void ppm(boolean theFlag) {
		if (theFlag == true && ppm == null) {
			ppm = new PPM(this, cp5, "ppm");
		} else if (theFlag == false && ppm != null) {
			ppm.removeSensor();
			ppm = null;
		}
	}

	void about(boolean theFlag) {

		mbClose() ;

		messageList.clear() ;

		messageList.append( "                            OXS Configurator " + oxsCversion + " for OXS " + oxsVersion ) ;
		messageList.append( "                                                       ---" ) ;
		messageList.append( "                         -- OpenXsensor configuration file GUI --" ) ;
		messageList.append( "\n" ) ;
		messageList.append( "Contributors:" ) ;
		messageList.append( "" ) ;
		messageList.append( "- Rainer Schloßhan" ) ;
		messageList.append( "- Bertrand Songis" ) ;
		messageList.append( "- André Bernet" ) ;
		messageList.append( "- Michael Blandford" ) ;
		messageList.append( "- Michel Strens" ) ;
		messageList.append( "- David Laburthe" ) ;
		messageList.append( "" ) ;
		messageList.append( "" ) ;

		String[] messageListArray = messageList.array() ;

		String joinedMessageList = join(messageListArray, "\n") ;

		cp5.get(Textarea.class, "messageBoxLabel").setText(joinedMessageList) ;

		cp5.getController("buttonOK").setColorForeground(orangeAct) ;
		cp5.getController("buttonOK").setColorBackground(color(100)) ;
		cp5.getController("buttonOK").setColorActive(blueAct) ;
		messageBox.setBackgroundColor(blueAct) ;
		messageBox.show() ;
	}

	public void loadButton(int theValue) {                                     // Load preset button
		File presetDir = new File( sketchPath("Preset/...") ) ;
		selectInput("Select a preset file to load:", "presetLoad", presetDir) ;
	}

	public void saveButton(int theValue) {                                     // Save preset button
		mbClose() ;
		validationProcess("preset") ;
		if ( allValid == 2 ) {
			messageBox.hide() ;
		}
		if ( allValid != 0 ) {
			File presetDir = new File( ("src/Preset/type name") ) ;  // sketchPath("Preset/type name")
			selectOutput("Type preset name to save:", "presetSave", presetDir) ;
		}
	}

	public void presetLoad(File selection) { // TODO preset load
		//PresetManagement.presetLoad(selection);
		/*if (selection == null) {
			//println("Window was closed or the user hit cancel.") ;
		} else {
			//println("User selected " + selection.getAbsolutePath()) ;
			//cp5.setBroadcast(false);
			cp5.loadProperties(selection.getAbsolutePath()) ;

			// Hack to keep slider labels alignement
			cp5.getController("varioHysteresis").getCaptionLabel().align(ControlP5.LEFT_OUTSIDE, ControlP5.CENTER).setPaddingX(10) ;
			cp5.getController("varioHysteresis").getValueLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(10) ;
			//cp5.setBroadcast(true);
		}*/
	}

	public void presetSave(File selection) { // TODO preset save
		if (selection == null) {
			//println("Window was closed or the user hit cancel.") ;
		} else {
			println("User selected " + selection.getAbsolutePath()) ;
			cp5.saveProperties(selection.getAbsolutePath()) ;
		}
	}

	public void writeConfButton(int theValue) {
		mbOkCancel() ;
		validationProcess("Config") ;
		if ( allValid == 0) {
			mbClose() ;
		}
	}

	public void validationProcess(String theString) {

		// Config file writing destination
		oxsDirectory = trim( cp5.get(Textfield.class, "oxsDirectory").getText() ) ;
		if ( oxsDirectory.equals("") ) {
			outputConfigDir = sketchPath("oXs_config.h") ;
		} else {
			outputConfigDir = oxsDirectory + "/oXs_config.h" ;
		}

		messageList.clear() ;
		messageList.set(0, "") ;
		messageList.append("") ;

		numPinsValid = true ;
		analogPinsValid = true ;
		vSpeedValid = 2 ;           // 0 -> not valid    1 -> warning   2 -> valid
		cellsValid = true ;
		sentDataValid = true ;
		versionValid = 2 ;          // 0 -> not valid    1 -> warning   2 -> valid

		Validation.validateNumPins() ;
		Validation.validateAnalogPins() ;
		Validation.validateVspeed() ;
		Validation.validateCells() ;

		if ( theString.equals("Config") ) {
			Validation.validateSentData() ;
			if ( numPinsValid && analogPinsValid && vSpeedValid != 0 && cellsValid && sentDataValid )
				try {
					Validation.validateVersion() ;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		if ( !numPinsValid || !analogPinsValid || vSpeedValid == 0 || !cellsValid || !sentDataValid || versionValid == 0 ) {

			messageBox.setBackgroundColor(errorColor) ;

			messageList.set(0, "                                              --- ERROR ---") ;
			messageList.append("") ;
			messageList.append("                                             ----------------------") ;
			messageList.append("") ;
			if ( theString.equals("preset") ) {
				messageList.append("Preset file can't be saved !") ;
			} else {
				messageList.append("Config file can't be written !") ;
			}
			//cp5.get(Textarea.class, "messageBoxLabel").setColor(color(255,0,0)) ;
			allValid = 0 ;

		} else if ( vSpeedValid == 1 || versionValid == 1 ) {

			messageBox.setBackgroundColor(warnColor) ;

			messageList.set(0, "                                           ----  WARNING  ----") ;
			messageList.append("") ;
			messageList.append("                                             ----------------------") ;
			messageList.append("") ;
			if ( theString.equals("Config") ) {
				messageList.append("Configuration file will be written to:") ;
				messageList.append(outputConfigDir) ;
				messageList.append("") ;
				messageList.append("                       ! If the file already exists, it will be replaced !") ;
			}

			allValid = 1 ;

		} else {

			messageBox.setBackgroundColor(okColor) ;

			messageList.set(0, "                                         --- ALL IS GOOD ! ---") ;
			if ( theString.equals("preset") ) {
				messageList.append("Preset file can be saved !") ;
			}
			messageList.append("") ;
			messageList.append("                                             ----------------------") ;

			allValid = 2 ;
		}

		String[] messageListArray = messageList.array() ;

		String joinedMessageList = join(messageListArray, "\n") ;

		cp5.get(Textarea.class, "messageBoxLabel").setText(joinedMessageList) ;
		//println(messageList) ;

		//messageBox.setBackgroundColor(color(240)) ;
		cp5.getController("buttonOK").setColorForeground(color(blueAct)) ;
		cp5.getController("buttonOK").setColorActive(color(orangeAct)) ;
		messageBox.show() ;

	}

	public void folderSelected(File selection) {
		if (selection == null) {
			//println("Window was closed or the user hit cancel.") ;
		} else {
			//println("User selected " + selection.getAbsolutePath()) ;
			cp5.get(Textfield.class, "oxsDirectory").setText(selection.getAbsolutePath()) ;
		}
	}

	// =========================================== Shortcuts ===========================================

	public void keyPressed() {
		// default properties load/save key combinations are
		// alt+shift+l to load properties
		// alt+shift+s to save properties
		if (key=='s') {
			//cp5.saveProperties(("settings.oxs")) ;
			savePreset();
			cp5.getProperties().print() ;
		} else if (key=='l') {
			//cp5.loadProperties(("settings.oxs")) ;
			loadPreset();
			// Hack to keep slider labels alignment
			//cp5.getController("varioHysteresis").getCaptionLabel().align(ControlP5.LEFT_OUTSIDE, ControlP5.CENTER).setPaddingX(10) ;
			//cp5.getController("varioHysteresis").getValueLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(10) ;

			cp5.getProperties().print() ;
		} else if ( key == 'c' ) {
			println( "mAmp / step " + mAmpStep() ) ;
			println( "Current offset " + offsetCurrent() ) ;
		} else if ( key == 'p' ) {
			//message.showOk("gxb");
		} else if ( key == 'g' ) {

		} else if ( key == 'd' ) {
			vario = null ;
			//println(vario.getMeasurementName(0)) ; 
		}
	}

	// =================================================================================================

	float round(float number, float decimal) {      // Rounding function
		return (float)(round((number*pow(10, decimal))))/pow(10, decimal) ;
	}

	float mVoltStep(int NbrVolt) {    // Voltage measurements milliVolt per ADC step calculation

		float mVoltStep ;
		float voltageDiv = cp5.getController("dividerVolt" + NbrVolt ).getValue() ;
		float arduinoVcc = cp5.getController("arduinoVccNb").getValue() ;

		if ( cp5.get(DropdownList.class, "voltRefChoice").getValue() == 1 ) {
			mVoltStep = (float) (( arduinoVcc * 1000.0 / 1024.0 ) * voltageDiv) ;
		} else {
			mVoltStep = (float) (( 1.1 * 1000.0 / 1024.0 ) * voltageDiv) ;
		}
		return mVoltStep ;
	}

	float mAmpStep() {    // Current sensor milliAmp per ADC step calculation

		float mAmpStep ;
		float mAmpPmV ;
		float arduinoVcc = cp5.getController("arduinoVccNb").getValue() ;
		float currentDiv = cp5.getController("currentDivNb").getValue() ;
		float currentOutSens = cp5.getController("currentOutSensNb").getValue() ;

		mAmpPmV = (float) (( currentOutSens == 0 ) ? 0 : 1000.0 / currentOutSens) ;
		if ( cp5.get(DropdownList.class, "voltRefChoice").getValue() == 1 ) {
			mAmpStep = (float) (( arduinoVcc * 1000.0 / 1024.0 ) * mAmpPmV * currentDiv) ;
		} else {
			mAmpStep = (float) (( 1.1 * 1000.0 / 1024.0 ) * mAmpPmV) ;
		}
		return mAmpStep ;
	}

	int offsetCurrent() {    // Current sensor offset calculation in ADC step

		int offsetCurrent ;
		//float currentVcc = cp5.getController("currentVccNb").getValue() ;
		float currentOutOffset = cp5.getController("currentOutOffsetNb").getValue() ;
		float currentDiv = cp5.getController("currentDivNb").getValue() ;
		float arduinoVcc = cp5.getController("arduinoVccNb").getValue() ;

		//if ( cp5.getController( "currentDir" ).value() == 0 ) {
		//  offsetCurrent = int( ( currentVcc / 2.0 + currentOutOffset / 1000.0 ) / arduinoVcc  * 1024.0 * currentDiv ) ;
		//} else {
		offsetCurrent = (int) ( currentOutOffset / 1000.0 / arduinoVcc  * 1024.0 * currentDiv ) ;
		//}

		return offsetCurrent ;
	}
		
	// Customize functions

	void customizeSlider(Controller<Slider> sld) {
		sld.setColorForeground(blueAct) ;
		sld.setColorCaptionLabel(0) ;
		sld.setColorValueLabel(0) ;
		sld.setSize(150, 15) ;
		sld.setValue(50) ;
		sld.setTab("vario") ;
		// reposition the Labels for controller 'slider'
		sld.getCaptionLabel().align(ControlP5.LEFT_OUTSIDE, ControlP5.CENTER).setPaddingX(10) ;
		sld.getValueLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(10) ;

		sld.getCaptionLabel().toUpperCase(false) ;
	}

	public static void customizeToggle(Controller<?> tgl) {
		tgl.setColorForeground(orangeAct) ;
		tgl.setColorBackground(MainP.darkBackGray) ;
		tgl.setColorActive(blueAct) ;
		tgl.setColorCaptionLabel(0) ;
		tgl.setSize(15, 15) ;

		// reposition the Labels
		tgl.getCaptionLabel().align(ControlP5.LEFT_OUTSIDE, ControlP5.CENTER).setPaddingX(10) ;

		tgl.getCaptionLabel().toUpperCase(false) ;
	}

	public static void customizeRange(Controller<?> rng) {
		rng.setColorForeground(blueAct) ;
		rng.setColorBackground(MainP.darkBackGray) ;
		rng.setColorActive(orangeAct) ;
		//rng.setSize(200, 20) ;
		rng.setColorCaptionLabel(0) ;
		rng.setTab("vario") ;

		//rng.getCaptionLabel().align(ControlP5.LEFT_OUTSIDE, ControlP5.CENTER) ;
		rng.getCaptionLabel().toUpperCase(false) ;
	}

	public void createMessageBox() {

		// create a group to store the messageBox elements
		messageBox = cp5.addGroup("messageBox", width / 2 - mBoxWidth / 2, 76, mBoxWidth)
				.setBackgroundHeight(mBoxHeight)
				.setBackgroundColor(color(240))
				.setTab("global")
				.hideBar()
				.hide()
				;

		// add a Textaera to the messageBox.
		cp5.addTextarea("messageBoxLabel")
		.setPosition(5,5)
		.setSize(mBoxWidth - 10, mBoxHeight - 48)
		.setLineHeight(14)
		.setColor(white)
		.setColorActive(orangeAct)
		//.setBorderColor(color(0))
		.setColorBackground(color(120))
		.setColorForeground(blueAct)
		.setScrollBackground(color(80))
		//.setTab("global")
		;
		cp5.get(Textarea.class, "messageBoxLabel").moveTo(messageBox) ;

		// OK button to the messageBox.
		controlP5.Button b1 = cp5.addButton(this, "btnOK", "buttonOK", 0, width / 2 - 60, 218, 80, 30) ;
		b1.moveTo(messageBox) ;
		b1.setColorForeground(color(blueAct)) ;
		b1.setColorBackground(color(100)) ;
		b1.setColorActive(color(orangeAct)) ;
		b1.getCaptionLabel().setFont(font20) ;
		b1.getCaptionLabel().toUpperCase(false) ;
		b1.getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(10) ;

		// Cancel button to the messageBox.
		cp5.addButton(this, "btnCancel", "buttonCancel", 0, mBoxWidth / 2 + 5, mBoxHeight - 37, 80, 30)
		.moveTo(messageBox)
		.setCaptionLabel("Cancel")
		.setColorForeground(blueAct)
		.setColorBackground(color(100))
		.setColorActive(orangeAct)
		.hide()
		;
		cp5.getController("buttonCancel").getCaptionLabel().setFont(font20) ;
		cp5.getController("buttonCancel").getCaptionLabel().toUpperCase(false) ;
		cp5.getController("buttonCancel").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(10) ;

	}

	public void mbOkCancel() {
		cp5.getController("buttonOK").setPosition(mBoxWidth / 2 - 80 - 5, mBoxHeight - 37) ;
		cp5.getController("buttonOK").setSize(80, 30) ;
		cp5.getController("buttonOK").setCaptionLabel("OK") ;

		cp5.getController("buttonCancel").show() ;
	}

	public void mbClose() {
		cp5.getController("buttonOK").setPosition(mBoxWidth / 2 - 40 , mBoxHeight - 37) ;
		cp5.getController("buttonOK").setSize(80, 30) ;
		cp5.getController("buttonOK").setCaptionLabel("CLOSE") ;

		cp5.getController("buttonCancel").hide() ;
	}

	public void buttonOK(int theValue) {
		//println("OK button: "+theValue) ;
		if ( allValid != 0 ) {
			//writeConf() ;    TODO later
		}
		messageBox.hide() ;
	}

	public void buttonCancel(int theValue) {
		//println("Cancel button: "+theValue) ;
		messageBox.hide() ;
	}

	// Validate functions

	

	

	

	
			
	// Preset

	public void savePreset() {
		// Tab Gen.
		cp5.getProperties().copy(cp5.getController("oxsDirectory"), "Preset");
		cp5.getProperties().copy(cp5.getGroup("serialPin"), "Preset");
		cp5.getProperties().copy(cp5.getGroup("sensorID"), "Preset");
		cp5.getProperties().copy(cp5.getGroup("protocolChoice"), "Preset");
		cp5.getProperties().copy(cp5.getGroup("voltRefChoice"), "Preset");
		cp5.getProperties().copy(cp5.getController("arduinoVccNb"), "Preset");
		cp5.getProperties().copy(cp5.getController("saveEprom"), "Preset");
		cp5.getProperties().copy(cp5.getGroup("resetButtonPin"), "Preset");

		cp5.getProperties().copy(cp5.getController("vario"), "Preset");
		cp5.getProperties().copy(cp5.getController("vario2"), "Preset");
		cp5.getProperties().copy(cp5.getController("airSpeed"), "Preset");
		cp5.getProperties().copy(cp5.getController("voltage"), "Preset");
		cp5.getProperties().copy(cp5.getController("current"), "Preset");
		cp5.getProperties().copy(cp5.getController("rpm"), "Preset");

		// PPM
		cp5.getProperties().copy(cp5.getController("ppm"), "Preset");
		cp5.getProperties().copy(cp5.getGroup("ppmPin"), "Preset");
		cp5.getProperties().copy(cp5.getController("ppmRngMin"), "Preset");
		cp5.getProperties().copy(cp5.getController("ppmRngMax"), "Preset");

		// Tab Vario
		cp5.getProperties().copy(cp5.getController("ppmRngSensMinMax"), "Preset");
		cp5.getProperties().copy(cp5.getController("ppmSensMinMax"), "Preset");
		cp5.getProperties().copy(cp5.getGroup("vSpeed1"), "Preset");
		cp5.getProperties().copy(cp5.getGroup("vSpeed2"), "Preset");
		cp5.getProperties().copy(cp5.getController("ppmVspeedSwMin"), "Preset");
		cp5.getProperties().copy(cp5.getController("ppmVspeedSwMax"), "Preset");

		cp5.getProperties().copy(cp5.getController("sensMinMax"), "Preset");
		cp5.getProperties().copy(cp5.getController("vSpeedMin"), "Preset");
		cp5.getProperties().copy(cp5.getController("vSpeedMax"), "Preset");

		cp5.getProperties().copy(cp5.getController("varioHysteresis"), "Preset");

		cp5.getProperties().copy(cp5.getController("analogClimb"), "Preset");
		cp5.getProperties().copy(cp5.getGroup("climbPin"), "Preset");
		cp5.getProperties().copy(cp5.getController("outClimbRateMinMax"), "Preset");

		cp5.saveProperties("testPreset.ser", "Preset");

	}

	public void loadPreset() {
		cp5.loadProperties(("testPreset.ser"));

		// Hack to keep slider labels alignement
		cp5.getController("varioHysteresis").getCaptionLabel().align(ControlP5.LEFT_OUTSIDE, ControlP5.CENTER).setPaddingX(10) ;
		cp5.getController("varioHysteresis").getValueLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(10) ;
	}
				
	// config file writing function

	public void writeConf() {

		boolean dataFirst = true ;

		output = createWriter( outputConfigDir ) ;

		output.println("// Configuration file generated by OpenXsensor Configurator " + oxsCversion + " the: " + day + "-" + month + "-" + year() ) ;
		output.println("// !! This file is only compatible with version " + oxsVersion + " of OpenXsensor !!") ;
		output.println("") ;
		output.println("// OpenXsensor https://code.google.com/p/openxsensor/") ;
		output.println("// started by Rainer Schlo�han") ;
		output.println("") ;
		output.println("//***********************************************************************************************************************") ;
		output.println("// Another file in this project (see oXs_config_description.h) provides detailed explanations on how to set up this file.") ;
		output.println("//***********************************************************************************************************************") ;
		output.println("") ;
		output.println("#ifndef OXS_CONFIG_h") ;
		output.println("#define OXS_CONFIG_h") ;
		output.println("") ;

		// ---------------------------------- Device id --------------------------------------

		output.println("// --------- 1 - FrSky device ID when Sport protocol is used ---------") ;
		output.println("#define SENSOR_ID    " + cp5.get(DropdownList.class, "sensorID").getCaptionLabel().getText()) ;
		output.println("") ;

		// ---------------------------------- Serial pin --------------------------------------

		output.println("// --------- 2 - Serial data pin choice ---------") ;
		output.println("#define PIN_SERIALTX    " + TabGeneralSettings.getSerialPinDdl().getCaptionLabel().getText() + "       // The pin which transmits the serial data to the FrSky telemetry enabled receiver") ;
		output.println("") ;

		// ---------------------------------- PPM --------------------------------------

		output.println("// --------- 3 - PPM settings ---------") ;
		if ( ( cp5.getController("vario").getValue() == 1 || cp5.getController("airSpeed").getValue() == 1 ) && cp5.getController("ppm").getValue() == 1 ) {
			output.println("#define PIN_PPM         " + (int)TabPPM.getPpmPin().getValue() + "       // Arduino can read a PPM Signal coming from Tx. This allows to change the vario sensitivity using a pot or a switch on TX.") ;
		} else {
			output.println("//#define PIN_PPM               // Arduino can read a PPM Signal coming from Tx. This allows to change the vario sensitivity using a pot or a switch on TX.") ;
		}
		output.println("#define PPM_MIN_100     " + (int)cp5.getController("ppmRngMin").getValue() + "     // 1500 - 512 ; pulse width (usec) when TX sends a channel = -100") ;
		output.println("#define PPM_PLUS_100    " + (int)cp5.getController("ppmRngMax").getValue() + "    // 1500 + 512 ; pulse width (usec) when TX sends a channel = +100") ;
		output.println("") ;

		// ---------------------------------- Vario --------------------------------------

		output.println("// --------- 4 - Vario settings ---------") ;
		output.println("") ;
		output.println("// ***** 4.1 - Connecting 1 or 2 MS5611 barometric sensor *****") ;
		if ( cp5.getController("vario").getValue() == 1 ) {
			output.println("#define VARIO                   // set as comment if there is no vario") ;
			if ( cp5.getController("vario2").getValue() == 1 ) {
				output.println("#define VARIO2                  // set as comment if there is no second vario") ;
			} else {
				output.println("//#define VARIO2                // set as comment if there is no second vario") ;
			}
		} else {
			output.println("//#define VARIO                 // set as comment if there is no vario") ;
			output.println("//#define VARIO2                // set as comment if there is no second vario") ;
		}
		output.println("") ;
		output.println("// ***** 4.2 - Sensitivity predefined by program *****") ;
		output.println("#define SENSITIVITY_MIN       " + (int)cp5.getController("sensMinMax").getArrayValue(0) ) ;
		output.println("#define SENSITIVITY_MAX       " + (int)cp5.getController("sensMinMax").getArrayValue(1) ) ;
		output.println("#define SENSITIVITY_MIN_AT    " + (int)cp5.getController("vSpeedMin").getValue() ) ;
		output.println("#define SENSITIVITY_MAX_AT    " + (int)cp5.getController("vSpeedMax").getValue() ) ;
		output.println("") ;
		output.println("// ***** 4.3 - Sensitivity adjusted from the TX *****") ;
		output.println("#define SENSITIVITY_MIN_AT_PPM    " + (int)cp5.getController("ppmRngSensMinMax").getArrayValue(0) + "   // sensitivity will be changed by OXS only when PPM signal is between the specified range enlarged by -5/+5") ;
		output.println("#define SENSITIVITY_MAX_AT_PPM    " + (int)cp5.getController("ppmRngSensMinMax").getArrayValue(1) ) ;
		output.println("#define SENSITIVITY_PPM_MIN       " + (int)cp5.getController("ppmSensMinMax").getArrayValue(0) + "   // common value for vario is 20") ;
		output.println("#define SENSITIVITY_PPM_MAX       " + (int)cp5.getController("ppmSensMinMax").getArrayValue(1) + "  // common value for vario is 100") ;
		output.println("") ;
		output.println("// ***** 4.4 - Hysteresis parameter *****") ;
		output.println("#define VARIOHYSTERESIS    " + (int)cp5.getController("varioHysteresis").getValue() ) ;
		output.println("") ;
		output.println("// ***** 4.5 - Vertical speeds calculations *****") ;
		output.println("#define VARIO_PRIMARY              " + (int)cp5.getGroup("vSpeed1").getValue() + "    // 0 means first ms5611, 1 means second ms5611 , 2 means vario based on vario 1 + compensation from airspeed") ;
		output.println("#define VARIO_SECONDARY            " + (int)cp5.getGroup("vSpeed2").getValue() + "    // 0 means first ms5611, 1 means second ms5611 , 2 means vario based on vario 1 + compensation from airspeed") ;
		output.println("#define SWITCH_VARIO_MIN_AT_PPM    " + (int)cp5.getController("ppmVspeedSwMin").getValue() ) ;
		output.println("#define SWITCH_VARIO_MAX_AT_PPM    " + (int)cp5.getController("ppmVspeedSwMax").getValue() ) ;
		output.println("") ;
		output.println("// ***** 4.6 - Analog vertical speed *****") ;
		if ( cp5.getController("vario").getValue() == 1 && cp5.getController("analogClimb").getValue() == 1 ) {
			output.println("#define PIN_ANALOG_VSPEED    " + TabVario.getClimbPin().getCaptionLabel().getText() + "    // the pin used to write the vertical speed to the Rx A1 or A2 pin (can be 3 or 11 because it has to use timer 2)") ;
		} else {
			output.println("//#define PIN_ANALOG_VSPEED       //  the pin used to write the vertical speed to the Rx A1 or A2 pin (can be 3 or 11 because it has to use timer 2)") ;
		}
		output.println("#define ANALOG_VSPEED_MIN    " + (int)cp5.getController("outClimbRateMinMax").getArrayValue(0) ) ;
		output.println("#define ANALOG_VSPEED_MAX     " + (int)cp5.getController("outClimbRateMinMax").getArrayValue(1) ) ;
		output.println("");

		// ---------------------------------- Air Speed --------------------------------------

		output.println("// --------- 5 - Airspeed settings ---------") ;
		if ( cp5.getController("airSpeed").getValue() == 0 ) {
			output.print("//") ;
		}
		output.println("#define AIRSPEED    MS4525") ;
		output.println("") ;
		output.println("#define AIRSPEED_RESET_AT_PPM      " + (int)cp5.getController("aSpeedReset").getValue() ) ;
		output.println("") ;
		output.println("#define COMPENSATION_MIN_AT_PPM    " + (int)cp5.getController("ppmRngCompMinMax").getArrayValue(0) ) ;
		output.println("#define COMPENSATION_MAX_AT_PPM    " + (int)cp5.getController("ppmRngCompMinMax").getArrayValue(1) ) ;
		output.println("#define COMPENSATION_PPM_MIN       " + (int)cp5.getController("ppmCompMinMax").getArrayValue(0) ) ;
		output.println("#define COMPENSATION_PPM_MAX       " + (int)cp5.getController("ppmCompMinMax").getArrayValue(1) ) ;
		output.println("") ;

		// --------------------------- Voltages & Current sensor settings ---------------------------

		output.println("// --------- 6 - Voltages & Current sensor settings ---------") ;
		output.println("") ;
		output.println("// ***** 6.1 - Voltage Reference selection (VCC or 1.1V internal) *****") ;
		if ( cp5.get(DropdownList.class, "voltRefChoice").getValue() == 1 ) {
			output.print("//") ;
		}
		output.println("#define USE_INTERNAL_REFERENCE    // Select the voltage reference, comment the line to activate the VCC voltage reference") ;
		output.println("") ;
		output.println("// ***** 6.2 - Voltages analog pins *****") ;
		for ( int i = 1; i <= TabVoltage.getVoltnbr(); i++ ) {
			if ( cp5.getController( "voltage" ).getValue() == 1 && cp5.getController( "volt" + i ).getValue() == 1 && (int)cp5.getGroup("ddlVolt" + i).getValue() >= 0 ) {
				output.println("#define PIN_VOLTAGE_" + i + "    " + (int)cp5.getGroup("ddlVolt" + i).getValue() ) ;
			} else {
				output.println("//#define PIN_VOLTAGE_" + i ) ;
			}
		}
		output.println("") ;
		output.println("// ***** 6.3 - Voltage measurements calibration parameters *****") ;
		for ( int i = 1; i <= TabVoltage.getVoltnbr(); i++ ) {
			if ( cp5.getController( "voltage" ).getValue() == 1 && cp5.getController( "volt" + i ).getValue() == 1 ) {
				output.println("#define OFFSET_" + i + "             " + cp5.getController("offsetVolt" + i).getValueLabel().getText() + "         // offset in mV") ;
				output.println("#define MVOLT_PER_STEP_" + i + "     " + round(mVoltStep(i), 2) ) ;
			} else {
				output.println("#define OFFSET_" + i + "             " + 0 ) ;
				output.println("#define MVOLT_PER_STEP_" + i + "     " + 1 ) ;
			}
		}
		output.println("") ;
		output.println("// ***** 6.4 - Number of lipo cells to measure (and transmit to Tx) *****") ;
		if ( cp5.getController( "voltage" ).getValue() == 1 && cp5.getController( "cells" ).getValue() == 1 ) {
			output.println("#define NUMBEROFCELLS    " + ( (int)cp5.getGroup("ddlNbrCells").getValue() ) ) ;
		} else {
			output.println("//#define NUMBEROFCELLS") ;
		}
		output.println("") ;

		// ------------------------------ Current sensor ------------------------------

		output.println("// ***** 6.5 - Current sensor analog pin *****") ;
		if ( cp5.getController( "current" ).getValue() == 1 && (int)cp5.getGroup("currentPin").getValue() >= 0 ) {
			output.println("#define PIN_CURRENTSENSOR    " + ( (int)cp5.getGroup("currentPin").getValue() ) ) ;
		} else {
			output.println("//#define PIN_CURRENTSENSOR") ;
		}
		output.println("") ;
		output.println("// ***** 6.6 - Current sensor calibration parameters *****") ;
		output.println("#define OFFSET_CURRENT_STEPS         " + offsetCurrent() ) ;
		output.println("#define MAMP_PER_STEP                " + round(mAmpStep(), 2) + "   // INA282 with 0.1 ohm shunt gives 5000mv/A ") ;
		output.println("") ;

		// ---------------------------- Temperature sensor ----------------------------

		if ( tempActive ) {
			if ( cp5.getController( "temperature" ).getValue() == 1 && (int)cp5.getGroup("tempPin").getValue() >= 0 ) {
				output.println("// -------- Temperature sensor --------") ;
				output.println("#define PIN_TemperatureSensor   " + ( (int)cp5.getGroup("tempPin").getValue() ) + "  // The Analog pin the optional temperature sensor is connected to") ;
				output.println("#define TEMPOFFSET              " + cp5.get(Textfield.class, "tempOffset").getText() + "  // Calibration offset") ;
			} else {
				//output.println("#define PIN_TemperatureSensor        // The Analog pin the optional temperature sensor is connected to") ;
				//output.println("#define TEMPOFFSET                   // Calibration offset") ;
			}
			output.println("") ;
		}

		// --------------------------------- RPM sensor ---------------------------------

		output.println("// --------- 7 - RPM (rotations per minute) settings ---------") ;
		if ( cp5.getController( "rpm" ).getValue() == 1 ) {
			output.println("#define MEASURE_RPM") ;
		} else {
			output.println("//#define MEASURE_RPM") ;
		}
		output.println("") ;

		// ------------------------------ Save to EEPROM --------------------------------

		output.println("// --------- 8 - Persistent memory settings ---------") ;
		if ( cp5.getController("saveEprom").getValue() == 1 && cp5.getGroup("resetButtonPin").getValue() != -1 ) {
			output.println("#define SAVE_TO_EEPROM            // Current consumption will be stored in EEProm every 10 seconds.") ;
			output.println("#define PIN_PUSHBUTTON       " + cp5.get(DropdownList.class, "resetButtonPin").getCaptionLabel().getText() ) ;
		} else {
			output.println("//#define SAVE_TO_EEPROM") ;
			output.println("//#define PIN_PUSHBUTTON") ;
		}
		output.println("") ;

		// ------------------------- Transmitted data settings -------------------------

		output.println("// --------- 9 - Data to transmit ---------") ;
		output.println("// General set up to define which measurements are transmitted and how") ;
		output.println("") ;
		output.println("#define SETUP_DATA_TO_SEND    \\") ;
		for ( int i = 1; i <= TabData.getDataSentFieldNbr(); i++ ) {
			if ( TabData.getSentDataField(i).getValue() != 0 ) {
				if ( !dataFirst ) {
					output.println(" , \\") ;
				}
				if ( TabData.getSentDataField(i).getCaptionLabel().getText().equals("Cells monitoring") ) {
					if ( cp5.getController("cells").getValue() == 1 ) {
						for ( int j = 1 ; j <= (int) cp5.getGroup("ddlNbrCells").getValue() ; j += 2 ) {
							output.print("                        " + "DEFAULTFIELD , CELLS_" + j + "_" + ( j + 1 ) + " , 1 , 1 , 0" ) ;
							if ( (int) cp5.getGroup("ddlNbrCells").getValue() > ( j + 1 ) ) {
								output.println(" , \\") ;
							}
							dataFirst = false ;
						}
					}
				} else if ( cp5.getGroup("protocolChoice").getValue() == 1 ) {
					output.print("                        " + TabData.getHubDataList()[(int)cp5.getGroup("hubDataField" + i ).getValue()][0] + " , "
							+ TabData.getSentDataList()[(int)TabData.getSentDataField(i).getValue()][0] + " , "
							+ cp5.getController("dataMultiplier" + i).getValueLabel().getText() + " , "
							+ cp5.getController("dataDivider" + i).getValueLabel().getText() + " , "
							+ cp5.getController("dataOffset" + i).getValueLabel().getText() ) ;
					dataFirst = false ;
				} else {
					output.print("                        " + TabData.getsPortDataList()[(int)TabData.getTargetDataField(i).getValue()][0] + " , "
							+ TabData.getSentDataList()[(int)TabData.getSentDataField(i).getValue()][0] + " , "
							+ cp5.getController("dataMultiplier" + i).getValueLabel().getText() + " , "
							+ cp5.getController("dataDivider" + i).getValueLabel().getText() + " , "
							+ cp5.getController("dataOffset" + i).getValueLabel().getText() ) ;
					dataFirst = false ;
				}
			}
		}
		output.println("") ;
		output.println("") ;

		// ---------------------------------- Debug --------------------------------------

		output.println("// --------- 10 - Reserved for developer. DEBUG must be activated here when we want to debug one or several functions in some other files. ---------") ;
		output.println("//#define DEBUG") ;
		output.println("") ;
		output.println("#ifdef DEBUG") ;
		output.println("#include \"HardwareSerial.h\"") ;
		output.println("#endif") ;
		output.println("") ;

		// ---------------------------------- The end --------------------------------------

		output.print("#endif// End define OXS_CONFIG_h") ;

		output.flush() ; // Writes the remaining data to the file
		output.close() ; // Finishes the file

		//exit() ; // Stops the program
	}

		

	public static void main(String _args[]) {
		PApplet.main(new String[] { oxsc.MainP.class
				.getName() });
	}
}
