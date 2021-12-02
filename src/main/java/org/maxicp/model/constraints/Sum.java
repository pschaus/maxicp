package org.maxicp.model.constraints;

import org.maxicp.model.Constraint;
import org.maxicp.model.IntVar;
import org.maxicp.model.Var;

import java.util.Collection;
import java.util.List;

public class Sum implements Constraint {

    public final IntVar [] x;
    public final IntVar y;

    @Override
    public Collection<Var> scope() {
        List<Var> vars =  List.of(x);
        vars.add(y);
        return vars;
    }

    public Sum(IntVar [] x, IntVar y) {
        this.x = x;
        this.y = y;
    }
}
