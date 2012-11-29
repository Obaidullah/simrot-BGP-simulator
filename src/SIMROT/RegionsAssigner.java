/*
 * RegionsAssigner.java
 *
 * Created on December 19, 2007, 2:50 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package SIMROT;

//import com.sun.javadoc.Parameter;
import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntDoubleIterator;
import java.sql.ResultSet;
import java.util.Random;

/**
 *
 * @author ahmed
 */
public class RegionsAssigner {
    private RandomGenerator rand;
    /** Creates a new instance of RegionsAssigner */
    public RegionsAssigner(RandomGenerator rand) {
        this.rand = rand;
    }
    public TIntArrayList getRegions(int numberOfRegions,Parameters parameters) {
        TIntArrayList result = new TIntArrayList();
        if(numberOfRegions==parameters.getNum_Of_Regions()) {
            for(int i=1;i<=numberOfRegions;i++) {
                result.add(i);
            }
        } else {
            int number_All_Regions = parameters.getNum_Of_Regions();
            TDoubleArrayList regionProps =parameters.getRegionsProps();
            double regionSelector = 0.0;
            int region=0;
            int index =0;
            while(index<numberOfRegions) {
                
                regionSelector = rand.getRand();
                region = getRegion(regionSelector,regionProps);
                if(region!=0 && !result.contains(region)) {
                    result.add(region);
                    ++index;
                }
            }            
        }
        return result;
    }
    private int getRegion(double probability,TDoubleArrayList regionProps) {
        int region = -1;
        for (int i =0;i<regionProps.size();i++) {
            if(probability<=regionProps.get(i)) {
                region = i+1;
                break;
            }
        }
        return region;
    }
}
