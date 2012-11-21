/*
 * LinksLatencyCalculator.java
 *
 * Created on September 20, 2007, 2:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Helper;

import gnu.trove.TIntArrayList;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Random;
import pvsimulator.Topology;

/**
 *
 * @author ahmed
 */
public class LinksLatencyCalculator {
    
    /** Creates a new instance of LinksLatencyCalculator */
    public LinksLatencyCalculator() {
    }
    public static void CalculateLatencies(int numberOfNodes,String file)
    {
      try{
          Random rand = new Random(791006);   
          FileOutputStream fout = new FileOutputStream(file);
          PrintStream ps = new PrintStream(fout);
          for(int i=1;i<=numberOfNodes;i++)
          {
            TIntArrayList neighbors = Topology.getStaticAdj(i);
            for(int j=0;j<neighbors.size();j++)
            {

              ps.println(i+"\t"+neighbors.get(j)+"\t"+rand.nextDouble()*0.1);
            }
          }
      }catch(Exception e)
      {
        e.printStackTrace();
      }
              
    }
}
