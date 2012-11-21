/*
 * Node.java
 *
 * Created on June 29, 2007, 11:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;


import gnu.trove.*;
import java.util.ArrayList;
/**This class represents all the functionality done by the network node
 * running SPV Protocol
 * @author ahmed
 */
public class Node {
    
    /** Creates a new instance of Node */
    private ArrayList<Message> messageQueue = new ArrayList<Message>();
    private boolean idle=true;
    private RIB rib = new RIB();
    private int AS;
    private TIntArrayList adjacenes = new TIntArrayList();
    private TIntArrayList attachedNetworks=new TIntArrayList();
    private boolean initialized = false;
    private MessagesMonitor monitor;
    public Node() {
    }
    /** instantiate a new node having the specified attached networks
     * @param attachedNetworks list of the the locally attached networks
     * @param AS This node AS-number
     */
    public Node(TIntArrayList attachedNetworks,int AS) {
        this.attachedNetworks = attachedNetworks;
        this.AS = AS;
    }
    /** Returns the adjacencies of this node
     *  @return a list of adjacent nodes
     */
    public TIntArrayList getAdjacenes() {
        return adjacenes;
    }
    /** Bootstrap this node
     *  @param adjacencies this node adajacencies
     *  @param kernel a reference to the simulator kernel
     *
     */
    public void initialize(TIntArrayList adjacencies, Kernel kernel) {
        
        if(!adjacencies.isEmpty()) {
            if(!attachedNetworks.isEmpty()) {
                TLinkedList result = new TLinkedList();
                //TIntArrayList path = new TIntArrayList();
                //path.add(AS);
                GRIBTNode path = GRIB.levelOne[AS-1];
                this.adjacenes = adjacencies;
                for(int i=0;i<attachedNetworks.size();i++) {
                    int announced = attachedNetworks.get(i);
                    result.add(new Message(Values.updateMessage,AS,1,path,announced));
                }
                for(int j=0;j<adjacencies.size();j++) {
                    for(int k=0;k<result.size();k++)
                        kernel.schedule(new ArrivalEvent(adjacencies.get(j),(Message)result.get(k),kernel,GetTime.getNextScheduleArrival(kernel,AS,adjacencies.get(j))));
                }
            } else {
                this.adjacenes = adjacencies;
                
                for(int j=0;j<adjacenes.size();j++) {
                    Message sessionEstablishment = new Message(Values.interruptMessage,Values.sessionEstablishment,AS);
                    
                    
                    kernel.schedule(new ArrivalEvent(adjacencies.get(j),sessionEstablishment,kernel,GetTime.getNextScheduleArrival(kernel,AS,adjacencies.get(j))));
                }
            }
        }
        monitor = new MessagesMonitor(0,1,0);
        this.initialized = true;
    }
    /**
     * Returns a reference to the local messages monitor
     */
    public MessagesMonitor getMonitor() {
        return monitor;
    }
    /**
     * Set Monitor
     */
    public void setMonitor(MessagesMonitor monitor) {
        this.monitor = monitor;
    }
    
    /** Erase this node state(RIB and adjacency list) upon failure
     *
     */
    
    public void eraseState() {
        //rib.getRIB().clear();
        rib.rib.listOfRIBS.clear();
        rib.rib.RIBsIndex.clear();
        adjacenes.clear();
        this.initialized = false;
    }
    /**
     * Returns a reference to the incoming messages queue
     */
    public ArrayList<Message> getMessageQueue() {
        return messageQueue;
    }
    /** Setter for the server status
     *  @param idle the server status
     */
    public void setIdle(boolean idle) {
        this.idle = idle;
    }
    /** A getter for the server status
     *  @return server status
     */
    public boolean getIdle() {
        return idle;
    }
    /**
     * Returns a pointer to this node RIB
     */
    public RIB getRib() {
        return rib;
    }
    /**
     *  Returns the node AS-Number
     */
    public int getAS() {
        return AS;
    }
    /**
     *  Returns the attached networks
     */
    public TIntArrayList getAttachedNetworks() {
        return attachedNetworks;
    }
    /**
     * Returns if this node is initialized or not
     */
    public boolean isInitialized() {
        return initialized;
    }
    
}
