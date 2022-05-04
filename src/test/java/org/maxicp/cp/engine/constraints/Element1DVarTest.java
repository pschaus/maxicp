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
import java.util.HashSet;

import static org.maxicp.cp.CPFactory.makeIntVar;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Element1DVarTest extends CPSolverTest {

    private static CPIntVar makeIVar(CPSolver cp, Integer... values) {
        return CPFactory.makeIntVar(cp, new HashSet<>(Arrays.asList(values)));
    }

    @Test
    public void element1dVarTest1() {

        try {

            CPSolver cp = solverFactory.get();
            CPIntVar y = CPFactory.makeIntVar(cp, -3, 10);
            CPIntVar z = CPFactory.makeIntVar(cp, 2, 40);

            CPIntVar[] T = new CPIntVar[]{CPFactory.makeIntVar(cp, 9, 9), CPFactory.makeIntVar(cp, 8, 8), CPFactory.makeIntVar(cp, 7, 7), CPFactory.makeIntVar(cp, 5, 5), CPFactory.makeIntVar(cp, 6, 6)};

            cp.post(new Element1DVar(T, y, z));

            assertEquals(0, y.min());
            assertEquals(4, y.max());


            assertEquals(5, z.min());
            assertEquals(9, z.max());

            z.removeAbove(7);
            cp.fixPoint();

            assertEquals(2, y.min());


            y.remove(3);
            cp.fixPoint();

            assertEquals(7, z.max());
            assertEquals(6, z.min());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void element1dVarTest2() {

        try {

            CPSolver cp = solverFactory.get();
            CPIntVar y = CPFactory.makeIntVar(cp, -3, 10);
            CPIntVar z = CPFactory.makeIntVar(cp, -4, 40);

            CPIntVar[] T = new CPIntVar[]{CPFactory.makeIntVar(cp, 1, 2),
                    CPFactory.makeIntVar(cp, 3, 4),
                    CPFactory.makeIntVar(cp, 5, 6),
                    CPFactory.makeIntVar(cp, 7, 8),
                    CPFactory.makeIntVar(cp, 9, 10)};

            cp.post(new Element1DVar(T, y, z));

            assertEquals(0, y.min());
            assertEquals(4, y.max());

            assertEquals(1, z.min());
            assertEquals(10, z.max());

            y.removeAbove(2);
            cp.fixPoint();

            assertEquals(6, z.max());

            y.fix(2);
            cp.fixPoint();

            assertEquals(5, z.min());
            assertEquals(6, z.max());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void element1dVarTest3() {

        try {

            CPSolver cp = solverFactory.get();
            CPIntVar y = CPFactory.makeIntVar(cp, -3, 10);
            CPIntVar z = CPFactory.makeIntVar(cp, -20, 40);

            CPIntVar[] T = new CPIntVar[]{CPFactory.makeIntVar(cp, 9, 9), CPFactory.makeIntVar(cp, 8, 8), CPFactory.makeIntVar(cp, 7, 7), CPFactory.makeIntVar(cp, 5, 5), CPFactory.makeIntVar(cp, 6, 6)};

            cp.post(new Element1DVar(T, y, z));

            DFSearch dfs = CPFactory.makeDfs(cp, BranchingScheme.firstFail(y, z));
            dfs.onSolution(() ->
                    assertEquals(T[y.min()].min(), z.min())
            );
            SearchStatistics stats = dfs.solve();

            assertEquals(5, stats.numberOfSolutions());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void element1dVarTest4() {

        try {

            CPSolver cp = solverFactory.get();
            CPIntVar x0 = makeIVar(cp, 0, 1, 5);
            CPIntVar x1 = makeIVar(cp, -5, -4, -3, -2, 0, 1, 5);
            CPIntVar x2 = makeIVar(cp, -2, 0);


            cp.post(new Element1DVar(new CPIntVar[]{x0}, x1, x2));

            assertEquals(0, x0.min());
            assertEquals(0, x1.min());
            assertEquals(0, x2.min());
            assertEquals(0, x0.max());
            assertEquals(0, x1.max());
            assertEquals(0, x2.max());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

}
