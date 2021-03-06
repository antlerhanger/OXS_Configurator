package gui;

import oxsc.MainP;
import processing.core.PConstants;

import java.util.ArrayList;
import java.util.List;

import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.DropdownList;
import controlP5.Numberbox;
import controlP5.Toggle;

public class TabVoltage {

	private static ControlP5 cp5;
	
	private static final int VOLT_NUMBER = 6;
	private static Toggle[] voltTgl = new Toggle[VOLT_NUMBER + 1];
	private static DropdownList[] ddlVolt = new DropdownList[VOLT_NUMBER + 1];
	private static Numberbox[] dividerVoltNBox = new Numberbox[VOLT_NUMBER + 1];
	private static Numberbox[] offsetVoltNBox = new Numberbox[VOLT_NUMBER + 1];
	private static Toggle cellsTgl;
	private static DropdownList ddlNbrCells;

	private static List<Object> controllers = new ArrayList<>();

	public TabVoltage(ControlP5 cp5) {

		TabVoltage.cp5 = cp5;

		cp5.getTab("voltage")
		   .setHeight(20)
		   .setColorForeground(MainP.tabGray)
		   .setColorBackground(MainP.darkBackGray)
		   .setColorActive(MainP.blueAct)
		   .setLabel("Voltage...")
		   .setId(3)
		   .hide();
		cp5.getTab("voltage").getCaptionLabel().toUpperCase(false);

		// Voltage 1-6 toggle
		cp5.addTextlabel("voltages")
		   .setText("Voltage number")
		   .setPosition(10, 145)
		   .setColorValueLabel(0)
		   .setTab("voltage");

		for (int i = 1; i <= VOLT_NUMBER; i++) {
			voltTgl[i] = cp5.addToggle("voltTgl" + i)
					        .setCaptionLabel("" + i)
					        .setPosition(128 + 55 * (i - 1), 147)
					        .setTab("voltage");
			customizeToggleVolt(voltTgl[i]);
		}

		// Voltage 1-6 pin
		cp5.addTextlabel("voltPin")
		   .setText("Pin number")
		   .setPosition(10, 173)
	   	   .setColorValueLabel(0)
	   	   .setTab("voltage");

		for (int i = 1; i <= VOLT_NUMBER; i++) {
			ddlVolt[i] = cp5.addDropdownList("ddlVolt" + i)
			                .setPosition(113 + 55 * (i - 1), 192)
			                .setTab("voltage");
			customizeDdlVpin(ddlVolt[i]);
			ddlVolt[i].setValue(-1);
		}

		// Voltage 1-6 divider factor
		cp5.addTextlabel("voltDivider")
		   .setText("Divider factor                                                                                           ")
		   .setPosition(10, 204)
		   .setColorValueLabel(0)
		   .setTab("voltage");
		cp5.getTooltip().register("voltDivider", "- Default: 1 -");

		for (int i = 1; i <= VOLT_NUMBER; i++) {
			dividerVoltNBox[i] = cp5.addNumberbox("dividerVoltNBox" + i)
			                        .setPosition(113 + 55 * (i - 1), 202)
			                        .setSize(45, 20)
			                        .setColorActive(MainP.blueAct)
			                        .setDecimalPrecision(2)
			                        .setRange((float) 0.01, (float) 99.99)
			                        // set the sensitivity of the numberbox
			                        .setMultiplier((float) 0.01)
			                        // change the control direction to left/right
			                        .setDirection(Controller.HORIZONTAL)
			                        .setValue(1)
			                        .setCaptionLabel("")
			                        .setTab("voltage")
			                        ;
			controllers.add(dividerVoltNBox[i]);
		}

		// Voltage 1-6 Offset
		cp5.addTextlabel("voltOffset")
		   .setText("Offset (mV)                                                                                              ")
		   .setPosition(10, 233)
		   .setColorValueLabel(0)
		   .setTab("voltage");
		cp5.getTooltip().register("voltOffset", "- Default: 0 -");

		for (int i = 1; i <= VOLT_NUMBER; i++) {
			offsetVoltNBox[i] = cp5.addNumberbox("offsetVoltNBox" + i)
			                       .setPosition(113 + 55 * (i - 1), 231)
			                       .setSize(45, 20)
			                       .setColorActive(MainP.blueAct)
			                       .setDecimalPrecision(0)
			                       .setRange(-5000, 5000)
			                       .setMultiplier(1) // set the sensitivity of  the numberbox
			                       .setDirection(Controller.HORIZONTAL) // change the control direction to left/right
			                       .setValue(0)
			                       .setCaptionLabel("")
			                       .setTab("voltage")
			                       ;
			controllers.add(offsetVoltNBox[i]);
		}

		// Cells monitoring -> Number of Cells
		cellsTgl = cp5.addToggle("cellsTgl")
				      .setPosition(148, 296)
		              .setCaptionLabel("Battery cells monitoring")
		              .setColorForeground(MainP.orangeAct)
		              .setColorBackground(MainP.darkBackGray)
		              .setColorActive(MainP.blueAct)
		              .setColorCaptionLabel(0)
		              .setSize(15, 15)
		              .setTab("voltage")
		              ;
		cellsTgl.getCaptionLabel()
				.align(ControlP5.LEFT_OUTSIDE, ControlP5.CENTER)
				.setPaddingX(10);
		cellsTgl.getCaptionLabel().toUpperCase(false);
		controllers.add(cellsTgl);

		cp5.addTextlabel("NbrCells")
		   .setText("Number of Cells")
		   .setPosition(190, 295)
		   .setColorValueLabel(0)
		   .setTab("voltage");

		ddlNbrCells = cp5.addDropdownList("ddlNbrCells")
						 .setColorForeground(MainP.orangeAct)
						 .setColorBackground(MainP.darkBackGray)
						 .setColorActive(MainP.blueAct)
						 .setPosition(288, 314)
						 .setSize(25, 140)
						 .setItemHeight(20)
						 .setBarHeight(20)
						 .setTab("voltage")
						 ;
		/*
		 * for ( int i = 1; i <= 6; i++ ) { ddlNbrCells.addItem("" + i, i) ; }
		 */
		ddlNbrCells.getCaptionLabel().getStyle().marginTop = 2;
		// ddlNbrCells.getCaptionLabel().set("-");
		ddlNbrCells.toUpperCase(false);
		controllers.add(ddlNbrCells);
		
		// dropdownlist overlap
		ddlNbrCells.bringToFront() ;
		for ( int i = 1; i <= VOLT_NUMBER; i++ ) {
			ddlVolt[i].bringToFront() ;
		}

	}

