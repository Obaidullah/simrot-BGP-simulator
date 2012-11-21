/*
 * Kernel.java
 *
 * Created on June 24, 2007, 11:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;
/**
 * This class is the heart of any simulation. It holds the simulator
 * clock, and calls the act methods of all stations in the order decided by
 * how they are scheduled. The most important methods are schedule -
 * scheduling a given object at a given time, reschedule - schedule or
 * reschedule a given object at a given time and simulate - starting the
 * simulator loop calling the act methods of one node at a time until a
 * given clock count is reached.
 * This class does also give methods for uniform and poisson distribution.<BR>
 * Written originally by Olav Lysne<BR>
 * Modified by Stein Gjessing Jan. - April 2001<BR>
 * Copyright Olav Lysne and Stein Gjessing 2001.<BR>
 * @author Olav Lysne og Stein Gjessing
 */
public class Kernel {
    
    /* A contains a binary tree with A[1] as the root. A[0] is also a "root"
       except that it has only one child, A[1]. Data is stored in A[1]..A[length].
       A[i].nextSchedule is the wakeup simulated time for each object.
       A[i].nextSchedule is 0, hence all rearranging of the tree will stop at A[0].
     
       The tree invariant is that mother.nextSchedule is less than or equal to
       child.nextSchedule (for both child).
     */
    Unit[] A;
    /** The event file which contains the events to be executed after the initial convergence*/
    public static String eventFile="";
    /**The sim length after the initial convergence*/
    public static double simLength=0;
    /**The number of Nodes in the network*/
    public static int numberofNodes = 0;
    /**The protocol version*/
    public static boolean withTimer = false;
    public static boolean withEvents = false;
    int length = 0;
    double time = 0, seed;
    int units;
    boolean errorfound;
    private GetTime getTime;
    public GetTime getGetTime() {
        return getTime;
    }
    public void setGetTime(GetTime getTime) {
        this.getTime = getTime;
    }
    
    
    
    
    // RandomTools random;
    
    /**
     * Returns a kernel with an eventqueue of length u, and a seed s for the
     * randomgenerator
     * @param s, A seed used in the kernels instance of RandomTools
     * @param u,
     */
    public Kernel(long s, int u) {
        seed = s;
        units = u;
        A = new Unit[units];
        A[0] = new Unit();  // top stop element
    }
    
    /**
     * Returns a kernel with an eventqueue of length u, and an
     * arbitrary seed s for the randomgenerator
     * @param u, The length of the eventqueue
     */
    public Kernel(int u) {
        units = u;
        A = new Unit[units];
        A[0] = new Unit();  // top stop element
    }
    /**
     * This variable stores the time at which simulation should stop
     */
    double stopAt;
    
    /**
     * Starts simulation, and stops when the time reaches n
     */
    public void simulate(double dur) {
        Unit current;
        stopAt = dur;
        //This deals with events after convergence phase
        if(withEvents) {
            if(withTimer)
                ResetMonitorsV2.reset(numberofNodes,0.0,1);
            else
                ResetMonitors.reset(numberofNodes,time,1);
            EventParser.parseEventFile(eventFile,this,0.0,withTimer);
            current = A[1];
            time = current.nextSchedule;
            current.scheduled = false;
            time = current.nextSchedule;
            while (time <= stopAt && length!=0) {
                
                current.act();
                // If current is  rescheduled it has set itself to scheduled
                // and come in the correct position in the tree.
                // In that case A[1] is scheduled and the next to be served
                // If not, current == A[1] and is still at the top
                // (because it is impossible to schedule process at an
                // earlier time than present)
                // In that case it should be removed (popped)
                if (!A[1].scheduled) pop();
                
                current = A[1];
                current.scheduled = false;
                time = current.nextSchedule;
            }
        }
    }
    
    
    /**
     * Resets the kernel object to make it ready for a new (usually continued)
     * simulation
     */
    public void clear() {
        length = 0;
        time = 0;
        
        for (int i = 0; i < A.length; ++i) A[i] = null;
        
        A[0] = new Unit();
    }
    
    
    /**
     * Reschedules process proc to time tm Schedule unit proc. The time tm is
     * scheduled for is supposed to be stored in the variable n.nextSchedule
     * within n.
     */
    public void reschedule(Unit proc, double tm) {
        int ind;
        if (time >= tm) error(" Reschedule to an earlier time");
        if (proc == null) error("Reschedule null process, time " + time);
        for ( ind=1 ; ind <= length && A[ind] != proc; ind ++) ;
        
        if (ind > length) {
            //Here the object intended for re-scheduling is not initially a member of the event queue
            proc.nextSchedule = tm;
            proc.scheduled = true;
            schedule(proc);
        } else alterschedule(tm, ind);
    }
    
