//package UbiLuminaire;

//import processing.core.*;

public class Luminaire extends Thread {
  
  /* Class variables */
  
  private UbiLuminaire p;
  private boolean demoMode;
  private int goalLevel = 0;
  private int ambientLevel = 0;
  private int outputLevel = 0;
  private float outputLevelFloat = 0.0f;
  
  private int ambMinValue = 0;
  private int ambMaxValue = 1900;
  
  /* Constructor */
  
  public Luminaire (UbiLuminaire p, boolean demo) {
    this.p = p;
    this.demoMode = demo;
  }
  
  /* Required method from Thread's Runnable implementation */
  
  public void run() {
    
    // loop forever
    while (true) {
    	
	    // try to sleep
		synchronized (this) {
			try {
				// TODO sleep for a minute, then check ambient level
			    this.wait();
		    }
		    catch (java.lang.InterruptedException iex) {
		    	System.out.println("Handler: Interrupted while trying to wait");
		    }
		    catch (java.lang.IllegalMonitorStateException ims) {
		    	System.out.println("Handler: Illegal Monitor State Exception while trying to wait");
		    }
		}
		
		// UPDATE
		
		// TODO wrap the update logic in a while loop
		// as it should only be called when a new goalLevel is set
		// adjustments should loop here, not only due to draw() calls in main class
		
		// set the desired ambient level (now directly derived from goal)
	    int desiredAmbientLevel = goalLevel;
		
		// get the ambient light level
		// get fake data in demo mode
		if (demoMode) {
			
			// instead of sensor data, fake the ambient level
			// e.g. useful for daylight testing purposes.
			// multiplied by 2.2 to approximate 100 percent brightness for the highest context desires
			float dOutputGoal = p.constrain( 2.2f * convertToPercentageLevel( desiredAmbientLevel ), 0, 100);
			float dDelta = outputLevelFloat - dOutputGoal;
			
			if (dDelta == 0) {
				// do nothing
			} else if (dDelta > 0) {
				// output too high, too little ambient
				ambientLevel = desiredAmbientLevel + p.round(p.pow( 1000000 * p.abs(dDelta), 0.25f));
			} else if (dDelta < 0) {
				// output too low, too much ambient
				ambientLevel = desiredAmbientLevel - p.round(p.pow( 1000000 * p.abs(dDelta), 0.25f));
			}
			ambientLevel = p.constrain( ambientLevel, 0, ambMaxValue);
		} else {
			// get sensor data
			ambientLevel = Math.round( p.sAmbiLight.getAmbientLightValue() );
		}
	    
	    // translate desired percentage level into output level
	    // based on desired ambient level
	    
    	// the core idea is that increased illumination will alter the ambient level as well
    	// this means it only activates light if not enough ambient light is available
	    // during the day the luminaire will thus not turn on -> enough light available.
	    // NOTE: if the output does not influence the input sensors this system will go out of control...
    	
    	// get difference in goal and current
		int delta = desiredAmbientLevel - ambientLevel;
		
		// check for zero value to avoid dividing by it
		// no adjustment needed anyway
		if (delta != 0) {
			// if gap is larger adjust more
			// make range independent of fps
			float multiplier = 100.0f / p.fps; // around 10 for 10 fps
			// diff / range = 0..1
			float magnitude = multiplier * Math.abs(delta) / (1.0f * (ambMaxValue-ambMinValue));
			
			// first calc output as float, so even small magnitude (< 1) has some effect
			// does not ignore the cases where delta is very little,
			// and magnitude should be small as well (not >= 1).
			// then convert to integer for output
			// delta / abs(delta) scales to 1 but keeps direction +/-
			outputLevelFloat = outputLevelFloat + magnitude * (delta / Math.abs(delta));
			outputLevel = Math.round( outputLevelFloat );
			// constrain outputLevel 0 - 100
			if (outputLevel < 0) {
			    outputLevel = 0;
			    outputLevelFloat = 0.0f;
		    } else if (outputLevel > 100) {
		    	outputLevel = 100;
			    outputLevelFloat = 100.0f;
		    }
	    	
	    	// give some feedback
	    	//System.out.println("Ambient: "+ambientLevel+"   Desired: "+desiredAmbientLevel+"   Delta: "+delta+"   Magn: "+magnitude+"   OUT: "+outputLevel);
		}
    }
  }
  
  /* Methods */
  
  public boolean getDemoStatus() {
    return demoMode;
  }

  public void setDemoStatus(boolean demo) {
    demoMode = demo;
  }
  
  public int getGoalLevel() {
    return goalLevel;
  }
  
  public void setGoalLevel(int goal) {
    
    // constrain value between 0 - max level
    if (goal < 0) {
      goalLevel = 0;
    } else if (goal > ambMaxValue) {
      goalLevel = ambMaxValue;
    } else {
      goalLevel = goal;
    }
  }
  
  public int getOutputLevel() {
    return outputLevel;
  }
  
  public float getOutputLevelF() {
    return outputLevelFloat;
  }
  
  public int getAmbientLevel() {
    return ambientLevel;
  }
  
  public int convertToAmbientLevel(int input) {
  	
  	int in = p.constrain( input, 0, 100); // input percentage
  	
  	int out = Math.round( (in/100.0f) * (ambMaxValue - ambMinValue) );
  	
  	return out;
  }
  
  public int convertToPercentageLevel(int input) {
  	
  	// input ambient level
 	// constrain min - max values if needed
  	int in = p.constrain(input, ambMinValue, ambMaxValue);

  	int out = Math.round( 100 * ((in-ambMinValue) / (1.0f * ambMaxValue)) );
  	
  	return out;
  }
}
