package org.maxicp.model.constraints;

import org.maxicp.model.Constraint;
import org.maxicp.model.IntVar;
import org.maxicp.model.Var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class AllDifferent implements Constraint {
    ArrayList<Var> s;
    public final IntVar[] x;

    @Override
    public Collection<Var> scope() {
        return s;
    }

    public AllDifferent(IntVar... x) {
        this.x = x;
        s = new ArrayList<>();
        s.addAll(Arrays.asList(x));
    }
}
