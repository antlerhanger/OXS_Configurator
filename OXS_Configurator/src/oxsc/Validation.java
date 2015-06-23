package oxsc;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;

import processing.core.PApplet;
import controlP5.ControlP5;
import gui.TabData;
import gui.TabGeneralSettings;
import gui.TabPPM;
import gui.TabVario;
import gui.TabVoltage;

public class Validation {
	
	private static ControlP5 cp5 ;
	private static File versionFile = new File(MainP.oxsDirectory + "/version.oxs");

	public static CharBuffer version;

	public Validation(ControlP5 cp5) {
		Validation.cp5 = cp5;
	}

	public static void validateNumPins() {

		String numPinsValidation[][] = new String[][] {                      // array { pin name, pin value, isActive }
				{ "Serial output", "" + (int)TabGeneralSettings.getSerialPinDdl().getValue(), "2" },
				{ "Reset button", "" + (int)cp5.getGroup( "resetButtonPin" ).getValue(), "" + (int) cp5.getController("saveEprom").getValue() + 1  },
				{ "PPM input", "" + (int)TabPPM.getPpmPin().getValue(), "" + (int) cp5.getController("ppm").getValue() + (int)cp5.getController("vario").getValue() + (int)cp5.getController("airSpeed").getValue() },
				{ "Analog climb output", "" + (int)TabVario.getClimbPin().getValue(), "" + (int) cp5.getController("analogClimb").getValue() + (int)cp5.getController("vario").getValue() },
				{ "RPM input", "" + 8, "" + (int) cp5.getController("rpm").getValue() + 1 }
		} ;

		for ( int i = 0; i < numPinsValidation.length; i++ ) {
			for ( int j = i+1; j < numPinsValidation.length; j++ ) {
				if ( Integer.parseInt(numPinsValidation[i][1]) != -1 && Integer.parseInt(numPinsValidation[j][1]) != -1 && Integer.parseInt(numPinsValidation[i][2]) > 1 && Integer.parseInt(numPinsValidation[j][2]) > 1 ) {
					if ( numPinsValidation[i][1].equals(numPinsValidation[j][1]) ) {
						//println("Attention !!  " + numPinsValidation[i][0] + " is using the same pin n°" + numPinsValidation[i][1] + " as " + numPinsValidation[j][0] + " !") ;
						MainP.numPinsValid = false ;
						MainP.messageList.append("- " + numPinsValidation[i][0] + " is using the same pin n°" + numPinsValidation[i][1] + " as " + numPinsValidation[j][0] + " !") ;
					}
				}
			}
		}
		/*
			  if ( numPinsValid ) {
			    println("No problem found with numeric pins ;)") ;
			    messageList.append("No problem found with numeric pins ;)") ;
			  }
		 */
	}

