package org.maxicp.model;

import org.maxicp.model.symbolic.SymbolicModel;

import java.util.HashSet;

public interface Model {
    void add(Constraint c);

    default Model symbolicCopy() {
        return new SymbolicModel(this);
    }

    ConstraintListNode getCstNode();
    Iterable<Constraint> getConstraints();
    ModelDispatcher getDispatcher();

    default Iterable<Var> getVariables() {
        HashSet<Var> allVars = new HashSet<>();
        for(Constraint c: getConstraints())
            for(Var v: c.scope())
                allVars.add(v);
        return allVars;
    }
}
