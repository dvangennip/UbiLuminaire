//package UbiLuminaire;

/**
 * Implements video sensor
 */
public class SensorVideo extends Sensor {
	
	/* Variables */
	
	private final static int numberOfValues = 4;
	
	private float[] presenceSeries;
	private float[] numOfItemsSeries;
	private float[] intensitySeries;
	private float avgPresence;
	private float avgNumOfItems;
	private float avgIntensity;
	private float avgVariety;
	
	private processing.video.Capture video;
	private s373.flob.Flob flob;
	private java.util.ArrayList blobs;
	private int backgroundResetCounter = 0;
	private int outWidth;
	private int outHeight;
	
	/* Constructor */
	
	public SensorVideo (UbiLuminaire p, int t) {
		
		super(p,"Video sensor",t,numberOfValues);
		
		setValueNames( new String[] {"Vpresence","Vblobs","Vintens","Vvar"} );
		
	    presenceSeries = new float[t];
	    numOfItemsSeries = new float[t];
	    intensitySeries = new float[t];
	    
	    // video params
	    outWidth = p.width;
	    outHeight = p.height;
		int tresh = 25;
		int fade = 10;
		int om = 0; // 0 or 1 (background or frame differencing)
		int videores=128;
		int videotex=3; // 0-3
		
		// get camera signal
		video = new processing.video.Capture( p, videores, videores, p.fps);
		
		// init blob tracker
		flob = new s373.flob.Flob( video, outWidth, outHeight);
		flob.setOm(om);
		flob.setThresh(tresh);
		flob.setSrcImage(videotex);
		flob.settrackedBlobLifeTime(fade);
	}
	
	/* Methods */
	
	/**
	 * Updates sensor output values @getValues()
	 * @override
	 */
	public void update() {
		
		// get video
		 
		if ( video.available() ) {
			video.read();
			blobs = flob.tracksimple( flob.binarize( video ) );    
		}
		
		// ANALYSIS
		
		// get presence
		
		float presence = flob.getPresencef();
		
		// if presence is very high or very low, the background is likely incorrect
		// function uses a counter to avoid continuous reseting
		if (presence < 0.03f || presence > 0.85f) {
			backgroundResetCounter++;
			
			if (backgroundResetCounter > 10 * p.fps) {
			  backgroundResetCounter = 0;
			  setBackgroundImage();
			}
		} else {
			backgroundResetCounter = 0;
		}
		
		// number of blobs detected
		
		int numOfItems = blobs.size();
		
		// calc intensity
		
		float intensity = 0.0f;
		
		for(int i = 0; i < numOfItems; i++) {
		
			s373.flob.trackedBlob tb = flob.getTrackedBlob(i);
			
			// movement vector length
			// uses the difference in previous and current vector length,
			// as this seems a better indicator of movement
			float oldVectorLength = (float) Math.sqrt( Math.pow(tb.prevelx, 2) + Math.pow(tb.prevely, 2) );
			float newVectorLength = (float) Math.sqrt( Math.pow(tb.velx, 2) + Math.pow(tb.vely, 2) );
			float vectorLengthDiff = newVectorLength - oldVectorLength;
			
			// intensity, as function of velocity, size and lifetime
			// inclusion of lifetime due to reduced impact of 'flickering' in blob detection
			float blobIntensity = vectorLengthDiff * (float) Math.log(tb.pixelcount); // * tb.lifetime;
			
			//println(i+": "+round(vectorLengthDiff)+"\t"+tb.pixelcount+"\t"+round(blobIntensity) );
			
			// add intensity to tally
			intensity += Math.abs(blobIntensity);
		}
		// convert to 0..1 float and constrain by an upper bound
		intensity = p.constrain( (intensity / 550), 0, 1);
		
		// UPDATE AVERAGES
  
		// calc average presence
		// first, update array by appending one to the array minus the first value
		presenceSeries = updateSeries(presenceSeries, presence);
		float[] tempStatsPresence = StDev(presenceSeries);
		avgPresence = tempStatsPresence[0];
		
		// calc average numOfItems
		// uses the highest numOfItems derived above
		numOfItemsSeries = updateSeries(numOfItemsSeries, numOfItems);
		float[] tempStatsNItems = StDev(numOfItemsSeries);
		avgNumOfItems = tempStatsNItems[0];
		
		// calc average intensity
		intensitySeries = updateSeries(intensitySeries, intensity);
		float[] tempStatsIntensity = StDev(intensitySeries);
		avgIntensity = tempStatsIntensity[0];
		
		// calc variety
		// use the standard deviation of presence, numOfItems and intensity series
		// because the sources are already longitudinal data, no need here to average over time
		// values are balanced due to the uneven range of the values
		avgVariety = 0.3f * p.sqrt(tempStatsPresence[2] * 4.0f);
		avgVariety += 0.3f * p.pow( (tempStatsNItems[2] / 2.0f), 0.33f);
		avgVariety += 0.4f * p.pow( (tempStatsIntensity[2] * 6.0f), 0.4f);
		// avgVariety has now a range of 0 - 2
		
		// GENERATE OUTPUT VALUES transform to fit well into -1,1 range
		
		// 0 - 1, 0.25 an avg value
		setValue( 0, p.constrain( p.sqrt(avgPresence * 4.0f) - 1, -1,1) );
		// min 0, max set to 15, avg value 2
		setValue( 1, p.constrain( p.pow( (avgNumOfItems/2.0f), 0.33f) - 1, -1,1) );
		// 0 - 1, 0.2 avg value
		setValue( 2, p.constrain( p.pow( (avgIntensity * 6.0f), 0.4f) - 1, -1,1));
		setValue( 3, p.constrain( avgVariety-1, -1,1) );
	}
	
	/**
	 * @override
	 */
	public void draw() {
		 
		 // draw image on screen
		p.image(flob.getSrcImage(), 0, 0, outWidth, outHeight);
		
		// add blobs on top
		for(int i = 0; i < blobs.size(); i++) {
			
			s373.flob.trackedBlob tb = flob.getTrackedBlob(i);
			
			float velmult = 100.0f;
			// bounding box
			p.fill(220,100,100,30);
			p.rect(tb.cx, tb.cy, tb.dimx, tb.dimy);
			// center dot
			p.fill(0,100,100,100);
			p.rect(tb.cx, tb.cy, 5, 5); 
			// tracking vector
			p.fill(100,70,100,100);
			p.line(tb.cx, tb.cy, tb.cx + tb.velx * velmult ,tb.cy + tb.vely * velmult );  
		}
		 
		// draw textual feedback
		p.noStroke();
		p.fill(0,50,100,100);
		p.textAlign(p.LEFT, p.CENTER);
		p.textFont(p.font);
		p.text("Presence: "+getValue(0), 12, 100+p.compensation);
		p.text("# Blobs: "+getValue(1), 23, 120+p.compensation);
		p.text("Intensity: "+getValue(2), 10, 140+p.compensation);
		p.text("Variety: "+getValue(3), 23, 160+p.compensation);
	}
	
	/**
	 * @override
	 */
	public void cleanup() {
		flob = null;
		video = null;
		blobs = null;
	}
	
	public void setBackgroundImage() {
		flob.setBackground( video );
	}
	
	/**
	 * Toggles between background subtraction and frame differencing methods
	 * Thus either static or continuous change of reference frame
	 */
	public void toggleVideoDetectionMethod() {
		int om = flob.getOm();
		om = Math.abs(om-1); // 0 becomes 1, 1 becomes 0
		flob.setOm(om);
	}
}