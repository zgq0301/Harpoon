// TEMP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Util.HashSet;
import harpoon.Util.Set;
import harpoon.Util.Util;

/**
 * <code>TEMP</code> objects are expressions which stand for a
 * value in a virtual register.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: TEMP.java,v 1.1.2.14 1999-06-29 05:43:53 cananian Exp $
 */
public class TEMP extends Exp {
    /** The <code>Temp</code> which this <code>TEMP</code> refers to. */
    public final Temp temp;
    /** The type of this <code>Temp</code> expression. */
    public final int type;
    /** Constructor. */
    public TEMP(TreeFactory tf, HCodeElement source, int type, Temp temp) {
	super(tf, source);
	this.type=type; this.temp=temp;
	Util.assert(Type.isValid(type) &&
		    temp!=null &&
		    (temp.tempFactory() == tf.tempFactory() ||
                    temp.tempFactory() == tf.getFrame().regTempFactory()));
    }
    
    public Set useSet() {
	HashSet set = new HashSet();
	set.union(temp);
	return set;
    }
  
    public ExpList kids() {return null;}
    public int kind() { return TreeKind.TEMP; }
    public Exp build(ExpList kids) {return this;}

    // Typed interface:
    public int type() { return type; }

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new TEMP(tf, this, this.type, map(ctm, this.temp));
    }

    public String toString() {
        return "TEMP<"+Type.toString(type)+">("+temp+")";
    }
}
