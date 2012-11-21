/*
 * WriteLogs.java
 *
 * Created on September 20, 2007, 11:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TLongArrayList;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;

/**
 *  A class for logging messages statistics (SPV+MRAI,SPV+MRAI+Policies)
 * @author ahmed
 */
public class WriteLogsV3 extends Unit{

    /** Creates a new instance of WriteLogs */
    String logFile = "";
    int numberOfNodes;
    Kernel kernel;
    public WriteLogsV3() {
    }
    /** Creates a new instance of WriteLogs */
    public WriteLogsV3(double nextSchedule,String logFile,int numberOfNodes) {
        this.nextSchedule = nextSchedule;
        this.logFile = logFile;
        this.numberOfNodes = numberOfNodes;
        this.kernel = null;
    }

    public WriteLogsV3(double nextSchedule,String logFile,int numberOfNodes, Kernel kernel) {
        this.nextSchedule = nextSchedule;
        this.logFile = logFile;
        this.numberOfNodes = numberOfNodes;
        this.kernel = kernel;
    }

    public void act() {
        try{
            FileOutputStream fout = new FileOutputStream(logFile);
            PrintStream ps = new PrintStream(fout);
            ps.println(MessagesMonitor.total);

            /* Rashed */
            FileWriter fstream = new FileWriter("NodeRoutingTable.txt", true);
            BufferedWriter bufout = new BufferedWriter(fstream);

            for(int i=1;i<=numberOfNodes;i++) {

                NodeV2 node = Topology.getNodeByAddressV2(i);
                TIntArrayList staticAdj = Topology.getStaticAdj(i);
                //node.getRib().toString();
                if(MessagesMonitor.withTime) {
                    TLongArrayList totalStat = node.getMonitor().getTotalStat();
                    TLongArrayList maxStat = node.getMonitor().getMaxStat();
                    long noOfMessages = node.getMonitor().getNoOfMessages();
                    long totalMessages = node.getMonitor().getTotalMessages();
                    String total="";
                    String max = "";
                    for(int j=0;j<totalStat.size();j++) {
                        total = total+""+totalStat.get(j)+"\t";
                        max = max+""+maxStat.get(j)+"\t";
                    }
                    ps.println(i+"\t"+max+""+noOfMessages);
                    ps.println(i+"\t"+total+""+totalMessages);

                    /* Rashed */
                    //bufout.write("UPdate node " +i + ": " + node.getRib().toString() + "\n");
                    //bufout.flush();
                    //Rashed

                } else {
                    TIntIntHashMap myUpStat = node.getMonitor().getNeighborsStatUp();
                    TIntIntHashMap myDownStat = node.getMonitor().getNeighborsStatDown();
                    String stat=  "";
                    for(int k=0;k<staticAdj.size();k++) {
                        if(staticAdj.size()!=k+1)
                            stat+=staticAdj.get(k)+"\t"+myDownStat.get(staticAdj.get(k))+"\t"+myUpStat.get(staticAdj.get(k))+"\t";
                        else
                            stat+=staticAdj.get(k)+"\t"+myDownStat.get(staticAdj.get(k))+"\t"+myUpStat.get(staticAdj.get(k));
                    }
                    ps.println(node.getMonitor().getTotalMessages()+"\t"+node.getMonitor().getTotalDownPhase()+"\t"+node.getMonitor().getTotalUPPhase()+"\t"+stat);
                    /* Rashed */
                    //bufout.write("Update node " +i + ": " + GetTime.class.newInstance()+ "\n");

                    /**if(this.kernel != null) {
                        GetTime getTime = new GetTime(0);
                        getTime.getProcessingTime(kernel);*/
                        //bufout.write("Update node " +i + ": " + getTime.getProcessingTime(kernel) + "\n");
                        //bufout.write("Update node " +i + ": " + System.currentTimeMillis()+ "\n");
                    bufout.write("RIB of node " +i+ ": \n\n" + node.getRib() + "\n");
                    bufout.flush();
                    
                    //}

                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}