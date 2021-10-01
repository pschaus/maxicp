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
import org.maxicp.cp.engine.core.CPIntVar;

/**
 * Not Equal constraint between two variables
 */
public class NotEqual extends AbstractCPConstraint {
    private final CPIntVar x, y;
    private final int v;

    /**
     * Creates a constraint such
     * that {@code x != y + v}
     * @param x the left member
     * @param y the right memer
     * @param v the offset value on y
     * @see Factory#notEqual(CPIntVar, CPIntVar, int)
     */
    public NotEqual(CPIntVar x, CPIntVar y, int v) { // x != y + v
        super(x.getSolver());
        this.x = x;
        this.y = y;
        this.v = v;
    }

    /**
     * Creates a constraint such
     * that {@code x != y}
     * @param x the left member
     * @param y the right memer
     * @see Factory#notEqual(CPIntVar, CPIntVar)
     */
    public NotEqual(CPIntVar x, CPIntVar y) { // x != y
        this(x, y, 0);
    }

    @Override
    public void post() {
        if (y.isBound())
            x.remove(y.min() + v);
        else if (x.isBound())
            y.remove(x.min() - v);
        else {
            x.propagateOnBind(this);
            y.propagateOnBind(this);
        }
    }

    @Override
    public void propagate() {
        if (y.isBound())
            x.remove(y.min() + v);
        else y.remove(x.min() - v);
        setActive(false);
    }
}
