/*
 * RIBV2.java
 *
 * Created on July 18, 2007, 3:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import com.sun.org.apache.xerces.internal.impl.xs.identity.ValueStore;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import gnu.trove.TIntArrayList;
import gnu.trove.TLinkedList;

/**
 * A class representing nodes' Adj_RIB-Ins and Loc_RIBs,
 * This class also encapsulate the decision process functionality for the SPV+MRAI+policies protocol
 * @author ahmed
 */
public class RIBV2 extends RIB{
    
    /** Creates a new instance of RIBV2 */
    public RIBV2() {
        
    }
    /**
     * Run the route selection process
     *
     */
    public TIntObjectHashMap decisionProcess(int prefix,int myAS) {
        
        TIntObjectHashMap result = new TIntObjectHashMap();
        if(havePath(prefix)) {
            TIntObjectHashMap candidates = initializeForDecisionProcess(prefix);
            TIntObjectIterator iterator = candidates.iterator();
            GRIBTNode path;
            TIntObjectHashMap tieBreakCandidates = new TIntObjectHashMap();
            int nextPeer;
            iterator.advance();
            int peer=iterator.key();
            path = ((RouteEntry)iterator.value()).path;
            int relation = Topology.getRelation(myAS,peer);
            tieBreakCandidates.put(peer,path);
            while (iterator.hasNext()){
                iterator.advance();
                nextPeer = iterator.key();
                int nextPeerRelation = Topology.getRelation(myAS,nextPeer);
                if(nextPeerRelation>relation) {
                    tieBreakCandidates.clear();
                    path =((RouteEntry)iterator.value()).path;
                    peer = nextPeer;
                    relation = nextPeerRelation;
                    tieBreakCandidates.put(peer,path);
                } else {
                    if(relation==nextPeerRelation) {
                        if(GRIB.getASPathLength(((RouteEntry)iterator.value()).path)< GRIB.getASPathLength(path)) {
                            tieBreakCandidates.clear();
                            path = ((RouteEntry)iterator.value()).path;
                            peer = nextPeer;
                            tieBreakCandidates.put(peer,path);
                        } else {
                            GRIBTNode thisPath = ((RouteEntry)candidates.get(nextPeer)).path;
                            if(GRIB.getASPathLength(thisPath)==GRIB.getASPathLength(path)) {
                                tieBreakCandidates.put(nextPeer,thisPath);
                            }
                        }
                    }
                }
            }
            if(tieBreakCandidates.size()>1) {
                TIntObjectIterator iter = tieBreakCandidates.iterator();
                iter.advance();
                peer = iter.key();
                path = (GRIBTNode)iter.value();
                int temp;
                int tempHash;
                int hash = mix(myAS,peer,0);
                while(iter.hasNext()){
                    iter.advance();
                    temp = iter.key();
                    tempHash = mix(myAS,temp,0);
                    if(tempHash<hash) {
                        hash = tempHash;
                        peer = temp;
                        path = (GRIBTNode)iter.value();
                    }
                }
            }
            getEntry(peer,prefix).selected = true;
            result.put(peer,path);
            return result;
        } else
            return null;
    }
    /**
     *  Check If the route characterized by this next-hop is exportable to the respective neighbor based on policies
     */
    public  boolean export(int bestPathNextHop,int neighbor,int myAS) {
        boolean export = false;
        int srcRelation = Topology.getRelation(myAS,bestPathNextHop);
        int dstRelation = Topology.getRelation(myAS,neighbor);
        if(srcRelation==Values.customer || srcRelation == Values.siblings)
            export = true;
        else {
            if(srcRelation == Values.provider && dstRelation == Values.peer)
                export = false;
            else {
                if(srcRelation == Values.peer && dstRelation == Values.provider)
                    export = false;
                else {
                    if(dstRelation == Values.customer || dstRelation == Values.siblings)
                        export = true;
                    
                }
            }
        }
        return export;
    }
    /**
     *  Returns a list of neighbors which were eligible of receiving an announcement for the respective route
     */
    public TIntArrayList exportedTO(int bestPathNextHop,int node) {
        TIntArrayList result = new TIntArrayList();
        TIntArrayList neighbors = Topology.getStaticAdj(node);
        for(int i=0;i<neighbors.size();i++) {
            if(bestPathNextHop!=neighbors.get(i)) {
                if(export(bestPathNextHop,neighbors.get(i),node))
                    result.add(neighbors.get(i));
            }
        }
        
        return result;
    }
    /**
     *  Returns a list of neighbors which were eligible of receiving an announcement for the respective route
     */
    public TIntArrayList exportedTO(int bestPathNextHop,NodeV2 node) {
        TIntArrayList result = new TIntArrayList();
        TIntArrayList neighbors = node.getAdjacenes();
        int AS = node.getAS();
        for(int i=0;i<neighbors.size();i++) {
            if(bestPathNextHop!=neighbors.get(i)) {
                if(export(bestPathNextHop,neighbors.get(i),AS))
                    result.add(neighbors.get(i));
            }
        }
        return result;
    }
    /**
     *  Returns the loc-RIB of this node
     */
    public TIntObjectHashMap getLocRIB(int TargetNode,int myAS) {
        TIntObjectHashMap locRIB=new TIntObjectHashMap();
        TIntArrayList prefixes = getPrefixes();
        for(int i=0;i<prefixes.size();i++) {
            if(export(getBestPathNextHop(prefixes.get(i)),TargetNode,myAS))
                locRIB.put(prefixes.get(i),getBestPathVector(prefixes.get(i)));
        }
        
        return locRIB;
    }
    
