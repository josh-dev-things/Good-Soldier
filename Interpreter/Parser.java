package Interpreter;

import java.util.*;
import java.util.regex.Pattern;

import Interpreter.Interfaces.IParser;

public class Parser implements IParser {
    // Debugging this will be a pain
    enum tokenMapping{
        Whitespace("^\s*$"),
        String("\".*\""),
        Tag("[a-zA-Z]+[0-9]*\\:"),
        Numeric("[0-9]+"),
        Operator("\\+|\\-|\\/|\\*|\\%"), // +, -, /, *
        BOperator("(\\|{1,2})|(\\&{1,2})"),
        Assignment("(\\<\\-)|(\\-\\>)|(\\=)"),
        Comparator("(\\=\\=)|(\\<\\=)|(\\>\\=)|\\>||\\<||(\\!\\=)"),
        Keyword("in|out|jump\\?|jump"),
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

    @Override
    public TokenMap parse(String[] tokens)
    {
        String[] tokenCombination = new String[tokens.length];
        String tokenDebugString = "";
        
        int tIndex = 0;
        tokenLoop:
        for(String token : tokens)
        {
            if(token == "")
            {
                Interpreter.log("EMPTY");
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
                        Interpreter.log("Comment found: Skipping...");
                        tokenCombination = Arrays.copyOfRange(tokenCombination, 0, tIndex); // Copy only the part of the expression that is not a comment. (This is clean imo).
                        tokens = Arrays.copyOfRange(tokens, 0, tIndex);
                        break tokenLoop; // THIS IS SO COOL
                    }

                    break;
                }
            }

            // If no match has been found then the parser fails!
            if(!match)
            {
                Interpreter.log("Parser failed to match token: '" + token + "'");
                return null;
            }
            tIndex++;
        }

        //The token combination is the most important!
        Interpreter.log(Arrays.toString(tokenCombination));
        return new TokenMap(tokens, tokenCombination);
    }

    public static boolean isTag(String line)
    {
        Pattern pattern = Pattern.compile(tokenMapping.Tag.regex);
        return pattern.matcher(line).matches();
    } 
}
