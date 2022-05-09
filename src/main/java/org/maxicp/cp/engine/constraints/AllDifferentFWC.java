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

import java.util.stream.IntStream;

public class AllDifferentFWC extends AbstractCPConstraint {

    private CPIntVar[] x;

    // STUDENT
    // BEGIN STRIP
    private final StateInt nFixed;
    private final int[] fixed;
    // END STRIP

    public AllDifferentFWC(CPIntVar... x) {
        super(x[0].getSolver());
        this.x = x;
        // STUDENT
        // BEGIN STRIP
        nFixed = getSolver().getStateManager().makeStateInt(0);
        fixed = IntStream.range(0, x.length).toArray();
        // END STRIP
    }

    @Override
    public void post() {
        // STUDENT throw new NotImplementedException("AllDifferentFWC");
        // BEGIN STRIP
        for (CPIntVar var : x) {
            if (!var.isFixed())
                var.propagateOnFix(this);
        }
        propagate();
        // END STRIP
    }

    @Override
    public void propagate() {
        // TODO use the sparse-set trick as seen in Sum.java
        // STUDENT throw new NotImplementedException("AllDifferentFWC");
        // BEGIN STRIP
        int nF = nFixed.value();
        // iterate over non fixed variables
        for (int i = nF; i < x.length; i++) {
            int idx = fixed[i];
            if (x[idx].isFixed()) {
                // x[idx] is fixed so this value should be removed from unfixed
                int val = x[fixed[i]].min();
                for (int j = i+1; j < x.length; j++) {
                    x[fixed[j]].remove(val);
                }
                fixed[i] = fixed[nF]; // Swap the variables
                fixed[nF] = idx;
                nF++;
            }
        }
        nFixed.setValue(nF);
        // END STRIP
    }
}