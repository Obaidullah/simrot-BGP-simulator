/*
 * PrefixDownEvent.java
 *
 * Created on July 5, 2007, 2:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import gnu.trove.TIntArrayList;
import java.io.PrintStream;


/**
 *  A class representing prefix down events - SPV protocol only
 * @author ahmed
 */
public class PrefixDownEvent extends Unit{
    
    private Kernel kernel = null;
    private TIntArrayList prefixesDown = null;
    private int atNode=0;
    /** Creates a new instance of PrefixDownEvent */
    public PrefixDownEvent() {
    }
    /** Creates a new instance of PrefixDownEvent */
    public PrefixDownEvent(int atNode,TIntArrayList prefixesDown,Kernel kernel,double nextSchedule)
    {
       this.atNode = atNode;
       this.prefixesDown = prefixesDown;
       this.kernel = kernel;
       this.nextSchedule = nextSchedule;
    }
    

    public void act() {
        Message interrupt = new Message(Values.interruptMessage,Values.prefixesDown,atNode);
        interrupt.setWithdrawans(prefixesDown);
        kernel.schedule(new ArrivalEvent(atNode,interrupt,kernel,GetTime.getNextSchedule(kernel)));
    }
}
