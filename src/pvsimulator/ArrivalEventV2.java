/*
 * ArrivalEventV2.java
 *
 * Created on July 17, 2007, 2:03 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;
import gnu.trove.TLinkedList;
import java.util.ArrayList;
/** A class representing messages arrival events at the network nodes' queues. This is used when simulating SPV+MRAI and SPV+MRAI+Policies
 *
 * @author ahmed
 */
public class ArrivalEventV2 extends Unit{
    
    /** Creates a new instance of ArrivalEventV2 */
    public ArrivalEventV2() {
    }
    private int recepient;
    private Message message;
    private Kernel kernel;
    public static boolean dumpMessages = false;
    public static boolean Counter = false;
    public static double time=0;
    /** A constructor for creating new arrival event */
    public ArrivalEventV2(int atNode,Message message,Kernel kernel,double nextSchedule){
        this.recepient = atNode;
        this.message = message;
        this.kernel = kernel;
        this.nextSchedule = nextSchedule;
    }
  /*Act is called by the event scheduler when executing this event*/  
    public void act() {
        if(dumpMessages)
        {
            if(message.getIID()==Values.updateMessage)
             System.out.println(Util.format(kernel.getTime(),3,0)+"\tUPDATE\t"+message.toString(recepient));
            else
             System.out.println(Util.format(kernel.getTime(),3,0)+"\tInterrupt\tIID="+message.getIID()+"\tAT_Node="+message.getAffectedNeighbor());   
        }
        NodeV2 receiver = Topology.getNodeByAddressV2(recepient);
        if(receiver.isInitialized()) {
            if(Counter) {

                if(message.getType()==Values.updateMessage&&!(message.isIsPartOfTableTransfer())) {
                                   
                    receiver.getMonitor().update(kernel.getTime(),message.getSrc());
                }
            }
            ArrayList<Message> inputQueue = receiver.getMessageQueue();
            
            if(receiver.getIdle()) {
                //Server not busy
                //This Check 
                if(Topology.withPolicies)
                    kernel.schedule(new ProcessingCompletionEventV3(message,kernel,receiver,kernel.getGetTime().getProcessingTime(kernel)));
                else
                {
                    kernel.schedule(new ProcessingCompletionEventV2(message,kernel,receiver,kernel.getGetTime().getProcessingTime(kernel)));
                }
                receiver.setIdle(false);
            } else {
                //Server busy
                // If the incoming message is an interrupt message It will be placed in the front of the incoming messages queue
                if(message.getType()==Values.interruptMessage && !inputQueue.isEmpty()) {
                    removeInvalidatedMessages(message,receiver);
                    boolean flag = false;
                    for(int i=0;i<inputQueue.size();i++) {
                        
                        if((inputQueue.get(i)).getType()!=Values.interruptMessage) {
                            flag = true;
                            inputQueue.add(i,message);
                            break;
                        }
                    }
                    if(!flag)
                        inputQueue.add(message);
                } else {
                    inputQueue.add(message);
                    
                }
                
            }
        } else {
            System.out.println("Message Dropped");
        }
    }
    // Remove from the outgoing queue all the invalidated messages by the current message
    private void removeInvalidatedMessages(Message message,NodeV2 node) {
        
        if(message.getIID()==Values.ReachLoss) {
            ArrayList<Message> inputQueue = node.getMessageQueue();
            int affectedNeighbor = message.getAffectedNeighbor();
            ArrayList<Message> toBeRemoved = new ArrayList<Message>();
            for(int i=0;i<inputQueue.size();i++) {
                Message currentMessage = (Message)inputQueue.get(i);
                if(currentMessage.getType()==Values.updateMessage)
                {
                    if(currentMessage.getSrc()==affectedNeighbor)
                    {  
                    toBeRemoved.add(currentMessage);    
                    }
                }
            }
            if(!toBeRemoved.isEmpty())
            {
              inputQueue.removeAll(toBeRemoved);
            }
        }
    }
}
