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
import org.maxicp.state.StateInt;
import org.maxicp.state.StateManager;
import org.maxicp.util.exception.InconsistencyException;

import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * Element Constraint modeling {@code array[y] = z}
 *
 */
public class Element1D extends AbstractCPConstraint {

    private final int[] t;


    private final Integer[] sortedPerm;
    private final StateInt low;
    private final StateInt up;

    private final CPIntVar y;
    private final CPIntVar z;


    /**
     * Creates an element constraint {@code array[y] = z}
     *
     * @param array the array to index
     * @param y the index variable
     * @param z the result variable
     */
    public Element1D(int[] array, CPIntVar y, CPIntVar z) {
        super(y.getSolver());
        this.t = array;

        sortedPerm = new Integer[t.length];
        for (int i = 0; i < t.length; i++) {
            sortedPerm[i] = i;
        }
        Arrays.sort(sortedPerm,Comparator.comparingInt(i -> t[i]));

        StateManager sm = getSolver().getStateManager();
        low = sm.makeStateInt(0);
        up = sm.makeStateInt(t.length - 1);

        this.y = y;
        this.z = z;
    }

    @Override
    public void post() {
        // STUDENT throw new NotImplementedException("Element1D");
        // BEGIN STRIP

        y.removeBelow(0);
        y.removeAbove(t.length - 1);
        z.removeBelow(t[sortedPerm[0]]);
        z.removeAbove(t[sortedPerm[t.length-1]]);

        y.propagateOnDomainChange(this);
        z.propagateOnBoundChange(this);
        propagate();

        // END STRIP
    }

    @Override
    public void propagate() {
        // STUDENT throw new NotImplementedException("Element1D");
        // BEGIN STRIP

        int l = low.value(), u = up.value();
        int zMin = z.min(), zMax = z.max();

        while (t[sortedPerm[l]] < zMin || !y.contains(sortedPerm[l])) {
            y.remove(sortedPerm[l]);
            l++;
            if (l > u) {
                throw new InconsistencyException();
            }
        }
        while (t[sortedPerm[u]] > zMax || !y.contains(sortedPerm[u])) {
            y.remove(sortedPerm[u]);
            u--;
            if (l > u) {
                throw new InconsistencyException();
            }
        }
        z.removeBelow(t[sortedPerm[l]]);
        z.removeAbove(t[sortedPerm[u]]);
        low.setValue(l);
        up.setValue(u);

        // END STRIP
    }
}
