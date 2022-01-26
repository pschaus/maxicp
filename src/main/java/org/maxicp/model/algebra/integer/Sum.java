package org.maxicp.model.algebra.integer;

import org.maxicp.model.ModelDispatcher;
import org.maxicp.model.algebra.NonLeafExpressionNode;
import org.maxicp.model.algebra.VariableNotBoundException;

import java.util.Arrays;
import java.util.function.Function;

public class Sum implements IntExpression, NonLeafExpressionNode<IntExpression> {
    private IntExpression[] subexprs;
    public Sum(IntExpression[] subexprs) {
        this.subexprs = subexprs;
    }

    @Override
    public IntExpression[] subexpressions() {
        return subexprs;
    }

    @Override
    public IntExpression mapSubexpressions(Function<IntExpression, IntExpression> f) {
        return new Sum(Arrays.stream(subexprs).map(f).toArray(IntExpression[]::new));
    }

    @Override
    public int evaluate() throws VariableNotBoundException {
        int c = 0;
        for(IntExpression expr: subexprs)
            c += expr.evaluate();
        return c;
    }

    @Override
    public int min() {
        int c = 0;
        for(IntExpression expr: subexprs)
            c += expr.min();
        return c;
    }

    @Override
    public int max() {
        int c = 0;
        for(IntExpression expr: subexprs)
            c += expr.max();
        return c;
    }
}
