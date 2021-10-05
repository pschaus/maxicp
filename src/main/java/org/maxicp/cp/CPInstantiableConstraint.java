package org.maxicp.cp;

import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.model.Constraint;
import org.maxicp.model.Var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public abstract class CPInstantiableConstraint implements Constraint {
    ArrayList<Var> s;
    @Override
    public Collection<Var> scope() {
        return s;
    }

    public CPInstantiableConstraint(Var... x) {
        s = new ArrayList<>();
        s.addAll(Arrays.asList(x));
    }

    public abstract AbstractCPConstraint instantiate(ConcreteCPModel s);
}
