// HInitializerSyn.java, created Tue Jan 11 02:40:46 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.Util;

import java.lang.reflect.Modifier;
/**
 * An <code>HInitializerSyn</code> is a mutable representation of
 * a class initializer method.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HInitializerSyn.java,v 1.4 2002-04-10 03:04:15 cananian Exp $
 */
class HInitializerSyn extends HMethodSyn implements HInitializer {
    
    /** Create a new class initializer in class <code>parent</code>. */
    public HInitializerSyn(HClassSyn parent) {
	super(parent, "<clinit>", "()V");
	this.modifiers = Modifier.STATIC | Modifier.FINAL;
    }
    
    public boolean isInterfaceMethod() { return false; }
    public int hashCode() { return HInitializerImpl.hashCode(this); }

    // can't really change any of the properties of a class initializer.
    public void addModifiers(int m) { assert m==0; }
    public void setModifiers(int m) { assert m==getModifiers(); }
    public void removeModifiers(int m) { assert (m&getModifiers())==0; }
    public void setReturnType(HClass returnType) {
	assert returnType==HClass.Void;
    }
    public void addExceptionType(HClass exceptionType) { assert false; }
    public void setExceptionTypes(HClass[] exceptionTypes) {
	assert exceptionTypes.length==0;
    }
    public void removeExceptionType(HClass exceptionType) { assert false; }
    public void setSynthetic(boolean isSynthetic) {
	assert isSynthetic==isSynthetic();
    }
}
