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

package org.maxicp.engine.constraints;

import org.maxicp.cp.Factory;
import org.maxicp.engine.core.AbstractConstraint;
import org.maxicp.engine.core.BoolVar;
import org.maxicp.engine.core.IntVar;

/**
 * Reified less or equal constraint.
 */
public class IsLessOrEqual extends AbstractConstraint { // b <=> x <= v

    private final BoolVar b;
    private final IntVar x;
    private final int v;

    /**
     * Creates a constraint that
     * link a boolean variable representing
     * whether one variable is less or equal to the given constant.
     * @param b a boolean variable that is true if and only if
     *         x takes a value less or equal to v
     * @param x the variable
     * @param v the constant
     * @see Factory#isLessOrEqual(IntVar, int)
     */
    public IsLessOrEqual(BoolVar b, IntVar x, int v) {
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