    /**
     * Must be called by user pressese wit extrem care! Only called if n is
     * unscheduled and not currently executing ( i.e. not present in A)
     */
    public void schedule(Unit n) {
        if (length == units) {
            error("Prioritetskø overflow");
        } else {
            if (n.nextSchedule < time)
                error("tiden går bakover");
            else
                if (n == A[1])
                    error("scheduling presently running object");
                else {
                //Trace.debug(1," Kernel Sched. Length: " + length);
                //Trace.debug(1,"         At time: " + n.nextSchedule);
                
                length = length+1;
                
                A[length] = n;
                siftup(length);
                }
        }
    }
    
    /**
     * Alters the time at which node n is scheduled to p
     */
    public void alterschedule(double p, int n) {
        
        if ((n==1) & (p == time)) {
            System.out.println("Warning: reschedule current at same time");
            A[1].scheduled = true;
        }
        
        if (! (n>= 1 && n <= length))
            error("Change tim on noexisting node, time " + n + " "+ time);
        else if (A[n].nextSchedule != p) {
            A[n].nextSchedule = p;
            A[n].scheduled = true;
            if  (A[n/2].nextSchedule > p)
                siftup(n);
            else
                siftdown(n);
        }
    }
    
    /**
     * Arranges a chaotic eventqueue to initiate the invariant of a
     * priorityqueue. This method is rarely necessary
     */
    public void arrange(int n) {
        if (n <= length) {
            arrange(n*2);
            arrange(n*2+1);
            siftdown(n);
        }
    }
    
    /**
     * Internal method for arranging the eventqueue. Down means towards the root
     */
    public void siftdown(int n) {
        int parent, child;
        boolean done = false;
        double targetTime = A[n].nextSchedule;
        Unit temp = A[n];
        // A[0] = A[n];
        child = n*2;
        parent = n;
        
        while (child <= length && ! done) {
            //   System.out.println("            SiftDown");
            if (child < length &&   // means also childs brother exists
                    A[child].nextSchedule > A[child+1].nextSchedule) {
                if (targetTime >= A[child+1].nextSchedule) {
                    A[parent] = A[child+1];
                    parent = child +1;
                } else
                    done = true;
            } else {
                if (targetTime >= A[child].nextSchedule) {
                    A[parent] = A[child];
                    parent = child;
                } else
                    done = true;
            }
            
            child = parent * 2;
            
        }
        
        A[parent] = temp;
    }
    
    /**
     * Internal method for arranging the eventqueue. Up means towards the
     * leafes
     */
    public void siftup(int n) {
        int parent, child;
        Unit temp = A[n];
        double targetTime = A[n].nextSchedule;
        //	 A[0] = A[n];
        child = n;
        parent = child/2;
        while (A[parent].nextSchedule > targetTime) {
            //   System.out.println("              SiftUp");
            A[child] = A[parent];
            child = parent;
            parent = child /2;
        }
        
        A[child] = temp;
    }
    
    
    /**
     * Internal method for arranging the eventqueue
     */
    public void pop() {
        Unit temp = null;
        if (length != 0) {
            A[1] = A[length];
            length = length - 1;
            siftdown(1);
        } else
            error("Pop on an empty priority queue");
    }
    /**
     * Return the current simulation time
     */
    public double getTime() {
        return time;
    }
    
    /**
     * This method prints out the string t on the screen, and terminates
     * the entire simulation program. It is useful for reporting errors while
     * programming a new simulation experiment
     */
    public void error(String t) {
        System.out.println(t);
        errorfound = true;
        throw new Error();
    }
    
    
}
