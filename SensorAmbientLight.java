//package UbiLuminaire;

/**
 * Implements ambient light sensor
 */
public class SensorAmbientLight extends Sensor {
	
	/* Variables */
	
	private final static int numberOfValues = 1;
	
	private float ambientLightLevel;
	
	/* Constructor */
	
	public SensorAmbientLight (UbiLuminaire p, int t) {
		
		super(p,"Ambient light sensor",t,numberOfValues);
		
		setValueNames( new String[] {"AmbiLight"} );
	}
	
	/* Methods */
	
	/**
	 * Updates sensor output values @getValues()
	 */
	public void update() {
		
		// use macbook ambient light sensors
	  	int[] lmu_values = lmu.LmuTracker.getLMUArray();
	  	ambientLightLevel = ( lmu_values[0] + lmu_values[1] ) / 2;
	  	
	  	// set value as -1,1 range
	  	// adjust for ambient light value
		float transformedValue = (p.lumi.convertToPercentageLevel( p.round( ambientLightLevel ) ) / 50.0f)-1;
	  	setValue(0, transformedValue);
	  	
	  	// alternative method: use camera brightness average
	}
	
	/**
	 * @return Ambient light level in original sensor values
	 */
	public float getAmbientLightValue() {
		return ambientLightLevel;
	}
}