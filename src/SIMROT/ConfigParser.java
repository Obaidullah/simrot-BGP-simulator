/*
 * ConfigParser.java
 *
 * Created on December 20, 2007, 12:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package SIMROT;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntDoubleHashMap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
/**
 * ConfigParser parses different configuration files and values
 * @author ahmed
 */
public class ConfigParser {
    private String configFile;
    private String regionsConfig;
    private int randomSeed;
    private double M_twoRegions;
    private double CP_twoRegions;
    private double C_MHD_ABS;
    private double C_MHD_SLOPE;
    private double C_MHD_T_Fraction;
    private double CP_MHD_ABS;
    private double CP_MHD_SLOPE;
    private double CP_MHD_T_Fraction;
    private double M_MHD_ABS;
    private double M_MHD_SLOPE;
    private double M_MHD_T_Fraction;
    private double M_P2P_ABS;
    private double M_P2P_SLOPE;
    private double CP_P2P_ABS;
    private double CP_P2P_SLOPE;
    private double CP_M_P2P_ABS;
    private double CP_M_P2P_SLOPE;
    /** Creates a new instance of ConfigParser */

    public ConfigParser(String configFile,String regionsConfig)
    {
      this.configFile = configFile;
      this.regionsConfig = regionsConfig;
    }
   
     /**
     * Parses configurations
     */

