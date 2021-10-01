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
 * Copyright (v)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package org.maxicp.cp.engine.constraints;

import org.maxicp.cp.CPFactory;
import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPBoolVar;
import org.maxicp.cp.engine.core.CPIntVar;

/**
 * Reified less or equal constraint.
 */
public class IsLessOrEqual extends AbstractCPConstraint { // b <=> x <= v

    private final CPBoolVar b;
    private final CPIntVar x;
    private final int v;

    /**
     * Creates a constraint that
     * link a boolean variable representing
     * whether one variable is less or equal to the given constant.
     * @param b a boolean variable that is true if and only if
     *         x takes a value less or equal to v
     * @param x the variable
     * @param v the constant
     * @see CPFactory#isLessOrEqual(CPIntVar, int)
     */
    public IsLessOrEqual(CPBoolVar b, CPIntVar x, int v) {
        super(b.getSolver());
        this.b = b;
        this.x = x;
        this.v = v;
    }

    @Override
    public void post() {
        // TODO
        // STUDENT throw new NotImplementedException("IsLessOrEqual");
        // BEGIN STRIP
        if (b.isTrue()) {
            x.removeAbove(v);
        } else if (b.isFalse()) {
            x.removeBelow(v + 1);
        } else if (x.max() <= v) {
            b.assign(1);
        } else if (x.min() > v) {
            b.assign(0);
        } else {
            b.whenBind(() -> {
                // should deactivate the constraint as it is entailed
                if (b.isTrue()) {
                    x.removeAbove(v);

                } else {
                    x.removeBelow(v + 1);
                }
            });
            x.whenBoundsChange(() -> {
                if (x.max() <= v) {
                    // should deactivate the constraint as it is entailed
                    b.assign(1);
                } else if (x.min() > v) {
                    // should deactivate the constraint as it is entailed
                    b.assign(0);
                }
            });
        }
        // END STRIP
    }
}
