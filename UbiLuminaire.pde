/**
 * UbiLuminaire - ubiquitous ambient room illumination
 *
 * explanation here
 *
 * For Ubiquitous Computing course (ID2012) @ KTH, Spring 2011
 *
 * @author Doménique van Gennip, dapvg@kth.se / domo@sinds1984.nl
 */

//package UbiLuminaire;

import fullscreen.*; // gives a fullscreen view if resolution is matching
import lmu.*; // for macbook ambient light sensor
import ddf.minim.*; // minim audio lib
import ddf.minim.effects.*;
import ddf.minim.analysis.*;
import processing.video.*; // quicktime video lib
import s373.flob.*; // fast flood fill blob tracking lib

public class UbiLuminaire extends PApplet {
  
  /* Global variables */
  
  // adjust these for performance
  private boolean useFullscreen = false;
  private int screenNumber = 0; // 0 - primary, 1 - secondary
  private boolean runAsDemo = false;
  private int timePeriod = 5; // sensors average over this period (in seconds)
  public int fps = 10;
  
  private boolean showLightLevels = true; // key l
  private boolean showSensorAudio = false; // key m
  private boolean showSensorVideo = false; // key v
  private boolean showActivationLevels = true; // key a
  
  private FullScreen fs;
  public int compensation = 0;
  
  public Luminaire lumi;
  private ContextManager conman;
  private Sensor[] sensors;
  public SensorAmbientLight sAmbiLight;
  public SensorAudio sAudio;
  public SensorVideo sVideo;
  
  public PFont font;
  
  /* Setup method runs once */

  void setup() {
    if (useFullscreen) {
      size(1440,900);
      //size(1920,1080);
      fs = new FullScreen(this, screenNumber); // goes to indicated screen
      fs.enter();
    } else {
      size(800, 600);
    }
    frameRate( fps );
    
    font = createFont("Bender-Bold", 15, true); //loadFont("Bender-Bold-20.vlw");
    rectMode(CENTER);
    ellipseMode(CENTER);
    colorMode(HSB, 360, 100, 100, 100);
    smooth();
    
    // timePeriod is adjusted based on framerate
    timePeriod = timePeriod * fps;
    
    // init sensors
    sAmbiLight = new SensorAmbientLight(this,timePeriod);
    sAudio = new SensorAudio(this,timePeriod);
    sVideo = new SensorVideo(this,timePeriod);
    // add sensors to array for easier use
    sensors = new Sensor[3];
    sensors[0] = sAmbiLight;
    sensors[1] = sAudio;
    sensors[2] = sVideo;
    
    // init context manager
    conman = new ContextManager();
    
    // init Luminaire which controls the actual light output
    lumi = new Luminaire(this, runAsDemo);
    lumi.start();
  }
  
  /* Draw method runs continiously */
  
