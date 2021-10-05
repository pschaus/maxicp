package org.maxicp.search;

import org.maxicp.BranchingScheme;
import org.maxicp.util.Procedure;

import java.util.function.Supplier;

public class LimitedDepthBranching implements Supplier<Procedure[]> {

    private int curD;
    private final int maxD;
    private final Supplier<Procedure[]> bs;

    /**
     * Creates a depth-limited combinator on a given branching.
     *
     * @param branching the branching on which to apply the depth-limiting combinator
     * @param maxDepth the maximum depth limit. Any node exceeding that limit is pruned.
     */
    public LimitedDepthBranching(Supplier<Procedure[]> branching, int maxDepth) {
        if (maxDepth < 0) throw new IllegalArgumentException("max depth should be >= 0");
        this.bs = branching;
        this.maxD = maxDepth;
    }

    @Override
    public Procedure[] get() {
        if(curD == maxD)
            return BranchingScheme.EMPTY;

        Procedure[] branches = bs.get();

        Procedure[] newBranches = new Procedure[branches.length];
        for (int i = 0; i < newBranches.length; i++) {
            int fi = i;
            int d = curD + 1; // branch index
            newBranches[i] = () -> {
                curD = d; // update depth
                branches[fi].call();
            };
        }

        return newBranches;
    }
}