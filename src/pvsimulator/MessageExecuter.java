/*
 * MessageExecuter.java
 *
 * Created on August 20, 2007, 2:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import gnu.trove.TIntArrayList;
import gnu.trove.TLinkedList;
import java.util.ArrayList;

/**
 *
 * @author ahmed
 */
public class MessageExecuter {
    
    /** Creates a new instance of MessageExecuter */
    public static TLinkedList queue = new TLinkedList();
    Processor processor = new Processor();
    public MessageExecuter() {
        init();
        execute();
    }
    public void execute() {
        while(!queue.isEmpty()) {
            
            MessageV2 msg = (MessageV2) queue.remove(0);
            processor.updateMessageHandler(msg);
        }
    }
    private void init() {
        for(int i=1;i<=Topology.numberOfNodes;i++) {
            GRIBTNode path = GRIB.levelOne[i-1];
            TIntArrayList attached = Topology.getNodeByAddressV2(i).getAttachedNetworks();
            TIntArrayList neighbors = Topology.getStaticAdj(i);
            for(int j=0;j<neighbors.size();j++) {
                for(int k=0;k<attached.size();k++) {
                    MessageV2 message = new MessageV2(Values.updateMessage,i,neighbors.get(j),1,path,attached.get(k));
                    queue.add(message);
                }
            }
        }
    }
    
}
