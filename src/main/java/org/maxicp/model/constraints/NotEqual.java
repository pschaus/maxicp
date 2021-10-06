package org.maxicp.model.constraints;

import org.maxicp.model.Constraint;
import org.maxicp.model.IntVar;
import org.maxicp.model.Var;

import java.util.ArrayList;
import java.util.Collection;

public class NotEqual implements Constraint {
    ArrayList<Var> s;
    @Override
    public Collection<Var> scope() {
        return s;
    }

    public final int v;
    public final IntVar x;

    public NotEqual(IntVar x, int v) {
        this.x = x;
        this.v = v;
        s = new ArrayList<>();
        s.add(x);
    }
}
