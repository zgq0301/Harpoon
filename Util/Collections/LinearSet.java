// LinearSet.java, created Wed Aug  4 12:03:54 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.AbstractSet;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * <code>LinearSet</code> is a simplistic light-weight
 * <code>Set</code> designed for use when the number of entries is
 * small.  It is backed by a <code>List</code>.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: LinearSet.java,v 1.3 2002-04-10 03:07:12 cananian Exp $
 */
public class LinearSet<E> extends AbstractSet<E> implements Cloneable,
						      java.io.Serializable {
    private List<E> list;
    private ListFactory<E> lf;

    /** Creates a <code>LinearSet</code>. Uses an
	<code>ArrayList</code> as the backing store.*/
    public LinearSet() {
        this(Factories.arrayListFactory());
    }

    /** Creates a <code>LinearSet</code> with given capacity. 
	Uses an <code>ArrayList</code> as the backing store.
     */
    public LinearSet(final int capacity) {
	this(Factories.arrayListFactory(), capacity);
    }

    /** Creates a <code>LinearSet</code>, filling it with the elements
	of <code>set</code>.  Uses an <code>ArrayList</code> as the
	backing store.
    */
    public <T extends E> LinearSet(final Set<T> set) {
	this(Factories.arrayListFactory(), set);
    }

    /** Creates an empty <code>LinearSet</code>, using a
	<code>List</code> generated by <code>lf</code> as the backing
	store.  
    */
    public LinearSet(final ListFactory<E> lf) {
	list = lf.makeList();
	this.lf = lf;
    }

    /** Creates an empty <code>LinearSet</code> with a given capacity,
	using a <code>List</code> generated by <code>lf</code> as the
	backing store.  
    */
    public LinearSet(final ListFactory<E> lf, int capacity) {
	list = lf.makeList(capacity);
	this.lf = lf;
    }

    /** Creates an empty <code>LinearSet</code>, using a
	<code>List</code> generated by <code>lf</code> as the backing
	store, and fills it with the elements of <code>set</code>.
    */
    public <T extends E> LinearSet(final ListFactory<E> lf, final Set<T> set) {
	list = lf.makeList(set);
	this.lf = lf;
    }

    public Iterator<E> iterator() {
	return list.iterator();
    }

    public int size() {
	return list.size();
    }

    public boolean add(E o) {
	if (list.contains(o)) {
	    return false;
	} else {
	    list.add(o);
	    return true;
	}
    }

    public <T extends E> boolean addAll(Collection<T> c) {
	HashSet<E> s = new HashSet<E>(this.size() + c.size());
	s.addAll(this);
	boolean r = s.addAll(c);
	this.list = lf.makeList(s);
	return r;
    }

    public boolean remove(Object o) {
	int index = list.indexOf(o);
	if (index == -1) {
	    return false;
	} else {
	    list.remove(index);
	    return true;
	}
    }

    public Object clone() {
	return new LinearSet<E>(this);
    }
}
