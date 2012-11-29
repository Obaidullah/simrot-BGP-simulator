/*
 * Node.java
 *
 * Created on December 19, 2007, 12:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package SIMROT;

import gnu.trove.TIntArrayList;

/**
 *
 * @author ahmed
 */
public class Node {
    private int AS_NUM;
    private int num_Regions;
    private int node_Type;
    private TIntArrayList regions = new TIntArrayList();
    private TIntArrayList Providers = new TIntArrayList();
    private TIntArrayList Peers = new TIntArrayList();
    private TIntArrayList Customers = new TIntArrayList();
    //private boolean mustHaveTier1P = false;
    /** Creates a new instance of Node */
    public Node(int AS_NUM,int node_Type,TIntArrayList regions) {
        this.AS_NUM = AS_NUM;
        this.node_Type = node_Type;
        this.regions = regions;
        num_Regions = regions.size();
    }

    public void setNum_Regions(int num_Regions) {
        this.num_Regions = num_Regions;
    }

    public void setRegions(TIntArrayList regions) {
        this.regions = regions;
    }

    public int getAS_NUM() {
        return AS_NUM;
    }
    public int getNode_Type() {
        return node_Type;
    }

    public int getNum_Regions() {
        return num_Regions;
    }

    public TIntArrayList getRegions() {
        return regions;
    }
   public void addPeer(int peer)
   {
     Peers.add(peer);
   }
   public void addProvider(int provider)
   {
     Providers.add(provider);
   }
   public void addCustomer(int customer)
   {
     Customers.add(customer);
   }
   public boolean isPeer(int node)
   {
     if(Peers.contains(node))
         return true;
     else
         return false;
   }
   public boolean isProvider(int node)
   {
     if(Providers.contains(node))
         return true;
     else
         return false;
   }
   public boolean isCustomer(int node)
   {
     if(Customers.contains(node))
         return true;
     else
         return false;                 
   }
   public int getNodeDegree()
   {
     return Providers.size()+Peers.size()+Customers.size();
   }
   public TIntArrayList getCustomers() {
        return Customers;
    }

    public TIntArrayList getPeers() {
        return Peers;
    }

    public TIntArrayList getProviders() {
        return Providers;
    }
    public int getPeerDegree()
    {
      return Peers.size();
    }
}
