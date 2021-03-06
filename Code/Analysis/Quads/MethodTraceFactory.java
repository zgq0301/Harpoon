// MethodTraceFactory.java, created Fri Nov  2 14:30:00 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.Transformation.MethodMutator;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Quads.DEBUG;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.Util.Collections.SnapshotIterator;

import java.util.Iterator;

/**
 * <code>MethodTraceFactory</code> adds <code>DEBUG</code> quads
 * to the entry and exit points of every method.  Comparing
 * the debug output of a working and 'broken' program should
 * make it easier to isolate which part of the 'broken' binary
 * isn't behaving as expected.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MethodTraceFactory.java,v 1.3 2002-09-03 15:08:04 cananian Exp $
 */
public class MethodTraceFactory extends MethodMutator<Quad> {
    
    /** Creates a <code>MethodTraceFactory</code>. */
    public MethodTraceFactory(HCodeFactory parent) {
	super(parent);
    }
    public HCode<Quad> mutateHCode(HCodeAndMaps<Quad> input) {
	final HCode<Quad> hc = input.hcode();
	QuadVisitor qv = new QuadVisitor() {
		public void visit(Quad q) { /* do nothing */ }
		public void visit(METHOD q) {
		    addAt(q.nextEdge(0),
			  new DEBUG(q.getFactory(), q,
				    ("ENTERING "+hc.getMethod()).intern()));
		}
		public void visit(RETURN q) {
		    addAt(q.prevEdge(0),
			  new DEBUG(q.getFactory(), q,
				    ("RETURN from "+hc.getMethod()).intern()));
		}
		public void visit(THROW q) {
		    addAt(q.prevEdge(0),
			  new DEBUG(q.getFactory(), q,
				    ("THROW from "+hc.getMethod()).intern()));
		}
	    };
	for (Iterator<Quad> it=new SnapshotIterator<Quad>(hc.getElementsI());
	     it.hasNext(); )
	    it.next().accept(qv);
	return hc;
    }
    /** helper routine to add a quad on an edge. */
    private static Edge addAt(Edge e, Quad q) { return addAt(e, 0, q, 0); }
    private static Edge addAt(Edge e, int which_pred, Quad q, int which_succ) {
	Quad frm = e.from(); int frm_succ = e.which_succ();
	Quad to  = e.to();   int to_pred = e.which_pred();
	Quad.addEdge(frm, frm_succ, q, which_pred);
	Quad.addEdge(q, which_succ, to, to_pred);
	return to.prevEdge(to_pred);
    }
}
