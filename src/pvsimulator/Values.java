/*
 * Values.java
 *
 * Created on July 4, 2007, 11:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

/**
 *  Interface holds constant values used during the simulation
 * @author ahmed
 */
public interface Values {
    final int ReachGain = 0;
    final int ReachLoss = 1;
    final int prefixesDown = 2;
    final int prefixesUp = 3;
    final int sessionEstablishment = 4;
    final int sessionSignalling = 5;
    final int updateMessage = 0;
    final int interruptMessage = 1;
    final int MRAI = 30;
    final int peer = 1;
    final int provider = 0;
    final int siblings = 2;
    final int customer = 3;
    
}
