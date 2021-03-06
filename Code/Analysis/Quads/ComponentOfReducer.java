// ComponentOfReducer.java, created Tue Dec  4 12:33:07 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Maps.ExactTypeMap;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.Quads.SCC.SCCAnalysis;
import harpoon.Analysis.Quads.SCC.SCCOptimize;
import harpoon.Analysis.Transformation.MethodMutator;
import harpoon.Backend.Maps.CHFinalMap;
import harpoon.Backend.Maps.FinalMap;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.COMPONENTOF;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadSSI;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.SnapshotIterator;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/**
 * The <code>ComponentOfReducer</code> pass attempts to transform
 * <code>COMPONENTOF</code> operations into (more efficient)
 * <code>INSTANCEOF</code> operations, when this is safe to do.
 * In particular, it is safe to transform <code>o componentof a</code>
 * where a has type <code>T[]</code> into <code>o instanceof T</code>
 * if a's type is exact or T is a final type.
 * <p>This analysis needs type information for its input factory
 * (a <code>TypeMap</code> or <code>ExactTypeMap</code>) and
 * final type information (a <code>Backend.Maps.FinalMap</code>).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ComponentOfReducer.java,v 1.3 2002-09-03 15:08:03 cananian Exp $
 */
public class ComponentOfReducer extends MethodMutator<Quad> {
    final ExactTypeMap<Quad> etm;
    
    /** Creates a <code>ComponentOfReducer</code>. */
    public ComponentOfReducer(HCodeFactory hcf,
			      final ExactTypeMap<Quad> tm, final FinalMap fm) {
	super(hcf);
	this.etm = new ExactTypeMap<Quad>() {
		public boolean isExactType(Quad hce, Temp t)
		    throws TypeMap.TypeNotKnownException {
		    if (tm.isExactType(hce, t)) return true;
		    HClass type = HClassUtil.baseClass(tm.typeMap(hce, t));
		    if (fm.isFinal(type)) return true;
		    return false;
		}
		public HClass typeMap(Quad hce, Temp t)
		    throws TypeNotKnownException {
		    return tm.typeMap(hce, t);
		}
	    };
    }
    public ComponentOfReducer(HCodeFactory hcf,
			      final TypeMap<Quad> tm, final FinalMap fm) {
	this(hcf,
	     tm instanceof ExactTypeMap<Quad>? (ExactTypeMap<Quad>)tm: new ExactTypeMap<Quad>() {
		public boolean isExactType(Quad hce, Temp t) {
		    return false;
		}
		public HClass typeMap(Quad hce, Temp t)
		    throws TypeNotKnownException {
		    return tm.typeMap(hce, t);
		}
	    }, fm);
    }
    // convenience constructor.
    public ComponentOfReducer(HCodeFactory hcf, ClassHierarchy ch) {
	this(new CachingCodeFactory(QuadSSI.codeFactory(hcf)), ch);
    }
    private ComponentOfReducer(CachingCodeFactory ccf, ClassHierarchy ch) {
	// (ccf will also be quadssi!)
	this(ccf, new CachingExactTypeMap(ccf), new CHFinalMap(ch));
    }
    private static class CachingExactTypeMap implements ExactTypeMap<Quad> {
	final CachingCodeFactory ccf;
	final Map<HMethod,ExactTypeMap<Quad>> etmMap =
	    new HashMap<HMethod,ExactTypeMap<Quad>>();
	CachingExactTypeMap(CachingCodeFactory ccf) {
	    this.ccf = ccf;
	}
	ExactTypeMap<Quad> fetchCached(Quad hce) {
	    // cached?
	    HMethod hm = hce.getFactory().getMethod();
	    ExactTypeMap<Quad> etm = etmMap.get(hm);
	    if (etm==null) {
		// no. =(
		etm = (ExactTypeMap/*XXX: cast won't be necessary after SCCAnalysis is GJ-ized*/) new SCCAnalysis(ccf.convert(hm));//could use factory here
		etmMap.put(hm, etm);
	    }
	    return etm;
	}
	public boolean isExactType(Quad hce, Temp t)
	    throws TypeNotKnownException {
	    return fetchCached(hce).isExactType(hce, t);
	}
	public HClass typeMap(Quad hce, Temp t)
	    throws TypeNotKnownException {
	    return fetchCached(hce).typeMap(hce, t);
	}
    }

    // okay, enough playing around.  let's mutate!
    protected HCode<Quad> mutateHCode(HCodeAndMaps<Quad> input) {
	boolean changed = false;
	// find COMPONENT OF.
	for (Iterator<Quad> it=new SnapshotIterator<Quad>
		 (input.hcode().getElementsI()); it.hasNext(); ) {
	    Quad thisq = it.next();
	    if (thisq instanceof COMPONENTOF) {
		COMPONENTOF q = (COMPONENTOF) thisq;
		COMPONENTOF qq = (COMPONENTOF)
		    input.ancestorElementMap().get(q);
		// can we change this?
		if (etm.isExactType(qq, qq.arrayref())) {
		    // change it!
		    HClass T = etm.typeMap(qq, qq.arrayref())
			.getComponentType();
		    QuadFactory qf = q.getFactory();
		    Quad.replace
			(q, new INSTANCEOF(q.getFactory(), q,
					   q.dst(), q.objectref(), T));
		    changed = true;
		}
	    }
	}
	// if anything's been changed...
	if (changed) {
	    // SCCOptimize this!
	    SCCAnalysis scc = new SCCAnalysis(input.hcode());
	    SCCOptimize sco = new SCCOptimize(scc);
	    sco.optimize(input.hcode());
	}
	return input.hcode();
    }
}
