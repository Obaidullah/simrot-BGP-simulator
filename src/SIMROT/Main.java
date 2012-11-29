/*
 * Main.java
 *
 * Created on December 19, 2007, 12:19 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package SIMROT;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


/**
 *
 * @author Ahmed Elmokashfi
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
         /* The topology generator takes two command line arguments. The first is a property file points to further configuration values and files
          * The second argument is the output file name
         */
        if(args.length<4)
        {
           System.out.println("Usage: java -jar SIMROT-top.jar configurtion_file region_configuration_file output_file output_format");
           System.out.println("The argument list is too short :-(");
           System.exit(0);
        }
        FileInputStream fin = new FileInputStream(args[0]);
        Properties properties = new Properties();
        properties.load(fin);
        ConfigParser parser = new ConfigParser(args[0],args[1]);
        Parameters parameters = parser.parseConfiguration();
        RandomGenerator random = parser.parseRandomSeeds(); 
        Graph graph = new Graph(parameters.getNum_Of_Regions());
        Generator generator = new Generator(parameters,graph,new RegionsAssigner(random),random);
        generator.GenerateGraph();
        int format = Integer.parseInt(args[3]);
        if(format==0)
        {
        graph.saveGraph(args[2]);
        }
        else
        {
        graph.saveGraphForamt2(args[2]);
        }
    }
    
}
