/*
 * Processor.java
 *
 * Created on August 20, 2007, 2:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import gnu.trove.TLinkedList;

/**
 *
 * @author ahmed
 */
public class Processor {
    
    /** Creates a new instance of Processor */
    private MessageV2 message=new MessageV2();
    private RIBV2 rib = null;
    public Processor() {
    }
    
    public void updateMessageHandler(MessageV2 message) {
        this.message = message;
        int src = message.getSrc();
        int dst = message.getDst();
        rib = (RIBV2) Topology.getNodeByAddressV2(dst).getRib();
        TIntObjectHashMap withd = new TIntObjectHashMap();
        TIntObjectHashMap upds = new TIntObjectHashMap();
        int peer = message.getSrc();
        
        if(message.getWithdrawanLength()>0) {
            TIntArrayList withdrawns = message.getWithdrawans();
            TIntArrayList myNW = Topology.getNodeByAddressV2(dst).getAttachedNetworks();
            for(int i=0;i<withdrawns.size();i++) {
                
                if(!myNW.contains(withdrawns.get(i))) {
                    int bestPathNextHop = rib.getBestPathNextHop(withdrawns.get(i));
                    TIntArrayList exportedTo = rib.exportedTO(bestPathNextHop,dst);
                    rib.removeEntry(withdrawns.get(i),peer);
                    if(bestPathNextHop == peer) {
                        if(rib.havePath(withdrawns.get(i))) {
                            rib.decisionProcess(withdrawns.get(i),dst);
                            GRIBTNode bestPath = rib.getBestPathVector(withdrawns.get(i));
                            int bPathNextHop = rib.getBestPathNextHop(withdrawns.get(i));
                            TIntArrayList toBeExportedTo = rib.exportedTO(bPathNextHop,dst);
                            TIntArrayList result = diff(exportedTo,toBeExportedTo);
                            if(!result.isEmpty())
                                withd.put(withdrawns.get(i),result);
                            upds.put(withdrawns.get(i),toBeExportedTo);
                            
                        } else {
                            withd.put(withdrawns.get(i),exportedTo);
                        }
                    }
                }
            }
        }
        if(message.getPathAttrLength()>0) {
            int prefix = message.getAnnouncedPrefix();
            GRIBTNode path = message.getPath();
            src = message.getSrc();
            int newBestPathNextHop=0;
            if(GRIB.isValidPath(dst,path)) {
                if(rib.havePath(prefix)) {
                    GRIBTNode oldBestPath = rib.getBestPathVector(prefix);
                    int oldPathNextHop = rib.getBestPathNextHop(prefix);
                    if(rib.entryExist(prefix,src)) {
                        rib.replaceEntry(prefix,src,path,false);
                    } else {
                        rib.addEntry(prefix,src,path);
                    }
                    rib.decisionProcess(prefix,dst);
                    GRIBTNode newBestPath = rib.getBestPathVector(prefix);
                    newBestPathNextHop = rib.getBestPathNextHop(prefix);
                    if(!(newBestPath.equals(oldBestPath))) {
                        TIntArrayList exportedTo = new TIntArrayList();
                        exportedTo=rib.exportedTO(oldPathNextHop,dst);    
                        TIntArrayList toBeExportedTo = rib.exportedTO(newBestPathNextHop,dst);
                        TIntArrayList diff = diff(exportedTo,toBeExportedTo);
                        if(!diff.isEmpty())
                            withd.put(prefix,diff);
                        upds.put(prefix,toBeExportedTo);
                    }
                    
                } else {
                    rib.addEntry(prefix,src,path);
                    rib.decisionProcess(prefix,dst);
                    TIntArrayList toBeExportedTo = rib.exportedTO(rib.getBestPathNextHop(prefix),dst);
                    upds.put(prefix,toBeExportedTo);
                }
            }
        }
        TIntArrayList nodeAdjacences = Topology.getStaticAdj(dst);
        for(int i=0;i<nodeAdjacences.size();i++) {
            
            if(!(upds.isEmpty()&&withd.isEmpty())) {
                TLinkedList updateMessages =  getMessage(withd,upds,nodeAdjacences.get(i));
                for(int j=0;j<updateMessages.size();j++)
                    MessageExecuter.queue.add((MessageV2) updateMessages.get(j));
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
            result.add(new MessageV2(Values.updateMessage,message.getDst(),targetNode,withdrawnsList));
        Iterator = upds.iterator();
        for(int j = upds.size(); j-- > 0;) {
            Iterator.advance();
            int Prefix = Iterator.key();
            TIntArrayList dst = (TIntArrayList) Iterator.value();
            if(dst.contains(targetNode)) {
                MessageV2 msg = new MessageV2(Values.updateMessage,message.getDst(),targetNode);
                msg.setPathAttrLength(1);
                msg.setAnnouncedPrefix(Prefix);
                GRIBTNode Path = rib.getBestPathVector(Prefix);
                GRIBTNode clonedPath = GRIB.addNode(message.getDst(),Path);
                msg.setPath(clonedPath);
                result.add(msg);
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
