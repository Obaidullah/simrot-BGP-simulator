/*
 * GetTime.java
 *
 * Created on June 29, 2007, 1:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import java.util.Random;

/** A class used for calculating the time need to complete some tasks during the simulation
 *
 * @author ahmed
 */
public class GetTime {
    
    /** Creates a new instance of GetTime */
    private Random rand;
    public static boolean withRandom;
    public GetTime() {
    }
    public GetTime(long seed)
    {
      rand = new Random(seed);
    }
    /** Always return a time value equals to the current time + 0.01 */
    public static double getNextSchedule(Kernel kernel)
    {
     double time = kernel.getTime();           
     return time+0.01;
    }
    /** Returns the network delay bewteen the specified pair of nodes*/
    public static double getNextScheduleArrival(Kernel kernel,int fromNode,int toNode)
    {
        if(withRandom)
        {    
        double latency = Topology.getDelay(fromNode,toNode);
        return kernel.getTime()+latency;
        }
        else
        {
        return kernel.getTime()+0.1; 
        }    
    }
    public static double getNextScheduleArrival(double time,int fromNode,int toNode)
    {
        if(withRandom)
        {    
        double latency = Topology.getDelay(fromNode,toNode);
        return time+latency;
        }
        else
        {
        return time+0.1; 
        }    
    }
    /**Calculate random processing time by getting it from a random variable normally distributed between 0 and 100ms*/
    public double getProcessingTime(Kernel kernel)
    {
      return kernel.getTime()+(rand.nextDouble()*0.1);
    }
}
