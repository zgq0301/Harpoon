// Debug.java, created Thu Feb 10 19:06:16 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Set;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collection;

import harpoon.Util.UComp;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Analysis.MetaMethods.MetaMethod;

import harpoon.Util.LightBasicBlocks.LightBasicBlock;
import harpoon.Util.Graphs.SCComponent;
import harpoon.ClassFile.HCodeElement;

import harpoon.Util.DataStructs.Relation;


/**
 * <code>Debug</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: Debug.java,v 1.5 2003-06-04 18:44:31 salcianu Exp $
 */
public abstract class Debug implements java.io.Serializable {

    /** Returns a sorted array containing all the objects from
	<code>set</code>. Increasing lexicographic order of the
	string representation of the objects is used. */
    public static Object[] sortedCollection(Collection col){
	Object[] objs = col.toArray(new Object[col.size()]);
	Arrays.sort(objs, UComp.uc);
	return objs;
    }

    public static Object[] sortedSet(Set set) {
	return sortedCollection(set);
    }

    
    /** Provides a string representation of a set; the elements of the
	set appear in increasing lexicographic order.
	<code>set1.equals(set2) <==> stringImg(set1).equals(stringImg(set2)).</code>*/
    public static String stringImg(Collection col){
	StringBuffer buffer = new StringBuffer();

	Object[] objs = sortedCollection(col);

	buffer.append("[ ");
	for(int i = 0; i < objs.length; i++){
	    buffer.append(objs[i]);
	    buffer.append(" ");
	}
	buffer.append("]");

	return buffer.toString();
    }

    public static String stringImg(Object[] v){

	if(v==null) return "null";

	StringBuffer buffer = new StringBuffer();

	Arrays.sort(v,UComp.uc);
	for(int i=0; i<v.length;i++){
	    buffer.append(v[i]);
	    buffer.append("\n");
	}

	return buffer.toString();
    }

    /** Displays a <i>split</i> relation (see the MetaCallGraph stuff). */
    public static void show_split(Relation split){
	for(Iterator it = split.keys().iterator(); it.hasNext(); ){
	    HMethod hm = (HMethod) it.next();
	    System.out.print(hm);
	    System.out.println("  (" + split.getValues(hm).size() +
			       " specialization(s))");
	    Iterator itmm = split.getValues(hm).iterator();
	    while(itmm.hasNext()) {
		System.out.print("  ");
		System.out.println((MetaMethod)itmm.next());
	    }
	}
    }


    public static void show_lbb(LightBasicBlock lbb){
	System.out.println("  " + lbb + "{");
	HCodeElement[] hces = lbb.getElements();
	for(int i = 0; i < hces.length; i++)
	    System.out.println("    " + hces[i].getSourceFile() + ":" +
			       hces[i].getLineNumber() + " " + hces[i]);

	LightBasicBlock[] prev = lbb.getPrevLBBs();
	if(prev.length != 0){
	    System.out.print("   Prev: ");
	    for(int i = 0; i < prev.length; i++)
		System.out.print(prev[i] + " ");
	    System.out.println();
	}

	LightBasicBlock[] next = lbb.getNextLBBs();
	if(next.length != 0){
	    System.out.print("   Next: ");
	    for(int i = 0; i < next.length; i++)
		System.out.print(next[i] + " ");
	    System.out.println();
	}
	
	System.out.println("  }");
    }

    public static void show_lbb_scc(SCComponent scc){
	for(; scc != null; scc = scc.nextTopSort()){
	    System.out.println("SCC" + scc.getId() + "{");

	    Object nodes[] = scc.nodes();
	    Arrays.sort(nodes, harpoon.Util.UComp.uc);
	    for(int i = 0; i < nodes.length; i++) 
		show_lbb((LightBasicBlock) nodes[i]);

	    System.out.println("}");
	}
    }


    /** Checks whether the method <code>hm</code> is the method named
	<code>mthd</code> from the class named <code>cls</code>. */
    public static boolean isThatOne(HMethod hm, String cls, String mthd) {
	return
	    hm.getName().equals(mthd) &&
	    hm.getDeclaringClass().getName().equals(cls);
    }
}
