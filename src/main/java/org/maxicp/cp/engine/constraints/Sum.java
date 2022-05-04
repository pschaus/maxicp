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

package org.maxicp.cp.engine.constraints;

import org.maxicp.cp.CPFactory;
import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.state.State;
import org.maxicp.state.StateInt;
import org.maxicp.util.exception.InconsistencyException;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Sum Constraint
 */
public class Sum extends AbstractCPConstraint {

    private int[] free;
    private StateInt nFrees;
    private State<Long> sumFixed;
    private CPIntVar[] x;
    private int[] min, max;
    private int n;

    /**
     * Creates a sum constraint.
     * <p> This constraint holds iff
     * {@code x[0]+x[1]+...+x[x.length-1] == y}.
     *
     * @param x the non empty left hand side of the sum
     * @param y the right hand side of the sum
     */
    public Sum(CPIntVar[] x, CPIntVar y) {
        this(Arrays.copyOf(x, x.length + 1));
        this.x[x.length] = CPFactory.minus(y);
    }

    /**
     * Creates a sum constraint.
     * <p> This constraint holds iff
     * {@code x[0]+x[1]+...+x[x.length-1] == y}.
     *
     * @param x the non empty left hand side of the sum
     * @param y the right hand side of the sum
     */
    public Sum(CPIntVar[] x, int y) {
        this(Arrays.copyOf(x, x.length + 1));
        this.x[x.length] = CPFactory.makeIntVar(getSolver(), -y, -y);
    }

    /**
     * Creates a sum constraint.
     * <p> This constraint holds iff
     * {@code x[0]+x[1]+...+x[x.length-1] == 0}.
     *
     * @param x the non empty set of variables that should sum to zero
     */
    public Sum(CPIntVar[] x) {
        super(x[0].getSolver());
        this.x = x;
        this.n = x.length;
        min = new int[x.length];
        max = new int[x.length];
        nFrees = getSolver().getStateManager().makeStateInt(n);
        sumFixed = getSolver().getStateManager().makeStateRef(Long.valueOf(0));
        free = IntStream.range(0, n).toArray();
    }

    @Override
    public void post() {
        for (CPIntVar var : x)
            var.propagateOnBoundChange(this);
        propagate();
    }

    @Override
    public void propagate() {
        // Filter the unbound vars and update the partial sum
        int nU = nFrees.value();
        long sumMin = sumFixed.value(), sumMax = sumFixed.value();
        for (int i = nU - 1; i >= 0; i--) {
            int idx = free[i];
            min[idx] = x[idx].min();
            max[idx] = x[idx].max();
            sumMin += min[idx]; // Update partial sum
            sumMax += max[idx];
            if (x[idx].isFixed()) {
                sumFixed.setValue(sumFixed.value() + x[idx].min());
                free[i] = free[nU - 1]; // Swap the variables
                free[nU - 1] = idx;
                nU--;
            }
        }
        nFrees.setValue(nU);
        if (sumMin > 0 || sumMax < 0) {
            throw new InconsistencyException();
        }

        for (int i = nU - 1; i >= 0; i--) {
            int idx = free[i];
            x[idx].removeAbove(-((int) (sumMin - min[idx])));
            x[idx].removeBelow(-((int) (sumMax - max[idx])));
        }
    }
}
