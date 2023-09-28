package Interpreter;

// Interfaces
import Interpreter.Interfaces.*;

/*
 * This is the interpreter for Good Soldier (gos) Script: .goss
 * gos protocols will be located in .gosp files
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
import java.util.stream.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private static ArrayList<String> variableNames = new ArrayList<String>(); // e.g. i1
    private static ArrayList<String> variableValues = new ArrayList<String>(); // e.g. 59
    private static LinkedList<String> tagNames = new LinkedList<String>();
    private static LinkedList<Integer> tagLines = new LinkedList<Integer>();
    private static int lineCount = 1;

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
                    
                    if(execute(parseResult) < 0)
                    {
                        log("Interpreter encountered error while executing line: " + lineCount);
                        return;
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

    private static ITokenMap executeComparison(ITokenMap tM)
    {
        String[] tokenTypes = tM.getTokenTypes();
        String[] tokens = tM.getTokens();
        IParser parser = new Parser(); //TODO: WRITE A PARSER FACTORY!

        int comparatorInstanceCount = Collections.frequency(Arrays.asList(tokenTypes), "<Comparator>");
        if(comparatorInstanceCount > 0)
        {
            for(int i = 0; i < comparatorInstanceCount; i++)
            {
                int comparatorIndex = Arrays.asList(tokenTypes).indexOf("<Comparator>");
                if(comparatorIndex < 0)
                {
                    log("Err. Couldn't find the comparator I found.");
                    return null;
                }

                boolean result = false;
                if(tokenTypes[comparatorIndex - 1] != null && tokenTypes[comparatorIndex + 1] != null)
                {
                    String type = tokenTypes[comparatorIndex - 1];
                    String l,r;
                    l = tokens[comparatorIndex-1];
                    r = tokens[comparatorIndex+1];

                    if(type.equals("<Variable>"))
                    {
                        if(variableNames.contains(l))
                        {
                            l = variableValues.get(variableNames.indexOf(l));
                            l = l.replace("\"", "");
                            type = parser.parse(new String[]{l}).getTokenTypes()[0];
                        }
                    }

                    if(tokenTypes[comparatorIndex + 1].equals("<Variable>"))
                    {
                        if(variableNames.contains(r))
                        {
                            r = variableValues.get(variableNames.indexOf(r));
                            r = r.replace("\"", "");
                            tokenTypes[comparatorIndex + 1] = parser.parse(new String[]{r}).getTokenTypes()[0];
                        }
                    }
                    

                    switch (tokens[comparatorIndex]) {
                        case "==":
                            result = l.equals(r);
                            break;

                        case "!=":
                            result = !(l.equals(r));
                            break;

                        case "<=":
                            if(type.equals("<Numeric>")) // This looks stupid. Someone fix.
                            {
                                result = Integer.parseInt(l) <= Integer.parseInt(r);
                            }
                            break;

                        case ">=":
                            if(type.equals("<Numeric>"))
                            {
                                result = Integer.parseInt(l) >= Integer.parseInt(r);
                            }
                            break;

                        case "<":
                            if(type.equals(("<Numeric>")))
                            {
                                result = Integer.parseInt(l) < Integer.parseInt(r);
                            }
                            break;

                        case ">":
                            if(type.equals("<Numeric>"))
                            {
                                result = Integer.parseInt(l) > Integer.parseInt(r);
                            }
                            break;
                    
                        default:
                        log("Err. Comparator switch fell through to default");
                            return null;
                    }
                    log("Result of comparison: " + l + " " + tokens[comparatorIndex] + " " + r + " = " + result);
                    
                } else {
                    log("Err. Incorrect number of operands for comparison.");
                    return null;
                }

                tokens[comparatorIndex + 1] = result ? "true" : "false";
                tokenTypes[comparatorIndex + 1] = "<Logic>";
                tokens = popIndexInStringArray(tokens, comparatorIndex - 1);
                tokens = popIndexInStringArray(tokens, comparatorIndex - 1);
                tokenTypes = popIndexInStringArray(tokenTypes, comparatorIndex - 1);
                tokenTypes = popIndexInStringArray(tokenTypes, comparatorIndex - 1);  
            }
        }
        return new TokenMap(tokens, tokenTypes);
    }

    private static ITokenMap executeBOperator(ITokenMap tM)
    {
        String[] tokenTypes = tM.getTokenTypes();
        String[] tokens = tM.getTokens();

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
                    return null;
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
                        return null; 
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
                        return null;
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
                        return null; 
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
                        return null;
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
                        return null;
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
        return new TokenMap(tokens, tokenTypes);
    }

    private static ITokenMap executeOperator(ITokenMap tM)
    {
        String[] tokenTypes = tM.getTokenTypes();
        String[] tokens = tM.getTokens();

        int operatorInstanceCount = Collections.frequency(Arrays.asList(tokenTypes), "<Operator>"); // There are obvious ways to improve performance. Calculate this when parsing for example.
        if(operatorInstanceCount > 0)
        {
            for(int i = 0; i < operatorInstanceCount; i++)
            {
                int operatorIndex = Arrays.asList(tokenTypes).indexOf("<Operator>"); // Gets index of first operator instance
                
                if(operatorIndex < 0)
                {
                    log("Err. Searching for percieved operator in expression failed.");
                    return null;
                }

                int l, r;
                String operator = tokens[operatorIndex];
                try{
                    if(tokenTypes[operatorIndex - 1].equals("<Numeric>"))
                    {
                        l = Integer.parseInt(tokens[operatorIndex-1]);
                    } else if(tokenTypes[operatorIndex - 1].equals("<Variable>")) {
                        l = Integer.parseInt(variableValues.get(variableNames.indexOf(tokens[operatorIndex - 1])).replace("\"", ""));
                    } else {
                        log("Err. unexpected type left of operator.");
                        return null;
                    }

                    if(tokenTypes[operatorIndex + 1].equals("<Numeric>"))
                    {
                        r = Integer.parseInt(tokens[operatorIndex + 1]);
                    } else if(tokenTypes[operatorIndex + 1].equals("<Variable>")) {
                        r = Integer.parseInt(variableValues.get(variableNames.indexOf(tokens[operatorIndex + 1])).replace("\"", ""));
                    } else {
                        log("Err. unexpected type right of operator.");
                        return null;
                    }
                } catch (NumberFormatException nfe) {
                    log("Err. Unable to format numeric type.");
                    nfe.printStackTrace();
                    return null;
                }

                //Now perform the integer operation
                int result = 0;
                try{
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
                            return null;
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    return null;
                }

                tokens[operatorIndex + 1] = String.valueOf(result);
                tokenTypes[operatorIndex + 1] = "<Numeric>";
                tokens = popIndexInStringArray(tokens, operatorIndex - 1);
                tokens = popIndexInStringArray(tokens, operatorIndex - 1);
                tokenTypes = popIndexInStringArray(tokenTypes, operatorIndex - 1);
                tokenTypes = popIndexInStringArray(tokenTypes, operatorIndex - 1);
                
            }
        } 
        return new TokenMap(tokens, tokenTypes);
    }

    private static ITokenMap executeKeyword(ITokenMap tM)
    {
        String[] tokenTypes = tM.getTokenTypes();
        String[] tokens = tM.getTokens();
        IParser parser = new Parser();
        
        if(java.util.Arrays.stream(tokenTypes).anyMatch("<Keyword>"::equals))
        {
            int keywordIndex = Arrays.binarySearch(tokenTypes, "<Keyword>");
            if(keywordIndex < 0)
            {
                log("Err. The keyword could not be found in the expression.");
                return null;
            } else {
                // Handle the keyword.
                String keyword = tokens[keywordIndex];
                switch (keyword) {
                    case "out":
                        // Output to console.
                        if(tokens.length > 2 || !(tokenTypes[1].equals("<Variable>") || tokenTypes[1].equals("<String>")))
                        {
                            log("Err. Incorrect usage of out keyword.");
                            return null;
                        } else {
                            if(tokenTypes[1].equals("<Variable>"))
                            {
                                if(variableNames.contains(tokens[1]))
                                {
                                    // Output variable value
                                    System.out.println(variableValues.get(variableNames.indexOf(tokens[1])).replace("\"", ""));
                                } else {
                                    log("Err. Incorrect usage of out keyword. Variable Access Error.");
                                    return null;
                                }
                            } else {
                                System.out.println(tokens[1].replace("\"", ""));
                            }
                        }
                        return tM;

                    case "in":
                        // Get input from console!
                        String inString ="\"" + System.console().readLine() + "\"";
                        ITokenMap inputTokenMap = parser.parse(new String[]{inString});
                        if(inputTokenMap == null)
                        {
                            log("Err. Invalid input.");
                            return null;
                        }
                        String token = inputTokenMap.getTokens()[0];
                        String tokenType = inputTokenMap.getTokenTypes()[0];
                        
                        tokens[keywordIndex] = token;
                        tokenTypes[keywordIndex] = tokenType;
                        log("Read input: " + token + " : " + tokenType);
                        break;

                    case "jump":
                        if(tokens[1] != null)
                        {
                            int tagIndex = 1; // Hacky
                            if(tagIndex < 0)
                            {
                                return null;
                            }

                            String tagName = tokens[tagIndex];
                            if(tagNames.contains(tagName))
                            {
                                lineCount = tagLines.get(tagNames.indexOf(tagName));
                            } else {
                                log("Err. Unexpected tag found.");
                                return null;
                            }
                        }
                        break;

                    case "jump?":
                        if(tokens[1] != null && tokens[2] != null)
                        {
                            // Token 1 will be the condition... Token 2 will be the destination
                            if(tokenTypes[1].equals("<Logic>"))
                            {
                                if(tokens[1].equals("false"))
                                {
                                    break;
                                }
                            } else if(tokenTypes[1].equals("<Variable>")) {
                                if(variableNames.contains(tokens[1]))
                                {
                                    if(variableValues.get(variableNames.indexOf(tokens[1])) == "false")
                                    {
                                        break;
                                    }
                                } else {
                                    log("Err. Unknown variable.");
                                    return null;
                                }
                            } else {
                                log("Err. Unexpected type @ condition.");
                                return null;
                            }

                            int tagIndex = 2;
                            if(tagIndex < 0)
                            {
                                return null;
                            }

                            String tagName = tokens[tagIndex];
                            if(tagNames.contains(tagName))
                            {
                                lineCount = tagLines.get(tagNames.indexOf(tagName));
                            } else {
                                log("Err. Unexpected tag found.");
                                return null;
                            }
                        }
                        break;
                
                    default:
                        log("Err. The keyword switch fell through to default.");
                        return null;
                }
            }
        }
        return tM;
    }

    private static ITokenMap executeAssignment(ITokenMap tM)
    {
        String[] tokenTypes = tM.getTokenTypes();
        String[] tokens = tM.getTokens();

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
        return new TokenMap(tokens, tokenTypes);
    }

    private static int execute(ITokenMap tm)
    {
        /*
         * Handling Comparisons
         */
        tm = executeComparison(tm);
        if(tm == null){return -1;}

        /*
         * Handling boolean operators
         */
        tm = executeBOperator(tm);
        if(tm == null){return -1;}
        
        /*
         * Handling Operators. (Integers for now : 17.09.23)
        */
        tm = executeOperator(tm);
        if(tm == null){return -1;}
        
        /*
         * Handling keywords!
         */
        tm = executeKeyword(tm);
        if(tm == null){return -1;}

        /*
         * Assignment to variables. Should only be done after all operations have been completed. Expression should be reduced to 3 tokens, 1 assignment symbol and L + R
         */
        tm = executeAssignment(tm);
        if(tm == null){return -1;}
        
        return 0;
    }

    public static void log(String s)
    {
        if(debug || s.contains("Err"))
        {
            System.out.println(s);
        }
    }

    /**
     * This is me being funny. Good Soldier Script etc. so internally "reporting" is the same as "speaking" or outputting something. Let me have fun.
     * @param s
     */
    public static void report(String s)
    {
        System.out.println(s);
    }
}