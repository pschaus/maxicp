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

import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.Objective;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.exception.NotImplementedException;
import org.maxicp.util.NotImplementedExceptionAssume;
import org.junit.Test;
import org.maxicp.BranchingScheme;
import org.maxicp.cp.CPFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MaximizeTest {

    @Test
    public void maximizeTest() {
        try {
            try {

                CPSolver cp = CPFactory.makeSolver();
                CPIntVar y = CPFactory.makeIntVar(cp, 10, 20);

                CPIntVar[] x = new CPIntVar[]{y};
                DFSearch dfs = CPFactory.makeDfs(cp, () -> y.isFixed() ? BranchingScheme.EMPTY :
                        BranchingScheme.branch(() -> cp.post(CPFactory.equal(y, y.min())),
                                () -> cp.post(CPFactory.notEqual(y, y.min()))));
                Objective obj = cp.maximize(y);

                SearchStatistics stats = dfs.solve();

                assertEquals(11, stats.numberOfSolutions());


            } catch (InconsistencyException e) {
                fail("should not fail");
            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }


}
