package org.maxicp.model.constraints;

import org.maxicp.model.Constraint;
import org.maxicp.model.IntVar;
import org.maxicp.model.Var;

import java.util.ArrayList;
import java.util.Collection;

public class LessOrEqual implements Constraint {

    ArrayList<Var> s;
    @Override
    public Collection<Var> scope() {
        return s;
    }

    public final IntVar x, y;

    public LessOrEqual(IntVar x, IntVar y) {
        this.x = x;
        this.y = y;
        s = new ArrayList<>();
        s.add(x);
        s.add(y);
    }
}
