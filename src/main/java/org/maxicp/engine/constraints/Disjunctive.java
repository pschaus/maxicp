/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */


package org.maxicp.engine.constraints;

import org.maxicp.cp.Factory;
import org.maxicp.engine.core.AbstractConstraint;
import org.maxicp.engine.core.BoolVar;
import org.maxicp.engine.core.IntVar;
import org.maxicp.util.exception.InconsistencyException;

import java.util.Arrays;
import java.util.Comparator;

import static org.maxicp.cp.Factory.*;

/**
 * Disjunctive Scheduling Constraint:
 * Any two pairs of activities cannot overlap in time.
 */
public class Disjunctive extends AbstractConstraint {

    private final IntVar[] start;
    private final int[] duration;
    private final IntVar[] end;

    // STUDENT
    // BEGIN STRIP
    private final Integer[] permEst;
    private final int[] rankEst;
    private final int[] startMin;
    private final int[] endMax;
    private final Integer[] permLct;
    private final Integer[] permLst;
    private final Integer[] permEct;

    private final boolean[] inserted;

    private final ThetaTree thetaTree;

    private final boolean postMirror;
    // END STRIP

    /**
     * Creates a disjunctive constraint that enforces
     * that for any two pair i,j of activities we have
     * {@code start[i]+duration[i] <= start[j] or start[j]+duration[j] <= start[i]}.
     *
     * @param start the start times of the activities
     * @param duration the durations of the activities
     */
    public Disjunctive(IntVar[] start, int[] duration) {
        this(start, duration, true);
    }


    private Disjunctive(IntVar[] start, int[] duration, boolean postMirror) {
        super(start[0].getSolver());
        this.start = start;
        this.duration = duration;
        this.end = Factory.makeIntVarArray(start.length, i -> plus(start[i], duration[i]));

        // STUDENT
        // BEGIN STRIP
        this.postMirror = postMirror;
        permEst = new Integer[start.length];
        rankEst = new int[start.length];
        permLct = new Integer[start.length];
        permLst = new Integer[start.length];
        permEct = new Integer[start.length];
        inserted = new boolean[start.length];

        for (int i = 0; i < start.length; i++) {
            permEst[i] = i;
            permLct[i] = i;
            permLst[i] = i;
            permEct[i] = i;
        }
        thetaTree = new ThetaTree(start.length);

        startMin = new int[start.length];
        endMax = new int[start.length];
        // END STRIP
    }


    @Override
    public void post() {

        int[] demands = new int[start.length];
        for (int i = 0; i < start.length; i++) {
            demands[i] = 1;
        }
        getSolver().post(new Cumulative(start, duration, demands, 1), false);


        // TODO 1: replace by  posting  binary decomposition using IsLessOrEqualVar
        // TODO 2: add the mirror filtering as done in the Cumulative Constraint
        // STUDENT throw new NotImplementedException("Disjunctive");
        // BEGIN STRIP
        for (int i = 0; i < start.length; i++) {
            start[i].propagateOnBoundChange(this);
        }


        if (postMirror) {
            for (int i = 0; i < start.length; i++) {
                IntVar endi = plus(start[i], duration[i]);
                for (int j = i + 1; j < start.length; j++) {
                    IntVar endj = plus(start[j], duration[j]);
                    BoolVar iBeforej = makeBoolVar(getSolver());
                    BoolVar jBeforei = makeBoolVar(getSolver());

                    getSolver().post(new IsLessOrEqualVar(iBeforej, endi, start[j]));
                    getSolver().post(new IsLessOrEqualVar(jBeforei, endj, start[i]));
                    getSolver().post(new NotEqual(iBeforej, jBeforei), false);

                    // TODO i before j or j before i
                }
            }


            IntVar[] startMirror = Factory.makeIntVarArray(start.length, i -> minus(end[i]));
            getSolver().post(new Disjunctive(startMirror, duration, false), false);

            propagate();
        }
        // END STRIP

    }

