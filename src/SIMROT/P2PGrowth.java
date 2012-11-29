/*
 * P2PGrowth.java
 *
 * Created on December 27, 2007, 9:05 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package SIMROT;

/**
 *
 * @author ahmed
 */
public class P2PGrowth {
    
    private double slope;
    private double offset;
    private double N_P2P;
    private double L;
    private double U;
    private double X;
    /** Creates a new instance of P2PGrowth */
    public P2PGrowth(double slope,int n,double offset) {
        this.slope = slope;
        this.offset= offset;
        N_P2P = offset+(slope*n);
        L = offset;
        U = offset+2*slope*n;
    }
    public int getNoPeeringLinks(RandomGenerator random) {
        double first_Rand = random.getRand(L,U);
        double second_Rand = random.getRand();
        if(second_Rand<(first_Rand-Math.floor(first_Rand))) {
            return (int)Math.ceil(first_Rand);
        } else {
            return (int)Math.floor(first_Rand);
        }
    }
}
