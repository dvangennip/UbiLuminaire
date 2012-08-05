//package UbiLuminaire;

public class Attribute {
	
	/* Variables */
	
	private String name;
	private float activation;
	private float[] sensorConnections;
	
	/* Constructor */
	
	public Attribute (String n, float[] sc) {
		
		this.name = n;
		this.activation = 0.0f;
		this.sensorConnections = sc;
	}
	
	/* Methods */
	
	/**
	 * Method calculates activation strength based on sensor values.
	 * Use of synchronised modifier to prevent activation value to be read while updating it.
	 * int[] sensorValues An array of sensor values
	 */
	public synchronized float calcActivation(float[] sensorValues) {
		
		// first reset activation
		activation = 0.0f;
		
		for (int i = 0; i < sensorConnections.length; i++) {
			activation = activation + sensorConnections[i] * sensorValues[i];
		}
		
		return activation;
	}
	
	public synchronized float getActivation() {
		return activation;
	}
	
	public String getName() {
		return name;
	}
}