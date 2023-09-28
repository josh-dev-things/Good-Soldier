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
    
    abstract ITokenMap executeComparison(ITokenMap tM);

    abstract ITokenMap executeBOperator(ITokenMap tM);

    abstract ITokenMap executeOperator(ITokenMap tM);

    abstract ITokenMap executeKeyword(ITokenMap tM);

    abstract ITokenMap executeAssignment(ITokenMap tM);

    /**
     * This is me being funny. Good Soldier Script etc. so internally "reporting" is the same as "speaking" or outputting something. Let me have fun.
     * @param string will be output to console irrespective of debug argument
     */ 
    abstract void report(String string);

    /**
     * Pops element from array and returns new array of length n-1
     * @param arr
     * @param index
     * @return New String[] with specified element removed.
     */
    abstract String[] popIndexInStringArray(String[] arr, int index);
}
