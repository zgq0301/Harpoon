// Type.java, created Fri Feb  5 05:16:21 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

/**
 * <code>Type</code> enumerates the possible Tree expression types.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Type.java,v 1.1.2.1 1999-02-05 10:50:57 cananian Exp $
 */
public abstract class Type {
    // enumerated constants.
    /** 32-bit integer type. */
    public final static int INT = 0;
    /** 64-bit integer type. */
    public final static int LONG = 1;
    /** 32-bit floating-point type. */
    public final static int FLOAT = 2;
    /** 64-bit floating-point type. */
    public final static int DOUBLE = 3;
    /** Pointer type.  Bitwidth is machine-dependent. */
    public final static int POINTER = 4;

    // Query functions.
    public final static boolean isDoubleWord(int type) {
	return type==LONG || type==DOUBLE;// FIXME: handle POINTER
    }
    public final static boolean isFloatingPoint(int type) {
	return type==FLOAT || type==DOUBLE;
    }
    public final static boolean isPointer(int type) {
	return type==POINTER;
    }

    // human-readability.
    public final static String toString(int type) {
	switch(type) {
	case INT: return "INT";
	case LONG: return "LONG";
	case FLOAT: return "FLOAT";
	case DOUBLE: return "DOUBLE";
	case POINTER: return "POINTER";
	default: throw new RuntimeException("Unknown Type: "+type);
	}
    }
}
