// DomFrontier.java, created Mon Sep 14 22:21:38 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGrapher;
import harpoon.Util.ArrayFactory;
import harpoon.Util.ArrayIterator;
import harpoon.Util.ArraySet;
import harpoon.Util.Collections.AggregateSetFactory;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Default;
import harpoon.Util.Util;
import harpoon.Util.Collections.WorkSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
/**
 * <code>DomFrontier</code> computes the dominance frontier of a 
 * flowgraph-structured IR.  The <code>HCodeElement</code>s must implement
 * the <code>harpoon.IR.Properties.CFGraphable</code> interface.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DomFrontier.java,v 1.7 2002-02-25 20:56:10 cananian Exp $
 */

public class DomFrontier  {
    final ArrayFactory af;
    /** Creates a <code>DomFrontier</code> using a pre-existing
     *  <code>DomTree</code>. */
    public DomFrontier(DomTree dt) {
	this.af = dt.hcode.elementArrayFactory();
	analyze(dt);
    }
    /** Creates a <code>DomFrontier</code> for the given
     *  <code>HCode</code> using the default grapher; if
     *  <code>isPost</code> is <code>false</code> creates the
     *  dominance frontier; otherwise creates the postdominance
     *  frontier. */
    public DomFrontier(HCode hcode, boolean isPost) {
	this(hcode, CFGrapher.DEFAULT, isPost);
    }
    /** Creates a <code>DomFrontier</code> for the given
     *  <code>HCode</code> using the given <code>CFGrapher</code>; if
     *  <code>isPost</code> is <code>false</code> creates the
     *  dominance frontier; otherwise creates the postdominance
     *  frontier. */
    public DomFrontier(HCode hcode, CFGrapher grapher, boolean isPost) {
	this(new DomTree(hcode, grapher, isPost));
    }

    MultiMap DF = new GenericMultiMap(new AggregateSetFactory());

    /** Return an array of <code>HCodeElement</code>s in the (post)dominance
     *  frontier of <code>n</code>.
     */
    public HCodeElement[] df(HCodeElement n) {
	Collection c = DF.getValues(n);
	return (HCodeElement[]) c.toArray(af.newArray(c.size()));
    }
    /** Return an immutable <code>Set</code> of <code>HCodeElement</code>s
     *  in the (post)dominance frontier of <code>n</code>.
     */
    public Set dfS(HCodeElement n) {
	return Collections.unmodifiableSet((Set)DF.getValues(n));
    }

    void analyze(DomTree dt) {
	HCodeElement[] roots = dt.roots();
	for (int i=0; i < roots.length; i++)
	    computeDF(dt, roots[i]);
    }
    void computeDF(DomTree dt, HCodeElement n) {
	CFGrapher grapher = dt.grapher;
	
	// for every child y in succ[n]
	for (Iterator it=grapher.succC(n).iterator(); it.hasNext(); ) {
	    HCodeElement y = ((HCodeEdge)it.next()).to();
	    if (!n.equals( dt.idom(y) ))
		DF.add(n, y);
	}
	// for each child c of n in the (post)dominator tree
	for (Iterator it=new ArrayIterator(dt.children(n)); it.hasNext(); ) {
	    HCodeElement c = (HCodeElement) it.next();
	    computeDF(dt, c);
	    // for each element w of DF[c]
	    for (Iterator it2=DF.getValues(c).iterator(); it2.hasNext(); ) {
		HCodeElement w = (HCodeElement) it2.next();
		if (!n.equals( dt.idom(w) ))
		    DF.add(n, w);
	    }
	}
    }
}
