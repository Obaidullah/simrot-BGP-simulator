/*
 * RandomGenerator.java
 *
 * Created on December 19, 2007, 12:51 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package SIMROT;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;
import java.util.Random;

/**
 *
 * @author ahmed
 */
public class RandomGenerator {
    private Random randomGenerator;
    
    /**
     * Creates a new instance of RandomGenerator
     */
    public RandomGenerator(int seed) {
    randomGenerator = new Random(seed);
   
    }
   
   
    public int getRand(int max)
    {
      return randomGenerator.nextInt(max);
    }
    public double getRand()
    {
      return randomGenerator.nextDouble();
    }
    public double getRand(double L,double U)
    {
      return randomGenerator.nextDouble()*(U-L)+L;     
    }
    public int getNumberOfRegions(TDoubleArrayList props)
    {
      int result = 0;
      double rand = getRand();
      for(int i=0;i<props.size();i++)
      {
        if(props.get(i)>rand)
        {
          result = i+1;
          break;
        }
      }
      return result;
    }
}
