/*
 * PrefixUpEvent.java
 *
 * Created on July 11, 2007, 1:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import gnu.trove.TIntArrayList;
import java.io.PrintStream;
/**
 *  A class representing prefix up events - SPV protocol only
 * @author ahmed
 */
public class PrefixUpEvent extends Unit{
    
    private Kernel kernel = null;
    private TIntArrayList prefixesUp = null;
    private int atNode=0;
    /** Creates a new instance of PrefixUpEvent */
    public PrefixUpEvent() {
    }
    /** Creates a new instance of PrefixUpEvent */
    public PrefixUpEvent(int atNode,TIntArrayList prefixesUp,Kernel kernel,double nextSchedule)
    {
       this.atNode = atNode;
       this.prefixesUp = prefixesUp;
       this.kernel = kernel;
       this.nextSchedule = nextSchedule;
    }
    

    public void act() {
        Message interrupt = new Message(Values.interruptMessage,Values.prefixesUp,atNode);
        interrupt.setAnnounced(prefixesUp);
        kernel.schedule(new ArrivalEvent(atNode,interrupt,kernel,GetTime.getNextSchedule(kernel)));
    }
}
