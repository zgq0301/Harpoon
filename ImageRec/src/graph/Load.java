// Load.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import java.util.jar.JarFile;

import imagerec.util.ImageDataManip;

/**
 * A {@link Load} node will load a series of <code>.ppm.gz</code> files from the disk
 * and send them to the out node.  The filenames are <code>filePrefix.#</code>
 * where <code>#</code> ranges from <code>0</code> to <code>num-1</code> inclusive.
 *
 * @see Save
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Load extends Node {
    private JarFile jarFile;
    private String filePrefix;
    private String fileSuffix;
    private int num;

    /** Construct a {@link Load} node that will load the files
     *  <code>filePrefix.0</code> through
     *  <code>filePrefix.num-1</code> and send them to
     *  <code>out</code> node.  
     *
     *  @param filePrefix The prefix of the fileNames to load.
     *  @param num The number of images to load.
     *  @param out The node to send them to.
     */
    public Load(String filePrefix, int num, Node out) {
	super(out);
	init(null, filePrefix, null, num);
    }

    /** Construct a {@link Load} node that will load the Jar entries
     *  <code>filePrefix.0</code> through
     *  <code>filePrefix.num-1</code> and send them to
     *  <code>out</code> node.  
     *
     *  @param jarFile The JAR file to load entries from.
     *  @param filePrefix The prefix of the file entries to load.
     *  @param num The number of images to load.
     *  @param out The node to send them to.
     */
    public Load(String jarFile, String filePrefix, int num, Node out) {
	super(out);
	init(jarFile, filePrefix, null, num);
    }

    public Load(String jarFile, String filePrefix, String fileSuffix, int num, Node out) {
	super(out);
	init(jarFile, filePrefix, fileSuffix, num);
    }
    
    /** Construct a {@link Load} node that will load the files
     *  <code>filePrefix.0</code> through
     *  <code>filePrefix.num-1</code> and send them to
     *  <code>out</code> node.  
     *
     *  @param filePrefix The prefix of the fileNames to load.
     *  @param num The number of images to load.
     *  @param out1 The first node to send them to.
     *  @param out2 The second node to send them to.
     */
    public Load(String filePrefix, int num, Node out1, Node out2) {
	super(out1, out2);
	init(null, filePrefix, null, num);
    }

    public Load(String jarFile, String filePrefix, int num, Node out1, Node out2) {
	super(out1, out2);
	init(jarFile, filePrefix, null, num);
    }

    /**
       Initialize a {@link Load} node that will load the files
       (or jar entries if jarFile != null)
       <code>filePrefix.0</code> through
       <code>filePrefix.num-1</code>.
    */
    private void init(String jarFile, String filePrefix, String fileSuffix, int num) {
	this.filePrefix = filePrefix;
	this.fileSuffix = fileSuffix;
	this.num = num;
	if (jarFile == null) {
	    this.jarFile = null;
	} else {
	    try {
		this.jarFile = new JarFile(jarFile);
	    } catch (Exception e) {
		throw new Error(e);
	    }
	}	
    }

    /** Load all the image files and send them, one at a time, to the <code>out</code> node. */
    public void process(ImageData id) {
	//added by benji, hack for improved memory usage and speed
	//Unclear whether this helps.  It depends on whether it is running
	//headless or not.
	//ImageDataManip.useSameArrays(true);
	for (int i=0; i<num; i++) {
	    String fileName = filePrefix+"."+i;
	    if (fileSuffix != null)
		fileName += "."+fileSuffix;
	    System.out.println("Loading image "+fileName);
	    id = null;
	    try {
		if (jarFile == null) {
		    (id = ImageDataManip.readPPM(fileName)).id = i;
		} else {
		    (id = ImageDataManip.readPPM(jarFile, fileName)).id = i;
		}
	    } catch (Error e) {
		System.out.println("Error:" + e);
		System.exit(-1);
	    } 
	    if (id != null) {
		if (i == num-1)
		    id.lastImage = true;
		id.scaleFactor = (float)id.width/320;
		super.process(id);
	    }
	    /* Continue even if a file isn't found or there are exceptions... */
	}
	System.out.println("All images loaded and processed.");
    }
}