  void draw() {
  	
  	// update SENSOR READINGS
  	// plus GATHER all SENSOR VALUES in one array
  	
  	// init sensor value array
  	float[] sensorValues = new float[0];
  	String[] sensorValuesNames = new String[0];
  	
  	// go through the list of sensors
  	for (int i = 0; i < sensors.length; i++) {
  		// first, call for an update
  		sensors[i].update();
  		
  		// get values and combine with array
        sensorValues = concat(sensorValues, sensors[i].getValues());
        sensorValuesNames = concat(sensorValuesNames, sensors[i].getValueNames());
  	}
  	
  	// TODO temp mouse value
	float mY = (height-mouseY)/(1.0f*height) * 2 - 1; // range -1,1
	float mX = (width-mouseX)/(1.0f*width) * 2 - 1;
	// TODO remove
	//sensorValues = new float[] { mX, mY };
	//sensorValuesNames = new String[] { "mouseX", "mouseY" };
  	
  	// update CONTEXT UNDERSTANDING
  	
  	// returns true if context choice changes, based on these sensor values
  	boolean contextAdjusted = conman.update( sensorValues );
  	
  	// CONTEXT DETERMINED
  	// below it gets translated to luminaire goal level
  	
  	if (contextAdjusted) {
	  	
	  	// SETTING the LUMINAIRE GOAL level
	  	
	  	//println("New context: " + conman.getCurrentContext().getName() );
	  	//lumi.setGoalLevel( lumi.convertToAmbientLevel( newValue ) );
	  	lumi.setGoalLevel( conman.getCurrentContext().getDesiredLightLevel() );
  	}
	    
    // notify luminaire handler so it can adjust the light level
    // this is always done, as the ambient may change and needs be adjusted for
    // TODO only call when a new goal is set, Luminaire handler itself should take care of timely adjustments
	synchronized (lumi) {
		try {
			lumi.notify();
		}
		catch (java.lang.IllegalMonitorStateException ims) {
			System.out.println("Manager could not wake up Luminaire Handler");
		}
	}
	
	// END of LOGIC ------------
	
	// DRAW FEEDBACK
	
	// the background can be seen as the luminaire output
	background(45, 30, lumi.getOutputLevelF() ,100); // yellowish colour
	
	// compensate for menu bar issue
	// (not disappearing, thus creating a black band on the top)
    if (useFullscreen) {
        compensation = 40;
    } else {
    	compensation = 0;
    }
	
	// show audio sensor data
	if (showSensorAudio) {
		sAudio.draw();
	}
	
	// show video sensor data
	if (showSensorVideo) {
		sVideo.draw();
	}
	
	// show activation levels
	if (showActivationLevels) {
		int currentCTX = conman.getCurrentContextIndex();
		Context[] ctxs = conman.getContexts();
		Attribute[] attrs = conman.getAttributes();
		noStroke();
		textAlign(CENTER, CENTER);
        textFont(font);
		
		// contexts
		for (int i = 0; i < ctxs.length; i++) {
			int xPos = 150*i + (width/2 - 150*(ctxs.length-1)/2);
			int yPos = (height/2) - 170;
			int diameter = Math.round( 50 * (ctxs[i].getActivation()+1) );
			fill(0,50,100,100);
			ellipse(xPos, yPos, diameter, diameter);
			if (currentCTX == i) {
				fill(0,100,100,100); // highlight the currently selected context
			}
	        text( ctxs[i].getName() , xPos, yPos+70);
		}
		
		// attributes
		fill(0,75,100,100);
		for (int i = 0; i < attrs.length; i++) {
			int xPos = 150*i + (width/2 - 150*(attrs.length-1)/2);
			int yPos = height/2;
			int diameter = Math.round( 50 * (attrs[i].getActivation()+1) );
			ellipse(xPos, yPos, diameter, diameter);
			text( attrs[i].getName() , xPos, yPos+70);
		}
		
		// sensor values
		fill(0,100,100,100);
		for (int i = 0; i < sensorValues.length; i++) {
			int xSpread = 100;
			if (useFullscreen) {
				xSpread = 150;
			}
			int xPos = xSpread*i + (width/2 - xSpread*(sensorValues.length-1)/2);
			int yPos = (height/2) + 170;
			int diameter = Math.round( 50 * (sensorValues[i]+1) );
			ellipse(xPos, yPos, diameter, diameter);
			text( sensorValuesNames[i], xPos, yPos+70);
		}
	}
	
	// show light levels
	if (showLightLevels) {
		noStroke();
		fill(0,50,100,100);
		textAlign(LEFT, TOP);
        textFont(font);
        
        // for ambient level refer to Luminaire handler (instead of sensor)
        // because of changes due to demo modus
        if ( lumi.getDemoStatus() ) {
        	// simulated data
        	text("Ambient: " + Math.round( lumi.getAmbientLevel() ) + " (D)", 10, 10+compensation);
        } else {
        	// sensor data
        	text("Ambient: " + Math.round( sAmbiLight.getAmbientLightValue() ), 10, 10+compensation);
        }
        text(" Output: " + lumi.getGoalLevel() + " / " + lumi.getOutputLevel(), 10, 30+compensation);
	}
  }
  
  // Methods --------------------------------------------------------
  
  /**
   * KeyEvent handler
   */
  void keyPressed() {
  	if (key == ESC) {
  		// cleanup if necessary
	    stop();
	}
	if (key == 'd' || key == 'D') {
		// toggle demo mode
	    lumi.setDemoStatus( !lumi.getDemoStatus() );
    }
	if (key == 'l' || key == 'L') {
		// toggle
		showLightLevels = !showLightLevels;
	}
	if (key == 'm' || key == 'M') {
		// toggle
		showSensorAudio = !showSensorAudio;
	}
	if (key == 'a' || key == 'A') {
		// toggle
		showActivationLevels = !showActivationLevels;
	}
	if (key == 'v' || key == 'V') {
		// toggle
		showSensorVideo = !showSensorVideo;
	}
	if(key == 'b' || key == 'B') {
	    // set new background for video sensor
	    sVideo.setBackgroundImage();
	}
	if(key == 'n' || key == 'N') {
	    // toggle video motion detection method
	    sVideo.toggleVideoDetectionMethod();
	}
  }
  
  /*
   * @override Clean up and exit
   */
  public void stop() {
  	
  	// ask all sensors to clean up
  	for (int i = 0; i < sensors.length; i++) {
  		// first, call for an update
  		sensors[i].cleanup();
  	}
  	
  	// NOTE: because of override the super stop() method needs be called as well
	super.stop();
	exit(); // Exit JVM
  }
  
  // ----------------------------------------------------------------
  /* No need to adjust anything below here */
  
  /* Constructor */
  
  public UbiLuminaire() {}
  
  /* Main method */
  
  public static void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#FFFFFF", "UbiLuminaire" });
  }
}
