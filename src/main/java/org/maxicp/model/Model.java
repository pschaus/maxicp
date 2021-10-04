package org.maxicp.model;

import java.util.HashSet;
import java.util.Iterator;

public interface Model {
    void add(Constraint c);
    ConstraintListNode getCstNode();
    Iterable<Constraint> getConstraints();

    default Iterable<Var> getVariables() {
        HashSet<Var> allVars = new HashSet<>();
        for(Constraint c: getConstraints())
            for(Var v: c.scope())
                allVars.add(v);
        return allVars;
    }
}
