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
 * A class representing prefix up events - SPV+MRAI and SPV+MRAI+Policies protocols only
 * @author ahmed
 */
public class PrefixUpEventV2 extends Unit{
    
    private Kernel kernel = null;
    private TIntArrayList prefixesUp = null;
    private int atNode=0;
    /** Creates a new instance of PrefixUpEvent */
    public PrefixUpEventV2() {
    }
    /** Creates a new instance of PrefixUpEvent */
    public PrefixUpEventV2(int atNode,TIntArrayList prefixesUp,Kernel kernel,double nextSchedule)
    {
       
       this.atNode = atNode;
       this.prefixesUp = prefixesUp;
       this.kernel = kernel;
       this.nextSchedule = nextSchedule;
    }
    

    public void act() {
        MessagesMonitor.UP = true;
        MessagesMonitor.DOWN = false;
        Message interrupt = new Message(Values.interruptMessage,Values.prefixesUp,atNode);
        interrupt.setAnnounced(prefixesUp);
        kernel.schedule(new ArrivalEventV2(atNode,interrupt,kernel,GetTime.getNextSchedule(kernel)));
    }
}
