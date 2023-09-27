package Interpreter;

import Interpreter.Interfaces.ITokenMap;

public class TokenMap implements ITokenMap{
    public String[] tokens;
    public String[] tokenTypes;

    public TokenMap(String[] tokens, String[] tokenTypes)
    {
        this.tokens = tokens;
        this.tokenTypes = tokenTypes;
    }
}
