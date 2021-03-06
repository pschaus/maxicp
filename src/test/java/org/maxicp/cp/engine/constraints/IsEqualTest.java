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
import org.maxicp.cp.engine.core.CPIntVar;
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


public class IsEqualTest extends CPSolverTest {

    @Test
    public void test1() {
        try {

            CPSolver cp = solverFactory.get();
            CPIntVar x = CPFactory.makeIntVar(cp, -4, 7);

            CPBoolVar b = CPFactory.isEqual(x, -2);

            DFSearch search = CPFactory.makeDfs(cp, BranchingScheme.firstFail(x));

            SearchStatistics stats = search.solve();

            search.onSolution(() ->
                    assertEquals(-2 == x.min(), b.isTrue())
            );

            assertEquals(12, stats.numberOfSolutions());


        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void test2() {
        try {

            CPSolver cp = solverFactory.get();
            CPIntVar x = CPFactory.makeIntVar(cp, -4, 7);

            CPBoolVar b = CPFactory.isEqual(x, -2);

            cp.getStateManager().saveState();
            cp.post(CPFactory.equal(b, 1));
            assertEquals(-2, x.min());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(CPFactory.equal(b, 0));
            assertFalse(x.contains(-2));
            cp.getStateManager().restoreState();

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void test3() {

        try {
            CPSolver cp = solverFactory.get();
            CPIntVar x = CPFactory.makeIntVar(cp, -4, 7);
            cp.post(CPFactory.equal(x, -2));

            {
                CPBoolVar b = CPFactory.makeBoolVar(cp);
                cp.post(new IsEqual(b, x, -2));
                assertTrue(b.isTrue());
            }
            {
                CPBoolVar b = CPFactory.makeBoolVar(cp);
                cp.post(new IsEqual(b, x, -3));
                assertTrue(b.isFalse());
            }

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void test4() {
        try {

            CPSolver cp = solverFactory.get();
            CPIntVar x = CPFactory.makeIntVar(cp, -4, 7);
            CPBoolVar b = CPFactory.makeBoolVar(cp);

            cp.getStateManager().saveState();
            cp.post(CPFactory.equal(b, 1));
            cp.post(new IsEqual(b, x, -2));
            assertEquals(-2, x.min());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(CPFactory.equal(b, 0));
            cp.post(new IsEqual(b, x, -2));
            assertFalse(x.contains(-2));
            cp.getStateManager().restoreState();


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
