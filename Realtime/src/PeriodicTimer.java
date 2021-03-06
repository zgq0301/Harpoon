// PeriodicTimer.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** An <code>AsyncEvent</code> whose fire method is executed periodically
 *  according to the given parameters. If a clock is given, calculation
 *  of the period uses the increments of the clock. If an interval is
 *  given or set the system guarantees that the fire method will execute
 *  <code>interval</code> time units after the last execution or its
 *  given start time as appropriate. If one of the
 *  <code>HighResolutionTime</code> argument types is
 *  <code>RationalTime</code> then the system guarantees that the fire
 *  method will be executed exactly <code>frequence</code> times every
 *  unit time by adjusting the interval between executions of
 *  <code>fire()</code>. This is similar to a thread with
 *  <code>PeriodicParameters</code> except that it is lighter weight.
 *  If a <code>PeriodicTimer</code> is disabled, it still counts, and
 *  if enabled at some later time, it will fire at its next scheduled
 *  fire time.
 */
public class PeriodicTimer extends Timer {

    private RelativeTime interval;

    /** Create an instance of <code>AsyncEvent</code> that executes its
     *  fire method periodically.
     *
     *  @param start The time when the first interval begins.
     *  @param interval The time between successive executions of the fire method.
     *  @param handler The instance of <code>AsyncEventHandler</code> that will be
     *                 scheduled each time the fire method is executed.
     */
    public PeriodicTimer(HighResolutionTime start,
			 RelativeTime interval,
			 AsyncEventHandler handler) {
	super(start, Clock.getRealtimeClock(), handler);
	this.interval = interval;
    }

    /** Create an instance of <code>AsyncEvent</code> that executes its
     *  fire method periodically.
     *
     *  @param start The time when the first interval begins.
     *  @param interval The time between successive executions of the fire method.
     *  @param clock The clock whose increments are used to calculate the interval.
     *  @param handler The instance of <code>AsyncEventhandler</code> that will be
     *                 scheduled each time the fire method is executed.
     */
    public PeriodicTimer(HighResolutionTime start,
			 RelativeTime interval, Clock clock,
			 AsyncEventHandler handler) {
	super(start, clock, handler);
	this.interval = interval;
    }

    /** Create a <code>ReleaseParameters</code> object with the next
     *  fire time as the start time and the interval of <code>this</code>
     *  as the period.
     *
     *  @return An instance of <code>ReleaseParameters</code> object.
     */
    public ReleaseParameters createReleaseParameters() {
	return new PeriodicParameters(getFireTime(), interval, null, null,
				      handler, null);
    }

    /** Causes the instance of the superclass <code>AsyncEvent</code>
     *  to occur now.
     */
    public void fire() {
	handler.run();
    }

    /** Gets the absolute time <code>this</code> will next fire
     *
     *  @return The next fire time as an absolute time.
     */
    public AbsoluteTime getFireTime() {
	return super.getFireTime();
    }

    /** Gets the interval of <code>this</code>.
     *
     *  @return The value in <code>interval</code>.
     */
    public RelativeTime getInterval() {
	return interval;
    }

    /** Sets the <code>interval</code> value of <code>this</code>.
     *
     *  @param interval The value to which <code>interval</code> will be set.
     */
    public void setInterval(RelativeTime interval) {
	this.interval = interval;
    }
}
