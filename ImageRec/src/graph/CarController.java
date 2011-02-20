// CarController.java, created by benster 5/29/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

/**
 * A car controller is responsible for responding to alerts (provided
 * using {@link ImageData}s) and moving a car left, right, forward
 * or backward.
 *
 * The commands generated by this class are intended to be
 * directed in some way to a {@link Servo} node.
 *
 * @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
 */
public class CarController extends Node {

    /**
     * Specifies whether this object will actually
     * generate servo commands if given alerts. If
     * <code>true</code>, then commands will be generated.
     * If <code>false</code>, then commands will not be
     * generated.
     */
    private boolean enabled = true;

    /**
     * The timer running an independent thread that
     * interrupts car commands after a specified time.
     */
    private InterruptTimer timer;

    /**
     * Constructs a {@link CarController} object which
     * will generate Servo commands 
     */
    public CarController() {
	super(null);
	init();
    }
    
    /**
     * This method should be called by all constructors
     * to ensure proper initialization of object fields.
     */
    private void init() {
	timer = new InterruptTimer();
	//frame = new EnableFrame();
    }

    /**
     * Sets the amount of time that commands are allowed to
     * run before being interrupted.<br><br>
     * 
     * If the latency between commands is smaller than
     * <code>t</code>, then commands will not be interrupted.
     *
     * @param t The new time before commands are interrupted.
     */
    public void setInterruptTime(long t) {
	timer.setInterruptTime(t);
    }

    /**
     * Sets whether this {@link CarController} object will generate
     * {@link Servo} commands. If <code>b == true</code>, then
     * this object will generate commands. If <code>b == false</code>,
     * then this object will generate "stop moving/straighten wheels" commands,
     * and then stop generating {@link Servo} commands.
     *
     */
    public void setEnabled(boolean b) {
	this.enabled = b;
	if (!b)
	    this.timer.stop();
    }

    /**
     * Receives an {@link ImageData} with alert data,
     * and generates a {@link Servo} command.
     */
    public void process(ImageData id) {
	System.out.println("CarController.process()");
	//id should have c1, c2, c3 set
	//either can come after RangeFind or
	//be the result of an alert call
	float x = id.c1;//pixel x location of target
        float y = id.c2;//pixel x location of target
	float z = id.c3;//distance to target in inches
	int width = id.origWidth;
	int height = id.origHeight;
	double idealX = width/2.;
	double idealY = height/2.;
        double epsilonX = width/15.;
	double epsilonY = height/20.;
	
	ImageData moveCommand = null;
	ImageData dirCommand = null;
	if (enabled) {
	    //if too far, then always move foward
	    if (y > idealY+epsilonY) {
		System.out.println(" --Tank is too close.");
		moveCommand =
		    ImageDataManip.create(Command.SERVO_BACKWARD_CONTINUE, 0);
		//if too much to left, turn wheels left 
		if (x > idealX+epsilonX) {
		    System.out.println(" --Tank is too far right");
		    System.out.println("CarController: moving backward and left");
		    dirCommand =
			ImageDataManip.create(Command.SERVO_LEFT_CONTINUE, 0);
		}
		//else turn wheels right
		else if (x < idealX-epsilonX){
		    System.out.println(" -- Tank is too far left.");
		    System.out.println("CarController: moving backward and right");
		    dirCommand =
			ImageDataManip.create(Command.SERVO_RIGHT_CONTINUE, 0);
		}
		else {
		    System.out.println(" --Tank is not too far right or left");
		    System.out.println("CarController: moving backward");
		    dirCommand =
			ImageDataManip.create(Command.SERVO_STOP_TURN, 0);
		}
	    }
	    //else if tank is too close move backwards
	    else if (y < idealY-epsilonY){
		System.out.println(" --Tank is too far.");
		moveCommand =
		    ImageDataManip.create(Command.SERVO_FORWARD_CONTINUE, 0);
		//if too much to left, turn wheels right 
		if (x > idealX+epsilonX) {
		    System.out.println(" --Tank is too far right");
		    System.out.println("CarController: moving forward and right");
		    dirCommand =
			ImageDataManip.create(Command.SERVO_RIGHT_CONTINUE, 0);
		}
		//else turn wheels left
		else if (x < idealX-epsilonX) {
		    System.out.println(" -- Tank is too far left.");
		    System.out.println("CarController: moving forward and left");
		    dirCommand =
			ImageDataManip.create(Command.SERVO_LEFT_CONTINUE, 0);
		}
		else {
		    System.out.println(" --Tank is not too far right or left");
		    System.out.println("CarController: moving forwards");
		    dirCommand =
			ImageDataManip.create(Command.SERVO_STOP_TURN, 0);
		}
	    }
	    //else [tank is at just the right distance]
	    else {
		System.out.println(" --Tank is just the right distance.");
		//if tank is too far left, turn wheels right,
		//and move backwards
		if (x > idealX+epsilonX) {
		    System.out.println(" --Tank is too far right.");
		    System.out.println("CarController: moving backward and left");
		    moveCommand =
			ImageDataManip.create(Command.SERVO_BACKWARD_CONTINUE, 0);
		    dirCommand =
			ImageDataManip.create(Command.SERVO_LEFT_CONTINUE, 0);		
		}
		//else if tank is too far right, turn wheels left
		//and move backwards
		else if (x < idealX-epsilonX) {
		    System.out.println(" --Tank is too far left.");
		    System.out.println("CarController: moving backward and right");
		    moveCommand =
			ImageDataManip.create(Command.SERVO_BACKWARD_CONTINUE, 0);
		    dirCommand =
			ImageDataManip.create(Command.SERVO_RIGHT_CONTINUE, 0);		
		}
		//else [tank is centered, stop wheels]
		else {
		    System.out.println(" --Tank is perfect.");
		    System.out.println("CarController: stopping and centering");
		    moveCommand =
			ImageDataManip.create(Command.SERVO_STOP_MOVING, 0);
		    dirCommand =
			ImageDataManip.create(Command.SERVO_STOP_TURN, 0);
		}
		
	    }
	    
	    timer.reset();
	}
	//[not enabled]
	else {
	    System.out.println("CarController: not enabled, so stopping and centering");
	    moveCommand =
		ImageDataManip.create(Command.SERVO_STOP_MOVING, 0);
	    dirCommand =
		ImageDataManip.create(Command.SERVO_STOP_TURN, 0);
	}

	//System.out.println("["+System.currentTimeMillis()+"]CarController: about to execute commands");
	if (moveCommand == null) {
	    System.out.println("moveCommand is null");
	}
	if (dirCommand == null) {
	    System.out.println("dirCommand is null");
	}
	super.process(dirCommand);
	super.process(moveCommand);
	//System.out.println("["+System.currentTimeMillis()+"]CarController: done executing commands");

    }

