/*
 * RestorationEventV2.java
 *
 * Created on July 17, 2007, 2:11 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;
import gnu.trove.TIntArrayList;
import java.util.*;
/**
 * This Class represents restoration events for SPV+MRAI and SPV+MRAI+policies Protocols
 *
 * @author ahmed
 */
public class RestorationEventV2 extends Unit{
    
    /** Creates a new instance of RestorationEventV2 */
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
    public RestorationEventV2() {
    }
    /** Creates a new instance of RestorationEvent for link restoration*/
    public RestorationEventV2(int first_node,int second_node,Kernel kernel,double nextSchedule) {
        this.restoration_type = Link_R;
        this.first_node = first_node;
        this.second_node = second_node;
        this.kernel=kernel;
        this.nextSchedule = nextSchedule;
    }
    /** Creates a new instance of RestorationEvent for node restoration*/
    public RestorationEventV2(int restored_node,Kernel kernel,double nextSchedule) {
        this.restoration_type = Node_R;
        this.restored_node = restored_node;
        this.kernel = kernel;
        this.nextSchedule = nextSchedule;
    }
    
    public void act() {
        Message message;
        //Link restoration handling
        if(restoration_type==Link_R) {
            message = new Message(Values.interruptMessage,Values.ReachGain,second_node);
            message = new Message(Values.interruptMessage,Values.ReachGain,first_node);
            kernel.schedule(new ArrivalEventV2(second_node,message,kernel,GetTime.getNextScheduleArrival(kernel,first_node,second_node)));
        }
        //Node restoration handling
        else {
            if(restoration_type == Node_R) {
                NodeV2 Restored_Node = Topology.getNodeByAddressV2(restored_node);
                adjacenes = Topology.getNodeAdjV2(restored_node);
                Restored_Node.initialize(adjacenes,kernel);
            }
        }
    }
    
}
