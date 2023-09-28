package Interpreter;

import Interpreter.Interfaces.ITokenMap;

public class TokenMap implements ITokenMap{
    private String[] tokens;
    private String[] tokenTypes;

    public TokenMap(String[] tokens, String[] tokenTypes)
    {
        this.tokens = tokens;
        this.tokenTypes = tokenTypes;
    }

    @Override
    public String[] getTokens()
    {
        return tokens;
    }

    @Override
    public String[] getTokenTypes()
    {
        return tokenTypes;
    }
}
