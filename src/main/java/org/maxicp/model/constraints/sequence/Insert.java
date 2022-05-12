package org.maxicp.model.constraints.sequence;

import org.maxicp.model.Constraint;
import org.maxicp.model.SequenceVar;
import org.maxicp.model.Var;

import java.util.ArrayList;
import java.util.Collection;

public class Insert implements Constraint {

    ArrayList<Var> s;
    public final SequenceVar sequenceVar;
    public final int pred;
    public final int node;

    @Override
    public Collection<Var> scope() {
        return null;
    }

    public Insert(SequenceVar seq, int pred, int node) {
        this.sequenceVar = seq;
        this.pred = pred;
        this.node = node;
        s = new ArrayList<>();
        s.add(seq);
    }
}
