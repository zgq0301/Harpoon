// OffsetMap.java, created Thu Jan 14 22:17:20 1999 by duncan
// Copyright (C) 1998 Duncan Bryce  <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HClass;
import harpoon.Temp.Label;

import java.util.Iterator;

/**
 * An <code>OffsetMap</code> maps an <code>HField</code> or an 
 * <code>HMethod</code> to an offset in bytes.  It also reports the
 * total size of an <code>HClass</code> object.
 * 
 * @author  Duncan Bryce  <duncan@lcs.mit.edu>
 * @version $Id: OffsetMap.java,v 1.1.2.14 1999-08-11 10:41:14 duncan Exp $
 */
public abstract class OffsetMap { // use an abstract class, if we can.

    /** Maps an <code>HClass</code> to an offset (in bytes).  
     *  Returns the offset from an object reference of the class pointer */
    public abstract int clazzPtrOffset(HClass hc);

    /** Returns the size (in bytes) of the display information.  
     */
    public abstract int displaySize();

    /** Maps an <code>HClass</code> to an offset (in bytes).  
     *  If hc is an array type, returns the offset of the
     *  array's 0th element. */
    public abstract int elementsOffset(HClass hc);

    /** Maps an <code>HClass</code> to an offset (in bytes).
     *  Returns the offset from an object reference at which the hashcode
     *  is stored. */
    public abstract int hashCodeOffset(HClass hc);

    /** Returns the offset from the class pointer of the list of interfaces
     *  implemented by the specified class
     */
    public abstract int interfaceListOffset(HClass hc);

    /** Maps an <code>HClass</code> to a <code>Label</code> representing the 
     *  location of its class pointer  */
    public abstract Label label(HClass hc);

    /** Maps a static <code>HField</code> to a <code>Label</code>. */
    public abstract Label label(HField hf);

    /** Maps an <code>HMethod</code> to a <code>Label</code>. Note that
     *  the method does not have to be static or final; in many cases we
     *  can determine the identity of a virtual function exactly using 
     *  type information, and <code>label()</code> should return a
     *  <code>Label</code> we can use to take advantage of this information. */
    public abstract Label label(HMethod hf);

    /** Maps a <code>String</code> constant to a <code>Label</code>. */
    public abstract Label label(String stringConstant);

    /** Maps an <code>HClass</code> to an offset (in bytes).  
     *  If <code>hc</code> is an array type, returns the offset from 
     *  an object reference of the array's length field. */
    public abstract int lengthOffset(HClass hc); 

    /** Maps an <code>HClass</code> to an offset (in bytes).
     *  Returns the offset from the class pointer of the specified
     *  class.  This will be some function of the class's depth in
     *  the class hierarchy. */
    public abstract int offset(HClass hc);

    /** Maps a non-static <code>HField</code> to an offset (in bytes).
     *  If the field is inlined using type 1 inlining (which preserves
     *  the class pointer) then the specified offset points just after the
     *  class descriptor, in the same place a normal object pointer points.
     *  If the field in inlined using type 2 inlining (which omits the
     *  class pointer) then the specified offset points to the first field
     *  of the object. */
    public abstract int offset(HField hf);

    /** Maps a non-static <code>HMethod</code> to an offset (in bytes).
     *  This method must work for interface methods as well as class methods.*/
    public abstract int offset(HMethod hm);

    /** Maps an <code>HClass</code> to a size (in bytes). */
    public abstract int size(HClass hc);

    /** Returns an <code>Iterator</code> of all string constants which this
     *  offset map knows about.  */
    public abstract Iterator stringConstants();
}



