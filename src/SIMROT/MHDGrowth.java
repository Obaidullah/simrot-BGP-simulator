/*
 * MHDGrowth.java
 *
 * Created on December 23, 2007, 10:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package SIMROT;


/**
 *
 * @author ahmed
 */
public class MHDGrowth {
    private double A;
    private double slope;
    private double T1_Fraction;
    //private double HavingT1Props;
    private double U;
    private double L;
    private double X;
    private double offset;
    /** Creates a new instance of MHDGrowth */
    public MHDGrowth(double slope,double T1_Fraction,int n,double offset) {
        this.slope = slope;
        this.T1_Fraction = T1_Fraction;
        //this.HavingT1Props = HavingT1Props;
        this.offset = offset;
        A = (slope*n);
        //double nPc = offset+A;
        L = offset; 
        U = offset+2*A; 
       
    }
    public int getNoProvider(RandomGenerator random) {
        double first_Rand = random.getRand(L,U); 
        double second_Rand = random.getRand();
        if(second_Rand<(first_Rand-Math.floor(first_Rand)))
        {
          return (int)Math.ceil(first_Rand);
        }
        else
        {
          return (int)Math.floor(first_Rand);
        }    
    }
    public int getNoT1Providers(RandomGenerator random,int numberOfProviders) 
    {
       int result = 0;
       double TH = T1_Fraction;
       for(int i=1;i<=numberOfProviders;i++)
       {
         double rand = random.getRand();
         if(rand<TH)
             ++result;
       }
       return  result; 
    }
    public int getNoMiddleProviders(RandomGenerator random)
    {
      double rand = random.getRand();
      if(0.5<rand)
          return 1;
      else
          return 0;
    }
    public double getT1_Fraction() {
        return T1_Fraction;
    }

}
