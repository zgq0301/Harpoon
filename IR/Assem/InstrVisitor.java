// InstrVisitor.java, created Mon Apr  5 17:24:45 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

/**
 * <code>InstrVisitor</code> is an implementation of the Visitor
 * pattern coupled to the Instr intermediate representation.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: InstrVisitor.java,v 1.1.2.4 1999-08-03 03:38:38 pnkfelix Exp $ */
public abstract class InstrVisitor  {
    
    /** Creates a <code>InstrVisitor</code>. */
    public InstrVisitor() { }
    
    /** Visit an <code>Instr</code> i. */
    public abstract void visit(Instr i);
    public void visit(InstrDIRECTIVE i) { visit((Instr)i); }
    public void visit(InstrLABEL i) { visit((Instr)i); }
    public void visit(InstrMEM i) { visit((Instr)i); }
    public void visit(InstrMOVE i) { visit((Instr)i); }
}
