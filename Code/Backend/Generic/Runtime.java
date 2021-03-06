// Runtime.java, created Wed Sep  8 14:24:18 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.Analysis.CallGraph;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Translation.Exp;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.io.Serializable;

/**
 * A <code>Generic.Runtime</code> provides runtime-specific
 * information to the backend.  It should be largely-to-totally
 * independent of the particular architecture targetted; all
 * interfaces in Runtime interact with <code>IR.Tree</code> form,
 * not the architecture-specific <code>IR.Assem</code> form.<p>
 * Among other things, a <code>Generic.Runtime</code> provides
 * class data constructors to provide class information to the
 * runtime system.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Runtime.java,v 1.5 2003-04-19 01:03:44 salcianu Exp $
 */
public abstract class Runtime implements Serializable {
    protected Runtime() { }

    /** Returns a <code>TreeBuilder</code> object for this
     *  <code>Generic.Runtime</code>. */
    public abstract TreeBuilder getTreeBuilder();

    /** Returns a <code>NameMap</code> valid for this
     *  <code>Generic.Runtime</code>. */
    public abstract NameMap getNameMap();

    /** Prefixes a runtime-specific path onto the specific resource name,
     *  so that transformations can refer to runtime-specific properties
     *  files in a uniform manner. */
    public abstract String resourcePath(String basename);

    // methods used during initialization
    /** Sets the <code>ClassHierarchy</code> to use for this
     *  <code>Generic.Runtime</code>.  This method must be called at least
     *  once before the <code>TreeBuilder</code> or <code>classData</code>
     *  are used to generate tree form.  It may be called multiple times
     *  if transformations cause the class hierarchy to change. */
    public void setClassHierarchy(ClassHierarchy ch) { }

    /** Sets the <code>CallGraph</code> to use for this
     *  <code>Generic.Runtime</code>.  Some runtime options do not
     *  require a call graph, and thus do not require that this method
     *  ever be called.  However, for those that do, this method must
     *  be called at least once before the <code>TreeBuilder</code> or
     *  <code>classData</code> are used to generate tree form.  It may
     *  be called multiple times if transformations cause the call
     *  graph to change. */
    public void setCallGraph(CallGraph cg) { }

    /** Returns a list of <code>HData</code>s which are needed for the
     *  given class. */
    public abstract List<HData> classData(HClass hc);

    /** This code factory hook allows the runtime to return
     *  runtime-specific stubs for native methods --- or any other
     *  method which the runtime wishes to reimplement.  The
     *  default implementation just passes the given code factory
     *  through without modification.<p>
     *  The code factory returned by this method should return
     *  a <code>null</code> for methods which the output routine
     *  should skip.  For example, if stubs are not needed for
     *  native methods, then the code factory should return
     *  <code>null</code> when <code>convert()</code> is called
     *  on a native method (just as the standard code factories do).
     *  <p>
     *  The code factory given should produce tree form.  The returned
     *  code factory should also produce tree form; the tree form need
     *  not be canonicalized or optimized.
     */
    public HCodeFactory nativeTreeCodeFactory(HCodeFactory hcf) {
	assert hcf!=null && hcf.getCodeName().endsWith("tree");
	return hcf;
    }
    /** Return a <code>Set</code> of <code>HMethod</code>s 
     *  and <code>HClass</code>es which are referenced /
     *  callable by code in the runtime implementation (and should
     *  therefore be included in every class hierarchy). */
    public abstract Collection runtimeCallableMethods();

    /** Return a <code>Set</code> of labels which specify configuration
     *  dependencies.  A reference will be emitted by the FLEX component
     *  of this <code>Generic.Runtime</code> for each string in
     *  this set, which should match correspondingly-named labels in
     *  a properly configured C runtime.  This allows us to check
     *  at link time that the C runtime's configuration matches the
     *  FLEX configuration. */
    public final Set<String> configurationSet = new HashSet<String>();

    /** The <code>TreeBuilder</code> constructs bits of code in the
     *  <code>IR.Tree</code> form to handle various runtime-dependent
     *  tasks---primarily method and field access.
     */
    public abstract static class TreeBuilder implements Serializable {
	/** Utility method for external analyses that want to know the
	 *  exact size of an object. This returns the size in bytes
	 *  <strong>not including the header</strong>. */
	public abstract int objectSize(HClass hc);
	/** Utility method for external analyses that want to know the
	 *  size of an object header.  Returns the header size in bytes.
	 *  The total allocated memory for an object is
	 *  <code>objectSize(hc)+headerSize(hc)</code>. */
	public abstract int headerSize(HClass hc);

