// RegAlloc.java, created Mon Mar 29 16:47:25 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Temp.Temp;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrVisitor;
import harpoon.IR.Properties.UseDef;
import harpoon.IR.Properties.HasEdges;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Code;
import harpoon.Analysis.UseMap;
import harpoon.Analysis.DataFlow.BasicBlock;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;
import harpoon.Util.LinearMap;

import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import java.util.List;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;


/**
 * <code>RegAlloc</code> performs Register Allocation for a set of
 * <code>Instr</code>s in a <code>Backend.Generic.Code</code>.  After
 * register allocation is completed for a set of <code>Instr</code>s,
 * the only references to non-register <code>Temp</code>s in the
 * <code>Instr</code>s will be <code>InstrMEM</code> instructions to
 * move values from the register file to data memory and vice-versa.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: RegAlloc.java,v 1.1.2.16 1999-08-04 18:41:41 pnkfelix Exp $ */
public abstract class RegAlloc  {
    
    protected Frame frame;
    protected Code code;
    protected BasicBlock rootBlock;

    protected class FskLoad extends InstrMEM {
	FskLoad(InstrFactory inf, HCodeElement hce, 
		String assem, Temp dst, Temp src) {
	    super(inf, hce, assem, 
		  new Temp[]{dst}, new Temp[]{src});
	}
	FskLoad(InstrFactory inf, HCodeElement hce, 
		String assem, List dsts, Temp src) {
	    super(inf, hce, assem, 
		  (Temp[])dsts.toArray(new Temp[0]), new Temp[]{src});
	}
	
	public void visit(RegInstrVisitor v) { v.visit(this); }
    }

    protected class FskStore extends InstrMEM {
	FskStore(InstrFactory inf, HCodeElement hce, 
		String assem, Temp dst, Temp src) {
	    super(inf, hce, assem, 
		  new Temp[]{dst}, new Temp[]{src});
	}
	FskStore(InstrFactory inf, HCodeElement hce, 
		String assem, Temp dst, List srcs) {
	    super(inf, hce, assem, 
		  new Temp[]{dst}, (Temp[])srcs.toArray(new Temp[0]));
	}

	public void visit(RegInstrVisitor v) { v.visit(this); }
    }
    
    protected abstract class RegInstrVisitor extends InstrVisitor {
	public void visit(FskStore i) { visit((InstrMEM) i); }
	public void visit(FskLoad i) { visit((InstrMEM) i); }
    }

    /** Creates a <code>RegAlloc</code>. 
	
	<BR> <B>Design Issue:</B> should there be a RegAlloc object
	for every method, or just for every machine target?  For now
	it seems associating a new one with every method will save a
	lot of headaches.

    */
    protected RegAlloc(Code code) {
        this.frame = code.getFrame();
	this.code = code;
	HasEdges first = (HasEdges) code.getRootElement();
	rootBlock = BasicBlock.computeBasicBlocks(first);
    }
    
    /** Assigns registers in the code for <code>this</code>.
	
	<BR> <B>effects:</B> Partially or completely allocates
	     registers for the values defined and used in the code for
	     <code>this</code>.  Values will be preserved in the code;
	     any live value will be stored before its assigned
	     register is overwritten.
    */
    protected abstract Code generateRegAssignment();

    
    /** Returns the root of the <code>BasicBlock</code> hierarchy for
	the <code>Code</code> associated with <code>this</code>.
    */
    protected BasicBlock getBasicBlocks() {
	return rootBlock;
    }


    /** Creates a register-allocating <code>HCodeFactory</code> for
	"instr" form.
	<BR> <B>requires:</B> <code>parentFactory</code> produces code
	     in a derivative of "instr" form.
	<BR> <B>effects:</B> Produces an <code>HCodeFactory</code>
	     which allocates registers in the code produced by
	     <code>parentFactory</code> using the machine properties
	     specified in <code>frame</code>.

	<BR> <B>DESIGN NOTE:</B> This method relies on the subclasses
	     of <code>RegAlloc</code> to perform actual allocation.
	     This causes a cycle in our module dependency graph,
	     which, while not strictly illegal, tends to be a sign of
	     a design flaw. Consider moving the code factory generator
	     out of the <code>RegAlloc</code> class into a seperate
	     class to get rid of the cycle.  In the meantime, any new
	     <code>RegAlloc</code> subclasses can be incorporated into
	     this method to be used in the compiler.  Perhaps should
	     also design a way to parameterize which
	     <code>RegAlloc</code> subclasses will be used.
     */
    public static HCodeFactory codeFactory(final HCodeFactory parentFactory, 
					   final Frame frame) {
	return new HCodeFactory() {
	    HCodeFactory parent = parentFactory;
	    Frame f = frame;
	    public HCode convert(HMethod m) {
		HCode preAllocCode = parent.convert(m);
		LocalCffRegAlloc localCode = 
		    new LocalCffRegAlloc((Code) preAllocCode);
		//DemandDrivenRegAlloc globalCode = 
		//   new DemandDrivenRegAlloc(frame, localCode.generateRegAssignment()); 
		//return globalCode.generateRegAssignment();

		return localCode.resolveOutstandingTemps( localCode.generateRegAssignment() );
	    }
	    public String getCodeName() {
		return parent.getCodeName();
	    }
	    public void clear(HMethod m) {
		parent.clear(m);
	    }
	};
    }

