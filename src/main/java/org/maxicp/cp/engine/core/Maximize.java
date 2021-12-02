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

package org.maxicp.cp.engine.core;

import org.maxicp.search.Objective;
import org.maxicp.util.exception.InconsistencyException;

/**
 * Maximization objective function
 */
public class Maximize implements Objective {
    private int bound = Integer.MIN_VALUE;
    private final CPIntVar x;

    public Maximize(CPIntVar x) {
        this.x = x;
        x.getSolver().onFixPoint(() -> x.removeBelow(bound));
    }

    public void tighten() {
        if (!x.isBound()) throw new RuntimeException("objective not bound");
        this.bound = x.min() + 1;
        throw InconsistencyException.INCONSISTENCY;
    }
}
