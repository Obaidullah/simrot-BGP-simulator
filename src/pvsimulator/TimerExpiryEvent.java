/*
 * TimerExpiryEvent.java
 *
 * Created on July 17, 2007, 2:17 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import java.util.ArrayList;
import java.util.Random;

/**
 *  This class represents MRAI timer expiry event
 * @author ahmed
 */
public class TimerExpiryEvent extends Unit{
    
    private int atNode;
    private int forNode;
    private Kernel kernel;
    private Random randomTimer;
    /** Creates a new instance of TimerExpiryEvent */
    public TimerExpiryEvent() {
    }
     /** Creates a new instance of TimerExpiryEvent */
     public TimerExpiryEvent(int atNode,int forNode,Kernel kernel,double nextSchedule)
     {
      
       this.atNode = atNode;
       this.forNode = forNode;
       this.kernel = kernel;
       this.nextSchedule = nextSchedule;
       randomTimer = new Random(7910*NodeV2.magicNumber+atNode*13);
     }

    public void act() {
        NodeV2 node = Topology.getNodeByAddressV2(atNode);
        ArrayList<Message> messagesToSend = new ArrayList<Message>();
        try
        {        
        if(node.getAdjacenes().contains(forNode))
        {    
           messagesToSend = (ArrayList<Message>)node.getOutputQueue().get(forNode);
           if(!messagesToSend.isEmpty())
           {    
           ArrayList<Message> clonedMessages = (ArrayList<Message>)messagesToSend.clone(); 
           for(int i=0;i<clonedMessages.size();i++)
           {
              int announcedPrefix = clonedMessages.get(i).getAnnouncedPrefix();
              if(NodeV2.ProtocolVersion==1)
              {    
              if(!node.getAttachedNetworks().contains(announcedPrefix))
                    ((RIBV2)node.getRib()).addToRIBOut(forNode,announcedPrefix);
              }
               kernel.schedule(new ArrivalEventV2(forNode,clonedMessages.get(i),kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),forNode)));
           }
           messagesToSend.clear();
           double timerValue = (0.25*randomTimer.nextDouble()+0.75)*NodeV2.MRAI;
           kernel.schedule(new TimerExpiryEvent(atNode,forNode,kernel,kernel.getTime()+timerValue));
           }
        else
        {
          double timerValue = (0.25*randomTimer.nextDouble()+0.75)*NodeV2.MRAI;
          kernel.schedule(new TimerExpiryEvent(atNode,forNode,kernel,kernel.getTime()+timerValue));
        }  
       }   
        } catch(Exception e)
        {
           System.out.println(atNode+"\t"+forNode+"\t"+node.getAdjacenes().contains(forNode)+"\t"+node.getOutputQueue().containsKey(forNode)+"\t"+node.getTimers().containsKey(forNode));
           e.printStackTrace();
           System.exit(0);
        }
    }

    
}