	public static void validateAnalogPins() {

		String voltActive[] = new String[7] ;
		int voltActiveCount = 0 ;

		for ( int i = 1 ; i <= TabVoltage.getVoltnbr() ; i++ ) {
			if ( (int)cp5.getController("voltage").getValue() == 1 && (int)cp5.getController("volt" + i ).getValue() == 1 ) {
				voltActive[i] = "1" ;
				voltActiveCount ++ ;
			} else {
				voltActive[i] = "0" ;
			}
		}

		if ( (int)cp5.getController("voltage").getValue() == 1 && voltActiveCount == 0 ) {
			MainP.analogPinsValid = false ;
			MainP.messageList.append("- Voltage sensor is active but there is no voltage to measure !") ;
		}

		String analogPinsValidation[][] = new String[][] {              // array { pin name, pin value, isActive }
				{ "Voltage n°1", "" + (int)cp5.getGroup("ddlVolt1").getValue(), "" + voltActive[1] },
				{ "Voltage n°2", "" + (int)cp5.getGroup("ddlVolt2").getValue(), "" + voltActive[2] },
				{ "Voltage n°3", "" + (int)cp5.getGroup("ddlVolt3").getValue(), "" + voltActive[3] },
				{ "Voltage n°4", "" + (int)cp5.getGroup("ddlVolt4").getValue(), "" + voltActive[4] },
				{ "Voltage n°5", "" + (int)cp5.getGroup("ddlVolt5").getValue(), "" + voltActive[5] },
				{ "Voltage n°6", "" + (int)cp5.getGroup("ddlVolt6").getValue(), "" + voltActive[6] },
				{ "Current Sensor", "" + (int)cp5.getGroup("currentPin").getValue(), "" + (int)cp5.getController("current").getValue() },
				//{ "Temperature Sensor", "" + (int)cp5.getGroup("tempPin").getValue(), "" + (int)cp5.getController("temperature").getValue() },
				{ "Vario/Air Speed (A4-A5)", "4", "" + (int) cp5.getController("vario").getValue() + (int)cp5.getController("airSpeed").getValue() },
				{ "Vario/Air Speed (A4-A5)", "5", "" + (int) cp5.getController("vario").getValue() + (int)cp5.getController("airSpeed").getValue() }
		} ;

		for ( int i = 0; i < analogPinsValidation.length; i++ ) {
			if ( Integer.parseInt(analogPinsValidation[i][1]) == -1 && Integer.parseInt(analogPinsValidation[i][2]) == 1 ) {
				MainP.analogPinsValid = false ;
				MainP.messageList.append("- " + analogPinsValidation[i][0] + " has no pin assigned !") ;
			}
			for ( int j = i+1; j < analogPinsValidation.length; j++ ) {

				if ( Integer.parseInt(analogPinsValidation[i][1]) != -1 && Integer.parseInt(analogPinsValidation[j][1]) != -1 && Integer.parseInt(analogPinsValidation[i][2]) >= 1 && Integer.parseInt(analogPinsValidation[j][2]) >= 1 ) {
					if ( analogPinsValidation[i][1].equals(analogPinsValidation[j][1]) ) {
						//println("Attention !!  " + analogPinsValidation[i][0] + " is using the same pin n�A" + analogPinsValidation[i][1] + " as " + analogPinsValidation[j][0] + " !") ;
						MainP.analogPinsValid = false ;
						MainP.messageList.append("- " + analogPinsValidation[i][0] + " is using the same pin n°A" + analogPinsValidation[i][1] + " as " + analogPinsValidation[j][0] + " !") ;
					}
				}

			}
		}
		/*
			  if ( analogPinsValid ) {
			    println("No problem found with analog pins ;)") ;
			    messageList.append("No problem found with analog pins ;)") ;
			  }
		 */
	}

	public static void validateVspeed() {

		if ( cp5.getController("vario").getValue() == 1 ) {                                                     // test V.Speed types with sensors

			for (;;) {
				if ( (int)cp5.getGroup("vSpeed1").getValue() == 1 && cp5.getController("vario2").getValue() == 0 ) {
					MainP.vSpeedValid = 0 ;
					MainP.messageList.append( "- You can't use Vario 2 V.Speed as Vario 2 is not activated !" ) ;
					break ;
				} else if ( (int)cp5.getGroup("vSpeed1").getValue() == 2 && cp5.getController("airSpeed").getValue() == 0 ) {
					MainP.vSpeedValid = 0 ;
					MainP.messageList.append( "- You can't use Vario 1 + A.Speed compensated V.Speed as Air Speed" ) ;
					MainP.messageList.append( "  sensor is not activated !" ) ;
					break ;
				}

				if ( cp5.getController("ppm").getValue() == 1 && ( cp5.getController("vario2").getValue() == 1 || cp5.getController("airSpeed").getValue() == 1 ) ) {
					if ( (int)cp5.getGroup("vSpeed2").getValue() == 1 && cp5.getController("vario2").getValue() == 0 ) {
						MainP.vSpeedValid = 0 ;
						MainP.messageList.append( "- You can't use Vario 2 V.Speed as Vario 2 is not activated !" ) ;
					} else if ( (int)cp5.getGroup("vSpeed2").getValue() == 2 &&  cp5.getController("airSpeed").getValue() == 0 ) {
						MainP.vSpeedValid = 0 ;
						MainP.messageList.append( "- You can't use Vario 1 + A.Speed compensated V.Speed as Air Speed" ) ;
						MainP.messageList.append( "  sensor is not activated !" ) ;
					}

					if ( (int)cp5.getGroup("vSpeed1").getValue() == (int)cp5.getGroup("vSpeed2").getValue() ) {
						MainP.vSpeedValid = ( MainP.vSpeedValid == 0 ) ? 0 : 1 ;
						MainP.messageList.append( "- You have set the same V.Speed types for switching in Vario TAB !" ) ;
					}
				}
				break ;
			}

		}

	}

