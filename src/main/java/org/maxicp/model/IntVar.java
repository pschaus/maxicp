package org.maxicp.model;

import org.maxicp.model.algebra.VariableNotBoundException;
import org.maxicp.model.algebra.integer.IntExpression;
import org.maxicp.model.symbolic.IntVarViewOffset;

import java.util.Iterator;
import java.util.function.Function;

public interface IntVar extends Var, IntExpression {
    int min();
    int max();
    int size();
    boolean contains(int v);
    int fillArray(int[] array);
    default IntVar plus(int offset) {
        return new IntVarViewOffset(this, offset);
    }
    default IntVar minus(int offset) {
        return new IntVarViewOffset(this, -offset);
    }
    default int evaluate() throws VariableNotBoundException {
        if(size() != 1)
            throw new VariableNotBoundException();
        return min();
    }
    default boolean isBound() {
        return size() == 1;
    }
    default IntExpression mapSubexpressions(Function<IntExpression, IntExpression> f) {
        return this;
    }
    default IntExpression[] subexpressions() {
        return new IntExpression[0];
    }
}
