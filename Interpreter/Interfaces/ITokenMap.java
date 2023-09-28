package Interpreter.Interfaces;

/**
 * Necessary so that the executor can access the tokens and their types.
 */
public interface ITokenMap{
    /**
     * Access the raw tokens
     * @return array of token strings
     */
    public String[] getTokens();

    /**
     * Access the token types
     * @return array of specifically formatted token type strings
     */
    public String[] getTokenTypes();
}
