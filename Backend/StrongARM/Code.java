// Code.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.ArrayFactory;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrFactory;

import java.util.*;

/**
 * <code>StrongARM.Code</code> is a code-view for StrongARM
 * assembly-like syntax (currently without register allocation).
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Code.java,v 1.1.2.3 1999-02-16 21:14:39 andyb Exp $
 */
public class Code extends HCode {
    /** The name of this code view. */
    public static final String codename = "strongarm";
    /** The method that this code view represents. */
    protected HMethod parent;
    /** The StrongARM instructions composing this code view. */
    protected Instr[] instrs;
    /** Instruction factory. */
    final InstrFactory inf;

    /** Creates a new <code>InstrFactory</code> for this codeview.
     *  
     *  @param  parent  The method which this codeview corresponds to.
     *  @return         Returns a new instruction factory for the scope
     *                  of the parent method and this codeview.
     */
    protected InstrFactory newINF(final HMethod parent) {
        final String scope = parent.getDeclaringClass().getName() + "." +
            parent.getName() + parent.getDescriptor() + "/" + getName();
        return new InstrFactory() {
            private final TempFactory tf = Temp.tempFactory(scope);
            private int id = 0;
            public TempFactory tempFactory() { return tf; }
            public HCode getParent() { return Code.this; }
            public synchronized int getUniqueID() { return id++; }
        };
    }

    /** Creates a new codeview with a given parent method and
     *  assembly instructions.
     *
     *  @param  parent  The method which this codeview corresponds to.
     *  @param  instrs  A list of StrongARM assembly instructions,
     *                  may be null.
     */
    Code(final HMethod parent, final Instr[] instrs) {
        this.parent = parent;
        this.instrs = instrs; 
        this.inf = newINF(parent);
    }

    /** Registers this codeview so that it will be recognized later. <BR>
     *  XXX - NOT YET IMPLEMENTED.
     */
    public static void register() { }

    /** Returns the method which this codeview is representing code for.
     *
     *  @return         An <code>HMethod</code> for this codeview's
     *                  parent method.
     */
    public HMethod getMethod() { return parent; }

    /** Returns the name of this code view.
     *
     *  @return         The String naming this codeview, "strongarm".
     */
    public String getName() { return codename; }

    /** Returns an array of the instructions in this codeview. <BR>
     *
     *  @return         An array of HCodeElements containing the Instrs
     *                  that make up this codeview.
     */
    public HCodeElement[] getElements() { return instrs; }

    /** Returns an enumeration of the instructions in this codeview. <BR>
     *  XXX - NOT YET IMPLEMENTED.
     *
     *  @return         An Enumeration containing the Instrs that make
     *                  up this codeview.
     */
    public Enumeration getElementsE() {
        return null;
    }
   
    /** Returns an array factory to create the instruction elements
     *  of this codeview.
     *
     *  @return         An ArrayFactory which produces Instrs.
     */
    public ArrayFactory elementArrayFactory() { return Instr.arrayFactory; }

    /** Displays the assembly instructions of this codeview. Attempts
     *  to do so in a well-formatted, easy to read way. <BR>
     *  XXX - currently uses generic, not so easy to read, printer.
     *
     *  @param  pw      A java.io.PrintWriter to send the formatted
     *                  output to.
     */
    public void print(java.io.PrintWriter pw) {
        super.print(pw);
    }
}