	/** Return a <code>Translation.Exp</code> giving the length of the
	 *  array pointed to by the given expression. */
	public abstract Exp arrayLength(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
					Exp arrayRef);
	/** Return a <code>Translation.Exp</code> which will create a
	 *  array of the given type, with length specified by the
	 *  given expression.  Only a single dimension of a multidimensional
	 *  array will be created. Elements of the array are only
	 *  initialized if the <code>initialize</code> parameter is
	 *  <code>true</code>; however the internal object header,
	 *  length, and any internal fields of the array inherited
	 *  from <code>java.lang.Object</code> are always initialized.
	 */
	public abstract Exp arrayNew(TreeFactory tf, HCodeElement source,
				     DerivationGenerator dg,
				     AllocationProperties ap,
				     HClass arraytype, Exp length,
				     boolean initialize);

	/** Return a <code>Translation.Exp</code> which tests the
	 *  given object expression for membership in the component
	 *  type of the given array expression. */
	public abstract Exp componentOf(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
					Exp arrayref, Exp objectref);

	/** Return a <code>Translation.Exp</code> which tests the
	 *  given expression for membership in the given class or
	 *  interface. */
	public abstract Exp instanceOf(TreeFactory tf, HCodeElement source,
				       DerivationGenerator dg,
				       Exp objectref, HClass classType);

	/** Return a <code>Translation.Exp</code> which acquires
	 *  the monitor lock of the object specified by the given
	 *  expression. */
	public abstract Exp monitorEnter(TreeFactory tf, HCodeElement source,
					 DerivationGenerator dg,
					 Exp objectref);

	/** Return a <code>Translation.Exp</code> which releases
	 *  the monitor lock of the object specified by the given
	 *  expression. */
	public abstract Exp monitorExit(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
					Exp objectref);

	/** Return a <code>Translation.Exp</code> which will create a
	 *  object of the given type, with length specified by the
	 *  given expression.  Fields of the object are only initialized
	 *  if the <code>initialize</code> parameter is <code>true</code>;
	 *  however the internal object header is always created
	 *  and initialized. */
	public abstract Exp objectNew(TreeFactory tf, HCodeElement source,
				      DerivationGenerator dg,
				      AllocationProperties ap,
				      HClass classType, boolean initialize);

	/** Return a <code>Translation.Exp</code> which represents a
	 *  reference to a <code>Class</code> constant.  The runtime
	 *  may implement this by a call to
	 *  <code>Class.forName(classname)</code> or other lookup
	 *  function, or may return a <code>NAME</code> if the class
	 *  objects are statically allocated. */
	public abstract Exp classConst(TreeFactory tf, HCodeElement source,
				       DerivationGenerator dg,
				       HClass classData);
	/** Return a <code>Translation.Exp</code> which represents a
	 *  reference to a <code>java.lang.reflect.Field</code>
	 *  constant.  The runtime may implement this by a call to
	 *  <code>Class.forName(classname).getDeclaredField(fieldname)</code>
	 *  or other lookup functions, or may return a
	 *  <code>NAME</code> if the <code>Field</code> objects are statically
	 *  allocated. */
	public abstract Exp fieldConst(TreeFactory tf, HCodeElement source,
				       DerivationGenerator dg,
				       HField fieldData);
	/** Return a <code>Translation.Exp</code> which represents a
	 *  reference to a <code>java.lang.reflect.Method</code>
	 *  constant.  The runtime may implement this by a call to
	 *  <code>Class.forName(classname).getDeclaredMethod(name,...)</code>
	 *  or other lookup functions, or may return a
	 *  <code>NAME</code> if the <code>Method</code> objects are statically
	 *  allocated. */
	public abstract Exp methodConst(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
					HMethod methodData);

	/** Return a <code>Translation.Exp</code> which represents
	 *  a reference to a string constant.  Note that invoking
	 *  <code>String.intern()</code> on the <code>String</code>
	 *  object reference returned must return a result identical to
	 *  the reference.  This may involve fix-up and/or addition
	 *  of objects to the class or global <code>HData</code>; these
	 *  tasks are the responsibility of the
	 *  <code>Generic.Runtime</code>, and it is anticipated that
	 *  the invocation of this method may have such side-effects
	 *  (although other implementations are certainly possible).
	 */
	public abstract Exp stringConst(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
					String stringData);

	// handle low-quad pointer manipulations.

