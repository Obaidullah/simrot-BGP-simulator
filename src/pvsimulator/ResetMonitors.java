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
 * This Class re-sets all monitors at the respective node for SPV protocol
 * @author ahmed
 */
public class ResetMonitors {
    
    /** Creates a new instance of ResetMonitors */
    public ResetMonitors() {
    }
    /**
     *  Re-sets all monitors
     */
    public static void reset(int numberOfNodes,double current,double duration)
    {
       ArrivalEvent.Counter = true; 
       for(int i=1;i<=numberOfNodes;i++)
       {
         Node node = Topology.getNodeByAddress(i);
         node.setMonitor(new MessagesMonitor(current,duration,0));
       }
    }
}
