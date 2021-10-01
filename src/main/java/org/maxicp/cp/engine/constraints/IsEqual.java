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

import org.maxicp.Factory;
import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPBoolVar;
import org.maxicp.cp.engine.core.CPIntVar;


/**
 * Reified equality constraint
 * @see Factory#isEqual(CPIntVar, int)
 */
public class IsEqual extends AbstractCPConstraint { // b <=> x == v

    private final CPBoolVar b;
    private final CPIntVar x;
    private final int v;

    /**
     * Returns a boolean variable representing
     * whether one variable is equal to the given constant.
     * @param x the variable
     * @param v the constant
     * @param b the boolean variable that is set to true
     *          if and only if x takes the value v
     * @see Factory#isEqual(CPIntVar, int)
     */
    public IsEqual(CPBoolVar b, CPIntVar x, int v) {
        super(b.getSolver());
        this.b = b;
        this.x = x;
        this.v = v;
    }

    @Override
    public void post() {
        propagate();
        if (isActive()) {
            x.propagateOnDomainChange(this);
            b.propagateOnBind(this);
        }
    }

    @Override
    public void propagate() {
        if (b.isTrue()) {
            x.assign(v);
            setActive(false);
        } else if (b.isFalse()) {
            x.remove(v);
            setActive(false);
        } else if (!x.contains(v)) {
            b.assign(false);
            setActive(false);
        } else if (x.isBound()) {
            b.assign(true);
            setActive(false);
        }
    }
}