	/** Return a <code>Translation.Exp</code> representing the
	 *  array-base of the object referenced by the given
	 *  <code>objectref</code> <code>Translation.Exp</code>.
	 *  This expression should point to the zero'th element of
	 *  the array object specified by <code>objectref</code>. */
	public abstract Exp arrayBase(TreeFactory tf, HCodeElement source,
				      DerivationGenerator dg,
				      Exp objectref);
	/** Return a <code>Translation.Exp</code> representing the
	 *  offset from the array base needed to access the array 
	 *  element specified by the <code>index</code> expression.
	 *  If <code>index</code> is zero, then the
	 *  <code>Translation.Exp</code> returned should also have
	 *  value zero. */
	public abstract Exp arrayOffset(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
					HClass arrayType, Exp index);
	/** Return a <code>Translation.Exp</code> representing the
	 *  field base of the object referenced by the given
	 *  <code>objectref</code> expression.  This expression should
	 *  point to the first field in the object. */
	public abstract Exp fieldBase(TreeFactory tf, HCodeElement source,
				      DerivationGenerator dg,
				      Exp objectref);
	/** Return a <code>Translation.Exp</code> representing an
	 *  offset from the field base required to access the given
	 *  <code>field</code>.  If the given field is the first in
	 *  the object, then the <code>Translation.Exp</code> returned
	 *  should have value zero. */
	public abstract Exp fieldOffset(TreeFactory tf, HCodeElement source,
					DerivationGenerator dg,
					HField field);
	/** Return a <code>Translation.Exp</code> representing the
	 *  method base of the object referenced by the given
	 *  <code>objectref</code> expression.  This expression
	 *  should point a location containing a pointer to the
	 *  first method of the object. */
	public abstract Exp methodBase(TreeFactory tf, HCodeElement source,
				       DerivationGenerator dg,
				       Exp objectref);
	/** Return a <code>Translation.Exp</code> representing an
	 *  offset from the method base required to access the
	 *  given <code>method</code>.  If the given method is the
	 *  first of the object, then the <code>Translation.Exp</code>
	 *  returned should have value zero. */
	public abstract Exp methodOffset(TreeFactory tf, HCodeElement source,
					 DerivationGenerator dg,
					 HMethod method);
	/** Return a <code>Translation.Exp</code> representing an
	 *  comparison between expressions evaluating to two references,
	 *  <code>refLeft</code> and <code>refRight</code>.
	 *  Note that this is *reference* equality, not *POINTER* equality.
	 *  The <code>fieldBase</code> method, for example, is required
	 *  to return a POINTER expression (which is typically a bit-masked
	 *  and offset version of the reference value);  one valid 
	 *  implementation of <code>referenceEqual</code> would be to
	 *  compare the values returned by a call to <code>fieldBase()</code>
	 *  on the operands, but there is typically a more efficient means.
	 */
	public abstract Exp referenceEqual(TreeFactory tf, HCodeElement source,
					   DerivationGenerator dg,
					   Exp refLeft, Exp refRight);
    }
    /** The <code>ObjectBuilder</code> constructs data tables in the
     *  <code>IR.Tree</code> form to represent static objects which may
     *  be needed by the runtime---primarily string constant objects.
     *  Not every runtime need implement an <code>ObjectBuilder</code>.
     */
    public abstract static class ObjectBuilder implements Serializable {
	// bits of data about an object needed to build it.
	/** General information about a built object's type and what label to
	 *  use to refer to it. */
	public static interface Info {
	    HClass type();
	    Label  label();
	}
	/** Information needed to build an array object. */
	public static interface ArrayInfo extends Info {
	    int length();
	    Object get(int i);
	}
	/** Information needed to build a non-array object. */
	public static interface ObjectInfo extends Info {
	    Object get(HField hf);
	}

	/** Build an array.
	 * @param info Information about the type, length and contents of
	 *             the array to build.
	 * @param segtype Segment in which to put the built array.
	 * @param exported <code>true</code> if this object's label
	 *             is to be exported outside its module.
	 */
	public abstract Stm buildArray(TreeFactory tf, ArrayInfo info,
				       boolean exported);
	/** Build an object.
	 * @param info Information about the type and contents of
	 *             the object to build.
	 * @param segtype Segment in which to put the built object.
	 * @param exported <code>true</code> if this object's label
	 *             is to be exported outside its module.
	 */
	public abstract Stm buildObject(TreeFactory tf, ObjectInfo info,
					boolean exported);
    }
}
