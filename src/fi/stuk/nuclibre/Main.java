/*
*(c) 2023 STUK - Finnish Radiation and Nuclear Safety Authority. 
*
* This source code is licensed under a
* Creative Commons Attribution 4.0 International License.
*
* You should have received a copy of the license along with this
* work.  If not, see <http://creativecommons.org/licenses/by/4.0/>. 
*/
package fi.stuk.nuclibre;

import fi.stuk.ensdf.ENSDFParser;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This is the entry point for the nuclibre application.
 * @author Tero Karhunen
 */
public class Main {
    /** A flag indicating whether to just browse the database or create it. */
    public static boolean browse = false;
    
    /** A flag indicating whether exceptions should be printed. */
    public static boolean printExceptions = false;
    
    /** A flag indicating whether parsing and encoding should be silent. */
    public static boolean silent = false;
    
    /** A flag indicating whether test run is made. */
    public static boolean testRun = false;
    
    /** Command line option specification. */
    private static final Options options = new Options();
    
    /** The ENSDF file used for initial import. */    
    private static File ensdfFile = null;
    
    /** The patch direcotory. */
    private static String patches = null;            
    
    /** Nuclide id of a nuclide to browse. */
    private static String browseId = null;
    
    /** Patch data source. */
    private static String patchSource = null;
    
    /**
     * Print usage information and exit with given code.
     * @param exitCode the code to exit with.
     */
    private static void printUsage(int exitCode){
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("nuclibre [OPTIONS] [SQLITEFILE]", options);
        System.exit(exitCode);
    }
    
    /**
     * Setup command line options.
     */
    private static void setupOptions(){        
        options.addOption("t", "Test run, don't modify anything.");
        options.addOption("s", "Silent, don't print anything during parsing and encoding.");
        options.addOption("w", "Print warnings during parsing and encoding. Overrides silent.");        
        options.addOption("b", "browse", true, "Browse the data for <arg> in the sqlite file.");
        options.addOption("P","patch-dir", true, "Patch the parsed input with files from a specified directory. ");
        options.addOption("S","patch-source", true, "Specify the source for patch directory data.");
        options.addOption("e","ensdf-file", true, "Specify the input ENSDF file");        
    }
    
     /**
     * Parse provided command line options.
     * @param args the options
     * @return the parsed command line.
     */
    private static CommandLine parseOptions(String[] args){
        if(args.length < 1)printUsage(0);
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            return(cmd);
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
            printUsage(-2);            
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return(null);
    }           
    
    /**
     * Handle the provided command line options.
     * @param cmd the command line.
     */
    private static void handleOptions(CommandLine cmd){
        if(cmd.hasOption("t"))testRun = true;
        if(cmd.hasOption("s"))silent = true;
        if(cmd.hasOption("w"))printExceptions = true;
        if(cmd.hasOption("e")){
            String value = cmd.getOptionValue("e");
            if(value == null){
                System.err.println("Error: ENSDF file not specified with option e.");
                printUsage(-3);
            }
            ensdfFile = new File(value);
        }        
        if(cmd.hasOption("b")){
            String value = cmd.getOptionValue("b");
            if(value == null){
                System.err.println("Error: browse ID not specified with option b.");
                printUsage(-4);
            }            
            browse = true;
            browseId = value;
        }       
        if(cmd.hasOption("S")){
            String value = cmd.getOptionValue("S");
            if(value == null){
                System.err.println("Error: source not specified with option S.");
                printUsage(-61);
            }
            patchSource = cmd.getOptionValue("S");            
        }
        if(cmd.hasOption("P")){
            String value = cmd.getOptionValue("P");
            if(value == null){
                System.err.println("Error: dir not specified with option P.");
                printUsage(-6);
            }
            if(patchSource == null){
                System.err.println("Error: patch source must be specified with option S.");
                printUsage(-6);
            }            
            patches = value;
        }
    }
        
    /**
     * Patch the data with files from a given directory.
     * @param p the parser whose data to patch.
     * @param dir the directory.
     * @throws IOException 
     */
    private static void patchDir(ENSDFParser p, String dir) throws IOException{
        final ENSDFParser parser = p;        
          Files.list(Paths.get(dir))             
         .forEach(new Consumer<Path>(){
                @Override
                public void accept(Path t) {
                    try {                    
                        File f = t.toFile();
                        FileReader fr = new FileReader(f);
                        parser.patch(f.getName(), fr, (int)f.length(), patchSource);               
                    } catch (Exception ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                "Error during patching.", ex);
                    }
                }
            });
    }
    
    /** Entry point of the application.
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        setupOptions();        
        CommandLine cmd = parseOptions(args);        
        String[] leftover = cmd.getArgs();
        if(leftover.length > 1 || leftover.length == 0){
            System.err.println("Error: Specify sqlite file.");
            printUsage(-1);
        }                     
        handleOptions(cmd);                
        try {
            File sqliteFile = new File(leftover[0]);             
            Connection c = null;
            if(!Main.browse && !Main.testRun)c = Nuclibre.createNuclibDatabase(sqliteFile);             
            else c = Nuclibre.getDatabaseConnection(sqliteFile);
            if(ensdfFile != null){
               FileReader fr = new FileReader(ensdfFile);
               ENSDFParser p = new ENSDFParser();
               p.parse(ensdfFile.getName(),fr, (int)ensdfFile.length(),"ENSDF");
               fr.close();                    
               if(patches != null)patchDir(p, patches);
               if(!Main.browse){
                    ENSDFNuclibreEncoder encoder = new ENSDFNuclibreEncoder();
                    encoder.store(p, c);
                    if(!testRun)c.commit();      
               }
            }               
            if(Main.browse){               
               DataBrowser b = new DataBrowser(c);
               b.browse(browseId);
            }            
            c.close();             
        }
        catch (SQLException ex1) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Error", ex1);
            if(ENSDFNuclibreEncoder.lastSQL != null)
                   System.err.println("Last SQL statment was:\n"+ENSDFNuclibreEncoder.lastSQL);
        } 
        catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Error", ex);
        }
    }
}
