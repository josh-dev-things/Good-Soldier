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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class interpreter
{
    /*
     * Debug attributes for modification via cmd line args
    */
    private static boolean debug = false; //NOT USED YET!

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

    private static ArrayList<String> variableNames = new ArrayList<String>(); // e.g. i1
    private static ArrayList<String> variableValues = new ArrayList<String>(); // e.g. 59

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

                    tokenMap parseResult = parser.parse(blocks);

                    if(parseResult == null)
                    {
                        // Error!
                        log("Interpreter encountered error while parsing line: " + lineCount);
                        return;
                    }
                    
                    if(execute(parseResult) < 0)
                    {
                        log("Interpreter encountered error while executing line: " + lineCount);
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

    private static String[] popIndexInStringArray(String[] arr, int index)
    {
        String[] local = new String[arr.length - 1];

        int j = 0;
        for(int i = 0; i < arr.length; i++)
        {
            if(i != index)
            {
                local[j] = arr[i];
                j++;
            }
        }

        return local;
    }

    private static int execute(tokenMap tm)
    {
        String[] tokens = tm.tokens;
        String[] tokenTypes = tm.tokenTypes;
        
        /*
         * Handling boolean operators
         */
        int bOperatorInstanceCount = Collections.frequency(Arrays.asList(tokenTypes), "<BOperator>");
        if(bOperatorInstanceCount > 0)
        {
            // Iterate through every boolean operator in the expression.
            for(int i = 0; i < bOperatorInstanceCount; i++)
            {
                int bOperatorIndex = Arrays.asList(tokenTypes).indexOf("<BOperator>");

                if(bOperatorIndex < 0)
                {
                    log("Err. Execution found a bOperator but also didn't.");
                    return -1;
                }


                boolean l = false, r = false;
                String bOperator = "";
                
                bOperator = tokens[bOperatorIndex];
                if(tokenTypes[bOperatorIndex - 1].equals("<Logic>"))
                {
                    if(tokens[bOperatorIndex-1].equals("true"))
                    {
                        l = true;
                    } else {
                        l = false;
                    } 
                } else if(tokenTypes[bOperatorIndex - 1].equals("<Variable>")) {
                    if(!variableNames.contains(tokens[bOperatorIndex - 1]))
                    {
                        log("Err. Variable not initialized.");
                        return -1; 
                    }

                    if(variableValues.get(variableNames.indexOf(tokens[bOperatorIndex-1])).equals("true"))
                    {
                        l = true;
                    } else if(variableValues.get(variableNames.indexOf(tokens[bOperatorIndex-1])).equals("false"))
                    {
                        l = false;
                    }  else {
                        // l variable is apparently not initialized or not of correct type
                        log("Err. Variable: " + tokens[bOperatorIndex-1] + " is not a boolean.");
                        return -1;
                    }
                }
                
                if(tokenTypes[bOperatorIndex + 1].equals("<Logic>"))
                {
                    if(tokens[bOperatorIndex+1].equals("true"))
                    {
                        r = true;
                    } else {
                        r = false;
                    } 
                } else if(tokenTypes[bOperatorIndex + 1].equals("<Variable>")) {
                    if(!variableNames.contains(tokens[bOperatorIndex + 1]))
                    {
                        log("Err. Variable not initialized.");
                        return -1; 
                    }

                    if(variableValues.get(variableNames.indexOf(tokens[bOperatorIndex+1])).equals("true"))
                    {
                        r = true;
                    } else if(variableValues.get(variableNames.indexOf(tokens[bOperatorIndex+1])).equals("false"))
                    {
                        r = false;
                    }  else {
                        // l variable is apparently not initialized or not of correct type
                        log("Err. Variable: " + tokens[bOperatorIndex+1] + " is not a boolean.");
                        return -1;
                    }
                }
                 
                // Now perform the boolean operation
                boolean result = false;
                switch (bOperator) {
                    case "|":
                    case "||":
                        log("OR: " + l + " || " + r);
                        result = l || r;
                        break;
                    
                    case "&":
                    case "&&":
                        log("AND");
                        result = l && r;
                        break;

                    default:
                        log("Err. BOperator calculation fell through to default. Operator not recognised?");
                        return -1;
                }

                tokens[bOperatorIndex + 1] = result ? "true" : "false";
                tokenTypes[bOperatorIndex + 1] = "<Logic>";
                tokens = popIndexInStringArray(tokens, bOperatorIndex - 1);
                tokens = popIndexInStringArray(tokens, bOperatorIndex - 1);
                tokenTypes = popIndexInStringArray(tokenTypes, bOperatorIndex - 1);
                tokenTypes = popIndexInStringArray(tokenTypes, bOperatorIndex - 1);
            }
            log("bOperation complete, result: " + Arrays.toString(tokenTypes) + " : " + Arrays.toString(tokens));
        }

        /*
         * Handling Operators. (Integers for now : 17.09.23)
        */
        int operatorInstanceCount = Collections.frequency(Arrays.asList(tokenTypes), "<Operator>"); // There are obvious ways to improve performance. Calculate this when parsing for example.
        if(operatorInstanceCount > 0)
        {
            for(int i = 0; i < operatorInstanceCount; i++)
            {
                int operatorIndex = Arrays.asList(tokenTypes).indexOf("<Operator>"); // Gets index of first operator instance
                
                if(operatorIndex < 0)
                {
                    log("Err. Searching for percieved operator in expression failed.");
                    return -1;
                }

                int l, r;
                String operator = tokens[operatorIndex];
                try{
                    if(tokenTypes[operatorIndex - 1].equals("<Numeric>"))
                    {
                        l = Integer.parseInt(tokens[operatorIndex-1]);
                    } else if(tokenTypes[operatorIndex - 1].equals("<Variable>")) {
                        l = Integer.parseInt(variableValues.get(variableNames.indexOf(tokens[operatorIndex - 1])));
                    } else {
                        log("Err. unexpected type left of operator.");
                        return -1;
                    }

                    if(tokenTypes[operatorIndex + 1].equals("<Numeric>"))
                    {
                        r = Integer.parseInt(tokens[operatorIndex + 1]);
                    } else if(tokenTypes[operatorIndex + 1].equals("<Variable>")) {
                        r = Integer.parseInt(variableValues.get(variableNames.indexOf(tokens[operatorIndex + 1])));
                    } else {
                        log("Err. unexpected type right of operator.");
                        return -1;
                    }
                } catch (NumberFormatException nfe) {
                    log("Err. Unable to format numeric type.");
                    return -1;
                }

                //Now perform the integer operation
                int result = 0;
                switch (operator) {
                    case "+":
                        result = l + r;
                        break;

                    case "-":
                        result = l - r;
                        break;

                    case "/":
                        result = l / r;
                        break;

                    case "*":
                        result = l * r;
                        break;

                    case "%":
                        result = l % r;
                        break;
                
                    default:
                        log("Err. Operator calculation fell through to defualt. Operator not recognised?");
                        return -1;
                }

                tokens[operatorIndex + 1] = String.valueOf(result);
                tokenTypes[operatorIndex + 1] = "<Numeric>";
                tokens = popIndexInStringArray(tokens, operatorIndex - 1);
                tokens = popIndexInStringArray(tokens, operatorIndex - 1);
                tokenTypes = popIndexInStringArray(tokenTypes, operatorIndex - 1);
                tokenTypes = popIndexInStringArray(tokenTypes, operatorIndex - 1);
                
            }
        }

        /*
         * Assignment to variables. Should only be done after all operations have been completed. Expression should be reduced to 3 tokens, 1 assignment symbol and L + R
         */
        if(java.util.Arrays.stream(tokenTypes).anyMatch("<Assignment>"::equals))
        {
            int assignIndex = java.util.Arrays.binarySearch(tokenTypes, "<Assignment>"); // Should normally just be 1 but who knows.
            //System.out.println(assignIndex);
            if(assignIndex > 0)
            {
                String varName = tokens[assignIndex-1];
                String varVal = tokens[assignIndex+1];

                switch(tokens[assignIndex])
                {
                    case "=":
                        if(variableNames.contains(varName))
                        {
                            variableValues.set(variableNames.indexOf(varName), varVal);
                            log("Assigned value: " + varVal + " to variable: " + varName);
                        } else {
                            variableNames.add(varName);
                            variableValues.add(variableNames.indexOf(varName), varVal);
                            log("Assigned value: " + varVal + " to new variable: " + varName);
                        }
                        break;

                    case "->":
                        //Handle a pointer assignment
                        break;

                    case "<-":
                        //Handle a pointer assignement
                        break;

                    default:
                        break;
                }
            }
        }
        return 0;
    }

    public static void log(String s)
    {
        System.out.println(s);;
    }

    public static void log(String s, boolean error)
    {
        if(error)
        {
            System.out.println(s);
        }
    }
}



class parser
{
    // Debugging this will be a pain
    enum tokenMapping{
        Whitespace("^\s*$"),
        Numeric("[0-9]+"),
        Operator("\\+|\\-|\\/|\\*|\\%"), // +, -, /, *
        BOperator("(\\|{1,2})|(\\&{1,2})"),
        Assignment("(\\<\\-)|(\\-\\>)|(\\=)"),
        Comparator("(\\=\\=)|(\\<\\=)|(\\>\\=)"),
        Keyword("in|out"),
        Logic("true|false"),
        Set("\\[([a-zA-Z_]+[a-zA-Z0-9_]*)(,[a-zA-Z_]+[a-zA-Z0-9_]*)*"),
        Comment("\\/\\/.*"),
        Variable("\\!?[a-zA-Z_]+[a-zA-Z0-9_]*");

        private final String regex;

        private tokenMapping(String s)
        {
            regex = s;
        }   
    }

    public static tokenMap parse(String[] tokens)
    {
        String[] tokenCombination = new String[tokens.length];
        String tokenDebugString = "";
        
        int tIndex = 0;
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
                    tokenCombination[tIndex] = "<" + tm.name() + ">";
                    tokenDebugString += "<" + tm.name() + ">"; 
                    match = true;
                    // interpreter.log(token + " : <" + tm.name() + ">");
                    
                    if(tm == tokenMapping.Comment)
                    {
                        // Comment found, rest of the line should not be parsed.
                        // interpreter.log("Comment found: Skipping...");
                        break tokenLoop; // THIS IS SO COOL
                    }

                    break;
                }
            }

            // If no match has been found then the parser fails!
            if(!match)
            {
                interpreter.log("Parser failed to match token: '" + token + "'");
                return null;
            }
            tIndex++;
        }

        //The token combination is the most important!
        interpreter.log(Arrays.toString(tokenCombination));
        return new tokenMap(tokens, tokenCombination);
    }
    
}


/*
 * Necessary so that the executor can access the tokens and their types.
 */
class tokenMap
{
    public String[] tokens;
    public String[] tokenTypes;

    public tokenMap(String[] tokens, String[] tokenTypes)
    {
        this.tokens = tokens;
        this.tokenTypes = tokenTypes;
    }
}