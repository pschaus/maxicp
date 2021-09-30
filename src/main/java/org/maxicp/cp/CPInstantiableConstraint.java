package org.maxicp.cp;

import org.maxicp.cp.engine.core.AbstractConstraint;
import org.maxicp.cp.engine.core.Solver;
import org.maxicp.model.Constraint;
import org.maxicp.model.IntVar;
import org.maxicp.model.Var;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class CPInstantiableConstraint implements Constraint {
    ArrayList<Var> s;
    @Override
    public Iterable<Var> scope() {
        return s;
    }

    public CPInstantiableConstraint(Var... x) {
        s = new ArrayList<>();
        s.addAll(Arrays.asList(x));
    }

    public abstract AbstractConstraint instantiate(Solver s);
}
