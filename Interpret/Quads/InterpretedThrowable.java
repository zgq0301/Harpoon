// InterpretedThrowable.java, created Mon Dec 28 01:27:53 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.IR.Quads.Quad;
import harpoon.Util.Util;
/**
 * <code>InterpretedThrowable</code> is a wrapper for an exception within
 * the interpreter.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InterpretedThrowable.java,v 1.1.2.2 1999-07-17 11:47:12 cananian Exp $
 */
final class InterpretedThrowable extends RuntimeException {
    final ObjectRef ex;
    final String[] stackTrace;
    /** Creates a <code>InterpretedThrowable</code>. */
    InterpretedThrowable(ObjectRef ex, String[] st) {
        this.ex = ex; this.stackTrace = st;
	Util.assert(ex.type.isInstanceOf(HClass.forDescriptor
					 ("Ljava/lang/Throwable;")));
    }
    InterpretedThrowable(ObjectRef ex, StaticState ss) {
	this.ex = ex; this.stackTrace = ss.stackTrace();
	Util.assert(ex.type.isInstanceOf(HClass.forDescriptor
					 ("Ljava/lang/Throwable;")));
    }
}
