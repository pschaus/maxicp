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
import org.maxicp.cp.engine.core.CPConstraint;
import org.maxicp.cp.engine.core.CPIntVar;

import static org.maxicp.cp.CPFactory.lessOrEqual;
import static org.maxicp.cp.CPFactory.plus;

/**
 * Reified is less or equal constraint {@code b <=> x <= y}.
 */
public class IsLessOrEqualVar extends AbstractCPConstraint {

    private final CPBoolVar b;
    private final CPIntVar x;
    private final CPIntVar y;

    private final CPConstraint lEqC;
    private final CPConstraint grC;

    /**
     * Creates a reified is less or equal constraint {@code b <=> x <= y}.
     * @param b the truth value that will be set to true if {@code x <= y}, false otherwise
     * @param x left hand side of less or equal operator
     * @param y right hand side of less or equal operator
     */
    public IsLessOrEqualVar(CPBoolVar b, CPIntVar x, CPIntVar y) {
        super(x.getSolver());
        this.b = b;
        this.x = x;
        this.y = y;
        lEqC = lessOrEqual(x, y);
        grC = lessOrEqual(plus(y, 1), x);
    }

    @Override
    public void post() {
        x.propagateOnBoundChange(this);
        y.propagateOnBoundChange(this);
        b.propagateOnBind(this);
        propagate();
    }

    @Override
    public void propagate() {
        if (b.isTrue()) {
            getSolver().post(lEqC, false);
            setActive(false);
        } else if (b.isFalse()) {
            getSolver().post(grC, false);
            setActive(false);
        } else {
            if (x.max() <= y.min()) {
                b.assign(1);
                setActive(false);
            } else if (x.min() > y.max()) {
                b.assign(0);
                setActive(false);
            }
        }
    }
}
