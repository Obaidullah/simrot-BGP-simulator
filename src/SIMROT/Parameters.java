/*
 * Parameters.java
 *
 * Created on December 19, 2007, 12:19 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package SIMROT;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntDoubleHashMap;

/**
 *
 * @author ahmed
 */
public class Parameters {
    private int numberOfNodes;
    private int N_TierOne;
    private int N_Middle;
    private int N_CP;
    private int N_Stubs;
    private int Max_Providers_Middle;
    private int Max_Providers_CP;
    private int Max_Providers_Stubs;
    private int Max_Num_Providers_T1_M;
    private int Max_Num_Providers_T1_C;
    private int Max_Num_Providers_T1_S;
    private int Max_Num_Peers_M;
    private int Max_Num_Peers_CP;
    private int Max_Num_Peers_M_CP;
    private int Max_Num_Peers_CP_CP;
    private int Num_Of_Regions;
    private int Max_Num_Regions_M;
    private int Max_Num_Regions_CP;
    private double P_M;
    private double P_CP_M;
    private double P_CP_CP;
    private int peers_selection_method=Values.Uniform;
    TIntDoubleHashMap regionLikelhood = new TIntDoubleHashMap();
    TDoubleArrayList regionsCoinFlipper_M = new TDoubleArrayList();
    TDoubleArrayList regionsCoinFlipper_CP = new TDoubleArrayList();
    TDoubleArrayList regionsProps = new TDoubleArrayList();
    MHDGrowth MHD_C = null;
    MHDGrowth MHD_CP = null;
    MHDGrowth MHD_M = null;
    P2PGrowth P_m_m = null;
    P2PGrowth P_cp_cp = null;
    P2PGrowth P_cp_m = null;
    /**
     * Creates a new instance of Parameters
     */
    public Parameters() {
    }

    public void setP_cp_cp(P2PGrowth P_cp_cp) {
        this.P_cp_cp = P_cp_cp;
    }

    public void setP_cp_m(P2PGrowth P_cp_m) {
        this.P_cp_m = P_cp_m;
    }

    public void setP_m_m(P2PGrowth P_m_m) {
        this.P_m_m = P_m_m;
    }


    public void setMHD_C(MHDGrowth MHD_C) {
        this.MHD_C = MHD_C;
    }

    public void setMHD_CP(MHDGrowth MHD_CP) {
        this.MHD_CP = MHD_CP;
    }

    public void setMHD_M(MHDGrowth MHD_M) {
        this.MHD_M = MHD_M;
    }

    public void setP_CP_CP(double P_CP_CP) {
        this.P_CP_CP = P_CP_CP;
    }

    public void setP_CP_M(double P_CP_M) {
        this.P_CP_M = P_CP_M;
    }

    public void setP_M(double P_M) {
        this.P_M = P_M;
    }
    

    
    public void setRegionsCoinFlipper_CP(TDoubleArrayList regionsCoinFlipper_CP) {
        this.regionsCoinFlipper_CP = regionsCoinFlipper_CP;
    }

    public void setRegionsCoinFlipper_M(TDoubleArrayList regionsCoinFlipper_M) {
        this.regionsCoinFlipper_M = regionsCoinFlipper_M;
    }
    

    public void setNum_Of_Regions(int Num_Of_Regions) {
        this.Num_Of_Regions = Num_Of_Regions;
    }
    
    public void setMax_Num_Peers_CP(int Max_Num_Peers_CP) {
        this.Max_Num_Peers_CP = Max_Num_Peers_CP;
    }
    
    public void setMax_Num_Peers_CP_CP(int Max_Num_Peers_CP_CP) {
        this.Max_Num_Peers_CP_CP = Max_Num_Peers_CP_CP;
    }
    
    public void setMax_Num_Peers_M(int Max_Num_Peers_M) {
        this.Max_Num_Peers_M = Max_Num_Peers_M;
    }
    
    public void setMax_Num_Peers_M_CP(int Max_Num_Peers_M_CP) {
        this.Max_Num_Peers_M_CP = Max_Num_Peers_M_CP;
    }
    
    public void setMax_Num_Providers_T1_C(int Max_Num_Providers_T1_C) {
        this.Max_Num_Providers_T1_C = Max_Num_Providers_T1_C;
    }
    
    public void setMax_Num_Providers_T1_M(int Max_Num_Providers_T1_M) {
        this.Max_Num_Providers_T1_M = Max_Num_Providers_T1_M;
    }
    
    public void setMax_Num_Providers_T1_S(int Max_Num_Providers_T1_S) {
        this.Max_Num_Providers_T1_S = Max_Num_Providers_T1_S;
    }
    
