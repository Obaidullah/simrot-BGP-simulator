/*
 * FailureEvent.java
 *
 * Created on June 29, 2007, 11:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import gnu.trove.TIntArrayList;


/** This class represents failure events (SPV+MRAI,SPV+MRAI+Policies)
 *
 * @author ahmed
 */
public class FailureEventV2 extends Unit{
    final short Link_F = 0;
    final short Node_F = 1;
    private int first_node;
    private int second_node;
    private int failed_node;
    private short failure_type;
    private TIntArrayList adjacenes;
    private Kernel kernel;
    /** Creates a new instance of FailureEvent */
    public FailureEventV2() {
    }
    /** Constructor used in case of instiating failure event of type Link failure**/
    public FailureEventV2(int first_node,int second_node,Kernel kernel,double nextSchedule)
    {
        this.failure_type = Link_F;
        this.first_node = first_node;
        this.second_node = second_node;
        this.kernel = kernel;
        this.nextSchedule = nextSchedule;
    }
    /**Constructor used in case of instantiating failure event of type node failure **/
    public FailureEventV2(int failed_node,Kernel kernel,double nextSchedule)
    {   
        this.failure_type = Node_F;
        this.failed_node = failed_node;
        this.kernel=kernel;
        this.nextSchedule = nextSchedule;
    }
    public void act()
    {
      Message message_NF;
      Message message_LFF;
      Message message_LFS;
      //Link failure handling
      if(failure_type == Link_F)
      {
        message_LFF = new Message(Values.interruptMessage,Values.ReachLoss,second_node);
        kernel.schedule(new ArrivalEventV2(first_node,message_LFF,kernel,GetTime.getNextSchedule(kernel)));
        message_LFS = new Message(Values.interruptMessage,Values.ReachLoss,first_node);
        kernel.schedule(new ArrivalEventV2(second_node,message_LFS,kernel,GetTime.getNextSchedule(kernel)));
      }
      //Node failure handling
      else
      {
        if(failure_type == Node_F)
        {
           NodeV2 Failed_Node = Topology.getNodeByAddressV2(failed_node);
           adjacenes = (TIntArrayList)(Failed_Node.getAdjacenes().clone());
           Failed_Node.eraseState();
           message_NF = new Message(Values.interruptMessage,Values.ReachLoss,failed_node);

           for(int i=0;i<adjacenes.size();i++)
           {
             kernel.schedule(new ArrivalEventV2(adjacenes.get(i),message_NF,kernel,GetTime.getNextSchedule(kernel)));
           }
        }
        else
        {
          
        }    
      
      }   
    }
}
