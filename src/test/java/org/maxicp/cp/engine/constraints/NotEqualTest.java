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

import org.maxicp.cp.engine.CPSolverTest;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.util.exception.InconsistencyException;
import org.junit.Test;
import org.maxicp.Factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class NotEqualTest extends CPSolverTest {

    @Test
    public void notEqualTest() {
        CPSolver cp = solverFactory.get();

        CPIntVar x = Factory.makeIntVar(cp, 10);
        CPIntVar y = Factory.makeIntVar(cp, 10);

        try {
            cp.post(Factory.notEqual(x, y));

            cp.post(Factory.equal(x, 6));

            assertFalse(y.contains(6));
            assertEquals(9, y.size());

        } catch (InconsistencyException e) {
            assert (false);
        }
        assertFalse(y.contains(6));
    }

}
