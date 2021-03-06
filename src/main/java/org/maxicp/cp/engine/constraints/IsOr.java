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
import org.maxicp.cp.engine.core.CPBoolVar;
import org.maxicp.state.StateInt;


/**
 * Reified logical or constraint
 */
public class IsOr extends AbstractCPConstraint { // b <=> x1 or x2 or ... xn

    private final CPBoolVar b;
    private final CPBoolVar[] x;
    private final int n;

    private int[] unBounds;
    private StateInt nUnBounds;

    private final Or or;

    /**
     * Creates a constraint such that
     * the boolean b is true if and only if
     * at least variable in x is true.
     *
     * @param b the boolean that is true if at least one variable in x is true
     * @param x an non empty array of variables
     */
    public IsOr(CPBoolVar b, CPBoolVar[] x) {
        super(b.getSolver());
        this.b = b;
        this.x = x;
        this.n = x.length;
        or = new Or(x);

        nUnBounds = getSolver().getStateManager().makeStateInt(n);
        unBounds = new int[n];
        for (int i = 0; i < n; i++) {
            unBounds[i] = i;
        }
    }

    @Override
    public void post() {
        b.propagateOnFix(this);
        for (CPBoolVar xi : x) {
            xi.propagateOnFix(this);
        }
    }

    @Override
    public void propagate() {
        if (b.isTrue()) {
            setActive(false);
            getSolver().post(or, false);
        } else if (b.isFalse()) {
            for (CPBoolVar xi : x) {
                xi.fix(false);
            }
            setActive(false);
        } else {
            int nU = nUnBounds.value();
            for (int i = nU - 1; i >= 0; i--) {
                int idx = unBounds[i];
                CPBoolVar y = x[idx];
                if (y.isFixed()) {
                    if (y.isTrue()) {
                        b.fix(true);
                        setActive(false);
                        return;
                    }
                    // Swap the variable
                    unBounds[i] = unBounds[nU - 1];
                    unBounds[nU - 1] = idx;
                    nU--;
                }
            }
            if (nU == 0) {
                b.fix(false);
                setActive(false);
            }
            nUnBounds.setValue(nU);
        }
    }
}
