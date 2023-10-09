package Interpreter.Interfaces;

/**
 * Parser converts a string to a token map.
 */
public interface IParser {
    public ITokenMap parse(String expression);
}
