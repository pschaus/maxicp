package org.maxicp.model.algebra;

import org.maxicp.model.ModelDispatcher;
import org.maxicp.util.exception.NotImplementedException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Function;

/**
 * An expression, that can be floating, integer or boolean
 */
public interface Expression<T extends Expression<T>> extends Serializable {
    /**
     * Returns an iterable that contains all sub-expressions of this expression
     */
    T[] subexpressions();

    /**
     * Apply a function on all sub-expressions of this expression and returns a new expression of the same type.
     * This function should return a value that is of the class as the object that was given to it.
     */
    T mapSubexpressions(Function<T, T> f);

    /**
     * True if the variable is bound
     */
    boolean isBound();

    /**
     * Returns the ModelDispatcher linked to this Expression
     */
    ModelDispatcher getDispatcher();
}
