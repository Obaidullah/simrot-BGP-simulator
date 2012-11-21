/*
 * RIB.java
 *
 * Created on July 1, 2007, 7:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntIterator;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import gnu.trove.TLinkableAdapter;
import gnu.trove.TLinkedList;
import gnu.trove.TObjectHash;
import gnu.trove.TObjectIntHashMap;
import java.io.Serializable;

import java.util.Random;

/**
 * A class representing nodes' Adj_RIB-Ins and Loc_RIBs,
 * This class also encapsulate the decision process functionality for SPV and SPV+MRAI Protocols
 * @author ahmed
 */
public class RIB{
    protected TIntObjectHashMap RIB=new TIntObjectHashMap();
    protected myRIB rib = new myRIB();
    /** Creates a new instance of RIB */
    public RIB() {
    }
    /** Get a refernce to the unified RIB*/
    public TIntObjectHashMap getRIB() {
        return RIB;
    }
    /** Check if this peer Adj-RIB-In contains a route for this destination prefix
     *
     */
    public boolean entryExist(int prefix,int peer) {
        if(rib.RIBsIndex.contains(peer)) {
            AdjRIBIn ribIn = (AdjRIBIn) rib.listOfRIBS.get(rib.RIBsIndex.indexOf(peer));
            if(ribIn.indexes.contains(prefix))
                return true;
            else
                return false;
        } else
            return false;
    }
    /** Check if the passed entry is already existing in the Adj-RIB=In of this peer
     *
     */
    public boolean sameEntry(int prefix,int peer,GRIBTNode path) {
        AdjRIBIn ribIn = (AdjRIBIn) rib.listOfRIBS.get(rib.RIBsIndex.indexOf(peer));
        RouteEntry entry = (RouteEntry) ribIn.RouteEntries.get(ribIn.indexes.indexOf(prefix));
        if(entry.path == path)
            return true;
        else
            return false;
    }
    /** Check if we have path for this destination prefix
     */
    public boolean havePath(int prefix) {
        boolean havePath = false;
        for(int i=0;i<rib.listOfRIBS.size();i++) {
            AdjRIBIn ribIn = (AdjRIBIn) rib.listOfRIBS.get(i);
            if(ribIn.indexes.contains(prefix)) {
                havePath = true;
                break;
            }
        }
        return havePath;
    }
    /** Check if the path announced by this peer is selected as the best path
     */
    public boolean bestPath(int prefix,int peer) {
        AdjRIBIn ribIn = (AdjRIBIn) rib.listOfRIBS.get(rib.RIBsIndex.indexOf(peer));
        RouteEntry entry = (RouteEntry)ribIn.RouteEntries.get(ribIn.indexes.indexOf(prefix));
        if(entry.selected)
            return true;
        else
            return false;
    }
    /** Return the best path for this destination prefix
     *
     */
    public TIntObjectHashMap getBestPath(int prefix) {
        TIntObjectHashMap bestPath = new TIntObjectHashMap();
        for(int i=0;i<rib.RIBsIndex.size();i++) {
            AdjRIBIn ribIn = (AdjRIBIn) rib.listOfRIBS.get(i);
            if(ribIn.indexes.contains(prefix)) {
                RouteEntry entry = (RouteEntry)(ribIn.RouteEntries.get(ribIn.indexes.indexOf(prefix)));
                if(entry.selected) {
                    bestPath.put(ribIn.peer,entry.path);
                    break;
                }
            }
        }
        return bestPath;
    }
    protected TIntObjectHashMap getCandidates(int prefix) {
        TIntObjectHashMap candidates = new TIntObjectHashMap();
        for(int i=0;i<rib.listOfRIBS.size();i++) {
            AdjRIBIn ribIn = (AdjRIBIn)(rib.listOfRIBS.get(i));
            if(ribIn.indexes.contains(prefix)) {
                candidates.put(ribIn.peer,ribIn.RouteEntries.get(ribIn.indexes.indexOf(prefix)));
            }
        }
        return candidates;
    }
    /**
     * Initialize all Adj-RIB-In entries associated with this destination prefix for the decsision process
     */
    protected TIntObjectHashMap initializeForDecisionProcess(int prefix) {
        TIntObjectHashMap entries = getCandidates(prefix);
        TIntObjectIterator iterator = entries.iterator();
        for (int i = entries.size(); i-- > 0;) {
            iterator.advance();
            //int neighbor = iterator.key();
            ((RouteEntry)iterator.value()).selected = false;
        }
        return entries;
    }
    /**
     *  Returns the AS-PATH of the selected best path for the respective prefix
     */
    public GRIBTNode getBestPathVector(int prefix) {
        GRIBTNode bestPath = null;
        for(int i=0;i<rib.RIBsIndex.size();i++) {
            AdjRIBIn ribIn = (AdjRIBIn) rib.listOfRIBS.get(i);
            if(ribIn.indexes.contains(prefix)) {
                RouteEntry entry = (RouteEntry)(ribIn.RouteEntries.get(ribIn.indexes.indexOf(prefix)));
                if(entry.selected) {
                    bestPath = entry.path;
                    break;
                }
            }
        }
        return bestPath;
    }
    
