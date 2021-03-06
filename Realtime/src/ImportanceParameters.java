// ImportanceParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

/** Importance is an additional scheduling metric that may be used
 *  by some priority-based scheduling algorithms during overload
 *  conditions to differenciate execution order among threads of the
 *  same priority.
 *  <p>
 *  In some real-time systems an external physical process determines
 *  the period of many threads. If rate-monotonic priority assignment
 *  is used to assign priorities many of the threads in the system may
 *  have the same priority because their periods are the same. However,
 *  it is conceivable that some threads may be more important than
 *  others and in an overload situation importance can help the
 *  scheduler decide which threads to execute first. The base scheduling
 *  algorithm represented by <code>PriorityScheduler</code> is not
 *  required to use importance. However, the RTSJ strongly suggests to
 *  implementers that a fairly simple subclass of
 *  <code>PriorityScheduler</code> that uses importance can offer value
 *  to some real-time applications.
 */
public class ImportanceParameters extends PriorityParameters {
    
    private int importance;

    /** Create an instance of <code>ImportanceParameters</code>.
     *
     *  @param priority The priority assigned to an instance of
     *                  <code>Schedulable</code>. This value is used in
     *                  place of <code>java.lang.Thread.priority</code>.
     *  @param importance The importance value assigned to an instance
     *                    of <code>Schedulable</code>.
     */
    public ImportanceParameters(int priority, int importance) {
	super(priority);
	this.importance = importance;
    }

    /** Gets the importance value.
     *
     *  @return The value of importance for the associated instance of
     *          <code>Schedulable</code>.
     */
    public int getImportance() {
	return importance;
    }

    /** Sets the importance. */
    public void setImportance(int importance) {
	this.importance = importance;
    }
    
    /** Print the value of the importance value of the associated instance
     *  of <code>Schedulable</code>.
     */
    public String toString() {
	return "ImportanceParameters: Priority = " + getPriority()
	    + ", Importance = " + getImportance();
    }
}
