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
import gnu.trove.TLongArrayList;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 *  A class for logging messages statistics (SPV+MRAI,SPV+MRAI+Policies)
 * @author ahmed
 */
public class WriteLogsV2 extends Unit{
    
    /** Creates a new instance of WriteLogs */
    String logFile = "";
    int numberOfNodes;
    public WriteLogsV2() {
    }
    /** Creates a new instance of WriteLogs */
    public WriteLogsV2(double nextSchedule,String logFile,int numberOfNodes)
    {
       this.nextSchedule = nextSchedule;
       this.logFile = logFile;
       this.numberOfNodes = numberOfNodes;
    }
    

    public void act() {
        try{    
        FileOutputStream fout = new FileOutputStream(logFile);
        PrintStream ps = new PrintStream(fout);
        ps.println(MessagesMonitor.total);
        for(int i=1;i<=numberOfNodes;i++)
        {
           NodeV2 node = Topology.getNodeByAddressV2(i);
           TLongArrayList totalStat = node.getMonitor().getTotalStat();
           TLongArrayList maxStat = node.getMonitor().getMaxStat();
           long noOfMessages = node.getMonitor().getNoOfMessages();
           long totalMessages = node.getMonitor().getTotalMessages();
           String total="";
           String max = "";
           for(int j=0;j<totalStat.size();j++)
           {
             total = total+""+totalStat.get(j)+"\t";
             max = max+""+maxStat.get(j)+"\t";
           }
           ps.println(i+"\t"+max+""+noOfMessages);
           ps.println(i+"\t"+total+""+totalMessages);
         }
        } catch(Exception ex)
        {
          ex.printStackTrace();
        }
    }
}
