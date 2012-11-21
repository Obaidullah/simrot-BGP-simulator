/*
 * ResetMonitors.java
 *
 * Created on September 19, 2007, 3:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

/**
 * This Class re-sets all monitors at the respective node for SPV+MRAI and SPV+MRAI+policies protocols
 * @author ahmed
 */
public class ResetMonitorsV2 {
    
    /** Creates a new instance of ResetMonitors */
    public ResetMonitorsV2() {
    }
    /**
     *  Re-sets all monitors
     */
    public static void reset(int numberOfNodes,double current,double duration)
    {
       ArrivalEventV2.Counter = true; 
       for(int i=1;i<=numberOfNodes;i++)
       {         
         NodeV2 node = Topology.getNodeByAddressV2(i);
         node.initializeTimers();
         node.initializeOutputQueue();
         node.setMonitor(new MessagesMonitor(current,duration,3600.0));
       }
    }
}
