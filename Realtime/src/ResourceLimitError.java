// ResourceLimitError.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** Thrown if an attempt is made to exceed a system resource limit,
 *  such as the maximum number of locks.
 */
public class ResourceLimitError extends Error {

    /** A constructor for <code>ResourceLimitError</code>. */
    public ResourceLimitError() {
	super();
    }

    /** A descriptive constructor for <code>ResourceLimitError</code>.
     *
     *  @param s The description of the exeption.
     */
    public ResourceLimitError(String s) {
	super(s);
    }
}