    /** Transforms Temp references in 'in' into appropriate offsets
	from the Stack Pointer in the Memory. 
        <BR> <B>modifies:</B> in
	<BR> <B>effects:</B> Replaces the <code>FskLoad</code> and
	     <code>FskStore</code>s with memory instructions for the
	     appropriate <code>Frame</code>.
    */
    protected HCode resolveOutstandingTemps(HCode in) {
	// This implementation is REALLY braindead.  Fix to do a
	// smarter Graph-Coloring stack offset allocator

	class TempFinder extends RegInstrVisitor {
	    HashMap tempsToOffsets = new HashMap();
	    int nextOffset = 1;

	    public void visit(FskLoad m) {
		// look for non-Register Temps in use, adding
		// them to internal map
		for(int i=0; i<m.use().length; i++){
		    if(!isTempRegister(m.use()[i]) &&
		       tempsToOffsets.get(m.use()[i])==null){
			tempsToOffsets.put
			    (m.use()[i], new Integer(nextOffset));
			nextOffset++;
		    }
		}
	    } 
	    public void visit(FskStore m) {
		// look for non-Register Temps in def, adding
		// them to internal map
		for(int i=0; i<m.def().length; i++){
		    if(!isTempRegister(m.def()[i]) &&
		       tempsToOffsets.get(m.def()[i])==null){
			tempsToOffsets.put
			    (m.def()[i], new Integer(nextOffset));
			nextOffset++;
		    }
		}
	    } 
	    public void visit(Instr m) {
		// do nothing
		 
		// adding a check to see if use/def are indeed all "in
		// registers"
		boolean check = true;
		for(int i=0; i<m.def().length; i++){
		    if(!isTempRegister(m.def()[i])) {
		       check = false; break;
		    }
		}
		for(int i=0; i<m.use().length; i++){
		    if(!isTempRegister(m.use()[i])) {
		       check = false; break;
		    }
		}
		Util.assert(check, " Temp reference in " + m);

	    }
	}
	
	// Not sure how to handle multiple Temp references in one
	// InstrMEM...for now will assume that there is only one
	// memory references per InstrMEM...
	class InstrReplacer extends RegInstrVisitor {
	    HashMap tempsToOffsets;
	    InstrReplacer(HashMap t2o) {
		tempsToOffsets = t2o; 
	    }
 	    
	    
	    // Make these SMARTER: get rid of requirement that Loads
	    // and Stores have only one references to memory (to
	    // allow for StrongARMs ldm* instructions
	    public void visit(FskStore m) {
		// replace all non-Register Temps with appropriate
		// stack offset locations
		List instrs = frame.makeStore
		    (m.use()[0], 
			 ((Integer)tempsToOffsets.get(m.def()[0])).intValue(),
			 m);
		    Instr.replaceInstrList(m, instrs);
	    }
	 
	    public void visit(FskLoad m) {
		// replace all non-Register Temps with appropriate
		// stack offset locations
		List instrs = frame.makeLoad
		    (m.def()[0], 
		     ((Integer)tempsToOffsets.get(m.use()[0])).intValue(),
			 m);
		Instr.replaceInstrList(m, instrs);
	    }
	
	 
	    public void visit(Instr i) {
		//Do nothing
	    }
	}

	TempFinder tf = new TempFinder();
	Iterator instrs = in.getElementsI();
	while(instrs.hasNext()) {
	    Instr i = (Instr) instrs.next();
	    i.visit(tf);
	}
	// now tf should have a full map of Temps to needed Stack
	// Offsets.

	// System.out.println("TempsToOffsets Mapping: " + tf.tempsToOffsets);

	InstrReplacer ir = new InstrReplacer(tf.tempsToOffsets);
	instrs = in.getElementsI();
	while(instrs.hasNext()) {
	    Instr i = (Instr) instrs.next();
	    i.visit(ir);
	}

	return in;
    }
    

