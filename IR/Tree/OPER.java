// OPER.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Util.Util;

/**
 * <code>OPER</code> objects are expressions which stand for the result
 * of applying some operator to subexpressions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: OPER.java,v 1.1.2.4 1999-02-05 11:48:51 cananian Exp $
 */
public abstract class OPER extends Exp implements Typed {
    /** An enumerated type encoding the operator.
     * @see Bop
     * @see Uop
     */
    public final int op;
    /** Type of the operands (not necessarily the result type). */
    public final int type;

    public OPER(TreeFactory tf, HCodeElement source,
		int type, int op) {
	super(tf, source);
	Util.assert(Type.isValid(type));
	// subclass must verify validity of op.
	this.op = op; this.type = type;
    }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    // <code>Typed</code> interface.
    /** Return type of operands (not necessarily the result type). */
    public int type() { return type; }
    /** Return result type. */
    public abstract int resultType();
}
