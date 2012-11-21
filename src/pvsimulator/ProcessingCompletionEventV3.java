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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;



/** A class represents processing completion event- SPV+MRAI+Policies Protocol
 *
 * @author ahmed
 */
public class ProcessingCompletionEventV3 extends Unit{
    
    /** Creates a new instance of ProcessingCompletionEvent */
    private Message message;
    private Kernel kernel;
    private NodeV2 processor;
    static long num=0;
    public ProcessingCompletionEventV3() {
    }
    public ProcessingCompletionEventV3(Message message,Kernel kernel,NodeV2 processor,double nextSchedule) {
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
            //-----------------------------------------------//
            //System.out.print("\n\nNODE "+this.processor.getAS()+"     TIME:"+kernel.getTime()+"\n"+this.processor.getRib());
            //-----------------------------------------------//
        } else {
            
            updateMessageHandler(message,processor);
            //System.out.print("\n\nNODE "+this.processor.getAS()+"     TIME:"+kernel.getTime()+"\n"+this.processor.getRib());
        }
        if(queue.isEmpty()) {
            
            processor.setIdle(true);
        } else {
            nextMessage = queue.remove(0);
            kernel.schedule(new ProcessingCompletionEventV3(nextMessage,kernel,processor,kernel.getGetTime().getProcessingTime(kernel)));
        }
    }
    
    private void updateMessageHandler(Message message,NodeV2 node) {
        try{
    	/* Rashed */
        FileWriter fstream = new FileWriter("NodeChangesAtExactTime.txt", true);
        BufferedWriter bufout = new BufferedWriter(fstream);
        
        // Giulio
        String nameOfFile = "Node_"+node.getAS()+"_.txt";
        FileWriter dumpfilestream = new FileWriter(nameOfFile, true);
        BufferedWriter buffer = new BufferedWriter(dumpfilestream);
    	
        RIBV2 rib = (RIBV2) node.getRib();
        TIntObjectHashMap withd = new TIntObjectHashMap();
        TIntObjectHashMap upds = new TIntObjectHashMap();
        int peer = message.getSrc();
        boolean restored = false;
        if(!processor.getAdjacenes().contains(peer)) {
            
        } else {
            if(message.getWithdrawanLength()>0) {
                TIntArrayList withdrawns = message.getWithdrawans();
                for(int i=0;i<withdrawns.size();i++) {
                    if(!node.getAttachedNetworks().contains(withdrawns.get(i))) {
                        int bestPathNextHop = rib.getBestPathNextHop(withdrawns.get(i));
                        TIntArrayList exportedTo = rib.getExportedTo(withdrawns.get(i));
                        rib.removeEntry(withdrawns.get(i),peer);
  
                        //System.out.print("\nwithwrawal prefix "+this.message.getWithdrawans()+" at node "+this.processor.getAS()+",  TIME:   "+kernel.getTime()+"\n");//------------------------------------------------------------------------------
                       	bufout.write("Remove \t" +this.message.getWithdrawans()+"\t At node"+this.processor.getAS()+"\t TIME: \t"+GetTime.getNextSchedule(kernel)+"\n");
                        bufout.flush();
                        
                        buffer.write("Remove \t" +this.message.getWithdrawans()+"\t At node"+this.processor.getAS()+"\t TIME: \t"+GetTime.getNextSchedule(kernel)+"\n");
                        buffer.flush();
	
                        if(bestPathNextHop == peer) {
                            if(rib.havePath(withdrawns.get(i))) {
                                rib.decisionProcess(withdrawns.get(i),node.getAS());
                                int bPathNextHop = rib.getBestPathNextHop(withdrawns.get(i));
                                TIntArrayList toBeExportedTo = rib.exportedTO(bPathNextHop,processor);
                                TIntArrayList result = diff(exportedTo,toBeExportedTo);
                                if(!result.isEmpty()) {
                                    withd.put(withdrawns.get(i),result);
                                }                          
                                upds.put(withdrawns.get(i),toBeExportedTo);
                            } else {
                                TIntArrayList toBeExportedTo = rib.exportedTO(peer,processor);
                                TIntArrayList result = diff(toBeExportedTo,exportedTo);
                                if(!result.isEmpty()) {
                                    for(int k=0;k<result.size();k++) {
                                        ArrayList<Message> neighborOutputQueue = (ArrayList<Message>) node.getOutputQueue().get(result.get(k));
                                        if(!neighborOutputQueue.isEmpty()) {
                                            ArrayList<Message> toBeRemoved = new ArrayList<Message>();
                                            Iterator<Message> listIterator = neighborOutputQueue.iterator();
                                            while(listIterator.hasNext()) {
                                                Message currentMessage = listIterator.next();
                                                if(currentMessage.getAnnouncedPrefix()==withdrawns.get(i))
                                                    toBeRemoved.add(currentMessage);
                                            }
                                            if(!toBeRemoved.isEmpty()) {
                                                
                                                neighborOutputQueue.removeAll(toBeRemoved);
                                            }
                                        }
                                    }
                                }
                                withd.put(withdrawns.get(i),exportedTo);
                                
                            }
                        }
                    }
                }
            }
            if(message.getPathAttrLength()>0) {
                int prefix = message.getAnnouncedPrefix();
                GRIBTNode path = message.getPath();
                int src = message.getSrc();
                int newBestPathNextHop=0;
                if(GRIB.isValidPath(node.getAS(),path)) {
                    if(rib.havePath(prefix)) {
                        GRIBTNode oldBestPath = rib.getBestPathVector(prefix);
                        int oldPathNextHop = rib.getBestPathNextHop(prefix);
                        if(rib.entryExist(prefix,src)) {
                            rib.replaceEntry(prefix,src,path,false);
                            System.out.print("\nreplace prefix "+this.message.getAnnouncedPrefix()+" at node "+this.processor.getAS()+",  TIME:   "+kernel.getTime()+"\n");//------------------------------------------------------------------------------
                        } else {
                            rib.addEntry(prefix,src,path);
                            System.out.print("\nannouncement prefix "+this.message.getAnnouncedPrefix()+" at node "+this.processor.getAS()+",  TIME:   "+kernel.getTime()+"\n");//------------------------------------------------------------------------------
                            
                        }
                        TIntArrayList toBeExportedTOStateful = rib.exportedTO(oldPathNextHop,processor);
                        rib.decisionProcess(prefix,node.getAS());
                        GRIBTNode newBestPath = rib.getBestPathVector(prefix);
                        newBestPathNextHop = rib.getBestPathNextHop(prefix);
                        if(!(newBestPath.equals(oldBestPath))) {
                            TIntArrayList exportedTo = new TIntArrayList();
                            
                            exportedTo=rib.getExportedTo(prefix);
                            TIntArrayList toBeExportedTo = rib.exportedTO(newBestPathNextHop,processor);
                            TIntArrayList diff = diff(exportedTo,toBeExportedTo);
                            if(!diff.isEmpty()) {
                                withd.put(prefix,diff);
                            }
                            upds.put(prefix,toBeExportedTo);
                            TIntArrayList diffResult = diff(toBeExportedTOStateful,exportedTo);
                            if(!diffResult.isEmpty()) {
                                for(int k=0;k<diffResult.size();k++) {
                                    ArrayList<Message> neighborOutputQueue = (ArrayList<Message>) node.getOutputQueue().get(diffResult.get(k));
                                    if(!neighborOutputQueue.isEmpty()) {
                                        ArrayList<Message> toBeRemoved = new ArrayList<Message>();
                                        Iterator<Message> listIterator = neighborOutputQueue.iterator();
                                        while(listIterator.hasNext()) {
                                            Message currentMessage = listIterator.next();
                                            if(currentMessage.getAnnouncedPrefix()==prefix)
                                                toBeRemoved.add(currentMessage);
                                        }
                                        if(!toBeRemoved.isEmpty()) {
                                            
                                            neighborOutputQueue.removeAll(toBeRemoved);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        rib.addEntry(prefix,src,path);
                        //System.out.print("\nannouncement prefix "+this.message.getAnnouncedPrefix()+" at node "+this.processor.getAS()+",  TIME:   "+kernel.getTime()+"\n");//------------------------------------------------------------------------------
                        try{
                        	bufout.write("Insert \t " +this.message.getAnnouncedPrefix()+"\t At node"+this.processor.getAS()+"\t TIME: \t"+GetTime.getNextSchedule(kernel)+"\n");
                            bufout.flush();
                            
                            buffer.write("Insert \t " +this.message.getAnnouncedPrefix()+"\t At node"+this.processor.getAS()+"\t TIME: \t"+GetTime.getNextSchedule(kernel)+"\n");
                            buffer.flush();
	
                        }
                        catch(Exception ex) {
                            ex.printStackTrace();
                        }
                        rib.decisionProcess(prefix,node.getAS());
                        TIntArrayList toBeExportedTo = rib.exportedTO(rib.getBestPathNextHop(prefix),node);
                        upds.put(prefix,toBeExportedTo);
                    }
                }
            }
            TIntArrayList nodeAdjacences = node.getAdjacenes();
            double time = kernel.getTime();
            if(NodeV2.MRAI!=0) {
                for(int i=0;i<nodeAdjacences.size();i++) {
                    boolean flag = false;
                    if(!(upds.isEmpty()&&withd.isEmpty())) {
                        TLinkedList updateMessages =  getMessage(withd,upds,nodeAdjacences.get(i));
                        for(int j=0;j<updateMessages.size();j++) {
                            //schedule arrival messages here
                            Message updateMessage = (Message) updateMessages.get(j);
                            if(updateMessage.getPathAttrLength()==0) {
                                rib.removeFromRIBOut(nodeAdjacences.get(i),updateMessage.getWithdrawans());
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
                                        rib.addToRIBOut(nodeAdjacences.get(i),announcedPrefix);
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
                    if(!(upds.isEmpty()&&withd.isEmpty())) {
                        TLinkedList updateMessages =  getMessage(withd,upds,nodeAdjacences.get(i));
                        for(int j=0;j<updateMessages.size();j++) {
                            //schedule arrival messages here
                            Message updateMessage = (Message) updateMessages.get(j);
                            if(updateMessage.getPathAttrLength()==0) {
                                rib.removeFromRIBOut(nodeAdjacences.get(i),updateMessage.getWithdrawans());
                                kernel.schedule(new ArrivalEventV2(nodeAdjacences.get(i),updateMessage, kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),nodeAdjacences.get(i))));
                            } else {
                                int announcedPrefix = updateMessage.getAnnouncedPrefix();
                                rib.addToRIBOut(nodeAdjacences.get(i),announcedPrefix);
                                kernel.schedule(new ArrivalEventV2(nodeAdjacences.get(i),updateMessage, kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),nodeAdjacences.get(i))));
                            }
                        }
                    }
                }
            }
            bufout.close();
            fstream.close();
            buffer.close();
            dumpfilestream.close();
        }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    private void interruptMessageHandler(Message message,NodeV2 node) {
    	//-------------------------------------------------------
    	//System.out.print("\n\nNODE "+this.processor.getAS()+"     TIME:"+kernel.getTime()+"\n"+this.processor.getRib());
    	//-------------------------------------------------------
    	if(message.getIID() == Values.sessionSignalling) {
            int newNeighbor = message.getAffectedNeighbor();
            node.addNeighbor(newNeighbor);
            if(!node.getRib().rib.listOfRIBS.isEmpty()||!node.getAttachedNetworks().isEmpty()) {
                TLinkedList updateMessages = prepareTableTransfer(newNeighbor);
                for(int i=0;i<updateMessages.size();i++){
                    //schedule message arrivals at affected neighbor here
                    kernel.schedule(new ArrivalEventV2(newNeighbor,(Message) updateMessages.get(i), kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),newNeighbor)));
                  //-------------------------------------------------------
                    System.out.print("\n\nNODE "+this.processor.getAS()+"     TIME:"+kernel.getTime()+"\n"+this.processor.getRib());
                  //-------------------------------------------------------
                }
                if(!updateMessages.isEmpty())
                node.setTimer(newNeighbor);
            }
        } else {
            if(message.getIID()==Values.prefixesDown) {
                TIntArrayList withdrawans = message.getWithdrawans();
                TIntArrayList neighbors = processor.getAdjacenes();
                for(int i=0;i<withdrawans.size();i++) {
                    processor.getAttachedNetworks().remove(processor.getAttachedNetworks().indexOf(withdrawans.get(i)));
                }
                Message updateMessage = populateUpdateMessages(withdrawans);
                double time = kernel.getTime();
                for(int j=0;j<neighbors.size();j++)
                    kernel.schedule(new ArrivalEventV2(neighbors.get(j),updateMessage,kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),neighbors.get(j))));
            }else {
                if(message.getIID()==Values.prefixesUp) {
                	//added by me
                	//System.out.print("\n\nNODE "+this.processor.getAS()+"     TIME:"+kernel.getTime()+"\n"+this.processor.getRib());
                    //----------------
                	TIntArrayList announced = message.getAnnounced();
                    TIntArrayList neighbors = processor.getAdjacenes();
                    TLinkedList updates = new TLinkedList();
                    GRIBTNode path = GRIB.levelOne[node.getAS()-1];
                    for(int i=0;i<announced.size();i++) {
                        processor.getAttachedNetworks().add(announced.get(i));
                        updates.add(new Message(Values.updateMessage,processor.getAS(),1,path,announced.get(i)));
                    }
                    double time = kernel.getTime();
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
                            for (int k=0;k<updates.size();k++) {
                                kernel.schedule(new ArrivalEventV2(neighbors.get(j),(Message) updates.get(k), kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),neighbors.get(j))));
                            }
                        }
                    }
                }else {
                    if(message.getIID() == Values.sessionEstablishment) {
                        int newNeighbor = message.getAffectedNeighbor();
                        node.addNeighbor(newNeighbor);
                        if(!node.getRib().rib.listOfRIBS.isEmpty()||!node.getAttachedNetworks().isEmpty()) {
                            TLinkedList updateMessages = prepareTableTransfer(newNeighbor);
                            for(int i=0;i<updateMessages.size();i++)
                                kernel.schedule(new ArrivalEventV2(newNeighbor,(Message) updateMessages.get(i), kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),newNeighbor)));
                            if(!updateMessages.isEmpty())
                                node.setTimer(newNeighbor);
                        }
                    }
                    
                    else {
                        TIntObjectHashMap updateList = new TIntObjectHashMap();
                        TIntObjectHashMap withdrawnsList = new TIntObjectHashMap();
                        RIBV2 rib = (RIBV2) node.getRib();
                        int IID = message.getIID();
                        int affectedNeighor = message.getAffectedNeighbor();
                        if(IID==Values.ReachGain) {
                            node.addNeighbor(affectedNeighor);
                            TLinkedList updateMessages = prepareTableTransfer(affectedNeighor);
                            for(int i=0;i<updateMessages.size();i++)
                                //schedule message arrivals at affected neighbor here
                                kernel.schedule(new ArrivalEventV2(affectedNeighor,(Message) updateMessages.get(i), kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),affectedNeighor)));
                            node.setTimer(affectedNeighor);
                        }
                        if(IID==Values.ReachLoss) {
                            node.removeNeighbor(affectedNeighor);
                            TIntArrayList keys = rib.getAdjRIBInKeys(affectedNeighor);
                            TIntArrayList entries = (TIntArrayList) keys.clone();
                            for(int i=0;i<entries.size();i++) {
                                int prefix = entries.get(i);
                                if(rib.getBestPathNextHop(prefix)==affectedNeighor) {
                                    rib.removeEntry(prefix,affectedNeighor);
                                    System.out.print("\nwithwrawal prefix "+this.message.getWithdrawans()+" at node "+this.processor.getAS()+",  TIME:   "+kernel.getTime()+"\n");
                                    TIntObjectHashMap newBestPath = rib.decisionProcess(prefix,node.getAS());
                                    if(newBestPath!=null) {
                                        TIntArrayList toBeExportedTo = rib.exportedTO(rib.getBestPathNextHop(prefix),processor);
                                        updateList.put(prefix,toBeExportedTo);
                                    } else {
                                        TIntArrayList exportedTo = rib.exportedTO(affectedNeighor,processor);
                                        withdrawnsList.put(prefix,exportedTo);
                                    }
                                } else {
                                    rib.removeEntry(prefix,affectedNeighor);
                                    System.out.print("\nwithwrawal prefix "+this.message.getWithdrawans()+" at node "+this.processor.getAS()+",  TIME:   "+kernel.getTime()+"\n");
                                }
                                
                            }
                            TIntArrayList nodeAdjacences = node.getAdjacenes();
                            
                            for(int j=0;j<nodeAdjacences.size();j++) {
                                
                                TLinkedList updateMessages =getMessage(withdrawnsList,updateList,nodeAdjacences.get(j));
                                for(int k=0;k<updateMessages.size();k++) {
                                    //schedule arrival messages here                           
                                    kernel.schedule(new ArrivalEventV2(nodeAdjacences.get(j),(Message) updateMessages.get(k), kernel,GetTime.getNextScheduleArrival(kernel,node.getAS(),nodeAdjacences.get(j))));
                                }
                            }
                            
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
                GRIBTNode clonedPath = GRIB.addNode(processor.getAS(),Path);
                message.setPath(clonedPath);
                result.add(message);
            }
        }
        return result;
    }
    
    private Message populateUpdateMessages(TIntArrayList withdrawanList) {
        
        Message result = new Message(Values.updateMessage,processor.getAS(),withdrawanList);
        
        return result;
    }
    private TLinkedList prepareTableTransfer(int TargetNode) {
        TIntObjectHashMap locRIB = ((RIBV2)(processor.getRib())).getLocRIB(TargetNode,processor.getAS());
        TIntArrayList attachments = processor.getAttachedNetworks();
        TLinkedList result = new TLinkedList();
        GRIBTNode path = GRIB.levelOne[processor.getAS()-1];
        if(!attachments.isEmpty()) {
            for(int i=0;i<attachments.size();i++) {
                result.add(new Message(Values.updateMessage,processor.getAS(),1,path,attachments.get(i),true));
            }
        }
        if(locRIB!=null) {
            TIntObjectIterator iterator = locRIB.iterator();
            for (int i = locRIB.size(); i-- > 0;){
                iterator.advance();
                int prefix = iterator.key();
                GRIBTNode Path = (GRIBTNode)iterator.value();
                GRIBTNode clonedPath = GRIB.addNode(processor.getAS(),Path);
                result.add(new Message(Values.updateMessage,processor.getAS(),1,clonedPath,prefix,true));
            }
        }
        return result;
    }
    private TIntArrayList diff(TIntArrayList first,TIntArrayList second) {
        TIntArrayList result = new TIntArrayList();
        for(int i=0;i<first.size();i++) {
            if(!second.contains(first.get(i)))
                result.add(first.get(i));
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

