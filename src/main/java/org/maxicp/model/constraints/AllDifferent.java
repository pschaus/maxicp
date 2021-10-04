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
    @Override
    public Collection<Var> scope() {
        return s;
    }

    public AllDifferent(IntVar... x) {
        s = new ArrayList<>();
        s.addAll(Arrays.asList(x));
    }
}
