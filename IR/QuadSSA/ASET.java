// ASET.java, created Wed Aug 26 19:12:32 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;

/**
 * <code>ASET</code> represents an array element assignment.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ASET.java,v 1.2 1998-09-04 06:31:21 cananian Exp $
 * @see ANEW
 * @see AGET
 * @see ALENGTH
 */

public class ASET extends Quad {
    /** The array reference */
    public Temp objectref;
    /** The Temp holding the index of the element to get. */
    public Temp index;
    /** The new value for the array element. */
    public Temp src;

    /** Creates an <code>ASET</code> object. */
    public ASET(String sourcefile, int linenumber,
		Temp objectref, Temp index, Temp src) {
	super(sourcefile, linenumber);
	this.objectref = objectref;
	this.index = index;
	this.src = src;
    }
    ASET(HCodeElement hce, Temp objectref, Temp index, Temp src) {
	this(hce.getSourceFile(), hce.getLineNumber(), objectref, index, src);
    }
    /** Returns all the Temps used by this quad. 
     * @return the <code>objectref</code>, <code>index</code>, and 
     *         <code>src</code> fields.
     */
    public Temp[] use() { return new Temp[] { objectref, index, src }; }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	return "ASET " + objectref + "["+index+"] = " + src;
    }
}
