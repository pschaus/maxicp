package org.maxicp.model.constraints.sequence;

import org.maxicp.model.Constraint;
import org.maxicp.model.SequenceVar;
import org.maxicp.model.Var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Disjoint implements Constraint {

    ArrayList<Var> s;
    public final SequenceVar[] sequenceVar;

    @Override
    public Collection<Var> scope() {
        return s;
    }

    public Disjoint(SequenceVar... sequenceVars) {
        s = new ArrayList<>();
        s.addAll(Arrays.asList(sequenceVars));
        this.sequenceVar = sequenceVars;
    }
}