    /**
     * Runs a timer thread in the background that gets reset by the
     * main thread each time a new alert is received. If an alert
     * is not received after a specified amount of time,
     * then a "stop moving" {@link Servo} command is sent.
     */
    private class InterruptTimer implements Runnable {
	
	private long lastCommandTime;
	private boolean started = false;

	//delay could be as long as "timeUntilInterrupt+sleepTime" milliseconds
	private static final long sleepTime = 150;
	private long timeUntilInterrupt = 750;

	/**
	 * Constructs a new {@link CarController.InterruptTimer}, which
	 * creates and starts an independent thread.
	 */
	InterruptTimer() {
	    Thread t = new Thread(this);
	    t.start();
	}

	/**
	 * Sets the amount of time that commands are allowed to
	 * run before being interrupted.<br><br>
	 * 
	 * If the latency between commands is smaller than
	 * <code>t</code>, then commands will not be interrupted.
	 *
	 * @param t The new time before commands are interrupted.
	 */
	public void setInterruptTime(long t) {
	    timeUntilInterrupt = t;
	}

	/**
	 * Resets the timer that keeps track of when to
	 * interrupt back to zero.
	 */
	public void reset() {
	    //System.out.println("["+System.currentTimeMillis()+"]InterruptTimer: resetCalled");
	    lastCommandTime = System.currentTimeMillis();
	    if (!started)
		started = true;
	}

	/**
	 * Stops the {@link CarController.InterruptTimer} from interrupting
	 * servo commands. This command does not reset or even stop
	 * the timer from counting.
	 */
	public void stop() {
	    started = false;
	}

	/**
	 * This method performs the infinite garbage collecting
	 * loop.
	 */
	public void run() {
	    boolean keepGoing = true;
	    while (keepGoing) {
		try {
		    Thread.currentThread().sleep(sleepTime);
		}
		catch (InterruptedException e) {
		}
		if (started &&
		    (System.currentTimeMillis()-lastCommandTime > timeUntilInterrupt)) {
		    System.out.println("["+System.currentTimeMillis()+"]InterruptTimer: interrupting");
		    ImageData servoCommand;
		    servoCommand =
			ImageDataManip.create(Command.SERVO_STOP_MOVING, 0);
		    CarController.super.process(servoCommand);
		    //servoCommand =
		    //ImageDataManip.create(Command.SERVO_STOP_TURN, 0);
		    //CarController.super.process(servoCommand);
		    stop();
		}
	    }
	}
    }
}
