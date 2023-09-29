package Interpreter;

// Interfaces
import Interpreter.Interfaces.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.*;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.*;

public class Interpreter
{
    /**
     * Debug attributes for modification via cmd line args
    */
    private static boolean debug = false;

    enum fileSearchRegex{
        Script(".*\\.goss"),
        Protocol(".*\\.gosp");
        private final String regex;

        private fileSearchRegex(String s)
        {
            regex = s;
        }
        
        @Override
        public String toString(){
            return regex;
        }
    }

    static enum errorMessage{
        Usage("Humbly report, the usage is as follows: \njava interpreter <filename>.goss");
        private final String msg;

        private errorMessage(String s)
        {
            msg = s;
        }

        @Override
        public String toString(){
            return msg;
        }
    }

    /*
     * Tags for jumping. Accessors?
     */
    public static LinkedList<String> tagNames = new LinkedList<String>();
    public static LinkedList<Integer> tagLines = new LinkedList<Integer>();
    public static int lineCount = 1;

    public static void main(String args[])
    {
        // Args will likely contain path to a .goss file for now.
        switch(args.length)
        {
            case 2:
                // Enables usage of full interpreter debug log.
                if(args[1].equals("debug"))
                {
                    debug = true;
                }
            case 1:
                //Only one argument is likely to be a file to interpret!
                Pattern goss_match = Pattern.compile(fileSearchRegex.Script.regex);
                Matcher matcher = goss_match.matcher(args[0]);
                boolean is_goss = matcher.find();

                if(is_goss)
                {
                    //Interpret the goss
                    log("Interpreter recieved a .goss path. Searching...");
                } else {
                    //Not a .goss file, what else could it be?
                    log(errorMessage.Usage.msg);
                    return;
                }
                break;

            default:
                log("Err. " + errorMessage.Usage.msg);
                return;

        }
        discoverProtocolFiles();
        parseGoss(args[0]);
    }

    private static String[] discoverProtocolFiles()
    {
        String[] protocolFilePaths = null;

        try(Stream<Path> stream = Files.walk(Paths.get("./"))){
            Object[] paths = stream.filter(path -> path.getFileName().toString().matches(fileSearchRegex.Protocol.regex)).toArray();
            for (Object p : paths) {
                log("Found protocol file: " + p.toString());
            }
        } catch (Exception e) {
            log("Err. Loading protocols failed.");
            e.printStackTrace();
        }

        return protocolFilePaths;
    }
    
    private static void parseGoss(String pathToGoss)
    {
        BufferedReader reader;
        boolean startFlagFound = false;

        Executor executor = new Executor();

        try {
            reader = new BufferedReader(new FileReader(pathToGoss));
            String[] lines = reader.lines().toArray(String[]::new);

            // Need to iterate through the whole document first to look for tags!
            String line = lines[lineCount - 1];
            while(lineCount <= lines.length)
            {
                line = lines[lineCount - 1];
                lines[lineCount - 1] = line;

                //Check for any tags
                if(Parser.isTag(line))
                {
                    tagNames.add(line.replace(":", ""));
                    tagLines.add(lineCount); // NB This is line number NOT index!
                    log("Found a tag: " + line.replace(":", "") + " -> " + lineCount);
                }

                lineCount++;
            }
            reader.close();
            
            lineCount = 1;
            while(lineCount - 1 < lines.length)
            {
                line = lines[lineCount - 1];
                if(startFlagFound)
                {
                    Pattern endMatch = Pattern.compile(".*END.*");
                    if(endMatch.matcher(line).find())
                    {
                        log("Interpreter found END flag on line " + lineCount);
                        break;
                    }
                    
                    /*
                     * Have found a statement to parse. Many lines are likely to be whitespace!
                     */
                    log("-----\nParsing: " + line+"\n-----");

                    /*
                     * Line splitting system needs to be re-written:
                     * - Iterate through every character until it STOPS matching a token? Then go to the previous character and there is your token!
                     * - e.g. variable1 would only be matched to a token until the space after variable1 was found.
                     * - e.g. variable2+variable3 would only be matched to <Var><Op><Var> Because variables cannot contain + character!
                     * - This is not top priority atm, save for later.
                     */
                    line = line.replaceAll("\\t+", "").replaceAll("\\s+|^ +| +$|( )+", " ").trim();
                    String[] blocks = line.split("\\s+");

                    /*
                     * Instantiate parser instance
                     */
                    IParser parser = new Parser();

                    ITokenMap parseResult = parser.parse(blocks);

                    if(parseResult == null)
                    {
                        // Error!
                        log("Interpreter encountered error while parsing line: " + lineCount);
                        return;
                    }
                    
                    if(executor.execute(parseResult) < 0)
                    {
                        log("Interpreter encountered error while executing line: " + lineCount);
                        //return;
                    } 

                }   else    {
                    if(line != null)
                    {
                        Pattern startMatch = Pattern.compile(".*START.*");
                        if(startMatch.matcher(line).find()) // Placed after potential parse to avoid parsing the START flag!
                        {
                            log("Interpreter found START flag on line " + lineCount);
                            startFlagFound = true;
                        }
                    } else {
                        log("Null line!?");
                        return;
                    }
                }
                
                // Read the next instruction!
                lineCount++;
            }
            reader.close();
        } catch(IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
    
    public static void log(String s)
    {
        if(debug || s.contains("Err"))
        {
            System.out.println(s);
        }
    }


}