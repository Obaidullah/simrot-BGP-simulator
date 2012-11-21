/*
 * NodeV2.java
 *
 * Created on July 17, 2007, 12:01 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;
import gnu.trove.*;
import java.util.ArrayList;
import java.util.Random;
/**This class represents all the functionality done by the network node
 * running SPV+MRAI Protocol or SPV+MRAI+Policies
 * @author ahmed
 */
public class NodeV2 {
    public static int magicNumber=0;
    public static double MRAI = 30.0;
    private ArrayList<Message> messageQueue = new ArrayList<Message>();
    private boolean idle=true;
    private RIB rib = new RIB();
    private int AS;
    private TIntArrayList adjacenes = new TIntArrayList();
    private TIntArrayList attachedNetworks;
    private boolean initialized = false;
    private TIntIntHashMap timers = new TIntIntHashMap();
    private TIntIntHashMap timersInitializeationStatus = new TIntIntHashMap();
    private TIntObjectHashMap outputQueue = new TIntObjectHashMap();
    private Kernel kernel;
    private MessagesMonitor monitor;
    private TIntArrayList restAttachements = new TIntArrayList();
    private Random random;
    private Random randomTimer;
    public static int ProtocolVersion = 1;
    /** Creates a new instance of NodeV2 */
    public NodeV2() {
    }
    /** instantiate a new node having the specified attached networks
     * @param attachedNetworks list of the the locally attached networks
     * @param AS This node AS-number
     */
    public NodeV2(TIntArrayList attachedNetworks,TIntArrayList restAttachements,int AS) {
        this.attachedNetworks = attachedNetworks;
        this.restAttachements = restAttachements;
        this.AS = AS;
        random = new Random(AS*12+1979*magicNumber);
        randomTimer = new Random(AS*30+1006*magicNumber);
        if(Topology.withPolicies)
            rib = new RIBV2();
    }
    
    public void removeNeighbor(int neighbor) {
        //System.out.println("Remove Called at "+AS+" For "+neighbor);
        try {
            adjacenes.remove(adjacenes.indexOf(neighbor));
            // if(adjacenes.contains(neighbor))
            //     System.out.println(neighbor);
            outputQueue.remove(neighbor);
            timers.remove(neighbor);
        }catch(Exception e) {
            e.printStackTrace();
            System.out.println(neighbor+"\t"+AS+"\t"+kernel.getTime());
            System.exit(0);
        }
    }
    public void addNeighbor(int neighbor) {
        adjacenes.add(neighbor);
        outputQueue.put(neighbor,new ArrayList<Message>());
        timers.put(neighbor,0);
    }
    /**
     * Initialize neighbors output queues
     */
    public void initializeOutputQueue() {
        TIntArrayList staticAdj = Topology.getStaticAdj(AS);
        for(int i=0;i<staticAdj.size();i++)
            outputQueue.put(staticAdj.get(i),new ArrayList<Message>());
    }
    /** return back the adjacencies of this node
     *  @return a list of adjacent nodes
     */
    public TIntArrayList getAdjacenes() {
        return adjacenes;
    }
    
    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }
    
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
    
    public TIntArrayList getRestAttachements() {
        return restAttachements;
    }
    public void combineAttachements() {
        for(int i=0;i<restAttachements.size();i++) {
            attachedNetworks.add(restAttachements.get(i));
        }
        restAttachements.clear();
    }
    public void setRestAttachements(TIntArrayList restAttachements) {
        this.restAttachements = restAttachements;
    }
    
    /**
     * Turn on the MRAI timer associated with the resoecive neighbor
     */
    public void setTimer(int neighbor) {
        timers.put(neighbor,1);
        double timerValue=0;
        
        if(timersInitializeationStatus.get(neighbor)==0)
        {
           
            double randValue = random.nextDouble();
            timerValue = MRAI*randValue;
            double expiry = kernel.getTime()+timerValue;
            timersInitializeationStatus.put(neighbor,1);
        }
        else
        {
            timerValue = (0.25*randomTimer.nextDouble()+0.75)*MRAI;
            double expiry = kernel.getTime()+timerValue;
        }           
       kernel.schedule(new TimerExpiryEvent(AS,neighbor,kernel,kernel.getTime()+timerValue));
    }
    public boolean getInitializationStatus(int neighbor)
    {
       if(timersInitializeationStatus.get(neighbor)==0)
           return false;
       else
           return true;
    }
    /**
     * Turn Off the MRAI timer associated with the resoecive neighbor
     */
    public void resetTimer(int neighbor) {
        timers.put(neighbor,0);
    }
    /**
     *  Initialize all neighbors timers
     */
    public void initializeTimers() {
        TIntArrayList staticAdj = Topology.getStaticAdj(AS);
        for(int i=0;i<staticAdj.size();i++) {
            timers.put(staticAdj.get(i),0);
            timersInitializeationStatus.put(staticAdj.get(i),0);
        }
    }
    /**
     * Returns all timers.
     */
    public TIntIntHashMap getTimers() {
        return timers;
    }
    /** Bootstrap this node
     *  @param adjacencies this node adajacencies
     *  @param kernel a reference to the simulator kernel
     *
     */
    public void initialize(TIntArrayList adjacencies, Kernel kernel) {
        initializeTimers();
        initializeOutputQueue();
        this.kernel = kernel;
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
                    for(int k=0;k<result.size();k++) {
                        kernel.schedule(new ArrivalEventV2(adjacencies.get(j),(Message)result.get(k),kernel,GetTime.getNextScheduleArrival(kernel,AS,adjacencies.get(j))));
                        
                    }
                    setTimer(adjacencies.get(j));
                }
            }else {
                this.adjacenes = adjacencies;
                Message sessionEstablishment = new Message(Values.interruptMessage,Values.sessionEstablishment,AS);
                for(int j=0;j<adjacencies.size();j++) {
                    kernel.schedule(new ArrivalEventV2(adjacencies.get(j),sessionEstablishment,kernel,GetTime.getNextScheduleArrival(kernel,AS,adjacencies.get(j))));
                }
            }
        }
        monitor = new MessagesMonitor();
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
        rib.rib.listOfRIBS.clear();
        rib.rib.RIBsIndex.clear();
        adjacenes.clear();
        this.initialized = false;
        initializeTimers();
    }
    /**
     * Checks if the MRAI timer associated to this neighbor is ON or OFF
     */
    public boolean isTimerOn(int neighbor) {
        if(timers.get(neighbor)==1)
            return true;
        else
            return false;
    }
    
    public void setAdjacenes(TIntArrayList adjacenes) {
        this.adjacenes = adjacenes;
    }
    
    /**
     * Returns a reference to the outgoing messages queue
     */
    public TIntObjectHashMap getOutputQueue() {
        return outputQueue;
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
    	// added by giulio
    	//System.out.print("\n\nNODE "+this.AS+"\n"+this.getRib().toString()); 
    	//
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
