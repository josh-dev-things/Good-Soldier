/*
 * This is the interpreter for Good Soldier (gos) Script: .goss
 * gos protocols will be located in .gosl files
 * 
 * Keywords:
 *  out
 *  in
 *  Integer
 * 
 * Operations:
 *  +, -, *, /
 *  &, |, ^, !
 *  &&, ||, ^^
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class interpreter
{
    enum fileSearchRegex{
        Script(".*\\.goss");
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

    public static void main(String args[])
    {
        // Args will likely contain path to a .goss file for now.
        switch(args.length)
        {
            case 1:
                //Only one argument is likely to be a file to interpret!
                Pattern goss_match = Pattern.compile(fileSearchRegex.Script.regex);
                Matcher matcher = goss_match.matcher(args[0]);
                boolean is_goss = matcher.find();
                if(is_goss)
                {
                    //Interpret the goss
                    log("Interpreter recieved a .goss path. Searching...");
                    parseGoss(args[0]);
                } else {
                    //Not a .goss file, what else could it be?
                    log(errorMessage.Usage.msg);
                    return;
                }
                break;

            default:
                log(errorMessage.Usage.msg);
                return;

        }
    }
    
    private static void parseGoss(String pathToGoss)
    {
        BufferedReader reader;
        boolean startFlagFound = false;

        try {
            reader = new BufferedReader(new FileReader(pathToGoss));
            String line = reader.readLine();
            int lineCount = 1;
            
            while(line != null)
            {
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
                    String[] blocks = line.split(" ");

                    int parseResult = parser.parse(blocks);
                    if(parseResult < 0)
                    {
                        // Error!
                        log("Interpreter encountered error while parsing line: " + lineCount);
                        return;
                    }

                }   else    {
                    Pattern startMatch = Pattern.compile(".*START.*");
                    if(startMatch.matcher(line).find()) // Placed after potential parse to avoid parsing the START flag!
                    {
                        log("Interpreter found START flag on line " + lineCount);
                        startFlagFound = true;
                    }
                }
                
                // Read the next instruction!
                line = reader.readLine();
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
        System.out.println(s);;
    }
}



class parser
{
    // Debugging this will be a pain
    enum tokenMapping{
        Whitespace("^\s*$"),
        Numeric("[0-9]+"),
        Operator("\\+|\\-|\\/|\\*"), // +, -, /, *
        BOperator("(\\|{1,2})|(\\&{1,2})|\\!"),
        Assignment("(\\<\\-)|(\\-\\>)|(\\=)"),
        Comparator("(\\=\\=)|(\\<\\=)|(\\>\\=)"),
        Keyword("in|out"),
        Variable("[a-zA-Z_]+[a-zA-Z0-9_]*"),
        Set("\\[([a-zA-Z_]+[a-zA-Z0-9_]*)(,[a-zA-Z_]+[a-zA-Z0-9_]*)*"),
        Comment("\\/\\/.*"),
        Logic("true|false");
        private final String regex;

        private tokenMapping(String s)
        {
            regex = s;
        }   
    }

    public static int parse(String[] tokens)
    {
        String tokenCombination = "";
        
        tokenLoop:
        for(String token : tokens)
        {
            if(token == "")
            {
                interpreter.log("EMPTY");
                continue;
            }

            boolean match = false;
            for(tokenMapping tm : tokenMapping.values())
            {
                Pattern tokenPattern = Pattern.compile(tm.regex);
                if(tokenPattern.matcher(token).matches())
                {
                    // Correct token!
                    tokenCombination = tokenCombination + "<" + tm.name() + ">";
                    match = true;
                    interpreter.log(token + " : <" + tm.name() + ">");
                    
                    if(tm == tokenMapping.Comment)
                    {
                        // Comment found, rest of the line should not be parsed.
                        interpreter.log("Comment found: Skipping...");
                        break tokenLoop; // THIS IS SO COOL
                    }

                    break;
                }
            }

            // If no match has been found then the parser fails!
            if(!match)
            {
                interpreter.log("Parser failed to match token: '" + token + "'");
                return -1;
            }
        }

        //The token combination is the most important!
        interpreter.log(tokenCombination);
        return 0;
    }
}