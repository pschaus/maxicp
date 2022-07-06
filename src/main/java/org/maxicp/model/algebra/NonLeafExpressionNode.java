package org.maxicp.model.algebra;

import org.maxicp.model.ModelDispatcher;
import org.maxicp.util.exception.NotImplementedException;

public interface NonLeafExpressionNode<T extends Expression<T>> extends Expression<T> {
    /**
     * True if the variable is bound
     */
    default boolean isBound() {
        for(T t: subexpressions())
            if(!t.isBound())
                return false;
        return true;
    }

    /**
     * Returns the ModelDispatcher linked to this Expression
     */
    default ModelDispatcher getDispatcher() {
        return subexpressions()[0].getDispatcher();
    }
}
