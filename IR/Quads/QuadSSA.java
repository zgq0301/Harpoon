// QuadSSA.java, created Fri Aug  7 13:45:29 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.QuadSSA.DeadCode;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;

/**
 * <code>Quads.QuadSSA</code> is a code view that exposes the details of
 * the java classfile bytecodes in a quadruple format.  Implementation
 * details of the stack-based JVM are hidden in favor of a flat consistent
 * temporary-variable based approach.  The generated quadruples adhere
 * to an SSA form; that is, every variable has exactly one definition,
 * and <code>PHI</code> functions are used where control flow merges.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadSSA.java,v 1.1.2.7 1999-08-04 02:13:33 cananian Exp $
 */
public class QuadSSA extends Code /* which extends HCode */ {
    /** The name of this code view. */
    public static final String codename = "quad-ssa";

    /** Creates a <code>Code</code> object from a bytecode object. */
    QuadSSA(QuadNoSSA qns) 
    {
	super(qns.getMethod(), null);
	quads = Quad.clone(qf, qns.quads);
	FixupFunc.fixup(this); // add phi/sigma functions.
	DeadCode.optimize(this); // get rid of unused phi/sigmas.
    }
    /** 
     * Create a new code object given a quadruple representation
     * of the method instructions.
     */
    private QuadSSA(HMethod parent, Quad quads) {
	super(parent, quads);
    }

    /** Clone this code representation. The clone has its own
     *  copy of the quad graph. */
    public HCode clone(HMethod newMethod) {
	QuadSSA qs = new QuadSSA(newMethod, null);
	qs.quads = Quad.clone(qs.qf, quads);
	return qs;
    }

    /**
     * Return the name of this code view.
     * @return the string <code>"quad-ssa"</code>.
     */
    public String getName() { return codename; }
    
    /** Return a code factory for <code>QuadSSA</code>, given a code
     *  factory for <code>QuadNoSSA</code>.  Given a code factory for
     *  <code>Bytecode</code> or <code>QuadWithTry</code>, chain
     *  through <code>QuadNoSSA.codeFactory()</code>.
     */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	if (hcf.getCodeName().equals(QuadNoSSA.codename)) {
	    return new HCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null)?null:new QuadSSA((QuadNoSSA)c);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else if (hcf.getCodeName().equals(harpoon.IR.Bytecode.Code.codename)
		   || hcf.getCodeName().equals(QuadWithTry.codename)) {
	    // do some implicit chaining.
	    return codeFactory(QuadNoSSA.codeFactory(hcf));
	} else throw new Error("don't know how to make " + codename + 
			       " from " + hcf.getCodeName());
    }
    /** Return a code factory for QuadSSA, using the default code factory
     *  for QuadNoSSA. */
    public static HCodeFactory codeFactory() {
	return codeFactory(QuadNoSSA.codeFactory());
    }
    // obsolete.
    public static void register() {
	HMethod.register(codeFactory());
    }
}
