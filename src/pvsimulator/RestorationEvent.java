/*
 * RestorationEvent.java
 *
 * Created on June 29, 2007, 1:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import gnu.trove.TIntArrayList;
import java.util.*;
/**
 * This Class represents restoration events for SPV Protocol
 *
 * @author ahmed
 */
public class RestorationEvent extends Unit {
    
    final short Link_R = 0;
    final short Node_R = 1;
    final short Node_N = 2;
    private int first_node;
    private int second_node;
    private int restored_node;
    private short restoration_type;
    private TIntArrayList adjacenes;
    private Kernel kernel;
    Random generator=new Random(655357654);
    /** Creates a new instance of RestorationEvent */
    public RestorationEvent() {
    }
    /** Creates a new instance of RestorationEvent for link restoration*/
    public RestorationEvent(int first_node,int second_node,Kernel kernel,double nextSchedule) {
        this.restoration_type = Link_R;
        this.first_node = first_node;
        this.second_node = second_node;
        this.kernel=kernel;
        this.nextSchedule = nextSchedule;
    }
    /** Creates a new instance of RestorationEvent for node restoration*/
    public RestorationEvent(int restored_node,Kernel kernel,double nextSchedule) {
        this.restoration_type = Node_R;
        this.restored_node = restored_node;
        this.kernel = kernel;
        this.nextSchedule = nextSchedule;
    }
    
    public void act() {
        Message message;
        //Link restoration handling
        if(restoration_type==Link_R) {
            //Randomly choose the session initiator
            int session_initator=generator.nextInt(2);
            if(session_initator==0)
            {
               message = new Message(Values.interruptMessage,Values.ReachGain,second_node); 
               kernel.schedule(new ArrivalEvent(first_node,message,kernel,GetTime.getNextSchedule(kernel)));
            }
            else
            {
              message = new Message(Values.interruptMessage,Values.ReachGain,first_node); 
              kernel.schedule(new ArrivalEvent(second_node,message,kernel,GetTime.getNextSchedule(kernel))); 
            }    
        }
        //Node restoration handling
        else
        {
          if(restoration_type == Node_R)
        {
           Node Restored_Node = Topology.getNodeByAddress(restored_node);
           adjacenes = Topology.getNodeAdj(restored_node);
           Restored_Node.initialize(adjacenes,kernel);
        }
        }    
    }
}