	public static void validateCells() {

		int cellsNbr = 0 ;

		if ( cp5.getController("cells").getValue() == 1 ) {
			//println("Cells active") ;
			for ( int i = 1 ; i <= TabVoltage.getVoltnbr() ; i++ ) {
				if ( cp5.getController("volt" + i ).getValue() == 1 ) {
					cellsNbr ++ ;
				} else {
					break ;
				}
			}
			if ( cp5.getGroup("ddlNbrCells").getValue() > cellsNbr ) {
				MainP.cellsValid = false ;
				MainP.messageList.append( "- You can't monitor more than " + cellsNbr + " cell(s)" ) ;
			}
			//println(cellsNbr ) ;
		}

	}

	public static void validateSentData() {                // TODO later better redundancy tests validateSentData()
		
		int oxsMeasureCount = 0 ;
	
		String oxsMeasureValidationList[][] = new String[TabData.getDataSentFieldNbr() + 1][5] ;
		for ( int i = 1 ; i < oxsMeasureValidationList.length ; i++ ) {
			oxsMeasureValidationList[i][0] = "" + TabData.getSentDataList()[ (int) TabData.getSentDataField(i).getValue() ][0] ; // Data sent - ( OXS measurement )
			oxsMeasureValidationList[i][1] = "" + TabData.getSentDataField(i).getCaptionLabel().getText() ;          // OXS measurement - ( Data sent )
			oxsMeasureValidationList[i][2] = "" + cp5.getGroup("hubDataField" + i ).getCaptionLabel().getText() ;           // HUB data field
			oxsMeasureValidationList[i][3] = "" + TabData.getTargetDataField(i).getCaptionLabel().getText() ;         // target data field
			oxsMeasureValidationList[i][4] = "0" ;                                                                          // is measurement active
		}
		for ( int i = 1 ; i <= TabData.getDataSentFieldNbr() ; i++ ) {
			switch ( (int) TabData.getSentDataField(i).getValue() ) {
			case 1 :                 // "ALTIMETER"
			case 2 :                 // "VERTICAL_SPEED"
			case 3 :                 // "ALT_OVER_10_SEC"
			case 7 :                 // "SENSITIVITY"
				if ( cp5.getController("vario").getValue() == 1 ) { oxsMeasureValidationList[i][4] = "1" ; }
				break ;
			case 4 :                 // "ALTIMETER_2"
			case 5 :                 // "VERTICAL_SPEED_2"
			case 6 :                 // "ALT_OVER_10_SEC_2"
				if ( cp5.getController("vario2").getValue() == 1 ) { oxsMeasureValidationList[i][4] = "1" ; }
				break ;
			case 8 :                 // "AIR_SPEED"
				if ( cp5.getController("airSpeed").getValue() == 1 ) { oxsMeasureValidationList[i][4] = "1" ; }
				break ;
			case 9 :                 // "PRANDTL_DTE"
			case 10 :                // "PRANDTL_COMPENSATION"
				if ( cp5.getController("vario").getValue() == 1 && cp5.getController("airSpeed").getValue() == 1 ) { oxsMeasureValidationList[i][4] = "1" ; }
				break ;
			case 11 :                // "PPM_VSPEED"
				if ( cp5.getController("vario").getValue() == 1 && cp5.getController("ppm").getValue() == 1 ) { oxsMeasureValidationList[i][4] = "1" ; }
				break ;
			case 12 :                 // "CURRENTMA"
			case 13 :                 // "MILLIAH"
				if ( cp5.getController("current").getValue() == 1 ) { oxsMeasureValidationList[i][4] = "1" ; }
				break ;
			case 14 :                 // "CELLS"
				if ( cp5.getController("voltage").getValue() == 1 && cp5.getController("cells").getValue() == 1 ) { oxsMeasureValidationList[i][4] = "1" ; }
				break ;
			case 15 :                 // "VOLT1"
				if ( cp5.getController("voltage").getValue() == 1 && cp5.getController("volt1").getValue() == 1 ) { oxsMeasureValidationList[i][4] = "1" ; }
				break ;
			case 16 :                 // "VOLT2"
				if ( cp5.getController("voltage").getValue() == 1 && cp5.getController("volt2").getValue() == 1 ) { oxsMeasureValidationList[i][4] = "1" ; }
				break ;
			case 17 :                // "VOLT3"
				if ( cp5.getController("voltage").getValue() == 1 && cp5.getController("volt3").getValue() == 1 ) { oxsMeasureValidationList[i][4] = "1" ; }
				break ;
			case 18 :                // "VOLT4"
				if ( cp5.getController("voltage").getValue() == 1 && cp5.getController("volt4").getValue() == 1 ) { oxsMeasureValidationList[i][4] = "1" ; }
				break ;
			case 19 :                // "VOLT5"
				if ( cp5.getController("voltage").getValue() == 1 && cp5.getController("volt5").getValue() == 1 ) { oxsMeasureValidationList[i][4] = "1" ; }
				break ;
			case 20 :                // "VOLT6"
				if ( cp5.getController("voltage").getValue() == 1 && cp5.getController("volt6").getValue() == 1 ) { oxsMeasureValidationList[i][4] = "1" ; }
				break ;
			case 21 :                // "RPM"
				if ( cp5.getController("rpm").getValue() == 1 ) { oxsMeasureValidationList[i][4] = "1" ; }
				break ;
			case 22 :                // "PPM"
				if ( cp5.getController("ppm").getValue() == 1 ) { oxsMeasureValidationList[i][4] = "1" ; }
				break ;
			}
		}
	
		int oxsMeasureCountHUB[][] = new int[TabData.getSentDataList().length + 1][TabData.getHubDataList().length] ;
		int oxsMeasureCountSPORT[][] = new int[TabData.getSentDataList().length + 1][TabData.getsPortDataList().length] ;               // array { sentData, sPortData }
	
		for ( int i = 1 ; i <= TabData.getDataSentFieldNbr() ; i++ ) {
			float sentDataFieldNb = TabData.getSentDataField(i).getValue() ;
			String sentDataFieldName = TabData.getSentDataField(i).getCaptionLabel().getText() ; // TODO ori: tab7.getDdlFieldDisplay("sentDataField" + i)
	
			float sPortDataFieldNb = TabData.getTargetDataField(i).getValue() ;
			String sPortDataFieldName = TabData.getSentDataField(i).getCaptionLabel().getText() ; // TODO ori: tab7.getDdlFieldDisplay("sPortDataField" + i)
			PApplet.println("(for) nom dest. = " + sPortDataFieldName) ;
	
			if ( sentDataFieldNb > 0 ) {   // if OXS measurement field is not empty
				oxsMeasureCount ++ ;
				//if ( OXSdata.isInList( sentDataFieldName ) ) {                // if OXS measurement is valid (sensor available) TODO remove
				/*if ( cp5.getGroup("protocolChoice").getValue() == 1 ) {          // if HUB protocol => maybe not up to date since OXSC v2.0
			            if ( cp5.getGroup("hubDataField" + i).getValue() == 0 ) {    // if telemetry field is empty
			              sentDataValid = false ;
			              messageList.append( "- The " + oxsMeasureValidationList[i][1] + " measure is not sent !" ) ;
			            } else if ( ( oxsMeasureValidationList[i][0].equals("ALTIMETER") || oxsMeasureValidationList[i][0].equals("CELLS")          // OXS measurement must be default
			                          || oxsMeasureValidationList[i][0].equals("RPM") ) && !oxsMeasureValidationList[i][2].equals("DEFAULT") ) {
			                sentDataValid = false ;
			                messageList.append( "- " + oxsMeasureValidationList[i][1] + " must be set to DEFAULT !" ) ;
			            } else if ( ( oxsMeasureValidationList[i][0].equals("SENSITIVITY") || oxsMeasureValidationList[i][0].equals("ALT_OVER_10_SEC")   // OXS measurement can't be default
			                          || oxsMeasureValidationList[i][0].equals("VOLT1") || oxsMeasureValidationList[i][0].equals("VOLT2")
			                          || oxsMeasureValidationList[i][0].equals("VOLT3") || oxsMeasureValidationList[i][0].equals("VOLT4")
			                          || oxsMeasureValidationList[i][0].equals("VOLT5") || oxsMeasureValidationList[i][0].equals("VOLT6")
			                          || oxsMeasureValidationList[i][0].equals("MILLIAH") ) && oxsMeasureValidationList[i][2].equals("DEFAULT") ) {
			                sentDataValid = false ;
			                messageList.append( "- " + oxsMeasureValidationList[i][1] + " can't be set to DEFAULT !" ) ;
			            }
			            oxsMeasureCountHUB[int( sentDataFieldNb )][int( cp5.getGroup("hubDataField" + i).getValue() )] ++ ;
			            oxsMeasureCountHUB[sentDataList.length][int( cp5.getGroup("hubDataField" + i).getValue() )] ++ ;
	
			        } else*/ 
				if ( sPortDataFieldNb == 0 ) {         // if telemetry field is empty
					MainP.sentDataValid = false ;
					MainP.messageList.append( "- The " + sentDataFieldName + " measure is not sent !" ) ;
				} else if ( MainP.protocol.getName() == "FrSky" ) {          // if FrSky protocol
					/*} else*/ if ( ( sentDataFieldName == "Cells monitoring" || sentDataFieldName == "RPM" )   // OXS measurement must be default
							&& sPortDataFieldName != "DEFAULT" ) {
						MainP.sentDataValid = false ;
						MainP.messageList.append( "- " + sentDataFieldName + " must be set to DEFAULT !" ) ;
					} else if ( ( oxsMeasureValidationList[i][0].equals("ALT_OVER_10_SEC") || oxsMeasureValidationList[i][0].equals("ALT_OVER_10_SEC_2")   // OXS measurement can't be default
							|| oxsMeasureValidationList[i][0].equals("SENSITIVITY") || oxsMeasureValidationList[i][0].equals("PRANDTL_COMPENSATION")
							|| oxsMeasureValidationList[i][0].equals("VOLT1") || oxsMeasureValidationList[i][0].equals("VOLT2")
							|| oxsMeasureValidationList[i][0].equals("VOLT3") || oxsMeasureValidationList[i][0].equals("VOLT4")
							|| oxsMeasureValidationList[i][0].equals("VOLT5") || oxsMeasureValidationList[i][0].equals("VOLT6")
							|| oxsMeasureValidationList[i][0].equals("PPM") ) && oxsMeasureValidationList[i][3].equals("DEFAULT") ) {
						MainP.sentDataValid = false ;
						MainP.messageList.append( "- " + oxsMeasureValidationList[i][1] + " can't be set to DEFAULT !" ) ;
					} else if ( ( oxsMeasureValidationList[i][0].equals("ALTIMETER") || oxsMeasureValidationList[i][0].equals("ALTIMETER_2") )   // Only one Altitude DEFAULT
							&& oxsMeasureValidationList[i][3].equals("DEFAULT") ) {
						for ( int j = i+1 ; j <= TabData.getDataSentFieldNbr() ; j++ ) {
							if ( ( oxsMeasureValidationList[j][0].equals("ALTIMETER") || oxsMeasureValidationList[j][0].equals("ALTIMETER_2") )
									&& oxsMeasureValidationList[j][3].equals("DEFAULT") ) {
								MainP.sentDataValid = false ;
								MainP.messageList.append( "- " + oxsMeasureValidationList[j][1] + " Telemetry data field can't be set to DEFAULT as it's" ) ;
								MainP.messageList.append( "  already used by " + oxsMeasureValidationList[i][1] + " measurement !" ) ;
							}
						}
					} else if ( ( oxsMeasureValidationList[i][0].equals("VERTICAL_SPEED") || oxsMeasureValidationList[i][0].equals("VERTICAL_SPEED_2")   // Only one V.Speed DEFAULT
							|| oxsMeasureValidationList[i][0].equals("PRANDTL_DTE") || oxsMeasureValidationList[i][0].equals("PPM_VSPEED") )
							&& oxsMeasureValidationList[i][3].equals("DEFAULT") ) {
						for ( int j = i+1 ; j <= TabData.getDataSentFieldNbr() ; j++ ) {
							/*if ( oxsMeasureValidationList[j][3].equals("Vertical Speed") ) {                    // V.Speed already used by DEFAULT
			                    sentDataValid = false ;
			                    messageList.append( "- Vertical Speed Telemetry data field is not available as it's already" ) ;
			                    messageList.append( "  used by " + oxsMeasureValidationList[i][1] + " measurement !" ) ;
			                  }*/
							if ( ( oxsMeasureValidationList[j][0].equals("VERTICAL_SPEED") || oxsMeasureValidationList[j][0].equals("VERTICAL_SPEED_2")
									|| oxsMeasureValidationList[j][0].equals("PRANDTL_DTE") || oxsMeasureValidationList[j][0].equals("PPM_VSPEED") )
									&& oxsMeasureValidationList[j][3].equals("DEFAULT") ) {
								MainP.sentDataValid = false ;
								MainP.messageList.append( "- " + oxsMeasureValidationList[j][1] + " Telemetry data field can't be set to DEFAULT as it's" ) ;
								MainP.messageList.append( "  already used by " + oxsMeasureValidationList[i][1] + " measurement !" ) ;
							}
						}
					}
	
					oxsMeasureCountSPORT[(int) sentDataFieldNb][(int) sPortDataFieldNb] ++ ;
					oxsMeasureCountSPORT[TabData.getSentDataList().length][(int) sPortDataFieldNb] ++ ;
				}
				/*}*/ /*else if ( oxsMeasureValidationList[i][0].equals("CELLS") ) {
			          sentDataValid = false ;
			          messageList.append( "- " + oxsMeasureValidationList[i][1] + " is not activated in voltage tab. !" ) ;
			      } else if ( oxsMeasureValidationList[i][0].equals("PPM_VSPEED") ) {
			          sentDataValid = false ;
			          messageList.append( "- PPM option is not activated in Vario tab. !" ) ;
			      } else if ( oxsMeasureValidationList[i][0].equals("PRANDTL_DTE") || oxsMeasureValidationList[i][0].equals("PRANDTL_COMPENSATION") ) {
			          sentDataValid = false ;
			          messageList.append( "- " + oxsMeasureValidationList[i][1] + " needs Vario AND Air Speed sensor !" ) ;
			      } else {
			          sentDataValid = false ;
			          messageList.append( "- " + oxsMeasureValidationList[i][1] + " needs the sensor to be active !" ) ;
			          println("nom = " + sentDataFieldName) ;
			          println("result = " + OXSdata.isInList( sentDataFieldName )) ;
			      }*/
			}
		}
	
		// ***  Duplicate tests  ***
		if ( cp5.getGroup("protocolChoice").getValue() == 1 ) {          //  HUB protocol => maybe not up to date since OXSC v2.0
			for ( int k = 1 ; k <= TabData.getSentDataList().length ; k++ ) {
				/*
			      if ( k == sentDataList.length ) {
			        print( "TOTAL: " ) ;
			      } else {
			        print( sentDataList[k][1] + ": " ) ;
			      }
				 */
				for ( int l = 1 ; l < TabData.getHubDataList().length ; l++ ) {
					//print( oxsMeasureCountHUB[k][l] + " " ) ;
					if ( oxsMeasureCountHUB[k][l] > 1 && k < TabData.getSentDataList().length ) {
						MainP.sentDataValid = false ;
						MainP.messageList.append( "- " + TabData.getSentDataList()[k][1] + " can't be sent " + oxsMeasureCountHUB[k][l] + "X to the same " +  TabData.getHubDataList()[l][1] + " field !" ) ;
					}
					if ( k == TabData.getSentDataList().length && l > 1 && oxsMeasureCountHUB[TabData.getSentDataList().length][l] > 1 ) {
						MainP.sentDataValid = false ;
						MainP.messageList.append( "- Different measurements can't be sent to the same " +  TabData.getHubDataList()[l][1] + " field !" ) ;
					}
				}
				//println("");
			}
			if ( oxsMeasureCountHUB[2][1] == 1 && oxsMeasureCountHUB[TabData.getSentDataList().length][2] >= 1 ) {  // Test VERTICAL_SPEED  default/VSpd
				MainP.sentDataValid = false ;
				MainP.messageList.append( "- Vertical Speed not available as it's already used by VERTICAL_SPEED !" ) ;
			}
			if ( oxsMeasureCountHUB[5][1] == 1 && oxsMeasureCountHUB[TabData.getSentDataList().length][3] >= 1 ) {  // Test CURRENTMA  default/Curr
				MainP.sentDataValid = false ;
				MainP.messageList.append( "- Current not available as it's already used by CURRENTMA !" ) ;
			}
			if ( oxsMeasureCountHUB[14][1] == 1 && oxsMeasureCountHUB[TabData.getSentDataList().length][7] >= 1 ) {  // Test RPM default/RPM
				MainP.sentDataValid = false ;
				MainP.messageList.append( "- RPM not available as it's already used by RPM !" ) ;
			}
		} else if ( cp5.getGroup("protocolChoice").getValue() == 2 ) {         //  SPORT protocol
			for ( int k = 1 ; k <= TabData.getSentDataList().length ; k++ ) {
				/*
			      if ( k == sentDataList.length ) {
			        print( "TOTAL: " ) ;
			      } else {
			        print( sentDataList[k][1] + ": " ) ;
			      }
				 */
				for ( int l = 1 ; l < TabData.getsPortDataList().length ; l++ ) {
					//print( oxsMeasureCountSPORT[k][l] + " " ) ;
					if ( oxsMeasureCountSPORT[k][l] > 1 && k < TabData.getSentDataList().length ) {
						MainP.sentDataValid = false ;
						MainP.messageList.append( "- " + TabData.getSentDataList()[k][1] + " can't be sent " + oxsMeasureCountSPORT[k][l] + "X to the same " +  TabData.getsPortDataList()[l][1] + " field !" ) ;
					}
					if ( k == TabData.getSentDataList().length && l > 1 && oxsMeasureCountSPORT[TabData.getSentDataList().length][l] > 1 ) {
						MainP.sentDataValid = false ;
						MainP.messageList.append( "- Different measurements can't be sent to the same " +  TabData.getsPortDataList()[l][1] + " field !" ) ;
					}
				}
				//println("");
			}
			if ( (oxsMeasureCountSPORT[1][1] == 1 || oxsMeasureCountSPORT[4][1] == 1) && oxsMeasureCountSPORT[TabData.getSentDataList().length][2] >= 1 ) {  // Test ALTITUDEs  default/Alt
				MainP.sentDataValid = false ;
				MainP.messageList.append( "- Altitude Telemetry data field is not available as it's already used by" ) ;
				MainP.messageList.append( "  Altitude 1 or 2 measurement !" ) ;
			}
			if ( (oxsMeasureCountSPORT[2][1] == 1 || oxsMeasureCountSPORT[5][1] == 1 || oxsMeasureCountSPORT[9][1] == 1
					|| oxsMeasureCountSPORT[11][1] == 1) && oxsMeasureCountSPORT[TabData.getSentDataList().length][3] >= 1 ) {  // Test VERTICAL_SPEEDs  default/VSpd
				MainP.sentDataValid = false ;
				MainP.messageList.append( "- Vertical Speed Telemetry data field is not available as it's already" ) ;
				MainP.messageList.append( "  used by Vertical Speed measurement !" ) ;
			}
			if ( oxsMeasureCountSPORT[12][1] == 1 && oxsMeasureCountSPORT[TabData.getSentDataList().length][4] >= 1 ) {  // Test CURRENTMA  default/Curr
				MainP.sentDataValid = false ;
				MainP.messageList.append( "- Current Telemetry data field is not available as it's already used by" ) ;
				MainP.messageList.append( "  Current (mA) measurement !" ) ;
			}
			if ( oxsMeasureCountSPORT[21][1] == 1 && oxsMeasureCountSPORT[TabData.getSentDataList().length][8] >= 1 ) {  // Test RPM default/RPM
				MainP.sentDataValid = false ;
				MainP.messageList.append( "- RPM Telemetry data field is not available as it's already used by" ) ;
				MainP.messageList.append( "  RPM measurement !" ) ;
			}
	
		}
	
		if ( oxsMeasureCount == 0 ) {
			MainP.sentDataValid = false ;
			MainP.messageList.append( "- You don't have any OXS measurement set !" ) ;
		}
	
	}

