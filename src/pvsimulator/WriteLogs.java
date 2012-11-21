/*
 * WriteLogs.java
 *
 * Created on September 20, 2007, 11:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 *  A class for logging messages statistics (SPV)
 * @author ahmed
 */
public class WriteLogs extends Unit{
    
    /** Creates a new instance of WriteLogs */
    String logFile = "";
    int numberOfNodes;
    public WriteLogs() {
    }
    /** Creates a new instance of WriteLogs */
    public WriteLogs(double nextSchedule,String logFile,int numberOfNodes) {
        this.nextSchedule = nextSchedule;
        this.logFile = logFile;
        this.numberOfNodes = numberOfNodes;
    }
    
    
    public void act() {
        try{
            FileOutputStream fout = new FileOutputStream(logFile);
            PrintStream ps = new PrintStream(fout);
            ps.println(MessagesMonitor.total);
            for(int i=1;i<=numberOfNodes;i++) {
                Node node = Topology.getNodeByAddress(i);
                long noOfMessages = node.getMonitor().getNoOfMessages();
                long totalMessages = node.getMonitor().getTotalMessages();
                ps.println(i+"\t"+noOfMessages);
                ps.println(i+"\t"+totalMessages);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
