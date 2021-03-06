// Pruning.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * {@link Pruning} will prune lines that don't form closed objects.
 * This prepares images for object labelling.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Pruning extends Node {
    private static final int iterations=10;

    /** Create a new {@link Pruning} that will prune lines that don't form closed objects. 
     *  Send the resulting images to <code>out</code>
     */
    public Pruning(Node out) {
	super(out);
    }

    /** <code>process</code> an {@link ImageData} by pruning away any lines that don't form 
     *  closed objects.
     */
    public void process(ImageData id) {
	byte[] in = id.gvals;
	int w = id.width;
	int h = id.height;
	byte[] out = null;
	int changed=1;
	for (int j=0;(j<iterations)&&(changed==1);j++) {
	    changed=0;
	    System.arraycopy(in,0,out=new byte[w*h],0,w*h);
	    for (int i=w+1;i<w*(h-2)-1;i++) {
		if (in[i]==127) {
		    /* 0 1 2
                       3 * 4
                       5 6 7 */
		    int[] n = new int[] {in[i-w-1],in[i-w],in[i-w+1],
					 in[i-1],          in[i+1],
					 in[i+w-1],in[i+w],in[i+w+1]};
		    if (((n[0]|n[1]|n[2]|n[3]|n[4])<65 && (n[5]&n[7])<65)||
			((n[1]|n[2]|n[4]|n[6]|n[7])<65 && (n[0]&n[5])<65)||
			((n[3]|n[4]|n[5]|n[6]|n[7])<65 && (n[0]&n[2])<65)||
			((n[0]|n[1]|n[3]|n[5]|n[6])<65 && (n[2]&n[7])<65)) {
			changed = 1;
			out[i]=0;
		    }
		}
	    }
	    in=out;
	}
	id.gvals=in;
	super.process(id);
    }
}
