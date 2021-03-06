// AddMemoryPreallocation.java, created Wed Nov 27 18:44:55 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MemOpt;

import java.util.Map;
import java.util.Iterator;
import java.util.Collection;

import java.io.PrintWriter;

import harpoon.Analysis.Transformation.MethodMutator;

import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HClass;

import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime;

import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Code;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Bop;

import harpoon.Temp.Temp;
import harpoon.Temp.Label;

/**
 * <code>AddMemoryPreallocation</code> is a code factory that provides
 * the code for the static method that allocates the pre-allocated
 * chunks of memory that are used by the unitary sites.
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: AddMemoryPreallocation.java,v 1.10 2003-03-03 23:28:58 salcianu Exp $ */
class AddMemoryPreallocation implements HCodeFactory {

    /** Creates a <code>AddMemoryPreallocation</code> code factory: it
        behaves like <code>parent_hcf</code> for all methods, except
        for the special memory preallocation methdo
        <code>init_method</code>.  For this method, it generates code
        to (pre)allocate some memory chunks and store references to
        them at the addresses indicated by the (label) keys of
        <code>label2size</code>.

	@param parent_hcf parent code factory; this factory provides
	the code for all methods, except <code>init_method</code>

	@param init_method handle of the method that does all pre-allocation

	@param label2size maps a label to the size of the preallocated
	chunk of memory that the corresponding pointer shold point to
	at runtime

	@param frame frame containing all the backend details */
    public AddMemoryPreallocation
	(HCodeFactory parent_hcf, HMethod init_method, 
	 Map/*<Label,Integer>*/ label2size, Frame frame,
	 Label beginLabel, Label endLabel) {
	assert parent_hcf.getCodeName().equals(CanonicalTreeCode.codename) : 
	    "AddMemoryPreallocation only works on CanonicalTree form";
	this.parent_hcf  = parent_hcf;
	this.label2size  = label2size;
	this.runtime     = frame.getRuntime();
	this.init_method = init_method;
	this.beginLabel  = beginLabel;
	this.endLabel    = endLabel;
    }

    public String getCodeName() { return parent_hcf.getCodeName(); }
    public void clear(HMethod m) { parent_hcf.clear(m); }


    private final HCodeFactory parent_hcf;
    private final Map/*<Label,Integer>*/ label2size;
    private final Runtime runtime;
    private final HMethod init_method;
    private final Label beginLabel;
    private final Label endLabel;

    private static boolean TRUE = true;

    public HCode convert(HMethod m) {
	// we don't change any method, except ...
	if(!m.equals(init_method))
	    return parent_hcf.convert(m);

	// ... the method that preallocates memory
	Code code = (Code) parent_hcf.convert(m);
	DerivationGenerator dg = 
	    (DerivationGenerator) code.getTreeDerivation();

	System.out.println("\n\nBefore  modifications:");
	code.print(new PrintWriter(System.out));

	SEQ start = (SEQ) ((SEQ) code.getRootElement()).getRight();

	if(PreallocOpt.HACKED_GC)
	    insertCode(start, getExcludeRootsCall(start));

	Temp tmem = new Temp(start.getFactory().tempFactory(), "tmem");

	for(Iterator it = label2size.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry entry = (Map.Entry) it.next();
	    Label label = (Label) entry.getKey();
	    int size    = ((Integer) entry.getValue()).intValue();
	    // Generate code that (pre)-allocates space and puts
	    // "hfield" point to it. As insertCode adds stuff right
	    // after start, we insert code in reverse order:
	    // 2. hfield = tmem
	    insertCode(start, getAssignment(label, tmem, start, dg));
	    // 1. tmem = "malloc-like-fun"(size)
	    insertCode(start, getAllocCall(tmem, size, start, dg));
	}

	System.out.println("\n\nAfter  modifications:");
	code.print(new PrintWriter(System.out));

	return code;
    }


    private Stm getExcludeRootsCall(Tree start) {
	TreeFactory tf = start.getFactory();

	return
	    new NATIVECALL
	    (tf, start, 
	     null, /* no retval */
	     new NAME(tf, start, new Label("GC_exclude_static_roots")),
	     new ExpList(new NAME(tf, start, beginLabel),
			 new ExpList(new NAME(tf, start, endLabel),
				     null)));
    }


    // produces a native call that allocates "length" bytes of memory;
    // the returned value (the pointed to the newly allocated piece of
    // memory) is stored in "tmem"
    private Stm getAllocCall(Temp tmem, int length,
			     Tree start, DerivationGenerator dg) {
	TreeFactory tf = start.getFactory();
	
	String malloc_method =
	    PreallocOpt.HACKED_GC ? "GC_malloc_prealloc" : "GC_malloc";

	return
	    new NATIVECALL
	    (tf, start, 
	     (TEMP)
	     DECLARE(dg, HClass.Void,
		     new TEMP(tf, start, Type.POINTER, tmem)),
	     new NAME(tf, start, new Label(malloc_method)),
	     new ExpList(new CONST(tf, start, length),
			 null));
    }

    // generate code for "*(label) = tmem"
    private Stm getAssignment(Label label, Temp tmem,
			      Tree start, DerivationGenerator dg) {
	// TODO: we should be able to get the DerivationGenerator
	// (a method-wide thing) from "start".
	TreeFactory tf = start.getFactory();

	TEMP temp = new TEMP(tf, start, Type.POINTER, tmem);
	dg.putType(temp, HClass.Void);
	
	return
	    new MOVE
	    (tf, start,
	     DECLARE(dg, HClass.Void,
		     new MEM
		     (tf, start,
		      Type.POINTER, new NAME(tf, start, label))),
	     temp);
    }


    // insert a statement right after "start"
    private SEQ insertCode(SEQ start, Stm code) {
	Stm former_right = start.getRight();
	former_right.unlink();
	start.setRight(new SEQ(code, former_right));
	return start;
    }


    // type declaration helper methods
    protected static Exp DECLARE(DerivationGenerator dg, HClass hc, Exp exp) {
	dg.putType(exp, hc);
	return exp;
    }
}
