// Factories.java, created Tue Oct 19 23:21:25 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** <code>Factories</code> consists exclusively of static methods that
    operate on or return <code>CollectionFactory</code>s. 
 
    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: Factories.java,v 1.3.2.2 2002-02-27 22:24:13 cananian Exp $
 */
public final class Factories {
    
    /** Private ctor so no one will instantiate this class. */
    private Factories() {
        
    }
    
    /** A <code>MapFactory</code> that generates <code>HashMap</code>s. */ 
    public static final MapFactory hashMapFactory = hashMapFactory();
    public static final <K,V> MapFactory<K,V> hashMapFactory() {
	return new SerialMapFactory<K,V>() {
	    public HashMap<K,V> makeMap(Map<K,V> map) {
		return new HashMap<K,V>(map);
	    }
	};
    }
    
    /** A <code>SetFactory</code> that generates <code>HashSet</code>s. */
    public static final SetFactory hashSetFactory = hashSetFactory();
    public static final <V> SetFactory<V> hashSetFactory() {
	return new SerialSetFactory<V>() {
	    public HashSet<V> makeSet(Collection<V> c) {
		return new HashSet<V>(c);
	    }
	    public HashSet<V> makeSet(int i) {
		return new HashSet<V>(i);
	    }
	};
    }
    
    /** A <code>SetFactory</code> that generates <code>WorkSet</code>s. */
    public static final SetFactory workSetFactory = workSetFactory();
    public static final <V> SetFactory<V> workSetFactory() {
	return new SerialSetFactory<V>() {
	    public WorkSet<V> makeSet(Collection<V> c) {
		return new WorkSet<V>(c);
	    }
	    public WorkSet<V> makeSet(int i) {
		return new WorkSet<V>(i);
	    }
	};
    }
    
    /** A <code>SetFactory</code> that generates
	<code>LinearSet</code>s backed by <code>ArrayList</code>s. */
    public static final SetFactory linearSetFactory = linearSetFactory();
    public static final <V> SetFactory<V> linearSetFactory() {
      return new SerialSetFactory<V>() {
	public LinearSet<V> makeSet(Collection<V> c) {
	    LinearSet<V> ls;
	    /*if (c instanceof Set<V>) {
		ls = new LinearSet<V>((Set<V>)c);
	    } else*/ { // XXX BUG IN JAVAC makes Set<V> and Collection<V> incompat
		ls = new LinearSet<V>(c.size());
		ls.addAll(c);
	    }
	    return ls;
	}
	public LinearSet<V> makeSet(int i) {
	    return new LinearSet<V>(i);
	}
      };
    }

    /** A <code>SetFactory</code> that generates <code>TreeSet</code>s. */
    public static final SetFactory treeSetFactory = treeSetFactory();
    public static final <V> SetFactory<V> treeSetFactory() {
	return new SerialSetFactory<V>() {
	    public TreeSet<V> makeSet(Collection<V> c) {
		return new TreeSet<V>(c);
	    }
	};
    }
    
    /** A <code>ListFactory</code> that generates <code>LinkedList</code>s. */
    public static final ListFactory linkedListFactory = linkedListFactory();
    public static final <V> ListFactory<V> linkedListFactory() {
	return new SerialListFactory<V>() {
	    public LinkedList<V> makeList(Collection<V> c) {
		return new LinkedList<V>(c);
	    }
	};
    }

    /** Returns a <code>ListFactory</code> that generates
	<code>ArrayList</code>s. */
    public static final ListFactory arrayListFactory = arrayListFactory();
    public static final <V> ListFactory<V> arrayListFactory() {
	return new SerialListFactory<V>() {
	    public ArrayList<V> makeList(Collection<V> c) {
		return new ArrayList<V>(c);
	    }
	    public ArrayList<V> makeList(int i) {
		return new ArrayList<V>(i);
	    }
	};
    }

    /** Returns a <code>SetFactory</code> that generates <code>MapSet</code>
     *  views of maps generated by the given <code>MapFactory</code>.  These
     *  can be passed in as arguments to a <code>GenericMultiMap</code>,
     *  for example, to make a multimap of maps. */
    public static <K,V> SetFactory<Map.Entry<K,V>> mapSetFactory(final MapFactory<K,V> mf) {
	return new SerialSetFactory<Map.Entry<K,V>>() {
		public Set<Map.Entry<K,V>> makeSet(Collection<Map.Entry<K,V>> c) {
		    final Map<K,V> m = mf.makeMap();
		    // we could call addAll on the result, but we'll be
		    // gentle on entrySet()s which might not allow 'add'.
		    for (Iterator<Map.Entry<K,V>> it=c.iterator(); it.hasNext(); ) {
			Map.Entry<K,V> me = it.next();
			m.put(me.getKey(), me.getValue());
		    }
		    Set<Map.Entry<K,V>> s = m.entrySet();
		    /* XXX BUG IN JAVAC
		    if (s instanceof MapSet<K,V> && ((MapSet)s).asMap()==m)
			return s; // optimize!
		    */
		    return new MapSetWrapper<K,V>(s) {
			    public Map<K,V> asMap() { return m; }
			};
		}
	    };
    }
    static abstract class MapSetWrapper<K,V> extends SetWrapper<Map.Entry<K,V>> implements MapSet<K,V> {
	MapSetWrapper(Set<Map.Entry<K,V>> set) { super(set); }
    }

