// SizeOutOfBoundsException.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>SizeOutOfBoundsException</code> means that the value given in 
 *  the size parameter is either negative, larger than an allowable range,
 *  or would cause an accessor method to access an address outside of the
 *  memory area. 
 */
public class SizeOutOfBoundsException extends Exception {
    
    /** A constructor for <code>SizeOutOfBoundsException</code>. */
    public SizeOutOfBoundsException() {
	super();
    } 

    /** A descriptive constructor for <code>SizeOutOfBoundsException</code>. */
    public SizeOutOfBoundsException(String s) {
	super(s);
    }
}
