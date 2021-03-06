// Normalize.java, created by wbeebee
//                 modified by benster
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * Normalize the magnatudes of the colors in an image.
 *
 * Use either histograms/percentiles or max magnatude.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Normalize extends Node {
    private boolean maxMag;

    /** Construct a {@link Normalize} node to normalize the magnatudes of the 
     *  intensities of the colors of the image.
     *
     *  @param out The node to send normalized images to.
     */
    public Normalize(Node out) {
	this(out, true);
    }

    /** Construct a {@link Normalize} node to normalize the magnatudes of the 
     *  intensities of the colors of the image.
     *
     *  @param out The node to send normalized images to.
     *  @param maxMag Whether to use maximum magnatude or histogram methods.
     */
    public Normalize(Node out, boolean maxMag) {
	super(out);
	this.maxMag = maxMag;
    }

    /** Normalize the image and send it to the next node. 
     *
     *  @param id The {@link ImageData} to normalize.
     */
    public void process(ImageData id) {

	byte[][] valArray = new byte[][] {id.rvals, id.gvals, id.bvals};
	for (int j=0; j<3; j++) {
	    byte[] vals = valArray[j];
	    int size = vals.length;
	    int maxIntensity = 0;
	    int minIntensity = 255;
	    int count;
	    for (count = 0; count < size; count++) {
		if (((vals[count]|256)&255) > maxIntensity)
		    maxIntensity = (vals[count]|256)&255;
		if (((vals[count]|255)&256) < minIntensity)
		    minIntensity = (vals[count]|256)&255;
	    }

	    int mid = (maxIntensity + minIntensity)/2;
	    int shift = 128 - mid;
	    double stretch = 256. / (maxIntensity - minIntensity);

	    System.out.println("Channel #"+j);
	    System.out.println("   max: "+maxIntensity);
	    System.out.println("   min: "+minIntensity);
	    System.out.println("   mid: "+mid);
	    System.out.println("   shift: "+shift);
	    System.out.println("   stretch: "+stretch);

	    for (count = 0; count < size; count++) {
		vals[count] = (byte)((((vals[count]|256)&255)+shift)*stretch);
	    }
	}

	/*
  	byte[][] in = new byte[][] {id.rvals, id.gvals, id.bvals};
  	for (int j=0; j<3; j++) {
  	    byte[] transferFunc = new byte[256];
  	    if (maxMag) {
  		int max = 0;
  		for (int i=0; i<numPix; i++) {
  		    max = (int)Math.max(max, (in[j][i]|256)&255);
  		}
  		for (int 
  		transferFunc[i]
  	    } else {
  		int[] scale = new int[256];
  		int numPix = id.width*id.height;
  		for (int i=0; i<numPix; i++) {
  		    scale[(in[j][i]|256)&255]++;
  		}
  		for (int i=1; i<256; i++) {
  		    scale[i]+=scale[i-1];
  		}

  	    }
  	    for (int i=0; i<numPix; i++) {
  		in[j][i] = transferFunc[((in[j][i])|256)&255];
  	    }
  	}
	*/
  	super.process(id);
    }
}
