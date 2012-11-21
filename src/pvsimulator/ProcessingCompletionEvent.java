/*
 * ProcessingCompletionEvent.java
 *
 * Created on July 2, 2007, 8:57 AM
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


/** A class represents processing completion event- SPV Protocol
 *
 * @author ahmed
 */
public class ProcessingCompletionEvent extends Unit{
    
    /** Creates a new instance of ProcessingCompletionEvent */
    private Message message;
    private Kernel kernel;
    private Node processor;
    static long num=0;
    public ProcessingCompletionEvent() {
    }
    /** Creates a new instance of ProcessingCompletionEvent */
    public ProcessingCompletionEvent(Message message,Kernel kernel,Node processor,double nextSchedule) {
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
        }
    }
    private void updateHandler(Message message,Node node) {
        RIB rib = node.getRib();
        TIntObjectHashMap upds = new TIntObjectHashMap();
        TIntObjectHashMap withds = new TIntObjectHashMap();
        int peer = message.getSrc();
        if(!node.getAdjacenes().contains(peer)) {
            node.getAdjacenes().add(peer);
            TLinkedList tableTransfer = prepareTableTransfer();
            for(int i=0;i<tableTransfer.size();i++)
                kernel.schedule(new ArrivalEvent(peer,(Message) tableTransfer.get(i), kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),peer)));
            
        }
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
                                }
                                else
                                {
                                 withds.put(withdrawans.get(i),nbPnextHop);
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
                    int nBPNextHop = rib.getBestPathNextHop(prefix);
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
        }
        TIntArrayList nodeAdjacences = node.getAdjacenes();
        
        for(int i=0;i<nodeAdjacences.size();i++) {
            
            if(!(upds.isEmpty()&&withds.isEmpty())) {
                TLinkedList updateMessages =  getMessage(withds,upds,nodeAdjacences.get(i));
                for(int j=0;j<updateMessages.size();j++) {
                    kernel.schedule(new ArrivalEvent(nodeAdjacences.get(i),(Message) updateMessages.get(j), kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),nodeAdjacences.get(i))));
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
                //TIntArrayList Path = processor.getRib().getBestPathVector(Prefix);
                //TIntArrayList clonedPath = (TIntArrayList)Path.clone();
                //clonedPath.insert(0,processor.getAS());
                GRIBTNode Path = processor.getRib().getBestPathVector(Prefix);
                GRIBTNode clonedPath = GRIB.addNode(processor.getAS(),Path);;
                message.setPath(clonedPath);
                result.add(message);
            }
        }
        return result;
    }
    
    private void interruptMessageHandler(Message message,Node node) {
        if(message.getIID()==Values.prefixesDown) {
            TIntArrayList withdrawans = message.getWithdrawans();
            TIntArrayList neighbors = processor.getAdjacenes();
            for(int i=0;i<withdrawans.size();i++) {
                processor.getAttachedNetworks().remove(processor.getAttachedNetworks().indexOf(withdrawans.get(i)));
            }
            Message updateMessage = populateUpdateMessages(withdrawans);
            for(int j=0;j<neighbors.size();j++)
                kernel.schedule(new ArrivalEvent(neighbors.get(j),updateMessage,kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),neighbors.get(j))));
        }else {
            if(message.getIID()==Values.prefixesUp) {
                TIntArrayList announced = message.getAnnounced();
                TIntArrayList neighbors = processor.getAdjacenes();
                TLinkedList updates = new TLinkedList();
                //TIntArrayList path = new TIntArrayList();
                //path.add(processor.getAS());
                GRIBTNode path = GRIB.levelOne[processor.getAS()-1];
                for(int i=0;i<announced.size();i++) {
                    processor.getAttachedNetworks().add(announced.get(i));
                    updates.add(new Message(Values.updateMessage,processor.getAS(),1,path,announced.get(i)));
                }
                for(int j=0;j<neighbors.size();j++)
                    for (int k=0;k<updates.size();k++)
                        kernel.schedule(new ArrivalEvent(neighbors.get(j),(Message) updates.get(k), kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),neighbors.get(j))));
            } else {
                if(message.getIID() == Values.sessionEstablishment) {
                    
                    int newNeighbor = message.getAffectedNeighbor();
                    
                    node.getAdjacenes().add(newNeighbor);
                    TLinkedList updateMessages = prepareTableTransfer();
                    for(int i=0;i<updateMessages.size();i++)
                        //schedule message arrivals at affected neighbor here
                        kernel.schedule(new ArrivalEvent(newNeighbor,(Message) updateMessages.get(i), kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),newNeighbor)));
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
                            kernel.schedule(new ArrivalEvent(affectedNeighor,(Message) updateMessages.get(i), kernel,GetTime.getNextSchedule(kernel)));
                    }
                    if(IID==Values.ReachLoss) {
                        node.getAdjacenes().remove(node.getAdjacenes().indexOf(affectedNeighor));
                        TIntArrayList entries = rib.getAdjRIBInKeys(affectedNeighor);
                        for(int i=0;i<entries.size();i++) {
                            int prefix = entries.get(i);
                            if(rib.getBestPathNextHop(prefix)==affectedNeighor) {
                                rib.removeEntry(prefix,affectedNeighor);
                                TIntObjectHashMap newBestPath = rib.decisionProcess(prefix,node.getAS());
                                if(newBestPath!=null) {
                                    
                                    //updateList.put(prefix,new pathAttr(rib.getBestPathNextHop(prefix),rib.getBestPathVector(prefix)));
                                    
                                    // withdrawTracker.get(rib.getBestPathNextHop(prefix)).add(prefix);
                                } else {
                                    withdrawnsList.add(prefix);
                                }
                            } else {
                                rib.removeEntry(entries.get(i),affectedNeighor);
                            }
                        }
                        TIntArrayList nodeAdjacences = node.getAdjacenes();
                        
                        for(int j=0;j<nodeAdjacences.size();j++) {
                            
                           /* TLinkedList updateMessages = populateUpdateMessages(updateList,withdrawnsList,nodeAdjacences.get(j));
                            for(int k=0;k<updateMessages.size();k++) {
                                //schedule arrival messages here
                                kernel.schedule(new ArrivalEvent(nodeAdjacences.get(j),(Message) updateMessages.get(k), kernel,GetTime.getNextSchedule(kernel)));
                            }*/
                        }
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
        //TIntArrayList path = new TIntArrayList();
        //path.add(processor.getAS());
        GRIBTNode path = GRIB.levelOne[processor.getAS()-1];
        if(!attachments.isEmpty()) {
            for(int i=0;i<attachments.size();i++) {
                result.add(new Message(Values.updateMessage,processor.getAS(),1,path,attachments.get(i)));
            }
        }
        if(locRIB!=null) {
            TIntObjectIterator iterator = locRIB.iterator();
            for (int i = locRIB.size(); i-- > 0;){
                iterator.advance();
                int prefix = iterator.key();
                //TIntArrayList Path = (TIntArrayList)iterator.value();
                //TIntArrayList clonedPath = (TIntArrayList) Path.clone();
                //clonedPath.insert(0,processor.getAS());
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

