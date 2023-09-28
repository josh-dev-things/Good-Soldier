package Interpreter.Interfaces;

public interface IExecutor {
    /**
     * Executes a set of tokens. Calls appropriate methods as needed.
     * @param tM
     * @see executeComparison
     * @see executeBOperator
     * @see executeOperator
     * @see executeKeyword
     * @see executeAssignment
     * @return integer. < 0 indicates error.
     */
    public int execute(ITokenMap tM);
}
