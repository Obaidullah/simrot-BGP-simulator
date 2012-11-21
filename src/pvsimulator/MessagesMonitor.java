/*
 * MessagesMonitor.java
 *
 * Created on September 17, 2007, 4:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TLongArrayList;

/**
 *  This class monitor the frequency of the messages received by the node
 * @author ahmed
 */
public class MessagesMonitor {
    
    /** Creates a new instance of MessagesMonitor */
    private double start_time;
    private double duration;
    private long noOfMessages=1;
    private double monitoringInterval;
    private double interval_start;
    private long counter = 0;
    private long totalCounter = 0;
    public static int total=0;
    public  int totalAnnouncements = 0;
    public  int totalWithdrawals = 0;
    private double totalDownPhase = 0;
    private double totalUPPhase = 0;
    private TIntIntHashMap neighborsStatUp = new TIntIntHashMap(); 
    private TIntIntHashMap neighborsStatDown = new TIntIntHashMap();
    private TLongArrayList totalStat = new TLongArrayList();
    private TLongArrayList maxStat = new TLongArrayList();
    public static boolean withTime = false;
    public static boolean UP = false;
    public static boolean DOWN = false;
    public MessagesMonitor() {
        
    }
    public MessagesMonitor(int myAS)
    {
      TIntArrayList staticAdj = Topology.getStaticAdj(myAS);
      for(int i=0;i<staticAdj.size();i++)
      {
        neighborsStatUp.put(staticAdj.get(i),0);  
        neighborsStatDown.put(staticAdj.get(i),0);
      }
    }
    /**
     *  Creates a new instance of MessagesMonitor
     *  @param start_time the time at which this monitor will start monitoring
     *  @param the granularity for recording
     */
    public MessagesMonitor(double start_time,double duration,double monitoringInterval) {
        this.duration = duration;
        this.start_time = start_time;
        interval_start = start_time;
        this.monitoringInterval = monitoringInterval;

    }
    /**
     *  A callback method informed upon the arrival of new messages at the node
     *  @param currentTime the current simulation time
     */
    public void update(double currentTime,int src) {
        if(withTime)
        {    
        if(currentTime<=monitoringInterval+start_time) {
            ++totalCounter;
            ++total;
            if(currentTime<interval_start+duration)
                ++counter;
            else {
                if(counter>noOfMessages)
                    noOfMessages = counter;
                counter = 0;
                interval_start = currentTime;
                ++counter;
            }
        } else {
            start_time = start_time+monitoringInterval;
            totalStat.add(totalCounter);
            maxStat.add(noOfMessages);
            totalCounter = 0;
            noOfMessages = 1;
            counter = 0;
            interval_start = start_time;
            ++totalCounter;
            ++total;
            if(currentTime<interval_start+duration)
                ++counter;
            else {
                if(counter>noOfMessages)
                    noOfMessages = counter;
                counter = 0;
                interval_start = currentTime;
                ++counter;
            }
        }
        }
        else
        {
           ++total;
           ++totalCounter;
           if(DOWN)
           {    
               ++totalDownPhase;
               int currentStatDown = neighborsStatDown.get(src);
               ++currentStatDown;
               neighborsStatDown.put(src,currentStatDown);
           }
           else
           {
               if(UP)
               {    
               int currentStatUp = neighborsStatUp.get(src);
               ++currentStatUp;
               neighborsStatUp.put(src,currentStatUp);
               ++totalUPPhase;
               }
           }
        }    
    }
    /**
     * Returns NoOfMessages
     */
    public long getNoOfMessages() {
        return noOfMessages;
    }
    /**
     * Returns the total number of observed messages
     */
    public long getTotalMessages() {
        return totalCounter;
        
    }

    public double getTotalUPPhase() {
        return totalUPPhase;
    }

    public TLongArrayList getTotalStat() {
        return totalStat;
    }

    public TLongArrayList getMaxStat() {
        return maxStat;
    }

    public double getTotalDownPhase() {
        return totalDownPhase;
    }

    public TIntIntHashMap getNeighborsStatDown() {
        return neighborsStatDown;
    }

    public TIntIntHashMap getNeighborsStatUp() {
        return neighborsStatUp;
    }




}
