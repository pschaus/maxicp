package org.maxicp.model.constraints.sequence;

import org.maxicp.model.Constraint;
import org.maxicp.model.IntVar;
import org.maxicp.model.SequenceVar;
import org.maxicp.model.Var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class TransitionTimes implements Constraint {

    ArrayList<Var> s;
    public final SequenceVar sequenceVar;
    public final IntVar[] time;
    public final IntVar dist;
    public final int[][] distMatrix;
    public final int[] duration;

    @Override
    public Collection<Var> scope() {
        return s;
    }

    public TransitionTimes(SequenceVar seq, IntVar[] t, IntVar dist, int[][] distMatrix, int[] duration) {
        sequenceVar = seq;
        time = t;
        this.dist = dist;
        this.distMatrix = distMatrix;
        this.duration = duration;
        s = new ArrayList<>();
        s.add(seq);
        s.add(dist);
        s.addAll(Arrays.asList(t));
    }
}
