//package UbiLuminaire;

/**
 * Implements audio sensor
 */
public class SensorAudio extends Sensor {
	
	/* Variables */
	
	private final static int numberOfValues = 3;
	
	float[] amplitudeSeries;
	float[] frequencySeries;
	float avgAmplitude;
	float avgVariety;
	float avgFrequency;
	
	private boolean initAudioLib = false;
	private ddf.minim.Minim minim;
	private ddf.minim.AudioInput lineIn;
	private ddf.minim.effects.LowPassFS lowpass;
	private ddf.minim.analysis.FFT fft;
	
	/* Constructor */
	
	public SensorAudio (UbiLuminaire p, int t) {
		
		super(p,"Audio sensor",t,numberOfValues);
		
		setValueNames( new String[] {"Aampli","Afreq","Avar"} );
		
	    amplitudeSeries = new float[t];
	    frequencySeries = new float[t];
	    
	    // init audio library
	    minim = new ddf.minim.Minim( p ); // requires ref to PApplet
	    //minim.debugOn();
	  
	    // get a line in from Minim, default bit depth is 16
	    lineIn = minim.getLineIn( ddf.minim.Minim.STEREO, 512);
	    // add filter
	    lowpass = new ddf.minim.effects.LowPassFS(2000, lineIn.sampleRate()); // cutoff at 2 kHz
	    lineIn.addEffect(lowpass);
	  
	    // init fast fourier analyser
	    fft = new ddf.minim.analysis.FFT(lineIn.bufferSize(), lineIn.sampleRate());
	    
	    initAudioLib = true;
	}
	
	/* Methods */
	
	/**
	 * Updates sensor output values @getValues()
	 * @override
	 */
	public void update() {
		
		// analysis
		
		 // get some fresh new data, yummy!
		 fft.forward( lineIn.mix );
		 
		 // get the frequency of the highest amplitude band
		 // intended to indicate the most interesting frequency band
		 float highestAmpli = 0.0f;
		 int highestIndex = 0;
		 for (int i = 0; i < fft.specSize(); i++) {
		   // get amplitude for each band
		   float a = fft.getBand(i);
		   
		   if (a > highestAmpli) {
		     highestAmpli = a;
		     highestIndex = i;
		   }
		 }
		 float tempFreq = fft.indexToFreq(highestIndex);
		 
		 // calc average frequency
		 // first, update array by appending one to the array minus the first value
		 frequencySeries = updateSeries(frequencySeries, tempFreq);
		 float[] tempStatsFreq = StDev(frequencySeries);
		 avgFrequency = tempStatsFreq[0];
		 
		 // calc amplitude
		 // uses the highest amplitude derived above
		 amplitudeSeries = updateSeries(amplitudeSeries, highestAmpli);
		 float[] tempStatsAmpli = StDev(amplitudeSeries);
		 avgAmplitude = tempStatsAmpli[0];
		 
		 // calc variety
		 // use the standard deviation of frequency and amplitude series
		 // because the sources are already longitudinal data, no need here to average over time
		 // values are balanced due to the uneven range of the values
		 avgVariety = 0.5f * (tempStatsFreq[2] / 1500.0f) + 0.5f * (tempStatsAmpli[2] / 35.0f);
		 
		 // generate output values in -1,1 range
		 // 0 min, 3-5 normal, 35, used, 50 practical max
		 setValue( 0, p.constrain( ( p.pow( (avgAmplitude/3.0f), 0.33f) - 1.3f), -1,1) );
		  // 0 - 1500 used, 2000 max
		 setValue( 1, p.constrain( (avgFrequency / 750.0f)-1, -1,1) );
		 setValue( 2, p.constrain( (avgVariety * 2.5f) - 1, -1,1) );
	}
	
	/**
	 * @override
	 */
	public void draw() {
		 
		 // draw the waveforms
		 p.stroke(0,50,100,100);
		 
		  // adjust wave width to screen width
		 float wm = 1.0f * p.width / lineIn.bufferSize();
		 
		 for(int i = 0; i < lineIn.bufferSize() - 1; i++) {
		 	
		   p.line(i*wm, p.height/2 + lineIn.mix.get(i)*80, i*wm+1, p.height/2 + lineIn.mix.get(i+1)*80);
		 }
		 
		 // draw textual feedback
		 p.noStroke();
		 p.fill(0,50,100,100);
		 p.textAlign(p.LEFT, p.CENTER);
		 p.textFont(p.font);
		 p.text("Amplitude: "+getValue(0), p.width/2-80, p.height/2+60);
		 p.text("Frequency: "+getValue(1), p.width/2-80, p.height/2+80);
		 p.text("Variety: "+getValue(2), p.width/2-59, p.height/2+100);
	}
	
	/**
	 * @override
	 */
	public void cleanup() {
		// always close Minim audio classes when you are done with them
  	 	if (initAudioLib) {
		    lineIn.close();
		    minim.stop();
	  	}
	}
}