	public static int getVoltnbr() {
		return VOLT_NUMBER;
	}

	public static Toggle[] getVoltTgl() {
		return voltTgl;
	}

	public static DropdownList[] getDdlVolt() {
		return ddlVolt;
	}

	public static Numberbox[] getDividerVoltNBox() {
		return dividerVoltNBox;
	}

	public static Numberbox[] getOffsetVoltNBox() {
		return offsetVoltNBox;
	}

	public static Toggle getCellsTgl() {
		return cellsTgl;
	}
	
	public static DropdownList getDdlNbrCells() {
		return ddlNbrCells;
	}

	public static int getDdlNbrCellsID() {
		return (int) ddlNbrCells.getValue();
	}

	public static List<Object> getControllers() {
		return controllers;
	}

	public static void populateNbrCells() {
		ddlNbrCells.clear();
		for (int i = 1; i <= VOLT_NUMBER; i++) {
			if (MainP.aVolt[i] != null) {
				ddlNbrCells.addItem("" + i, i);
			} else {
				// change NbrCells if not possible
				if (ddlNbrCells.getValue() >= i) {
					ddlNbrCells.setValue(i - 1);
				}
				return;
			}
		}
	}

	public static void draw(MainP mainP) {
		// separation lines
		mainP.stroke(MainP.darkBackGray) ;
		mainP.line(10, 272, 440, 272) ;
		mainP.noStroke() ;

		// Voltage tab grayed items
		for ( int i = 1; i <= TabVoltage.getVoltnbr(); i++ ) {
			if ( voltTgl[i].getValue() == 0 ) {
				ddlVolt[i].hide() ;
				mainP.fill(MainP.grayedColor) ;
				mainP.rect(113 + 55 * (i-1), 171, 45, 20) ;
				dividerVoltNBox[i].lock() 
				                  .setColorBackground(mainP.color(165)) 
				                  .setColorForeground(mainP.color(195)) 
				                  .setColorValueLabel(mainP.color(165))
				                  ;
				offsetVoltNBox[i].lock()
				                 .setColorBackground(mainP.color(175))
				                 .setColorForeground(mainP.color(195))
				                 .setColorValueLabel(mainP.color(175))
				                 ;
			} else {
				ddlVolt[i].show() ;
				dividerVoltNBox[i].unlock()
				                  .setColorBackground(MainP.darkBackGray)
				                  .setColorValueLabel(MainP.white)
				                  ;
				offsetVoltNBox[i].unlock()
				                 .setColorBackground(MainP.darkBackGray)
				                 .setColorValueLabel(MainP.white)
				                 ;
			}
		}

		// Battery cells monitoring grayed
		if (!voltTgl[1].getState() /*|| MainP.protocol instanceof ProtMultiplex*/) {
			mainP.stroke(MainP.grayedColor) ;                    // toggle border gray
			mainP.noFill() ;
			mainP.rect(10, 293, 155, 20) ;
			mainP.noStroke() ;
			cellsTgl.lock()
			        .setState(false)
			        .setColorBackground(MainP.grayedColor)
			        .setColorCaptionLabel(MainP.grayedColor)
			        ;
		} else {
			mainP.stroke(MainP.darkBackGray) ;                             // toggle border
			mainP.noFill() ;
			mainP.rect(10, 293, 155, 20) ;
			mainP.noStroke() ;
			cellsTgl.setBroadcast(true)
			        .unlock()
			        .setColorBackground(MainP.darkBackGray)
			        .setColorCaptionLabel(0)
			        ;
		}

		// Cells number grayed
		if ( cellsTgl.getValue() == 0 ) {
			cp5.getController("NbrCells").setColorValueLabel(MainP.grayedColor) ;
			ddlNbrCells.hide() ;
			mainP.fill(MainP.grayedColor) ;
			mainP.rect(288, 293, 25, 20) ;
		} else {
			mainP.stroke(MainP.darkBackGray) ;                              // toggle border filled
			mainP.fill(MainP.lightBlue) ;
			mainP.rect(10, 293, 155, 20) ;
			mainP.noStroke() ;
			cp5.getController("NbrCells").setColorValueLabel(0) ;
			ddlNbrCells.show() ;
		}
	
		// Voltage tab ->  Cells indicator
		if ( cellsTgl.getValue() == 1 && ddlNbrCells.getValue() > 0 ) {
			int nCells = (int) TabVoltage.getDdlNbrCells().getValue() ;
			mainP.noSmooth() ;
			mainP.stroke(MainP.blueAct) ;
			mainP.strokeWeight(3) ;
			mainP.strokeCap(PConstants.PROJECT) ;
			mainP.fill(MainP.blueAct) ;
			mainP.textFont(MainP.fontCells) ;
			mainP.text("BATTERY CELLS", 13, 122) ;
			mainP.line ( 115, 122, 115, 137 ) ;
			mainP.line ( 115, 122, 100 + 55 * nCells, 122 ) ;
			mainP.line ( 100 + 55 * nCells, 122, 100 + 55 * nCells, 137 ) ;
			mainP.strokeWeight(1) ;
			mainP.noStroke() ;
			mainP.smooth() ;
		}

		// ----------------- Texfield and Numberbox mouse-over -----------------
		for (int i = 1; i <= VOLT_NUMBER; i++) {
			if (cp5.isMouseOver(dividerVoltNBox[i])) {
				dividerVoltNBox[i].setColorForeground(MainP.orangeAct);
			} else {
				dividerVoltNBox[i].setColorForeground(mainP.color(170));
			}
		}

		for (int i = 1; i <= VOLT_NUMBER; i++) {
			if (cp5.isMouseOver(offsetVoltNBox[i])) {
				offsetVoltNBox[i].setColorForeground(MainP.orangeAct);
			} else {
				offsetVoltNBox[i].setColorForeground(mainP.color(170));
			}
		}

		// ----------------- Dropdownlist: mouse pressed elsewhere closes list -----------------
		for (int i = 1; i <= VOLT_NUMBER; i++) {
			if (!cp5.isMouseOver(ddlVolt[i])) {
				if (mainP.mousePressed) {
					ddlVolt[i].close();
				}
			}
		}

		if (!cp5.isMouseOver(ddlNbrCells)) {
			if (mainP.mousePressed) {
				ddlNbrCells.close();
			}
		}
	}

