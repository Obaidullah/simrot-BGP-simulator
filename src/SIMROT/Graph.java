/*
 * Graph.java
 *
 * Created on December 19, 2007, 12:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package SIMROT;

import com.sun.org.apache.xerces.internal.impl.xs.identity.ValueStore;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 *
 * @author ahmed
 */
public class Graph {
    private TIntObjectHashMap nodes_Per_Regions_M=new TIntObjectHashMap();
    private TIntObjectHashMap nodes_Per_Regions_CP = new TIntObjectHashMap();
    private TIntObjectHashMap nodes_Per_Regions_S = new TIntObjectHashMap();
    private TIntObjectHashMap nodes=new TIntObjectHashMap();
    private TIntArrayList regions = new TIntArrayList();

    /**
     * Creates a new instance of Graph
     * @param numberOfRegions The number of regions in the graph
     */
    public Graph(int numberOfRegions) {
       initRegions(numberOfRegions);
    }
    private void initRegions(int number_Of_Regions) {
        for(int i=1;i<=number_Of_Regions;i++) {
            nodes_Per_Regions_M.put(i,new TIntArrayList());
            nodes_Per_Regions_CP.put(i,new TIntArrayList());
            nodes_Per_Regions_S.put(i,new TIntArrayList());
            regions.add(i);
        }
    }
    public TIntArrayList getRegions() {
        return regions;
    }
    /**
     * Return all nodes of type nodeType that belong to region region
     * @param nodeType Node type
     * @param region Region id
     * @return all nodes of type nodeType that belong to region region
     */
    public TIntArrayList getNodesPerRegion(int nodeType,int region)
    {
      if(nodeType==Values.Middle)
      {
        return (TIntArrayList) nodes_Per_Regions_M.get(region);
      }
      else
      {
        if(nodeType==Values.CP)
        {
          return (TIntArrayList) nodes_Per_Regions_CP.get(region);
        }
        else
        {
          return (TIntArrayList) nodes_Per_Regions_S.get(region);  
        }    
      }    
    }
    
    public Node getNode(int AS_Num)
    {
       return (Node) nodes.get(AS_Num);
    }
    public void addNode(Node node) {
        nodes.put(node.getAS_NUM(),node);
        if(node.getNode_Type()!=Values.Tier_1) {
            //TIntArrayList regions = node.getRegions();
            for(int i=0;i<regions.size();i++) {
                addNodeToRegion(node.getAS_NUM(),node.getNode_Type(),regions.get(i));
            }
        }
    }
    private void addNodeToRegion(int AS_Num,int nodeType,int region) {
        if(nodeType==Values.Middle) {
            ((TIntArrayList)nodes_Per_Regions_M.get(region)).add(AS_Num);
        } else {
            if(nodeType==Values.CP) {
                ((TIntArrayList)nodes_Per_Regions_CP.get(region)).add(AS_Num);
            } else {
                ((TIntArrayList)nodes_Per_Regions_S.get(region)).add(AS_Num);
            }
        }
    }
  public boolean isInMyCustomerTree(int node_A,int node_B)
  {
    int node_A_Type = getNode(node_A).getNode_Type();   
    int node_B_Type = getNode(node_B).getNode_Type();   
    if(node_A_Type==Values.Middle && node_B_Type==Values.CP)
    {
      return false;
    }
    else
    {    
    if(node_A_Type==Values.CP && node_B_Type == Values.CP)
    {
     return false;
    }
    else
    {    
    boolean result = false;
    TIntArrayList B_Customers = ((Node)nodes.get(node_B)).getCustomers();
    if(B_Customers.contains(node_A))
    {
      return true;
    } 
    else
    {    
    for(int i=0;i<B_Customers.size();i++)
    {        
        if(isInMyCustomerTree(node_A,B_Customers.get(i)))
        {
          return true;
        }
    }
    return result;
    }
    
    }
    }
  }

  public void saveGraph(String outputFile) throws FileNotFoundException
  {
    FileOutputStream fout = new FileOutputStream(outputFile);
    PrintStream ps = new PrintStream(fout);
    ps.println(nodes.size());
    for(int i=1;i<=nodes.size();i++)
    {
     TIntArrayList providers = ((Node)nodes.get(i)).getProviders();
     TIntArrayList customers = ((Node)nodes.get(i)).getCustomers();
     TIntArrayList peers = ((Node)nodes.get(i)).getPeers();
     String toPrint="";
     for(int j=0;j<providers.size();j++)
     {
       toPrint=toPrint+providers.get(j)+"\t0\t";
     }
     for(int j=0;j<peers.size();j++)
     {
       toPrint=toPrint+peers.get(j)+"\t1\t";
     }
     for(int j=0;j<customers.size();j++)
     {
       toPrint=toPrint+customers.get(j)+"\t3\t";
     }
     ps.println(toPrint);
    }
  }

  public void saveGraphForamt2(String outputFile) throws FileNotFoundException
  {
    FileOutputStream fout = new FileOutputStream(outputFile);
    PrintStream ps = new PrintStream(fout);
    ps.println(nodes.size());
    for(int i=1;i<=nodes.size();i++)
    {
     TIntArrayList providers = ((Node)nodes.get(i)).getProviders();
     TIntArrayList customers = ((Node)nodes.get(i)).getCustomers();
     TIntArrayList peers = ((Node)nodes.get(i)).getPeers();
     for(int j=0;j<providers.size();j++)
     {

       ps.println(i+"\t"+providers.get(j)+"\t0");
     }
     for(int j=0;j<peers.size();j++)
     {

       ps.println(i+"\t"+peers.get(j)+"\t1");
     }
     for(int j=0;j<customers.size();j++)
     {

       ps.println(i+"\t"+customers.get(j)+"\t3");
     }
    }
  }
}
