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

import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPIntVar;

/**
 * Maximum Constraint
 */
public class Maximum extends AbstractCPConstraint {

    private final CPIntVar[] x;
    private final CPIntVar y;

    /**
     * Creates the maximum constraint y = maximum(x[0],x[1],...,x[n])?
     *
     * @param x the variable on which the maximum is to be found
     * @param y the variable that is equal to the maximum on x
     */
    public Maximum(CPIntVar[] x, CPIntVar y) {
        super(x[0].getSolver());
        assert (x.length > 0);
        this.x = x;
        this.y = y;
    }


    @Override
    public void post() {
        for (CPIntVar xi : x) {
            xi.propagateOnBoundChange(this);
        }
        y.propagateOnBoundChange(this);
        propagate();
    }


    @Override
    public void propagate() {
        int max = Integer.MIN_VALUE;
        int min = Integer.MIN_VALUE;
        int nSupport = 0;
        int supportIdx = -1;
        for (int i = 0; i < x.length; i++) {
            x[i].removeAbove(y.max());

            if (x[i].max() > max) {
                max = x[i].max();
            }
            if (x[i].min() > min) {
                min = x[i].min();
            }

            if (x[i].max() >= y.min()) {
                nSupport += 1;
                supportIdx = i;
            }
        }
        if (nSupport == 1) {
            x[supportIdx].removeBelow(y.min());
        }
        y.removeAbove(max);
        y.removeBelow(min);
    }
}