    /** Returns a <code>SetFactory</code> that generates
     *  <code>MultiMapSet</code> views of <code>MultiMap</code>s
     *  generated by the given <code>MultiMapFactory</code>.  These can be
     *  passed in as arguments to a <code>GenericMultiMap</code>, for
     *  example, to make a multimap of multimaps. */
    public static <K,V> SetFactory<Map.Entry<K,V>> multiMapSetFactory(final MultiMapFactory<K,V> mf) {
	return new SerialSetFactory<Map.Entry<K,V>>() {
		public Set<Map.Entry<K,V>> makeSet(Collection<Map.Entry<K,V>> c) {
		    final MultiMap<K,V> m = mf.makeMultiMap();
		    // we could call addAll on the result, but we'll be
		    // gentle on entrySet()s which might not allow 'add'.
		    for (Iterator<Map.Entry<K,V>> it=c.iterator(); it.hasNext(); ) {
			Map.Entry<K,V> me = it.next();
			m.add(me.getKey(), me.getValue());
		    }
		    Set<Map.Entry<K,V>> s = m.entrySet();
		    /* XXX BUG IN JAVAC
		    if (s instanceof MultiMapSet &&
			((MultiMapSet)s).asMultiMap()==m)
			return s; // optimize!
		    */
		    return new MultiMapSetWrapper<K,V>(s) {
			    public MultiMap<K,V> asMap() { return asMultiMap(); }
			    public MultiMap<K,V> asMultiMap() { return m; }
			};
		}
	    };
    }
    static abstract class MultiMapSetWrapper<K,V> extends SetWrapper<Map.Entry<K,V>>
	implements MultiMapSet<K,V> {
	MultiMapSetWrapper(Set<Map.Entry<K,V>> set) { super(set); }
    }

    /** Returns a <code>CollectionFactory</code> that generates
	synchronized (thread-safe) <code>Collection</code>s.  
	The <code>Collection</code>s generated are backed by the 
	<code>Collection</code>s generated by <code>cf</code>. 
	@see Collections#synchronizedCollection
    */
    public static <V> CollectionFactory<V>
	synchronizedCollectionFactory(final CollectionFactory<V> cf) { 
	return new SerialCollectionFactory<V>() {
	    public Collection<V> makeCollection(Collection<V> c) {
		return Collections.synchronizedCollection
		    (cf.makeCollection(c));
	    }
	};
    }

    /** Returns a <code>SetFactory</code> that generates synchronized
	(thread-safe) <code>Set</code>s.  The <code>Set</code>s
	generated are backed by the <code>Set</code>s generated by
	<code>sf</code>. 
	@see Collections#synchronizedSet
    */
    public static <V> SetFactory<V>
	synchronizedSetFactory(final SetFactory<V> sf) {
	return new SerialSetFactory<V>() {
	    public Set<V> makeSet(Collection<V> c) {
		return Collections.synchronizedSet(sf.makeSet(c));
	    }
	};
    }

    /** Returns a <code>ListFactory</code> that generates synchronized
	(thread-safe) <code>List</code>s.   The <code>List</code>s
	generated are backed by the <code>List</code>s generated by
	<code>lf</code>. 
	@see Collections#synchronizedList
    */
    public static <V> ListFactory<V>
	synchronizedListFactory(final ListFactory<V> lf) {
	return new SerialListFactory<V>() {
	    public List<V> makeList(Collection<V> c) {
		return Collections.synchronizedList(lf.makeList(c));
	    }
	};
    }

    /** Returns a <code>MapFactory</code> that generates synchronized
	(thread-safe) <code>Map</code>s.  The <code>Map</code>s
	generated are backed by the <code>Map</code> generated by
	<code>mf</code>.
	@see Collections#synchronizedMap
    */
    public static <K,V> MapFactory<K,V>
	synchronizedMapFactory(final MapFactory<K,V> mf) {
	return new SerialMapFactory<K,V>() {
	    public Map<K,V> makeMap(Map<K,V> map) {
		return Collections.synchronizedMap(mf.makeMap(map));
	    }
	};
    }

    public static <V> CollectionFactory<V>
	noNullCollectionFactory(final CollectionFactory<V> cf) {
	return new SerialCollectionFactory<V>() {
	    public Collection<V> makeCollection(final Collection<V> c) {
		assert noNull(c);
		final Collection<V> back = cf.makeCollection(c);
		return new CollectionWrapper<V>(back) {
		    public boolean add(V o) {
			assert o != null;
			return super.add(o);
		    }
		    public boolean addAll(Collection<V> c2) {
			assert Factories.noNull(c2);
			return super.addAll(c2);
		    }
		};
	    }
	};
    }

    private static <V> boolean noNull(Collection<V> c) {
	Iterator<V> iter = c.iterator();
	while(iter.hasNext()) {
	    if(iter.next() == null) return false;
	}
	return true;
    }

    // private classes to add java.io.Serializable to *Factories.
    // if we could make anonymous types w/ multiple inheritance, we wouldn't
    // need these.
    private static abstract class SerialMapFactory<K,V>
	extends MapFactory<K,V> implements java.io.Serializable { }
    private static abstract class SerialSetFactory<V>
	extends SetFactory<V> implements java.io.Serializable { }
    private static abstract class SerialListFactory<V>
	extends ListFactory<V> implements java.io.Serializable { }
    private static abstract class SerialCollectionFactory<V>
	extends CollectionFactory<V> implements java.io.Serializable { }
}
