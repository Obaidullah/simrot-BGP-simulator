/*
 * EventParser.java
 *
 * Created on August 22, 2007, 2:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import com.sun.org.apache.xerces.internal.impl.xs.identity.ValueStore;
import gnu.trove.TIntArrayList;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Scanner;

/**
 *  This class is used for parsing event files. Currently it parses only Prefix up/Down Events
 * @author ahmed
 */
public class EventParser {
    
    /** Creates a new instance of EventParser */
    public EventParser() {
    }
    /**
     * Parse event files and schedule the parsed events
     * @param file, The event file
     * @param kernel, Reference to the simulator kernel
     * @param currentTime, The value of the current simulation time
     *@withTime, Which versions of the protocol to choose
     */
    public static void parseEventFile(String file,Kernel kernel,double currentTime,boolean withTimer) {
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String str = "";
            while((str=reader.readLine())!=null) {
                Scanner scr = new Scanner(str);
                int type = scr.nextInt();
                int paramOne = scr.nextInt();
                int paramTwo = scr.nextInt();
                double time = scr.nextDouble();
                TIntArrayList list = new TIntArrayList();
                list.add(paramOne);
                if(withTimer) {
                    
                    if(type==1)
                        kernel.schedule(new PrefixDownEventV2(paramTwo,list,kernel,time+currentTime));
                    else {
                        if(type==2)
                            kernel.schedule(new PrefixUpEventV2(paramTwo,list,kernel,time+currentTime));
                        else {
                            if(type==3)
                                kernel.schedule(new FailureEventV2(paramOne,paramTwo,kernel,time+currentTime));
                            else {
                                if(type==4)
                                    kernel.schedule(new RestorationEventV2(paramOne,paramTwo,kernel,time+currentTime));
                            }
                        }
                    }
                } else {
                    if(type==1)
                        kernel.schedule(new PrefixDownEvent(paramTwo,list,kernel,time+currentTime));
                    else {
                        if(type==2)
                            kernel.schedule(new PrefixUpEvent(paramTwo,list,kernel,time+currentTime));
                    }
                }
            }
            
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    /* Returns an array list contains all active network prefixes during the simulation.
     * @param file The event file
     * @param simL The duration of the simulation
     */
    public static TIntArrayList getActivityList(String file,double simL) {
        TIntArrayList result = new TIntArrayList();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String str = "";
            while((str=reader.readLine())!=null) {
                Scanner scr = new Scanner(str);
                int type = scr.nextInt();
                if(type==Values.prefixesUp || type==Values.prefixesDown) {
                    int prefix = scr.nextInt();
                    int node = scr.nextInt();
                    double time = scr.nextDouble();
                    if(time<=(simL)) {
                        if(!result.contains(prefix))
                            result.add(prefix);
                    }
                } else {
                    int paramOne = scr.nextInt();
                    int paramTwo = scr.nextInt();
                    double time = scr.nextDouble();
                    if(time<=(simL)) {
                        if(!result.contains(paramOne))
                            result.add(paramOne);
                    }
                }
                
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
