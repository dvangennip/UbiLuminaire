//package UbiLuminaire;

public class ContextManager {
	
	/* Variables */
	
	private Context[] contexts;
  	private int contextCurrent;
  	private Attribute[] attributes;
	
	/* Constructor */
	
	public ContextManager () {
		
		// NOTE: the sum of absolute connection strength values should be equal to 1
		
		// init attributes
		attributes = new Attribute[5];
		// constructor: name, float[] sensor connection strengths
	    // NOTE: the number of attribute strengths should be equal to the number of sensors defined
		attributes[0] = new Attribute("Movement",  new float[] { 0.0f,  0.2f,  0.0f,  0.1f,  0.2f, 0.0f,  0.4f,  0.1f });
		attributes[1] = new Attribute("Intensity", new float[] { 0.0f,  0.3f,  0.1f, -0.05f, 0.2f, 0.05f, 0.3f,  0.0f });
		attributes[2] = new Attribute("People",    new float[] { 0.0f,  0.2f,  0.05f, 0.05f, 0.2f, 0.3f,  0.2f,  0.0f });
		attributes[3] = new Attribute("Quietness", new float[] { 0.0f, -0.3f, -0.1f, -0.1f, -0.3f, 0.0f, -0.1f, -0.1f });
		attributes[4] = new Attribute("Variety",   new float[] { 0.0f,  0.1f,  0.1f,  0.35f, 0.0f, 0.0f,  0.1f,  0.35f });
		
		// init contexts
	    contexts = new Context[4];
	    contextCurrent = 0; // start at default
	    
	    // constructor: name, desired light level, float[] attribute connection strengths
	    // NOTE: the number of attribute strengths should be equal to the number of attributes defined
	    contexts[0] = new Context("No activity", 0, new float[] { -0.2f, -0.3f, -0.2f,   0.3f,  0.0f });
	    contexts[1] = new Context("Desk work", 866, new float[] {  0.2f,  0.1f, -0.3f,   0.2f,  0.2f });
	    contexts[2] = new Context("Social",    600, new float[] {  0.1f,  0.2f,  0.25f, -0.35f, 0.1f });
	    contexts[3] = new Context("Media use", 100, new float[] { -0.2f, -0.1f,  0.4f,  -0.1f, -0.2f });
	}
	
	/* Methods */
	
	/**
	 * Method updates activation levels and selects the highest activated context as current.
	 * @return Boolean value that is true when context has changed, false if decision remains the same
	 */
	public synchronized boolean update(float[] sensorValues) {
		
		// first, update attribute activation strength
		// and save values in temporary array
		float[] attributeActivation = new float[attributes.length];
		
		for (int a = 0; a < attributes.length; a++) {
			attributeActivation[a] = attributes[a].calcActivation(sensorValues);
		}
		
		// second step, update context activation strength
		// and get the highest activated context
		float highestActivationLevel = 0.0f;
		int highestActivationIndex = 0;
		
		for (int c = 0; c < contexts.length; c++) {
			// calc activation based on fresh attribute data
			float ctxActivation = contexts[c].calcActivation(attributeActivation);
			
			// compare with current highest, and select if higher
			if (ctxActivation > highestActivationLevel) {
				highestActivationLevel = ctxActivation;
				highestActivationIndex = c;
			}
		}
		
		// check if selected context is different from previous one
		// return as appropriate
		if (highestActivationIndex != contextCurrent) {
			contextCurrent = highestActivationIndex;
			return true;
		}
		return false;
	}
	
	/**
	 * Synchronized with update() to avoid getting the wrong context while updating
	 */
	public synchronized Context getCurrentContext() {
		return contexts[contextCurrent];
	}
	
	public synchronized int getCurrentContextIndex() {
		return contextCurrent;
	}
	
	public Context[] getContexts() {
		return contexts;
	}
	
	public Attribute[] getAttributes() {
		return attributes;
	}
	
}