    /** Checks if <code>t</code> is a register (Helper method).
	<BR> <B>effects:</B> If <code>t</code> is a register for the
	     <code>frame</code> associated with <code>this</code>,
	     then returns true.  Else returns false.   
    */ 
    protected boolean isTempRegister(Temp t) {
	return frame.isRegister(t);
        
	// Temp[] allRegs = frame.getAllRegisters();
	// boolean itIs = false;
	// for (int i=0; i < allRegs.length; i++) {
	//    if (t.equals(allRegs[i])) {
	//	itIs = true;
	//	break;
	//    }
	// }
	// return itIs;
    }

    /** Checks if <code>i</code> is last use of <code>reg</code> in
	the block of instructions lists in <code>iter</code>.  
	
	<BR> <B>requires:</B> 
	     <BR> 1. <code>i</code> is an element in <code>iter</code> 
	     <BR> 2. <code>iter</code> is an <code>Iterator</code> of
	             a linear series of <code>Instr</code>s in the
		     order that they will be executed.
	     <BR> 3. <code>iter</code> is currently indexed at
	             <code>i</code> 
	<BR> <B>modifies:</B> <code>iter</code>
	<BR> <B>effects:</B> 
             <BR> 1. Returns true if no instruction after
	             <code>i</code> in <code>iter</code> uses
		     <code>reg</code> before <code>reg</code> is
		     redefined (<code>i</code> redefining
		     <code>reg</code> is sufficient).  Else returns
		     false. 
	     <BR> 2. <code>iter</code> is left at an undetermined
	             index. 
    */
    protected static boolean lastUse(Temp reg, UseDef i, Iterator iter) {
	UseDef curr = i;
	boolean r = true;
	while (iter.hasNext() && ! contained( curr.def(), reg ) ) {
	    curr = (UseDef) iter.next();
	    if (contained( curr.use(), reg )) {
		r = false;
		break;
	    }
	}
	return r;
    } 

    private static boolean contained(Object[] array, Object o) {
	boolean yes = false;
	for (int i=0; i<array.length; i++) {
	    if (array[i] == o) {
		yes = true;
		break;
	    }
	}
	return yes;
    }

    static class BrainDeadLocalAlloc extends RegAlloc {
	BrainDeadLocalAlloc(Code code) {
	    super(code);
	}
	
	class BrainDeadInstrVisitor extends InstrVisitor {
	    Temp[] regs = frame.getGeneralRegisters();
		
	    public void visit(Instr instr) {
		InstrFactory inf = instr.getFactory();
		    
		try {
		    // in this (dumb) model, each instruction will
		    // load all uses and store all defs, so we can
		    // treat the register file as being empty for each
		    // instruction

		    Map regFile = new LinearMap();

		    // load srcs
		    for(int i=0; i<instr.use().length; i++) {
			Temp preg = instr.use()[i];
			Iterator iter =
			    frame.suggestRegAssignment(preg, regFile); 
			List regList = (List) iter.next();
			String dests = ""; 
			for(int j=0; j<regList.size(); j++) {
			    regFile.put(regList.get(j), preg);
			    dests += ("`d"+j+", ");
			}
			InstrMEM loadSrcs = 
			    new FskLoad(inf, null, "FSK-LOAD " + dests
					+ "`s0", regList, preg); 
			Instr.insertInstrBefore(instr, loadSrcs);
			code.assignRegister(instr, preg, regList);
		    }
		    // store dsts
		    for(int i=0; i<instr.def().length; i++) {
			Temp preg = instr.def()[i];
			Iterator iter =
			    frame.suggestRegAssignment(preg, regFile); 
			List regList = (List) iter.next();
			String srcs = "";
			for (int j=0; j<regList.size(); j++) {
			    regFile.put(regList.get(j), preg);
			    srcs += (", `s"+j);
			}
			InstrMEM storeDsts = 
			    new FskStore(inf, null, "FSK-STORE `d0"+srcs,
					 preg, regList);
			Instr.insertInstrAfter(instr, storeDsts);
			code.assignRegister(instr, preg, regList);
		    }
		} catch (Frame.SpillException e) {
		    Util.assert(false, "One Instr uses/defines more "+
				"registers than "+frame+" can accomidate "+
				"in Register File!"); 
		}
	    }

	}
	/** For each instruction:
	        1. Load every use from memory into the register file.
		2. Execute the instruction
		3. Store every dest from the register file
	    regFile will be clean in between each instruction in this
	    (very dumb) allocation strategy. 
	*/
	protected Code generateRegAssignment() {
	    Iterator instrs = code.getElementsI();
	    InstrVisitor memVisitor = new BrainDeadInstrVisitor();

	    while(instrs.hasNext()) {
		((Instr)instrs.next()).visit(memVisitor);
	    }

	    return code;
	}
    }
   
}