    @Override
    public void propagate() {
        // HINT: for the TODO 1-4 you'll need the ThetaTree data-structure

        // TODO 3: add the OverLoadCheck algorithms

        // TODO 4: add the Detectable Precedences algorithm

        // TODO 5: add the Not-Last algorithm

        // TODO 6 (optional, for a bonus): implement the Lambda-Theta tree and implement the Edge-Finding        overLoadChecker();

        boolean fixed = false;
        while (!fixed) {
            fixed = true;
            overLoadChecker();
            fixed =  fixed && !detectablePrecedence();
            fixed =  fixed && !notLast();
        }

    }
    // STUDENT
    // BEGIN STRIP
    private void update() {
        Arrays.sort(permEst, Comparator.comparingInt(i -> start[i].min()));
        for (int i = 0; i < start.length; i++) {
            rankEst[permEst[i]] = i;
            startMin[i] = start[i].min();
            endMax[i] = end[i].max();
        }
    }
    // END STRIP

    private void overLoadChecker() {
        // STUDENT throw new NotImplementedException("Disjunctive");
        // BEGIN STRIP
        update();
        Arrays.sort(permLct, Comparator.comparingInt(i -> end[i].max()));
        thetaTree.reset();
        for (int i = 0; i < start.length; i++) {
            int activity = permLct[i];
            thetaTree.insert(rankEst[activity], end[activity].min(), duration[activity]);
            if (thetaTree.getECT() > end[activity].max()) {
                throw new InconsistencyException();
            }
        }
        // END STRIP
    }

    /**
     * @return true if one domain was changed by the detectable precedence algo
     */
    private boolean detectablePrecedence() {
        // STUDENT throw new NotImplementedException("Disjunctive");
        // BEGIN STRIP
        update();
        boolean changed = false;
        Arrays.sort(permLst, Comparator.comparingInt(i -> start[i].max()));
        Arrays.sort(permEct, Comparator.comparingInt(i -> end[i].min()));
        Arrays.fill(inserted, false);
        int idxj = 0;
        int j = permLst[idxj];
        thetaTree.reset();
        for (int acti : permEct) {
            while (idxj < start.length && end[acti].min() > start[permLst[idxj]].max()) {
                j = permLst[idxj];
                inserted[j] = true;
                thetaTree.insert(rankEst[j], end[j].min(), duration[j]);
                idxj++;
            }
            if (inserted[acti]) {
                thetaTree.remove(rankEst[acti]);
                startMin[acti] = Math.max(startMin[acti], thetaTree.getECT());
                thetaTree.insert(rankEst[acti], end[acti].min(), duration[acti]);
            } else {
                startMin[acti] = Math.max(startMin[acti], thetaTree.getECT());
            }
        }

        for (int i = 0; i < start.length; i++) {
            changed = changed || (startMin[i] > start[i].min());
            start[i].removeBelow(startMin[i]);
        }
        return changed;
        // END STRIP
    }

    /**
     * @return true if one domain was changed by the not-last algo
     */
    private boolean notLast() {
        // STUDENT throw new NotImplementedException("Disjunctive");
        // BEGIN STRIP
        update();
        boolean changed = false;
        Arrays.sort(permLst, Comparator.comparingInt(i -> start[i].max()));
        Arrays.sort(permLct, Comparator.comparingInt(i -> end[i].max()));
        Arrays.fill(inserted, false);
        int idxj = 0;
        int j = permLst[idxj];
        thetaTree.reset();
        for (int acti : permLct) {
            while (idxj < start.length && end[acti].max() > start[permLst[idxj]].max()) {
                j = permLst[idxj];
                inserted[j] = true;
                thetaTree.insert(rankEst[j], end[j].min(), duration[j]);
                idxj++;
            }
            if (inserted[acti]) {
                thetaTree.remove(rankEst[acti]);
                if (thetaTree.getECT() > start[acti].max()) {
                    endMax[acti] = start[j].max();
                }
                thetaTree.insert(rankEst[acti], end[acti].min(), duration[acti]);
            } else {
                if (thetaTree.getECT() > start[acti].max()) {
                    endMax[acti] = start[j].max();
                }
            }
        }

        for (int i = 0; i < start.length; i++) {
            changed = changed || (endMax[i] < end[i].max());
            end[i].removeAbove(endMax[i]);
        }
        return changed;
        // END STRIP
    }


}