    public Parameters parseConfiguration() throws FileNotFoundException, IOException {
        Parameters parameters = new Parameters();
        FileInputStream fin = new FileInputStream(configFile);
        Properties properties = new Properties();
        properties.load(fin);
        parameters.setNumberOfNodes(Integer.parseInt(properties.getProperty("N")));
        parameters.setN_TierOne(Integer.parseInt(properties.getProperty("N_T")));
        parameters.setN_Middle(Math.round(Float.parseFloat(properties.getProperty("N_M"))*parameters.getNumberOfNodes()));
        parameters.setN_CP(Math.round(Float.parseFloat(properties.getProperty("N_CP"))*parameters.getNumberOfNodes()));
        parameters.setN_Stubs(parameters.getNumberOfNodes()-parameters.getN_TierOne()-parameters.getN_Middle()-parameters.getN_CP());
        parameters.setNum_Of_Regions(Integer.parseInt(properties.getProperty("N_R")));
        this.randomSeed = (Integer.parseInt(properties.getProperty("randomSeed")));
        setRegionsConfig(parameters);
        this.M_twoRegions = (Double.parseDouble(properties.getProperty("M_two_regions")));
        this.CP_twoRegions = (Double.parseDouble(properties.getProperty("CP_two_regions")));
        this.C_MHD_ABS = (Double.parseDouble(properties.getProperty("C_MHD_ABS")));
        this.C_MHD_SLOPE = (Double.parseDouble(properties.getProperty("C_MHD_SLOPE")));
        this.C_MHD_T_Fraction = (Double.parseDouble(properties.getProperty("C_MHD_T_Fraction")));
        this.CP_MHD_ABS = (Double.parseDouble(properties.getProperty("CP_MHD_ABS")));
        this.CP_MHD_SLOPE = (Double.parseDouble(properties.getProperty("CP_MHD_SLOPE")));
        this.CP_MHD_T_Fraction = (Double.parseDouble(properties.getProperty("CP_MHD_T_Fraction")));
        this.M_MHD_ABS = (Double.parseDouble(properties.getProperty("M_MHD_ABS")));
        this.M_MHD_SLOPE = (Double.parseDouble(properties.getProperty("M_MHD_SLOPE")));
        this.M_MHD_T_Fraction = (Double.parseDouble(properties.getProperty("M_MHD_T_Fraction")));
        this.M_P2P_ABS = (Double.parseDouble(properties.getProperty("M_P2P_ABS")));
        this.M_P2P_SLOPE = (Double.parseDouble(properties.getProperty("M_P2P_SLOPE")));
        this.CP_P2P_ABS = (Double.parseDouble(properties.getProperty("CP_P2P_ABS")));
        this.CP_P2P_SLOPE = (Double.parseDouble(properties.getProperty("CP_P2P_SLOPE")));
        this.CP_M_P2P_ABS = (Double.parseDouble(properties.getProperty("CP_M_P2P_ABS")));
        this.CP_M_P2P_SLOPE = (Double.parseDouble(properties.getProperty("CP_M_P2P_SLOPE")));
        setMHD_C(parameters);
        setMHD_CP(parameters);
        setMHD_M(parameters);
        setP2P_M(parameters);
        setP2P_CP(parameters);
        setP2P_CP_M(parameters);
        setRegionsCoinFlipper_M(parameters);
        setRegionsCoinFlipper_CP(parameters);
        return parameters;
    }
    /*
     * Instatiate a new random number generator using the specified seed
     */
    public RandomGenerator parseRandomSeeds() throws FileNotFoundException, IOException {
        return new RandomGenerator(randomSeed);
    }
    /**
     * Set region configurations
     * @param parameters Parametrs object that stores desired configurations for regions
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void setRegionsConfig(Parameters parameters) throws FileNotFoundException, IOException {
        //TIntDoubleHashMap liklehood = new TIntDoubleHashMap();
        TDoubleArrayList regionsProps = new TDoubleArrayList();
        BufferedReader reader = new BufferedReader(new FileReader(regionsConfig));
        for(int i=1;i<=parameters.getNum_Of_Regions();i++) {
            regionsProps.add(Double.parseDouble(reader.readLine()));
        }
        parameters.setRegionsProps(regionsProps);
    }
    /**
     *  Set region's belonging probability for M nodes
     * @param parameters Parametrs object that stores desired configurations
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void setRegionsCoinFlipper_M(Parameters parameters) throws FileNotFoundException, IOException {
        TDoubleArrayList values = new TDoubleArrayList();
        double oneRegion = 1.0 - M_twoRegions;
        values.add(oneRegion);
        values.add(1.0);
        parameters.setRegionsCoinFlipper_M(values);
    }
    /**
     * Set region's belonging probability for CP nodes
     *
     * @param parameters Parametrs object that stores desired configurations
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void setRegionsCoinFlipper_CP(Parameters parameters) throws FileNotFoundException, IOException {
        TDoubleArrayList values = new TDoubleArrayList();
        double oneRegion = 1.0 - CP_twoRegions;
        values.add(oneRegion);
        values.add(1.0);
        parameters.setRegionsCoinFlipper_CP(values);
    }
    /**
     * Set MHD configurations for C nodes
     * @param file configuration file for the MHD of C nodes
     * @param parameters Parametrs object that stores the desired configurations
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void setMHD_C(Parameters parameters) throws FileNotFoundException, IOException {
        
        parameters.setMHD_C(new MHDGrowth(C_MHD_SLOPE,C_MHD_T_Fraction,parameters.getNumberOfNodes(),C_MHD_ABS));
    }
    /**
     * Set MHD configurations for CP nodes
     * @param file configuration file for the MHD of CP nodes
     * @param parameters Parametrs object that stores the desired configurations
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void setMHD_CP(Parameters parameters) throws FileNotFoundException, IOException {

        parameters.setMHD_CP(new MHDGrowth(CP_MHD_SLOPE,CP_MHD_T_Fraction,parameters.getNumberOfNodes(),CP_MHD_ABS));
    }
    /**
     * Set MHD configurations for M nodes
     * @param file configuration file for the MHD of M nodes
     * @param parameters Parametrs object that stores the desired configurations
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void setMHD_M(Parameters parameters) throws FileNotFoundException, IOException {

        parameters.setMHD_M(new MHDGrowth(M_MHD_SLOPE,M_MHD_T_Fraction,parameters.getNumberOfNodes(),M_MHD_ABS));
    }
     /**
     * Set P2P configurations between M nodes
     * @param file configuration file for the P2P between M nodes
     * @param parameters Parametrs object that stores the desired configurations
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void setP2P_M(Parameters parameters) throws FileNotFoundException, IOException {
       
        parameters.setP_m_m(new P2PGrowth(M_P2P_SLOPE,parameters.getNumberOfNodes(),M_P2P_ABS));
    }
    /**
     * Set P2P configurations between CP nodes
     * @param file configuration file for the P2P between CP nodes
     * @param parameters Parametrs object that stores the desired configurations
     * @throws FileNotFoundException
     * @throws IOException
     */
     private void setP2P_CP(Parameters parameters) throws FileNotFoundException, IOException {
       
        parameters.setP_cp_cp(new P2PGrowth(CP_P2P_SLOPE,parameters.getNumberOfNodes(),CP_P2P_ABS));
    }
     /**
     * Set P2P configurations between CP and M nodes
     * @param file configuration file for the P2P between CP and M nodes
     * @param parameters Parametrs object that stores the desired configurations
     * @throws FileNotFoundException
     * @throws IOException
     */
      private void setP2P_CP_M(Parameters parameters) throws FileNotFoundException, IOException {
       
        parameters.setP_cp_m(new P2PGrowth(CP_M_P2P_SLOPE,parameters.getNumberOfNodes(),CP_M_P2P_ABS));
    }
}
