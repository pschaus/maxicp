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

import static org.junit.Assert.*;

public class AllDifferentDCTest extends CPSolverTest {

    @Test
    public void allDifferentTest1() {

        CPSolver cp = solverFactory.get();

        CPIntVar[] x = CPFactory.makeIntVarArray(cp, 5, 5);

        try {
            cp.post(new AllDifferentDC(x));
            cp.post(CPFactory.equal(x[0], 0));
            for (int i = 1; i < x.length; i++) {
                assertEquals(4, x[i].size());
                assertEquals(1, x[i].min());
            }

        } catch (InconsistencyException e) {
            assert (false);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void allDifferentTest2() {

        CPSolver cp = solverFactory.get();

        CPIntVar[] x = CPFactory.makeIntVarArray(cp, 5, 5);

        try {
            cp.post(new AllDifferentDC(x));

            SearchStatistics stats = CPFactory.makeDfs(cp, BranchingScheme.firstFail(x)).solve();
            assertEquals(120, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            assert (false);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    private static CPIntVar makeIVar(CPSolver cp, Integer... values) {
        return CPFactory.makeIntVar(cp, new HashSet<>(Arrays.asList(values)));
    }


    @Test
    public void allDifferentTest3() {
        try {
            CPSolver cp = solverFactory.get();
            CPIntVar[] x = new CPIntVar[]{
                    makeIVar(cp, 1, 2),
                    makeIVar(cp, 1, 2),
                    makeIVar(cp, 1, 2, 3, 4)};
            int[] matching = new int[x.length];

            cp.post(new AllDifferentDC(x));

            assertEquals(x[2].min(), 3);
            assertEquals(x[2].size(), 2);

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @Test
    public void allDifferentTest5() {
        try {
            CPSolver cp = solverFactory.get();
            CPIntVar[] x = new CPIntVar[]{
                    makeIVar(cp, 1, 2, 3, 4, 5),
                    makeIVar(cp, 2),
                    makeIVar(cp, 1, 2, 3, 4, 5),
                    makeIVar(cp, 1),
                    makeIVar(cp, 1, 2, 3, 4, 5, 6),
                    makeIVar(cp, 6, 7, 8),
                    makeIVar(cp, 3),
                    makeIVar(cp, 6, 7, 8, 9),
                    makeIVar(cp, 6, 7, 8)};
            int[] matching = new int[x.length];

            cp.post(new AllDifferentDC(x));

            assertEquals(x[0].size(), 2);
            assertEquals(x[2].size(), 2);
            assertEquals(x[4].min(), 6);
            assertEquals(x[7].min(), 9);
            assertEquals(x[8].min(), 7);
            assertEquals(x[8].max(), 8);

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void allDifferentTest6() {
        try {
            CPSolver cp = solverFactory.get();
            CPIntVar[] x = new CPIntVar[]{
                    makeIVar(cp, 1, 2, 3, 4, 5),
                    makeIVar(cp, 2, 7),
                    makeIVar(cp, 1, 2, 3, 4, 5),
                    makeIVar(cp, 1, 3),
                    makeIVar(cp, 1, 2, 3, 4, 5, 6),
                    makeIVar(cp, 6, 7, 8),
                    makeIVar(cp, 3, 4, 5),
                    makeIVar(cp, 6, 7, 8, 9),
                    makeIVar(cp, 6, 7, 8)};
            int[] matching = new int[x.length];

            cp.post(new AllDifferentDC(x));

            DFSearch dfs = CPFactory.makeDfs(cp, () -> {
                CPIntVar xs = BranchingScheme.selectMin(x,
                        xi -> xi.size() > 1,
                        xi -> -xi.size());
                if (xs == null)
                    return BranchingScheme.EMPTY;
                else {
                    int v = xs.min();
                    return BranchingScheme.branch(
                            () -> {
                                cp.post(CPFactory.equal(xs, v));
                            },
                            () -> {
                                cp.post(CPFactory.notEqual(xs, v));
                            });
                }
            });

            SearchStatistics stats = dfs.solve();
            // GAC filter with a single constraint should have no fail
            assertEquals(0, stats.numberOfFailures());
            assertEquals(80, stats.numberOfSolutions());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void allDifferentTest7() {
        try {
            CPSolver cp = solverFactory.get();
            CPIntVar[] x = new CPIntVar[]{
                    makeIVar(cp, 3, 4),
                    makeIVar(cp, 1),
                    makeIVar(cp, 3, 4),
                    makeIVar(cp, 0),
                    makeIVar(cp, 3, 4, 5),
                    makeIVar(cp, 5, 6, 7),
                    makeIVar(cp, 2, 9, 10),
                    makeIVar(cp, 5, 6, 7, 8),
                    makeIVar(cp, 5, 6, 7)};
            int[] matching = new int[x.length];

            cp.post(new AllDifferentDC(x));

            assertTrue(!x[4].contains(3));
            assertTrue(!x[4].contains(4));
            assertTrue(!x[5].contains(5));
            assertTrue(!x[7].contains(5));
            assertTrue(!x[7].contains(6));
            assertTrue(!x[8].contains(5));
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void allDifferentTest8() {
        try {
            CPSolver cp = solverFactory.get();
            CPIntVar[] x = new CPIntVar[]{
                    makeIVar(cp, 0,2,3,5),
                    makeIVar(cp, 4),
                    makeIVar(cp, -1,1),
                    makeIVar(cp, -4,-2,0,2,3),
                    makeIVar(cp, -1)};
            int[] matching = new int[x.length];

            cp.post(new AllDifferentDC(x));

            assertTrue(!x[2].contains(-1));

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }




}
