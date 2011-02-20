// InstrFactory.java, created Tue Feb  9  0:45:33 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.Backend.Generic.Frame;
import harpoon.Temp.Label;
import harpoon.Temp.TempFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A <code>InstrFactory</code> is responsible for generating 
 * generic <code>Assem.Instr</code>s used in code generation.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: InstrFactory.java,v 1.3 2002-04-10 03:04:27 cananian Exp $
 */
public abstract class InstrFactory {
    /** Maintains a
	<code>Temp.Label</code><code>-></code><code>InstrLABEL</code>
	<code>Map</code> for <code>Instr</code>s constructed by
	<code>this</code>.  Used in dynamic <code>CFGraphable</code>
	successor resolution.
    */
    Map<Label,InstrLABEL> labelToInstrLABELmap=new HashMap<Label,InstrLABEL>();

    /** Maintains a 
	<code>Temp.Label</code> <code>-></code>
	</code>Set</code><code>[</code> <code>Instr</code> <code>]</code>     
	<code>Map</code> for <code>Instr</code>s constructed by
	<code>this</code>.  Used in dynamic <code>CFGraphable</code>
	predecessor resolution. 
	Note that the <code>get(label)</code> method for this object
	will never return <code>null</code>; it will just create new
	empty sets as needed and return them instead. 
    */
    Map<Label,Set<Instr>> labelToBranches = new HashMap<Label,Set<Instr>>() {
	public Set<Instr> get(final Object key) {
	    Set<Instr> v = super.get(key);
	    if (v != null) {
		return v;
	    } else {
		v = new HashSet<Instr>()
		/* {
		    public boolean add(Object o) { 
			System.out.println("Adding " + o + " to " + this); 
			return super.add(o);
		    } 
		    public String toString() { 
			return "Set of Instrs branching->"+key; 
		    } 
		}
		*/  ;
		put((Label)key, v);
		return v;
	    }
	}
    };
    
    /** Caches the end of the instruction layout to allow for arbtrary
	code appending. 
    */     
    Instr cachedTail = null;
    public Instr getTail() {
	if (cachedTail == null) return null;
	while(cachedTail.next != null) {
	    cachedTail = cachedTail.next;
	}
	return cachedTail;
    }

    /** Returns the <code>TempFactory</code> to use for creating
     *  <code>Temp</code>s which are used as arguments to <code>Instr</code>s
     *  generated by this factory. */
    public abstract TempFactory tempFactory();

    /** Returns the <code>HCode</code> to which all <code>Instr</code>s
     *  generated by this factory belong. 
     */ 
    public abstract Code getParent(); 

    /** Returns the <code>Frame</code> which is associated with all
     *  of the <code>Instr</code>s generated by this factory. */
    public abstract Frame getFrame();

    /** Returns the <code>HMethod</code> which corresponds to 
	<code>Instr</code>s generated by this factory.
	<BR><B>effects:</B> Returns the <code>HMethod</code>
	    associated with <code>this</code>, or <code>null</code> if
	    no such <code>HMethod</code> exists.  
    */
    public HMethod getMethod() { return getParent().getMethod(); }

    /** Returns a unique ID number for each new <code>Instr</code>
     *  generated by this factory. */
    public abstract int getUniqueID();

    /** Returns a human-readable representation for this 
     *  <code>InstrFactory</code>. */
    public String toString() {
        return "InstrFactory["+getParent()+"]";
    }

    public int hashCode() { return getParent().getName().hashCode(); }

    class ToMap extends HashMap<InstrGroup.Type,Map> {
	public Map getMap(InstrGroup.Type key) {
	    Map m = get(key);
	    if (m == null) { m = new HashMap(); super.put(key, m); }
	    return m;
	}
    }
    private ToMap typeToInstrToGroup = new ToMap();

    /** Adds information from <code>group</code> to the
	InstrGroup.Type -> CFGrapher mapping for <code>this</code>.
	<BR> <B>requires:</B> <code>group</code> has had its entry and
	     exit fields assigned.
    */
    public void addGroup(InstrGroup group) {
	Map m = typeToInstrToGroup.getMap(group.type);
	m.put(group.entry, group);
	m.put(group.exit, group);
    }
    
    /** Returns a <code>CFGrapher</code> that treats
	<code>InstrGroup</code>s of <code>Type</code>
	<code>t</code> as single atomic elements.
    */
    public CFGrapher<Instr> getGrapherFor(InstrGroup.Type t) { 
	final Map<Instr,InstrGroup> i2g = typeToInstrToGroup.getMap(t); 
	return new InstrGroup.GroupGrapher(i2g);
    }
    
    /** Returns a <code>UseDefer</code> that treats
	<code>InstrGroup</code>s of <code>Type</code> 
	<code>t</code> as single atomic elements.
    */
    public UseDefer<Instr> getUseDeferFor(InstrGroup.Type t) {
	final Map<Instr,InstrGroup> i2g = typeToInstrToGroup.getMap(t);
	return new InstrGroup.GroupUseDefer(i2g, t);
    }

}
