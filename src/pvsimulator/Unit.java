/*
 * Unit.java
 *
 * Created on June 24, 2007, 11:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

/**
 * This is the basic abstract class for all the interacting
 * units in that can be programmed.  The most interesting 
 * part of it is the virtual public void "act" that is called
 * by the kernel at the times when the node is scheduled.
 *
 * The second interesting thing is the pocedure "report" into which
 * the output of the final reports are to be put.
 * This is currently not in use (I believe)
 */
public class Unit {
    public double nextSchedule;
    public boolean scheduled;
    
    /**
     * The superclass contructor. The subclasses constructors should do more
     */
    public Unit() {
	nextSchedule = 0;
	scheduled = false;
    }
    
    /** 
     * This method is called by the kernel whenever the time unit at
     * which the object is scheduled is reached
     */
    public void act(){};
    
    /**
     * This is a virtual method that might be instantiated in a
     * subclass. The instance should in case write state information to
     * the screen
     */
    public void report(){};
    
    /**
     * This is a virtual method that is to be instantiated by a method
     * giving data to a neighbor object. Typically - in a link object
     * this method should return the next flit, or the next packet.
     */
    public int give() {
	return 0;
    }
    
    /**
     * This is a virtual method that is to be instantiated by a method
     * receiving data from a neighbor object. Typically - if this node is
     * a link the method should put a flit or packet into the output
     * buffer of the link.
     */
    public boolean receive(int d) {
	return false;
    }
    
    /**
     * This method reschedules this object in the simulator kernel
     * "simulator" at time slot "time". The effect should be that
     * "simulator" shoud call the act method of this object when the
     * kernel clock has reached "time".
     */
    public void reschedule(Kernel simulator,double time) {
	simulator.reschedule(this,time);
    }
    
    /**
     * Should be called with extreme caution and only when a process
     * is sure it is not already scheduled.
     */
    public void schedule(Kernel simulator,double time) {
	if (! scheduled) {
	    nextSchedule = time;
	    simulator.schedule(this);
	    scheduled = true;
	}
	else
	    //Const.error("Wrong scheduling in Unit");
            System.out.println("Wrong scheduling in Unit"); 
    }
    
    /** Deshedules the object */
    public void deSchedule() {
	scheduled = false;
    }
}