    public TIntArrayList getExportedTo(int prefix) {
        TIntArrayList result = new TIntArrayList();
        TLinkedList RIBsOut = rib.listOfRIBSOut;
        for(int i=0;i<RIBsOut.size();i++) {
            AdjRIBOut RIBOut = (AdjRIBOut) RIBsOut.get(i);
            if(RIBOut.indexes.contains(prefix)) {
                result.add(RIBOut.peer);
            }
        }
        return result;
    }
    public void addToRIBOut(int peer,int prefix) {
        AdjRIBOut RIBOut = (AdjRIBOut) rib.listOfRIBSOut.get(rib.RIBSOutIndex.indexOf(peer));
        if(!RIBOut.indexes.contains(prefix))
            RIBOut.indexes.add(prefix);
    }
    public void removeFromRIBOut(int peer,TIntArrayList prefixes) {
        AdjRIBOut RIBOut = (AdjRIBOut) rib.listOfRIBSOut.get(rib.RIBSOutIndex.indexOf(peer));
        for(int i=0;i<prefixes.size();i++) {
            if(RIBOut.indexes.contains(prefixes.get(i)))
                RIBOut.indexes.remove(RIBOut.indexes.indexOf(prefixes.get(i)));
        }
    }
    public void populateExportedTo(int myAddress) {
        
        TIntArrayList prefixes = getPrefixes();
        TIntArrayList staticAdj = Topology.getStaticAdj(myAddress);
        for(int i=0;i<staticAdj.size();i++) {
            AdjRIBOut ribOut = new AdjRIBOut(staticAdj.get(i));
            rib.listOfRIBSOut.add(ribOut);
            rib.RIBSOutIndex.add(staticAdj.get(i));
        }
        for(int j=0;j<prefixes.size();j++) {
            int nextHop = getBestPathNextHop(prefixes.get(j));
            TIntArrayList exportedTo = exportedTO(nextHop,myAddress);
            for(int k=0;k<exportedTo.size();k++) {
                AdjRIBOut ribOut = (AdjRIBOut) rib.listOfRIBSOut.get(rib.RIBSOutIndex.indexOf(exportedTo.get(k)));
                ribOut.indexes.add(prefixes.get(j));
            }
        }
    }
}