	public static void validateVersion() throws IOException { // TODO first crashes

		FileReader verFileReader = new FileReader(versionFile);
		verFileReader.read(version);

		if (version == null) {
			MainP.versionValid = 1;
			// messageList.append("") ;
			// messageList.append("                                              -------------------")
			// ;
			MainP.messageList.append("");
			MainP.messageList
					.append("                ** The Configurator can't find OXS version number **");
			MainP.messageList
					.append("                **      Configuration file may not be compatible...    **");

			// println("no version file") ;
		} else if (version.charAt(1) == MainP.oxsCversion.charAt(1)) {
			MainP.messageList.append("Configuration file will be written to:");
			MainP.messageList.append(MainP.outputConfigDir);
			MainP.messageList.append("");
			MainP.messageList
					.append("                       ! If the file already exists, it will be replaced !");
			// println("OXS and the Configurator are compatible,") ;
			// println("OXS version = " + version[0] + " and OXSC version = " +
			// oxsCversion) ;

		} else {
			MainP.versionValid = 0;
			MainP.messageList.append("            ** The Configurator "
					+ MainP.oxsCversion + " isn't compatible with OXS "
					+ version + " **");
			MainP.messageList.append("");
			MainP.messageList
					.append("         You may go to \"https://code.google.com/p/openxsensor/\" and");
			MainP.messageList
					.append("       download the latest version of both OXS and OXS Configurator.");
			// println("OXS version " + version[0]) ;
		}
		verFileReader.close();
	}
	
}


	
	
	
	
	
	
	
	
