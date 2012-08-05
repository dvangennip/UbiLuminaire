//package UbiLuminaire;

public class Context {
	
	/* Variables */
	
	private String name;
	private int desiredLightLevel;
	private float activation;
	private float[] attrConnections;
	
	/* Constructor */
	
	public Context (String n, int dl, float[] ac) {
		
		this.name = n;
		this.desiredLightLevel = dl;
		this.activation = 0.0f;
		this.attrConnections = ac;
	}
	
	/* Methods */
	
	/**
	 * Method calculates activation strength based on attribute activation strength.
	 * Use of synchronised modifier to prevent activation value to be read while updating it.
	 * int[] attrActivation An array of attribute activation levels
	 */
	public synchronized float calcActivation(float[] attrActivation) {
		
		// first reset activation
		activation = 0.0f;
		
		for (int i = 0; i < attrConnections.length; i++) {
			activation = activation + attrConnections[i] * attrActivation[i];
		}
		
		return activation;
	}
	
	public synchronized float getActivation() {
		return activation;
	}
	
	public String getName() {
		return name;
	}
	
	public int getDesiredLightLevel() {
		return desiredLightLevel;
	}
}