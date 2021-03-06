// ClassReplacer.java, created Fri Mar 23 10:31:56 2001 by wbeebee
// Copyright (C) 2000 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import java.io.PrintWriter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Transformation.MethodMutator;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;

import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadWithTry;
import harpoon.IR.Quads.QuadNoSSA;

import harpoon.Util.Collections.SnapshotIterator;

import harpoon.Util.Util;

/** 
 * <code>ClassReplacer</code> is a <code>MethodMutator</code> which 
 * works on any QuadForm and replaces NEW's and CALL's to one class with 
 * NEW's and CALL's to another class using a mapping function to map methods 
 * of one to methods of the other.  It can also ignore (not update) listed
 * classes or packages.  This class allows you to write a wrapper for a 
 * class for which you don't have the code, and have other code selectively
 * point to the wrapper.
 *
 * Currently, only QuadNoSSA and QuadWithTry are supported.  This may change
 * in the future.
 */

public class ClassReplacer extends MethodMutator {
    private HashMap methodMap;
    private HClass fromClass, toClass;
    private Set ignorePackages;
    private Set ignoreClasses;
    private String codeName;
    private static final boolean debugOutput = false;

    /** 
     * Construct a <code>ClassReplacer</code> with an <code>HCodeFactory</code>
     * that will replace the <code>HClass</code> <code>from</code> with 
     * <code>to</code>.
     */

    public ClassReplacer(HCodeFactory parent, HClass from, HClass to) {
	super(parent);
	fromClass = from;
	toClass = to;
	methodMap = new HashMap();
	ignorePackages = new HashSet();
	ignoreClasses = new HashSet();
	codeName = parent.getCodeName();
	assert codeName.equals(QuadWithTry.codename)||
		    codeName.equals(QuadNoSSA.codename) : ("currently only QuadWithTry and QuadNoSSA are supported: " +
		    codeName + " cannot be used as a parent for a ClassReplacer");
    }

    /**
     * Do not replace references to <code>from</code> in the package 
     * <code>pkg</code>.
     */

    public void addIgnorePackage(String pkg) {
	ignorePackages.add(pkg);
    }

    /**
     * Do not replace references to <code>from</code> in the class
     * <code>claz</code>.  <code>from</code> is automatically ignored.
     */

    public void addIgnoreClasses(HClass claz) {
	ignoreClasses.add(claz);
    }

    /**
     * Map a method on the original class to a method on the "to" class.
     * Only mapped method referencesd will be replaced.
     */

    public void map(HMethod from, HMethod to) {
	assert from.getReturnType() == to.getReturnType();

	HClass[] types = from.getParameterTypes();
	HClass[] types2 = to.getParameterTypes();

	for (int i=0; i<types.length; i++) {
	    assert types[i].equals(types2[i]);
	}

	methodMap.put(from, to);
    }

    /**
     * Map all of the methods from <code>HClass<code> <code>from</code>
     * to <code>to</code> that share the same name and method signature.
     * Warning: slow for big classes.
     */

    public void mapAll(HClass from, HClass to) {
	HMethod[] fromMethods = from.getMethods();
	HMethod[] toMethods = to.getMethods();
	for (int i=0; i<fromMethods.length; i++) {
	    HClass[] types = fromMethods[i].getParameterTypes();
	    for (int j=0; j<toMethods.length; j++) {
		HClass[] types2 = toMethods[j].getParameterTypes();
		if (fromMethods[i].getReturnType()
		    .equals(toMethods[j].getReturnType())&&
		    (types.length == types2.length)&&
		    fromMethods[i].getName().equals(toMethods[j].getName())&&
		    (!fromMethods[i].getDeclaringClass().equals(toMethods[j].getDeclaringClass()))) {
		    boolean mapMethod = true;
		    for (int k=0; k<types.length; k++) {
			if (!types[k].equals(types2[k])) {
			    mapMethod = false;
			    break;
			}
		    }
		    
		    if (mapMethod) {
			if (debugOutput) {
			    System.out.println("Mapping methods: " + 
					       fromMethods[i].toString() +
					       " to: " + toMethods[j].toString());
			} 
			methodMap.put(fromMethods[i], toMethods[j]);
		    }
		}
	    }
	}
    }

    /**
     * Get the <code>QuadVisitor</code> that will make the replacements to the code,
     * parameterized by <code>codeName</code>.  Currently only <code>QuadNoSSA</code> and
     * <code>QuadWithTry</code> are supported.  Change this method if you want to
     * support more.
     */

    private QuadVisitor getQuadVisitor(final String codeName) {
	final HClass from = fromClass;
	final HClass to = toClass;
	return new QuadVisitor() {
		public void visit(CALL q) {
		    HMethod method = q.method();
		    HClass claz = method.getDeclaringClass();

		    if ((claz == to) || // Current class automatically on 
			                  // ignore list.
			(!methodMap.containsKey(method))) return;

		    CALL newCALL =
			new CALL(q.getFactory(), q,
				 (HMethod)methodMap.get(method),
				 q.params(), q.retval(), q.retex(),
				 q.isVirtual(), q.isTailCall(),
				 q.dst(), q.src());
		    if (debugOutput) {
			System.out.println("Replacing "+q+" with "+newCALL);
		    }
		    Quad.replace(q, newCALL);
		    if (codeName.equals(QuadWithTry.codename)) {
			Quad.transferHandlers(q, newCALL);
		    }
		}
		
		public void visit(NEW q) {
		    
		    if (q.hclass() != from) {
			return;
		    }

		    NEW newNEW = new NEW(q.getFactory(),
					 q, q.dst(), to);
		    if (debugOutput) {
			System.out.println("Replacing "+q+" with "+newNEW);
		    }
		    Quad.replace(q, newNEW);
		    if (codeName.equals(QuadWithTry.codename)) {
			Quad.transferHandlers(q, newNEW);
		    }
		}

		public void visit(Quad q) {}
	    };
    }

    /**
     * Make the actual changes to the HCode.
     */

    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
	HClass hclass = hc.getMethod().getDeclaringClass();
	if ((hc == null)||
	    (ignoreClasses.contains(hclass))||
	    (ignorePackages.contains(hclass.getPackage()))) {
	    return hc;
	}
	
	QuadVisitor visitor = getQuadVisitor(codeName);

	if (debugOutput) {
 	    System.out.println("Before replacing " + fromClass.getName() +
 			       " with " + toClass.getName() + ":");
 	    hc.print(new PrintWriter(System.out));
	}

	// note that we use a SnapshotIterator so that qi is 'static';
	// i.e. the underlying list of elements to iterate over does not
	// change as the visitor mutates the hcode.
	Iterator qi = new SnapshotIterator(hc.getElementsI());
	while (qi.hasNext()) ((Quad)(qi.next())).accept(visitor);

	if (debugOutput) {
 	    System.out.println("After replacing " + fromClass.getName() +
 			       " with " + toClass.getName() + ":");
 	    hc.print(new PrintWriter(System.out));
	}
	return hc;
    }
}    
