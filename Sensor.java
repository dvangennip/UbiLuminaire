//package UbiLuminaire;

/**
 * Sensor.java is meant to be extended by actual sensor implementations
 */
public class Sensor {
	
	/* Variables */
	
	public UbiLuminaire p;
	private String name;
	private int timePeriod; // in seconds
	private float[] values;
	private String[] valueNames;
	
	/* Constructor */
	
	public Sensor (UbiLuminaire p, String n, int t, int numberOfValues) {
		
		this.p = p;
		this.name = n;
		this.timePeriod = t;
		this.values = new float[numberOfValues];
		this.valueNames = new String[numberOfValues];
	}
	
	/* Methods */
	
	/**
	 * Updates sensor output values @getValues()
	 * Methods needs be overriden in extending classes
	 */
	public void update() {
		// empty
	}
	
	/**
	 * Methods needs be overriden in extending classes
	 */
	public void draw() {
		// empty
	}
	
	/**
	 * Methods needs be overriden in extending classes
	 * Main class will ask all sensors to clean up, stop threads, close connections, etc.
	 */
	public void cleanup() {
		// empty
	}
	
	public UbiLuminaire p() {
		return p;
	}
	
	public String getName() {
		return name;
	}
	
	public int getTimePeriod() {
		return timePeriod;
	}
	
	public float[] getValues() {
		return values;
	}
	
	public float getValue(int index) {
		return values[index];
	}
	
	public int getNumberOfValues() {
		return values.length;
	}
	
	public void setValues(float[] nv) {
		values = nv;
	}
	
	public void setValue(int index, float nv) {
		values[index] = nv;
	}
	
	public String[] getValueNames() {
		return valueNames;
	}
	
	public void setValueNames(String[] vnms) {
		if (vnms.length == valueNames.length) {
			valueNames = vnms;
		}
	}

	/**
	 * Appends a value at the end and drops one at the front, so series remains of same length
	 */
	public float[] updateSeries(float[] series, float newValue) {
		series = p().append( p().subset(series,1), newValue);
		return series;
	}
	
	public float sum(float[] series) {
		float sum = 0.0f;
		
		for (int i = 0; i < series.length; i++) {
			sum += series[i];
		}
  		
  		return sum;
  	}
  	
  	public float average(float[] series) {
  		return sum(series) / timePeriod;
  	}

	/**
	* @return Array with at [0] the mean, and at [1] the variance, and at [2] the SD
	*/
	public float[] StDev(float[] series) {
	 
		float[] results = {0.0f, 0.0f, 0.0f};
	 
		// mean
		float mean = average(series);
		results[0] = mean;
		
		// variance
		float varsum = 0.0f;
		for (int i = 0; i < series.length; i++) {
		  varsum += Math.pow( series[i] - mean, 2); // (X - mu)^2
		}
		results[1] = varsum / series.length;
		
		// stdev
		results[2] = (float) Math.sqrt(results[1]);
		
		return results;
	}
}