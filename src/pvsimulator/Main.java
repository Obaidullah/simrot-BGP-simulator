/*
 * Main.java
 *
 * Created on June 24, 2007, 11:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;


import gnu.trove.TIntArrayList;
import Helper.LinksLatencyCalculator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author ahmed
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
        
    }
    
    /**
     * @param args the command line arguments
     */
    private static void gc() {
        try {
            System.gc();
            Thread.currentThread().sleep(100);
            System.runFinalization();
            Thread.currentThread().sleep(100);
            System.gc();
            Thread.currentThread().sleep(100);
            System.runFinalization();
            Thread.currentThread().sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static long getUsedMemory() {
        gc();
        long totalMemory = Runtime.getRuntime().totalMemory();
        gc();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = totalMemory - freeMemory;
        return usedMemory;
    }
    public static void main(String[] args) throws Exception {
    	/* ADDED BY GIULIO */
    	Topology.parseTopologyV2(args[0],false);
    	for(int i=1;i<=Topology.numberOfNodes;i++)
        {
    		String nameOfFile="Node_"+i+"_.txt";
    		File dumpfiles = new File(nameOfFile);
            dumpfiles.delete();           
        }
    	File ribchanges = new File("NodeChangesAtExactTime.txt");
    	ribchanges.delete();
    	File finalRoutingTable = new File("NodeRoutingTable.txt");
    	finalRoutingTable.delete();
    	File event_generator = new File("generated_event.txt");
    	event_generator.delete();
    	/* THIS IS IT */
        long start = System.currentTimeMillis();
        long startingMemoryUse = getUsedMemory();
        System.out.println("SMemory="+startingMemoryUse);
        int protocolVersion = Integer.parseInt(args[8]);        
        if(protocolVersion==0) {
            GetTime.withRandom = true;
            ArrivalEventV2.dumpMessages = false;
            Topology.withPolicies = false;
            Kernel.withEvents = true;
            Kernel.withTimer = true;
            NodeV2.magicNumber = Integer.parseInt(args[7]);
            String eventFile = args[2];
            Kernel.eventFile = args[2];
            if(Integer.parseInt(args[5])==1)
                Topology.partialConverging = true;
            else
                Topology.partialConverging = false;
            System.out.println(Topology.partialConverging);
            if(Topology.partialConverging) {
                Topology.activityList = EventParser.getActivityList(eventFile,24*60*60);
                System.out.println("Length = "+Topology.activityList.size());
            }
            Topology.parseTopologyV2(args[0],false);
            System.out.println(Topology.numberOfNodes);
            for(int i=1;i<=Topology.numberOfNodes;i++)
                Topology.getNodeByAddressV2(i).setInitialized(true);
            GRIB.initializeRIB(Topology.numberOfNodes);
            MessageExecuterV2 executer = new MessageExecuterV2();
            Topology.fillRIBs();
            if(GetTime.withRandom)
                Topology.parseLatency(args[1]);
            NodeV2.MRAI = Double.parseDouble(args[6]);
            System.out.println(NodeV2.MRAI);
            long seed = Integer.parseInt(args[7])*10+1979;
            Kernel.numberofNodes = Topology.numberOfNodes;
            Kernel kernel = new Kernel(Integer.parseInt(args[3]));
            kernel.setGetTime(new GetTime(seed));
            Topology.initializeAllV2(kernel);
            GRIB.phaseOne = false;
            System.out.println("Simulating Events");
            kernel.simulate(24*60*60);
            WriteLogsV2 writer = new WriteLogsV2(0,args[4],Topology.numberOfNodes);
            writer.act();
            for(int i=1;i<=Topology.numberOfNodes;i++) {
                System.out.println("---------------Node\t"+i+"\tRIB------------------");
                System.out.println(Topology.getNodeByAddressV2(i).getRib().toString());
                System.out.println("------------------------------------------------");
            }
        } else {
            boolean withEvents = true;
            GetTime.withRandom = true;
            ArrivalEventV2.dumpMessages = false;
            Topology.withPolicies = true;
            Kernel.withEvents = true;
            Kernel.withTimer = true;
            NodeV2.magicNumber = Integer.parseInt(args[7]);
            String eventFile = args[2];
            Kernel.eventFile = args[2];
            if(Integer.parseInt(args[5])==1)
                Topology.partialConverging = true;
            else
                Topology.partialConverging = false;
            System.out.println(Topology.partialConverging);
            if(Topology.partialConverging) {
                Topology.activityList = EventParser.getActivityList(eventFile,24*60*60);
                System.out.println("Length = "+Topology.activityList.size());
            }
            Topology.parseTopologyV2(args[0],true);
            for(int i=1;i<=Topology.numberOfNodes;i++)
                Topology.getNodeByAddressV2(i).setInitialized(true);
            GRIB.initializeRIB(Topology.numberOfNodes);
            MessageExecuter executer = new MessageExecuter();
         
            if(withEvents) {
                Topology.fillRIBs();
                if(GetTime.withRandom)
                    Topology.parseLatency(args[1]);
                NodeV2.MRAI = Double.parseDouble(args[6]);
                long seed = Integer.parseInt(args[7])*10+1979;
                Kernel.numberofNodes = Topology.numberOfNodes;
                Kernel kernel = new Kernel(Integer.parseInt(args[3]));
                kernel.setGetTime(new GetTime(seed));
                Topology.initializeAll(kernel);
                GRIB.phaseOne = false;
                System.out.println("Simulating Events");
                kernel.simulate(24*60*60);
                WriteLogsV3 writer = new WriteLogsV3(0,args[4],Topology.numberOfNodes);
                writer.act();
            } else {
                for(int i=1;i<=Topology.numberOfNodes;i++) {
                    System.out.println("-----------------------"+i+"------------------------");
                    System.out.println(Topology.getNodeByAddressV2(i).getRib().getPrefixes().size());
         
                }
            }
        }
        //RIB
        long stop = System.currentTimeMillis();
        System.out.println( "Stop=" + (stop-start));
        long endingMemoryUse = getUsedMemory();
        System.out.println("EMemory="+endingMemoryUse);
    }
    
}

