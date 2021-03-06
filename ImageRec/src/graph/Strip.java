// Strip.java, created by Amerson Lin
// Copyright (C) 2003 Amerson H. Lin <amerson@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

/**
 *  Strips an {@link ImageData} to remove the massive gvals, rvals, bvals
 *  for efficient sending from groundATR to embeddedATR
 *  @see RobertsCross
 *
 *  @author Amerson H. Lin <<a href="mailto:amerson@mit.edu"> amerson@mit.edu</a>>
 */

public class Strip extends Node {
    
    private boolean copyImage = false;

    public void copyImage(boolean b) {
	copyImage = b;
    }

  /* 
   * Process just sets the gvals/rvals/bvals to null.
   * @param id the {@link ImageData} that needs to be stripped
   */
  public void process(ImageData id) {
      //System.out.println("Strip: x:"+id.x);
      //System.out.println("Strip: y:"+id.x);
      //System.out.println("Strip: width:"+id.width);
      //System.out.println("Strip: height:"+id.height);
      ImageData newid;
      if (copyImage) {
	  newid =
	      ImageDataManip.create(id, new byte[0], new byte[0],
				     new byte[0], id.width, id.height);
      }
      else {
	  id.gvals = new byte[0];
	  id.rvals = new byte[0];
	  id.bvals = new byte[0];
	  newid = id;
      }
      super.process(newid);
  }
}
