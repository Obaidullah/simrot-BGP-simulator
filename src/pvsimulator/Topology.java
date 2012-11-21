/*
 * Topology.java
 *
 * Created on June 29, 2007, 12:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TLinkedList;
import gnu.trove.TObjectIntHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.Scanner;

/**
 *  This class represents the underlying network topology
 * @author ahmed
 */
public class Topology {
    
    /** Creates a new instance of Topology */
    public Topology() {
    }
    public static boolean connected(int first_node,int second_node) {
        return true;
    }
    private static List<Node> nodes = new ArrayList<Node>();
    private static List<NodeV2> nodesV2 = new ArrayList<NodeV2>();
    public static TIntObjectHashMap adjacencyList = new TIntObjectHashMap();
    public static TIntObjectHashMap relations = new TIntObjectHashMap();
    public static boolean withPolicies = true;
    public static List<Integer> indexes = new ArrayList<Integer>();
    public static TIntIntHashMap prefixToNodeLookup = new TIntIntHashMap();
    private static TIntObjectHashMap latency = new TIntObjectHashMap();
    public static TIntArrayList activityList = new TIntArrayList();
    public static int numberOfNodes = 0;
    public static boolean partialConverging = false;
    /** Get a reference to a node with the specified address (SPV)
     *  @param address: Node address
     *  @return A refernce to the node
     */
    public static Node getNodeByAddress(int address) {
        return nodes.get(address-1);
    }
    /** Get a reference to a node with the specified address (SPV+MRAI,SPV+MRAI+policies)
     *  @param address: Node address
     *  @return A refernce to the node
     */
    public static NodeV2 getNodeByAddressV2(int address) {
        return nodesV2.get(address-1);
    }
    /**
     *  Returns the buisness relationship between these two nodes
     */
    public static int getRelation(int firstNode,int SecondNode) {
        TIntIntHashMap value = (TIntIntHashMap) relations.get(firstNode);
        return value.get(SecondNode);
    }
    /**
     * Returns the network delay between these two nodes
     */
    public static double getDelay(int first_Node,int second_Node) {
        return ((TIntDoubleHashMap)latency.get(first_Node)).get(second_Node);
    }
    /**
     *  Add new node to the network (SPV)
     */
    public static void addNode(int address,Node node) {
        nodes.add(address-1,node);
    }
    /**
     *  Add new node to the network (SPV+MRAI,SPV+MRAI+policies)
     */
    public static void addNodeV2(int address,NodeV2 node) {
        nodesV2.add(address-1,node);
    }
    /** Get the list of the adjacences for the specified node (SPV)
     *  @param address the address of the node
     *  @return The adjacency list
     *
     */
    public static TIntArrayList getNodeAdj(int address) {
        TIntArrayList result = new TIntArrayList();
        TIntArrayList staticAdjacency = (TIntArrayList)adjacencyList.get(address-1);
        for(int i=0;i<staticAdjacency.size();i++) {
            if(getNodeByAddress(staticAdjacency.get(i)).isInitialized())
                result.add(staticAdjacency.get(i));
        }
        return result;
    }
    /** Get the list of the adjacences for the specified node (SPV+MRAI,SPV+MRAI+policies)
     *  @param address the address of the node
     *  @return The adjacency list
     *
     */
    public static TIntArrayList getNodeAdjV2(int address) {
        TIntArrayList result = new TIntArrayList();
        TIntArrayList staticAdjacency = (TIntArrayList)adjacencyList.get(address-1);
        for(int i=0;i<staticAdjacency.size();i++) {
            if(getNodeByAddressV2(staticAdjacency.get(i)).isInitialized())
                result.add(staticAdjacency.get(i));
        }
        return result;
    }
    /**
     *  Returns the adjacency information directly from the AS graph irrespect to the liveness of the nodes
     */
    public static TIntArrayList getStaticAdj(int address) {
        TIntArrayList staticAdjacency = (TIntArrayList)adjacencyList.get(address-1);
        return staticAdjacency;
    }
    /**
     *  Parse the topology file (SPV)
     *
     */
    public static void parseTopology(String file,boolean withPolicies) {
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            
            numberOfNodes = Integer.parseInt(br.readLine());
            System.out.println(numberOfNodes);
            for(int i=1;i<numberOfNodes+1;i++) {
                TIntArrayList attachedNetworks = new TIntArrayList();
                Scanner scr = new Scanner(br.readLine());
                while(scr.hasNext()) {
                    int network = scr.nextInt();
                    if(activityList.contains(network)) {
                        attachedNetworks.add(network);
                    }
                }
                addNode(i,new Node(attachedNetworks,i));
                scr.close();
            }
            if(!withPolicies) {
                for(int j=1;j<numberOfNodes+1;j++) {
                    TIntArrayList neighbors = new TIntArrayList();
                    Scanner scr = new Scanner(br.readLine());
                    while(scr.hasNext()) {
                        int neighbor = scr.nextInt();
                        neighbors.add(neighbor);
                    }
                    adjacencyList.put(j-1,neighbors);
                    scr.close();
                }
            } else {
                for(int j=1;j<numberOfNodes+1;j++) {
                    TIntArrayList neighbors = new TIntArrayList();
                    Scanner scr = new Scanner(br.readLine());
                    TIntIntHashMap tempMap = new TIntIntHashMap();
                    while(scr.hasNext()) {
                        int neighbor = scr.nextInt();
                        int relation = scr.nextInt();
                        tempMap.put(neighbor,relation);
                        neighbors.add(neighbor);
                    }
                    adjacencyList.put(j-1,neighbors);
                    relations.put(j,tempMap.clone());
                    scr.close();
                }
            }
            br.close();
        }catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    /**
     *  Parse the topology file (SPV+MRAI,SPV+MRAI+policies)
     *
     */
    public static void parseTopologyV2(String file,boolean withPolicies) {
        try{
            resetAll();
            BufferedReader br = new BufferedReader(new FileReader(file));
            
            numberOfNodes = Integer.parseInt(br.readLine());
            System.out.println(numberOfNodes);
            for(int i=1;i<numberOfNodes+1;i++) {
                TIntArrayList attachedNetworks = new TIntArrayList();
                TIntArrayList restAttachements = new TIntArrayList();
                Scanner scr = new Scanner(br.readLine());
                int prefixCount = 0;
                while(scr.hasNext()) {
                    int network = scr.nextInt();
                    if(partialConverging) {
                        if(activityList.contains(network)) {
                            if(prefixCount>0) {
                                restAttachements.add(network);
                            } else
                                attachedNetworks.add(network);
                            ++prefixCount;
                        }
                    } else {
                        if(prefixCount>0) {
                            restAttachements.add(network);
                        } else
                            attachedNetworks.add(network);
                        ++prefixCount;
                        
                    }
                }
                addNodeV2(i,new NodeV2(attachedNetworks,restAttachements,i));
                
                scr.close();
            }
            if(!withPolicies) {
                for(int j=1;j<numberOfNodes+1;j++) {
                    TIntArrayList neighbors = new TIntArrayList();
                    Scanner scr = new Scanner(br.readLine());
                    while(scr.hasNext()) {
                        int neighbor = scr.nextInt();
                        neighbors.add(neighbor);
                    }
                    adjacencyList.put(j-1,neighbors);
                    scr.close();
                }
            } else {
                Topology.withPolicies = true;
                for(int j=1;j<numberOfNodes+1;j++) {
                    TIntArrayList neighbors = new TIntArrayList();
                    Scanner scr = new Scanner(br.readLine());
                    TIntIntHashMap tempMap = new TIntIntHashMap();
                    while(scr.hasNext()) {
                        int neighbor = scr.nextInt();
                        int relation = scr.nextInt();
                        tempMap.put(neighbor,relation);
                        neighbors.add(neighbor);
                    }
                    adjacencyList.put(j-1,neighbors);
                    relations.put(j,tempMap.clone());
                    scr.close();
                }
                
            }
            br.close();
        }catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     *  Parse the latency information
     */
    public static void parseLatency(String file) {
        for(int i=1;i<=numberOfNodes;i++) {
            latency.put(i,new TIntDoubleHashMap());
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String str="";
            while((str=reader.readLine())!=null) {
                Scanner scr = new Scanner(str);
                
                int node1 = scr.nextInt();
                int node2 = scr.nextInt();
                double latencyVal = scr.nextDouble();
                
                
                ((TIntDoubleHashMap)latency.get(node1)).put(node2,latencyVal);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void fillRIBs() {
        for(int i=1;i<=numberOfNodes;i++) {
            NodeV2 node = getNodeByAddressV2(i);
            if(!node.getAttachedNetworks().isEmpty()) {
                int prefix = node.getAttachedNetworks().get(0);
                TIntArrayList restAttachements = node.getRestAttachements();
                if(!restAttachements.isEmpty()) {
                    for(int j=1;j<=numberOfNodes;j++) {
                        if(i!=j) {
                            NodeV2 theOtherNode = getNodeByAddressV2(j);
                            theOtherNode.getRib().populateRIB(prefix,restAttachements);
                        }
                    }
                }
                node.combineAttachements();
            }
        }
    }
    
    public static void initializeAll(Kernel kernel) {
        for(int i=1;i<=numberOfNodes;i++) {
            NodeV2 node = getNodeByAddressV2(i);
            node.setKernel(kernel);
            node.setAdjacenes(getStaticAdj(i));
            node.initializeOutputQueue();
            node.initializeTimers();
            node.setMonitor(new MessagesMonitor(i));
            ((RIBV2)node.getRib()).populateExportedTo(i);
        }
    }
     public static void initializeAllV2(Kernel kernel) {
        for(int i=1;i<=numberOfNodes;i++) {
            NodeV2 node = getNodeByAddressV2(i);
            node.setKernel(kernel);
            node.setAdjacenes(getStaticAdj(i));
            node.initializeOutputQueue();
            node.initializeTimers();
        }
    }
    private static void resetAll()
    {
      nodesV2.clear();
      relations.clear();
      adjacencyList.clear();
      numberOfNodes=0;
    }
}
