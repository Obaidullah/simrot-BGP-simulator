/*
 * ArrivalEvent.java
 *
 * Created on June 29, 2007, 1:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import gnu.trove.TLinkedList;
import java.util.ArrayList;


/** A class representing messages arrival events at the network nodes' queues. This is used when simulating SPV only.
 *
 * @author ahmed
 */
public class ArrivalEvent extends Unit{
    
    /** Creates a new instance of ArrivalEvent */
    private int recepient;
    private Message message;
    private Kernel kernel;
    //public static long count=0;
    public static boolean Counter = false;
    public ArrivalEvent() {
    }
    /** A constructor for creating new arrival event */
    public ArrivalEvent(int atNode,Message message,Kernel kernel,double nextSchedule){
        this.recepient = atNode;
        this.message = message;
        this.kernel = kernel;
        this.nextSchedule = nextSchedule;
    }
    public void act() {
        Node receiver = Topology.getNodeByAddress(recepient);
        if(receiver.isInitialized()) {
            ArrayList<Message> inputQueue = receiver.getMessageQueue();
            if(Counter) {
                if(message.getType()==Values.updateMessage)
                    receiver.getMonitor().update(kernel.getTime(),message.getSrc());
            }
            if(receiver.getIdle()) {
                //Server not busy         
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
        }
    }
    private void removeInvalidatedMessages(Message message,Node node) {
        
        if(message.getIID()==Values.ReachLoss) {
            ArrayList<Message> inputQueue = node.getMessageQueue();
            int affectedNeighbor = message.getAffectedNeighbor();
            for(int i=0;i<inputQueue.size();i++) {
                Message currentMessage = (Message)inputQueue.get(i);
                if(currentMessage.getType()==Values.updateMessage && currentMessage.getSrc()==affectedNeighbor)
                    inputQueue.remove(currentMessage);
            }
        }
    }
}