    protected RouteEntry getEntry(int peer,int prefix) {
        AdjRIBIn ribIn = (AdjRIBIn)(rib.listOfRIBS.get(rib.RIBsIndex.indexOf(peer)));
        return (RouteEntry)(ribIn.RouteEntries.get(ribIn.indexes.indexOf(prefix)));
    }
    /**
     * Returns the next-hop AS of the selected best path for the respective prefix
     */
    public int getBestPathNextHop(int prefix) {
        int nextHop = 0;
        for(int i=0;i<rib.RIBsIndex.size();i++) {
            AdjRIBIn ribIn = (AdjRIBIn) rib.listOfRIBS.get(i);
            if(ribIn.indexes.contains(prefix)) {
                RouteEntry entry = (RouteEntry)(ribIn.RouteEntries.get(ribIn.indexes.indexOf(prefix)));
                if(entry.selected) {
                    nextHop = ribIn.peer;
                    break;
                }
            }
        }
        return nextHop;
    }
    /**
     * Add new entry to the Adj-RIB-In of this peer
     *
     */
    public void addEntry(int prefix,int peer,GRIBTNode path) {
    	if(rib.RIBsIndex.contains(peer)) {
            AdjRIBIn ribIn = (AdjRIBIn)(rib.listOfRIBS.get(rib.RIBsIndex.indexOf(peer)));
            RouteEntry entry = new RouteEntry(prefix,path,false);
            ribIn.RouteEntries.add(entry);
            ribIn.indexes.add(prefix);
            GRIB.increaseRefCount(path);
        } else {
            rib.RIBsIndex.add(peer);
            AdjRIBIn ribIn = new AdjRIBIn();
            ribIn.peer = peer;
            ribIn.indexes.add(prefix);
            RouteEntry entry = new RouteEntry(prefix,path,false);
            ribIn.RouteEntries.add(entry);
            rib.listOfRIBS.add(ribIn);
            GRIB.increaseRefCount(path);
        }
    }
    /**
     * Replace an existing entry
     */
    public void replaceEntry(int prefix,int peer,GRIBTNode path,boolean selected) {
        AdjRIBIn ribIn = (AdjRIBIn)(rib.listOfRIBS.get(rib.RIBsIndex.indexOf(peer)));
        RouteEntry entry = new RouteEntry(prefix,path,selected);
        ribIn.RouteEntries.remove(ribIn.indexes.indexOf(prefix));
        ribIn.RouteEntries.add(ribIn.indexes.indexOf(prefix),entry);
        GRIB.increaseRefCount(path);
    }
    /**
     * Remove this entry
     */
    public void removeEntry(int prefix,int peer) {
        if(entryExist(prefix,peer)) {
            AdjRIBIn ribIn = (AdjRIBIn) rib.listOfRIBS.get(rib.RIBsIndex.indexOf(peer));
            GRIB.decreaseRefCount(((RouteEntry)ribIn.RouteEntries.get(ribIn.indexes.indexOf(prefix))).path);
            int index = ribIn.indexes.indexOf(prefix);
            ribIn.RouteEntries.remove(index);
            ribIn.indexes.remove(index);
            
            if(ribIn.RouteEntries.isEmpty()) {
                int peerIndex = rib.RIBsIndex.indexOf(peer);
                rib.listOfRIBS.remove(peerIndex);
                rib.RIBsIndex.remove(peerIndex);
            }
        }
    }
    /**
     *   Deterministic Tie-Breaker
     */
    protected int mix(int a, int b, int c) {
        a=a-b;  a=a-c;  a=a^(c >>> 13);
        b=b-c;  b=b-a;  b=b^(a << 8);
        c=c-a;  c=c-b;  c=c^(b >>> 13);
        a=a-b;  a=a-c;  a=a^(c >>> 12);
        b=b-c;  b=b-a;  b=b^(a << 16);
        c=c-a;  c=c-b;  c=c^(b >>> 5);
        a=a-b;  a=a-c;  a=a^(c >>> 3);
        b=b-c;  b=b-a;  b=b^(a << 10);
        c=c-a;  c=c-b;  c=c^(b >>> 15);
        return c;
    }
    /**
     * Run the route selection process
     *
     */
    public TIntObjectHashMap decisionProcess(int prefix,int myAS) {
        TIntObjectHashMap result = new TIntObjectHashMap();
        TIntObjectHashMap candidates = initializeForDecisionProcess(prefix);
        TIntObjectIterator iterator = candidates.iterator();
        GRIBTNode path;
        TIntObjectHashMap tieBreakCandidates = new TIntObjectHashMap();
        int nextPeer;
        iterator.advance();
        int peer=iterator.key();
        path = ((RouteEntry)iterator.value()).path;
        tieBreakCandidates.put(peer,path);
        while (iterator.hasNext()){
            iterator.advance();
            nextPeer = iterator.key();
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
    }
    protected TIntArrayList getPrefixes() {
        TIntArrayList result = new TIntArrayList();
        for(int i=0;i<rib.listOfRIBS.size();i++) {
            AdjRIBIn ribIn = (AdjRIBIn) rib.listOfRIBS.get(i);
            for(int j=0;j<ribIn.indexes.size();j++) {
                if(!(result.contains(ribIn.indexes.get(j)))) {
                    result.add(ribIn.indexes.get(j));
                }
            }
        }
        return result;
    }
    /**
     *  Returns the loc-RIB of this node
     */
    public TIntObjectHashMap getLocRIB() {
        TIntObjectHashMap locRIB=new TIntObjectHashMap();
        TIntArrayList prefixes = getPrefixes();
        for(int i=0;i<prefixes.size();i++) {
            locRIB.put(prefixes.get(i),getBestPathVector(prefixes.get(i)));
        }
        return locRIB;
    }
    /**
     *  Returns a list of prefixes contains all prefixes learned from this neighbor
     */
    public TIntArrayList getAdjRIBInKeys(int peer) {
        TIntArrayList entries = new TIntArrayList();
        if(rib.RIBsIndex.contains(peer))
        {    
        AdjRIBIn ribIn = (AdjRIBIn)(rib.listOfRIBS.get(rib.RIBsIndex.indexOf(peer)));
        entries = ribIn.indexes;
        }
        return entries;
    }
    public void removeAdjRIBIn(int peer)
    {
      if(rib.RIBsIndex.contains(peer))
        {
          AdjRIBIn ribIn = (AdjRIBIn) rib.listOfRIBS.get(rib.RIBsIndex.indexOf(peer));
          TIntArrayList indexes = ribIn.indexes;
          for(int i=0;i<indexes.size();i++)
          {
            GRIB.decreaseRefCount(((RouteEntry)ribIn.RouteEntries.get(i)).path);
          }
          int index = rib.RIBsIndex.indexOf(peer);
          rib.listOfRIBS.remove(index);
          rib.RIBsIndex.remove(index);
        }
      
    }
    /**
     *  Returns a string representation for the RIB
     */
    public String toString() {
        String result = "";
        TIntArrayList prefixes = getPrefixes();
        prefixes.sort();
        for(int i=0;i<prefixes.size();i++) {
            result+="\tprefix "+prefixes.get(i)+"\n";  //Modified this line: the original is: result+="prefix "+prefixes.get(i)+"\n";
            for(int j=0;j<rib.listOfRIBS.size();j++) {
                AdjRIBIn ribIn = (AdjRIBIn)(rib.listOfRIBS.get(j));
                if(ribIn.indexes.contains(prefixes.get(i))) {
                    RouteEntry entry = (RouteEntry)(ribIn.RouteEntries.get(ribIn.indexes.indexOf(prefixes.get(i))));
                    result+="\t\t"+ribIn.peer+"\t"+entry.toString()+"\n";  //Modified this line: the original is: result+=ribIn.peer+"\t"+entry.toString()+"\n";
                }
            }
        }
        return result;
    }
    
    protected void populateRIB(int prefix,TIntArrayList extraNetworks)
    {
       for(int i=0;i<rib.listOfRIBS.size();i++)
       {
         AdjRIBIn RIBIn = (AdjRIBIn)(rib.listOfRIBS.get(i));  
         if(RIBIn.indexes.contains(prefix))
         {
           RouteEntry entry = (RouteEntry)(RIBIn.RouteEntries.get(RIBIn.indexes.indexOf(prefix)));
           for(int j=0;j<extraNetworks.size();j++)
           {
             RouteEntry newEntry = new RouteEntry(extraNetworks.get(j),entry.path,entry.selected);
             RIBIn.RouteEntries.add(newEntry);
             RIBIn.indexes.add(extraNetworks.get(j));
           }
         }
       }
    }
    
    /**
     *  This class represents route-entries in the RIB
     */
    protected class myRIB{
        public myRIB(){}
        public TLinkedList listOfRIBS = new TLinkedList();
        public TIntArrayList RIBsIndex = new TIntArrayList();
        public TLinkedList listOfRIBSOut = new TLinkedList();
        public TIntArrayList RIBSOutIndex = new TIntArrayList();
    }
    protected class RouteEntry extends TLinkableAdapter{
        public int prefix;
        //public TIntArrayList path;
        public GRIBTNode path;
        public boolean selected;
        public RouteEntry(int prefix,GRIBTNode path,boolean selected) {
            this.path = path;
            this.selected = selected;
            this.prefix = prefix;
        }
        public String toString() {
            return "Path ="+GRIB.getPath(path)+" selected = "+selected;
        }
    }
    protected class AdjRIBIn extends TLinkableAdapter{
        public AdjRIBIn() {
        }
        public TIntArrayList indexes = new TIntArrayList();
        public int peer;
        public TLinkedList RouteEntries = new TLinkedList();
    }
   protected class AdjRIBOut extends TLinkableAdapter
   {
     public AdjRIBOut(int peer)
     {
         this.peer = peer;
     }
     public int peer;
     public TIntArrayList indexes = new TIntArrayList();
   }
    
}
