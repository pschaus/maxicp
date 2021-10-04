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
import org.maxicp.cp.engine.core.CPBoolVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.exception.NotImplementedException;
import org.maxicp.util.NotImplementedExceptionAssume;
import org.junit.Test;
import org.maxicp.BranchingScheme;
import org.maxicp.cp.CPFactory;

import static org.junit.Assert.*;

public class IsOrTest extends CPSolverTest {

    @Test
    public void isOr1() {
        try {

            CPSolver cp = solverFactory.get();
            CPBoolVar[] x = new CPBoolVar[]{CPFactory.makeBoolVar(cp), CPFactory.makeBoolVar(cp), CPFactory.makeBoolVar(cp), CPFactory.makeBoolVar(cp)};
            CPBoolVar b = CPFactory.makeBoolVar(cp);
            cp.post(new IsOr(b, x));

            for (CPBoolVar xi : x) {
                assertTrue(!xi.isBound());
            }

            cp.getStateManager().saveState();
            cp.post(CPFactory.equal(x[1], 0));
            cp.post(CPFactory.equal(x[2], 0));
            cp.post(CPFactory.equal(x[3], 0));
            assertTrue(!b.isBound());
            cp.post(CPFactory.equal(x[0], 0));
            assertTrue(b.isFalse());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(CPFactory.equal(x[1], 0));
            cp.post(CPFactory.equal(x[2], 1));
            assertTrue(b.isTrue());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(CPFactory.equal(b, 1));
            cp.post(CPFactory.equal(x[1], 0));
            cp.post(CPFactory.equal(x[2], 0));
            assertTrue(!x[0].isBound());
            cp.post(CPFactory.equal(x[3], 0));
            assertTrue(x[0].isTrue());
            cp.getStateManager().restoreState();


            cp.getStateManager().saveState();
            cp.post(CPFactory.equal(b, 0));
            assertTrue(x[0].isFalse());
            assertTrue(x[1].isFalse());
            assertTrue(x[2].isFalse());
            assertTrue(x[3].isFalse());
            cp.getStateManager().restoreState();


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @Test
    public void isOr2() {
        try {
            CPSolver cp = solverFactory.get();
            CPBoolVar[] x = new CPBoolVar[]{CPFactory.makeBoolVar(cp), CPFactory.makeBoolVar(cp), CPFactory.makeBoolVar(cp), CPFactory.makeBoolVar(cp)};
            CPBoolVar b = CPFactory.makeBoolVar(cp);
            cp.post(new IsOr(b, x));

            DFSearch dfs = CPFactory.makeDfs(cp, BranchingScheme.firstFail(x));

            dfs.onSolution(() -> {
                        int nTrue = 0;
                        for (CPBoolVar xi : x) {
                            if (xi.isTrue()) nTrue++;
                        }
                        assertTrue((nTrue > 0 && b.isTrue()) || (nTrue == 0 && b.isFalse()));
                    }
            );

            SearchStatistics stats = dfs.solve();
            assertEquals(16, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }
}
