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
public class ConfigParser2 {
    private String configFile;
    private String regionsConfig;
    private String randomSeeds;
    private String regions_flip_M;
    private String regions_flip_CP;
    private String MHD_C;
    private String MHD_CP;
    private String MHD_M;
    private String P_M;
    private String P_CP;
    private String P_CP_M;

    /** Creates a new instance of ConfigParser */
    public ConfigParser2(String configFile,String regionsConfig,String randomSeeds,String regions_flip_M,String regions_flip_CP,String MHD_C,String MHD_CP,String MHD_M,String P_M,String P_CP,String P_CP_M) {
        this.configFile = configFile;
        this.regionsConfig = regionsConfig;
        this.randomSeeds = randomSeeds;
        this.regions_flip_M = regions_flip_M;
        this.regions_flip_CP = regions_flip_CP;
        this.MHD_C = MHD_C;
        this.MHD_CP = MHD_CP;
        this.MHD_M = MHD_M;
        this.P_M = P_M;
        this.P_CP = P_CP;
        this.P_CP_M = P_CP_M;
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
        setRegionsConfig(parameters);
        setMHD_C(MHD_C,parameters);
        setMHD_CP(MHD_CP,parameters);
        setMHD_M(MHD_M,parameters);
        setP2P_M(P_M,parameters);
        setP2P_CP(P_CP,parameters);
        setP2P_CP_M(P_CP_M,parameters);
        setRegionsCoinFlipper_M(parameters);
        setRegionsCoinFlipper_CP(parameters);
        return parameters;
    }
    /*
     * Instatiate a new random number generator using the specified seed
     */
    public RandomGenerator parseRandomSeeds() throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(randomSeeds));
        return new RandomGenerator(Integer.parseInt(reader.readLine()));
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
        BufferedReader reader = new BufferedReader(new FileReader(regions_flip_M));
        String str = "";
        while((str=reader.readLine())!=null) {
            values.add(Double.parseDouble(str));
        }
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
        BufferedReader reader = new BufferedReader(new FileReader(regions_flip_CP));
        String str = "";
        while((str=reader.readLine())!=null) {
            values.add(Double.parseDouble(str));
        }
        parameters.setRegionsCoinFlipper_CP(values);
    }
    /**
     * Set MHD configurations for C nodes
     * @param file configuration file for the MHD of C nodes
     * @param parameters Parametrs object that stores the desired configurations
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void setMHD_C(String file,Parameters parameters) throws FileNotFoundException, IOException {
        FileInputStream fin = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(fin);
        double offset = Double.parseDouble(properties.getProperty("ABS"));
        double slope = Double.parseDouble(properties.getProperty("slope"));
        double T1_Fraction = Double.parseDouble(properties.getProperty("T1_Fraction"));
        //double HavingT1Props = Double.parseDouble(properties.getProperty("HavingT1Props"));
        parameters.setMHD_C(new MHDGrowth(slope,T1_Fraction,parameters.getNumberOfNodes(),offset));
    }
    /**
     * Set MHD configurations for CP nodes
     * @param file configuration file for the MHD of CP nodes
     * @param parameters Parametrs object that stores the desired configurations
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void setMHD_CP(String file,Parameters parameters) throws FileNotFoundException, IOException {
        FileInputStream fin = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(fin);
        double offset = Double.parseDouble(properties.getProperty("ABS"));
        double slope = Double.parseDouble(properties.getProperty("slope"));
        double T1_Fraction = Double.parseDouble(properties.getProperty("T1_Fraction"));
        //double HavingT1Props = Double.parseDouble(properties.getProperty("HavingT1Props"));
        parameters.setMHD_CP(new MHDGrowth(slope,T1_Fraction,parameters.getNumberOfNodes(),offset));
    }
    /**
     * Set MHD configurations for M nodes
     * @param file configuration file for the MHD of M nodes
     * @param parameters Parametrs object that stores the desired configurations
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void setMHD_M(String file,Parameters parameters) throws FileNotFoundException, IOException {
        FileInputStream fin = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(fin);
        double offset = Double.parseDouble(properties.getProperty("ABS"));
        double slope = Double.parseDouble(properties.getProperty("slope"));
        double T1_Fraction = Double.parseDouble(properties.getProperty("T1_Fraction"));
        //double HavingT1Props = Double.parseDouble(properties.getProperty("HavingT1Props"));
        parameters.setMHD_M(new MHDGrowth(slope,T1_Fraction,parameters.getNumberOfNodes(),offset));
    }
     /**
     * Set P2P configurations between M nodes
     * @param file configuration file for the P2P between M nodes
     * @param parameters Parametrs object that stores the desired configurations
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void setP2P_M(String file,Parameters parameters) throws FileNotFoundException, IOException {
        FileInputStream fin = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(fin);
        double offset = Double.parseDouble(properties.getProperty("ABS"));
        double slope = Double.parseDouble(properties.getProperty("slope"));
        parameters.setP_m_m(new P2PGrowth(slope,parameters.getNumberOfNodes(),offset));
    }
    /**
     * Set P2P configurations between CP nodes
     * @param file configuration file for the P2P between CP nodes
     * @param parameters Parametrs object that stores the desired configurations
     * @throws FileNotFoundException
     * @throws IOException
     */
     private void setP2P_CP(String file,Parameters parameters) throws FileNotFoundException, IOException {
        FileInputStream fin = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(fin);
        double offset = Double.parseDouble(properties.getProperty("ABS"));
        double slope = Double.parseDouble(properties.getProperty("slope"));
        parameters.setP_cp_cp(new P2PGrowth(slope,parameters.getNumberOfNodes(),offset));
    }
     /**
     * Set P2P configurations between CP and M nodes
     * @param file configuration file for the P2P between CP and M nodes
     * @param parameters Parametrs object that stores the desired configurations
     * @throws FileNotFoundException
     * @throws IOException
     */
      private void setP2P_CP_M(String file,Parameters parameters) throws FileNotFoundException, IOException {
        FileInputStream fin = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(fin);
        double offset = Double.parseDouble(properties.getProperty("ABS"));
        double slope = Double.parseDouble(properties.getProperty("slope"));
        parameters.setP_cp_m(new P2PGrowth(slope,parameters.getNumberOfNodes(),offset));
    }
}
