package org.maxicp.model.algebra.integer;

import org.maxicp.model.IntVar;
import org.maxicp.model.algebra.Expression;
import org.maxicp.model.algebra.VariableNotBoundException;
import org.maxicp.model.constraints.Equal;

import java.util.function.Function;

public interface IntExpression extends Expression<IntExpression> {
    /**
     * Evaluate this expression. All variables referenced have to be bound.
     * @throws VariableNotBoundException when a variable is not bound
     * @return the value of this expression
     */
    int evaluate() throws VariableNotBoundException;

    /**
     * Return a *lower bound* for this expression
     */
    int min();

    /**
     * Return a *higher bound* for this expression
     */
    int max();
}