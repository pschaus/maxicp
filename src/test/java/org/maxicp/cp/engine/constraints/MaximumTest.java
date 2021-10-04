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
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.exception.NotImplementedException;
import org.maxicp.util.NotImplementedExceptionAssume;
import org.junit.Test;
import org.maxicp.BranchingScheme;
import org.maxicp.cp.CPFactory;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MaximumTest extends CPSolverTest {

    @Test
    public void maximumTest1() {

        try {

            CPSolver cp = solverFactory.get();
            CPIntVar[] x = CPFactory.makeIntVarArray(cp, 3, 10);
            CPIntVar y = CPFactory.makeIntVar(cp, -5, 20);
            cp.post(new Maximum(x, y));

            assertEquals(9, y.max());
            assertEquals(0, y.min());

            y.removeAbove(8);
            cp.fixPoint();

            assertEquals(8, x[0].max());
            assertEquals(8, x[1].max());
            assertEquals(8, x[2].max());

            y.removeBelow(5);
            x[0].removeAbove(2);
            x[1].removeBelow(6);
            x[2].removeBelow(6);
            cp.fixPoint();

            assertEquals(8, y.max());
            assertEquals(6, y.min());

            y.removeBelow(7);
            x[1].removeAbove(6);
            cp.fixPoint();
            // x0 = 0..2
            // x1 = 6
            // x2 = 6..8
            // y = 7..8
            assertEquals(7, x[2].min());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void maximumTest2() {

        try {

            CPSolver cp = solverFactory.get();
            CPIntVar x1 = CPFactory.makeIntVar(cp, 0, 0);
            CPIntVar x2 = CPFactory.makeIntVar(cp, 1, 1);
            CPIntVar x3 = CPFactory.makeIntVar(cp, 2, 2);
            CPIntVar y = CPFactory.maximum(x1, x2, x3);


            assertEquals(2, y.max());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void maximumTest3() {

        try {

            CPSolver cp = solverFactory.get();
            CPIntVar x1 = CPFactory.makeIntVar(cp, 0, 10);
            CPIntVar x2 = CPFactory.makeIntVar(cp, 0, 10);
            CPIntVar x3 = CPFactory.makeIntVar(cp, -5, 50);
            CPIntVar y = CPFactory.maximum(x1, x2, x3);

            y.removeAbove(5);
            cp.fixPoint();

            assertEquals(5, x1.max());
            assertEquals(5, x2.max());
            assertEquals(5, x3.max());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void maximumTest4() {
        try {
            try {
                CPSolver cp = solverFactory.get();
                CPIntVar[] x = CPFactory.makeIntVarArray(cp, 4, 5);
                CPIntVar y = CPFactory.makeIntVar(cp, -5, 20);

                CPIntVar[] allIntVars = new CPIntVar[x.length+1];
                System.arraycopy(x, 0, allIntVars, 0, x.length);
                allIntVars[x.length] = y;

                DFSearch dfs = CPFactory.makeDfs(cp, BranchingScheme.firstFail(allIntVars));

                cp.post(new Maximum(x, y));
                // 5*5*5*5 // 625

                dfs.onSolution(() -> {
                    int max = Arrays.stream(x).mapToInt(xi -> xi.max()).max().getAsInt();
                    assertEquals(y.min(), max);
                    assertEquals(y.max(), max);
                });

                SearchStatistics stats = dfs.solve();

                assertEquals(625, stats.numberOfSolutions());

            } catch (InconsistencyException e) {
                fail("should not fail");
            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }
}
