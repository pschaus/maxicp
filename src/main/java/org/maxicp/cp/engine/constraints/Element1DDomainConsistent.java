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


/**
 *
 * Element Constraint modeling {@code array[y] = z}
 *
 */
public class Element1DDomainConsistent extends AbstractCPConstraint {

    private final int[] t;
    private final CPIntVar y;
    private final CPIntVar z;

    /**
     * Creates an element constraint {@code array[y] = z}
     *
     * @param array the array to index
     * @param y the index variable
     * @param z the result variable
     */
    public Element1DDomainConsistent(int[] array, CPIntVar y, CPIntVar z) {
        super(y.getSolver());
        this.t = array;
        this.y = y;
        this.z = z;
    }

    @Override
    public void post() {
        // STUDENT throw new NotImplementedException("Element1D");
        // BEGIN STRIP
        int [][] table = new int [t.length][2];
        for (int i = 0; i < table.length; i++) {
            table[i][0] = i;
            table[i][1] = t[i];
        }
        getSolver().post(new TableCT(new CPIntVar[]{y,z},table));
        // END STRIP
    }
}