	public static void customizeDdlVpin(DropdownList ddlV) {
		ddlV.setSize(45, 200)
		    .setColorForeground(MainP.orangeAct)
		    .setBackgroundColor(190) // can't use standard color
		    .setColorActive(MainP.blueAct)
		    .setItemHeight(20)
		    .setBarHeight(20);
		ddlV.getCaptionLabel().set(" ") ;
		ddlV.getCaptionLabel().setPaddingX(12) ;
		ddlV.getCaptionLabel().getStyle().marginTop = 2 ;
	
		ddlV.addItem(" --", -1) ;
		ddlV.addItems(MainP.analogPins) ;
		//ddlv.setValue(-1) ;
	
		ddlV.toUpperCase(false) ;
		controllers.add(ddlV);
	}

	public static void customizeToggleVolt(Toggle tglV) {
		tglV.setColorForeground(MainP.orangeAct) ;
		tglV.setColorBackground(MainP.darkBackGray) ;
		tglV.setColorActive(MainP.blueAct) ;
		tglV.setColorCaptionLabel(0) ;
		tglV.setSize(15, 15) ;
	
		// reposition the Labels
		tglV.getCaptionLabel().align(ControlP5.CENTER, ControlP5.TOP_OUTSIDE).setPaddingX(10) ;	
		tglV.getCaptionLabel().toUpperCase(false) ;
		controllers.add(tglV);
	}

}
