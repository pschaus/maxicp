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

public class Element1DVar extends AbstractCPConstraint {

    private final CPIntVar[] array;
    private final CPIntVar y;
    private final CPIntVar z;

    private final int[] yValues;
    private CPIntVar supMin;
    private CPIntVar supMax;
    private int zMin;
    private int zMax;

    public Element1DVar(CPIntVar[] array, CPIntVar y, CPIntVar z) {
        super(y.getSolver());
        this.array = array;
        this.y = y;
        this.z = z;

        yValues = new int[y.size()];
    }

    @Override
    public void post() {
        y.removeBelow(0);
        y.removeAbove(array.length - 1);

        for (CPIntVar t : array) {
            t.propagateOnBoundChange(this);
        }
        y.propagateOnDomainChange(this);
        z.propagateOnBoundChange(this);

        propagate();
    }

    @Override
    public void propagate() {
        zMin = z.min();
        zMax = z.max();
        if (y.isFixed()) equalityPropagate();
        else {
            filterY();
            if (y.isFixed())
                equalityPropagate();
            else {
                z.removeBelow(supMin.min());
                z.removeAbove(supMax.max());
            }
        }

    }

    private void equalityPropagate() {
        int id = y.min();
        CPIntVar tVar = array[id];
        tVar.removeBelow(zMin);
        tVar.removeAbove(zMax);
        z.removeBelow(tVar.min());
        z.removeAbove(tVar.max());
    }

    private void filterY() {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        int i = y.fillArray(yValues);
        while (i > 0) {
            i -= 1;
            int id = yValues[i];
            CPIntVar tVar = array[id];
            int tMin = tVar.min();
            int tMax = tVar.max();
            if (tMax < zMin || tMin > zMax) {
                y.remove(id);
            } else {
                if (tMin < min) {
                    min = tMin;
                    supMin = tVar;
                }
                if (tMax > max) {
                    max = tMax;
                    supMax = tVar;
                }
            }
        }
    }

}
