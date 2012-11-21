/*
 * ProcessingCompletionEventV2.java
 *
 * Created on July 17, 2007, 12:15 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import gnu.trove.TLinkedList;
import java.util.ArrayList;
import java.util.Iterator;

/** A class represents processing completion event- SPV+MRAI Protocol
 *
 * @author ahmed
 */
public class ProcessingCompletionEventV2 extends Unit{
    
    /** Creates a new instance of ProcessingCompletionEventV2 */
    private Message message;
    private Kernel kernel;
    private NodeV2 processor;
    static long num=0;
    public ProcessingCompletionEventV2() {
    }
    public ProcessingCompletionEventV2(Message message,Kernel kernel,NodeV2 processor,double nextSchedule) {
        this.message = message;
        this.kernel = kernel;
        this.processor = processor;
        this.nextSchedule = nextSchedule;
    }
    public void act() {
        ArrayList<Message> queue = processor.getMessageQueue();
        Message nextMessage = new Message();
        if(message.getType()==Values.interruptMessage) {
            interruptMessageHandler(message,processor);
        } else {
            updateHandler(message,processor);
        }
        if(queue.isEmpty()) {
            
            processor.setIdle(true);
        } else {
            nextMessage = queue.remove(0);
            kernel.schedule(new ProcessingCompletionEventV2(nextMessage,kernel,processor,kernel.getGetTime().getProcessingTime(kernel)));
        }
    }
    private void updateHandler(Message message,NodeV2 node) {
        RIB rib = node.getRib();
        TIntObjectHashMap upds = new TIntObjectHashMap();
        TIntObjectHashMap withds = new TIntObjectHashMap();
        int peer = message.getSrc();
        if(message.getWithdrawanLength()>0) {
            TIntArrayList withdrawans = message.getWithdrawans();
            for(int i=0;i<withdrawans.size();i++) {
                if(!node.getAttachedNetworks().contains(withdrawans.get(i))) {
                    int bPathNextHop = rib.getBestPathNextHop(withdrawans.get(i));
                    rib.removeEntry(withdrawans.get(i),peer);
                    if(bPathNextHop == peer) {
                        if(rib.havePath(withdrawans.get(i))) {
                            rib.decisionProcess(withdrawans.get(i),node.getAS());
                            int nbPnextHop = rib.getBestPathNextHop(withdrawans.get(i));
                            TIntArrayList toBeSendTo = new TIntArrayList();
                            TIntArrayList neighors = node.getAdjacenes();
                            for(int j=0;j<neighors.size();j++) {
                                if(neighors.get(j)!=nbPnextHop) {
                                    toBeSendTo.add(neighors.get(j));
                                } else {
                                    TIntArrayList toBeWithdrawanFrom = new TIntArrayList();
                                    toBeWithdrawanFrom.add(nbPnextHop);
                                    withds.put(withdrawans.get(i),toBeWithdrawanFrom);
                                }
                            }
                            upds.put(withdrawans.get(i),toBeSendTo);
                        } else {
                            TIntArrayList toBeSendTo = new TIntArrayList();
                            TIntArrayList neighors = node.getAdjacenes();
                            for(int j=0;j<neighors.size();j++) {
                                if(neighors.get(j)!=peer) {
                                    toBeSendTo.add(neighors.get(j));
                                }
                            }
                            withds.put(withdrawans.get(i),toBeSendTo);
                        }
                    }
                }
            }
            
        }
        if(message.getPathAttrLength()>0) {
            int prefix = message.getAnnouncedPrefix();
            GRIBTNode path = message.getPath();
            int nBPNextHop = 0 ;
            if(GRIB.isValidPath(node.getAS(),path)) {
                if(rib.havePath(prefix)) {
                    int oldBPNextHop = rib.getBestPathNextHop(prefix);
                    GRIBTNode oldBP = rib.getBestPathVector(prefix);
                    if(rib.entryExist(prefix,peer)) {
                        rib.replaceEntry(prefix,peer,path,false);
                    } else {
                        rib.addEntry(prefix,peer,path);
                    }
                    rib.decisionProcess(prefix,node.getAS());
                    nBPNextHop = rib.getBestPathNextHop(prefix);
                    GRIBTNode newBP = rib.getBestPathVector(prefix);
                    if(!(newBP.equals(oldBP))) {
                        TIntArrayList toBeSendTo = new TIntArrayList();
                        TIntArrayList neighors = node.getAdjacenes();
                        for(int j=0;j<neighors.size();j++) {
                            if(neighors.get(j)!=nBPNextHop) {
                                toBeSendTo.add(neighors.get(j));
                            }
                        }
                        upds.put(prefix,toBeSendTo);
                        if(oldBPNextHop!=nBPNextHop) {
                            TIntArrayList withdrawFrom = new TIntArrayList();
                            withdrawFrom.add(nBPNextHop);
                            withds.put(prefix,withdrawFrom);
                        }
                    }
                }else {
                    rib.addEntry(prefix,peer,path);
                    rib.decisionProcess(prefix,node.getAS());
                    TIntArrayList toBeSendTo = new TIntArrayList();
                    TIntArrayList neighors = node.getAdjacenes();
                    for(int j=0;j<neighors.size();j++) {
                        if(neighors.get(j)!=peer) {
                            toBeSendTo.add(neighors.get(j));
                        }
                    }
                    upds.put(prefix,toBeSendTo);
                }
            }
            else
            {
               if(rib.entryExist(prefix,peer)) {
                    int oldBPNextHop = rib.getBestPathNextHop(prefix);
                    rib.removeEntry(prefix,peer);
                    if(oldBPNextHop==peer)
                    {
                       if(rib.havePath(prefix))
                       {   
                       rib.decisionProcess(prefix,node.getAS());
                       int newBPNextHop = rib.getBestPathNextHop(prefix);
                       TIntArrayList neighors = node.getAdjacenes();
                       if(newBPNextHop!=0)
                       {
                          TIntArrayList toBeSendTo = new TIntArrayList();
                          TIntArrayList toBeWithdrawanFrom = new TIntArrayList();
                          for(int i=0;i<neighors.size();i++)
                          {
                            if(neighors.get(i)!=newBPNextHop)
                            {
                              toBeSendTo.add(neighors.get(i));
                            }
                          }
                          toBeWithdrawanFrom.add(newBPNextHop);
                          upds.put(prefix,toBeSendTo);
                          withds.put(prefix,toBeWithdrawanFrom);
                       }
                       }
                       else
                       {
                         TIntArrayList neighors = node.getAdjacenes();  
                         TIntArrayList toBeSendTo = new TIntArrayList();
                          for(int i=0;i<neighors.size();i++)
                          {
                            if(neighors.get(i)!=peer)
                            {
                              toBeSendTo.add(neighors.get(i));
                            }
                          }
                          
                          withds.put(prefix,toBeSendTo);
                       }    
                    }
               }
            }    
        }
        TIntArrayList nodeAdjacences = node.getAdjacenes();
        if(NodeV2.MRAI!=0) {
            for(int i=0;i<nodeAdjacences.size();i++) {
                boolean flag = false;
                if(!(upds.isEmpty()&&withds.isEmpty())) {
                    TLinkedList updateMessages =  getMessage(withds,upds,nodeAdjacences.get(i));
                    for(int j=0;j<updateMessages.size();j++) {
                        //schedule arrival messages here
                        Message updateMessage = (Message) updateMessages.get(j);
                        if(updateMessage.getPathAttrLength()==0) {
                            kernel.schedule(new ArrivalEventV2(nodeAdjacences.get(i),updateMessage, kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),nodeAdjacences.get(i))));
                            ArrayList<Message> neighborOutputQueue = (ArrayList<Message>) node.getOutputQueue().get(nodeAdjacences.get(i));
                            if(!neighborOutputQueue.isEmpty()) {
                                ArrayList<Message> toBeRemoved = new ArrayList<Message>();
                                Iterator<Message> listIterator = neighborOutputQueue.iterator();
                                while(listIterator.hasNext()) {
                                    Message currentMessage = listIterator.next();
                                    if(updateMessage.getWithdrawans().contains(currentMessage.getAnnouncedPrefix()))
                                        toBeRemoved.add(currentMessage);
                                }
                                if(!toBeRemoved.isEmpty()) {
                                    
                                    neighborOutputQueue.removeAll(toBeRemoved);
                                }
                            }
                        } else {
                            if(!node.isTimerOn(nodeAdjacences.get(i))) {
                                if(!node.getInitializationStatus(nodeAdjacences.get(i))) {
                                    ArrayList<Message> neighborOutputQueue = (ArrayList<Message>) node.getOutputQueue().get(nodeAdjacences.get(i));
                                    neighborOutputQueue.add(updateMessage);
                                    node.setTimer(nodeAdjacences.get(i));
                                } else {
                                    int announcedPrefix = updateMessage.getAnnouncedPrefix();
                                    kernel.schedule(new ArrivalEventV2(nodeAdjacences.get(i),updateMessage, kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),nodeAdjacences.get(i))));
                                    flag = true;
                                }
                            } else {
                                
                                ArrayList<Message> neighborOutputQueue = (ArrayList<Message>) node.getOutputQueue().get(nodeAdjacences.get(i));
                                if(!neighborOutputQueue.isEmpty()) {
                                    ArrayList<Message> toBeRemoved = new ArrayList<Message>();
                                    Iterator<Message> listIterator = neighborOutputQueue.iterator();
                                    while(listIterator.hasNext()) {
                                        Message currentMessage = listIterator.next();
                                        if(currentMessage.getAnnouncedPrefix()==updateMessage.getAnnouncedPrefix())
                                            toBeRemoved.add(currentMessage);
                                    }
                                    if(!toBeRemoved.isEmpty()) {
                                        
                                        neighborOutputQueue.removeAll(toBeRemoved);
                                    }
                                    neighborOutputQueue.add(updateMessage);
                                } else {
                                    neighborOutputQueue.add(updateMessage);
                                }
                            }
                            
                        }
                        
                    }
                    
                }
                if(flag)
                    node.setTimer(nodeAdjacences.get(i));
            }
        } else {
            for(int i=0;i<nodeAdjacences.size();i++) {
                if(!(upds.isEmpty()&&withds.isEmpty())) {
                    TLinkedList updateMessages =  getMessage(withds,upds,nodeAdjacences.get(i));
                    for(int j=0;j<updateMessages.size();j++) {
                        //schedule arrival messages here
                        Message updateMessage = (Message) updateMessages.get(j);
                        if(updateMessage.getPathAttrLength()==0) {
                            kernel.schedule(new ArrivalEventV2(nodeAdjacences.get(i),updateMessage, kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),nodeAdjacences.get(i))));
                        } else {
                            kernel.schedule(new ArrivalEventV2(nodeAdjacences.get(i),updateMessage, kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),nodeAdjacences.get(i))));
                        }
                    }
                }
            }
        }
        
    }
    private TLinkedList getMessage(TIntObjectHashMap withd,TIntObjectHashMap upds,int targetNode) {
        TLinkedList result = new TLinkedList();
        TIntObjectIterator Iterator = withd.iterator();
        TIntArrayList withdrawnsList = new TIntArrayList();
        for(int i = withd.size(); i-- > 0;) {
            Iterator.advance();
            int prefix = Iterator.key();
            TIntArrayList dst = (TIntArrayList) Iterator.value();
            if(dst.contains(targetNode))
                withdrawnsList.add(prefix);
        }
        if(!withdrawnsList.isEmpty())
            result.add(new Message(Values.updateMessage,processor.getAS(),withdrawnsList));
        Iterator = upds.iterator();
        for(int j = upds.size(); j-- > 0;) {
            Iterator.advance();
            int Prefix = Iterator.key();
            TIntArrayList dst = (TIntArrayList) Iterator.value();
            if(dst.contains(targetNode)) {
                Message message = new Message(Values.updateMessage,processor.getAS());
                message.setPathAttrLength(1);
                message.setAnnouncedPrefix(Prefix);
                GRIBTNode Path = processor.getRib().getBestPathVector(Prefix);
                GRIBTNode clonedPath = GRIB.addNode(processor.getAS(),Path);;
                message.setPath(clonedPath);
                result.add(message);
            }
        }
        return result;
    }
    private void interruptMessageHandler(Message message,NodeV2 node) {
        
        if(message.getIID()==Values.prefixesDown) {
            TIntArrayList withdrawans = message.getWithdrawans();
            TIntArrayList neighbors = node.getAdjacenes();
            for(int i=0;i<withdrawans.size();i++) {
                node.getAttachedNetworks().remove(node.getAttachedNetworks().indexOf(withdrawans.get(i)));
                
            }
            Message updateMessage = populateUpdateMessages(withdrawans);
            for(int j=0;j<neighbors.size();j++)
                kernel.schedule(new ArrivalEventV2(neighbors.get(j),updateMessage,kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),neighbors.get(j))));
        }else {
            if(message.getIID()==Values.prefixesUp) {
                TIntArrayList announced = message.getAnnounced();
                TIntArrayList neighbors = node.getAdjacenes();
                TLinkedList updates = new TLinkedList();
                GRIBTNode path = GRIB.levelOne[node.getAS()-1];
                for(int i=0;i<announced.size();i++) {
                    node.getAttachedNetworks().add(announced.get(i));
                    updates.add(new Message(Values.updateMessage,processor.getAS(),1,path,announced.get(i)));
                }
                if(NodeV2.MRAI!=0) {
                    for(int j=0;j<neighbors.size();j++) {
                        boolean flag = false;
                        for (int k=0;k<updates.size();k++) {
                            if(!node.isTimerOn(neighbors.get(j))) {
                                if(!node.getInitializationStatus(neighbors.get(j))) {
                                    ArrayList<Message> neighborOutputQueue = (ArrayList<Message>) node.getOutputQueue().get(neighbors.get(j));
                                    neighborOutputQueue.add((Message) updates.get(k));
                                    node.setTimer(neighbors.get(j));
                                } else {
                                    kernel.schedule(new ArrivalEventV2(neighbors.get(j),(Message) updates.get(k), kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),neighbors.get(j))));
                                    flag = true;
                                }
                            } else {
                                ArrayList<Message> neighborOutputQueue = (ArrayList<Message>) node.getOutputQueue().get(neighbors.get(j));
                                neighborOutputQueue.add((Message)updates.get(k));
                            }
                        }
                        if(flag)
                            node.setTimer(neighbors.get(j));
                    }
                } else {
                    for(int j=0;j<neighbors.size();j++) {
                        //boolean flag = false;
                        for (int k=0;k<updates.size();k++) {
                            kernel.schedule(new ArrivalEventV2(neighbors.get(j),(Message) updates.get(k), kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),neighbors.get(j))));
                        }
                    }
                }
            } else {
                if(message.getIID() == Values.sessionEstablishment) {
                    int newNeighbor = message.getAffectedNeighbor();
                    node.getAdjacenes().add(newNeighbor);
                    TLinkedList updateMessages = prepareTableTransfer();
                    for(int i=0;i<updateMessages.size();i++)
                        //schedule message arrivals at affected neighbor here
                        kernel.schedule(new ArrivalEventV2(newNeighbor,(Message) updateMessages.get(i), kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),newNeighbor)));
                    if(!updateMessages.isEmpty())
                        node.setTimer(newNeighbor);
                } else {
                    TIntObjectHashMap updateList = new TIntObjectHashMap();
                    TIntArrayList withdrawnsList = new TIntArrayList();
                    RIB rib = node.getRib();
                    int IID = message.getIID();
                    int affectedNeighor = message.getAffectedNeighbor();
                    if(IID==Values.ReachGain) {
                        node.getAdjacenes().add(affectedNeighor);
                        TLinkedList updateMessages = prepareTableTransfer();
                        for(int i=0;i<updateMessages.size();i++)
                            //schedule message arrivals at affected neighbor here
                            kernel.schedule(new ArrivalEventV2(affectedNeighor,(Message) updateMessages.get(i), kernel,GetTime.getNextSchedule(kernel)));
                        node.setTimer(affectedNeighor);
                    }
                    if(IID==Values.ReachLoss) {
                        node.getAdjacenes().remove(new Integer(affectedNeighor));
                        TIntArrayList entries = rib.getAdjRIBInKeys(affectedNeighor);
                        for(int i=0;i<entries.size();i++) {
                            int prefix = entries.get(i);
                            if(rib.getBestPathNextHop(prefix)==affectedNeighor) {
                                rib.removeEntry(prefix,affectedNeighor);
                                TIntObjectHashMap newBestPath = rib.decisionProcess(prefix,node.getAS());
                                if(newBestPath!=null) {
                                    //updateList.put(prefix,new pathAttr(rib.getBestPathNextHop(prefix),rib.getBestPathVector(prefix)));
                                } else {
                                    withdrawnsList.add(prefix);
                                }
                            } else {
                                rib.removeEntry(entries.get(i),affectedNeighor);
                            }
                        }
                        TIntArrayList nodeAdjacences = node.getAdjacenes();
                        boolean flag = false;
                    }
                }
            }
        }
    }
    private Message populateUpdateMessages(TIntArrayList withdrawanList) {
        
        Message result = new Message(Values.updateMessage,processor.getAS(),withdrawanList);
        
        return result;
    }
    private TLinkedList prepareTableTransfer() {
        TIntObjectHashMap locRIB = processor.getRib().getLocRIB();
        TIntArrayList attachments = processor.getAttachedNetworks();
        TLinkedList result = new TLinkedList();
        GRIBTNode path = GRIB.levelOne[processor.getAS()-1];
        for(int i=0;i<attachments.size();i++) {
            result.add(new Message(Values.updateMessage,processor.getAS(),1,path,attachments.get(i)));
        }
        if(locRIB!=null) {
            TIntObjectIterator iterator = locRIB.iterator();
            for (int i = locRIB.size(); i-- > 0;){
                iterator.advance();
                int prefix = iterator.key();
                GRIBTNode Path = (GRIBTNode)iterator.value();
                GRIBTNode clonedPath = GRIB.addNode(processor.getAS(),Path);
                result.add(new Message(Values.updateMessage,processor.getAS(),1,clonedPath,prefix));
            }
        }
        return result;
    }
    private class pathAttr implements Cloneable{
        public TIntArrayList path;
        public int nextHop;
        public pathAttr(int nextHop,TIntArrayList path) {
            this.path = path;
            this.nextHop = nextHop;
        }
        
        public String toString() {
            return "path "+path.toString()+" Next_HOP="+nextHop;
        }
        
        public Object clone() throws CloneNotSupportedException {
            Object o = null;
            try{
                o = super.clone();
            }catch(CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return o;
        }
        
    }
    
}