    public void setMax_Num_Regions_CP(int Max_Num_Regions_CP) {
        this.Max_Num_Regions_CP = Max_Num_Regions_CP;
    }
    
    public void setMax_Num_Regions_M(int Max_Num_Regions_M) {
        this.Max_Num_Regions_M = Max_Num_Regions_M;
    }
    
    public void setMax_Providers_CP(int Max_Providers_CP) {
        this.Max_Providers_CP = Max_Providers_CP;
    }
    
    public void setMax_Providers_Middle(int Max_Providers_Middle) {
        this.Max_Providers_Middle = Max_Providers_Middle;
    }
    
    public void setMax_Providers_Stubs(int Max_Providers_Stubs) {
        this.Max_Providers_Stubs = Max_Providers_Stubs;
    }
    
    public void setN_CP(int N_CP) {
        this.N_CP = N_CP;
    }
    
    public void setN_Middle(int N_Middle) {
        this.N_Middle = N_Middle;
    }
    
    public void setN_Stubs(int N_Stubs) {
        this.N_Stubs = N_Stubs;
    }
    
    public void setN_TierOne(int N_TierOne) {
        this.N_TierOne = N_TierOne;
    }
    
    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }
    
    public void setRegionLikelhood(TIntDoubleHashMap regionLikelhood) {
        this.regionLikelhood = regionLikelhood;
    }
    
    
    
    public void setPeers_selection_method(int peers_selection_method) {
        this.peers_selection_method = peers_selection_method;
    }
    
    public void setRegionsProps(TDoubleArrayList regionsProps) {
        this.regionsProps = regionsProps;
    }

    public P2PGrowth getP_m_m() {
        return P_m_m;
    }

    public P2PGrowth getP_cp_m() {
        return P_cp_m;
    }

    public P2PGrowth getP_cp_cp() {
        return P_cp_cp;
    }

    public double getP_CP_CP() {
        return P_CP_CP;
    }

    public double getP_CP_M() {
        return P_CP_M;
    }

    public double getP_M() {
        return P_M;
    }


    public TDoubleArrayList getRegionsCoinFlipper_CP() {
        return regionsCoinFlipper_CP;
    }

    public TDoubleArrayList getRegionsCoinFlipper_M() {
        return regionsCoinFlipper_M;
    }
    
    public TDoubleArrayList getRegionsProps() {
        return regionsProps;
    }

    public MHDGrowth getMHD_C() {
        return MHD_C;
    }

    public MHDGrowth getMHD_CP() {
        return MHD_CP;
    }

    public MHDGrowth getMHD_M() {
        return MHD_M;
    }
    
    public int getPeers_selection_method() {
        return peers_selection_method;
    }
    
    public int getMax_Num_Peers_CP() {
        return Max_Num_Peers_CP;
    }
    
    public int getMax_Num_Peers_CP_CP() {
        return Max_Num_Peers_CP_CP;
    }
    
    public int getMax_Num_Peers_M() {
        return Max_Num_Peers_M;
    }
    
    public int getMax_Num_Peers_M_CP() {
        return Max_Num_Peers_M_CP;
    }
    
    public int getMax_Num_Providers_T1_C() {
        return Max_Num_Providers_T1_C;
    }
    
    public int getMax_Num_Providers_T1_M() {
        return Max_Num_Providers_T1_M;
    }
    
    public int getMax_Num_Providers_T1_S() {
        return Max_Num_Providers_T1_S;
    }
    
    public int getMax_Num_Regions_CP() {
        return Max_Num_Regions_CP;
    }
    
    public int getMax_Num_Regions_M() {
        return Max_Num_Regions_M;
    }
    
    public int getMax_Providers_CP() {
        return Max_Providers_CP;
    }
    
    public int getMax_Providers_Middle() {
        return Max_Providers_Middle;
    }
    
    public int getMax_Providers_Stubs() {
        return Max_Providers_Stubs;
    }
    
    public int getN_CP() {
        return N_CP;
    }
    
    public int getN_Middle() {
        return N_Middle;
    }
    
    public int getN_Stubs() {
        return N_Stubs;
    }
    
    public int getN_TierOne() {
        return N_TierOne;
    }
    
    public int getNum_Of_Regions() {
        return Num_Of_Regions;
    }
    
    public int getNumberOfNodes() {
        return numberOfNodes;
    }
    
    
    public TIntDoubleHashMap getRegionLikelhood() {
        return regionLikelhood;
    }
    
    
    
}
