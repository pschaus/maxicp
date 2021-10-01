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
import org.maxicp.cp.engine.core.IntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.exception.InconsistencyException;
import org.junit.Test;
import org.maxicp.BranchingScheme;
import org.maxicp.Factory;

import static org.junit.Assert.assertEquals;


public class AllDifferentTest extends CPSolverTest {

    @Test
    public void allDifferentTest1() {

        CPSolver cp = solverFactory.get();

        IntVar[] x = Factory.makeIntVarArray(cp, 5, 5);

        try {
            cp.post(Factory.allDifferent(x));
            cp.post(Factory.equal(x[0], 0));
            for (int i = 1; i < x.length; i++) {
                assertEquals(4, x[i].size());
                assertEquals(1, x[i].min());
            }

        } catch (InconsistencyException e) {
            assert (false);
        }
    }


    @Test
    public void allDifferentTest2() {

        CPSolver cp = solverFactory.get();

        IntVar[] x = Factory.makeIntVarArray(cp, 5, 5);

        try {
            cp.post(Factory.allDifferent(x));

            SearchStatistics stats = Factory.makeDfs(cp, BranchingScheme.firstFail(x)).solve();
            assertEquals(120, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            assert (false);
        }
    }

}
